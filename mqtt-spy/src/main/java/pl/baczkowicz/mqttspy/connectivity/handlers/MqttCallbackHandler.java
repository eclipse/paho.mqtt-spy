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

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.application.Platform;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.ui.events.queuable.connectivity.MqttConnectionLostEvent;
import pl.baczkowicz.spy.messages.MessageIdGenerator;

/**
 * MQTT callback handler - one per connection.
 */
public class MqttCallbackHandler implements MqttCallback
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(MqttCallbackHandler.class);
	
	/** Stores all received messages, so that we don't block the receiving thread. */
	private final Queue<FormattedMqttMessage> messageQueue = new LinkedBlockingQueue<FormattedMqttMessage>();
	
	private MqttAsyncConnection connection;
	
	private MqttMessageHandler messageHandler;

	public MqttCallbackHandler(final MqttAsyncConnection connection)
	{
		this.setConnection(connection);
		this.messageHandler = new MqttMessageHandler(connection, messageQueue);
		new Thread(messageHandler).start();
	}

	public void connectionLost(Throwable cause)
	{
		logger.error("Connection " + connection.getProperties().getName() + " lost", cause);
		Platform.runLater(new MqttEventHandler(new MqttConnectionLostEvent(connection, cause)));
	}

	public void messageArrived(final String topic, final MqttMessage message)
	{
		logger.debug("[{}] Received message on topic \"{}\". Payload = \"{}\"", messageQueue.size(), topic, new String(message.getPayload()));
		messageQueue.add(new FormattedMqttMessage(MessageIdGenerator.getNewId(), topic, message, connection));
	}

	public void deliveryComplete(IMqttDeliveryToken token)
	{
		logger.trace("Delivery complete for " + token.getMessageId());
	}

	public MqttAsyncConnection getConnection()
	{
		return connection;
	}

	public void setConnection(MqttAsyncConnection connection)
	{
		this.connection = connection;
	}
}
