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
package pl.baczkowicz.spy.ui.utils;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;

/**
 * General purpose utilities.
 */
public class UiUtils
{
	public final static String MODIFIED_ITEM = "* ";
	
	public static void copyToClipboard(final String value)
	{
		final ClipboardContent content = new ClipboardContent();
		content.putString(value);
		Clipboard.getSystemClipboard().setContent(content);
	}
	
	public static HBox createSecurityIcons(final boolean tlsEnabled, final boolean userAuthEnabled, final boolean showBothStates)
	{
		final HBox icons = new HBox();
		
		createTlsIcon(icons, tlsEnabled, showBothStates);
		createAuthIcon(icons, userAuthEnabled, showBothStates);
		
		return icons;
	}
	
	public static void createTlsIcon(final HBox icons, final boolean tlsEnabled, final boolean showBothStates)
	{
		if (tlsEnabled)
		{
			icons.getChildren().add(ImageUtils.createIcon("lock-yes", 16));
		}
		else if (!tlsEnabled && showBothStates)
		{
			icons.getChildren().add(ImageUtils.createIcon("lock-no", 16));
		}		
	}
	
	public static void createAuthIcon(final HBox icons, final boolean userAuthEnabled, final boolean showBothStates)
	{
		if (userAuthEnabled)
		{
			icons.getChildren().add(ImageUtils.createIcon("auth-yes", 19));
		}
		else if (!userAuthEnabled && showBothStates)
		{
			icons.getChildren().add(ImageUtils.createIcon("auth-none", 19));
		}
	}
}
