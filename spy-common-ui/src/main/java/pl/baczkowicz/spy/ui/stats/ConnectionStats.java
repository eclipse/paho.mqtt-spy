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
package pl.baczkowicz.spy.ui.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Connection statistics.
 */
public class ConnectionStats
{
	final public List<ConnectionIntervalStats> runtimeStats = new ArrayList<>();
	
	final public Map<Integer, ConnectionIntervalStats> avgPeriods = new HashMap<>();
	
	public ConnectionStats(List<Integer> periods)
	{
		for (final int period : periods)
		{
			avgPeriods.put(period, new ConnectionIntervalStats());
		}
	}
	
	public void resetTopic(final String topic)
	{
		for (final ConnectionIntervalStats stats : runtimeStats)
		{
			stats.resetTopic(topic);
		}
		
		for (final ConnectionIntervalStats stats : avgPeriods.values())
		{
			stats.resetTopic(topic);
		}
	}
}
