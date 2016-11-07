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
package pl.baczkowicz.mqttspy.ui.controllers;

import javafx.fxml.Initializable;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.ui.scripts.InteractiveMqttScriptManager;
import pl.baczkowicz.spy.ui.controllers.BasePublicationScriptsController;
import pl.baczkowicz.spy.ui.panes.TitledPaneController;
import pl.baczkowicz.spy.ui.scripts.ScriptTypeEnum;
import pl.baczkowicz.spy.ui.scripts.events.ScriptListChangeEvent;

/**
 * Controller for publications scripts pane.
 */
public class MqttPublicationScriptsController extends BasePublicationScriptsController implements Initializable, TitledPaneController
{
	private MqttAsyncConnection connection;
	
	public void init()
	{
		super.init();
		
		scriptManager = connection.getScriptManager();
		publicationScriptsDirectory = InteractiveMqttScriptManager.getScriptDirectoryForConnection(
				connection.getProperties().getConfiguredProperties().getPublicationScripts());		
		
		refreshList();
		scriptTable.setItems(scriptManager.getObservableScriptList());
	}

	@Override
	protected void refreshList()
	{
		// Directory-driven
		scriptManager.addScripts(publicationScriptsDirectory, includeSubdirectories, ScriptTypeEnum.PUBLICATION);
		
		// As defined
		scriptManager.addScripts(connection.getProperties().getConfiguredProperties().getBackgroundScript(), ScriptTypeEnum.BACKGROUND);		
		
		// Subscription-based
		((InteractiveMqttScriptManager) scriptManager).addSubscriptionScripts(connection.getProperties().getConfiguredProperties().getSubscription());
		
		// TODO: move this to script manager?
		eventBus.publish(new ScriptListChangeEvent(connection));
	}

	public void setConnection(final MqttAsyncConnection connection)
	{
		this.connection = connection;
	}
}
