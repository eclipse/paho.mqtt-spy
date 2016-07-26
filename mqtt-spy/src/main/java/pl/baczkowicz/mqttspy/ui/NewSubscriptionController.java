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
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.configuration.generated.TabbedSubscriptionDetails;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.ui.connections.ConnectionManager;
import pl.baczkowicz.spy.ui.keyboard.TimeBasedKeyEventFilter;
import pl.baczkowicz.spy.ui.panes.PaneVisibilityStatus;
import pl.baczkowicz.spy.ui.panes.TitledPaneController;
import pl.baczkowicz.spy.ui.utils.DialogFactory;
import pl.baczkowicz.mqttspy.utils.MqttUtils;
import pl.baczkowicz.spy.exceptions.SpyException;

/**
 * Controller for creating new subscriptions.
 */
public class NewSubscriptionController implements Initializable, TitledPaneController
{
	final static Logger logger = LoggerFactory.getLogger(NewSubscriptionController.class);
	
	@FXML
	private Button subscribeButton;

	@FXML
	private ComboBox<String> subscriptionTopicText;
	@FXML
	
	private ChoiceBox<String> subscriptionQosChoice;

	@FXML
	private Label subscriptionQosLabel;
	
	@FXML
	private ColorPicker subscriptionColorPicker;

	private ObservableList<String> subscriptionTopics = FXCollections.observableArrayList();

	private MqttAsyncConnection connection;

	private List<Color> colors = new ArrayList<Color>();

	private ConnectionController connectionController;

	private boolean connected;

	private ConnectionManager connectionManager;

	private boolean detailedView;

	private TimeBasedKeyEventFilter timeBasedFilter;

	private TitledPane pane;

	private AnchorPane paneTitle;

	private MenuButton settingsButton;

	public NewSubscriptionController()
	{
		// TODO: subscription colors - move that to a property file
		// 8
		colors.add(Color.valueOf("f9d900"));
		colors.add(Color.valueOf("a9e200"));
		colors.add(Color.valueOf("22bad9"));
		colors.add(Color.valueOf("0181e2"));
		colors.add(Color.valueOf("2f357f"));
		colors.add(Color.valueOf("860061"));
		colors.add(Color.valueOf("c62b00"));
		colors.add(Color.valueOf("ff5700"));

		// 8
		colors.add(Color.valueOf("f9d950"));
		colors.add(Color.valueOf("a9e250"));
		colors.add(Color.valueOf("22baa9"));
		colors.add(Color.valueOf("018122"));
		colors.add(Color.valueOf("2f351f"));
		colors.add(Color.valueOf("8600F1"));
		colors.add(Color.valueOf("c62b60"));
		colors.add(Color.valueOf("ff5760"));
	}

	public void initialize(URL location, ResourceBundle resources)
	{
		timeBasedFilter = new TimeBasedKeyEventFilter(100);
		
		subscriptionColorPicker.setValue(colors.get(0));
		subscriptionTopicText.setItems(subscriptionTopics);
		
		subscriptionTopicText.addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() 
		{
	        @Override
	        public void handle(KeyEvent keyEvent) 
	        {
	        	switch (keyEvent.getCode())
	        	{
		        	case ENTER:
		        	{
		        		if (connected && timeBasedFilter.processEvent(keyEvent))
			        	{
		        			subscribe();
		    	        	keyEvent.consume();
		        		}
		        		break;
		        	}		        	
		        	default:
		        		break;
	        	}
	        }
	    });	
		
