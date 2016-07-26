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

public class TestCaseStepResult
{
	private TestCaseStatus status;

	private String info;

	public TestCaseStepResult(final TestCaseStatus status, final String info)
	{
		this.status = status;
		this.info = info;
	}
	
	/**
	 * @return the status
	 */
	public TestCaseStatus getStatus()
	{
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(TestCaseStatus status)
	{
		this.status = status;
	}

	/**
	 * @return the info
	 */
	public String getInfo()
	{
		return info;
	}

	/**
	 * @param info the info to set
	 */
	public void setInfo(String info)
	{
		this.info = info;
	}

	@Override
	public String toString()
	{
		return "TestCaseStepResult [status=" + status + ", info=" + info + "]";
	}
}
