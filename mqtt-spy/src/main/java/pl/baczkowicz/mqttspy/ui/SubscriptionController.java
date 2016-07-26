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

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.connectivity.MqttSubscription;
import pl.baczkowicz.mqttspy.connectivity.RuntimeConnectionProperties;
import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.stats.StatisticsManager;
import pl.baczkowicz.mqttspy.ui.connections.SubscriptionManager;
import pl.baczkowicz.mqttspy.ui.events.EventManager;
import pl.baczkowicz.mqttspy.ui.events.observers.SubscriptionStatusChangeObserver;
import pl.baczkowicz.mqttspy.ui.messagelog.MessageLogUtils;
import pl.baczkowicz.spy.common.generated.FormatterDetails;
import pl.baczkowicz.spy.common.generated.Formatting;
import pl.baczkowicz.spy.formatting.FormattingManager;
import pl.baczkowicz.spy.formatting.FormattingUtils;
import pl.baczkowicz.spy.ui.events.observers.ClearTabObserver;
import pl.baczkowicz.spy.ui.events.queuable.ui.BrowseReceivedMessageEvent;
import pl.baczkowicz.spy.ui.panes.SpyPerspective;
import pl.baczkowicz.spy.ui.panes.TabController;
import pl.baczkowicz.spy.ui.panes.TabStatus;
import pl.baczkowicz.spy.ui.search.UniqueContentOnlyFilter;
import pl.baczkowicz.spy.ui.storage.BasicMessageStoreWithSummary;
import pl.baczkowicz.spy.ui.storage.ManagedMessageStoreWithFiltering;
import pl.baczkowicz.spy.ui.utils.FxmlUtils;
import pl.baczkowicz.spy.ui.utils.UiUtils;
import pl.baczkowicz.spy.utils.ConversionUtils;

/**
 * Controller for the subscription tab.
 */
