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

package pl.baczkowicz.mqttspy.connectivity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Test;

import pl.baczkowicz.mqttspy.common.generated.MqttConnectionDetails;
import pl.baczkowicz.mqttspy.common.generated.ProtocolVersionEnum;
import pl.baczkowicz.mqttspy.common.generated.SecureSocketSettings;
import pl.baczkowicz.mqttspy.common.generated.UserCredentials;
import pl.baczkowicz.mqttspy.connectivity.reconnection.ReconnectionManager;
import pl.baczkowicz.spy.common.generated.SecureSocketModeEnum;
import pl.baczkowicz.spy.exceptions.SpyException;
import pl.baczkowicz.spy.utils.ConversionUtils;

/**
 * Unit and integration tests for connections. Some of the tests assume
 * 'mosquitto' is available on the path, and is a version 1.4 or later.
 * 
 * Also, some tests assume availability of the network and 'test.mosquitto.org'.
 * 
 */
public class ConnectionTestingWithMosquitto
{
	private final ReconnectionManager reconnectionManager = new ReconnectionManager();
	
	private Process startMosquitto(final String configurationFile) throws IOException
	{
		String execStr = "mosquitto -c " + configurationFile;
        Process proc = Runtime.getRuntime().exec(execStr);
        System.out.println("Proc: " + proc);
        
        return proc;
	}
	
	private void stopProcess(final Process mosquitto)
	{
		System.out.println("Destroying");
        mosquitto.destroy();
        System.out.println("Destroyed");
	}
	
	private MqttCallback createTestCallback(final String connection)
	{
		return new MqttCallback()
		{			
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception
			{
				System.out.println("Got a message for " 
						+ connection 
						+ " on " + topic 
						+ " with content " 
						+ new String(message.getPayload()));			
			}
			
			@Override
			public void deliveryComplete(IMqttDeliveryToken arg0)
			{
				// Not used	
			}
			
			@Override
			public void connectionLost(Throwable arg0)
			{
				// Not used				
			}
		};
	}
	
	private MqttConnectionDetails createMqttConnectionDetails(final String brokerAddress, final UserCredentials uc, final SecureSocketSettings ssl)
	{
		return new MqttConnectionDetails(
				"id",
				"test", 
				ProtocolVersionEnum.MQTT_DEFAULT, 
				Arrays.asList(brokerAddress), 
				false,
				"mqtt-spy-test", 
				uc, 
				null, 
				true, 
				10, 
				10, 
				ssl, 
				null);
	}
	
	@Test
	public void testAnonConnection() throws IOException, SpyException, InterruptedException
	{
		final Process mosquitto = startMosquitto("src/test/resources/mosquitto/mosquitto_allow_anon.conf");
		
		final MqttConnectionDetails connectionDetails = createMqttConnectionDetails("tcp://localhost:10001", null, null);
		
		final SimpleMqttConnection connection = new SimpleMqttConnection(reconnectionManager, "0", connectionDetails);
		connection.createClient(createTestCallback("tcp://localhost:10001"));
		assertTrue(connection.connect());
		System.out.println("Connected...");
		
		assertTrue(connection.subscribe("/mqtt-spy/test/", 0));
		System.out.println("Subscribed...");
		
		connection.publish("/mqtt-spy/test/", "message over TCP", 0, false);
		System.out.println("Published...");
		
		// Waiting for message to be received now...
		Thread.sleep(1000);
		
		connection.disconnect();
		System.out.println("Disconnected");
				
		stopProcess(mosquitto);
		Thread.sleep(2000);
	}
	
	@Test
	public void testRejectingAnonConnection() throws IOException, SpyException, InterruptedException
	{
		final Process mosquitto = startMosquitto("src/test/resources/mosquitto/mosquitto_specified_users.conf");
		
		final MqttConnectionDetails connectionDetails = createMqttConnectionDetails("tcp://localhost:10002", null, null);
		
		final SimpleMqttConnection connection = new SimpleMqttConnection(reconnectionManager, "0", connectionDetails);
		connection.createClient(createTestCallback("tcp://localhost:10002"));
		assertFalse(connection.connect());
		Thread.sleep(1000);
				
		stopProcess(mosquitto);
		Thread.sleep(2000);
	}
	
