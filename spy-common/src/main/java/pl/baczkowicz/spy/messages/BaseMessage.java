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
package pl.baczkowicz.spy.messages;

import java.util.Date;

/**
 * Represents a message, e.g. received on a topic.
 */
public class BaseMessage implements IBaseMessage
{
	/** Topic on which the message was received. */
	private final String topic;

	/** When the message was received. */
	private Date date;

	/** A unique message ID - guaranteed to be unique at runtime. */
	private final long id;

	private String payload;
	
	/**
	 * Creates a BaseMessage from the given parameters.
	 * 
	 * @param id Message ID
	 * @param topic Topic on which it was received
	 * @param message The received message
	 */
	public BaseMessage(final long id, final String topic)
	{
		this(MessageIdGenerator.getNewId(), topic, null, new Date());
	}
	
	/**
	 * Creates a BaseMessage from the given parameters.
	 * 
	 * @param id Message ID
	 * @param topic Topic on which it was received
	 * @param message The received message
	 */
	public BaseMessage(final String topic, final String payload)
	{
		this(MessageIdGenerator.getNewId(), topic, payload, new Date());
	}
	
	/**
	 * Creates a BaseMessage from the given parameters.
	 * 
	 * @param id Message ID
	 * @param topic Topic on which it was received
	 * @param date When the message was received
	 */
	public BaseMessage(final long id, final String topic, final String payload, final Date date)
	{
		this.id = id;
		this.topic = topic;
		this.payload = payload;
		this.date = date;
	}

	/**
	 * Gets the date.
	 * 
	 * @return The received date
	 */
	@Override
	public Date getDate()
	{
		return date;
	}

	/**
	 * Gets the message ID.
	 * 
	 * @return Message ID
	 */
	@Override
	public long getId()
	{
		return id;
	}

	@Override
	public String getTopic()
	{
		return topic;
	}
	
	@Override
	public String getPayload()
	{
		return payload;
	}
	
	@Override
	public void setPayload(final String payload)
	{
		this.payload = payload;
	}	
}
