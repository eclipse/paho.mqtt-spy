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
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.connectivity.MqttSubscription;
import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.formatting.FormattingManager;
import pl.baczkowicz.spy.messages.FormattedMessage;
import pl.baczkowicz.spy.ui.configuration.IConfigurationManager;
import pl.baczkowicz.spy.ui.events.MessageAddedEvent;
import pl.baczkowicz.spy.ui.events.MessageListChangedEvent;
import pl.baczkowicz.spy.ui.events.MessageRemovedEvent;
import pl.baczkowicz.spy.ui.storage.ManagedMessageStoreWithFiltering;
import pl.baczkowicz.spy.ui.utils.FxmlUtils;
import pl.baczkowicz.spy.ui.utils.StylingUtils;

/**
 * Controller for the search window.
 */
public class SearchWindowController extends AnchorPane implements Initializable
{
	/** Initial and minimal scene/stage width. */	
	public final static int WIDTH = 780;
	
	/** Initial and minimal scene/stage height. */
	public final static int HEIGHT = 550;
	
	final static Logger logger = LoggerFactory.getLogger(SearchWindowController.class);
	
	@FXML
	private Button createNewSearchButton;

	@FXML
	private TabPane searchTabs;
	
	private int searchNumber = 1;
	
	private Map<Tab, SearchPaneController> searchPaneControllers = new HashMap<Tab, SearchPaneController>();

	private ManagedMessageStoreWithFiltering<FormattedMqttMessage> store;

	private MqttSubscription subscription;

	private String subscriptionName;

	private Stage stage;
	
	private IKBus eventBus;

	private MqttAsyncConnection connection;

	private MqttConnectionController connectionController;

	private IConfigurationManager configurationManager;

	private FormattingManager formattingManager;
	
	/**
	 * @param formattingManager the formattingManager to set
	 */
	public void setFormattingManager(FormattingManager formattingManager)
	{
		this.formattingManager = formattingManager;
	}

	public void initialize(URL location, ResourceBundle resources)
	{
		searchTabs.getTabs().clear();				
	}
	
	public void createNewSearch()
	{
		final Tab tab = createSearchTab(this);
		searchTabs.getTabs().add(tab);
		
		Platform.runLater(new Runnable()
		{			
			@Override
			public void run()
			{
				if (searchPaneControllers.get(tab) != null)
				{
					searchPaneControllers.get(tab).requestSearchFocus();
				}
			}
		});		
	}
	
	public Tab createSearchTab(final Object parent)
	{
		// Load a new tab and message pane
		final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("SearchPane.fxml");

		final AnchorPane searchPane = FxmlUtils.loadAnchorPane(loader);
		final SearchPaneController searchPaneController = ((SearchPaneController) loader.getController());
		
		final Tab tab = new Tab();
		tab.setText("New search " + searchNumber);
		searchNumber++;

		tab.setClosable(true);
		tab.setContent(searchPane);
		tab.setOnClosed(new EventHandler<Event>()
		{
			@Override
			public void handle(Event event)
			{
				searchPaneController.cleanup();				
			}
		});
		
		searchPaneController.setTab(tab);
		searchPaneController.setEventBus(eventBus);
		searchPaneController.setStore(store);
		searchPaneController.setConfingurationManager(configurationManager);
		searchPaneController.setFormattingManager(formattingManager);
		searchPaneController.setConnection(connection);
		searchPaneController.toggleMessagePayloadSize(connectionController.getResizeMessageContentMenu().isSelected());
		searchPaneController.init();		
		
		searchPaneControllers.put(tab, searchPaneController);		

		return tab;
	}
	
	public void toggleMessagePayloadSize(final boolean resize)
	{
		for (final SearchPaneController controller : searchPaneControllers.values())
		{
			controller.toggleMessagePayloadSize(resize);
		}
	}

	public void handleClose()
	{
		 for (final SearchPaneController controller : searchPaneControllers.values())
		 {
			 controller.disableAutoSearch();
		 }		
	}

	public void init()
	{
		stage = (Stage) searchTabs.getScene().getWindow();
		updateTitle();
		createNewSearchButton.setText("Create new search for \"" + subscriptionName + "\"");
		
		if (subscription != null)
		{
			createNewSearchButton.setStyle(StylingUtils.createBaseRGBString(subscription.getColor()));
		}
	}
	
	private void updateTitle()
	{
		final String messagesText = store.getMessages().size() == 1 ?  "message" : "messages";
		
		if (!store.browsingFiltersEnabled())
		{			
			stage.setTitle(subscriptionName + " - " + store.getMessages().size() + " " + messagesText + " available for searching");
		}
		else
		{
			stage.setTitle(subscriptionName + " - " + store.getMessages().size() + " " + messagesText 
					+ " available for searching (" + MessageNavigationController.getBrowsingTopicsInfo(store) + ")");		
		}		
	}
	
	// TODO: optimise message handling
	public void onMessageAdded(final MessageAddedEvent<FormattedMessage> event)
	{
		updateTitle();		
	}
	
	// TODO: optimise message handling
	public void onMessageRemoved(final MessageRemovedEvent<FormattedMessage> event)
	{
		updateTitle();
	}
	
	public void onMessageListChanged(final MessageListChangedEvent event)
	{
		updateTitle();
	}
	
	// ===============================
	// === Setters and getters =======
	// ===============================

	public void setStore(ManagedMessageStoreWithFiltering<FormattedMqttMessage> store)
	{
		this.store = store;	
	}
	
	public void setSubscription(MqttSubscription subscription)
	{
		this.subscription = subscription;		
	}

	public void setSubscriptionName(final String name)
	{
		this.subscriptionName = name;		
	}
	
	public void setConnection(MqttAsyncConnection connection)
	{
		this.connection = connection;		
	}
	
	public void setConnectionController(final MqttConnectionController connectionController)
	{
		this.connectionController = connectionController;
	}
	
	public void setConfingurationManager(final IConfigurationManager configurationManager)
	{
		this.configurationManager = configurationManager;
	}
	
	public void setEventBus(final IKBus eventBus)
	{
		this.eventBus = eventBus;
	}
}
