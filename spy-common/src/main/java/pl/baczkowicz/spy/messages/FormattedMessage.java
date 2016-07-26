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

import pl.baczkowicz.spy.common.generated.FormatterDetails;

/**
 * Represents a formatted message, e.g. received on a topic.
 */
public abstract class FormattedMessage extends BaseMessage
{	
	/** The first matching subscription. */ 
	private String subscription;

	private FormatterDetails lastUsedFormatter;
	
	private String formattedPayload;

	public FormattedMessage(final long id, final String topic)
	{
		super(id, topic);
	}

	public FormattedMessage(final long id, final String topic, final String payload, final Date date)
	{
		super(id, topic, payload, date);
	}
	
	public String getSubscription()
	{
		return subscription;
	}

	public void setSubscription(final String subscription)
	{
		this.subscription = subscription;
	}
	
	public FormatterDetails getLastUsedFormatter()
	{
		return lastUsedFormatter;
	}
	
	public String getFormattedPayload()
	{
		return formattedPayload;
	}

	public void setFormattedPayload(final String formattedPayload)
	{
		this.formattedPayload = formattedPayload;
	}

	public void setLastUsedFormatter(final FormatterDetails formatter)
	{
		this.lastUsedFormatter = formatter;		
	}
	
	public abstract byte[] getRawBinaryPayload();
}
