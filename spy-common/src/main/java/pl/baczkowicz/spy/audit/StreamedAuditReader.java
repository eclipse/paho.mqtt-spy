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

package pl.baczkowicz.spy.audit;

import java.io.File;
import java.io.IOException;

import pl.baczkowicz.spy.exceptions.SpyException;
import pl.baczkowicz.spy.files.FileUtils;
import pl.baczkowicz.spy.files.StreamedFileReader;

public class StreamedAuditReader
{
	private long lines;
	
	private File file;
	
	private StreamedFileReader reader = new StreamedFileReader();
	
	public void openAuditFile(final File selectedFile) throws SpyException
	{
		file = selectedFile;
		lines = FileUtils.countLines(file);
		
		try
		{
			reader.openFile(file);
		}
		catch (IOException e)
		{
			new SpyException("Cannon open file " + file, e);
		}
	}
	
	public boolean hasNext() throws IOException
	{
		return reader.hasNext();
	}
	
	public String getNextLine() throws IOException
	{
		return reader.getNextLine();
	}
	
	public void closeAuditFile()
	{
		try
		{
			reader.closeFile();
		}
		catch (IOException e)
		{
			new SpyException("Cannon close file " + file, e);
		}
	}
	
	public long getCount()
	{
		return lines;
	}
}
