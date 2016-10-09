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
package pl.baczkowicz.mqttspy.ui.stats;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.configuration.MqttConfigurationManager;
import pl.baczkowicz.mqttspy.stats.generated.MqttSpyStats;
import pl.baczkowicz.spy.exceptions.XMLException;
import pl.baczkowicz.spy.ui.stats.SpyStats;
import pl.baczkowicz.spy.ui.stats.StatsIO;
import pl.baczkowicz.spy.xml.XMLParser;

@SuppressWarnings({"unchecked", "rawtypes"})
public class MqttStatsFileIO implements StatsIO
{
	private final static Logger logger = LoggerFactory.getLogger(MqttStatsFileIO.class);
	
	public static final String PACKAGE = "pl.baczkowicz.mqttspy.stats.generated";
	
	public static final String SCHEMA = "/mqtt-spy-stats.xsd";
	
	private static final String STATS_FILENAME = "mqtt-spy-stats.xml";

	private final XMLParser parser;

	private File statsFile;

	public MqttSpyStats stats;

	public MqttStatsFileIO() throws XMLException
	{
		this.parser = new XMLParser(PACKAGE, SCHEMA);
		
		statsFile = new File(MqttConfigurationManager.getDefaultHomeDirectory() + STATS_FILENAME);
	}
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.mqttspy.stats.StatsIO#loadStats()
	 */
	@Override
	public SpyStats loadStats()
	{
		try
		{
			stats = (MqttSpyStats) parser.loadFromFile(statsFile);
			return getStats(stats);
		}
		catch (XMLException e)
		{
			logger.error("Cannot process the statistics file at " + statsFile.getAbsolutePath(), e);
		}
		catch (FileNotFoundException e)
		{
			logger.error("Cannot read the statistics file from " + statsFile.getAbsolutePath(), e);
		}
		
		// If reading the stats failed...
		final Random random = new Random();
		
		stats = new MqttSpyStats();
		stats.setID(random.nextLong());
		
		final GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(new Date());
		try 
		{
		    stats.setStartDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(gc));
		} 
		catch (DatatypeConfigurationException e) 
		{
		    logger.error("Cannot create date for stats", e);
		}

		return getStats(stats);
	}
	
	public static SpyStats getStats(final MqttSpyStats stats)
	{
		return new SpyStats(stats.getID(), stats.getStartDate().toGregorianCalendar().getTime(), 
				stats.getConnections(), stats.getSubscriptions(), 
				stats.getMessagesPublished(), stats.getMessagesReceived());
	}
	
	/* (non-Javadoc)
	 * @see pl.baczkowicz.mqttspy.stats.StatsIO#saveStats(pl.baczkowicz.spy.ui.stats.SpyStats)
	 */
	@Override
	public boolean saveStats(final SpyStats stats)
	{
		try
		{
			// Create the mqtt-spy home directory if it doesn't exit
			new File(MqttConfigurationManager.getDefaultHomeDirectory()).mkdirs();
			
			if(!statsFile.exists()) 
			{
				statsFile.createNewFile();
			}
			// This is in case the previous version of mqtt-spy create a directory with the same name as the stats file
			else if (statsFile.isDirectory())
			{
				statsFile.delete();
				statsFile.createNewFile();
			}
			
			// Turn SpyStats into MqttSpyStats
			final MqttSpyStats mqttSpyStats = new MqttSpyStats();
			mqttSpyStats.setConnections(stats.getConnections());
			mqttSpyStats.setID(stats.getId());
			mqttSpyStats.setMessagesPublished(stats.getMessagesPublished());
			mqttSpyStats.setMessagesReceived(stats.getMessagesReceived());
			mqttSpyStats.setSubscriptions(stats.getSubscriptions());
			
			// TODO: turn this into a function
			final GregorianCalendar gc = new GregorianCalendar();
			gc.setTime(stats.getStartDate());
			try 
			{
				mqttSpyStats.setStartDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(gc));
			} 
			catch (DatatypeConfigurationException e) 
			{
			    logger.error("Cannot create date for stats", e);
			}
			
			parser.saveToFile(statsFile, 
					new JAXBElement(new QName("http://baczkowicz.pl/mqtt-spy-stats", "MqttSpyStats"), MqttSpyStats.class, mqttSpyStats));
			return true;
		}
		catch (XMLException | IOException e)
		{
			logger.error("Cannot save the statistics file - " + statsFile.getAbsolutePath(), e);
		}
		
		return false;
	}
}
