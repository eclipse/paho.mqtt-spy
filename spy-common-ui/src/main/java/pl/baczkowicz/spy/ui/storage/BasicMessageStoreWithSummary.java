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
package pl.baczkowicz.spy.ui.storage;

import pl.baczkowicz.spy.common.generated.FormatterDetails;
import pl.baczkowicz.spy.formatting.FormattingManager;
import pl.baczkowicz.spy.messages.FormattedMessage;
import pl.baczkowicz.spy.storage.BasicMessageStore;

/**
 * Basic message store, keeping all messages in a list.
 */
public class BasicMessageStoreWithSummary<T extends FormattedMessage> extends BasicMessageStore<T>
{
	private MessageStoreGarbageCollector<T> messageStoreGarbageCollector;
	
	protected FormattingManager formattingManager;
	
	private MessageListWithObservableTopicSummary<T> messageListWithTopicSummary;
	
	public BasicMessageStoreWithSummary(final String name, final int preferredSize, final int maxSize, final int maxPayloadLength, 
			final FormattingManager formattingManager)
	{
		super(null);
		this.formattingManager = formattingManager;
		messageListWithTopicSummary = new MessageListWithObservableTopicSummary<T>(preferredSize, maxSize, name, messageFormat, maxPayloadLength); 
		setMessageList(messageListWithTopicSummary);
	}
	
	@Override
	public MessageListWithObservableTopicSummary<T> getMessageList()
	{
		return messageListWithTopicSummary;
	}

	@Override
	public void clear()
	{
		messageListWithTopicSummary.clear();
		messageListWithTopicSummary.getTopicSummary().clear();
	}	
	
	@Override
	public void setFormatter(final FormatterDetails messageFormat)
	{
		this.messageFormat = messageFormat;	
		messageListWithTopicSummary.getTopicSummary().setFormatter(messageFormat, formattingManager);
	}

	/**
	 * @return the messageStoreGarbageCollector
	 */
	public MessageStoreGarbageCollector<T> getMessageStoreGarbageCollector()
	{
		return messageStoreGarbageCollector;
	}

	/**
	 * @param messageStoreGarbageCollector the messageStoreGarbageCollector to set
	 */
	public void setMessageStoreGarbageCollector(final MessageStoreGarbageCollector<T> messageStoreGarbageCollector)
	{
		this.messageStoreGarbageCollector = messageStoreGarbageCollector;
	}	
}
