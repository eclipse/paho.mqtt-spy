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
package pl.baczkowicz.spy.ui.configuration;

import java.io.File;
import java.util.List;

import pl.baczkowicz.spy.common.generated.FormatterDetails;
import pl.baczkowicz.spy.configuration.PropertyFileLoader;
import pl.baczkowicz.spy.connectivity.BaseSubscription;
import pl.baczkowicz.spy.ui.panes.SpyPerspective;
import pl.baczkowicz.spy.ui.properties.ModifiableConnection;

public interface IConfigurationManager
{
	ConfiguredConnectionGroupDetails getRootGroup();

	String generateConnectionGroupId();

	boolean saveConfiguration();

	boolean isConfigurationWritable();

	List<ConfiguredConnectionGroupDetails> getConnectionGrops();

	List<? extends ModifiableConnection> getConnections();

	List<? extends ModifiableConnection> getConnections(ConfiguredConnectionGroupDetails group);
	
	List<FormatterDetails> getFormatters();
	
	boolean removeFormatter(final FormatterDetails formatter);
	
	int countFormatter(final FormatterDetails formatter);

	PropertyFileLoader getDefaultPropertyFile();

	PropertyFileLoader getUiPropertyFile();

	void initialiseConfiguration();

	boolean loadConfiguration(File file);

	File getLoadedConfigurationFile();

	boolean isConfigurationReadOnly();

	List<ConfiguredConnectionGroupDetails> getOrderedGroups();

	void saveUiProperties(double lastWidth, double lastHeight, boolean maximized, SpyPerspective perspective, boolean selected);

	void updateSubscriptionConfiguration(String id, BaseSubscription subscription);

	void deleteSubscriptionConfiguration(String id, BaseSubscription subscription);
}
