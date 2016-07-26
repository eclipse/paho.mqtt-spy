function publish()
{
	var Thread = Java.type("java.lang.Thread");

	for (i = 0; i < 20; i++)
	{
		// "<temp>" + 
		mqttspy.publish("home/office/current", (21 + Math.floor((Math.random() * 20) + 1) / 10), 0, false);
		// + "</temp>"
		
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
