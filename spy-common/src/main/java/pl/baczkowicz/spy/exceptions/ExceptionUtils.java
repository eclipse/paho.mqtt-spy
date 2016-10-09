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
package pl.baczkowicz.spy.exceptions;

public class ExceptionUtils
{
	public static String getInfo(final Throwable throwable)
	{
		return throwable.toString();
	}
	
	public static String getCauses(final Throwable throwable)	
	{
		String causes = throwable.toString();
		
		Throwable cause = throwable.getCause();
		causes = causes + " / " + cause.toString();
		
		while (cause.getCause() != null)
		{
			cause = cause.getCause();
			causes = causes + " / " + cause.toString();
		}
		
		return causes;
	}
	
	public static String getRootCauseMessage(final Throwable throwable)
	{
		Throwable cause = throwable.getCause();
		
		while (cause.getCause() != null)
		{
			cause = cause.getCause();
		}
		
		return cause.getMessage();
	}
}
