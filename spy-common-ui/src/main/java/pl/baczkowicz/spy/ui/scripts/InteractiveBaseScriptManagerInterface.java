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
import java.util.List;

import javafx.collections.ObservableList;
import pl.baczkowicz.spy.common.generated.ScriptDetails;
import pl.baczkowicz.spy.scripts.BaseScriptManagerInterface;
import pl.baczkowicz.spy.ui.properties.PublicationScriptProperties;

public interface InteractiveBaseScriptManagerInterface extends BaseScriptManagerInterface
{
	void addScripts(List<ScriptDetails> scriptDetails, ScriptTypeEnum type);

	void addScripts(String directory, boolean includeSubdirectories, ScriptTypeEnum type);

	void addScripts(String directory, ScriptTypeEnum type);

	void populateScriptsFromFileList(String rootDirectory, List<File> files, ScriptTypeEnum type);

	void stopScriptFile(File scriptFile);

	ObservableList<PublicationScriptProperties> getObservableScriptList();

	void removeScript(PublicationScriptProperties item);

	void clear();
}