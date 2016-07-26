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
package pl.baczkowicz.mqttspy.ui;

import java.io.File;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.PublicationDetails;
import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.configuration.ConfiguredConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.TabbedSubscriptionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.messages.BaseMqttMessage;
import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.stats.ConnectionStatsUpdater;
import pl.baczkowicz.mqttspy.stats.StatisticsManager;
import pl.baczkowicz.mqttspy.ui.connections.ConnectionManager;
import pl.baczkowicz.mqttspy.ui.events.EventManager;
import pl.baczkowicz.mqttspy.ui.messagelog.LogReaderTask;
import pl.baczkowicz.mqttspy.ui.messagelog.TaskWithProgressUpdater;
import pl.baczkowicz.mqttspy.ui.utils.DialogUtils;
import pl.baczkowicz.mqttspy.versions.VersionManager;
import pl.baczkowicz.spy.exceptions.ConfigurationException;
import pl.baczkowicz.spy.exceptions.SpyUncaughtExceptionHandler;
import pl.baczkowicz.spy.exceptions.XMLException;
import pl.baczkowicz.spy.ui.panes.SpyPerspective;
import pl.baczkowicz.spy.ui.panes.PaneVisibilityStatus;
import pl.baczkowicz.spy.ui.utils.FxmlUtils;

/**
 * Controller for the main window.
 */
public class MainController
{
	private final static Logger logger = LoggerFactory.getLogger(MainController.class);
	
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
	private RadioMenuItem defaultPerspective;
	
	@FXML
	private RadioMenuItem detailedPerspective;
	
	@FXML
	private RadioMenuItem spyPerspective;
	
	@FXML
	private RadioMenuItem superSpyPerspective;
	
	@FXML
	private CheckMenuItem resizeMessagePaneMenu;

	private EditConnectionsController editConnectionsController;
	
	private Stage editConnectionsStage; 	

	private Application application;

	private ConfigurationManager configurationManager;

	private Stage stage;
	
	private Scene scene;

	private EventManager<FormattedMqttMessage> eventManager;
	
	private StatisticsManager statisticsManager;

	private ConnectionManager connectionManager;

	private Stage formattersStage;
	
	private Stage testCasesStage;
	
	private Stage aboutStage;

	private SpyPerspective selectedPerspective = SpyPerspective.DEFAULT;
	
	private double lastWidth;
	
	private double lastHeight;

	private VersionManager versionManager;

	private AboutController aboutController;
	
	public MainController() throws XMLException
	{
		Thread.setDefaultUncaughtExceptionHandler(new SpyUncaughtExceptionHandler());
		 
		this.statisticsManager = new StatisticsManager();		
	}	
	
	public void init()
	{		
		this.connectionManager = new ConnectionManager(eventManager, statisticsManager, configurationManager);	
				
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

		// Clear any test tabs
		stage.setTitle("mqtt-spy");
		
		try
		{
			this.versionManager = new VersionManager(configurationManager.getDefaultPropertyFile());
		}
		catch (XMLException e)
		{
			e.printStackTrace();
		}
		
		controlPanelPaneController.setMainController(this);
		controlPanelPaneController.setConfigurationMananger(configurationManager);
		controlPanelPaneController.setApplication(application);
		controlPanelPaneController.setEventManager(eventManager);
		controlPanelPaneController.setConnectionManager(connectionManager);
		controlPanelPaneController.setVersionManager(versionManager);
		controlPanelPaneController.init();	
		
		new Thread(new ConnectionStatsUpdater(connectionManager)).start();
	}		

	@FXML
	public void createNewConnection()
	{
		logger.trace("Creating new connection...");

		showEditConnectionsWindow(true);
	}

	@FXML
	public void editConnections()
	{
		showEditConnectionsWindow(false);
	}

	@FXML
	private void showFormatters()
	{
		if (formattersStage == null)
		{
			initialiseFormattersWindow();
		}
		
		formattersStage.show();
	}
	
