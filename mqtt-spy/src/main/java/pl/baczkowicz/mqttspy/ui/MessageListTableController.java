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
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.connectivity.BaseMqttSubscription;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.connectivity.MqttSubscription;
import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.ui.events.EventManager;
import pl.baczkowicz.mqttspy.ui.utils.StylingUtils;
import pl.baczkowicz.spy.ui.events.observers.MessageIndexChangeObserver;
import pl.baczkowicz.spy.ui.properties.MessageContentProperties;
import pl.baczkowicz.spy.ui.storage.BasicMessageStoreWithSummary;
import pl.baczkowicz.spy.ui.utils.UiUtils;

/**
 * Controller for the message list table.
 */
public class MessageListTableController implements Initializable, MessageIndexChangeObserver
{
	final static Logger logger = LoggerFactory.getLogger(MessageListTableController.class);
	
	private ObservableList<MessageContentProperties<FormattedMqttMessage>> items; 
	
	@FXML
	private TableView<MessageContentProperties<FormattedMqttMessage>> messageTable;

	@FXML
	private TableColumn<MessageContentProperties<FormattedMqttMessage>, String> messageTopicColumn;
	
	@FXML
	private TableColumn<MessageContentProperties<FormattedMqttMessage>, String> messageContentColumn;

	@FXML
	private TableColumn<MessageContentProperties<FormattedMqttMessage>, String> messageReceivedAtColumn;

	private BasicMessageStoreWithSummary<FormattedMqttMessage> store;
	
	private MqttAsyncConnection connection;

	private EventManager<FormattedMqttMessage> eventManager;

	public void initialize(URL location, ResourceBundle resources)
	{				
		// Table
		messageTopicColumn.setCellValueFactory(new PropertyValueFactory<MessageContentProperties<FormattedMqttMessage>, String>(
				"topic"));

		messageContentColumn
				.setCellValueFactory(new PropertyValueFactory<MessageContentProperties<FormattedMqttMessage>, String>(
						"lastReceivedPayload"));

		messageReceivedAtColumn.setCellValueFactory(new PropertyValueFactory<MessageContentProperties<FormattedMqttMessage>, String>("lastReceivedTimestamp"));
		messageReceivedAtColumn.setCellFactory(new Callback<TableColumn<MessageContentProperties<FormattedMqttMessage>, String>, 
				TableCell<MessageContentProperties<FormattedMqttMessage>, String>>()
		{
			public TableCell<MessageContentProperties<FormattedMqttMessage>, String> call(
					TableColumn<MessageContentProperties<FormattedMqttMessage>, String> param)
			{
				final TableCell<MessageContentProperties<FormattedMqttMessage>, String> cell = 
						new TableCell<MessageContentProperties<FormattedMqttMessage>, String>()
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
		
		messageTable.setOnMouseClicked(new EventHandler<Event>()
		{
			@Override
			public void handle(Event event)
			{
				selectItem();
			}
		});
		
		messageTable
				.setRowFactory(new Callback<TableView<MessageContentProperties<FormattedMqttMessage>>, TableRow<MessageContentProperties<FormattedMqttMessage>>>()
				{
					public TableRow<MessageContentProperties<FormattedMqttMessage>> call(
							TableView<MessageContentProperties<FormattedMqttMessage>> tableView)
					{
						final TableRow<MessageContentProperties<FormattedMqttMessage>> row = new TableRow<MessageContentProperties<FormattedMqttMessage>>()
						{
							@Override
							protected void updateItem(final MessageContentProperties<FormattedMqttMessage> item, boolean empty)
							{
								super.updateItem(item, empty);															
								
								if (!isEmpty() && item.getSubscription() != null)
								{								
									final BaseMqttSubscription subscription = connection.getMqttSubscriptionForTopic(item.getSubscription());
									
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
	
	private void selectItem()
	{
		final MessageContentProperties<FormattedMqttMessage> item = messageTable.getSelectionModel().getSelectedItem();
		if (item != null)
		{
			final List<FormattedMqttMessage> list = store.getMessages();
			for (int i = 0; i < store.getMessages().size(); i++)
			{
				if (list.get(i).getId() == item.getId())
				{
					// logger.info("{} Changing selection to " + (array.length - i), store.getName());
					eventManager.changeMessageIndex(store, this, i + 1);
				}
			}
		}
	}

	@Override
	public void onMessageIndexChange(int messageIndex)
	{
		if (store.getMessages().size() > 0)
		{
			final long id = (store.getMessages().get(messageIndex - 1)).getId();

			for (final MessageContentProperties<FormattedMqttMessage> item : items)
			{
				if (item.getId() == id)
				{
					if (!item.equals(messageTable.getSelectionModel().getSelectedItem()))
					{
						messageTable.getSelectionModel().select(item);
						break;
					}
				}
			}
		}
	}
	
	public void init()
	{
		messageTable.setContextMenu(createMessageListTableContextMenu(messageTable));
		messageTable.setItems(items);	
	}

	public void setEventManager(final EventManager<FormattedMqttMessage> eventManager)
	{
		this.eventManager = eventManager;
	}
	
	public void setItems(final ObservableList<MessageContentProperties<FormattedMqttMessage>> items)
	{
		this.items = items;
	}
	
	public void setStore(final BasicMessageStoreWithSummary<FormattedMqttMessage> store)
	{
		this.store = store;
	}
	
	public void setConnection(final MqttAsyncConnection connection)
	{
		this.connection = connection;
	}
	
	public static ContextMenu createMessageListTableContextMenu(final TableView<MessageContentProperties<FormattedMqttMessage>> messageTable)
	{
		final ContextMenu contextMenu = new ContextMenu();
		
		// Copy topic
		final MenuItem copyTopicItem = new MenuItem("[Topic] Copy to clipboard");
		copyTopicItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final MessageContentProperties<FormattedMqttMessage> item = messageTable.getSelectionModel()
						.getSelectedItem();
				if (item != null)
				{
					UiUtils.copyToClipboard(item.topicProperty().getValue());
				}
			}
		});
		contextMenu.getItems().add(copyTopicItem);

		// Separator
		contextMenu.getItems().add(new SeparatorMenuItem());
		
		// Copy content
		final MenuItem copyContentItem = new MenuItem("[Content] Copy to clipboard");
		copyContentItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final MessageContentProperties<FormattedMqttMessage> item = messageTable.getSelectionModel()
						.getSelectedItem();
				if (item != null)
				{
					UiUtils.copyToClipboard(item.lastReceivedPayloadProperty().getValue());
				}
			}
		});
		contextMenu.getItems().add(copyContentItem);

		return contextMenu;
	}
}
