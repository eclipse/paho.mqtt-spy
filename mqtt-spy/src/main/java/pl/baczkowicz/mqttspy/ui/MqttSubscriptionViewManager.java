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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.configuration.generated.TabbedSubscriptionDetails;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.connectivity.MqttSubscription;
import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.scripts.MqttScriptManager;
import pl.baczkowicz.mqttspy.ui.controllers.MqttConnectionController;
import pl.baczkowicz.mqttspy.ui.controllers.SubscriptionController;
import pl.baczkowicz.mqttspy.ui.events.SubscriptionStatusChangeEvent;
import pl.baczkowicz.mqttspy.ui.utils.ContextMenuUtils;
import pl.baczkowicz.spy.connectivity.ConnectionStatus;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.formatting.FormattingManager;
import pl.baczkowicz.spy.ui.configuration.IConfigurationManager;
import pl.baczkowicz.spy.ui.configuration.UiProperties;
import pl.baczkowicz.spy.ui.events.queuable.EventQueueManager;
import pl.baczkowicz.spy.ui.panes.PaneVisibilityStatus;
import pl.baczkowicz.spy.ui.panes.SpyPerspective;
import pl.baczkowicz.spy.ui.panes.TabStatus;
import pl.baczkowicz.spy.ui.storage.ManagedMessageStoreWithFiltering;
import pl.baczkowicz.spy.ui.utils.FxmlUtils;
import pl.baczkowicz.spy.ui.utils.StylingUtils;
import pl.baczkowicz.spy.ui.utils.TabUtils;

/**
 * Class for managing subscription tabs.
 */
public class MqttSubscriptionViewManager
{
	/** Title for the 'all subscriptions' tab. */
	public static String ALL_SUBSCRIPTIONS_TAB_TITLE = "All";
	
	/** Index for the 'all subscriptions' tab. */
	public static int ALL_SUBSCRIPTIONS_TAB_INDEX = 0;
	
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(MqttSubscriptionViewManager.class);
		
	/** Subscription controllers (subscription topic to controller mapping). */
	private final Map<String, SubscriptionController> subscriptionControllers = new LinkedHashMap<>();
	
	/** UI event queue to be used. */
	private final EventQueueManager<FormattedMqttMessage> uiEventQueue;

	/** Configuration manager. */
	private IConfigurationManager configurationManager;
	
	private MqttViewManager viewManager;

	private IKBus eventBus;

	/**
	 * Creates a SubscriptionManager with the given parameters.
	 * 
	 * @param eventManager The global event manager
	 * @param eventBus The global event bus
	 * @param configurationManager The configuration manager
	 * @param viewManager 
	 * @param uiEventQueue The UI event queue to be used
	 */
	public MqttSubscriptionViewManager(final IKBus eventBus, final IConfigurationManager configurationManager, 
			final MqttViewManager viewManager, final EventQueueManager<FormattedMqttMessage> uiEventQueue)
	{
		this.eventBus = eventBus;
		this.configurationManager = configurationManager;
		this.viewManager = viewManager;
		this.uiEventQueue = uiEventQueue;
	}
	
