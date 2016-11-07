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
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.spy.ui.controllers.BaseMessageController;

/**
 * Controller for displaying a message.
 */
public class MqttMessageController extends BaseMessageController<FormattedMqttMessage> implements Initializable
{
	final static Logger logger = LoggerFactory.getLogger(MqttMessageController.class);
	
	@FXML
	private CheckBox retainedField;

	@FXML
	private TextField qosField;
	
	@FXML
	private Label retainedFieldLabel;
	
	@FXML
	private Label qosFieldLabel;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{		
		// All done in init() as this is where the dataFieldInterface is assigned
	}
	
	@Override
	protected void updateVisibility()
	{
		super.updateVisibility();
		
		qosField.setVisible(detailedView);
		qosFieldLabel.setVisible(detailedView);
		
		retainedField.setVisible(detailedView);
		retainedFieldLabel.setVisible(detailedView);
		
		// TODO: basic perspective
	}

	@Override
	public void populate(final FormattedMqttMessage message)
	{
		// Don't populate with the same message object
		if (message != null && !message.equals(this.message))
		{
			super.populate(message);
	
			qosField.setText(String.valueOf(message.getQoS()));
			retainedField.setSelected(message.isRetained());
		}
	}

	@Override
	public void clear()
	{
		super.clear();
	
		qosField.setText("");
		retainedField.setSelected(false);
	}
}
