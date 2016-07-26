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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.utils.ConversionUtils;

public class BaseConfigurationUtils
{
	public static final String DEFAULT_GROUP = "default_group";
	
	public final static int DEFAULT_RECONNECTION_INTERVAL = 5000;
	
	private final static Logger logger = LoggerFactory.getLogger(BaseConfigurationUtils.class);
		
	public static void streamToFile (final InputStream input, final File output) throws IOException 
	{            
	    try (FileOutputStream out = new FileOutputStream(output)) 
	    {
	        IOUtils.copy(input, out);
	    }         
	}
	
	public static boolean getBooleanProperty(final String propertyName, final boolean defaultValue, final PropertyFileLoader fileLoader)
	{
		final String value = fileLoader.getProperty(propertyName);
		Boolean returnValue = defaultValue;
		
		// Default, when non present is X
		if (value == null || value.isEmpty())
		{
			returnValue = defaultValue; 
		}
		else
		{			
			try
			{
				returnValue = Boolean.valueOf(value);
			}
			catch (IllegalArgumentException e)
			{
				logger.error("Invalid format " + value);
			}
		}
		
		fileLoader.setProperty(propertyName, String.valueOf(returnValue));
		return returnValue;
	}
	
	public static double getDoubleProperty(final String propertyName, final double defaultValue, final PropertyFileLoader fileLoader)
	{
		final String value = fileLoader.getProperty(propertyName);		
		Double returnValue = defaultValue;
		
		try
		{
			returnValue = Double.valueOf(value);
		}
		catch (NumberFormatException e)
		{
			logger.error("Invalid number format " + value);
		}
		
		fileLoader.setProperty(propertyName, String.valueOf(returnValue));
		return returnValue;
	}	
	
	public static int getIntegerProperty(final String propertyName, final int defaultValue, final PropertyFileLoader fileLoader)
	{
		final String value = fileLoader.getProperty(propertyName);
		Integer returnValue = defaultValue;
		
		try
		{
			returnValue = Integer.valueOf(value);
		}
		catch (NumberFormatException e)
		{
			logger.error("Invalid number format " + value);
		}
		
		fileLoader.setProperty(propertyName, String.valueOf(returnValue));
		return returnValue;
	}	
	
	
	/**
	 * Encodes the given password to Base 64.
	 * 
	 * @param value The password to encode
	 * 
	 * @return The encoded password
	 */
	public static String encodePassword(final String value)
	{
		return ConversionUtils.stringToBase64(value);
	}
	
	/**
	 * Decodes the given password from Base 64.
	 * 
	 * @param value The password to decode
	 * 
	 * @return The decoded password
	 */
	public static String decodePassword(final String value)
	{
		return ConversionUtils.base64ToString(value);
	}
}
