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
package pl.baczkowicz.mqttspy.connectivity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.connectivity.topicmatching.TopicMatcher;
import pl.baczkowicz.mqttspy.utils.ConnectionUtils;
import pl.baczkowicz.spy.common.generated.ProtocolEnum;
import pl.baczkowicz.spy.common.generated.ScriptDetails;
import pl.baczkowicz.spy.exceptions.SpyException;
import pl.baczkowicz.spy.scripts.BaseScriptManager;
import pl.baczkowicz.spy.scripts.Script;
import pl.baczkowicz.spy.utils.TimeUtils;

/**
 * Base MQTT connection class, encapsulating the Paho's MQTT client and providing some common features.
 */
public abstract class BaseMqttConnection implements IMqttConnection
{
	/** Diagnostic logger. */
	private static final Logger logger = LoggerFactory.getLogger(BaseMqttConnection.class);

	/** Number of connection attempts made. */
	private int connectionAttempts = 0;

	/** Last connection attempt timestamp. */
	private long lastConnectionAttemptTimestamp = ConnectionUtils.NEVER_STARTED;
	
	/** Last successful connection attempt timestamp. */
	private Date lastSuccessfulConnectionAttempt;	
	
	protected final Map<String, BaseMqttSubscription> subscriptions = new HashMap<>();
	
	private int lastUsedSubscriptionId = 0;
	
	/** The Paho MQTT client. */
	protected MqttAsyncClient client;	

	/** Connection options. */
	protected final MqttConnectionDetailsWithOptions connectionDetails;	

	/** COnnection status. */
	private MqttConnectionStatus connectionStatus = MqttConnectionStatus.NOT_CONNECTED;

	/** Disconnection reason (if any). */
	private String disconnectionReason;
	
	/** Used for matching topics to subscriptions. */
	private final TopicMatcher topicMatcher;

	/** Used for calling subscription scripts. */
	private BaseScriptManager scriptManager;
	
	/**
	 * Instantiates the BaseMqttConnection.
	 * 
	 * @param connectionDetails Connection details
	 */
	public BaseMqttConnection(final MqttConnectionDetailsWithOptions connectionDetails)
	{
		this.connectionDetails = connectionDetails;
		this.topicMatcher = new TopicMatcher();		
	}
	
	/**
	 * Creates an asynchronous client with the given callback.
	 * 
	 * @param callback The callback to be set on the MQTT client
	 * 
	 * @throws SpyException Thrown when errors detected
	 */
	public void createClient(final MqttCallback callback) throws SpyException
	{
		try
		{
			logger.debug("Creating MQTT client with server URI {} and client ID {}",
					connectionDetails.getServerURI().get(0), 
					connectionDetails.getClientID());
			
			// Creating MQTT client instance
			client = new MqttAsyncClient(
					connectionDetails.getServerURI().get(0), 
					connectionDetails.getClientID(),
					null);
			
			// Set MQTT callback
			client.setCallback(callback);
		}
		catch (IllegalArgumentException e)
		{
			throw new SpyException("Cannot instantiate the MQTT client", e);
		}
		catch (MqttException e)
		{
			throw new SpyException("Cannot instantiate the MQTT client", e);
		}
	}
	
	/**
	 * Asynchronous connection attempt.
	 * 
	 * TODO: check if this parameter is needed
	 * @param options The connection options
	 * @param userContext The user context (used for any callbacks)
	 * @param callback Connection result callback
	 * 
	 * @throws SpyException Thrown when errors detected
	 */
	public void connect(final MqttConnectOptions options, final Object userContext, final IMqttActionListener callback) throws SpyException
	{
		recordConnectionAttempt();
		
		try
		{
			client.connect(options, userContext, callback);
		}
		catch (IllegalArgumentException e)
		{
			throw new SpyException("Connection attempt failed", e);
		}
		catch (MqttSecurityException e)
		{
			throw new SpyException("Connection attempt failed", e);
		}
		catch (MqttException e)
		{
			throw new SpyException("Connection attempt failed", e);
		}
	}
	
	/**
	 * Synchronous connection attempt.
	 * 
	 * TODO: check if this parameter is needed
	 * @param options The connection options
	 * 
	 * @throws SpyException Thrown when errors detected
	 */
	public void connectAndWait(final MqttConnectOptions options) throws SpyException
	{
		recordConnectionAttempt();
		
		try
		{
			client.connect(options).waitForCompletion();
		}
		catch (IllegalArgumentException e)
		{
			throw new SpyException("Connection attempt failed", e);
		}
		catch (MqttSecurityException e)
		{
			throw new SpyException("Connection attempt failed", e);
		}
		catch (MqttException e)
		{
			throw new SpyException("Connection attempt failed", e);
		}
	}
	
