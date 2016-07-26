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
package pl.baczkowicz.spy.ui.keyboard;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

/**
 * Keyboard-related utilities.
 */
public class KeyboardUtils
{
	/** Non-numeric key consumer - consumes any non-number characters. */
	public static final EventHandler<KeyEvent> nonNumericKeyConsumer = new EventHandler<KeyEvent>()
	{
		public void handle(KeyEvent t)
		{
			char ar[] = t.getCharacter().toCharArray();
			char ch = ar[t.getCharacter().toCharArray().length - 1];
			if (!(ch >= '0' && ch <= '9'))
			{
				t.consume();
			}
		}
	};
}
