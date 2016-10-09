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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Predicate;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.configuration.generated.TabbedSubscriptionDetails;
import pl.baczkowicz.mqttspy.connectivity.BaseMqttSubscription;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.connectivity.MqttSubscription;
import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.ui.charts.ChartFactory;
import pl.baczkowicz.spy.ui.charts.ChartMode;
import pl.baczkowicz.spy.ui.events.MessageIndexToFirstEvent;
import pl.baczkowicz.spy.ui.events.MessageListChangedEvent;
import pl.baczkowicz.spy.ui.properties.SubscriptionTopicSummaryProperties;
import pl.baczkowicz.spy.ui.storage.ManagedMessageStoreWithFiltering;
import pl.baczkowicz.spy.ui.utils.DialogFactory;
import pl.baczkowicz.spy.ui.utils.StylingUtils;
import pl.baczkowicz.spy.ui.utils.UiUtils;

/**
 * Controller for the subscription summary table.
 */
@SuppressWarnings("rawtypes")
public class SubscriptionSummaryTableController implements Initializable
{
	private static final int CHART_TOPIC_COUNT = 10;

	private final static Logger logger = LoggerFactory.getLogger(SubscriptionSummaryTableController.class);

	private ManagedMessageStoreWithFiltering<FormattedMqttMessage> store; 
	
	@FXML
	private TableView<SubscriptionTopicSummaryProperties> filterTable;

	@FXML
	private TableColumn<SubscriptionTopicSummaryProperties, Boolean> showColumn;

	@FXML
	private TableColumn<SubscriptionTopicSummaryProperties, String> topicColumn;
	
	@FXML
	private TableColumn<SubscriptionTopicSummaryProperties, String> contentColumn;

	@FXML
	private TableColumn<SubscriptionTopicSummaryProperties, Integer> messageCountColumn;

	@FXML
	private TableColumn<SubscriptionTopicSummaryProperties, String> lastReceivedColumn;

	private FilteredList<SubscriptionTopicSummaryProperties<FormattedMqttMessage>> filteredData;
	
	private MqttConnectionController connectionController;
	
	private IKBus eventBus;

	private Menu filteredTopicsMenu;

	private ObservableList<SubscriptionTopicSummaryProperties<FormattedMqttMessage>> nonFilteredData;
	
	private Set<String> shownTopics = new HashSet<>();
	
