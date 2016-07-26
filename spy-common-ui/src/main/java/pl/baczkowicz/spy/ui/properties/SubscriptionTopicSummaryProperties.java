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
package pl.baczkowicz.spy.ui.properties;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import pl.baczkowicz.spy.messages.FormattedMessage;

/**
 * Properties displayed in the subscription topic summary table.
 */
public class SubscriptionTopicSummaryProperties<T extends FormattedMessage> extends MessageContentProperties<T>
{
	/** Whether to include this topic in the list of messages for browsing. */
	private BooleanProperty show;
	
	/** How many messages have been received for that topic - subject to clean-up. */
	private IntegerProperty count;

	/**
	 * Creates SubscriptionTopicSummaryProperties with the supplied parameters.
	 * 
	 * @param show Whether to include this topic in the list of messages for browsing
	 * @param count How many messages have been received for that topic - subject to clean-up
	 * @param message The last message
	 * @param maxPayloadLength Maximum payload length to be displayed - to make sure UI remains responsive for large messages
	 */
	public SubscriptionTopicSummaryProperties(final Boolean show, final Integer count, final T message, final int maxPayloadLength)
	{
		super(message, maxPayloadLength);
		
		this.show = new SimpleBooleanProperty(show);	               
        this.count = new SimpleIntegerProperty(count);                    
	}

	/**
	 * 'Show' property.
	 * 
	 * @return 'Show' property as BooleanProperty
	 */
	public BooleanProperty showProperty()
	{
		return show;
	}

	/** 
	 * Count property.
	 *  
	 * @return Count property as IntegerProperty  
	 */	
	public IntegerProperty countProperty()
	{
		return count;
	}

	/**
	 * Setter for the count property.
	 * 
	 * @param count The new count value
	 */
	public void setCount(final Integer count)
	{
		this.count.set(count);
	}
}