	/**
	 * Records a connection attempt.
	 */
	protected void recordConnectionAttempt()
	{
		lastConnectionAttemptTimestamp = TimeUtils.getMonotonicTime();
		connectionAttempts++;		
	}
	
	/** Records a successful connection. */
	public void recordSuccessfulConnection()
	{
		lastSuccessfulConnectionAttempt = new Date();
	}
	
	/**
	 * Returns last successful connection attempt.
	 * 
	 * @return Formatted date of the last successful connection attempt
	 */
	public String getLastSuccessfulyConnectionAttempt()
	{
		return TimeUtils.DATE_WITH_SECONDS_SDF.format(lastSuccessfulConnectionAttempt);
	}
	
	/**
	 * Subscribes to the given topic and quality of service.
	 * 
	 * @param topic The topic to subscribe to
	 * @param qos The quality of service requested
	 * 
	 * @throws SpyException Thrown when errors detected
	 */
	private void subscribeToTopic(final String topic, final int qos) throws SpyException
	{
		if (client == null || !client.isConnected())
		{
			// TODO: consider throwing an exception here
			logger.warn("Client not connected");
			return;
		}
		
		try
		{
			client.subscribe(topic, qos);
			
			topicMatcher.addSubscriptionToStore(topic, "subscription");
		}
		catch (MqttException e)
		{
			throw new SpyException("Subscription attempt failed", e);
		}
	}	
	
	/**
	 * Attempts a subscription to the given topic and quality of service.
	 * 
	 * @param topic Subscription topic
	 * @param qos Subscription QoS
	 */
	// TODO: deprecate? only used for testing
	public boolean subscribe(final String topic, final int qos)
	{
		try
		{
			subscribeToTopic(topic, qos);
			logger.info("Successfully subscribed to " + topic);
			return true;
		}
		catch (SpyException e)
		{
			logger.error("Subscription attempt failed for topic {}", topic, e);
		}
		
		return false;
	}	

	public boolean subscribe(final BaseMqttSubscription subscription)
	{
		// Subscription are either triggered by configuration or user actions, so default to auto-subscribe
		subscription.setSubscriptionRequested(true);
		
		// Record the subscription, regardless of whether further stuff succeeds
		addSubscription(subscription);
		
		// If already active, simply ignore
		if (subscription.isActive())
		{
			return false;
		}

		if (client == null || !client.isConnected())
		{
			logger.warn("Client not connected");
			return false;
		}

		try
		{			
			// Retained messages can be received very quickly, even so quickly we still haven't set the subscription's state to active
			subscription.setSubscribing(true);
			
			logger.debug("Subscribing to " + subscription.getTopic());			
			client.subscribe(subscription.getTopic(), subscription.getQos());			
			logger.info("Subscribed to " + subscription.getTopic());
						
			subscription.setActive(true);
			subscription.setSubscribing(false);
			
			logger.trace("Subscription " + subscription.getTopic() + " is active = "
					+ subscription.isActive());
			
			if (subscription.getDetails() != null 
					&& subscription.getDetails().getScriptFile() != null 
					&& !subscription.getDetails().getScriptFile().isEmpty())
			{
				final Script script = scriptManager.addScript(new ScriptDetails(false, false, subscription.getDetails().getScriptFile()));
				subscription.setScript(script);
				scriptManager.runScript(script, false);
				
				if (scriptManager.invokeBefore(script))					
				{
					subscription.setScriptActive(true);
				}
			}

			return true;
		}
		catch (MqttException e)
		{
			subscription.setSubscribing(false);
			logger.error("Cannot subscribe to " + subscription.getTopic(), e);
			removeSubscription(subscription);
			return false;
		}
	}
	
	public void addSubscription(final BaseMqttSubscription subscription)
	{
		// Add it to the store if it hasn't been created before
		if (subscriptions.put(subscription.getTopic(), subscription) == null)
		{
			subscription.setId(lastUsedSubscriptionId++);				
		}
		
		logger.debug("Adding topic " + subscription.getTopic() + " to the subsciption store");
		addSubscriptionToMatcher(subscription);
	}	

	public void removeSubscription(final BaseMqttSubscription subscription)
	{
		subscriptions.remove(subscription.getTopic());
		removeSubscriptionFromMatcher(subscription);
	}
	
	public void addSubscriptionToMatcher(final BaseMqttSubscription subscription)
	{
		getTopicMatcher().addSubscriptionToStore(subscription.getTopic(), "subscription" + subscription.getId());
	}
	
	public void removeSubscriptionFromMatcher(final BaseMqttSubscription subscription)
	{
		getTopicMatcher().removeSubscriptionFromStore(subscription.getTopic(), "subscription" + subscription.getId());
	}
	
