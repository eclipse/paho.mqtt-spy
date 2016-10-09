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
package pl.baczkowicz.spy.ui.controls;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.ui.utils.ImageUtils;

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
	 * 
	 * @param textValue The text to display 
	 * @param logo The logo to display
	 */
	public GettingInvolvedTooltip(final String textValue, final String logo)
	{
		final HBox tooltipContent = new HBox();		
		final Label text = new Label(textValue);
		text.getStyleClass().add("small-font");
		tooltipContent.getChildren().addAll(ImageUtils.createIcon(logo, 70), text);
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
