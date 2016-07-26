function publishImage()
{
	var Files = Java.type("java.nio.file.Files");
	var Paths = Java.type("java.nio.file.Paths");
	var Path = Java.type("java.nio.file.Path");

	var path = Paths.get("/home/kamil/Desktop/image2.png");
	var data = Files.readAllBytes(path);
	
	mqttspy.publish(message.getTopic(), data, 0, false);

	return true;
}

publishImage();
