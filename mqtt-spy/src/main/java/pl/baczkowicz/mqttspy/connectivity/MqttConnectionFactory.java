package pl.baczkowicz.mqttspy.connectivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.ProtocolVersionEnum;
import pl.baczkowicz.mqttspy.configuration.ConfiguredMqttConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.MqttConfigurationManager;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.ui.MqttConnectionViewManager;
import pl.baczkowicz.mqttspy.ui.controllers.EditMqttConnectionController;
import pl.baczkowicz.mqttspy.ui.utils.MqttStylingUtils;
import pl.baczkowicz.mqttspy.utils.ConnectionUtils;
import pl.baczkowicz.mqttspy.utils.MqttUtils;
import pl.baczkowicz.spy.common.generated.ConnectionGroupReference;
import pl.baczkowicz.spy.common.generated.ConnectionReference;
import pl.baczkowicz.spy.connectivity.ConnectionStatus;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.exceptions.ConfigurationException;
import pl.baczkowicz.spy.ui.configuration.ConfiguredConnectionGroupDetails;
import pl.baczkowicz.spy.ui.configuration.IConfigurationManager;
import pl.baczkowicz.spy.ui.connections.IConnectionFactory;
import pl.baczkowicz.spy.ui.controllers.ControlPanelController;
import pl.baczkowicz.spy.ui.controllers.EditConnectionsController;
import pl.baczkowicz.spy.ui.events.CreateNewConnectionEvent;
import pl.baczkowicz.spy.ui.panes.SpyPerspective;
import pl.baczkowicz.spy.ui.properties.ConnectionListItemProperties;
import pl.baczkowicz.spy.ui.properties.ModifiableConnection;
import pl.baczkowicz.spy.ui.utils.FxmlUtils;
import pl.baczkowicz.spy.ui.utils.ImageUtils;

public class MqttConnectionFactory implements IConnectionFactory
{
	private final static Logger logger = LoggerFactory.getLogger(MqttConnectionFactory.class);

	private EditMqttConnectionController editConnectionPaneController;

	private MqttConnectionViewManager connectionManager;

	private IKBus eventBus;

	private IConfigurationManager configurationManager;

	private AnchorPane editConnectionPane;
	
	public static String getIconNameForProtocol(final String protocol)
	{
		return protocol.toLowerCase() + "-icon";
	}
	
	@Override
	public void populateProtocolCell(TableCell<ConnectionListItemProperties, String> cell, String item)
	{
		if (item.contains(MQTT))
		{							
			cell.setGraphic(ImageUtils.createIcon(getIconNameForProtocol(MQTT), 18));
			cell.setText(item.replace("Default", ""));
		}									
		else
		{
			cell.setText(item);
		}			
	}

	public ModifiableConnection newConnection(final String protocol)
	{
		return newConnection();
	}
	
	public static ModifiableConnection newConnection()
	{
//		if (MQTT.equals(protocol))
//		{
			final UserInterfaceMqttConnectionDetails baseConnection = new UserInterfaceMqttConnectionDetails();				
			baseConnection.getServerURI().add("127.0.0.1");
			baseConnection.setClientID(MqttUtils.generateClientIdWithTimestamp(System.getProperty("user.name"), ProtocolVersionEnum.MQTT_DEFAULT));
			baseConnection.setName(ConnectionUtils.composeConnectionName(baseConnection.getClientID(), baseConnection.getServerURI()));
			baseConnection.setAutoConnect(true);
			
			final ConfiguredMqttConnectionDetails connectionDetails = new ConfiguredMqttConnectionDetails(
					true, true, baseConnection);
			connectionDetails.setID(MqttConfigurationManager.generateConnectionId());
			
			return connectionDetails;
		//}
		
		//return null;
	}
	
	public ModifiableConnection duplicateConnection(final ModifiableConnection copyFrom)
	{
		if (copyFrom instanceof UserInterfaceMqttConnectionDetails)
		{
			final ConfiguredMqttConnectionDetails connectionDetails = new ConfiguredMqttConnectionDetails(				
					true, true, (UserInterfaceMqttConnectionDetails) copyFrom);		
			connectionDetails.setID(MqttConfigurationManager.generateConnectionId());
			
			return connectionDetails;
		}
				
		return null;
	}
	
