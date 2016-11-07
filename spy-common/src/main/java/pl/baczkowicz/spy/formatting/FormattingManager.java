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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.common.generated.FormatterDetails;
import pl.baczkowicz.spy.common.generated.FormatterFunction;
import pl.baczkowicz.spy.common.generated.ScriptExecutionDetails;
import pl.baczkowicz.spy.files.FileUtils;
import pl.baczkowicz.spy.messages.FormattedMessage;
import pl.baczkowicz.spy.scripts.BaseScriptManagerInterface;

public class FormattingManager
{
	final static Logger logger = LoggerFactory.getLogger(FormattingManager.class);
	
	private ScriptBasedFormatter scriptFormatter;
	
	public FormattingManager(final BaseScriptManagerInterface scriptManager)
	{
		this.scriptFormatter = new ScriptBasedFormatter(scriptManager);
	}
	
	public void initialiseFormatter(final FormatterDetails formatter)
	{
		if (formatter == null)
		{
			return;
		}
		
		try
		{		
			scriptFormatter.addFormatter(formatter);
		}
		catch (ScriptException e)
		{
			logger.error("Couldn't load the formatter");
		}
	}
	
	public void formatMessage(final FormattedMessage message, final FormatterDetails formatter)
	{
		if (formatter == null)
		{
			message.setFormattedPayload(message.getPayload());
		}		
		else if (!formatter.equals(message.getLastUsedFormatter()))
		{
			message.setLastUsedFormatter(formatter);
			
			if (FormattingUtils.isScriptBased(formatter))
			{
				// logger.debug("Formatting using {}", formatter.getName());
				message.setFormattedPayload(scriptFormatter.formatMessage(formatter, message));
				message.setPrettyPayload(scriptFormatter.formatMessage(formatter, message, true));
			}
			else
			{
				// Use the raw payload to make sure any formatting/encoding that is applied is correct
				message.setFormattedPayload(FormattingUtils.checkAndFormatText(formatter, message.getRawPayload()));
				message.setPrettyPayload(message.getFormattedPayload());
			}
		}
	}

	public static List<FormatterDetails> createDefaultScriptFormatters()
	{
		final List<FormatterDetails> defaultScriptFormatters = new ArrayList<>();
		
		try
		{
			// JSON
			final String prettyJsonScript = FileUtils.loadFileByNameBase64Encoded("/formatters/prettyJson.js");
			
			final FormatterDetails prettyJson = new FormatterDetails(
					"Pretty JSON",
					FormattingUtils.DEFAULT_PREFIX + FormattingUtils.SCRIPT_PREFIX + "-pretty-json",
					"Takes a JSON string and does pretty formatting with indentation.",
					Arrays.asList(new FormatterFunction(null, null, null, null,
							null, new ScriptExecutionDetails(prettyJsonScript))));
			
			defaultScriptFormatters.add(prettyJson);
			
			// XML
			final String prettyXmlScript = FileUtils.loadFileByNameBase64Encoded("/formatters/prettyXml.js");
			
			final FormatterDetails prettyXml = new FormatterDetails(
					"Pretty XML",
					FormattingUtils.DEFAULT_PREFIX + FormattingUtils.SCRIPT_PREFIX + "-pretty-xml",
					"Takes an XML string and does pretty formatting with indentation.",
					Arrays.asList(new FormatterFunction(null, null, null, null,
							null, new ScriptExecutionDetails(prettyXmlScript))));
			
			defaultScriptFormatters.add(prettyXml);
			
			// Eclipse Kura
			final String kuraScript = FileUtils.loadFileByNameBase64Encoded("/formatters/eclipseKura.js");
			
			final FormatterDetails kura = new FormatterDetails(
					"Eclipse Kura",
					FormattingUtils.DEFAULT_PREFIX + FormattingUtils.SCRIPT_PREFIX + "-eclipse-kura",
					"Decodes the Eclipse Kura Protocol Buffer format and converts it to pretty JSON.",
					Arrays.asList(new FormatterFunction(null, null, null, null,
							null, new ScriptExecutionDetails(kuraScript))));
			
			defaultScriptFormatters.add(kura);
		}
		catch (IOException e)
		{
			logger.error("Cannot read file", e);
		}
		
		return defaultScriptFormatters;
	}
}
