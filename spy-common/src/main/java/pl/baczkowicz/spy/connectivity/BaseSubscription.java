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
package pl.baczkowicz.spy.connectivity;

import pl.baczkowicz.spy.common.generated.ScriptedSubscriptionDetails;
import pl.baczkowicz.spy.scripts.Script;

public class BaseSubscription
{
	private int id;
	
	private String topic;

	private boolean subscribing;

	private boolean active;
	
	private ScriptedSubscriptionDetails details;

	private Script script;

	private boolean scriptActive;

	public BaseSubscription(final String topic)
	{
		this.topic = topic;
		this.active = false;
	}

	public String getTopic()
	{
		return topic;
	}

	public void setTopic(String topic)
	{
		this.topic = topic;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive(final boolean active)
	{
		this.active = active;
	}

	public int getId()
	{
		return id;
	}

	public void setId(final int id)
	{
		this.id = id;		
	}

	public boolean isSubscribing()
	{
		return subscribing;
	}
	
	public void setSubscribing(final boolean value)
	{
		subscribing = value;
	}

	public ScriptedSubscriptionDetails getDetails()
	{
		return details;
	}

	public void setDetails(final ScriptedSubscriptionDetails details)
	{
		this.details = details;
	}

	public void setScript(final Script script)
	{
		this.script = script;		
	}

	public void setScriptActive(final boolean scriptActive)
	{
		this.scriptActive = scriptActive;		
	}
	
	public boolean isScriptActive()
	{
		return scriptActive;
	}

	public Script getScript()
	{
		return script;
	}
}