	public void initialize(URL location, ResourceBundle resources)
	{				
		// Table
		showColumn.setCellValueFactory(new PropertyValueFactory<SubscriptionTopicSummaryProperties, Boolean>(
				"show"));
		showColumn
				.setCellFactory(new Callback<TableColumn<SubscriptionTopicSummaryProperties, Boolean>, TableCell<SubscriptionTopicSummaryProperties, Boolean>>()
		{
			public TableCell<SubscriptionTopicSummaryProperties, Boolean> call(
					TableColumn<SubscriptionTopicSummaryProperties, Boolean> param)
			{
				final CheckBoxTableCell<SubscriptionTopicSummaryProperties, Boolean> cell = new CheckBoxTableCell<SubscriptionTopicSummaryProperties, Boolean>()
				{
					@Override
					public void updateItem(final Boolean checked, boolean empty)
					{
						super.updateItem(checked, empty);
						if (!isEmpty() && checked != null && this.getTableRow() != null && this.getTableRow().getItem() != null && store != null)
						{
							changeShowProperty((SubscriptionTopicSummaryProperties) this.getTableRow().getItem(), checked);															
						}									
					}
				};
				cell.setAlignment(Pos.TOP_CENTER);
				
				return cell;
			}
		});

		topicColumn.setCellValueFactory(new PropertyValueFactory<SubscriptionTopicSummaryProperties, String>("topic"));

		contentColumn.setCellValueFactory(new PropertyValueFactory<SubscriptionTopicSummaryProperties, String>("lastReceivedPayloadShort"));
		contentColumn.setCellFactory(new Callback<TableColumn<SubscriptionTopicSummaryProperties, String>, TableCell<SubscriptionTopicSummaryProperties, String>>()
		{
			public TableCell<SubscriptionTopicSummaryProperties, String> call(
					TableColumn<SubscriptionTopicSummaryProperties, String> param)
			{
				final TableCell<SubscriptionTopicSummaryProperties, String> cell = new TableCell<SubscriptionTopicSummaryProperties, String>()
				{
					@Override
					public void updateItem(String item, boolean empty)
					{
						super.updateItem(item, empty);
						if (!isEmpty())
						{								
							setText(item);
						}
						else
						{
							setText(null);
						}
					}
				};
				
				return cell;
			}
		});

		messageCountColumn.setCellValueFactory(new PropertyValueFactory<SubscriptionTopicSummaryProperties, Integer>("count"));
		messageCountColumn.setCellFactory(new Callback<TableColumn<SubscriptionTopicSummaryProperties, Integer>, TableCell<SubscriptionTopicSummaryProperties, Integer>>()
		{
			public TableCell<SubscriptionTopicSummaryProperties, Integer> call(
					TableColumn<SubscriptionTopicSummaryProperties, Integer> param)
			{
				final TableCell<SubscriptionTopicSummaryProperties, Integer> cell = new TableCell<SubscriptionTopicSummaryProperties, Integer>()
				{
					@Override
					public void updateItem(Integer item, boolean empty)
					{
						super.updateItem(item, empty);
						if (!isEmpty())
						{
							setText(item.toString());
						}
						else
						{
							setText(null);
						}
					}
				};
				cell.setAlignment(Pos.TOP_CENTER);
				
				return cell;
			}
		});

		lastReceivedColumn.setCellValueFactory(new PropertyValueFactory<SubscriptionTopicSummaryProperties, String>("lastReceivedTimestamp"));
		lastReceivedColumn.setCellFactory(new Callback<TableColumn<SubscriptionTopicSummaryProperties, String>, TableCell<SubscriptionTopicSummaryProperties, String>>()
		{
			public TableCell<SubscriptionTopicSummaryProperties, String> call(
					TableColumn<SubscriptionTopicSummaryProperties, String> param)
			{
				final TableCell<SubscriptionTopicSummaryProperties, String> cell = new TableCell<SubscriptionTopicSummaryProperties, String>()
				{
					@Override
					public void updateItem(String item, boolean empty)
					{
						super.updateItem(item, empty);
						if (!isEmpty())
						{
							setText(item.toString());
						}
						else
						{
							setText(null);
						}
					}
				};
				cell.setAlignment(Pos.TOP_CENTER);
				
				return cell;
			}
		});

		filterTable
		.setRowFactory(new Callback<TableView<SubscriptionTopicSummaryProperties>, TableRow<SubscriptionTopicSummaryProperties>>()
		{
			public TableRow<SubscriptionTopicSummaryProperties> call(
					TableView<SubscriptionTopicSummaryProperties> tableView)
			{
				final TableRow<SubscriptionTopicSummaryProperties> row = new TableRow<SubscriptionTopicSummaryProperties>()
				{
					@Override
					protected void updateItem(SubscriptionTopicSummaryProperties item, boolean empty)
					{
						super.updateItem(item, empty);											
						
						if (!isEmpty() && item.getSubscription() != null)
						{
							final BaseMqttSubscription subscription = connectionController.getConnection().getMqttSubscriptionForTopic(item.getSubscription());
						
							if (subscription instanceof MqttSubscription)
							{
								this.setStyle(StylingUtils.createBgRGBString(
									((MqttSubscription) subscription).getColor(), 
									getIndex() % 2 == 0 ? 0.8 : 0.6)
									+ " -fx-background-radius: 6; ");
							}
							else
							{
								this.setStyle(null);
							}
						}
						else
						{
							this.setStyle(null);
						}
					}
				};

				return row;
			}
		});				
	}
	
	private void changeShowProperty(final SubscriptionTopicSummaryProperties item, final boolean checked)
	{
		logger.trace("[{}] Show property changed; topic = {}, show value = {}", store.getName(), item.topicProperty().getValue(), checked);
		logger.trace("[{}] Store = {}, filtered store = {}", store.getName(), store.getNonFilteredMessageList(), store.getFilteredMessageStore().getMessageList());
		
		if (store.getFilteredMessageStore().updateTopicFilter(item.topicProperty().getValue(), checked))
		{
			// Wouldn't get updated properly if this is in the same thread 
			Platform.runLater(new Runnable()
			{
				@Override
				public void run()
				{
					eventBus.publish(new MessageIndexToFirstEvent(store));
					// eventManager.navigateToFirst(store);
					eventBus.publish(new MessageListChangedEvent(store.getMessageList()));
					// eventManager.notifyMessageListChanged(store.getMessageList());
				}											
			});
		}				
	}
	
