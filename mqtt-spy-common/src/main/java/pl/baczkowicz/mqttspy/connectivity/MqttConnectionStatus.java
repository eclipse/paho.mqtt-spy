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

/**
 * Indicates the status of the connection.
 */
public enum MqttConnectionStatus
{
	/** No attempt made yet. */
	NOT_CONNECTED, 
	
	/** Trying to connect. */
	CONNECTING, 
	
	/** Connected. */
	CONNECTED, 
	
	/** Trying to disconnect. */
	DISCONNECTING, 
	
	/** Disconnected. */
	DISCONNECTED
}
