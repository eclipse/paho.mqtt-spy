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
package pl.baczkowicz.spy.ui.connections;

import java.util.Collection;
import java.util.List;

import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.layout.AnchorPane;
import pl.baczkowicz.spy.ui.configuration.ConfiguredConnectionGroupDetails;
import pl.baczkowicz.spy.ui.panes.SpyPerspective;
import pl.baczkowicz.spy.ui.properties.ConnectionListItemProperties;
import pl.baczkowicz.spy.ui.properties.ModifiableConnection;

public interface IConnectionFactory
{
	static final String MQTT = "MQTT";
	
	void populateProtocolCell(final TableCell<ConnectionListItemProperties, String> cell, final String item);
	
	// String getIconNameForProtocol(final String protocol);
	
	ConnectionListItemProperties createConnectionListItemProperties(final ModifiableConnection connection);
	
	void findConnections(final ConfiguredConnectionGroupDetails parentGroup, final List<ModifiableConnection> connections);
	
	Collection<MenuItem> createMenuItems();
	
	ModifiableConnection newConnection(final String protocol);
	
	ModifiableConnection duplicateConnection(final ModifiableConnection copyFrom);
	
	Collection<AnchorPane> loadControllers(final Object parent);
	
	void editConnection(final ModifiableConnection connection);
	
	void openConnection(final ModifiableConnection connection);
	
	void setRecordModifications(final boolean record);

	void setPerspective(SpyPerspective perspective);

	void setEmptyConnectionListMode(boolean empty);

	// void readAndOpenConnection(final String protocol) throws ConfigurationException;

	void setVisible(boolean groupSelected);

	Button createConnectionButton(final ModifiableConnection connection);	
}
