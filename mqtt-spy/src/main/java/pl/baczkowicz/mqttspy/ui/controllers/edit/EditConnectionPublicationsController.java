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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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

import pl.baczkowicz.mqttspy.common.generated.PublicationDetails;
import pl.baczkowicz.mqttspy.configuration.ConfiguredConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.ui.EditConnectionController;
import pl.baczkowicz.spy.common.generated.ScriptDetails;
import pl.baczkowicz.spy.ui.properties.BackgroundScriptProperties;
import pl.baczkowicz.spy.ui.properties.BaseTopicProperty;

/**
 * Controller for editing a single connection - publications tab.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class EditConnectionPublicationsController extends AnchorPane implements Initializable, EditConnectionSubController
{
	private final static Logger logger = LoggerFactory.getLogger(EditConnectionPublicationsController.class);
	
	/** The parent controller. */
	private EditConnectionController parent;

	// Action buttons
	
	@FXML
	private Button removePublicationButton;
		
	@FXML
	private Button removeScriptButton;
	
	
	// Pubs / subs
	
	@FXML
	private TextField publicationScriptsText;
	
	// Tables
	
	@FXML
	private TableView<BaseTopicProperty> publicationsTable;
	
	@FXML
	private TableView<BackgroundScriptProperties> backgroundPublicationScriptsTable;
	
	@FXML
	private TableColumn<BaseTopicProperty, String> publicationTopicColumn;	
	
	// Background publication scripts
	@FXML
	private TableColumn<BackgroundScriptProperties, String> publicationScriptColumn;
	
	@FXML
	private TableColumn<BackgroundScriptProperties, Boolean> publicationAutoStartColumn;
	
	@FXML
	private TableColumn<BackgroundScriptProperties, Boolean> publicationRepeatColumn;
	
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
		// Publication topics
		publicationTopicColumn.setCellValueFactory(new PropertyValueFactory<BaseTopicProperty, String>("topic"));
		publicationTopicColumn.setCellFactory(TextFieldTableCell.<BaseTopicProperty>forTableColumn());
		publicationTopicColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<BaseTopicProperty, String>>()
		{
			@Override
			public void handle(CellEditEvent<BaseTopicProperty, String> event)
			{
				BaseTopicProperty p = event.getRowValue();
	            String newValue = event.getNewValue();
	            p.topicProperty().set(newValue);            
				logger.debug("New value = {}", publicationsTable.getSelectionModel().getSelectedItem().topicProperty().getValue());
				onChange();
			}		
		});
		
		// Publication scripts
		publicationScriptsText.textProperty().addListener(basicOnChangeListener);
		publicationAutoStartColumn.setCellValueFactory(new PropertyValueFactory<BackgroundScriptProperties, Boolean>("autoStart"));
		publicationAutoStartColumn.setCellFactory(new Callback<TableColumn<BackgroundScriptProperties, Boolean>, TableCell<BackgroundScriptProperties, Boolean>>()
				{
					public TableCell<BackgroundScriptProperties, Boolean> call(
							TableColumn<BackgroundScriptProperties, Boolean> p)
					{
						final TableCell<BackgroundScriptProperties, Boolean> cell = new TableCell<BackgroundScriptProperties, Boolean>()
						{
							@Override
							public void updateItem(final Boolean item, boolean empty)
							{
								super.updateItem(item, empty);
								if (!isEmpty())
								{
									final BackgroundScriptProperties shownItem = getTableView().getItems().get(getIndex());
									CheckBox box = new CheckBox();
									box.selectedProperty().bindBidirectional(shownItem.autoStartProperty());
									box.setOnAction(new EventHandler<ActionEvent>()
									{										
										@Override
										public void handle(ActionEvent event)
										{
											logger.info("New value = {} {}", 
													shownItem.scriptProperty().getValue(),
													shownItem.autoStartProperty().getValue());
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
		
		publicationRepeatColumn.setCellValueFactory(new PropertyValueFactory<BackgroundScriptProperties, Boolean>("repeat"));
		publicationRepeatColumn.setCellFactory(new Callback<TableColumn<BackgroundScriptProperties, Boolean>, TableCell<BackgroundScriptProperties, Boolean>>()
				{
					public TableCell<BackgroundScriptProperties, Boolean> call(
							TableColumn<BackgroundScriptProperties, Boolean> p)
					{
						final TableCell<BackgroundScriptProperties, Boolean> cell = new TableCell<BackgroundScriptProperties, Boolean>()
						{
							@Override
							public void updateItem(final Boolean item, boolean empty)
							{
								super.updateItem(item, empty);
								if (!isEmpty())
								{
									final BackgroundScriptProperties shownItem = getTableView().getItems().get(getIndex());
									CheckBox box = new CheckBox();
									box.selectedProperty().bindBidirectional(shownItem.repeatProperty());
									box.setOnAction(new EventHandler<ActionEvent>()
									{										
										@Override
										public void handle(ActionEvent event)
										{
											logger.info("New value = {} {}", 
													shownItem.scriptProperty().getValue(),
													shownItem.repeatProperty().getValue());
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
		
		publicationScriptColumn.setCellValueFactory(new PropertyValueFactory<BackgroundScriptProperties, String>("script"));
		publicationScriptColumn.setCellFactory(TextFieldTableCell.<BackgroundScriptProperties>forTableColumn());
		publicationScriptColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<BackgroundScriptProperties, String>>()
				{
					@Override
					public void handle(CellEditEvent<BackgroundScriptProperties, String> event)
					{
						BackgroundScriptProperties p = event.getRowValue();
			            String newValue = event.getNewValue();
			            p.scriptProperty().set(newValue);            
						logger.debug("New value = {}", backgroundPublicationScriptsTable.getSelectionModel().getSelectedItem().scriptProperty().getValue());
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
	private void addPublication()
	{
		final BaseTopicProperty item = new BaseTopicProperty("/samplePublication/");		
		publicationsTable.getItems().add(item);
		onChange();
	}
	
	@FXML
	private void addScript()
	{
		final BackgroundScriptProperties item = new BackgroundScriptProperties("put your script location here...", false, false);
		backgroundPublicationScriptsTable.getItems().add(item);
		onChange();
	}
	
	@FXML
	private void removePublication()
	{
		final BaseTopicProperty item = publicationsTable.getSelectionModel().getSelectedItem(); 
		if (item != null)
		{
			publicationsTable.getItems().remove(item);
			onChange();
		}
	}
	
	@FXML
	private void removeScript()
	{
		final BackgroundScriptProperties item = backgroundPublicationScriptsTable.getSelectionModel().getSelectedItem();
		if (item != null)
		{
			backgroundPublicationScriptsTable.getItems().remove(item);
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
		// Publications topics		
		for (final BaseTopicProperty publicationDetails : publicationsTable.getItems())
		{
			final PublicationDetails newPublicationDetails = new PublicationDetails();
			newPublicationDetails.setTopic(publicationDetails.topicProperty().getValue());
			connection.getPublication().add(newPublicationDetails);
		}
		
		// Publication scripts
		connection.setPublicationScripts(publicationScriptsText.getText());
		for (final BackgroundScriptProperties scriptDetails : backgroundPublicationScriptsTable.getItems())
		{
			final ScriptDetails newScriptDetails = new ScriptDetails();			
			newScriptDetails.setFile(scriptDetails.scriptProperty().getValue());
			newScriptDetails.setAutoStart(scriptDetails.autoStartProperty().getValue());
			newScriptDetails.setRepeat(scriptDetails.repeatProperty().getValue());
			connection.getBackgroundScript().add(newScriptDetails);
		}
		
		return connection;
	}
	
	@Override
	public void displayConnectionDetails(final ConfiguredConnectionDetails connection)
	{
		// Publications topics
		removePublicationButton.setDisable(true);
		publicationsTable.getItems().clear();
		for (final PublicationDetails pub : connection.getPublication())
		{
			publicationsTable.getItems().add(new BaseTopicProperty(pub.getTopic()));
		}
		publicationsTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue)
			{
				removePublicationButton.setDisable(false);
			}		
		});
		
		// Publication scripts
		publicationScriptsText.setText(connection.getPublicationScripts());	
		removeScriptButton.setDisable(true);
		
		backgroundPublicationScriptsTable.getItems().clear();
		for (final ScriptDetails script : connection.getBackgroundScript())
		{
			backgroundPublicationScriptsTable.getItems().add(new BackgroundScriptProperties(script.getFile(), script.isAutoStart(), script.isRepeat()));
		}
		backgroundPublicationScriptsTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue)
			{
				removeScriptButton.setDisable(false);
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
