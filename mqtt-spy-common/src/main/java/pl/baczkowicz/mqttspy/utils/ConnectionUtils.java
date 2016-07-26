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

import java.util.List;

import pl.baczkowicz.mqttspy.common.generated.MqttConnectionDetails;
import pl.baczkowicz.mqttspy.common.generated.ProtocolVersionEnum;

/**
 * Connection utils.
 */
public class ConnectionUtils
{
	/** Indicates connection was never started. */
	public static final long NEVER_STARTED = 0;

	/** This is the delimiter for separating server URIs in a single string. */
	public static final String SERVER_DELIMITER = ";";

	/**
	 * Composes a connection name based on the supplied client ID and server URIs.
	 * 
	 * @param clientId Client ID
	 * @param serverURIs Server URIs
	 * 
	 * @return Composed connection name
	 */
	public static String composeConnectionName(final String clientId, final List<String> serverURIs)
	{
		return composeConnectionName(clientId, serverURIsToString(serverURIs));
	}

	/**
	 * Composes a connection name based on the supplied client ID and server URIs.
	 * 
	 * @param clientId Client ID
	 * @param serverURIs Server URIs
	 * 
	 * @return Composed connection name
	 */
	public static String composeConnectionName(final String clientId, final String serverURIs)
	{
		return clientId + "@" + serverURIs;
	}

	/**
	 * Turns the given list of server URIs into a single string.
	 * 
	 * @param serverURIs List of server URIs
	 * 
	 * @return String representing all URIs
	 */
	public static String serverURIsToString(final List<String> serverURIs)
	{
		StringBuffer serverURIsAsString = new StringBuffer();
		boolean first = true;
		for (final String serverURI : serverURIs)
		{
			if (first)
			{
				serverURIsAsString.append(serverURI);
			}
			else
			{
				serverURIsAsString.append(ConnectionUtils.SERVER_DELIMITER
						+ " " + serverURI);
			}

			first = false;
		}

		return serverURIsAsString.toString();
	}
	
	public static String validateConnectionDetails(final MqttConnectionDetails connectionDetails)
	{
		if (connectionDetails.getServerURI() == null || connectionDetails.getServerURI().size() == 0)
		{
			return "Server URI cannot be empty";
		}
		
		boolean allEmpty = true;
		for (final String serverURI : connectionDetails.getServerURI())
		{
			if (!serverURI.trim().isEmpty())
			{
				allEmpty = false;
				break;
			}
		}		
		if (allEmpty)
		{
			return "Server URI cannot be empty";
		}
		
		final boolean emptyClientId = (connectionDetails.getClientID() == null)
				|| connectionDetails.getClientID().trim().isEmpty();
		
		if (ProtocolVersionEnum.MQTT_3_1_1.equals(connectionDetails.getProtocol()) && emptyClientId)
		{
			if (!connectionDetails.isCleanSession())
			{
				return "Client ID can only be empty when the 'clean session' flag is set";
			}
		}
		else if (emptyClientId)
		{
			return "Empty client ID is only allowed in MQTT 3.1.1";
		}
	
		if (MqttUtils.limitClientId(connectionDetails.getProtocol()) 
						&& connectionDetails.getClientID().length() > MqttUtils.MAX_CLIENT_LENGTH_FOR_3_1)
		{
			return "Client ID cannot longer than " + MqttUtils.MAX_CLIENT_LENGTH_FOR_3_1;
		}
		
		if (connectionDetails.getLastWillAndTestament() != null)
		{
			if (connectionDetails.getLastWillAndTestament().getTopic() == null || connectionDetails.getLastWillAndTestament().getTopic().isEmpty())				
			{
				return "With last will and testament enabled, publication topic cannot be empty";
			}
		}
		
		if (connectionDetails.getConnectionTimeout() < 0)
		{
			return "Connection timeout cannot be less than 0";
		}
		
		if (connectionDetails.getKeepAliveInterval() < 0)
		{
			return "Keep alive interval cannot be less than 0";
		}		
		
		return null;
	}
}
