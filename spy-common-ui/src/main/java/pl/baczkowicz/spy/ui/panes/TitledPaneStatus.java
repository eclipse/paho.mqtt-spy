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
package pl.baczkowicz.spy.ui.panes;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;

public class TitledPaneStatus extends PaneStatus
{
	private boolean expanded = false;
	
	private boolean lastExpanded = false;

	private int displayIndex;

	private Menu menu;

	private SplitPane parentWhenAttached;
	
	private Stage parentWhenDetached;

	private TitledPaneController controller;
	
	public TitledPaneStatus(final int displayIndex)
	{
		super();
		this.displayIndex = displayIndex;
	}
	
	/**
	 * Gets the expanded flag.
	 * 
	 * @return the expanded
	 */
	public boolean isExpanded()
	{
		return expanded;
	}

	/**
	 * Sets the expanded flag.
	 * 
	 * @param expanded the expanded to set
	 */
	public void setExpanded(final boolean expanded)
	{
		this.expanded = expanded;
	}

	/**
	 * Gets the display index value.
	 * 
	 * @return the displayIndex
	 */
	public int getDisplayIndex()
	{
		return displayIndex;
	}

	/**
	 * Returns the display index value.
	 * 
	 * @param displayIndex the displayIndex to set
	 */
	public void setDisplayIndex(final int displayIndex)
	{
		this.displayIndex = displayIndex;
	}

	public void setContentMenu(final Menu menu)
	{
		this.menu = menu;
	}
	
	public void updateMenu()
	{
		if (getVisibility().equals(PaneVisibilityStatus.DETACHED))
		{
			((CheckMenuItem) menu.getItems().get(0)).setSelected(false);
			((CheckMenuItem) menu.getItems().get(1)).setSelected(false);
			((CheckMenuItem) menu.getItems().get(2)).setSelected(true);
		}
		else if (getVisibility().equals(PaneVisibilityStatus.ATTACHED))
		{
			((CheckMenuItem) menu.getItems().get(0)).setSelected(false);
			((CheckMenuItem) menu.getItems().get(1)).setSelected(true);
			((CheckMenuItem) menu.getItems().get(2)).setSelected(false);
		}
		else
		{
			((CheckMenuItem) menu.getItems().get(0)).setSelected(true);
			((CheckMenuItem) menu.getItems().get(1)).setSelected(false);
			((CheckMenuItem) menu.getItems().get(2)).setSelected(false);
		}			
	}
	
	/**
	 * Gets the parent object.
	 * 
	 * @return the parent
	 */
	public SplitPane getParentWhenAttached()
	{
		return parentWhenAttached;
	}

	/**
	 * Sets the parent object.
	 * 
	 * @param parent the parent to set
	 */
	public void setParent(SplitPane parent)
	{
		this.parentWhenAttached = parent;
	}

	/**
	 * Gets the parent when detached.
	 * 
	 * @return the parentWhenDetached
	 */
	public Stage getParentWhenDetached()
	{
		return parentWhenDetached;
	}

	/**
	 * Sets the parent when detached.
	 * 
	 * @param parentWhenDetached the parentWhenDetached to set
	 */
	public void setParentWhenDetached(Stage parentWhenDetached)
	{
		this.parentWhenDetached = parentWhenDetached;
	}

	/**
	 * Gets last expanded.
	 * 
	 * @return the lastExpanded
	 */
	public boolean isLastExpanded()
	{
		return lastExpanded;
	}

	/**
	 * Sets last expanded.
	 * 
	 * @param lastExpanded the lastExpanded to set
	 */
	public void setLastExpanded(boolean lastExpanded)
	{
		this.lastExpanded = lastExpanded;
	}

	/**
	 * @return the controller
	 */
	public TitledPaneController getController()
	{
		return controller;
	}

	/**
	 * @param controller the controller to set
	 */
	public void setController(TitledPaneController controller)
	{
		this.controller = controller;
	}
}
