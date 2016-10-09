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
package pl.baczkowicz.spy.ui.properties;

import pl.baczkowicz.spy.common.generated.ConnectionGroupReference;

public interface ModifiableConnection
{
	boolean isModified();
	
	String getName();
	
	String getID();

	void apply();
	
	void setName(final String newName);

	void setGroupingModified(boolean modified);

	void setGroup(ConnectionGroupReference connectionGroupReference);

	boolean isNew();

	void removeFromGroup();

	void undoAll();

	void setDeleted(boolean value);

	boolean isGroupingModified();
	
	ConnectionGroupReference getGroup();

	boolean isBeingCreated();
}
