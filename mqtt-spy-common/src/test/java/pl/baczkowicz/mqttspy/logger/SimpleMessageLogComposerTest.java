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
package pl.baczkowicz.mqttspy.logger;

import static org.junit.Assert.assertEquals;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;

import pl.baczkowicz.mqttspy.common.generated.MessageLog;
import pl.baczkowicz.mqttspy.common.generated.MessageLogEnum;
import pl.baczkowicz.mqttspy.connectivity.BaseMqttConnection;
import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;

/**
 * Tests for the SimpleMessageLogComposer.
 */
public class SimpleMessageLogComposerTest
{
	private static final String SAMPLE_PAYLOAD = "payload";
	
	private Mockery context = new Mockery()
	{
		{
			setImposteriser(ClassImposteriser.INSTANCE);
		}
	};
	
	/**
	 * Test method for {@link pl.baczkowicz.mqttspy.logger.MqttMessageLogParserUtils#createReceivedMessageLog(pl.baczkowicz.mqttspy.messages.BaseMqttMessageWithSubscriptions, pl.baczkowicz.mqttspy.common.generated.MessageLog)}.
	 */
	@Test
	public void testCreateReceivedMessageLogWithPlainPayload()
	{
		final MqttMessage mqttMessage = new MqttMessage();
		mqttMessage.setPayload(SAMPLE_PAYLOAD.getBytes());
		
		final BaseMqttConnection connection = context.mock(BaseMqttConnection.class); 
		final FormattedMqttMessage message = new FormattedMqttMessage(1, "topic", mqttMessage, connection);
		
		final MessageLog messageLog = new MessageLog(MessageLogEnum.XML_WITH_PLAIN_PAYLOAD, null, false, false, false, false, false);
		final long timestamp = message.getDate().getTime();
		
		final String loggedMessage = "<MqttMessage id=\"1\" timestamp=\"" + timestamp + "\" topic=\"topic\">" + SAMPLE_PAYLOAD + "</MqttMessage>";
		assertEquals(loggedMessage, SimpleMqttMessageLogComposer.createReceivedMessageLog(message, messageLog));
	}

	/**
	 * Test method for {@link pl.baczkowicz.mqttspy.logger.MqttMessageLogParserUtils#createReceivedMessageLog(pl.baczkowicz.mqttspy.messages.BaseMqttMessageWithSubscriptions, pl.baczkowicz.mqttspy.common.generated.MessageLog)}.
	 * 
	 * This is to cover Issue 18.
	 */
	@Test
	public void testCreateReceivedMessageLogWithEncodedPayload()
	{
		final MqttMessage mqttMessage = new MqttMessage();
		mqttMessage.setPayload("payload".getBytes());
		
		final BaseMqttConnection connection = context.mock(BaseMqttConnection.class); 
		final FormattedMqttMessage message = new FormattedMqttMessage(1, "topic", mqttMessage, connection);
		
		final MessageLog messageLog = new MessageLog(MessageLogEnum.XML_WITH_ENCODED_PAYLOAD, null, false, false, false, false, false);
		final long timestamp = message.getDate().getTime();
		
		final String loggedMessage = "<MqttMessage id=\"1\" timestamp=\"" + timestamp + "\" topic=\"topic\" encoded=\"true\">" + 
				Base64.encodeBase64String(SAMPLE_PAYLOAD.getBytes()) + "</MqttMessage>";
		
		assertEquals(loggedMessage, SimpleMqttMessageLogComposer.createReceivedMessageLog(message, messageLog));
	}
}
