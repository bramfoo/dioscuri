/*
 * $Revision: 1.3 $ $Date: 2007-08-20 15:18:47 $ $Author: jrvanderhoeven $
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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import nl.kbna.dioscuri.Emulator;
import nl.kbna.dioscuri.GUI;
import nl.kbna.dioscuri.Modules;
import nl.kbna.dioscuri.exception.ModuleException;
import nl.kbna.dioscuri.module.ModuleATA;
import nl.kbna.dioscuri.module.ModuleCPU;
import nl.kbna.dioscuri.module.ModuleFDC;
import nl.kbna.dioscuri.module.ModuleKeyboard;
import nl.kbna.dioscuri.module.ModuleMemory;
import nl.kbna.dioscuri.module.ModuleMouse;
import nl.kbna.dioscuri.module.ModulePIT;
import nl.kbna.dioscuri.module.ModuleVideo;
import nl.kbna.dioscuri.module.ata.ATA;
import nl.kbna.dioscuri.module.ata.ATAConstants;
import nl.kbna.dioscuri.module.ata.ATATranslationType;
import nl.kbna.dioscuri.module.bios.BIOS;
import nl.kbna.dioscuri.module.clock.Clock;
import nl.kbna.dioscuri.module.cpu.CPU;
import nl.kbna.dioscuri.module.dma.DMA;
import nl.kbna.dioscuri.module.fdc.FDC;
import nl.kbna.dioscuri.module.keyboard.Keyboard;
import nl.kbna.dioscuri.module.memory.Memory;
import nl.kbna.dioscuri.module.motherboard.DeviceDummy;
import nl.kbna.dioscuri.module.motherboard.Motherboard;
import nl.kbna.dioscuri.module.mouse.Mouse;
import nl.kbna.dioscuri.module.parallelport.ParallelPort;
import nl.kbna.dioscuri.module.pic.PIC;
import nl.kbna.dioscuri.module.pit.PIT;
import nl.kbna.dioscuri.module.rtc.RTC;
import nl.kbna.dioscuri.module.screen.Screen;
import nl.kbna.dioscuri.module.serialport.SerialPort;
import nl.kbna.dioscuri.module.video.Video;

public class DioscuriXmlReader
{
       
    // Logging
    private static Logger logger = Logger.getLogger("nl.kbna.dioscuri.config");
                  
    //XML Read Methods
        
    /**
     * Create the Modules.
     * 
     * @param emulator
     */
    public void createModules(Emulator emulator, Document document)
    {
        
        Node biosNode =  DioscuriXmlParams.getModuleNode(document, ModuleType.BIOS);
        Node cpuNode =   DioscuriXmlParams.getModuleNode(document, ModuleType.CPU);
        Node ramNode =   DioscuriXmlParams.getModuleNode(document, ModuleType.MEMORY);           
        Node pitNode =   DioscuriXmlParams.getModuleNode(document, ModuleType.PIT);
        Node keyboardNode =   DioscuriXmlParams.getModuleNode(document, ModuleType.KEYBOARD);     
        Node floppyDiskDrivesNode =   DioscuriXmlParams.getModuleNode(document, ModuleType.FDC);         
        Node mouseNode =   DioscuriXmlParams.getModuleNode(document, ModuleType.MOUSE);
        Node ataDrivesNode =   DioscuriXmlParams.getModuleNode(document, ModuleType.ATA); 
        Node videoNode =   DioscuriXmlParams.getModuleNode(document, ModuleType.VGA);
        
        // Create modules
        // TODO: Maybe using Generics in Java (1.5) is better than "modules" container
        // optional modules are in if-clause, mandatory modules are expected to be there
        logger.log(Level.INFO, "=================== CREATE MODULES ===================");
        
        Modules modules = new Modules(20);
        
        if(cpuNode != null)
        {
            modules.addModule(new CPU(emulator));
        }
        if (ramNode != null)
        {
            modules.addModule(new Memory(emulator));
        }
        if (biosNode != null)
        {
            modules.addModule(new BIOS(emulator));
        }
        modules.addModule(new Motherboard(emulator));       // Motherboard should always be initialised before other I/O devices!!! Else I/O address space will be reset
        modules.addModule(new PIC(emulator));               // PIC should always be initialised directly after motherboard
        modules.addModule(new Clock(emulator));
        
        if(pitNode != null)
        {
            modules.addModule(new PIT(emulator));
        }
        
        modules.addModule(new RTC(emulator));               // RTC should always be initialised before other devices
        
        if(ataDrivesNode != null)
        {
            modules.addModule(new ATA(emulator)); 
        }        
        modules.addModule(new DMA(emulator));
        
        if (floppyDiskDrivesNode != null)
        {
            modules.addModule(new FDC(emulator));
        }
        
        if (keyboardNode != null)
        {
            modules.addModule(new Keyboard(emulator));
        }
        if (mouseNode != null)
        {
            modules.addModule(new Mouse(emulator));		// Mouse always requires a keyboard (controller)
        }
        
        modules.addModule(new ParallelPort(emulator));      
        modules.addModule(new SerialPort(emulator));
        
        if(videoNode != null)
        {
            modules.addModule(new Video(emulator));
        }
        
        modules.addModule(new DeviceDummy(emulator));
        modules.addModule(new Screen(emulator));
        emulator.setModules(modules);
        
        logger.log(Level.INFO, "All modules are created.");
        
    }
    
    /**
     * Get the Timing params.
     * 
     * @param emulator
     */
    public void readTimingParams(Document document, Emulator emulator)
    {
        Node cpuNode =   DioscuriXmlParams.getModuleNode(document, ModuleType.CPU);
        
        int timingParam = -1;
                
        // Module CPU: set instructions per second (IPS)
        timingParam = this.getTimingParam(emulator, ModuleType.CPU, cpuNode);
        if(timingParam != -1)
        {
            ((ModuleCPU)emulator.getModules().getModule(ModuleType.CPU.toString())).setIPS(timingParam * 1000000); 
        }
        
        Node videoNode =   DioscuriXmlParams.getModuleNode(document, ModuleType.VGA);   
        // Module VGA: set update interval            
        timingParam = this.getTimingParam(emulator, ModuleType.VGA, videoNode);
        if(timingParam != -1)
        {
            ((ModuleVideo)emulator.getModules().getModule(ModuleType.VGA.toString())).setUpdateInterval(timingParam); 
        }

        Node pitNode =   DioscuriXmlParams.getModuleNode(document, ModuleType.PIT);
        timingParam = this.getTimingParam(emulator, ModuleType.PIT, pitNode);
        if(timingParam != -1)
        {
            ((ModulePIT)emulator.getModules().getModule(ModuleType.PIT.toString())).setUpdateInterval(timingParam); 
        }
        
        // Module Keyboard: set update interval   
        Node keyboardNode =   DioscuriXmlParams.getModuleNode(document, ModuleType.KEYBOARD);     
        timingParam = this.getTimingParam(emulator, ModuleType.KEYBOARD, keyboardNode);
        if(timingParam != -1)
        {
            ((ModuleKeyboard)emulator.getModules().getModule(ModuleType.KEYBOARD.toString())).setUpdateInterval(timingParam); 
        }
               
        // Module FDC: set update interval
        Node floppyDiskDrivesNode =   DioscuriXmlParams.getModuleNode(document, ModuleType.FDC);         
        timingParam = this.getTimingParam(emulator, ModuleType.FDC, floppyDiskDrivesNode);
        if(timingParam != -1)
        {
            ((ModuleFDC)emulator.getModules().getModule(ModuleType.FDC.toString())).setUpdateInterval(timingParam); 
        }
        
        // Module ATA: set update interval
        Node ataDrivesNode =   DioscuriXmlParams.getModuleNode(document, ModuleType.ATA); 
        timingParam = this.getTimingParam(emulator, ModuleType.ATA, ataDrivesNode);
        if(timingParam != -1)
        {
            ((ModuleATA)emulator.getModules().getModule(ModuleType.ATA.toString())).setUpdateInterval(timingParam); 
        }
    }

    /**
     * Get specified timing param from xml document.
     * 
     * @param emulator
     * @param moduleName
     * @param moduleNode
     * 
     * @return the timing param
     */
    public int getTimingParam(Emulator emulator, ModuleType moduleType, Node moduleNode)
    {
        
        int timingParam = -1;
        String attributeText = "";
        
        if(moduleNode != null)
        {
            if(moduleType.equals(ModuleType.VGA) ||  moduleType.equals(ModuleType.KEYBOARD)
               || moduleType.equals(ModuleType.FDC) || moduleType.equals(ModuleType.ATA))
            {
            
                attributeText = DioscuriXmlParams.UPDATE_INTERVAL_TEXT;
            
            } else if (moduleType.equals(ModuleType.CPU))
            {
                
                attributeText = DioscuriXmlParams.CPU_SPEED_MHZ_TEXT;
                
            } else if (moduleType.equals(ModuleType.PIT))
            {
                
                attributeText = DioscuriXmlParams.PIT_CLOCKRATE_TEXT;
                
            } else 
            {              
                return timingParam;
            }
            
            NamedNodeMap attributes = moduleNode.getAttributes();
            Node updateIntervalNode = attributes.getNamedItem(attributeText);
            timingParam = Integer.parseInt(updateIntervalNode.getTextContent());
                           
        }
        
        return timingParam;
        
    }
    
    /**
     * Load the Bios into RAM.
     * 
     * @param emulator
     * @return success flag
     */
    public boolean loadBios(Document document, Emulator emulator)
    {
        // CONFIGURE: configure modules (when necessary)
        logger.log(Level.INFO, "=================== LOAD BIOS IN RAM ===================");
        // Module BIOS: load System BIOS in BIOS ROM
        BIOS bios = (BIOS)emulator.getModules().getModule(ModuleType.BIOS.toString());
             
        Node biosNode =  DioscuriXmlParams.getModuleNode(document, ModuleType.BIOS);       
        NodeList biosChildNodes = biosNode.getChildNodes();
        
        String sysBiosFilePath = null;
        String vgaBiosFilePath = null;
        int ramAddressSysBiosStart = 0;
        int ramAddressVgaBiosStart = 0;
        
        for (int i = 0; i < biosChildNodes.getLength(); i++)
        {
            Node theNode = biosChildNodes.item(i);         
            String name = theNode.getNodeName();
            Node biosChildNode = theNode.getChildNodes().item(0);
            
            String theValue;
                     
            if(biosChildNode != null)
            {
                theValue = biosChildNode.getNodeValue();
                
                if (name.equals("sysbiosfilepath"))
                {
                    sysBiosFilePath = theValue;
                    
                } else if (name.equals("vgabiosfilepath"))
                {
                    vgaBiosFilePath = theValue;      
            
                } else if (name.equals("ramaddresssysbiosstartdec"))
                {
                    ramAddressSysBiosStart = Integer.parseInt(theValue);
                    
                } else if (name.equals("ramaddressvgabiosstartdec"))
                {
                    ramAddressVgaBiosStart = Integer.parseInt(theValue);
                }
            }
        }
            
        try
        {
            // Fetch System BIOS binaries from file system and store it in BIOS ROM
            if (bios.setSystemBIOS(emulator.getIo().importBinaryStream(sysBiosFilePath)))
            {
                logger.log(Level.CONFIG, "System BIOS successfully stored in ROM.");
                
                // Retrieve System BIOS and store it in RAM at address 0xF0000
                Memory mem = (Memory)emulator.getModules().getModule(ModuleType.MEMORY.toString());
                
                int ramSizeMb = this.readRamSize(document);               
                if(ramSizeMb != -1)
                {
                    mem.setRamSizeInMB(ramSizeMb);
                    
                    logger.log(Level.INFO, "RAM size set to " + ramSizeMb + "MB");
                    
                } else 
                {
                  return false;    
                }
                
                mem.setBytes(ramAddressSysBiosStart, bios.getSystemBIOS());

                logger.log(Level.CONFIG, "System BIOS successfully loaded in RAM.");
            }
            else
            {
                logger.log(Level.SEVERE, "Not able to retrieve System BIOS binaries from file system.");
                return false;
                
            }
        }
        catch (ModuleException emod)
        {
            logger.log(Level.SEVERE, emod.getMessage());
            return false;
        }
        catch (IOException eio)
        {
            logger.log(Level.SEVERE, eio.getMessage());
            return false;
        }
        
        // Module BIOS: load Video BIOS in BIOS ROM
        try
        {
            // Fetch System BIOS binaries from file system and store it in BIOS ROM
            if (bios.setVideoBIOS(emulator.getIo().importBinaryStream(vgaBiosFilePath)))
            {
                logger.log(Level.CONFIG, "Video BIOS successfully stored in ROM.");
                
                // Retrieve VGA BIOS and store it in RAM at address 0xC0000
                Memory mem = (Memory)emulator.getModules().getModule(ModuleType.MEMORY.toString());
                mem.setBytes(ramAddressVgaBiosStart, bios.getVideoBIOS());
                logger.log(Level.CONFIG, "Video BIOS successfully loaded in RAM.");
            }
            else
            {
                logger.log(Level.SEVERE, "Not able to retrieve Video BIOS binaries from file system.");
                return false;
            }
        }
        catch (ModuleException emod)
        {
            logger.log(Level.SEVERE, emod.getMessage());
            return false;
        }
        catch (IOException eio)
        {
            logger.log(Level.SEVERE, eio.getMessage());
            return false;
        }
        
        return true;
    }
    
    /**
     * Get the RAM size.
     * 
     * @return the RAM size
     */
    public int readRamSize(Document document)
    {
        int ramSize = -1;
        
        Node ramNode =   DioscuriXmlParams.getModuleNode(document, ModuleType.MEMORY);
  
        if(ramNode != null)
        {
            NamedNodeMap attributes = ramNode.getAttributes();
            Node ramSizeNode = attributes.getNamedItem(DioscuriXmlParams.RAM_SIZE_TEXT);
            ramSize = Integer.parseInt(ramSizeNode.getTextContent());
        }
        
        return ramSize;
    }
    
    /**
     * Set the debug mode.
     * 
     * @param emulator
     */
    public void readDebugMode(Document document, Emulator emulator)
    {
             
        Node emulatorNode = XmlConnect.getFirstNode(document, DioscuriXmlParams.EMULATOR_NODE);       
        if (emulatorNode == null)
        {
            logger.log(Level.SEVERE, "A problem was encounted reading from the configuration file.");
            return;
        }
        
        boolean isDebugMode = this.getDebug(emulatorNode);
        
        // If debug mode is on set all modules in debug mode
        if (isDebugMode)
        {
            for (int i = 0; i < emulator.getModules().size(); i++)
            {
                emulator.getModules().getModule(i).setDebugMode(true);
            }
            logger.log(Level.INFO, "Modules in debug mode.");
        
        } else 
        {
            
            Node keyboardNode = DioscuriXmlParams.getModuleNode(document, ModuleType.KEYBOARD); 
            boolean isDebug = this.getDebug(keyboardNode);
            ((ModuleKeyboard)emulator.getModules().getModule(ModuleType.KEYBOARD.toString())).setDebugMode(isDebug);   
            
            Node mouseNode =   DioscuriXmlParams.getModuleNode(document, ModuleType.MOUSE); 
            isDebug = this.getDebug(mouseNode);
            ((ModuleMouse)emulator.getModules().getModule(ModuleType.MOUSE.toString())).setDebugMode(isDebug);   

            Node pitNode =   DioscuriXmlParams.getModuleNode(document, ModuleType.PIT); 
            isDebug = this.getDebug(pitNode);
            ((ModulePIT)emulator.getModules().getModule(ModuleType.PIT.toString())).setDebugMode(isDebug);   
            
            Node floppyDiskDrivesNode =   DioscuriXmlParams.getModuleNode(document, ModuleType.FDC); 
            isDebug = this.getDebug(floppyDiskDrivesNode);
            ((ModuleFDC)emulator.getModules().getModule(ModuleType.FDC.toString())).setDebugMode(isDebug);
            
            Node ataDrivesNode =   DioscuriXmlParams.getModuleNode(document, ModuleType.ATA); 
            isDebug = this.getDebug(ataDrivesNode);
            ((ModuleATA)emulator.getModules().getModule(ModuleType.ATA.toString())).setDebugMode(isDebug);   
            
            Node videoNode =   DioscuriXmlParams.getModuleNode(document, ModuleType.VGA); 
            isDebug = this.getDebug(videoNode);
            ((ModuleVideo)emulator.getModules().getModule(ModuleType.VGA.toString())).setDebugMode(isDebug); 
    
        }
        
        //set CPU instruction debug
        Node cpuNode =   DioscuriXmlParams.getModuleNode(document, ModuleType.CPU); 
        boolean cpuInstructionDebug = this.getDebug(cpuNode);
        ((ModuleCPU)emulator.getModules().getModule(ModuleType.CPU.toString())).setCpuInstructionDebug(cpuInstructionDebug);
        logger.log(Level.INFO, "CPU Instruction debug mode is on.");
        
        //Set RAM address watch
        Node ramNode =   DioscuriXmlParams.getModuleNode(document, ModuleType.MEMORY); 
        if(ramNode != null)
        {
            boolean ramWatch = this.getDebug(ramNode);
            
            int addressToWatch = -1;
            
            NamedNodeMap attributes = ramNode.getAttributes();
            Node debugNode = attributes.getNamedItem(DioscuriXmlParams.RAM_ADDRESS_TEXT);
            
            if(debugNode != null)
            {
                addressToWatch = Integer.parseInt(debugNode.getTextContent());
                
                ((ModuleMemory)emulator.getModules().getModule(ModuleType.MEMORY.toString())).setWatchValueAndAddress(ramWatch, addressToWatch);
                logger.log(Level.INFO, "RAM address watch debug is on.");
            }
        }
        
    }
    
    /**
     * Get specified debug setting.
     * 
     * @param moduleNode
     * @return the debug setting
     */
    public boolean getDebug(Node moduleNode)
    {
        boolean isDebugMode = false;
        
        if(moduleNode != null)
        {
            NamedNodeMap attributes = moduleNode.getAttributes();
            Node debugNode = attributes.getNamedItem(DioscuriXmlParams.DEBUG_TEXT);
            isDebugMode = Boolean.parseBoolean(debugNode.getTextContent());
            
        }
        return isDebugMode;
    }
    
    /**
     * Get and set floppy params.
     * 
     * @param emulator
     */
    public void readFloppyParams(Document document, Emulator emulator)
    {
        
        // Module FDC: set number of drives (4 at max), insert floppy and set update interval
        ModuleFDC fdc = (ModuleFDC)emulator.getModules().getModule(ModuleType.FDC.toString());
        fdc.setNumberOfDrives(1);   // TODO: this could be a user-defined variable
        
        Node floppyDiskDrivesNode =   DioscuriXmlParams.getModuleNode(document, ModuleType.FDC); 
        NodeList floppyDrives = floppyDiskDrivesNode.getChildNodes();
        
        
        for (int i = 0; i < floppyDrives.getLength(); i++)
        {
            String name = floppyDrives.item(i).getNodeName();
            if(name != null && name.equalsIgnoreCase("floppy"))
            {
                this.addFloppyDrive(emulator, fdc, floppyDrives.item(i));
            }
        }
                  
    }
    
    /**
     * Add a floppy drive.
     * 
     * @param emulator
     * @param fdc
     * @param floppyDriveNode
     */
    public void addFloppyDrive(Emulator emulator, ModuleFDC fdc, Node floppyDriveNode)
    {
        
        NodeList floppyNodes = floppyDriveNode.getChildNodes();
        
        boolean enabled = false;
        boolean inserted = false;
        String driveLetter = null;
        byte carrierType = 0x0;
        boolean writeProtected = false;
        String imageFilePath = null;
        
        for (int i = 0; i < floppyNodes.getLength(); i++)
        {
            Node theNode = floppyNodes.item(i);         
            String name = theNode.getNodeName();
            Node floppyNode = theNode.getChildNodes().item(0);
            
            String theValue;
                     
            if(floppyNode != null)
            {
                theValue = floppyNode.getNodeValue();
                
                if (name.equals("enabled"))
                {
                   enabled = Boolean.parseBoolean(theValue);    
                   
                } else if (name.equals("inserted"))
                {
                    inserted = Boolean.parseBoolean(theValue);
                    
                } else if (name.equals("driveletter"))
                {
                    driveLetter = theValue.toUpperCase();
                    
                } else if (name.equals("diskformat"))
                {
                   if(theValue.equals("360K") 
                      || theValue.equals("0.36M")
                      || theValue.equals("360")
                       ||  theValue.equals("0.36")) 
                   {
                       carrierType = 0x01;
                       
                   } else if (theValue.equals("1.2M")
                           || theValue.equals("1.2")
                           || theValue.equals("1_2")
                           ||  theValue.equals("1_2M"))
                   {
                
                       carrierType   = 0x02;
                       
                   } else if (theValue.equals("720K") 
                           || theValue.equals("720")
                           || theValue.equals("0.72")
                           ||  theValue.equals("0.72M"))
                        {
                            carrierType = 0x03;
                                         
                   } else if (theValue.equals("1.44M") 
                       || theValue.equals("1.44")
                       || theValue.equals("1_44")
                       ||  theValue.equals("1_44M"))
                    {
                        carrierType = 0x04;
                        
                    } else if (theValue.equals("2.88M") 
                            || theValue.equals("2.88")
                            || theValue.equals("2_88")
                            ||  theValue.equals("2_88M"))
                    {
                             carrierType = 0x05;
                             
                    } else if (theValue.equals("160K") 
                            || theValue.equals("160")
                            || theValue.equals("0.16")
                            ||  theValue.equals("0.16M"))
                    {
                             carrierType = 0x06;  
                             
                    } else if (theValue.equals("180K") 
                            || theValue.equals("180")
                            || theValue.equals("0.18")
                            ||  theValue.equals("0.18M"))
                    {
                             carrierType = 0x07;   
                             
                    } else if (theValue.equals("320K") 
                            || theValue.equals("320")
                            || theValue.equals("0.32")
                            ||  theValue.equals("0.32M"))
                    {
                             carrierType = 0x08;   
                    } else 
                    {
                        logger.log(Level.SEVERE, "Floppy disk format not recognised.");
                    }

                    
                } else if (name.equals("writeprotected"))
                {
                    writeProtected = Boolean.parseBoolean(theValue);
                    
                } else if (name.equals("imagefilepath"))
                {
                    imageFilePath = theValue;
                }
                
            }
        } 
        
        if(inserted)
        {
            
            File imageFile = new File(imageFilePath);
            
            fdc.insertCarrier(driveLetter, 
                              carrierType, 
                              imageFile, 
                              writeProtected);
            
            //TODO: updates for B ?
            if(driveLetter.equals("A"))
            {                    
                emulator.getGui().updateGUI(GUI.EMU_FLOPPYA_INSERT);
            }
        }       
      
    }
    
    /**
     * REad from config and set the hard drive params.
     * 
     * @param emulator
     * @param ide
     */
    public void readHardDriveParams(Document document, Emulator emulator, ATA ide)
    {
        

        // Module ATA: set drive params, disk image and set update interval
        // TODO: replace IDE reference by ModuleIDE
        Node ataDrivesNode =   DioscuriXmlParams.getModuleNode(document, ModuleType.ATA); 
        NodeList hardDrives = ataDrivesNode.getChildNodes();
        
        
        for (int i = 0; i < hardDrives.getLength(); i++)
        {
            this.addHardDrive(emulator, ide, hardDrives.item(i));
        }     
    }
    
    /**
     * Add a hard drive.
     * 
     * @param emulator
     * @param ide
     * @param hardDriveNode
     */
    public void addHardDrive(Emulator emulator, ModuleATA ide, Node hardDriveNode)
    {
        
        NodeList hardDriveChildNodes = hardDriveNode.getChildNodes();
        
        boolean enabled = false;
        int ideChannelIndex = 0;
        boolean isMaster = true;
        boolean autoDetectCylinders = false;
        int numCylinders = 0;
        int numHeads = 0;
        int numSectorsPerTrack = 0;        
        String imageFilePath = null;
 
        for (int i = 0; i < hardDriveChildNodes.getLength(); i++)
        {
            Node theNode = hardDriveChildNodes.item(i);
            
            String name = theNode.getNodeName();
   
            Node floppyNode = theNode.getChildNodes().item(0);
            String theValue;
                     
            if(floppyNode != null)
            {
                theValue = floppyNode.getNodeValue();
                
                if (name.equals("enabled"))
                {
                   enabled = Boolean.parseBoolean(theValue);    
                   
                } else if (name.equals("channelindex"))
                {
                    ideChannelIndex = Integer.parseInt(theValue);
                    
                } else if (name.equals("master"))
                {
                    isMaster = Boolean.parseBoolean(theValue); 
                    
                } else if (name.equals("autodetectcylinders"))
                {
  
                   autoDetectCylinders = Boolean.parseBoolean(theValue); 
                   
                } else if (name.equals("cylinders"))
                {
                    numCylinders = Integer.parseInt(theValue);
                    
                } else if (name.equals("heads"))
                {
                    numHeads = Integer.parseInt(theValue);
                    
                } else if (name.equals("sectorspertrack"))
                {
                    numSectorsPerTrack = Integer.parseInt(theValue);
          
                } else if (name.equals("imagefilepath"))
                {
                    imageFilePath = theValue;                   
                }             
            }
        } 
                    
        if(enabled && ideChannelIndex >= 0 && ideChannelIndex < 4)
        {
       
            if(autoDetectCylinders)
            {
                numCylinders = 0;
            }
            
            ide.initConfig(ideChannelIndex,
                           isMaster,
                           true,  
                           false,
                           numCylinders,
                           numHeads,
                           numSectorsPerTrack,
                           ATATranslationType.AUTO,
                           imageFilePath);
            
            //TODO: updates for other hard drives?
            if (ideChannelIndex == 0 && isMaster)
            {
                emulator.getGui().updateGUI(GUI.EMU_HD1_INSERT);
            }
        }
                  
    }
    
    /**
     * Read from config and set the boot params.
     * 
     * @param ide
     */
    public void readBootParams(Document document, ATA ide)
    {
        
        Node bootDrivesNode = XmlConnect.getFirstNode(document, DioscuriXmlParams.BOOT_DRIVES_NODE);      
        NodeList bootDriveNodes = bootDrivesNode.getChildNodes();

        boolean floppyCheckDisabled = false;
        
        int[] bootDrives = new int[3];
        bootDrives[0] = ATAConstants.BOOT_NONE; 
        bootDrives[1] = ATAConstants.BOOT_NONE; 
        bootDrives[2] = ATAConstants.BOOT_NONE; 
        
        int bootDriveCounter =0;      
        
        
        for (int i = 0; i < bootDriveNodes.getLength(); i++)
        {
                     
            Node theNode = bootDriveNodes.item(i);
            
            String name = theNode.getNodeName();
   
            Node bootDriveNode = theNode.getChildNodes().item(0);
            String theValue;
                     
            if(bootDriveNode != null)
            {
                theValue = bootDriveNode.getNodeValue();
                
                if (name.equals("bootdrive"))
                {
                    
                    if(theValue.equalsIgnoreCase("HARDDRIVE")
                       || theValue.equalsIgnoreCase("HD") 
                       || theValue.equalsIgnoreCase("C:")
                       || theValue.equalsIgnoreCase("C")
                       || theValue.equalsIgnoreCase("DISKC"))
                    {
                        bootDrives[bootDriveCounter] = ATAConstants.BOOT_DISKC;
                        
                    } else if(theValue.equalsIgnoreCase("Floppy") 
                              || theValue.equalsIgnoreCase("FloppyDrive")
                              || theValue.equalsIgnoreCase("a:")
                              || theValue.equalsIgnoreCase("a"))
                    {
                        bootDrives[bootDriveCounter] = ATAConstants.BOOT_FLOPPYA;
                    
                    } else if(theValue.equalsIgnoreCase("cd") 
                            || theValue.equalsIgnoreCase("cdrom"))
                    {
                      bootDrives[bootDriveCounter] = ATAConstants.BOOT_CDROM;
                          
                    } else if (theValue.equalsIgnoreCase("none") )
                    {    
                        bootDrives[bootDriveCounter] = ATAConstants.BOOT_NONE;
                    }
                    
                    bootDriveCounter++;
                }
            }
        }
                     
        Node floppyCheckDisabledNode = XmlConnect.getFirstNode(document, DioscuriXmlParams.FLOPPY_CHECK_DISABLED_NODE);
        
        String name = floppyCheckDisabledNode.getNodeName();

        String theValue;
                 
        if(floppyCheckDisabledNode.getChildNodes().item(0) != null)
        {
            theValue = floppyCheckDisabledNode.getChildNodes().item(0).getNodeValue();
            
            if (name.equals("floppycheckdisabled"))
            {
                    floppyCheckDisabled = Boolean.parseBoolean(theValue);  
            }
         }
        
//      control CMOS settings
        ide.setCmosSettings(bootDrives, floppyCheckDisabled);
        
    }
    
}
