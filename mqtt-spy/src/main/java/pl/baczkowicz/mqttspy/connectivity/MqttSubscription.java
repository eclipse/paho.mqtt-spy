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

import javafx.scene.paint.Color;
import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.ui.events.SubscriptionStatusChangeEvent;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.formatting.FormattingManager;
import pl.baczkowicz.spy.ui.events.queuable.EventQueueManager;
import pl.baczkowicz.spy.ui.storage.ManagedMessageStoreWithFiltering;

public class MqttSubscription extends BaseMqttSubscription
{
	private Color color;
	
	private MqttAsyncConnection connection;
	
	private final ManagedMessageStoreWithFiltering<FormattedMqttMessage> store;

	private IKBus eventBus;

	public MqttSubscription(final String topic, final Integer qos, final Color color, 
			final int minMessagesPerTopic, final int preferredStoreSize, final EventQueueManager<FormattedMqttMessage> uiEventQueue,
			final IKBus eventBus,
			final FormattingManager formattingManager, final int summaryMaxPayloadLength)
	{
		super(topic, qos, minMessagesPerTopic, preferredStoreSize);
		
		// Max size is double the preferred size
		store = new ManagedMessageStoreWithFiltering<FormattedMqttMessage>(topic, minMessagesPerTopic, 
				preferredStoreSize, preferredStoreSize * 2, 
				uiEventQueue,
				formattingManager,
				summaryMaxPayloadLength);
		
		this.color = color;
		this.eventBus = eventBus;
	}

	public Color getColor()
	{
		return color;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}

	@Override
	public void setActive(final boolean active)
	{
		super.setActive(active);
		subscriptionStatusChanged();
	}

	public void subscriptionStatusChanged()
	{
		eventBus.publish(new SubscriptionStatusChangeEvent(this));
	}

	public void setConnection(final MqttAsyncConnection connection)
	{
		this.connection = connection;		
	}
	
	public MqttAsyncConnection getConnection()	
	{
		return connection;
	}

	@Override
	public ManagedMessageStoreWithFiltering<FormattedMqttMessage> getStore()
	{
		return store;
	}
}
