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
package pl.baczkowicz.mqttspy.daemon.connectivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.SubscriptionDetails;
import pl.baczkowicz.mqttspy.connectivity.BaseMqttSubscription;
import pl.baczkowicz.mqttspy.connectivity.SimpleMqttConnection;
import pl.baczkowicz.mqttspy.daemon.configuration.generated.DaemonMqttConnectionDetails;
import pl.baczkowicz.mqttspy.utils.ConnectionUtils;
import pl.baczkowicz.spy.common.generated.ReconnectionSettings;
import pl.baczkowicz.spy.common.generated.ScriptDetails;
import pl.baczkowicz.spy.scripts.BaseScriptManager;
import pl.baczkowicz.spy.utils.ThreadingUtils;

/**
 * This runnable is responsible for establishing a connection.
 */
public class SimpleMqttConnectionRunnable implements Runnable
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(SimpleMqttConnectionRunnable.class);
	
	/** The connection to be used. */	
	private final SimpleMqttConnection connection;
	
	/** The connection settings to be used. */
	private final DaemonMqttConnectionDetails connectionSettings;

	/** The script manager - used for subscription scripts. */
	private final BaseScriptManager scriptManager;

	/**
	 * Creates a ConnectionRunnable.
	 * 
	 * @param scriptManager The script manager - used for subscription scripts
	 * @param connection The connection to be used
	 * @param connectionSettings The connection settings to be used
	 */
	public SimpleMqttConnectionRunnable(final BaseScriptManager scriptManager, final SimpleMqttConnection connection, final DaemonMqttConnectionDetails connectionSettings)
	{
		this.connection = connection;
		this.connectionSettings = connectionSettings;
		this.scriptManager = scriptManager;
	}
	
	public void run()
	{
		ThreadingUtils.logThreadStarting("Connection " + connection.getMqttConnectionDetails().getName());
		
		// Get reconnection settings
		final ReconnectionSettings reconnectionSettings = connection.getMqttConnectionDetails().getReconnectionSettings();
		
		final boolean neverStarted = connection.getLastConnectionAttemptTimestamp() == ConnectionUtils.NEVER_STARTED;
		
		// If successfully connected, and re-subscription is configured
		if (connection.connect() 
				&& (neverStarted || (reconnectionSettings != null && reconnectionSettings.isResubscribe())))
		{
			// Subscribe to all configured subscriptions
			for (final SubscriptionDetails subscriptionDetails : connectionSettings.getSubscription())
			{	
				if (neverStarted && subscriptionDetails.getScriptFile() != null)
				{
					logger.debug("Adding script " + subscriptionDetails.getScriptFile());
					scriptManager.addScript(new ScriptDetails(true, false, subscriptionDetails.getScriptFile()));
				}
				
				final BaseMqttSubscription subscription = new BaseMqttSubscription(
						subscriptionDetails.getTopic(), subscriptionDetails.getQos()); 
				subscription.setDetails(subscriptionDetails);
				
				connection.subscribe(subscription);					
			}
		}
		
		ThreadingUtils.logThreadEnding();
	}				
}
