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
package pl.baczkowicz.mqttspy.connectivity.topicmatching;

import io.moquette.spi.ISessionsStore.ClientTopicCouple;
import io.moquette.spi.impl.subscriptions.Subscription;
import io.moquette.spi.impl.subscriptions.SubscriptionsStore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for matching topics against subscriptions, and
 * figure out which subscription the message has been received for. It uses
 * moquette's SubscriptionStore to achieve that.
 */
public class TopicMatcher
{
	/** Diagnostic logger. */
	private static final Logger logger = LoggerFactory.getLogger(TopicMatcher.class);
	
	/** Subscription store - used to matching topics against subscriptions - from moquette. */
	private SubscriptionsStore subscriptionsStore;
	
	/** All topics that are in the store. */
	private Set<String> topics = new HashSet<>();
	
	/**
	 * Creates the topic matcher.
	 */
	public TopicMatcher()
	{
		// Manage subscriptions, uses moquette's SubscriptionsStore
		subscriptionsStore = new SubscriptionsStore();
		subscriptionsStore.init(new MapBasedSubscriptionStore());
	}
	
	/**
	 * Returns matching subscriptions for the given topic.
	 * 
	 * @param topic The topic to get active subscriptions for
	 * 
	 * @return List of subscription topics matching the given topic
	 */
	public List<String> getMatchingSubscriptions(final String topic)
	{		
		// Check matching subscription
		final List<Subscription> matchingSubscriptions = subscriptionsStore.matches(topic);
		
		final List<String> matchingSubscriptionTopics = new ArrayList<String>();
		
		// For all found subscriptions
		for (final Subscription matchingSubscription : matchingSubscriptions)
		{						
			matchingSubscriptionTopics.add(matchingSubscription.getTopicFilter());
		}		

		return matchingSubscriptionTopics;
	}

	/**
	 * Adds the given topic to the subscription store - used for topic to subscription matching.
	 *  
	 * @param topic Topic to add
	 */
	public void addSubscriptionToStore(final String topic, final String clientId)
	{
		final ClientTopicCouple subscription = new ClientTopicCouple(clientId, topic);
		
		if (!topics.contains(topic))
		{
			logger.debug("Added subscription " + topic + " (" + clientId + ") to store");
			// Store the subscription topic for further matching
			subscriptionsStore.add(subscription);
			topics.add(topic);
		}
	}
	
	/**
	 * Removes the given topic from the subscription store - used for topic to subscription matching.
	 *  
	 * @param topic Topic to remove
	 */
	public void removeSubscriptionFromStore(final String topic, final String clientId)
	{
		subscriptionsStore.removeSubscription(topic, clientId);
		
		topics.remove(topic);
	}
}
