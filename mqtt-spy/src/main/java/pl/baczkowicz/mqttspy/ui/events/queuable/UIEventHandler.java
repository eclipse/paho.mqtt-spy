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
package pl.baczkowicz.mqttspy.ui.events.queuable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.storage.MessageList;
import pl.baczkowicz.spy.ui.events.MessageAddedEvent;
import pl.baczkowicz.spy.ui.events.MessageRemovedEvent;
import pl.baczkowicz.spy.ui.events.queuable.EventQueueManager;
import pl.baczkowicz.spy.ui.events.queuable.ui.BrowseReceivedMessageEvent;
import pl.baczkowicz.spy.ui.events.queuable.ui.BrowseRemovedMessageEvent;
import pl.baczkowicz.spy.ui.events.queuable.ui.SpyUIEvent;
import pl.baczkowicz.spy.ui.events.queuable.ui.TopicSummaryNewMessageEvent;
import pl.baczkowicz.spy.ui.events.queuable.ui.TopicSummaryRemovedMessageEvent;
import pl.baczkowicz.spy.utils.ThreadingUtils;
import pl.baczkowicz.spy.utils.TimeUtils;

/**
 * This class is responsible for handling queued events. This is done in batches
 * for improved performance. So rather than flooding JavaFX with hundreds or
 * thousands of requests to do runLater, we buffer those events, and then
 * process them in batches.
 */
public class UIEventHandler implements Runnable
{
	final static Logger logger = LoggerFactory.getLogger(UIEventHandler.class);
	
	private final EventQueueManager<FormattedMqttMessage> uiEventQueue;

	private IKBus eventBus;

	public UIEventHandler(final EventQueueManager<FormattedMqttMessage> uiEventQueue, final IKBus eventBus)
	{
		this.uiEventQueue = uiEventQueue;
		this.eventBus = eventBus;
	}

	@Override
	public void run()
	{
		ThreadingUtils.logThreadStarting("UI event handler");
		
		while (true)
		{
			if (uiEventQueue.getEventCount() > 0)
			{
				showUpdates();
			}
			
			// Sleep so that we don't run all the time - updating the UI 10 times a second should be more than enough
			if (ThreadingUtils.sleep(100))			
			{
				break;
			}
		}		
		
		ThreadingUtils.logThreadEnding();
	}

	private void showUpdates()
	{
		final long start = TimeUtils.getMonotonicTime();
		long processed = 0;
		while (uiEventQueue.getEventCount() > 0)
		{
			final Map<String, List<SpyUIEvent<FormattedMqttMessage>>> events = uiEventQueue.getEvents();
			{
				for (final String type : events.keySet())
				{
					// Remove the event queue from the manager
					final List<SpyUIEvent<FormattedMqttMessage>> eventQueue = uiEventQueue.getAndRemoveEvents(type);
					
					if (eventQueue.isEmpty())
					{
						continue;
					}
					
					processed = processed + eventQueue.size();
					processEventType(eventQueue);					
				}
			}								
		}	
		final long end = TimeUtils.getMonotonicTime();
		if (logger.isTraceEnabled())
		{
			logger.trace("UI event handling of {} items took {} ms", processed, (end - start));
		}
	}
	
	private void processEventType(final List<SpyUIEvent<FormattedMqttMessage>> eventQueue)
	{
		// Split by parent
		final Map<MessageList<FormattedMqttMessage>, List<SpyUIEvent<FormattedMqttMessage>>> parentToEvent = new HashMap<>();		
		for (final SpyUIEvent<FormattedMqttMessage> event : eventQueue)
		{
			List<SpyUIEvent<FormattedMqttMessage>> parentQueue = parentToEvent.get(event.getList());
			
			if (parentQueue == null)
			{
				parentQueue = new ArrayList<>();
				parentToEvent.put(event.getList(), parentQueue);
			}
			
			parentToEvent.get(event.getList()).add(event);
		}

		// Process in batches
		for (final MessageList<FormattedMqttMessage> parent : parentToEvent.keySet())
		{
			Platform.runLater(new Runnable()
			{				
				@Override
				public void run()
				{					
					handleEvents(parentToEvent.get(parent));
				}
			});			
		}		
	}

	@SuppressWarnings("unchecked")
	private void handleEvents(final List<SpyUIEvent<FormattedMqttMessage>> eventQueue)
	{
		final SpyUIEvent<FormattedMqttMessage> event = eventQueue.get(0);
		
		if (event instanceof BrowseReceivedMessageEvent)
		{
			eventBus.publish(new MessageAddedEvent<>((List<BrowseReceivedMessageEvent<FormattedMqttMessage>>)(Object)eventQueue, 
					((BrowseReceivedMessageEvent<FormattedMqttMessage>) event).getList()));
		}
		else if (event instanceof BrowseRemovedMessageEvent)
		{
			eventBus.publish(new MessageRemovedEvent<>((List<BrowseRemovedMessageEvent<FormattedMqttMessage>>)(Object)eventQueue, 
					event.getList()));
		}
		else if (event instanceof TopicSummaryNewMessageEvent)
		{
			for (final SpyUIEvent<FormattedMqttMessage> item : eventQueue)
			{
				handleTopicSummaryNewMessageEvent((TopicSummaryNewMessageEvent<FormattedMqttMessage>) item);
			}
		}
		else if (event instanceof TopicSummaryRemovedMessageEvent)
		{
			for (final SpyUIEvent<FormattedMqttMessage> item : eventQueue)
			{
				handleTopicSummaryRemovedMessageEvent((TopicSummaryRemovedMessageEvent<FormattedMqttMessage>) item);
			}			
		}
	}
	
	private void handleTopicSummaryNewMessageEvent(final TopicSummaryNewMessageEvent<FormattedMqttMessage> updateEvent)
	{
		// Calculate the overall message count per topic
		updateEvent.getList().getTopicSummary().addMessage(updateEvent.getAdded());
		
		// Update the 'show' property if required
		if (updateEvent.isShowTopic())
		{			
			updateEvent.getList().getTopicSummary().setShowValue(updateEvent.getAdded().getTopic(), true);											
		}
	}
	
	private void handleTopicSummaryRemovedMessageEvent(final TopicSummaryRemovedMessageEvent<FormattedMqttMessage> removeEvent)
	{
		// Remove old message from stats
		if (removeEvent.getRemoved() != null)
		{
			// TODO: does this actually work?
			removeEvent.getList().getTopicSummary().removeMessage(removeEvent.getRemoved());
		}
	}
}