	public void showFormatters(final boolean showAndWait, final Stage stage)
	{
		initialiseFormattersWindow();		
		
		if (showAndWait)
		{
			formattersStage.initOwner(stage.getScene().getWindow());
			formattersStage.showAndWait();
			
			formattersStage = null;
		}
		else
		{
			showFormatters();
		}
	}
	
	@FXML
	public void showTestCases()
	{
		if (testCasesStage == null)
		{
			initialiseTestCasesWindow();
		}
		
		testCasesStage.show();
	}
	
	@FXML
	public void openMessageLog()
	{
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select message log file to open");
		String extensions = "messages";
		fileChooser.setSelectedExtensionFilter(new ExtensionFilter("Message log file", extensions));

		final File selectedFile = fileChooser.showOpenDialog(getParentWindow());

		if (selectedFile != null)
		{			
			final TaskWithProgressUpdater<List<BaseMqttMessage>> readAndProcess = new LogReaderTask(selectedFile, connectionManager, this);
			
			pl.baczkowicz.spy.ui.utils.DialogFactory.createWorkerDialog(readAndProcess);
			
			new Thread(readAndProcess).start();			
		}
	}
	
	private void initialiseEditConnectionsWindow()
	{
		// This is a dirty way to reload connection settings :) possibly could be removed if all connections are closed before loading a new config file
		if (editConnectionsController != null)
		{
			eventManager.deregisterConnectionStatusObserver(editConnectionsController);
		}
		
		final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("EditConnectionsWindow.fxml");
		final AnchorPane connectionWindow = FxmlUtils.loadAnchorPane(loader);
		editConnectionsController = ((EditConnectionsController) loader.getController());		
		editConnectionsController.setMainController(this);
		editConnectionsController.setEventManager(eventManager);
		editConnectionsController.setConnectionManager(connectionManager);
		editConnectionsController.setConfigurationManager(configurationManager);
		editConnectionsController.init();
		
		Scene scene = new Scene(connectionWindow);
		scene.getStylesheets().addAll(mainPane.getScene().getStylesheets());		

		editConnectionsStage = new Stage();
		editConnectionsStage.setTitle("Connection list");		
		editConnectionsStage.initModality(Modality.WINDOW_MODAL);
		editConnectionsStage.initOwner(getParentWindow());
		editConnectionsStage.setScene(scene);
	}

	private void initialiseFormattersWindow()
	{
		final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("FormattersWindow.fxml");
		final AnchorPane formattersWindow = FxmlUtils.loadAnchorPane(loader);
		
		final FormattersController formattersController = ((FormattersController) loader.getController());
		formattersController.setConfigurationManager(configurationManager);	
		formattersController.init();
		
		Scene scene = new Scene(formattersWindow);
		scene.getStylesheets().addAll(mainPane.getScene().getStylesheets());		

		formattersStage = new Stage();
		formattersStage.setTitle("Formatters");		
		formattersStage.initModality(Modality.WINDOW_MODAL);
		formattersStage.initOwner(getParentWindow());
		formattersStage.setScene(scene);
	}
	
	private void initialiseAboutWindow()
	{
		final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("AboutWindow.fxml");
		final AnchorPane window = FxmlUtils.loadAnchorPane(loader);
		
		aboutController = ((AboutController) loader.getController());
		aboutController.setApplication(application);
		aboutController.setConfigurationManager(configurationManager);
		aboutController.setVersionManager(versionManager);
		aboutController.setEventManager(eventManager);
		
		eventManager.registerVersionInfoObserver(aboutController);
		
		aboutController.init();
		
		Scene scene = new Scene(window);
		scene.getStylesheets().addAll(mainPane.getScene().getStylesheets());		

		aboutStage = new Stage();
		aboutStage.setTitle("About mqtt-spy");		
		aboutStage.initModality(Modality.WINDOW_MODAL);
		aboutStage.initOwner(getParentWindow());
		aboutStage.setScene(scene);
	}
	
