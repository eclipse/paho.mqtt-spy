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
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.exceptions.CriticalException;
import pl.baczkowicz.spy.exceptions.SpyException;
import pl.baczkowicz.spy.files.FileUtils;
import pl.baczkowicz.spy.messages.IBaseMessage;

/**
 * This class manages script creation and execution.
 */
public abstract class BaseScriptManager implements BaseScriptManagerInterface
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
	protected IKBus eventBus;

	/** Executor for tasks. */
	protected Executor executor;

	private Map<String, Object> customParameters;
	
	/**
	 * Creates the script manager.
	 * 
	 * @param eventBus The event bus to be used
	 * @param executor The executor to be used
	 * @param connection The connection for which to run the scripts
	 */
	public BaseScriptManager(final IKBus eventBus, final Executor executor)
	{
		this.eventBus = eventBus;
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
	 * Gets the file (script) name for the given file object, including the subdirectory it's in.
	 * 
	 * @param file The file from which to get the filename
	 * 
	 * @return The name of the script file, including the subdirectory
	 */
	public static String getScriptNameWithSubdirectory(final File file, final String rootDirectory)
	{
		final String filePathSeparator = System.getProperty("file.separator");
		
		final String valueToReplace = rootDirectory.endsWith(filePathSeparator) ? rootDirectory : rootDirectory + filePathSeparator;
		
		return file.getAbsolutePath().replace(valueToReplace, "").replace(file.getName(), getScriptName(file));
	}
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#addScript(pl.baczkowicz.spy.common.generated.ScriptDetails)
	 */
	@Override
	public Script addScript(final ScriptDetails scriptDetails)
	{
		final File scriptFile = new File(scriptDetails.getFile());
		
		final Script script = new Script();
				
		createFileBasedScript(script, scriptFile, scriptDetails);
		
		logger.info("Adding script {} at {}", script.getName(), scriptFile.getAbsolutePath());
		scripts.put(scriptFile.getAbsolutePath(), script);
		
		return script;
	}
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#addScript(java.lang.String)
	 */
	@Override
	public Script addScript(final String scriptLocation)
	{
		return addScript(new ScriptDetails(false, false, scriptLocation));
	}
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#addInlineScript(java.lang.String, java.lang.String)
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#addScripts(java.util.List)
	 */
	@Override
	public List<Script> addScripts(final List<ScriptDetails> scriptDetails)
	{
		final List<Script> addedScripts = new ArrayList<>();
		
		for (final ScriptDetails script : scriptDetails)
		{
			addedScripts.add(addScript(script));			
		}
		
		return addedScripts;
	}
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#addScripts(java.lang.String)
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#populateScriptsFromFileList(java.util.List)
	 */
	@Override
	public List<Script> populateScriptsFromFileList(final List<File> files)
	{
		final List<Script> addedScripts = new ArrayList<>();
		
		for (final File scriptFile : files)
		{
			Script script = scripts.get(Script.getScriptIdFromFile(scriptFile));
			
			if (script == null)					
			{			
				script = new Script();
				
				createFileBasedScript(script, scriptFile, new ScriptDetails(true, false, scriptFile.getName())); 			
				
				addedScripts.add(script);
				addScript(script);
			}				
		}
		
		return addedScripts;
	}
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#populateScripts(java.util.List)
	 */
	@Override
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
					script = new Script();
					
					createFileBasedScript(script, scriptFile, details); 			
					
					addedScripts.add(script);
					addScript(script);
				}	
			}
		}			
		
		return addedScripts;
	}
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#createFileBasedScript(pl.baczkowicz.spy.scripts.Script, java.io.File, pl.baczkowicz.spy.common.generated.ScriptDetails)
	 */
	@Override
	public void createFileBasedScript(final Script script, final File scriptFile, final ScriptDetails scriptDetails)
	{
		final String scriptName = BaseScriptManager.getScriptName(scriptFile);
		
		createScript(script, scriptName);
		script.setScriptFile(scriptFile);
		script.setScriptDetails(scriptDetails);
	}
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#setVariable(pl.baczkowicz.spy.scripts.Script, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setVariable(final Script script, final String name, final Object value)
	{
		script.getScriptEngine().put(name, value);
	}
		
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#createScript(pl.baczkowicz.spy.scripts.Script, java.lang.String)
	 */
	@Override
	public void createScript(final Script script, final String scriptName)
	{
		try
		{
			final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");										
			
			if (scriptEngine != null)
			{
				script.setName(scriptName);			
				script.setStatusAndNotify(ScriptRunningState.NOT_STARTED);
				script.setScriptEngine(scriptEngine);		
				
				populateEngineVariables(script);
			}
			else
			{
				throw new CriticalException("Cannot instantiate the nashorn javascript engine - most likely you don't have Java 8 installed. "
						+ "Please either disable scripts in your configuration file or install the appropriate JRE/JDK.");
			}
		}
		catch (SpyException e)
		{
			throw new CriticalException("Cannot initialise the script objects");
		}
	}
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#populateEngineVariables(pl.baczkowicz.spy.scripts.Script)
	 */
	@Override
	abstract public void populateEngineVariables(final Script script) throws SpyException;
				
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
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#runScript(pl.baczkowicz.spy.scripts.Script, boolean)
	 */
	@Override
	public void runScript(final Script script, final boolean asynchronous)
	{
		runScript(script, asynchronous, null);
	}
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#runScript(pl.baczkowicz.spy.scripts.Script, boolean, java.util.Map)
	 */
	@Override
	public void runScript(final Script script, final boolean asynchronous, final Map<String, Object> args)
	{
		// Only start if not running already
		if (!ScriptRunningState.RUNNING.equals(script.getStatus()))
		{
			script.createScriptRunner(eventBus, executor);
			script.setAsynchronous(asynchronous);
			
			final Map<String, Object> scriptArgs = new HashMap<>();			
			if (args != null)
			{				
				scriptArgs.putAll(args);
			}			
			if (customParameters != null)
			{
				scriptArgs.putAll(customParameters);		
			}			
			setVariable(script, "args", scriptArgs);

			
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
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#addAndRunScript(java.lang.String, boolean, java.util.Map)
	 */
	@Override
	public Script addAndRunScript(final String scriptLocation, final boolean async, final Map<String, Object> args)
	{
		final Script script = addScript(scriptLocation);
		runScript(script, async, args);
		return script;
	}	
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#runScriptFileWithReceivedMessage(java.lang.String, pl.baczkowicz.spy.messages.IBaseMessage)
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#runScriptWithReceivedMessage(pl.baczkowicz.spy.scripts.Script, pl.baczkowicz.spy.messages.IBaseMessage)
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#runScriptFileWithMessage(pl.baczkowicz.spy.scripts.Script, pl.baczkowicz.spy.messages.IBaseMessage)
	 */
	@Override
	public void runScriptFileWithMessage(final Script script, final IBaseMessage message)
	{				
		runScriptFileParameter(script, BaseScriptManager.MESSAGE_PARAMETER, message, true);
	}
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#runScriptFileParameter(pl.baczkowicz.spy.scripts.Script, java.lang.String, java.lang.Object, boolean)
	 */
	@Override
	public void runScriptFileParameter(final Script script, final String parameterName, final Object parameter, final boolean asynchronous)
	{	
		setVariable(script, parameterName, parameter);
		runScript(script, asynchronous);		
	}
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#invokeBefore(pl.baczkowicz.spy.scripts.Script)
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#invokeAfter(pl.baczkowicz.spy.scripts.Script)
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#invokeFunction(pl.baczkowicz.spy.scripts.Script, java.lang.String, java.lang.Object)
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#stopScript(pl.baczkowicz.spy.scripts.Script)
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#stopScripts()
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#getScriptObjectFromName(java.lang.String)
	 */
	@Override
	public Script getScriptObjectFromName(final String scriptFile)
	{
		return scripts.get(scriptFile);
	}

	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#areScriptsRunning()
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#getScriptsMap()
	 */
	@Override
	public Map<String, Script> getScriptsMap()
	{
		return scripts;
	}
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#getScripts()
	 */
	@Override
	public Collection<Script> getScripts()
	{
		return scripts.values();
	}
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#addScript(pl.baczkowicz.spy.scripts.Script)
	 */
	@Override
	public void addScript(final Script script)
	{
		scripts.put(script.getScriptId(), script);
	}
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#containsScript(pl.baczkowicz.spy.scripts.Script)
	 */
	@Override
	public boolean containsScript(final Script script)
	{
		return scripts.containsKey(script.getScriptId());
	}

	/* (non-Javadoc)
	 * @see pl.baczkowicz.spy.scripts.BaseScriptManageInterface#addCustomParameters(java.util.Map)
	 */
	@Override
	public void addCustomParameters(final Map<String, Object> parameters)
	{
		this.customParameters = parameters;
	}
}
