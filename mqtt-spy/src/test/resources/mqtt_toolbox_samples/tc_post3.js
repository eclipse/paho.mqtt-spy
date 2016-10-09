var TestCaseStepResult = Java.type("pl.baczkowicz.spy.testcases.TestCaseStepResult");
var TestCaseStatus = Java.type("pl.baczkowicz.spy.testcases.TestCaseStatus");

var getInfo = function () 
{
    var TestCaseInfo = Java.type("pl.baczkowicz.spy.testcases.TestCaseInfo");
	var info = new TestCaseInfo();
	info.setName("Pub/sub test");
	info.getSteps().add("Subscribe to test topic");
	info.getSteps().add("Publish a sample message");
	info.getSteps().add("Verify if received the correct message");

	return info;
};

var step1 = function ()
{
	// Subscribe to messages
	mqttspy.subscribe("testcase/#", 0);
	return new TestCaseStepResult(TestCaseStatus.ACTIONED, "Subscribed");
};

var step2 = function ()
{
	mqttspy.publish("testcase/step2", "sample message 1", 0, false);
	return new TestCaseStepResult(TestCaseStatus.ACTIONED, "Message published");
};

var step3 = function ()
{
	// Wait up to a second to received the expected message
	java.lang.Thread.sleep(1000);
	
	// Check if received
	var messages = mqttspy.getMessages("testcase/#");
	
	var lastMessage = messages.size() > 0 ? messages.get(0).getPayload() : "";
	
	if ("sample message 1".equals(lastMessage))
	{
		return new TestCaseStepResult(TestCaseStatus.PASSED, "Correct message received");
	}
	
	return new TestCaseStepResult(TestCaseStatus.FAILED, "Incorrect message received");
};

