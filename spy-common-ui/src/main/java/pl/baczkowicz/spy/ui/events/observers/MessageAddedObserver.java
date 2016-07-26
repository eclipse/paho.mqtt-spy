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
package pl.baczkowicz.spy.ui.events.observers;

import java.util.List;

import pl.baczkowicz.spy.messages.FormattedMessage;
import pl.baczkowicz.spy.ui.events.queuable.ui.BrowseReceivedMessageEvent;

public interface MessageAddedObserver<T extends FormattedMessage>
{
	void onMessageAdded(final List<BrowseReceivedMessageEvent<T>> message);
}
