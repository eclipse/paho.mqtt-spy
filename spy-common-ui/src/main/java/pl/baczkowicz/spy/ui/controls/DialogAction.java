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
package pl.baczkowicz.spy.ui.controls;

import javafx.scene.control.Button;

public class DialogAction
{
	private String heading;
	
	private String longText;

	private Button button;

	public DialogAction(final String heading, final String longText)
	{
		this.setHeading(heading);
		this.setLongText(longText);
	}

	/**
	 * @return the heading
	 */
	public String getHeading()
	{
		return heading;
	}

	/**
	 * @param heading
	 *            the heading to set
	 */
	public void setHeading(String heading)
	{
		this.heading = heading;
	}

	/**
	 * @return the longText
	 */
	public String getLongText()
	{
		return longText;
	}

	/**
	 * @param longText
	 *            the longText to set
	 */
	public void setLongText(String longText)
	{
		this.longText = longText;
	}

	public void setButton(Button button)
	{
		this.button = button;		
	}
	
	public Button getButton()
	{
		return button;
	}
}
