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
package pl.baczkowicz.mqttspy.messages;

import java.util.Date;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import pl.baczkowicz.mqttspy.connectivity.BaseMqttConnection;
import pl.baczkowicz.spy.utils.ConversionUtils;

public class FormattedMqttMessage extends BaseMqttMessage
{
//	/** The first matching subscription. */ 
//	private String subscription;
	
	/** Subscriptions matching the message's topic. */
	private List<String> matchingSubscriptionTopics;
	
	/** The connection on which the message was received. */
	private final BaseMqttConnection connection;
	
	public FormattedMqttMessage(final long id, final String topic, final MqttMessage message, final BaseMqttConnection connection)
	{
		super(id, topic, message);
		this.connection = connection;
		setFormattedPayload(ConversionUtils.arrayToString(message.getPayload()));
	}
	
	public FormattedMqttMessage(final long id, final String topic, final MqttMessage message, final Date date, final BaseMqttConnection connection)
	{
		super(id, topic, message, date);
		this.connection = connection;
		setFormattedPayload(ConversionUtils.arrayToString(message.getPayload()));
	}
	
	public FormattedMqttMessage(final FormattedMqttMessage message)
	{
		this(message.getId(), message.getTopic(), copyMqttMessage(message.getRawMessage()), message.getConnection());
		// Note: if payload is set, this results in a bug, where it performs unnecessary conversions
		// setPayload(new String(message.getPayload()));
		setRawPayload(message.getRawPayload().clone());
		setFormattedPayload(message.getFormattedPayload());
		setLastUsedFormatter(message.getLastUsedFormatter());
		setSubscription(message.getSubscription());
		setMatchingSubscriptionTopics(message.getMatchingSubscriptionTopics());
	}

	public FormattedMqttMessage(final BaseMqttMessage message, final BaseMqttConnection connection)
	{
		this(message.getId(), message.getTopic(), message.getRawMessage(), message.getDate(), connection);
		setFormattedPayload(message.getPayload());
	}	

	/**
	 * Gets the list of matching subscriptions.
	 * 
	 * @return List of matching subscriptions
	 */
	public List<String> getMatchingSubscriptionTopics()
	{
		return matchingSubscriptionTopics;
	}

	/**
	 * Sets the list of matching subscriptions.
	 * 
	 * @param subscriptions The matching subscriptions to set
	 */
	public void setMatchingSubscriptionTopics(final List<String> subscriptions)
	{
		this.matchingSubscriptionTopics = subscriptions;
	}

	/**
	 * Gets the connection.
	 * 
	 * @return The connection on which the message was received
	 */
	public BaseMqttConnection getConnection()
	{
		return connection;
	}
}
