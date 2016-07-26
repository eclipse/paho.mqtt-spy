/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
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


/**
 * Constants for loading the configuration.
 */
public final class MqttSpyDaemonConstants
{
	/** Configuration package. */
	public static final String PACKAGE = "pl.baczkowicz.mqttspy.daemon.configuration.generated";
	
	/** Configuration schema. */
	public static final String SCHEMA = "/mqtt-spy-daemon-configuration.xsd";
	
	/** Location of the properties file. */
	public static final String DEFAULT_PROPERTIES_FILE_NAME = "/mqtt-spy-daemon.properties";
}
