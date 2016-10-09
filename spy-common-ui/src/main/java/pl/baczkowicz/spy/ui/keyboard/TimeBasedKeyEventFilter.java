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
package pl.baczkowicz.spy.ui.keyboard;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 * This class filters out events that happen too close to each other.
 */
public class TimeBasedKeyEventFilter
{
	private final long minimumInterval;
	
	private Map<Object, Long> lastEvents = new HashMap<>();
	
	public TimeBasedKeyEventFilter(final long minimumInterval)
	{
		this.minimumInterval = minimumInterval;
	}
	
	public boolean processEvent(final KeyEvent event)
	{
		final Long lastEvent = lastEvents.get(event.getCode());
		final long now = new Date().getTime();
		
		if (lastEvent != null && lastEvent + minimumInterval > now)
		{
			return false;
		}
		
		lastEvents.put(event.getCode(), now);
		return true;
	}
	
	public boolean processEvent(final MouseEvent event)
	{
		final Long lastEvent = lastEvents.get(event.getClickCount());
		final long now = new Date().getTime();
		
		if (lastEvent != null && lastEvent + minimumInterval > now)
		{
			return false;
		}
		
		lastEvents.put(event.getClickCount(), now);
		return true;
	}
}
