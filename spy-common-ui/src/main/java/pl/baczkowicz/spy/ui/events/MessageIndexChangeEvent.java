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

import pl.baczkowicz.spy.eventbus.FilterableEvent;

public class MessageIndexChangeEvent extends FilterableEvent
{
	private final int index;
	
	private final Object dispatcher;

	public MessageIndexChangeEvent(final int index, final Object filter, final Object dispatcher)
	{
		this.index = index;
		this.dispatcher = dispatcher;
		super.setFilter(filter);
	}

	/**
	 * @return the index
	 */
	public int getIndex()
	{
		return index;
	}

	/**
	 * @return the dispatcher
	 */
	public Object getDispatcher()
	{
		return dispatcher;
	}
}
