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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.AnchorPane;
import pl.baczkowicz.mqttspy.common.generated.SecureSocketSettings;
import pl.baczkowicz.mqttspy.configuration.ConfiguredConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;
import pl.baczkowicz.mqttspy.ui.EditConnectionController;
import pl.baczkowicz.spy.common.generated.SecureSocketModeEnum;
import pl.baczkowicz.spy.ui.utils.DialogFactory;

/**
 * Controller for editing a single connection - security tab - certificates pane.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class EditConnectionSecurityTlsCertificatesController extends AnchorPane implements Initializable
{
	/** The parent controller. */
	private EditConnectionController parent;
	
	@FXML
	private AnchorPane tlsCertificatesPane;

	// Certificates
	
	@FXML
	private TextField serverCertificateFile;
	
	@FXML
	private Button serverCertificateFileButton;
	
	@FXML
	private PasswordField clientPassword;
	
	@FXML
	private TextField clientKeyFile;
	
	@FXML
	private Button clientKeyFileButton;
	
	@FXML
	private TextField clientCertificateFile;
	
	@FXML
	private Button clientCertificateFileButton;
	
	@FXML
	private Label clientKeyPasswordLabel;
	
	@FXML
	private Label clientKeyFileLabel;
	
	@FXML
	private Label clientCertificateFileLabel;
	
	@FXML
	private Label clientKeyPemLabel;
	
	@FXML
	private CheckBox clientKeyPemFormatted;
	
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
		// Set up edit buttons
		DialogFactory.setUpTextFieldFileOpenButton(serverCertificateFile, serverCertificateFileButton);
		DialogFactory.setUpTextFieldFileOpenButton(clientCertificateFile, clientCertificateFileButton);
		DialogFactory.setUpTextFieldFileOpenButton(clientKeyFile, clientKeyFileButton);
		
		// Certificates
		serverCertificateFile.textProperty().addListener(basicOnChangeListener);
		clientCertificateFile.textProperty().addListener(basicOnChangeListener);
		clientKeyFile.textProperty().addListener(basicOnChangeListener);
		clientPassword.textProperty().addListener(basicOnChangeListener);
		clientKeyPemFormatted.selectedProperty().addListener(basicOnChangeListener);
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
	
	public void updateSSL(final SecureSocketModeEnum mode)
	{
		final boolean certificates = SecureSocketModeEnum.SERVER_ONLY.equals(mode)	|| SecureSocketModeEnum.SERVER_AND_CLIENT.equals(mode);
				
		if (certificates)
		{
			final boolean serverAndClient = SecureSocketModeEnum.SERVER_AND_CLIENT.equals(mode);		
		
			clientPassword.setVisible(serverAndClient);
			clientKeyFile.setVisible(serverAndClient);
			clientCertificateFile.setVisible(serverAndClient);
			
			clientKeyPasswordLabel.setVisible(serverAndClient);
			clientKeyFileLabel.setVisible(serverAndClient);
			clientCertificateFileLabel.setVisible(serverAndClient);
			clientKeyPemLabel.setVisible(serverAndClient);
			clientKeyPemFormatted.setVisible(serverAndClient);
			
			clientCertificateFileButton.setVisible(serverAndClient);
			clientKeyFileButton.setVisible(serverAndClient);
		}
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
						
			final boolean certificates = SecureSocketModeEnum.SERVER_ONLY.equals(mode)
					|| SecureSocketModeEnum.SERVER_AND_CLIENT.equals(mode);
			
			if (certificates)
			{			
				sslSettings.setCertificateAuthorityFile(serverCertificateFile.getText());
				sslSettings.setClientCertificateFile(clientCertificateFile.getText());
				sslSettings.setClientKeyFile(clientKeyFile.getText());
				sslSettings.setClientKeyPassword(clientPassword.getText());				
				sslSettings.setClientKeyPEM(clientKeyPemFormatted.isSelected());
			}			
			
			connection.setSSL(sslSettings);
		}
	}

	public void displayConnectionDetails(final ConfiguredConnectionDetails connection)
	{
		if (connection.getSSL() != null)
		{	
			// Certificates
			serverCertificateFile.setText(connection.getSSL().getCertificateAuthorityFile());
			clientCertificateFile.setText(connection.getSSL().getClientCertificateFile());
			clientKeyFile.setText(connection.getSSL().getClientKeyFile());
			clientPassword.setText(connection.getSSL().getClientKeyPassword());	
			clientKeyPemFormatted.setSelected(Boolean.TRUE.equals(connection.getSSL().isClientKeyPEM()));
		}
	}
	
	@FXML
	private void editCaCrtFile()
	{
		// TODO
	}
	
	@FXML
	private void editClientCrtFile()
	{
		// TODO
	}
	
	@FXML
	private void editClientKeyFile()
	{
		// TODO
	}

	// ===============================
	// === Setters and getters =======
	// ===============================
	
	public void setParent(final EditConnectionController controller)
	{
		this.parent = controller;
	}

	/**
	 * @return the tlsCertificatesPane
	 */
	public AnchorPane getTlsCertificatesPane()
	{
		return tlsCertificatesPane;
	}

	/**
	 * @param tlsCertificatesPane the tlsCertificatesPane to set
	 */
	public void setTlsCertificatesPane(AnchorPane tlsCertificatesPane)
	{
		this.tlsCertificatesPane = tlsCertificatesPane;
	}
}
