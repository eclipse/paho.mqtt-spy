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

import io.moquette.proto.messages.AbstractMessage.QOSType;
import io.moquette.spi.ClientSession;
import io.moquette.spi.ISessionsStore;
import io.moquette.spi.impl.subscriptions.Subscription;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple implementation of the ISessionsStore as required by moquette.
 */
public class MapBasedSubscriptionStore implements ISessionsStore
{
	/** List of subscriptions. */
	private final Map<String, List<ClientTopicCouple>> subscriptions = new HashMap<>();

	@Override
	public void wipeSubscriptions(final String clientID)
	{
		subscriptions.remove(clientID);
	}

	@Override
	public List<ClientTopicCouple> listAllSubscriptions()
	{
		List<ClientTopicCouple> list = new ArrayList<>();
		
		for (final List<ClientTopicCouple>clientSubscriptions : subscriptions.values())
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
		
		List<ClientTopicCouple> clientSubscriptions = subscriptions.get(clientID);
		
		if (clientSubscriptions == null)
		{
			clientSubscriptions = new ArrayList<>();
			subscriptions.put(clientID, clientSubscriptions);
		}
		
		clientSubscriptions.add(newSubscription.asClientTopicCouple());
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
		final List<ClientTopicCouple> clientSubscriptions = subscriptions.get(clientID);
				
		if (clientSubscriptions != null)
		{
			clientSubscriptions.remove(topic);
		}
	}

	@Override
	public void bindToDeliver(String arg0, String arg1)
	{
		// Not used
	}

	@Override
	public ClientSession createNewSession(String arg0, boolean arg1)
	{
		// Not used
		return null;
	}

	@Override
	public Collection<String> enqueued(String arg0)
	{
		// Not used
		return null;
	}

	@Override
	public Subscription getSubscription(final ClientTopicCouple couple)
	{
		final Subscription subscription = new Subscription(couple.clientID, couple.topicFilter, QOSType.LEAST_ONE);
		return subscription;
	}

	@Override
	public void inFlight(String arg0, int arg1, String arg2)
	{
		// Not used		
	}

	@Override
	public void inFlightAck(String arg0, int arg1)
	{
		// Not used		
	}

	@Override
	public void initStore()
	{
		// Not used
	}

	@Override
	public String mapToGuid(String arg0, int arg1)
	{
		// Not used
		return null;
	}

	@Override
	public int nextPacketID(String arg0)
	{
		// Not used
		return 0;
	}

	@Override
	public void removeEnqueued(String arg0, String arg1)
	{
		// Not used
	}

	@Override
	public void secondPhaseAckWaiting(String arg0, int arg1)
	{
		// Not used		
	}

	@Override
	public void secondPhaseAcknowledged(String arg0, int arg1)
	{
		// Not used		
	}

	@Override
	public ClientSession sessionForClient(String arg0)
	{
		// Not used
		return null;
	}
}
