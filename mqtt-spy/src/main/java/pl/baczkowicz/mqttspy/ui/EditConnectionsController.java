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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.ProtocolVersionEnum;
import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.configuration.ConfiguredConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.ui.connections.ConnectionManager;
import pl.baczkowicz.mqttspy.ui.events.EventManager;
import pl.baczkowicz.mqttspy.ui.events.observers.ConnectionStatusChangeObserver;
import pl.baczkowicz.mqttspy.utils.ConnectionUtils;
import pl.baczkowicz.mqttspy.utils.MqttUtils;
import pl.baczkowicz.spy.common.generated.ConnectionGroup;
import pl.baczkowicz.spy.common.generated.ConnectionGroupReference;
import pl.baczkowicz.spy.common.generated.ConnectionReference;
import pl.baczkowicz.spy.configuration.BaseConfigurationUtils;
import pl.baczkowicz.spy.exceptions.ConfigurationException;
import pl.baczkowicz.spy.ui.configuration.ConfiguredConnectionGroupDetails;
import pl.baczkowicz.spy.ui.controls.DragAndDropTreeViewCell;
import pl.baczkowicz.spy.ui.events.observers.ItemsReorderedObserver;
import pl.baczkowicz.spy.ui.panes.SpyPerspective;
import pl.baczkowicz.spy.ui.properties.ConnectionTreeItemProperties;
import pl.baczkowicz.spy.ui.utils.DialogFactory;
import pl.baczkowicz.spy.ui.utils.TooltipFactory;

