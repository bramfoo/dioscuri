/*
 * $Revision: 1.3 $ $Date: 2007-10-04 14:25:46 $ $Author: jrvanderhoeven $
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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import nl.kbna.dioscuri.Emulator;
import nl.kbna.dioscuri.module.Module;
import nl.kbna.dioscuri.module.ModuleScreen;
import nl.kbna.dioscuri.module.ata.ATA;

public class ConfigController
{

    // Logging
    private static Logger logger = Logger.getLogger("nl.kbna.dioscuri");
    
    public final static String CONFIG_FILE_PATH            = "config/DioscuriConfig.xml"; 
    public final static String SCHEMA_FILE_PATH            = "config/DioscuriConfig.xsd";
  
    
    /**
     * Initiate the Modules -
     * Loading and initialising modules is done based on the 
     * Emulator Specification Document (ESD)
     * Which is an XML based configuration file.
     * 
     * @param emulator
     */
    public boolean initModules(Emulator emulator)
    {
        
        File configFile = new File(CONFIG_FILE_PATH);
        File schemaFile = new File(SCHEMA_FILE_PATH);   
        Document document = null;
        FileInputStream xmlConfigInputStream = null;  
        
        DioscuriXmlReader dioscuriXmlReader = new DioscuriXmlReader();
        
        try 
        {
            
            try 
            {
                xmlConfigInputStream = new FileInputStream(configFile);                      
                document = XmlConnect.loadXmlDocument(xmlConfigInputStream, schemaFile);
           
            } catch (Exception e)
            {
                logger.log(Level.SEVERE, e.getMessage());
                
                return false;
            }
    
            // Check how to reset emulator
            if (emulator.getColdStart() == true)
            {
            	// Cold start (hard reset)
                logger.log(Level.INFO, "=================== COLD START ===================");
                
                // Create modules
                dioscuriXmlReader.createModules(emulator, document);
                
                // Connect modules
                this.connectModules(emulator);
                
                // Set corresponding timers in all modules
                logger.log(Level.INFO, "=================== INIT TIMERS ===================");
                 
                dioscuriXmlReader.readTimingParams(document, emulator);
    
            }
            else
            {
            	// Warm start (soft reset)
                logger.log(Level.INFO, "=================== WARM START ===================");
                
                // Make sure next time cold start will happen (FIXME: why?)
                emulator.setColdStart(true);
            }
            
            // Reset all modules (equals a soft reset)
            this.resetModules(emulator);
    
            logger.log(Level.INFO, "=================== INIT OUTPUT DEVICES ===================");
            // Initialise screen (if available)
            this.initScreenOutputDevice(emulator);
            
            logger.log(Level.INFO, "=================== INIT INPUT DEVICES ===================");
            // Initialise mouse (if available)
            dioscuriXmlReader.readMouseParams(document, emulator);         

            logger.log(Level.INFO, "=================== LOAD BIOS ===================");
            // Load system and video BIOS
            if (!dioscuriXmlReader.loadBios(document,emulator))
            {
                return false;
            }
        
            // Set storage device settings
            logger.log(Level.INFO, "=================== LOAD STORAGE MEDIA ===================");
            
            dioscuriXmlReader.readFloppyParams(document, emulator);
            dioscuriXmlReader.readHardDriveParams(document, emulator);
            
            // Set other settings
            logger.log(Level.INFO, "=================== OTHER STUFF ===================");
            dioscuriXmlReader.readBootParams(document, emulator);
            dioscuriXmlReader.readDebugMode(document, emulator);
            
            // Print ready status
            logger.log(Level.CONFIG, "Modules initialisation done.");
            logger.log(Level.INFO, "=================== READY FOR EXECUTION ===================");
        
        } catch (Exception e)
        {
            logger.log(Level.SEVERE, "An error occurred initializing the configuration.");
            return false;
            
        } finally 
        {       
            XmlConnect.closeXmlDocument(document, xmlConfigInputStream);

        }
        
        return true;
    }
     
    /**
     * Reset all modules.
     * 
     * @param emulator
     */
    public void resetModules(Emulator emulator)
    {
        
        // Reset all modules
        logger.log(Level.INFO, "=================== RESET MODULES ===================");
        
        boolean isReset = true;
        for (int i = 0; i < emulator.getModules().size(); i++)
        {
            if (!(emulator.getModules().getModule(i).reset()))
            {
                isReset = false;
                logger.log(Level.SEVERE, "Could not reset module: " + emulator.getModules().getModule(i).getType() + ".");
            }
        }
        if (isReset == false)
        {
            logger.log(Level.SEVERE, "Not all modules are reset. Emulator may be unstable.");
        }
        else
        {
            logger.log(Level.INFO, "All modules are successfully reset.");
        }
        return;
    }
    
    /**
     * Init Screen Output Device.
     * 
     * @param emulator
     */
    public void initScreenOutputDevice(Emulator emulator)
    {
    
        // Set screen output
        // Connect screen (check if screen is available)
        ModuleScreen screen = (ModuleScreen)emulator.getModules().getModule(ModuleType.SCREEN.toString());
        if (screen != null)
        {
            emulator.getGui().setScreen(screen.getScreen(), true);
        }
        else
        {
            logger.log(Level.WARNING, "[CONFIG]" + " No screen available.");
        }
    }
        
    /**
     * Connect the modules together.
     * 
     * @param emulator
     */
    public void connectModules(Emulator emulator)
    {
        // Connect modules with each other
        logger.log(Level.INFO, "=================== CONNECT MODULES ===================");
        Module mod1, mod2;
        for (int i = 0; i < emulator.getModules().size(); i++)
        {
            mod1 = emulator.getModules().getModule(i);
            String[] connections = mod1.getConnection();
            for (int c = 0; c < connections.length; c++)
            {
                mod2 = emulator.getModules().getModule(connections[c]);
                if (mod2 != null)
                {
                    if (mod1.setConnection(mod2))
                    {
                        logger.log(Level.CONFIG, "Successfully established connection between " + mod1.getType() + " and " + mod2.getType());
                    }
                    else
                    {
                        logger.log(Level.SEVERE, "Failed to establish connection between " + mod1.getType() + " and " + mod2.getType());
                    }
                }
                else
                {
                    logger.log(Level.SEVERE, "Failed to establish connection between " + mod1.getType() + " and unknown module " + connections[c]);
                }
            }
        }
        
        // Check if all modules are connected
        boolean isConnected = true;
        for (int i = 0; i < emulator.getModules().size(); i++)
        {
            if (!(emulator.getModules().getModule(i).isConnected()))
            {
                isConnected = false;
                logger.log(Level.SEVERE, "Could not connect module: " + emulator.getModules().getModule(i).getType() + ".");
            }
        }
        if (isConnected == false)
        {
            logger.log(Level.SEVERE, "Not all modules are connected. Emulator may be unstable.");
        }
        else
        {
            logger.log(Level.INFO, "All modules are successfully connected.");
        }
        
        return;
    }
      
    
}
