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

package pl.baczkowicz.spy.ui.events;

import javafx.stage.Window;
import pl.baczkowicz.spy.ui.properties.ModifiableConnection;

public class ShowEditConnectionsWindowEvent
{
	private final Window parent;
	
	private final boolean createNew;

	private final ModifiableConnection connectionProperties;

	public ShowEditConnectionsWindowEvent(final Window parent, final boolean createNew, final ModifiableConnection connectionProperties)
	{
		this.parent = parent;
		this.createNew = createNew;
		this.connectionProperties = connectionProperties;
	}

	/**
	 * @return the changedSubscription
	 */
	public Window getParent()
	{
		return parent;
	}

	/**
	 * @return the createNew
	 */
	public boolean isCreateNew()
	{
		return createNew;
	}

	/**
	 * @return the connection
	 */
	public ModifiableConnection getConnectionProperties()
	{
		return connectionProperties;
	}
}
