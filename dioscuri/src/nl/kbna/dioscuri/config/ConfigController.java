/*
 * $Revision: 1.5 $ $Date: 2009-04-03 11:06:27 $ $Author: jrvanderhoeven $
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
package nl.kbna.dioscuri.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.kbna.dioscuri.GUI;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ConfigController
{
	// Read/write XML variables
	private GUI gui;
    private XmlConnect xmlConnect;
    private DioscuriXmlReader xmlReader;
    private DioscuriXmlWriter xmlWriter;

	
    // Logging
    private static Logger logger = Logger.getLogger("nl.kbna.dioscuri");
    
    // File config and schema paths (set to default)
    private String configFilePath            = "config/DioscuriConfig.xml";
    private String schemaFilePath            = "config/DioscuriConfig.xsd";
    
//    public final static String CONFIG_FILE_PATH            = "config/DioscuriConfig.xml"; 
//    public final static String SCHEMA_FILE_PATH            = "config/DioscuriConfig.xsd";

    
    // Constructor
    
    public ConfigController(GUI gui)
    {
    	this.gui = gui;
        xmlConnect = new XmlConnect(this);

        // Create XML reader and writer
        xmlReader = new DioscuriXmlReader(this, xmlConnect);
        xmlWriter = new DioscuriXmlWriter(this, xmlConnect);
        

    }

    // Methods
    
	public boolean setConfigFilePath(String configFilePath)
	{
		this.configFilePath = configFilePath;
		return true;
	}

	public String getConfigFilePath()
	{
		return configFilePath;
	}

	public void setSchemaFilePath(String schemaFilePath)
	{
		this.schemaFilePath = schemaFilePath;
	}

	public String getSchemaFilePath()
	{
		return schemaFilePath;
	}

	public DioscuriXmlReader getXMLReader()
	{
		return xmlReader;
	}
    
	
	public DioscuriXmlWriter getXMLWriter()
	{
		return xmlWriter;
	}
    
	
    /**
     * Creates a recursive HashMap from a given node in a xml document
     * Iterates over all subnodes adding key-value pairs as either
     * nodeName=nodeValue for simple elements, or 
     * nodeName=HashMap for complex elements, recursing into these elements in turn
     * Ignores TEXT and COMMENT node fields
     * 
     * @param node The starting node
     * @param HashMap The HashMap to add the node attributes to
     * 
     * @return HashMap A recursively filled HashMap consisting of node attributes 
     */
    private HashMap nodeToHashMap(Node node, HashMap hm)
    {
        //FIXME: A HashMap can only contain unique keys, so multiple instances of e.g. floppy will result in one being added
        
        logger.log(Level.INFO, "Creating hashmap for: " + node.getNodeName());
        
        // Loop through the attribute of the current node
        NamedNodeMap nodeAttr = node.getAttributes();
        if (nodeAttr.getLength() > 0)
        {
            for (int j = 0; j < nodeAttr.getLength(); j++)
            {
                logger.log(Level.INFO, "\tAdding attribute: " + nodeAttr.item(j).getNodeName() + "=" + nodeAttr.item(j).getNodeValue());
                hm.put(nodeAttr.item(j).getNodeName(), nodeAttr.item(j).getNodeValue());
            }
        }
        
        // Recurse through each of the child nodes
        for (int i = 0; i < node.getChildNodes().getLength(); i++)
        {
            Node childNode = node.getChildNodes().item(i); 
            if (childNode.getNodeType() != Node.TEXT_NODE && childNode.getNodeType() != Node.COMMENT_NODE )
            {
                // A complex node should be have its own subHashMap, added to the parent HashMap
                if (childNode.getChildNodes().getLength() > 1)
                {
                    logger.log(Level.INFO, "Found subMap: " + childNode.getNodeName());
                    HashMap childHashMap = new HashMap();
                    hm.put(childNode.getNodeName(), childHashMap);
                    
                    // Recursion
                    nodeToHashMap(childNode, childHashMap);
                }
                // Simple elements can be added straight to the parent HashMap
                else
                {
                    logger.log(Level.INFO, "\tAdding element: " + childNode.getNodeName() + "=" + childNode.getFirstChild().getNodeValue());
                    hm.put(childNode.getNodeName(), childNode.getFirstChild().getNodeValue());
                }
            }
        }
        
        return hm;
    }
    
    /**
     * Temporary interface for the xml2hm function
     * @param nodeName string representation for the starting node
     * @return Hashmap containing all the emulator settings
     */
    public HashMap getSettings(String nodeName)
    {
        HashMap hm = new HashMap();
        
        File configFile = new File(configFilePath);
        File schemaFile = new File(schemaFilePath);
        Document document = null;
        FileInputStream xmlConfigInputStream = null;

        try
        {
            xmlConfigInputStream = new FileInputStream(configFile);
            document = xmlConnect.loadXmlDocument(xmlConfigInputStream, schemaFile);

        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, e.getMessage());
            return null;
        }

        // Translate the string
        Node modNode = document.getElementsByTagName(nodeName).item(0);

        nodeToHashMap(modNode, hm);
        
        xmlConnect.closeXmlDocument(document, xmlConfigInputStream);
        
        return hm;

    }

}
