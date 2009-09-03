/*
 * $Revision$ $Date$ $Author$
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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import nl.kbna.dioscuri.GUI;

import nl.kbna.dioscuri.config.Emulator;

public class ConfigController
{
    // Logging
    private static Logger logger = Logger.getLogger("nl.kbna.dioscuri");
    
    // File config and schema paths (set to default)
    
    private static JAXBContext jc;
    public static String EMULATOR_XML = "nl.kbna.dioscuri.config";

    static {
    	try {
    		jc = JAXBContext.newInstance(EMULATOR_XML);
    	}  catch (JAXBException e) {
    		logger.log(Level.SEVERE, "[Config] Cannot initialise JAXBContext for binding Emulator config xml files: " + e.getMessage());
    	}
    }
    
    // Constructor
    public ConfigController(GUI gui)
    {
    }

    // Methods
    
    /**
     * Get an unmarshaller that can unmarshal Emulator types.
     * 
     * @return	A new unmarshaller
     * @throws JAXBException
     */
    public static Unmarshaller getEmuUnmarshaller() throws JAXBException {
    	return jc.createUnmarshaller();
    }
    
    /**
     * Get a marshaller that can marshal Emulator types
     * 
     * @return	A new marshaller
     * @throws JAXBException
     */
    public static Marshaller getEmuMarshaller() throws JAXBException {
    	Marshaller m = jc.createMarshaller();
    	m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
    	return m;
    }
    
    /**
     *  save JAXB Emu object to disk as an XML file
     *
     * @param emuObject          The Emulator object
     * @param outputXMLFile       The xml output file
     * @throws Exception
     */
    public static void saveToXML(Emulator emuObject,  File outputXMLFile) throws Exception {
        FileOutputStream fos =  new FileOutputStream(outputXMLFile);
        try {
        	Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.marshal(emuObject, fos);
        } finally {
        	fos.close();
        }
    }
    
    /**
     *  Load a whole Emu object from an XML file on disk
     *
     * @param inputEmuFile	An Emulator XML config file to load into memory
     * @return An Emulator object representing the whole Emulator file
     * @throws Exception
     */
    public static Emulator loadFromXML(File inputEmuFile) throws Exception {
    	FileInputStream fis = new FileInputStream(inputEmuFile);
        try {
        	return (Emulator) jc.createUnmarshaller().unmarshal(fis);
        } finally {
        	fis.close();
        }
    }
    
    public static Emulator loadFromXML(InputStream is) throws Exception {
        return (Emulator) jc.createUnmarshaller().unmarshal(is);
    }
}
