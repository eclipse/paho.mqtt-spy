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
package pl.baczkowicz.spy.ui.search;

import pl.baczkowicz.spy.messages.FormattedMessage;
import pl.baczkowicz.spy.storage.MessageList;

public interface MessageFilter<T extends FormattedMessage>
{
	/**
	 * Checks if the given message should be filtered out.
	 * 
	 * @param message The message to be checked
	 * @param store The message store (could be modified)
	 * @param updateUi Whether to update the UI
	 * 
	 * @return True if to filter the message out.
	 */
	boolean filter(final T message, final MessageList<T> messageList, final boolean updateUi);
	
	void reset();
	
	boolean isActive();
}
