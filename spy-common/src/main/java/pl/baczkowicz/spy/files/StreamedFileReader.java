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

package pl.baczkowicz.spy.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class StreamedFileReader
{
	private BufferedReader in;
	
	private String currentLine;
	
	private boolean readAhead = false;
	
	public void openFile(final File selectedFile) throws IOException
	{
		in = new BufferedReader(new FileReader(selectedFile));
		
		// Read first line
		readLine();
	}
	
	private void readLine() throws IOException
	{
		currentLine = in.readLine();
		readAhead = true;
	}	

	public String getNextLine() throws IOException
	{
		if (readAhead)
		{
			readAhead = false;
			return currentLine;
		}
		else
		{
			return in.readLine();
		}
	}
	
	public boolean hasNext() throws IOException
	{
		if (!readAhead)
		{
			readLine();
		}
		return currentLine != null;
	}
	
	public void closeFile() throws IOException
	{
		in.close();
	}
}
