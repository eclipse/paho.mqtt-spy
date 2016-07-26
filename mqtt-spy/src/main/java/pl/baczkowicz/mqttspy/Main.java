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
package pl.baczkowicz.mqttspy;

import java.io.File;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.ui.MainController;
import pl.baczkowicz.mqttspy.ui.events.EventManager;
import pl.baczkowicz.spy.ui.configuration.UiProperties;
import pl.baczkowicz.spy.ui.utils.FxmlUtils;

/** 
 * The main class, loading the app.
 */
public class Main extends Application
{	
	/** Name of the parameter supplied on the command line to indicate where to find the configuration file - optional. */
	private final static String CONFIGURATION_PARAMETER_NAME = "configuration";
	
	/** Name of the parameter supplied on the command line to indicate no configuration wanted - optional. */
	private final static String NO_CONFIGURATION_PARAMETER_NAME = "no-configuration";

	@Override
	/**
	 * Starts the application.
	 */
	public void start(final Stage primaryStage)
	{
		final EventManager<FormattedMqttMessage> eventManager = new EventManager<FormattedMqttMessage>();			
				
		try
		{
			final ConfigurationManager configurationManager = new ConfigurationManager(eventManager);			
			
			// Load the main window
			FxmlUtils.setParentClass(getClass());
			final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("MainWindow.fxml");

			// Get the associated pane
			AnchorPane pane = (AnchorPane) loader.load();
			
			final Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
			
			// Set scene width, height and style
			final double height = Math.min(UiProperties.getApplicationHeight(configurationManager.getUiPropertyFile()), primaryScreenBounds.getHeight());			
			final double width = Math.min(UiProperties.getApplicationWidth(configurationManager.getUiPropertyFile()), primaryScreenBounds.getWidth());
			
			final Scene scene = new Scene(pane, width, height);			
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			// Get the associated controller
			final MainController mainController = (MainController) loader.getController();
			mainController.setEventManager(eventManager);
			mainController.setConfigurationManager(configurationManager);
			mainController.setSelectedPerspective(UiProperties.getApplicationPerspective(configurationManager.getUiPropertyFile()));
			mainController.getResizeMessagePaneMenu().setSelected(UiProperties.getResizeMessagePane(configurationManager.getUiPropertyFile()));

			// Set the stage's properties
			primaryStage.setScene(scene);	
			primaryStage.setMaximized(UiProperties.getApplicationMaximized(configurationManager.getUiPropertyFile()));
			
			// Initialise resources in the main controller			
			mainController.setApplication(this);
			mainController.setStage(primaryStage);
			mainController.setLastHeight(height);
			mainController.setLastWidth(width);
			mainController.init();
			final Image applicationIcon = new Image(getClass().getResourceAsStream("/images/large/mqtt-spy-logo.png"));
		    primaryStage.getIcons().add(applicationIcon);
			
			// Show the main window
			primaryStage.show();
			
			// Load the config file if specified
			final String noConfig = this.getParameters().getNamed().get(NO_CONFIGURATION_PARAMETER_NAME); 
			final String configurationFileLocation = this.getParameters().getNamed().get(CONFIGURATION_PARAMETER_NAME);
			
			if (noConfig != null)
			{
				// Do nothing - no config wanted
			}
			else if (configurationFileLocation != null)
			{
				mainController.loadConfigurationFileAndShowErrorWhenApplicable(new File(configurationFileLocation));				
			}
			else
			{
				// If no configuration parameter is specified, use the user's home directory and the default configuration file name
				mainController.loadDefaultConfigurationFile();						
			}
		}
		catch (Exception e)
		{
			LoggerFactory.getLogger(Main.class).error("Error while loading the main window", e);
		}
	}

	public static void main(String[] args)
	{
		launch(args);
	}
}
