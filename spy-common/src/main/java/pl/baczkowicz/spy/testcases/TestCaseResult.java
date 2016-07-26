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
