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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.configuration.generated.Connectivity;
import pl.baczkowicz.mqttspy.configuration.generated.MqttSpyConfiguration;
import pl.baczkowicz.mqttspy.configuration.generated.TabbedSubscriptionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.connectivity.MqttSubscription;
import pl.baczkowicz.spy.common.generated.FormatterDetails;
import pl.baczkowicz.spy.common.generated.Formatting;
import pl.baczkowicz.spy.connectivity.BaseSubscription;
import pl.baczkowicz.spy.exceptions.XMLException;
import pl.baczkowicz.spy.ui.configuration.BaseConfigurationManager;
import pl.baczkowicz.spy.xml.XMLParser;

/**
 * Manages loading and saving configuration files.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class MqttConfigurationManager extends BaseConfigurationManager
{
	private final static Logger logger = LoggerFactory.getLogger(MqttConfigurationManager.class);
	
	public static final String PACKAGE = "pl.baczkowicz.mqttspy.configuration.generated";
	
	public static final String SCHEMA = "/mqtt-spy-configuration.xsd";
		
	public static final String MQTT_COMMON_SCHEMA = "/mqtt-spy-common.xsd";

	private MqttSpyConfiguration configuration;
	
	private List<ConfiguredMqttConnectionDetails> connections = new ArrayList<>();	
	
	private final XMLParser parser;
	
	public MqttConfigurationManager() throws XMLException
	{
		loadDefaultPropertyFile();
		loadUiPropertyFile();		
		
		this.parser = new XMLParser(PACKAGE, new String[] {SPY_COMMON_SCHEMA, MQTT_COMMON_SCHEMA, SCHEMA});
					
		// Create empty configuration
		this.configuration = new MqttSpyConfiguration();
		this.configuration.setConnectivity(new Connectivity());
		this.configuration.setFormatting(new Formatting());		
	}
	
	public boolean loadConfiguration(final File file)
	{
		configuration = (MqttSpyConfiguration) loadConfiguration(parser, file);
		initialiseConfiguration();
		
		return configuration != null ? true : false;
	}
	
	public void initialiseConfiguration()
	{
		createConnections(configuration.getConnectivity().getConnectionV2());
		createConnectionGroups(configuration.getConnectionGroups(), new ArrayList<>(getConnections()));
		createConfigurationDefaults();
	}
	
	private void createConfigurationDefaults()
	{
		if (configuration.getFormatting() == null)
		{
			configuration.setFormatting(new Formatting());
		}
	}
	
	public static ConfiguredMqttConnectionDetails toConfiguredMqttConnectionDetails(final UserInterfaceMqttConnectionDetails connectionDetails)
	{
		// Put the defaults at the point of loading the config, so we don't need to do it again
		ConfigurationUtils.populateConnectionDefaults((UserInterfaceMqttConnectionDetails) connectionDetails);
		final ConfiguredMqttConnectionDetails configuredConnectionDetails = new ConfiguredMqttConnectionDetails(false, false, 
				(UserInterfaceMqttConnectionDetails) connectionDetails);
		
		// Populate the connection ID for referencing in XML
		if (configuredConnectionDetails.getID() == null)
		{
			configuredConnectionDetails.setID(generateConnectionId());				
		}
		
		return configuredConnectionDetails;
	}
	
	private void createConnections(final List<UserInterfaceMqttConnectionDetails> loadedConnections)
	{
		for (final Object connectionDetails : loadedConnections)
		{			
			if (connectionDetails instanceof UserInterfaceMqttConnectionDetails)
			{				
				connections.add(toConfiguredMqttConnectionDetails((UserInterfaceMqttConnectionDetails) connectionDetails));
			}
		}		
	}
	
	public boolean saveConfiguration()
	{
		if (isConfigurationWritable())
		{
			try
			{
				configuration.getConnectivity().getConnectionV2().clear();
				configuration.getConnectivity().getConnectionV2().addAll(connections);
				
				configuration.getConnectionGroups().clear();
				configuration.getConnectionGroups().addAll(getConnectionGrops());
				
				populateMissingFormatters(configuration.getFormatting().getFormatter(), connections);
				
				parser.saveToFile(getLoadedConfigurationFile(), 
						new JAXBElement(new QName("http://baczkowicz.pl/mqtt-spy-configuration", "MqttSpyConfiguration"), MqttSpyConfiguration.class, configuration));
				return true;
			}
			catch (XMLException e)
			{
				setLastException(e);
				logger.error("Cannot save the configuration file", e);
			}
		}
		
		return false;
	}
	
	private void populateMissingFormatters(final List<FormatterDetails> formatters, final List<ConfiguredMqttConnectionDetails> connections)
	{
		for (final ConfiguredMqttConnectionDetails connection : connections)
		{
			if (connection.getFormatter() == null)
			{
				continue;
			}
			
			boolean formatterFound = false;
			
			for (final FormatterDetails formatter : formatters)
			{
				if (((FormatterDetails) connection.getFormatter()).getID().equals(formatter.getID()))
				{
					formatterFound = true;
				}
			}
			
			if (!formatterFound)
			{
				formatters.add((FormatterDetails) connection.getFormatter());
			}
		}
	}
	
	public void clear()
	{
		connections.clear();
		configuration = null;
		setLoadedConfigurationFile(null);
		setLastException(null);
	}
	
	public ConfiguredMqttConnectionDetails getMatchingConnection(final String id)
	{
		for (final ConfiguredMqttConnectionDetails details : getConnections())
		{
			if (id.equals(details.getID()))
			{
				return details;
			}
		}
		
		return null;
	}
	
	public void updateSubscriptionConfiguration(final String connectionId, final BaseSubscription subscription)	
	{
		final ConfiguredMqttConnectionDetails details = getMatchingConnection(connectionId);
		
		boolean matchFound = false;
		for (final TabbedSubscriptionDetails subscriptionDetails : details.getSubscription())
		{							
			if (subscriptionDetails.getTopic().equals(subscription.getTopic()))
			{
				if (subscription instanceof MqttSubscription)
				{
					subscriptionDetails.setQos(((MqttSubscription) subscription).getQos());
				}
				subscriptionDetails.setCreateTab(true);
				subscriptionDetails.setScriptFile(subscription.getDetails().getScriptFile());
				matchFound = true;
				break;
			}
		}
		
		// If no match found, add this subscription
		if (!matchFound)
		{
			final TabbedSubscriptionDetails subscriptionDetails = new TabbedSubscriptionDetails();
			subscriptionDetails.setTopic(subscription.getTopic());
			if (subscription instanceof MqttSubscription)
			{
				subscriptionDetails.setQos(((MqttSubscription) subscription).getQos());
			}
			subscriptionDetails.setCreateTab(true);
			subscriptionDetails.setScriptFile(subscription.getDetails().getScriptFile());
			details.getSubscription().add(subscriptionDetails);							
		}					
		
		saveConfiguration();
	}
	
	public void deleteSubscriptionConfiguration(final String connectionId, final BaseSubscription subscription)	
	{
		final ConfiguredMqttConnectionDetails details = getMatchingConnection(connectionId);
		
		TabbedSubscriptionDetails itemToRemove = null;
		
		for (final TabbedSubscriptionDetails subscriptionDetails : details.getSubscription())
		{							
			if (subscriptionDetails.getTopic().equals(subscription.getTopic()))
			{
				itemToRemove = subscriptionDetails;
				break;
			}
		}
		
		if (itemToRemove != null)
		{
			details.getSubscription().remove(itemToRemove);
		}		
		
		saveConfiguration();
	}
	
	// ===============================
	// === Setters and getters =======
	// ===============================
	
	public MqttSpyConfiguration getConfiguration()
	{
		return configuration;
	}
	
	public List<ConfiguredMqttConnectionDetails> getConnections()
	{
		return connections;
	}
	
	@Override
	public List<FormatterDetails> getFormatters()
	{
		return configuration.getFormatting().getFormatter();
	}

	@Override
	public boolean removeFormatter(final FormatterDetails formatter)
	{
		for (final ConfiguredMqttConnectionDetails connectionDetails : getConnections())
		{		
			// To avoid NPE below
			if (connectionDetails.getFormatter() == null)
			{
				continue;
			}
			
			if (formatter.getID().equals(((FormatterDetails) connectionDetails.getFormatter()).getID()))
			{
				connectionDetails.setFormatter(null);
			}
		}
		
		getFormatters().remove(formatter);
		
		return saveConfiguration();
	}

	@Override
	public int countFormatter(final FormatterDetails formatter)
	{
		int count = 0;
		for (final ConfiguredMqttConnectionDetails connectionDetails : getConnections())
		{
			if (connectionDetails.getFormatter() == null)
			{
				continue;
			}
			
			if (formatter.getID().equals(((FormatterDetails) connectionDetails.getFormatter()).getID()))
			{
				count++;
			}
		}
		
		return count;
	}
}
