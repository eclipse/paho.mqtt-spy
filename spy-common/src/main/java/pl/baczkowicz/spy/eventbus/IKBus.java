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
package pl.baczkowicz.spy.eventbus;

import java.util.function.Consumer;

/**
 * This as an interface for an Event Bus called Spy Bus. 
 * 
 * Basic principle: decouple producers of events from their consumers.
 * 
 * TODO: Support for asynchronous execution, queueing and batching. 
 * 
 * TODO: subscribing could be extended to support batch, custom executors, etc
 */
public interface IKBus
{
	/**
	 * Publish an event to consumers (if any).
	 * 
	 * @param event The event to publish
	 */
	void publish(final Object event);
	
	/**
	 * Subscribes a consumer method for events of certain type.
	 * 
	 * @param consumer The consumer method that is subscribing (one subscription per consumer method allowed)
	 * @param eventType Type of event the consumer is interested in
	 */
	<S> void subscribe(final Object subscriber, final Consumer<? super S> consumer, final Class<S> eventType);
	
	/**
	 * Subscribes a consumer method for events of certain type and given filter object.
	 * 
	 * @param consumer The consumer method that is subscribing (one subscription per consumer method allowed)
	 * @param eventType Type of event the consumer is interested in
	 * @param filter The filter object to perform filtering on (null => no filter)
	 */
	<S> void subscribe(final Object subscriber, final Consumer<? super S> consumer, final Class<S> eventType, final Object filter);
	
	/**
	 * Unsubscribes the given subscriber.
	 * 
	 * @param subscriber The subscriber to unsubscribe
	 */
	void unsubscribe(final Object subscriber);
	
	/**
	 * Unsubscribes the given consumer.
	 * 
	 * @param subscriber The subscriber to unsubscribe
	 * @param consumer The consumer to unsubscribe
	 */
	void unsubscribeConsumer(final Object subscriber, final Consumer<?> consumer);
	
	/**
	 * Unsubscribes the given consumer.
	 * 
	 * @param subscriber The subscriber to unsubscribe
	 * @param consumer The consumer to unsubscribe
	 */
	void unsubscribeConsumer(final Object subscriber,  Class<?> eventType);
}
