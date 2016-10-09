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

import java.util.List;

import pl.baczkowicz.spy.eventbus.FilterableEvent;
import pl.baczkowicz.spy.messages.FormattedMessage;
import pl.baczkowicz.spy.ui.events.queuable.ui.BrowseRemovedMessageEvent;

public class MessageRemovedEvent<T extends FormattedMessage> extends FilterableEvent
{
	private final List<BrowseRemovedMessageEvent<T>> messages;

	public MessageRemovedEvent(final List<BrowseRemovedMessageEvent<T>> messages, final Object filter)
	{
		this.messages = messages;
		super.setFilter(filter);
	}

	/**
	 * @return the events
	 */
	public List<BrowseRemovedMessageEvent<T>> getMessages()
	{
		return messages;
	}
}
