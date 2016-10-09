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

package pl.baczkowicz.mqttspy.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.logger.MqttMessageLogParser;
import pl.baczkowicz.mqttspy.logger.MqttMessageLogParserUtils;
import pl.baczkowicz.mqttspy.messages.BaseMqttMessage;
import pl.baczkowicz.spy.audit.AuditReplay;
import pl.baczkowicz.spy.exceptions.SpyException;
import pl.baczkowicz.spy.exceptions.XMLException;
import pl.baczkowicz.spy.messages.BaseMessage;

public class MqttAuditReplay extends AuditReplay
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(MqttAuditReplay.class);
	
	private boolean messageReadAhead = false;
	
	private final MqttMessageLogParser parser;	
	
	private BaseMqttMessage message;

	private long messageIndex = 0;
	
	public MqttAuditReplay() throws SpyException
	{
		try
		{
			parser = new MqttMessageLogParser();
		}
		catch (XMLException e)
		{
			throw new SpyException("Cannot initiate the audit replay", e);
		}	
	}
	
	private void processNextMessage()
	{
		try
		{
			messageIndex++;
			
			final String next = auditReader.getNextLine();
			
			message = MqttMessageLogParserUtils.convertToBaseMqttMessage(parser.parse(next));
			
			messageReadAhead = true;
		}
		catch (Exception e)
		{
			logger.error("Cannot parse message number {}", messageIndex, e);
		}
	}
	
	@Override
	protected long getMessageTime()
	{
		if (!messageReadAhead)
		{
			processNextMessage();
		}
		
		return message.getDate().getTime();
	}

	@Override
	protected BaseMessage getMessage()
	{
		if (!messageReadAhead)
		{
			processNextMessage();
		}
		
		// Consider it as processed
		messageReadAhead = false;
		
		return message;
	}
}
