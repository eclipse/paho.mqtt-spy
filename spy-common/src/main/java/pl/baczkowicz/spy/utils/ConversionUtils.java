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
package pl.baczkowicz.spy.utils;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import pl.baczkowicz.spy.exceptions.ConversionException;
import pl.baczkowicz.spy.exceptions.CriticalException;

/** 
 * String conversion utilities.
 */
public class ConversionUtils
{
	/** Line separator for Linux and Mac. */
	public static final String LINE_SEPARATOR_LINUX = "\n";	
	
	/** Line separator for Old Mac. */ 
	public static final String LINE_SEPARATOR_MAC = "\r";    
	
	/** Line separator for Windows. */
	public static final String LINE_SEPARATOR_WIN = "\r\n"; 
	
	/** The default charset. */	
	public static final String DEFAULT_CHARSET = "UTF-8";
	
	/**
	 * Converts the given string into a HEX string.
	 * 
	 * @param data The string to convert
	 * 
	 * @return Converted string in HEX form
	 */
	public static String stringToHex(final String data)
	{
		return new String(Hex.encodeHex(ConversionUtils.stringToArray(data)));
	}
	
	/**
	 * Converts the given HEX string into a plain string.
	 * 
	 * @param data The HEX string to convert from
	 * 
	 * @return The plain string
	 * 
	 * @throws ConversionException Thrown if the given string is not a valid HEX string
	 */
	public static String hexToString(final String data) throws ConversionException
	{
		try
		{
			return ConversionUtils.arrayToString(Hex.decodeHex(data.toCharArray()));
		}
		catch (DecoderException e)
		{
			throw new ConversionException("Cannot convert given hex text into plain text", e);
		}
	}
	
	/**
	 * Converts the given HEX string into a plain string.
	 * 
	 * @param data The HEX string to convert from
	 * 
	 * @return The plain string
	 * 
	 * @throws ConversionException Thrown if the given string is not a valid HEX string
	 */
	public static byte[] hexToArray(final String data) throws ConversionException
	{
		try
		{
			return Hex.decodeHex(data.toCharArray());
		}
		catch (DecoderException e)
		{
			throw new ConversionException("Cannot convert given hex text into plain text", e);
		}
	}
	

	public static String arrayToHex(byte[] plainArray)
	{
		return Hex.encodeHexString(plainArray);
	}
	
	public static String arrayToString(final byte[] data)
	{
		try
		{
			return new String(data, ConversionUtils.DEFAULT_CHARSET);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new CriticalException("Cannot use " + ConversionUtils.DEFAULT_CHARSET, e);
		}
	}
	
	public static byte[] stringToArray(final String data)
	{
		try
		{
			return data.getBytes(ConversionUtils.DEFAULT_CHARSET);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new CriticalException("Cannot use " + ConversionUtils.DEFAULT_CHARSET, e);
		}
	}
	
	/**
	 * Converts the given HEX string into a plain string.
	 * 
	 * @param data The HEX string to convert from
	 * 
	 * @return The plain string or [invalid hex] if invalid HEX string detected
	 */
	public static String hexToStringNoException(final String data)
	{
		try
		{
			return ConversionUtils.arrayToString(Hex.decodeHex(data.toCharArray()));
		}
		catch (DecoderException e)
		{
			return "[invalid hex]";
		}
	}
	
	// ============= BASE 64 ==========================

	/**
	 * Converts Base64 string to a plain string.
	 * 
	 * @param data The Base64 string to decode
	 * 
	 * @return Decoded string
	 */
	public static String base64ToString(final String data)
	{
		return ConversionUtils.arrayToString(Base64.decodeBase64(data));
	}
	
	public static byte[] base64ToArray(String text)
	{
		return Base64.decodeBase64(text);
	}
	
	/**
	 * Converts plain string into Base64 string.
	 *  
	 * @param data The string to encode to Base64
	 * 
	 * @return Encoded string (Base64)
	 */
	public static String stringToBase64(final String data)
	{
		return Base64.encodeBase64String(ConversionUtils.stringToArray(data));
	}

	public static String arrayToBase64(byte[] payload)
	{
		return Base64.encodeBase64String(payload);
	}
}
