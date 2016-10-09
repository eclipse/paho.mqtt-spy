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

import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * This as an interface for an Event Bus called Spy Bus. 
 * 
 * Basic principle: decouple producers of events from their consumers.
 * 
 * TODO: Support for queueing and batching. Subscribing could be extended to support batch, etc
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
     * @param subscriber The subscriber who is interested in these events
     * @param consumer The consumer method that is subscribing (one subscription per consumer method allowed)
     * @param eventType Type of event the consumer is interested in
     * @param <S> The type of the event.
     */
    <S> void subscribe(final Object subscriber, final Consumer<? super S> consumer, final Class<S> eventType);
    
    /**
     * Subscribes a consumer method for events of certain type, allowing an executor to be specified for asynchronous execution.
     *
     * @param subscriber The subscriber who is interested in these events
     * @param consumer The consumer method that is subscribing (one subscription per consumer method allowed)
     * @param eventType Type of event the consumer is interested in
     * @param executor The executor to use for asynchronous execution
     * @param <S> The type of the event.
     */
    <S> void subscribe(final Object subscriber, final Consumer<? super S> consumer, final Class<S> eventType, final Executor executor);

    /**
     * Subscribes a consumer method for events of certain type and given filter object.
     *
     * @param subscriber The subscriber who is interested in these events
     * @param consumer The consumer method that is subscribing (one subscription per consumer method allowed)
     * @param eventType Type of event the consumer is interested in
     * @param filter The filter object to perform filtering on (null = no filter)
     * @param <S> The type of the event.
     */
    <S> void subscribeWithFilterOnly(final Object subscriber, final Consumer<? super S> consumer, final Class<S> eventType, final Object filter);
    
    /**
     * Subscribes a consumer method for events of certain type and given filter object, allowing an executor to be specified for asynchronous
     * execution.
     *
     * @param subscriber The subscriber who is interested in these events
     * @param consumer The consumer method that is subscribing (one subscription per consumer method allowed)
     * @param eventType Type of event the consumer is interested in
     * @param executor The executor to use for asynchronous execution
     * @param filter The filter object to perform filtering on (null = no filter)
     * @param <S> The type of the event.
     */
    <S> void subscribe(final Object subscriber, final Consumer<? super S> consumer, final Class<S> eventType, final Executor executor,
            final Object filter);

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
     * @param eventType Type of event to unsubscribe to
     */
    void unsubscribeConsumer(final Object subscriber,  final Class<?> eventType);
}
