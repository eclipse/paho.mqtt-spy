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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.connectivity.SimpleMqttConnection;
import pl.baczkowicz.mqttspy.daemon.configuration.MqttSpyDaemonConfigLoader;
import pl.baczkowicz.mqttspy.daemon.configuration.generated.DaemonMqttConnectionDetails;
import pl.baczkowicz.mqttspy.daemon.configuration.generated.MqttSpyDaemonConfiguration;
import pl.baczkowicz.mqttspy.daemon.connectivity.MqttCallbackHandler;
import pl.baczkowicz.mqttspy.daemon.connectivity.SimpleMqttConnectionRunnable;
import pl.baczkowicz.mqttspy.scripts.MqttScriptIO;
import pl.baczkowicz.mqttspy.scripts.MqttScriptManager;
import pl.baczkowicz.spy.common.generated.ReconnectionSettings;
import pl.baczkowicz.spy.configuration.BasePropertyNames;
import pl.baczkowicz.spy.connectivity.ReconnectionManager;
import pl.baczkowicz.spy.daemon.BaseDaemon;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.eventbus.KBus;
import pl.baczkowicz.spy.exceptions.SpyException;
import pl.baczkowicz.spy.exceptions.XMLException;
import pl.baczkowicz.spy.testcases.TestCaseManager;

/**
 * The main class of the daemon.
 */
public class MqttSpyDaemon extends BaseDaemon
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(MqttSpyDaemon.class);
	
	private MqttSpyDaemonConfigLoader loader;

	private ReconnectionManager mqttReconnectionManager;

	protected SimpleMqttConnection mqttConnection;

	private MqttCallbackHandler mqttCallback;

	private MqttScriptIO scriptIO;
	
	/**
	 * This is an internal method - initialises the daemon class.
	 * 
	 * @throws XMLException Thrown if cannot instantiate itself
	 */
	public void initialise() throws XMLException
	{
		loader = new MqttSpyDaemonConfigLoader();
		showInfo();
	}
	
	protected void showInfo()
	{
		logger.info("#######################################################");
		logger.info("### Starting mqtt-spy-daemon v{}", loader.getFullVersionName());
		logger.info("### If you find it useful, see how you can help at {}", loader.getProperty(BasePropertyNames.DOWNLOAD_URL));
		logger.info("### To get release updates follow @mqtt_spy on Twitter ");
		logger.info("#######################################################");
	}
	
	/**
	 * This is an internal method - requires "initialise" to be called first.
	 * 
	 * @param configurationFile Location of the configuration file
	 * @throws SpyException Thrown if cannot initialise
	 */
	public void loadAndRun(final String configurationFile) throws SpyException
	{
		// Load the configuration
		// loader.loadConfiguration(new File(configurationFile));
		loader.loadConfiguration(configurationFile);
		
		loadAndRun(loader.getConfiguration());
	}
	
	/**
	 * This is an internal method - requires "initialise" to be called first.
	 * 
	 * @param configuration Configuration object
	 * @throws SpyException Thrown if cannot initialise
	 */
	protected void loadAndRun(final MqttSpyDaemonConfiguration configuration) throws SpyException
	{			
		// Retrieve connection details
		final DaemonMqttConnectionDetails connectionSettings = configuration.getConnection();

		configureMqtt(connectionSettings);
		runScripts(connectionSettings.getBackgroundScript(), connectionSettings.getTestCases(), connectionSettings.getRunningMode());
	}
	
	protected void configureMqtt(final DaemonMqttConnectionDetails connectionSettings) throws SpyException
	{
		final IKBus eventBus = new KBus();
		
		// Wire up all classes (assuming ID = 0)
		mqttReconnectionManager = new ReconnectionManager();
		mqttConnection = new SimpleMqttConnection(mqttReconnectionManager, "0", connectionSettings);
		scriptManager = new MqttScriptManager(eventBus, null, mqttConnection);
		testCaseManager = new TestCaseManager(scriptManager);
		mqttCallback = new MqttCallbackHandler(mqttConnection, connectionSettings, scriptManager); 
				
		// Set up reconnection
		final ReconnectionSettings reconnectionSettings = mqttConnection.getMqttConnectionDetails().getReconnectionSettings();			
		final Runnable connectionRunnable = new SimpleMqttConnectionRunnable(scriptManager, mqttConnection, connectionSettings);
		
		mqttConnection.setScriptManager(scriptManager);
		mqttConnection.connect(mqttCallback, connectionRunnable);
		scriptIO = new MqttScriptIO(mqttConnection, eventBus, null, null);
		if (reconnectionSettings != null)
		{
			new Thread(mqttReconnectionManager).start();
		}
	}
	
	public boolean canPublish()
	{
		return mqttConnection.canPublish();
	}
	
	/**
	 *  Tries to stop all running threads (apart from scripts) and close the connection.
	 */
	protected void waitAndStop()
	{
		waitForScripts();
		
		stopMqtt();
		
		displayGoodbyeMessage();
	}	
	
	protected void stopMqtt()
	{
		// Stop reconnection manager
		if (mqttReconnectionManager != null)
		{
			mqttReconnectionManager.stop();
		}
						
		// Disconnect
		mqttConnection.disconnect();
		
		// Stop message logger
		mqttCallback.stop();
	}
	
	/**
	 * This exposes additional methods, e.g. publish, subscribe, unsubscribe.
	 *  
	 * @return The Script IO with the extra methods
	 */
	public MqttScriptIO more()
	{
		return scriptIO;
	}
}