	@Test
	public void testUserConnection() throws IOException, SpyException, InterruptedException
	{
		final Process mosquitto = startMosquitto("src/test/resources/mosquitto/mosquitto_specified_users.conf");
		
		final MqttConnectionDetails connectionDetails = createMqttConnectionDetails(
				"tcp://localhost:10002", 
				new UserCredentials("nopassword", ""), 
				null);
		
		final SimpleMqttConnection connection = new SimpleMqttConnection(reconnectionManager, "0", connectionDetails);
		connection.createClient(createTestCallback("tcp://localhost:10002"));
		assertTrue(connection.connect());
		System.out.println("Connected...");
		
		assertTrue(connection.subscribe("/mqtt-spy/test/", 0));
		System.out.println("Subscribed...");
		
		connection.publish("/mqtt-spy/test/", "message over TCP", 0, false);
		System.out.println("Published...");
		
		// Waiting for message to be received now...
		Thread.sleep(1000);
		
		connection.disconnect();
		System.out.println("Disconnected");
				
		stopProcess(mosquitto);
		Thread.sleep(2000);
	}
	
	@Test
	public void testUserConnectionWithPassword() throws IOException, SpyException, InterruptedException
	{
		final Process mosquitto = startMosquitto("src/test/resources/mosquitto/mosquitto_specified_users.conf");
		
		final MqttConnectionDetails connectionDetails = createMqttConnectionDetails(
				"tcp://localhost:10002", 
				new UserCredentials("test1", ConversionUtils.stringToBase64("t1")), 
				null);
		
		final SimpleMqttConnection connection = new SimpleMqttConnection(reconnectionManager, "0", connectionDetails);
		connection.createClient(createTestCallback("tcp://localhost:10002"));
		assertTrue(connection.connect());
		System.out.println("Connected...");
		
		assertTrue(connection.subscribe("/mqtt-spy/test/", 0));
		System.out.println("Subscribed...");
		
		connection.publish("/mqtt-spy/test/", "message over TCP", 0, false);
		System.out.println("Published...");
		
		// Waiting for message to be received now...
		Thread.sleep(1000);
		
		connection.disconnect();
		System.out.println("Disconnected");
				
		stopProcess(mosquitto);
		Thread.sleep(2000);
	}
	
	@Test
	public void testUserConnectionWithInvalidPassword() throws IOException, SpyException, InterruptedException
	{
		final Process mosquitto = startMosquitto("src/test/resources/mosquitto/mosquitto_specified_users.conf");
		
		final MqttConnectionDetails connectionDetails = createMqttConnectionDetails("tcp://localhost:10002", new UserCredentials("test1", "blabla"), null);
		
		final SimpleMqttConnection connection = new SimpleMqttConnection(reconnectionManager, "0", connectionDetails);
		connection.createClient(createTestCallback("tcp://localhost:10002"));
		assertFalse(connection.connect());	
		Thread.sleep(1000);
				
		stopProcess(mosquitto);
		Thread.sleep(2000);
	}
	
