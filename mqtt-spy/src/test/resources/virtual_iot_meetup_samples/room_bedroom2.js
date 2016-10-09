function publish()
{
	var Thread = Java.type("java.lang.Thread");

	for (i = 0; i < 20; i++)
	{
		mqttspy.publish("home/rooms/bedroom2", 
				"{ " +
				"temp: " + (19 + Math.floor((Math.random() * 10) + 1) / 10) + ", " + 
				"humidity: " + (61 + Math.floor((Math.random() * 10) + 1) / 10) + ", " + 
				//"energy: " + (40 + Math.floor((Math.random() * 10) + 1)) + 
				"}", 0, false);	

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
