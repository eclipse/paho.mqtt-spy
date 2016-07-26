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
package pl.baczkowicz.mqttspy.ui.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.connectivity.MqttSubscription;
import pl.baczkowicz.mqttspy.ui.events.observers.ConnectionStatusChangeObserver;
import pl.baczkowicz.mqttspy.ui.events.observers.SubscriptionStatusChangeObserver;
import pl.baczkowicz.mqttspy.ui.events.observers.VersionInfoObserver;
import pl.baczkowicz.mqttspy.versions.generated.MqttSpyVersions;
import pl.baczkowicz.spy.messages.FormattedMessage;
import pl.baczkowicz.spy.scripts.IScriptEventManager;
import pl.baczkowicz.spy.scripts.ScriptRunningState;
import pl.baczkowicz.spy.storage.MessageList;
import pl.baczkowicz.spy.storage.MessageStore;
import pl.baczkowicz.spy.ui.events.observers.ClearTabObserver;
import pl.baczkowicz.spy.ui.events.observers.MessageAddedObserver;
import pl.baczkowicz.spy.ui.events.observers.MessageFormatChangeObserver;
import pl.baczkowicz.spy.ui.events.observers.MessageIndexChangeObserver;
import pl.baczkowicz.spy.ui.events.observers.MessageIndexIncrementObserver;
import pl.baczkowicz.spy.ui.events.observers.MessageIndexToFirstObserver;
import pl.baczkowicz.spy.ui.events.observers.MessageListChangedObserver;
import pl.baczkowicz.spy.ui.events.observers.MessageRemovedObserver;
import pl.baczkowicz.spy.ui.events.observers.ScriptListChangeObserver;
import pl.baczkowicz.spy.ui.events.observers.ScriptStateChangeObserver;
import pl.baczkowicz.spy.ui.events.queuable.ui.BrowseReceivedMessageEvent;
import pl.baczkowicz.spy.ui.events.queuable.ui.BrowseRemovedMessageEvent;
import pl.baczkowicz.spy.ui.storage.ManagedMessageStoreWithFiltering;
import pl.baczkowicz.spy.ui.storage.MessageListWithObservableTopicSummary;

/**
 * There are two ways events are distributed around the application. First,
 * using the EventManager - this uses observers, registrations and
 * notifications. Second is by using individual events, that can be buffered on
 * a queue or list - see the pl.baczkowicz.mqttspy.events.queuable package.
 */
public class EventManager<T extends FormattedMessage> implements IScriptEventManager
{
	private final Map<MessageAddedObserver<T>, MessageListWithObservableTopicSummary<T>> messageAddedObservers = new HashMap<>();
	
	private final Map<MessageRemovedObserver<T>, MessageList<T>> messageRemovedObservers = new HashMap<>();
	
	private final Map<MessageListChangedObserver, MessageListWithObservableTopicSummary<T>> messageListChangeObservers = new HashMap<>();
	
	private final Map<ConnectionStatusChangeObserver, MqttAsyncConnection> connectionStatusChangeObservers = new HashMap<>();
	
	private final Map<SubscriptionStatusChangeObserver, MqttSubscription> subscriptionStatusChangeObservers = new HashMap<>();
	
	private final Map<ClearTabObserver<T>, ManagedMessageStoreWithFiltering<T>> clearTabObservers = new HashMap<>();

	private final Map<MessageIndexChangeObserver, MessageStore<T>> changeMessageIndexObservers = new HashMap<>();
	
	private final Map<MessageIndexToFirstObserver, MessageStore<T>> displayFirstMessageObservers = new HashMap<>();
	
	private final Map<MessageIndexIncrementObserver, MessageStore<T>> incrementMessageIndexObservers = new HashMap<>();
	
	private final Map<MessageFormatChangeObserver, MessageStore<T>> formatChangeObservers = new HashMap<>();
	
	private final Map<ScriptStateChangeObserver, String> scriptStateChangeObservers = new HashMap<>();
	
	private final Map<ScriptListChangeObserver, MqttAsyncConnection> scriptListChangeObservers = new HashMap<>();

	private final Map<VersionInfoObserver, Object> versionInfoObservers = new HashMap<>();
	
	/**
	 * 
	 * Registers an observer for connection status changes.
	 * 
	 * @param observer The observer to register
	 * @param filter Null for all, or value to match
	 */
	public void registerConnectionStatusObserver(final ConnectionStatusChangeObserver observer, final MqttAsyncConnection filter)
	{
		connectionStatusChangeObservers.put(observer, filter);
	}
	
