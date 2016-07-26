function before()
{
	logger.info("Runing the 'before' function...");
}

function onMessage()
{
	if (!receivedMessage.getTopic().equals("reply"))
	{
		mqttspy.publish(
			"reply", "<simpleReply><topic>" + receivedMessage.getTopic() + "</topic>" 
			+ "<payload><![CDATA[" + receivedMessage.getPayload() + "]]></payload>"
			+ "</simpleReply>", 0, false);
		
		receivedMessage.setPayload("<tag>" + receivedMessage.getPayload() + "- modified :)</tag>");
	}
	else
	{
		receivedMessage.setPayload("<replyTag>" + receivedMessage.getPayload() + "- modified :)</replyTag>");
	}
	
	return true;
}
