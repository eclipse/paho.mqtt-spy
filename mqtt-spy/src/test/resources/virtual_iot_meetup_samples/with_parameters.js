function publish()
{
	mqttspy.publish(message.getTopic(), "{ payload: \"" + message.getPayload() + "\", timestamp: \"" + (new Date()) + "\"}", 0, false);

	return true;
}

publish();