	@Override
	public Collection<AnchorPane> loadControllers(final Object parent)
	{
		final Collection<AnchorPane> items = new ArrayList<AnchorPane>();
		
		items.add(loadController(parent));
		
		return items;
	}
	
	@Override
	public Collection<MenuItem> createMenuItems()
	{
		final Collection<MenuItem> items = new ArrayList<MenuItem>();
		
		items.add(createMenuItemForProtocol(MQTT));
		
		return items;
	}
	
	public MenuItem createMenuItemForProtocol(final String protocol)
	{
		// Populate menu
		final MenuItem newConnection = new MenuItem("Create new " + protocol + " connection");
		newConnection.setGraphic(ImageUtils.createIcon(getIconNameForProtocol(protocol), 18));
		newConnection.setOnAction(new EventHandler<ActionEvent>()
		{			
			@Override
			public void handle(ActionEvent event)
			{
				eventBus.publish(new CreateNewConnectionEvent(protocol));
				// TODO: make an event bus call rather than calling new connection directly
				//newConnection(protocol);				
			}
		});
		
		return newConnection;
	}
	
	public ConnectionListItemProperties createConnectionListItemProperties(final ModifiableConnection connection)
	{
		ConnectionListItemProperties properties = null;
	
		if (connection instanceof ConfiguredMqttConnectionDetails)
		{
			final ConfiguredMqttConnectionDetails mqttConnection = (ConfiguredMqttConnectionDetails) connection;
			
			final ProtocolVersionEnum protocol = mqttConnection.getProtocol();
		
			properties = new ConnectionListItemProperties(
				connection.getName(), 
				(protocol == null ? ProtocolVersionEnum.MQTT_DEFAULT.value() : protocol.value()), 
				mqttConnection.getClientID() + "@" + ConnectionUtils.serverURIsToString(mqttConnection.getServerURI()), 
				mqttConnection.getSSL() != null, 
						mqttConnection.getUserAuthentication() != null);
		}
		
		return properties;
	}	
	
	public void findConnections(final ConfiguredConnectionGroupDetails parentGroup, final List<ModifiableConnection> connections)
	{		
		for (final ConnectionGroupReference reference : parentGroup.getSubgroups())			
		{
			final ConfiguredConnectionGroupDetails groupDetails = (ConfiguredConnectionGroupDetails) reference.getReference();
						
			// Recursive
			findConnections(groupDetails, connections);
		}
		
		for (final ConnectionReference reference : parentGroup.getConnections())			
		{
			final ModifiableConnection connectionDetails = (ModifiableConnection) reference.getReference();
			connections.add(connectionDetails);
		}		
	}
	
	public AnchorPane loadController(final Object parent)
	{
		final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("EditMqttConnectionPane.fxml");
		editConnectionPane = FxmlUtils.loadAnchorPane(loader);
		
		editConnectionPaneController = ((EditMqttConnectionController) loader.getController());

		editConnectionPaneController.setConfigurationManager(configurationManager);
		editConnectionPaneController.setEventBus(eventBus);
		editConnectionPaneController.setConnectionManager(connectionManager);
		editConnectionPaneController.setEditConnectionsController((EditConnectionsController) parent);
		editConnectionPaneController.init();
		
		return editConnectionPane;
	}

	@Override
	public void editConnection(final ModifiableConnection connection)
	{
		if (connection instanceof ConfiguredMqttConnectionDetails)
		{
			editConnectionPaneController.editConnection((ConfiguredMqttConnectionDetails) connection);
		}
	}


	@Override
	public void openConnection(final ModifiableConnection connection)
	{
		if (connection instanceof ConfiguredMqttConnectionDetails)
		{
			editConnectionPaneController.openConnection((ConfiguredMqttConnectionDetails) connection);
		}		
	}
	
	@Override
	public void setRecordModifications(final boolean record)
	{
		// Apply to all controllers
		editConnectionPaneController.setRecordModifications(record);		
	}

	@Override
	public void setPerspective(final SpyPerspective perspective)
	{
		// Apply to all controllers
		editConnectionPaneController.setPerspective(perspective);		
	}


