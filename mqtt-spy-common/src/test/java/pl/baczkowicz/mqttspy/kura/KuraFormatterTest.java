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
package pl.baczkowicz.mqttspy.kura;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.kura.KuraInvalidMessageException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KuraFormatterTest
{
	/** Diagnostic logger. */
	private static final Logger logger = LoggerFactory.getLogger(KuraFormatterTest.class);
	
	@Test
	public void testKuraPayloadFormatting() throws KuraInvalidMessageException, IOException
	{
		final Path path = Paths.get("src/test/resources/kura/kura.birth");
		byte[] data = Files.readAllBytes(path);
		
		logger.info(KuraPayloadFormatter.format(data));
	}

}
