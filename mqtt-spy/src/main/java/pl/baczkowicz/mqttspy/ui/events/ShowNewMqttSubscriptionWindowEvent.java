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

package pl.baczkowicz.mqttspy.ui.events;

import pl.baczkowicz.mqttspy.ui.controllers.MqttConnectionController;
import pl.baczkowicz.spy.ui.panes.PaneVisibilityStatus;

public class ShowNewMqttSubscriptionWindowEvent
{
	private final MqttConnectionController controller;
	
	private final PaneVisibilityStatus newStatus;
	
	private final PaneVisibilityStatus previousStatus;

	public ShowNewMqttSubscriptionWindowEvent(final MqttConnectionController controller, 
			final PaneVisibilityStatus newStatus, final PaneVisibilityStatus previousStatus)
	{
		this.controller = controller;
		this.newStatus = newStatus;
		this.previousStatus = previousStatus;
	}

	/**
	 * @return the changedSubscription
	 */
	public MqttConnectionController getConnectionController()
	{
		return controller;
	}

	/**
	 * @return the status
	 */
	public PaneVisibilityStatus getNewStatus()
	{
		return newStatus;
	}
	
	/**
	 * @return the status
	 */
	public PaneVisibilityStatus getPreviousStatus()
	{
		return previousStatus;
	}
}
