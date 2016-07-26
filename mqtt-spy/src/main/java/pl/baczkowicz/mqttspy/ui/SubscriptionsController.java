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
package pl.baczkowicz.mqttspy.ui;

import javafx.scene.control.MenuButton;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import pl.baczkowicz.spy.ui.panes.PaneVisibilityStatus;
import pl.baczkowicz.spy.ui.panes.TitledPaneController;

public class SubscriptionsController implements TitledPaneController
{
	private TitledPane pane;
	private AnchorPane paneTitle;
	private MenuButton settingsButton;
	private ConnectionController connectionController;
	
	@Override
	public TitledPane getTitledPane()
	{
		return pane;
	}

	@Override
	public void setTitledPane(TitledPane pane)
	{
		this.pane = pane;
	}

	public void init()
	{
		paneTitle = new AnchorPane();
		settingsButton = NewPublicationController.createTitleButtons(pane, paneTitle, connectionController);		
	}
	

	public void setConnectionController(final ConnectionController connectionController)
	{
		this.connectionController = connectionController;
	}

	
	@Override
	public void updatePane(PaneVisibilityStatus status)
	{
		if (PaneVisibilityStatus.ATTACHED.equals(status))
		{
			settingsButton.setVisible(true);
		}		
		else
		{
			settingsButton.setVisible(false);
		}
	}
}
