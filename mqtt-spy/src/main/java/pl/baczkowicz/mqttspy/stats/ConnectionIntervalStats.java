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
package pl.baczkowicz.mqttspy.stats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Connection statistics for a single interval.
 */
public class ConnectionIntervalStats
{
	public Map<String, Double> messageCount = new HashMap<>();
	
	public double overallCount = 0;
	
	public void add(final String topic)
	{
		overallCount++;
		
		if (messageCount.get(topic) == null)
		{
			messageCount.put(topic, (double) 1);
		}
		else
		{
			messageCount.put(topic, messageCount.get(topic) + 1);
		}		
	}
	
	public void add(final List<String> topics)
	{
		overallCount++;
		
		for (final String topic : topics)
		{
			if (messageCount.get(topic) == null)
			{
				messageCount.put(topic, (double) 1);
			}
			else
			{
				messageCount.put(topic, messageCount.get(topic) + 1);
			}
		}
	}
	
	public void reset()
	{
		overallCount = 0;
		messageCount.clear();
	}

	public void resetTopic(final String topic)
	{
		messageCount.put(topic, (double) 0);
	}

	public ConnectionIntervalStats average(final int interval)
	{
		final ConnectionIntervalStats avg = new ConnectionIntervalStats();
		avg.overallCount = overallCount / interval;
		
		for (final String topic : messageCount.keySet())
		{
			avg.messageCount.put(topic, messageCount.get(topic) / interval);			
		}
		
		return avg;
	}
	
	public void plus(final ConnectionIntervalStats cs)
	{
		overallCount = overallCount + cs.overallCount;
		for (final String topic : cs.messageCount.keySet())
		{
			if (messageCount.containsKey(topic))
			{
				messageCount.put(topic, messageCount.get(topic) + cs.messageCount.get(topic));
			}
			else
			{
				messageCount.put(topic, cs.messageCount.get(topic));
			}
		}
	}
	public void minus(final ConnectionIntervalStats cs)
	{
		overallCount = overallCount - cs.overallCount;
		for (final String topic : cs.messageCount.keySet())
		{
			if (messageCount.containsKey(topic))
			{
				messageCount.put(topic, messageCount.get(topic) - cs.messageCount.get(topic));
			}			
		}		
	}
}
