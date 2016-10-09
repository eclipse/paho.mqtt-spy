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
package pl.baczkowicz.mqttspy.scripts;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.connectivity.BaseMqttSubscription;
import pl.baczkowicz.mqttspy.connectivity.IMqttConnection;
import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.scripts.Script;
import pl.baczkowicz.spy.scripts.ScriptIO;

/**
 * Implementation of the interface between a script and the rest of the application.
 */
public class MqttScriptIO extends ScriptIO implements IMqttScriptIO
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(MqttScriptIO.class);
	
	/** Reference to the MQTT connection. */
	private final IMqttConnection connection;
	
	/**
	 * Creates the PublicationScriptIO.
	 * 
	 * @param connection The connection for which the script is executed
	 * @param eventManager The global event manager
	 * @param script The script itself
	 * @param executor Task executor
	 */
	public MqttScriptIO(
			final IMqttConnection connection, final IKBus eventBus, 
			final Script script, final Executor executor)
	{
		super(script, executor);	
		this.connection = connection;
	}
	
	@Override
	public void publish(final String publicationTopic, final String data)
	{
		publish(publicationTopic, data, 0, false);
	}
	
	@Override
	public void publish(final String publicationTopic, final byte[] data, final int qos, final boolean retained)
	{
		touch();
		
		logger.debug("[JS {}] Publishing message to {} with payload size = {}, qos = {}, retained = {}", 
				scriptName, publicationTopic, data.length, qos, retained);
		final boolean published = connection.publish(publicationTopic, data, qos, retained);
		
		if (published)
		{				
			updatePublished();
		}
		
		// TODO: change state to finished?
	}
	
	@Override
	public void publish(final String publicationTopic, final String data, final int qos, final boolean retained)
	{
		touch();
		
		logger.debug("[JS {}] Publishing message to {} with payload = {}, qos = {}, retained = {}", 
				scriptName, publicationTopic, data, qos, retained);
		
		final boolean published = connection.publish(publicationTopic, data, qos, retained);
		
		if (published)
		{				
			updatePublished();
		}
		
		// TODO: change state to finished?
	}

	@Override
	public boolean subscribe(final String topic, final int qos)
	{
		BaseMqttSubscription subscription = connection.getMqttSubscriptionForTopic(topic);
		
		if (subscription == null)
		{
			subscription = new BaseMqttSubscription(topic, qos, 1, 1000);
		}
		
		return connection.subscribe(subscription);
	}

	@Override
	public boolean unsubscribe(final String topic)
	{
		BaseMqttSubscription subscription = connection.getMqttSubscriptionForTopic(topic);
		
		if (subscription != null)
		{
			connection.removeSubscription(subscription);
			return connection.unsubscribe(topic);
		}
		
		return false;		
	}
	
	// TODO: getAllMessages
	
	@Override
	public List<FormattedMqttMessage> getMessages(final String subscriptionTopic)
	{
		BaseMqttSubscription subscription = connection.getMqttSubscriptionForTopic(subscriptionTopic);
		
		if (subscription != null)
		{
			return subscription.getStore().getMessages();
		}
		
		// No messages, return empty list
		return new ArrayList<>();
	}
}
