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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.connectivity.BaseMqttSubscription;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.connectivity.MqttSubscription;
import pl.baczkowicz.mqttspy.ui.MqttConnectionViewManager;
import pl.baczkowicz.mqttspy.ui.MqttViewManager;
import pl.baczkowicz.mqttspy.ui.events.ShowNewMqttSubscriptionWindowEvent;
import pl.baczkowicz.mqttspy.ui.scripts.InteractiveScriptManager;
import pl.baczkowicz.mqttspy.ui.utils.DialogUtils;
import pl.baczkowicz.mqttspy.ui.utils.MqttStylingUtils;
import pl.baczkowicz.spy.connectivity.ConnectionStatus;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.ui.controllers.IConnectionController;
import pl.baczkowicz.spy.ui.controllers.TestCasesExecutionController;
import pl.baczkowicz.spy.ui.events.ConnectionStatusChangeEvent;
import pl.baczkowicz.spy.ui.panes.PaneVisibilityManager;
import pl.baczkowicz.spy.ui.panes.PaneVisibilityStatus;
import pl.baczkowicz.spy.ui.panes.TabController;
import pl.baczkowicz.spy.ui.panes.TabStatus;
import pl.baczkowicz.spy.ui.panes.TitledPaneStatus;
import pl.baczkowicz.spy.ui.stats.StatisticsManager;
import pl.baczkowicz.spy.ui.utils.DialogFactory;
import pl.baczkowicz.spy.ui.utils.FxmlUtils;
import pl.baczkowicz.spy.ui.utils.UiUtils;

/**
 * Controller looking after the connection tab.
 * 
 * TODO: create an MqttConnectionViewManager, and simplify this controller class
 */
public class MqttConnectionController implements Initializable, TabController, PaneVisibilityManager, IConnectionController
{
	private static final int MIN_COLLAPSED_PANE_HEIGHT = 26;
	
	private static final int SUBSCRIPTION_PANE_MIN_EXPANDED_HEIGHT = 66;
	
	private static final int TEST_CASES_PANE_MIN_EXPANDED_HEIGHT = 190;
	
	private static final int PUBLICATION_PANE_MIN_EXPANDED_HEIGHT = 96;	
	
	private static final int SCRIPTED_PUBLICATION_PANE_MIN_EXPANDED_HEIGHT = 136;	

	private static final double MIN_WINDOW_HIGHT = SUBSCRIPTION_PANE_MIN_EXPANDED_HEIGHT;
	
	private static final double MIN_WINDOW_WIDTH = 300;

	private final static Logger logger = LoggerFactory.getLogger(MqttConnectionController.class);

	@FXML
	private AnchorPane connectionPane;
	
	@FXML
	private SplitPane splitPane;
	
	@FXML
	private AnchorPane newPublicationPane;
	
	@FXML
	private AnchorPane newSubscriptionPane;
	
	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private NewPublicationController newPublicationPaneController;
	
	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private PublicationScriptsController publicationScriptsPaneController;
	
	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private NewSubscriptionController newSubscriptionPaneController;
	
	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private TestCasesExecutionController testCasesPaneController;
	
	/** For convenience, this represents a controller for the subscriptions titled pane. */
	private SubscriptionsController subscriptionsController = new SubscriptionsController();

	@FXML 
	private Button newSubButton;
	
	@FXML
	private TitledPane publishMessageTitledPane;
	
	@FXML
	private TitledPane newSubscriptionTitledPane;

	@FXML
	private TitledPane scriptedPublicationsTitledPane;
	
	@FXML
	private TitledPane subscriptionsTitledPane;
	
	@FXML
	private TitledPane testCasesTitledPane;
	
	private TitledPaneStatus publishMessageTitledStatus = new TitledPaneStatus(0);
	
	private TitledPaneStatus newSubscriptionTitledStatus = new TitledPaneStatus(3);

	private TitledPaneStatus scriptedPublicationsTitledStatus = new TitledPaneStatus(1);
	
