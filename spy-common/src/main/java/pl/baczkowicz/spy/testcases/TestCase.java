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

import java.util.ArrayList;
import java.util.List;

import pl.baczkowicz.spy.scripts.Script;

public class TestCase extends Script
{
	private TestCaseInfo info;
	
	private int currentStep;
	
	private String lastUpdated = "";
	
	private List<TestCaseStep> steps = new ArrayList<>();
	
	private TestCaseStatus testCaseStatus = TestCaseStatus.NOT_RUN;
	
	private TestCaseResult testCaseResult = new TestCaseResult(); 

	/**
	 * @return the info
	 */
	public TestCaseInfo getInfo()
	{
		return info;
	}

	/**
	 * @param info the info to set
	 */
	public void setInfo(TestCaseInfo info)
	{
		this.info = info;
		// nofityChange();
	}
	
	public List<TestCaseStep> getSteps()
	{
		return this.steps;
	}

	/**
	 * @return the status
	 */
	public TestCaseStatus getTestCaseStatus()
	{
		return testCaseStatus;
	}

	/**
	 * @param status the status to set
	 */
	public void setTestCaseStatus(TestCaseStatus status)
	{
		this.testCaseStatus = status;
		nofityChange();
	}

	/**
	 * @return the currentStep
	 */
	public int getCurrentStep()
	{
		return currentStep;
	}

	/**
	 * @param currentStep the currentStep to set
	 */
	public void setCurrentStep(int currentStep)
	{
		this.currentStep = currentStep;
		nofityChange();
	}

	public String getLastUpdated()
	{
		return lastUpdated;
	}

	public void setLastUpdated(String lastUpdated)
	{
		this.lastUpdated = lastUpdated;
		nofityChange();
	}

	public TestCaseResult getTestCaseResult()
	{
		return testCaseResult;
	}

	public void setTestCaseResult(TestCaseResult testCaseResult)
	{
		this.testCaseResult = testCaseResult;
	}
}
