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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.MessageLog;
import pl.baczkowicz.mqttspy.common.generated.MessageLogEnum;
import pl.baczkowicz.mqttspy.configuration.ConfiguredMqttConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.connectivity.BaseMqttSubscription;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnectionRunnable;
import pl.baczkowicz.mqttspy.connectivity.MqttRuntimeConnectionProperties;
import pl.baczkowicz.mqttspy.connectivity.handlers.MqttCallbackHandler;
import pl.baczkowicz.mqttspy.connectivity.handlers.MqttDisconnectionResultHandler;
import pl.baczkowicz.mqttspy.connectivity.handlers.MqttEventHandler;
import pl.baczkowicz.mqttspy.logger.MqttMessageLogger;
import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.ui.controllers.MqttConnectionController;
import pl.baczkowicz.mqttspy.ui.events.queuable.UIEventHandler;
import pl.baczkowicz.mqttspy.ui.events.queuable.connectivity.MqttConnectionAttemptFailureEvent;
import pl.baczkowicz.mqttspy.ui.events.queuable.connectivity.MqttDisconnectionAttemptFailureEvent;
import pl.baczkowicz.mqttspy.ui.scripts.InteractiveMqttScriptManager;
import pl.baczkowicz.mqttspy.ui.utils.ConnectivityUtils;
import pl.baczkowicz.mqttspy.ui.utils.DialogUtils;
import pl.baczkowicz.spy.common.generated.UserCredentials;
import pl.baczkowicz.spy.connectivity.ConnectionStatus;
import pl.baczkowicz.spy.connectivity.ReconnectionManager;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.exceptions.ConfigurationException;
import pl.baczkowicz.spy.exceptions.SpyException;
import pl.baczkowicz.spy.formatting.FormattingManager;
import pl.baczkowicz.spy.ui.IConnectionViewManager;
import pl.baczkowicz.spy.ui.configuration.IConfigurationManager;
import pl.baczkowicz.spy.ui.configuration.UiProperties;
import pl.baczkowicz.spy.ui.connections.IUiConnection;
import pl.baczkowicz.spy.ui.controllers.IConnectionController;
import pl.baczkowicz.spy.ui.events.queuable.EventQueueManager;
import pl.baczkowicz.spy.ui.properties.ModifiableConnection;
import pl.baczkowicz.spy.ui.stats.StatisticsManager;
import pl.baczkowicz.spy.ui.utils.DialogFactory;
import pl.baczkowicz.spy.ui.utils.TabUtils;

/**
 * Class for managing connection tabs.
 */
