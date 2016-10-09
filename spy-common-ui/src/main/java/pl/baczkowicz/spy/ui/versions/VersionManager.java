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
package pl.baczkowicz.spy.ui.versions;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import pl.baczkowicz.spy.configuration.BasePropertyNames;
import pl.baczkowicz.spy.configuration.PropertyFileLoader;
import pl.baczkowicz.spy.exceptions.XMLException;
import pl.baczkowicz.spy.ui.controlpanel.ItemStatus;
import pl.baczkowicz.spy.ui.generated.versions.ReleaseStatus;
import pl.baczkowicz.spy.ui.generated.versions.SpyVersions;
import pl.baczkowicz.spy.ui.properties.VersionInfoProperties;
import pl.baczkowicz.spy.xml.XMLParser;

/**
 * Manages loading of the version information.
 */
public class VersionManager extends XMLParser
{
	/** Diagnostic logger. */
	// private final static Logger logger = LoggerFactory.getLogger(VersionManager.class);
	
	/** Packages where the JAXB-generated classes are store. */
	private static final String PACKAGE = "pl.baczkowicz.spy.ui.generated.versions";
	
	/** Schema location. */
	private static final String SCHEMA = "/spy-versions.xsd";

	/** Used for reading the version URL property. */
	private PropertyFileLoader propertyLoader;
	
	/** The version information retrieved from the URL. */
	private SpyVersions versions;
	
	private boolean loading = false;

	/** Current release. */
	private String currentRelease;

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
					
		this.versions = new SpyVersions();
		this.currentRelease = propertyLoader.getFullVersionNumber();
	}
	
	/**
	 * Loads version information from the URL.
	 * 
	 * @return Object representing the version information
	 * 
	 * @throws XMLException Thrown when cannot load the version information
	 */
	public SpyVersions loadVersions() throws XMLException
	{
		setLoading(true);
		final String urlString = propertyLoader.getProperty(BasePropertyNames.VERSION_INFO_URL) + "?current=" + currentRelease; 
		
		try
		{		
			final URL url = new URL(urlString);

			final String xml = IOUtils.toString(url.openStream());
			
			// logger.debug("Retrieved XML: {}", xml);
			
			versions = (SpyVersions) loadFromString(xml);			
		}
		catch (IOException | NullPointerException e)
		{
			throw new XMLException("Cannot read version info from " + urlString, e);
		}
		
		setLoading(false);
				
		return versions;
	}
	
	/**
	 * Gets the version information object.
	 * 
	 * @return Object representing the version information
	 */
	public SpyVersions getVersions()
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
	
	public VersionInfoProperties getVersionInfoProperties(final PropertyFileLoader propertyFileLoader)
	{
		final VersionInfoProperties properties = new VersionInfoProperties();
		
		if (getVersions() != null)
		{
			boolean versionFound = false;
			
			for (final ReleaseStatus release : getVersions().getReleaseStatuses().getReleaseStatus())
			{
				if (VersionManager.isInRange(currentRelease, release))
				{					
					properties.setStatus(VersionManager.convertVersionStatus(release));
					properties.setTitle(replaceTokens(release.getUpdateTitle(), propertyFileLoader));
					properties.setDetails(replaceTokens(release.getUpdateDetails(), propertyFileLoader));
					versionFound = true;
					break;
				}
			}
			
			if (!versionFound)
			{
				properties.setStatus(ItemStatus.INFO);
				properties.setTitle("Couldn't find any information about your version - please check manually.");
				properties.setDetails("Your version is " + propertyFileLoader.getFullVersionName() + ".");
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
	
	public static String replaceTokens(final String value, final PropertyFileLoader propertyFileLoader)
	{
		return value.replace("[newline]", System.lineSeparator()).replace("[version]", propertyFileLoader.getFullVersionName());
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
