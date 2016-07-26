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
import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import pl.baczkowicz.spy.exceptions.XMLException;

/**
 * Utility class for XML Schema.
 */
public class XMLSchemaUtils
{
	/**
     * Creates the schema object.
     * 
     * @param schemaLocation Location of the XML schema file
     * 
     * @return Instance of the Schema object, based on the supplied XSD file
     * 
     * @throws XMLException Thrown when cannot create the schema object
     */
    public static Schema createSchema(final String schemaLocation) throws XMLException
    {
        Schema schema = null;
        final SchemaFactory schemaFactory = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try
        {
            final File file = new File(schemaLocation);
            if (file.exists())
            {
                schema = schemaFactory.newSchema(file);
            }
            else
            {
                final InputStream resourceAsStream = XMLParser.class.getResourceAsStream(schemaLocation);
                if (resourceAsStream == null)
                {
                    throw new XMLException("Cannot load the schema from file or classpath - fix the schema or amend the location: " + schemaLocation);
                }

                schema = schemaFactory.newSchema(new StreamSource(resourceAsStream));
            }

            return schema;
        }
        catch (SAXException e)
        {
            throw new XMLException("Cannot set the schema - please fix the schema or the location", e);
        }
    }

	/**
	 * Sets the schemas for validation. The files specified can be either an
	 * absolute file or a resources' location in the classpath.
	 *
	 * @param resourceLocations Schema file locations
	 * 
	 * @return Instance of the Schema object, based on the supplied message XML schema file
	 * 
	 * @throws XMLException Thrown when the schema is wrong.
	 */
	public static Schema createSchema(final String[] resourceLocations) throws XMLException
	{
		if (resourceLocations == null)
		{
			throw new XMLException("Schema file locations not specified");
		}

		final SchemaFactory schemaFactory = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = null;

		final Source[] schemaSources = new Source[resourceLocations.length];

		try
		{
			String resourceLocation;
			for (int i = 0; i < resourceLocations.length; i++)
			{
				resourceLocation = resourceLocations[i];
				if (resourceLocation == null)
				{
					throw new XMLException("Schema file location not specified");
				}

				final File file = new File(resourceLocation);
				if (file.exists())
				{
					schemaSources[i] = new StreamSource(file);
				}
				else
				{
					final InputStream resourceAsStream = XMLSchemaUtils.class.getResourceAsStream(resourceLocation);
					if (resourceAsStream == null)
					{
						throw new XMLException("Cannot load the schema from file or classpath - fix the schema or amend the location: " + resourceLocation);
					}

					schemaSources[i] = new StreamSource(resourceAsStream);
				}
			}
			schema = schemaFactory.newSchema(schemaSources);

			return schema;
		}
		catch (final SAXException e)
		{
			throw new XMLException("Cannot set the schema - fix the schema or the location", e);
		}
	}
}