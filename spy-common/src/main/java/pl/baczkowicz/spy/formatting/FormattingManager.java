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

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.common.generated.FormatterDetails;
import pl.baczkowicz.spy.messages.FormattedMessage;
import pl.baczkowicz.spy.scripts.BaseScriptManager;

public class FormattingManager
{
	final static Logger logger = LoggerFactory.getLogger(FormattingManager.class);
	
	private ScriptBasedFormatter scriptFormatter;
	
	public FormattingManager(final BaseScriptManager scriptManager)
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
				message.setFormattedPayload(scriptFormatter.formatMessage(formatter, message));
			}
			else
			{
				// Use the raw payload to make sure any formatting/encoding that is applied is correct
				message.setFormattedPayload(FormattingUtils.checkAndFormatText(formatter, message.getRawBinaryPayload()));
			}
		}
	}
}
