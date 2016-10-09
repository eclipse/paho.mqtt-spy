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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.StringConverter;

import javax.net.ssl.SSLContext;

import pl.baczkowicz.mqttspy.common.generated.SecureSocketSettings;
import pl.baczkowicz.mqttspy.configuration.ConfiguredMqttConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.UserAuthenticationOptions;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.ui.controllers.EditMqttConnectionController;
import pl.baczkowicz.spy.common.generated.SecureSocketModeEnum;
import pl.baczkowicz.spy.common.generated.UserCredentials;
import pl.baczkowicz.spy.configuration.BaseConfigurationUtils;
import pl.baczkowicz.spy.ui.utils.UiUtils;

/**
 * Controller for editing a single connection - security tab.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class EditConnectionSecurityController extends AnchorPane implements Initializable, IEditConnectionSubController
{
	/** The parent controller. */
	private EditMqttConnectionController parent;
	
	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private EditConnectionSecurityTlsCertificatesController certificatesPaneController;
	
	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private EditConnectionSecurityTlsKeyStoresController keyStoresPaneController;
	
	/**
	 * The name of this field needs to be set to the name of the pane +
	 * Controller (i.e. <fx:id>Controller).
	 */
	@FXML
	private EditConnectionSecurityTlsPropertiesController propertiesPaneController;

	// Security
	
	@FXML
	private CheckBox userAuthentication;
	
	@FXML
	private TextField username;
	
	@FXML
	private RadioButton predefinedUsername;
	
	@FXML
	private RadioButton askForUsername;
	
	@FXML
	private RadioButton askForPassword;
	
	@FXML
	private RadioButton predefinedPassword;
	
	@FXML
	private PasswordField password;
	
	@FXML
	private ComboBox<SecureSocketModeEnum> modeCombo;
	
	@FXML
	private ComboBox<String> protocolCombo;
	
	@FXML
	private AnchorPane customSocketFactoryPane;
	
	@FXML
	private Tab tlsTab;
	
	@FXML
	private Tab authTab;
	
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
		// Authentication
		userAuthentication.selectedProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue)
			{
				updateUserAuthentication();
				
				onChange();
			}		
		});
		username.textProperty().addListener(basicOnChangeListener);
		password.textProperty().addListener(basicOnChangeListener);
		askForUsername.selectedProperty().addListener(basicOnChangeListener);
		askForPassword.selectedProperty().addListener(basicOnChangeListener);
		predefinedUsername.selectedProperty().addListener(basicOnChangeListener);
		predefinedPassword.selectedProperty().addListener(basicOnChangeListener);
		
		// SSL
		protocolCombo.getSelectionModel().selectedIndexProperty().addListener(basicOnChangeListener);
		
		final Map<SecureSocketModeEnum, String> modeEnumText = new HashMap<>();
		modeEnumText.put(SecureSocketModeEnum.DISABLED, 			"Disabled");
		
		// Certificates and keys provided externally, e.g.
		// -Djavax.net.ssl.trustStore=/home/kamil/certificates/public_brokers.jks
		// -Djavax.net.ssl.trustStorePassword=password
		modeEnumText.put(SecureSocketModeEnum.BASIC, 				"Certificates & keys provided externally");
		
		// Server only - cert / trust store
		modeEnumText.put(SecureSocketModeEnum.SERVER_ONLY, 			"CA certificate");
		modeEnumText.put(SecureSocketModeEnum.SERVER_KEYSTORE, 		"CA trust store");
		
		// Server and client
		modeEnumText.put(SecureSocketModeEnum.SERVER_AND_CLIENT, 	"CA certificate & client certificate/key");
		modeEnumText.put(SecureSocketModeEnum.SERVER_AND_CLIENT_KEYSTORES, "CA trust store & client key store");
		
		// SSL&TLS properties
		modeEnumText.put(SecureSocketModeEnum.PROPERTIES, 			"TLS/SSL properties");
		
		try
		{
			final SSLContext context = SSLContext.getDefault();		
			final String[] values = context.getSupportedSSLParameters().getProtocols();
			final List<String> filteredValues = new ArrayList<>();
			filteredValues.addAll(Arrays.asList(values));
			final Iterator<String> i = filteredValues.iterator();
			while (i.hasNext()) 
			{
				if (i.next().contains("Hello"))
				{
					i.remove();
				}				
			}
			
			protocolCombo.getSelectionModel().selectedIndexProperty().addListener(basicOnChangeListener);
			protocolCombo.getItems().addAll(filteredValues);			
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}		
		
		// SSL Mode
		modeCombo.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener()
		{
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue)
			{
				updateSSL();											
				
				onChange();
			}		
		});
		modeCombo.setCellFactory(new Callback<ListView<SecureSocketModeEnum>, ListCell<SecureSocketModeEnum>>()
		{
			@Override
			public ListCell<SecureSocketModeEnum> call(ListView<SecureSocketModeEnum> l)
			{
				return new ListCell<SecureSocketModeEnum>()
				{
					@Override
					protected void updateItem(SecureSocketModeEnum item, boolean empty)
					{
						super.updateItem(item, empty);
						if (item == null || empty)
						{
							setText(null);
						}
						else
						{									
							setText(modeEnumText.get(item));
						}
					}
				};
			}
		});
		modeCombo.setConverter(new StringConverter<SecureSocketModeEnum>()
		{
			@Override
			public String toString(SecureSocketModeEnum item)
			{
				if (item == null)
				{
					return null;
				}
				else
				{
					return modeEnumText.get(item);
				}
			}

			@Override
			public SecureSocketModeEnum fromString(String id)
			{
				return null;
			}
		});
		
		for (SecureSocketModeEnum modeEnum : SecureSocketModeEnum.values())
		{
			modeCombo.getItems().add(modeEnum);
		}
	}

	public void init()
	{
		certificatesPaneController.setParent(parent);
		keyStoresPaneController.setParent(parent);
		propertiesPaneController.setParent(parent);
	}

	// ===============================
	// === Logic =====================
	// ===============================

	public void onChange()
	{	
		parent.onChange();
	}
	
	public void updateSSL()
	{
		final SecureSocketModeEnum mode = modeCombo.getSelectionModel().getSelectedItem();
		
		final boolean certificates = SecureSocketModeEnum.SERVER_ONLY.equals(mode) || SecureSocketModeEnum.SERVER_AND_CLIENT.equals(mode);
		final boolean keyStores = SecureSocketModeEnum.SERVER_KEYSTORE.equals(mode)	|| SecureSocketModeEnum.SERVER_AND_CLIENT_KEYSTORES.equals(mode);
		
		// Set up pane visibility		
		propertiesPaneController.getPropertiesPane().setVisible(SecureSocketModeEnum.PROPERTIES.equals(mode));
		certificatesPaneController.getTlsCertificatesPane().setVisible(certificates);
		keyStoresPaneController.getTlsKeyStoresPane().setVisible(keyStores);
		customSocketFactoryPane.setVisible(certificates || keyStores || SecureSocketModeEnum.BASIC.equals(mode));
		
		if (certificates)
		{
			certificatesPaneController.updateSSL(mode);
		}
		else if (keyStores)
		{
			keyStoresPaneController.updateSSL(mode);
		}
		
		updateTlsIcon(!SecureSocketModeEnum.DISABLED.equals(mode));
	}

	@Override
	public UserInterfaceMqttConnectionDetails readValues(final UserInterfaceMqttConnectionDetails connection)
	{
		if (userAuthentication.isSelected())
		{
			final UserAuthenticationOptions userAuthentication = new UserAuthenticationOptions();
						
			userAuthentication.setAskForUsername(askForUsername.isSelected());
			userAuthentication.setAskForPassword(askForPassword.isSelected());
			
			final UserCredentials userCredentials = new UserCredentials();
			userCredentials.setUsername(username.getText());
			userCredentials.setPassword(BaseConfigurationUtils.encodePassword(password.getText()));			
			
			connection.setUserAuthentication(userAuthentication);
			connection.setUserCredentials(userCredentials);
		}
		
		if (modeCombo.getSelectionModel().getSelectedItem() == null || SecureSocketModeEnum.DISABLED.equals(modeCombo.getSelectionModel().getSelectedItem()))
		{
			connection.setSSL(null);
		}		
		else
		{
			final SecureSocketSettings sslSettings = new SecureSocketSettings();
			sslSettings.setMode(modeCombo.getSelectionModel().getSelectedItem());
			connection.setSSL(sslSettings);
			
			final boolean certificates = SecureSocketModeEnum.SERVER_ONLY.equals(modeCombo.getSelectionModel().getSelectedItem())
					|| SecureSocketModeEnum.SERVER_AND_CLIENT.equals(modeCombo.getSelectionModel().getSelectedItem());
			final boolean keyStores = SecureSocketModeEnum.SERVER_KEYSTORE.equals(modeCombo.getSelectionModel().getSelectedItem())
					|| SecureSocketModeEnum.SERVER_AND_CLIENT_KEYSTORES.equals(modeCombo.getSelectionModel().getSelectedItem());
			
			propertiesPaneController.readAndSetValues(modeCombo.getSelectionModel().getSelectedItem(), connection);
			
			if (SecureSocketModeEnum.BASIC.equals(modeCombo.getSelectionModel().getSelectedItem()))
			{
				sslSettings.setProtocol(protocolCombo.getSelectionModel().getSelectedItem());
			}
			else if (certificates)
			{
				sslSettings.setProtocol(protocolCombo.getSelectionModel().getSelectedItem());		
				certificatesPaneController.readAndSetValues(modeCombo.getSelectionModel().getSelectedItem(), connection);
			}
			else if (keyStores)
			{
				sslSettings.setProtocol(protocolCombo.getSelectionModel().getSelectedItem());
				keyStoresPaneController.readAndSetValues(modeCombo.getSelectionModel().getSelectedItem(), connection);
			}
		}
		
		return connection;
	}

	public void updateUserAuthentication()
	{
		if (userAuthentication.isSelected())
		{
			predefinedUsername.setDisable(false);
			predefinedPassword.setDisable(false);			
			askForUsername.setDisable(false);
			askForPassword.setDisable(false);
			
			if (askForUsername.isSelected())
			{
				username.setDisable(true);
			}
			else				
			{
				username.setDisable(false);
			}
			
			if (askForPassword.isSelected())
			{
				password.setDisable(true);
			}
			else				
			{
				password.setDisable(false);
			}
		}
		else
		{
			username.setDisable(true);			
			password.setDisable(true);
			predefinedUsername.setDisable(true);
			predefinedPassword.setDisable(true);
			askForUsername.setDisable(true);
			askForPassword.setDisable(true);
		}
		
		updateAuthIcon(userAuthentication.isSelected());
	}
	
	@Override
	public void displayConnectionDetails(final ConfiguredMqttConnectionDetails connection)
	{
		// Security
		userAuthentication.setSelected(connection.getUserAuthentication() != null && connection.getUserCredentials() != null);

		if (userAuthentication.isSelected())
		{			
			username.setText(connection.getUserCredentials().getUsername());			
			password.setText(BaseConfigurationUtils.decodePassword(connection.getUserCredentials().getPassword()));	
			
			askForUsername.setSelected(connection.getUserAuthentication().isAskForUsername());
			askForPassword.setSelected(connection.getUserAuthentication().isAskForPassword());
			
			predefinedUsername.setSelected(!connection.getUserAuthentication().isAskForUsername());
			predefinedPassword.setSelected(!connection.getUserAuthentication().isAskForPassword());
		}
		else
		{
			username.setText("");
			password.setText("");
			
			predefinedUsername.setSelected(false);
			predefinedPassword.setSelected(false);
			
			askForUsername.setSelected(true);
			askForPassword.setSelected(true);
		}
		
		if (connection.getSSL() == null)
		{
			modeCombo.getSelectionModel().select(SecureSocketModeEnum.DISABLED);
		}
		else
		{
			propertiesPaneController.displayConnectionDetails(connection);
			
			modeCombo.getSelectionModel().select(connection.getSSL().getMode());			

			for (final String item : protocolCombo.getItems())
			{
				if (item.equals(connection.getSSL().getProtocol()))
				{
					protocolCombo.getSelectionModel().select(item);
					break;
				}
			}			

			certificatesPaneController.displayConnectionDetails(connection);
			keyStoresPaneController.displayConnectionDetails(connection);
		}
				
		showIcons(connection);
		updateUserAuthentication();
		updateSSL();			
	}	
	
	private void updateAuthIcon(boolean authEnabled)
	{
		final HBox authIcon = new HBox();
		UiUtils.createAuthIcon(authIcon, authEnabled, true);
		authTab.setGraphic(authIcon);
	}
	
	private void updateTlsIcon(boolean tlsEnabled)
	{
		final HBox tlsIcon = new HBox();
		UiUtils.createTlsIcon(tlsIcon, tlsEnabled, true);
		tlsTab.setGraphic(tlsIcon);		
	}
	
	private void showIcons(final ConfiguredMqttConnectionDetails connection)
	{
		updateTlsIcon(connection.getSSL() != null);
		updateAuthIcon(connection.getUserCredentials() != null);
	}

	// ===============================
	// === Setters and getters =======
	// ===============================
	
	@Override
	public void setParent(final EditMqttConnectionController controller)
	{
		this.parent = controller;
	}
}