	@Override
	public void setEmptyConnectionListMode(boolean empty)
	{
		// Apply to all controllers
		editConnectionPaneController.setEmptyConnectionListMode(empty);
	}
	
	public void setConfigurationManager(final IConfigurationManager configurationManager)
	{
		this.configurationManager = configurationManager;
	}	
	
	public void setConnectionManager(final MqttConnectionViewManager connectionManager)
	{
		this.connectionManager = connectionManager;
	}
	
	public void setEventBus(final IKBus eventBus)
	{
		this.eventBus = eventBus;
	}

	@Override
	public void setVisible(boolean groupSelected)
	{
		editConnectionPane.setVisible(!groupSelected);		
	}
	

	@Override
	public Button createConnectionButton(final ModifiableConnection connectionDetails)
	{
		if (connectionDetails instanceof UserInterfaceMqttConnectionDetails)
		{
			return createMqttConnectionButton(connectionDetails, connectionManager);
		}	
		
		return null;
	}
	
	private static Button createMqttConnectionButton(final ModifiableConnection connectionDetails, final MqttConnectionViewManager connectionManager)
	{
		MqttAsyncConnection connection = null; 
		for (final MqttAsyncConnection openedConnection : connectionManager.getMqttConnections())
		{					
			if (connectionDetails.getID().equals(openedConnection.getId()))
			{
				connection = openedConnection;
				break;
			}
		}
		
		final Button connectionButton = new Button();
		connectionButton.setFocusTraversable(false);
		
		final String connectionName = connectionDetails.getName();
		
		if (connection != null)
		{
			logger.trace("Button for " + connectionName + " " 
				+ connection.getConnectionStatus() + "/" + connection.isOpening() + "/" + connection.isOpened());
		}
		
		if (connection == null || (!connection.isOpened() && !connection.isOpening()))
		{
			final String buttonText = "Open " + connectionName; 
			connectionButton.getStyleClass().add(MqttStylingUtils.getStyleForMqttConnectionStatus(null));	
			connectionButton.setOnAction(new EventHandler<ActionEvent>()
			{						
				@Override
				public void handle(ActionEvent event)
				{
					try
					{				
						connectionManager.openConnection(connectionDetails);						
						event.consume();
					}
					catch (ConfigurationException e)
					{
						logger.error("Cannot open connection", e);
					}							
				}
			});
			
			connectionButton.setText(buttonText);
		}		
		else if (connection.isOpening())
		{
			showPending("Opening", null, connectionButton, connectionName, MqttConnectionViewManager.createNextAction(null, connection, connectionManager));
		}
		else if (connection.getConnectionStatus() == ConnectionStatus.CONNECTING)
		{
			showPending("Connecting to", connection.getConnectionStatus(), connectionButton, connectionName, MqttConnectionViewManager.createNextAction(connection.getConnectionStatus(), connection, connectionManager));
		}
		else if (connection.getConnectionStatus() != null)
		{
			final String buttonText = ControlPanelController.nextActionTitle.get(connection.getConnectionStatus()) + " " + connectionName; 
			connectionButton.getStyleClass().add(MqttStylingUtils.getStyleForMqttConnectionStatus(connection.getConnectionStatus()));	
			connectionButton.setOnAction(MqttConnectionViewManager.createNextAction(connection.getConnectionStatus(), connection, connectionManager));
			
			connectionButton.setGraphic(null);
			connectionButton.setText(buttonText);
		}		
				
		return connectionButton;
	}
	
	public static void showPending(final String statusText, final ConnectionStatus status, 
			final Button connectionButton, final String connectionName,
			final EventHandler<ActionEvent> action)
	{			
		connectionButton.getStyleClass().add(MqttStylingUtils.getStyleForMqttConnectionStatus(status));	
		connectionButton.setOnAction(action);
		
		final HBox buttonBox = new HBox();			
		final ProgressIndicator buttonProgress = new ProgressIndicator();
		buttonProgress.setMaxSize(15, 15);
					
		buttonBox.getChildren().add(buttonProgress);
		buttonBox.getChildren().add(new Label(" " + statusText + " " + connectionName));

		connectionButton.setGraphic(buttonBox);
		connectionButton.setText(null);
	}
}
