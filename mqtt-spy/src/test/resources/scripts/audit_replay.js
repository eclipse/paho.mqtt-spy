function replay()
{
	// Get the number of available messages (0 when run for the first time)
	var messageCount = auditReplay.getMessageCount();
	
	// If repeat = true, only read the message log once
	if (messageCount == 0)
	{
		messageCount = auditReplay.readFromFile("/home/kamil/Programming/Source/mqtt-spy/mqtt-spy-daemon/src/test/resources/mqtt-spy-daemon.messages");		
		auditReplay.setSpeed(2);		
	}
	
	// If there are messages to replay...
	if (messageCount > 0)
	{
		// Start the message log time updater...
		auditReplay.start();
			
		var Thread = Java.type("java.lang.Thread");	
	
		// For all messages
		for (i = 0; i < messageCount; i++)
		{
			// Wait until this message is ready to be published
			while (!auditReplay.isReadyToPublish())		
			{
				try 
				{
					Thread.sleep(10);
				}
				catch(err) 
				{
					return false;				
				}
			}
			
			var message = auditReplay.getNextMessage();
			
			// When ready, publish the message
			mqttspy.publish(message.getTopic(), message.getRawMessage(), 0, false);				
		}
	}
	else
	{
		logger.warn("No messages available");
	}
	
	return true;
}

replay();
