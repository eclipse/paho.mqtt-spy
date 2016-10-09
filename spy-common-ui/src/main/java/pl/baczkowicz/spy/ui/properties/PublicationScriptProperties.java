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
package pl.baczkowicz.spy.ui.properties;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import pl.baczkowicz.spy.scripts.Script;
import pl.baczkowicz.spy.scripts.ScriptChangeObserver;
import pl.baczkowicz.spy.scripts.ScriptRunningState;
import pl.baczkowicz.spy.ui.scripts.ScriptTypeEnum;
import pl.baczkowicz.spy.utils.TimeUtils;

/**
 * This represents a single row displayed in the scripts table.
 */
public class PublicationScriptProperties implements ScriptChangeObserver
{
	private SimpleObjectProperty<ScriptRunningState> statusProperty;
	
	private SimpleObjectProperty<ScriptTypeEnum> typeProperty;

	private SimpleStringProperty lastPublishedProperty;

	private SimpleLongProperty countProperty;
	
	private SimpleBooleanProperty repeatProperty;
	
	private Script script;
	
	public PublicationScriptProperties(final Script script)
	{
		this.script = script;
		
		this.statusProperty = new SimpleObjectProperty<ScriptRunningState>(ScriptRunningState.NOT_STARTED);		
		this.typeProperty = new SimpleObjectProperty<ScriptTypeEnum>(ScriptTypeEnum.PUBLICATION);
		this.lastPublishedProperty = new SimpleStringProperty("");
		this.countProperty = new SimpleLongProperty(0);
		this.repeatProperty = new SimpleBooleanProperty(false);
		
		this.repeatProperty.set(script.getScriptDetails() != null && Boolean.TRUE.equals(script.getScriptDetails().isRepeat()));
		update();
	}
	
	public void update()
	{
		this.countProperty.set(script.getMessagesPublished());
		this.statusProperty().set(script.getStatus());
		
		if (script.getLastPublishedDate() != null)
		{
			this.lastPublishedProperty.set(TimeUtils.DATE_WITH_SECONDS_SDF.format(script.getLastPublishedDate()));
		}
	}
	
	public SimpleObjectProperty<ScriptRunningState> statusProperty()
	{
		return this.statusProperty;
	}
	
	public SimpleObjectProperty<ScriptTypeEnum> typeProperty()
	{
		return this.typeProperty;
	}
	
	public SimpleStringProperty lastPublishedProperty()
	{
		return this.lastPublishedProperty;
	}
	
	public SimpleLongProperty countProperty()
	{
		return this.countProperty;
	}
	
	public SimpleBooleanProperty repeatProperty()
	{
		return this.repeatProperty;
	}
	
	/**
	 * Gets the script name.
	 * 
	 * @return Name of the script
	 */
	public String getName()
	{
		return script.getNameWithSubdirectory();
	}
	
	/**
	 * Gets the repeat flag.
	 * 
	 * @return True is the script is set to repeat
	 */
	public boolean isRepeat()
	{
		return this.repeatProperty.getValue();
	}
	
	/**
	 * Sets the repeat value.
	 * 
	 * @param value The new repeat value.
	 */
    public void setRepeat(final boolean value) 
    {
        this.script.getScriptDetails().setRepeat(value);
        this.repeatProperty.set(script.getScriptDetails().isRepeat());
    }
	
	public Script getScript()
	{
		return script;
	}

	@Override
	public void onChange()
	{
		update();		
	}
}
