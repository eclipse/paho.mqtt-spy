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

import pl.baczkowicz.spy.connectivity.IConnection;

/** 
 * Basic interface for interacting with an MQTT connection.
 * 
 * TODO: might need adding more methods from BaseMqttConnection
 */
public interface IMqttConnection extends IConnection
{
	BaseMqttSubscription getMqttSubscriptionForTopic(final String topic);
	
	void addSubscription(final BaseMqttSubscription subscription);
	
	void removeSubscription(final BaseMqttSubscription subscription);
	
	/**
	 * Attempts a subscription to the given topic and quality of service.
	 * 
	 * @param topic Subscription topic
	 * @param qos Subscription QoS
	 */
	boolean subscribe(final String topic, final int qos);
	
	boolean subscribe(final BaseMqttSubscription subscription);
	
	/**
	 * Attempts to unsubscribe from the given topic.
	 * 
	 * @param topic Subscription topic
	 */
	boolean unsubscribe(final String topic);
	
	/** 
	 * Checks if a message can be published (e.g. client is connected).
	 * 
	 * @return True if publication is possible
	 */
	boolean canPublish();
	
	/**
	 * Publishes a message with the given parameters.
	 * 
	 * @param publicationTopic Publication topic
	 * @param payload Payload
	 * @param qos Quality of Service
	 * @param retained Retained flag
	 * 
	 * @return True if publication was successful
	 */
	boolean publish(final String publicationTopic, final String payload, final int qos, final boolean retained);
	
	/**
	 * Publishes a message with the given parameters.
	 * 
	 * @param publicationTopic Publication topic
	 * @param payload Payload
	 * @param qos Quality of Service
	 * @param retained Retained flag
	 * 
	 * @return True if publication was successful
	 */
	boolean publish(final String publicationTopic, final byte[] payload, final int qos, final boolean retained);
}
