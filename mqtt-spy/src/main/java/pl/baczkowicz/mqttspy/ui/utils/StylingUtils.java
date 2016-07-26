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

import javafx.scene.paint.Color;
import pl.baczkowicz.mqttspy.connectivity.MqttConnectionStatus;

/**
 * Styling-related utilities.
 */
public class StylingUtils
{
	/**
	 * Gets CSS style for the given MQTT connection status.
	 * 
	 * @param status The MQTT connection status
	 * 
	 * @return The style to be used
	 */
	public static String getStyleForMqttConnectionStatus(final MqttConnectionStatus status)
	{
		String style = "connection-default";
		
		if (status != null)
		{
			switch ((MqttConnectionStatus) status)
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
	
	/**
	 * Creates 'base' RGB string from the given color.
	 * 
	 * @param c Color
	 * 
	 * @return CSS string setting the base with the given color
	 */
	public static String createBaseRGBString(Color c)
	{
		return "-fx-base: " + createRGBString(c);
	}

	/**
	 * Creates 'control-inner-background' RGB string with opacity.
	 * 
	 * @param c Color
	 * @param opacity Opacity to be used
	 * 
	 * @return CSS string setting the inner bg with the given color and opacity
	 */
	public static String createBgRGBString(Color c, double opacity)
	{
		return "-fx-control-inner-background: " + createRGBAString(c, opacity);
	}

	/**
	 * Creates CSS RGB string from the given color.
	 * 
	 * @param c Color
	 * 
	 * @return CSS RGB string
	 */
	public static String createRGBString(Color c)
	{
		return "rgb(" + (c.getRed() * 255) + "," + (c.getGreen() * 255) + "," + (c.getBlue() * 255) + ");";
	}

	/**
	 * Creates CSS RGBA string from the given color and opacity.
	 * 
	 * @param c Color
	 * @param opacity Opacity to be used
	 * 
	 * @return CSS RGBA string
	 */
	public static String createRGBAString(Color c, double opacity)
	{
		return "rgba(" + (c.getRed() * 255) + "," + (c.getGreen() * 255) + "," + (c.getBlue() * 255) + ", " + opacity
				+ ");";
	}
}
