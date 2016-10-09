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
package pl.baczkowicz.spy.ui.controllers;

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
import javafx.scene.control.MenuButton;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.common.generated.ConnectionGroup;
import pl.baczkowicz.spy.common.generated.ConnectionGroupReference;
import pl.baczkowicz.spy.common.generated.ConnectionReference;
import pl.baczkowicz.spy.configuration.BaseConfigurationUtils;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.ui.configuration.IConfigurationManager;
import pl.baczkowicz.spy.ui.configuration.ConfiguredConnectionGroupDetails;
import pl.baczkowicz.spy.ui.connections.IConnectionFactory;
import pl.baczkowicz.spy.ui.controls.DragAndDropTreeViewCell;
import pl.baczkowicz.spy.ui.events.ConnectionNameChangedEvent;
import pl.baczkowicz.spy.ui.events.ConnectionStatusChangeEvent;
import pl.baczkowicz.spy.ui.events.CreateNewConnectionEvent;
import pl.baczkowicz.spy.ui.events.observers.ItemsReorderedObserver;
import pl.baczkowicz.spy.ui.panes.SpyPerspective;
import pl.baczkowicz.spy.ui.properties.ConnectionTreeItemProperties;
import pl.baczkowicz.spy.ui.properties.ModifiableConnection;
import pl.baczkowicz.spy.ui.threading.SimpleRunLaterExecutor;
import pl.baczkowicz.spy.ui.utils.DialogFactory;
import pl.baczkowicz.spy.ui.utils.TooltipFactory;

