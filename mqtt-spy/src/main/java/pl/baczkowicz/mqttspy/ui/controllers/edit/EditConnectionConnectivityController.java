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
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import pl.baczkowicz.mqttspy.common.generated.ProtocolVersionEnum;
import pl.baczkowicz.mqttspy.configuration.ConfigurationUtils;
import pl.baczkowicz.mqttspy.configuration.ConfiguredMqttConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.ui.controllers.EditMqttConnectionController;
import pl.baczkowicz.mqttspy.utils.ConnectionUtils;
import pl.baczkowicz.mqttspy.utils.MqttUtils;
import pl.baczkowicz.spy.common.generated.ReconnectionSettings;
import pl.baczkowicz.spy.ui.keyboard.KeyboardUtils;
import pl.baczkowicz.spy.ui.panes.SpyPerspective;

/**
 * Controller for editing a single connection - connectivity tab.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class EditConnectionConnectivityController extends AnchorPane implements Initializable, IEditConnectionSubController
{
	private final static Logger logger = LoggerFactory.getLogger(EditConnectionConnectivityController.class);

	// Connectivity
	
	//@FXML
	//private Label multiLabel;
	
	@FXML
	private Label keepAliveLabel;
	
	@FXML
	private Label timeoutLabel;
	
	@FXML
	private Label reconnectIntervalLabel;
	
	@FXML
	private Label resubscribeLabel;

	@FXML
	private ComboBox<String> connectionTypeCombo;
	
	@FXML
	private ComboBox<ProtocolVersionEnum> protocolCombo;
	
	@FXML
	private TextField brokerAddressText;
	
	@FXML
	private CheckBox reconnect;
		
	@FXML
	private TextField reconnectionInterval;
	
	@FXML
	private CheckBox resubscribe;

	@FXML
	private TextField clientIdText;

	@FXML
	private Button addTimestampButton;
	
	@FXML
	private Label lengthLabel;
	
	@FXML
	private TextField connectionTimeout;
	
	@FXML
	private TextField keepAlive;
	
	@FXML
	private CheckBox cleanSession;
	
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
	private EditMqttConnectionController parent;
	
	// ===============================
	// === Initialisation ============
	// ===============================

	public void initialize(URL location, ResourceBundle resources)
	{	
		protocolCombo.getSelectionModel().selectedIndexProperty().addListener(basicOnChangeListener);
		protocolCombo.setCellFactory(new Callback<ListView<ProtocolVersionEnum>, ListCell<ProtocolVersionEnum>>()
		{
			@Override
			public ListCell<ProtocolVersionEnum> call(ListView<ProtocolVersionEnum> l)
			{
				return new ListCell<ProtocolVersionEnum>()
				{
					@Override
					protected void updateItem(ProtocolVersionEnum item, boolean empty)
					{
						super.updateItem(item, empty);
						if (item == null || empty)
						{
							setText(null);
						}
						else
						{			
							if (item.equals(ProtocolVersionEnum.MQTT_DEFAULT))
							{
								setText("MQTT (auto-resolve)");
							}
							else
							{
								setText(item.value());
							}
						}
					}
				};
			}
		});
		protocolCombo.setConverter(new StringConverter<ProtocolVersionEnum>()
		{
			@Override
			public String toString(ProtocolVersionEnum item)
			{
				if (item == null)
				{
					return null;
				}
				else
				{
					if (item.equals(ProtocolVersionEnum.MQTT_DEFAULT))
					{
						return "MQTT (auto-resolve)";
					}
					return item.value();
				}
			}

			@Override
			public ProtocolVersionEnum fromString(String id)
			{
				return null;
			}
		});
		
		for (ProtocolVersionEnum protocolEnum : ProtocolVersionEnum.values())
		{
			protocolCombo.getItems().add(protocolEnum);
		}
		
		connectionTypeCombo.getItems().add("Default");
		connectionTypeCombo.getItems().add("WebSockets");
		connectionTypeCombo.getSelectionModel().selectedIndexProperty().addListener(basicOnChangeListener);
		
		brokerAddressText.textProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue)
			{				
				parent.updateConnectionName();
				
				onChange();
			}		
		});
		
		clientIdText.textProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue)
			{
				parent.updateConnectionName();
				
				onChange();
			}		
		});
		
		cleanSession.selectedProperty().addListener(basicOnChangeListener);
		
		connectionTimeout.textProperty().addListener(basicOnChangeListener);
		connectionTimeout.addEventFilter(KeyEvent.KEY_TYPED, KeyboardUtils.nonNumericKeyConsumer);
		keepAlive.textProperty().addListener(basicOnChangeListener);
		keepAlive.addEventFilter(KeyEvent.KEY_TYPED, KeyboardUtils.nonNumericKeyConsumer);
		
		reconnect.selectedProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue)
			{
				updateReconnection();
				
				onChange();
			}		
		});
		reconnectionInterval.textProperty().addListener(basicOnChangeListener);
		reconnectionInterval.addEventFilter(KeyEvent.KEY_TYPED, KeyboardUtils.nonNumericKeyConsumer);
		resubscribe.selectedProperty().addListener(basicOnChangeListener);
	}

	public void init()
	{
		// Nothing to do here	
	}

	// ===============================
	// === FXML ======================
	// ===============================

	@FXML
	private void addTimestamp()
	{
		updateClientId(true);
	}	
	
	public boolean updateClientId(final boolean addTimestamp)
	{
		String clientId = clientIdText.getText();
		String newClientId = clientId;
		
		if (MqttUtils.limitClientId(parent.getEditedConnectionDetails().getProtocol()) 
				&& clientId.length() > MqttUtils.MAX_CLIENT_LENGTH_FOR_3_1)
		{
			newClientId = clientId.substring(0, MqttUtils.MAX_CLIENT_LENGTH_FOR_3_1);
		}
		
		if (addTimestamp)
		{
			// Ignore previous client ID if generating a new one
			newClientId = MqttUtils.generateClientIdWithTimestamp(System.getProperty("user.name").replaceAll("[^A-Za-z0-9]", ""), parent.getEditedConnectionDetails().getProtocol());
		}
		
		if (!clientId.equals(newClientId))
		{
			final int currentCurrentPosition = clientIdText.getCaretPosition();
			clientIdText.setText(newClientId);
			clientIdText.positionCaret(currentCurrentPosition);
			return true;
		}
		
		return false;
	}

	public void updateClientIdLength()
	{				
		if (MqttUtils.limitClientId(parent.getEditedConnectionDetails().getProtocol()))
		{
			lengthLabel.setText("Length = " + clientIdText.getText().length() + "/" + MqttUtils.MAX_CLIENT_LENGTH_FOR_3_1);
		}
		else
		{
			lengthLabel.setText("Length = " + clientIdText.getText().length());
		}
	}

	// ===============================
	// === Logic =====================
	// ===============================

	private void onChange()
	{
		parent.onChange();
	}

	@Override
	public UserInterfaceMqttConnectionDetails readValues(final UserInterfaceMqttConnectionDetails connection)
	{
		final List<String> serverURIs = Arrays.asList(brokerAddressText.getText().split(ConnectionUtils.SERVER_DELIMITER));
		for (final String serverURI : serverURIs)
		{
			logger.trace("Adding " + serverURI);
			// Trim and remove any prefixes - these are done dynamically based on SSL mode
			connection.getServerURI().add(serverURI.trim().replaceAll(MqttUtils.TCP_PREFIX, "").replaceAll(MqttUtils.SSL_PREFIX, ""));
		}
		if (brokerAddressText.getText().endsWith(ConnectionUtils.SERVER_DELIMITER))
		{
			logger.trace("Adding empty");
			connection.getServerURI().add("");
		}
		
		connection.setClientID(clientIdText.getText());
		
		connection.setProtocol(protocolCombo.getSelectionModel().getSelectedItem());

		connection.setWebSocket(connectionTypeCombo.getSelectionModel().getSelectedIndex() == 1);
		
		connection.setCleanSession(cleanSession.isSelected());
		connection.setConnectionTimeout(Integer.valueOf(connectionTimeout.getText()));
		connection.setKeepAliveInterval(Integer.valueOf(keepAlive.getText()));
		
		if (reconnect.isSelected())
		{
			connection.setReconnectionSettings(
					new ReconnectionSettings(
							Integer.valueOf(reconnectionInterval.getText()) * 1000, 
							resubscribe.isSelected()));
		}
		
		return connection;
	}
	
	public void updateReconnection()
	{
		if (reconnect.isSelected())
		{
			reconnectionInterval.setDisable(false);
			if (reconnectionInterval.getText().length() == 0)
			{
				reconnectionInterval.setText(String.valueOf(ConfigurationUtils.DEFAULT_RECONNECTION_INTERVAL / 1000));
				resubscribe.setSelected(true);
			}
			
			resubscribe.setDisable(false);
		}
		else
		{
			reconnectionInterval.setDisable(true);
			reconnectionInterval.setText("");
			
			resubscribe.setDisable(true);
			resubscribe.setSelected(false);
		}
	}
	
	@Override
	public void displayConnectionDetails(final ConfiguredMqttConnectionDetails connection)
	{	
		// Connectivity			
		protocolCombo.getSelectionModel().select(connection.getProtocol());
		
		connectionTypeCombo.getSelectionModel().select(connection.isWebSocket() ? 1 : 0);

		brokerAddressText.setText(ConnectionUtils.serverURIsToString(connection.getServerURI()));
		clientIdText.setText(connection.getClientID());
				
		connectionTimeout.setText(connection.getConnectionTimeout().toString());
		keepAlive.setText(connection.getKeepAliveInterval().toString());
		cleanSession.setSelected(connection.isCleanSession());
		
		reconnect.setSelected(connection.getReconnectionSettings() != null);
		if (connection.getReconnectionSettings() != null)
		{
			reconnect.setSelected(true);
			reconnectionInterval.setText(String.valueOf(connection.getReconnectionSettings().getRetryInterval() / 1000));
			resubscribe.setSelected(connection.getReconnectionSettings().isResubscribe());
		}
		else
		{
			reconnect.setSelected(false);
			reconnectionInterval.setText("");
			resubscribe.setSelected(false);
		}
		
		updateReconnection();
	}		

	// ===============================
	// === Setters and getters =======
	// ===============================
	
	@Override
	public void setParent(final EditMqttConnectionController controller)
	{
		this.parent = controller;
	}
	
	public TextField getClientIdText()	
	{
		return clientIdText;
	}
	
	public TextField getBrokerAddressText()
	{
		return brokerAddressText;
	}

	public void setPerspective(SpyPerspective perspective)
	{
		final boolean detailed = perspective.equals(SpyPerspective.DETAILED) || perspective.equals(SpyPerspective.SUPER_SPY);
		
		connectionTimeout.setVisible(detailed);
		keepAlive.setVisible(detailed);
		reconnectionInterval.setVisible(detailed);
		resubscribe.setVisible(detailed);
		
		// multiLabel.setVisible(detailed);
		keepAliveLabel.setVisible(detailed);
		timeoutLabel.setVisible(detailed);
		reconnectIntervalLabel.setVisible(detailed);
		resubscribeLabel.setVisible(detailed);
	}
}
