var TestCaseStepResult = Java.type("pl.baczkowicz.spy.testcases.TestCaseStepResult");
var TestCaseStatus = Java.type("pl.baczkowicz.spy.testcases.TestCaseStatus");

// This is to demonstrate updating a global variable
var count = 0;

var getInfo = function () 
{
    var TestCaseInfo = Java.type("pl.baczkowicz.spy.testcases.TestCaseInfo");
	var info = new TestCaseInfo();
	info.setName("Sample test case 1");
	info.getSteps().add("Step 1");
	info.getSteps().add("Step 2");
	info.getSteps().add("Step 3");
	info.getSteps().add("Step 4");
	
	return info;
};

var before = function()
{
	// Subscribe to messages
	mqttspy.subscribe("/testcase/1/#", 0);
}

var step1 = function ()
{
	var messageCount = mqttspy.getMessages("/testcase/1/#").size();
	
	// Make sure any variables are reset if the test case is re-run
	count = 1;
	return new TestCaseStepResult(TestCaseStatus.PASSED, "All fine in step 1 " + "[" + count + "] msgs = " + messageCount);
};

var step2 = function ()
{
	mqttspy.publish("/testcase/1/step2", "sample message " + count, 0, false);
	var messageCount = mqttspy.getMessages("/testcase/1/#").size();
	var lastMessage = "";
	
	if (messageCount > 0)
	{
		lastMessage = mqttspy.getMessages("/testcase/1/#").get(0).getPayload();
	}
	
	count = count + 1;	
	if (count < 5 || messageCount < 10)
	{		
		return new TestCaseStepResult(TestCaseStatus.IN_PROGRESS, "Still waiting... " + "[" + count + "]; msgs = " + messageCount + "; last = " + lastMessage);
	}
	return new TestCaseStepResult(TestCaseStatus.ACTIONED, "All fine in step 2 " + "[" + count + "]; msgs = " + messageCount);
};

var step3 = function ()
{
	count = count + 1;
	return new TestCaseStepResult(TestCaseStatus.SKIPPED, "Step 3 skipped " + "[" + count + "]");
};

var step4 = function ()
{
	count = count + 1;
	return new TestCaseStepResult(TestCaseStatus.FAILED, "Step 4 failed " + "[" + count + "]");
};

var after = function()
{
	// Unsubscribe
	mqttspy.unsubscribe("/testcase/1/#");
}


