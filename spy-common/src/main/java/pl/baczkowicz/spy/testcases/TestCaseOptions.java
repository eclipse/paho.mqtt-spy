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
package pl.baczkowicz.spy.testcases;


public class TestCaseOptions
{
	public static final int DEFAULT_STEP_INTERVAL = 1000;
	
	private boolean autoExport;
	
	private long stepInterval;
	
	private boolean recordRepeatedSteps;
	
	public TestCaseOptions()
	{
		this.autoExport = true;
		this.stepInterval = DEFAULT_STEP_INTERVAL;
		this.recordRepeatedSteps = true;
	}

	public boolean isAutoExport()
	{
		return autoExport;
	}

	public long getStepInterval()
	{
		return stepInterval;
	}
	
	public void setAutoExport(final boolean autoExport)
	{
		this.autoExport = autoExport;
	}
	
	public void setStepInterval(final long interval)
	{
		this.stepInterval = interval;
	}

	/**
	 * @return the recordRepeatedSteps
	 */
	public boolean isRecordRepeatedSteps()
	{
		return recordRepeatedSteps;
	}

	/**
	 * @param recordRepeatedSteps the recordRepeatedSteps to set
	 */
	public void setRecordRepeatedSteps(boolean recordRepeatedSteps)
	{
		this.recordRepeatedSteps = recordRepeatedSteps;
	}
}
