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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Slider;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.connectivity.MqttRuntimeConnectionProperties;
import pl.baczkowicz.mqttspy.connectivity.MqttSubscription;
import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.ui.MqttSubscriptionViewManager;
import pl.baczkowicz.mqttspy.ui.MqttViewManager;
import pl.baczkowicz.mqttspy.ui.events.SubscriptionStatusChangeEvent;
import pl.baczkowicz.mqttspy.ui.layout.MessageBrowserLayout;
import pl.baczkowicz.mqttspy.ui.messagelog.MqttMessageAuditUtils;
import pl.baczkowicz.spy.common.generated.FormatterDetails;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.formatting.FormattingManager;
import pl.baczkowicz.spy.formatting.FormattingUtils;
import pl.baczkowicz.spy.ui.configuration.IConfigurationManager;
import pl.baczkowicz.spy.ui.controllers.MessageNavigationController;
import pl.baczkowicz.spy.ui.events.ClearTabEvent;
import pl.baczkowicz.spy.ui.events.FormattersChangedEvent;
import pl.baczkowicz.spy.ui.events.MessageAddedEvent;
import pl.baczkowicz.spy.ui.events.MessageFormatChangeEvent;
import pl.baczkowicz.spy.ui.events.MessageIndexChangeEvent;
import pl.baczkowicz.spy.ui.events.MessageIndexIncrementEvent;
import pl.baczkowicz.spy.ui.events.MessageIndexToFirstEvent;
import pl.baczkowicz.spy.ui.events.MessageListChangedEvent;
import pl.baczkowicz.spy.ui.events.MessageRemovedEvent;
import pl.baczkowicz.spy.ui.events.ShowFormattersWindowEvent;
import pl.baczkowicz.spy.ui.events.queuable.ui.BrowseReceivedMessageEvent;
import pl.baczkowicz.spy.ui.panes.TabController;
import pl.baczkowicz.spy.ui.panes.TabStatus;
import pl.baczkowicz.spy.ui.search.UniqueContentOnlyFilter;
import pl.baczkowicz.spy.ui.stats.StatisticsManager;
import pl.baczkowicz.spy.ui.storage.BasicMessageStoreWithSummary;
import pl.baczkowicz.spy.ui.storage.ManagedMessageStoreWithFiltering;
import pl.baczkowicz.spy.ui.threading.SimpleRunLaterExecutor;
import pl.baczkowicz.spy.ui.utils.FxmlUtils;
import pl.baczkowicz.spy.ui.utils.UiUtils;
import pl.baczkowicz.spy.utils.ConversionUtils;
import pl.baczkowicz.spy.utils.TimeUtils;

/**
 * Controller for the subscription tab.
 */
