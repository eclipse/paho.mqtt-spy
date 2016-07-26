function onMessage()
{	
	mqtt.publish(
			"/reply", "<simpleReply><topic>" + receivedMessage.getTopic() + "</topic>" 
			+ "<payload><![CDATA[" + receivedMessage.getPayload() + "]]></payload>"
			+ "</simpleReply>", 0, false);
	
	receivedMessage.setPayload("<tag>" + receivedMessage.getPayload() + "- modified :)</tag>");

	return true;
}
