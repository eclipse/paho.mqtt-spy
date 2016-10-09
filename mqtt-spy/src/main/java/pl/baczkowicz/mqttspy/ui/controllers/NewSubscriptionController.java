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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.configuration.generated.TabbedSubscriptionDetails;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.ui.MqttConnectionViewManager;
import pl.baczkowicz.mqttspy.ui.MqttSubscriptionViewManager;
import pl.baczkowicz.mqttspy.ui.MqttViewManager;
import pl.baczkowicz.mqttspy.ui.events.ShowNewMqttSubscriptionWindowEvent;
import pl.baczkowicz.mqttspy.utils.MqttUtils;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.exceptions.SpyException;
import pl.baczkowicz.spy.ui.keyboard.TimeBasedKeyEventFilter;
import pl.baczkowicz.spy.ui.panes.PaneVisibilityStatus;
import pl.baczkowicz.spy.ui.panes.TitledPaneController;
import pl.baczkowicz.spy.ui.utils.DialogFactory;

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

	private MqttConnectionController connectionController;

	private boolean connected;

	private MqttConnectionViewManager connectionManager;

	private boolean detailedView;

	private TimeBasedKeyEventFilter timeBasedFilter;

	private TitledPane pane;

	private AnchorPane paneTitle;

	private MenuButton settingsButton;

	private PaneVisibilityStatus status;
	
	private PaneVisibilityStatus previousStatus;
	
	private IKBus eventBus;

	private Label titleLabel;

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
	        public void handle(KeyEvent event) 
	        {
	        	switch (event.getCode())
	        	{
		        	case ENTER:
		        	{
		        		if (connected && timeBasedFilter.processEvent(event))
			        	{
		        			onSubscribe(event.isControlDown());		        			
		    	        	event.consume();
		        		}
		        		break;
		        	}		        	
		        	default:
		        		break;
	        	}
	        }
	    });	
		
//		subscribeButton.setOnMouseClicked(new EventHandler<MouseEvent>()
//		{
//			@Override
//			public void handle(final MouseEvent event)
//			{
//				if (connected && timeBasedFilter.processEvent(event))
//				{
//					onSubscribe(event.isControlDown());	
//					event.consume();
//				}
//			}
//		});
	}
	
	@FXML
	private void onSubscribe()
	{
		onSubscribe(false);
	}
	
	private void onSubscribe(final boolean controlDown)
	{
		logger.debug("onSubscribe() {} {}", controlDown, status);
		
		if (PaneVisibilityStatus.DETACHED.equals(status))
		{
			subscribe();
			
			if (!controlDown)
			{
				eventBus.publish(new ShowNewMqttSubscriptionWindowEvent(connectionController, previousStatus, PaneVisibilityStatus.DETACHED));
			}
		}
		else
		{
			subscribe();
		}
	}
	
	public void init()
	{
		titleLabel = new Label(pane.getText());
		paneTitle = new AnchorPane();
		settingsButton = MqttViewManager.createTitleButtons(this, paneTitle, connectionController);
	}
	
	private void updateVisibility()
	{
		if (detailedView)
		{
			AnchorPane.setRightAnchor(subscriptionTopicText, 262.0);
			subscriptionQosChoice.setVisible(true);
			subscriptionQosLabel.setVisible(true);
		}
		else
		{
			AnchorPane.setRightAnchor(subscriptionTopicText, 179.0);
			subscriptionQosChoice.setVisible(false);
			subscriptionQosLabel.setVisible(false);
		}
		
		// TODO: basic perspective
	}
	
	public void setViewVisibility(final boolean detailedView)
	{
		this.detailedView = detailedView;
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
		logger.debug("subscribe() to {}", subscriptionTopic);
		
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
		
		final MqttSubscriptionViewManager subscriptionManager = connectionManager.getSubscriptionManager(connectionController);

		// Note: check the subscription controllers as opposed to active subscriptions - there might be already a script-based subscription - this is OK	
		if (!subscriptionManager.getSubscriptionControllersMap().keySet().contains(subscriptionDetails.getTopic()))
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

	@Override
	public void updatePane(final PaneVisibilityStatus status)
	{
		this.status = status;
		if (PaneVisibilityStatus.ATTACHED.equals(status))
		{
			settingsButton.setVisible(true);
			titleLabel.setText("Define new subscription"); 
		}		
		else
		{			
			settingsButton.setVisible(false);
			titleLabel.setText("After typing the value, hit Enter or click Subscribe; hold Control to keep the window");			
		}
	}
	
	public void requestFocus()
	{
		// Bring to front
		if (status.equals(PaneVisibilityStatus.DETACHED))
		{
			connectionController.getPaneToStatusMapping().get(pane).getParentWhenDetached().toFront();
		}
		
		pane.requestFocus();
		subscriptionTopicText.requestFocus();
		
		// Select all text, so it's easier to edit
		subscriptionTopicText.fireEvent(new KeyEvent(this, subscriptionTopicText, KeyEvent.KEY_PRESSED, "", "", KeyCode.A, false, true, false, false));
	}
	
	public void setConnectionManager(final MqttConnectionViewManager connectionManager)
	{
		this.connectionManager = connectionManager;
	}
	
	public void setConnectionController(MqttConnectionController connectionController)
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
	
	/**
	 * Sets the event bus.
	 *  
	 * @param eventBus the eventBus to set
	 */
	public void setEventBus(final IKBus eventBus)
	{
		this.eventBus = eventBus;
	}

	@Override
	public Label getTitleLabel()
	{
		return titleLabel;
	}

	public void setPreviousStatus(PaneVisibilityStatus previousStatus)
	{
		this.previousStatus = previousStatus;		
	}
}
