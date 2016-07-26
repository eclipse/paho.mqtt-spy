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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.formatting.FormattingManager;
import pl.baczkowicz.spy.messages.FormattedMessage;
import pl.baczkowicz.spy.ui.events.queuable.EventQueueManager;
import pl.baczkowicz.spy.ui.events.queuable.ui.BrowseReceivedMessageEvent;
import pl.baczkowicz.spy.ui.events.queuable.ui.TopicSummaryNewMessageEvent;
import pl.baczkowicz.spy.ui.events.queuable.ui.TopicSummaryRemovedMessageEvent;

/**
 * The top level message store, handling received messages.
 * 
 * TODO: need to rationalise the BasicMessageStore, FilteredMessageStore and
 * MessageStore interface. There are 3 message lists, two in FilteredStore and
 * one in BasicMessageStore - probably only need two.
 */
public class ManagedMessageStoreWithFiltering<T extends FormattedMessage> extends BasicMessageStoreWithSummary<T>
{
	final static Logger logger = LoggerFactory.getLogger(ManagedMessageStoreWithFiltering.class);
	
	/** All topics this store knows about. */
	private final Set<String> allTopics = new HashSet<String>();
	
	private FilteredMessageStore<T> filteredStore;
	
	//protected final EventManager eventManager;	
	
	/** Stores events for the UI to be updated. */
	protected final EventQueueManager<T> uiEventQueue;
	
	public ManagedMessageStoreWithFiltering(final String name, final int minMessagesPerTopic, final int preferredSize, final int maxSize, 
			final EventQueueManager<T> uiEventQueue, /*final EventManager eventManager, */final FormattingManager formattingManager,
			final int maxPayloadLength)
	{
		super(name, preferredSize, maxSize, maxPayloadLength, formattingManager);
		
		this.uiEventQueue = uiEventQueue;
		//this.eventManager = eventManager;
		this.filteredStore = new FilteredMessageStore<T>(super.getMessageList(), preferredSize, maxSize, name, 
				messageFormat, formattingManager, maxPayloadLength);
		
		// Set up message store garbage collectors
		this.filteredStore.setMessageStoreGarbageCollector(
				new MessageStoreGarbageCollector<T>(this, filteredStore.getFilteredMessages(), uiEventQueue, minMessagesPerTopic, false, true));		
		this.setMessageStoreGarbageCollector(
				new MessageStoreGarbageCollector<T>(this, super.getMessageList(), uiEventQueue, minMessagesPerTopic, true, false));
		
		new Thread(this.filteredStore.getMessageStoreGarbageCollector()).start();
		new Thread(this.getMessageStoreGarbageCollector()).start();
	}
	
	/**
	 * Stores the received message and triggers UI updates. The following
	 * updates are queued as UI events so that the JavaFX thread is not swamped
	 * with hundreds or thousands of requests to do Platform.runLater().
	 * 
	 * @param message Received message
	 */
	public void messageReceived(final T message)
	{	
		// 0. Format the message with the currently selected formatter
		formattingManager.formatMessage(message, getFormatter());
				
		// Record the current state of topics
		final boolean allTopicsShown = !browsingFiltersEnabled();		
		final boolean topicAlreadyExists = allTopics.contains(message.getTopic());
		
		// Start processing the received message...
		
		// 1. Store the topic for the received message
		allTopics.add(message.getTopic());
		
		// 2. Add the message to 'all messages' store - oldest could be removed if the store has reached its max size 
		final T removed = storeMessage(message);
		
		// 3. Add it to the filtered store if:
		// - message is not filtered out
		// - all messages are shown or the topic is already on the list
		if (!filteredStore.filterMessage(message, true) && (allTopicsShown || filteredStore.getBrowsedTopics().contains(message.getTopic())))
		{
			filteredStore.getFilteredMessages().add(message);
			
			// Message browsing update
			uiEventQueue.add(this, new BrowseReceivedMessageEvent<T>(filteredStore.getFilteredMessages(), message));
		}

		// 4. If the topic doesn't exist yet, add it (e.g. all shown but this is the first message for this topic)
		if (allTopicsShown && !topicAlreadyExists)
		{
			// This doesn't need to trigger 'show first' or sth because the following two UI events should refresh the screen
			filteredStore.applyTopicFilter(message.getTopic(), false);	 
		}
			
		// 5. Summary table update - required are: removed message, new message, and whether to show the topic
		if (removed != null)
		{
			uiEventQueue.add(this, new TopicSummaryRemovedMessageEvent<T>(super.getMessageList(), removed));
		}
		uiEventQueue.add(this, new TopicSummaryNewMessageEvent<T>(super.getMessageList(), message, allTopicsShown && !topicAlreadyExists));
	}	
	
