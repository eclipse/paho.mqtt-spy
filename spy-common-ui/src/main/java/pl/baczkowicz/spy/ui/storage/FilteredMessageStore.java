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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.ui.search.MessageFilter;
import pl.baczkowicz.spy.common.generated.FormatterDetails;
import pl.baczkowicz.spy.formatting.FormattingManager;
import pl.baczkowicz.spy.messages.FormattedMessage;

/**
 * Message store with filtering. 
 */
public class FilteredMessageStore<T extends FormattedMessage> extends BasicMessageStoreWithSummary<T>
{
	final static Logger logger = LoggerFactory.getLogger(FilteredMessageStore.class);
	
	/** This is the same as 'show' flag on topic summary. */
	private final Set<String> browsedTopics = new HashSet<String>();
	
	//private final MessageListWithObservableTopicSummary filteredMessages;

	private final MessageListWithObservableTopicSummary<T> allMessages;
	
	private final Set<MessageFilter<T>> messageFilters = new HashSet<>();
	
	public FilteredMessageStore(final MessageListWithObservableTopicSummary<T> allMessages, 
			final int preferredSize, final int maxSize, final String name, final FormatterDetails messageFormat, 
			final FormattingManager formattingManager, final int maxPayloadLength)
	{
		super("filtered-" + name, preferredSize, maxSize, maxPayloadLength, formattingManager);
		setFormatter(messageFormat);
		//this.filteredMessages = new MessageListWithObservableTopicSummary(preferredSize, maxSize, "filtered-" + name, messageFormat);
		this.allMessages = allMessages;
	}
	
	public void addMessageFilter(final MessageFilter<T> messageFilter)
	{
		messageFilters.add(messageFilter);
	}
	
	public void removeMessageFilter(final MessageFilter<T> messageFilter)
	{
		messageFilters.remove(messageFilter);
		// TODO: rebuild the store?
	}
	
	public void runFilter(final MessageFilter<T> messageFilter)
	{
		reinitialiseFilteredStore();
	}
	
	@Override
	public boolean messageFiltersEnabled()
	{
		for (final MessageFilter<T> filter : messageFilters)
		{
			if (filter.isActive())
			{
				return true;
			}
		}	
		
		return false;
	}
	
	private void reinitialiseFilteredStore()
	{
		getMessageList().clear();
			
		logger.trace("[{}] Store reinitialise = {}/{}", allMessages.getName(), allMessages.getMessages().size(), allMessages);
		synchronized (allMessages.getMessages())
		{			
			final int size = allMessages.getMessages().size();
			for (int i = size - 1; i >= 0; i--)
			{
				final T message = allMessages.getMessages().get(i);
				
				if (browsedTopics.contains(message.getTopic()) && !filterMessage(message, false))
				{
					getMessageList().add(message);								
				}
			}
		}
	}	
	
	public boolean filterMessage(final T message, final boolean updateUi)
	{
		for (final MessageFilter<T> filter : messageFilters)
		{
			if (filter.filter(message, getMessageList(), updateUi))
			{
				return true;
			}
		}	
		
		return false;
	}
	
	public boolean updateTopicFilter(final String topic, final boolean show)
	{
		boolean updated = false;
		if (show)
		{
			updated = applyTopicFilter(topic, true);
		}
		else
		{
			updated = removeTopicFilter(topic);
		}
		
		return updated;
	}	
	
	public void addAllTopicFilters()
	{
		removeAllTopicFilters();
		
		synchronized (allMessages.getMessages())
		{
			for (T message : allMessages.getMessages())
			{
				browsedTopics.add(message.getTopic());
				
				if (!filterMessage(message, false))
				{
					getMessageList().add(message);
				}
			}
		}
	}
	
	public void removeAllTopicFilters()
	{
		synchronized (browsedTopics)
		{
			browsedTopics.clear();
			getMessageList().clear();
		}
	}
	
	public boolean applyTopicFilters(final Collection<String> topics, final boolean recreateStore)
	{
		synchronized (browsedTopics)
		{
			boolean updated = false;
			
			for (final String topic : topics)
			{
				if (!browsedTopics.contains(topic))
				{
					logger.debug("Adding {} to active filters for {}; recreate = {}", topic, allMessages.getName(), recreateStore);
					browsedTopics.add(topic);														
					updated = true;
				}
			}
			
			// TODO: optimise
			if (updated && recreateStore)
			{
				logger.warn("Recreating store for topics in {}", allMessages.getName());
				reinitialiseFilteredStore();
			}
			
			return updated;
		}
	}
	
	public boolean applyTopicFilter(final String topic, final boolean recreateStore)
	{
		return applyTopicFilters(Arrays.asList(topic), recreateStore);
	}
	
	public boolean removeTopicFilters(final Collection<String> topics)
	{
		synchronized (browsedTopics)
		{
			boolean updated = false;
			
			for (final String topic : topics)
			{
				if (browsedTopics.contains(topic))
				{
					logger.debug("Removing {} from active filters for {}", topic, allMessages.getName());
					browsedTopics.remove(topic);		
					updated = true;
				}
			}
			
			if (updated)
			{
				reinitialiseFilteredStore();
			}
			
			return updated;
		}
	}

	private boolean removeTopicFilter(final String topic)
	{
		return removeTopicFilters(Arrays.asList(topic));
	}
	
	public MessageListWithObservableTopicSummary<T> getFilteredMessages()
	{
		return getMessageList();
	}

	public Set<String> getBrowsedTopics()
	{
		return Collections.unmodifiableSet(browsedTopics);
	}
}
