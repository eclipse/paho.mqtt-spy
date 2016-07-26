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
package pl.baczkowicz.mqttspy.ui.controlpanel;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.ui.ControlPanelController;

/**
 * This is the getting involved tooltip, which extends the Tooltip class to add some
 * custom behaviour (only hiding the tooltip when mouse moved more than X; not hiding the tooltip after X seconds).
 */
public class GettingInvolvedTooltip extends Tooltip
{		
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(GettingInvolvedTooltip.class);
	
	/** True if the default JavaFX behaviour requested the tooltip to be hidden. */
	private boolean hideRequested;
	
	/** Mouse position on showing the tooltip. */
	private MouseEvent mousePositionOnShown;
	
	/** Current mouse position. */
	private MouseEvent currentMousePosition;
	
	/**
	 * Creates the tooltip.
	 */
	public GettingInvolvedTooltip()
	{
		final HBox tooltipContent = new HBox();		
		final ImageView logo = new ImageView(new Image(ControlPanelController.class.getResource("/images/large/mqtt-spy-logo.png").toString()));
		logo.setFitHeight(70);
		logo.setFitWidth(70);
		final Label text = new Label(
				"mqtt-spy needs you! Please support the project" + System.lineSeparator()
				+ "by raising bugs, " + "helping out with testing" + System.lineSeparator()
				+ "or making a charity donation. " + System.lineSeparator()
				+ "See http://github.com/kamilfb/mqtt-spy/wiki/Getting-involved" + System.lineSeparator()
				+ "for more information on how to get involved." + System.lineSeparator()
				);		
		text.setFont(new Font("System", 11));
		tooltipContent.getChildren().addAll(logo, text);
		tooltipContent.setSpacing(20);
		tooltipContent.setPadding(new Insets(0, 10, 0, 0));
		
		setGraphic(tooltipContent);
		setAutoHide(false);
		setHideOnEscape(true);
		setOpacity(0.95);
	}
	
	/**
	 * JavaFX Tooltip's show method.
	 */
	@Override
	protected void show()
	{			
		this.getScene().setOnKeyPressed(new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent event)
			{
				if (event.getCode().equals(KeyCode.ESCAPE))
				{
					logger.trace("Escape - closing tooltip");
					hideTooltip();
				}				
			}
		});
		
		super.show();
		mousePositionOnShown = currentMousePosition;
	}

	/**
	 * JavaFX Tooltip's hide method.
	 */
	@Override
	public void hide()
	{
		logger.trace("Hiding tooltip request...");
		hideRequested = true;
		
		checkAndHide();			
	}
	
	/**
	 * Hides the tooltip.
	 */
	public void hideTooltip()
	{
		logger.trace("Hiding tooltip...");
		hideRequested = false;
		super.hide();
	}
	
	/**
	 * Checks if mouse is further than X from the on-show position.
	 * 
	 * @param delta How much variance to allow
	 * 
	 * @return True if mouse is now further than X from the on-show position
	 */
	private boolean mouseFurtherThan(final double delta)
	{
		if (getMousePositionOnShown() == null)
		{
			return false;
		}
		
		return (Math.abs(getMousePositionOnShown().getSceneX() - getCurrentMousePosition().getSceneX()) > delta 
				|| Math.abs(getMousePositionOnShown().getSceneY() - getCurrentMousePosition().getSceneY()) > delta);
	}
	
	/**
	 * Checks if the tooltip should be hidden, and if so, does it.
	 */
	public void checkAndHide()
	{
		if ((hideRequested() && mouseFurtherThan(5)) || (mouseFurtherThan(15)))
		{
			hideTooltip();
		}
	}
	
	/**
	 * Returns the status of the hideRequested flag.
	 * 
	 * @return True if Java requested the tooltip to be hidden
	 */
	public boolean hideRequested()
	{
		return hideRequested;
	}

	/**
	 * Gets mouse position when the tooltip was first shown.
	 * 
	 * @return The on-show mouse position
	 */
	public MouseEvent getMousePositionOnShown()
	{
		return mousePositionOnShown;
	}
	
	/**
	 * Gets the current mouse position stored.
	 * 
	 * @return The last recorded 'current position'
	 */
	public MouseEvent getCurrentMousePosition()
	{
		return currentMousePosition;
	}

	/**
	 * Sets the current mouse position.
	 * 
	 * @param currentMousePosition Current mouse position
	 */
	public void setCurrentMousePosition(final MouseEvent currentMousePosition)
	{
		this.currentMousePosition = currentMousePosition;
	}
}