	public void init()
	{		
		filterTable.setContextMenu(createTopicTableContextMenu(connectionController.getConnection()));		
		
		nonFilteredData = store.getNonFilteredMessageList().getTopicSummary().getObservableMessagesPerTopic();
		
		// Create filtered data set
		filteredData = new FilteredList<>(nonFilteredData);
		
		// Create sortable data set
		final SortedList<SubscriptionTopicSummaryProperties> sortedData = new SortedList<>(filteredData);
		
		// Bind the sortable list with the table
		sortedData.comparatorProperty().bind(filterTable.comparatorProperty());
		
		// Set the data on the table
		filterTable.setItems(sortedData);
		
		filteredData.addListener(new ListChangeListener<SubscriptionTopicSummaryProperties>()
		{
			@Override
			public void onChanged(Change<? extends SubscriptionTopicSummaryProperties> c)
			{
				filteredTopicsMenu.setDisable(filteredData.size() == nonFilteredData.size());
			}
		});
	}
	
	public void refreshRowStyling()
	{
		// To refresh the styling, add and remove an invisible column
		final TableColumn<SubscriptionTopicSummaryProperties, String> column = new TableColumn<>();
		column.setMaxWidth(0);
		column.setPrefWidth(0);
		
		filterTable.getColumns().add(column);
		filterTable.getColumns().remove(column);        
	}
	
	public int getFilteredDataSize()
	{
		return filteredData.size();
	}
	
	public void updateTopicFilter(final String topicFilter)
	{
		synchronized (filteredData)
		{
			filteredData.setPredicate(new Predicate<SubscriptionTopicSummaryProperties>()
			{
				@Override
				public boolean test(final SubscriptionTopicSummaryProperties item)
				{
					// If filter text is empty, display all persons.
		            if (topicFilter == null || topicFilter.isEmpty()) 
		            {
		                return true;
		            }
		            
		            final String topic = item.topicProperty().getValue();
	
		            if (topic.toLowerCase().indexOf(topicFilter.toLowerCase()) != -1) 
		            {
		            	// Filter matches first name
		            	synchronized (shownTopics)
		            	{
		            		shownTopics.add(topic);
		            	}
		                return true; 
		            }
		            
		            // Does not match
		            synchronized (shownTopics)
		            {
		            	shownTopics.remove(topic);
		            }
		            return false; 
				}
			});						
		}
	}
		
	private void showChartsWindow(final Set<String> topics, final ChartMode mode, final MqttAsyncConnection connection)
	{
		final String connectionName = connection != null ? " - " + connection.getName() : "";
		
		if (ChartMode.USER_DRIVEN_MSG_SIZE.equals(mode))
		{
			new ChartFactory<FormattedMqttMessage>().createMessageBasedLineChart(topics, store, mode, 
					"Topic", "Size", "bytes", "Message size chart" + connectionName, 
					filterTable.getScene(), eventBus);
		}
		else
		{
			new ChartFactory<FormattedMqttMessage>().createMessageBasedLineChart(topics, store, mode, 
					"Topic", "Value", "", "Message content chart" + connectionName,
					filterTable.getScene(), eventBus);
		}		
	}
	
