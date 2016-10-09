var ByteArrayOutputStream = Java.type("java.io.ByteArrayOutputStream");
var ByteArrayInputStream = Java.type("java.io.ByteArrayInputStream");
var InflaterInputStream = Java.type("java.util.zip.InflaterInputStream");
var InputStream = Java.type("java.io.InputStream");
var ByteBuffer = Java.type("java.nio.ByteBuffer");
var String = Java.type("java.lang.String");

function format()
{	
	try
	{
		return decompress(receivedMessage.getRawPayload());
	}
	catch (err)
	{
		logger.error("Formatting error");
		logger.error(err);
		return "";
	}	
}

function decompress(bytes)
{
	var is = new InflaterInputStream(new ByteArrayInputStream(bytes));
	var baos = new ByteArrayOutputStream();
	
	var buffer = ByteBuffer.allocate(2048);
	var len;
	while ((len = is.read(buffer.array())) > 0)
	{
		baos.write(buffer.array(), 0, len);
	}
	return new String(baos.toByteArray(), "UTF-8");
}
