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
package pl.baczkowicz.mqttspy.ui.scripts;

import java.util.ArrayList;
import java.util.List;

import pl.baczkowicz.mqttspy.configuration.generated.TabbedSubscriptionDetails;
import pl.baczkowicz.mqttspy.connectivity.IMqttConnection;
import pl.baczkowicz.mqttspy.scripts.MqttScriptManager;
import pl.baczkowicz.spy.common.generated.ScriptDetails;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.exceptions.SpyException;
import pl.baczkowicz.spy.scripts.Script;
import pl.baczkowicz.spy.ui.scripts.InteractiveBaseScriptManager;
import pl.baczkowicz.spy.ui.scripts.ScriptTypeEnum;
import pl.baczkowicz.spy.ui.utils.RunLaterExecutor;

/**
 * Script manager that interacts with the UI.
 */
public class InteractiveMqttScriptManager extends InteractiveBaseScriptManager
{
	/** Diagnostic logger. */
	// private final static Logger logger = LoggerFactory.getLogger(InteractiveScriptManager.class);
	
	/** Connection for which the script will be run. */
	private IMqttConnection connection;
	
	public InteractiveMqttScriptManager(final IKBus eventBus, final IMqttConnection connection)
	{
		super(eventBus, new RunLaterExecutor());
		this.connection = connection;
	}
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.mqttspy.ui.scripts.InteractiveBaseScriptManager#addSubscriptionScripts(java.util.List)
	 */
	public void addSubscriptionScripts(final List<TabbedSubscriptionDetails> list)
	{
		final List<ScriptDetails> scripts = new ArrayList<>(); 
		
		for (final TabbedSubscriptionDetails sub : list)
		{
			if (sub.getScriptFile() != null  && !sub.getScriptFile().trim().isEmpty())
			{
				scripts.add(new ScriptDetails(false, false, sub.getScriptFile()));
			}
		}
		
		addScripts(scripts, ScriptTypeEnum.SUBSCRIPTION);
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

	@Override
	public void populateEngineVariables(Script script) throws SpyException
	{
		MqttScriptManager.populateEngineVariables(script, connection, eventBus, executor);
	}
}
