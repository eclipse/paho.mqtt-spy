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
package pl.baczkowicz.mqttspy.configuration;

import pl.baczkowicz.mqttspy.common.generated.MessageLog;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.utils.MqttConfigurationUtils;
import pl.baczkowicz.spy.storage.MessageList;

public class ConfigurationUtils
{
	// private final static Logger logger = LoggerFactory.getLogger(ConfigurationUtils.class);
	
	public final static int DEFAULT_RECONNECTION_INTERVAL = 5000;
		
	public static void populateConnectionDefaults(final UserInterfaceMqttConnectionDetails connection)
	{
		MqttConfigurationUtils.populateConnectionDefaults(connection);
		
		if (connection.getMessageLog() == null)
		{
			connection.setMessageLog(new MessageLog());
			MqttConfigurationUtils.populateMessageLogDefaults(connection.getMessageLog());
		}
		else
		{
			MqttConfigurationUtils.populateMessageLogDefaults(connection.getMessageLog());
		}
				
		if (connection.getMaxMessagesStored() == null)
		{
			connection.setMaxMessagesStored(MessageList.DEFAULT_MAX_SIZE);
		}
		
		if (connection.getMinMessagesStoredPerTopic() == null)
		{
			connection.setMinMessagesStoredPerTopic(MessageList.DEFAULT_MIN_MESSAGES_PER_TOPIC);
		}
		
		if (connection.isAutoOpen() == null)
		{
			connection.setAutoOpen(false);
		}
		
		if (connection.isAutoConnect() == null)
		{
			connection.setAutoConnect(true);
		}
		
		if (connection.isAutoSubscribe() == null)
		{
			connection.setAutoSubscribe(false);
		}
	}
}
