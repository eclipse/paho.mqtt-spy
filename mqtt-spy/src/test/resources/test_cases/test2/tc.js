var TestCaseStepResult = Java.type("pl.baczkowicz.spy.testcases.TestCaseStepResult");
var TestCaseStatus = Java.type("pl.baczkowicz.spy.testcases.TestCaseStatus");

var getInfo = function () 
{
    var TestCaseInfo = Java.type("pl.baczkowicz.spy.testcases.TestCaseInfo");
	var info = new TestCaseInfo();
	info.setName("Sample test case 2");
	info.getSteps().add("Step 1");
	info.getSteps().add("Step 2");
	info.getSteps().add("Step 3");

	return info;
};

var step1 = function ()
{
	return new TestCaseStepResult(TestCaseStatus.PASSED, "All fine in step 1");
};

var step2 = function ()
{
	return new TestCaseStepResult(TestCaseStatus.ACTIONED, "All fine in step 2");
};

var step3 = function ()
{
	return new TestCaseStepResult(TestCaseStatus.SKIPPED, "Step 3 skipped");
};

