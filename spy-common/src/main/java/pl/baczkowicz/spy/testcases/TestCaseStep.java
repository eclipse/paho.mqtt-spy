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

import pl.baczkowicz.spy.scripts.ScriptChangeObserver;
import pl.baczkowicz.spy.testcases.TestCaseStatus;

/**
 * This represents a test step that is part of a test case.
 */
public class TestCaseStep
{
	/** The step number. */
	private String stepNumber;
	
	/** Description of the step. */	
	private String description;
	
	/** Step status. */	
	private TestCaseStatus status;
	
	/** Information about the execution. */	
	private String executionInfo;

	private ScriptChangeObserver observer;
		
	/**
	 * Creates a TestCaseStepProperties with the given parameters.
	 * 
	 * @param stepNumber
	 * @param description
	 * @param status
	 * @param info
	 */
	public TestCaseStep(final String stepNumber, final String description, final TestCaseStatus status, final String info)
	{
		this.stepNumber = stepNumber;
		this.description = description;
		this.status = status;
		this.executionInfo = info;
	}

	/**
	 * Copying constructor.
	 * 
	 * @param step Step to copy
	 */
	public TestCaseStep(TestCaseStep step)
	{
		this(step.getStepNumber(), step.getDescription(), step.getStatus(), step.getExecutionInfo());
	}

	public String getStepNumber()
	{
		return stepNumber;
	}

	public void setStepNumber(String stepNumber)
	{
		this.stepNumber = stepNumber;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public TestCaseStatus getStatus()
	{
		return status;
	}

	public void setStatus(TestCaseStatus status)
	{
		this.status = status;
		nofityChange();
	}

	public String getExecutionInfo()
	{
		return executionInfo;
	}

	public void setExecutionInfo(String executionInfo)
	{
		this.executionInfo = executionInfo;
		nofityChange();
	}
	
	/**
	 * Notifies an observer a change has occurred.
	 */
	protected void nofityChange()
	{
		if (observer != null)
		{
			observer.onChange();
		}
	}
	
	/**
	 * Sets the observer of the object.
	 * 
	 * @param observer the observer to set
	 */
	public void setObserver(final ScriptChangeObserver observer)
	{
		this.observer = observer;
	}

	@Override
	public String toString()
	{
		return "TestCaseStep [stepNumber=" + stepNumber + ", description=" + description + ", status=" + status	+ ", executionInfo=" + executionInfo + "]";
	}	
}
