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

/**
 * Indicates the running state of a script.
 */
public enum ScriptRunningState
{
	NOT_STARTED("Not started"), FAILED("Failed"), RUNNING("Running"), STOPPED("Stopped"), 
	FINISHED("Finished"), FROZEN("Not responding");

	private final String name;

	private ScriptRunningState(final String s)
	{
		name = s;
	}

	public boolean equalsName(String otherName)
	{
		return (otherName == null) ? false : name.equals(otherName);
	}

	public String toString()
	{
		return name;
	}
}
