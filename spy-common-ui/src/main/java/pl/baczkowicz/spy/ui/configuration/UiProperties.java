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
package pl.baczkowicz.spy.ui.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.configuration.BaseConfigurationUtils;
import pl.baczkowicz.spy.configuration.PropertyFileLoader;
import pl.baczkowicz.spy.ui.panes.SpyPerspective;

public class UiProperties
{	
	/** Initial and minimal scene/stage width. */	
	public final static int DEFAULT_WIDTH = 800;

	/** Initial and minimal scene/stage height. */
	public final static int DEFAULT_HEIGHT = 600;
	
	public final static String WIDTH_PROPERTY = "application.width";
	
	public final static String HEIGHT_PROPERTY = "application.height";
	
	public final static String PERSPECTIVE_PROPERTY = "application.perspective";
	
	public final static String MESSAGE_PANE_RESIZE_PROPERTY = "application.panes.message.resize";

	public static final String MAXIMIZED_PROPERTY = "application.maximized";
	
	public static final String SUMMARYTABLE_PAYLOAD_MAX = "ui.summarytable.columns.payload.maxlength";
	
	public static final String BROWSER_LM_SIZE = "ui.messagebrowser.largemessage.size";
	
	public static final String BROWSER_LM_HIDE = "ui.messagebrowser.largemessage.hide";
	
	public static final String BROWSER_LM_SUBSTRING = "ui.messagebrowser.largemessage.substring";
	
	private final static Logger logger = LoggerFactory.getLogger(UiProperties.class);
	
	private static Integer summaryMaxPayloadLength;
	
	private static Integer largeMessageSize;
	
	private static Boolean largeMessageHide;
	
	private static Integer largeMessageSubstring;

	public static double getApplicationHeight(final PropertyFileLoader fileLoader)
	{
		return BaseConfigurationUtils.getDoubleProperty(HEIGHT_PROPERTY, DEFAULT_HEIGHT, fileLoader);		
	}
	
	public static boolean getApplicationMaximized(final PropertyFileLoader fileLoader)
	{
		return BaseConfigurationUtils.getBooleanProperty(MAXIMIZED_PROPERTY, Boolean.FALSE, fileLoader);		
	}
	
	public static double getApplicationWidth(final PropertyFileLoader fileLoader)
	{
		return BaseConfigurationUtils.getDoubleProperty(WIDTH_PROPERTY, DEFAULT_WIDTH, fileLoader);
	}
	
	public static int getSummaryMaxPayloadLength(final PropertyFileLoader fileLoader)
	{
		if (summaryMaxPayloadLength == null)
		{
			summaryMaxPayloadLength = BaseConfigurationUtils.getIntegerProperty(SUMMARYTABLE_PAYLOAD_MAX, 250, fileLoader);
		}
		
		return summaryMaxPayloadLength;
	}
	
	public static int getLargeMessageSize(final PropertyFileLoader fileLoader)
	{
		if (largeMessageSize == null)
		{
			largeMessageSize = BaseConfigurationUtils.getIntegerProperty(BROWSER_LM_SIZE, 10000, fileLoader);
		}
		
		return largeMessageSize;
	}
	
	public static boolean getLargeMessageHide(final PropertyFileLoader fileLoader)
	{
		if (largeMessageHide == null)
		{
			largeMessageHide = BaseConfigurationUtils.getBooleanProperty(BROWSER_LM_HIDE, Boolean.FALSE, fileLoader);
		}
		
		return largeMessageHide;
	}
	
	public static int getLargeMessageSubstring(final PropertyFileLoader fileLoader)
	{
		if (largeMessageSubstring == null)
		{
			largeMessageSubstring = BaseConfigurationUtils.getIntegerProperty(BROWSER_LM_SUBSTRING, 1000, fileLoader);
		}
		
		return largeMessageSubstring;
	}	

	public static SpyPerspective getApplicationPerspective(final PropertyFileLoader fileLoader)
	{
		final String value = fileLoader.getProperty(PERSPECTIVE_PROPERTY);
		
		try
		{
			return SpyPerspective.valueOf(value);
		}
		catch (IllegalArgumentException e)
		{
			logger.error("Invalid format " + value);
			return SpyPerspective.DEFAULT;
		}
	}

	public static boolean getResizeMessagePane(final PropertyFileLoader fileLoader)
	{
		return BaseConfigurationUtils.getBooleanProperty(MESSAGE_PANE_RESIZE_PROPERTY, Boolean.TRUE, fileLoader);
	}
}
