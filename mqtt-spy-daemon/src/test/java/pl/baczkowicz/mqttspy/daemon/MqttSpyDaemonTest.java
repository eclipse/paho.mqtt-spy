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
package pl.baczkowicz.mqttspy.daemon;

import static org.junit.Assert.*;

import java.util.stream.Collectors;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.testcases.TestCaseResult;
import pl.baczkowicz.spy.testcases.TestCaseStatus;
import pl.baczkowicz.spy.utils.ThreadingUtils;

public class MqttSpyDaemonTest
{
	private final static Logger logger = LoggerFactory.getLogger(MqttSpyDaemonTest.class);
	
	@Test
	public void testBasicConfiguration()
	{
		final MqttSpyDaemon daemon = new MqttSpyDaemon();
		
		assertTrue(daemon.start("src/test/resources/test_configurations/basic-configuration.xml"));
	}
	
	@Test
	public void testSslWithTestCasesConfiguration()
	{
		final MqttSpyDaemon daemon = new MqttSpyDaemon();
		
		assertTrue(daemon.start("src/test/resources/test_configurations/test-cases-iot-eclipse.xml"));
	}
	
	@Test
	public void testSslWithMosquittoConfiguration()
	{
		final MqttSpyDaemon daemon = new MqttSpyDaemon();
		
		assertTrue(daemon.start("src/test/resources/test_configurations/mosquitto-org.xml"));
		
		while (!daemon.canPublish())
		{
			logger.debug("Client not connected yet - can't start test cases... [waiting another 1000ms]");
			ThreadingUtils.sleep(1000);
		}
		
		final TestCaseResult result = daemon.runTestCase("src/test/resources/test_cases/test3/tc3.js");
		assertTrue(result.getResult().equals(TestCaseStatus.PASSED));
		assertTrue(result.getStepResults().size() >= 3);
		
		logger.info("Steps = " + result.getStepResults().stream()
				.map(Object::toString)
                .collect(Collectors.joining(", ")));
		
		daemon.stop();
	}
}
