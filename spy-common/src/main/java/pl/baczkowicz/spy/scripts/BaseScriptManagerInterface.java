/***********************************************************************************
 * 
 * Copyright (c) 2016 Kamil Baczkowicz
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

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import pl.baczkowicz.spy.common.generated.ScriptDetails;
import pl.baczkowicz.spy.exceptions.SpyException;
import pl.baczkowicz.spy.messages.IBaseMessage;

public interface BaseScriptManagerInterface
{

	/**
	 * Creates and records a script with the given details.
	 * 
	 * @param scriptDetails The script details
	 * 
	 * @return Created script
	 */
	Script addScript(ScriptDetails scriptDetails);

	Script addScript(String scriptLocation);

	/**
	 * Creates and records a script with the given details.
	 * 
	 * @param scriptName The script name
	 * @param content Script's content
	 * 
	 * @return Created script
	 */
	Script addInlineScript(String scriptName, String content);

	/**
	 * Creates and records scripts with the given details.
	 * 
	 * @param scriptDetails The script details
	 */
	List<Script> addScripts(List<ScriptDetails> scriptDetails);

	/**
	 * Adds scripts from the given directory.
	 * 
	 * @param directory The directory to search for scripts
	 */
	void addScripts(String directory);

	/**
	 * Populates scripts from a list of files. This doesn't override existing files.
	 * 
	 * @param files List of script files
	 * 
	 * @return The list of created (newly added) script objects
	 */
	List<Script> populateScriptsFromFileList(List<File> files);

	/**
	 * Populates scripts from a list of script details.
	 * 
	 * @param scriptDetails List of script details
	 * 
	 * @return The list of created script objects
	 */
	List<Script> populateScripts(List<ScriptDetails> scriptDetails);

	/**
	 * Populates the script object with the necessary values and references.
	 * 
	 * @param script The script object to be populated
	 * @param scriptFile The script's file name 
	 * @param connection The connection for which this script will be running
	 * @param scriptDetails Script details
	 */
	void createFileBasedScript(Script script, File scriptFile, ScriptDetails scriptDetails);

	void setVariable(Script script, String name, Object value);

	/**
	 * Populates the script object with the necessary values and references.
	 * 
	 * @param script The script object to be populated
	 * @param scriptName The name of the script
	 */
	void createScript(Script script, String scriptName);

	void populateEngineVariables(Script script) throws SpyException;

	/**
	 * Runs the given script in a synchronous or asynchronous way.
	 * 
	 * @param script The script to run
	 * @param asynchronous Whether to run the script asynchronously or not
	 */
	void runScript(Script script, boolean asynchronous);

	/**
	 * Runs the given script in a synchronous or asynchronous way.
	 * 
	 * @param script The script to run
	 * @param asynchronous Whether to run the script asynchronously or not
	 * @param args Arguments/parameters passed onto the script
	 */
	void runScript(Script script, boolean asynchronous, Map<String, Object> args);

	Script addAndRunScript(String scriptLocation, boolean async, Map<String, Object> args);

	/**
	 * Runs the given script and passes the given message as a parameter. Defaults to the 'receivedMessage' parameter and synchronous execution.
	 * 
	 * @param script The script to run
	 * @param message The message to be passed onto the script
	 */
	void runScriptFileWithReceivedMessage(String scriptFile, IBaseMessage receivedMessage);

	/**
	 * Runs the given script and passes the given message as a parameter. Defaults to the 'receivedMessage' parameter and synchronous execution.
	 * 
	 * @param script The script to run
	 * @param message The message to be passed onto the script
	 */
	boolean runScriptWithReceivedMessage(Script script, IBaseMessage receivedMessage);

	/**
	 * Runs the given script and passes the given message as a parameter. Defaults to the 'message' parameter and asynchronous execution.
	 * 
	 * @param script The script to run
	 * @param message The message to be passed onto the script
	 */
	void runScriptFileWithMessage(Script script, IBaseMessage message);

	/**
	 * Runs the given script and passes the given object as a parameter.
	 * 
	 * @param script The script to run
	 * @param parameterName The name of the message parameter
	 * @param message The message to be passed onto the script
	 * @param asynchronous Whether the call should be asynchronous
	 */
	void runScriptFileParameter(Script script, String parameterName, Object parameter, boolean asynchronous);

	boolean invokeBefore(Script script);

	boolean invokeAfter(Script script);

	Object invokeFunction(Script script, String function, Object... args) throws NoSuchMethodException, ScriptException;

	void stopScript(Script script);

	void stopScripts();

	/**
	 * Gets script object for the given file.
	 * 
	 * @param scriptFile The file for which to get the script object
	 * 
	 * @return Script object or null if not found
	 */
	Script getScriptObjectFromName(String scriptFile);

	/**
	 * Checks if any of the scripts is running.
	 * 
	 * @return True if any of the scripts is running
	 */
	boolean areScriptsRunning();

	/**
	 * Gets the name to script object mapping.
	 *  
	 * @return Script name to object mapping
	 */
	Map<String, Script> getScriptsMap();

	/**
	 * Gets the collection of scripts.
	 *  
	 * @return All scripts
	 */
	Collection<Script> getScripts();

	void addScript(Script script);

	boolean containsScript(Script script);

	void addCustomParameters(Map<String, Object> parameters);

}