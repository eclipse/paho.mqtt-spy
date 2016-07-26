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
package pl.baczkowicz.mqttspy.versions;

import java.io.IOException;
import java.net.URL;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.ui.controlpanel.ItemStatus;
import pl.baczkowicz.mqttspy.ui.properties.VersionInfoProperties;
import pl.baczkowicz.mqttspy.versions.generated.MqttSpyVersions;
import pl.baczkowicz.mqttspy.versions.generated.ReleaseStatus;
import pl.baczkowicz.spy.configuration.PropertyFileLoader;
import pl.baczkowicz.spy.exceptions.XMLException;
import pl.baczkowicz.spy.xml.XMLParser;

/**
 * Manages loading of the version information.
 */
public class VersionManager extends XMLParser
{
	/** Packages where the JAXB-generated classes are store. */
	private static final String PACKAGE = "pl.baczkowicz.mqttspy.versions.generated";
	
	/** Schema location. */
	private static final String SCHEMA = "/mqtt-spy-versions.xsd";

	/** Used for reading the version URL property. */
	private PropertyFileLoader propertyLoader;
	
	/** The version information retrieved from the URL. */
	private MqttSpyVersions versions;
	
	private boolean loading = false;

	/**
	 * Creates the VersionManager.
	 * 
	 * @param propertyLoader Used for reading the version URL property
	 * 
	 * @throws XMLException Thrown when cannot create the VersionManager
	 */
	public VersionManager(final PropertyFileLoader propertyLoader) throws XMLException
	{
		super(PACKAGE, SCHEMA);
		this.propertyLoader = propertyLoader;
					
		this.versions = new MqttSpyVersions();
	}
	
	/**
	 * Loads version information from the URL.
	 * 
	 * @return Object representing the version information
	 * 
	 * @throws XMLException Thrown when cannot load the version information
	 */
	public MqttSpyVersions loadVersions() throws XMLException
	{
		setLoading(true);
		
		try
		{
			final URL url = new URL(propertyLoader.getProperty(ConfigurationManager.VERSION_INFO_URL));

			versions = (MqttSpyVersions) loadFromInputStream(url.openStream());			
		}
		catch (IOException | NullPointerException e)
		{
			throw new XMLException("Cannot read version info from " + propertyLoader.getProperty(ConfigurationManager.VERSION_INFO_URL), e);
		}
		
		setLoading(false);
				
		return versions;
	}
	
	/**
	 * Gets the version information object.
	 * 
	 * @return Object representing the version information
	 */
	public MqttSpyVersions getVersions()
	{
		return versions;
	}

	/**
	 * Checks whether the current release is within the range of the given release to check.
	 * 
	 * @param currentRelease The current release to check
	 * @param release The release to check against
	 * 
	 * @return True if the current release is within the range of the given release.
	 * 
	 */
	public static boolean isInRange(final String currentRelease, final ReleaseStatus release)
	{
		if ((new DefaultArtifactVersion(currentRelease).compareTo(new DefaultArtifactVersion(release.getFromVersion())) >= 0)
			&& (new DefaultArtifactVersion(currentRelease).compareTo(new DefaultArtifactVersion(release.getToVersion())) <= 0))
		{
			return true;		
		}
		
		return false;
	}
	
	/**
	 * Gets item status enum for the given release.
	 * 
	 * @param release The release to check
	 * 
	 * @return ItemStatus enum based on the UpdateStatus field of the release
	 */
	public static ItemStatus convertVersionStatus(final ReleaseStatus release)
	{
		switch (release.getUpdateStatus())
		{
			case CRITICAL:
				return ItemStatus.ERROR;
			case UPDATE_RECOMMENDED:
				return ItemStatus.WARN;
			case NEW_AVAILABLE:
				return ItemStatus.INFO;
			case ON_LATEST:
				return ItemStatus.OK;
			default:
				return ItemStatus.ERROR;		
		}
	}
	
	public VersionInfoProperties getVersionInfoProperties(final ConfigurationManager configurationManager)
	{
		final VersionInfoProperties properties = new VersionInfoProperties();
		
		if (getVersions() != null)
		{
			boolean versionFound = false;
			
			for (final ReleaseStatus release : getVersions().getReleaseStatuses().getReleaseStatus())
			{
				if (VersionManager.isInRange(configurationManager.getDefaultPropertyFile().getFullVersionNumber(), release))
				{					
					properties.setStatus(VersionManager.convertVersionStatus(release));
					properties.setTitle(replaceTokens(release.getUpdateTitle(), configurationManager));
					properties.setDetails(replaceTokens(release.getUpdateDetails(), configurationManager));
					versionFound = true;
					break;
				}
			}
			
			if (!versionFound)
			{
				properties.setStatus(ItemStatus.INFO);
				properties.setTitle("Couldn't find any information about your version - please check manually.");
				properties.setDetails("Your version is " + configurationManager.getDefaultPropertyFile().getFullVersionName() + ".");
			}
		}	
		else
		{
			// Set the default state
			properties.setStatus(ItemStatus.WARN);
			properties.setTitle("Cannot check for updates - is your internet connection up?");
			properties.setDetails("Click here to go to the download page for mqtt-spy.");
		}
		
		
		
		return properties;
	}
	
	public static String replaceTokens(final String value, final ConfigurationManager configurationManager)
	{
		return value.replace("[newline]", System.lineSeparator()).replace("[version]", configurationManager.getDefaultPropertyFile().getFullVersionName());
	}

	/**
	 * @return the loading
	 */
	public boolean isLoading()
	{
		return loading;
	}

	/**
	 * @param loading the loading to set
	 */
	public void setLoading(boolean loading)
	{
		this.loading = loading;
	}
}
