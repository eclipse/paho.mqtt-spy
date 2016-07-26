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

import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.MessageLog;
import pl.baczkowicz.mqttspy.common.generated.MessageLogEnum;
import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.spy.utils.ConversionUtils;

/**
 * Simple message log composer (string buffer based, no JAXB).
 */
public class SimpleMqttMessageLogComposer
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(SimpleMqttMessageLogComposer.class);
	
	/** Pattern that is used to decide whether a string should be wrapped in CDATA. */
    private static final Pattern XML_CHARS = Pattern.compile("[&<>]");
    
    /**
     * Creates a single message log entry for the supplied message object
     * 
     * @param message The message to be logged
     * @param messageLogOptions Logging options
     * 
     * @return The log message as string
     */
	public static String createReceivedMessageLog(final FormattedMqttMessage message, final MessageLog messageLogOptions)
	{
		final StringBuffer logMessage = new StringBuffer();
		logMessage.append("<MqttMessage");
		
		appendAttribute(logMessage, "id", String.valueOf(message.getId()));
		appendAttribute(logMessage, "timestamp", String.valueOf(message.getDate().getTime()));				
		appendAttribute(logMessage, "topic", message.getTopic());		
		
		// Quality of service
		if (messageLogOptions.isLogQos())
		{
			appendAttribute(logMessage, "qos", String.valueOf(message.getQoS()));
		}
		
		// Retained flag
		if (messageLogOptions.isLogRetained())
		{
			appendAttribute(logMessage, "retained", String.valueOf(message.isRetained()));
		}
		
		// Connection info
		if (messageLogOptions.isLogConnection())
		{
			appendAttribute(logMessage, "connection", message.getConnection().getMqttConnectionDetails().getName());
		}
		
		// Subscription (logs the first one only)
		if (messageLogOptions.isLogSubscription() && message.getMatchingSubscriptionTopics() != null && message.getMatchingSubscriptionTopics().size() > 0)
		{
			// Log the first matching subscription
			appendAttribute(logMessage, "subscription", message.getMatchingSubscriptionTopics().get(0));
		}
		
		populatePayload(logMessage, message, messageLogOptions);
		
		logMessage.append("</MqttMessage>");
		
		return logMessage.toString();
	}
	
	/**
	 * Populates the message log with the message's payload.
	 * 
	 * @param logMessage The message log to be populated
	 * @param message The message to be logged 
     * @param messageLogOptions Logging options
	 */
	private static void populatePayload(final StringBuffer logMessage, final FormattedMqttMessage message, final MessageLog messageLogOptions)
	{
		boolean encoded = MessageLogEnum.XML_WITH_ENCODED_PAYLOAD.equals(messageLogOptions.getValue());
		final String payload = new String(message.getPayload());
		
		// If the payload contains a new line character, encode it, as it would make the message log invalid (no new lines allowed for a message)
		if (!encoded && 
				(payload.contains(ConversionUtils.LINE_SEPARATOR_LINUX) 
						|| payload.contains(ConversionUtils.LINE_SEPARATOR_MAC)
						|| payload.contains(ConversionUtils.LINE_SEPARATOR_WIN)))
		{
			logger.debug("Message on topic {} contains a new line separator, so it needs to be encoded", message.getTopic());
			encoded = true;
		}
		
		if (encoded)
		{
			appendAttribute(logMessage, "encoded", "true");
		}
		
		logMessage.append(">");
		
		if (encoded)
		{
			appendValue(logMessage, Base64.encodeBase64String(message.getRawMessage().getPayload()));
		}
		else
		{			
			final boolean useCData = XML_CHARS.matcher(payload).find();
			if (useCData)
			{
				appendValue(logMessage, "<![CDATA[" + payload + "]]>");
			}
			else
			{
				appendValue(logMessage, payload);
			}
		}
	}
	
	/**
	 * Appends a single attribute to the log.
	 * 
	 * @param logMessage The message log to be populated
	 * @param attributeName The attribute name
	 * @param attributeValue The attribute value
	 */
	public static void appendAttribute(final StringBuffer logMessage, final String attributeName, final String attributeValue)
	{
		logMessage.append(" " + attributeName + "=\"" + attributeValue + "\"");
	}
	
	/**
	 * Appends an element value to the log.
	 * 
	 * @param logMessage The message log to be populated
	 * @param value The value to be appended
	 */
	public static void appendValue(final StringBuffer logMessage, final String value)
	{
		logMessage.append(value);
	}
}
