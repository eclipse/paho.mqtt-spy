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

import java.io.File;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.ui.MqttConnectionViewManager;
import pl.baczkowicz.mqttspy.ui.MqttViewManager;
import pl.baczkowicz.mqttspy.ui.controlpanel.MqttConfigControlPanelItem;
import pl.baczkowicz.mqttspy.ui.controlpanel.MqttStatsControlPanelItem;
import pl.baczkowicz.mqttspy.ui.events.ShowNewMqttSubscriptionWindowEvent;
import pl.baczkowicz.spy.configuration.PropertyFileLoader;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.exceptions.SpyUncaughtExceptionHandler;
import pl.baczkowicz.spy.exceptions.XMLException;
import pl.baczkowicz.spy.ui.configuration.BaseConfigurationManager;
import pl.baczkowicz.spy.ui.configuration.IConfigurationManager;
import pl.baczkowicz.spy.ui.configuration.UiProperties;
import pl.baczkowicz.spy.ui.connections.IConnectionFactory;
import pl.baczkowicz.spy.ui.controllers.ControlPanelController;
import pl.baczkowicz.spy.ui.controlpanel.ConnectionsControlPanelItem;
import pl.baczkowicz.spy.ui.events.AddConnectionTabEvent;
import pl.baczkowicz.spy.ui.events.LoadConfigurationFileEvent;
import pl.baczkowicz.spy.ui.events.NewPerspectiveSelectedEvent;
import pl.baczkowicz.spy.ui.events.ShowAboutWindowEvent;
import pl.baczkowicz.spy.ui.events.ShowEditConnectionsWindowEvent;
import pl.baczkowicz.spy.ui.events.ShowExternalWebPageEvent;
import pl.baczkowicz.spy.ui.events.ShowFormattersWindowEvent;
import pl.baczkowicz.spy.ui.events.ShowMessageLogEvent;
import pl.baczkowicz.spy.ui.events.ShowTestCasesWindowEvent;
import pl.baczkowicz.spy.ui.panes.PaneVisibilityStatus;
import pl.baczkowicz.spy.ui.panes.SpyPerspective;
import pl.baczkowicz.spy.ui.stats.ConnectionStatsUpdater;
import pl.baczkowicz.spy.ui.stats.StatisticsManager;
import pl.baczkowicz.spy.ui.utils.DialogFactory;
import pl.baczkowicz.spy.ui.versions.VersionManager;

/**
 * Controller for the main window.
 */
public class MqttSpyMainController
{
	private final static Logger logger = LoggerFactory.getLogger(MqttSpyMainController.class);
	
	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private ControlPanelController controlPanelPaneController;
	
	@FXML
	private AnchorPane mainPane;

	@FXML
	private TabPane connectionTabs;

	@FXML
	private MenuItem openConfigFileMenu;
	
	@FXML
	private MenuItem newConnectionMenu;

	@FXML
	private MenuItem newSubscriptionMenu;
	
	@FXML
	private MenuItem editConnectionsMenu;
	
	@FXML
	private RadioMenuItem defaultPerspective;
	
	@FXML
	private RadioMenuItem basicPerspective;
	
	@FXML 
	private RadioMenuItem detailedPerspective;
	
	@FXML
	private RadioMenuItem spyPerspective;
	
	@FXML
	private RadioMenuItem superSpyPerspective;
	
	@FXML
	private CheckMenuItem resizeMessagePaneMenu;

	private IConfigurationManager configurationManager;

	private Stage stage;
	
	private Scene scene;
	
	private IKBus eventBus;
	
	private StatisticsManager statisticsManager;

	private MqttConnectionViewManager connectionViewManager;
	
	private double lastWidth;
	
	private double lastHeight;

	private VersionManager versionManager;

	private MqttViewManager viewManager;

	private IConnectionFactory connectionFactory;

	public MqttSpyMainController() throws XMLException
	{
		Thread.setDefaultUncaughtExceptionHandler(new SpyUncaughtExceptionHandler());		
	}	
	
