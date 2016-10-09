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
package pl.baczkowicz.spy.ui.controllers;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.configuration.BasePropertyNames;
import pl.baczkowicz.spy.connectivity.ConnectionStatus;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.exceptions.XMLException;
import pl.baczkowicz.spy.ui.configuration.IConfigurationManager;
import pl.baczkowicz.spy.ui.controlpanel.IControlPanelItem;
import pl.baczkowicz.spy.ui.controlpanel.ItemStatus;
import pl.baczkowicz.spy.ui.events.ConfigurationLoadedEvent;
import pl.baczkowicz.spy.ui.events.ConnectionStatusChangeEvent;
import pl.baczkowicz.spy.ui.events.ConnectionsChangedEvent;
import pl.baczkowicz.spy.ui.events.ShowExternalWebPageEvent;
import pl.baczkowicz.spy.ui.events.VersionInfoErrorEvent;
import pl.baczkowicz.spy.ui.events.VersionInfoReceivedEvent;
import pl.baczkowicz.spy.ui.generated.versions.SpyVersions;
import pl.baczkowicz.spy.ui.properties.VersionInfoProperties;
import pl.baczkowicz.spy.ui.threading.SimpleRunLaterExecutor;
import pl.baczkowicz.spy.ui.versions.VersionManager;
import pl.baczkowicz.spy.utils.ThreadingUtils;

/**
 * The controller looking after the control panel.
 */
public class ControlPanelController extends AnchorPane implements Initializable
{
	private final static Logger logger = LoggerFactory.getLogger(ControlPanelController.class);

	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private ControlPanelItemController controlPanelItem1Controller;
	
	@FXML
	private ControlPanelItemController controlPanelItem2Controller;
	
	@FXML
	private ControlPanelItemController controlPanelItem3Controller;
	
	@FXML
	private ControlPanelItemController controlPanelItem4Controller;
	
	@FXML
	private Button button1;
	
	@FXML
	private Button button2;
	
	@FXML
	private Button button3;
	
	@FXML
	private Button button4;

	private VersionManager versionManager;

	private IConfigurationManager configurationManager;

	private IKBus eventBus;

	// private IConnectionViewManager connectionManager;
	
	public static Map<ConnectionStatus, String> nextActionTitle = new HashMap<ConnectionStatus, String>();
	
	private IControlPanelItem userAndConfigItem;
	
	private IControlPanelItem connectionsItem;
	
	private IControlPanelItem statsItem;

	// ===============================
	// === Initialisation ============
	// ===============================
	
	public void initialize(URL location, ResourceBundle resources)
	{
		nextActionTitle.put(ConnectionStatus.NOT_CONNECTED, "Connect to");
		nextActionTitle.put(ConnectionStatus.CONNECTING, "Connecting to");
		nextActionTitle.put(ConnectionStatus.CONNECTED, "Disconnect from");
		nextActionTitle.put(ConnectionStatus.DISCONNECTED, "Connect to");
		nextActionTitle.put(ConnectionStatus.DISCONNECTING, "Disconnecting from");
	}
		
	public void init()
	{		
		eventBus.subscribe(this, this::onVersionInfoReceived, VersionInfoReceivedEvent.class, new SimpleRunLaterExecutor());
		eventBus.subscribe(this, this::onVersionInfoError, VersionInfoErrorEvent.class, new SimpleRunLaterExecutor());
		eventBus.subscribe(this, this::onConnectionStatusChanged, ConnectionStatusChangeEvent.class, new SimpleRunLaterExecutor());
		eventBus.subscribe(this, this::onConnectionsChanged, ConnectionsChangedEvent.class);
		eventBus.subscribe(this, this::onConfigurationFileStatusChange, ConfigurationLoadedEvent.class);
		
		// Item 1
		userAndConfigItem.update(controlPanelItem1Controller, button1);
		
		// Item 2
		connectionsItem.update(controlPanelItem2Controller, button2);
		
		// Item 3
		checkForUpdates(controlPanelItem3Controller, button3);	
		
		// Item 4			
		statsItem.update(controlPanelItem4Controller, button4);
	}
	

	// ===============================
	// === FXML ======================
	// ===============================

	// ===============================
	// === Logic =====================
	// ===============================	
	
