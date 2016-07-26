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
package pl.baczkowicz.mqttspy.messages;

import java.util.Date;

import pl.baczkowicz.mqttspy.common.generated.SimpleMqttMessage;

/**
 * Wrapper around the XSD-based simple MQTT message, exposing standard access methods.
 */
public class SimpleMqttMessageWrapper implements IBaseMqttMessage
{
	/** Wrapped MQTT message. */
	private final SimpleMqttMessage message;

	/**
	 * Creates a BaseMqttMessageWrapper from the provided message.
	 * 
	 * @param message The message to be wrapped
	 */
	public SimpleMqttMessageWrapper(final SimpleMqttMessage message)
	{
		this.message = message;
	}

	@Override
	public String getTopic()
	{
		return message.getTopic();
	}

	@Override
	public String getPayload()
	{
		return message.getValue();
	}

	@Override
	public int getQoS()
	{
		return message.getQos();
	}

	@Override
	public boolean isRetained()
	{
		return message.isRetained();
	}

	@Override
	public void setPayload(String payload)
	{
		message.setValue(payload);		
	}

	@Override
	public long getId()
	{
		// Not used
		return 0;
	}

	@Override
	public Date getDate()
	{
		// Not used
		return null;
	}
}
