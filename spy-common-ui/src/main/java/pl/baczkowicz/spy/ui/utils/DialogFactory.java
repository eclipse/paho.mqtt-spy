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
package pl.baczkowicz.spy.ui.utils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Pair;
import pl.baczkowicz.spy.exceptions.ExceptionUtils;
import pl.baczkowicz.spy.ui.configuration.BaseConfigurationManager;
import pl.baczkowicz.spy.ui.controls.CommandLinksDialog;
import pl.baczkowicz.spy.ui.controls.DialogAction;
import pl.baczkowicz.spy.ui.controls.WorkerProgressPane;

public class DialogFactory
{
	/**
	 * Shows an error dialog.
	 * 
	 * @param title Title of the dialog
	 * @param message Message to be displayed
	 */
	public static void createErrorDialog(final String title, final String message)
	{
		final Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setContentText(message);
		alert.setHeaderText(null);

		alert.showAndWait();
	}
	
	public static void createExceptionDialog(final String title, final String contentText, final String multilineText)
	{	
		final Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(contentText);

		final TextArea textArea = new TextArea(multilineText);
		
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane content = new GridPane();
		content.setMaxWidth(Double.MAX_VALUE);
		content.add(textArea, 0, 0);

		alert.getDialogPane().setExpandableContent(content);
		alert.getDialogPane().setExpanded(true);

		alert.showAndWait();
	}	
	
	public static void createExceptionDialog(final String title, final Exception e)
	{
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);

