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

import pl.baczkowicz.mqttspy.configuration.ConfiguredMqttConnectionDetails;
import pl.baczkowicz.spy.common.generated.FormatterDetails;
import pl.baczkowicz.spy.exceptions.ConfigurationException;

public class MqttRuntimeConnectionProperties extends MqttConnectionDetailsWithOptions
{
	private static final long serialVersionUID = -2411594766874071799L;
	
	private ConfiguredMqttConnectionDetails configuredProperties;

	public MqttRuntimeConnectionProperties(final ConfiguredMqttConnectionDetails configuredProperties) throws ConfigurationException
	{	
		super(configuredProperties.getID(), configuredProperties);
		this.configuredProperties = configuredProperties;		
	}
	
	public FormatterDetails getFormatter()
	{
		return (FormatterDetails) configuredProperties.getFormatter();
	}

	public int getMaxMessagesStored()
	{
		return configuredProperties.getMaxMessagesStored();
	}

	public boolean isAutoConnect()
	{
		return configuredProperties.isAutoConnect();
	}
	
	public ConfiguredMqttConnectionDetails getConfiguredProperties()
	{
		return this.configuredProperties;
	}
	
	public String getId()
	{
		return this.configuredProperties.getID();
	}
}
