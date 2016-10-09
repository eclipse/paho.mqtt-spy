function publishTime()
{
	var Thread = Java.type("java.lang.Thread");
	var Date = Java.type("java.util.Date");
	var SimpleDateFormat = Java.type("java.text.SimpleDateFormat");
	
	var TIME_FORMAT_WITH_SECONDS = "HH:mm:ss";
	var TIME_WITH_SECONDS_SDF = new SimpleDateFormat(TIME_FORMAT_WITH_SECONDS);

	while (true)
	{
		var currentTime = TIME_WITH_SECONDS_SDF.format(new Date());
		
		mqttspy.publish("time/", currentTime, 0, false);

		// Sleep for 1 second and handle a stop request 
		try 
		{
			Thread.sleep(1000);				
		}
		catch(err) 
		{
			return false;				
		}
		
		// Keep mqtt-spy informed the script is still running
		mqttspy.touch();
	}

	return true;
}

publishTime();
