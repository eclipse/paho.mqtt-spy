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
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import pl.baczkowicz.mqttspy.configuration.ConfiguredMqttConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.ui.controllers.EditMqttConnectionController;
import pl.baczkowicz.spy.common.generated.FormatterDetails;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.formatting.FormattingManager;
import pl.baczkowicz.spy.formatting.FormattingUtils;
import pl.baczkowicz.spy.ui.configuration.IConfigurationManager;
import pl.baczkowicz.spy.ui.controllers.FormattersController;
import pl.baczkowicz.spy.ui.events.FormattersChangedEvent;
import pl.baczkowicz.spy.ui.events.ShowFormattersWindowEvent;
import pl.baczkowicz.spy.ui.keyboard.KeyboardUtils;

/**
 * Controller for editing a single connection - other/ui tab.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class EditConnectionOtherController extends AnchorPane implements Initializable, IEditConnectionSubController
{
	/** The parent controller. */
	private EditMqttConnectionController parent;

	// UI & Formatting
		
	@FXML
	private Button editFormatters;
	
	@FXML
	private CheckBox autoOpen;
	
	@FXML
	private CheckBox autoConnect;
	
	@FXML
	private CheckBox autoSubscribe;
		
	@FXML
	private TextField maxMessagesStored;
	
	@FXML
	private TextField minMessagesPerTopicStored;
	
	@FXML
	private ComboBox<FormatterDetails> formatter;
	
	private IConfigurationManager configurationManager;

	private final ChangeListener basicOnChangeListener = new ChangeListener()
	{
		@Override
		public void changed(ObservableValue observable, Object oldValue, Object newValue)
		{
			onChange();			
		}		
	};

	private ConfiguredMqttConnectionDetails currentConnection;

	private IKBus eventBus;
	
	// ===============================
	// === Initialisation ============
	// ===============================

	public void initialize(URL location, ResourceBundle resources)
	{		
		// UI
		autoConnect.selectedProperty().addListener(basicOnChangeListener);
		autoOpen.selectedProperty().addListener(basicOnChangeListener);
		autoSubscribe.selectedProperty().addListener(basicOnChangeListener);
		
		maxMessagesStored.textProperty().addListener(basicOnChangeListener);
		maxMessagesStored.addEventFilter(KeyEvent.KEY_TYPED, KeyboardUtils.nonNumericKeyConsumer);
		
		minMessagesPerTopicStored.textProperty().addListener(basicOnChangeListener);
		minMessagesPerTopicStored.addEventFilter(KeyEvent.KEY_TYPED, KeyboardUtils.nonNumericKeyConsumer);
		
		formatter.getSelectionModel().selectedIndexProperty().addListener(basicOnChangeListener);
		formatter.setCellFactory(new Callback<ListView<FormatterDetails>, ListCell<FormatterDetails>>()
				{
					@Override
					public ListCell<FormatterDetails> call(ListView<FormatterDetails> l)
					{
						return new ListCell<FormatterDetails>()
						{
							@Override
							protected void updateItem(FormatterDetails item, boolean empty)
							{
								super.updateItem(item, empty);
								if (item == null || empty)
								{
									setText(null);
								}
								else
								{									
									setText(item.getName());
								}
							}
						};
					}
				});
		formatter.setConverter(new StringConverter<FormatterDetails>()
		{
			@Override
			public String toString(FormatterDetails item)
			{
				if (item == null)
				{
					return null;
				}
				else
				{
					return item.getName();
				}
			}

			@Override
			public FormatterDetails fromString(String id)
			{
				return null;
			}
		});
	}

	public void init()
	{
		eventBus.subscribe(this, this::handleFormattersChange, FormattersChangedEvent.class);
				
		refreshFormattersList();
		
		// Populate those from the configuration file
		FormattersController.addFormattersToList(configurationManager.getFormatters(), formatter.getItems());		
	}
	
	public void handleFormattersChange(final FormattersChangedEvent event)	
	{
		refreshFormattersList();
	}
	
	public void refreshFormattersList()
	{
		formatter.getItems().clear();		
		formatter.getItems().addAll(FormattingUtils.createBaseFormatters());
		formatter.getItems().addAll(FormattingManager.createDefaultScriptFormatters());		
	}

	// ===============================
	// === Logic =====================
	// ===============================

	@FXML
	private void editFormatters()
	{
		eventBus.publish(new ShowFormattersWindowEvent(this.getScene().getWindow(), true));
		
		// In case there was a change
		init();
		displayConnectionDetails(currentConnection);
	}
	
	public void onChange()
	{
		parent.onChange();				
	}

	@Override
	public UserInterfaceMqttConnectionDetails readValues(final UserInterfaceMqttConnectionDetails connection)
	{
		connection.setAutoConnect(autoConnect.isSelected());
		connection.setAutoOpen(autoOpen.isSelected());
		connection.setAutoSubscribe(autoSubscribe.isSelected());
		connection.setFormatter(formatter.getSelectionModel().getSelectedItem());
		connection.setMaxMessagesStored(Integer.valueOf(maxMessagesStored.getText()));
		connection.setMinMessagesStoredPerTopic(Integer.valueOf(minMessagesPerTopicStored.getText()));
		
		return connection;
	}
	
	@Override
	public void displayConnectionDetails(final ConfiguredMqttConnectionDetails connection)
	{
		this.currentConnection = connection;
		
		// UI
		autoConnect.setSelected(connection.isAutoConnect() == null ? false : connection.isAutoConnect());
		autoOpen.setSelected(connection.isAutoOpen() == null ? false : connection.isAutoOpen());
		autoSubscribe.setSelected(connection.isAutoSubscribe() == null ? false : connection.isAutoSubscribe());
		maxMessagesStored.setText(connection.getMaxMessagesStored().toString());
		minMessagesPerTopicStored.setText(connection.getMinMessagesStoredPerTopic().toString());
				
		if (formatter.getItems().size() > 0 && connection.getFormatter() != null)
		{
			for (final FormatterDetails item : formatter.getItems())
			{
				if (item.getID().equals(((FormatterDetails) connection.getFormatter()).getID()))
				{
					formatter.getSelectionModel().select(item);
					break;
				}
			}
		}	
		else
		{
			formatter.getSelectionModel().clearSelection();
		}
	}		

	// ===============================
	// === Setters and getters =======
	// ===============================
	
	public void setConfigurationManager(final IConfigurationManager configurationManager)
	{
		this.configurationManager = configurationManager;
	}

	@Override
	public void setParent(final EditMqttConnectionController controller)
	{
		this.parent = controller;
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
}
