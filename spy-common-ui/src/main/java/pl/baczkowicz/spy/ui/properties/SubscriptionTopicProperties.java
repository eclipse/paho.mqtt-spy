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

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Extended properties for a topic (e.g. used as subscription topic settings). 
 */
public class SubscriptionTopicProperties extends BaseTopicProperty
{
	/** Quality of service property for this subscription topic. */
	private SimpleIntegerProperty qos;
	
	/** The 'show' flag for this subscription topic. */	
	private SimpleBooleanProperty show;
	
	/** The script property associated with this topic. */
	private SimpleStringProperty script;
			
	/**
	 * Creates a SubscriptionTopicProperties with the supplied parameters.
	 * 
	 * @param topic Topic
	 * @param script Script location
	 * @param qos Quality of service
	 * @param show 'Show' flag
	 */
	public SubscriptionTopicProperties(final String topic, final String script, final int qos, final boolean show)
	{
		super(topic);

		this.script= new SimpleStringProperty(script);
		this.qos = new SimpleIntegerProperty(qos);
		this.show = new SimpleBooleanProperty(show);
	}
	
	/**
	 * The script location property.
	 * 
	 * @return The script location as SimpleStringProperty
	 */
	public SimpleStringProperty scriptProperty()
	{
		return this.script;
	}

	/**
	 * Quality of service property.
	 * 
	 * @return Qos as SimpleIntegerProperty
	 */
	public SimpleIntegerProperty qosProperty()
	{
		return this.qos;
	}
	
	/**
	 * 'Show' flag.
	 * 
	 * @return 'Show' flag as SimpleBooleanProperty
	 */
	public SimpleBooleanProperty showProperty()
	{
		return this.show;
	}
}
