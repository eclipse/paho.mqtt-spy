function publish()
{
	var Thread = Java.type("java.lang.Thread");

	for (i = 0; i < 20; i++)
	{
		mqttspy.publish(message.getTopic(), message.getPayload() + i, 0, false);

		try 
		{
			Thread.sleep(1000);
		}
		catch(err) 
		{
			return false;				
		}
	}

	return true;
}

publish();
