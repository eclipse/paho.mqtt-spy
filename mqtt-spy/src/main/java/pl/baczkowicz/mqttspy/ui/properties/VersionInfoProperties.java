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
package pl.baczkowicz.mqttspy.ui.properties;

import pl.baczkowicz.mqttspy.ui.controlpanel.ItemStatus;

public class VersionInfoProperties
{
	private ItemStatus status;
	
	private String title;
	
	private String details;

	/**
	 * @return the status
	 */
	public ItemStatus getStatus()
	{
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(ItemStatus status)
	{
		this.status = status;
	}

	/**
	 * @return the title
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * @return the details
	 */
	public String getDetails()
	{
		return details;
	}

	/**
	 * @param details the details to set
	 */
	public void setDetails(String details)
	{
		this.details = details;
	}
}