	/**
	 * Creates a subscription and a tab for it.
	 * 
	 * @param color The color to use for this subscription
	 * @param subscribe Whether to subscribe straight after creating it (true) or leave it unsubscribed (false)
	 * @param subscriptionDetails Subscription details
	 * @param connection The connection for which to create this subscription
	 * @param connectionController The connection controller
	 * @param parent The parent UI node
	 * @param formattingManager 
	 * @param scene 
	 */
	public void createSubscription(final Color color, final boolean subscribe, final TabbedSubscriptionDetails subscriptionDetails, 
			final MqttAsyncConnection connection, final MqttConnectionController connectionController, final Object parent)
	{
		logger.info("Creating subscription for " + subscriptionDetails.getTopic());
		final MqttSubscription subscription = new MqttSubscription(subscriptionDetails.getTopic(),
				subscriptionDetails.getQos(), color,
				connection.getProperties().getConfiguredProperties().getMinMessagesStoredPerTopic(),
				connection.getPreferredStoreSize(), uiEventQueue, eventBus,   
				connection.getStore().getFormattingManager(),
				UiProperties.getSummaryMaxPayloadLength(configurationManager.getUiPropertyFile()));
		subscription.setConnection(connection);
		subscription.setDetails(subscriptionDetails);
		
		// Add a new tab
		final SubscriptionController subscriptionController = createSubscriptionTab(
				false, subscription.getStore(), subscription, connection, connectionController);
		subscriptionController.getTab().setContextMenu(ContextMenuUtils.createSubscriptionTabContextMenu(
				connection, subscription, eventBus, this, configurationManager, subscriptionController));		

		subscriptionController.setConnectionController(connectionController);
		subscriptionController.setFormatters(configurationManager.getFormatters());
		subscriptionController.setTabStatus(new TabStatus());
		subscriptionController.getTabStatus().setVisibility(PaneVisibilityStatus.NOT_VISIBLE);
		subscriptionController.init();
		subscriptionController.onSubscriptionStatusChanged(new SubscriptionStatusChangeEvent(subscription));
		
		final SpyPerspective perspective = viewManager.getPerspective();
		subscriptionController.setViewVisibility(MqttViewManager.getDetailedViewStatus(perspective), MqttViewManager.getBasicViewStatus(perspective));
		subscriptionController.getTabStatus().setVisibility(PaneVisibilityStatus.ATTACHED);
		subscriptionController.getTabStatus().setParent(connectionController.getSubscriptionTabs());
		
		final TabPane subscriptionTabs = connectionController.getSubscriptionTabs();
		
		subscriptionTabs.getTabs().add(subscriptionController.getTab());
		subscriptionTabs.getTabs().get(ALL_SUBSCRIPTIONS_TAB_INDEX).setDisable(false);
		
		if (subscribe)
		{
			logger.debug("Trying to subscribe {}", subscription.getTopic());
			connection.subscribe(subscription);
		}
		else
		{
			connection.addSubscription(subscription);
			subscription.setActive(false);
		}
	}	

	/**
	 * Creates a subscription tab & controller with the given parameters.
	 * 
	 * @param allTab True if this is the 'all' tab
	 * @param observableMessageStore The message store to use
	 * @param subscription The subscription object
	 * @param connection Connection associated with this subscription
	 * @param connectionController 
	 * 
	 * @return Created subscription controller for the tab
	 */
	public SubscriptionController createSubscriptionTab(final boolean allTab, 
			final ManagedMessageStoreWithFiltering<FormattedMqttMessage> observableMessageStore, final MqttSubscription subscription,
			final MqttAsyncConnection connection, final MqttConnectionController connectionController)
	{
		// Load a new tab and connection pane
		final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("SubscriptionMessageListPane.fxml");

		final AnchorPane subscriptionPane = FxmlUtils.loadAnchorPane(loader);
		final SubscriptionController subscriptionController = ((SubscriptionController) loader.getController());
		
		final Tab tab = new Tab();
		if (subscription != null)
		{
			eventBus.subscribeWithFilterOnly(subscriptionController, subscriptionController::onSubscriptionStatusChanged, SubscriptionStatusChangeEvent.class, subscription);
		}
		
		subscriptionController.setStore(observableMessageStore);
		subscriptionController.setEventBus(eventBus);
		subscriptionController.setConfingurationManager(configurationManager);
		if (connection != null)
		{
			subscriptionController.setFormattingManager(connection.getStore().getFormattingManager());
			subscriptionController.setConnectionProperties(connection.getProperties());
		}
		else
		{
			subscriptionController.setFormattingManager(new FormattingManager(new MqttScriptManager(null, null, null)));
		}
		subscriptionController.setTab(tab);
		subscriptionController.toggleMessagePayloadSize(connectionController.getResizeMessageContentMenu().isSelected());
				
		tab.setClosable(false);
		tab.setContent(subscriptionPane);

		if (subscription != null)
		{
			tab.setStyle(StylingUtils.createBaseRGBString(subscription.getColor()));
		}

		if (allTab)
		{
			subscriptionControllers.put(ALL_SUBSCRIPTIONS_TAB_TITLE, subscriptionController);						
			tab.setGraphic(new Label(ALL_SUBSCRIPTIONS_TAB_TITLE));
			tab.getGraphic().getStyleClass().add("subscribed");
		}
		else
		{
			logger.debug("Mapping subscription topic {} to controller", subscription.getTopic());
			subscriptionControllers.put(subscription.getTopic(), subscriptionController);						
			tab.setGraphic(new Label(subscription.getTopic()));
			tab.getGraphic().getStyleClass().add("unsubscribed");
			tab.setTooltip(new Tooltip("Status: " + "unsubscribed"));
		}		
		
		return subscriptionController;
	}
	
