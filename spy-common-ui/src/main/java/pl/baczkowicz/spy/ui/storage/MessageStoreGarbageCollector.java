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

import pl.baczkowicz.spy.messages.FormattedMessage;
import pl.baczkowicz.spy.ui.events.queuable.EventQueueManager;
import pl.baczkowicz.spy.ui.events.queuable.ui.BrowseRemovedMessageEvent;
import pl.baczkowicz.spy.ui.events.queuable.ui.TopicSummaryRemovedMessageEvent;
import pl.baczkowicz.spy.utils.ThreadingUtils;

/**
 * This class is responsible for deleting old messages from memory, so we don't
 * use too much of it. This is particularly important when processing 100s or
 * 1000s messages per second.
 */
public class MessageStoreGarbageCollector<T extends FormattedMessage> implements Runnable
{
	/** Stores events for the UI to be updated. */
	protected final EventQueueManager<T> uiEventQueue;
	
	private MessageListWithObservableTopicSummary<T> messages;
	
	private int minMessagesPerTopic;

	private boolean createTopicSummaryEvents;

	private boolean createBrowseEvents;

	private ManagedMessageStoreWithFiltering<T> store;

	private boolean running;
	
	public MessageStoreGarbageCollector(final ManagedMessageStoreWithFiltering<T> store, final MessageListWithObservableTopicSummary<T> messages, 
			final EventQueueManager<T> uiEventQueue, 
			final int minMessages, final boolean createTopicSummaryEvents, final boolean createBrowseEvents)
	{
		this.messages = messages;
		this.uiEventQueue = uiEventQueue;
		this.minMessagesPerTopic = minMessages;
		this.createTopicSummaryEvents = createTopicSummaryEvents;
		this.createBrowseEvents = createBrowseEvents;
		this.store = store;
	}
	
	public void setRunning(final boolean running)
	{
		this.running = running;
	}
	
	private void checkAndRemove(boolean shouldRemove)
	{
		// logger.trace("[{}] Checking if can delete messages...", messages.getName());
		for (int i = messages.getMessages().size() - 1; i >=0; i--)				
		{
			final T element = messages.getMessages().get(i);
								
			final int count = messages.getTopicSummary().getCountForTopic(element.getTopic());
			if (count > minMessagesPerTopic)
			{	
				// Remove from the store
				messages.remove(i);
				shouldRemove = messages.exceedingPreferredSize();
										
				// Update topic summary and UI

				// Remove events are for the normal store
				if (createTopicSummaryEvents)
				{
					uiEventQueue.add(store, new TopicSummaryRemovedMessageEvent<T>(messages, element));
				}
				
				// Index update are for the filtered store
				if (createBrowseEvents)
				{
					uiEventQueue.add(store, new BrowseRemovedMessageEvent<T>(messages, element, i + 1));
				}
				
				if (!shouldRemove)
				{
					break;
				}
			}				
		}
	}
	
	@Override
	public void run()
	{
		running = true;
		
		ThreadingUtils.logThreadStarting("Message Store Garbage Collector for " + messages.getName());
				
		while (running)		
		{			
			if (ThreadingUtils.sleep(1000))			
			{
				break;
			}
			
			synchronized (messages.getMessages())
			{
				boolean shouldRemove =  messages.exceedingPreferredSize();
				
				if (!shouldRemove)
				{
					continue;
				}
				
				checkAndRemove(shouldRemove);
			}
		}
		ThreadingUtils.logThreadEnding();
	}
}
