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
package pl.baczkowicz.spy.daemon;

import java.util.Map;

import pl.baczkowicz.spy.scripts.Script;
import pl.baczkowicz.spy.testcases.TestCaseOptions;
import pl.baczkowicz.spy.testcases.TestCaseResult;

public interface IDaemon
{
	/**
	 * Start the daemon with the provided configuration file location.
	 * 
	 * @param configurationFile The daemon's configuration file location
	 * 
	 * @return True if started correctly
	 */
	boolean start(final String configurationFile);
	
	/**
	 * Checks if we can publish/send any messages (connection is established).
	 * 
	 * @return True if OK
	 */
	boolean canPublish();

	/**
	 * Runs a script from a given location.
	 * 
	 * @param scriptLocation Script location
	 * 
	 * @return The created script object, for reference
	 */
	Script runScript(final String scriptLocation);
	
	/**
	 * Runs a script from a given location.
	 * 
	 * @param scriptLocation Script location
	 * @param async Whether the execution should be asynchronous
	 * @param args A map of key/value pairs with arguments for the script; available as "args" in the script
	 * 
	 * @return The created script object, for reference
	 */
	Script runScript(final String scriptLocation, final boolean async, final Map<String, Object> args);
	
	/**
	 * Runs a particular script's function.
	 * 
	 * @param scriptLocation The script location
	 * @param functionName Name of the function
	 * @param args A map of key/value pairs with arguments for the script; available as "args" in the script
	 * 
	 * @return Function's result
	 */
	Object runScriptFunction(final String scriptLocation, final String functionName, final Map<String, Object> args);
	
	/**
	 * Stops the given script.
	 * 
	 * @param script The script object to stop
	 */
	void stopScript(final Script script);
	
	/**
	 * Stops the given script.
	 * 
	 * @param scriptName The script name to stop
	 */
	void stopScript(final String scriptName);

	/**
	 * Runs a test case from the given location.
	 * 
	 * @param testCaseLocation Test case location
	 * 
	 * @return Test result
	 */
	TestCaseResult runTestCase(final String testCaseLocation);

	/**
	 * Runs a test case from the given location.
	 * 
	 * @param testCaseLocation Test case location
	 * @param args A map of key/value pairs with arguments for the script; available as "args" in the script
	 * 
	 * @return Test result
	 */
	TestCaseResult runTestCase(final String testCaseLocation, final Map<String, Object> args);	
	
	/**
	 * Runs a test case from the given location.
	 * 
	 * @param testCaseLocation Test case location
	 * @param args A map of key/value pairs with arguments for the script; available as "args" in the script 
	 * @param options Additional test case options (autoExport, stepInterval, recordRepeatedSteps)
	 * 
	 * @return Test result
	 */
	TestCaseResult runTestCase(final String testCaseLocation, final Map<String, Object> args, final TestCaseOptions options);	
	
	/**
	 * Stops the daemon.
	 */
	void stop();
}
