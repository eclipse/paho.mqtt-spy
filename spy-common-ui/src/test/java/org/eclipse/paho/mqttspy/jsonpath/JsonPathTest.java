/***********************************************************************************
 * 
 * Copyright (c) 2017 Kamil Baczkowicz
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

package org.eclipse.paho.mqttspy.jsonpath;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.JsonPath;

public class JsonPathTest 
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(JsonPathTest.class);
	
	@Test
	public void testDoubleWithQuotes() 
	{
		final String messagePayload = "{ temp: \"20.6\", energy: 42}";
			
		final Double value = JsonPath.parse(messagePayload).read("$.temp", Double.class);
		
		logger.info("Value = " + value);
		
		assertEquals(value, (Double) 20.6);
	}
	
	@Test
	public void testDoubleWithtoutQuotes() 
	{
		final String messagePayload = "{ temp: 20.6, energy: 42}";
	
		final Double value = JsonPath.parse(messagePayload).read("$.temp", Double.class);
		
		logger.info("Value = " + value);
		
		assertEquals(value, (Double) 20.6);
	}
	
	@Test
	public void testIntWithtoutQuotes() 
	{
		final String messagePayload = "{ temp: 20, energy: 42}";
	
		final Double value = JsonPath.parse(messagePayload).read("$.temp", Double.class);
		
		logger.info("Value = " + value);
		
		assertEquals(value, (Double) 20.0);
	}

}
