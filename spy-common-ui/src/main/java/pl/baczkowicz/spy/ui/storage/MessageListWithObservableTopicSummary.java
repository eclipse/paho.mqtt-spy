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
package pl.baczkowicz.spy.ui.storage;

import pl.baczkowicz.spy.common.generated.FormatterDetails;
import pl.baczkowicz.spy.messages.FormattedMessage;
import pl.baczkowicz.spy.storage.MessageList;
import pl.baczkowicz.spy.ui.storage.summary.ObservableTopicSummary;

/**
 * Message list with observable topic summary.
 */
public class MessageListWithObservableTopicSummary<T extends FormattedMessage> extends MessageList<T>
{
	private final ObservableTopicSummary<T> topicSummary;
	
	public MessageListWithObservableTopicSummary(final int preferredSize, final int maxSize, 
			final String name, final FormatterDetails messageFormat, final int maxPayloadLength)
	{
		super(preferredSize, maxSize, name);
				
		this.topicSummary = new ObservableTopicSummary<T>(name, maxPayloadLength);
		this.topicSummary.setFormatter(messageFormat);
	}

	public ObservableTopicSummary<T> getTopicSummary()
	{
		return topicSummary;
	}
	
	public T add(final T message)
	{
		final T removed = super.add(message);
		
		if (removed != null)
		{
			topicSummary.decreaseCount(removed);
		}
		topicSummary.increaseCount(message);
		
		return removed;
	}
	
	public T remove(final int index)
	{
		final T removed = super.remove(index);
		
		topicSummary.decreaseCount(removed);
		
		return removed;
	}
}
