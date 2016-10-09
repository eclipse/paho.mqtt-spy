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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.common.generated.ScriptDetails;
import pl.baczkowicz.spy.files.FileUtils;
import pl.baczkowicz.spy.scripts.BaseScriptManager;
import pl.baczkowicz.spy.scripts.ScriptRunningState;
import pl.baczkowicz.spy.utils.ThreadingUtils;
import pl.baczkowicz.spy.utils.TimeUtils;

public class TestCaseManager
{		
	public static String GET_INFO_METHOD = "getInfo";
	
	public static SimpleDateFormat testCaseFileSdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
	
	public static SimpleDateFormat testCasesFileSdf = new SimpleDateFormat("yyyyMMdd");

	private final static Logger logger = LoggerFactory.getLogger(TestCaseManager.class);
	
	protected final BaseScriptManager scriptManager;
	
	protected Map<String, TestCase> testCases = new HashMap<>();
			
	protected int running = 0;
	
	private TestCaseOptions options = new TestCaseOptions();

	public TestCaseManager(final BaseScriptManager scriptManager)	
	{
		this.scriptManager = scriptManager;
	}
	
	public TestCase addTestCase(final File scriptFile)
	{
		if (!scriptFile.exists())
		{
			logger.error("Script file " + scriptFile.getPath() + " does not exist");
			// TODO: Throw an exception instead?
			return null;
		}
		
		logger.info("Adding " + scriptFile.getName() + " with parent " + scriptFile.getParent());
		
		final ScriptDetails scriptDetails = new ScriptDetails();					
		scriptDetails.setFile(scriptFile.getAbsolutePath());
		scriptDetails.setRepeat(false);
		
		final TestCase testCase = new TestCase();
				
		scriptManager.createFileBasedScript(testCase, scriptFile, scriptDetails);
		
		try
		{	
			scriptManager.runScript(testCase, false);
			testCase.setInfo((TestCaseInfo) scriptManager.invokeFunction(testCase, GET_INFO_METHOD));
			
			int stepNumber = 1;
			for (final String step : testCase.getInfo().getSteps())
			{
				testCase.getSteps().add(new TestCaseStep(String.valueOf(stepNumber), step, TestCaseStatus.NOT_RUN, ""));
				stepNumber++;
			}
			
			logger.info(testCase.getInfo().getName() + " " + Arrays.asList(testCase.getInfo().getSteps()));
		}
		catch (ScriptException | NoSuchMethodException e)
		{
			logger.error("Cannot read test case", e);
		}
		
		// Override name
		if (testCase.getInfo() != null && testCase.getInfo().getName() != null)
		{
			testCase.setName(testCase.getInfo().getName());
		}
		else
		{
			testCase.setName(scriptFile.getParentFile().getName());
		}
		
		testCases.put(testCase.getScriptId(), testCase);
		return testCase;
	}
	
	public void loadTestCases(final String testCaseLocation)
	{
		final List<File> scripts = FileUtils.getDirectoriesWithFile(testCaseLocation, "tc.*.js");

		for (final File scriptFile : scripts)
		{
			addTestCase(scriptFile);
		}
	}
	
	private TestCaseStepResult runTestCaseSteps(final TestCase testCase)
	{
		TestCaseStepResult lastResult = null;
		
		while (testCase.getCurrentStep() < testCase.getSteps().size() && testCase.getStatus().equals(ScriptRunningState.RUNNING))
		{
			final TestCaseStep step = testCase.getSteps().get(testCase.getCurrentStep());
			
			testCase.setLastUpdated(TimeUtils.DATE_WITH_SECONDS_SDF.format(new Date()));
			step.setStatus(TestCaseStatus.IN_PROGRESS);										
			
			try
			{
				final TestCaseStepResult result = (TestCaseStepResult) scriptManager.invokeFunction(testCase, "step" + step.getStepNumber());
				lastResult = result;
				
				if (result == null)
				{
					continue;
				}
				
				step.setStatus(result.getStatus());
				step.setExecutionInfo(result.getInfo());		
				
				if (TestCaseStatus.IN_PROGRESS.equals(result.getStatus()))
				{
					// Add a copy of the step, as status and exec info might change later
					testCase.getTestCaseResult().getStepResults().add(new TestCaseStep(step));
					
					try
					{
						Thread.sleep(options.getStepInterval());
					}
					catch (InterruptedException e)
					{
						break;
					}	
				}
				// If not in progress any more, move to next
				else
				{			
					testCase.getTestCaseResult().getStepResults().add(step);
					testCase.setCurrentStep(testCase.getCurrentStep() + 1);
				}														
			}
			catch (NoSuchMethodException e)
			{
				step.setStatus(TestCaseStatus.ERROR);
				logger.error("Step execution error for step " + step.getStepNumber(), e);
			}
			catch (ScriptException e)
			{
				step.setStatus(TestCaseStatus.FAILED);
				logger.error("Step execution failure for step " + step.getStepNumber(), e);
			}
		}				
		
		return lastResult;
	}
	
