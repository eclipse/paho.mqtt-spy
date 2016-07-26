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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.moquette.spi.ISessionsStore;
import org.eclipse.moquette.spi.impl.subscriptions.Subscription;

/**
 * Simple implementation of the ISessionsStore as required by moquette.
 */
public class MapBasedSubscriptionStore implements ISessionsStore
{
	/** List of subscriptions. */
	private final Map<String, List<Subscription>> subscriptions = new HashMap<String, List<Subscription>>();

	@Override
	public void wipeSubscriptions(final String clientID)
	{
		subscriptions.remove(clientID);
	}

	@Override
	public List<Subscription> listAllSubscriptions()
	{
		List<Subscription> list = new ArrayList<Subscription>();
		
		for (final List<Subscription>clientSubscriptions : subscriptions.values())
		{
			list.addAll(clientSubscriptions);
		}
		
		return list;
	}

    /**
     * Add a new subscription to the session.
     */
	@Override
    public void addNewSubscription(final Subscription newSubscription)
    {
		final String clientID = newSubscription.getClientId();
		
		List<Subscription> clientSubscriptions = subscriptions.get(clientID);
		
		if (clientSubscriptions == null)
		{
			clientSubscriptions = new ArrayList<Subscription>();
			subscriptions.put(clientID, clientSubscriptions);
		}
		
		clientSubscriptions.add(newSubscription);
    }

    /**
     * @return true if there are subscriptions persisted with clientID.
     */
	@Override
	public boolean contains(final String clientID)
	{
		return subscriptions.containsKey(clientID);
	}
	
    /**
     * Remove a specific subscription.
     */
	@Override
	public void removeSubscription(final String topic, final String clientID)
	{
		final List<Subscription> clientSubscriptions = subscriptions.get(clientID);
				
		if (clientSubscriptions != null)
		{
			clientSubscriptions.remove(topic);
		}
	}

	@Override
	public void updateSubscriptions(final String clientID, final Set<Subscription> subscriptions)
	{
		// Not used		
	}
}
