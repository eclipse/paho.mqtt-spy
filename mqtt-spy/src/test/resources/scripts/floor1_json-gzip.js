function publish()
{
	var Thread = Java.type("java.lang.Thread");
	var ByteArrayOutputStream = Java.type("java.io.ByteArrayOutputStream");
	var DeflaterOutputStream = Java.type("java.util.zip.DeflaterOutputStream");
	var OutputStream = Java.type("java.io.OutputStream");

	for (i = 0; i < 20; i++)
	{
		var payload = "{ temp: " + (20 + Math.floor((Math.random() * 10) + 1) / 10) + ", " + "energy: " + (40 + Math.floor((Math.random() * 10) + 1)) + "}";
		
		var baos = new ByteArrayOutputStream();		
		var out = new DeflaterOutputStream(baos);
		out.write(payload.getBytes("UTF-8"));
		out.close();
		
		mqttspy.publish("/home/floor1/average", baos.toByteArray(), 0, false);	

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
