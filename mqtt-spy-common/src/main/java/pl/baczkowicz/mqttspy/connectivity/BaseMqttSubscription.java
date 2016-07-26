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
package pl.baczkowicz.mqttspy.connectivity;

import pl.baczkowicz.mqttspy.common.generated.SubscriptionDetails;
import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.spy.scripts.Script;
import pl.baczkowicz.spy.storage.BasicMessageStore;
import pl.baczkowicz.spy.storage.MessageList;

public class BaseMqttSubscription
{
	private int id;
	
	private String topic;

	private Integer qos;

	private boolean subscribing;
	
	private boolean subscriptionRequested;

	private boolean active;
	
	private BaseMqttConnection connection;
	
	private SubscriptionDetails details;
	
	private BasicMessageStore<FormattedMqttMessage> store;

	private Script script;

	private boolean scriptActive;

	public BaseMqttSubscription(final String topic, final Integer qos)
	{
		this.topic = topic;
		this.qos = qos;
		this.active = false;
		this.subscriptionRequested = false;
		
		this.store = null;
	}
	
	public BaseMqttSubscription(final String topic, final Integer qos, 
			final int minMessagesPerTopic, final int preferredStoreSize)
	{
		this(topic, qos);
		
		this.store = new BasicMessageStore<FormattedMqttMessage>(
				new MessageList<FormattedMqttMessage>(minMessagesPerTopic, preferredStoreSize, topic));
	}

	public String getTopic()
	{
		return topic;
	}

	public void setTopic(String topic)
	{
		this.topic = topic;
	}

	public Integer getQos()
	{
		return qos;
	}

	public void setQos(Integer qos)
	{
		this.qos = qos;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive(final boolean active)
	{
		this.active = active;
	}

	public void setConnection(final BaseMqttConnection connection)
	{
		this.connection = connection;		
	}
	
	public BaseMqttConnection getConnection()	
	{
		return connection;
	}

	public int getId()
	{
		return id;
	}

	public void setId(final int id)
	{
		this.id = id;		
	}

	public boolean isSubscribing()
	{
		return subscribing;
	}
	
	public void setSubscribing(final boolean value)
	{
		subscribing = value;
	}

	public boolean getSubscriptionRequested()
	{
		return subscriptionRequested;
	}

	public void setSubscriptionRequested(final boolean subscriptionRequested)
	{
		this.subscriptionRequested = subscriptionRequested;
	}

	public SubscriptionDetails getDetails()
	{
		return details;
	}

	public void setDetails(final SubscriptionDetails details)
	{
		this.details = details;
	}

	public BasicMessageStore<FormattedMqttMessage> getStore()
	{
		return store;
	}

	public void setScript(final Script script)
	{
		this.script = script;		
	}

	public void setScriptActive(final boolean scriptActive)
	{
		this.scriptActive = scriptActive;		
	}
	
	public boolean isScriptActive()
	{
		return scriptActive;
	}

	public Script getScript()
	{
		return script;
	}
}
