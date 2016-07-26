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
 * Property for FX controls (e.g. table) containing a key and a value.
 */
public class KeyValueProperty
{
	/** Key as string property. */
	private SimpleStringProperty key;
	
	/** Value as string property. */
	private SimpleStringProperty value;
	
	/**
	 * Creates the KeyValueProperty object with the provided key and value.
	 * 
	 * @param key The key to set
	 * @param value The value to set
	 */
	public KeyValueProperty(final String key, final String value)
	{
		this.key = new SimpleStringProperty(key);
		this.value = new SimpleStringProperty(value);
	}
	
	/**
	 * The key property.
	 * 
	 * @return The key property as SimpleStringProperty
	 */
	public SimpleStringProperty keyProperty()
	{
		return this.key;
	}
	
	/**
	 * The value property.
	 * 
	 * @return The value property as SimpleStringProperty
	 */
	public SimpleStringProperty valueProperty()
	{
		return this.value;
	}
}
