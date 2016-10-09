/***********************************************************************************
 * 
 * Copyright (c) 2016 Kamil Baczkowicz
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
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.ui.configuration.IConfigurationManager;
import pl.baczkowicz.spy.ui.controllers.ControlPanelItemController;
import pl.baczkowicz.spy.ui.controlpanel.ControlPanelStatsUpdater;
import pl.baczkowicz.spy.ui.controlpanel.IControlPanelItem;
import pl.baczkowicz.spy.ui.controls.GettingInvolvedTooltip;

public class MqttStatsControlPanelItem implements IControlPanelItem
{
	private IKBus eventBus;
	
	private IConfigurationManager configurationManager;
	
	public MqttStatsControlPanelItem(final IConfigurationManager configurationManager, final IKBus eventBus)
	{
		this.eventBus = eventBus;
		this.configurationManager = configurationManager;
	}

	@Override
	public void update(ControlPanelItemController controlPanelItemController, Button button)
	{
		showStats(controlPanelItemController, button, eventBus);
	}
	
	public void showStats(final ControlPanelItemController controller, final Button button, final IKBus eventBus)
	{
		controller.refresh();
		
		final ControlPanelStatsUpdater statsUpdater = new ControlPanelStatsUpdater(controller, button, eventBus, configurationManager);
		statsUpdater.show();
		
		final String text = 
				configurationManager.getDefaultPropertyFile().getApplicationName() + " needs you! Please support the project" + System.lineSeparator()
				+ "by raising bugs, " + "helping out with testing" + System.lineSeparator()
				+ "or making a charity donation. " + System.lineSeparator()
				+ "See " + configurationManager.getDefaultPropertyFile().getApplicationWikiUrl() + "Getting-involved" + System.lineSeparator()
				+ "for more information on how to get involved." + System.lineSeparator();		
		
		final GettingInvolvedTooltip gettingInvolvedTooltip = new GettingInvolvedTooltip(
				text,
				configurationManager.getDefaultPropertyFile().getApplicationLogo());		
		
		button.setTooltip(gettingInvolvedTooltip);
		button.setOnMouseMoved(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				gettingInvolvedTooltip.setCurrentMousePosition(event);
				
				if (gettingInvolvedTooltip.isShowing())
				{
					gettingInvolvedTooltip.checkAndHide();
				}
			}
		});
	}
}
