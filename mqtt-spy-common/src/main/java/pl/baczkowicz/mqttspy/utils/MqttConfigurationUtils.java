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

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.MessageLog;
import pl.baczkowicz.mqttspy.common.generated.MessageLogEnum;
import pl.baczkowicz.mqttspy.common.generated.MqttConnectionDetails;
import pl.baczkowicz.mqttspy.common.generated.ProtocolVersionEnum;

/**
 * Configuration utilities.
 */
public class MqttConfigurationUtils
{
	/** Commons schema. */
	public static final String SPY_COMMON_SCHEMA = "/spy-common.xsd";
	
	/** Commons schema. */
	public static final String MQTT_COMMON_SCHEMA = "/mqtt-spy-common.xsd";

	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(MqttConfigurationUtils.class);
	
	/**
	 * Goes through all server URIs and completes them with the TCP prefix if necessary.
	 * 
	 * @param connection Connection details
	 */
	public static void completeServerURIs(final MqttConnectionDetails connection, final boolean sslEnabled, final boolean websocket)
	{
		for (int i = 0; i < connection.getServerURI().size(); i++)
		{
			final String serverURI = connection.getServerURI().get(i);			
			final String completeServerURI = MqttUtils.getCompleteServerURI(serverURI, sslEnabled, websocket);
			
			// Replace the existing value if it is not complete
			if (!completeServerURI.equals(serverURI))
			{
				logger.info("Auto-complete for server URI ({} -> {})", serverURI, completeServerURI);
				connection.getServerURI().set(i, completeServerURI);
			}
		}	
	}
	
	/**
	 * Populates the connection details with missing parameters, e.g. name, clean session, etc.
	 * 
	 * @param connection The connection details to complete
	 */
	public static void populateConnectionDefaults(final MqttConnectionDetails connection)
	{	
		if (connection.getProtocol() == null)
		{
			connection.setProtocol(ProtocolVersionEnum.MQTT_DEFAULT);
		}
		
		if (connection.isWebSocket() == null)
		{
			connection.setWebSocket(false);
		}
		
		if (connection.getName() == null || connection.getName().isEmpty())
		{
			connection.setName(ConnectionUtils.composeConnectionName(connection.getClientID(), connection.getServerURI()));
		}
		
		if (connection.isCleanSession() == null)
		{
			connection.setCleanSession(MqttConnectOptions.CLEAN_SESSION_DEFAULT);
		}
		
		if (connection.getConnectionTimeout() == null)
		{
			connection.setConnectionTimeout(MqttConnectOptions.CONNECTION_TIMEOUT_DEFAULT);
		}		

		if (connection.getKeepAliveInterval() == null)
		{
			connection.setKeepAliveInterval(MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT);
		}		
	}
	
	public static void populateMessageLogDefaults(final MessageLog messageLog)
	{
		if (messageLog.getValue() == null)
		{
			messageLog.setValue(MessageLogEnum.DISABLED);
		}
		
		// Connection
		if (messageLog.isLogConnection() == null)
		{
			messageLog.setLogConnection(false);
		}
		
		// QoS
		if (messageLog.isLogQos() == null)
		{
			messageLog.setLogQos(false);
		}
		
		// Retained
		if (messageLog.isLogRetained() == null)
		{
			messageLog.setLogRetained(false);
		}
		
		// Subscription
		if (messageLog.isLogSubscription() == null)
		{
			messageLog.setLogSubscription(false);
		}
		
		// Log before scripts
		if (messageLog.isLogBeforeScripts() == null)
		{
			messageLog.setLogBeforeScripts(false);
		}
	}
}
