function replay()
{
	// Get the number of available messages (0 when run for the first time)
	var messageCount = messageLog.getMessageCount();
	
	// If repeat = true, only read the message log once
	if (messageCount == 0)
	{
		messageCount = messageLog.readFromFile("/home/kamil/Programming/Git/mqtt-spy-logger/src/test/resources/mqtt-spy-daemon.messages");		
		messageLog.setSpeed(2);		
	}
	
	// If there are messages to replay...
	if (messageCount > 0)
	{
		// Start the message log time updater...
		messageLog.start();
			
		var Thread = Java.type("java.lang.Thread");	
	
		// For all messages
		for (i = 0; i < messageCount; i++)
		{
			// Wait until this message is ready to be published
			while (!messageLog.isReadyToPublish(i))		
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
			
			// When ready, publish the message
			mqttspy.publish(messageLog.getMessage(i).getTopic(), messageLog.getMessage(i).getMessage(), 0, false);				
		}
	}
	else
	{
		logger.warn("No messages available");
	}
	
	return true;
}

replay();
