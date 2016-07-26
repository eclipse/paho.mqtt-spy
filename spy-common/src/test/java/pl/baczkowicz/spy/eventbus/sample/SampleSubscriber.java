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
package pl.baczkowicz.spy.eventbus.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.eventbus.FilterableEvent;

public class SampleSubscriber implements ISampleSubscriber
{
	final static Logger logger = LoggerFactory.getLogger(SampleSubscriber.class);

	private int messageCount;

	// Subscriptions to events can be put here, in an init or postConstruct methods
	//	public SampleSubscriber(final IKBus spyBus)
	//	{
	//		spyBus.subscribe(this, (Consumer<SampleCountChangeEvent>) this::onCountChange, SampleCountChangeEvent.class);
	//		spyBus.subscribe(this, (Consumer<KBusEvent>) this::onAnyEvent, KBusEvent.class);
	//	}
	
	@Override
	public void onInfoChange(FilterableEvent event)
	{
		logger.info("onInfoChange: {}", event);
	}
	
	public void onAnyEvent(Object event)
	{
		setMessageCount(getMessageCount() + 1);
		logger.info("onAnyEvent: {}", event);
	}

	@Override
	public void onCountChange(final SampleCountChangeEvent event)
	{
		setMessageCount(getMessageCount() + 1);
		logger.info("onCountChange: {}", event);		
	}

	/**
	 * @return the messageCount
	 */
	public int getMessageCount()
	{
		return messageCount;
	}

	/**
	 * @param messageCount the messageCount to set
	 */
	public void setMessageCount(int messageCount)
	{
		this.messageCount = messageCount;
	}
}