public class SubscriptionController implements Initializable, ClearTabObserver<FormattedMqttMessage>, 
	SubscriptionStatusChangeObserver, TabController
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(SubscriptionController.class);
	
	public static final String AVG300_TOPIC = "5 minute average";

	public static final String AVG30_TOPIC = "30 second average";

	public static final String AVG5_TOPIC = "5 second average";

	private static final int MIN_EXPANDED_SUMMARY_PANE_HEIGHT = 130;

	private static final int MIN_COLLAPSED_SUMMARY_PANE_HEIGHT = 31;
	
	private static final String SUMMARY_PANE_TITLE = "Received messages summary";

	/** (10 topics; 50 messages, load average: 0.1/0.5/5.0). */
	private static final String SUMMARY_PANE_STATS_FORMAT = " (%s, %s, " + StatisticsManager.STATS_FORMAT + ")";

	@FXML
	private SplitPane splitPane;

	@FXML
	private AnchorPane messagePane;

	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private MessageNavigationController messageNavigationPaneController;
	
	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private MessageController messagePaneController;
	
	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private SubscriptionSummaryTableController summaryTablePaneController;

	@FXML
	private TitledPane summaryTitledPane;

	@FXML
	private ToggleGroup wholeMessageFormat;

	@FXML
	private MenuButton formattingMenuButton;

	@FXML
	private Menu formatterMenu;
	
	@FXML
	private Menu customFormatterMenu;

	@FXML
	private ToggleGroup selectionFormat;
	
	@FXML
	private ToggleButton searchButton;

	private ManagedMessageStoreWithFiltering<FormattedMqttMessage> store; 
	
	private BasicMessageStoreWithSummary<FormattedMqttMessage> statsHistory;

	private Tab tab;

	private RuntimeConnectionProperties connectionProperties;

	private MqttSubscription subscription;

	private Stage searchStage;

	private SearchWindowController searchWindowController;

	private EventManager<FormattedMqttMessage> eventManager;

	private ConnectionController connectionController;

	private Label statsLabel;

	private AnchorPane paneTitle;

	private TextField searchBox;

	private HBox topicFilterBox;

	private boolean replayMode;

	private Formatting formatting;

	private UniqueContentOnlyFilter<FormattedMqttMessage> uniqueContentOnlyFilter;

	private TabStatus tabStatus;

	private HBox titleBox;

	private ConfigurationManager configurationManager;

	private FormattingManager formattingManager;

	public void initialize(URL location, ResourceBundle resources)
	{			
		statsLabel = new Label();
		topicFilterBox = new HBox();
		topicFilterBox.setPadding(new Insets(0, 0, 0, 0));
		
		final List<FormatterDetails> baseFormatters = FormattingUtils.createBaseFormatters();
		for (int i = 0; i < 5; i++)
		{
			wholeMessageFormat.getToggles().get(i).setUserData(baseFormatters.get(i));
		}

		wholeMessageFormat.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
		{
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue,
					Toggle newValue)
			{
				if (wholeMessageFormat.getSelectedToggle() != null)
				{
					formatWholeMessage();
				}
			}
		});
		
		selectionFormat.getToggles().get(0).setUserData(null);		
		for (int i = 0; i < 5; i++)
		{
			selectionFormat.getToggles().get(i+1).setUserData(baseFormatters.get(i));
		}		
		
		selectionFormat.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
		{
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue,
					Toggle newValue)
			{
				if (selectionFormat.getSelectedToggle() != null)
				{
					formatSelection();
				}
			}
		});

		summaryTitledPane.expandedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2)
			{
				updateMinHeights();

			}
		});

		updateMinHeights();		
	}
	
	private ChangeListener<Number> createPaneTitleWidthListener()
	{
		return new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{				 										
				Platform.runLater(new Runnable()
				{					
					@Override
					public void run()
					{
						final double absoluteSearchBoxX = searchBox.getLayoutX() + topicFilterBox.getLayoutX() + titleBox.getLayoutX();
						final double titledPaneWidth = updateTitleWidth(summaryTitledPane, paneTitle, 40);
						
						if (logger.isTraceEnabled())
						{
							logger.trace("New width = " + titledPaneWidth);
						}
						
						searchBox.setPrefWidth(titledPaneWidth - absoluteSearchBoxX - statsLabel.getWidth() - 100);
					}
				});
			}
		};
	}
	
	public void init()
	{
		final Tooltip summaryTitledPaneTooltip = new Tooltip(
				"Load, the average number of messages per second, is calculated over the following intervals: " 
						+ StatisticsManager.getPeriodList() + ".");
				
		statsHistory = new BasicMessageStoreWithSummary<FormattedMqttMessage>(
				"stats" + store.getName(), 
				store.getMessageList().getPreferredSize(), store.getMessageList().getMaxSize(), 
				0, formattingManager);
		
		eventManager.registerClearTabObserver(this, store);
		
		getSummaryTablePaneController().setStore(store);
		getSummaryTablePaneController().setConnectionController(connectionController);
		getSummaryTablePaneController().setEventManager(eventManager);
		getSummaryTablePaneController().init();
		
		messagePaneController.setStore(store);
		messagePaneController.setConfingurationManager(configurationManager);
		messagePaneController.setFormattingManager(formattingManager);
		messagePaneController.init();
		// The search pane's message browser wants to know about changing indices and format
		eventManager.registerChangeMessageIndexObserver(messagePaneController, store);
		eventManager.registerFormatChangeObserver(messagePaneController, store);
		
		messageNavigationPaneController.setStore(store);
		messageNavigationPaneController.setEventManager(eventManager);
		messageNavigationPaneController.init();		
		
		// The subscription pane's message browser wants to know about show first, index change and update index events 
		eventManager.registerChangeMessageIndexObserver(messageNavigationPaneController, store);
		eventManager.registerChangeMessageIndexFirstObserver(messageNavigationPaneController, store);
		eventManager.registerIncrementMessageIndexObserver(messageNavigationPaneController, store);
		
		eventManager.registerMessageAddedObserver(messageNavigationPaneController, store.getMessageList());
		eventManager.registerMessageRemovedObserver(messageNavigationPaneController, store.getMessageList());
		
		if (formatting.getFormatter().size() > 0)
		{
			customFormatterMenu.setDisable(false);
		}
					
		for (final FormatterDetails formatter : formatting.getFormatter())
		{
			// Check if this is really a custom one
			if (FormattingUtils.isDefault(formatter))
			{
				continue;
			}
			
			final RadioMenuItem customFormatterMenuItem = new RadioMenuItem(formatter.getName());
			customFormatterMenuItem.setToggleGroup(wholeMessageFormat);						
			customFormatterMenuItem.setUserData(formatter);			
			customFormatterMenu.getItems().add(customFormatterMenuItem);
			
			if (connectionProperties != null && formatter.equals(connectionProperties.getFormatter()))
			{
				customFormatterMenuItem.setSelected(true);
			}
		}
		
		store.setFormatter((FormatterDetails) wholeMessageFormat.getSelectedToggle().getUserData());	
		
		paneTitle = new AnchorPane();
		paneTitle.setPadding(new Insets(0, 0, 0, 0));
		paneTitle.setMaxWidth(Double.MAX_VALUE);
				
		searchBox = new TextField();
		searchBox.setFont(new Font("System", 11));
		searchBox.setPadding(new Insets(2, 5, 2, 5));
		searchBox.setMaxWidth(400);
		searchBox.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, final String oldValue, final String newValue)
			{
				getSummaryTablePaneController().updateTopicFilter(newValue);			
			}
		});
		
		paneTitle = new AnchorPane();
		paneTitle.setPadding(new Insets(0, 0, 0, 0));
		
		topicFilterBox.getChildren().addAll(new Label(" [search topics: "), searchBox, new Label("] "));
		titleBox = new HBox();
		titleBox.setPadding(new Insets(0, 0, 0, 0));				
		titleBox.getChildren().addAll(new Label(SUMMARY_PANE_TITLE), topicFilterBox);
		titleBox.prefWidth(Double.MAX_VALUE);
				
		summaryTitledPane.widthProperty().addListener(createPaneTitleWidthListener());
		statsLabel.widthProperty().addListener(createPaneTitleWidthListener());
		
		paneTitle.getChildren().addAll(titleBox, statsLabel);
		
		summaryTitledPane.setText(null);
		summaryTitledPane.setGraphic(paneTitle);
				
		if (!replayMode)
		{
			statsLabel.setTextAlignment(TextAlignment.RIGHT);
			statsLabel.setAlignment(Pos.CENTER_RIGHT);
			statsLabel.setTooltip(summaryTitledPaneTooltip);
			AnchorPane.setRightAnchor(statsLabel, 5.0);

			updateSubscriptionStats();
		}
		else
		{
			messageNavigationPaneController.hideShowLatest();
		}
		
		// logger.info("init(); finished on SubscriptionController");
		
		// Filtering
		uniqueContentOnlyFilter = new UniqueContentOnlyFilter<FormattedMqttMessage>(store, store.getUiEventQueue());
		uniqueContentOnlyFilter.setUniqueContentOnly(messageNavigationPaneController.getUniqueOnlyMenu().isSelected());
		store.getFilteredMessageStore().addMessageFilter(uniqueContentOnlyFilter);
		messageNavigationPaneController.getUniqueOnlyMenu().setOnAction(new EventHandler<ActionEvent>()
		{			
			@Override
			public void handle(ActionEvent event)
			{
				uniqueContentOnlyFilter.setUniqueContentOnly(messageNavigationPaneController.getUniqueOnlyMenu().isSelected());
				store.getFilteredMessageStore().runFilter(uniqueContentOnlyFilter);
				eventManager.notifyMessageListChanged(store.getMessageList());
				eventManager.navigateToFirst(store);
			}
		});	
	}

	public void setReplayMode(final boolean value)
	{
		replayMode = value;
	}
	
	public void setFormatting(final Formatting formatting)
	{
		this.formatting = formatting;
	}
	
	public void setDetailedViewVisibility(final boolean visible)
	{
		messagePaneController.setDetailedViewVisibility(visible);
		messageNavigationPaneController.setDetailedViewVisibility(visible);
	}
	
	public void toggleDetailedViewVisibility()
	{
		messagePaneController.toggleDetailedViewVisibility();
		messageNavigationPaneController.toggleDetaileledViewVisibility();
	}
	
	@Override
	public void onClearTab(final ManagedMessageStoreWithFiltering<FormattedMqttMessage> subscription)
	{	
		messagePaneController.clear();
		messageNavigationPaneController.clear();
		
		// TODO: need to check this
		store.setAllShowValues(false);		
	}
	
	public void onSubscriptionStatusChanged(final MqttSubscription changedSubscription)
	{
		subscription = changedSubscription;
		updateContextMenu();
	}

	@FXML
	public void formatWholeMessage()
	{
		store.setFormatter((FormatterDetails) wholeMessageFormat.getSelectedToggle().getUserData());
	
		eventManager.notifyFormatChanged(store);
	}
	
	@FXML
	public void formatSelection()
	{
		final FormatterDetails messageFormat = (FormatterDetails) selectionFormat.getSelectedToggle().getUserData();
		
		messagePaneController.formatSelection(messageFormat);
	}

	public void updateMinHeights()
	{
		if (summaryTitledPane.isExpanded())
		{
			topicFilterBox.setVisible(true);
			summaryTitledPane.setMinHeight(MIN_EXPANDED_SUMMARY_PANE_HEIGHT);
		}
		else
		{
			topicFilterBox.setVisible(false);
			summaryTitledPane.setMinHeight(MIN_COLLAPSED_SUMMARY_PANE_HEIGHT);
			splitPane.setDividerPosition(0, 0.95);
		}
	}
	
	public static double updateTitleWidth(final TitledPane titledPane, final AnchorPane paneTitle, final int margin)
	{
		double titledPaneWidth = titledPane.getWidth();
		
		if (titledPane.getScene() != null)			
		{
			if (titledPane.getScene().getWidth() < titledPaneWidth)
			{
				titledPaneWidth = titledPane.getScene().getWidth();
			}
		}
		paneTitle.setPrefWidth(titledPaneWidth - margin);				
		paneTitle.setMaxWidth(titledPaneWidth - margin);
		
		return titledPaneWidth;
	}

	public void setStore(final ManagedMessageStoreWithFiltering<FormattedMqttMessage> store)
	{
		this.store = store;
	}
	
	public void setEventManager(final EventManager<FormattedMqttMessage> eventManager)
	{
		this.eventManager = eventManager;
	}

	/**
	 * 
	 * Sets the subscription tab for which this controller is.
	 * 
	 * @param tab The tab for which this controller is
	 */
	public void setTab(final Tab tab)
	{
		this.tab = tab;
	}

	public void setConnectionProperties(RuntimeConnectionProperties connectionProperties)
	{
		this.connectionProperties = connectionProperties;		
	}

	public Tab getTab()
	{
		return tab;
	}
	
	public void setConnectionController(final ConnectionController connectionController)
	{
		this.connectionController = connectionController;
	}
	
	public void updateContextMenu()
	{
		if (subscription != null)
		{
			SubscriptionManager.updateSubscriptionTabContextMenu(tab, subscription);
		}
	}
	

	private Window getParentWindow()
	{
		return tab.getTabPane().getScene().getWindow();
	}
	
	@FXML
	public void showSearchWindow()
	{
		if (searchStage == null)
		{
			// Create the search window controller
			final FXMLLoader searchLoader = FxmlUtils.createFxmlLoaderForProjectFile("SearchWindow.fxml");
			AnchorPane searchWindow = FxmlUtils.loadAnchorPane(searchLoader);
			searchWindowController = (SearchWindowController) searchLoader.getController();
			searchWindowController.setStore(store);
			searchWindowController.setSubscription(subscription);
			searchWindowController.setConnection(connectionController.getConnection());
			searchWindowController.setSubscriptionName(subscription != null ? subscription.getTopic() : SubscriptionManager.ALL_SUBSCRIPTIONS_TAB_TITLE);
			searchWindowController.setEventManager(eventManager);
			searchWindowController.setConfingurationManager(configurationManager);
			searchWindowController.setFormattingManager(formattingManager);
			searchWindowController.setConnectionController(connectionController);
			
			eventManager.registerMessageAddedObserver(searchWindowController, store.getMessageList());
			eventManager.registerMessageRemovedObserver(searchWindowController, store.getMessageList());
			eventManager.registerMessageListChangedObserver(searchWindowController, store.getMessageList());
					
			// Set scene width, height and style
			final Scene scene = new Scene(searchWindow, SearchWindowController.WIDTH, SearchWindowController.HEIGHT);
			scene.getStylesheets().addAll(tab.getTabPane().getScene().getStylesheets());
			
			searchStage = new Stage();
			searchStage.initModality(Modality.NONE);
			searchStage.initOwner(getParentWindow());
			searchStage.setScene(scene);
			
			searchWindowController.init();
		}

		if (!searchStage.isShowing())
		{
			searchStage.show();
			searchStage.setOnCloseRequest(new EventHandler<WindowEvent>(){

				@Override
				public void handle(WindowEvent event)
				{
					searchButton.setSelected(false);
					searchWindowController.handleClose();
				}				
			});
		}		
		else
		{
			searchStage.close();
		}			
	}

	public void updateSubscriptionStats()
	{
		final int topicCount = store.getNonFilteredMessageList().getTopicSummary().getObservableMessagesPerTopic().size();
		final int filteredTopicCount = getSummaryTablePaneController().getFilteredDataSize();
		final int messageCount = store.getNonFilteredMessageList().getMessages().size(); 
		
		final String filteredTopics = topicCount != filteredTopicCount ? ("showing " + filteredTopicCount + "/") : "";
		final String topicCountText = filteredTopics + (topicCount == 1 ? "1 topic" : topicCount + " topics");
		final String messageCountText = messageCount == 1 ? "1 message" : messageCount + " messages";
		
		Double avg5sec = 0.0;
		Double avg30sec = 0.0;
		Double avg300sec = 0.0;
		
		if (subscription == null)
		{
			avg5sec = StatisticsManager.getMessagesReceived(connectionProperties.getId(), 5).overallCount;
			avg30sec = StatisticsManager.getMessagesReceived(connectionProperties.getId(), 30).overallCount;
			avg300sec = StatisticsManager.getMessagesReceived(connectionProperties.getId(), 300).overallCount;
			
			statsLabel.setText(String.format(SUMMARY_PANE_STATS_FORMAT, 
				topicCountText,
				messageCountText,
				avg5sec,
				avg30sec,
				avg300sec));						
		}
		else
		{
			avg5sec = StatisticsManager.getMessagesReceived(connectionProperties.getId(), 5).messageCount.get(subscription.getTopic());
			avg30sec = StatisticsManager.getMessagesReceived(connectionProperties.getId(), 30).messageCount.get(subscription.getTopic());
			avg300sec = StatisticsManager.getMessagesReceived(connectionProperties.getId(), 300).messageCount.get(subscription.getTopic());
			
			avg5sec = avg5sec == null ? 0 : avg5sec;
			avg30sec = avg30sec == null ? 0 : avg30sec;
			avg300sec = avg300sec == null ? 0 : avg300sec;
			
			statsLabel.setText(String.format(SUMMARY_PANE_STATS_FORMAT, 
					topicCountText,
					messageCountText,
					avg5sec == null ? 0 : avg5sec, 
					avg30sec == null ? 0 : avg30sec, 
					avg300sec == null ? 0 : avg300sec));
		}
		
		final FormattedMqttMessage avg5message = new FormattedMqttMessage(0, AVG5_TOPIC, 
				new MqttMessage(ConversionUtils.stringToArray(String.valueOf(avg5sec))), null);
		final FormattedMqttMessage avg30message = new FormattedMqttMessage(0, AVG30_TOPIC, 
				new MqttMessage(ConversionUtils.stringToArray(String.valueOf(avg30sec))), null);
		final FormattedMqttMessage avg300message = new FormattedMqttMessage(0, AVG300_TOPIC, 
				new MqttMessage(ConversionUtils.stringToArray(String.valueOf(avg300sec))), null);
		
		statsHistory.storeMessage(avg5message);
		statsHistory.storeMessage(avg30message);
		statsHistory.storeMessage(avg300message);
		eventManager.notifyMessageAdded(
				Arrays.asList(new BrowseReceivedMessageEvent<FormattedMqttMessage>(statsHistory.getMessageList(), avg5message)),
				statsHistory.getMessageList());
		eventManager.notifyMessageAdded(
				Arrays.asList(new BrowseReceivedMessageEvent<FormattedMqttMessage>(statsHistory.getMessageList(), avg30message)),
				statsHistory.getMessageList());
		eventManager.notifyMessageAdded(
				Arrays.asList(new BrowseReceivedMessageEvent<FormattedMqttMessage>(statsHistory.getMessageList(), avg300message)),
				statsHistory.getMessageList());
	}

	/**
	 * @return the statsHistory
	 */
	public BasicMessageStoreWithSummary<FormattedMqttMessage> getStatsHistory()
	{
		return statsHistory;
	}

	public void toggleMessagePayloadSize(final boolean resize)
	{
		if (resize)
		{
			messagePane.setMaxHeight(Double.MAX_VALUE);
		}
		else
		{			
			messagePane.setMaxHeight(85);
		}
		
		if (searchWindowController != null)
		{
			searchWindowController.toggleMessagePayloadSize(resize);
		}
	}
	
	@FXML
	private void copyMessageToClipboard()
	{
		messageNavigationPaneController.copyMessageToClipboard();
	}
	
	@FXML
	private void copyMessagesToClipboard()	
	{
		messageNavigationPaneController.copyMessagesToClipboard();
	}
	
	@FXML
	private void copyMessageToFile()
	{
		messageNavigationPaneController.copyMessageToFile();
	}
	
	@FXML
	private void copyMessagesToFile()
	{
		messageNavigationPaneController.copyMessagesToFile();
	}
	
	@FXML
	private void copyBrowsedTopic()
	{
		messageNavigationPaneController.copyMessageTopicToClipboard();
	}
	
	@FXML
	private void copyBrowsedTopics()
	{
		
		UiUtils.copyToClipboard(MessageLogUtils.getAllTopicsAsString(
				store.getFilteredMessageStore().getBrowsedTopics()));
	}
	
	@FXML
	private void copyFilteredTopics()
	{
		UiUtils.copyToClipboard(MessageLogUtils.getAllTopicsAsString(
				getSummaryTablePaneController().getShownTopics()));
	}
	
	@FXML
	private void copyAllTopics()
	{
		UiUtils.copyToClipboard(MessageLogUtils.getAllTopicsAsString(
				store.getAllTopics()));
	}
	
	@FXML
	private void copyMessageToBinaryFile()
	{
		messageNavigationPaneController.copyMessageToBinaryFile();
	}
	
	public MqttSubscription getSubscription()
	{
		return subscription;
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
		// Nothing to do here...	
	}

	/**
	 * Gets the summary table pane controller.
	 * 
	 * @return the summaryTablePaneController
	 */
	public SubscriptionSummaryTableController getSummaryTablePaneController()
	{
		return summaryTablePaneController;
	}
	
	/**
	 * Gets the connection controller.
	 * 
	 * @return the connectionController
	 */
	public ConnectionController getConnectionController()
	{
		return connectionController;
	}
	
	public void setConfingurationManager(final ConfigurationManager configurationManager)
	{
		this.configurationManager = configurationManager;
	}

	public Scene getScene()
	{
		return splitPane.getScene();
	}

	/**
	 * @param formattingManager the formattingManager to set
	 */
	public void setFormattingManager(FormattingManager formattingManager)
	{
		this.formattingManager = formattingManager;
	}
}