	public void onConnectionStatusChanged(final ConnectionStatusChangeEvent event)
	{
		refreshConnectionsStatus();
	}
	
	public void onConnectionsChanged(final ConnectionsChangedEvent event)
	{
		refreshConnectionsStatus();
	}
	
	public void refreshConnectionsStatus()
	{
		logger.trace("Refreshing connection status...");
		connectionsItem.update(controlPanelItem2Controller, button2);			
	}

	public void onConfigurationFileStatusChange(final ConfigurationLoadedEvent event)
	{
		userAndConfigItem.update(controlPanelItem1Controller, button1);	
	}
	
	public void checkForUpdates(final ControlPanelItemController controller, final Button button)
	{
		button.setOnAction(new EventHandler<ActionEvent>()
		{			
			@Override
			public void handle(ActionEvent event)
			{				
				eventBus.publish(new ShowExternalWebPageEvent(configurationManager.getDefaultPropertyFile().getProperty(BasePropertyNames.DOWNLOAD_URL)));			
			}
		});
		
		// Set the default state
		controller.setStatus(ItemStatus.INFO);
		controller.setTitle("Connecting to the " + configurationManager.getDefaultPropertyFile().getApplicationName() + " update server...");
		controller.setShowProgress(true);
		controller.setDetails("Please wait while " 
				+ configurationManager.getDefaultPropertyFile().getApplicationName() 
				+ " retrieves information about available updates.");

		// Run the version check in a separate thread, so that it doesn't block JavaFX
		new Thread(new Runnable()
		{			
			@Override
			public void run()
			{
				try
				{
					versionManager.setLoading(true);
					
					// Wait some time for the app to start properly
					ThreadingUtils.sleep(5000);					
					
					final SpyVersions versions = versionManager.loadVersions();
					
					logger.debug("Retrieved version info = " + versions.toString());
					eventBus.publish(new VersionInfoReceivedEvent(versions));
				}
				catch (final XMLException e)
				{
					// If an error occurred					
					eventBus.publish(new VersionInfoErrorEvent(e));			
				}
			}
		}).start();		
			
		controller.refresh();
	}
	
	public void showUpdateInfo(final ControlPanelItemController controller, final Button button)
	{
		controller.setShowProgress(false);
		
		final VersionInfoProperties properties = versionManager.getVersionInfoProperties(configurationManager.getDefaultPropertyFile());
		controller.setStatus(properties.getStatus());
		controller.setTitle(properties.getTitle());
		controller.setDetails(properties.getDetails());
		
		controller.refresh();
	}
	
	// ===============================
	// === Setters and getters =======
	// ===============================
	
	public void setConfigurationMananger(final IConfigurationManager configurationManager)
	{
		this.configurationManager = configurationManager;
	}

	public void onVersionInfoReceived(final VersionInfoReceivedEvent event)
	{
		showUpdateInfo(controlPanelItem3Controller, button3);							
	}

	public void onVersionInfoError(final VersionInfoErrorEvent event)
	{
		controlPanelItem3Controller.setStatus(ItemStatus.ERROR);
		controlPanelItem3Controller.setShowProgress(false);
		controlPanelItem3Controller.setTitle("Error occurred while getting version info. Please perform manual update.");
		logger.error("Cannot retrieve version info", event.getException());
		
		controlPanelItem3Controller.refresh();		
	}

	public void setVersionManager(final VersionManager versionManager)
	{
		this.versionManager = versionManager;		
	}
	
	public void setEventBus(final IKBus eventBus)
	{
		this.eventBus = eventBus;
	}

	/**
	 * @param userAndConfigItem the userAndConfigItem to set
	 */
	public void setUserAndConfigItem(IControlPanelItem userAndConfigItem)
	{
		this.userAndConfigItem = userAndConfigItem;
	}

	/**
	 * @param connectionsItem the connectionsItem to set
	 */
	public void setConnectionsItem(IControlPanelItem connectionsItem)
	{
		this.connectionsItem = connectionsItem;
	}

	/**
	 * @param statsItem the statsItem to set
	 */
	public void setStatsItem(IControlPanelItem statsItem)
	{
		this.statsItem = statsItem;
	}
}
