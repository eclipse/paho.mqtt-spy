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
package pl.baczkowicz.spy.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import pl.baczkowicz.spy.exceptions.XMLException;

/**
 * Simplifies XML marshalling and unmarshalling.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class XMLParser
{	
	/** XML marshaller. */
	private final Marshaller marshaller;
	
	/** XML unmarshaller. */
	private final Unmarshaller unmarshaller;
	
	/**
	 * Creates the XMLParser with the namespace and schema file for validation.
	 * 
	 * @param namespace The context path / namespace
	 * @param schema The schema file to be used for validation 
	 * 
	 * @throws XMLException Thrown when cannot instantiate the marshaller/unmarshaller
	 */
	public XMLParser(final String namespace, final String schema) throws XMLException
	{
		try
		{
			JAXBContext jc = JAXBContext.newInstance(namespace);
			marshaller = jc.createMarshaller();
			marshaller.setSchema(XMLSchemaUtils.createSchema(schema));
			unmarshaller = jc.createUnmarshaller();
			unmarshaller.setSchema(XMLSchemaUtils.createSchema(schema));
			
		}
		catch (JAXBException e)
		{
			throw new XMLException("Cannot instantiate marshaller/unmarshaller for " + namespace, e);
		}
	}
	
	/**
	 * Creates the XMLParser with the given class as root and schema file for validation.
	 * 
	 * @param classToBeBound The class to be bound
	 * @param schema The schema file to be used for validation
	 * 
	 * @throws XMLException Thrown when cannot instantiate the marshaller/unmarshaller
	 */
	public XMLParser(final Class classToBeBound, final String schema) throws XMLException
	{
		try
		{
			JAXBContext jc = JAXBContext.newInstance(classToBeBound);
			marshaller = jc.createMarshaller();
			marshaller.setSchema(XMLSchemaUtils.createSchema(schema));
			unmarshaller = jc.createUnmarshaller();
			unmarshaller.setSchema(XMLSchemaUtils.createSchema(schema));
			
		}
		catch (JAXBException e)
		{
			throw new XMLException("Cannot instantiate marshaller/unmarshaller for " + classToBeBound, e);
		}
	}
	
	/**
	 * Creates the XMLParser with the namespace and schema files for validation.
	 * 
	 * @param namespace The context path / namespace
	 * @param schemas The schema files to be used for validation 
	 * 
	 * @throws XMLException Thrown when cannot instantiate the marshaller/unmarshaller
	 */
	public XMLParser(final String namespace, final String[] schemas) throws XMLException
	{
		try
		{
			JAXBContext jc = JAXBContext.newInstance(namespace);
			marshaller = jc.createMarshaller();
			marshaller.setSchema(XMLSchemaUtils.createSchema(schemas));
			unmarshaller = jc.createUnmarshaller();
			unmarshaller.setSchema(XMLSchemaUtils.createSchema(schemas));
			
		}
		catch (JAXBException e)
		{
			throw new XMLException("Cannot instantiate marshaller/unmarshaller for " + namespace, e);
		}
	}
	
	/**
	 * Creates the XMLParser with the given class as root and schema files for validation.
	 * 
	 * @param classToBeBound The class to be bound
	 * 
	 * @throws XMLException Thrown when cannot instantiate the marshaller/unmarshaller
	 */
	public XMLParser(final Class classToBeBound, final String[] schemas) throws XMLException
	{
		try
		{
			JAXBContext jc = JAXBContext.newInstance(classToBeBound);
			marshaller = jc.createMarshaller();
			marshaller.setSchema(XMLSchemaUtils.createSchema(schemas));
			unmarshaller = jc.createUnmarshaller();
			unmarshaller.setSchema(XMLSchemaUtils.createSchema(schemas));
			
		}
		catch (JAXBException e)
		{
			throw new XMLException("Cannot instantiate marshaller/unmarshaller for " + classToBeBound, e);
		}
	}
	
	/**
	 * Creates the XMLParser with the given class as root. No schema validation.
	 * 
	 * @param classToBeBound The class to be bound
	 * 
	 * @throws XMLException Thrown when cannot instantiate the marshaller/unmarshaller
	 */
	public XMLParser(final Class classToBeBound) throws XMLException
	{
		try
		{
			JAXBContext jc = JAXBContext.newInstance(classToBeBound.getPackage().getName());
			marshaller = jc.createMarshaller();
			unmarshaller = jc.createUnmarshaller();
			
		}
		catch (JAXBException e)
		{
			throw new XMLException("Cannot instantiate marshaller/unmarshaller for " + classToBeBound, e);
		}
	}
	
	/**
	 * Unmarshals the given XML.
	 * 
	 * @param xml The XML to unmarshal
	 * 
	 * @return The unmarshalled XML document
	 * 
	 * @throws XMLException When cannot unmarshal the XML document 
	 */
	public Object unmarshal(final String xml) throws XMLException
	{
		Object readObject = null;
		try
		{
			readObject = unmarshaller.unmarshal(new StreamSource(xml));
			if (readObject instanceof JAXBElement)
			{
				readObject = ((JAXBElement) readObject).getValue();
			}			
		}
		catch (JAXBException e)		
		{
			throw new XMLException("Cannot read the XML ", e);
		}
		catch (IllegalArgumentException e)
		{
			throw new XMLException("Cannot read the XML ", e);
		}

		return readObject;
	}
	
	/**
	 * Unmarshals the given XML with the given root class.
	 * 
	 * @param xml The XML to unmarshal
	 * @param rootClass The root class
	 * 
	 * @return The unmarshalled XML document
	 * 
	 * @throws XMLException When cannot unmarshal the XML document 
	 */
	public Object unmarshal(final String xml, final Class rootClass) throws XMLException
	{
		Object readObject = null;
		try
		{
			if (xml == null || xml.isEmpty())
			{
				throw new XMLException("Cannot parse empty XML");
			}
	        final DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	        final InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(xml));
	 	        
	        readObject = unmarshaller.unmarshal(db.parse(is).getFirstChild(), rootClass);
			if (readObject instanceof JAXBElement)
			{
				readObject = ((JAXBElement) readObject).getValue();
			}			
		}
		catch (JAXBException e)		
		{
			throw new XMLException("Cannot read the XML ", e);
		}
		catch (IllegalArgumentException e)
		{
			throw new XMLException("Cannot read the XML ", e);
		}
		catch (SAXException e)
		{
			throw new XMLException("Cannot read the XML ", e);
		}
		catch (IOException e)
		{
			throw new XMLException("Cannot read the XML ", e);
		}
		catch (ParserConfigurationException e)
		{
			throw new XMLException("Cannot read the XML ", e);
		}

		return readObject;
	}
	
	/**
	 * Loads an XML document from a stream and unmarshals it.
	 * 
	 * @param inputStream The stream to load from
	 * 
	 * @return The unmarshalled XML document
	 * 
	 * @throws XMLException When cannot unmarshal the XML document 
	 */
	public Object loadFromString(final String xml) throws XMLException
	{
		Object readObject = null;
		try
		{
			readObject = unmarshaller.unmarshal(new StringReader(xml));
			if (readObject instanceof JAXBElement)
			{
				readObject = ((JAXBElement) readObject).getValue();
			}			
		}
		catch (JAXBException e)		
		{
			throw new XMLException("Cannot unmarshal the XML", e);			
		}
		catch (IllegalArgumentException e)
		{
			throw new XMLException("Cannot unmarshal the XML", e);
		}

		return readObject;
	}

	/**
	 * Loads an XML document from a stream and unmarshals it.
	 * 
	 * @param inputStream The stream to load from
	 * 
	 * @return The unmarshalled XML document
	 * 
	 * @throws XMLException When cannot unmarshal the XML document 
	 */
	public Object loadFromInputStream(final InputStream inputStream) throws XMLException
	{
		Object readObject = null;
		try
		{
			readObject = unmarshaller.unmarshal(inputStream);
			if (readObject instanceof JAXBElement)
			{
				readObject = ((JAXBElement) readObject).getValue();
			}			
		}
		catch (JAXBException e)		
		{
			try
			{
				throw new XMLException("Cannot unmarshal the XML: " + IOUtils.toString(inputStream), e);
			}
			catch (IOException e1)
			{
				throw new XMLException("Cannot unmarshal the XML", e1);
			}
		}
		catch (IllegalArgumentException e)
		{
			try
			{
				throw new XMLException("Cannot unmarshal the XML: " + IOUtils.toString(inputStream), e);
			}
			catch (IOException e1)
			{
				throw new XMLException("Cannot unmarshal the XML", e1);
			}
		}

		return readObject;
	}
	
	/**
	 * Loads an XML document from a file and unmarshals it.
	 * 
	 * @param file The file to load from
	 * 
	 * @return The unmarshalled XML document
	 * 
	 * @throws XMLException When cannot unmarshal the XML document 
	 * @throws FileNotFoundException When cannot read from the given file
	 */
	public Object loadFromFile(final File file) throws XMLException, FileNotFoundException
	{
		if (file == null)
		{
			throw new FileNotFoundException("Cannot load a null file");
		}
		else if (!file.exists())
		{
			throw new FileNotFoundException("Cannot load the file from " + file.getAbsolutePath());
		}
		
		Object readObject = null;
		try
		{
			readObject = unmarshaller.unmarshal(file);
			if (readObject instanceof JAXBElement)
			{
				readObject = ((JAXBElement) readObject).getValue();
			}			
		}
		catch (JAXBException e)		
		{
			throw new XMLException("Cannot unmarshal the XML from " + file.getAbsolutePath(), e);
		}
		catch (IllegalArgumentException e)
		{
			throw new XMLException("Cannot unmarshal the XML from " + file.getAbsolutePath(), e);
		}

		return readObject;
	}

	/**
	 * Marshals and saves the given object to a file. The generated XML is formatted.
	 * 
	 * @param file The file to write to
	 * @param objectToSave The object to save
	 * 
	 * @throws XMLException Thrown if any errors occur
	 */
	public void saveToFile(final File file, final Object objectToSave) throws XMLException
	{
		try
		{
			// Format the output
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			// Test write - if we cannot marshal, we won't destroy the config file
			// TODO: take the write output and then write it to a file, rather than marshalling twice
			StringWriter writer = new StringWriter();
			marshaller.marshal(objectToSave, writer);
			
			// Convert the object to XML, and save to given file
			marshaller.marshal(objectToSave, file);
		}
		catch (Exception e)
		{
			throw new XMLException("Cannot save to " + file.getAbsolutePath(), e);
		}
	}
}
