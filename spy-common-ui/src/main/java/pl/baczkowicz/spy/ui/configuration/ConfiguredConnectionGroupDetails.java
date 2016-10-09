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
package pl.baczkowicz.spy.ui.configuration;

import java.util.ArrayList;
import java.util.List;

import pl.baczkowicz.spy.common.generated.ConnectionGroup;
import pl.baczkowicz.spy.common.generated.ConnectionGroupReference;
import pl.baczkowicz.spy.common.generated.ConnectionReference;
import pl.baczkowicz.spy.ui.properties.ModifiableConnection;

public class ConfiguredConnectionGroupDetails extends ConnectionGroup implements ModifiableConnection
{
	private static final long serialVersionUID = 7400105091442371397L;

	private boolean modified;
	
	private boolean newGroup;
	
	private ConfiguredConnectionGroupDetails lastSavedValues;
	
	private ConnectionGroupReference group;

	private boolean groupingModified;

    /**
     * Initialising value constructor
     */
    public ConfiguredConnectionGroupDetails(final String id, final String name, 
    		final ConnectionGroupReference group,
    		final List<ConnectionGroupReference> subgroups, final List<ConnectionReference> connections) 
    {
        this.id = id;
        this.name = name;
        this.subgroups = subgroups;
        this.connections = connections;
        this.group = group;
    }
	
	public ConfiguredConnectionGroupDetails(final ConnectionGroup group, final boolean newConnection)
	{
		this.modified = newConnection;
		this.newGroup = newConnection;
		
		final ConfiguredConnectionGroupDetails groupDetails = new ConfiguredConnectionGroupDetails(group.getID(), group.getName(), 
				null, group.getSubgroups(), group.getConnections());
		
		setGroupDetails(groupDetails);
		setLastSavedValues(groupDetails);
	}
	
	private void setGroupDetails(final ConfiguredConnectionGroupDetails groupDetails)
	{
		// Take a copy and null it, so that copyTo can work...
		final ConnectionGroup group = groupDetails.getGroup() != null ? (ConnectionGroup) groupDetails.getGroup().getReference() : null;
		groupDetails.setGroup(null);
		
		final List<ConnectionReference> connections = new ArrayList<>(groupDetails.getConnections());
		groupDetails.getConnections().clear();
		
		final List<ConnectionGroupReference> subgroups = new ArrayList<>(groupDetails.getSubgroups());
		groupDetails.getSubgroups().clear();
				
		if (groupDetails != null)
		{
			groupDetails.copyTo(this);
		}
		
		// Restore the group value
		groupDetails.setGroup(new ConnectionGroupReference(group));
		setGroup(new ConnectionGroupReference(group));
		
		groupDetails.getConnections().addAll(connections);
		getConnections().addAll(connections);
		
		groupDetails.getSubgroups().addAll(subgroups);
		getSubgroups().addAll(subgroups);
	}
	
	public String getFullName()
	{
		String fullName = getName();
		
		ConfiguredConnectionGroupDetails parentGroup = ConfiguredConnectionGroupDetails.getGroup(getGroup());
		
		// This both the parent group and its parent are not null (so ignore the top level group as well)
		while (parentGroup != null && ConfiguredConnectionGroupDetails.getGroup(parentGroup.getGroup()) != null)
		{
			fullName = parentGroup.getName() + " / " + fullName;
			
			parentGroup = ConfiguredConnectionGroupDetails.getGroup(parentGroup.getGroup());
		}
		
		return fullName;
	}
	
	public static ConfiguredConnectionGroupDetails getGroup(final ConnectionGroupReference group)
	{
		if (group == null)
		{
			return null;
		}
		
		return (ConfiguredConnectionGroupDetails) group.getReference();
	}
	
	/**
	 * @return the modified
	 */
	public boolean isModified()
	{
		return modified;
	}

	/**
	 * @param modified the modified to set
	 */
	public void setModified(boolean modified)
	{
		this.modified = modified;
	}

	/**
	 * @return the lastSavedValues
	 */
	public ConnectionGroup getLastSavedValues()
	{
		return lastSavedValues;
	}

	/**
	 * @param lastSavedValues the lastSavedValues to set
	 */
	private void setLastSavedValues(final ConfiguredConnectionGroupDetails lastSavedValues)
	{
		this.lastSavedValues = lastSavedValues;
	}
	
	public void undo()
	{
		setID(lastSavedValues.getID());
		setName(lastSavedValues.getName());			
		modified = newGroup;
	}
	
	/**
	 * This method undoes all changes, including those about grouping.
	 */
	public void undoAll()
	{
		undo();
		
		setGroup(lastSavedValues.getGroup());
		getConnections().clear();
		getConnections().addAll(lastSavedValues.getConnections());
		getSubgroups().clear();
		getSubgroups().addAll(lastSavedValues.getSubgroups());
		groupingModified = false;
	}

	public void apply()
	{
		final ConfiguredConnectionGroupDetails valuesToSave = new ConfiguredConnectionGroupDetails(getID(), getName(), getGroup(), 
				new ArrayList<>(getSubgroups()), new ArrayList<>(getConnections()));
		
		setLastSavedValues(valuesToSave);
		modified = false;
		newGroup = false;
		groupingModified = false;
	}

	public boolean isNew()
	{
		return newGroup;
	}
	
	public void removeFromGroup()
	{
		removeFromGroup(this, (ConnectionGroup) getGroup().getReference());
	}
	
	public static void removeFromGroup(final ConnectionGroup groupToRemove, final ConnectionGroup groupToRemoveFrom)
	{
		ConnectionGroupReference refToDelete = null;
		final List<ConnectionGroupReference> subgroups = groupToRemoveFrom.getSubgroups(); 
		for (ConnectionGroupReference subgroupRef : subgroups)
		{
			if (subgroupRef.getReference().equals(groupToRemove))
			{
				refToDelete = subgroupRef;
				break;
			}
		}
		subgroups.remove(refToDelete);	
	}

	public void setGroupingModified(boolean modified)
	{
		this.groupingModified = modified;		
	}
	
	public boolean isGroupingModified()
	{
		return groupingModified;
	}

	/**
	 * @return the group
	 */
	public ConnectionGroupReference getGroup()
	{
		return group;
	}

	/**
	 * @param group the group to set
	 */
	public void setGroup(final ConnectionGroupReference group)
	{
		this.group = group;
	}
	
	public static void removeFromGroup(final Object connectionToRemove, final ConnectionGroup groupToRemoveFrom)
	{
		ConnectionReference refToDelete = null;
		final List<ConnectionReference> connections = groupToRemoveFrom.getConnections(); 
		for (ConnectionReference connectionRef : connections)
		{
			if (connectionRef.getReference().equals(connectionToRemove))
			{
				refToDelete = connectionRef;
				break;
			}
		}
		connections.remove(refToDelete);
	}

	@Override
	public void setDeleted(boolean value)
	{
		// Not used		
	}

	@Override
	public boolean isBeingCreated()
	{
		return false;
	}
}
