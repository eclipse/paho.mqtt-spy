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

import java.util.ArrayList;
import java.util.List;

import pl.baczkowicz.spy.ui.utils.UiUtils;

public class ConnectionTreeItemProperties
{
	private ConnectionTreeItemProperties parent;
	
	private boolean grouping;

	private String name;
	
	private ModifiableConnection connectionGroup; 
	
	private ModifiableConnection connection;

	private List<ConnectionTreeItemProperties> children = new ArrayList<>();

	private int id;
	
	public ConnectionTreeItemProperties(final int id)
	{
		this.id = id;
	}
	
	/**
	 * @return the grouping
	 */
	public boolean isGroup()
	{
		return grouping;
	}

	/**
	 * @param grouping the grouping to set
	 */
	public void setGrouping(boolean grouping)
	{
		this.grouping = grouping;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		if ((!grouping && connection.isModified()) || (grouping && connectionGroup.isModified()))
		{
			return UiUtils.MODIFIED_ITEM + name;
		}
		
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the connectionGroup
	 */
	public ModifiableConnection getGroup()
	{
		return connectionGroup;
	}

	/**
	 * @param connectionGroup the connectionGroup to set
	 */
	public void setGroup(ModifiableConnection connectionGroup)
	{
		this.connectionGroup = connectionGroup;
		grouping = true;
		name = connectionGroup.getName();
	}

	/**
	 * @return the connection
	 */
	public ModifiableConnection getConnection()
	{
		return connection;
	}

	/**
	 * @param connection the connection to set
	 */
	public void setConnection(ModifiableConnection connection)
	{
		this.connection = connection;
		grouping = false;
		name = connection.getName();
	}
	
	public String toString()
	{
		return getName();
	}

	/**
	 * @return the parent
	 */
	public ConnectionTreeItemProperties getParent()
	{
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(ConnectionTreeItemProperties parent)
	{
		this.parent = parent;
	}

	public void addChild(ConnectionTreeItemProperties group)
	{
		children.add(group);
		
	}

	public List<ConnectionTreeItemProperties> getChildren()
	{
		return children;
	}

	public String getId()
	{
		return String.valueOf(id);
	}
}
