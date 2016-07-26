/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
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
package pl.baczkowicz.mqttspy.ui.controllers.edit;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.AnchorPane;
import pl.baczkowicz.mqttspy.common.generated.SimpleMqttMessage;
import pl.baczkowicz.mqttspy.common.generated.PublicationDetails;
import pl.baczkowicz.mqttspy.configuration.ConfiguredConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.messages.BaseMqttMessage;
import pl.baczkowicz.mqttspy.ui.EditConnectionController;
import pl.baczkowicz.mqttspy.ui.NewPublicationController;

/**
 * Controller for editing a single connection - last will tab.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class EditConnectionLastWillController extends AnchorPane implements Initializable, EditConnectionSubController
{
	// LWT
	
	@FXML
	private CheckBox lastWillAndTestament;
	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private NewPublicationController lastWillAndTestamentMessageController;
	
	// Other fields
    
	private final ChangeListener basicOnChangeListener = new ChangeListener()
	{
		@Override
		public void changed(ObservableValue observable, Object oldValue, Object newValue)
		{
			onChange();			
		}		
	};
	
	/** The parent controller. */
	private EditConnectionController parent;
	
	// ===============================
	// === Initialisation ============
	// ===============================

	public void initialize(URL location, ResourceBundle resources)
	{
		// LWT
		lastWillAndTestament.selectedProperty().addListener(basicOnChangeListener);
		lastWillAndTestamentMessageController.getPublicationTopicText().valueProperty().addListener(basicOnChangeListener);
		lastWillAndTestamentMessageController.getPublicationData().textProperty().addListener(basicOnChangeListener);
		lastWillAndTestamentMessageController.getPublicationQosChoice().getSelectionModel().selectedIndexProperty().addListener(basicOnChangeListener);
		lastWillAndTestamentMessageController.getRetainedBox().selectedProperty().addListener(basicOnChangeListener);
		lastWillAndTestamentMessageController.hidePublishButton();
	}

	public void init()
	{
		// Nothing to do
	}

	// ===============================
	// === Logic =====================
	// ===============================

	public void onChange()
	{
		parent.onChange();
	}

	@Override
	public UserInterfaceMqttConnectionDetails readValues(final UserInterfaceMqttConnectionDetails connection)
	{			
		if (lastWillAndTestament.isSelected())
		{			
			final BaseMqttMessage message = lastWillAndTestamentMessageController.readMessage(false, false);
			if (message != null)
			{
				connection.setLastWillAndTestament(new SimpleMqttMessage(message.getPayload(), message.getTopic(), message.getQoS(), message.isRetained()));
			}
		}		
		
		return connection;
	}
	
	@Override
	public void displayConnectionDetails(final ConfiguredConnectionDetails connection)
	{
		lastWillAndTestamentMessageController.clearTopics();
		for (final PublicationDetails pub : connection.getPublication())
		{
			lastWillAndTestamentMessageController.recordPublicationTopic(pub.getTopic());
		}
		
		// LWT
		lastWillAndTestament.setSelected(connection.getLastWillAndTestament() != null);
		lastWillAndTestamentMessageController.displayMessage(connection.getLastWillAndTestament());				
	}		

	// ===============================
	// === Setters and getters =======
	// ===============================
	
	@Override
	public void setParent(final EditConnectionController controller)
	{
		this.parent = controller;
	}
}