public class SubscriptionController implements Initializable, TabController
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
	private MqttMessageController messagePaneController;
	
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
	private ToggleGroup messageBrowserLayout;

	@FXML
	private MenuButton formattingMenuButton;

	@FXML
	private CheckMenuItem uniqueOnlyMenu;
	
	@FXML
	private Menu customFormatterMenu;
	
	@FXML
	private ToggleButton searchButton;
	
	@FXML
	private ScrollBar messageIndexScrollBar;
	
	@FXML
	private VBox messagesPane;
	
	@FXML
	private Slider messageCountSlider;
	
	private final List<MqttMessageController> messageControllers = new ArrayList<>();

	private ManagedMessageStoreWithFiltering<FormattedMqttMessage> store; 
	
	private BasicMessageStoreWithSummary<FormattedMqttMessage> statsHistory;

	private Tab tab;

	private MqttRuntimeConnectionProperties connectionProperties;

	private MqttSubscription subscription;

	private Stage searchStage;

	private SearchWindowController searchWindowController;

	private IKBus eventBus;

	private MqttConnectionController connectionController;

	private Label statsLabel;

	private AnchorPane paneTitle;

	private TextField searchBox;

	private HBox topicFilterBox;

	private boolean replayMode;

	private List<FormatterDetails> formatters;

	private UniqueContentOnlyFilter<FormattedMqttMessage> uniqueContentOnlyFilter;

	private TabStatus tabStatus;

	private HBox titleBox;

	private IConfigurationManager configurationManager;

	private FormattingManager formattingManager;
	
	private long messageIndexLastChangedWithScrollBar;
	
	private int maxMessagesDisplayed = 1;
	
	private int messagesDisplayed = 1;

	private Map<MqttMessageController, AnchorPane> messagePanes = new HashMap<>();

	private boolean detailedView;

	private Orientation orientation = Orientation.VERTICAL;

	private double summaryTitledPaneTargetWidth;

	public void initialize(URL location, ResourceBundle resources)
	{			
		statsLabel = new Label();
		topicFilterBox = new HBox();
		topicFilterBox.setPadding(new Insets(0, 0, 0, 0));
		
		// Set up formatters toggle
		final List<FormatterDetails> formatters = FormattingUtils.createBaseFormatters();
		formatters.addAll(FormattingManager.createDefaultScriptFormatters());
		
		for (int i = 0; i < formatters.size(); i++)
		{
			wholeMessageFormat.getToggles().get(i).setUserData(formatters.get(i));
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
		
		// Set up layout toggle
		messageBrowserLayout.getToggles().get(0).setUserData(MessageBrowserLayout.SINGLE_TOP);
		messageBrowserLayout.getToggles().get(1).setUserData(MessageBrowserLayout.SINGLE_BOTTOM);
		messageBrowserLayout.getToggles().get(2).setUserData(MessageBrowserLayout.SINGLE_RIGHT);
		messageBrowserLayout.getToggles().get(3).setUserData(MessageBrowserLayout.MULTI_TOP);
		messageBrowserLayout.getToggles().get(4).setUserData(MessageBrowserLayout.MULTI_BOTTOM);
		messageBrowserLayout.getToggles().get(5).setUserData(MessageBrowserLayout.MULTI_RIGHT);
		
		messageBrowserLayout.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
		{
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue)
			{
				if (messageBrowserLayout.getSelectedToggle() != null)
				{
					updateLayout((MessageBrowserLayout) newValue.getUserData());
				}
			}
		});
		
		final Object controller = this;		
		
		messageIndexScrollBar.valueProperty().addListener(new ChangeListener<Number>()
		{			
			@Override
			public void changed(final ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				// Check and store the time to avoid cyclic updates
				if (store.getMessages().size() > 0 && (TimeUtils.getMonotonicTime() > messageIndexLastChangedWithScrollBar + 50))
				{					
					// logger.debug("Setting message index from {} to {}", oldValue, newValue);
					eventBus.publish(new MessageIndexChangeEvent(newValue.intValue(), store, controller));
					messageIndexLastChangedWithScrollBar = TimeUtils.getMonotonicTime();
				}
			}
		});
		
		messageCountSlider.valueProperty().addListener(new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{		
				int newNumber = newValue.intValue();
				
				if (newNumber >= 1)
				{				
					messageCountSlider.setValue(newNumber);
					
					updateMessagesDisplayed(newNumber);
					
					// TODO: update scroll bar
					
					// Update message index field range
					messageNavigationPaneController.updateRange(messagesDisplayed);
				}
				else
				{
					// If we don't do this, the slider looses the UI value blob
					messageCountSlider.setValue(1);
				}
			}
		});
		
		messagesPane.heightProperty().addListener(new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{								
				final int prevMaxMessagesDisplayed = maxMessagesDisplayed;
				
				// Added the -10 margin for smoother transition
				maxMessagesDisplayed = (int) Math.floor((newValue.intValue() - 10) / 85);
				if (maxMessagesDisplayed <= 0)
				{
					maxMessagesDisplayed = 1;
				}
				
				// logger.debug("Height = {}, prev/max messages = {}/{}", newValue.intValue(), prevMaxMessagesDisplayed, maxMessagesDisplayed);
				
				messageCountSlider.setMax(maxMessagesDisplayed);
				
				// Auto scale up/down
				if (messageCountSlider.isVisible() && ((int) messageCountSlider.getValue() == prevMaxMessagesDisplayed))
				{
					messageCountSlider.setValue(maxMessagesDisplayed);
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
		
		messageControllers.add(messagePaneController);
		messagePanes.put(messagePaneController, messagePane);	

		updateMinHeights();		
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
		
		eventBus.subscribeWithFilterOnly(this, this::onClearTab, ClearTabEvent.class, store);
		
		getSummaryTablePaneController().setStore(store);
		getSummaryTablePaneController().setConnectionController(connectionController);
		getSummaryTablePaneController().setEventBus(eventBus);
		getSummaryTablePaneController().init();
		
		initialiseMessagePaneController(messagePaneController);
		
		messageNavigationPaneController.setMessageAuditUtils(new MqttMessageAuditUtils());
		messageNavigationPaneController.setStore(store);
		messageNavigationPaneController.setEventBus(eventBus);
		messageNavigationPaneController.init();		
		
		// The subscription pane's message browser wants to know about show first, index change and update index events
		eventBus.subscribe(this, this::onMessageIndexChange, MessageIndexChangeEvent.class, new SimpleRunLaterExecutor(), store);
		
		eventBus.subscribe(messageNavigationPaneController, messageNavigationPaneController::onMessageIndexChange, 
				MessageIndexChangeEvent.class, new SimpleRunLaterExecutor(), store);
		eventBus.subscribe(messageNavigationPaneController, messageNavigationPaneController::onNavigateToFirst, 
				MessageIndexToFirstEvent.class, new SimpleRunLaterExecutor(), store);
		eventBus.subscribe(messageNavigationPaneController, messageNavigationPaneController::onMessageIndexIncrement, 
				MessageIndexIncrementEvent.class, new SimpleRunLaterExecutor(), store);
		eventBus.subscribe(this, this::handleFormattersChange, FormattersChangedEvent.class);
		
		eventBus.subscribeWithFilterOnly(messageNavigationPaneController, messageNavigationPaneController::onMessageAdded, MessageAddedEvent.class, store.getMessageList());		
		eventBus.subscribeWithFilterOnly(messageNavigationPaneController, messageNavigationPaneController::onMessageRemoved, MessageRemovedEvent.class, store.getMessageList());
		
		populateFormatters();
		
		paneTitle = new AnchorPane();
		paneTitle.setPadding(new Insets(0, 0, 0, 0));
		paneTitle.setMaxWidth(Double.MAX_VALUE);
				
		searchBox = new TextField();
		searchBox.getStyleClass().add("small-font");
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
		uniqueContentOnlyFilter.setUniqueContentOnly(uniqueOnlyMenu.isSelected());
		store.getFilteredMessageStore().addMessageFilter(uniqueContentOnlyFilter);
		uniqueOnlyMenu.setOnAction(new EventHandler<ActionEvent>()
		{			
			@Override
			public void handle(ActionEvent event)
			{
				uniqueContentOnlyFilter.setUniqueContentOnly(uniqueOnlyMenu.isSelected());
				store.getFilteredMessageStore().runFilter(uniqueContentOnlyFilter);
				eventBus.publish(new MessageListChangedEvent(store.getMessageList()));
				eventBus.publish(new MessageIndexToFirstEvent(store));
			}
		});	
		
		resetScrollBar();
	}
	
	private void initialiseMessagePaneController(final MqttMessageController controller)
	{
		controller.setStore(store);
		controller.setConfingurationManager(configurationManager);
		controller.setFormattingManager(formattingManager);
		controller.init();
		controller.setViewVisibility(detailedView);
		
		// The search pane's message browser wants to know about changing indices and format
		eventBus.subscribe(controller, controller::onMessageIndexChange, 
				MessageIndexChangeEvent.class, new SimpleRunLaterExecutor(), store);
		eventBus.subscribe(controller, controller::onFormatChange, MessageFormatChangeEvent.class, 
				new SimpleRunLaterExecutor(), store);
	}

	private void updateMessagesDisplayed(int newValue)
	{
		// logger.debug("Updating messages displayed. New val = {}", newValue);
		while (newValue > messageControllers.size())
		{
			final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("MessagePane.fxml");
			final AnchorPane messagePane = FxmlUtils.loadAnchorPane(loader);
			
			final MqttMessageController controller = (MqttMessageController) loader.getController();
			initialiseMessagePaneController(controller); 
			
			// Add X to displayed message index
			controller.setMessageIndexOfset(messageControllers.size());
			
			messageControllers.add(controller);
			messagePanes.put(controller, messagePane);			
			VBox.setVgrow(messagePane, Priority.ALWAYS);
		}
		
		while (messagesDisplayed > newValue)
		{
			messagesPane.getChildren().remove(messagesPane.getChildren().size() - 1);
			messagesDisplayed--;
		}		
		
		while (messagesDisplayed < newValue)
		{
			final MqttMessageController mc = messageControllers.get(messagesDisplayed);
			
			if (mc != null)
			{
				messagesPane.getChildren().add(messagePanes.get(mc));
				messagesDisplayed++;
			}			
		}
		
		// logger.debug("Updating messages displayed. New val = {}, msgs disp = {}", newValue, messagesDisplayed);
		
		// Refresh all messages to populate all panes
		eventBus.publish(new MessageIndexChangeEvent((int) messageIndexScrollBar.getValue(), store, this));
	}
	
	private void updateLayout(final MessageBrowserLayout layout)
	{
		boolean summaryPaneFirst = true;
		boolean multi = true;		
		Orientation newOrientation = Orientation.VERTICAL;
		
		switch (layout)
		{
			case MULTI_BOTTOM:
				break;
			case MULTI_RIGHT:
				newOrientation = Orientation.HORIZONTAL;
				break;
			case MULTI_TOP:
				summaryPaneFirst = false;
				break;
			case SINGLE_BOTTOM:
				multi = false;
				break;
			case SINGLE_RIGHT:
				newOrientation = Orientation.HORIZONTAL;
				multi = false;
				break;
			case SINGLE_TOP:
				summaryPaneFirst = false;
				multi = false;
				break;
			default:
				break;		
		}		
		
		if (!newOrientation.equals(orientation) && newOrientation.equals(Orientation.HORIZONTAL))
		{
			summaryTitledPaneTargetWidth = summaryTitledPane.getWidth() * 0.65;
			splitPane.setDividerPositions(0.65);
		}
		
		splitPane.setOrientation(newOrientation);
		
		if (!newOrientation.equals(orientation) && newOrientation.equals(Orientation.HORIZONTAL))
		{
			Platform.runLater(() -> 
			{
				logger.debug("layout => updating width with target = {}", summaryTitledPaneTargetWidth);
				updateWidth(null);
			});
		}
		
		orientation = newOrientation;
		
		// If not in multi mode, reset message count to 1
		if (!multi)
		{
			messageCountSlider.setValue(1);
			messageCountSlider.setMax(1);
		}
		messageCountSlider.setVisible(multi);
		
		// TODO: resize summaryTitledPane quicker
		
		if (splitPane.getItems().size() > 0)
		{
			if (summaryPaneFirst && splitPane.getItems().get(1).equals(summaryTitledPane))
			{
				splitPane.getItems().remove(summaryTitledPane);
				splitPane.getItems().add(0, summaryTitledPane);
			}
			else if (!summaryPaneFirst && splitPane.getItems().get(0).equals(summaryTitledPane))
			{
				splitPane.getItems().remove(summaryTitledPane);
				splitPane.getItems().add(1, summaryTitledPane);
			}
		}
	}
	
	private ChangeListener<Number> createPaneTitleWidthListener()
	{
		return new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{		
				final double oldV = (double) oldValue;
				final double newV = (double) newValue;
				
				final Double diff = oldV - newV;
				
				// logger.debug("Using target width of {}; requested {}; previously {}", diff, newValue, oldValue);				
								
				Platform.runLater(() -> 
				{
					// logger.debug("listener => updating width with target = {}", diff);
					updateWidth(diff);
				});
				
			}
		};
	}
	
	private void updateWidth(final Double target)
	{
		// logger.debug("1 pane W={}, title X={}, W={}, target = {}", summaryTitledPane.getWidth(), paneTitle.getLayoutX(), paneTitle.getWidth(), target);
		
		final double absoluteSearchBoxX = searchBox.getLayoutX() + topicFilterBox.getLayoutX() + titleBox.getLayoutX();
		final double titledPaneWidth = MqttViewManager.updateTitleWidth(summaryTitledPane, paneTitle, MqttViewManager.TITLE_MARGIN, target);
		
		// logger.debug("2 New width = {}, absolute X = {}, stats label pos = {}, width = {}", titledPaneWidth, absoluteSearchBoxX, statsLabel.getLayoutX(), statsLabel.getWidth());
		AnchorPane.setRightAnchor(statsLabel, 5.0);
		// logger.debug("3 New width = {}, absolute X = {}, stats label pos = {}, width = {}", titledPaneWidth, absoluteSearchBoxX, statsLabel.getLayoutX(), statsLabel.getWidth());
		
								
		searchBox.setPrefWidth(titledPaneWidth - absoluteSearchBoxX - statsLabel.getWidth() - 100);
		
		// logger.debug("4 pane W={}, title X={}, W={}", summaryTitledPane.getWidth(), paneTitle.getLayoutX(), paneTitle.getWidth());
	}
	
	private void resetScrollBar()
	{
		messageIndexScrollBar.setMin(1);
		messageIndexScrollBar.setValue(1);
		messageIndexScrollBar.setMax(1);
		
		// TODO: this will need to be adjusted once multiple messages are displayed
		messageIndexScrollBar.setVisibleAmount(1);		
		messageIndexScrollBar.setDisable(true);
	}
	
	public void onMessageIndexChange(final MessageIndexChangeEvent event)
	{
		// Make sure this is not from itself
		if (event.getDispatcher() == this)
		{
			return;
		}
		
		if (event.getIndex() > 0)
		{
			messageIndexScrollBar.setValue(event.getIndex());
			messageIndexScrollBar.setMax(store.getMessages().size());
			messageIndexScrollBar.setDisable(false);
			
			// logger.debug("Setting max to {}", store.getMessages().size());
		}
		else
		{
			resetScrollBar();
		}
	}
	
	public void onClose()
	{
		eventBus.unsubscribeConsumer(this, FormattersChangedEvent.class);
	}
	
	public void handleFormattersChange(final FormattersChangedEvent event)	
	{
		populateFormatters();
	}
	
	public void populateFormatters()
	{
		if (formatters.size() > 0)
		{
			customFormatterMenu.setDisable(false);
		}
					
		customFormatterMenu.getItems().clear();
		for (final FormatterDetails formatter : formatters)
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
	}

	public void setReplayMode(final boolean value)
	{
		replayMode = value;
	}
	
	public void setFormatters(final List<FormatterDetails> formatters)
	{
		this.formatters = formatters;
	}
	
	public void setViewVisibility(final boolean detailedView, final boolean basicView)
	{
		this.detailedView = detailedView;
		
		// TODO: add toggle menu to layout settings - or make the perspective change that
		for (final MqttMessageController controller : messageControllers)
		{
			controller.setViewVisibility(detailedView);
		}

		messageNavigationPaneController.setViewVisibility(detailedView);
		
		if (basicView && splitPane.getItems().contains(summaryTitledPane))
		{
			splitPane.getItems().remove(summaryTitledPane);			
		}
		else if (!basicView && !splitPane.getItems().contains(summaryTitledPane))
		{
			splitPane.getItems().add(summaryTitledPane);
		}
	}
	
	public void toggleDetailedViewVisibility()
	{
		detailedView = !detailedView;
		
		for (final MqttMessageController controller : messageControllers)
		{
			controller.toggleDetailedViewVisibility();
		}
		
		messageNavigationPaneController.toggleDetaileledViewVisibility();
	}
	
	public void onClearTab(final ClearTabEvent event)
	{	
		for (final MqttMessageController controller : messageControllers)
		{
			controller.clear();
		}
		
		messageNavigationPaneController.clear();
		
		resetScrollBar();
		
		// TODO: need to check this
		store.setAllShowValues(false);		
	}
	
	public void onSubscriptionStatusChanged(final SubscriptionStatusChangeEvent event)
	{
		subscription = event.getChangedSubscription();
		updateContextMenu();
	}

	@FXML
	public void formatWholeMessage()
	{
		store.setFormatter((FormatterDetails) wholeMessageFormat.getSelectedToggle().getUserData());
	
		logger.debug("Format changed to {}", store.getFormatter().getName());
		eventBus.publish(new MessageFormatChangeEvent(store));
	}
	
	@FXML
	public void editFormatters()
	{
		eventBus.publish(new ShowFormattersWindowEvent(getParentWindow(), true));
		
		// TODO: update formatters
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

	public void setStore(final ManagedMessageStoreWithFiltering<FormattedMqttMessage> store)
	{
		this.store = store;
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

	public void setConnectionProperties(MqttRuntimeConnectionProperties connectionProperties)
	{
		this.connectionProperties = connectionProperties;		
	}

	public Tab getTab()
	{
		return tab;
	}
	
	public void setConnectionController(final MqttConnectionController connectionController)
	{
		this.connectionController = connectionController;
	}
	
	public void updateContextMenu()
	{
		if (subscription != null)
		{
			MqttSubscriptionViewManager.updateSubscriptionTabContextMenu(tab, subscription);
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
			searchWindowController.setSubscriptionName(subscription != null ? subscription.getTopic() : MqttSubscriptionViewManager.ALL_SUBSCRIPTIONS_TAB_TITLE);
			searchWindowController.setEventBus(eventBus);
			searchWindowController.setConfingurationManager(configurationManager);
			searchWindowController.setFormattingManager(formattingManager);
			searchWindowController.setConnectionController(connectionController);
			
			eventBus.subscribeWithFilterOnly(searchWindowController, searchWindowController::onMessageAdded, MessageAddedEvent.class, store.getMessageList());		
			eventBus.subscribeWithFilterOnly(searchWindowController, searchWindowController::onMessageRemoved, MessageRemovedEvent.class, store.getMessageList());
			eventBus.subscribeWithFilterOnly(searchWindowController, searchWindowController::onMessageListChanged, MessageListChangedEvent.class, store.getMessageList());
					
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

	@SuppressWarnings("unchecked")
	public void updateSubscriptionStats()
	{
		// logger.debug("Stats label position = {}, width = {}", statsLabel.getLayoutX(), statsLabel.getWidth());
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
		eventBus.publish(new MessageAddedEvent(
				Arrays.asList(new BrowseReceivedMessageEvent<FormattedMqttMessage>(statsHistory.getMessageList(), avg5message)),
				statsHistory.getMessageList()));
		eventBus.publish(new MessageAddedEvent(
				Arrays.asList(new BrowseReceivedMessageEvent<FormattedMqttMessage>(statsHistory.getMessageList(), avg30message)),
				statsHistory.getMessageList()));
		eventBus.publish(new MessageAddedEvent(
				Arrays.asList(new BrowseReceivedMessageEvent<FormattedMqttMessage>(statsHistory.getMessageList(), avg300message)),
				statsHistory.getMessageList()));
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
		
		UiUtils.copyToClipboard(MqttMessageAuditUtils.getAllTopicsAsString(
				store.getFilteredMessageStore().getBrowsedTopics()));
	}
	
	@FXML
	private void copyFilteredTopics()
	{
		UiUtils.copyToClipboard(MqttMessageAuditUtils.getAllTopicsAsString(
				getSummaryTablePaneController().getShownTopics()));
	}
	
	@FXML
	private void copyAllTopics()
	{
		UiUtils.copyToClipboard(MqttMessageAuditUtils.getAllTopicsAsString(
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
	public MqttConnectionController getConnectionController()
	{
		return connectionController;
	}
	
	public void setConfingurationManager(final IConfigurationManager configurationManager)
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
	
	public void setEventBus(final IKBus eventBus)
	{
		this.eventBus = eventBus;
	}
}
