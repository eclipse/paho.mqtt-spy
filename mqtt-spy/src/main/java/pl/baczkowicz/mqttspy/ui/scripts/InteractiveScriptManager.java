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
package pl.baczkowicz.mqttspy.ui.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.configuration.generated.TabbedSubscriptionDetails;
import pl.baczkowicz.mqttspy.connectivity.IMqttConnection;
import pl.baczkowicz.mqttspy.scripts.MqttScriptManager;
import pl.baczkowicz.spy.common.generated.ScriptDetails;
import pl.baczkowicz.spy.scripts.IScriptEventManager;
import pl.baczkowicz.spy.scripts.Script;
import pl.baczkowicz.spy.ui.properties.PublicationScriptProperties;
import pl.baczkowicz.spy.ui.scripts.ScriptTypeEnum;
import pl.baczkowicz.spy.ui.utils.RunLaterExecutor;
import pl.baczkowicz.spy.utils.FileUtils;

/**
 * Script manager that interacts with the UI.
 */
public class InteractiveScriptManager extends MqttScriptManager
{
	/** Diagnostic logger. */
	// private final static Logger logger = LoggerFactory.getLogger(InteractiveScriptManager.class);
	
	/** List of scripts, as displayed on the UI. */
	private final ObservableList<PublicationScriptProperties> observableScriptList = FXCollections.observableArrayList();
	
	public InteractiveScriptManager(final IScriptEventManager eventManager, final IMqttConnection connection)
	{
		super(eventManager, new RunLaterExecutor(), connection);
	}
	
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
			return ConfigurationManager.getDefaultHomeDirectory();
		}	
	}
	
	public void addScripts(final String directory, final ScriptTypeEnum type)
	{
		final List<File> files = new ArrayList<File>(); 
		
		files.addAll(FileUtils.getFileNamesForDirectory(
				getScriptDirectoryForConnection(directory), SCRIPT_EXTENSION));	
		
		populateScriptsFromFileList(files, type);
	}
	
	public void addSubscriptionScripts(final List<TabbedSubscriptionDetails> list)
	{
		final List<ScriptDetails> scripts = new ArrayList<>(); 
		
		for (final TabbedSubscriptionDetails sub : list)
		{
			if (sub.getScriptFile() != null  && !sub.getScriptFile().trim().isEmpty())
			{
				scripts.add(new ScriptDetails(false, false, sub.getScriptFile()));
			}
		}
		
		addScripts(scripts, ScriptTypeEnum.SUBSCRIPTION);
	}
	
	public void populateScriptsFromFileList(final List<File> files, final ScriptTypeEnum type)
	{
		final List<Script> addedScripts = populateScriptsFromFileList(files);
		
		for (final Script script : addedScripts)
		{
			final PublicationScriptProperties properties = new PublicationScriptProperties(script);
			properties.typeProperty().setValue(type);
			script.setObserver(properties);
			observableScriptList.add(properties);
		}		
	}
	
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
	
	public ObservableList<PublicationScriptProperties> getObservableScriptList()
	{
		return observableScriptList;
	}

	public void removeScript(final PublicationScriptProperties item)
	{
		getScriptsMap().remove(item.getScript().getScriptId());
		observableScriptList.remove(item);
	}		
}