	public void runAllTestCaseMethods(final TestCase testCase)
	{
		running++;				
		testCase.setCurrentStep(0);		
		
		// Before
		if (!scriptManager.invokeBefore(testCase))
		{
			testCase.setStatusAndNotify(ScriptRunningState.FAILED);					
		}
		
		// Test steps
		TestCaseStepResult lastResult = runTestCaseSteps(testCase);

		// After
		if (!scriptManager.invokeAfter(testCase))
		{
			testCase.setStatusAndNotify(ScriptRunningState.FAILED);					
		}
		
		final TestCaseStepResult testCaseStatus = lastResult;
		
		if (testCase.getStatus().equals(ScriptRunningState.STOPPED))
		{
			testCase.setTestCaseStatus(TestCaseStatus.SKIPPED);
		}
		else
		{
			testCase.setTestCaseStatus(testCaseStatus.getStatus());
			testCase.setStatusAndNotify(ScriptRunningState.FINISHED);
		}
		
		testCase.getTestCaseResult().setInfo(testCase.getInfo());
		testCase.getTestCaseResult().setResult(testCase.getTestCaseStatus());
		testCase.setLastUpdated(TimeUtils.DATE_WITH_SECONDS_SDF.format(new Date()));
		
		running--;
		
		logger.info("Test case \"{}\" ended with result: {}", testCase.getName(), testCaseStatus.getStatus());
		
		if (options.isAutoExport())
		{
			final String parentDir = testCase.getScriptFile().getParent() + System.getProperty("file.separator");
			exportTestCaseResultAsCSV(testCase, new File(parentDir + "result_" + testCaseFileSdf.format(new Date()) + "_" + testCaseStatus.getStatus() + ".csv"));
		}
	}
	
	public void runTestCase(final TestCase testCase, final Map<String, Object> args)
	{				
		testCase.setStatusAndNotify(ScriptRunningState.RUNNING);
		testCase.setTestCaseStatus(TestCaseStatus.IN_PROGRESS);
		
		// Set test case args
		if (args != null)
		{
			scriptManager.setVariable(testCase, "args", args);
		}
		
		// Clear last run for this test case
		for (final TestCaseStep step : testCase.getSteps())
		{
			step.setStatus(TestCaseStatus.NOT_RUN);
			step.setExecutionInfo("");
		}
		
		runAllTestCaseMethods(testCase);	
	}
	
	public TestCaseResult addAndRunTestCase(final String testCaseLocation, final Map<String, Object> args)	
	{
		final TestCase testCase = addTestCase(new File(testCaseLocation));
		// TODO: add protection against missing/invalid files
		runTestCase(testCase, args);
		return testCase.getTestCaseResult();
	}	
	
	public void runAllTestCases()
	{
		running = testCases.size();
		
		new Thread(new Runnable()
		{			
			@Override
			public void run()
			{
				ThreadingUtils.logThreadStarting("runAllTestCases");

				for (final TestCase testCase : testCases.values())
				{
					runTestCase(testCase, null);
					running--;
				}
				
				ThreadingUtils.logThreadEnding();
			}
		}).start();		
	}

	public void stopTestCase(final TestCase testCase)
	{
		testCase.setStatusAndNotify(ScriptRunningState.STOPPED);		
		
		final TestCaseStep step = testCase.getSteps().get(testCase.getCurrentStep());
		
		step.setStatus(TestCaseStatus.SKIPPED);
	}

	public int getTotalCount()
	{
		return testCases.size();
	}

	public Collection<TestCase> getTestCases()
	{
		return testCases.values();
	}

	// *** Export methods ***
	
	public void exportTestCaseResultAsCSV(final TestCase testCase, final File selectedFile)
	{
		logger.info("Saving test case results to " + selectedFile.getAbsolutePath());
		
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(selectedFile));
			
			out.write(
					//"Time, " + 
					"Step" + ", " + "\"" + 
					"Description" + "\"" + ", " + 
					"Status" + ", " + "\"" + 
					"Info" + "\"");
			out.newLine();
			
			for (TestCaseStep step : testCase.getSteps())
			{
				out.write(
						//step.
						step.getStepNumber() + ", " + "\"" + 
						step.getDescription() + "\"" + ", " + 
						step.getStatus() + ", " + "\"" + 
						step.getExecutionInfo() + "\"");
				out.newLine();
			}
						
			out.close();
		}
		catch (IOException e)
		{
			logger.error("Cannot write to file", e);
		}
	}
	
	public void exportTestCasesResultsAsCSV(final File selectedFile)
	{
		logger.info("Saving test cases results to " + selectedFile.getAbsolutePath());
		
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(selectedFile));
			
			out.write(
					"\"" + "Test case" + "\"" + ", " +
					"\"" + "Last updated" + "\"" + ", " + "\"" +
					"Status");
			out.newLine();
			
			for (TestCase testCase : getTestCases())
			{
				out.write(
						"\"" + testCase.getName() + "\"" + ", " + 
						"\"" + testCase.getLastUpdated() + "\"" + ", " +
						"\"" + testCase.getTestCaseStatus() + "\"" );
				out.newLine();
			}
						
			out.close();
		}
		catch (IOException e)
		{
			logger.error("Cannot write to file", e);
		}
	}
	
	public boolean areTestCasesStillRunning()
	{
		return running > 0;
	}

	public void setOptions(final TestCaseOptions options)
	{
		this.options = options;		
	}
	
	public TestCaseOptions getOptions()
	{
		return this.options;
	}
}
