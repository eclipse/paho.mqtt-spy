/***********************************************************************************
 * 
 * Copyright (c) 2014 Kamil Baczkowicz
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 *    
 * The Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * 
 *    Kamil Baczkowicz - initial API and implementation and/or initial documentation
 *    
 */
package pl.baczkowicz.spy.scripts;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executor;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.files.FileUtils;
import pl.baczkowicz.spy.scripts.events.ScriptStateChangeEvent;
import pl.baczkowicz.spy.utils.ThreadingUtils;

/**
 * This runnable implementation is responsible for running a script in its own thread.
 */
public class ScriptRunner implements Runnable
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(ScriptRunner.class);
	
	/** The associated script. */
	private final Script script;

	/** The Event Bus. */
	private IKBus eventBus;

	/** The executor. */
	private Executor executor;
	
	/** The thread running the script. */
	private Thread runningThread;

	private Object lastReturnValue;
	
	private Exception lastThrownException;

	/**
	 * Creates a ScriptRunner.
	 * 
	 * @param eventBus The event bus to be used
	 * @param script The associated script
	 * @param executor The executor to be used
	 */
	public ScriptRunner(final IKBus eventBus, final Script script, final Executor executor)
	{
		this.script = script;
		this.eventBus = eventBus;
		this.executor = executor;
	}
	
	/**
	 * Runs once or in a loop if repeat flag is set.
	 */
	public void run()
	{
		if (script.isAsynchronous())
		{
			ThreadingUtils.logThreadStarting("Script " + script.getName());
		}		
		
		script.touch();
		runningThread = Thread.currentThread();
		
		boolean firstRun = true;

		// Either a first run or repeat is on but not stopped by user
		while (firstRun || (script.isRepeat() && !ScriptRunningState.STOPPED.equals(script.getStatus())))
		{
			logger.debug("Running [{}] script: first run = {}, repeat = {}, state = {}", 
					script.getName(), firstRun, script.isRepeat(), script.getStatus());
			
			firstRun = false;
			
			changeState(ScriptRunningState.RUNNING);
			
			if (script.isAsynchronous())
			{
				new Thread(new ScriptHealthDetector(eventBus, script, executor)).start();
			}
			
			try
			{
				runScript();
			}
			catch (Exception e)
			{
				changeState(ScriptRunningState.FAILED);
				setLastThrownException(e);
				logger.error("Script execution exception", e);
				break;
			}		
			
			if (script.isRepeat())
			{
				logger.debug("Re-running script {}", script.getName());
			}
		}
		
		script.stop();
		
		if (script.isAsynchronous())
		{
			ThreadingUtils.logThreadEnding();
		}
	}
	
	/**
	 * Runs the script and checks the returned value.
	 * 
	 * @throws ScriptException Thrown when a script executor error occurs
	 * @throws IOException Thrown when cannot load the script
	 */
	private void runScript() throws ScriptException, IOException
	{
		// Clear the last returned value
		lastReturnValue = null;
		setLastThrownException(null);
		
		// Script in a file
		if (script.getScriptFile() != null)
		{
			lastReturnValue = script.getScriptEngine().eval(new InputStreamReader(FileUtils.loadFileByName(script.getScriptFile().getAbsolutePath())));
			// lastReturnValue = script.getScriptEngine().eval(new FileReader(script.getScriptFile()));
			logger.debug("Script {} returned with value {}", script.getName(), lastReturnValue);
		}
		// In-line script
		else if (script.getScriptContent() != null)
		{
			lastReturnValue = script.getScriptEngine().eval(script.getScriptContent());
			logger.debug("Inline script {} returned with value {}", script.getName(), lastReturnValue);
		}
		else
		{
			logger.warn("No script content defined for script {}", script.getName());
		}
		
		// If nothing returned, assume all good
		if (lastReturnValue == null)
		{
			changeState(ScriptRunningState.FINISHED);
		}
		// If boolean returned, check if OK
		else if (lastReturnValue instanceof Boolean)
		{
			if ((Boolean) lastReturnValue)
			{
				changeState(ScriptRunningState.FINISHED);
			}
			else
			{
				changeState(ScriptRunningState.STOPPED);
			}
		}
		// Anything else, assume all good
		else
		{
			changeState(ScriptRunningState.FINISHED);
		}
	}

	/**
	 * Gets the last returned value for the script.
	 * 
	 * @return the lastReturnValue
	 */
	public Object getLastReturnValue()
	{
		return lastReturnValue;
	}

	/**
	 * Changes the state of the script.
	 * 
	 * @param newState New state
	 */
	private void changeState(final ScriptRunningState newState)
	{
		changeState(eventBus, script.getName(), newState, script, executor);
	}
	
	/**
	 * Changes the state of the script.
	 * 
	 * @param eventBus The event bus to be used
	 * @param scriptName The script name
	 * @param newState The new state requested
	 * @param script The script itself
	 * @param executor The executor to be used
	 */
	public static void changeState(final IKBus eventBus, final String scriptName, 
			final ScriptRunningState newState, final Script script, final Executor executor)
	{		
		logger.trace("Changing [{}] script's state to [{}]", scriptName, newState);
		script.setStatus(newState);
				
		if (eventBus != null && executor != null)
		{
			executor.execute(new Runnable()
			{			
				public void run()
				{
					script.nofityChange();
					eventBus.publish(new ScriptStateChangeEvent(scriptName, newState));
					logger.trace("Notified [{}] script's state to [{}]", scriptName, newState);
				}
			});
		}
		else
		{
			script.nofityChange();
			logger.trace("Notified [{}] script's state to [{}]", scriptName, newState);
		}
	}
	
	/**
	 * Gets the runner's thread.
	 * 
	 * @return The runner's Thread object
	 */
	public Thread getThread()
	{
		return this.runningThread;
	}

	/**
	 * Gets last thrown exception or null if none.
	 * 
	 * @return the lastThrownException
	 */
	public Exception getLastThrownException()
	{
		return lastThrownException;
	}

	/**
	 * Sets last thrown exception.
	 * 
	 * @param lastThrownException the lastThrownException to set
	 */
	public void setLastThrownException(Exception lastThrownException)
	{
		this.lastThrownException = lastThrownException;
	}
}
