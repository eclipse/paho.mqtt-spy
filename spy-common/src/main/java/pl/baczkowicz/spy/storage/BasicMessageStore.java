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
package pl.baczkowicz.spy.storage;

import java.util.List;

import pl.baczkowicz.spy.common.generated.ConversionMethod;
import pl.baczkowicz.spy.common.generated.FormatterDetails;
import pl.baczkowicz.spy.formatting.FormattingUtils;

/**
 * Basic message store, keeping all messages in a list.
 */
public class BasicMessageStore<T> implements MessageStore<T>
{
	private MessageList<T> messageList;
		
	/** The message format used for this message store. */
	protected FormatterDetails messageFormat = FormattingUtils.createBasicFormatter("default", "Plain", null, ConversionMethod.PLAIN);

	public BasicMessageStore(final MessageList<T> messageList)
	{
		setMessageList(messageList);
	}	
	
	public void setMessageList(final MessageList<T> messageList)
	{
		this.messageList = messageList;
	}
	
	public void messageReceived(final T message)	
	{
		storeMessage(message);
	}
	
	public T storeMessage(final T message)
	{
		if (message != null)
		{
			return messageList.add(message);
		}	
		
		return null;
	}

	public List<T> getMessages()
	{
		return messageList.getMessages();
	}
	
	public MessageList<T> getMessageList()
	{
		return messageList;
	}

	public void clear()
	{
		messageList.clear();
	}	
	
	public void setFormatter(final FormatterDetails messageFormat)
	{
		this.messageFormat = messageFormat;		
	}
	
	public FormatterDetails getFormatter()
	{
		return messageFormat;
	}
	
	public boolean browsingFiltersEnabled()
	{
		return false;
	}
	
	public boolean messageFiltersEnabled()
	{
		return false;
	}

	public String getName()
	{
		return messageList.getName();
	}	
	
	public void cleanUp()
	{
		clear();
	}
}