	/**
	 * Completely removes subscription with the given topic (unsubscribes & closes tab).
	 * 
	 * @param topic The subscription topic to remove
	 */
	public void removeSubscription(final String topic)	
	{
		synchronized (subscriptionControllers)
		{
			logger.debug("Trying to remove subscription {}", topic);
			final MqttSubscription subscription = subscriptionControllers.get(topic).getSubscription();
			subscription.getConnection().unsubscribeAndRemove(subscription);
			subscription.getStore().cleanUp();
			subscriptionControllers.get(topic).onClose();
			TabUtils.requestClose(subscriptionControllers.get(topic).getTab());
			subscriptionControllers.remove(topic);
		}
	}
	
	/**
	 * Gets all subscription controllers.
	 * 
	 * @return Collection of SubscriptionController instances.
	 */
	public Collection<SubscriptionController> getSubscriptionControllers()
	{
		synchronized (subscriptionControllers)
		{
			return Collections.unmodifiableCollection(subscriptionControllers.values());
		}
	}
	
	/**
	 * Gets all subscription controllers.
	 * 
	 * @return Collection of SubscriptionController instances.
	 */
	public Map<String, SubscriptionController> getSubscriptionControllersMap()
	{
		synchronized (subscriptionControllers)
		{
			return Collections.unmodifiableMap(subscriptionControllers);
		}
	}
	
	
	/**
	 * Updates the subscription tab's context menu.
	 * 
	 * TODO: use the Specification interface here
	 * 
	 * @param tab The tab for which to perform the update
	 * @param subscription The subscription for which to perform the update
	 */
	public static void updateSubscriptionTabContextMenu(final Tab tab, final MqttSubscription subscription)
	{
		logger.debug("Updating subscription tab context menu [{}, {}, {}]", 
				subscription.getTopic(), subscription.getConnection().getConnectionStatus(), subscription.isActive());
		
		// Update title style
		tab.getGraphic().getStyleClass().remove(tab.getGraphic().getStyleClass().size() - 1);
		if (subscription.isActive())
		{
			tab.getGraphic().getStyleClass().add("subscribed");
			tab.getTooltip().setText("Status: " + "subscribed");
		}
		else
		{
			tab.getGraphic().getStyleClass().add("unsubscribed");
			tab.getTooltip().setText("Status: " + "unsubscribed");
		}

		// Set menu items
		if (subscription.getConnection().getConnectionStatus().equals(ConnectionStatus.CONNECTED))
		{									
			if (subscription.isActive())
			{
				tab.getContextMenu().getItems().get(0).setDisable(false);
				tab.getContextMenu().getItems().get(1).setDisable(true);
			}
			else
			{
				tab.getContextMenu().getItems().get(0).setDisable(true);
				tab.getContextMenu().getItems().get(1).setDisable(false);
			}
			
			tab.getContextMenu().getItems().get(2).setDisable(false);
		}
		else
		{
			tab.getContextMenu().getItems().get(0).setDisable(true);
			tab.getContextMenu().getItems().get(1).setDisable(true);
			tab.getContextMenu().getItems().get(2).setDisable(true);			
		}			
	}	
}
