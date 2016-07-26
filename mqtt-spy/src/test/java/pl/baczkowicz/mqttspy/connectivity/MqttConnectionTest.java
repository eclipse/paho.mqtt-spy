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

import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.configuration.ConfiguredConnectionDetails;
import pl.baczkowicz.mqttspy.connectivity.reconnection.ReconnectionManager;
import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.stats.StatisticsManager;
import pl.baczkowicz.mqttspy.ui.events.EventManager;
import pl.baczkowicz.spy.common.generated.FormatterDetails;
import pl.baczkowicz.spy.exceptions.ConfigurationException;
import pl.baczkowicz.spy.exceptions.XMLException;
import pl.baczkowicz.spy.formatting.FormattingManager;
import pl.baczkowicz.spy.ui.events.queuable.EventQueueManager;

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
	
	private EventManager mockEventManager;
	
	private StatisticsManager statisticsManager;
	
	private ReconnectionManager mockedReconnectionManager;
	
	private FormattingManager mockedFormattingManager;

	@Before
	public void setUp() throws XMLException
	{
		mockClient = context.mock(MqttAsyncClient.class);		
		mockEventManager = context.mock(EventManager.class);
		mockedReconnectionManager = context.mock(ReconnectionManager.class);
		mockedFormattingManager = context.mock(FormattingManager.class);
		statisticsManager = new StatisticsManager();
		statisticsManager.loadStats();
	}

	@Test
	public void testUnsubscribeAndRemove() throws MqttException, ConfigurationException
	{
		// Set up connection
		final ConfiguredConnectionDetails configuredConnectionDetails = new ConfiguredConnectionDetails();
		configuredConnectionDetails.setName(name);
		configuredConnectionDetails.getServerURI().add(serverURI);
		configuredConnectionDetails.setClientID(clientId);
		configuredConnectionDetails.setCleanSession(false);
		configuredConnectionDetails.setConnectionTimeout(5);
		configuredConnectionDetails.setKeepAliveInterval(5);
		configuredConnectionDetails.setMinMessagesStoredPerTopic(10);
		configuredConnectionDetails.setMaxMessagesStored(500);
		
		final RuntimeConnectionProperties connectionProperties = new RuntimeConnectionProperties(configuredConnectionDetails);
		context.checking(new Expectations()
		{
			{
				oneOf(mockEventManager).notifyConnectionStatusChanged(with(any(MqttAsyncConnection.class)));
			}
		});
		
		final MqttAsyncConnection connection = new MqttAsyncConnection(
				mockedReconnectionManager, connectionProperties, MqttConnectionStatus.CONNECTING, 
				mockEventManager, null, mockedFormattingManager, new EventQueueManager(), 100);
		connection.setStatisticsManager(statisticsManager);
		context.assertIsSatisfied();
		
		connection.setClient(mockClient);

		// This should add a subscription
		final MqttSubscription subscription = new MqttSubscription(subscription_TOPIC, 0, Color.WHITE, 10, 100, 
				new EventQueueManager(), mockEventManager, mockedFormattingManager, 100);

		context.checking(new Expectations()
		{
			{
				oneOf(mockClient).subscribe(subscription_TOPIC, 0);
				oneOf(mockClient).unsubscribe(subscription_TOPIC);
				
				allowing(mockClient).isConnected();
				will(returnValue(true));
				
				allowing(mockEventManager).notifySubscriptionStatusChanged(subscription);
				
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
		final ConfiguredConnectionDetails configuredConnectionDetails = new ConfiguredConnectionDetails();
		configuredConnectionDetails.setName(name);
		configuredConnectionDetails.getServerURI().add(serverURI);
		configuredConnectionDetails.setClientID(clientId);
		configuredConnectionDetails.setCleanSession(false);
		configuredConnectionDetails.setConnectionTimeout(5);
		configuredConnectionDetails.setKeepAliveInterval(5);
		configuredConnectionDetails.setMinMessagesStoredPerTopic(10);
		configuredConnectionDetails.setMaxMessagesStored(200);
		
		final RuntimeConnectionProperties connectionProperties = new RuntimeConnectionProperties(configuredConnectionDetails);
		
		context.checking(new Expectations()
		{
			{
				oneOf(mockEventManager).notifyConnectionStatusChanged(with(any(MqttAsyncConnection.class)));
			}
		});
		
		final MqttAsyncConnection connection = new MqttAsyncConnection(
				mockedReconnectionManager, connectionProperties, MqttConnectionStatus.CONNECTING, 
				mockEventManager, null, mockedFormattingManager, new EventQueueManager(), 100);
		connection.setStatisticsManager(statisticsManager);
		context.assertIsSatisfied();
		
		connection.setClient(mockClient);

		// This should add a subscription
		final MqttSubscription subscription = new MqttSubscription(subscription_TOPIC, 0, Color.WHITE, 10, 100, 
				new EventQueueManager(), mockEventManager, mockedFormattingManager, 100);
		
		// This should handle the message
		FormattedMqttMessage message = new FormattedMqttMessage(1, message_TOPIC, new MqttMessage("test".getBytes()), connection);
				
		context.checking(new Expectations()
		{
			{
				oneOf(mockClient).subscribe(subscription_TOPIC, 0);
				oneOf(mockClient).unsubscribe(subscription_TOPIC);
				
				allowing(mockClient).isConnected();
				will(returnValue(true));
				
				allowing(mockEventManager).notifySubscriptionStatusChanged(subscription);
				
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
