/***********************************************************************************
 * 
 * Copyright (c) 2016 Kamil Baczkowicz
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
package pl.baczkowicz.spy.ui.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.common.generated.ConnectionDetails;
import pl.baczkowicz.spy.common.generated.ConnectionGroup;
import pl.baczkowicz.spy.common.generated.ConnectionGroupReference;
import pl.baczkowicz.spy.common.generated.ConnectionReference;
import pl.baczkowicz.spy.configuration.BaseConfigurationUtils;
import pl.baczkowicz.spy.configuration.PropertyFileLoader;
import pl.baczkowicz.spy.exceptions.ConfigurationException;
import pl.baczkowicz.spy.exceptions.XMLException;
import pl.baczkowicz.spy.ui.panes.SpyPerspective;
import pl.baczkowicz.spy.ui.properties.ModifiableConnection;
import pl.baczkowicz.spy.ui.utils.DialogFactory;
import pl.baczkowicz.spy.utils.ThreadingUtils;
import pl.baczkowicz.spy.utils.TimeUtils;
import pl.baczkowicz.spy.xml.XMLParser;

public abstract class BaseConfigurationManager implements IConfigurationManager
{
	public static final String SPY_COMMON_SCHEMA = "/spy-common.xsd";

	private final static Logger logger = LoggerFactory.getLogger(BaseConfigurationManager.class);
	
	private List<ConfiguredConnectionGroupDetails> connectionGroups = new ArrayList<>();
	
	private ConfiguredConnectionGroupDetails rootGroup;
	
	private File loadedConfigurationFile;

	private Exception lastException;
	
	/** The application name. */
	public static String APPLICATION_NAME = "mqtt-spy";

	private PropertyFileLoader defaultPropertyFile;

	private PropertyFileLoader uiPropertyFile;
	
	protected void loadDefaultPropertyFile() throws ConfigurationException
	{
		// Load the default property file from classpath
		this.defaultPropertyFile = new PropertyFileLoader();
		this.defaultPropertyFile.readFromClassPath(getDefaultPropertyFileLocation());
	}
	
	public Object loadConfiguration(final XMLParser parser, final File file)
	{
		Object configuration;
		
		try
		{
			clear();
			configuration = parser.loadFromFile(file);
			
			setLoadedConfigurationFile(file);
			return configuration;
		}
		catch (XMLException e)
		{
			setLastException(e);
			DialogFactory.createErrorDialog("Invalid configuration file", "Cannot process the given configuration file. See the log file for more details.");					
			logger.error("Cannot process the configuration file at " + file.getAbsolutePath(), e);
		}
		catch (FileNotFoundException e)
		{
			setLastException(e);
			DialogFactory.createErrorDialog("Invalid configuration file", "Cannot read the given configuration file. See the log file for more details.");
			logger.error("Cannot read the configuration file from " + file.getAbsolutePath(), e);
		}
		
		return null;
	}
	

	public void createConnectionGroups(final List<ConnectionGroup> configuredGroups, final List<ModifiableConnection> configuredConnections)
	{						
		final List<ConnectionGroup> groupsWithoutParent = new ArrayList<>(configuredGroups);
		
		// Clear up resources - in case something was loaded before
		getConnectionGrops().clear();
		setRootGroup(null);
		
		// This is expected from v0.3.0
		for (final ConnectionGroup group : configuredGroups)
		{			
			final ConfiguredConnectionGroupDetails details = new ConfiguredConnectionGroupDetails(group, false);
			
			for (ConnectionGroupReference subgroup : group.getSubgroups())
			{
				groupsWithoutParent.remove(subgroup.getReference());
			}
			
			getConnectionGrops().add(details);						
		}
		
		// Create the root if no groups present (pre v0.3.0)
		if (getConnectionGrops().isEmpty() || groupsWithoutParent.isEmpty())
		{
			logger.debug("Creating root group called 'All connections'");
			setRootGroup(new ConfiguredConnectionGroupDetails(new ConnectionGroup(
					BaseConfigurationUtils.DEFAULT_GROUP, "All connections", new ArrayList<>(), new ArrayList<>()), false));
			
			getConnectionGrops().add(getRootGroup());
			
			// Assign all connections to the new root
			for (final ModifiableConnection connection : configuredConnections)
			{
				connection.setGroup(new ConnectionGroupReference(getRootGroup()));
				getRootGroup().getConnections().add(new ConnectionReference(connection));
			}
			
			getRootGroup().apply();
		}
		else
		{
			// Find the root group
			final String rootId = groupsWithoutParent.get(0).getID();
			for (final ConfiguredConnectionGroupDetails group : getConnectionGrops())
			{
				if (group.getID().equals(rootId))
				{
					setRootGroup(group);
					break;
				}
			}
			// At this point, new groups link to old connection and group objects, and old connection objects to old groups
			
			// Re-wire all connections
			updateTree(getRootGroup());
		}
	}
	
	private void updateTree(final ConfiguredConnectionGroupDetails parentGroup)
	{
		final List<ConnectionGroupReference> subgroups = new ArrayList<>(parentGroup.getSubgroups());
		parentGroup.getSubgroups().clear();
		
		for (final ConnectionGroupReference reference : subgroups)			
		{
			final ConnectionGroup group = (ConnectionGroup) reference.getReference();
			final ConfiguredConnectionGroupDetails groupDetails = findMatchingGroup(group);
			parentGroup.getSubgroups().add(new ConnectionGroupReference(groupDetails));
			groupDetails.setGroup(new ConnectionGroupReference(parentGroup));
			groupDetails.apply();
			
			// Recursive
			updateTree(groupDetails);
		}
		
		final List<ConnectionReference> connections = new ArrayList<>(parentGroup.getConnections());
		parentGroup.getConnections().clear();
		
		for (final ConnectionReference reference : connections)			
		{
			final ConnectionDetails connection = (ConnectionDetails) reference.getReference();
			final ModifiableConnection connectionDetails = findMatchingConnection(connection, new ArrayList<>(getConnections()));
			
			if (connectionDetails != null)
			{
				parentGroup.getConnections().add(new ConnectionReference(connectionDetails));
				connectionDetails.setGroup(new ConnectionGroupReference(parentGroup));	
				connectionDetails.apply();
			}
			else
			{
				logger.warn("Match not found for connection {}", connection.getName());
			}
		}
		
		parentGroup.apply();
	}
	
	public static ModifiableConnection findMatchingConnection(final ConnectionDetails connection, 
			final List<ModifiableConnection> connections)
	{
		for (final ModifiableConnection connectionDetails : connections)
		{
			if (connection.getID().equals(connectionDetails.getID()))
			{
				return connectionDetails;
			}
		}
		
		return null;
	}
	
	public List<ConfiguredConnectionGroupDetails> getOrderedGroups()
	{
		List<ModifiableConnection> orderedConnections = new ArrayList<>();		
		List<ConfiguredConnectionGroupDetails> orderedGroups = new ArrayList<>();
		
		orderedGroups.add(getRootGroup());
		sortConnections(getRootGroup(), orderedGroups, orderedConnections);
		
		return orderedGroups;
	}
	
	public List<ModifiableConnection> getOrderedConnections()
	{
		List<ModifiableConnection> orderedConnections = new ArrayList<>();		
		List<ConfiguredConnectionGroupDetails> orderedGroups = new ArrayList<>();
		
		sortConnections(getRootGroup(), orderedGroups, orderedConnections);
		
		return orderedConnections;
	}
	
	private void sortConnections(final ConfiguredConnectionGroupDetails parentGroup, 
			final List<ConfiguredConnectionGroupDetails> orderedGroups, List<ModifiableConnection> orderedConnections)
	{		
		for (final ConnectionGroupReference reference : parentGroup.getSubgroups())		
		{
			final ConfiguredConnectionGroupDetails group = (ConfiguredConnectionGroupDetails) reference.getReference();						
			orderedGroups.add(group);
			
			// Recursive
			sortConnections(group, orderedGroups, orderedConnections);
		}
		
		for (final ConnectionReference reference : parentGroup.getConnections())			
		{
			final ModifiableConnection connection = (ModifiableConnection) reference.getReference();
			orderedConnections.add(connection);
		}				
	}

	public List<ModifiableConnection> getConnections(final ConfiguredConnectionGroupDetails group)
	{
		List<ModifiableConnection> orderedConnections = new ArrayList<>();
		for (final ConnectionReference connetionRef : group.getConnections())
		{
			orderedConnections.add((ModifiableConnection) connetionRef.getReference());
		}
		return orderedConnections;
	}
	
	protected abstract void clear();

	protected void loadUiPropertyFile() throws ConfigurationException
	{
		// Load the UI property file
		if (!getUiPropertyFileObject().exists())
		{
			logger.info("Creating UI property file");
			createUiPropertyFileFromClassPath();
		}
		this.uiPropertyFile = new PropertyFileLoader();
		this.uiPropertyFile.readFromFileSystem(getUiPropertyFileObject());
	}
	
	public static String getDefaultHomeDirectory()
	{
		final String filePathSeparator = System.getProperty("file.separator");
		String userHomeDirectory = System.getProperty("user.home");
		
		if (!userHomeDirectory.endsWith(filePathSeparator))
		{
			userHomeDirectory = userHomeDirectory + filePathSeparator;
		}
		
		return userHomeDirectory + APPLICATION_NAME + filePathSeparator;
	}
	
	public static String getDefaultPropertyFileLocation()
	{
		return "/" + APPLICATION_NAME + ".properties";
	}
	
	public static String getUiPropertyFileLocation()
	{
		return "/" + APPLICATION_NAME + "-ui.properties";
	}
	
	public static String getDefaultConfigurationFileName()
	{			
		return APPLICATION_NAME + "-configuration.xml";
	}
	
	public static File getDefaultConfigurationFileObject()
	{			
		return new File(getDefaultHomeDirectory() + getDefaultConfigurationFileName());
	}
	
	/**
	 * Gets the default property file.
	 * 
	 * @return the defaultPropertyFile
	 */
	public PropertyFileLoader getDefaultPropertyFile()
	{
		return defaultPropertyFile;
	}

	/**
	 * Gets the UI property file.
	 * 
	 * @return the uiPropertyFile
	 */
	public PropertyFileLoader getUiPropertyFile()
	{
		return uiPropertyFile;
	}

	public static File getUiPropertyFileObject()
	{			
		return new File(getDefaultHomeDirectory() + getUiPropertyFileLocation());
	}
	
	
	public static boolean createUiPropertyFileFromClassPath()
	{
		final String origin = "/samples" + getUiPropertyFileLocation();
		try
		{			
			return copyFileFromClassPath(BaseConfigurationManager.class.getResourceAsStream(origin), getUiPropertyFileObject());
		}
		catch (IllegalArgumentException | IOException e)
		{
			// TODO: show warning dialog for invalid
			logger.error("Cannot copy file from {}", origin, e);
		}
		
		return false;
	} 
	
	public static File getDefaultHomeDirectoryFileObject()
	{			
		return new File(getDefaultHomeDirectory());
	}
	
	public static boolean copyFileFromClassPath(final InputStream orig, final File dest) throws IOException
	{
		getDefaultHomeDirectoryFileObject().mkdirs();
		streamToFile(orig, dest);

		return true;	
	}	
		
	public static void streamToFile (final InputStream input, final File output) throws IOException 
	{            
	    try (FileOutputStream out = new FileOutputStream(output)) 
	    {
	        IOUtils.copy(input, out);
	    }         
	}
	
	public String generateConnectionGroupId()
	{
		ThreadingUtils.sleep(1);
		return "cg" + TimeUtils.getMonotonicTime();
	}
	
	public static String generateConnectionId()
	{
		ThreadingUtils.sleep(1);
		return "conn" + TimeUtils.getMonotonicTime();		
	}
	
	public List<ConfiguredConnectionGroupDetails> getConnectionGrops()
	{
		return connectionGroups;
	}
	
	public ConfiguredConnectionGroupDetails getRootGroup()
	{
		return rootGroup;
	}
	
	public void setRootGroup(ConfiguredConnectionGroupDetails value)
	{
		this.rootGroup = value;
	}

	protected ConfiguredConnectionGroupDetails findMatchingGroup(final ConnectionGroup group)
	{
		for (final ConfiguredConnectionGroupDetails groupDetails : connectionGroups)
		{
			if (group.getID().equals(groupDetails.getID()))
			{
				return groupDetails;
			}
		}
		
		return null;
	}
	

	public Exception getLastException()
	{
		return lastException;
	}

	public void setLastException(Exception lastException)
	{
		this.lastException = lastException;
	}
	
	public File getLoadedConfigurationFile()
	{
		return loadedConfigurationFile;
	}
	
	public void setLoadedConfigurationFile(File file)
	{
		this.loadedConfigurationFile = file;		
	}

	
	public boolean isConfigurationWritable()
	{
		if (loadedConfigurationFile != null && loadedConfigurationFile.canWrite())
		{
			return true;
		}
		return false;
	}
	
	public boolean isConfigurationReadOnly()
	{
		if (loadedConfigurationFile != null && !loadedConfigurationFile.canWrite())
		{					
			return true;
		}
		
		return false;
	}
	
	
	public void updateUiProperty(final String propertyName, final String propertyValue)
	{
		getUiPropertyFile().setProperty(propertyName, propertyValue);
	}
	

	public static boolean createDefaultConfigFromFile(final File orig)
	{
		try
		{ 
			final File dest = BaseConfigurationManager.getDefaultConfigurationFileObject();
		
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

	public static boolean createDefaultConfigFromClassPath(final String name)
	{
		final String origin = "/samples" + "/" + name + "-" + APPLICATION_NAME + "-configuration.xml";
		try
		{			
			return BaseConfigurationManager.copyFileFromClassPath(BaseConfigurationManager.class.getResourceAsStream(origin), 
					BaseConfigurationManager.getDefaultConfigurationFileObject());
		}
		catch (IllegalArgumentException | IOException e)
		{
			// TODO: show warning dialog for invalid
			logger.error("Cannot copy configuration file from {}", origin, e);
		}
		
		return false;
	}
	

	public void saveUiProperties(final double width, final double height, boolean maximized, 
			final SpyPerspective selectedPerspective, final boolean resizeMessagePane)
	{
		updateUiProperty(UiProperties.WIDTH_PROPERTY, String.valueOf(width));
		updateUiProperty(UiProperties.HEIGHT_PROPERTY, String.valueOf(height));
		updateUiProperty(UiProperties.MAXIMIZED_PROPERTY, String.valueOf(maximized));
		updateUiProperty(UiProperties.PERSPECTIVE_PROPERTY, selectedPerspective.toString());
		updateUiProperty(UiProperties.MESSAGE_PANE_RESIZE_PROPERTY, String.valueOf(resizeMessagePane));
		
		// Other properties are read-only from file
		
		try
		{
			getUiPropertyFile().saveToFileSystem(APPLICATION_NAME + "-ui", getUiPropertyFileObject());
		}
		catch (IOException e)
		{
			logger.error("Cannot save UI properties", e);
		}
	}

}
