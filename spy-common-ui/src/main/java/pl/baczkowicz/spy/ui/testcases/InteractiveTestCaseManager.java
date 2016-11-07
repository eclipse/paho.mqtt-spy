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
package pl.baczkowicz.spy.ui.testcases;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import pl.baczkowicz.spy.scripts.BaseScriptManagerInterface;
import pl.baczkowicz.spy.scripts.ScriptRunningState;
import pl.baczkowicz.spy.testcases.TestCase;
import pl.baczkowicz.spy.testcases.TestCaseManager;
import pl.baczkowicz.spy.testcases.TestCaseStatus;
import pl.baczkowicz.spy.testcases.TestCaseStep;
import pl.baczkowicz.spy.ui.controllers.TestCaseExecutionController;
import pl.baczkowicz.spy.ui.controllers.TestCasesExecutionController;
import pl.baczkowicz.spy.ui.properties.TestCaseProperties;
import pl.baczkowicz.spy.utils.ThreadingUtils;

public class InteractiveTestCaseManager extends TestCaseManager
{	
	private TestCaseExecutionController testCaseExecutionController;

	private List<TestCaseProperties> testCasesProperties = new ArrayList<>();
	
	private List<TestCaseProperties> enqueuedtestCases = new ArrayList<>();

	private TestCasesExecutionController testCasesExecutionController;
		
	public InteractiveTestCaseManager(final BaseScriptManagerInterface scriptManager, final TestCasesExecutionController testCasesExecutionController, final TestCaseExecutionController testCaseExecutionController)	
	{
		super(scriptManager);
		this.testCaseExecutionController = testCaseExecutionController;
		this.testCasesExecutionController = testCasesExecutionController;
	}
	
	public void loadTestCases(final String testCaseLocation)
	{
		testCasesProperties.clear();
		super.loadTestCases(testCaseLocation);
		
		for (final TestCase testCase : getTestCases())
		{
			final TestCaseProperties testCaseProperties = new TestCaseProperties(testCase); 
			testCasesProperties.add(testCaseProperties);
			testCase.setObserver(testCaseProperties);
		}		
		
		new Thread(new Runnable()
		{			
			@Override
			public void run()
			{
				ThreadingUtils.logThreadStarting("runEnqueuedTestCases");
								
				// TODO: there must be a way to stop this
				while (true)
				{
					try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException e)
					{
						break;
					}
					
					if (enqueuedtestCases.size() > 0 && running == 0)
					{
						runTestCase(enqueuedtestCases.remove(0));
					}
				}
				
				ThreadingUtils.logThreadEnding();
			}
		}).start();
	}
	
	public void runTestCase(final TestCaseProperties selectedTestCase)
	{	
		// Prepare
		super.getOptions().setAutoExport(testCaseExecutionController.isAutoExportEnabled());
		final TestCase testCase = selectedTestCase.getScript();
		
		// Run
		testCase.setStatusAndNotify(ScriptRunningState.RUNNING);
		testCase.setTestCaseStatus(TestCaseStatus.IN_PROGRESS);
		
		// Clear last run for this test case
		for (final TestCaseStep step : testCase.getSteps())
		{
			step.setStatus(TestCaseStatus.NOT_RUN);
			step.setExecutionInfo("");
		}
		
		new Thread(new Runnable()
		{			
			@Override
			public void run()
			{
				runAllTestCaseMethods(testCase);	
				
				Platform.runLater(new Runnable()
				{							
					@Override
					public void run()
					{
						testCaseExecutionController.refreshState();
						testCasesExecutionController.refreshInfo();
					}
				});
			}
		}).start();		
	}

	public void stopTestCase(final TestCaseProperties testCaseProperties)
	{
		stopTestCase(testCaseProperties.getScript());
	}

	public void enqueueAllTestCases()
	{
		enqueuedtestCases.addAll(testCasesProperties);
	}

	public void enqueueTestCase(final TestCaseProperties testCaseProperties)
	{
		enqueuedtestCases.add(testCaseProperties);
	}

	public void enqueueAllNotRun()
	{
		for (final TestCaseProperties testCase : getTestCasesProperties())
		{
			if (testCase.statusProperty().getValue().equals(TestCaseStatus.NOT_RUN))
			{
				enqueuedtestCases.add(testCase);
			}			
		}
	}

	public void enqueueAllFailed()
	{
		for (final TestCaseProperties testCase : getTestCasesProperties())
		{
			if (testCase.statusProperty().getValue().equals(TestCaseStatus.FAILED))
			{
				enqueuedtestCases.add(testCase);
			}			
		}
	}

	public void clearEnqueued()
	{
		enqueuedtestCases.clear();		
	}

	public int getEnqueuedCount()
	{
		return enqueuedtestCases.size();
	}

	public List<TestCaseProperties> getTestCasesProperties()
	{
		return testCasesProperties;
	}
}
