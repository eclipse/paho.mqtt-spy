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

import java.text.SimpleDateFormat;

/** 
 * Time and date related utilities.
 */
public class TimeUtils
{	
	public static final int ONE_SECOND = 1000;

	public static final int ONE_MINUTE = 60 * ONE_SECOND;
	
	public static final int ONE_HOUR = 60 * ONE_MINUTE;
	
	public static final int ONE_DAY = 24 * ONE_HOUR;
	
	public final static String DATE_FORMAT_WITH_MILLISECONDS = "yyyy/MM/dd HH:mm:ss:SSS";
	
	public final static String DATE_FORMAT_WITH_SECONDS = "yyyy/MM/dd HH:mm:ss";
	
	public final static String DATE_FORMAT_NO_TIME = "yyyy/MM/dd";
	
	public final static String TIME_FORMAT = "HH:mm:ss";

	public final static SimpleDateFormat DATE_WITH_MILLISECONDS_SDF = new SimpleDateFormat(DATE_FORMAT_WITH_MILLISECONDS);
	
	public final static SimpleDateFormat DATE_WITH_SECONDS_SDF = new SimpleDateFormat(DATE_FORMAT_WITH_SECONDS);
	
	public final static SimpleDateFormat DATE_SDF = new SimpleDateFormat(DATE_FORMAT_NO_TIME);
	
	public final static SimpleDateFormat TIME_SDF = new SimpleDateFormat(TIME_FORMAT);
	
	/**
	 * Returns the monotonic (not system) time in milliseconds. This can be used
	 * for measuring time intervals as this time is not affected by time
	 * adjustment in the OS.
	 * 
	 * @return The monotonic time in milliseconds
	 */
	public static long getMonotonicTime()
	{
		return System.nanoTime() / 1000000;
	}
}
