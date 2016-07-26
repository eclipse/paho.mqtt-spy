/***********************************************************************************
 * 
 * Copyright (c) 2014 Kamil Baczkowicz
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
package pl.baczkowicz.mqtt.spy.versions;

import static org.junit.Assert.*;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.junit.Before;
import org.junit.Test;

public class VersionComparison
{

	@Before
	public void setUp() throws Exception
	{
	}

	@Test
	public final void test()
	{
		final String v8b1 = "0.0.8-beta-1";
		final String v8b10 = "0.0.8-beta-10";
		final String v8b2 = "0.0.8-beta-2";
		final String v8 = "0.0.8-11";
		
		assertTrue(0 == new DefaultArtifactVersion(v8b1).compareTo(new DefaultArtifactVersion(v8b1)));
		assertTrue(0 > new DefaultArtifactVersion(v8b1).compareTo(new DefaultArtifactVersion(v8b10)));
		assertTrue(0 > new DefaultArtifactVersion(v8b1).compareTo(new DefaultArtifactVersion(v8b2)));
		assertTrue(0 > new DefaultArtifactVersion(v8b2).compareTo(new DefaultArtifactVersion(v8b10)));
		assertTrue(0 > new DefaultArtifactVersion(v8b2).compareTo(new DefaultArtifactVersion(v8)));
	}
}
