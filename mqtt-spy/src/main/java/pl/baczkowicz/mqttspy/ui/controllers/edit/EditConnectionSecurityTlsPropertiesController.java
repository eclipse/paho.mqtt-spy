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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import pl.baczkowicz.mqttspy.common.generated.SecureSocketSettings;
import pl.baczkowicz.mqttspy.configuration.ConfiguredConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.ui.EditConnectionController;
import pl.baczkowicz.spy.common.generated.Property;
import pl.baczkowicz.spy.common.generated.SecureSocketModeEnum;
import pl.baczkowicz.spy.ui.properties.KeyValueProperty;

/**
 * Controller for editing a single connection - security tab.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class EditConnectionSecurityTlsPropertiesController extends AnchorPane implements Initializable
{
	/** The parent controller. */
	private EditConnectionController parent;
	
	@FXML
	private AnchorPane propertiesPane;

	// Properties
	
	@FXML
	private TableView<KeyValueProperty> sslPropertiesTable;
	
	@FXML
	private TableColumn<KeyValueProperty, String> propertyNameColumn;	
	
	@FXML
	private TableColumn<KeyValueProperty, String> propertyValueColumn;
	
	@FXML
	private Button removePropertyButton;
	
	// ===============================
	// === Initialisation ============
	// ===============================

	public void initialize(URL location, ResourceBundle resources)
	{		
		// Properties
		propertyNameColumn.setCellValueFactory(new PropertyValueFactory<KeyValueProperty, String>("key"));
		propertyNameColumn.setCellFactory(TextFieldTableCell.<KeyValueProperty>forTableColumn());
		propertyNameColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<KeyValueProperty, String>>()
		{
			@Override
			public void handle(CellEditEvent<KeyValueProperty, String> event)
			{
				KeyValueProperty p = event.getRowValue();
	            String newValue = event.getNewValue();
	            p.keyProperty().set(newValue);            
				onChange();
			}		
		});
		propertyValueColumn.setCellValueFactory(new PropertyValueFactory<KeyValueProperty, String>("value"));
		propertyValueColumn.setCellFactory(TextFieldTableCell.<KeyValueProperty>forTableColumn());
		propertyValueColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<KeyValueProperty, String>>()
		{
			@Override
			public void handle(CellEditEvent<KeyValueProperty, String> event)
			{
				KeyValueProperty p = event.getRowValue();
	            String newValue = event.getNewValue();
	            p.valueProperty().set(newValue);            
				onChange();
			}		
		});
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

	public void readAndSetValues(final SecureSocketModeEnum mode, final UserInterfaceMqttConnectionDetails connection)
	{
		if (mode == null || SecureSocketModeEnum.DISABLED.equals(mode))
		{
			connection.setSSL(null);
		}		
		else
		{
			final SecureSocketSettings sslSettings = connection.getSSL();
			
			if (SecureSocketModeEnum.PROPERTIES.equals(mode))
			{
				for (final KeyValueProperty property : sslPropertiesTable.getItems())
				{
					sslSettings.getProperty().add(new Property(property.keyProperty().getValue(), property.valueProperty().getValue()));
				}
			}
			
			connection.setSSL(sslSettings);
		}
	}

	public void displayConnectionDetails(final ConfiguredConnectionDetails connection)
	{		
		if (connection.getSSL() != null)
		{
			removePropertyButton.setDisable(true);
			sslPropertiesTable.getItems().clear();
			sslPropertiesTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener()
			{
				@Override
				public void changed(ObservableValue observable, Object oldValue, Object newValue)
				{
					removePropertyButton.setDisable(false);
				}		
			});
			
			// Properties
			for (final Property property : connection.getSSL().getProperty())
			{
				sslPropertiesTable.getItems().add(new KeyValueProperty(property.getName(), property.getValue()));
			}
		}
	}	
	
	@FXML
	private void addProperty()
	{
		final KeyValueProperty item = new KeyValueProperty("sample.property", "sampleValue");		
		sslPropertiesTable.getItems().add(item);
		onChange();
	}
	
	@FXML
	private void removeProperty()
	{
		final KeyValueProperty item = sslPropertiesTable.getSelectionModel().getSelectedItem(); 
		if (item != null)
		{
			sslPropertiesTable.getItems().remove(item);
			onChange();
		}
	}

	// ===============================
	// === Setters and getters =======
	// ===============================
	
	public void setParent(final EditConnectionController controller)
	{
		this.parent = controller;
	}

	/**
	 * @return the propertiesPane
	 */
	public AnchorPane getPropertiesPane()
	{
		return propertiesPane;
	}

	/**
	 * @param propertiesPane the propertiesPane to set
	 */
	public void setPropertiesPane(AnchorPane propertiesPane)
	{
		this.propertiesPane = propertiesPane;
	}
}