/**
 * Controller for editing all connections.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class EditConnectionsController extends AnchorPane implements Initializable, ItemsReorderedObserver
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(EditConnectionsController.class);
	
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
	
	private IConfigurationManager configurationManager;

	private List<ModifiableConnection> connections = new ArrayList<>();

	private IKBus eventBus;

	int lastUsedId = 0;
	
	final ConnectionTreeItemProperties rootItemProperties = new ConnectionTreeItemProperties(lastUsedId++);
	
	final TreeItem<ConnectionTreeItemProperties> rootItem = new TreeItem<ConnectionTreeItemProperties>(rootItemProperties);
	
	private List<ConfiguredConnectionGroupDetails> groups;
	
	private IConnectionFactory connectionFactory;

	@FXML
	private Node editConnectionGroupPane;

	@FXML
	private MenuButton newConnectionButton;

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
						if (getSelectedItem() != null && !getSelectedItem().isGroup())
						{
							// Open the connection
							connectionFactory.openConnection(getSelectedItem().getConnection());
						}	
					}
				}
			}
		});
	}	
	
	public void init()
	{
		connections = (List<ModifiableConnection>) configurationManager.getConnections();

		newConnectionButton.getItems().addAll(0, connectionFactory.createMenuItems());
		
		groups = configurationManager.getConnectionGrops();
		rootItemProperties.setGroup(configurationManager.getRootGroup());
		
		eventBus.subscribe(this, this::onConnectionStatusChanged, ConnectionStatusChangeEvent.class, new SimpleRunLaterExecutor());
		eventBus.subscribe(this, this::onNewConnection, CreateNewConnectionEvent.class);

		editConnectionGroupPaneController.setEditConnectionsController(this);
		editConnectionGroupPaneController.setConnectionFactory(connectionFactory);
		editConnectionGroupPaneController.init();
		
		eventBus.subscribe(this, this::connectionNameChanged, ConnectionNameChangedEvent.class);
		
		connectionDetailsPane.getChildren().addAll(connectionFactory.loadControllers(this));

		connectionFactory.setRecordModifications(false);
		listConnections();
		connectionFactory.setRecordModifications(true);
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
			final List<ConfiguredConnectionGroupDetails> groups
			//, 
			//final List<ConfiguredMqttConnectionDetails> connectionList
			)
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
			final ModifiableConnection connection = (ModifiableConnection) reference.getReference();
			
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
	
	private static boolean addToTree(TreeItem<ConnectionTreeItemProperties> treeItem, final ConnectionTreeItemProperties properties)
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
			connectionFactory.setEmptyConnectionListMode(true);
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
			connectionFactory.setEmptyConnectionListMode(false);
			
			connectionFactory.setVisible(selected.isGroup());			
			editConnectionGroupPane.setVisible(selected.isGroup());
			
			if (selected.isGroup())
			{
				editConnectionGroupPaneController.setRecordModifications(false);
				editConnectionGroupPaneController.editConnectionGroup((ConfiguredConnectionGroupDetails) selected.getGroup(), selected.getChildren());
				editConnectionGroupPaneController.setRecordModifications(true);			
			}
			else if (!selected.getConnection().isBeingCreated())
			{			
				logger.trace("Editing connection {}", selected.getName());
				
				connectionFactory.setRecordModifications(false);
				connectionFactory.editConnection(selected.getConnection());
				connectionFactory.setRecordModifications(true);							
			}
		}
	}

	/**
	 * This links the group with its parent group - done on the configuration objects.
	 * 
	 * @param groupDetails
	 * @param parent
	 */
	private static void addToParentGroup(final ConfiguredConnectionGroupDetails groupDetails, final ConfiguredConnectionGroupDetails parent)
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
	private static void addToParentGroup(final ModifiableConnection connectionDetails, final ConfiguredConnectionGroupDetails parent)
	{
		connectionDetails.setGroup(new ConnectionGroupReference(parent));
		parent.getConnections().add(new ConnectionReference(connectionDetails));
	}
		
	// ===============================
	// === FXML ======================
	// ===============================

	// TODO: is that really FXML?
	// @FXML
	// TODO: turn that into a kbus event
	
	public void onNewConnection(final CreateNewConnectionEvent event)
	{
		newConnection(event.getProtocol());
	}
	
	public void newConnection(final String protocol)
	{
		final ModifiableConnection connectionDetails = connectionFactory.newConnection(protocol);
		
		addToParentGroup(connectionDetails, configurationManager.getRootGroup());
		
		connections.add(connectionDetails);
		newConnectionMode(connectionDetails);
	}
	
	@FXML
	private void duplicateConnection()
	{
		final ConnectionGroupReference parent = getSelectedItem().getConnection().getGroup();
		getSelectedItem().getConnection().setGroup(null);
		
		final ModifiableConnection connectionDetails = connectionFactory.duplicateConnection(getSelectedItem().getConnection());
		
		if (connectionDetails != null)
		{
			getSelectedItem().getConnection().setGroup(parent);
			addToParentGroup(connectionDetails, (ConfiguredConnectionGroupDetails) parent.getReference());
			
			connections.add(connectionDetails);
			newConnectionMode(connectionDetails);
		}
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
				final ConnectionGroup group = new ConnectionGroup(configurationManager.generateConnectionGroupId(), 
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
			final ModifiableConnection connection = getSelectedItem().getConnection(); 
			connection.setDeleted(true);
			
			final String connectionName = connection.getName();
			
			if (DialogFactory.createQuestionDialog(
					"Deleting connection", 
					"Are you sure you want to delete connection '" + connectionName + "'?" + " This cannot be undone.", 
					false).get() == ButtonType.YES)
			{	
				connectionFactory.setRecordModifications(false);
				
				connection.removeFromGroup();
				connections.remove(connection);
				
				listConnections();			
				selectFirst();
				connectionFactory.setRecordModifications(true);
					
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
		
		final List<ModifiableConnection> allConnections = new ArrayList<>();
		allConnections.addAll(connections);
		
		for (final ModifiableConnection connection : allConnections)
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
		if (configurationManager.isConfigurationWritable())
		{
			for (final ModifiableConnection connection : connections)
			{
				connection.apply();
			}
			for (final ModifiableConnection group : groups)
			{
				group.apply();
			}
			
			listConnections();
				
			logger.debug("Saving all connections & groups");
			if (configurationManager.saveConfiguration())
			{
				TooltipFactory.createTooltip(applyAllButton, "Changes for all connections and groups have been saved.");
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
	
	public void selectConnection(final ModifiableConnection connection)
	{
		selectConnection(rootItem, connection);
	}
	
	public void selectGroup(final ConfiguredConnectionGroupDetails group)
	{
		selectGroup(rootItem, group);
	}
	
	public void selectConnection(final TreeItem<ConnectionTreeItemProperties> parentItem, final ModifiableConnection connection)
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
		
		populateConnections(groups/*, connections*/);
		
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
				selectConnection(selected.getConnection());
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
	
	protected void connectionNameChanged(final ConnectionNameChangedEvent event)
	{
		if (getSelectedItem() != null)
		{
			final String newName = event.getName();
			getSelectedItem().getConnection().setName(newName);
			listConnections();
		}
	}
	
	private void newConnectionMode(final ModifiableConnection createdConnection)
	{	
		connectionFactory.setRecordModifications(false);		
		listConnections();
		selectConnection(rootItem, createdConnection);
		connectionFactory.editConnection(createdConnection);
		connectionFactory.setRecordModifications(true);
	}
	
	public void openConnection(final ModifiableConnection connectionDetails)
	{
		connectionFactory.openConnection(connectionDetails);
	}
	
	// ===============================
	// === Setters and getters =======
	// ===============================
	
	public void setConfigurationManager(final IConfigurationManager configurationManager)
	{
		this.configurationManager = configurationManager;
	}	

	public void onConnectionStatusChanged(final ConnectionStatusChangeEvent event)
	{
		if (getSelectedItem() != null)
		{
			//showSelected();
			updateUIForSelectedItem();
		}
	}

	@Override
	public void onItemsReordered()
	{
		listConnections();		
	}
	
	public void setPerspective(final SpyPerspective perspective)
	{
		connectionFactory.setPerspective(perspective);
	}
	
	public void setEventBus(final IKBus eventBus)
	{
		this.eventBus = eventBus;
	}

	public void setConnectionFactory(final IConnectionFactory connectionFactory)
	{
		this.connectionFactory = connectionFactory;
	}
}
