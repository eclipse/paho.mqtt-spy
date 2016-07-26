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
package pl.baczkowicz.mqttspy.ui.utils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javafx.scene.control.Tooltip;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;
import javafx.util.Pair;
import pl.baczkowicz.mqttspy.common.generated.UserCredentials;
import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.configuration.ConfigurationUtils;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.connectivity.MqttConnectionStatus;
import pl.baczkowicz.spy.configuration.BaseConfigurationUtils;
import pl.baczkowicz.spy.ui.controls.CommandLinksDialog;
import pl.baczkowicz.spy.ui.controls.DialogAction;
import pl.baczkowicz.spy.ui.utils.DialogFactory;

/**
 * Utilities for creating all sorts of dialogs.
 */
public class DialogUtils
{

	/**
	 * Asks the user to review/complete username and password information.
	 * 
	 * @param owner The window owner
	 * @param connectionName Name of the connection
	 * @param userCredentials Existing user credentials
	 * 
	 * @return True when confirmed by user
	 */
	public static boolean createMqttUsernameAndPasswordDialog(final Object owner,
			final String connectionName, final UserCredentials userCredentials)
	{
		final Pair<String, String> userInfo = new Pair<String, String>(
				userCredentials.getUsername(), 
				BaseConfigurationUtils.decodePassword(userCredentials.getPassword()));
		
		Optional<Pair<String, String>> response = DialogFactory.createUsernameAndPasswordDialog(
				"MQTT user credentials",
				"User credentials for connection " + connectionName,
				userInfo);
		
		if (response.isPresent())
		{
			userCredentials.setUsername(response.get().getKey());			
			userCredentials.setPassword(BaseConfigurationUtils.encodePassword(response.get().getValue()));
			return true;
		}
		
		return false;
	}
	
	/**
	 * Shows the choice dialog when missing configuration file is detected.
	 * 
	 * @param title The title of the window
	 * @param window The parent
	 * 
	 * @return True when action performed / configuration file created
	 */
	public static boolean showDefaultConfigurationFileMissingChoice(final String title, final Window window)
	{	
		// TODO: use Java dialogs
		final DialogAction createWithSample = new DialogAction("Create mqtt-spy configuration file with sample content",
				System.getProperty("line.separator") + "This creates a configuration file " +  
                "in \"" + ConfigurationManager.DEFAULT_HOME_DIRECTORY + "\"" + 
                " called \"" + ConfigurationManager.DEFAULT_FILE_NAME + "\"" + 
                ", which will include sample connections to localhost and iot.eclipse.org.");
		
		 final DialogAction createEmpty = new DialogAction("Create empty mqtt-spy configuration file",
				 System.getProperty("line.separator") + "This creates a configuration file " +  
                 "in \"" + ConfigurationManager.DEFAULT_HOME_DIRECTORY + "\"" + 
                 " called \"" + ConfigurationManager.DEFAULT_FILE_NAME + "\" with no sample connections.");
		 
		 final DialogAction copyExisting = new DialogAction("Copy existing mqtt-spy configuration file",
				 System.getProperty("line.separator") + "This copies an existing configuration file (selected in the next step) " +  
                 "to \"" + ConfigurationManager.DEFAULT_HOME_DIRECTORY + "\"" + 
                 " and renames it to \"" + ConfigurationManager.DEFAULT_FILE_NAME + "\".");
		 
		 final DialogAction dontDoAnything = new DialogAction("Don't do anything",
				 System.getProperty("line.separator") + "You can still point mqtt-spy at your chosen configuration file " +  
                 "by using the \"--configuration=my_custom_path\"" + 
                 " command line parameter or open a configuration file from the main menu.");
		
		final List<DialogAction> links = Arrays.asList(createWithSample, createEmpty, copyExisting, dontDoAnything);
		
		Optional<DialogAction> response = CommandLinksDialog.showCommandLinks(title,
				"Please select one of the following options with regards to the mqtt-spy configuration file:",
				links.get(0), links, 650, 30, 110, 
				Arrays.asList(DialogUtils.class.getResource("/pl/baczkowicz/mqttspy/application.css").toExternalForm()));
		
		boolean configurationFileCreated = false;
		
		if (!response.isPresent())
		{
			// Do nothing
		}
		else if (response.get().getHeading().toLowerCase().contains("sample"))
		{
			configurationFileCreated = ConfigurationUtils.createDefaultConfigFromClassPath("sample");
		}
		else if (response.get().getHeading().toLowerCase().contains("empty"))
		{
			configurationFileCreated = ConfigurationUtils.createDefaultConfigFromClassPath("empty");
		}
		else if (response.get().getHeading().toLowerCase().contains("copy"))
		{
			final FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Select configuration file to copy");
			String extensions = "xml";
			fileChooser.setSelectedExtensionFilter(new ExtensionFilter("XML file", extensions));

			final File selectedFile = fileChooser.showOpenDialog(window);

			if (selectedFile != null)
			{
				configurationFileCreated = ConfigurationUtils.createDefaultConfigFromFile(selectedFile);
			}
		}
		else
		{
			// Do nothing
		}
		
		return configurationFileCreated;
	}	
    
    /**
	 * Updates the given connection tooltip with connection information.
	 * 
	 * @param connection The connection to which the tooltip refers
	 * @param tooltip The tooltip to be updated
	 */
	public static void updateConnectionTooltip(final MqttAsyncConnection connection, final Tooltip tooltip)
	{
		final StringBuffer sb = new StringBuffer();
		sb.append("Status: " + connection.getConnectionStatus().toString().toLowerCase());
		
		if (MqttConnectionStatus.CONNECTED.equals(connection.getConnectionStatus()))
		{
			sb.append(" (" + connection.getLastSuccessfulyConnectionAttempt() + ")");
			
			sb.append(System.getProperty("line.separator"));
			final String sslStatus = connection.getProperties().getSSL() != null ? "on" : "off";
			final String userAuthStatus = connection.getProperties().getUserCredentials() != null ? "on" : "off";
			sb.append("Security: TLS/SSL is " +  sslStatus + "; user authentication is " + userAuthStatus);
		}
		
		if (connection.getConnectionAttempts() > 1)
		{
			sb.append(System.getProperty("line.separator"));
			sb.append("Connection attempts: " + connection.getConnectionAttempts());
		}
				
		if (connection.getDisconnectionReason() != null && !connection.getDisconnectionReason().isEmpty())
		{
			sb.append(System.getProperty("line.separator"));
			sb.append("Last error: " + connection.getDisconnectionReason().toLowerCase());
		}	
		
		tooltip.setText(sb.toString());
	}
}
