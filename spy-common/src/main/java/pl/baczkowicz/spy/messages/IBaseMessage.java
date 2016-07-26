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
 * Basic interface for a message (e.g. used by scripts accessing received or to be published messages.
 */
public interface IBaseMessage
{
	long getId();
	
	Date getDate();
	
	/**
	 * Gets the message's topic.
	 * 
	 * @return The topic string
	 */
	String getTopic();
	
	/**
	 * Gets the message's payload.
	 * 
	 * @return The payload string
	 */
	String getPayload();
	
	/**
	 * Sets the payload on the message.
	 * 
	 * @param payload The new payload
	 */
	void setPayload(final String payload);
}
