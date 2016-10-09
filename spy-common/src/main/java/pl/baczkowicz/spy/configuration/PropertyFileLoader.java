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
package pl.baczkowicz.spy.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import pl.baczkowicz.spy.exceptions.ConfigurationException;

/**
 * Utility class for loading a property file and reading property values. 
 */
public class PropertyFileLoader
{	
	/** Properties read from the provided file. */
	private Properties properties;	
	
	/**
	 * Creates the loader.
	 */
	public PropertyFileLoader()
	{
		// Default constructor
	}
	 
	/**
	 * Reads a property file into memory.
	 * 
	 * @param propertyFileLocation Class path location
	 * 
	 * @throws ConfigurationException Thrown when cannot process the file
	 */
	public void readFromClassPath(final String propertyFileLocation) throws ConfigurationException
	{
		properties = readPropertyFileFromClassPath(propertyFileLocation);
	}
	
	/**
	 * Reads a property file into a Properties object.
	 * 
	 * @param propertyFileLocation Class path location
	 * 
	 * @throws ConfigurationException Thrown when cannot process the file
	 */
	public void readFromFileSystem(final File propertyFileLocation) throws ConfigurationException
	{
		properties = readPropertyFileFromFileSystem(propertyFileLocation);
	}
	
	/**
	 * Reads a property file into a Properties object.
	 * 
	 * @param propertyFileLocation Class path location
	 * @return Properties file
	 * 
	 * @throws ConfigurationException Thrown when cannot process the file
	 */
	public static Properties readPropertyFileFromClassPath(final String propertyFileLocation) throws ConfigurationException
	{
		final Properties fileProperties = new Properties();
	
		try
		{
			final InputStream inputStream = PropertyFileLoader.class.getResourceAsStream(propertyFileLocation);
			fileProperties.load(inputStream);
			
			if (inputStream == null)
			{
				throw new FileNotFoundException("Property file '" + propertyFileLocation + "' not found in the classpath");
			}
		}
		catch (IOException e)
		{
			throw new ConfigurationException("Cannot load the properties file", e);
		}
		return fileProperties;
	}	
	
	/**
	 * Reads a property file into a Properties object.
	 * 
	 * @param propertyFileLocation File system location
	 * @return Properties file
	 * 
	 * @throws ConfigurationException Thrown when cannot process the file
	 */
	public static Properties readPropertyFileFromFileSystem(final File propertyFileLocation) throws ConfigurationException
	{
		final Properties fileProperties = new Properties();
	
		try
		{		
			final InputStream inputStream = new FileInputStream(propertyFileLocation);
			
			fileProperties.load(inputStream);
			inputStream.close();		
		}
		catch (IOException e)
		{
			throw new ConfigurationException("Cannot load the properties file", e);
		}
		return fileProperties;
	}	
	
	/**
	 * Saves the properties to the given file.
	 * 
	 * @param propertyFileLocation File system location
	 * 
	 * @throws IOException Thrown when cannot save to the given location
	 */
	public void saveToFileSystem(final String appName, final File propertyFileLocation) throws IOException
	{
		final OutputStream outputStream = new FileOutputStream(propertyFileLocation);
		properties.store(outputStream, "=== " + appName + " properties ===");
	}

	/**
	 * Retrieve a value of the specified property.
	 *  
	 * @param propertyName Name of the property to retrieve
	 * 
	 * @return Value of the property or an empty string if it doesn't exist
	 */
	public String getProperty(final String propertyName)
	{
		return properties.getProperty(propertyName, "");
	}
	
	/**
	 * Retrieve a value of the specified property.
	 *  
	 * @param propertyName Name of the property to retrieve
	 * 
	 * @return Value of the property or false if it doesn't exist
	 */
	public boolean getBooleanProperty(final String propertyName)
	{
		return new Boolean(properties.getProperty(propertyName, "false"));
	}
	
	/**
	 * Save a value of the specified property.
	 *  
	 * @param propertyName Name of the property to retrieve
	 * @param propertyValue Value of the property
	 */
	public void setProperty(final String propertyName, final String propertyValue)
	{
		properties.setProperty(propertyName, propertyValue);
	}
		
	/**
	 * Returns the build number, e.g. "16".
	 * 
	 * @return Build number property as string
	 */
	public String getBuildNumber()
	{
		return getProperty(BasePropertyNames.BUILD_PROPERTY);
	}
	
	/**
	 * Returns the full version number as string, e.g. "0.1.0-beta-10".
	 * 
	 * @return Full version number as string
	 */
	public String getFullVersionNumber()
	{
		return getProperty(BasePropertyNames.VERSION_PROPERTY) + "-" + getBuildNumber();
	}
	
	/**
	 * Returns the full version name as string, e.g. "0.1.0 beta (build 10)".
	 * @return
	 */
	public String getFullVersionName()
	{
		return getProperty(BasePropertyNames.VERSION_PROPERTY).replace("-", " ") + " (build " + getBuildNumber() + ")";
	}

	public String getApplicationLogo()
	{
		return getProperty(BasePropertyNames.LOGO_PROPERTY);
	}
	
	public String getApplicationName()
	{
		return getProperty(BasePropertyNames.NAME_PROPERTY);
	}
	
	public String getApplicationWikiUrl()
	{
		return getProperty(BasePropertyNames.WIKI_URL);
	}
}