		// Workaround for bug in JRE 8 Update 60-66 (https://bugs.openjdk.java.net/browse/JDK-8136838)
		subscriptionTopicText.getEditor().focusedProperty().addListener((obs, old, isFocused) -> 
		{ 
            if (!isFocused) 
            { 
            	//subscriptionTopicText.setValue(subscriptionTopicText.getConverter().fromString(subscriptionTopicText.getEditor().getText())); 
            } 
        }); 
	}
	
	public void init()
	{
		paneTitle = new AnchorPane();
		settingsButton = NewPublicationController.createTitleButtons(pane, paneTitle, connectionController);
	}
	
	private void updateVisibility()
	{
		if (detailedView)
		{
			AnchorPane.setRightAnchor(subscriptionTopicText, 327.0);
			subscriptionQosChoice.setVisible(true);
			subscriptionQosLabel.setVisible(true);
		}
		else
		{
			AnchorPane.setRightAnchor(subscriptionTopicText, 244.0);
			subscriptionQosChoice.setVisible(false);
			subscriptionQosLabel.setVisible(false);
		}
	}
	
	public void setDetailedViewVisibility(final boolean visible)
	{
		detailedView = visible;
		updateVisibility();
	}
	
	public void toggleDetailedViewVisibility()
	{
		detailedView = !detailedView;
		updateVisibility();
	}
	
	public void setConnected(final boolean connected)
	{
		this.connected = connected;
		this.subscribeButton.setDisable(!connected);
		this.subscriptionTopicText.setDisable(!connected);
	}

	public boolean recordSubscriptionTopic(final String subscriptionTopic)
	{
		return MqttUtils.recordTopic(subscriptionTopic, subscriptionTopics);
	}
	
	@FXML
	public void subscribe()
	{
		// Note: here using the editor, as the value stored directly in the ComboBox might
		// not be committed yet, whereas the editor (TextField) has got the current text in it
		
		// Note: this is also a workaround for bug in JRE 8 Update 60-66 (https://bugs.openjdk.java.net/browse/JDK-8136838)
		final String subscriptionTopic = subscriptionTopicText.getEditor().getText();
		
		if (subscriptionTopic != null)
		{			
			try
			{
				MqttUtils.validateTopic(subscriptionTopic);
			
				final TabbedSubscriptionDetails subscriptionDetails = new TabbedSubscriptionDetails();
				subscriptionDetails.setTopic(subscriptionTopic);
				subscriptionDetails.setQos(subscriptionQosChoice.getSelectionModel().getSelectedIndex());
							
				subscribe(subscriptionDetails, true);
			}
			catch (SpyException e)
			{
				DialogFactory.createErrorDialog("Invalid topic", "Provided topic is not valid. " + e.getMessage());
			}
		}
		else
		{
			DialogFactory.createErrorDialog("Invalid topic", "Cannot subscribe to an empty topic.");
		}
	}	

	public void subscribe(final TabbedSubscriptionDetails subscriptionDetails, final boolean subscribe)
	{
		logger.info("Subscribing to " + subscriptionDetails.getTopic());
		if (!connection.getSubscriptions().keySet().contains(subscriptionDetails.getTopic()))		
		{
			recordSubscriptionTopic(subscriptionDetails.getTopic());
			
			connectionManager.getSubscriptionManager(connectionController).
				createSubscription(subscriptionColorPicker.getValue(), subscribe, subscriptionDetails, 
						connection, connectionController, this);
			
			subscriptionColorPicker.setValue(colors.get(connection.getLastUsedSubscriptionId() % 16));
		}
		else
		{
			DialogFactory.createErrorDialog("Duplicate topic", "You already have a subscription tab with " + subscriptionDetails.getTopic() + " topic.");
		}
	}
	
	public void setConnectionManager(final ConnectionManager connectionManager)
	{
		this.connectionManager = connectionManager;
	}
	
	public void setConnectionController(ConnectionController connectionController)
	{
		this.connectionController = connectionController;
	}

	public void setConnection(MqttAsyncConnection connection)
	{
		this.connection = connection;
	}

	@Override
	public TitledPane getTitledPane()
	{
		return pane;
	}

	@Override
	public void setTitledPane(TitledPane pane)
	{
		this.pane = pane;
	}
	
	@Override
	public void updatePane(PaneVisibilityStatus status)
	{
		if (PaneVisibilityStatus.ATTACHED.equals(status))
		{
			settingsButton.setVisible(true);
		}		
		else
		{
			settingsButton.setVisible(false);
		}
	}
}
