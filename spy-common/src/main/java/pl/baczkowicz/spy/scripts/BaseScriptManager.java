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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.common.generated.ScriptDetails;
import pl.baczkowicz.spy.exceptions.CriticalException;
import pl.baczkowicz.spy.messages.IBaseMessage;
import pl.baczkowicz.spy.utils.FileUtils;

/**
 * This class manages script creation and execution.
 */
public abstract class BaseScriptManager
{
	/** Name of the variable in JS for received messages. */
	public static final String RECEIVED_MESSAGE_PARAMETER = "receivedMessage";
	
	/** Name of the variable in JS for published/searched message. */
	public static final String MESSAGE_PARAMETER = "message";
	
	public static final String SCRIPT_EXTENSION = ".js";
	
	public static final String BEFORE_METHOD = "before";
	
	public static final String AFTER_METHOD = "after";
	
	public static final String ON_MESSAGE_METHOD = "onMessage";
	
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(BaseScriptManager.class);
	
	/** Mapping between unique script names and scripts. */
	private Map<String, Script> scripts = new HashMap<String, Script>();
	
	/** Used for notifying events related to script execution. */
	protected IScriptEventManager eventManager;

	/** Executor for tasks. */
	protected Executor executor;
	
	/**
	 * Creates the script manager.
	 * 
	 * @param eventManager The event manager to be used
	 * @param executor The executor to be used
	 * @param connection The connection for which to run the scripts
	 */
	public BaseScriptManager(final IScriptEventManager eventManager, final Executor executor)
	{
		this.eventManager = eventManager;
		this.executor = executor;
	}
	
	/**
	 * Gets the file (script) name for the given file object.
	 * 
	 * @param file The file from which to get the filename
	 * 
	 * @return The name of the script file
	 */
	public static String getScriptName(final File file)
	{
		return file.getName().replace(SCRIPT_EXTENSION,  "");
	}
	
	/**
	 * Creates and records a script with the given details.
	 * 
	 * @param scriptDetails The script details
	 * 
	 * @return Created script
	 */
	public Script addScript(final ScriptDetails scriptDetails)
	{
		final File scriptFile = new File(scriptDetails.getFile());
		
		final String scriptName = getScriptName(scriptFile);
		
		final Script script = new Script();
				
		createFileBasedScript(script, scriptName, scriptFile, scriptDetails);
		
		logger.info("Adding script {} at {}", scriptName, scriptFile.getAbsolutePath());
		scripts.put(scriptFile.getAbsolutePath(), script);
		
		return script;
	}
	
	public Script addScript(final String scriptLocation)
	{
		return addScript(new ScriptDetails(false, false, scriptLocation));
	}
	
	/**
	 * Creates and records a script with the given details.
	 * 
	 * @param scriptName The script name
	 * @param content Script's content
	 * 
	 * @return Created script
	 */
	public Script addInlineScript(final String scriptName, final String content)
	{
		final Script script = new Script();
		script.setScriptContent(content);
		script.setScriptDetails(new ScriptDetails(true, false, null));
				
		createScript(script, scriptName);
		
		logger.debug("Adding in-line script {}", scriptName);

		scripts.put(scriptName, script);
		
		return script;
	}
	
	/**
	 * Creates and records scripts with the given details.
	 * 
	 * @param scriptDetails The script details
	 */
	public List<Script> addScripts(final List<ScriptDetails> scriptDetails)
	{
		final List<Script> addedScripts = new ArrayList<>();
		
		for (final ScriptDetails script : scriptDetails)
		{
			addedScripts.add(addScript(script));			
		}
		
		return addedScripts;
	}
	
	/**
	 * Adds scripts from the given directory.
	 * 
	 * @param directory The directory to search for scripts
	 */
	public void addScripts(final String directory)
	{
		final List<File> files = new ArrayList<File>(); 
		
		if (directory != null && !directory.isEmpty())
		{
			files.addAll(FileUtils.getFileNamesForDirectory(directory, SCRIPT_EXTENSION));				
		}
		else
		{
			logger.error("Given directory is empty");
		}	
		
		populateScriptsFromFileList(files);
	}
	
