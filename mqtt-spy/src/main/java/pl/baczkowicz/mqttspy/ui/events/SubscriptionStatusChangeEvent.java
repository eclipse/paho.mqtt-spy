/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
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

import pl.baczkowicz.mqttspy.connectivity.MqttSubscription;
import pl.baczkowicz.spy.eventbus.FilterableEvent;

public class SubscriptionStatusChangeEvent extends FilterableEvent
{
	private final MqttSubscription changedSubscription;

	public SubscriptionStatusChangeEvent(final MqttSubscription changedSubscription)
	{
		this.setFilter(changedSubscription);
		this.changedSubscription = changedSubscription;
	}

	/**
	 * @return the changedSubscription
	 */
	public MqttSubscription getChangedSubscription()
	{
		return changedSubscription;
	}
}
