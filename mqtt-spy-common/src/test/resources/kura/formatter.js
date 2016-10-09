function format()
{	
	var KuraPayloadFormatter = Java.type("pl.baczkowicz.mqttspy.kura.KuraPayloadFormatter");
	return KuraPayloadFormatter.format(receivedMessage.getRawBinaryPayload());
}

