/***********************************************************************************
 * 
 * Copyright (c) 2016 Kamil Baczkowicz
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
package pl.baczkowicz.spy.ui.charts;

public enum ChartSeriesStatusEnum
{
	NO_MESSAGES("No messages"),
	OK("OK"), 
	ERROR("Error");

	private final String value;

	ChartSeriesStatusEnum(String v)
	{
		value = v;
	}

	public String value()
	{
		return value;
	}

	public static ChartSeriesStatusEnum fromValue(final String v)
	{
		for (ChartSeriesStatusEnum c : ChartSeriesStatusEnum.values())
		{
			if (c.value.equals(v))
			{
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}
}