/**
 * Controller for editing all connections.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class EditConnectionsController extends AnchorPane implements Initializable, ConnectionStatusChangeObserver, ItemsReorderedObserver
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(EditConnectionsController.class);

	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private EditConnectionController editConnectionPaneController;
	
	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private EditConnectionGroupController editConnectionGroupPaneController;
	
	@FXML
	private AnchorPane connectionDetailsPane;
	
	@FXML
	private TreeView<ConnectionTreeItemProperties> connectionList;
	
	@FXML
	private Button duplicateConnectionButton;
	
	@FXML
	private Button deleteConnectionButton;
	
	@FXML
	private Button importConnectionsButton;
	
	@FXML
	private Button applyAllButton;
	
	@FXML
	private Button undoAllButton;
	
	@FXML
	private Label changesDetectedLabel;
	
	private MainController mainController;

	private ConfigurationManager configurationManager;

	private List<ConfiguredConnectionDetails> connections = new ArrayList<ConfiguredConnectionDetails>();

	private EventManager eventManager;

	private ConnectionManager connectionManager;
	
	int lastUsedId = 0;
	
	final ConnectionTreeItemProperties rootItemProperties = new ConnectionTreeItemProperties(lastUsedId++);
	
	final TreeItem<ConnectionTreeItemProperties> rootItem = new TreeItem<ConnectionTreeItemProperties>(rootItemProperties);
	
	private List<ConfiguredConnectionGroupDetails> groups;

	@FXML
	private Node editConnectionPane;

	@FXML
	private Node editConnectionGroupPane;

	// ===============================
	// === Initialisation ============
	// ===============================
	
	public void initialize(URL location, ResourceBundle resources)
	{
		final EditConnectionsController controller = this;
		
		connectionList.setCellFactory(new Callback<TreeView<ConnectionTreeItemProperties>, TreeCell<ConnectionTreeItemProperties>>() 
		{
            @Override
            public TreeCell call(TreeView<ConnectionTreeItemProperties> treeView) 
            {
            	
                return new DragAndDropTreeViewCell(treeView, controller);
            }
        });
		
		duplicateConnectionButton.setDisable(true);
		deleteConnectionButton.setDisable(true);
		
		connectionList.setShowRoot(true);
		connectionList.setRoot(rootItem);
		rootItem.setExpanded(true);
		rootItem.expandedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue)
			{				
				if (!rootItem.isExpanded())
				{
					rootItem.setExpanded(true);
				}				
			}
		});
		
		
		connectionList.getStyleClass().add("connectionList");
		connectionList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue)
			{
				logger.debug("Item selected = " + newValue);
				if (newValue == null)
				{					
					return;
				}
				
				// showSelected();
				updateUIForSelectedItem(((TreeItem<ConnectionTreeItemProperties>) newValue).getValue());
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
						try
						{
							if (getSelectedItem() != null && !getSelectedItem().isGroup())
							{
								// Open the connection
								editConnectionPaneController.createConnection();
							}	
						}
						catch (ConfigurationException e)
						{
							logger.error("Cannot create connection", e);
						}
					}
				}
			}
		});
	}	
	
	public void init()
	{
		connections = configurationManager.getConnections();
		groups = configurationManager.getConnectionGrops();
		rootItemProperties.setGroup(configurationManager.getRootGroup());
		
		eventManager.registerConnectionStatusObserver(this, null);

		editConnectionGroupPaneController.setMainController(mainController);
		editConnectionGroupPaneController.setEditConnectionsController(this);
		editConnectionGroupPaneController.init();

		editConnectionPaneController.setConfigurationManager(configurationManager);
		editConnectionPaneController.setConnectionManager(connectionManager);
		editConnectionPaneController.setMainController(mainController);
		editConnectionPaneController.setEditConnectionsController(this);
		editConnectionPaneController.init();

		editConnectionPaneController.getConnectionName().textProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable,
					Object oldValue, Object newValue)
			{
				if (editConnectionPaneController
						.isRecordModifications())
				{
					connectionNameChanged();
				}
			}

		});

		editConnectionPaneController.setRecordModifications(false);
		listConnections();
		editConnectionPaneController.setRecordModifications(true);
	}

	/**
	 * TreeItem contains ConnectionTreeItemProperties
	 * 
	 * ConnectionTreeItemProperties contains either 
	 * 		ConfiguredConnectionDetails
	 * 		ConfiguredConnectionGroupDetails
	 * 
	 * ConfiguredConnectionDetails extends the connection object
	 * ConfiguredConnectionGroupDetails extends the group object
	 * 
	 * 
	 * @param groups
	 * @param connectionList
	 */
	private void populateConnections(
			final List<ConfiguredConnectionGroupDetails> groups, 
			final List<ConfiguredConnectionDetails> connectionList)
	{
		rootItem.getChildren().clear();
		rootItemProperties.getChildren().clear();
		
		final List<ConnectionTreeItemProperties> treeItemGroupProperties = new ArrayList<>();
		final List<ConnectionTreeItemProperties> treeItemConnectionProperties = new ArrayList<>();

		// Builds the tree item properties		
		buildTree(rootItemProperties, treeItemGroupProperties, treeItemConnectionProperties);
		
		// Adds tree items to the given root
		addToTree(rootItem, rootItemProperties);
	}
	
	private void buildTree(ConnectionTreeItemProperties treeItem, 
			final List<ConnectionTreeItemProperties> treeItemGroupProperties, 
			final List<ConnectionTreeItemProperties> treeItemConnectionProperties)
	{
		// This is always called for a group
		
		final ConfiguredConnectionGroupDetails group = (ConfiguredConnectionGroupDetails) treeItem.getGroup();

		for (final ConnectionGroupReference reference : group.getSubgroups()) 
		{
			final ConfiguredConnectionGroupDetails subgroup = (ConfiguredConnectionGroupDetails) reference.getReference();
			
			// Create new tree item for the subgroup
			final ConnectionTreeItemProperties subgroupTreeItemProperties = new ConnectionTreeItemProperties(lastUsedId++);
			subgroupTreeItemProperties.setGroup(subgroup);
			
			// Add the tree item subgroup to the parent tree item
			treeItemGroupProperties.add(subgroupTreeItemProperties);
			
			// Set the parent/child relationship for the tree items (this is already set on the configuration objects)
			subgroupTreeItemProperties.setParent(treeItem);
			treeItem.addChild(subgroupTreeItemProperties);
			
			// Recursive
			buildTree(subgroupTreeItemProperties, treeItemGroupProperties, treeItemConnectionProperties);
		}
		
		for (final ConnectionReference reference : group.getConnections()) 
		{
			final ConfiguredConnectionDetails connection = (ConfiguredConnectionDetails) reference.getReference();
			
			// Create new tree item for the connection
			final ConnectionTreeItemProperties connectionTreeItemProperties = new ConnectionTreeItemProperties(lastUsedId++);
			connectionTreeItemProperties.setConnection(connection);
			
			// Add the tree item connection to the parent tree item
			treeItemConnectionProperties.add(connectionTreeItemProperties);
			
			// Set the parent/child relationship for the tree items (this is already set on the configuration objects)
			connectionTreeItemProperties.setParent(treeItem);
			treeItem.addChild(connectionTreeItemProperties);
		}
	}
	
	private boolean addToTree(TreeItem<ConnectionTreeItemProperties> treeItem, final ConnectionTreeItemProperties properties)
	{
		boolean added = false;
		for (final ConnectionTreeItemProperties item : properties.getChildren()) 
		{
			final TreeItem<ConnectionTreeItemProperties> newTreeItem = new TreeItem<ConnectionTreeItemProperties>(item);						
			
			if (item.isGroup())
			{
				if (addToTree(newTreeItem, item))			
				{
					newTreeItem.setExpanded(true);
				}			
			}
			
			treeItem.getChildren().add(newTreeItem);
			added = true;
		}
		
		return added;
	}
	
