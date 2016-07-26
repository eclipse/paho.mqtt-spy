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
package pl.baczkowicz.spy.ui.search;

import pl.baczkowicz.spy.messages.FormattedMessage;
import pl.baczkowicz.spy.storage.BasicMessageStore;
import pl.baczkowicz.spy.storage.MessageList;
import pl.baczkowicz.spy.ui.events.queuable.EventQueueManager;
import pl.baczkowicz.spy.ui.events.queuable.ui.BrowseRemovedMessageEvent;

public class UniqueContentOnlyFilter<T extends FormattedMessage> implements MessageFilter<T>
{
	private boolean uniqueContentOnly;
	
	private int deleted = 0;
	
	/** Stores events for the UI to be updated. */
	protected final EventQueueManager<T> uiEventQueue;

	private BasicMessageStore<T> store;
	
	public UniqueContentOnlyFilter(final BasicMessageStore<T> store, final EventQueueManager<T> uiEventQueue)
	{
		this.uiEventQueue = uiEventQueue;
		this.store = store;
	}
	
	@Override
	public boolean filter(final T message, final MessageList<T> messageList, final boolean updateUi)
	{
		if (!uniqueContentOnly || messageList.getMessages().size() == 0)
		{
			return false;			
		}
		
		final T lastMessage = messageList.getMessages().get(0);
		
		if (message.getFormattedPayload().equals(lastMessage.getFormattedPayload()) && message.getTopic().equals(lastMessage.getTopic()))
		{
			final T deletedMessage = messageList.getMessages().remove(0);
			
			if (updateUi)
			{
				uiEventQueue.add(store, new BrowseRemovedMessageEvent<T>(messageList, deletedMessage, 0));
			}
			deleted++;
		}
		
		return false;
	}

	/**
	 * Sets the flag.
	 * 
	 * @return the uniqueContentOnly
	 */
	public boolean isUniqueContentOnly()
	{
		return uniqueContentOnly;
	}

	/**
	 * Gets the flag.
	 * 
	 * @param uniqueContentOnly the uniqueContentOnly to set
	 */
	public void setUniqueContentOnly(boolean uniqueContentOnly)
	{
		this.uniqueContentOnly = uniqueContentOnly;
		
		if (!uniqueContentOnly)
		{
			reset();
		}
	}

	@Override
	public void reset()
	{
		deleted = 0;		
	}

	@Override
	public boolean isActive()
	{
		return deleted > 0;
	}

}
