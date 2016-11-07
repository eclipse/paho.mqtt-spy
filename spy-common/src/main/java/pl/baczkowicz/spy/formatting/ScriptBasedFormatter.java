/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
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

package pl.baczkowicz.spy.formatting;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.common.generated.FormatterDetails;
import pl.baczkowicz.spy.messages.FormattedMessage;
import pl.baczkowicz.spy.scripts.BaseScriptManagerInterface;
import pl.baczkowicz.spy.scripts.BaseScriptManager;
import pl.baczkowicz.spy.scripts.Script;
import pl.baczkowicz.spy.utils.ConversionUtils;
import pl.baczkowicz.spy.utils.TimeUtils;

public class ScriptBasedFormatter
{
	public static final String FORMAT_FUNCTION_NAME = "format";
	
	public static final String PRETTY_FUNCTION_NAME = "pretty";

	final static Logger logger = LoggerFactory.getLogger(ScriptBasedFormatter.class);	
	
	private BaseScriptManagerInterface scriptManager;
	
	private Map<FormatterDetails, Script> formattingScripts = new HashMap<>();
	
	private Map<FormatterDetails, Boolean> prettyFormattingAvailable = new HashMap<>();
		
	public ScriptBasedFormatter(final BaseScriptManagerInterface scriptManager)
	{
		this.scriptManager = scriptManager;
	}

	public Script getScript(final FormatterDetails formatter) throws ScriptException
	{
		Script script = formattingScripts.get(formatter);
		
		if (script == null)
		{
			addFormatter(formatter);
			script = formattingScripts.get(formatter);
		}
		
		return script;
	}
	
	public void evaluate(final Script script)
	{
		// Evaluate it
		scriptManager.runScript(script, false);
		
		// Run before / setup
		try
		{
			scriptManager.invokeFunction(script, BaseScriptManager.BEFORE_METHOD);
		}
		catch (NoSuchMethodException | ScriptException e)
		{
			logger.info("No setup method present");
		}
	}
	
	public void addFormatter(final FormatterDetails formatter) throws ScriptException
	{
		final long start = TimeUtils.getMonotonicTime();					
		
		if (FormattingUtils.isScriptBased(formatter))
		{
			final Script script = scriptManager.addInlineScript(formatter.getID(), 
					ConversionUtils.base64ToString(formatter.getFunction().get(0).getScriptExecution().getInlineScript()));
			
			// Store it for future
			formattingScripts.put(formatter, script);
			
			evaluate(script);
		}
		
		final long end = TimeUtils.getMonotonicTime();
		logger.debug("Adding formatter {} took {} ms", formatter.getName(), (end - start));
	}
	
	private String formatMessageWithFunction(final FormatterDetails formatter, final FormattedMessage message, final String functionName) 
			throws ScriptException, NoSuchMethodException
	{
		Script script = formattingScripts.get(formatter);
		
		if (script == null)
		{
			logger.debug("Formatting script not found");
			addFormatter(formatter);
			script = formattingScripts.get(formatter);
		}
		
		logger.trace("Setting variable {} on {} with {}", BaseScriptManager.RECEIVED_MESSAGE_PARAMETER, script, message);
		scriptManager.setVariable(script, BaseScriptManager.RECEIVED_MESSAGE_PARAMETER, message);		
	
		return (String) scriptManager.invokeFunction(script, functionName);
	}
	
	public String formatMessage(final FormatterDetails formatter, final FormattedMessage message)
	{
		try
		{
			return formatMessageWithFunction(formatter, message, FORMAT_FUNCTION_NAME);
		}
		catch (NoSuchMethodException | ScriptException e)
		{
			logger.trace("Cannot parse the message", e);
			return message.getPayload();
		}	
	}
	
	public String formatMessage(final FormatterDetails formatter, final FormattedMessage message, final boolean pretty)
	{
		if (pretty && !Boolean.FALSE.equals(prettyFormattingAvailable.get(formatter)))
		{
			logger.debug("Pretty formatting...");
			try
			{
				return formatMessageWithFunction(formatter, message, PRETTY_FUNCTION_NAME);
			}
			catch (NoSuchMethodException e)
			{
				prettyFormattingAvailable.put(formatter, Boolean.FALSE);
				return formatMessage(formatter, message);
			}
			catch (ScriptException e)
			{				
				logger.trace("Cannot parse the message", e);
				return message.getPayload();
			}	
		}
		else
		{
			return formatMessage(formatter, message);
		}		
	}
}
