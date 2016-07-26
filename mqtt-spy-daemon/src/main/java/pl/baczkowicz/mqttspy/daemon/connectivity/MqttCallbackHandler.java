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
package pl.baczkowicz.mqttspy.daemon.connectivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.MessageLogEnum;
import pl.baczkowicz.mqttspy.common.generated.SubscriptionDetails;
import pl.baczkowicz.mqttspy.connectivity.BaseMqttConnection;
import pl.baczkowicz.mqttspy.connectivity.BaseMqttSubscription;
import pl.baczkowicz.mqttspy.daemon.configuration.generated.DaemonMqttConnectionDetails;
import pl.baczkowicz.mqttspy.logger.MqttMessageLogger;
import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.spy.common.generated.FormatterDetails;
import pl.baczkowicz.spy.formatting.FormattingManager;
import pl.baczkowicz.spy.messages.MessageIdGenerator;
import pl.baczkowicz.spy.scripts.BaseScriptManager;

/**
 * Callback handler for the MQTT connection.
 */
public class MqttCallbackHandler implements MqttCallback
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(MqttCallbackHandler.class);
	
	/** Stores all received messages, so that we don't block the receiving thread. */
	private final Queue<FormattedMqttMessage> messageQueue = new LinkedBlockingQueue<FormattedMqttMessage>();
	
	/** Logs all received messages (if configured). */
	private final MqttMessageLogger messageLogger;
	
	/** The connection. */
	private final BaseMqttConnection connection;

	/** Connection details. */
	private final DaemonMqttConnectionDetails connectionSettings;
	
	/** Subscription details (as configured). */
	private final Map<String, SubscriptionDetails> subscriptionsDetails = new HashMap<String, SubscriptionDetails>();

	/** Script manager - for running subscription scripts. */
	private final BaseScriptManager scriptManager;
	
	private final FormattingManager formattingManager;
	
	/** Message ID. */
	//private long currentId = 1;

	/**
	 * Creates a MqttCallbackHandler.
	 * 
	 * @param connection The connection to be used
	 * @param connectionSettings Connection's details
	 * @param scriptManager Script manager - for running subscription scripts
	 */
	public MqttCallbackHandler(final BaseMqttConnection connection, final DaemonMqttConnectionDetails connectionSettings, 
			final BaseScriptManager scriptManager)
	{
		this.connection = connection;
		this.connectionSettings = connectionSettings;
		this.scriptManager = scriptManager;
		this.formattingManager = new FormattingManager(scriptManager);
		this.messageLogger = new MqttMessageLogger("0", messageQueue, connectionSettings.getMessageLog(), false, 10);
		
		for (final SubscriptionDetails subscriptionDetails : connectionSettings.getSubscription())
		{
			this.subscriptionsDetails.put(subscriptionDetails.getTopic(), subscriptionDetails);
		}
		
		new Thread(messageLogger).start();			
	}

	/** 
	 * Handles connection loss.
	 * 
	 * @param cause Reason of the connection loss
	 */
	public void connectionLost(final Throwable cause)
	{
		logger.error("Connection lost", cause);
		connection.connectionLost(cause);
	}

	/**
	 * Handles received messages.
	 * 
	 * @param topic Topic on which the message has been received
	 * @param message The received message
	 */
	public void messageArrived(final String topic, final MqttMessage message)
	{
		if (logger.isTraceEnabled())
		{
			logger.trace("[{}] Received message on topic \"{}\". Payload = \"{}\"", messageQueue.size(), topic, new String(message.getPayload()));
		}
		
		final long newId = MessageIdGenerator.getNewId();
		
		final FormattedMqttMessage receivedMessage = new FormattedMqttMessage(newId, topic, message, connection);
		
		// Check matching subscriptions
		final List<String> matchingSubscriptions = connection.getTopicMatcher().getMatchingSubscriptions(receivedMessage.getTopic());
		receivedMessage.setMatchingSubscriptionTopics(matchingSubscriptions);
		
		if (logger.isTraceEnabled())
		{
			logger.trace("Matching subscriptions for message on " + receivedMessage.getTopic() + " = " + matchingSubscriptions);
		}
		
		// Before scripts
		if (connectionSettings.getMessageLog().isLogBeforeScripts())
		{
			// Log a copy, so that it cannot be modified
			logMessage(new FormattedMqttMessage(receivedMessage));
		}
		
		// Format the message if configured
		final FormattedMqttMessage formattedMessage = new FormattedMqttMessage(newId, topic, message, connection);
		if (connectionSettings.getFormatter() != null)
		{
			formattingManager.formatMessage(formattedMessage, (FormatterDetails) connectionSettings.getFormatter());
		}
		
		for (final String matchingSubscriptionTopic : matchingSubscriptions)
		{
			// If configured, run scripts for the matching subscriptions
			final BaseMqttSubscription subscription = connection.getMqttSubscriptionForTopic(matchingSubscriptionTopic);
			if (subscription.isScriptActive())
			{
				scriptManager.runScriptWithReceivedMessage(subscription.getScript(), receivedMessage);
			}
			
			// Store the message (e.g. to be used by test cases; not needed for logging and general scripts)
			if (subscription.getStore() != null)
			{
				subscription.getStore().messageReceived(formattedMessage);
			}
		}
				
		// After scripts
		if (!connectionSettings.getMessageLog().isLogBeforeScripts())
		{
			logMessage(receivedMessage);
		}
	}
	
	/**
	 * Adds the message to the 'to be logged' queue.
	 *  
	 * @param receivedMessage The received message
	 */
	public void logMessage(final FormattedMqttMessage receivedMessage)
	{
		// Add the received message to queue for logging
		if (!MessageLogEnum.DISABLED.equals(connectionSettings.getMessageLog().getValue()))
		{
			messageQueue.add(receivedMessage);
		}
	}

	/**
	 * Handles completion of message delivery.
	 * 
	 * @param token Delivery token
	 */
	public void deliveryComplete(final IMqttDeliveryToken token)
	{
		if (logger.isTraceEnabled())
		{
			logger.trace("Delivery complete for " + token.getMessageId());
		}
	}
	
	/**
	 * Stops the message logger.
	 */
	public void stop()
	{
		messageLogger.stop();
	}
}
