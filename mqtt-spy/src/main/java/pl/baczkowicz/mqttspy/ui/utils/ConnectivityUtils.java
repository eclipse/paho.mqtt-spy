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
package pl.baczkowicz.mqttspy.ui.utils;

import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;


/**
 * Connectivity related utils for the UI.
 */
public class ConnectivityUtils
{	
	/**
	 * Validates given connection details.
	 * 
	 * @param connectionDetails Connection details to check
	 * @param finalCheck Whether to do a final check (before connecting)
	 * 
	 * @return Null if all OK, otherwise a string with a failure reason
	 */
	public static String validateConnectionDetails(final UserInterfaceMqttConnectionDetails connectionDetails, final boolean finalCheck)
	{
		String returnValue = pl.baczkowicz.mqttspy.utils.ConnectionUtils.validateConnectionDetails(connectionDetails);
		
		if (connectionDetails.getUserAuthentication() != null && connectionDetails.getUserCredentials() != null)
		{
			if ((finalCheck || !connectionDetails.getUserAuthentication().isAskForUsername()) && (connectionDetails.getUserCredentials().getUsername() == null
					|| connectionDetails.getUserCredentials().getUsername().trim().isEmpty()))
			{
				returnValue = "With user authentication enabled, user name cannot be empty";
			}
		}
		
		return returnValue;
	}	
}