public class MqttConnectionViewManager implements IConnectionViewManager
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(MqttConnectionViewManager.class);
	
	/** Global event bus. */
	private final IKBus eventBus;
	
	/** Global configuration manager. */
	private final IConfigurationManager configurationManager;
	
	// TODO: not sure this is needed
	/** Map of connections and their IDs. */
	private Map<String, MqttAsyncConnection> connections = new HashMap<>();
	
	/** Map of connections and their connection controllers. */
	private final Map<MqttAsyncConnection, MqttConnectionController> connectionControllersMapping = new HashMap<>();

	/** Map of connections and their tabs. */
	private final Map<MqttAsyncConnection, Tab> connectionTabs = new HashMap<>();
	
	/** Map of connection controllers and their subscription managers. */
	private final Map<MqttConnectionController, MqttSubscriptionViewManager> subscriptionManagers = new HashMap<>();
	
	private Stage parentStage;
	
	/** UI event queue. */
	private final EventQueueManager<FormattedMqttMessage> uiEventQueue;
	
	private MqttViewManager viewManager;

	/** Reconnection manager. */
	private ReconnectionManager reconnectionManager;

	private Set<MqttConnectionController> offlineConnectionControllers = new HashSet<>();

	public MqttConnectionViewManager(final IKBus eventBus, final StatisticsManager statisticsManager, 
			final IConfigurationManager configurationManager)
	{
		this.uiEventQueue = new EventQueueManager<FormattedMqttMessage>();
		
		this.eventBus = eventBus;
		this.configurationManager = configurationManager;
		
		this.reconnectionManager = new ReconnectionManager();
		new Thread(reconnectionManager).start();
		
		new Thread(new UIEventHandler(uiEventQueue, eventBus)).start();
	}
	
	public void openConnection(final ModifiableConnection configuredConnectionDetails) throws ConfigurationException
	{
		// Note: this is not a complete ConfiguredConnectionDetails copy but ConnectionDetails copy - any user credentials entered won't be stored in config
		final ConfiguredMqttConnectionDetails connectionDetails = new ConfiguredMqttConnectionDetails();
		//configuredConnectionDetails.copyTo(connectionDetails);
		connectionDetails.setConnectionDetails(configuredConnectionDetails);
		connectionDetails.setID(configuredConnectionDetails.getID());			
		
		final boolean cancelled = completeUserAuthenticationCredentials(connectionDetails, parentStage);		
		
		if (!cancelled)
		{
			final String validationResult = ConnectivityUtils.validateConnectionDetails(connectionDetails, true);
			if (validationResult != null)
			{
				DialogFactory.createWarningDialog("Invalid value detected", validationResult);
			}
			else
			{
				try
				{
					final MqttRuntimeConnectionProperties connectionProperties = new MqttRuntimeConnectionProperties(connectionDetails);
					new Thread(new Runnable()
					{					
						@Override
						public void run()
						{
							viewManager.loadConnectionTab(connectionProperties);					
						}
					}).start();											
				}
				catch (ConfigurationException e)
				{
					logger.error("Cannot create connection properties", e);
					DialogFactory.createExceptionDialog("Invalid configuration detected", e);
				}
			}
		}
	}	

	private boolean completeUserAuthenticationCredentials(final UserInterfaceMqttConnectionDetails connectionDetails, final Stage stage)
	{
		if (connectionDetails.getUserAuthentication() != null)
		{
			// Copy so that we don't store it in the connection and don't save those values
			final UserCredentials userCredentials = new UserCredentials();
			connectionDetails.getUserCredentials().copyTo(userCredentials);
			
			// Check if ask for username or password, and then override existing values if confirmed
			if (connectionDetails.getUserAuthentication().isAskForPassword() || connectionDetails.getUserAuthentication().isAskForUsername())
			{
				// Password is decoded and encoded in this utility method
				if (!DialogUtils.createMqttUsernameAndPasswordDialog(stage, connectionDetails.getName(), userCredentials))
				{
					return true;
				}
			}
			
			// Settings user credentials so they can be validated and passed onto the MQTT client library			
			connectionDetails.setUserCredentials(userCredentials);
		}
		
		return false;
	}	
	
	/**
	 * Gets a collection of all connections.
	 * 
	 * @return Collection of MqttAsyncConnection instances
	 */
	public Collection<MqttAsyncConnection> getMqttConnections()
	{
		// TODO: needs to use the connections list, as the controllers are populated later,
		// so opening doesn't work properly
		return connections.values();
		//return connectionControllers.keySet();
	}
	
	/**
	 * Gets a collection of all connections.
	 * 
	 * @return Collection of MqttAsyncConnection instances
	 */
	public Collection<IUiConnection> getConnections()
	{
		// TODO: needs to use the connections list, as the controllers are populated later,
		// so opening doesn't work properly
		return new ArrayList<>(connections.values());
		//return connectionControllers.keySet();
	}
	
	/**
	 * Disconnects all connections.
	 */
	public void disconnectAll()
	{
		for (final MqttAsyncConnection connection : getMqttConnections())
		{
			disconnectFromBroker(connection);
		}
	}
	
	/**
	 * Disconnects and closes the tab of the given connection.
	 * 
	 * @param connection The connection to close
	 */
	public void disconnectAndCloseTab(final MqttAsyncConnection connection)
	{		
		disconnectFromBroker(connection);
		connection.closeConnection();
		if (connection.getMessageLogger() != null && connection.getMessageLogger().isRunning())
		{
			connection.getMessageLogger().stop();			
		}
		
		TabUtils.requestClose(connectionControllersMapping.get(connection).getTab());
		subscriptionManagers.remove(connectionControllersMapping.get(connection));
		connectionControllersMapping.remove(connection);
		connectionTabs.remove(connection);
		logger.debug("Closing connection tab; sm = {}; cc = {}; ct = {}", 
				subscriptionManagers.keySet().size(),
				connectionControllersMapping.keySet().size(),
				connectionTabs.keySet().size());
		
		// Stop all scripts
		connection.getScriptManager().stopScripts();
		
		for (final BaseMqttSubscription subscription : connection.getSubscriptions().values())
		{
			subscription.getStore().cleanUp();
		}
		connection.getStore().cleanUp();
	}
		
	public void closeOfflineTab(final MqttConnectionController connectionController)
	{
		TabUtils.requestClose(connectionController.getTab());	
		offlineConnectionControllers.remove(connectionController);
	}
	
	public IConnectionController getControllerForTab(final Tab tab)	
	{
		for (final MqttConnectionController controller : getConnectionControllers())
		{
			if (controller.getTab().equals(tab))
			{
				return controller;
			}
		}
		
		return null;
	}

	public MqttAsyncConnection createConnection(final MqttRuntimeConnectionProperties connectionProperties, final EventQueueManager<FormattedMqttMessage> uiEventQueue)
	{
		final InteractiveMqttScriptManager scriptManager = new InteractiveMqttScriptManager(eventBus, null);
		final FormattingManager formattingManager = new FormattingManager(scriptManager);
		final MqttAsyncConnection connection = new MqttAsyncConnection(reconnectionManager,
				connectionProperties, ConnectionStatus.DISCONNECTED, 
				eventBus, scriptManager, formattingManager, uiEventQueue, 
				UiProperties.getSummaryMaxPayloadLength(configurationManager.getUiPropertyFile()));

		formattingManager.initialiseFormatter(connection.getProperties().getFormatter());
		scriptManager.setConnection(connection);
		
		// Set up message logger		
		final MessageLog messageLog = connectionProperties.getConfiguredProperties().getMessageLog();		
		if (messageLog != null && !messageLog.getValue().equals(MessageLogEnum.DISABLED) 
				&& messageLog.getLogFile() != null && !messageLog.getLogFile().isEmpty())
		{
			final Queue<FormattedMqttMessage> messageQueue= new LinkedBlockingQueue<FormattedMqttMessage>();
			
			if (connection.getMessageLogger() == null)
			{
				final MqttMessageLogger messageLogger = new MqttMessageLogger(
						connection.getId(), messageQueue, messageLog, true, 50);
				connection.setMessageLogger(messageLogger);
			}
			
			if (!connection.getMessageLogger().isRunning())
			{
				new Thread(connection.getMessageLogger()).start();
			}
		}		
				
		// Store the created connection
		connections.put(connectionProperties.getConfiguredProperties().getID(), connection);

		return connection;
	}

	/**
	 * Connects the specified connection to a broker.
	 * 
	 * @param connection The connection to connect
	 * 
	 * @return True if successfully connected
	 */
	public boolean connectToBroker(final MqttAsyncConnection connection)
	{
		try
		{
			connection.connect(new MqttCallbackHandler(connection), new MqttAsyncConnectionRunnable(connection));			
			return true;
		}
		catch (SpyException e)
		{
			// TODO: simplify this
			Platform.runLater(new MqttEventHandler(new MqttConnectionAttemptFailureEvent(connection, e)));
			logger.error(e.getMessage(), e);
		}

		return false;
	}

	/**
	 * Disconnects the specified connection from the broker.
	 * 
	 * @param connection The connection to disconnect
	 */
	public void disconnectFromBroker(final MqttAsyncConnection connection)
	{				
		try
		{
			connection.disconnect(new MqttDisconnectionResultHandler());
		}
		catch (SpyException e)
		{
			// TODO: simplify this
			Platform.runLater(new MqttEventHandler(new MqttDisconnectionAttemptFailureEvent(connection, e)));
			logger.error(e.getMessage(), e);
		}		
	}
	
	/**
	 * Disconnects and closes all connections.
	 */
	public void disconnectAndCloseAll()
	{
		for (final MqttAsyncConnection connection : getMqttConnections())
		{
			disconnectAndCloseTab(connection);
		}
	}

	/**
	 * Gets the connection controllers.
	 * 
	 * @return Collection of ConnectionController instances
	 */
	public Map<MqttAsyncConnection, MqttConnectionController> getConnectionControllersMapping()
	{
		return connectionControllersMapping;
	}
	
	/**
	 * Gets the connection controllers.
	 * 
	 * @return Collection of ConnectionController instances
	 */
	public Collection<MqttConnectionController> getConnectionControllers()
	{
		return connectionControllersMapping.values();
	}
	
	/**
	 * Gets the connection controllers.
	 * 
	 * @return Collection of ConnectionController instances
	 */
	public Collection<MqttConnectionController> getOfflineConnectionControllers()
	{
		return offlineConnectionControllers;
	}
	
	/**
	 * Gets subscription manager for the given connection.
	 * 
	 * @param connectionController The connection controller for which to retrieve the subscription manager
	 * 
	 * @return SubscriptionManager instance
	 */
	public MqttSubscriptionViewManager getSubscriptionManager(final MqttConnectionController connectionController)
	{
		return subscriptionManagers.get(connectionController);
	}
	
	public void setViewManager(final MqttViewManager viewManager)
	{
		this.viewManager = viewManager;		
	}
	
	public void setParentStage(final Stage parentStage)
	{
		this.parentStage = parentStage;		
	}
	
	/**
	 * @return the connectionTabs
	 */
	public Map<MqttAsyncConnection, Tab> getConnectionTabs()
	{
		return connectionTabs;
	}

	/**
	 * @return the uiEventQueue
	 */
	public EventQueueManager<FormattedMqttMessage> getUiEventQueue()
	{
		return uiEventQueue;
	}

	/**
	 * @param offlineConnectionControllers the offlineConnectionControllers to set
	 */
	public void setOfflineConnectionControllers(Set<MqttConnectionController> offlineConnectionControllers)
	{
		this.offlineConnectionControllers = offlineConnectionControllers;
	}

	/**
	 * @return the subscriptionManagers
	 */
	public Map<MqttConnectionController, MqttSubscriptionViewManager> getSubscriptionManagers()
	{
		return subscriptionManagers;
	}

	@Override
	public void autoOpenConnections()
	{
		for (final ModifiableConnection connection : configurationManager.getConnections())
		{
			final ConfiguredMqttConnectionDetails details = (ConfiguredMqttConnectionDetails) connection;
			
			if (details.isAutoOpen() != null && details.isAutoOpen())
			{					
				try
				{
					openConnection(details);
				}
				catch (ConfigurationException e)
				{
					// TODO: show warning dialog for invalid
					logger.error("Cannot open conection {}", connection.getName(), e);
				}
			}
		}		
	}

	/**
	 * Creates an event handler with a disconnection action.
	 * 
	 * @param connection The connection to be used
	 * @param connectionManager The connection manager
	 * 
	 * @return The EventHandler with the action
	 */
	public static EventHandler<ActionEvent> createDisconnectAction(final MqttConnectionViewManager connectionManager, final MqttAsyncConnection connection)
	{
		return new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent event)
			{
				connectionManager.disconnectFromBroker(connection);
				event.consume();
			}
		};
	}
	
	/**
	 * Creates an event handler with an empty action.
	 * 
	 * @return The EventHandler with the action
	 */
	public static EventHandler<ActionEvent> createEmptyAction()
	{
		return new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent event)
			{				
				event.consume();
			}
		};
	}
	
	/**
	 * Creates an event handler with a disconnection and close action.
	 * 
	 * @param connection The connection to be used
	 * @param connectionManager The connection manager used
	 * 
	 * @return The EventHandler with the action
	 */
	public static EventHandler<ActionEvent> createDisconnectAndCloseAction(final MqttConnectionViewManager connectionManager, final MqttAsyncConnection connection)
	{
		return new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent event)
			{
				connectionManager.disconnectAndCloseTab(connection);
				event.consume();
			}
		};
	}
	
	/**
	 * Creates an event handler with a 'connect to broker' action.
	 * 
	 * @param connectionManager The connection manager to be used to connect
	 * @param connection The connection to be used
	 * 
	 * @return The EventHandler with the action
	 */
	public static EventHandler<ActionEvent> createConnectAction(final MqttConnectionViewManager connectionManager, final MqttAsyncConnection connection)
	{
		return new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent event)
			{
				connectionManager.connectToBroker(connection);
				event.consume();
			}
		};
	}

	/**
	 * Creates an event handler with a next allowable action for the given connection.
	 * 
	 * @param state The next state
	 * @param connection The connection to be used
	 * @param mqttManager The MQTT manager to be used
	 * 
	 * @return The EventHandler with the action
	 */
	public static EventHandler<ActionEvent> createNextAction(final ConnectionStatus state, 
			final MqttAsyncConnection connection, final MqttConnectionViewManager connectionManager)
	{
		if (state == null)
		{
			return createEmptyAction();
		}
		
		switch (state)
		{
			case CONNECTED:				
				return createDisconnectAction(connectionManager, connection);
			case CONNECTING:
				return createEmptyAction();
			case DISCONNECTED:
				return createConnectAction(connectionManager, connection);
			case DISCONNECTING:
				return createEmptyAction();
			case NOT_CONNECTED:
				return createConnectAction(connectionManager, connection);
			default:
				return createEmptyAction();
		}		
	}
}
