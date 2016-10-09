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
package pl.baczkowicz.mqttspy.kura;

import java.io.IOException;

import org.eclipse.kura.KuraInvalidMessageException;
import org.eclipse.kura.core.cloud.CloudPayloadProtoBufDecoderImpl;
import org.eclipse.kura.message.KuraPayload;
import org.json.JSONObject;

public class KuraPayloadFormatter
{
	public static String format(byte[] data)
	{
		final CloudPayloadProtoBufDecoderImpl decoder = new CloudPayloadProtoBufDecoderImpl(data);
		
		try
		{
			final KuraPayload kuraPayload = decoder.buildFromByteArray();
			return KuraPayloadFormatter.payloadToString(kuraPayload);
		}
		catch (KuraInvalidMessageException | IOException e)
		{
			return e.getLocalizedMessage();
		} 
	}

	public static String payloadToString(final KuraPayload payload)
	{
		final String body = payload.getBody() != null ? (", body: " + new JSONObject(payload.getBody()).toString()) : "";
		
		final String position = payload.getPosition() != null ? (", position: " + new JSONObject(payload.getPosition()).toString()) : "";
		
		return "{kuraPayload: {timestamp: " + payload.getTimestamp()
				+ ", metrics: " + new JSONObject(payload.metrics()).toString() 
				+ position
				+ body
				+ "}}";
	}
}
