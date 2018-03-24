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
package pl.baczkowicz.mqttspy.connectivity;

import javafx.scene.paint.Color;
import junit.framework.TestCase;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import pl.baczkowicz.mqttspy.configuration.ConfiguredMqttConnectionDetails;
import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.ui.events.SubscriptionStatusChangeEvent;
import pl.baczkowicz.mqttspy.ui.stats.MqttStatsFileIO;
import pl.baczkowicz.spy.common.generated.FormatterDetails;
import pl.baczkowicz.spy.connectivity.ConnectionStatus;
import pl.baczkowicz.spy.connectivity.ReconnectionManager;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.exceptions.ConfigurationException;
import pl.baczkowicz.spy.exceptions.XMLException;
import pl.baczkowicz.spy.formatting.FormattingManager;
import pl.baczkowicz.spy.ui.events.ConnectionStatusChangeEvent;
import pl.baczkowicz.spy.ui.events.queuable.EventQueueManager;
import pl.baczkowicz.spy.ui.stats.StatisticsManager;

public class MqttConnectionTest extends TestCase
{
	private static final String name = "testClient";
	
	private static final String serverURI = "localhost";
	
	private static final String clientId = "testClientId";
	
	private static final String subscription_TOPIC = "/#";
	
	private static final String message_TOPIC = "/topic";
	
	private MqttAsyncClient mockClient;
	
	private Mockery context = new Mockery()
	{
		{
			setImposteriser(ClassImposteriser.INSTANCE);
		}
	};
	
	// private EventManager mockEventManager;
	
	private IKBus mockEventBus;
	
	private StatisticsManager statisticsManager;
	
	private ReconnectionManager mockedReconnectionManager;
	
	private FormattingManager mockedFormattingManager;

	@Before
	public void setUp() throws XMLException
	{
		mockClient = context.mock(MqttAsyncClient.class);		
		// mockEventManager = context.mock(EventManager.class);
		mockEventBus = context.mock(IKBus.class);
		mockedReconnectionManager = context.mock(ReconnectionManager.class);
		mockedFormattingManager = context.mock(FormattingManager.class);
		statisticsManager = new StatisticsManager(new MqttStatsFileIO());
		statisticsManager.loadStats();
	}

	@Test
	public void testUnsubscribeAndRemove() throws MqttException, ConfigurationException
	{
		// Set up connection
		final ConfiguredMqttConnectionDetails configuredConnectionDetails = new ConfiguredMqttConnectionDetails();
		configuredConnectionDetails.setName(name);
		configuredConnectionDetails.getServerURI().add(serverURI);
		configuredConnectionDetails.setClientID(clientId);
		configuredConnectionDetails.setCleanSession(false);
		configuredConnectionDetails.setConnectionTimeout(5);
		configuredConnectionDetails.setKeepAliveInterval(5);
		configuredConnectionDetails.setMinMessagesStoredPerTopic(10);
		configuredConnectionDetails.setMaxMessagesStored(500);
		
		final MqttRuntimeConnectionProperties connectionProperties = new MqttRuntimeConnectionProperties(configuredConnectionDetails);
		context.checking(new Expectations()
		{
			{
				exactly(0).of(mockEventBus).publish(with(any(ConnectionStatusChangeEvent.class)));
			}
		});
		
		final MqttAsyncConnection connection = new MqttAsyncConnection(
				mockedReconnectionManager, connectionProperties, ConnectionStatus.CONNECTING, 
				mockEventBus, null, mockedFormattingManager, new EventQueueManager(), 100);
		connection.setStatisticsManager(statisticsManager);
		context.assertIsSatisfied();
		
		connection.setClient(mockClient);

		// This should add a subscription
		final MqttSubscription subscription = new MqttSubscription(subscription_TOPIC, 0, Color.WHITE, 10, 100, 
				new EventQueueManager(), mockEventBus, mockedFormattingManager, 100);

		context.checking(new Expectations()
		{
			{
				oneOf(mockClient).subscribe(subscription_TOPIC, 0);
				oneOf(mockClient).unsubscribe(subscription_TOPIC);
				
				allowing(mockClient).isConnected();
				will(returnValue(true));
				
				// Note: not checking if passing the subscription object as param
				allowing(mockEventBus).publish(with(any(SubscriptionStatusChangeEvent.class)));
				
				allowing(mockedFormattingManager).formatMessage(with(any(FormattedMqttMessage.class)), with(any(FormatterDetails.class)));
			}
		});

		//subscription.addObserver(mockObserver);
		assertTrue(connection.subscribe(subscription));

		// This should handle the message
		FormattedMqttMessage message = new FormattedMqttMessage(1, message_TOPIC, new MqttMessage("test".getBytes()), connection);
		connection.messageReceived(message);

		// This should remove the subscription
		connection.unsubscribeAndRemove(subscription);

		// This should be ignored - no matching subscriptions
		connection.messageReceived(message);

		context.assertIsSatisfied();
	}

	@Test
	public void testUnsubscribe() throws MqttException, ConfigurationException
	{
		// Set up connection
		final ConfiguredMqttConnectionDetails configuredConnectionDetails = new ConfiguredMqttConnectionDetails();
		configuredConnectionDetails.setName(name);
		configuredConnectionDetails.getServerURI().add(serverURI);
		configuredConnectionDetails.setClientID(clientId);
		configuredConnectionDetails.setCleanSession(false);
		configuredConnectionDetails.setConnectionTimeout(5);
		configuredConnectionDetails.setKeepAliveInterval(5);
		configuredConnectionDetails.setMinMessagesStoredPerTopic(10);
		configuredConnectionDetails.setMaxMessagesStored(200);
		
		final MqttRuntimeConnectionProperties connectionProperties = new MqttRuntimeConnectionProperties(configuredConnectionDetails);
		
		context.checking(new Expectations()
		{
			{
				exactly(0).of(mockEventBus).publish(with(any(ConnectionStatusChangeEvent.class)));
			}
		});
		
		final MqttAsyncConnection connection = new MqttAsyncConnection(
				mockedReconnectionManager, connectionProperties, ConnectionStatus.CONNECTING, 
				mockEventBus, null, mockedFormattingManager, new EventQueueManager(), 100);
		connection.setStatisticsManager(statisticsManager);
		context.assertIsSatisfied();
		
		connection.setClient(mockClient);

		// This should add a subscription
		final MqttSubscription subscription = new MqttSubscription(subscription_TOPIC, 0, Color.WHITE, 10, 100, 
				new EventQueueManager(), mockEventBus, mockedFormattingManager, 100);
		
		// This should handle the message
		FormattedMqttMessage message = new FormattedMqttMessage(1, message_TOPIC, new MqttMessage("test".getBytes()), connection);
				
		context.checking(new Expectations()
		{
			{
				oneOf(mockClient).subscribe(subscription_TOPIC, 0);
				oneOf(mockClient).unsubscribe(subscription_TOPIC);
				
				allowing(mockClient).isConnected();
				will(returnValue(true));
				
				// Note: not checking if passing the subscription object as param
				allowing(mockEventBus).publish(with(any(SubscriptionStatusChangeEvent.class)));
				
				allowing(mockedFormattingManager).formatMessage(with(any(FormattedMqttMessage.class)), with(any(FormatterDetails.class)));
			}
		});
	
		assertTrue(connection.subscribe(subscription));

		connection.messageReceived(message);

		// This should remove the subscription
		connection.unsubscribe(subscription, true);

		// This should be ignored - subscription not active
		connection.messageReceived(message);
		context.assertIsSatisfied();
	}
}
