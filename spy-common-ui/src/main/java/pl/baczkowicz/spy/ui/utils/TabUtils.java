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
package pl.baczkowicz.spy.ui.utils;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import pl.baczkowicz.spy.ui.panes.PaneVisibilityStatus;
import pl.baczkowicz.spy.ui.panes.TabController;

import com.sun.javafx.scene.control.behavior.TabPaneBehavior;
import com.sun.javafx.scene.control.skin.TabPaneSkin;

/**
 * Tab pane utilities.
 */
public class TabUtils
{
	/**
	 * Requests the given tab to be closed.
	 * 
	 * @param tab The tab to be closed
	 */
	public static void requestClose(final Tab tab)
	{
		TabPaneBehavior behavior = getBehavior(tab);
		if (behavior.canCloseTab(tab))
		{
			behavior.closeTab(tab);
		}
	}

	/**
	 * Gets the behavior object for the given tab.
	 * 
	 * @param tab The tab for which to get the behaviour
	 *  
	 * @return TabPaneBehavior
	 */
	private static TabPaneBehavior getBehavior(final Tab tab)
	{
		return ((TabPaneSkin) tab.getTabPane().getSkin()).getBehavior();
	}
	

	private static Tab copyTab(final Tab tabToCopy, final ContextMenu contextMenu, 
			final TabController controller, final TabPane tabPane)
	{
		final Tab newTab = new Tab();
		newTab.setContextMenu(contextMenu);
		newTab.setText(tabToCopy.getText());
		newTab.setGraphic(tabToCopy.getGraphic());
		newTab.setTooltip(tabToCopy.getTooltip());
		newTab.setContent(tabToCopy.getContent());
		newTab.setStyle(tabToCopy.getStyle());			
		newTab.getStyleClass().addAll(tabToCopy.getStyleClass());
		
		controller.setTab(newTab);
		tabPane.getTabs().add(newTab);			
		controller.refreshStatus();
		
		return newTab;
	}
	
	/**
	 * Creates an event for detaching the given controller's tab.
	 * 
	 * @param menuItem The menu item for which this event is created
	 * @param controller The controller behind the tab
	 * @param windowTitle The window title for the detached tab
	 * @param margin The window margin for content
	 * 
	 * @return Created event
	 */
	public static EventHandler<ActionEvent> createTabDetachEvent(
			final MenuItem menuItem, final TabController controller, 
			final String windowTitle, final double margin)
	{
		final EventHandler<ActionEvent> detachEvent = new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				final Tab tab = controller.getTab();
				final boolean closableWhenAttached = tab.isClosable();
				
				// Hide, so it is not duplicated				
				tab.getContextMenu().hide();
				final ContextMenu existingContextMenu = tab.getContextMenu();
				tab.setContextMenu(null);				
				
				final TabPane tabPane = new TabPane();
								
				// Remove from old parent	
				controller.getTabStatus().getParentWhenAttached().getTabs().remove(tab);
				
				// Add to new parent
				copyTab(tab, existingContextMenu, controller, tabPane);
				controller.getTabStatus().setVisibility(PaneVisibilityStatus.DETACHED);
				controller.getTab().setClosable(false);				
				menuItem.setDisable(true);
				
				final Stage stage = DialogFactory.createWindowWithPane(
						tabPane, controller.getTabStatus().getParentWhenAttached().getScene(), 
						windowTitle, margin);
				
				controller.getTab().setOnCloseRequest(new EventHandler<Event>()
				{					
					@Override
					public void handle(Event event)
					{
						stage.close();						
					}
				});
				
				stage.setOnCloseRequest(new EventHandler<WindowEvent>()
				{				
					@Override
					public void handle(WindowEvent event)
					{
						if (tabPane.getTabs().size() > 0)
						{							
							final Tab tab = controller.getTab();
							final ContextMenu existingContextMenu = tab.getContextMenu();
							tab.setContextMenu(null);
							
							// Remove from new parent
							tabPane.getTabs().remove(tab);
							
							// Add to old parent
							copyTab(tab, existingContextMenu, controller, controller.getTabStatus().getParentWhenAttached());						
							controller.getTabStatus().setVisibility(PaneVisibilityStatus.ATTACHED);						
							controller.getTab().setClosable(closableWhenAttached);						
							menuItem.setDisable(false);
						}
					}
				});						
				
		        stage.show();		        
			};
		};
		
		return detachEvent;
	}
}
