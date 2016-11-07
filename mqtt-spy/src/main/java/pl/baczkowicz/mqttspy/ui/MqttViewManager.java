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

package pl.baczkowicz.mqttspy.ui;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Screen;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.Main;
import pl.baczkowicz.mqttspy.common.generated.PublicationDetails;
import pl.baczkowicz.mqttspy.configuration.generated.TabbedSubscriptionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.connectivity.MqttRuntimeConnectionProperties;
import pl.baczkowicz.mqttspy.messages.BaseMqttMessage;
import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.scripts.MqttScriptManager;
import pl.baczkowicz.mqttspy.ui.controllers.MqttConnectionController;
import pl.baczkowicz.mqttspy.ui.controllers.MqttSpyMainController;
import pl.baczkowicz.mqttspy.ui.controllers.SubscriptionController;
import pl.baczkowicz.mqttspy.ui.events.ShowNewMqttSubscriptionWindowEvent;
import pl.baczkowicz.mqttspy.ui.messagelog.LogReaderTask;
import pl.baczkowicz.mqttspy.ui.messagelog.TaskWithProgressUpdater;
import pl.baczkowicz.mqttspy.ui.utils.ContextMenuUtils;
import pl.baczkowicz.spy.connectivity.ConnectionStatus;
import pl.baczkowicz.spy.formatting.FormattingManager;
import pl.baczkowicz.spy.ui.BaseViewManager;
import pl.baczkowicz.spy.ui.configuration.BaseConfigurationManager;
import pl.baczkowicz.spy.ui.configuration.UiProperties;
import pl.baczkowicz.spy.ui.controllers.EditConnectionsController;
import pl.baczkowicz.spy.ui.events.AddConnectionTabEvent;
import pl.baczkowicz.spy.ui.events.ConfigurationLoadedEvent;
import pl.baczkowicz.spy.ui.events.ConnectionStatusChangeEvent;
import pl.baczkowicz.spy.ui.events.ConnectionsChangedEvent;
import pl.baczkowicz.spy.ui.events.LoadConfigurationFileEvent;
import pl.baczkowicz.spy.ui.events.NewPerspectiveSelectedEvent;
import pl.baczkowicz.spy.ui.events.ShowMessageLogEvent;
import pl.baczkowicz.spy.ui.panes.PaneVisibilityStatus;
import pl.baczkowicz.spy.ui.panes.TabStatus;
import pl.baczkowicz.spy.ui.panes.TitledPaneStatus;
import pl.baczkowicz.spy.ui.storage.ManagedMessageStoreWithFiltering;
import pl.baczkowicz.spy.ui.threading.SimpleRunLaterExecutor;
import pl.baczkowicz.spy.ui.utils.FxmlUtils;
import pl.baczkowicz.spy.ui.utils.ImageUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class MqttViewManager extends BaseViewManager
{
	private final static Logger logger = LoggerFactory.getLogger(MqttViewManager.class);

	// private MqttConfigurationManager mqttConfigurationManager;

	private MqttConnectionViewManager mqttConnectionViewManager;

	private EditConnectionsController editConnectionsController;

	private MqttSpyMainController mqttSpyMainController;

	public void init()
	{
		super.init();
		
		eventBus.subscribe(this, this::loadConfigurationFile, LoadConfigurationFileEvent.class, new SimpleRunLaterExecutor());
		eventBus.subscribe(this, this::onNewSelectedPerspective, NewPerspectiveSelectedEvent.class);
		eventBus.subscribe(this, this::openMessageLog, ShowMessageLogEvent.class);
		eventBus.subscribe(this, MqttViewManager::showNewSubscriptionWindow, ShowNewMqttSubscriptionWindowEvent.class);
		
		// mqttConfigurationManager = (MqttConfigurationManager) configurationManager;
	}
	
	public Scene createMainWindow(final Stage primaryStage) throws IOException
	{
		final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("MqttSpyMainWindow.fxml");
		
		// Get the associated pane
		AnchorPane pane = (AnchorPane) loader.load();
		
		final Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		
		// Set scene width, height and style
		final double height = Math.min(UiProperties.getApplicationHeight(configurationManager.getUiPropertyFile()), primaryScreenBounds.getHeight());			
		final double width = Math.min(UiProperties.getApplicationWidth(configurationManager.getUiPropertyFile()), primaryScreenBounds.getWidth());
		
		final Scene scene = new Scene(pane, width, height);			
		scene.getStylesheets().add(Main.class.getResource("/ui/css/application.css").toExternalForm());
		
		stylesheets = scene.getStylesheets();
		
		// Get the associated controller
		mqttSpyMainController = (MqttSpyMainController) loader.getController();
		
		mqttSpyMainController.setEventBus(eventBus);
		mqttSpyMainController.setConnectionManager(mqttConnectionViewManager);
		mqttSpyMainController.setStatisticsManager(statisticsManager);
		mqttSpyMainController.setVersionManager(versionManager);
		mqttSpyMainController.setConnectionFactory(connectionFactory);
		mqttSpyMainController.setViewManager(this);
		mqttSpyMainController.setConfigurationManager(configurationManager);
		mqttSpyMainController.updateUiProperties(configurationManager.getUiPropertyFile());

		mqttConnectionViewManager.setParentStage(mqttSpyMainController.getStage());
		
		// Set the stage's properties
		primaryStage.setScene(scene);	
		primaryStage.setMaximized(UiProperties.getApplicationMaximized(configurationManager.getUiPropertyFile()));			
					
		// Initialise resources in the main controller			
		mqttSpyMainController.setStage(primaryStage);
		mqttSpyMainController.setLastHeight(height);
		mqttSpyMainController.setLastWidth(width);
		mqttSpyMainController.init();
		
	    primaryStage.getIcons().add(ImageUtils.createIcon(configurationManager.getDefaultPropertyFile().getApplicationLogo()).getImage());
	    
	    setUpKeyHandlers(scene);
		
		return scene;
	}
	
	public static void showNewSubscriptionWindow(final ShowNewMqttSubscriptionWindowEvent event)
	{
		final MqttConnectionController connectionController = event.getConnectionController();
		
		if (event.getPreviousStatus().equals(PaneVisibilityStatus.ATTACHED))
		{
			if (!connectionController.getNewSubscriptionPaneController().getTitledPane().isExpanded())
			{
				connectionController.getNewSubscriptionPaneController().getTitledPane().setExpanded(true);
			}
			connectionController.getNewSubscriptionPaneController().requestFocus();
		}
		else
		{		
			final TitledPaneStatus paneStatus = connectionController.getNewSubscriptionPaneStatus();
			
			connectionController.setPaneVisiblity(paneStatus, event.getNewStatus());
			
			if (event.getNewStatus().equals(PaneVisibilityStatus.DETACHED))
			{
				connectionController.getNewSubscriptionPaneController().setPreviousStatus(event.getPreviousStatus());
				paneStatus.getParentWhenDetached().setWidth(600);
				connectionController.getNewSubscriptionPaneController().requestFocus();
			}
		}
	}
	
	public void onNewSelectedPerspective(final NewPerspectiveSelectedEvent event)
	{
		selectedPerspective = event.getPerspective();
		
		for (final MqttConnectionController connectionController : mqttConnectionViewManager.getConnectionControllers())
		{
			showPerspective(connectionController);
		}
		
		logger.debug("Selected perspective = " + selectedPerspective.toString());
	}
	
	public void showPerspective(final MqttConnectionController connectionController)
	{
		switch (selectedPerspective)
		{
			case BASIC:
				connectionController.showPanes(PaneVisibilityStatus.ATTACHED, PaneVisibilityStatus.NOT_VISIBLE, PaneVisibilityStatus.ATTACHED, PaneVisibilityStatus.ATTACHED);				
				break;
			case DETAILED:
				connectionController.showPanes(PaneVisibilityStatus.ATTACHED, PaneVisibilityStatus.ATTACHED, PaneVisibilityStatus.NOT_VISIBLE, PaneVisibilityStatus.ATTACHED);
				break;
			case SPY:
				connectionController.showPanes(PaneVisibilityStatus.NOT_VISIBLE, PaneVisibilityStatus.NOT_VISIBLE, PaneVisibilityStatus.NOT_VISIBLE, PaneVisibilityStatus.ATTACHED);		
				break;
			case SUPER_SPY:
				connectionController.showPanes(PaneVisibilityStatus.NOT_VISIBLE, PaneVisibilityStatus.NOT_VISIBLE, PaneVisibilityStatus.NOT_VISIBLE, PaneVisibilityStatus.ATTACHED);
				break;
			default:
				connectionController.showPanes(PaneVisibilityStatus.ATTACHED, PaneVisibilityStatus.ATTACHED, PaneVisibilityStatus.NOT_VISIBLE, PaneVisibilityStatus.ATTACHED);
				break;		
		}
		
		connectionController.setViewVisibility(getDetailedViewStatus(selectedPerspective), getBasicViewStatus(selectedPerspective));
	}
	
	public void openMessageLog(final ShowMessageLogEvent event)
	{
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select message audit log file to open");
		String extensions = "messages";
		fileChooser.setSelectedExtensionFilter(new ExtensionFilter("Message audit log file", extensions));

		final File selectedFile = fileChooser.showOpenDialog(event.getParent());

		if (selectedFile != null)
		{			
			final TaskWithProgressUpdater<List<BaseMqttMessage>> readAndProcess = new LogReaderTask(selectedFile, this, mqttSpyMainController);
			
			pl.baczkowicz.spy.ui.utils.DialogFactory.createWorkerDialog(readAndProcess);
			
			new Thread(readAndProcess).start();			
		}
	}
	
	public void clear()
	{
		mqttConnectionViewManager.disconnectAndCloseAll();
		
		// Only re-initialise if it has been initialised already
		if (editConnectionsController != null)
		{
			initialiseEditConnectionsWindow(mqttSpyMainController.getStage().getScene().getWindow());
		}	
	}		

	public void loadConfigurationFile(final LoadConfigurationFileEvent event)
	{
		logger.info("Loading configuration file from " + event.getFile().getAbsolutePath());
		
		if (configurationManager.loadConfiguration(event.getFile()))
		{
			clear();
			// controlPanelPaneController.refreshConnectionsStatus();
			eventBus.publish(new ConnectionsChangedEvent());
			
			// Process the connection settings		
			mqttConnectionViewManager.autoOpenConnections();
		}
		
		eventBus.publish(new ConfigurationLoadedEvent());	
	}	
	
	/**
	 * Creates a new connection tab.
	 * 
	 * @param name Name of the tab
	 * @param content The content of the tab
	 * @param connectionController The connection controller
	 * 
	 * @return Created tab
	 */
	private Tab createConnectionTab(final String name, final Node content, final MqttConnectionController connectionController)
	{
		final Tab tab = new Tab();
		connectionController.setTab(tab);
		tab.setText(name);
		tab.setContent(content);		

		return tab;
	}
	
	/**
	 * Creates and loads a new connection tab.
	 * 
	 * @param connectionProperties The connection properties from which to create the connection
	 */
	public void loadConnectionTab(final MqttRuntimeConnectionProperties connectionProperties)
	{		
		// Create connection
		final MqttAsyncConnection connection = mqttConnectionViewManager.createConnection(connectionProperties, mqttConnectionViewManager.getUiEventQueue());
		connection.setOpening(true);
		connection.setStatisticsManager(statisticsManager);

		// Load a new tab and connection pane
		final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("ConnectionTab.fxml");
		AnchorPane connectionPane = FxmlUtils.loadAnchorPane(loader);
		
		final MqttConnectionController connectionController = (MqttConnectionController) loader.getController();
		connectionController.setConnection(connection);
		connectionController.setConnectionManager(mqttConnectionViewManager);
		connectionController.setEventBus(eventBus);
		connectionController.setStatisticsManager(statisticsManager);
		connectionController.setTabStatus(new TabStatus());
		connectionController.getTabStatus().setVisibility(PaneVisibilityStatus.NOT_VISIBLE);
		connectionController.getResizeMessageContentMenu().setSelected(
				configurationManager.getUiPropertyFile().getBooleanProperty(UiProperties.MESSAGE_PANE_RESIZE_PROPERTY));
		
		final Tab connectionTab = createConnectionTab(connection.getProperties().getName(), connectionPane, connectionController);
		
		final MqttSubscriptionViewManager subscriptionManager = new MqttSubscriptionViewManager(
				eventBus, configurationManager, this, mqttConnectionViewManager.getUiEventQueue());			
		
		final SubscriptionController subscriptionController = subscriptionManager.createSubscriptionTab(
				true, connection.getStore(), null, connection, connectionController);
		subscriptionController.setConnectionController(connectionController);
		subscriptionController.setFormatters(configurationManager.getFormatters());
		
		// final ConnectionManager connectionManager = this;
		
		Platform.runLater(new Runnable()
		{			
			@Override
			public void run()
			{					
				connectionController.init();
				subscriptionController.init();				

				eventBus.publish(new AddConnectionTabEvent(connectionTab));

				connectionController.getTabStatus().setVisibility(PaneVisibilityStatus.ATTACHED);
				connectionController.getTabStatus().setParent(connectionTab.getTabPane());
				
				connectionTab.setContextMenu(ContextMenuUtils.createConnectionMenu(connection, eventBus, connectionController, mqttConnectionViewManager));
				
				subscriptionController.getTab().setContextMenu(ContextMenuUtils.createAllSubscriptionsTabContextMenu(
						connection, eventBus, subscriptionManager, configurationManager, subscriptionController));
				
				eventBus.subscribe(connectionController, connectionController::onConnectionStatusChanged, ConnectionStatusChangeEvent.class, new SimpleRunLaterExecutor(), connection);
											
				connection.setOpening(false);
				connection.setOpened(true);
				
				// Connect
				if (connection.getProperties().isAutoConnect())
				{
					mqttConnectionViewManager.connectToBroker(connection);
				}
				else
				{
					connection.setConnectionStatus(ConnectionStatus.NOT_CONNECTED);
				}	
				
				// Add "All" tab								
				connectionController.getSubscriptionTabs().getTabs().add(subscriptionController.getTab());
				subscriptionController.getTab().setDisable(true);
				
				mqttConnectionViewManager.getConnectionControllersMapping().put(connection, connectionController);
				mqttConnectionViewManager.getConnectionTabs().put(connection, connectionTab);
				mqttConnectionViewManager.getSubscriptionManagers().put(connectionController, subscriptionManager);
								
				// Populate panes
				populateConnectionPanes(connectionProperties.getConfiguredProperties(), connectionController);	
				
				// Apply perspective
				showPerspective(connectionController);
			}
		});		
	}
	
	public static void populateConnectionPanes(final UserInterfaceMqttConnectionDetails connectionDetails, final MqttConnectionController connectionController)
	{
		for (final PublicationDetails publicationDetails : connectionDetails.getPublication())
		{
			// Add it to the list of pre-defined topics
			connectionController.getNewPublicationPaneController().recordPublicationTopic(publicationDetails.getTopic());
		}
		
		for (final TabbedSubscriptionDetails subscriptionDetails : connectionDetails.getSubscription())
		{
			// Check if we should create a tab for the subscription
			if (subscriptionDetails.isCreateTab())
			{
				connectionController.getNewSubscriptionPaneController().subscribe(subscriptionDetails, connectionDetails.isAutoSubscribe());
			}
			
			// Add it to the list of pre-defined topics
			connectionController.getNewSubscriptionPaneController().recordSubscriptionTopic(subscriptionDetails.getTopic());
		}
	}
	
	/**
	 * Creates and loads a message log tab.
	 * 
	 * @param mainController The main controller
	 * @param parent The parent UI node
	 * @param name Name of the tab
	 * @param list List of messages to display
	 */
	public void loadMessageLogTab(final MqttSpyMainController mainController, final String name, final List<BaseMqttMessage> list)
	{		
		// Load a new tab and connection pane
		final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("ConnectionTab.fxml");
		AnchorPane connectionPane = FxmlUtils.loadAnchorPane(loader);
		
		final MqttConnectionController connectionController = (MqttConnectionController) loader.getController();
		
		connectionController.setConnectionManager(mqttConnectionViewManager);
		connectionController.setEventBus(eventBus);
		connectionController.setStatisticsManager(statisticsManager);
		connectionController.setReplayMode(true);
		connectionController.setTabStatus(new TabStatus());
		connectionController.getTabStatus().setVisibility(PaneVisibilityStatus.NOT_VISIBLE);
		connectionController.getResizeMessageContentMenu().setSelected(
				configurationManager.getUiPropertyFile().getBooleanProperty(UiProperties.MESSAGE_PANE_RESIZE_PROPERTY));
		
		final Tab replayTab = createConnectionTab(name, connectionPane, connectionController);
		final MqttSubscriptionViewManager subscriptionManager = new MqttSubscriptionViewManager(
				eventBus, configurationManager, this, mqttConnectionViewManager.getUiEventQueue());			
		
        final ManagedMessageStoreWithFiltering<FormattedMqttMessage> store = new ManagedMessageStoreWithFiltering<FormattedMqttMessage>(
        		name, 0, list.size(), list.size(), mqttConnectionViewManager.getUiEventQueue(), //eventManager, 
        		new FormattingManager(new MqttScriptManager(null, null, null)), UiProperties.getSummaryMaxPayloadLength(configurationManager.getUiPropertyFile()));               
        
		final SubscriptionController subscriptionController = subscriptionManager.createSubscriptionTab(
				true, store, null, null, connectionController);
		subscriptionController.setConnectionController(connectionController);
		subscriptionController.setFormatters(configurationManager.getFormatters());
		subscriptionController.setReplayMode(true);
		
		Platform.runLater(new Runnable()
		{			
			@Override
			public void run()
			{					
				connectionController.init();
				subscriptionController.init();				
								
				eventBus.publish(new AddConnectionTabEvent(replayTab));
				
				replayTab.setContextMenu(ContextMenuUtils.createMessageLogMenu(replayTab, connectionController, mqttConnectionViewManager));
								
				// Add "All" subscription tab
				connectionController.getSubscriptionTabs().getTabs().clear();
				connectionController.getSubscriptionTabs().getTabs().add(subscriptionController.getTab());
				connectionController.getTabStatus().setVisibility(PaneVisibilityStatus.ATTACHED);
				connectionController.getTabStatus().setParent(replayTab.getTabPane());
				// TODO: pane status
				
				mqttConnectionViewManager.getOfflineConnectionControllers().add(connectionController);
				mqttConnectionViewManager.getSubscriptionManagers().put(connectionController, subscriptionManager);
				// Apply perspective
				connectionController.showReplayMode();				
				
				// Process the messages
		        for (final BaseMqttMessage mqttMessage : list)
		        {		        	
		        	store.messageReceived(new FormattedMqttMessage(mqttMessage, null));
		        }
		        
		        replayTab.getTabPane().getSelectionModel().select(replayTab);
			}
		});		
	}
	
	public void loadDefaultConfigurationFile()
	{		
		final File defaultConfigurationFile = BaseConfigurationManager.getDefaultConfigurationFileObject();
		
		logger.info("Default configuration file present (" + defaultConfigurationFile.getAbsolutePath() + ") = " + defaultConfigurationFile.exists());
		
		if (defaultConfigurationFile.exists())
		{
			eventBus.publish(new LoadConfigurationFileEvent(defaultConfigurationFile));
		}
		else
		{
			configurationManager.initialiseConfiguration();
		}
	}
	
	// ************
	
	public void setConnectionManager(final MqttConnectionViewManager connectionManager)
	{
		this.mqttConnectionViewManager = connectionManager;		
	}

	protected void setUpKeyHandlers(final Scene scene)
	{
		// Set up key shortcuts
		final KeyCombination newConnection = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
		final KeyCombination editConnections = new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN);				
		
		scene.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler()
		{
			@Override
			public void handle(Event event)
			{
				if (newConnection.match((KeyEvent) event))
				{
					mqttSpyMainController.createNewConnection();
				}				
				else if (editConnections.match((KeyEvent) event))
				{
					mqttSpyMainController.editConnections();
				}
				else if (newSubscription.match((KeyEvent) event))
				{
					final Tab selectedTab = mqttSpyMainController.getConnectionTabs().getSelectionModel().getSelectedItem();
					final MqttConnectionController controller = (MqttConnectionController) mqttConnectionViewManager.getControllerForTab(selectedTab);
					
					if (controller != null)
					{
						eventBus.publish(new ShowNewMqttSubscriptionWindowEvent(
								controller, 
								PaneVisibilityStatus.DETACHED,
								controller.getNewSubscriptionPaneStatus().getVisibility()));
					}
				}
				else if (newPublication.match((KeyEvent) event))
				{
					final Tab selectedTab = mqttSpyMainController.getConnectionTabs().getSelectionModel().getSelectedItem();
					final MqttConnectionController controller = (MqttConnectionController) mqttConnectionViewManager.getControllerForTab(selectedTab);
					
					if (controller != null)
					{
						controller.getNewPublicationPaneController().publish();
					}
				}
			}
		});			

		mqttSpyMainController.getNewConnectionMenu().setAccelerator(newConnection);
		mqttSpyMainController.getEditConnectionsMenu().setAccelerator(editConnections);
		mqttSpyMainController.getNewSubuscriptionMenu().setAccelerator(newSubscription);		
	}
}
