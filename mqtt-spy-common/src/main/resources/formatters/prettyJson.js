function pretty()
{	
	var JSONObject = Java.type("org.json.JSONObject");
	var json = new JSONObject(receivedMessage.getPayload());
	return json.toString(10);
}

function format()
{
	return receivedMessage.getPayload();
}
