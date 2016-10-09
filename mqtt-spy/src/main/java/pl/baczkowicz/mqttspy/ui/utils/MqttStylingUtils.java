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
package pl.baczkowicz.mqttspy.ui.utils;

import pl.baczkowicz.spy.connectivity.ConnectionStatus;

/**
 * Styling-related utilities.
 */
public class MqttStylingUtils
{
	/**
	 * Gets CSS style for the given MQTT connection status.
	 * 
	 * @param status The MQTT connection status
	 * 
	 * @return The style to be used
	 */
	public static String getStyleForMqttConnectionStatus(final ConnectionStatus status)
	{
		String style = "connection-default";
		
		if (status != null)
		{
			switch ((ConnectionStatus) status)
			{
				case NOT_CONNECTED:
					style = "connection-not-connected";
					break;
				case CONNECTED:					
					style = "connection-connected";
					break;
				case CONNECTING:
					style = "connection-connecting";
					break;
				case DISCONNECTED:
					style = "connection-disconnected";
					break;
				case DISCONNECTING:					
					style = "connection-disconnected";
					break;
				default:
					style = "connection-default";
					break;
			}
		}
		
		return style;
	}
}