	public void registerMessageAddedObserver(final MessageAddedObserver<T> observer, final MessageListWithObservableTopicSummary<T> filter)
	{
		messageAddedObservers.put(observer, filter);
	}
	
	public void deregisterMessageAddedObserver(final MessageAddedObserver<T> observer)
	{
		messageAddedObservers.remove(observer);
	}
	
	public void registerMessageRemovedObserver(final MessageRemovedObserver<T> observer, final MessageList<T> filter)
	{
		messageRemovedObservers.put(observer, filter);
	}
	
	public void registerMessageListChangedObserver(final MessageListChangedObserver observer, final MessageListWithObservableTopicSummary<T> filter)
	{
		messageListChangeObservers.put(observer, filter);
	}
	
	public void registerSubscriptionStatusObserver(final SubscriptionStatusChangeObserver observer, final MqttSubscription filter)
	{
		subscriptionStatusChangeObservers.put(observer, filter);
	}
	
	public void deregisterConnectionStatusObserver(final ConnectionStatusChangeObserver observer)
	{
		connectionStatusChangeObservers.remove(observer);
	}
	
	public void registerClearTabObserver(final ClearTabObserver<T> observer, final ManagedMessageStoreWithFiltering<T> filter)
	{
		clearTabObservers.put(observer, filter);
	}
	
	public void registerChangeMessageIndexObserver(final MessageIndexChangeObserver observer, final MessageStore<T> filter)
	{
		changeMessageIndexObservers.put(observer, filter);
	}
	
	public void registerChangeMessageIndexFirstObserver(final MessageIndexToFirstObserver observer, final MessageStore<T> filter)
	{
		displayFirstMessageObservers.put(observer, filter);
	}
	
	public void registerIncrementMessageIndexObserver(final MessageIndexIncrementObserver observer, final MessageStore<T> filter)
	{
		incrementMessageIndexObservers.put(observer, filter);
	}
	
	public void registerFormatChangeObserver(final MessageFormatChangeObserver observer, final MessageStore<T> filter)
	{
		formatChangeObservers.put(observer, filter);
	}
	
	public void deregisterFormatChangeObserver(MessageFormatChangeObserver observer)
	{
		formatChangeObservers.remove(observer);		
	}
	
	public void registerScriptStateChangeObserver(final ScriptStateChangeObserver observer, final String filter)
	{
		scriptStateChangeObservers.put(observer, filter);
	}
	
	public void registerScriptListChangeObserver(final ScriptListChangeObserver observer, final MqttAsyncConnection filter)
	{
		scriptListChangeObservers.put(observer, filter);
	}	

	public void registerVersionInfoObserver(final VersionInfoObserver observer)
	{
		versionInfoObservers.put(observer, null);
		
	}
	
	public void notifyVersionInfoRetrieved(final MqttSpyVersions versions)
	{
		for (final VersionInfoObserver observer : versionInfoObservers.keySet())
		{
			observer.onVersionInfoReceived(versions);			
		}				
	}
	
	public void notifyVersionInfoError(final Exception e)
	{
		for (final VersionInfoObserver observer : versionInfoObservers.keySet())
		{
			observer.onVersionInfoError(e);			
		}				
	}
	
	public void notifyMessageAdded(final List<BrowseReceivedMessageEvent<T>> browseEvents, 
			final MessageListWithObservableTopicSummary<T> list)
	{
		for (final MessageAddedObserver<T> observer : messageAddedObservers.keySet())
		{
			final MessageListWithObservableTopicSummary<T> filter = messageAddedObservers.get(observer);
			
			if (filter == null || filter.equals(list))
			{				
				observer.onMessageAdded(browseEvents);
			}			
		}				
	}
	
	public void notifyMessageRemoved(final List<BrowseRemovedMessageEvent<T>> browseEvents, 
			final MessageList<T> messageList)
	{
		for (final MessageRemovedObserver<T> observer : messageRemovedObservers.keySet())
		{
			final MessageList<T> filter = messageRemovedObservers.get(observer);
			
			if (filter == null || filter.equals(messageList))
			{				
				//observer.onMessageRemoved(browseEvent.getMessage(), browseEvent.getMessageIndex());
				observer.onMessageRemoved(browseEvents);
			}			
		}		
	}
	