	private void initialiseTestCasesWindow()
	{
		final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("TestCasesExecutionPane.fxml");
		final AnchorPane testCasesWindow = FxmlUtils.loadAnchorPane(loader);
		
		Scene scene = new Scene(testCasesWindow);
		scene.getStylesheets().addAll(mainPane.getScene().getStylesheets());		

		testCasesStage = new Stage();
		testCasesStage.setTitle("Test cases");		
		testCasesStage.initOwner(getParentWindow());
		testCasesStage.setScene(scene);
		((TestCasesExecutionController) loader.getController()).init();
	}
	
	private void showEditConnectionsWindow(final boolean createNew)
	{
		if (editConnectionsController  == null)
		{
			initialiseEditConnectionsWindow();
		}
		
		if (createNew)
		{
			editConnectionsController.newMqttConnection();
		}

		editConnectionsController.updateUIForSelectedItem();
		editConnectionsController.setPerspective(selectedPerspective);
		editConnectionsStage.showAndWait();		
		controlPanelPaneController.refreshConnectionsStatus();
	}
	
	@FXML
	public void exit()
	{
		// This is triggered by the user
		connectionManager.disconnectAll();
		
		statisticsManager.saveStats();
		
		configurationManager.saveUiProperties(
				getLastWidth(), getLastHeight(), stage.isMaximized(), 
				selectedPerspective, resizeMessagePaneMenu.isSelected());
		
		System.exit(0);
	}