		createExceptionDialog(title, e.getMessage() + " - " + ExceptionUtils.getRootCauseMessage(e), sw.toString());
//		final Alert alert = new Alert(AlertType.ERROR);
//		alert.setTitle(title);
//		alert.setHeaderText(null);
//		alert.setContentText(e.getMessage() + " - " + ExceptionUtils.getRootCauseMessage(e));
//		
//		final StringWriter sw = new StringWriter();
//		final PrintWriter pw = new PrintWriter(sw);
//		e.printStackTrace(pw);
//
//		final TextArea textArea = new TextArea(sw.toString());
//		
//		textArea.setEditable(false);
//		textArea.setWrapText(true);
//
//		textArea.setMaxWidth(Double.MAX_VALUE);
//		textArea.setMaxHeight(Double.MAX_VALUE);
//		
//		GridPane.setVgrow(textArea, Priority.ALWAYS);
//		GridPane.setHgrow(textArea, Priority.ALWAYS);
//
//		GridPane content = new GridPane();
//		content.setMaxWidth(Double.MAX_VALUE);
//		content.add(textArea, 0, 0);
//
//		alert.getDialogPane().setExpandableContent(content);
//
//		alert.showAndWait();
	}	

	public static void createWarningDialog(final String title, final String message)
	{
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle(title);
		alert.setContentText(message + ".");
		alert.setHeaderText(null);

		alert.showAndWait();
	}	
	

	public static Optional<ButtonType> createQuestionDialog(final String title, final String message, final boolean showNoButton)
	{
		final Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setContentText(message);
		alert.setHeaderText(null);

		if (showNoButton)
		{
			alert.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.NO, ButtonType.YES);
		}
		else
		{
			alert.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.YES);
		}

		return alert.showAndWait();
	}

	/**
	 * Asks the user for input.
	 * 
	 * @return The user's response
	 */
	public static Optional<String> createInputDialog(final Window owner, final String title, final String label)
	{
		final TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle(title);
		dialog.setHeaderText(null);
		dialog.setContentText(label);
		dialog.initOwner(owner);
		
		return dialog.showAndWait();	
	}
	
	public static Optional<ButtonType> createQuestionDialog(final String title, final String message)
	{
		return createQuestionDialog(title, message, true);
	}	
	
	public static Optional<Pair<String, String>> createUsernameAndPasswordDialog(
			final String title, final String header, 
			final Pair<String, String> userInfo)
	{
		// Create the custom dialog
		final Dialog<Pair<String, String>> dialog = new Dialog<>();
		dialog.setResizable(false);
		dialog.setTitle(title);
		dialog.setHeaderText(header);

		// Set the icon 
		dialog.setGraphic(ImageUtils.createIcon("preferences-desktop-user-password-large", 48));

		// Set the button types
		final ButtonType loginButtonType = new ButtonType("Connect", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

		// Create the username and password labels and fields
		final GridPane grid = new GridPane();
		grid.setHgap(15);
		grid.setVgap(15);
		grid.setPadding(new Insets(15, 100, 15, 15));

		final TextField username = new TextField();
		username.setPromptText("Username");
		username.setText(userInfo.getKey());
		final PasswordField password = new PasswordField();
		password.setPromptText("Password");
		password.setText(userInfo.getValue());

		grid.add(new Label("Username:"), 0, 0);
		grid.add(username, 1, 0);
		grid.add(new Label("Password:"), 0, 1);
		grid.add(password, 1, 1);

		// Set the login button state depending on whether a username was entered
		final Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
		loginButton.setDisable(username.getText().trim().isEmpty());

		// Validate
		username.textProperty().addListener((observable, oldValue, newValue) -> 
		{
		    loginButton.setDisable(newValue.trim().isEmpty());
		});

		dialog.getDialogPane().setContent(grid);

		// Convert the result to a username-password-pair
		dialog.setResultConverter(dialogButton -> 
		{
		    if (dialogButton == loginButtonType) 
		    {
		        return new Pair<>(username.getText(), password.getText());
		    }
		    return null;
		});

		Platform.runLater(() -> username.requestFocus());
		return dialog.showAndWait();
	}

	public static Stage createWindowWithPane(final Node pane, final Scene parentScene, 
			final String title, final double margin)
	{
		final Stage stage = new Stage(StageStyle.UTILITY);
		final AnchorPane content = new AnchorPane();
		
		content.getChildren().add(pane);
		AnchorPane.setBottomAnchor(pane, margin);
		AnchorPane.setLeftAnchor(pane, margin);
		AnchorPane.setTopAnchor(pane, margin);
		AnchorPane.setRightAnchor(pane, margin);
		
		final Scene scene = new Scene(content);
		scene.getStylesheets().addAll(parentScene.getStylesheets());
		stage.initOwner(parentScene.getWindow());
		stage.setTitle(title);
		stage.setScene(scene);
		
		return stage;
	}
	
	public static Color createColorChoiceDialog(final Color color, final String title, final String label)
	{
		// Create the custom dialog
		final Dialog<Color> dialog = new Dialog<>();
		dialog.setResizable(false);
		dialog.setTitle(title);
		dialog.setHeaderText(null);		

		// Set the button types
		dialog.getDialogPane().getButtonTypes().setAll(ButtonType.APPLY, ButtonType.CANCEL);

		// Create fields
		final GridPane grid = new GridPane();
		grid.setHgap(15);
		grid.setVgap(15);
		grid.setPadding(new Insets(15, 100, 15, 15));

		final ColorPicker picker = new ColorPicker(color);

		grid.add(new Label(label), 0, 0);
		grid.add(picker, 1, 0);		

		dialog.getDialogPane().setContent(grid);

		// Convert the result
		dialog.setResultConverter(dialogButton -> 
		{
		    if (dialogButton == ButtonType.APPLY) 
		    {
		        return picker.getValue();
		    }
		    return null;
		});

		Platform.runLater(() -> picker.requestFocus());
		Optional<Color> result = dialog.showAndWait();
		
		if (result.isPresent())
		{
			return result.get();
		}
		else						
		{
			return color;
		}
	}
	
	/**
	 * Shows a worker / progress dialog.
	 * 
	 * @param readAndProcess The task backing up the dialog
	 */
	public static void createWorkerDialog(final Task<?> readAndProcess)
	{
		final Alert dialog = new Alert(AlertType.INFORMATION);
		dialog.setTitle(readAndProcess.getTitle());
		dialog.setHeaderText(null);
		final ButtonType bgButtonType = new ButtonType("Run in background", ButtonData.APPLY);
		dialog.getButtonTypes().setAll(ButtonType.CANCEL, bgButtonType);		
        
        final Label progressMessage = new Label();
        progressMessage.textProperty().bind(readAndProcess.messageProperty());
        
        dialog.setResultConverter(dialogButton -> 
		{
		    if (dialogButton == ButtonType.CANCEL) 
		    {
		        readAndProcess.cancel();
		    }
			return null;
		});

        final WorkerProgressPane content = new WorkerProgressPane(dialog);
        content.setMaxWidth(Double.MAX_VALUE);

        VBox vbox = new VBox(10, progressMessage, content);
        vbox.setMaxWidth(Double.MAX_VALUE);
        vbox.setPrefSize(400, 80);
        
        dialog.getDialogPane().setContent(vbox);
        content.setWorker(readAndProcess);
	}

	public static void setUpTextFieldFileOpenButton(final TextField field, final Button button)
	{
		button.setOnAction(new EventHandler<ActionEvent>()
		{			
			@Override
			public void handle(final ActionEvent event)
			{
				final FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Select file to open");
				
				final File selectedFile = fileChooser.showOpenDialog(field.getScene().getWindow());

				if (selectedFile != null)
				{			
					field.setText(selectedFile.getAbsolutePath());
				}				
			}
		});
	}	
	

	/**
	 * Shows the choice dialog when missing configuration file is detected.
	 * 
	 * @param title The title of the window
	 * @param window The parent
	 * 
	 * @return True when action performed / configuration file created
	 */
	public static boolean showDefaultConfigurationFileMissingChoice(final String title, final Scene scene)
	{	
		// TODO: use Java dialogs
		final DialogAction createWithSample = new DialogAction("Create " + BaseConfigurationManager.APPLICATION_NAME
				+ " configuration file with sample content",
				System.getProperty("line.separator") + "This creates a configuration file " +  
                "in \"" + BaseConfigurationManager.getDefaultHomeDirectory() + "\"" + 
                " called \"" + BaseConfigurationManager.getDefaultConfigurationFileName() + "\"" + 
                ", which will include sample connections to localhost and iot.eclipse.org.");
		
		 final DialogAction createEmpty = new DialogAction("Create empty " + BaseConfigurationManager.APPLICATION_NAME
		 		+ " configuration file",
				 System.getProperty("line.separator") + "This creates a configuration file " +  
                 "in \"" + BaseConfigurationManager.getDefaultHomeDirectory() + "\"" + 
                 " called \"" + BaseConfigurationManager.getDefaultConfigurationFileName() + "\" with no sample connections.");
		 
		 final DialogAction copyExisting = new DialogAction("Copy existing " + BaseConfigurationManager.APPLICATION_NAME
		 		+ " configuration file",
				 System.getProperty("line.separator") + "This copies an existing configuration file (selected in the next step) " +  
                 "to \"" + BaseConfigurationManager.getDefaultHomeDirectory() + "\"" + 
                 " and renames it to \"" + BaseConfigurationManager.getDefaultConfigurationFileName() + "\".");
		 
		 final DialogAction dontDoAnything = new DialogAction("Don't do anything",
				 System.getProperty("line.separator") + "You can still point " + BaseConfigurationManager.APPLICATION_NAME
				 		+ " at your chosen configuration file " +  
                 "by using the \"--configuration=my_custom_path\"" + 
                 " command line parameter or open a configuration file from the main menu.");
		
		final List<DialogAction> links = Arrays.asList(createWithSample, createEmpty, copyExisting, dontDoAnything);
		
		Optional<DialogAction> response = CommandLinksDialog.showCommandLinks(title,
				"Please select one of the following options with regards to the " + BaseConfigurationManager.APPLICATION_NAME
				+ " configuration file:",
				links.get(0), links, 550, 650, 30, 110, 
				scene.getStylesheets());
		
		boolean configurationFileCreated = false;
		
		if (!response.isPresent())
		{
			// Do nothing
		}
		else if (response.get().getHeading().toLowerCase().contains("sample"))
		{
			configurationFileCreated = BaseConfigurationManager.createDefaultConfigFromClassPath("sample");
		}
		else if (response.get().getHeading().toLowerCase().contains("empty"))
		{
			configurationFileCreated = BaseConfigurationManager.createDefaultConfigFromClassPath("empty");
		}
		else if (response.get().getHeading().toLowerCase().contains("copy"))
		{
			final FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Select configuration file to copy");
			String extensions = "xml";
			fileChooser.setSelectedExtensionFilter(new ExtensionFilter("XML file", extensions));

			final File selectedFile = fileChooser.showOpenDialog(scene.getWindow());

			if (selectedFile != null)
			{
				configurationFileCreated = BaseConfigurationManager.createDefaultConfigFromFile(selectedFile);
			}
		}
		else
		{
			// Do nothing
		}
		
		return configurationFileCreated;
	}	
}
