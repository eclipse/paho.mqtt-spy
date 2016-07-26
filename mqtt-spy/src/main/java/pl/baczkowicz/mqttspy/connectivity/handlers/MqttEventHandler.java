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
package pl.baczkowicz.mqttspy.connectivity.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.connectivity.MqttConnectionStatus;
import pl.baczkowicz.mqttspy.ui.events.queuable.connectivity.MqttConnectionAttemptSuccessEvent;
import pl.baczkowicz.mqttspy.ui.events.queuable.connectivity.MqttConnectionFailureEvent;
import pl.baczkowicz.mqttspy.ui.events.queuable.connectivity.MqttConnectionLostEvent;
import pl.baczkowicz.mqttspy.ui.events.queuable.connectivity.MqttDisconnectionAttemptSuccessEvent;
import pl.baczkowicz.spy.events.SpyEvent;

public class MqttEventHandler implements Runnable
{
	private final static Logger logger = LoggerFactory.getLogger(MqttEventHandler.class);
	
	private SpyEvent event;

	public MqttEventHandler(final SpyEvent event)
	{
		this.event = event;
	}

	public void run()
	{
		if (event instanceof MqttConnectionLostEvent)
		{
			((MqttConnectionLostEvent) event).getConnection().connectionLost(
					((MqttConnectionLostEvent) event).getCause());
		}

		// This also covers sub-types of the failure event
		if (event instanceof MqttConnectionFailureEvent)
		{
			final MqttConnectionFailureEvent mqttConnectionFailureEvent = (MqttConnectionFailureEvent) event;
			
			mqttConnectionFailureEvent.getConnection().setDisconnectionReason(mqttConnectionFailureEvent.getCause().getMessage());
			mqttConnectionFailureEvent.getConnection().setConnectionStatus(MqttConnectionStatus.DISCONNECTED);
		}
		
		if (event instanceof MqttConnectionAttemptSuccessEvent)
		{
			final MqttAsyncConnection connection = ((MqttConnectionAttemptSuccessEvent) event).getConnection(); 
			
			connection.setConnectionStatus(MqttConnectionStatus.CONNECTED);
			connection.recordSuccessfulConnection();
			
			// This should restore any previously requested subscriptions
			logger.info("About to resubscribe to all requested topics");			
			connection.resubscribeAll(true);
			connection.startBackgroundScripts();
		}
		
		if (event instanceof MqttDisconnectionAttemptSuccessEvent)
		{
			final MqttDisconnectionAttemptSuccessEvent mqttDisconnectionAttemptSuccessEvent = (MqttDisconnectionAttemptSuccessEvent) event;
			
			mqttDisconnectionAttemptSuccessEvent.getConnection().setDisconnectionReason("");
			mqttDisconnectionAttemptSuccessEvent.getConnection().setConnectionStatus(MqttConnectionStatus.DISCONNECTED);
		}
	}
}
