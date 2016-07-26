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
package pl.baczkowicz.mqttspy.ui.controllers.edit;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.configuration.ConfiguredConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.TabbedSubscriptionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.ui.EditConnectionController;
import pl.baczkowicz.spy.ui.properties.BaseTopicProperty;
import pl.baczkowicz.spy.ui.properties.SubscriptionTopicProperties;

/**
 * Controller for editing a single connection - subscriptions tab.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class EditConnectionSubscriptionsController extends AnchorPane implements Initializable, EditConnectionSubController
{
	private final static Logger logger = LoggerFactory.getLogger(EditConnectionSubscriptionsController.class);
	
	/** The parent controller. */
	private EditConnectionController parent;
	
	// Action buttons
	
	@FXML
	private Button removeSubscriptionButton;
	
	// Pubs / subs
	
	@FXML
	private TextField searchScriptsText;
	
	// Tables
	
	@FXML
	private TableView<SubscriptionTopicProperties> subscriptionsTable;
	
	@FXML
	private TableColumn<SubscriptionTopicProperties, String> subscriptionTopicColumn;
	
	@FXML
	private TableColumn<SubscriptionTopicProperties, String> scriptColumn;
	
	@FXML
	private TableColumn<SubscriptionTopicProperties, Integer> qosSubscriptionColumn;
	
	@FXML
	private TableColumn<SubscriptionTopicProperties, Boolean> createTabSubscriptionColumn;
	
	// Other fields

	private final ChangeListener basicOnChangeListener = new ChangeListener()
	{
		@Override
		public void changed(ObservableValue observable, Object oldValue, Object newValue)
		{
			onChange();			
		}		
	};
	
	// ===============================
	// === Initialisation ============
	// ===============================

	public void initialize(URL location, ResourceBundle resources)
	{
		// Subscriptions
		searchScriptsText.textProperty().addListener(basicOnChangeListener);
		createTabSubscriptionColumn.setCellValueFactory(new PropertyValueFactory<SubscriptionTopicProperties, Boolean>("show"));
		createTabSubscriptionColumn.setCellFactory(new Callback<TableColumn<SubscriptionTopicProperties, Boolean>, TableCell<SubscriptionTopicProperties, Boolean>>()
				{
					public TableCell<SubscriptionTopicProperties, Boolean> call(
							TableColumn<SubscriptionTopicProperties, Boolean> p)
					{
						final TableCell<SubscriptionTopicProperties, Boolean> cell = new TableCell<SubscriptionTopicProperties, Boolean>()
						{
							@Override
							public void updateItem(final Boolean item, boolean empty)
							{
								super.updateItem(item, empty);
								if (!isEmpty())
								{
									final SubscriptionTopicProperties shownItem = getTableView().getItems().get(getIndex());
									CheckBox box = new CheckBox();
									box.selectedProperty().bindBidirectional(shownItem.showProperty());
									box.setOnAction(new EventHandler<ActionEvent>()
									{										
										@Override
										public void handle(ActionEvent event)
										{
											logger.info("New value = {} {}", 
													shownItem.topicProperty().getValue(),
													shownItem.showProperty().getValue());
											onChange();
										}
									});
									setGraphic(box);
								}
								else
								{
									setGraphic(null);
								}
							}
						};
						cell.setAlignment(Pos.CENTER);
						return cell;
					}
				});

		subscriptionTopicColumn.setCellValueFactory(new PropertyValueFactory<SubscriptionTopicProperties, String>("topic"));
		subscriptionTopicColumn.setCellFactory(TextFieldTableCell.<SubscriptionTopicProperties>forTableColumn());
		subscriptionTopicColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<SubscriptionTopicProperties, String>>()
				{
					@Override
					public void handle(CellEditEvent<SubscriptionTopicProperties, String> event)
					{
						BaseTopicProperty p = event.getRowValue();
			            String newValue = event.getNewValue();
			            p.topicProperty().set(newValue);            
						logger.debug("New value = {}", subscriptionsTable.getSelectionModel().getSelectedItem().topicProperty().getValue());
						onChange();
					}		
				});
		
		scriptColumn.setCellValueFactory(new PropertyValueFactory<SubscriptionTopicProperties, String>("script"));
		scriptColumn.setCellFactory(TextFieldTableCell.<SubscriptionTopicProperties>forTableColumn());
		scriptColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<SubscriptionTopicProperties, String>>()
				{
					@Override
					public void handle(CellEditEvent<SubscriptionTopicProperties, String> event)
					{
						SubscriptionTopicProperties p = event.getRowValue();
			            String newValue = event.getNewValue();
			            p.scriptProperty().set(newValue);            
						logger.debug("New value = {}", subscriptionsTable.getSelectionModel().getSelectedItem().scriptProperty().getValue());
						onChange();
					}		
				});
		
		final ObservableList<Integer> qosChoice = FXCollections.observableArrayList (
			    new Integer(0),
			    new Integer(1),
			    new Integer(2)
			);
		
		qosSubscriptionColumn.setCellValueFactory(new PropertyValueFactory<SubscriptionTopicProperties, Integer>("qos"));
		qosSubscriptionColumn.setCellFactory(new Callback<TableColumn<SubscriptionTopicProperties, Integer>, TableCell<SubscriptionTopicProperties, Integer>>()
				{
					public TableCell<SubscriptionTopicProperties, Integer> call(
							TableColumn<SubscriptionTopicProperties, Integer> p)
					{
						final TableCell<SubscriptionTopicProperties, Integer> cell = new TableCell<SubscriptionTopicProperties, Integer>()
						{
							@Override
							public void updateItem(final Integer item, boolean empty)
							{
								super.updateItem(item, empty);
								if (!isEmpty())
								{
									final SubscriptionTopicProperties shownItem = getTableView().getItems().get(getIndex());
									ChoiceBox box = new ChoiceBox();
									box.setItems(qosChoice);
									box.setId("subscriptionQosChoice");
									int qos = shownItem.qosProperty().getValue();
									box.getSelectionModel().select(qos);
									box.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>()
									{
										@Override
										public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue)
										{
											shownItem.qosProperty().setValue(newValue);
											logger.info("New value = {} {}", 
													shownItem.topicProperty().getValue(),
													shownItem.qosProperty().getValue());
											onChange();
										}
									});
									setGraphic(box);
								}
								else
								{
									setGraphic(null);
								}
							}
						};
						cell.setAlignment(Pos.CENTER);
						return cell;
					}
				});
		qosSubscriptionColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<SubscriptionTopicProperties, Integer>>()
				{
					@Override
					public void handle(CellEditEvent<SubscriptionTopicProperties, Integer> event)
					{
						SubscriptionTopicProperties p = event.getRowValue();
						Integer newValue = event.getNewValue();
			            p.qosProperty().set(newValue);            
						logger.debug("New value = {}", subscriptionsTable.getSelectionModel().getSelectedItem().qosProperty().getValue());
						onChange();
					}		
				});
	}

	public void init()
	{
		// Nothing to do
	}

	// ===============================
	// === FXML ======================
	// ===============================
	
	@FXML
	private void addSubscription()
	{
		final SubscriptionTopicProperties item = new SubscriptionTopicProperties("/sampleSubscription/", "", 0, false);		
		subscriptionsTable.getItems().add(item);
		onChange();
	}	
	
	@FXML
	private void removeSubscription()
	{
		final SubscriptionTopicProperties item = subscriptionsTable.getSelectionModel().getSelectedItem(); 
		if (item != null)
		{
			subscriptionsTable.getItems().remove(item);
			onChange();
		}
	}

	// ===============================
	// === Logic =====================
	// ===============================

	public void onChange()
	{
		parent.onChange();		
	}

	@Override
	public UserInterfaceMqttConnectionDetails readValues(final UserInterfaceMqttConnectionDetails connection)
	{
		// Subscriptions
		connection.setSearchScripts(searchScriptsText.getText());
		for (final SubscriptionTopicProperties subscriptionDetails : subscriptionsTable.getItems())
		{
			final TabbedSubscriptionDetails newSubscriptionDetails = new TabbedSubscriptionDetails();
			newSubscriptionDetails.setTopic(subscriptionDetails.topicProperty().getValue());
			newSubscriptionDetails.setScriptFile(subscriptionDetails.scriptProperty().getValue());
			newSubscriptionDetails.setCreateTab(subscriptionDetails.showProperty().getValue());
			newSubscriptionDetails.setQos(subscriptionDetails.qosProperty().getValue());
			connection.getSubscription().add(newSubscriptionDetails);
		}
		
		return connection;
	}
	
	@Override
	public void displayConnectionDetails(final ConfiguredConnectionDetails connection)
	{
		// Subscriptions
		searchScriptsText.setText(connection.getSearchScripts());
		removeSubscriptionButton.setDisable(true);
		subscriptionsTable.getItems().clear();
		for (final TabbedSubscriptionDetails sub : connection.getSubscription())
		{
			subscriptionsTable.getItems().add(new SubscriptionTopicProperties(
					sub.getTopic(), 
					sub.getScriptFile() == null ? "" : sub.getScriptFile(), 
					sub.getQos(), sub.isCreateTab()));
		}
		subscriptionsTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue)
			{
				removeSubscriptionButton.setDisable(false);
			}		
		});
	}		

	// ===============================
	// === Setters and getters =======
	// ===============================
	
	@Override
	public void setParent(final EditConnectionController controller)
	{
		this.parent = controller;
	}
}
