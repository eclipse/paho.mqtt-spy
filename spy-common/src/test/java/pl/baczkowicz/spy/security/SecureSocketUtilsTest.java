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
package pl.baczkowicz.spy.security;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;

import pl.baczkowicz.spy.common.generated.KeyStoreTypeEnum;

public class SecureSocketUtilsTest
{

	@Test
	public void testGetKeyStoreInstance() throws KeyStoreException
	{
		SecureSocketUtils.getKeyStoreInstance(KeyStoreTypeEnum.DEFAULT);
	}

	@Test
	public void testGetKeyStoreInstanceBaseTypes() throws KeyStoreException
	{
		SecureSocketUtils.getKeyStoreInstance(KeyStoreTypeEnum.JKS);
		
		SecureSocketUtils.getKeyStoreInstance(KeyStoreTypeEnum.JCEKS);
		
		SecureSocketUtils.getKeyStoreInstance(KeyStoreTypeEnum.PKCS_12);		
	}

	@Test
	public void testGetKeyStoreInstanceBKSType() throws KeyStoreException
	{
		// Try by supplying a provider
		SecureSocketUtils.getKeyStoreInstance(KeyStoreTypeEnum.BKS, new BouncyCastleProvider());
		
		// Try be registering a provider
		Security.addProvider(new BouncyCastleProvider());
		SecureSocketUtils.getKeyStoreInstance(KeyStoreTypeEnum.BKS);			
	}
	
	@Test
	public void testLoadingDefaultKeyStore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
	{
		SecureSocketUtils.loadKeystore("src/test/resources/keystores/public_brokers.jks", "mqtt-spy", KeyStoreTypeEnum.DEFAULT);
	}
	
	@Test (expected = IOException.class)
	public void testInvalidFormatForDefaultType() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException	
	{		
		SecureSocketUtils.loadKeystore("src/test/resources/keystores/public_brokers.jceks", "mqtt-spy", KeyStoreTypeEnum.DEFAULT);
	}
	
	@Test
	public void testLoadingKeyStoreForType() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
	{
		SecureSocketUtils.loadKeystore("src/test/resources/keystores/public_brokers.jks", "mqtt-spy", KeyStoreTypeEnum.JKS);
		SecureSocketUtils.loadKeystore("src/test/resources/keystores/public_brokers.jceks", "mqtt-spy", KeyStoreTypeEnum.JCEKS);
		SecureSocketUtils.loadKeystore("src/test/resources/keystores/public_brokers.p12", "mqtt-spy", KeyStoreTypeEnum.PKCS_12);
	}
	
	@Test
	public void testLoadingBksKeyStore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
	{
		Security.addProvider(new BouncyCastleProvider());
		SecureSocketUtils.loadKeystore("src/test/resources/keystores/public_brokers.bks", "mqtt-spy", KeyStoreTypeEnum.BKS);
	}
	
	@Test
	public void testLoadingKeyStores() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
	{
		testKeyStore("src/test/resources/keystores/public_brokers.jks");
		testKeyStore("src/test/resources/keystores/public_brokers.jceks");
		testKeyStore("src/test/resources/keystores/public_brokers.p12");
		
		Security.addProvider(new BouncyCastleProvider());
		testKeyStore("src/test/resources/keystores/public_brokers.bks");
	}
	
	private void testKeyStore(final String name) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
	{
		SecureSocketUtils.loadKeystore(name, "mqtt-spy", SecureSocketUtils.getTypeFromFilename(name));
	}
}