	public void notifyMessageListChanged(final MessageListWithObservableTopicSummary<T> list)
	{
		for (final MessageListChangedObserver observer : messageListChangeObservers.keySet())
		{
			final MessageListWithObservableTopicSummary<T> filter = messageListChangeObservers.get(observer);
			
			if (filter == null || filter.equals(list))
			{				
				observer.onMessageListChanged();
			}			
		}				
	}
	
	public void notifyConnectionStatusChanged(final MqttAsyncConnection changedConnection)
	{
		Platform.runLater(new Runnable()
		{			
			@Override
			public void run()
			{
				for (final ConnectionStatusChangeObserver observer : connectionStatusChangeObservers.keySet())
				{
					final MqttAsyncConnection filter = connectionStatusChangeObservers.get(observer);
					
					if (filter == null || filter.equals(changedConnection))
					{				
						observer.onConnectionStatusChanged(changedConnection);
					}
				}				
			}
		});		
	}
	
	public void notifySubscriptionStatusChanged(final MqttSubscription changedSubscription)
	{
		Platform.runLater(new Runnable()
		{			
			@Override
			public void run()
			{
				for (final SubscriptionStatusChangeObserver observer : subscriptionStatusChangeObservers.keySet())
				{
					final MqttSubscription filter = subscriptionStatusChangeObservers.get(observer);
					
					if (filter == null || filter.equals(changedSubscription))
					{				
						observer.onSubscriptionStatusChanged(changedSubscription);
					}
				}				
			}
		});		
		
	}
		
	public void notifyFormatChanged(final MessageStore<T> store)
	{
		Platform.runLater(new Runnable()
		{			
			@Override
			public void run()
			{
				for (final MessageFormatChangeObserver observer : formatChangeObservers.keySet())
				{
					final MessageStore<T> filter = formatChangeObservers.get(observer);
					
					if (filter == null || filter.equals(store))
					{				
						observer.onFormatChange();
					}
				}				
			}
		});		
	}
	
	public void navigateToFirst(final MessageStore<T> store)
	{
		for (final MessageIndexToFirstObserver observer : displayFirstMessageObservers.keySet())
		{
			final MessageStore<T> filter = displayFirstMessageObservers.get(observer);

			if (filter == null || filter.equals(store))
			{
				observer.onNavigateToFirst();
			}
		}
	}
	
	public void changeMessageIndex(final MessageStore<T> store, final Object dispatcher, final int index)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				for (final MessageIndexChangeObserver observer : changeMessageIndexObservers.keySet())
				{
					final MessageStore<T> filter = changeMessageIndexObservers.get(observer);

					if ((filter == null || filter.equals(store)) && (dispatcher != observer))
					{
						observer.onMessageIndexChange(index);
					}
				}
			}
		});
	}
	
	public void incrementMessageIndex(final MessageStore<T> store)
	{
		for (final MessageIndexIncrementObserver observer : incrementMessageIndexObservers.keySet())
		{
			final MessageStore<T> filter = incrementMessageIndexObservers.get(observer);

			if (filter == null || filter.equals(store))
			{
				observer.onMessageIndexIncrement(1);
			}
		}
	}

	public void notifyConfigurationFileWriteFailure()
	{
		// No action
	}

	public void notifyConfigurationFileCopyFailure()
	{
		// No action		
	}

	public void notifyConfigurationFileReadFailure()
	{
		// No action
	}

	public void notifyClearHistory(final ManagedMessageStoreWithFiltering<T> store)
	{
		for (final ClearTabObserver<T> observer : clearTabObservers.keySet())
		{
			final ManagedMessageStoreWithFiltering<T> filter = clearTabObservers.get(observer);
			
			if (filter == null || filter.equals(store))
			{
				observer.onClearTab(store);
			}
		}
	}

	public void notifyScriptStateChange(final String scriptName, final ScriptRunningState state)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				for (final ScriptStateChangeObserver observer : scriptStateChangeObservers.keySet())
				{
					final String filter = scriptStateChangeObservers.get(observer);

					if (filter == null || filter.equals(scriptName))
					{
						observer.onScriptStateChange(scriptName, state);
					}
				}
			}
		});		
	}

	public void notifyScriptListChange(final MqttAsyncConnection connection)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				for (final ScriptListChangeObserver observer : scriptListChangeObservers.keySet())
				{
					final MqttAsyncConnection filter = scriptListChangeObservers.get(observer);

					if (filter == null || filter.equals(connection))
					{
						observer.onScriptListChange();
					}
				}
			}
		});
	}
}
