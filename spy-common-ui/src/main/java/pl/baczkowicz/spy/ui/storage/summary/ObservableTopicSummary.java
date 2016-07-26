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
package pl.baczkowicz.spy.ui.storage.summary;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import pl.baczkowicz.spy.common.generated.FormatterDetails;
import pl.baczkowicz.spy.formatting.FormattingManager;
import pl.baczkowicz.spy.messages.FormattedMessage;
import pl.baczkowicz.spy.ui.properties.SubscriptionTopicSummaryProperties;

/**
 * This class provides a direct feed to the topic summary table, by exposing the observable list.
 */
public class ObservableTopicSummary<T extends FormattedMessage> extends TopicSummary<T>
{
	private final ObservableList<SubscriptionTopicSummaryProperties<T>> observableTopicSummaryList = FXCollections.observableArrayList();

	public ObservableTopicSummary(final String name, final int maxPayloadLength)
	{
		super(name, maxPayloadLength);
	}
	
	public void clear()
	{
		synchronized (topicToSummaryMapping)
		{
			super.clear();
			observableTopicSummaryList.clear();
		}
	}
	
	public SubscriptionTopicSummaryProperties<T> addMessage(final T message)
	{
		synchronized (topicToSummaryMapping)
		{
			final AtomicBoolean newAdded = new AtomicBoolean(false);

			SubscriptionTopicSummaryProperties<T> updatedElement = super.addMessage(message, newAdded);
			
			if (newAdded.get())
			{				
				observableTopicSummaryList.add(updatedElement);
			}
			else
			{
				// Set the updated object to notify the observers of the list
				//  - checked, and this seems not to be needed any more
				//observableTopicSummaryList.set(observableTopicSummaryList.indexOf(updatedElement), updatedElement);
			}
			
			return updatedElement;
		}				
	}

	public void toggleShowValues(final Collection<String> topics)
	{
		synchronized (topicToSummaryMapping)
		{
			for (final String topic : topics)
			{
				final SubscriptionTopicSummaryProperties<T> item = topicToSummaryMapping.get(topic);
				
				if (item != null)
				{
					item.showProperty().set(!item.showProperty().get());
				}
			}
			
//			for (final SubscriptionTopicSummaryProperties item : observableTopicSummaryList)
//			{
//				if (topics.contains(item.topicProperty().getValue()))
//				{
//					item.showProperty().set(!item.showProperty().get());
//				}
//			}
		}
	}
	
	public void setShowValue(final String topic, final boolean value)
	{
		synchronized (topicToSummaryMapping)
		{
			final SubscriptionTopicSummaryProperties<T> item = topicToSummaryMapping.get(topic);
			
			if (item != null)
			{
				item.showProperty().set(value);
			}
			
//			for (final SubscriptionTopicSummaryProperties item : observableTopicSummaryList)
//			{
//				if (item.topicProperty().getValue().equals(topic))
//				{
//					item.showProperty().set(value);
//					break;
//				}
//			}
		}
	}
	
	public void setShowValues(final Collection<String> topics, final boolean value)
	{
		synchronized (topicToSummaryMapping)
		{
			for (final SubscriptionTopicSummaryProperties<T> item : observableTopicSummaryList)
			{
				if (topics.contains(item.topicProperty().getValue()))
				{
					item.showProperty().set(value);
				}
			}
		}
	}
	
	public void setAllShowValues(final boolean value)
	{
		synchronized (topicToSummaryMapping)
		{
			for (final SubscriptionTopicSummaryProperties<T> item : observableTopicSummaryList)
			{
				item.showProperty().set(value);
			}
		}
	}

	public ObservableList<SubscriptionTopicSummaryProperties<T>> getObservableMessagesPerTopic()
	{
		return observableTopicSummaryList;
	}
	
	public void setFormatter(final FormatterDetails messageFormat, final FormattingManager formattingManager)
	{
		super.setFormatter(messageFormat);
		
		for (final SubscriptionTopicSummaryProperties<T> item : observableTopicSummaryList)
		{
			formattingManager.formatMessage(item.getMqttContent(), messageFormat);
			item.updateReceivedPayload(item.getMqttContent().getFormattedPayload());
		}
	}
}
