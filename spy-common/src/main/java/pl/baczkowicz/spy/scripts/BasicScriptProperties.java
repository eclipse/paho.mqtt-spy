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
package pl.baczkowicz.spy.scripts;

import pl.baczkowicz.spy.common.generated.ScriptDetails;

/**
 * This class represents basic script properties.
 */
public class BasicScriptProperties
{
	/** Name of the script. */
	private String name;
		
	/** Script timeout - used for detecting stalled scripts. */
	private long scriptTimeout = ScriptHealthDetector.DEFAULT_THREAD_TIMEOUT;

	/** Configured script details. */
	private ScriptDetails scriptDetails;
	
	/**
	 * Creates a BasicScriptProperties.
	 */
	public BasicScriptProperties()
	{
		// Default
	}

	// ===============================
	// === Setters and getters =======
	// ===============================
	
	/**
	 * Sets the script's name.
	 * 
	 * @param scriptName Script name
	 */
	public void setName(String scriptName)
	{
		this.name = scriptName;
	}

	/** 
	 * Sets the script details.
	 * 
	 * @param scriptDetails Script details
	 */
	public void setScriptDetails(final ScriptDetails scriptDetails)
	{
		this.scriptDetails = scriptDetails;		
	}
	
	/**
	 * Gets the script details.
	 * 
	 * @return Script details
	 */
	public ScriptDetails getScriptDetails()
	{
		return this.scriptDetails ;
	}
	
	/**
	 * Gets the script name.
	 * 
	 * @return Name of the script
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the script timeout.
	 * 
	 * @param customTimeout The new custom timeout
	 */
	public void setScriptTimeout(long customTimeout)
	{
		this.scriptTimeout = customTimeout;		
	}
	
	/**
	 * Gets the script timeout.
	 * 
	 * @return Script timeout
	 */
	public long getScriptTimeout()
	{
		return scriptTimeout;
	}

	/**
	 * Gets the repeat flag.
	 * 
	 * @return True is the script is set to repeat
	 */
	public boolean isRepeat()
	{
		return scriptDetails.isRepeat();
	}
}
