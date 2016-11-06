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
package pl.baczkowicz.mqttspy.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttTopic;

import pl.baczkowicz.mqttspy.common.generated.ProtocolVersionEnum;
import pl.baczkowicz.spy.exceptions.SpyException;

/**
 * MQTT utilities.
 */
public class MqttUtils
{
	/** TCP prefix. */
	public final static String TCP_PREFIX = "tcp://";
	
	/** SSL prefix. */
	public final static String SSL_PREFIX = "ssl://";
	
	/** WebSocket prefix. */
	public final static String WS_PREFIX = "ws://";
	
	/** Secure WebSocket prefix. */
	public final static String SWS_PREFIX = "wss://";
	
	/** Multi-level MQTT topic wildcard. */
	public static final String MULTI_LEVEL_WILDCARD = "#";
	
	/** Single-level MQTT topic wildcard. */
	public static final String SINGLE_LEVEL_WILDCARD = "+";
	
	/** MQTT topic level delimiter. */
	public static final String TOPIC_DELIMITER = "/";
	
	/** Max client length for MQTT 3.1. */
	public static final int MAX_CLIENT_LENGTH_FOR_3_1 = 23;

	/** Client ID timestamp format. */
	private static final String CLIENT_ID_TIMESTAMP_FORMAT = "HHmmssSSS";
	
	/** Client ID timestamp SDF. */
	private static final SimpleDateFormat CLIENT_ID_SDF = new SimpleDateFormat(CLIENT_ID_TIMESTAMP_FORMAT);
	
	/**
	 * Removes last topic delimiter if present.
	 * 
	 * @param topic Topic to remove the delimiter from
	 * 
	 * @return Topic with removed delimiter
	 */
	public static String removeLastDelimiter(String topic)
	{
		if (topic.endsWith(TOPIC_DELIMITER))
		{
			topic = topic.substring(0, topic.length() - TOPIC_DELIMITER.length());
		}
		
		return topic;
	}		

	/**
	 * Generate client ID with timestamp.
	 * 
	 * @param prefix The client ID prefix to use
	 * 
	 * @return The generated client ID
	 */
	public static String generateClientIdWithTimestamp(final String prefix, final ProtocolVersionEnum protocol)
	{
		final int addedLength = CLIENT_ID_TIMESTAMP_FORMAT.length();
		String newClientId = prefix;
	
		if (limitClientId(protocol) && newClientId.length() + addedLength > MAX_CLIENT_LENGTH_FOR_3_1)
		{
			newClientId = newClientId.substring(0, MAX_CLIENT_LENGTH_FOR_3_1 - addedLength);
		}

		newClientId = newClientId + CLIENT_ID_SDF.format(new Date());

		return newClientId;
	}
	
	public static boolean limitClientId(final ProtocolVersionEnum protocol)
	{
		if (ProtocolVersionEnum.MQTT_3_1.equals(protocol) || ProtocolVersionEnum.MQTT_DEFAULT.equals(protocol))
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Completes the server URI with the TCP prefix if not present.
	 *  
	 * @param brokerAddress The broker URL to complete
	 * 
	 * @return Complete URL
	 */
	public static String getCompleteServerURI(final String brokerAddress, final boolean sslEnabled, final boolean websocket)
	{
		String serverURI = brokerAddress.replaceAll(TCP_PREFIX, "").replaceAll(SSL_PREFIX, "").replaceAll(WS_PREFIX, "").replaceAll(SWS_PREFIX, "");
		
		if (sslEnabled && websocket)
		{
			serverURI = SWS_PREFIX + serverURI;			
		}
		else if (sslEnabled && !websocket)
		{			
			serverURI = SSL_PREFIX + serverURI;
		}
		else if (!sslEnabled && websocket)
		{
			serverURI = WS_PREFIX + serverURI;			
		}
		else
		{			
			serverURI = TCP_PREFIX + serverURI;
		}		

		return serverURI;
	}
	
	public static boolean recordTopic(final String newTopic, final List<String> topics)
	{
		final boolean contains = topics.contains(newTopic);
		
		if (!contains)
		{
			topics.add(newTopic);
			return true;
		}
		
		return false;		
	}
	
	public static void validateTopic(final String topic) throws SpyException
	{
		try
		{
			MqttTopic.validate(topic, true);
		}
		catch (Exception e)
		{
			throw new SpyException(e.getMessage(), e);
		}
	}
}