	/**
	 * Populates scripts from a list of files. This doesn't override existing files.
	 * 
	 * @param files List of script files
	 * 
	 * @return The list of created (newly added) script objects
	 */
	public List<Script> populateScriptsFromFileList(final List<File> files)
	{
		final List<Script> addedScripts = new ArrayList<>();
		
		for (final File scriptFile : files)
		{
			Script script = scripts.get(Script.getScriptIdFromFile(scriptFile));
			
			if (script == null)					
			{
				final String scriptName = getScriptName(scriptFile);				
				script = new Script();
				
				createFileBasedScript(script, scriptName, scriptFile, new ScriptDetails(true, false, scriptFile.getName())); 			
				
				addedScripts.add(script);
				addScript(script);
			}				
		}
		
		return addedScripts;
	}
	
	/**
	 * Populates scripts from a list of script details.
	 * 
	 * @param scriptDetails List of script details
	 * 
	 * @return The list of created script objects
	 */
	public List<Script> populateScripts(final List<ScriptDetails> scriptDetails)
	{
		final List<Script> addedScripts = new ArrayList<>();
		
		for (final ScriptDetails details : scriptDetails)
		{
			final File scriptFile = new File(details.getFile());
			
			if (!scriptFile.exists())					
			{
				logger.warn("Script {} does not exist!", details.getFile());
			}
			else
			{
				logger.info("Adding script {}", details.getFile());
							
				Script script = scripts.get(Script.getScriptIdFromFile(scriptFile));
				
				if (script == null)					
				{
					final String scriptName = getScriptName(scriptFile);
					script = new Script();
					
					createFileBasedScript(script, scriptName, scriptFile, details); 			
					
					addedScripts.add(script);
					addScript(script);
				}	
			}
		}			
		
		return addedScripts;
	}
	
	/**
	 * Populates the script object with the necessary values and references.
	 * 
	 * @param script The script object to be populated
	 * @param scriptName The name of the script
	 * @param scriptFile The script's file name 
	 * @param connection The connection for which this script will be running
	 * @param scriptDetails Script details
	 */
	public void createFileBasedScript(final Script script,
			final String scriptName, final File scriptFile, final ScriptDetails scriptDetails)
	{
		createScript(script, scriptName);
		script.setScriptFile(scriptFile);
		script.setScriptDetails(scriptDetails);
	}
	
	public void setVariable(final Script script, final String name, final Object value)
	{
		script.getScriptEngine().put(name, value);
	}
		
	/**
	 * Populates the script object with the necessary values and references.
	 * 
	 * @param script The script object to be populated
	 * @param scriptName The name of the script
	 */
	public void createScript(final Script script, final String scriptName)
	{
		final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");										
		
		if (scriptEngine != null)
		{
			script.setName(scriptName);			
			script.setStatus(ScriptRunningState.NOT_STARTED);
			script.setScriptEngine(scriptEngine);		
			
			populateEngineVariables(script);
//			final MqttScriptIO scriptIO = new MqttScriptIO(connection, eventManager, script, executor); 
//			//script.setScriptIO(scriptIO);
//			
//			final Map<String, Object> scriptVariables = new HashMap<String, Object>();
//			
//			// This should be considered deprecated
//			scriptVariables.put("mqttspy", scriptIO);
//			// This should be used for general script-related actions
//			scriptVariables.put("spy", scriptIO);
//			// Going forward, this should only have mqtt-specific elements, e.g. pub/sub
//			scriptVariables.put("mqtt", scriptIO);
//			
//			scriptVariables.put("logger", LoggerFactory.getLogger(ScriptRunner.class));
//			
//			final IMqttMessageLogIO mqttMessageLog = new MqttMessageLogIO();
//			// Add it to the script IO so that it gets stopped when requested
//			script.addTask(mqttMessageLog);			
//			scriptVariables.put("messageLog", mqttMessageLog);
//			
//			putJavaVariablesIntoEngine(scriptEngine, scriptVariables);
		}
		else
		{
			throw new CriticalException("Cannot instantiate the nashorn javascript engine - most likely you don't have Java 8 installed. "
					+ "Please either disable scripts in your configuration file or install the appropriate JRE/JDK.");
		}
	}
	
