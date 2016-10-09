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
package pl.baczkowicz.spy.testcases;

import java.util.ArrayList;
import java.util.List;

public class TestCaseResult
{
	private TestCaseInfo info;
	
	private TestCaseStatus result;
	
	private List<TestCaseStep> stepResults = new ArrayList<>();

	public TestCaseStatus getResult()
	{
		return result;
	}

	public void setResult(TestCaseStatus result)
	{
		this.result = result;
	}

	public List<TestCaseStep> getStepResults()
	{
		return stepResults;
	}

	public void setStepResults(List<TestCaseStep> stepResults)
	{
		this.stepResults = stepResults;
	}

	public TestCaseInfo getInfo()
	{
		return info;
	}

	public void setInfo(TestCaseInfo info)
	{
		this.info = info;
	}

	@Override
	public String toString()
	{
		return "TestCaseResult [info=" + info + ", result=" + result + ", stepResults=" + stepResults + "]";
	}
}