	private TitledPaneStatus subscriptionsTitledStatus = new TitledPaneStatus(4);
	
	private TitledPaneStatus testCasesTitledStatus = new TitledPaneStatus(2);
	
	@FXML
	private TabPane subscriptionTabs;

	private MqttAsyncConnection connection;

	private Tab connectionTab;
	
	private Tooltip tooltip;

	private StatisticsManager statisticsManager;

	private MqttConnectionViewManager connectionManager;

	private IKBus eventBus;
	
	private Map<TitledPane, TitledPaneStatus> paneToStatus = new HashMap<>();

	private boolean detailedView;

	private boolean replayMode;

	private TabStatus tabStatus;

	private CheckMenuItem resizeMessageContentMenu = new CheckMenuItem();

	private ChangeListener<Boolean> createChangeListener()
	{
		return new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2)
			{
				updateMinHeights();
			}
		};
	}
	
	public void initialize(URL location, ResourceBundle resources)
	{		
		newSubButton.setTooltip(new Tooltip("Create new subscription [" + MqttViewManager.newSubscription.getDisplayText() + "]"));
		final MenuItem attach = new MenuItem("Show attached");
		final MqttConnectionController controller = this;
		attach.setOnAction(new EventHandler<ActionEvent>()
		{					
			@Override
			public void handle(ActionEvent event)
			{
				showNewSubscription(PaneVisibilityStatus.ATTACHED, controller);				
			}
		});
		newSubButton.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				if (MouseButton.PRIMARY.equals(event.getButton()))
				{
					showNewSubscription(PaneVisibilityStatus.DETACHED, controller);
				}
				else
				{
					newSubButton.getContextMenu().show(newSubButton.getScene().getWindow());
				}
			}				
		});
		
		newSubButton.setContextMenu(new ContextMenu(attach));	
		newSubButton.setDisable(false);
	}
		
	private void showNewSubscription(final PaneVisibilityStatus status, final MqttConnectionController connectionController)
	{
		eventBus.publish(new ShowNewMqttSubscriptionWindowEvent(connectionController, 
				status,
				connectionController.getNewSubscriptionPaneStatus().getVisibility()));
	}
	
	@FXML
	public void newSubscription()
	{
		final Tab selectedTab = connectionTab;
		final MqttConnectionController controller = (MqttConnectionController) connectionManager.getControllerForTab(selectedTab);
		
		if (controller != null)
		{
			eventBus.publish(new ShowNewMqttSubscriptionWindowEvent(
					controller, 
					PaneVisibilityStatus.DETACHED,
					controller.getNewSubscriptionPaneStatus().getVisibility()));
		}
	}
	
	public void init()
	{
		publishMessageTitledStatus.setController(newPublicationPaneController);
		newSubscriptionTitledStatus.setController(newSubscriptionPaneController);
		scriptedPublicationsTitledStatus.setController(publicationScriptsPaneController);
		subscriptionsTitledStatus.setController(subscriptionsController);
		testCasesTitledStatus.setController(testCasesPaneController);
		
		subscriptionsTitledPane.expandedProperty().addListener(createChangeListener());
		
		// panes.put(subscriptionsController, true);
		subscriptionsTitledStatus.setVisibility(PaneVisibilityStatus.ATTACHED);
		paneToStatus.put(subscriptionsTitledPane, subscriptionsTitledStatus);
		
		subscriptionsController.setTitledPane(subscriptionsTitledPane);
		subscriptionsController.setConnectionController(this);
		subscriptionsController.init();
		
		if (!replayMode)
		{
			publishMessageTitledPane.expandedProperty().addListener(createChangeListener());		
			scriptedPublicationsTitledPane.expandedProperty().addListener(createChangeListener());		
			newSubscriptionTitledPane.expandedProperty().addListener(createChangeListener());
						
			scriptedPublicationsTitledPane.setExpanded(false);			
			
			publishMessageTitledStatus.setVisibility(PaneVisibilityStatus.ATTACHED);
			scriptedPublicationsTitledStatus.setVisibility(PaneVisibilityStatus.ATTACHED);
			newSubscriptionTitledStatus.setVisibility(PaneVisibilityStatus.ATTACHED);
			testCasesTitledStatus.setVisibility(PaneVisibilityStatus.NOT_VISIBLE);
			
			paneToStatus.put(publishMessageTitledPane, publishMessageTitledStatus);
			paneToStatus.put(scriptedPublicationsTitledPane, scriptedPublicationsTitledStatus);
			paneToStatus.put(newSubscriptionTitledPane, newSubscriptionTitledStatus);			
			
			newPublicationPaneController.setConnection(connection);
			newPublicationPaneController.setScriptManager(connection.getScriptManager());
			newPublicationPaneController.setEventBus(eventBus);
			newPublicationPaneController.setConnectionController(this);
			newPublicationPaneController.setTitledPane(publishMessageTitledPane);
			newPublicationPaneController.init();
			
			newSubscriptionPaneController.setConnection(connection);
			newSubscriptionPaneController.setConnectionController(this);
			newSubscriptionPaneController.setEventBus(eventBus);
			newSubscriptionPaneController.setConnectionManager(connectionManager);
			newSubscriptionPaneController.setTitledPane(newSubscriptionTitledPane);
			newSubscriptionPaneController.init();
			
			publicationScriptsPaneController.setConnection(connection);
			publicationScriptsPaneController.setEventBus(eventBus);
			publicationScriptsPaneController.setConnectionController(this);
			publicationScriptsPaneController.setTitledPane(scriptedPublicationsTitledPane);
			publicationScriptsPaneController.init();	
			
			tooltip = new Tooltip();
			connectionTab.setTooltip(tooltip);
		}
		else
		{
			// If in replay more, remote the panes from the split pane altogether
			// TODO: don't add them in the first place?
			splitPane.getItems().remove(publishMessageTitledPane);
			splitPane.getItems().remove(scriptedPublicationsTitledPane);
			splitPane.getItems().remove(newSubscriptionTitledPane);
			splitPane.getItems().remove(testCasesTitledPane);
		}
		
		updateVisiblePanes();
		//updateMenus();
		updateMinHeights();
		// connectionPane.setMaxWidth(500);
		// subscriptionsTitledPane.setMaxWidth(500);
		// subscriptionTabs.setMaxWidth(500);
		// TODO: how not to resize the tab pane on too many tabs? All max sizes seems to be ignored...
	}
	
	private void initialiseTestCasesPane()
	{
		if (testCasesPaneController == null)
		{
			final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("TestCasesExecutionPane.fxml");
			final AnchorPane testCasesPane = FxmlUtils.loadAnchorPane(loader);
			
			testCasesTitledPane = new TitledPane();
			testCasesTitledPane.setText("Test cases");
			testCasesTitledPane.setContent(testCasesPane);
			testCasesTitledPane.expandedProperty().addListener(createChangeListener());
			testCasesTitledPane.setExpanded(false);
			
			testCasesPaneController = loader.getController();
			// testCasesPaneController.setConnection(connection);
			testCasesPaneController.setScriptManager(new InteractiveScriptManager(eventBus, connection));
			// testCasesPaneController.setConnectionController(this);
			
			testCasesPaneController.setTitledPane(testCasesTitledPane);
			testCasesPaneController.init();
			testCasesPaneController.setSettingsButton(MqttViewManager.createTitleButtons(testCasesPaneController, new AnchorPane(), this));
			
			testCasesTitledStatus.setController(testCasesPaneController);
						
			paneToStatus.put(testCasesTitledPane, testCasesTitledStatus);
			
			logger.debug("Test cases pane initialised!");
		}
	}
	
	public Map<TitledPane, TitledPaneStatus> getPaneToStatusMapping()
	{
		return paneToStatus;
	}
	
	public void setConnectionManager(final MqttConnectionViewManager connectionManager)
	{
		this.connectionManager = connectionManager;
	}
	
	public void updateMinHeights()
	{
		if (publishMessageTitledPane.isExpanded())
		{
			publishMessageTitledPane.setMinHeight(PUBLICATION_PANE_MIN_EXPANDED_HEIGHT);
		}
		else
		{
			publishMessageTitledPane.setMinHeight(MIN_COLLAPSED_PANE_HEIGHT);
		}
		
		if (scriptedPublicationsTitledPane.isExpanded())
		{
			scriptedPublicationsTitledPane.setMinHeight(SCRIPTED_PUBLICATION_PANE_MIN_EXPANDED_HEIGHT);
		}
		else
		{
			scriptedPublicationsTitledPane.setMinHeight(MIN_COLLAPSED_PANE_HEIGHT);
		}
		
		if (newSubscriptionTitledPane.isExpanded())
		{
			newSubscriptionTitledPane.setMinHeight(SUBSCRIPTION_PANE_MIN_EXPANDED_HEIGHT);
			newSubscriptionTitledPane.setMaxHeight(SUBSCRIPTION_PANE_MIN_EXPANDED_HEIGHT);
		}
		else
		{
			newSubscriptionTitledPane.setMinHeight(MIN_COLLAPSED_PANE_HEIGHT);
			newSubscriptionTitledPane.setMaxHeight(MIN_COLLAPSED_PANE_HEIGHT);
		}
		
		if (testCasesTitledPane != null)
		{
			if (testCasesTitledPane.isExpanded())
			{
				testCasesTitledPane.setMinHeight(TEST_CASES_PANE_MIN_EXPANDED_HEIGHT);
			}
			else
			{
				testCasesTitledPane.setMinHeight(MIN_COLLAPSED_PANE_HEIGHT);
			}
		}
	}

	public MqttAsyncConnection getConnection()
	{
		return connection;
	}

	public void setConnection(MqttAsyncConnection connection)
	{
		this.connection = connection;
	}

	public Tab getTab()
	{
		return connectionTab;
	}

	public void setTab(Tab tab)
	{
		this.connectionTab = tab;
	}

	public TabPane getSubscriptionTabs()
	{
		return subscriptionTabs;
	}
	
	public void showTabTile(final boolean pending)
	{
		if (pending)
		{
			final HBox title = new HBox();
			title.setAlignment(Pos.CENTER);
			final ProgressIndicator progressIndicator = new ProgressIndicator();
			progressIndicator.setMaxSize(15, 15);												
			title.getChildren().add(progressIndicator);
			title.getChildren().add(new Label(" " + connection.getName()));
			connectionTab.setGraphic(title);
			connectionTab.setText(null);
		}
		else if (connection.getConnectionStatus().equals(ConnectionStatus.CONNECTED))
		{								
			connectionTab.setGraphic(
					UiUtils.createSecurityIcons(
							connection.getProperties().getSSL() != null, 
							connection.getProperties().getUserCredentials() != null, false));
			connectionTab.setText(connection.getName());
		}
		else
		{			
			connectionTab.setGraphic(null);
			connectionTab.setText(connection.getName());
		}
	}
	
	public void onConnectionStatusChanged(final ConnectionStatusChangeEvent event)
	{
		final ConnectionStatus connectionStatus = event.getConnectionStatus();
		
		logger.debug("Updating {} connection status to {}", event.getName(), connectionStatus);		
		
		newSubscriptionPaneController.setConnected(false);
		getNewPublicationPaneController().setConnected(false);
		
		for (final BaseMqttSubscription sub : connection.getSubscriptions().values())
		{
			if (sub instanceof MqttSubscription)
			{
				((MqttSubscription) sub).getSubscriptionController().updateContextMenu();
			}
		}
		
		// If the context menu is available and has items in it
		if (connectionTab.getContextMenu() != null && connectionTab.getContextMenu().getItems().size() > 0)
		{
			// TODO: change that to the Specification pattern
			switch (connectionStatus)
			{
				case NOT_CONNECTED:
					connectionTab.getContextMenu().getItems().get(0).setDisable(false);
					connectionTab.getContextMenu().getItems().get(2).setDisable(true);										
					connectionTab.getContextMenu().getItems().get(3).setDisable(false);
					connectionTab.getContextMenu().getItems().get(5).setDisable(true);
					showTabTile(false);
					break;
				case CONNECTED:					
					connectionTab.getContextMenu().getItems().get(0).setDisable(true);
					connectionTab.getContextMenu().getItems().get(2).setDisable(false);
					connectionTab.getContextMenu().getItems().get(3).setDisable(false);
					connectionTab.getContextMenu().getItems().get(5).setDisable(false);
					newSubscriptionPaneController.setConnected(true);
					getNewPublicationPaneController().setConnected(true);
					showTabTile(false);
					break;
				case CONNECTING:
					connectionTab.getContextMenu().getItems().get(2).setDisable(true);
					connectionTab.getContextMenu().getItems().get(0).setDisable(true);					
					connectionTab.getContextMenu().getItems().get(3).setDisable(true);
					connectionTab.getContextMenu().getItems().get(5).setDisable(true);
					showTabTile(true);						
					break;
				case DISCONNECTED:
					connectionTab.getContextMenu().getItems().get(0).setDisable(false);
					connectionTab.getContextMenu().getItems().get(2).setDisable(true);										
					connectionTab.getContextMenu().getItems().get(3).setDisable(false);
					connectionTab.getContextMenu().getItems().get(5).setDisable(true);
					showTabTile(false);
					break;
				case DISCONNECTING:					
					connectionTab.getContextMenu().getItems().get(0).setDisable(true);
					connectionTab.getContextMenu().getItems().get(2).setDisable(true);
					connectionTab.getContextMenu().getItems().get(3).setDisable(false);
					connectionTab.getContextMenu().getItems().get(5).setDisable(true);
					showTabTile(false);
					break;
				default:
					break;
			}
		}

		if (connectionTab.getStyleClass().size() > 1)
		{
			connectionTab.getStyleClass().remove(1);
		}
		connectionTab.getStyleClass().add(MqttStylingUtils.getStyleForMqttConnectionStatus(connectionStatus));
		
		DialogUtils.updateConnectionTooltip(connection, tooltip);
	}
	
	public void updateConnectionStats()
	{
		for (final SubscriptionController subscriptionController : connectionManager.getSubscriptionManager(this).getSubscriptionControllers())
		{
			subscriptionController.updateSubscriptionStats();
		}
	}

	public StatisticsManager getStatisticsManager()
	{
		return statisticsManager;
	}

	public void setStatisticsManager(StatisticsManager statisticsManager)
	{
		this.statisticsManager = statisticsManager;
	}
	
	public NewSubscriptionController getNewSubscriptionPaneController()
	{
		return newSubscriptionPaneController;
	}

	public void setPaneVisiblity(final TitledPaneStatus paneStatus, final PaneVisibilityStatus visibility)
	{
		if (paneStatus == testCasesTitledStatus && testCasesPaneController == null 
				&& (PaneVisibilityStatus.ATTACHED.equals(visibility) || PaneVisibilityStatus.DETACHED.equals(visibility)))
		{
			initialiseTestCasesPane();
		}
		
		// Ignore any layout requests when in replay mode
		if (!replayMode)
		{
			paneStatus.setRequestedVisibility(visibility);			
			updateVisiblePanes();
			updateMenus();
		}		
	}
	
	public boolean getDetailedViewVisibility()
	{
		return detailedView;
	}
	
	public void setViewVisibility(final boolean detailedView, final boolean basicView)
	{
		this.detailedView = detailedView;
		newSubscriptionPaneController.setViewVisibility(detailedView);
		getNewPublicationPaneController().setViewVisibility(detailedView);
		
		for (final SubscriptionController subscriptionController : connectionManager.getSubscriptionManager(this).getSubscriptionControllers())
		{
			subscriptionController.setViewVisibility(detailedView, basicView);
		}
	}
	
	public void toggleMessagePayloadSize(final boolean resize)
	{
		for (final SubscriptionController subscriptionController : connectionManager.getSubscriptionManager(this).getSubscriptionControllers())
		{
			subscriptionController.toggleMessagePayloadSize(resize);
		}
	}
	
	public void toggleDetailedViewVisibility()
	{
		newSubscriptionPaneController.toggleDetailedViewVisibility();
		getNewPublicationPaneController().toggleDetailedViewVisibility();
		
		for (final SubscriptionController subscriptionController : connectionManager.getSubscriptionManager(this).getSubscriptionControllers())
		{
			subscriptionController.toggleDetailedViewVisibility();
		}
	}
	
	public void showPanes(final PaneVisibilityStatus showManualPublications, final PaneVisibilityStatus showScriptedPublications, 
			final PaneVisibilityStatus showNewSubscription, final PaneVisibilityStatus showReceivedMessagesSummary)
	{
		// Ignore any layout requests when in replay mode
		if (!replayMode)
		{
			subscriptionsTitledStatus.setRequestedVisibility(showReceivedMessagesSummary);
			publishMessageTitledStatus.setRequestedVisibility(showManualPublications);
			scriptedPublicationsTitledStatus.setRequestedVisibility(showScriptedPublications);
			newSubscriptionTitledStatus.setRequestedVisibility(showNewSubscription);						
			
			updateVisiblePanes();
			updateMenus();
		}
	}
	
	public void setReplayMode(final boolean value)
	{
		replayMode = value;
	}
	
	public void showReplayMode()
	{	
		connectionTab.getStyleClass().add("connection-replay");
				
		subscriptionsTitledStatus.setRequestedVisibility(PaneVisibilityStatus.ATTACHED);
		publishMessageTitledStatus.setRequestedVisibility(PaneVisibilityStatus.NOT_VISIBLE);
		scriptedPublicationsTitledStatus.setRequestedVisibility(PaneVisibilityStatus.NOT_VISIBLE);
		newSubscriptionTitledStatus.setRequestedVisibility(PaneVisibilityStatus.NOT_VISIBLE);						
		testCasesTitledStatus.setRequestedVisibility(PaneVisibilityStatus.NOT_VISIBLE);
		
		updateVisiblePanes();
				
		subscriptionsTitledPane.setText("Logged messages");
		subscriptionsController.init();
	}
	
	private void updateMenus()
	{
		for (final TitledPaneStatus status : paneToStatus.values())
		{
			status.updateMenu();
		}		
	}
		
	private void insertPane(final TitledPaneStatus status)
	{
		int insertIndex = splitPane.getItems().size();
		
		for (int i = 0; i < splitPane.getItems().size(); i++)
		{
			final Node pane = splitPane.getItems().get(i);
			
			if (paneToStatus.get(pane).getDisplayIndex() > status.getDisplayIndex())
			{
				insertIndex = i;
				break;
			}
		}
		
		// logger.info("Inserting at " + insertIndex + "; " + controller);
		splitPane.getItems().add(insertIndex, status.getController().getTitledPane());
	}
	
	// TODO: this should be moved out to the ViewManager
	private void updateVisiblePanes()
	{	
		for (final TitledPaneStatus status : paneToStatus.values())
		{	
			// If no changes, go to next controller...
			if (status.getVisibility().equals(status.getRequestedVisibility()))
			{
				continue;
			}
			
			status.setVisibility(status.getRequestedVisibility());
			status.getController().updatePane(status.getRequestedVisibility());
			
			// If previous value was detached, close the detached window
			if (status.getPreviousVisibility().equals(PaneVisibilityStatus.DETACHED))
			{
				status.getController().getTitledPane().setCollapsible(true);
				status.getController().getTitledPane().setExpanded(status.isLastExpanded());
				
				if (status.getParentWhenDetached().isShowing())
				{
					status.getParentWhenDetached().close();
				}
			}
			// If previous value was attached, remove the pane
			else if (status.getPreviousVisibility().equals(PaneVisibilityStatus.ATTACHED))
			{
				// Remove from main window
				if (splitPane.getItems().contains(status.getController().getTitledPane()))
				{
					splitPane.getItems().remove(status.getController().getTitledPane());
				}
			}
			
			// If the pane should be detached
			if (status.getVisibility().equals(PaneVisibilityStatus.DETACHED))
			{				
				// Add to separate window
				final Stage stage = DialogFactory.createWindowWithPane(status.getController().getTitledPane(), splitPane.getScene(), 
						connection.getName(), 0);
				status.setParentWhenDetached(stage);
				status.setLastExpanded(status.getController().getTitledPane().isExpanded());
				stage.setOnCloseRequest(new EventHandler<WindowEvent>()
				{					
					@Override
					public void handle(WindowEvent event)
					{
						status.setRequestedVisibility(status.getPreviousVisibility());						
						updateVisiblePanes();
						updateMenus();
						updateMinHeights();
					}
				});
				
				status.getController().getTitledPane().setExpanded(true);
				status.getController().getTitledPane().setCollapsible(false);
				updateMinHeights();
				
				final double paneMinHeight = status.getController().getTitledPane().getMinHeight(); 
				
				stage.setMinHeight(paneMinHeight > MIN_WINDOW_HIGHT ? paneMinHeight : MIN_WINDOW_HIGHT);
				stage.setMinWidth(MIN_WINDOW_WIDTH);
				stage.show();								
			}
			// If set to be shown
			else if (status.getVisibility().equals(PaneVisibilityStatus.ATTACHED))
			{
				// logger.info("Show; contains = " + splitPane.getItems().contains(controller.getTitledPane()));
				
				// Show
				if (!splitPane.getItems().contains(status.getController().getTitledPane()))
				{
					insertPane(status);
				}
			}
		}
	}
	
	public TabStatus getTabStatus()
	{		
		return tabStatus;
	}	

	/**
	 * Sets the pane status.
	 * 
	 * @param paneStatus the paneStatus to set
	 */
	public void setTabStatus(TabStatus paneStatus)
	{
		this.tabStatus = paneStatus;
	}

	@Override
	public void refreshStatus()
	{
		if (connection != null)
		{
			
			onConnectionStatusChanged(new ConnectionStatusChangeEvent(connection, connection.getName(), connection.getConnectionStatus()));
		}
	}

	/**
	 * Gets the new publication pane controller.
	 * 
	 * @return the newPublicationPaneController
	 */
	public NewPublicationController getNewPublicationPaneController()
	{
		return newPublicationPaneController;
	}

	/**
	 * Gets the subscriptions controller.
	 * 
	 * @return the subscriptionsController
	 */
	public SubscriptionsController getSubscriptionsController()
	{
		return subscriptionsController;
	}

	/**
	 * Gets the publication scripts controller.
	 * 
	 * @return the publicationScriptsPaneController
	 */
	public PublicationScriptsController getPublicationScriptsPaneController()
	{
		return publicationScriptsPaneController;
	}

	public CheckMenuItem getResizeMessageContentMenu()
	{
		return resizeMessageContentMenu;
	}

	public TestCasesExecutionController getTestCasesPaneController()
	{
		return testCasesPaneController;
	}

	public TitledPaneStatus getNewPublicationPaneStatus()
	{
		return publishMessageTitledStatus;
	}

	public TitledPaneStatus getPublicationScriptsPaneStatus()
	{
		return scriptedPublicationsTitledStatus;
	}

	public TitledPaneStatus getNewSubscriptionPaneStatus()
	{
		return newSubscriptionTitledStatus;
	}

	public TitledPaneStatus getSubscriptionsStatus()
	{
		return subscriptionsTitledStatus;
	}

	public TitledPaneStatus getTestCasesPaneStatus()
	{
		return testCasesTitledStatus;
	}
	
	public void setEventBus(final IKBus eventBus)
	{
		this.eventBus = eventBus;
	}
}
