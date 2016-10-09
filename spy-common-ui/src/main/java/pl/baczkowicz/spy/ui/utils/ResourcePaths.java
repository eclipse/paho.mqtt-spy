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
package pl.baczkowicz.spy.ui.utils;

public class ResourcePaths
{
	/** Package with all UI stuff. */
	public static final String UI_PACKAGE = "/ui/";
	
	private static final String IMAGES_FOLDER = "images/";	
	
	/** Folder with all FXML files. */
	private static final String FXML_FOLDER = "fxml/";
	
	public static final String IMAGES_PATH = UI_PACKAGE + IMAGES_FOLDER;
	
	public static final String FXML_PATH = UI_PACKAGE + FXML_FOLDER;
}
