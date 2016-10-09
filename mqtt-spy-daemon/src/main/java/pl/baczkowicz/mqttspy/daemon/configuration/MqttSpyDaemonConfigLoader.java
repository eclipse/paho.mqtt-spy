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
package pl.baczkowicz.mqttspy.daemon.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.MqttConnectionDetails;
import pl.baczkowicz.mqttspy.common.generated.ProtocolVersionEnum;
import pl.baczkowicz.mqttspy.daemon.configuration.generated.MqttSpyDaemonConfiguration;
import pl.baczkowicz.mqttspy.utils.MqttConfigurationUtils;
import pl.baczkowicz.mqttspy.utils.MqttUtils;
import pl.baczkowicz.spy.common.generated.ScriptDetails;
import pl.baczkowicz.spy.configuration.PropertyFileLoader;
import pl.baczkowicz.spy.exceptions.XMLException;
import pl.baczkowicz.spy.files.FileUtils;
import pl.baczkowicz.spy.xml.XMLParser;

/**
 * Helper class for loading the daemon's configuration.
 */
public class MqttSpyDaemonConfigLoader extends PropertyFileLoader
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(MqttSpyDaemonConfigLoader.class);
	
	private static String CLIENT_ID_REFIX = "mqttspy";
	
	/** XML config parser. */
	private final XMLParser parser;

	/** Daemon's configuration (once parsed). */
	private MqttSpyDaemonConfiguration configuration;
	
	/**
	 * Creates the loader. 
	 * 
	 * @throws XMLException Thrown if cannot read the properties file or instantiate the config parser
	 */
	public MqttSpyDaemonConfigLoader() throws XMLException
	{
		super();
		readFromClassPath(MqttSpyDaemonConstants.DEFAULT_PROPERTIES_FILE_NAME);
		
		this.parser = new XMLParser(MqttSpyDaemonConstants.PACKAGE, 
				new String[] {	MqttConfigurationUtils.SPY_COMMON_SCHEMA, 
								MqttConfigurationUtils.MQTT_COMMON_SCHEMA, 
								MqttSpyDaemonConstants.SCHEMA});					
	}
	
	/**
	 * Loads configuration from the given file.
	 * 
	 * @param file The file to load from
	 * 
	 * @return True if all OK
	 */
	public boolean loadConfiguration(final File file)
	{
		try
		{
			configuration = (MqttSpyDaemonConfiguration) parser.loadFromFile(file);	
			populateDefaults();
			
			return true;
		}
		catch (XMLException e)
		{							
			logger.error("Cannot process the configuration file at " + file.getAbsolutePath(), e);
		}
		catch (FileNotFoundException e)
		{
			logger.error("Cannot read the configuration file from " + file.getAbsolutePath(), e);
		}
		
		return false;
	}
	
	/**
	 * Loads configuration from the file location.
	 * 
	 * @param configurationFile The file location to load from
	 * 
	 * @return True if all OK
	 */
	public boolean loadConfiguration(final String configurationFile)
	{
		try
		{
			final InputStream is = FileUtils.loadFileByName(configurationFile);
			configuration = (MqttSpyDaemonConfiguration) parser.loadFromInputStream(is);	
			populateDefaults();
			
			return true;
		}
		catch (XMLException e)
		{							
			logger.error("Cannot process the configuration from input stream", e);
		}
		catch (IOException e)
		{
			logger.error("Cannot read the configuration from input stream", e);
		}
		
		return false;
	}

	/**
	 * Populates the connection configuration with default values.
	 */
	private void populateDefaults()
	{				
		MqttConfigurationUtils.populateMessageLogDefaults(configuration.getConnection().getMessageLog());
		populateDaemonDefaults(configuration.getConnection().getBackgroundScript());
		generateClientIdIfMissing(configuration.getConnection());
	}
	
	public static void populateDaemonDefaults(List<ScriptDetails> scripts)
	{
		for (final ScriptDetails scriptDetails : scripts)
		{
			if (scriptDetails.isRepeat() == null)
			{
				scriptDetails.setRepeat(false);
			}
		}
	}
	
	public static void generateClientIdIfMissing(final MqttConnectionDetails connection)
	{
		if (connection.getClientID().isEmpty() 
				&& !ProtocolVersionEnum.MQTT_3_1_1.equals(connection.getProtocol()))
		{
			logger.info("Client ID is empty and protocol version is not 3.1.1, so going to generate one...");
			connection.setClientID(MqttUtils.generateClientIdWithTimestamp(CLIENT_ID_REFIX, ProtocolVersionEnum.MQTT_3_1));
			logger.info("Generated Client ID is " + connection.getClientID());
		}
	}

	/**
	 * Gets the configuration value.
	 * 
	 * @return The daemon's configuration
	 */
	public MqttSpyDaemonConfiguration getConfiguration()
	{
		return configuration;
	}

	/**
	 * Sets the MQTT client ID prefix.
	 * 
	 * @param prefix the prefix to set
	 */
	public static void setClientIdPrefix(final String prefix)
	{
		CLIENT_ID_REFIX = prefix;
	}
}
