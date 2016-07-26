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
package pl.baczkowicz.mqttspy.scripts;

import java.util.List;

import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.spy.scripts.IScriptIO;

/**
 * Interface between a script and the mqttspy object, primarily used for publishing messages.
 */
public interface IMqttScriptIO extends IScriptIO
{
	boolean subscribe(final String topic, final int qos);
	
	boolean unsubscribe(final String topic);
	
	/**
	 * Publishes a message with the given payload to the given topic (qos = 0; retained = false).
	 * 
	 * @param publicationTopic The publication topic
	 * @param payload The payload of the message
	 */
	void publish(final String publicationTopic, final String payload);

	/**
	 * Publishes a message with the given payload, qos and retained flag to the given topic.
	 * 
	 * @param publicationTopic The publication topic
	 * @param payload The payload of the message
	 * @param qos The quality of service to be used
	 * @param retained The retained flag
	 */
	void publish(final String publicationTopic, final String payload, final int qos, final boolean retained);
	
	/**
	 * Publishes a message with the given payload, qos and retained flag to the given topic.
	 * 
	 * @param publicationTopic The publication topic
	 * @param payload The payload of the message
	 * @param qos The quality of service to be used
	 * @param retained The retained flag
	 */
	void publish(final String publicationTopic, final byte[] payload, final int qos, final boolean retained);

	List<FormattedMqttMessage> getMessages(String subscriptionTopic);	
}