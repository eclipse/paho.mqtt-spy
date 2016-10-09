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
package pl.baczkowicz.spy.ui.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.configuration.BaseConfigurationUtils;
import pl.baczkowicz.spy.exceptions.ConfigurationException;
import pl.baczkowicz.spy.ui.configuration.ConfiguredConnectionGroupDetails;
import pl.baczkowicz.spy.ui.connections.IConnectionFactory;
import pl.baczkowicz.spy.ui.properties.ConnectionListItemProperties;
import pl.baczkowicz.spy.ui.properties.ConnectionTreeItemProperties;
import pl.baczkowicz.spy.ui.properties.ModifiableConnection;
import pl.baczkowicz.spy.ui.utils.UiUtils;

/**
 * Controller for editing a single connection.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class EditConnectionGroupController extends AnchorPane implements Initializable
{
	final static Logger logger = LoggerFactory.getLogger(EditConnectionGroupController.class);
	
	@FXML
	private TextField connectionGroupNameText;
	
	// Action buttons
	
	@FXML
	private Button connectButton;
	
	@FXML
	private Button saveButton;
	
	@FXML
	private Button undoButton;
	
	@FXML 
	private TableView<ConnectionListItemProperties> connectionList;
	
	@FXML 
	private TableColumn<ConnectionListItemProperties, String> nameColumn;
       
	@FXML 
	private TableColumn<ConnectionListItemProperties, String> protocolColumn;
	
	@FXML 
	private TableColumn<ConnectionListItemProperties, String> detailsColumn;
	
	// Other fields

	// private MainController mainController;

	private ConfiguredConnectionGroupDetails editedConnectionGroupDetails;

	private List<ModifiableConnection> connections;
	
	private Map<ConnectionListItemProperties, ModifiableConnection> connectionMapping;

	private boolean recordModifications;

	private int noModificationsLock;

	private EditConnectionsController editConnectionsController;
	
	private IConnectionFactory connectionFactory;
	
	// ===============================
	// === Initialisation ============
	// ===============================

	public void initialize(URL location, ResourceBundle resources)
	{
		nameColumn.setCellValueFactory(new PropertyValueFactory<ConnectionListItemProperties, String>("name"));
		
		protocolColumn.setCellValueFactory(new PropertyValueFactory<ConnectionListItemProperties, String>("protocol"));
		protocolColumn.setCellFactory(new Callback<TableColumn<ConnectionListItemProperties,String>, TableCell<ConnectionListItemProperties,String>>()
		{			
			@Override
			public TableCell<ConnectionListItemProperties, String> call(
					TableColumn<ConnectionListItemProperties, String> param)
			{
				final TableCell<ConnectionListItemProperties, String> cell = new TableCell<ConnectionListItemProperties, String>()
				{
					@Override
					public void updateItem(final String item, boolean empty)
					{
						super.updateItem(item, empty);
						
						if (getTableRow().getItem() != null)
						{
							connectionFactory.populateProtocolCell(this, item);												
						}
						else
						{
							setGraphic(null);
							setText(null);
						}
					}
				};
				cell.setAlignment(Pos.TOP_CENTER);
				return cell;
			}
		});
		
		detailsColumn.setCellValueFactory(new PropertyValueFactory<ConnectionListItemProperties, String>("details"));
		detailsColumn.setCellFactory(new Callback<TableColumn<ConnectionListItemProperties,String>, TableCell<ConnectionListItemProperties,String>>()
		{			
			@Override
			public TableCell<ConnectionListItemProperties, String> call(
					TableColumn<ConnectionListItemProperties, String> param)
			{
				final TableCell<ConnectionListItemProperties, String> cell = new TableCell<ConnectionListItemProperties, String>()
				{
					@Override
					public void updateItem(final String item, boolean empty)
					{
						super.updateItem(item, empty);
						
						if (getTableRow().getItem() != null)
						{
							final ConnectionListItemProperties row = (ConnectionListItemProperties) getTableRow().getItem();
							
							setGraphic(UiUtils.createSecurityIcons(
									row.isTlsEnabled(), 
									row.isUserAuthenticationEnabled(),
									true));
							setText(item);
						}
						else
						{
							setGraphic(null);
							setText(null);
						}
					}
				};
				return cell;
			}
		});
		
		connectionList.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent mouseEvent)
			{
				if (mouseEvent.getButton().equals(MouseButton.PRIMARY))
				{
					if (mouseEvent.getClickCount() == 2)
					{
						final ConnectionListItemProperties selected = connectionList.getSelectionModel().getSelectedItem(); 
						if (selected != null)
						{
							editConnectionsController.selectConnection(connectionMapping.get(selected));
						}	
					}
				}
			}
		});
		
		connectionGroupNameText.textProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue)
			{
				onChange();
			}		
		});		
	}

	public void init()
	{
	}

	// ===============================
	// === FXML ======================
	// ===============================
	
	@FXML
	private void undo()
	{
		editedConnectionGroupDetails.undo();
		editConnectionsController.listConnections();
		
		// Note: listing connections should display the existing one
		
		updateButtons();
	}
	
	
	@FXML
	private void save()
	{
		editedConnectionGroupDetails.apply();
		editConnectionsController.listConnections();
				
		updateButtons();
	}	
	
	@FXML
	public void createConnection() throws ConfigurationException
	{
		for (final ModifiableConnection connection : connections)
		{
			editConnectionsController.openConnection(connection);
		}
	}

	// ===============================
	// === Logic =====================
	// ===============================
	
	public void onChange()
	{
		if (recordModifications)
		{					
			if (readAndDetectChanges())
			{
				updateButtons();			
				editConnectionsController.listConnections();
			}
		}				
	}
	
	private boolean readAndDetectChanges()
	{
		editedConnectionGroupDetails.setName(connectionGroupNameText.getText());
		
		boolean changed = !editedConnectionGroupDetails.getName().
				equals(editedConnectionGroupDetails.getLastSavedValues().getName());
			
		logger.debug("Values read. Changed = " + changed);
		editedConnectionGroupDetails.setModified(changed);
		
		return changed;
	}

	public void editConnectionGroup(final ConfiguredConnectionGroupDetails connectionGroup, final List<ConnectionTreeItemProperties> list)
	{	
		synchronized (this)
		{
			this.editedConnectionGroupDetails = connectionGroup;
			this.connections = new ArrayList<>();
			connectionFactory.findConnections(connectionGroup, connections);					
			
			displayConnectionDetails(editedConnectionGroupDetails);		
						
			updateButtons();
		}
	}
	
	private void updateButtons()
	{
		if (editedConnectionGroupDetails != null && editedConnectionGroupDetails.isModified())
		{
			saveButton.setDisable(false);
			undoButton.setDisable(false);
		}
		else
		{
			saveButton.setDisable(true);
			undoButton.setDisable(true);
		}
	}
	
	private void displayConnectionDetails(final ConfiguredConnectionGroupDetails group)
	{	
		connectionGroupNameText.setText(group.getName());
		connectionGroupNameText.setDisable(group.getID().equals(BaseConfigurationUtils.DEFAULT_GROUP));
		connectionList.getItems().clear();
		connectionMapping = new HashMap<>();
		
		for (final ModifiableConnection connection : connections)
		{						
			final ConnectionListItemProperties properties = connectionFactory.createConnectionListItemProperties(connection);
			
			connectionList.getItems().add(properties);
			connectionMapping.put(properties, connection);
		}
	}		

	// ===============================
	// === Setters and getters =======
	// ===============================
	
	public void setEditConnectionsController(EditConnectionsController editConnectionsController)
	{
		this.editConnectionsController = editConnectionsController;		
	}
	
	public void setRecordModifications(boolean recordModifications)
	{
		if (!recordModifications)
		{
			logger.trace("Modifications suspended...");
			noModificationsLock++;
			this.recordModifications = recordModifications;
		}
		else
		{ 
			noModificationsLock--;
			// Only allow modifications once the parent caller removes the lock
			if (noModificationsLock == 0)
			{
				logger.trace("Modifications restored...");
				this.recordModifications = recordModifications;
			}
		}
	}

	public void setConnectionFactory(final IConnectionFactory connectionFactory)
	{
		this.connectionFactory = connectionFactory;
	}
}
