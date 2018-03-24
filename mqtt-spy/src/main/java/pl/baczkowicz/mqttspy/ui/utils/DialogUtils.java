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
package pl.baczkowicz.mqttspy.ui.utils;

import java.util.Optional;

import javafx.scene.control.Tooltip;
import javafx.util.Pair;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.spy.common.generated.UserCredentials;
import pl.baczkowicz.spy.configuration.BaseConfigurationUtils;
import pl.baczkowicz.spy.connectivity.ConnectionStatus;
import pl.baczkowicz.spy.ui.utils.DialogFactory;

/**
 * Utilities for creating all sorts of dialogs.
 */
public class DialogUtils
{
	/**
	 * Asks the user to review/complete username and password information.
	 * 
	 * @param owner The window owner
	 * @param connectionName Name of the connection
	 * @param userCredentials Existing user credentials
	 * 
	 * @return True when confirmed by user
	 */
	public static boolean createMqttUsernameAndPasswordDialog(final Object owner,
			final String connectionName, final UserCredentials userCredentials)
	{
		final Pair<String, String> userInfo = new Pair<String, String>(
				userCredentials.getUsername(), 
				BaseConfigurationUtils.decodePassword(userCredentials.getPassword()));
		
		Optional<Pair<String, String>> response = DialogFactory.createUsernameAndPasswordDialog(
				"MQTT user credentials",
				"User credentials for connection " + connectionName,
				userInfo);
		
		if (response.isPresent())
		{
			userCredentials.setUsername(response.get().getKey());			
			userCredentials.setPassword(BaseConfigurationUtils.encodePassword(response.get().getValue()));
			return true;
		}
		
		return false;
	}
	
    /**
	 * Updates the given connection tooltip with connection information.
	 * 
	 * @param connection The connection to which the tooltip refers
	 * @param tooltip The tooltip to be updated
	 */
	public static void updateConnectionTooltip(final MqttAsyncConnection connection, final Tooltip tooltip)
	{
		final StringBuffer sb = new StringBuffer();
		sb.append("Status: " + connection.getConnectionStatus().toString().toLowerCase());
		
		if (ConnectionStatus.CONNECTED.equals(connection.getConnectionStatus()))
		{
			sb.append(" (" + connection.getLastSuccessfulConnectionAttempt() + ")");
			
			sb.append(System.getProperty("line.separator"));
			final String sslStatus = connection.getProperties().getSSL() != null ? "on" : "off";
			final String userAuthStatus = connection.getProperties().getUserCredentials() != null ? "on" : "off";
			sb.append("Security: TLS/SSL is " +  sslStatus + "; user authentication is " + userAuthStatus);
		}
		
		if (connection.getConnectionState().getConnectionAttempts() > 1)
		{
			sb.append(System.getProperty("line.separator"));
			sb.append("Connection attempts: " + connection.getConnectionState().getConnectionAttempts());
		}
				
		if (connection.getConnectionState().getDisconnectionReason() != null && !connection.getConnectionState().getDisconnectionReason().isEmpty())
		{
			sb.append(System.getProperty("line.separator"));
			sb.append("Last error: " + connection.getConnectionState().getDisconnectionReason().toLowerCase());
		}	
		
		tooltip.setText(sb.toString());
	}
}
