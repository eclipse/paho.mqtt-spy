function publish()
{
	var Thread = Java.type("java.lang.Thread");

	for (i = 0; i < 20; i++)
	{
		mqttspy.publish("home/bedroom/current", (19 + Math.floor((Math.random() * 10) + 1) / 10), 0, false);

		if (i == 10)
		{
			Thread.sleep(1000);
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
