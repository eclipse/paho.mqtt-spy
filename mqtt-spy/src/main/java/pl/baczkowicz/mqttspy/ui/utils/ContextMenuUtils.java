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
package pl.baczkowicz.mqttspy.ui.utils;

import java.util.Arrays;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.paint.Color;
import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.configuration.generated.TabbedSubscriptionDetails;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.connectivity.MqttSubscription;
import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.stats.StatisticsManager;
import pl.baczkowicz.mqttspy.ui.ConnectionController;
import pl.baczkowicz.mqttspy.ui.SubscriptionController;
import pl.baczkowicz.mqttspy.ui.charts.ChartFactory;
import pl.baczkowicz.mqttspy.ui.charts.ChartMode;
import pl.baczkowicz.mqttspy.ui.connections.ConnectionManager;
import pl.baczkowicz.mqttspy.ui.connections.SubscriptionManager;
import pl.baczkowicz.mqttspy.ui.events.EventManager;
import pl.baczkowicz.spy.ui.panes.PaneVisibilityStatus;
import pl.baczkowicz.spy.ui.panes.TitledPaneStatus;
import pl.baczkowicz.spy.ui.utils.DialogFactory;
import pl.baczkowicz.spy.ui.utils.TabUtils;
import pl.baczkowicz.spy.ui.utils.UiUtils;

/**
 * Context menu utils - mainly for creating various context menus.
 */
public class ContextMenuUtils
{	
	// private final static Logger logger = LoggerFactory.getLogger(ContextMenuUtils.class);
	
