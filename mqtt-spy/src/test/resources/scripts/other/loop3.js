function publish()
{
	mqttspy.setScriptTimeout(11000);

	var Thread = Java.type("java.lang.Thread");

	for (i = 0; i < 20; i++)
	{
		mqttspy.publish("/mqtt-spy/script/test3/", "hello" + i, 0, false);

		if (i == 10)
		{
			Thread.sleep(10000);
		}

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
