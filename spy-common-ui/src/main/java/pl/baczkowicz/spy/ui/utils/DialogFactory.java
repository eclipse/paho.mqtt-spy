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
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;
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
		dialog.setGraphic(ImageUtils.createLargeIcon("preferences-desktop-user-password", 48));

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
		final Stage stage = new Stage();
		final AnchorPane content = new AnchorPane();
		
		content.getChildren().add(pane);
		AnchorPane.setBottomAnchor(pane, margin);
		AnchorPane.setLeftAnchor(pane, margin);
		AnchorPane.setTopAnchor(pane, margin);
		AnchorPane.setRightAnchor(pane, margin);
		
		final Scene scene = new Scene(content);
		scene.getStylesheets().addAll(parentScene.getStylesheets());
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
}
