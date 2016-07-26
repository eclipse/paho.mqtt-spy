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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.concurrent.Executor;

import javax.script.Bindings;
import javax.script.ScriptContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the interface between a script and the rest of the application.
 */
public class ScriptIO implements IScriptIO
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(ScriptIO.class);
	
	/** Script properties. */
	protected Script script;
	
	// TODO: could possibly replace that with a local variable
	/** The number of messages published by the script. */
	protected int publishedMessages;	

	/** Task executor. */
	protected Executor executor;

	protected String scriptName = "n/a";
	
	/**
	 * Creates the ScriptIO.
	 * 
	 * @param script The script itself
	 * @param executor Task executor
	 */
	public ScriptIO(final Script script, final Executor executor)
	{
		this.script = script;
		this.executor = executor;
		
		if (script != null)
		{
			scriptName = script.getName();
		}
	}

	@Override
	public void touch()
	{
		script.touch();
	}
	
	@Override
	public void setScriptTimeout(final long customTimeout)
	{
		script.setScriptTimeout(customTimeout);
		logger.debug("Timeout for script {} changed to {}", scriptName, customTimeout);
	}
	
	@Override
	@Deprecated
	public boolean instantiate(final String className)
	{
		try
		{
			final Bindings bindings = script.getScriptEngine().getBindings(ScriptContext.ENGINE_SCOPE);
			bindings.put(className.replace(".", "_"), Class.forName(className).newInstance());
			script.getScriptEngine().setBindings(bindings, ScriptContext.ENGINE_SCOPE);
			return true;
		}
		catch (Exception e)
		{
			logger.error("Cannot instantiate class " + className, e);
			return false;
		}
	}
	
	@Override
	public String execute(final String command) throws IOException, InterruptedException
	{
		Runtime rt = Runtime.getRuntime();
		Process p = rt.exec(command);
		p.waitFor();
		BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = null;

		try
		{
			final StringBuffer sb = new StringBuffer();
			while ((line = input.readLine()) != null)
			{
				sb.append(line);
			}
			return sb.toString();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}			
		
		return null;
	}
	
	protected void updatePublished()
	{
		publishedMessages++;
		
		if (executor != null)
		{
			executor.execute(new Runnable()
			{			
				public void run()
				{
					script.setLastPublished(new Date());
					script.setMessagesPublished(publishedMessages);				
				}
			});
		}
		else if (script != null)
		{
			script.setLastPublished(new Date());
			script.setMessagesPublished(publishedMessages);		
		}
	}
}
