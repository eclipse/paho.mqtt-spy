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
	boolean start(final String configurationFile);
	
	void stop();
	
	TestCaseResult runTestCase(final String testCaseLocation);

	TestCaseResult runTestCase(final String testCaseLocation, final Map<String, Object> args);	
	
	TestCaseResult runTestCase(final String testCaseLocation, final Map<String, Object> args, final TestCaseOptions options);	
	
	Script runScript(final String scriptLocation);
	
	Script runScript(final String scriptLocation, final boolean async, final Map<String, Object> args);
	
	Object runScriptFunction(final String scriptLocation, final String functionName, final Map<String, Object> args);
	
	void stopScript(final Script script);
	
	void stopScript(final String scriptName);	
}
