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
 *   http://www.eclipse.org/org/documents/edl-v10.php
 *
 * Contributors:
 * 
 *    Kamil Baczkowicz - initial API and implementation and/or initial documentation
 *    
 */
package pl.baczkowicz.mqttspy.connectivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.logger.MqttMessageLogger;
import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.ui.scripts.InteractiveMqttScriptManager;
import pl.baczkowicz.spy.common.generated.ScriptDetails;
import pl.baczkowicz.spy.connectivity.ConnectionStatus;
import pl.baczkowicz.spy.connectivity.ReconnectionManager;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.formatting.FormattingManager;
import pl.baczkowicz.spy.scripts.Script;
import pl.baczkowicz.spy.ui.connections.IUiConnection;
import pl.baczkowicz.spy.ui.events.ConnectionStatusChangeEvent;
import pl.baczkowicz.spy.ui.events.queuable.EventQueueManager;
import pl.baczkowicz.spy.ui.stats.StatisticsManager;
import pl.baczkowicz.spy.ui.storage.ManagedMessageStoreWithFiltering;
import pl.baczkowicz.spy.utils.ConversionUtils;

/**
 * Asynchronous MQTT connection with the extra UI elements required.
 */
public class MqttAsyncConnection extends MqttConnectionWithReconnection implements IUiConnection
{
	private final static Logger logger = LoggerFactory.getLogger(MqttAsyncConnection.class);

	private final MqttRuntimeConnectionProperties properties;
	
	private boolean isOpened;
	
	private boolean isOpening;
	
	private final ManagedMessageStoreWithFiltering<FormattedMqttMessage> store;

	/** Maximum number of messages to store for this connection in each message store. */
	private int preferredStoreSize;

	private StatisticsManager statisticsManager;

	// private final EventManager<FormattedMqttMessage> eventManager;
	
	private IKBus eventBus;

	private final InteractiveMqttScriptManager scriptManager;

	private MqttMessageLogger messageLogger;

	public MqttAsyncConnection(final ReconnectionManager reconnectionManager, final MqttRuntimeConnectionProperties properties, 
			final ConnectionStatus status, final IKBus eventBus,
			final InteractiveMqttScriptManager scriptManager, final FormattingManager formattingManager,
			final EventQueueManager<FormattedMqttMessage> uiEventQueue, final int summaryMaxPayloadLength)
	{ 
		super(reconnectionManager, properties);
		setScriptManager(scriptManager);
		
		// Max size is double the preferred size
		store = new ManagedMessageStoreWithFiltering<FormattedMqttMessage>(properties.getName(), 
				properties.getConfiguredProperties().getMinMessagesStoredPerTopic(), 
				properties.getMaxMessagesStored(), 
				properties.getMaxMessagesStored() * 2, 
				uiEventQueue,
				formattingManager,
				summaryMaxPayloadLength);
		
		this.setPreferredStoreSize(properties.getMaxMessagesStored());
		this.properties = properties;
		this.eventBus = eventBus;
		this.scriptManager = scriptManager;
		setConnectionStatus(status);
	}
	
	public void messageReceived(final FormattedMqttMessage receivedMessage)
	{		
		// TODO: we should only delete from the topic matcher when a subscription is closed for good, not when just unsubscribed
		final List<String> matchingSubscriptionTopics = getTopicMatcher().getMatchingSubscriptions(receivedMessage.getTopic());
		logger.trace("Matching subscriptions = " + matchingSubscriptionTopics);		
		
		final List<String> matchingActiveSubscriptions = new ArrayList<String>();
		
		// This will modify the message if there is an onMessage
		final BaseMqttSubscription lastMatchingSubscription = 
				matchMessageToSubscriptions(matchingSubscriptionTopics, receivedMessage, matchingActiveSubscriptions);
		
		// TODO: should the copy be created after the subscription processing?
		final FormattedMqttMessage message = new FormattedMqttMessage(receivedMessage);		
				
		// If logging is enabled
		if (messageLogger != null && messageLogger.isRunning())
		{
			message.setMatchingSubscriptionTopics(matchingActiveSubscriptions);
			messageLogger.getQueue().add(message);
		}
		
		statisticsManager.messageReceived(getId(), matchingActiveSubscriptions);

		if (lastMatchingSubscription != null)
		{
			message.setSubscription(lastMatchingSubscription.getTopic());
		} 
		else
		{
			logger.warn("Cannot find a matching subscription for " + receivedMessage.getTopic());
		}
		
		// Pass the message to the "all" message store		
		store.messageReceived(message);
	}
	
