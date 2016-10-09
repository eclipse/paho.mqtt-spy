function onMessage()
{
	mqttspy.publish(
		// Topic
		"reply", 
		// Payload
		"<reply>" + 
		"<originalMessage>" + 
		receivedMessage.getPayload() +
		”</originalMessage>" + 
		"</reply>", 
		// QoS
		0, 
		// Retained
		false);
	
	return true;
}