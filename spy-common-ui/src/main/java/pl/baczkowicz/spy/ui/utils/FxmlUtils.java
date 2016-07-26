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
package pl.baczkowicz.spy.ui.utils;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import pl.baczkowicz.spy.exceptions.CriticalException;

/**
 * FXML-related utilities.
 */
@SuppressWarnings("rawtypes")
public class FxmlUtils
{
	/** Package with all FXML files. */
	public final static String FXML_PACKAGE = "ui/";

	/** Folder with all FXML files. */
	public static final String FXML_LOCATION = "fxml/";
	
	/** The parent class to be used for getting resources. */
	private static Class parentClass;

	/**
	 * Creates an FXML loader.
	 * 
	 * @param localFxmlFile The FXML file to load
	 * 
	 * @return FXMLLoader Created FXML loader
	 */
	public static FXMLLoader createFxmlLoaderForProjectFile(final String localFxmlFile)
	{
		return createFxmlLoader(parentClass, FxmlUtils.FXML_PACKAGE + FxmlUtils.FXML_LOCATION + localFxmlFile);
	}
	
	/**
	 * Creates an FXML loader.
	 * 
	 * @param parent Parent object
	 * @param fxmlFile The FXML file to load
	 * 
	 * @return FXMLLoader
	 */
	public static FXMLLoader createFxmlLoader(final Class parent, final String fxmlFile)
	{
		return new FXMLLoader(parent.getResource(fxmlFile));
	}
	
	/**
	 * Loads an anchor pane using the supplied loader.
	 * 
	 * @param loader The FXML loader to be used
	 * 
	 * @return The loader AnchorPane
	 */
	public static AnchorPane loadAnchorPane(final FXMLLoader loader)
	{
		try
		{
			return (AnchorPane) loader.load();
		}
		catch (IOException e)
		{
			// TODO: log
			throw new CriticalException("Cannot load FXML", e);
		}
	}

	/**
	 * Gets the parent class.
	 * 
	 * @return the parentClass
	 */
	public static Class getParentClass()
	{
		return parentClass;
	}

	/**
	 * Sets the parent class.
	 * 
	 * @param parentClass the parentClass to set
	 */
	public static void setParentClass(Class parentClass)
	{
		FxmlUtils.parentClass = parentClass;
	}
}
