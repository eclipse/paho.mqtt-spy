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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.common.generated.FormatterDetails;
import pl.baczkowicz.spy.messages.FormattedMessage;
import pl.baczkowicz.spy.messages.IBaseMessage;
import pl.baczkowicz.spy.ui.properties.SubscriptionTopicSummaryProperties;

/**
 * This class is responsible for managing counts of messages for each topic. It
 * is responsible for adding new topic entries and storing the formatting
 * settings.
 */
public class TopicSummary<T extends FormattedMessage> extends TopicMessageCount
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(TopicSummary.class);
	
	protected Map<String, SubscriptionTopicSummaryProperties<T>> topicToSummaryMapping = new HashMap<>();
	
	protected FormatterDetails messageFormat;

	private int maxPayloadLength;

	public TopicSummary(final String name, final int maxPayloadLength)
	{
		super(name);
		this.maxPayloadLength = maxPayloadLength;
	}
	
	public void clear()
	{
		synchronized (topicToSummaryMapping)
		{
			super.clear();
			topicToSummaryMapping.clear();
		}
	}
	
	public void removeMessage(final IBaseMessage message)
	{
		synchronized (topicToSummaryMapping)
		{
			final SubscriptionTopicSummaryProperties<T> item = topicToSummaryMapping.get(message.getTopic());
	
			// There should be something in
			if (item != null)
			{
				item.setCount(item.countProperty().intValue() - 1);
			}
			else
			{
				logger.error("[{}] Found empty value for topic {}", name, message.getTopic());
			}
		}
	}
	
	public SubscriptionTopicSummaryProperties<T> addMessage(final T message, final AtomicBoolean newAdded)
	{
		synchronized (topicToSummaryMapping)
		{
			SubscriptionTopicSummaryProperties<T> item = topicToSummaryMapping.get(message.getTopic());
	
			if (item == null)
			{
				item = new SubscriptionTopicSummaryProperties<T>(false, 1, message, maxPayloadLength);
				topicToSummaryMapping.put(message.getTopic(), item);
				newAdded.set(true);
			}
			else
			{
				item.setCount(item.countProperty().intValue() + 1);	
				item.setMessage(message);				
			}
			
			logger.trace("[{}] has {} messages", name, item.countProperty().intValue());
			
			return item;
		}				
	}

	public void setFormatter(final FormatterDetails messageFormat)
	{
		this.messageFormat = messageFormat;		
	}
}
