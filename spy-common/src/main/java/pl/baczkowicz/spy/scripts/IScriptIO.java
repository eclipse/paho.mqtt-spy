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
package pl.baczkowicz.spy.scripts;

import java.io.IOException;

/**
 * Interface between a script and the rest of the application, primarily used for publishing messages.
 */
public interface IScriptIO
{
	/**
	 * Informs the java side the script is still alive.
	 */
	void touch();

	/**
	 * Sets a custom thread timeout for the script.
	 *  
	 * @param customTimeout Custom timeout in milliseconds (normally expected to be higher than the default)
	 */
	void setScriptTimeout(long customTimeout);

	/**
	 * Instantiates a class with the given package name and class name, e.g. by
	 * passing `com.test.MyClass`, the following object `com_test_MyClass`
	 * becomes available.
	 * 
	 * @param className The package name and class name (e.g. com.test.MyClass)
	 * 
	 * @return True if successfully initialised
	 */
	@Deprecated
	boolean instantiate(String className);

	/**
	 * Executes a system command.
	 * 
	 * @param command The command to execute
	 * 
	 * @return Result of the command
	 * 
	 * @throws IOException Thrown when a problem is encountered
	 * @throws InterruptedException Thrown when a the thread is interrupted
	 */
	String execute(String command) throws IOException, InterruptedException;
}