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
package pl.baczkowicz.spy.ui;

import java.util.Collection;

import javafx.scene.control.Tab;
import pl.baczkowicz.spy.exceptions.ConfigurationException;
import pl.baczkowicz.spy.ui.connections.IUiConnection;
import pl.baczkowicz.spy.ui.controllers.IConnectionController;
import pl.baczkowicz.spy.ui.properties.ModifiableConnection;

public interface IConnectionViewManager
{
	IConnectionController getControllerForTab(final Tab selectedTab);

	Collection<? extends IConnectionController> getConnectionControllers();
	
	void disconnectAll();
	
	void disconnectAndCloseAll();

	void autoOpenConnections();

	void openConnection(ModifiableConnection connectionDetails) throws ConfigurationException;

	Collection<IUiConnection> getConnections();
}
