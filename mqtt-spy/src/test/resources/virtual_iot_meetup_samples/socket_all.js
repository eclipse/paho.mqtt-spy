function publish()
{
	var Thread = Java.type("java.lang.Thread");

	for (i = 0; i < 20; i++)
	{
		mqttspy.publish("home/sockets/all", 
				"{ " +
				"energy: " + (240 + Math.floor((Math.random() * 70) + 1)) + 
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
