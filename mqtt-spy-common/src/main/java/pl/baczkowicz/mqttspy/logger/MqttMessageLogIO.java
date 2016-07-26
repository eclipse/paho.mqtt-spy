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
package pl.baczkowicz.mqttspy.logger;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.messages.BaseMqttMessage;
import pl.baczkowicz.spy.utils.ThreadingUtils;
import pl.baczkowicz.spy.utils.TimeUtils;

/**
 * Implementation of the interface between a script and the messageLog object.
 */
public class MqttMessageLogIO implements IMqttMessageLogIO, Runnable
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(MqttMessageLogIO.class);
	
	/** Messages. */
	private List<BaseMqttMessage> messages;	

	/** Current replay time (as in the message log). */
	private long replayTime;

	/** Timestamp of the last time checker run. */
	private long lastUpdated;
	
	/** Flag indicating whether the time checker is running. */
	private boolean running = false;

	/** The current running speed. */
	private double speed = 1;
	
	@Override
	public int readFromFile(final String logLocation)
	{
		try
		{
			messages = MqttMessageLogParserUtils.readAndConvertMessageLog(new File(logLocation));
						
			return messages.size();
		}
		catch (Exception e)
		{
			logger.error("Cannot read message log at " + logLocation, e);
			return 0;
		}
	}
	
	@Override
	public void start()
	{
		if (messages != null)
		{
			replayTime = messages.get(0).getDate().getTime();
			lastUpdated = TimeUtils.getMonotonicTime();
			running = true;
			
			new Thread(this).start();
		}		
	}
	
	@Override
	public void stop()
	{
		running = false;		
	}

	@Override
	public void setSpeed(double newSpeed)
	{
		if (newSpeed > 1)
		{
			if (this.speed <= 1)
			{
				logger.info("Warp enabled. Changing replay speed from {} to {}", this.speed, newSpeed);
			}
			else
			{
				logger.info("Warp still on. Changing replay speed from {} to {}", this.speed, newSpeed);
			}		
		}
		else if (newSpeed == 1)
		{
			logger.info("Back to normal. Changing replay speed from {} to {}", this.speed, newSpeed);
		}
		else
		{
			logger.info("Tea time! Changing replay speed from {} to {}", this.speed, newSpeed);
		}
		
		this.speed = newSpeed;
	}

	@Override
	public boolean isReadyToPublish(final int messageIndex)
	{
		if (messages != null && messages.size() > messageIndex)
		{
			if (replayTime > messages.get(messageIndex).getDate().getTime())
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public BaseMqttMessage getMessage(final int messageIndex)
	{
		if (messages != null && messages.size() > messageIndex)
		{
			return messages.get(messageIndex);
		}
		
		return null;
	}
	
	@Override
	public void run()
	{
		ThreadingUtils.logThreadStarting("Message Log IO");
		
		while (running)
		{
			final long now = TimeUtils.getMonotonicTime();
			
			if (now > lastUpdated)
			{
				final long sinceLastUpdated = now - lastUpdated;				
				final double increase = sinceLastUpdated * speed;
				
				replayTime =  replayTime + (long) increase;
				lastUpdated = now;
			}
			
			if (ThreadingUtils.sleep(10))			
			{
				break;
			}
		}	
		stop();
		
		ThreadingUtils.logThreadEnding();
	}
	
	@Override
	public int getMessageCount()
	{
		if (messages == null)
		{
			return 0;
		}
		
		return messages.size();
	}
}
