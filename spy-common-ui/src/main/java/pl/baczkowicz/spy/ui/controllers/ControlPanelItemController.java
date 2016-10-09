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
package pl.baczkowicz.spy.ui.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.ui.controlpanel.ItemStatus;
import pl.baczkowicz.spy.ui.utils.ImageUtils;

/**
 * Controller for a single control panel item.
 */
public class ControlPanelItemController extends AnchorPane implements Initializable
{
	final static Logger logger = LoggerFactory.getLogger(ControlPanelItemController.class);

	@FXML
	private ImageView statusIcon;

	@FXML
	private VBox itemsBox;
	
	@FXML
	private Label titleText;
	
	@FXML
	private Label detailsText;
	
	@FXML
	private Button smallButton1;
	
	@FXML
	private FlowPane details;

	@FXML
	private Button smallButton2;

	@FXML
	private ProgressIndicator progressIndicator;
	
	private ItemStatus status = ItemStatus.ERROR;
	
	private boolean showProgress;
		
	// ===============================
	// === Initialisation ============
	// ===============================

	public void initialize(URL location, ResourceBundle resources)
	{		
		//
	}	
	
	public void init()
	{
		//
	}
	
	public static void setButtonProperties(final Button button, final String imageName, final boolean visibility, final EventHandler<ActionEvent> action)
	{
		button.setVisible(true);
		button.setGraphic(ImageUtils.createIcon(imageName));
		button.setOnAction(action);
	}
	
	public static void setButtonProperties(final Button button, final String imageLocation, final boolean visibility)
	{
		button.setVisible(true);
		button.setGraphic(new ImageView(new Image(ControlPanelItemController.class.getResource(imageLocation).toString())));
	}
	
	public Button getButton1()	
	{
		return smallButton1;
	}
	
	public Button getButton2()	
	{
		return smallButton2;
	}
	
	public static String getStatusIconName(final ItemStatus status)
	{
		String imageLocation;
		
		switch (status)
		{
			case OK:
				imageLocation = "dialog-ok-apply-large";
				break;
			case INFO:
				imageLocation = "dialog-information-large";
				break;
			case WARN:
				imageLocation = "dialog-warning-large";
				break;
			case ERROR:
				imageLocation = "dialog-error-large";
				break;
			case STATS:
				imageLocation = "rating-large";
				break;
			default:
				imageLocation = "dialog-error-large";
				break;
		}
		
		return imageLocation;
	}
	
	public void refresh()
	{
		String imageName = getStatusIconName(status);
		
		if (showProgress)
		{
			progressIndicator.setVisible(true);
			statusIcon.setVisible(false);
		}
		else
		{
			progressIndicator.setVisible(false);
			statusIcon.setVisible(true);
			
			statusIcon.setImage(ImageUtils.createIcon(imageName).getImage());		
			if (status == ItemStatus.OK)
			{
				statusIcon.setLayoutY(5);
				statusIcon.setLayoutX(5);
				statusIcon.setFitHeight(64);
				statusIcon.setFitWidth(64);
			}
			else
			{
				statusIcon.setLayoutY(10);
				statusIcon.setLayoutX(10);
				statusIcon.setFitHeight(64);
				statusIcon.setFitWidth(64);
			}
		}
	}

	// ===============================
	// === FXML ======================
	// ===============================

	// ===============================
	// === Logic =====================
	// ===============================

	// ===============================
	// === Setters and getters =======
	// ===============================
	
	public boolean isShowProgress()
	{
		return showProgress;
	}

	public void setShowProgress(boolean showProgress)
	{
		this.showProgress = showProgress;
	}

	public void setValues(final ItemStatus status, final String title, final String details)
	{
		this.status = status;		
		this.titleText.setText(title);
		this.detailsText.setText(details);
	}
	
	public void setStatus(final ItemStatus status)
	{
		this.status = status;
	}
	
	public void setTitle(final String title)
	{
		this.titleText.setText(title);
	}
	
	public void setDetails(final String details)
	{
		this.detailsText.setText(details);
	}
	
	public VBox getCustomItems()
	{
		return this.itemsBox;
	}	
	
	public FlowPane getDetails()
	{
		return details;
	}
}
