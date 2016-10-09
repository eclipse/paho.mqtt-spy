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

package pl.baczkowicz.spy.ui.events;

import pl.baczkowicz.spy.ui.properties.ChartSeriesProperties;
import javafx.stage.Window;

public class ShowEditChartSeriesWindowEvent
{
	private final Window parent;
	private ChartSeriesProperties editedProperties;

	public ShowEditChartSeriesWindowEvent(final Window parent, final ChartSeriesProperties editedProperties)
	{
		this.parent = parent;
		this.editedProperties = editedProperties;
	}

	public Window getParent()
	{
		return parent;
	}

	/**
	 * @return the editedProperties
	 */
	public ChartSeriesProperties getEditedProperties()
	{
		return editedProperties;
	}
}
