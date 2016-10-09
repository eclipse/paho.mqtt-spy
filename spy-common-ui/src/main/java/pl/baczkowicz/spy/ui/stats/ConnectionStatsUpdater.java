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

import javafx.application.Platform;
import pl.baczkowicz.spy.ui.IConnectionViewManager;
import pl.baczkowicz.spy.ui.controllers.IConnectionController;
import pl.baczkowicz.spy.utils.ThreadingUtils;

/**
 * This class is responsible for updating connection statistics for all connections.
 */
public class ConnectionStatsUpdater implements Runnable
{
	private static final int REFRESH_INTERVAL = 1000;
	
	private IConnectionViewManager connectionManager;

	public ConnectionStatsUpdater(final IConnectionViewManager connectionManager)
	{
		this.connectionManager = connectionManager;
	}
	
	@Override
	public void run()
	{
		ThreadingUtils.logThreadStarting("Connection Stats Updater");
		
		while (true)
		{
			if (ThreadingUtils.sleep(REFRESH_INTERVAL))			
			{
				break;
			}
		
			// TODO: turn this into an event via event bus
			Platform.runLater(new Runnable()
			{					
				@Override
				public void run()
				{
					updateConnectionStats();					
				}
			});						
		}
		
		ThreadingUtils.logThreadEnding();
	}

	private void updateConnectionStats()
	{		
		for (final IConnectionController connectionController : connectionManager.getConnectionControllers())
		{
			connectionController.updateConnectionStats();
		}	
	}
}
