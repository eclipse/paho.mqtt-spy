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
package pl.baczkowicz.spy.ui.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import pl.baczkowicz.spy.common.generated.ScriptDetails;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.files.FileUtils;
import pl.baczkowicz.spy.scripts.BaseScriptManager;
import pl.baczkowicz.spy.scripts.Script;
import pl.baczkowicz.spy.ui.configuration.BaseConfigurationManager;
import pl.baczkowicz.spy.ui.properties.PublicationScriptProperties;

public abstract class InteractiveBaseScriptManager extends BaseScriptManager implements InteractiveBaseScriptManagerInterface
{
	/** List of scripts, as displayed on the UI. */
	private final ObservableList<PublicationScriptProperties> observableScriptList = FXCollections.observableArrayList();
	
	public InteractiveBaseScriptManager(IKBus eventBus, Executor executor)
	{
		super(eventBus, executor);
	}

	public static String getScriptDirectoryForConnection(final String configuredDirectory)
	{
		final String filePathSeparator = System.getProperty("file.separator");
		
		if (configuredDirectory != null && !configuredDirectory.isEmpty())
		{
			if (!configuredDirectory.endsWith(filePathSeparator))
			{
				return configuredDirectory + filePathSeparator;
			}
			return configuredDirectory;				
		}
		else
		{
			// If directory defined, use the mqtt-spy's home directory
			return BaseConfigurationManager.getDefaultHomeDirectory();
		}	
	}

	/* (non-Javadoc)
	 * @see pl.baczkowicz.mqttspy.ui.scripts.InteractiveBaseScriptManager#addScripts(java.util.List, pl.baczkowicz.spy.ui.scripts.ScriptTypeEnum)
	 */
	@Override
	public void addScripts(final List<ScriptDetails> scriptDetails, final ScriptTypeEnum type)
	{
		final List<Script> addedScripts = populateScripts(scriptDetails);
		
		for (final Script script : addedScripts)
		{
			final PublicationScriptProperties properties = new PublicationScriptProperties(script);
			properties.typeProperty().setValue(type);
			script.setObserver(properties);
			observableScriptList.add(properties);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.mqttspy.ui.scripts.InteractiveBaseScriptManager#addScripts(java.lang.String, boolean, pl.baczkowicz.spy.ui.scripts.ScriptTypeEnum)
	 */
	@Override
	public void addScripts(final String directory, final boolean includeSubdirectories, final ScriptTypeEnum type)
	{
		final List<File> files = new ArrayList<File>(); 
		
		files.addAll(FileUtils.getFileNamesForDirectory(
				getScriptDirectoryForConnection(directory), 
				includeSubdirectories, 
				SCRIPT_EXTENSION));	
		
		populateScriptsFromFileList(directory, files, type);
	}
	
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.mqttspy.ui.scripts.InteractiveBaseScriptManager#addScripts(java.lang.String, pl.baczkowicz.spy.ui.scripts.ScriptTypeEnum)
	 */
	@Override
	public void addScripts(final String directory, final ScriptTypeEnum type)
	{
		final List<File> files = new ArrayList<File>(); 
		
		files.addAll(FileUtils.getFileNamesForDirectory(
				getScriptDirectoryForConnection(directory), SCRIPT_EXTENSION));	
		
		populateScriptsFromFileList(directory, files, type);
	}
	

	/* (non-Javadoc)
	 * @see pl.baczkowicz.mqttspy.ui.scripts.InteractiveBaseScriptManager#populateScriptsFromFileList(java.lang.String, java.util.List, pl.baczkowicz.spy.ui.scripts.ScriptTypeEnum)
	 */
	@Override
	public void populateScriptsFromFileList(final String rootDirectory, final List<File> files, final ScriptTypeEnum type)
	{
		final List<Script> addedScripts = populateScriptsFromFileList(files);
		
		for (final Script script : addedScripts)
		{
			script.setRootDirectory(rootDirectory);
			final PublicationScriptProperties properties = new PublicationScriptProperties(script);
			properties.typeProperty().setValue(type);
			script.setObserver(properties);
			observableScriptList.add(properties);
		}		
	}

	/* (non-Javadoc)
	 * @see pl.baczkowicz.mqttspy.ui.scripts.InteractiveBaseScriptManager#stopScriptFile(java.io.File)
	 */
	@Override
	public void stopScriptFile(final File scriptFile)
	{
		final Script script = getPublicationScriptProperties(observableScriptList, getScriptName(scriptFile)).getScript();
		
		stopScript(script);
	}
	
	public static PublicationScriptProperties getPublicationScriptProperties(final ObservableList<PublicationScriptProperties> observableScriptList, final String scriptName)
	{
		for (final PublicationScriptProperties properties : observableScriptList)
		{
			if (properties.getScript().getName().equals(scriptName))
			{
				return properties;				
			}
		}
		
		return null;
	}
	

	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.mqttspy.ui.scripts.InteractiveBaseScriptManager#getObservableScriptList()
	 */
	@Override
	public ObservableList<PublicationScriptProperties> getObservableScriptList()
	{
		return observableScriptList;
	}

	/* (non-Javadoc)
	 * @see pl.baczkowicz.mqttspy.ui.scripts.InteractiveBaseScriptManager#removeScript(pl.baczkowicz.spy.ui.properties.PublicationScriptProperties)
	 */
	@Override
	public void removeScript(final PublicationScriptProperties item)
	{
		getScriptsMap().remove(item.getScript().getScriptId());
		observableScriptList.remove(item);
	}

	@Override
	public void clear()
	{
		getObservableScriptList().clear();
		getScriptsMap().clear();
	}		
}