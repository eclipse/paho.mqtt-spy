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

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.utils.ThreadingUtils;
import pl.baczkowicz.spy.utils.TimeUtils;

/**
 * This class is used for detecting stalled/frozen scripts.
 */
public class ScriptHealthDetector implements Runnable
{
	/** Default script/thread timeout - if not touched, the script will be reported as frozen. */
	public final static int DEFAULT_THREAD_TIMEOUT = 5000;
	
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(ScriptHealthDetector.class);
	
	/** The script. */
	private final Script script;

	/** Event bus. */
	private IKBus eventBus;

	/** Executor. */
	private Executor executor;

	/**
	 * Creates a ScriptHealthDetector.
	 * 
	 * @param eventBus The event bus to use
	 * @param script The script
	 * @param executor The executor to use
	 */
	public ScriptHealthDetector(final IKBus eventBus, final Script script, final Executor executor)
	{
		this.script = script;
		this.eventBus = eventBus;
		this.executor = executor;
	}
	
	/**
	 * This is expected to run as long as a script is running.
	 */
	public void run()
	{
		ThreadingUtils.logThreadStarting("Script Health Detector");
		
		while (script.getStatus().equals(ScriptRunningState.RUNNING))
		{
			if (logger.isTraceEnabled())
			{
				logger.trace("Checking script {} for responsiveness, last touch = {}, timeout = {}, current time = {}", script.getName(), 
					script.getLastTouch(), script.getScriptTimeout(), TimeUtils.getMonotonicTime());
			}
			
			if (script.getLastTouch() + script.getScriptTimeout() < TimeUtils.getMonotonicTime())
			{
				logger.warn("Script {} detected as frozen, last touch = {}, current time = {}", script.getName(), 
						script.getLastTouch(), TimeUtils.getMonotonicTime());
				ScriptRunner.changeState(eventBus, script.getName(), ScriptRunningState.FROZEN, script, executor);
			}
			
			if (ThreadingUtils.sleep(1000))			
			{
				break;
			}
		}
		// TODO: what if it freezes for the second time in the same run?
		
		ThreadingUtils.logThreadEnding();
	}
}
