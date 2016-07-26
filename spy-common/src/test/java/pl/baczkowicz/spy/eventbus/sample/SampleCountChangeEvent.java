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
package pl.baczkowicz.spy.eventbus.sample;

import pl.baczkowicz.spy.eventbus.FilterableEvent;

public class SampleCountChangeEvent extends FilterableEvent
{
	private final String info;

	private final int count;

	public SampleCountChangeEvent(final String info, final int count)
	{
		this.info = info;
		this.count = count;
	}

	/**
	 * @return the info
	 */
	public String getInfo()
	{
		return info;
	}

	/**
	 * @return the count
	 */
	public int getCount()
	{
		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "SampleCountChangeEvent [info=" + info + ", count=" + count + "]";
	}
}