	public int getLastUsedSubscriptionId()
	{
		return lastUsedSubscriptionId;
	}
	
	public void unsubscribeFromTopic(final String topic) throws SpyException
	{
		if (client == null || !client.isConnected())
		{
			// TODO: consider throwing an exception here
			logger.warn("Client not connected");
			return;
		}
				
		try
		{
			client.unsubscribe(topic);
			
			// topicMatcher.removeSubscriptionFromStore(topic);
		}
		catch (MqttException e)
		{
			throw new SpyException("Unsubscription attempt failed", e);
		}
	}
	
	/**
	 * Attempts to unsubscribe from the given topic.
	 * 
	 * @param topic Subscription topic
	 */
	@Override
	public boolean unsubscribe(final String topic)
	{
		try
		{
			unsubscribeFromTopic(topic);
			logger.info("Successfully unsubscribed from " + topic);
			return true;
		}
		catch (SpyException e)
		{
			logger.error("Unsubscribe attempt failed for topic {}", topic, e);
		}
		
		return false;
	}
	
	/**
	 * Unsubscribes from all topics.
	 * 
	 * @param manualOverride True if it was requested by user
	 * 
	 * @return True if successful
	 */
	public abstract boolean unsubscribeAll(final boolean manualOverride);

	/**
	 * Checks if a message can be published.
	 * 
	 * @return True if the client is connected	
	 */
	@Override
	public boolean canPublish()
	{
		return client != null && client.isConnected();
	}
	
	/**
	 * Records lost connection.
	 * 
	 * @param cause The cause of the connection loss
	 */
	public void connectionLost(Throwable cause)
	{
		setDisconnectionReason(cause.getMessage());
		setConnectionStatus(MqttConnectionStatus.DISCONNECTED);
	}
	
	/**
	 * Sets the disconnection reason to the given message.
	 * 
	 * @param message The disconnection reason
	 */
	public void setDisconnectionReason(final String message)
	{
		this.disconnectionReason = message;
		if (!message.isEmpty())
		{
			this.disconnectionReason = this.disconnectionReason + " ("
					+ TimeUtils.DATE_WITH_SECONDS_SDF.format(new Date()) + ")";
		}
	}
	
	public void disconnect()
	{
		try
		{
			client.disconnect(0);
			logger.info("Client {} disconnected", client.getClientId());
		}
		catch (MqttException e)
		{
			logger.error("Cannot disconnect", e);
		}
	}		
	
	// ===============================
	// === Setters and getters =======
	// ===============================
	
	/**
	 * Gets the last disconnection reason.
	 * 
	 * @return The disconnection reason
	 */
	public String getDisconnectionReason()
	{
		return disconnectionReason;
	}
	
	/**
	 * Gets the MQTT connection details.
	 * 
	 * @return The MQTT connection details
	 */
	public MqttConnectionDetailsWithOptions getMqttConnectionDetails()
	{
		return connectionDetails;
	}

	/**
	 * Gets the last connection attempt timestamp.
	 * 
	 * @return Last connection attempt timestamp
	 */
	public long getLastConnectionAttemptTimestamp()
	{
		return lastConnectionAttemptTimestamp;
	}

	/**
	 * Gets the number of connections attempts made so far.
	 * 
	 * @return The number of connection attempts
	 */
	public int getConnectionAttempts()
	{
		return connectionAttempts;
	}
	
	/**
	 * Gets the current connection status.
	 * 
	 * @return Current connection status
	 */
	public MqttConnectionStatus getConnectionStatus()
	{
		return connectionStatus;
	}
	
	/**
	 * Sets the new connection status
	 * 
	 * @param connectionStatus The connection status to set
	 */
	public void setConnectionStatus(final MqttConnectionStatus connectionStatus)
	{
		this.connectionStatus = connectionStatus;
	}

	/**
	 * Gets the topic matcher.
	 * 
	 * @return Topic matcher
	 */
	public TopicMatcher getTopicMatcher()
	{
		return topicMatcher;
	}

	/**
	 * Gets the MQTT client.
	 * 
	 * @return the client
	 */
	public MqttAsyncClient getClient()
	{
		return client;
	}

	/**
	 * Sets the MQTT client - primarily for testing. 
	 * 
	 * @param client the client to set
	 */
	public void setClient(final MqttAsyncClient client)
	{
		this.client = client;
	}
	
	public void setScriptManager(final BaseScriptManager scriptManager)
	{
		this.scriptManager = scriptManager;
	}
	
	// TODO: is that needed?
	public BaseMqttSubscription getMqttSubscriptionForTopic(final String topic)
	{
		return subscriptions.get(topic);
	}
	
	public ProtocolEnum getProtocol()	
	{
		return ProtocolEnum.MQTT;
	}
}
