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
package pl.baczkowicz.mqttspy.logger;

import pl.baczkowicz.mqttspy.messages.BaseMqttMessage;
import pl.baczkowicz.spy.utils.tasks.StoppableTask;

/**
 * Defines the interface between a script and the MessageLog object.
 */
public interface IMqttMessageLogIO extends StoppableTask
{
	/**
	 * Reads MQTT messages from a file and returns the number of messages read.
	 * 
	 * @param logLocation The file to process
	 * 
	 * @return The number of MQTT messages read
	 */
	int readFromFile(final String logLocation);
	
	/**
	 * Gets the number of messages read from file.
	 * 
	 * @return The number of MQTT messages read
	 */
	int getMessageCount();
	
	/**
	 * Starts the time checker to see if messages are due for publishing - see the isReadyToPublish method.
	 */
	void start();
	
	//	TODO: to be added in the future
	// void pause();	
	// void resume();
	
	/**
	 * Stops the time checker - see the start method.
	 */
	void stop();
	
	/**
	 * Sets the replay speed, which is used by the time checker to see if messages are due to be published - see the isReadyToPublish method.
	 * 
	 * @param speed The replay speed (1 is normal; 2 is twice the normal; 0.5 is half the normal)
	 */
	void setSpeed(final double speed);
	
	/**
	 * Once the time checker has been started (see the start method), checks is a message with the given index is due to be published.
	 * 
	 * @param messageIndex The message index to be checked
	 * 
	 * @return True if the message should be published
	 */
	boolean isReadyToPublish(final int messageIndex);
	
	/**
	 * Returns a message with the given index.
	 *  
	 * @param messageIndex Index of the message to be returned
	 * 
	 * @return ReceivedMqttMessage
	 */
	BaseMqttMessage getMessage(final int messageIndex);
}
