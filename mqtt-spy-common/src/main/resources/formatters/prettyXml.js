function pretty()
{	
	var Utils = Java.type("pl.baczkowicz.spy.formatting.FormattingUtils");	
	return Utils.prettyXml(receivedMessage.getPayload(), 10);
}

function format()
{
	return receivedMessage.getPayload();
}
