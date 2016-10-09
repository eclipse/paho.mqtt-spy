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

package pl.baczkowicz.spy.ui.threading;

import java.util.concurrent.Executor;

import javafx.application.Platform;

/**
 * Simple JavaFX Platform.runLater executor if not on FX thread, otherwise asynchronous.
 */
public class SimplePlatformRunLaterOrAsynchronousExecutor implements Executor
{
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final Runnable command)
    {
		if (Platform.isFxApplicationThread())
		{
			new Thread(command).start();
		}
		else
		{
			Platform.runLater(command);
		}
    }
}
