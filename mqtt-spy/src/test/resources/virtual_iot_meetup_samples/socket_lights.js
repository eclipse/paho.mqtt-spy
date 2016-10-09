function publish()
{
	var Thread = Java.type("java.lang.Thread");

	for (i = 0; i < 20; i++)
	{
		mqttspy.publish("home/sockets/lights", 
				"{ " +
				"energy: " + (40 + Math.floor((Math.random() * 5) + 1)) + 
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
