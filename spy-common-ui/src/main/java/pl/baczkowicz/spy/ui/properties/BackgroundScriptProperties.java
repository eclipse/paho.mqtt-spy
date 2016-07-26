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
import javafx.beans.property.SimpleStringProperty;

/**
 * This represents a single row displayed in the background publication scripts table.
 */
public class BackgroundScriptProperties
{
	/** The script location. */
	private SimpleStringProperty script;
	
	/** The 'auto-start' flag for this script. */	
	private SimpleBooleanProperty autoStart;
	
	/** The 'repeat' flag for this script. */	
	private SimpleBooleanProperty repeat;
		
	/**
	 * Creates a BackgroundScriptProperties with the given parameters.
	 * 
	 * @param script The script location
	 * @param autoStart The 'auto-start' flag for this script
	 * @param repeat The 'repeat' flag for this script
	 */
	public BackgroundScriptProperties(final String script, final boolean autoStart, final boolean repeat)
	{
		this.script= new SimpleStringProperty(script);
		this.autoStart = new SimpleBooleanProperty(autoStart);
		this.repeat = new SimpleBooleanProperty(repeat);
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
	 * 'Auto-start' flag.
	 * 
	 * @return 'Auto-start' flag as SimpleBooleanProperty
	 */
	public SimpleBooleanProperty autoStartProperty()
	{
		return this.autoStart;
	}
	
	/**
	 * 'Repeat' flag.
	 * 
	 * @return 'Repeat' flag as SimpleBooleanProperty
	 */
	public SimpleBooleanProperty repeatProperty()
	{
		return this.repeat;
	}
}
