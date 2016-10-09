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
package pl.baczkowicz.mqttspy.scripts;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.audit.MqttAuditReplay;
import pl.baczkowicz.mqttspy.connectivity.IMqttConnection;
import pl.baczkowicz.mqttspy.logger.IMqttMessageLogIO;
import pl.baczkowicz.mqttspy.logger.MqttMessageLogIO;
import pl.baczkowicz.spy.audit.IAuditReplayIO;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.exceptions.SpyException;
import pl.baczkowicz.spy.scripts.BaseScriptManager;
import pl.baczkowicz.spy.scripts.Script;
import pl.baczkowicz.spy.scripts.ScriptRunner;

/**
 * This class manages script creation and execution.
 */
public class MqttScriptManager extends BaseScriptManager
{
	/** Connection for which the script will be run. */
	private IMqttConnection connection;
	
	/**
	 * Creates the script manager.
	 * 
	 * @param eventBus The event bus to be used
	 * @param executor The executor to be used
	 * @param connection The connection for which to run the scripts
	 */
	public MqttScriptManager(final IKBus eventBus, final Executor executor, final IMqttConnection connection)
	{
		super(eventBus, executor);
		this.setConnection(connection);
	}
	
	public void populateEngineVariables(final Script script) throws SpyException
	{
		final MqttScriptIO scriptIO = new MqttScriptIO(connection, eventBus, script, executor); 
		//script.setScriptIO(scriptIO);
		
		final Map<String, Object> scriptVariables = new HashMap<String, Object>();
		
		// This should be considered deprecated
		scriptVariables.put("mqttspy", scriptIO);
		// This should be used for general script-related actions
		scriptVariables.put("spy", scriptIO);
		// Going forward, this should only have mqtt-specific elements, e.g. pub/sub
		scriptVariables.put("mqtt", scriptIO);
		
		scriptVariables.put("logger", LoggerFactory.getLogger(ScriptRunner.class));
		
		final IMqttMessageLogIO mqttMessageLog = new MqttMessageLogIO();
		scriptVariables.put("messageLog", mqttMessageLog);
		
		final IAuditReplayIO mqttAuditReplay = new MqttAuditReplay();
		scriptVariables.put("auditReplay", mqttAuditReplay);
		
		// Add it to the script IO so that it gets stopped when requested
		script.addTask(mqttMessageLog);			
		script.addTask(mqttAuditReplay);		
		
		putJavaVariablesIntoEngine(script.getScriptEngine(), scriptVariables);
	}

	/**
	 * @return the connection
	 */
	public IMqttConnection getConnection()
	{
		return connection;
	}

	/**
	 * @param connection the connection to set
	 */
	public void setConnection(IMqttConnection connection)
	{
		this.connection = connection;
	}
}
