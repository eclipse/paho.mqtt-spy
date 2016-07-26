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

import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class TextUtils
{
	/** The text object for which operations will be performed. */
	private static final Text TEXT;
	
	/** Default wrapping width. */
	private static final double DEFAULT_WRAPPING_WIDTH;
	
	/** Default line spacing. */
	private static final double DEFAULT_LINE_SPACING;
	
	/** Default text. */
	private static final String DEFAULT_TEXT;
		
	/** Static initialiser. */
	static
	{
		TEXT = new Text();
		DEFAULT_WRAPPING_WIDTH = TEXT.getWrappingWidth();
		DEFAULT_LINE_SPACING = TEXT.getLineSpacing();
		DEFAULT_TEXT = TEXT.getText();
	}

	/**
	 * Calculates text width for the given text.
	 * 
	 * @param font Font used
	 * @param text Text calculate with for
	 * 
	 * @return Expected text width
	 */
	public static double computeTextWidth(final Font font, final String text)
	{
		TEXT.setText(text);
		TEXT.setFont(font);

		TEXT.setWrappingWidth(0.0D);
		TEXT.setLineSpacing(0.0D);
		double d = Math.min(TEXT.prefWidth(-1.0D), 0.0D);
		TEXT.setWrappingWidth((int) Math.ceil(d));
		d = Math.ceil(TEXT.getLayoutBounds().getWidth());

		TEXT.setWrappingWidth(DEFAULT_WRAPPING_WIDTH);
		TEXT.setLineSpacing(DEFAULT_LINE_SPACING);
		TEXT.setText(DEFAULT_TEXT);
		
		return d;
	}
}