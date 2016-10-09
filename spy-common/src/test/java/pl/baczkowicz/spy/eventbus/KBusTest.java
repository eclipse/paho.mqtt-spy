package pl.baczkowicz.spy.eventbus;

import java.util.function.Consumer;

import junit.framework.TestCase;

import org.junit.Test;

import pl.baczkowicz.spy.eventbus.sample.SampleCountChangeEvent;
import pl.baczkowicz.spy.eventbus.sample.SampleInfoChangeEvent;
import pl.baczkowicz.spy.eventbus.sample.SampleSubscriber;

public class KBusTest extends TestCase
{
	@Test
	public void testSpyBus()
	{
		final IKBus eventBus = new KBus();
		
		// Subscription and removal of subscriptions or consumers could be also done in the subscriber class - see commented out code
		final SampleSubscriber subscriber = new SampleSubscriber();
		eventBus.subscribe(subscriber, (Consumer<SampleCountChangeEvent>) subscriber::onCountChange, SampleCountChangeEvent.class);
		eventBus.subscribe(subscriber, (Consumer<Object>) subscriber::onAnyEvent, Object.class);
		
		// Expecting that to be handled twice
		eventBus.publish(new SampleCountChangeEvent("hello", 1));
		
		// Expecting that to be handled twice		
		eventBus.publish(new SampleCountChangeEvent("hello", 5));
		
		// Only one subscriber configured
		eventBus.publish(new SampleInfoChangeEvent("hello", 8));
		
		assertEquals(5, subscriber.getMessageCount());
		
		eventBus.unsubscribeConsumer(subscriber, SampleCountChangeEvent.class);

		// Only one subscriber remaining	
		eventBus.publish(new SampleCountChangeEvent("hello", 21));

		assertEquals(6, subscriber.getMessageCount());
		
		// Only one subscriber configured
		eventBus.publish(new SampleInfoChangeEvent("hello", 25));
		
		assertEquals(7, subscriber.getMessageCount());
		
		// After this, no events should be handled
		eventBus.unsubscribe(subscriber);
		
		eventBus.publish(new SampleCountChangeEvent("hello", 31));
		eventBus.publish(new SampleInfoChangeEvent("hello", 32));
		
		assertEquals(7, subscriber.getMessageCount());
	}
	
	@Test
	public void testFilter()
	{
		final IKBus eventBus = new KBus();
		
		// Subscription and removal of subscriptions or consumers could be also done in the subscriber class - see commented out code
		final SampleSubscriber subscriber = new SampleSubscriber();
		
		// Subscription with filter value - no need for cast because onAnyEvents excepts Object type
		eventBus.subscribeWithFilterOnly(subscriber, subscriber::onAnyEvent, FilterableEvent.class, "keepMe");
		
		// Only one subscriber configured - has the right filter content
		final SampleInfoChangeEvent filteredEvent = new SampleInfoChangeEvent("hello", 41);
		filteredEvent.setFilter("keepMe");
		eventBus.publish(filteredEvent);
		
		assertEquals(1, subscriber.getMessageCount());
		
		// Only one subscriber configured - but should be filtered out, no "keepMe" in there...
		eventBus.publish(new SampleInfoChangeEvent("hello", 42));		
		eventBus.publish(new Integer(42));
		eventBus.publish("hello");
		
		assertEquals(1, subscriber.getMessageCount());
	}
}
