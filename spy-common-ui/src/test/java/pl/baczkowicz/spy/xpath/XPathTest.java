/***********************************************************************************
 * 
 * Copyright (c) 2016 Kamil Baczkowicz
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
package pl.baczkowicz.spy.xpath;

import java.io.StringReader;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Test;
import org.xml.sax.InputSource;

import junit.framework.TestCase;

public class XPathTest extends TestCase
{

	@Test
	public void test()
	{
		final String message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"  +

		"<book category=\"COOKING\"> <year>2005</year> <price>30.00</price> </book>";
		
		assertEquals(evaluateXPath("/book/price", message), 30.0);
	}
	
	protected Double evaluateXPath(final String expression, final String message)
	{
		Double value = 0.0;
		final XPath xpath = XPathFactory.newInstance().newXPath();
		final InputSource inputSource = new InputSource(new StringReader(message));

		try
		{
			final XPathExpression exp = xpath.compile(expression);
			value = (Double) exp.evaluate(inputSource, XPathConstants.NUMBER);
		}
		catch (XPathExpressionException e)
		{
			// TODO
		}

		return value;
	}
}