	private BaseMqttSubscription matchMessageToSubscriptions(final List<String> matchingSubscriptionTopics, final FormattedMqttMessage receivedMessage, 
			final List<String> matchingActiveSubscriptions)
	{
		BaseMqttSubscription lastMatchingSubscription = matchMessageToSubscriptions(
				matchingSubscriptionTopics, receivedMessage, matchingActiveSubscriptions, false);
		
		// If no active subscriptions available, use the first one that matches (as we might be still receiving messages for a non-active subscription)
		if (matchingActiveSubscriptions.isEmpty())
		{
			logger.debug("No active subscription available for {}, trying to find first matching...", receivedMessage.getTopic());
			lastMatchingSubscription = matchMessageToSubscriptions(matchingSubscriptionTopics, receivedMessage, matchingActiveSubscriptions, true);
			logger.debug("First matching = {} {}", lastMatchingSubscription, matchingSubscriptionTopics);
		}
		
		return lastMatchingSubscription;
	}
	
	private BaseMqttSubscription matchMessageToSubscriptions(final List<String> matchingSubscriptionTopics, final FormattedMqttMessage receivedMessage, 
			final List<String> matchingSubscriptions, final boolean anySubscription)
	{
		BaseMqttSubscription foundMqttSubscription = null;
		
		// For all found subscriptions
		for (final String matchingSubscriptionTopic : matchingSubscriptionTopics)
		{					
			logger.trace("Message on topic " + receivedMessage.getTopic() + " matched to " + matchingSubscriptionTopic);
			
			// Get the mqtt-spy's subscription object
			final BaseMqttSubscription mqttSubscription = getMqttSubscriptionForTopic(matchingSubscriptionTopic);

			// If a match has been found, and the subscription is active or we don't care
			if (mqttSubscription != null && (anySubscription || mqttSubscription.isSubscribing() || mqttSubscription.isActive()))
			{
				matchingSubscriptions.add(matchingSubscriptionTopic);
				
				// Set subscription reference on the message
				receivedMessage.setSubscription(mqttSubscription.getTopic());
				foundMqttSubscription = mqttSubscription;
				
				// Run the script first, modify the source object
				if (mqttSubscription.isScriptActive())
				{
					scriptManager.runScriptWithReceivedMessage(mqttSubscription.getScript(), receivedMessage);
				}
				
				// Create a copy of the message for each subscription
				final FormattedMqttMessage message = new FormattedMqttMessage(receivedMessage);
								
				// Pass the message for subscription handling
				mqttSubscription.getStore().messageReceived(message);
				
				// Find only one matching subscription if checking non-active ones
				if (anySubscription)
				{
					logger.trace("Found one match - exiting...");
					break;
				}
			}
		}			
		
		return foundMqttSubscription;
	}
	
	public boolean publish(final String publicationTopic, final String data, final int qos)
	{
		return publish(publicationTopic, ConversionUtils.stringToArray(data), qos, false);
	}
	
	public boolean publish(final String publicationTopic, final String data, final int qos, final boolean retained)
	{
		return publish(publicationTopic, ConversionUtils.stringToArray(data), qos, retained);
	}
	
	public boolean publish(final String publicationTopic, final byte[] data, final int qos, final boolean retained)
	{
		if (canPublish())
		{
			try
			{
				logger.info("Publishing message on topic \"" + publicationTopic + "\". Payload size = \"" + data.length + "\"");
				client.publish(publicationTopic, data, qos, retained);
				
				logger.trace("Published message on topic \"" + publicationTopic + "\". Payload size = \"" + data.length + "\"");
				statisticsManager.messagePublished(getId(), publicationTopic);
				
				return true;
			}
			catch (MqttException e)
			{
				logger.error("Cannot publish message on " + publicationTopic, e);
			}
		}
		else
		{
			logger.warn("Publication attempt failure - no connection available...");
		}
		
		return false;
	}

	public void connectionLost(Throwable cause)
	{
		super.connectionLost(cause);
		unsubscribeAll(false);
	}

	public void startBackgroundScripts()	
	{
		final boolean firstConnection = getConnectionAttempts() == 1;
		
		// Attempt starting background scripts
		if (firstConnection)
		{
			for (final ScriptDetails scriptDetails : properties.getConfiguredProperties().getBackgroundScript())
			{
				final File scriptFile = new File(scriptDetails.getFile());							
				final Script script = scriptManager.getScriptsMap().get(Script.getScriptIdFromFile(scriptFile));
				
				if (scriptDetails.isAutoStart() && scriptFile.exists() && script != null)
				{					
					scriptManager.runScript(script, true);
				}
				else if (!scriptFile.exists())
				{
					logger.warn("File " + scriptDetails.getFile() + " does not exist");
				}
				else if (script == null)
				{
					logger.warn("Couldn't retrieve a script for " + scriptDetails.getFile());
				}					
			}
		}
	}

