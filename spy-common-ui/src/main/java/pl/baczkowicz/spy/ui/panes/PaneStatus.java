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
package pl.baczkowicz.spy.ui.panes;

public class PaneStatus
{
	private PaneVisibilityStatus visibility = PaneVisibilityStatus.NOT_LOADED;
	
	private PaneVisibilityStatus requestedVisibility = PaneVisibilityStatus.NOT_LOADED;
	
	private PaneVisibilityStatus previousVisibility = PaneVisibilityStatus.NOT_LOADED;

	public PaneStatus()
	{
		// Default
	}

	/**
	 * Gets the visibility status.
	 * 
	 * @return the visibility
	 */
	public PaneVisibilityStatus getVisibility()
	{
		return visibility;
	}

	/**
	 * Sets the visibility status.
	 * 
	 * @param visibility the visibility to set
	 */
	public void setVisibility(final PaneVisibilityStatus visibility)
	{
		// Store the previous value
		if (!visibility.equals(this.visibility))
		{
			this.previousVisibility = this.visibility;
		}
		
		this.visibility = visibility;
	}

	/**
	 * Sets the previous visibility status.
	 * 
	 * @return the previousVisibility
	 */
	public PaneVisibilityStatus getPreviousVisibility()
	{
		return previousVisibility;
	}

	/**
	 * Gets the previous visibility status.
	 * 
	 * @param previousVisibility the previousVisibility to set
	 */
	public void setPreviousVisibility(PaneVisibilityStatus previousVisibility)
	{
		this.previousVisibility = previousVisibility;
	}

	/**
	 * Sets the requested visibility.
	 * 
	 * @return the requestedVisibility
	 */
	public PaneVisibilityStatus getRequestedVisibility()
	{
		return requestedVisibility;
	}

	/**
	 * Gets the requested visibility.
	 * 
	 * @param requestedVisibility the requestedVisibility to set
	 */
	public void setRequestedVisibility(PaneVisibilityStatus requestedVisibility)
	{
		this.requestedVisibility = requestedVisibility;
	}
}
