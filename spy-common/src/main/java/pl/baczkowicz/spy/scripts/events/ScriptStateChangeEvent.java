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

package pl.baczkowicz.spy.scripts.events;

import pl.baczkowicz.spy.scripts.ScriptRunningState;

public class ScriptStateChangeEvent
{
	private String scriptName;
	
	private ScriptRunningState state;

	public ScriptStateChangeEvent(final String scriptName, final ScriptRunningState state)
	{
		this.scriptName = scriptName;
		this.state = state;
	}
	
	/**
	 * @return the scriptName
	 */
	public String getScriptName()
	{
		return scriptName;
	}

	/**
	 * @return the state
	 */
	public ScriptRunningState getState()
	{
		return state;
	}
}