	public boolean resubscribeAll(final boolean requestedOnly)
	{
		final boolean firstConnection = getConnectionAttempts() == 1;
		final boolean resubscribeEnabled = connectionDetails.getReconnectionSettings() != null 
				&& connectionDetails.getReconnectionSettings().isResubscribe();
		
		final boolean tryAutoSubscribe = firstConnection || resubscribeEnabled;
			
		// Attempt re-subscription
		for (final BaseMqttSubscription subscription : subscriptions.values())
		{
			logger.debug("Subscription {} status [requestedOnly = {}, firstConnection = {}, resubscribeEnabled = {}, subscriptionRequested = {}", 
					subscription.getTopic(), requestedOnly, firstConnection, resubscribeEnabled, subscription.getSubscriptionRequested());
			
			if (!requestedOnly || (tryAutoSubscribe && subscription.getSubscriptionRequested()))
			{
				resubscribe(subscription);
			}
		}		

		return true;
	}

	public boolean resubscribe(final BaseMqttSubscription subscription)
	{
		return subscribe(subscription);
	}


	public boolean unsubscribeAll(final boolean manualOverride)
	{
		// Copy the set of values so that we can start removing them from the 'subscriptions' map
		final Set<BaseMqttSubscription> allSubscriptions = new HashSet<>(subscriptions.values()); 
		
		for (final BaseMqttSubscription subscription : allSubscriptions)
		{
			unsubscribe(subscription, manualOverride);
		}

		return true;
	}

	public boolean unsubscribe(final BaseMqttSubscription subscription, final boolean manualOverride)
	{
		// If this is a user action, set it not to auto-subscribe
		if (manualOverride && subscription.getSubscriptionRequested())
		{
			subscription.setSubscriptionRequested(false);
		}
		
		// If already unsubscribed, ignore
		if (!subscription.isActive())
		{
			return false;
		}

		logger.debug("Unsubscribing from " + subscription.getTopic());
		removeSubscriptionFromMatcher(subscription);
		
		final boolean unsubscribed = unsubscribe(subscription.getTopic());

		// Run 'after' for script - TODO: move to BaseMqttConnection?
		if (subscription.isScriptActive())
		{
			scriptManager.invokeAfter(subscription.getScript());
		}
		
		subscription.setActive(false);
		logger.trace("Subscription " + subscription.getTopic() + " is active = " + subscription.isActive());

		return unsubscribed;
	}

	public boolean unsubscribeAndRemove(final BaseMqttSubscription subscription)
	{
		final boolean unsubscribed = unsubscribe(subscription, true);
		removeSubscription(subscription);
		logger.info("Subscription " + subscription.getTopic() + " removed");
		return unsubscribed;
	}
	
	@Override
	public boolean subscribe(final BaseMqttSubscription subscription)
	{
		final boolean subscribed = super.subscribe(subscription);
		
		if (subscribed)
		{
			StatisticsManager.newSubscription();
		}
		
		return subscribed;
	}

	public void setConnectionStatus(ConnectionStatus connectionStatus)
	{
		super.setConnectionStatus(connectionStatus);
		
		eventBus.publish(new ConnectionStatusChangeEvent(this, this.getName(), this.getConnectionStatus()));
	}

	public MqttRuntimeConnectionProperties getProperties()
	{
		return properties;
	}

	public Map<String, BaseMqttSubscription> getSubscriptions()
	{
		return subscriptions;
	}

	public int getPreferredStoreSize()
	{
		return preferredStoreSize;
	}

	public void setPreferredStoreSize(int preferredStoreSize)
	{
		this.preferredStoreSize = preferredStoreSize;
	}
	
	public String getId()
	{
		return properties.getId();
	}

	public boolean isOpened()
	{
		return isOpened;
	}
	
	public void closeConnection()
	{
		setOpened(false);
	}

	public void setOpened(boolean isOpened)
	{
		this.isOpened = isOpened;
		
		eventBus.publish(new ConnectionStatusChangeEvent(this, this.getName(), this.getConnectionStatus()));
	}

	public boolean isOpening()
	{
		return isOpening;
	}

	public void setOpening(boolean isOpening)
	{
		this.isOpening = isOpening;
		
		eventBus.publish(new ConnectionStatusChangeEvent(this, this.getName(), this.getConnectionStatus()));
	}

	public ManagedMessageStoreWithFiltering<FormattedMqttMessage> getStore()
	{
		return store;
	}

	public String getName()
	{
		return store.getName();
	}	

	public void setStatisticsManager(final StatisticsManager statisticsManager)
	{
		this.statisticsManager = statisticsManager;
	}

	public InteractiveMqttScriptManager getScriptManager()
	{
		return this.scriptManager;
	}

	public void setMessageLogger(final MqttMessageLogger messageLogger)
	{
		this.messageLogger = messageLogger;
	}
	
	public MqttMessageLogger getMessageLogger()
	{
		return messageLogger;
	}
}
