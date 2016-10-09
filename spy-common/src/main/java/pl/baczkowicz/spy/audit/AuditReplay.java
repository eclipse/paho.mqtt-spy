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

package pl.baczkowicz.spy.audit;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.messages.BaseMessage;
import pl.baczkowicz.spy.utils.ThreadingUtils;
import pl.baczkowicz.spy.utils.TimeUtils;

public abstract class AuditReplay implements IAuditReplayIO, Runnable
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(AuditReplay.class);
	
	protected StreamedAuditReader auditReader;
	
	/** Current replay time (as in the message log). */
	private long replayTime;

	/** Timestamp of the last time checker run. */
	private long lastUpdated;
	
	/** Flag indicating whether the time checker is running. */
	private boolean running = false;

	/** The current running speed. */
	private double speed = 1;

	@Override
	public long readFromFile(final String logLocation)
	{
		auditReader = new StreamedAuditReader();
		
		try
		{
			auditReader.openAuditFile(new File(logLocation));
			logger.info("Found {} messages in {}", auditReader.getCount(), logLocation);
			
			return auditReader.getCount();
		}
		catch (Exception e)
		{
			logger.error("Cannot read message audit log at " + logLocation, e);
			return 0;
		}		
	}

	@Override
	public long getMessageCount()
	{
		if (auditReader == null)
		{
			return 0;
		}
		
		return auditReader.getCount();
	}
	
	@Override
	public void start()
	{
		if (auditReader.getCount() > 0)
		{
			replayTime = getMessageTime();
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
	public boolean isReadyToPublish()
	{
		if (replayTime > getMessageTime())
		{
			return true;
		}
		
		return false;
	}

	@Override
	public BaseMessage getNextMessage()
	{
		return getMessage();
	}
	
	@Override
	public void run()
	{
		ThreadingUtils.logThreadStarting("Audit Replay IO");
		
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
	
	abstract protected long getMessageTime();

	abstract protected BaseMessage getMessage();
}
