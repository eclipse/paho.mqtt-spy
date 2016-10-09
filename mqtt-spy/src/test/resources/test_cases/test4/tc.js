var TestCaseStepResult = Java.type("pl.baczkowicz.spy.testcases.TestCaseStepResult");
var TestCaseStatus = Java.type("pl.baczkowicz.spy.testcases.TestCaseStatus");
var Thread = Java.type("java.lang.Thread");

var getInfo = function () 
{
    var TestCaseInfo = Java.type("pl.baczkowicz.spy.testcases.TestCaseInfo");
	var info = new TestCaseInfo();
	info.setName("Pub/sub test for auto-reply");
	info.getSteps().add("Subscribe to test topic");
	info.getSteps().add("Publish a sample message");
	info.getSteps().add("Verify if received the correct message");

	return info;
};

var step1 = function ()
{
	// Subscribe to messages
	mqttspy.subscribe("mqtt-spy/auto-reply", 0);
	
	Thread.sleep(1000);
	
	return new TestCaseStepResult(TestCaseStatus.ACTIONED, "Subscribed");
};

var step2 = function ()
{
	mqttspy.publish("mqtt-spy/hello", "sample message 1", 0, false);
	
	Thread.sleep(1000);
	
	return new TestCaseStepResult(TestCaseStatus.ACTIONED, "Message published");
};

var step3 = function ()
{
	Thread.sleep(1000);
	
	var messages = mqttspy.getMessages("mqtt-spy/auto-reply");
	
	var lastMessage = messages.size() > 0 ? messages.get(0).getPayload() : "";
	
	if (lastMessage.contains("simpleReply"))
	{
		return new TestCaseStepResult(TestCaseStatus.PASSED, "Correct message received");
	}
	
	return new TestCaseStepResult(TestCaseStatus.FAILED, "Incorrect message received");
};

