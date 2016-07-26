var Thread = Java.type("java.lang.Thread");

for (i = 0; i < 200; i++)
{
	mqttspy.publish("/mqtt-spy/script/test2/", "hello" + i, 0, false);

	try 
	{
		Thread.sleep(30);
	}
	catch(err) 
	{
		break;
	}
}