	public void init()
	{									
		statisticsManager.loadStats();
		
		// Set up scene
		scene = getParentWindow().getScene();
		
		// Set up window events
		getParentWindow().setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			public void handle(WindowEvent t)
			{
				exit();
			}
		});		
		scene.widthProperty().addListener(new ChangeListener<Number>() 
		{
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) 
		    {
		    	if (!getStage().isMaximized())
		    	{
		    		setLastWidth((double) newSceneWidth);
		    	}
		    }
		});
		scene.heightProperty().addListener(new ChangeListener<Number>() 
		{
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) 
		    {
		    	if (!getStage().isMaximized())
		    	{
		    		setLastHeight((double) newSceneHeight);
		    	}
		    }
		});

		stage.setTitle(configurationManager.getDefaultPropertyFile().getApplicationName());
		
		controlPanelPaneController.setUserAndConfigItem(new MqttConfigControlPanelItem(configurationManager, eventBus));
		controlPanelPaneController.setConnectionsItem(new ConnectionsControlPanelItem(configurationManager, connectionViewManager, connectionFactory, eventBus));
		controlPanelPaneController.setStatsItem(new MqttStatsControlPanelItem(configurationManager, eventBus));
		
		controlPanelPaneController.setConfigurationMananger(configurationManager);
		controlPanelPaneController.setEventBus(eventBus);		
		controlPanelPaneController.setVersionManager(versionManager);
		controlPanelPaneController.init();	
		
		new Thread(new ConnectionStatsUpdater(connectionViewManager)).start();
		
		resizeMessagePaneMenu.selectedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, final Boolean oldValue, final Boolean newValue)
			{
				configurationManager.getUiPropertyFile().setProperty(UiProperties.MESSAGE_PANE_RESIZE_PROPERTY, newValue.toString());
			}
		});
		
		eventBus.subscribe(this, this::onConnectionTabAdded, AddConnectionTabEvent.class);
	}		

	@FXML
	public void createNewConnection()
	{
		logger.trace("Creating new connection...");
		eventBus.publish(new ShowEditConnectionsWindowEvent(getParentWindow(), true, null));
	}

	@FXML
	public void editConnections()
	{
		eventBus.publish(new ShowEditConnectionsWindowEvent(getParentWindow(), false, null));
	}
	
	@FXML
	public void newSubscription()
	{
		final Tab selectedTab = this.getConnectionTabs().getSelectionModel().getSelectedItem();
		final MqttConnectionController controller = (MqttConnectionController) connectionViewManager.getControllerForTab(selectedTab);
		
		if (controller != null)
		{
			eventBus.publish(new ShowNewMqttSubscriptionWindowEvent(
					controller, 
					PaneVisibilityStatus.DETACHED,
					controller.getNewSubscriptionPaneStatus().getVisibility()));
		}
	}

	@FXML
	private void showFormatters()
	{
		eventBus.publish(new ShowFormattersWindowEvent(getParentWindow(), false));
	}
	
	@FXML
	public void showTestCases()
	{
		eventBus.publish(new ShowTestCasesWindowEvent(getParentWindow()));
	}
	
	@FXML
	public void openMessageLog()
	{
		eventBus.publish(new ShowMessageLogEvent(getParentWindow()));
	}
	
	@FXML
	public void exit()
	{
		// This is triggered by the user
		connectionViewManager.disconnectAll();
		
		statisticsManager.saveStats();
		
		configurationManager.saveUiProperties(
				getLastWidth(), getLastHeight(), stage.isMaximized(), 
				viewManager.getPerspective(), resizeMessagePaneMenu.isSelected());
		
		System.exit(0);
	}

	/**
	 * Sets the perspective.
	 * 
	 * @param selectedPerspective the selectedPerspective to set
	 */
	private void updateSelectedPerspective(final SpyPerspective selectedPerspective)
	{
		switch (selectedPerspective)
		{
			case BASIC:
				basicPerspective.setSelected(true);
				break;
			case DETAILED:
				detailedPerspective.setSelected(true);
				break;
			case SPY:
				spyPerspective.setSelected(true);
				break;
			case SUPER_SPY:
				superSpyPerspective.setSelected(true);
				break;
			default:
				defaultPerspective.setSelected(true);
				break;		
		}
		
		eventBus.publish(new NewPerspectiveSelectedEvent(selectedPerspective));
	}

	public TabPane getConnectionTabs()
	{
		return connectionTabs;
	}

	public void onConnectionTabAdded(final AddConnectionTabEvent event)
	{
		addConnectionTab(event.getTab());
	}
	
	private void addConnectionTab(Tab tab)
	{
		connectionTabs.getTabs().add(tab);
	}

	private Window getParentWindow()
	{
		return mainPane.getScene().getWindow();
	}

	@FXML
	public void openConfigurationFile()
	{
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select configuration file to open");
		String extensions = "xml";
		fileChooser.setSelectedExtensionFilter(new ExtensionFilter("XML file", extensions));

		final File selectedFile = fileChooser.showOpenDialog(getParentWindow());

		if (selectedFile != null)
		{
			eventBus.publish(new LoadConfigurationFileEvent(selectedFile));
			// loadConfigurationFileOnRunLater(selectedFile);
		}
	}
	
	@FXML
	private void showPerspective()
	{
		final SpyPerspective selectedPerspective;
		
		if (spyPerspective.isSelected())
		{
			selectedPerspective = SpyPerspective.SPY;
		}
		else if (superSpyPerspective.isSelected())
		{
			selectedPerspective = SpyPerspective.SUPER_SPY;
		}		
		else if (detailedPerspective.isSelected())
		{
			selectedPerspective = SpyPerspective.DETAILED;
		}
		else if (basicPerspective.isSelected())
		{
			selectedPerspective = SpyPerspective.BASIC;
		}
		else
		{
			selectedPerspective = SpyPerspective.DEFAULT;
		}
		
		eventBus.publish(new NewPerspectiveSelectedEvent(selectedPerspective));		
	}
	
	
	@FXML
	private void resizeMessagePane()
	{
		// Connection tabs
		for (final MqttConnectionController controller : connectionViewManager.getConnectionControllers())
		{
			controller.getResizeMessageContentMenu().setSelected(resizeMessagePaneMenu.isSelected());
		}
		// Offline (message log) tabs
		for (final MqttConnectionController controller : connectionViewManager.getOfflineConnectionControllers())
		{
			controller.getResizeMessageContentMenu().setSelected(resizeMessagePaneMenu.isSelected());
		}
	}
	
	@FXML
	private void restoreConfiguration()
	{
		if (DialogFactory.showDefaultConfigurationFileMissingChoice("Restore defaults", mainPane.getScene()))
		{
			eventBus.publish(new LoadConfigurationFileEvent(BaseConfigurationManager.getDefaultConfigurationFileObject()));			
		}
	}
	
	@FXML
	private void showAbout()
	{
		eventBus.publish(new ShowAboutWindowEvent(getParentWindow()));					
	}
	
	@FXML
	private void openGettingInvolved()
	{
		eventBus.publish(new ShowExternalWebPageEvent(configurationManager.getDefaultPropertyFile().getApplicationWikiUrl() + "Getting-involved"));
	}

	@FXML
	private void overviewWiki()
	{
		eventBus.publish(new ShowExternalWebPageEvent(configurationManager.getDefaultPropertyFile().getApplicationWikiUrl() + "Overview"));		
	}
	
	@FXML
	private void changelogWiki()
	{
		eventBus.publish(new ShowExternalWebPageEvent(configurationManager.getDefaultPropertyFile().getApplicationWikiUrl() + "Changelog"));
	}
	
	@FXML
	private void scriptingWiki()
	{
		eventBus.publish(new ShowExternalWebPageEvent(configurationManager.getDefaultPropertyFile().getApplicationWikiUrl() + "Scripting"));
	}
	
	@FXML
	private void messageSearchWiki()
	{
		eventBus.publish(new ShowExternalWebPageEvent(configurationManager.getDefaultPropertyFile().getApplicationWikiUrl() + "MessageSearch"));
	}
	
