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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

import javax.script.ScriptEngine;

import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.utils.TimeUtils;
import pl.baczkowicz.spy.utils.tasks.StoppableTask;

/**
 * This class represents a JS script run with the Nashorn engine.
 */
public class Script extends BasicScriptProperties
{	
	/** The running state of the script. */
	private ScriptRunningState status;
	
	/** Number of messages published by the script. */
	private long messagesPublished;

	/** Timestamp of the last publication. */
	private Date lastPublished;
	
	/** The associated script file. */
	private File scriptFile;
	
	/** Definition of the in-line script. */
	private String scriptContent;
	
	/** Script engine instance. */
	private ScriptEngine scriptEngine;
	
	/** The script runner - dedicated runnable for that script. */
	private ScriptRunner scriptRunner;
	
	/** Observer of any changes to script's properties. */
	private ScriptChangeObserver observer;
	
	/** Last time the script touched or published a message. */
	private long lastTouch;	
	
	private List<StoppableTask> backgroundTasks = new ArrayList<>();

	private boolean asynchronous;

	private String rootDirectory;

	/**
	 * Creates a script.
	 */
	public Script()
	{
		// Default
	}
	
	public String getNameWithSubdirectory()
	{
		if (rootDirectory != null)
		{
			return BaseScriptManager.getScriptNameWithSubdirectory(getScriptFile(), rootDirectory);
		}
		
		return getName();
	}
	
	/**
	 * Stops any running tasks (threads).
	 */
	public void stop()
	{
		for (final StoppableTask task : backgroundTasks)
		{
			task.stop();
		}
		// messageLog.stop();
	}

	public void touch()
	{
		this.lastTouch = TimeUtils.getMonotonicTime();
	}	

	/**
	 * Returns the time of the last touch.
	 * 
	 * @return Time of the last touch (in milliseconds)
	 */
	public long getLastTouch()
	{
		return lastTouch;
	}
	
	/**
	 * Creates a script runner for the script if it doesn't exist yet.
	 * 
	 * @param eventBus The event bus to use
	 * @param executor The executor to use
	 */
	public void createScriptRunner(final IKBus eventBus, final Executor executor)
	{
		if (scriptRunner == null)
		{
			this.scriptRunner = new ScriptRunner(eventBus, this, executor);
		}
	}
	
	/**
	 * Notifies an observer a change has occurred.
	 */
	public void nofityChange()
	{
		if (observer != null)
		{
			observer.onChange();
		}
	}
	
	public void addTask(final StoppableTask task)
	{
		backgroundTasks.add(task);
	}

	// ===============================
	// === Setters and getters =======
	// ===============================
	
	public void setMessagesPublished(final long messageCount)
	{
		this.messagesPublished = messageCount;		
		nofityChange();
	}
	
	public void setLastPublished(final Date lastPublishedDate)
	{
		this.lastPublished = lastPublishedDate;
		nofityChange();
	}
	
	public void setStatus(final ScriptRunningState status)
	{
		this.status = status;
	}
	
	// TODO: replace the notifyChange with the EventBus
	public void setStatusAndNotify(final ScriptRunningState status)
	{
		setStatus(status);
		nofityChange();
	}
	
	public Date getLastPublishedDate()
	{
		return lastPublished;
	}

	public long getMessagesPublished()
	{
		return messagesPublished;
	}
	
	public File getScriptFile()
	{
		return this.scriptFile;
	}

	public ScriptEngine getScriptEngine()
	{
		return scriptEngine;
	}

	public ScriptRunningState getStatus()
	{
		return status;
	}

	public void setScriptEngine(final ScriptEngine scriptEngine)
	{
		this.scriptEngine = scriptEngine;
	}

	public void setScriptFile(final File scriptFile)
	{
		this.scriptFile = scriptFile;
	}

	public ScriptRunner getScriptRunner()
	{
		return this.scriptRunner;
	}

	/**
	 * Gets the script content.
	 * 
	 * @return the scriptContent
	 */
	public String getScriptContent()
	{
		return scriptContent;
	}

	/**
	 * Sets the script content.
	 *  
	 * @param scriptContent the scriptContent to set
	 */
	public void setScriptContent(final String scriptContent)
	{
		this.scriptContent = scriptContent;
	}
	
	public static String getScriptIdFromFile(final File file)
	{
		return file.getAbsolutePath();
	}
	
	public String getScriptId()
	{
		if (scriptFile == null)
		{
			return null;
		}
		
		return getScriptIdFromFile(scriptFile);
	}

	/**
	 * Sets the observer of the script properties.
	 * 
	 * @param observer the observer to set
	 */
	public void setObserver(ScriptChangeObserver observer)
	{
		this.observer = observer;
	}

	public void setAsynchronous(boolean asynchronous)
	{
		this.asynchronous = asynchronous;
	}
	
	public boolean isAsynchronous()
	{
		return this.asynchronous;
	}


	public void setRootDirectory(final String rootDirectory)
	{
		this.rootDirectory = rootDirectory;		
	}
}
