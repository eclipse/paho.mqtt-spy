function pretty()
{
	var JSONObject = Java.type("org.json.JSONObject");
	var json = new JSONObject(format());
	return json.toString(10);
}

function format()
{	
	var KuraPayloadFormatter = Java.type("pl.baczkowicz.mqttspy.kura.KuraPayloadFormatter");
	return KuraPayloadFormatter.format(receivedMessage.getRawPayload());
}