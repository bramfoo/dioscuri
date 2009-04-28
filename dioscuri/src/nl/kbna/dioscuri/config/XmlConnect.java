/*
 * $Revision: 1.2 $ $Date: 2009-04-03 11:06:27 $ $Author: jrvanderhoeven $
 * 
 * Copyright (C) 2007  National Library of the Netherlands, Nationaal Archief of the Netherlands
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information about this project, visit
 * http://dioscuri.sourceforge.net/
 * or contact us via email:
 * jrvanderhoeven at users.sourceforge.net
 * blohman at users.sourceforge.net
 * 
 * Developed by:
 * Nationaal Archief               <www.nationaalarchief.nl>
 * Koninklijke Bibliotheek         <www.kb.nl>
 * Tessella Support Services plc   <www.tessella.com>
 *
 * Project Title: DIOSCURI
 *
 */

/*
 * Information used in this module was taken from:
 * - http://en.wikipedia.org/wiki/AT_Attachment
 * - http://bochs.sourceforge.net/techspec/IDE-reference.txt
 */
package nl.kbna.dioscuri.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class XmlConnect
{
    // Config variable (parent)
	ConfigController configController;
	
    // Logging
    private static Logger logger = Logger.getLogger("nl.kbna.dioscuri.config");
    
    
    // Constructor
    
    public XmlConnect(ConfigController configController)
    {
    	this.configController = configController;
	}

    
    // Methods
    
	/**
     * Load the XML config document and check validation.
     * 
     * @param xmlConfigInputStream
     * @param xmlSchemaInputStream 
     * @return the XML Document
     */
    public Document loadXmlDocument(FileInputStream xmlConfigInputStream,
                                           File schemaFile) throws Exception
    {
        
       Document xmlDocument = null; 
       
       try 
       {  
                               
           DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance (  ) ; 
           factory.setNamespaceAware(true);
                 
           SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI); 
                              
           Schema schema = schemaFactory.newSchema(schemaFile);
                      
           factory.setSchema(schema);

           DocumentBuilder builder = factory.newDocumentBuilder (  ) ; 

           xmlDocument = builder.parse (xmlConfigInputStream) ; 
                       
       } catch (IOException de)
       {        
           throw new Exception("Can not read from xml configuration file.");
           
       } catch (SAXException de)
       {
           throw new Exception("Validation failed for configuration file. The error is: " + de.getMessage());
           
       } catch  ( ParserConfigurationException ex )   
       {  
           throw new Exception("Error loading XML document. The error is:  " + ex.getMessage());
       }  
       
        return xmlDocument;
    }
   
    /**
     * Close the XML document and associated file stream.
     * 
     * @param document
     * @param xmlConfigInputStream
     */
    public void closeXmlDocument(Document document, 
                                        FileInputStream xmlConfigInputStream)
    {
        try 
        {
            xmlConfigInputStream.close();
            
        } catch (IOException e )
        {
            logger.log(Level.SEVERE, "Error closing configuration file.");
        }
               
        document = null;
    }
    
    /**
     * Write XML Document to File.
     * 
     * @param document
     * @return
     */
    public boolean writeDocumentToFile(Document document)
    {
        OutputStream outStream = null;
        
        try 
        {
            
        outStream = new FileOutputStream(configController.getConfigFilePath());
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(outStream);
        transformer.transform(source, result);
    

        } catch (Exception e)
        {
            logger.log(Level.SEVERE, "Error closing configuration file during save.");
            return false;
            
        } finally 
        { 
            
            if(outStream != null)
            {
                try 
                {
                    
                    outStream.flush();
                    outStream.close(); 
                
                } catch(IOException e)
                {};
            }
            
        }   
        
        return true;
    }
    

    /**
     * Get first Node to match given node text.
     * 
     * @param document
     * @param nodeName
     * @return the node
     */
    public Node getFirstNode(Document document, String nodeName)
    {
        
        Node theNode = null;
        
        theNode = document.getElementsByTagName ( nodeName ).item(0);
    
        return theNode;
    }
    
}