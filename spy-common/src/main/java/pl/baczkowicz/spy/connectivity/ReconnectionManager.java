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
package pl.baczkowicz.spy.connectivity;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.common.generated.ReconnectionSettings;
import pl.baczkowicz.spy.utils.ThreadingUtils;
import pl.baczkowicz.spy.utils.TimeUtils;

/**
 * This class is responsible for (re)establishing connections. 
 */
public class ReconnectionManager implements Runnable
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(ReconnectionManager.class);
	
	/** Mapping between connections and connectors implemented as runnables. */
	private final Map<IConnectionWithReconnect, Runnable> connections = new HashMap<>();
	
	/** Sleep between reconnection cycles. */
	private final static int SLEEP = 100;
	
	/** Indicates whether the Reconnection Manager is running. */
	private boolean running;
	
	/**
	 * Adds a connection and its connector to the manager.
	 * 
	 * @param connection The connection to add
	 * @param connector The connector runnable
	 */
	public void addConnection(final IConnectionWithReconnect connection, final Runnable connector)
	{
		synchronized (connections)
		{
			connections.put(connection, connector);
		}
	}
	
	/**
	 * Removes a connection from the manager.
	 * 
	 * @param connection The connection to remove
	 */
	public void removeConnection(final IConnectionWithReconnect connection)
	{
		synchronized (connections)
		{
			connections.remove(connection);
		}
	}
	
	/**
	 * Performs one connecting cycle - checks if any connections need reconnecting.
	 */
	public void oneCycle()
	{
		for (final IConnectionWithReconnect connection : connections.keySet())
		{
			if (connection.getConnectionStatus().equals(ConnectionStatus.CONNECTING))
			{
				// If already connecting, ignore it
				continue;
			}
			
			final ReconnectionSettings reconnectionSettings = connection.getReconnectionSettings();				
			if (connection.getLastConnectionAttemptTimestamp() + reconnectionSettings.getRetryInterval() > TimeUtils.getMonotonicTime())
			{
				// If we're not due to reconnect yet
				continue;
			}
			
			if (connection.getConnectionStatus().equals(ConnectionStatus.DISCONNECTED) 
					|| connection.getConnectionStatus().equals(ConnectionStatus.NOT_CONNECTED))
			{
				logger.info("Starting connection {}", connection.getName());
				new Thread(connections.get(connection)).start();
			}
		}			
	}

	/**
	 * Once called, it runs until 'stop' is called.
	 */
	public void run()
	{
		ThreadingUtils.logThreadStarting("Reconnection Manager");
		
		running = true;
		
		while (running)
		{
			synchronized (connections)
			{
				oneCycle();
			}
			
			if (ThreadingUtils.sleep(SLEEP))
			{
				break;
			}
		}	
		
		ThreadingUtils.logThreadEnding();
	}
	
	/**
	 * Stops the reconnection manager (graceful shutdown).
	 */
	public void stop()
	{
		running = false;
	}
}
