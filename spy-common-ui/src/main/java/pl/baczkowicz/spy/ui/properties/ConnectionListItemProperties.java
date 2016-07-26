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

public class ConnectionListItemProperties
{
	private final String name; 
	
	private final String protocol;
	
	private final boolean tlsEnabled;
	
	private final boolean userAuthenticationEnabled;
	
	private final String details;
	
	public ConnectionListItemProperties(final String name, final String protocol, final String details, final boolean tls, final boolean userAuth)	
	{
		this.name = name;
		this.protocol = protocol;
		this.details = details;
		this.tlsEnabled = tls;
		this.userAuthenticationEnabled = userAuth;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return the protocol
	 */
	public String getProtocol()
	{
		return protocol;
	}

	/**
	 * @return the tlsEnabled
	 */
	public boolean isTlsEnabled()
	{
		return tlsEnabled;
	}

	/**
	 * @return the userAuthenticationEnabled
	 */
	public boolean isUserAuthenticationEnabled()
	{
		return userAuthenticationEnabled;
	}

	/**
	 * @return the details
	 */
	public String getDetails()
	{
		return details;
	}
}