	public ContextMenu createTopicTableContextMenu(final MqttAsyncConnection connection)
	{
		final ContextMenu contextMenu = new ContextMenu();
		
		// Subscribe to topic
		final MenuItem subscribeToTopicItem = new MenuItem("Subscribe (and create tab)");
		subscribeToTopicItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final SubscriptionTopicSummaryProperties item = filterTable.getSelectionModel()
						.getSelectedItem();
				if (item != null)
				{
					final TabbedSubscriptionDetails subscriptionDetails = new TabbedSubscriptionDetails();
					subscriptionDetails.setTopic(item.topicProperty().getValue());
					subscriptionDetails.setQos(0);
					
					connectionController.getNewSubscriptionPaneController().subscribe(subscriptionDetails, true);
				}
			}
		});
		contextMenu.getItems().add(subscribeToTopicItem);
		

		// Separator
		contextMenu.getItems().add(new SeparatorMenuItem());
		
		// Copy topic
		final MenuItem copyTopicItem = new MenuItem("Copy topic to clipboard");
		copyTopicItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final SubscriptionTopicSummaryProperties item = filterTable.getSelectionModel()
						.getSelectedItem();
				if (item != null)
				{
					UiUtils.copyToClipboard(item.topicProperty().getValue());
				}
			}
		});
		contextMenu.getItems().add(copyTopicItem);
		
		// Copy content
		final MenuItem copyContentItem = new MenuItem("Copy message content to clipboard");
		copyContentItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final SubscriptionTopicSummaryProperties item = filterTable.getSelectionModel()
						.getSelectedItem();
				if (item != null)
				{
					UiUtils.copyToClipboard(item.lastReceivedPayloadProperty().getValue());
				}
			}
		});
		contextMenu.getItems().add(copyContentItem);
		
		
		// Separator
		contextMenu.getItems().add(new SeparatorMenuItem());
		
		// All filters
		final Menu allTopicsMenu = new Menu("Browse all topics");
		
		// Apply all filters
		final MenuItem selectAllTopicsItem = new MenuItem("Select all topics");
		selectAllTopicsItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final SubscriptionTopicSummaryProperties item = filterTable.getSelectionModel()
						.getSelectedItem();
				if (item != null)
				{
					store.setAllShowValues(true);
					eventBus.publish(new MessageIndexToFirstEvent(store));
					// eventManager.navigateToFirst(store);
				}
			}
		});
		
		allTopicsMenu.getItems().add(selectAllTopicsItem);		
		
		// Toggle all filters
		final MenuItem toggleAllTopicsItem = new MenuItem("Toggle all topics");
		toggleAllTopicsItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final SubscriptionTopicSummaryProperties item = filterTable.getSelectionModel()
						.getSelectedItem();
				if (item != null)
				{
					store.toggleAllShowValues();
					eventBus.publish(new MessageIndexToFirstEvent(store));
					// eventManager.navigateToFirst(store);
				}
			}
		});
		allTopicsMenu.getItems().add(toggleAllTopicsItem);
		
		// Remove all filters
		final MenuItem removeAllTopicsItem = new MenuItem("Deselect all topics");
		removeAllTopicsItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final SubscriptionTopicSummaryProperties item = filterTable.getSelectionModel()
						.getSelectedItem();
				if (item != null)
				{
					store.setAllShowValues(false);
					eventBus.publish(new MessageIndexToFirstEvent(store));
					// eventManager.navigateToFirst(store);
				}
			}
		});
		allTopicsMenu.getItems().add(removeAllTopicsItem);
		contextMenu.getItems().add(allTopicsMenu);	
		
		// Filtered topics
		filteredTopicsMenu = new Menu("Browse filtered topics");
		
		// Apply filtered filters
		final MenuItem selectFilteredTopicsItem = new MenuItem("Add filtered topics to selection");
		selectFilteredTopicsItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final SubscriptionTopicSummaryProperties item = filterTable.getSelectionModel()
						.getSelectedItem();
				if (item != null)
				{
					store.setShowValues(true, shownTopics);
					eventBus.publish(new MessageIndexToFirstEvent(store));
					// eventManager.navigateToFirst(store);
				}
			}
		});		
		filteredTopicsMenu.getItems().add(selectFilteredTopicsItem);
		
		
		// Clear and add filtered filters
		final MenuItem removeAllAndAddFilteredTopicsItem = new MenuItem("Deselect all and selected filtered topics");
		removeAllAndAddFilteredTopicsItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final SubscriptionTopicSummaryProperties item = filterTable.getSelectionModel().getSelectedItem();
				if (item != null)
				{
					store.setAllShowValues(false);
					store.setShowValues(true, shownTopics);
					eventBus.publish(new MessageIndexToFirstEvent(store));
					// eventManager.navigateToFirst(store);
				}
			}
		});
		filteredTopicsMenu.getItems().add(removeAllAndAddFilteredTopicsItem);
		
		// Toggle filtered filters
		final MenuItem toggleFilteredTopicsItem = new MenuItem("Toggle selection for filtered topics");
		toggleFilteredTopicsItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final SubscriptionTopicSummaryProperties item = filterTable.getSelectionModel()
						.getSelectedItem();
				if (item != null)
				{
					store.toggleShowValues(shownTopics);
					eventBus.publish(new MessageIndexToFirstEvent(store));
					// eventManager.navigateToFirst(store);
				}
			}
		});
		filteredTopicsMenu.getItems().add(toggleFilteredTopicsItem);
		
		// Remove filtered filters
		final MenuItem removeFilteredTopicsItem = new MenuItem("Deselect filtered topics");
		removeFilteredTopicsItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final SubscriptionTopicSummaryProperties item = filterTable.getSelectionModel()
						.getSelectedItem();
				if (item != null)
				{
					store.setShowValues(false, shownTopics);
					eventBus.publish(new MessageIndexToFirstEvent(store));
					// eventManager.navigateToFirst(store);
				}
			}
		});
		filteredTopicsMenu.getItems().add(removeFilteredTopicsItem);
		contextMenu.getItems().add(filteredTopicsMenu);	
		
		// Only this topic
		final MenuItem selectOnlyThisItem = new MenuItem("Browse & select only this topic");
		selectOnlyThisItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final SubscriptionTopicSummaryProperties item = filterTable.getSelectionModel()
						.getSelectedItem();
				if (item != null)
				{
					store.setAllShowValues(false);
					store.setShowValue(item.topicProperty().getValue(), true);
					eventBus.publish(new MessageIndexToFirstEvent(store));
					// eventManager.navigateToFirst(store);
				}
			}
		});
		contextMenu.getItems().add(selectOnlyThisItem);
		

		// Separator
		contextMenu.getItems().add(new SeparatorMenuItem());

				
		// Charts
		Menu chartsItem = new Menu("Charts");
		
		MenuItem chartPayloadItem = new MenuItem("Show payload values for this topic");
		chartPayloadItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{				
				final SubscriptionTopicSummaryProperties item = filterTable.getSelectionModel().getSelectedItem();
				if (item != null)
				{
					final String topic = item.topicProperty().getValue();					
					showChartsWindow(
							new HashSet<String>(Arrays.asList(topic)), 
							ChartMode.USER_DRIVEN_MSG_PAYLOAD, 
							connection);
				}
			}
		});
		chartsItem.getItems().add(chartPayloadItem);		
		
		MenuItem chartPayloadForAllSelectedItem = new MenuItem("Show payload values for browsed topics");
		chartPayloadForAllSelectedItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{				
				final SubscriptionTopicSummaryProperties item = filterTable.getSelectionModel().getSelectedItem();
				if (item != null)
				{										
					final Set<String> topics = store.getFilteredMessageStore().getBrowsedTopics();
					if (topics.size() > CHART_TOPIC_COUNT)
					{
						final Optional<ButtonType> response = DialogFactory.createQuestionDialog(
								"Number of selected topics", 
								"More than " + CHART_TOPIC_COUNT 
								+ " topics have been selected to be displayed on a chart. Do you want to proceed?",
								false);
						
						if (response.get() != ButtonType.YES)
						{
							return;
						}
					}
					showChartsWindow(topics, ChartMode.USER_DRIVEN_MSG_PAYLOAD, connection);
				}
			}
		});
		chartsItem.getItems().add(chartPayloadForAllSelectedItem);		

		// Separator
		chartsItem.getItems().add(new SeparatorMenuItem());
		
		MenuItem chartSizeItem = new MenuItem("Show payload size for this topic");
		chartSizeItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{				
				final SubscriptionTopicSummaryProperties item = filterTable.getSelectionModel().getSelectedItem();
				if (item != null)
				{
					final String topic = item.topicProperty().getValue();					
					showChartsWindow(new HashSet<String>(Arrays.asList(topic)), ChartMode.USER_DRIVEN_MSG_SIZE, connection);
				}
			}
		});
		chartsItem.getItems().add(chartSizeItem);
		
		MenuItem chartSizeForAllSelectedItem = new MenuItem("Show payload size for browsed topics");
		chartSizeForAllSelectedItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{				
				final SubscriptionTopicSummaryProperties item = filterTable.getSelectionModel().getSelectedItem();
				if (item != null)
				{										
					final Set<String> topics = store.getFilteredMessageStore().getBrowsedTopics();
					if (topics.size() > CHART_TOPIC_COUNT)
					{
						final Optional<ButtonType> response = DialogFactory.createQuestionDialog(
								"Number of selected topics", 
								"More than " + CHART_TOPIC_COUNT 
								+ " topics have been selected to be displayed on a chart. Do you want to proceed?",
								false);
						
						if (response.get() != ButtonType.YES)
						{
							return;
						}
					}
					showChartsWindow(topics, ChartMode.USER_DRIVEN_MSG_SIZE, connection);
				}
			}
		});
		chartsItem.getItems().add(chartSizeForAllSelectedItem);
		contextMenu.getItems().add(chartsItem);

		return contextMenu;
	}

	public void setStore(final ManagedMessageStoreWithFiltering<FormattedMqttMessage> store)
	{
		this.store = store;
	}
	
	public void setConnectionController(final MqttConnectionController connectionController)
	{
		this.connectionController = connectionController;
	}

	/**
	 * Gets the shown/filtered topics.
	 * 
	 * @return the shownTopics
	 */
	public Set<String> getShownTopics()
	{
		return shownTopics;
	}
	
	public void setEventBus(final IKBus eventBus)
	{
		this.eventBus = eventBus;
	}
}
