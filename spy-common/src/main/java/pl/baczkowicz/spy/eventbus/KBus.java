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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KBus implements IKBus
{
    /** Logger. */
    private static final Logger logger = LoggerFactory.getLogger(KBus.class);

    /** Map of subscribers to the consumer methods for that subscriber. */
    private final Map<Object, Set<Consumer<?>>> subscribers = new HashMap<>();

    /** Map of Consumers to filters. */
    private final Map<Consumer<?>, Object> consumerFilters = new HashMap<>();

    /** Map of Consumers to the class types. */
    private final Map<Consumer<?>, Class<?>> consumerTypes = new HashMap<>();

    /** Map of Class types to consumers.  This is used for performance reasons and is recalculated after a change. */
    private final Map<Class<?>, Collection<Consumer<?>>> typeConsumers = new HashMap<>();
    
    /** Map of Class types to executors. This is used for asynchronous execution. */
    private final Map<Consumer<?>, Executor> consumerExecutors = new HashMap<>();
	
	private Collection<Consumer<?>> getConsumersForType(final Object event)
	{
		Collection<Consumer<?>> matchedConsumers;
		
		// Try to get previously matched consumers
		final Collection<Consumer<?>> previouslyMatchedConsumers = typeConsumers.get(event.getClass());
		
		// If none available, try to match
		if (previouslyMatchedConsumers == null)
		{		
			matchedConsumers = matchConsumersForType(event.getClass());
			
			typeConsumers.put(event.getClass(), matchedConsumers);
		}
		else
		{
			matchedConsumers = previouslyMatchedConsumers;
		}
		
		return matchedConsumers;
	}
	
	private Collection<Consumer<?>> matchConsumersForType(final Class<?> eventType)
	{
		final Collection<Consumer<?>> matchedConsumers = new ArrayList<>();
	
		logger.trace("Matching consumers for type {}", eventType);
		
		synchronized (consumerTypes)
		{
			for (final Consumer<?> consumer : consumerTypes.keySet())
			{
				final Class<?> consumerType = consumerTypes.get(consumer);
				
				// Compares two Classes with each other (because of that couldn't use instanceof or isInstance)
				if (consumerType.isAssignableFrom(eventType))
				{
					matchedConsumers.add(consumer);
				}
			}
		}
		
		logger.trace("Matched {} consumers for type {}: {}", matchedConsumers.size(), eventType, matchedConsumers);
		
		return matchedConsumers;
	}

	/**
     * Publishes an event in a synchronous way.
     */
    @SuppressWarnings("unchecked")
	@Override
    public void publish(final Object event)
    {
        final Collection<Consumer<?>> matchedConsumers = getConsumersForType(event);

        for (final Consumer<?> consumer : matchedConsumers)
        {
            // No need to synchronise here, read-only
            final Object filter = consumerFilters.get(consumer);

            try
            {
                if (filter == null)
                {
                    notifyConsumer((Consumer<Object>) consumer, event);
                }
                else if (event instanceof IFilterableEvent && filter.equals(((IFilterableEvent) event).getFilter()))
                {
                    notifyConsumer((Consumer<Object>) consumer, event);
                }
            }
            catch (final ClassCastException e)
            {
                logger.warn("Consumer {} can't accept events of type = {}", consumer, event.getClass(), e);
            }
        }
    }
    
    /**
     * Notifies the consumer with the event. If an executor has been specified, it is used.
     *
     * @param consumer Consumer of the event
     * @param event The event to notify
     */
    private void notifyConsumer(final Consumer<Object> consumer, final Object event)
    {
        final Executor executor = consumerExecutors.get(consumer);
        
        if (executor == null)
        {
            consumer.accept(event);
        }
        else
        {
            executor.execute(() -> { consumer.accept(event); });
        }
    }
	
	private void recalculateExistingMappings()
	{
		// Recalculate all existing eventType to consumer mappings
		for (final Class<?> type : typeConsumers.keySet())
		{	
			typeConsumers.put(type, matchConsumersForType(type));
		}
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public <S> void subscribe(final Object subscriber, final Consumer<? super S> consumer, final Class<S> eventType)
    {
        subscribe(subscriber, consumer, eventType, null, null);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <S> void subscribe(final Object subscriber, final Consumer<? super S> consumer, final Class<S> eventType, final Executor executor)
    {
        subscribe(subscriber, consumer, eventType, executor, null);        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S> void subscribeWithFilterOnly(final Object subscriber, final Consumer<? super S> consumer, final Class<S> eventType, final Object filter)
    {
        subscribe(subscriber, consumer, eventType, null, filter);      
    }    

    /**
     * {@inheritDoc}
     */
    @Override
    public <S> void subscribe(final Object subscriber, final Consumer<? super S> consumer, final Class<S> eventType, final Executor executor,
            final Object filter)
    {
        synchronized (consumerTypes)
        {
            Set<Consumer<?>> consumers = subscribers.get(subscriber);

            if (consumers == null)
            {
                consumers = new HashSet<>();
                subscribers.put(subscriber, consumers);
            }
            consumers.add(consumer);

            consumerFilters.put(consumer, filter);
            consumerTypes.put(consumer, eventType);
            consumerExecutors.put(consumer, executor);

            recalculateExistingMappings();
        }        
    }

	@Override
	public void unsubscribe(final Object subscriber)
	{
		synchronized (consumerTypes)
		{
			logger.trace("Trying to remove {} from subscribers", subscriber);
			
			final Set<Consumer<?>> removed = subscribers.remove(subscriber);
						
			if (removed != null)
			{
				for (final Consumer<?> consumer : removed)
				{
					consumerFilters.remove(consumer);		
					consumerTypes.remove(consumer);				
				}
				logger.trace("Removed consumers: {}", removed.size());
			}
			else
			{
				logger.warn("Removed consumers: 0");	
			}
					
			recalculateExistingMappings();
		}
	}
	
	@Override
	public void unsubscribeConsumer(final Object subscriber, final Consumer<?> consumer)
	{
		synchronized (consumerTypes)
		{
			logger.trace("Trying to remove {} owned by {}", consumer, subscriber);
			
			// Remove from subscriber's list of consumers
			final Set<Consumer<?>> consumers = subscribers.get(subscriber);
			if (consumers != null)
			{
				consumers.remove(consumer);
			}
			
			logger.trace("Removing {} from filters; contains: {}", consumer, consumerFilters.containsKey(consumer));
			consumerFilters.remove(consumer);
						
			logger.trace("Removing {} from types; contains: {}", consumer, consumerTypes.containsKey(consumer));
			consumerTypes.remove(consumer);
			
			recalculateExistingMappings();
		}
	}

	@Override
	public void unsubscribeConsumer(final Object subscriber, final Class<?> eventType)
	{
		synchronized (consumerTypes)
		{
			logger.trace("Trying to remove consumer of type {} from {}", eventType, subscriber);
						
			final Collection<Consumer<?>> consumers = subscribers.get(subscriber);
			
			Consumer<?> foundConsumer = null;
			
			// Find the consumer based on its type
			for (final Consumer<?> consumer : consumerTypes.keySet())
			{
				if (consumerTypes.get(consumer).equals(eventType) && consumers.contains(consumer))
				{
					foundConsumer = consumer;
					break;
				}
			}
			
			if (foundConsumer != null)
			{
				unsubscribeConsumer(subscriber, foundConsumer);
			}
		}
	}
}