	/**
	 * Sets the perspective.
	 * 
	 * @param selectedPerspective the selectedPerspective to set
	 */
	public void setSelectedPerspective(final SpyPerspective selectedPerspective)
	{
		this.selectedPerspective = selectedPerspective;
		
		switch (selectedPerspective)
		{
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
	}

	public TabPane getConnectionTabs()
	{
		return connectionTabs;
	}

	public void addConnectionTab(Tab tab)
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
			loadConfigurationFileAndShowErrorWhenApplicable(selectedFile);
		}
	}
	
	public void loadConfigurationFileAndShowErrorWhenApplicable(final File selectedFile)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				loadConfigurationFile(selectedFile);				
			}
			
		});		
	}
	
	
	private void clear()
	{
		connectionManager.disconnectAndCloseAll();
		
		// Only re-initialise if it has been initialised already
		if (editConnectionsController  != null)
		{
			initialiseEditConnectionsWindow();
		}	
	}
	
	private void loadConfigurationFile(final File selectedFile)
	{
		logger.info("Loading configuration file from " + selectedFile.getAbsolutePath());
		
		if (configurationManager.loadConfiguration(selectedFile))
		{
			clear();
			controlPanelPaneController.refreshConnectionsStatus();
			
			// Process the connection settings		
			for (final ConfiguredConnectionDetails connection : configurationManager.getConnections())
			{
				if (connection.isAutoOpen() != null && connection.isAutoOpen())
				{					
					try
					{
						connectionManager.openConnection(connection, this);
					}
					catch (ConfigurationException e)
					{
						// TODO: show warning dialog for invalid
						logger.error("Cannot open conection {}", connection.getName(), e);
					}
				}
			}			
		}
		
		controlPanelPaneController.refreshConfigurationFileStatus();		
	}		
	
	public void populateConnectionPanes(final UserInterfaceMqttConnectionDetails connectionDetails, final ConnectionController connectionController)
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

	public void loadDefaultConfigurationFile()
	{		
		final File defaultConfigurationFile = ConfigurationManager.getDefaultConfigurationFile();
		
		logger.info("Default configuration file present (" + defaultConfigurationFile.getAbsolutePath() + ") = " + defaultConfigurationFile.exists());
		
		if (defaultConfigurationFile.exists())
		{
			loadConfigurationFileAndShowErrorWhenApplicable(defaultConfigurationFile);
		}
	}
	
	@FXML
	private void showPerspective()
	{
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
		else
		{
			selectedPerspective = SpyPerspective.DEFAULT;
		}
		
		for (final ConnectionController connectionController : connectionManager.getConnectionControllers())
		{
			showPerspective(connectionController);
		}
		
		logger.debug("Selected perspective = " + selectedPerspective.toString());
	}
	
	public void showPerspective(final ConnectionController connectionController)
	{
		switch (selectedPerspective)
		{
			case DETAILED:
				connectionController.showPanes(PaneVisibilityStatus.ATTACHED, PaneVisibilityStatus.ATTACHED, PaneVisibilityStatus.ATTACHED, PaneVisibilityStatus.ATTACHED);
				connectionController.setDetailedViewVisibility(true);
				break;
			case SPY:
				connectionController.showPanes(PaneVisibilityStatus.NOT_VISIBLE, PaneVisibilityStatus.NOT_VISIBLE, PaneVisibilityStatus.ATTACHED, PaneVisibilityStatus.ATTACHED);		
				connectionController.setDetailedViewVisibility(false);
				break;
			case SUPER_SPY:
				connectionController.showPanes(PaneVisibilityStatus.NOT_VISIBLE, PaneVisibilityStatus.NOT_VISIBLE, PaneVisibilityStatus.ATTACHED, PaneVisibilityStatus.ATTACHED);
				connectionController.setDetailedViewVisibility(true);
				break;
			default:
				connectionController.showPanes(PaneVisibilityStatus.ATTACHED, PaneVisibilityStatus.ATTACHED, PaneVisibilityStatus.ATTACHED, PaneVisibilityStatus.ATTACHED);
				connectionController.setDetailedViewVisibility(false);
				break;		
		}
	}
	
	@FXML
	private void resizeMessagePane()
	{
		// Connection tabs
		for (final ConnectionController controller : connectionManager.getConnectionControllers())
		{
			controller.getResizeMessageContentMenu().setSelected(resizeMessagePaneMenu.isSelected());
		}
		// Offline (message log) tabs
		for (final ConnectionController controller : connectionManager.getOfflineConnectionControllers())
		{
			controller.getResizeMessageContentMenu().setSelected(resizeMessagePaneMenu.isSelected());
		}
	}
	
	@FXML
	private void restoreConfiguration()
	{
		if (DialogUtils.showDefaultConfigurationFileMissingChoice("Restore defaults", mainPane.getScene().getWindow()))
		{
			loadConfigurationFileAndShowErrorWhenApplicable(ConfigurationManager.getDefaultConfigurationFile());			
		}
	}
	
	@FXML
	private void showAbout()
	{
		if (aboutStage == null)
		{
			initialiseAboutWindow();
		}
		
		aboutController.reloadVersionInfo();
		aboutStage.show();		
	}
	
	@FXML
	private void openGettingInvolved()
	{
		application.getHostServices().showDocument("https://github.com/kamilfb/mqtt-spy/wiki/Getting-involved");
	}

	@FXML
	private void overviewWiki()
	{
		application.getHostServices().showDocument("https://github.com/kamilfb/mqtt-spy/wiki/Overview");		
	}
	
	@FXML
	private void changelogWiki()
	{
		application.getHostServices().showDocument("https://github.com/kamilfb/mqtt-spy/wiki/Changelog");
	}
	
	@FXML
	private void scriptingWiki()
	{
		application.getHostServices().showDocument("https://github.com/kamilfb/mqtt-spy/wiki/Scripting");
	}
	
	@FXML
	private void messageSearchWiki()
	{
		application.getHostServices().showDocument("https://github.com/kamilfb/mqtt-spy/wiki/MessageSearch");
	}
	
	@FXML
	private void loggingWiki()
	{
		application.getHostServices().showDocument("https://github.com/kamilfb/mqtt-spy/wiki/Logging");
	}

	public void setApplication(Application application)
	{
		this.application = application;
	}

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
	public void setConfigurationManager(ConfigurationManager configurationManager)
	{
		this.configurationManager = configurationManager;
	}

	/**
	 * Sets the event manager.
	 *  
	 * @param eventManager the eventManager to set
	 */
	public void setEventManager(EventManager<FormattedMqttMessage> eventManager)
	{
		this.eventManager = eventManager;
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

	public CheckMenuItem getResizeMessagePaneMenu()
	{
		return resizeMessagePaneMenu;
	}

	public Stage getEditConnectionsStage()
	{
		return editConnectionsStage;
	}
}
