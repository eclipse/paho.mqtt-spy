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

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.exceptions.XMLException;
import pl.baczkowicz.spy.utils.ThreadingUtils;

/**
 * This class is responsible for loading, processing and saving processing statistics.
 */
public class StatisticsManager implements Runnable
{
	final static Logger logger = LoggerFactory.getLogger(StatisticsManager.class);
	
	public final static List<Integer> periods = Arrays.asList(5, 30, 300);
	
	private final static int removeAfter = 301;
	
	public static SpyStats stats;
	
	public static Map<String, ConnectionStats> runtimeMessagesPublished = new HashMap<>();
	
	public static Map<String, ConnectionStats> runtimeMessagesReceived = new HashMap<>();

	private StatsIO statsFileIO;	
	
	public StatisticsManager(final StatsIO statsFileIO) throws XMLException
	{	
		// Default values
		stats = new SpyStats(0, new Date(), 0, 0, 0, 0);
		
		this.statsFileIO = statsFileIO;
		new Thread(this).start();
	}
	
	public static void newConnection()
	{
		if (stats != null)
		{
			stats.setConnections(stats.getConnections() + 1);		
		}
	}

	public static void newSubscription()
	{
		if (stats != null)
		{
			stats.setSubscriptions(stats.getSubscriptions() + 1);
		}
	}
	
	public void messageReceived(final String connectionId, final List<String> subscriptions)
	{
		if (stats != null)
		{
			// Global stats (saved to XML)
			stats.setMessagesReceived(stats.getMessagesReceived() + 1);
	
			synchronized (runtimeMessagesReceived)
			{
				// Runtime stats
				if (runtimeMessagesReceived.get(connectionId) == null)
				{
					resetConnection(runtimeMessagesReceived, connectionId);
				}		
					
				runtimeMessagesReceived.get(connectionId).runtimeStats.get(0).add(subscriptions);
			}
		}
	}
	
	public void messagePublished(final String connectionId, final String topic)
	{
		if (stats != null)
		{					
			// Global stats (saved to XML)
			stats.setMessagesPublished(stats.getMessagesPublished() + 1);		
						
			synchronized (runtimeMessagesPublished)
			{			
				// Runtime stats
				if (runtimeMessagesPublished.get(connectionId) == null)
				{
					runtimeMessagesPublished.put(connectionId, new ConnectionStats(periods));
					runtimeMessagesPublished.get(connectionId).runtimeStats.add(new ConnectionIntervalStats());
				}		
					
				runtimeMessagesPublished.get(connectionId).runtimeStats.get(0).add(topic);
			}
		}
	}
	
	public static void nextInterval(final Map<String, ConnectionStats> runtimeMessages)
	{
		for (final String connectionId : runtimeMessages.keySet())
		{
			final Map<Integer, ConnectionIntervalStats> avgs = runtimeMessages.get(connectionId).avgPeriods;
			final List<ConnectionIntervalStats> runtime = runtimeMessages.get(connectionId).runtimeStats;	
			
			final ConnectionIntervalStats lastComplete = runtime.size() > 0 ? runtime.get(0) : new ConnectionIntervalStats(); 
					
			// Add the last complete and subtract over the interval
			for (final int period : avgs.keySet())
			{
				avgs.get(period).plus(lastComplete);
				if (runtime.size() > period)
				{
					avgs.get(period).minus(runtime.get(period));
				}
			}
			
			runtime.add(0, new ConnectionIntervalStats());
			
			if (runtime.size() > removeAfter)
			{
				runtime.remove(removeAfter);
			}
		}
	}
	
	public static void nextInterval()
	{		
		synchronized (runtimeMessagesPublished)
		{
			nextInterval(runtimeMessagesPublished);
		}
		
		synchronized (runtimeMessagesReceived)
		{
			nextInterval(runtimeMessagesReceived);
		}
	}
	
	public static ConnectionIntervalStats getMessagesPublished(final String connectionId, final int period)
	{
		if (runtimeMessagesPublished.get(connectionId) == null)
		{
			return new ConnectionIntervalStats();
		}
				
		return runtimeMessagesPublished.get(connectionId).avgPeriods.get(period).average(period);
	}
	
	public static long getMessagesPublished()
	{
		long total = 0;
		
		for (final ConnectionStats stats : runtimeMessagesPublished.values())
		{
			if (stats.runtimeStats.size() > 1)
			{
				total = total + (int) stats.runtimeStats.get(1).overallCount;
			}
		}
		
		return total;
	}
	
	public static long getMessagesReceived()
	{
		long total = 0;
		
		for (final ConnectionStats stats : runtimeMessagesReceived.values())
		{
			if (stats.runtimeStats.size() > 1)
			{
				total = total + (int) stats.runtimeStats.get(1).overallCount;
			}
		}
		
		return total;
	}
	
	public static ConnectionIntervalStats getMessagesReceived(final String connectionId, final int period)
	{
		if (runtimeMessagesReceived.get(connectionId) == null)
		{
			return new ConnectionIntervalStats();
		}
		
		return runtimeMessagesReceived.get(connectionId).avgPeriods.get(period).average(period);
	}
	
	public static void resetMessagesReceived(final String connectionId, final String topic)
	{
		if (runtimeMessagesReceived.get(connectionId) == null)
		{
			return;
		}
		
		runtimeMessagesReceived.get(connectionId).resetTopic(topic);
	}
	
	public static void resetMessagesReceived(final String connectionId)
	{
		resetConnection(runtimeMessagesReceived, connectionId);
	}
	
	private static void resetConnection(final Map<String, ConnectionStats> runtimeMessages, final String connectionId)
	{
		runtimeMessages.put(connectionId, new ConnectionStats(periods));
		runtimeMessages.get(connectionId).runtimeStats.add(new ConnectionIntervalStats());
	}

	@Override
	public void run()
	{
		ThreadingUtils.logThreadStarting("StatisticsManager");
		
		while (true)
		{
			if (ThreadingUtils.sleep(1000))
			{
				break;
			}
			
			nextInterval();			
		}					
		
		ThreadingUtils.logThreadEnding();
	}
	
	/** Format of the stats label. */
	public static final String STATS_FORMAT = "load: " + getPeriodValues();
	
	/**
	 * Creates the list of all periods defined in the statistics manager.
	 * 
	 * @return List of all periods
	 */
	public static String getPeriodList()
	{
		final StringBuffer sb = new StringBuffer();
		
		final Iterator<Integer> iterator = StatisticsManager.periods.iterator();
		while (iterator.hasNext()) 
		{
			final int period = (int) iterator.next();
			if (period > 60)
			{
				sb.append((period / 60) + "m");
			}
			else
			{
				sb.append(period + "s");
			}
			
			if (iterator.hasNext())
			{
				sb.append(", ");
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Creates the stats format for all periods defined in the statistics manager.
	 * 
	 * @return Format for all periods
	 */
	public static String getPeriodValues()
	{
		final StringBuffer sb = new StringBuffer();
		
		final Iterator<Integer> iterator = StatisticsManager.periods.iterator();
		while (iterator.hasNext()) 
		{
			sb.append("%.1f");	
			iterator.next();
			
			if (iterator.hasNext())
			{
				sb.append("/");
			}
		}
		
		return sb.toString();
	}

	public void loadStats()
	{
		stats = statsFileIO.loadStats();		
	}

	public void saveStats()
	{
		statsFileIO.saveStats(stats);		
	}
}