	/**
	 * Create context menu for the subscription tab.
	 * 
	 * @param connection The connection associated with the tab
	 * @param subscription The subscription associated with the tab
	 * @param eventManager The global event manager
	 * @param subscriptionManager The connection's subscription manager 
	 * @param configurationManager The global configuration manager
	 * @param subscriptionController The subscription controller for this subscription
	 * 
	 * @return The created context menu
	 */
	public static ContextMenu createSubscriptionTabContextMenu(
			final MqttAsyncConnection connection, 
			final MqttSubscription subscription, 
			final EventManager<FormattedMqttMessage> eventManager, 
			final SubscriptionManager subscriptionManager,
			final ConfigurationManager configurationManager,
			final SubscriptionController subscriptionController)
	{
		final ContextMenu contextMenu = new ContextMenu();

		// Cancel
		MenuItem cancelItem = new MenuItem("Unsubscribe (and keep the tab)");
		cancelItem.setDisable(false);

		cancelItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				connection.unsubscribe(subscription, true);
			}
		});
		contextMenu.getItems().add(cancelItem);

		// Re-subscribe
		MenuItem resubscribeItem = new MenuItem("Re-subscribe");
		resubscribeItem.setDisable(true);

		resubscribeItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				connection.resubscribe(subscription);
			}
		});
		contextMenu.getItems().add(resubscribeItem);

		// Close
		MenuItem closeItem = new MenuItem("Unsubscribe (and close tab)");

		closeItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{				
				subscriptionManager.removeSubscription(subscription.getTopic());				
			}
		});
		contextMenu.getItems().add(closeItem);

		// Separator
		contextMenu.getItems().add(new SeparatorMenuItem());

		// Copy subscription topic string
		final MenuItem copyTopicItem = new MenuItem("Copy subscription topic to clipboard");
		copyTopicItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				UiUtils.copyToClipboard(subscription.getTopic());
			}
		});
		contextMenu.getItems().add(copyTopicItem);


		// Separator
		contextMenu.getItems().add(new SeparatorMenuItem());
		
		// Clear data
		MenuItem clearItem = new MenuItem("Clear subscription history");

		clearItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{				
				eventManager.notifyClearHistory(subscription.getStore());
				StatisticsManager.resetMessagesReceived(connection.getId(), subscription.getTopic());
				subscription.getStore().clear();
			}
		});
		contextMenu.getItems().add(clearItem);		
		
		// Separator
		contextMenu.getItems().add(new SeparatorMenuItem());
		

		final Menu chartsMenu = new Menu("Charts");
		contextMenu.getItems().add(chartsMenu);
		
		// Message load charts
		MenuItem messageLoadChartItem = new MenuItem("Show message load chart");
		messageLoadChartItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{			
				new ChartFactory<FormattedMqttMessage>().createMessageBasedLineChart(
						Arrays.asList(SubscriptionController.AVG5_TOPIC, SubscriptionController.AVG30_TOPIC, SubscriptionController.AVG300_TOPIC),
						subscriptionController.getStatsHistory(), 
						ChartMode.USER_DRIVEN_MSG_PAYLOAD,
						"Series", "Load", "msgs/s", 
						"Message load statistics for " + subscription.getTopic() + " - " + connection.getName(), 
						subscriptionController.getScene(), eventManager);
			}
		});			
		chartsMenu.getItems().add(messageLoadChartItem);
		
		// Message count charts
		MenuItem messageCountChartItem = new MenuItem("Show message count chart");
		messageCountChartItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{			
				new ChartFactory<FormattedMqttMessage>().createMessageBasedPieChart("Message count statistics for " + subscription.getTopic(), 
						subscriptionController.getScene(),
						subscriptionController.getSubscription().getStore().getNonFilteredMessageList().getTopicSummary().getObservableMessagesPerTopic());
			}
		});			
		chartsMenu.getItems().add(messageCountChartItem);		
		
		final Menu configuration = new Menu("Configuration");
		contextMenu.getItems().add(configuration);
		
		// Separator
		// configuration.getItems().add(new SeparatorMenuItem());
		
		// Adds/updates this subscription in the configuration file
		final MenuItem addItem = new MenuItem("Add/update this subscription");
		addItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				configurationManager.updateSubscriptionConfiguration(connection, subscription);
			}
		});
		configuration.getItems().add(addItem);
		
		// Removes this subscription from the configuration file
		final MenuItem removeItem = new MenuItem("Remove this subscription");
		removeItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				configurationManager.deleteSubscriptionConfiguration(connection, subscription);
			}
		});
		configuration.getItems().add(removeItem);
		
		// Separator
		configuration.getItems().add(new SeparatorMenuItem());
		

		final Menu view = new Menu("View");
		contextMenu.getItems().add(view);
		
		// Separator
		//contextMenu.getItems().add(new SeparatorMenuItem());
		
		// Change color
		final MenuItem changeColorMenu = new MenuItem("Change tab color");
		changeColorMenu.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				final Color newColor = 
						DialogFactory.createColorChoiceDialog(subscription.getColor(), 
								"Select new subscription color", "Subscription color: ");
				
				if (!newColor.equals(subscription.getColor()))
				{
					subscription.setColor(newColor);
					subscription.getSubscriptionController().getTab().setStyle(StylingUtils.createBaseRGBString(newColor));
					
					// Update subscription tab
					subscription.getSubscriptionController().getSummaryTablePaneController().refreshRowStyling();
					
					// Update 'all' tab				
					subscriptionManager.getSubscriptionControllersMap().
						get(SubscriptionManager.ALL_SUBSCRIPTIONS_TAB_TITLE).getSummaryTablePaneController().refreshRowStyling();
				}
			}
		});
		view.getItems().add(changeColorMenu);
		
		// View
		final MenuItem detachMenu = new MenuItem("Detach to a separate window");
		detachMenu.setOnAction(TabUtils.createTabDetachEvent(
				detachMenu, subscriptionController, 
				connection.getName() + " - " + subscription.getTopic(), 5));
		view.getItems().add(detachMenu);		

		return contextMenu;
	}

	/**
	 * Creates a context menu for the 'all' tab of the connection.
	 * 
	 * @param connection The connection associated with this tab
	 * @param eventManager The global event manager
	 * @param subscriptionManager The connection's subscription manager 
	 * @param configurationManager The global configuration manager 
	 * 
	 * @return Created context menu
	 */
	public static ContextMenu createAllSubscriptionsTabContextMenu(
			final MqttAsyncConnection connection, 
			final EventManager<FormattedMqttMessage> eventManager,
			final SubscriptionManager subscriptionManager,
			final ConfigurationManager configurationManager,
			final SubscriptionController subscriptionController)
	{
		final ContextMenu contextMenu = new ContextMenu();

		MenuItem cancelItem = new MenuItem("Unsubscribe from all active subscriptions (if any)");
		cancelItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				connection.unsubscribeAll(true);
			}
		});
		contextMenu.getItems().add(cancelItem);

		MenuItem resubscribeItem = new MenuItem("Re-subscribe to all non-active subscriptions (if any)");

		resubscribeItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				connection.resubscribeAll(false);
			}
		});
		contextMenu.getItems().add(resubscribeItem);

		// Separator
		contextMenu.getItems().add(new SeparatorMenuItem());
		
		// Clear data
		MenuItem clearItem = new MenuItem("Clear tab history");

		clearItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				eventManager.notifyClearHistory(connection.getStore());
				StatisticsManager.resetMessagesReceived(connection.getId());
				connection.getStore().clear();
			}
		});
		contextMenu.getItems().add(clearItem);
		
		// Separator
		contextMenu.getItems().add(new SeparatorMenuItem());

		final Menu charts = new Menu("Charts");
		MenuItem showAllChartItem = new MenuItem("Show overall message load chart");
		showAllChartItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				new ChartFactory<FormattedMqttMessage>().createMessageBasedLineChart(
						Arrays.asList(SubscriptionController.AVG5_TOPIC, SubscriptionController.AVG30_TOPIC, SubscriptionController.AVG300_TOPIC),
						subscriptionController.getStatsHistory(), 
						ChartMode.USER_DRIVEN_MSG_PAYLOAD,
						"Series", "Load", "msgs/s", 
						"Message load statistics for all subscriptions - " + connection.getName(), 
						subscriptionController.getScene(), eventManager);
			}
		});
		contextMenu.getItems().add(charts);
		charts.getItems().add(showAllChartItem);
		
		final Menu configuration = new Menu("Configuration");
		
		// Adds/updates this subscription in the configuration file
		final MenuItem addItem = new MenuItem("Add/update all shown subscriptions");
		addItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				for (final SubscriptionController controller : subscriptionManager.getSubscriptionControllers())
				{
					if (controller.getSubscription() != null)
					{
						configurationManager.updateSubscriptionConfiguration(connection, controller.getSubscription());
					}
				}
			}
		});
		contextMenu.getItems().add(configuration);
		configuration.getItems().add(addItem);
		
		
		// Removes this subscription from the configuration file
		final MenuItem removeItem = new MenuItem("Remove all shown subscriptions");
		removeItem.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				for (final SubscriptionController controller : subscriptionManager.getSubscriptionControllers())
				{
					if (controller.getSubscription() != null)
					{
						configurationManager.deleteSubscriptionConfiguration(connection, controller.getSubscription());
					}
				}
			}
		});
		configuration.getItems().add(removeItem);		

		return contextMenu;
	}
	
	private static Menu createConnectionPaneMenu(final String name, 
			final ConnectionController connectionController, 
			final TitledPaneStatus status)
	{
		final Menu menu = new Menu(name);
		
		status.setContentMenu(menu);
		
		final CheckMenuItem hidden = new CheckMenuItem("Hidden");
		hidden.setSelected(true);
		final CheckMenuItem visible = new CheckMenuItem("Visible (attached to connection tab)");
		final CheckMenuItem detached = new CheckMenuItem("Visible (detached from connection tab)");
		
		menu.getItems().add(hidden);
		menu.getItems().add(visible);
		menu.getItems().add(detached);
		
		hidden.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{				
				connectionController.setPaneVisiblity(status, PaneVisibilityStatus.NOT_VISIBLE);
			}
		});
		
		visible.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{				
				connectionController.setPaneVisiblity(status, PaneVisibilityStatus.ATTACHED);
			}
		});
		
		detached.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{				
				connectionController.setPaneVisiblity(status, PaneVisibilityStatus.DETACHED);
			}
		});
		
		return menu;
	}
	
	/**
	 * Creates a context menu for the connection tab.
	 * 
	 * @param connection The connection associated with this tab
	 * @param connectionController The ConnectionController for this tab's pane
	 * @param connectionManager The global connection manager
	 * 
	 * @return Created context menu
	 */
	public static ContextMenu createConnectionMenu(final MqttAsyncConnection connection, 
			final ConnectionController connectionController, final ConnectionManager connectionManager)
	{
		// Context menu
		ContextMenu contextMenu = new ContextMenu();

		MenuItem reconnectItem = new MenuItem("Connect / reconnect");
		reconnectItem.setOnAction(ActionUtils.createConnectAction(connectionManager, connection));
		
		MenuItem disconnectItem = new MenuItem("Disconnect (and keep tab)");
		disconnectItem.setOnAction(ActionUtils.createDisconnectAction(connectionManager, connection));

		MenuItem disconnectAndCloseItem = new MenuItem("Disconnect (and close tab)");
		disconnectAndCloseItem.setOnAction(ActionUtils.createDisconnectAndCloseAction(connectionManager, connection));

		contextMenu.getItems().add(reconnectItem);

		// Separator
		contextMenu.getItems().add(new SeparatorMenuItem());

		contextMenu.getItems().add(disconnectItem);		
		contextMenu.getItems().add(disconnectAndCloseItem);
		
		// Separator
		contextMenu.getItems().add(new SeparatorMenuItem());

		// Show statistics
		final MenuItem stats = new MenuItem("Show broker's statistics");
		stats.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{
				final TabbedSubscriptionDetails subscriptionDetails = new TabbedSubscriptionDetails();
				subscriptionDetails.setTopic("$SYS/#");
				subscriptionDetails.setQos(0);
				
				connectionController.getNewSubscriptionPaneController().subscribe(subscriptionDetails, true);
			}
		});
		contextMenu.getItems().add(stats);
		
		// Separator
		contextMenu.getItems().add(new SeparatorMenuItem());
		
		final Menu view = new Menu("View");
		contextMenu.getItems().add(view);
		
		// View
		final MenuItem detachMenu = new MenuItem("Detach to a separate window");
		detachMenu.setOnAction(TabUtils.createTabDetachEvent(
				detachMenu, connectionController, 
				"Connection " + connection.getName(), 0));
		view.getItems().add(detachMenu);
		
		// Separator
		view.getItems().add(new SeparatorMenuItem());
		
		final MenuItem detailedView = new MenuItem("Toggle between simplified and detailed views (QoS, Retained)");
		view.getItems().add(detailedView);
		
		final CheckMenuItem resizeMessageContent = connectionController.getResizeMessageContentMenu();
		resizeMessageContent.setText("Resizable message pane");	
		resizeMessageContent.selectedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				connectionController.toggleMessagePayloadSize(resizeMessageContent.isSelected());
			}
		});
		view.getItems().add(resizeMessageContent);
		
		// Separator
		view.getItems().add(new SeparatorMenuItem());
		
		final Menu panes = new Menu("Panes");
		final Menu manualPublications = createConnectionPaneMenu("'Publish message' pane", connectionController, connectionController.getNewPublicationPaneStatus());
		final Menu scriptedPublications = createConnectionPaneMenu("'Scripted publications' pane", connectionController, connectionController.getPublicationScriptsPaneStatus());
		final Menu newSubscription = createConnectionPaneMenu("'Define new subscription' pane", connectionController, connectionController.getNewSubscriptionPaneStatus());
		final Menu messageSummary = createConnectionPaneMenu("'Subscriptions and received messages' pane", connectionController, connectionController.getSubscriptionsStatus());
		final Menu testCases = createConnectionPaneMenu("'Test cases' pane", connectionController, connectionController.getTestCasesPaneStatus());
		
		panes.getItems().add(manualPublications);
		panes.getItems().add(scriptedPublications);
		panes.getItems().add(newSubscription);
		panes.getItems().add(messageSummary);
		panes.getItems().add(testCases);

		detailedView.setOnAction(new EventHandler<ActionEvent>()
		{
			public void handle(ActionEvent e)
			{				
				connectionController.toggleDetailedViewVisibility();
			}
		});
		view.getItems().add(panes);
		
		return contextMenu;
	}
	
	/**
	 * Creates a context menu for the message log tab.
	 * 
	 * @param tab The tab for which to create the menu
	 * @param connectionController 
	 * 
	 * @return Created context menu
	 */
	public static ContextMenu createMessageLogMenu(
			final Tab tab, 
			final ConnectionController connectionController, 
			final ConnectionManager connectionManager)
	{
		// Context menu
		ContextMenu contextMenu = new ContextMenu();

		MenuItem closedItem = new MenuItem("Close");
		closedItem.setOnAction(new EventHandler<ActionEvent>()
		{			
			@Override
			public void handle(ActionEvent event)
			{
				connectionManager.closeOfflineTab(connectionController);				
			}
		});
		
		contextMenu.getItems().add(closedItem);
		
		// Separator
		contextMenu.getItems().add(new SeparatorMenuItem());
		
		final Menu view = new Menu("View");
		contextMenu.getItems().add(view);
		
		// View
		final MenuItem detachMenu = new MenuItem("Detach to a separate window");
		detachMenu.setOnAction(TabUtils.createTabDetachEvent(
				detachMenu, connectionController, 
				"Message log " + tab.getText(), 0));
		view.getItems().add(detachMenu);
		
		final CheckMenuItem resizeMessageContent = connectionController.getResizeMessageContentMenu();
		resizeMessageContent.setText("Resizable message pane");	
		resizeMessageContent.selectedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				connectionController.toggleMessagePayloadSize(resizeMessageContent.isSelected());
			}
		});
		view.getItems().add(resizeMessageContent);

		return contextMenu;
	}
}