	@Test
	public void testServerOnlyAuthenticationWithLocalMosquitto() throws SpyException, InterruptedException, IOException
	{			
		final Process mosquitto = startMosquitto("src/test/resources/mosquitto/mosquitto_ssl_server_only.conf");
				
		final MqttConnectionDetails connectionDetails = createMqttConnectionDetails(
				"ssl://localhost:10010", 
				new UserCredentials("nopassword", ""),
				new SecureSocketSettings(SecureSocketModeEnum.SERVER_ONLY, "TLSv1", 
						"src/test/resources/mosquitto/ssl/ca.crt", 
						null, null, null, null, null, null, null, null, null));
		
		final SimpleMqttConnection connection = new SimpleMqttConnection(reconnectionManager, "0", connectionDetails);
		connection.createClient(createTestCallback("ssl://localhost:10010"));
		assertTrue(connection.connect());
		System.out.println("Connected...");
		
		assertTrue(connection.subscribe("/mqtt-spy/test/", 0));
		System.out.println("Subscribed...");
		
		connection.publish("/mqtt-spy/test/", "message over SSL", 0, false);
		System.out.println("Published...");
		
		// Waiting for message to be received now...
		Thread.sleep(11000);
		
		connection.disconnect();
		System.out.println("Disconnected");
		
		stopProcess(mosquitto);
		Thread.sleep(2000);
	}
	
	@Test
	public void testServerAndClientAuthenticationWithLocalMosquitto() throws SpyException, InterruptedException, IOException
	{			
		final Process mosquitto = startMosquitto("src/test/resources/mosquitto/mosquitto_ssl_server_and_client.conf");
				
		final MqttConnectionDetails connectionDetails = createMqttConnectionDetails(
				"ssl://localhost:10011", 
				new UserCredentials("nopassword", ""),
				new SecureSocketSettings(SecureSocketModeEnum.SERVER_AND_CLIENT, "TLSv1.1", 
						"src/test/resources/mosquitto/ssl/ca.crt", 
						"src/test/resources/mosquitto/ssl/bouncy_castle/client.crt", 
						"src/test/resources/mosquitto/ssl/bouncy_castle/client.key", 
						"", null, null, null, null, null, null));
		
		final SimpleMqttConnection connection = new SimpleMqttConnection(reconnectionManager, "0", connectionDetails);
		connection.createClient(createTestCallback("ssl://localhost:10011"));
		assertTrue(connection.connect());
		System.out.println("Connected...");
		
		assertTrue(connection.subscribe("/mqtt-spy/test/", 0));
		System.out.println("Subscribed...");
		
		connection.publish("/mqtt-spy/test/", "message over SSL", 0, false);
		System.out.println("Published...");
		
		// Waiting for message to be received now...
		Thread.sleep(1000);
		
		connection.disconnect();
		System.out.println("Disconnected");
		
		stopProcess(mosquitto);
		Thread.sleep(2000);
	}
	
	@Test
	public void testServerOnlyAuthenticationWithLiveMosquitto() throws SpyException, InterruptedException
	{				
		testServerOnlyAuthentication("ssl://test.mosquitto.org", "/certificates/certificate_authority_files/mosquitto.org.crt");
	}
	
	@Test
	public void testServerOnlyAuthenticationWithLiveEclipseServer() throws SpyException, InterruptedException
	{				
		testServerOnlyAuthentication("ssl://iot.eclipse.org", "/certificates/certificate_authority_files/iot.eclipse.org.crt");
	}
	
	private void testServerOnlyAuthentication(final String server, final String certificateAuthorityFile) 
			throws InterruptedException, SpyException
	{
		final MqttConnectionDetails connectionDetails = createMqttConnectionDetails(server, null, 
				new SecureSocketSettings(SecureSocketModeEnum.SERVER_ONLY, "TLSv1.2", 
						certificateAuthorityFile, 
						null, null, null, null, null, null, null, null, null));
		
		final SimpleMqttConnection connection = new SimpleMqttConnection(reconnectionManager, "0", connectionDetails);
		connection.createClient(createTestCallback(server));
		assertTrue(connection.connect());
		System.out.println("Connected...");
		
		assertTrue(connection.subscribe("/mqtt-spy/test/", 0));
		System.out.println("Subscribed...");
		
		connection.publish("/mqtt-spy/test/", "message over SSL", 0, false);
		System.out.println("Published...");
		
		// Waiting for message to be received now...
		Thread.sleep(1000);
		
		connection.disconnect();
		System.out.println("Disconnected");
	}
}
