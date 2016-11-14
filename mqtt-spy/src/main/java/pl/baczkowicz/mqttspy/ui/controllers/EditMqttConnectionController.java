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
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.MessageLog;
import pl.baczkowicz.mqttspy.configuration.ConfigurationUtils;
import pl.baczkowicz.mqttspy.configuration.ConfiguredMqttConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.ui.MqttConnectionViewManager;
import pl.baczkowicz.mqttspy.ui.controllers.edit.EditConnectionConnectivityController;
import pl.baczkowicz.mqttspy.ui.controllers.edit.EditConnectionLastWillController;
import pl.baczkowicz.mqttspy.ui.controllers.edit.EditConnectionMessageLogController;
import pl.baczkowicz.mqttspy.ui.controllers.edit.EditConnectionOtherController;
import pl.baczkowicz.mqttspy.ui.controllers.edit.EditConnectionPublicationsController;
import pl.baczkowicz.mqttspy.ui.controllers.edit.EditConnectionSecurityController;
import pl.baczkowicz.mqttspy.ui.controllers.edit.EditConnectionSubscriptionsController;
import pl.baczkowicz.mqttspy.ui.utils.ConnectivityUtils;
import pl.baczkowicz.mqttspy.utils.ConnectionUtils;
import pl.baczkowicz.spy.common.generated.ConnectionGroupReference;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.exceptions.ConfigurationException;
import pl.baczkowicz.spy.ui.configuration.IConfigurationManager;
import pl.baczkowicz.spy.ui.controllers.EditConnectionsController;
import pl.baczkowicz.spy.ui.events.ConnectionNameChangedEvent;
import pl.baczkowicz.spy.ui.panes.SpyPerspective;
import pl.baczkowicz.spy.ui.utils.DialogFactory;
import pl.baczkowicz.spy.ui.utils.TooltipFactory;

