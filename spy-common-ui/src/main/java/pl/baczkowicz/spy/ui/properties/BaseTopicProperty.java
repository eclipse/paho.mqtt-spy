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
package pl.baczkowicz.spy.ui.properties;

import javafx.beans.property.SimpleStringProperty;

/**
 * Property for FX controls (e.g. table) containing a topic.
 */
public class BaseTopicProperty
{
	/** Topic as string property. */
	private SimpleStringProperty topic;
	
	/**
	 * Creates the BaseTopicProperty object with the provided topic value.
	 * 
	 * @param topic The topic to set
	 */
	public BaseTopicProperty(final String topic)
	{
		this.topic = new SimpleStringProperty(topic);
	}
	
	/**
	 * The topic property.
	 * 
	 * @return The topic property as SimpleStringProperty
	 */
	public SimpleStringProperty topicProperty()
	{
		return this.topic;
	}
}