	@Override
	public List<T> getMessages()
	{		
		return filteredStore.getFilteredMessages().getMessages();
	}
	
	@Override
	public MessageListWithObservableTopicSummary<T> getMessageList()
	{
		return filteredStore.getFilteredMessages();
	}
	
	public MessageListWithObservableTopicSummary<T> getNonFilteredMessageList()
	{
		return super.getMessageList();
	}
	
	public FilteredMessageStore<T> getFilteredMessageStore()
	{
		return filteredStore;
	}	
	
	@Override
	public boolean browsingFiltersEnabled()
	{
		return filteredStore.getBrowsedTopics().size() != allTopics.size();
	}
	
	@Override
	public boolean messageFiltersEnabled()
	{
		return filteredStore.messageFiltersEnabled();
	}
	
	public Collection<String> getAllTopics()
	{
		return Collections.unmodifiableCollection(allTopics);
	}

	@Override
	public void clear()
	{
		super.clear();
		allTopics.clear();
		filteredStore.removeAllTopicFilters();
	}

	public void setAllShowValues(final boolean show)
	{
		if (show)
		{
			filteredStore.addAllTopicFilters();
		}
		else
		{
			filteredStore.removeAllTopicFilters();
		}
		
		super.getMessageList().getTopicSummary().setAllShowValues(show);
	}
	
	public void setShowValues(final boolean show, final Collection<String> topics)
	{		
		synchronized (topics)
		{
			if (show)
			{
				filteredStore.applyTopicFilters(topics, true);
			}
			else
			{
				filteredStore.removeTopicFilters(topics);
			}
			
			super.getMessageList().getTopicSummary().setShowValues(topics, show);
		}
	}
	
	public void toggleAllShowValues()
	{
		toggleShowValues(allTopics);
	}

	public void toggleShowValues(final Collection<String> topics)
	{
		final Set<String> topicsToAdd = new HashSet<>();
		final Set<String> topicsToRemove = new HashSet<>();
		
		synchronized (topics)
		{
			for (final String topic : topics)
			{		
				if (filteredStore.getBrowsedTopics().contains(topic))				
				{
					topicsToRemove.add(topic);				
				}
				else
				{
					topicsToAdd.add(topic);
				}
			}
			
			filteredStore.removeTopicFilters(topicsToRemove);
			filteredStore.applyTopicFilters(topicsToAdd, true);
			
			super.getMessageList().getTopicSummary().toggleShowValues(topics);
		}
	}

	public void setShowValue(final String topic, final boolean show)
	{
		filteredStore.updateTopicFilter(topic, show);
		super.getMessageList().getTopicSummary().setShowValue(topic, show);
	}
		
	public EventQueueManager<T> getUiEventQueue()
	{
		return this.uiEventQueue;
	}

	public FormattingManager getFormattingManager()
	{
		return formattingManager;
	}
	
	public void cleanUp()
	{
		this.getMessageStoreGarbageCollector().setRunning(false);
		this.filteredStore.getMessageStoreGarbageCollector().setRunning(false);
	}
}