/**
 * Controller for editing a single MQTT connection.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class EditMqttConnectionController extends AnchorPane implements Initializable
{
	final static Logger logger = LoggerFactory.getLogger(EditMqttConnectionController.class);
	
	private SpyPerspective perspective;
	
	private List<Tab> tabs;
	
	@FXML
	private TextField connectionNameText;
	
	@FXML
	private ComboBox<SpyPerspective> perspectiveCombo;
	
	@FXML
	private Tab publicationsTab;
	
	@FXML
	private Tab subscriptionsTab;
	
	@FXML
	private Tab otherTab;

	@FXML
	private Tab logTab;
	
	@FXML
	private Tab ltwTab;
	
	@FXML
	private TabPane tabPane;
	
	// Action buttons
	
	@FXML
	private Button connectButton;
	
	@FXML
	private Button cancelButton;
	
	@FXML
	private Button saveButton;
	
	@FXML
	private Button undoButton;
	
	// Controllers
	
	@FXML
	private EditConnectionConnectivityController editConnectionConnectivityController;
	
	@FXML
	private EditConnectionLastWillController editConnectionLastWillController;
	
	@FXML
	private EditConnectionMessageLogController editConnectionMessageLogController;
	
	@FXML
	private EditConnectionOtherController editConnectionOtherController;
	
	@FXML
	private EditConnectionPublicationsController editConnectionPublicationsController;
	
	@FXML
	private EditConnectionSecurityController editConnectionSecurityController;
	
	@FXML
	private EditConnectionSubscriptionsController editConnectionSubscriptionsController;
	
	// Other fields

	private String lastGeneratedConnectionName = "";
	
	// private MainController mainController;

	private ConfiguredMqttConnectionDetails editedConnectionDetails;

	private boolean recordModifications;
    
	private IConfigurationManager configurationManager;

	private EditConnectionsController editConnectionsController;

	private boolean openNewMode;

	private MqttAsyncConnection existingConnection;

	private int noModificationsLock;

	private MqttConnectionViewManager connectionManager;

	private boolean emptyConnectionList;

	private IKBus eventBus;
	
//	private final ChangeListener basicOnChangeListener = new ChangeListener()
//	{
//		@Override
//		public void changed(ObservableValue observable, Object oldValue, Object newValue)
//		{
//			onChange();			
//		}		
//	};
	
	// ===============================
	// === Initialisation ============
	// ===============================

	public void initialize(URL location, ResourceBundle resources)
	{
		connectionNameText.textProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue)
			{
				onChange();
			}		
		});
		
		editConnectionConnectivityController.setParent(this);
		editConnectionLastWillController.setParent(this);
		editConnectionMessageLogController.setParent(this);
		editConnectionOtherController.setParent(this);
		editConnectionPublicationsController.setParent(this);
		editConnectionSecurityController.setParent(this);
		editConnectionSubscriptionsController.setParent(this);
		
		tabs = new ArrayList(tabPane.getTabs());
	}
	
	private String getPerspectiveString(final SpyPerspective item)
	{
		if (item.equals(SpyPerspective.BASIC))
		{
			return ("Basic - the absolute minimum");
		}
		else if (item.equals(SpyPerspective.DEFAULT))
		{
			return ("Default - simpified properties");
		}
		else if (item.equals(SpyPerspective.DETAILED))
		{
			return ("Detailed - all properties");
		}
		else if (item.equals(SpyPerspective.SPY))
		{
			return ("Spy - simplified subscriptions");
		}
		else if (item.equals(SpyPerspective.SUPER_SPY))
		{
			return ("Super Spy - subscriptions only");
		}
		
		return null;
	}

	public void init()
	{
		for (SpyPerspective sp : SpyPerspective.values())
		{
			perspectiveCombo.getItems().add(sp);
		}
		
		perspectiveCombo.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				updatePerspective(perspectiveCombo.getSelectionModel().getSelectedItem());
				
			}
		});
		
		perspectiveCombo.setCellFactory(new Callback<ListView<SpyPerspective>, ListCell<SpyPerspective>>()
		{
			@Override
			public ListCell<SpyPerspective> call(ListView<SpyPerspective> l)
			{
				return new ListCell<SpyPerspective>()
				{
					@Override
					protected void updateItem(SpyPerspective item, boolean empty)
					{
						super.updateItem(item, empty);
						if (item == null || empty)
						{
							setText(null);
						}
						else
						{			
							setText(getPerspectiveString(item));
						}
					}
				};
			}
		});
		
		perspectiveCombo.setConverter(new StringConverter<SpyPerspective>()
		{
			@Override
			public String toString(SpyPerspective item)
			{
				if (item == null)
				{
					return null;
				}
				else
				{
					return getPerspectiveString(item);
				}
			}

			@Override
			public SpyPerspective fromString(String id)
			{
				return null;
			}
		});
		
		editConnectionOtherController.setEventBus(eventBus);
		editConnectionOtherController.setConfigurationManager(configurationManager);
		
		editConnectionConnectivityController.init();
		editConnectionLastWillController.init();
		editConnectionMessageLogController.init();
		editConnectionOtherController.init();
		editConnectionPublicationsController.init();
		editConnectionSecurityController.init();
		editConnectionSubscriptionsController.init();		

		getConnectionName().textProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable,
					Object oldValue, Object newValue)
			{
				if (isRecordModifications())
				{
					eventBus.publish(new ConnectionNameChangedEvent(getConnectionName().getText()));
				}
			}
		});
	}
	
	private void updatePerspective(final SpyPerspective perspective)
	{
		editConnectionConnectivityController.setPerspective(perspective);
		
		tabPane.getTabs().clear();
		tabPane.getTabs().addAll(tabs);
		
		if (perspective.equals(SpyPerspective.BASIC) || perspective.equals(SpyPerspective.SPY) || perspective.equals(SpyPerspective.SUPER_SPY))
		{
			tabPane.getTabs().remove(publicationsTab);
		}
		
		if (perspective.equals(SpyPerspective.BASIC))
		{
			tabPane.getTabs().remove(subscriptionsTab);
			tabPane.getTabs().remove(otherTab);
			tabPane.getTabs().remove(logTab);
			tabPane.getTabs().remove(ltwTab);
		}
		
		// TODO Auto-generated method stub
	}

	// ===============================
	// === FXML ======================
	// ===============================

	@FXML
	private void addTimestamp()
	{
		editConnectionConnectivityController.updateClientId(true);
	}
	
	@FXML
	private void undo()
	{
		editedConnectionDetails.undo();
		editConnectionsController.listConnections();
		
		// Note: listing connections should display the existing one
		
		updateButtons();
	}
	
	
	@FXML
	private void save()
	{		
		if (configurationManager.isConfigurationWritable())
		{
			logger.debug("Saving connection " + getConnectionName().getText());
			if (configurationManager.saveConfiguration())
			{
				editedConnectionDetails.apply();
				editConnectionsController.listConnections();
						
				updateButtons();
				
				TooltipFactory.createTooltip(saveButton, "Changes for connection " + editedConnectionDetails.getName() + " have been saved.");
			}
			else
			{
				DialogFactory.createErrorDialog(
						"Cannot save the configuration file", 
						"Oops... an error has occurred while trying to save your configuration. "
						+ "Please check the log file for more information. Your changes were not saved.");
			}
		}
		else
		{
			DialogFactory.createErrorDialog(
					"Cannot save the configuration file", 
					"Oops... your configuration file isn't right. Please restore default configuration. ");
		}
	}	
	
	@FXML
	private void cancel()
	{
		// Get a handle to the stage
		Stage stage = (Stage) cancelButton.getScene().getWindow();
		
		// Close the window
		stage.close();
	}
	
	public void openConnection(final ConfiguredMqttConnectionDetails connectionDetails)
	{
		final String validationResult = ConnectivityUtils.validateConnectionDetails(connectionDetails, false);
		
		if (validationResult != null)
		{
			DialogFactory.createWarningDialog("Invalid value detected", validationResult);
		}
		else
		{					
			if (connectionDetails.isModified())
			{	
				Optional<ButtonType> response = DialogFactory.createQuestionDialog(
						"Unsaved changes detected", 
						"You've got unsaved changes for " + "connection " 
						+ connectionDetails.getName() + ". Do you want to save/apply them now?", 
						true);
				
				if (response.get() == ButtonType.YES)
				{
					save();
				}
				else if (response.get() == ButtonType.NO)
				{
					// Do nothing
				}
				else
				{
					return;
				}
			}
			
			checkIfOpened(connectionDetails.getID());
			if (!openNewMode)
			{
				connectionManager.disconnectAndCloseTab(existingConnection);
			}
			
			logger.info("Opening connection " + getConnectionName().getText());
	
			// Get a handle to the stage
			Stage stage = (Stage) connectButton.getScene().getWindow();
	
			// Close the window
			stage.close();
	 
			Platform.runLater(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{						
						connectionManager.openConnection(connectionDetails);
					}
					catch (ConfigurationException e)
					{
						// TODO: show warning dialog for invalid
						logger.error("Cannot open conection {}", connectionDetails.getName(), e);
					}					
				}				
			});			
		}
	}
	
	@FXML
	public void readAndOpenConnection() throws ConfigurationException
	{
		readAndDetectChanges();
		openConnection(editedConnectionDetails);
	}

	// ===============================
	// === Logic =====================
	// ===============================


	public void updateConnectionName()
	{
		if (connectionNameText.getText().isEmpty()
				|| lastGeneratedConnectionName.equals(connectionNameText.getText()))
		{
			final String newName = ConnectionUtils.composeConnectionName(
					editConnectionConnectivityController.getClientIdText().getText(), 
					editConnectionConnectivityController.getBrokerAddressText().getText());
			connectionNameText.setText(newName);
			lastGeneratedConnectionName = newName;
		}
	}
	
	public void onChange()
	{
		if (recordModifications && !emptyConnectionList)
		{					
			if (readAndDetectChanges())
			{
				updateButtons();
				editConnectionConnectivityController.updateClientId(false);
				editConnectionConnectivityController.updateClientIdLength();
				updateConnectionName();		
				editConnectionConnectivityController.updateReconnection();
				editConnectionSecurityController.updateUserAuthentication();
				editConnectionsController.listConnections();
			}
		}				
	}

	private UserInterfaceMqttConnectionDetails readValues()
	{
		final UserInterfaceMqttConnectionDetails connection = new UserInterfaceMqttConnectionDetails();
		connection.setMessageLog(new MessageLog());
		
		// Populate the default for the values we don't display / are not used
		ConfigurationUtils.populateConnectionDefaults(connection);
		
		connection.setName(connectionNameText.getText());
		
		editConnectionConnectivityController.readValues(connection);
		editConnectionOtherController.readValues(connection);
		editConnectionSecurityController.readValues(connection);
		editConnectionMessageLogController.readValues(connection);
		editConnectionPublicationsController.readValues(connection);
		editConnectionSubscriptionsController.readValues(connection);
		editConnectionLastWillController.readValues(connection);			
		
		return connection;
	}
	
	private boolean readAndDetectChanges()
	{
		final ConfiguredMqttConnectionDetails connection = new ConfiguredMqttConnectionDetails(null, readValues());

		// Copy...
		final ConnectionGroupReference group = editedConnectionDetails.getGroup();
		final String id = editedConnectionDetails.getID();
		
		// Set it.. so that comparison is correct...
		connection.setGroup(group);
		connection.setID(id);
		
		boolean changed = !connection.equals(editedConnectionDetails.getSavedValues());
			
		logger.debug("Values read. Changed = " + changed);
		if (changed)
		{
			logger.debug("New value = {}", connection.toString());
			logger.debug("Old value = {}", editedConnectionDetails.getSavedValues().toString());
		}
				
		editedConnectionDetails.setModified(changed);
		editedConnectionDetails.setConnectionDetails(connection);
		
		// ... and override the group because this is not read from this pane
		editedConnectionDetails.setGroup(group);
		editedConnectionDetails.setID(id);
		
		return changed;
	}
	
	public void checkIfOpened(final String id)
	{
		openNewMode = true;
		for (final MqttAsyncConnection mqttConnection : connectionManager.getMqttConnections())
		{
			if (id.equals(mqttConnection.getProperties().getConfiguredProperties().getID()) && mqttConnection.isOpened())
			{
				openNewMode = false;
				existingConnection = mqttConnection;
				connectButton.setText("Close and re-open existing connection");
				break;
			}				
		}
	}

	public void editConnection(final ConfiguredMqttConnectionDetails connectionDetails)
	{	
		synchronized (this)
		{
			this.editedConnectionDetails = connectionDetails;
			
			// Set 'open connection' button mode
			openNewMode = true;
			existingConnection = null;
			connectButton.setText("Open connection");
			
			logger.debug("Editing connection id={} name={}", editedConnectionDetails.getID(), editedConnectionDetails.getName());
			checkIfOpened(connectionDetails.getID());
			
			if (editedConnectionDetails.getName().equals(
					ConnectionUtils.composeConnectionName(editedConnectionDetails.getClientID(), editedConnectionDetails.getServerURI())))
			{
				lastGeneratedConnectionName = editedConnectionDetails.getName();
			}
			else
			{
				lastGeneratedConnectionName = "";
			}
			
			displayConnectionDetails(editedConnectionDetails);		
			editConnectionConnectivityController.updateClientIdLength();
			updateConnectionName();
						
			updateButtons();
		}
	}
	
	private void updateButtons()
	{
		if (editedConnectionDetails != null && editedConnectionDetails.isModified())
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
	
	private void displayConnectionDetails(final ConfiguredMqttConnectionDetails connection)
	{
		ConfigurationUtils.populateConnectionDefaults(connection);
		
		connectionNameText.setText(connection.getName());
		
		editConnectionConnectivityController.displayConnectionDetails(connection);
		editConnectionSecurityController.displayConnectionDetails(connection);
		editConnectionMessageLogController.displayConnectionDetails(connection);
		editConnectionOtherController.displayConnectionDetails(connection);
		editConnectionPublicationsController.displayConnectionDetails(connection);
		editConnectionSubscriptionsController.displayConnectionDetails(connection);
		editConnectionLastWillController.displayConnectionDetails(connection);
		
		connection.setBeingCreated(false);
	}		

	// ===============================
	// === Setters and getters =======
	// ===============================

	public void setConfigurationManager(final IConfigurationManager configurationManager)
	{
		this.configurationManager = configurationManager;
	}

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
	
	public boolean isRecordModifications()	
	{
		return recordModifications;
	}
	
	public void setEmptyConnectionListMode(boolean emptyConnectionList)
	{
		this.emptyConnectionList = emptyConnectionList;
		connectButton.setDisable(emptyConnectionList);
		updateButtons();
	}

	public void setConnectionManager(final MqttConnectionViewManager connectionManager)
	{
		this.connectionManager = connectionManager;
	}

	public TextField getConnectionName()
	{
		return connectionNameText;
	}
	
	public ConfiguredMqttConnectionDetails getEditedConnectionDetails()
	{
		return editedConnectionDetails;
	}

	/**
	 * @return the mainController
	 */
//	public MainController getMainController()
//	{
//		return mainController;
//	}

	public SpyPerspective getPerspective()
	{
		return perspective;
	}

	public void setPerspective(SpyPerspective perspective)
	{
		this.perspective = perspective;
		perspectiveCombo.getSelectionModel().select(perspective);
	}
	
	/**
	 * Sets the event bus.
	 *  
	 * @param eventBus the eventBus to set
	 */
	public void setEventBus(final IKBus eventBus)
	{
		this.eventBus = eventBus;
	}
}
