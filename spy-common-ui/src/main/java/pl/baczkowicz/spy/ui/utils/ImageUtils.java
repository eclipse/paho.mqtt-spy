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
package pl.baczkowicz.spy.ui.utils;

import javafx.scene.image.ImageView;

public class ImageUtils
{	
	private static final String IMG_EXT = ".png";
	
	public static ImageView createIcon(final String name, final int size, final String style)
	{
		final String location = ResourcePaths.IMAGES_PATH + name + IMG_EXT;
		return createImage(location, size, style);
	}
	
	public static ImageView createIcon(final String name, final int size)
	{
		final String location = ResourcePaths.IMAGES_PATH + name + IMG_EXT;
		return createImage(location, size);
	}
	
	public static ImageView createIcon(final String name)
	{
		final String location = ResourcePaths.IMAGES_PATH + name + IMG_EXT;
		return createImage(location);
	}
	
	public static String getLocationFromResource(final String location)
	{
		return ImageUtils.class.getResource(location).toString();
	}
	
	private static ImageView createImage(final String iconLocation)
	{
		final ImageView icon = new ImageView(iconLocation);
		return icon;
	}
	
	private static ImageView createImage(final String iconLocation, final int size)
	{
		final ImageView icon = new ImageView(iconLocation);
		icon.setFitHeight(size);
		icon.setFitWidth(size);
		return icon;
	}
	
	private static ImageView createImage(final String iconLocation, final int size, final String style)
	{
		final ImageView icon = new ImageView(iconLocation);
		icon.setFitHeight(size);
		icon.setFitWidth(size);
		icon.getStyleClass().add(style);
		return icon;
	}
}
