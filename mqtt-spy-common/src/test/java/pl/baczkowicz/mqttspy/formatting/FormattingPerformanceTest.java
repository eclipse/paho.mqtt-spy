/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 *    
 * The Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * 
 *    Kamil Baczkowicz - initial API and implementation and/or initial documentation
 *    
 */
package pl.baczkowicz.mqttspy.formatting;

import java.io.File;

import javax.script.ScriptException;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Ignore;
import org.junit.Test;

import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.scripts.MqttScriptManager;
import pl.baczkowicz.spy.common.generated.ConversionMethod;
import pl.baczkowicz.spy.common.generated.FormatterDetails;
import pl.baczkowicz.spy.common.generated.FormatterFunction;
import pl.baczkowicz.spy.common.generated.ScriptDetails;
import pl.baczkowicz.spy.common.generated.ScriptExecutionDetails;
import pl.baczkowicz.spy.common.generated.SubstringConversionFormatterDetails;
import pl.baczkowicz.spy.common.generated.SubstringExtractFormatterDetails;
import pl.baczkowicz.spy.formatting.FormattingUtils;
import pl.baczkowicz.spy.formatting.ScriptBasedFormatter;
import pl.baczkowicz.spy.utils.ConversionUtils;

public class FormattingPerformanceTest
{
	private FormatterDetails defaultFormatter = FormattingUtils.createBasicFormatter("default", "Plain", null, ConversionMethod.PLAIN);
	
	@Ignore
	@Test
	public void compareFormattingMethods() throws NoSuchMethodException, ScriptException
	{
		final String payload = "<Body>VGhpcyBpcyBhIHNhbXBsZSBtZXNzYWdlIGVuY29kZWQgaW4gQkFTRTY0Lg==</Body>";
		final FormattedMqttMessage message = new FormattedMqttMessage(0, "test", new MqttMessage(payload.getBytes()), null);
		
		long startTime = 0;
		long totalTime = 0;
		long repeat = 10000;
		
		// 1. Function-based
		final FormatterDetails functionBased = new FormatterDetails();
		functionBased.setID("base64-body-decoder");
		functionBased.setName("Base64 body decoder");
		//	  <Function>
		//        <SubstringConversion>
		//            <StartTag>&lt;Body&gt;</StartTag>
		//            <EndTag>&lt;/Body&gt;</EndTag>
		//            <KeepTags>true</KeepTags>
		//            <Format>Base64Decode</Format>
		//        </SubstringConversion>
		//    </Function>
		//    <Function>
		//        <SubstringExtract>
		//            <StartTag>&lt;Body&gt;</StartTag>
		//            <EndTag>&lt;/Body&gt;</EndTag>
		//            <KeepTags>false</KeepTags>
		//        </SubstringExtract>
		//    </Function>

		final SubstringConversionFormatterDetails substringConversion = 
				new SubstringConversionFormatterDetails("&lt;Body&gt;", "&lt;/Body&gt;", true, ConversionMethod.BASE_64_DECODE);
		functionBased.getFunction().add(new FormatterFunction(null, substringConversion, null, null, null, null));
		
        final SubstringExtractFormatterDetails 
        	substringExtract = new SubstringExtractFormatterDetails("&lt;Body&gt;", "&lt;/Body&gt;", false);
		functionBased.getFunction().add(new FormatterFunction(null, null, null, substringExtract, null, null));
		
		startTime = System.nanoTime();
		for (int i = 0; i < repeat; i++)
		{
			// Use the raw payload to make sure any formatting/encoding that is applied is correct
			message.setFormattedPayload(FormattingUtils.checkAndFormatText(functionBased, message.getRawMessage().getPayload()));
			
			// Use the raw payload to make sure any formatting/encoding that is applied is correct
			message.setFormattedPayload(FormattingUtils.checkAndFormatText(defaultFormatter, message.getRawMessage().getPayload()));
		}
		totalTime = System.nanoTime() - startTime;
		System.out.println("Function-based took " + totalTime + " ns; avg = " + (totalTime / repeat) + " ns");
		
		// 2. Script file-based		
		MqttScriptManager scriptManager = new MqttScriptManager(null, null, null);
		final String scriptFile1 = "src/test/resources/scripts/base64-body-decoder.js";
		scriptManager.addScript(new ScriptDetails(false, false, scriptFile1));
		
		startTime = System.nanoTime();
		for (int i = 0; i < repeat; i++)
		{
			message.setPayload(payload);
			scriptManager.runScriptFileWithReceivedMessage(new File(scriptFile1).getAbsolutePath(), message);	
		}
		System.out.println("Message payload = " + message.getPayload());
		
		totalTime = System.nanoTime() - startTime;		
		System.out.println("Script file-based took " + totalTime + " ns; avg = " + (totalTime / repeat) + " ns");
		
		// 3. Script function-based
		final FormatterDetails scriptFunctionBased = new FormatterDetails();
		scriptFunctionBased.setID("script-base64-body-decoder");
		scriptFunctionBased.setName("Script-based BASE64 body decoder");
		
		final String inlineScript = "function format() { return \"<tag2>\" + receivedMessage.getPayload() + \"- modified :)</tag2>\"; }"; 
	
		ScriptExecutionDetails scriptExecution = new ScriptExecutionDetails(ConversionUtils.stringToBase64(inlineScript));
		scriptFunctionBased.getFunction().add(new FormatterFunction(null, null, null, null, null, scriptExecution));
		
		final ScriptBasedFormatter scriptFormatter = new ScriptBasedFormatter(scriptManager);
		startTime = System.nanoTime();
		String result = "";
		for (int i = 0; i < repeat; i++)
		{
			result = scriptFormatter.formatMessage(scriptFunctionBased, message, true);
		}
		System.out.println("Message payload = " + result);
		
		totalTime = System.nanoTime() - startTime;		
		System.out.println("Script function-based took " + totalTime + " ns; avg = " + (totalTime / repeat) + " ns");
	}
}
