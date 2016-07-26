function formatPayload()
{
	receivedMessage.setPayload("<tag1>" + receivedMessage.getPayload() + "- modified :)</tag1>");
	
	return true;
}

formatPayload();