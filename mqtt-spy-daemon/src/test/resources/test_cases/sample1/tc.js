// Define all types and classes you want to use
var TestCaseStepResult = Java.type("pl.baczkowicz.spy.testcases.TestCaseStepResult");
var TestCaseStatus = Java.type("pl.baczkowicz.spy.testcases.TestCaseStatus");

// Define any global variables here
var count = 0;

// Sample subscription topic
var testSub = "sampleTestCase/#";

// This function provides information about the test case and test steps
var getInfo = function () 
{
    var TestCaseInfo = Java.type("pl.baczkowicz.spy.testcases.TestCaseInfo");
	var info = new TestCaseInfo();
	
	// Name of the test case
	info.setName("Pub/sub test");
	
	// Test steps
	info.getSteps().add("Subscribe to test topic");
	info.getSteps().add("Publish a sample message");
	info.getSteps().add("Verify if received the correct message");

	return info;
};

var step1 = function ()
{
	// Subscribe to messages
	mqttspy.subscribe(testSub, 0);
	
	// Log a diagnostic message
	logger.info("Subscribed!");
	
	// Complete the test step and return its result
	return new TestCaseStepResult(TestCaseStatus.ACTIONED, "Subscribed");
};

var step2 = function ()
{
	// Publish a sample message
	mqttspy.publish("sampleTestCase/step2", "sample message 1", 0, false);
	
	// Log a diagnostic message
	logger.info("Published!");
	
	// Complete the test step and return its result
	return new TestCaseStepResult(TestCaseStatus.ACTIONED, "Message published");
};

var step3 = function ()
{
	// Every time we execute this test step, increase the count
	count++;
	
	// If we've been here more than 5 times and still no messages received, FAIL
	if (count > 5)
	{
		return new TestCaseStepResult(TestCaseStatus.FAILED, "No messages received");
	}
	
	if (mqttspy.getMessages(testSub).size() > 0)
	{	
		// Get the last received message
		var lastTestMessage = mqttspy.getMessages(testSub).get(0).getPayload();
		
		// Check if the message contains the expected payload
		if (lastTestMessage.equals("sample message 1"))
		{
			return new TestCaseStepResult(TestCaseStatus.PASSED, "Correct message received");
		}
		else
		{
			// If the value does not match, log it
			logger.info("Value received = " + lastTestMessage)
			
			// Incorrect message, FAIL
			return new TestCaseStepResult(TestCaseStatus.FAILED, "Incorrect message received");
		}
	}
};