	abstract public void populateEngineVariables(final Script script);
				
	/**
	 * Puts a the given map of variables into the engine.
	 * 
	 * @param engine The engine to be populated with variables
	 * @param variables The variables to be populated
	 */
	public static void putJavaVariablesIntoEngine(final ScriptEngine engine, final Map<String, Object> variables)
	{		
		final Bindings bindings = new SimpleBindings();
		// final Bindings bindings = engine.createBindings();

		for (String key : variables.keySet())
		{
			bindings.put(key, variables.get(key));
		}

		engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
	}
	
	/**
	 * Runs the given script in a synchronous or asynchronous way.
	 * 
	 * @param script The script to run
	 * @param asynchronous Whether to run the script asynchronously or not
	 */
	public void runScript(final Script script, final boolean asynchronous)
	{
		runScript(script, asynchronous, null);
	}
	
	/**
	 * Runs the given script in a synchronous or asynchronous way.
	 * 
	 * @param script The script to run
	 * @param asynchronous Whether to run the script asynchronously or not
	 * @param args Arguments/parameters passed onto the script
	 */
	public void runScript(final Script script, final boolean asynchronous, final Map<String, Object> args)
	{
		// Only start if not running already
		if (!ScriptRunningState.RUNNING.equals(script.getStatus()))
		{
			script.createScriptRunner(eventManager, executor);
			script.setAsynchronous(asynchronous);
			// Set test case args
			if (args != null)
			{
				setVariable(script, "args", args);
			}
			
			if (asynchronous)
			{
				new Thread(script.getScriptRunner()).start();
			}
			else
			{
				script.getScriptRunner().run();
			}
		}
	}
	
	public Script addAndRunScript(final String scriptLocation, final boolean async, final Map<String, Object> args)
	{
		final Script script = addScript(scriptLocation);
		runScript(script, async, args);
		return script;
	}	
	
	/**
	 * Runs the given script and passes the given message as a parameter. Defaults to the 'receivedMessage' parameter and synchronous execution.
	 * 
	 * @param script The script to run
	 * @param message The message to be passed onto the script
	 */
	public void runScriptFileWithReceivedMessage(final String scriptFile, final IBaseMessage receivedMessage)
	{
		final Script script = getScriptObjectFromName(scriptFile);
		
		if (script != null)
		{
			runScriptFileParameter(script, BaseScriptManager.RECEIVED_MESSAGE_PARAMETER, receivedMessage, false);
		}
		else
		{
			logger.warn("No script file found at {}. Please check if this location is correct.", scriptFile);
		}
	}
	
	/**
	 * Runs the given script and passes the given message as a parameter. Defaults to the 'receivedMessage' parameter and synchronous execution.
	 * 
	 * @param script The script to run
	 * @param message The message to be passed onto the script
	 */
	public boolean runScriptWithReceivedMessage(final Script script, final IBaseMessage receivedMessage)
	{
		setVariable(script, BaseScriptManager.RECEIVED_MESSAGE_PARAMETER, receivedMessage);
		try
		{
			invokeFunction(script, ON_MESSAGE_METHOD);
			return true;
		}
		catch (NoSuchMethodException | ScriptException e)
		{
			return false;
		}		
	}
	
