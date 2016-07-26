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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.connectivity.MqttConnectionStatus;
import pl.baczkowicz.mqttspy.ui.connections.ConnectionManager;

/**
 * Utilities for creating user actions.
 */
public class ActionUtils
{

	/**
	 * Creates an event handler with a disconnection action.
	 * 
	 * @param connection The connection to be used
	 * @param connectionManager The connection manager
	 * 
	 * @return The EventHandler with the action
	 */
	public static EventHandler<ActionEvent> createDisconnectAction(final ConnectionManager connectionManager, final MqttAsyncConnection connection)
	{
		return new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent event)
			{
				connectionManager.disconnectFromBroker(connection);
				event.consume();
			}
		};
	}
	
	/**
	 * Creates an event handler with an empty action.
	 * 
	 * @return The EventHandler with the action
	 */
	public static EventHandler<ActionEvent> createEmptyAction()
	{
		return new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent event)
			{				
				event.consume();
			}
		};
	}
	
	/**
	 * Creates an event handler with a disconnection and close action.
	 * 
	 * @param connection The connection to be used
	 * @param connectionManager The connection manager used
	 * 
	 * @return The EventHandler with the action
	 */
	public static EventHandler<ActionEvent> createDisconnectAndCloseAction(final ConnectionManager connectionManager, final MqttAsyncConnection connection)
	{
		return new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent event)
			{
				connectionManager.disconnectAndCloseTab(connection);
				event.consume();
			}
		};
	}
	
	/**
	 * Creates an event handler with a 'connect to broker' action.
	 * 
	 * @param connectionManager The connection manager to be used to connect
	 * @param connection The connection to be used
	 * 
	 * @return The EventHandler with the action
	 */
	public static EventHandler<ActionEvent> createConnectAction(final ConnectionManager connectionManager, final MqttAsyncConnection connection)
	{
		return new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent event)
			{
				connectionManager.connectToBroker(connection);
				event.consume();
			}
		};
	}

	/**
	 * Creates an event handler with a next allowable action for the given connection.
	 * 
	 * @param state The next state
	 * @param connection The connection to be used
	 * @param mqttManager The MQTT manager to be used
	 * 
	 * @return The EventHandler with the action
	 */
	public static EventHandler<ActionEvent> createNextAction(final MqttConnectionStatus state, 
			final MqttAsyncConnection connection, final ConnectionManager connectionManager)
	{
		if (state == null)
		{
			return createEmptyAction();
		}
		
		switch (state)
		{
			case CONNECTED:				
				return createDisconnectAction(connectionManager, connection);
			case CONNECTING:
				return createEmptyAction();
			case DISCONNECTED:
				return createConnectAction(connectionManager, connection);
			case DISCONNECTING:
				return createEmptyAction();
			case NOT_CONNECTED:
				return createConnectAction(connectionManager, connection);
			default:
				return createEmptyAction();
		}		
	}	
}