//	@FXML
//	private void loggingWiki()
//	{
//		eventBus.publish(new ShowExternalWebPageEvent("https://github.com/kamilfb/mqtt-spy/wiki/Logging"));
//	}

	// *********************

	public void setStage(Stage primaryStage)
	{
		this.stage = primaryStage;		
	}
	
	public Stage getStage()
	{
		return this.stage;		
	}

	/**
	 * Sets the configuration manager.
	 * 
	 * @param configurationManager the configurationManager to set
	 */
	public void setConfigurationManager(IConfigurationManager configurationManager)
	{
		this.configurationManager = configurationManager;
	}
	
	/**
	 * Sets the event bus.
	 *  
	 * @param eventBus the eventBus to set
	 */
	public void setEventBus(final IKBus eventBus)
	{
		this.eventBus = eventBus;
	}
	
	public void setConnectionManager(final MqttConnectionViewManager connectionManager)
	{
		this.connectionViewManager = connectionManager;		
	}

	public void setStatisticsManager(final StatisticsManager statisticsManager)
	{
		this.statisticsManager = statisticsManager;		
	}

	/**
	 * Gets last recorded width.
	 * 
	 * @return the lastWidth
	 */
	public double getLastWidth()
	{
		return lastWidth;
	}

	/**
	 * Sets last recorded width.
	 * 
	 * @param lastWidth the lastWidth to set
	 */
	public void setLastWidth(double lastWidth)
	{
		this.lastWidth = lastWidth;
	}

	/**
	 * Gets last recorder height
	 * 
	 * @return the lastHeight
	 */
	public double getLastHeight()
	{
		return lastHeight;
	}

	/**
	 * Sets last recorded height.
	 * 
	 * @param lastHeight the lastHeight to set
	 */
	public void setLastHeight(double lastHeight)
	{
		this.lastHeight = lastHeight;
	}

	private CheckMenuItem getResizeMessagePaneMenu()
	{
		return resizeMessagePaneMenu;
	}

	public void setViewManager(final MqttViewManager viewManager)
	{
		this.viewManager = viewManager;		
	}

	public void setVersionManager(final VersionManager versionManager)
	{
		this.versionManager = versionManager;		
	}

	/**
	 * @return the newConnectionMenu
	 */
	public MenuItem getNewConnectionMenu()
	{
		return newConnectionMenu;
	}

	/**
	 * @param newConnectionMenu the newConnectionMenu to set
	 */
	public void setNewConnectionMenu(MenuItem newConnectionMenu)
	{
		this.newConnectionMenu = newConnectionMenu;
	}

	/**
	 * @return the editConnectionsMenu
	 */
	public MenuItem getEditConnectionsMenu()
	{
		return editConnectionsMenu;
	}

	/**
	 * @param editConnectionsMenu the editConnectionsMenu to set
	 */
	public void setEditConnectionsMenu(MenuItem editConnectionsMenu)
	{
		this.editConnectionsMenu = editConnectionsMenu;
	}

	public MenuItem getNewSubuscriptionMenu()
	{
		return newSubscriptionMenu;
	}
	
	public void updateUiProperties(final PropertyFileLoader uiPropertyFile)
	{
		updateSelectedPerspective(UiProperties.getApplicationPerspective(configurationManager.getUiPropertyFile()));
		getResizeMessagePaneMenu().setSelected(UiProperties.getResizeMessagePane(configurationManager.getUiPropertyFile()));		
	}

	public void setConnectionFactory(IConnectionFactory connectionFactory)
	{
		this.connectionFactory = connectionFactory;		
	}
}
