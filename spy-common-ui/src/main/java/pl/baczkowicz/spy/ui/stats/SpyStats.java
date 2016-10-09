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
package pl.baczkowicz.spy.ui.stats;

import java.util.Date;

public class SpyStats
{
    private long id;

    private Date startDate;

    private long connections;

    private long subscriptions;

    private long messagesPublished;

    private long messagesReceived;

    public SpyStats(final long id, final Date startDate, final long connections, final long subscriptions, final long messagesPublished, final long messagesReceived)
    {
    	this.id = id;
    	this.startDate = startDate;
    	this.connections = connections;
    	this.subscriptions = subscriptions;
    	this.messagesPublished = messagesPublished;
    	this.messagesReceived = messagesReceived;
    }
    
	/**
	 * @return the id
	 */
	public long getId()
	{
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id)
	{
		this.id = id;
	}

	/**
	 * @return the startDate
	 */
	public Date getStartDate()
	{
		return startDate;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(Date startDate)
	{
		this.startDate = startDate;
	}

	/**
	 * @return the connections
	 */
	public long getConnections()
	{
		return connections;
	}

	/**
	 * @param connections the connections to set
	 */
	public void setConnections(long connections)
	{
		this.connections = connections;
	}

	/**
	 * @return the subscriptions
	 */
	public long getSubscriptions()
	{
		return subscriptions;
	}

	/**
	 * @param subscriptions the subscriptions to set
	 */
	public void setSubscriptions(long subscriptions)
	{
		this.subscriptions = subscriptions;
	}

	/**
	 * @return the messagesPublished
	 */
	public long getMessagesPublished()
	{
		return messagesPublished;
	}

	/**
	 * @param messagesPublished the messagesPublished to set
	 */
	public void setMessagesPublished(long messagesPublished)
	{
		this.messagesPublished = messagesPublished;
	}

	/**
	 * @return the messagesReceived
	 */
	public long getMessagesReceived()
	{
		return messagesReceived;
	}

	/**
	 * @param messagesReceived the messagesReceived to set
	 */
	public void setMessagesReceived(long messagesReceived)
	{
		this.messagesReceived = messagesReceived;
	}
}