//	private void showSelected()
//	{
//		synchronized (connections)
//		{
//			updateUIForSelectedItem();
//		}
//	}
	
	public void updateUIForSelectedItem()	
	{
		updateUIForSelectedItem(getSelectedItem());
	}
	
	public void updateUIForSelectedItem(final ConnectionTreeItemProperties selected)	
	{
		if (selected == null)
		{
			logger.debug("Selection is null");
			selectFirst();
			return;
		}
		
		if (connections.isEmpty() && groups.isEmpty())
		{
			duplicateConnectionButton.setDisable(true);
			deleteConnectionButton.setDisable(true);
			editConnectionPaneController.setEmptyConnectionListMode(true);
		}
		else 
		{
			// This this is not the default group (first one on the list)
			if (!selected.isGroup() || !selected.getGroup().getID().equals(BaseConfigurationUtils.DEFAULT_GROUP))
			{
				deleteConnectionButton.setDisable(false);
			}
			else
			{
				deleteConnectionButton.setDisable(true);
			}
						
			duplicateConnectionButton.setDisable(selected.isGroup());
			editConnectionPaneController.setEmptyConnectionListMode(false);
			
			editConnectionPane.setVisible(!selected.isGroup());
			editConnectionGroupPane.setVisible(selected.isGroup());
			
			if (selected.isGroup())
			{
				editConnectionGroupPaneController.setRecordModifications(false);
				editConnectionGroupPaneController.editConnectionGroup((ConfiguredConnectionGroupDetails) selected.getGroup(), selected.getChildren());
				editConnectionGroupPaneController.setRecordModifications(true);			
			}
			else if (!((ConfiguredConnectionDetails) selected.getConnection()).isBeingCreated())
			{			
				logger.trace("Editing connection {}", selected.getName());
				
				editConnectionPaneController.setRecordModifications(false);
				editConnectionPaneController.editConnection((ConfiguredConnectionDetails) selected.getConnection());
				editConnectionPaneController.setRecordModifications(true);							
			}
		}
	}

	/**
	 * This links the group with its parent group - done on the configuration objects.
	 * 
	 * @param groupDetails
	 * @param parent
	 */
	private void addToParentGroup(final ConfiguredConnectionGroupDetails groupDetails, final ConfiguredConnectionGroupDetails parent)
	{
		groupDetails.setGroup(new ConnectionGroupReference(parent));
		parent.getSubgroups().add(new ConnectionGroupReference(groupDetails));
	}
	
	/**
	 * This links the connection with its parent group - done on the configuration objects.
	 * 
	 * @param groupDetails
	 * @param parent
	 */
	private void addToParentGroup(final ConfiguredConnectionDetails connectionDetails, final ConfiguredConnectionGroupDetails parent)
	{
		connectionDetails.setGroup(new ConnectionGroupReference(parent));
		parent.getConnections().add(new ConnectionReference(connectionDetails));
	}
		
	// ===============================
	// === FXML ======================
	// ===============================

	@FXML
	public void newMqttConnection()
	{
		final UserInterfaceMqttConnectionDetails baseConnection = new UserInterfaceMqttConnectionDetails();				
		baseConnection.getServerURI().add("127.0.0.1");
		baseConnection.setClientID(MqttUtils.generateClientIdWithTimestamp(System.getProperty("user.name"), ProtocolVersionEnum.MQTT_DEFAULT));
		baseConnection.setName(ConnectionUtils.composeConnectionName(baseConnection.getClientID(), baseConnection.getServerURI()));
		baseConnection.setAutoConnect(true);
		
		final ConfiguredConnectionDetails connectionDetails = new ConfiguredConnectionDetails(
				true, true, baseConnection);
		connectionDetails.setID(ConfigurationManager.generateConnectionId());
		
		addToParentGroup(connectionDetails, configurationManager.getRootGroup());
		
		connections.add(connectionDetails);
		newConnectionMode(connectionDetails);
	}
	
	@FXML
	private void duplicateConnection()
	{
		final ConnectionGroupReference parent = ((ConfiguredConnectionDetails) getSelectedItem().getConnection()).getGroup();
		((ConfiguredConnectionDetails) getSelectedItem().getConnection()).setGroup(null);
		
		final ConfiguredConnectionDetails connectionDetails = new ConfiguredConnectionDetails(				
				true, true, (UserInterfaceMqttConnectionDetails) getSelectedItem().getConnection());		
		connectionDetails.setID(ConfigurationManager.generateConnectionId());
		
		((ConfiguredConnectionDetails) getSelectedItem().getConnection()).setGroup(parent);
		addToParentGroup(connectionDetails, (ConfiguredConnectionGroupDetails) parent.getReference());
		
		connections.add(connectionDetails);
		newConnectionMode(connectionDetails);
	}
	
	@FXML
	private void newGroup()
	{
		try
		{
			final Optional<String> result = DialogFactory.createInputDialog(
					connectionList.getScene().getWindow(), "New connection group", "Please enter the connection group name: ");
			
			if (result.isPresent())
			{
				final ConnectionGroup group = new ConnectionGroup(ConfigurationManager.generateConnectionGroupId(), 
						result.get(), new ArrayList(), new ArrayList()); 
				final ConfiguredConnectionGroupDetails groupDetails = new ConfiguredConnectionGroupDetails(group, true);
				
				addToParentGroup(groupDetails, configurationManager.getRootGroup());
				
				groups.add(groupDetails);
				
				//populateConnections(groups, connections);
				listConnections();
				selectGroup(groupDetails);			
			}
		}
		catch (Exception e)
		{
			logger.error("Cannot create a new group", e);
		}
	}
	
	@FXML
	private void deleteConnection()
	{
		final ConnectionTreeItemProperties selected = getSelectedItem();
		if (getSelectedItem().isGroup())
		{
			final ConfiguredConnectionGroupDetails group = (ConfiguredConnectionGroupDetails) getSelectedItem().getGroup(); 
			
			final String groupName = group.getName();
			final int childrenCount = selected.getChildren().size();
			
			if (DialogFactory.createQuestionDialog("Deleting connection group", 
					"Are you sure you want to delete connection group '" + groupName + "'"
							+ (childrenCount == 0 ? "" : " and all subitems")
							+ "?" + " This cannot be undone.", false).get() == ButtonType.YES)
			{
				group.removeFromGroup();
				groups.remove(group);
				
				listConnections();			
				selectFirst();
				
				logger.debug("Saving all connections");
				if (configurationManager.saveConfiguration())
				{
					// TODO: for some reason, this is not shown
					TooltipFactory.createTooltip(deleteConnectionButton, "Connection group " + groupName + " deleted.");
				}
			}
		}
		else
		{
			final ConfiguredConnectionDetails connection = (ConfiguredConnectionDetails) getSelectedItem().getConnection(); 
			connection.setDeleted(true);
			
			final String connectionName = connection.getName();
			
			if (DialogFactory.createQuestionDialog(
					"Deleting connection", 
					"Are you sure you want to delete connection '" + connectionName + "'?" + " This cannot be undone.", 
					false).get() == ButtonType.YES)
			{	
				editConnectionPaneController.setRecordModifications(false);
				
				connection.removeFromGroup();
				connections.remove(connection);
				
				listConnections();			
				selectFirst();
				editConnectionPaneController.setRecordModifications(true);
					
				logger.debug("Saving all connections");
				if (configurationManager.saveConfiguration())
				{
					// TODO: for some reason, this is not shown
					TooltipFactory.createTooltip(deleteConnectionButton, "Connection " + connectionName + " deleted.");
				}
			}
		}
	}
	
	@FXML
	private void undoAll()
	{
		// TODO: how to restore all parent-child relationships?
		
		final List<ConfiguredConnectionDetails> allConnections = new ArrayList<>();
		allConnections.addAll(connections);
		
		for (final ConfiguredConnectionDetails connection : allConnections)
		{
			if (connection.isNew())
			{
				connection.removeFromGroup();
				connections.remove(connection);
			}
			else
			{
				connection.undoAll();
			}
		}
		
		final List<ConfiguredConnectionGroupDetails> allGroups = new ArrayList<>();
		allGroups.addAll(groups);
		
		for (final ConfiguredConnectionGroupDetails group : allGroups)
		{			
			if (group.isNew())
			{
				group.removeFromGroup();
				groups.remove(group);
			}
			else
			{
				group.undoAll();
			}
		}
		
		listConnections();
	}
	
	@FXML
	private void applyAll()
	{
		for (final ConfiguredConnectionDetails connection : connections)
		{
			connection.apply();
		}
		for (final ConfiguredConnectionGroupDetails group : groups)
		{
			group.apply();
		}
		
		listConnections();
		
		logger.debug("Saving all connections & groups");
		if (configurationManager.saveConfiguration())
		{
			TooltipFactory.createTooltip(applyAllButton, "Changes for all connections and groups have been saved.");
		}
	}
	
	@FXML
	private void importConnections()
	{
		// TODO: import
	}
	
	// ===============================
	// === Logic =====================
	// ===============================

	private void selectFirst()
	{
		logger.info("Selecting first item...");
		// Select the first item if any connections present
		if (connections.size() > 0)
		{
			connectionList.getSelectionModel().select(0);
		}
	}
	
	public void selectConnection(final ConfiguredConnectionDetails connection)
	{
		selectConnection(rootItem, connection);
	}
	
	public void selectGroup(final ConfiguredConnectionGroupDetails group)
	{
		selectGroup(rootItem, group);
	}
	
	public void selectConnection(final TreeItem<ConnectionTreeItemProperties> parentItem, final ConfiguredConnectionDetails connection)
	{
		for (final TreeItem<ConnectionTreeItemProperties> treeItem : parentItem.getChildren()) 
		{
			final ConnectionTreeItemProperties item = treeItem.getValue();
			if (!item.isGroup() && item.getConnection().equals(connection))
			{
				connectionList.getSelectionModel().select(treeItem);
				updateUIForSelectedItem(item);
				return;
			}
			
			selectConnection(treeItem, connection);
		}
	}
	
	public void selectGroup(final TreeItem<ConnectionTreeItemProperties> parentItem, final ConfiguredConnectionGroupDetails group)
	{
		for (final TreeItem<ConnectionTreeItemProperties> treeItem : parentItem.getChildren()) 
		{
			final ConnectionTreeItemProperties item = treeItem.getValue();
			if (item.isGroup() && item.getGroup().equals(group))
			{
				connectionList.getSelectionModel().select(treeItem);
				updateUIForSelectedItem(item);
				return;
			}
			
			selectGroup(treeItem, group);
		}
	}
	
	private ConnectionTreeItemProperties getSelectedItem()
	{
		if (connectionList.getSelectionModel().getSelectedItem() == null)
		{
			return null;
		}
		
		return connectionList.getSelectionModel().getSelectedItem().getValue();
	}
	
	public void listConnections()
	{
		final TreeItem<ConnectionTreeItemProperties> selectedItem = connectionList.getSelectionModel().getSelectedItem();
		ConnectionTreeItemProperties selected = null;
		
		if (selectedItem != null)
		{
			selected = selectedItem.getValue();			
		}

		applyAllButton.setDisable(true);
		undoAllButton.setDisable(true);
		setChangesInfoLabelVisibility(false);
		
		populateConnections(groups, connections);
		
		for (int i = 0; i < connections.size(); i++)
		{	
			if (connections.get(i).isModified() || connections.get(i).isGroupingModified())
			{
				applyAllButton.setDisable(false);
				undoAllButton.setDisable(false);
				setChangesInfoLabelVisibility(true);
				break;
			}
		}
		for (int i = 0; i < groups.size(); i++)
		{	
			if (groups.get(i).isModified() || groups.get(i).isGroupingModified())
			{
				applyAllButton.setDisable(false);
				undoAllButton.setDisable(false);
				setChangesInfoLabelVisibility(true);
				break;
			}
		}
		
		// Reselect
		if (selected != null)
		{
			if (selected.isGroup())
			{
				selectGroup((ConfiguredConnectionGroupDetails) selected.getGroup());
			}
			else
			{
				selectConnection((ConfiguredConnectionDetails) selected.getConnection());
			}
			//connectionList.getSelectionModel().select(selected);
		}
		else
		{
			logger.debug("No selection present");
			selectFirst();
		}
		updateUIForSelectedItem();
	}	
	
	public void setChangesInfoLabelVisibility(final boolean visible)
	{
		if (visible)
		{
			AnchorPane.setBottomAnchor(connectionList, 98.0);			
		}
		else
		{
			AnchorPane.setBottomAnchor(connectionList, 85.0);
		}
		changesDetectedLabel.setVisible(visible);
	}
	
	protected void connectionNameChanged()
	{
		if (getSelectedItem() != null)
		{
			final String newName = editConnectionPaneController.getConnectionName().getText();
			getSelectedItem().getConnection().setName(newName);
			listConnections();
		}
	}
	
	private void newConnectionMode(final ConfiguredConnectionDetails createdConnection)
	{	
		editConnectionPaneController.setRecordModifications(false);		
		listConnections();
		selectConnection(rootItem, createdConnection);
		editConnectionPaneController.editConnection(createdConnection);
		editConnectionPaneController.setRecordModifications(true);
	}
	
	public void openConnection(final ConfiguredConnectionDetails connectionDetails)
	{
		editConnectionPaneController.openConnection(connectionDetails);
	}
	
	// ===============================
	// === Setters and getters =======
	// ===============================

	public void setMainController(MainController mainController)
	{
		this.mainController = mainController;
	}
	
	public void setConfigurationManager(final ConfigurationManager configurationManager)
	{
		this.configurationManager = configurationManager;
	}	

	public void setEventManager(final EventManager eventManager)
	{
		this.eventManager = eventManager;		
	}

	@Override
	public void onConnectionStatusChanged(final MqttAsyncConnection changedConnection)
	{
		if (getSelectedItem() != null)
		{
			//showSelected();
			updateUIForSelectedItem();
		}
	}
	
	public void setConnectionManager(final ConnectionManager connectionManager)
	{
		this.connectionManager = connectionManager;
	}

	@Override
	public void onItemsReordered()
	{
		listConnections();		
	}
	
	public void setPerspective(final SpyPerspective perspective)
	{
		editConnectionPaneController.setPerspective(perspective);
	}
}
