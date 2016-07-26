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
package pl.baczkowicz.mqttspy.ui.events.queuable.connectivity;

import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.spy.events.SpyEvent;

public class MqttConnectionFailureEvent implements SpyEvent
{
	private final MqttAsyncConnection connection;
	private final Throwable cause;

	public MqttConnectionFailureEvent(final MqttAsyncConnection connection, final Throwable cause)
	{
		this.connection = connection;
		this.cause = cause;
	}

	public MqttAsyncConnection getConnection()
	{
		return connection;
	}

	public Throwable getCause()
	{
		return cause;
	}
}
