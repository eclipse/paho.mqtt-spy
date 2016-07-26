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
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import pl.baczkowicz.mqttspy.common.generated.MessageLogEnum;
import pl.baczkowicz.mqttspy.configuration.ConfiguredConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.ui.EditConnectionController;

/**
 * Controller for editing a single connection - message log tab.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class EditConnectionMessageLogController extends AnchorPane implements Initializable, EditConnectionSubController
{
	/** The parent controller. */
	private EditConnectionController parent;	
	
	// Log
	
	@FXML
	private TextField messageLogLocation;
	
	@FXML
	private ComboBox<MessageLogEnum> loggingMode;
	
	// Other fields

	private final ChangeListener basicOnChangeListener = new ChangeListener()
	{
		@Override
		public void changed(ObservableValue observable, Object oldValue, Object newValue)
		{
			onChange();			
		}		
	};
	
	// ===============================
	// === Initialisation ============
	// ===============================

	public void initialize(URL location, ResourceBundle resources)
	{		
		// Log
		messageLogLocation.textProperty().addListener(basicOnChangeListener);
		loggingMode.getSelectionModel().selectedIndexProperty().addListener(basicOnChangeListener);
	}

	public void init()
	{		
		loggingMode.getItems().addAll(MessageLogEnum.values());	
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
		// Log
		connection.getMessageLog().setLogFile(messageLogLocation.getText());
		connection.getMessageLog().setValue(loggingMode.getValue());		
		
		return connection;
	}
	
	@Override
	public void displayConnectionDetails(final ConfiguredConnectionDetails connection)
	{		
		// Log
		loggingMode.setValue(connection.getMessageLog() == null ? MessageLogEnum.DISABLED : connection.getMessageLog().getValue());
		messageLogLocation.setText(connection.getMessageLog() == null ? "" : connection.getMessageLog().getLogFile());
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
