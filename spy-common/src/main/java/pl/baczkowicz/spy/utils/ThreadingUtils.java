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
package pl.baczkowicz.spy.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.utils.ThreadingUtils;

/** 
 * Threading related utilities.
 */
public class ThreadingUtils
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(ThreadingUtils.class);
	
	/** Log entry for starting a thread. */
	private static final String STARTING_THREAD = "Starting thread ID=%s \"%s\" ...";
	
	/** Log entry for stopping a thread. */
	private static final String ENDING_THREAD = "Ending thread ID=%s \"%s\" ...";
	
	/** Application name. */
	private static String APP_NAME = "mqtt-spy";	
	
	/**
	 * Logs the fact of starting a thread.
	 */
	private static void logThreadStarting()
	{
		if (logger.isTraceEnabled())
		{
			logger.trace(String.format(ThreadingUtils.STARTING_THREAD, Thread.currentThread().getId(), Thread.currentThread().getName()));
		}
	}
	
	/**
	 * Logs the fact of starting a thread.
	 */
	public static void logThreadStarting(final String threadName)
	{
		Thread.currentThread().setName(APP_NAME + " [" + threadName + "]");
		logThreadStarting();
	}
	
	/**
	 * Logs the fact of finishing a thread.
	 */
	public static void logThreadEnding()
	{
		if (logger.isTraceEnabled())
		{
			logger.trace(String.format(ThreadingUtils.ENDING_THREAD, Thread.currentThread().getId(), Thread.currentThread().getName()));
		}
	}
	
	/**
	 * Performs a sleep on the current thread.
	 * 
	 * @param milliseconds How log to sleep for in milliseconds
	 * 
	 * @return True if interrupted
	 */
	public static boolean sleep(final long milliseconds)	
	{
		try
		{
			Thread.sleep(milliseconds);
			return false;
		}
		catch (InterruptedException e)
		{
			logger.warn("Thread {} has been interrupted", Thread.currentThread().getName(), e);
			return true;
		}
	}
	
	/**
	 * Sets the application name.
	 * 
	 * @param name The prefix to be used for all threads names
	 */
	public static void setAppName(final String name)
	{
		APP_NAME = name;
	}
}
