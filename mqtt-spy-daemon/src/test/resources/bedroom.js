function publish()
{
	var Thread = Java.type("java.lang.Thread");

	for (i = 0; i < 5; i++)
	{
		mqttspy.publish("/home/bedroom/current", "<temp>" + (20 + Math.floor((Math.random() * 10) + 1) / 10) + "</temp><energy>" + (100 + Math.floor((Math.random() * 10) + 1)) + "</energy>", 0, false);

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