	/**
	 * Runs the given script and passes the given message as a parameter. Defaults to the 'message' parameter and asynchronous execution.
	 * 
	 * @param script The script to run
	 * @param message The message to be passed onto the script
	 */
	public void runScriptFileWithMessage(final Script script, final IBaseMessage message)
	{				
		runScriptFileParameter(script, BaseScriptManager.MESSAGE_PARAMETER, message, true);
	}
	
	/**
	 * Runs the given script and passes the given object as a parameter.
	 * 
	 * @param script The script to run
	 * @param parameterName The name of the message parameter
	 * @param message The message to be passed onto the script
	 * @param asynchronous Whether the call should be asynchronous
	 */
	public void runScriptFileParameter(final Script script, final String parameterName, final Object parameter, final boolean asynchronous)
	{	
		setVariable(script, parameterName, parameter);
		runScript(script, asynchronous);		
	}
	
	public boolean invokeBefore(final Script script)
	{
		try
		{
			invokeFunction(script, BaseScriptManager.BEFORE_METHOD);
		}
		catch (NoSuchMethodException e)
		{
			logger.info("No setup function present");
		}
		catch (ScriptException e)
		{					
			logger.error("Function execution failure", e);
			
			return false;
		}
		
		return true;
	}
	
	public boolean invokeAfter(final Script script)
	{
		try
		{
			invokeFunction(script, BaseScriptManager.AFTER_METHOD);
		}
		catch (NoSuchMethodException e)
		{
			logger.info("No after function present");
		}
		catch (ScriptException e)
		{					
			logger.error("Function execution failure", e);
			
			return false;
		}
		
		return true;
	}
	
	public Object invokeFunction(final Script script, final String function, final Object... args) throws NoSuchMethodException, ScriptException
	{
		final Invocable invocable = (Invocable) script.getScriptEngine();
		
		Object result = null;
		try
		{
			 result = invocable.invokeFunction(function, args);
		}
		catch (NoSuchMethodException e)
		{
			throw e;
		}
		// Catch all - in case the script throws an undefined exception
		catch (Exception e)
		{
			throw new ScriptException(e);
		}
		
		return result;
	}	
	
	public void stopScript(final Script script)
	{
		logger.debug("Stopping script " + script.getName());
		
		if (script.getScriptRunner() != null)
		{
			final Thread scriptThread = script.getScriptRunner().getThread();
	
			if (scriptThread != null)
			{
				scriptThread.interrupt();
			}
		}
	}
	
	public void stopScripts()
	{
		// Stop all scripts
		for (final Script script : getScripts())
		{
			// Only stop file-based scripts
			if (script.getScriptFile() != null)
			{
				stopScript(script);
			}
		}		
	}
	
	/**
	 * Gets script object for the given file.
	 * 
	 * @param scriptFile The file for which to get the script object
	 * 
	 * @return Script object or null if not found
	 */
	public Script getScriptObjectFromName(final String scriptFile)
	{
		return scripts.get(scriptFile);
	}

	/**
	 * Checks if any of the scripts is running.
	 * 
	 * @return True if any of the scripts is running
	 */
	public boolean areScriptsRunning()
	{
		for (final Script script : scripts.values())
		{
			if (ScriptRunningState.RUNNING.equals(script.getStatus()))
			{
				logger.debug("Script {} is still running", script.getName());
				return true;
			}
		}

		return false;
	}
	
	/**
	 * Gets the name to script object mapping.
	 *  
	 * @return Script name to object mapping
	 */
	public Map<String, Script> getScriptsMap()
	{
		return scripts;
	}
	
	/**
	 * Gets the collection of scripts.
	 *  
	 * @return All scripts
	 */
	public Collection<Script> getScripts()
	{
		return scripts.values();
	}
	
	public void addScript(final Script script)
	{
		scripts.put(script.getScriptId(), script);
	}
	
	public boolean containsScript(final Script script)
	{
		return scripts.containsKey(script.getScriptId());
	}
}
