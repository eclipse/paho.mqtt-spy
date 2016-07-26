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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.Main;
import pl.baczkowicz.mqttspy.common.generated.MessageLog;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.utils.MqttConfigurationUtils;
import pl.baczkowicz.spy.storage.MessageList;

public class ConfigurationUtils
{
	private final static Logger logger = LoggerFactory.getLogger(ConfigurationUtils.class);
	
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
		
	public static void streamToFile (final InputStream input, final File output) throws IOException 
	{            
	    try (FileOutputStream out = new FileOutputStream(output)) 
	    {
	        IOUtils.copy(input, out);
	    }         
	}
	
	public static boolean createDefaultConfigFromFile(final File orig)
	{
		try
		{ 
			final File dest = ConfigurationManager.getDefaultConfigurationFile();
		
			dest.mkdirs();
			Files.copy(orig.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);			
			
			return true;
			
		}
		catch (IOException e)
		{
			// TODO: show warning dialog for invalid
			logger.error("Cannot copy configuration file", e);
		}
		
		return false;
	}

	private static boolean copyFileFromClassPath(final InputStream orig, final File dest) throws IOException
	{
		ConfigurationManager.getDefaultHomeDirectoryFile().mkdirs();
		ConfigurationUtils.streamToFile(orig, dest);

		return true;	
	}	
	
	public static boolean createDefaultConfigFromClassPath(final String name)
	{
		final String origin = "/samples" + "/" + name + "-mqtt-spy-configuration.xml";
		try
		{			
			return copyFileFromClassPath(Main.class.getResourceAsStream(origin), ConfigurationManager.getDefaultConfigurationFile());
		}
		catch (IllegalArgumentException | IOException e)
		{
			// TODO: show warning dialog for invalid
			logger.error("Cannot copy configuration file from {}", origin, e);
		}
		
		return false;
	}
	
	public static boolean createUiPropertyFileFromClassPath()
	{
		final String origin = "/samples" + ConfigurationManager.UI_PROPERTIES_FILE_NAME;
		try
		{			
			return copyFileFromClassPath(Main.class.getResourceAsStream(origin), ConfigurationManager.getUiPropertiesFile());
		}
		catch (IllegalArgumentException | IOException e)
		{
			// TODO: show warning dialog for invalid
			logger.error("Cannot copy file from {}", origin, e);
		}
		
		return false;
	} 
}
