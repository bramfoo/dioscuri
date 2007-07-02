/*
 * $Revision: 1.1 $ $Date: 2007-07-02 14:31:25 $ $Author: blohman $
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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DioscuriXmlWriter
{
    // Logging
    private static Logger logger = Logger.getLogger("nl.kbna.dioscuri.config");
    
    
    public boolean writeModuleParams(Object[] params, ModuleType moduleType)
    {

        File configFile = new File(ConfigController.CONFIG_FILE_PATH);
        File schemaFile = new File(ConfigController.SCHEMA_FILE_PATH);   
        Document document = null;
        FileInputStream xmlConfigInputStream = null; 
              
        try 
        {
            xmlConfigInputStream = new FileInputStream(configFile);                      
            document = XmlConnect.loadXmlDocument(xmlConfigInputStream, schemaFile);
            
            boolean success = false;
            
            if(moduleType == ModuleType.ATA)
            {
              success = this.saveAtaParams(document, params);
              
            } else if (moduleType == ModuleType.BIOS)
            {                
                success = this.saveBiosParams(document, params);
            
            } else if (moduleType == ModuleType.BOOT)
            {                
                success = this.saveBootParams(document, params);
            
            } else if(moduleType == ModuleType.CPU)
            {
                
                int cpuSpeedMhz = ((Integer)(params[0])).intValue();               
                success = this.saveCpuParams(document, cpuSpeedMhz);
                
            } else if(moduleType == ModuleType.FDC)
            {
              success = this.saveFdcParams(document, params);
              
            } else if(moduleType == ModuleType.MEMORY)
            {
                int ramSizeMb = ((Integer)(params[0])).intValue();
                success = this.saveRamParams(document, ramSizeMb);
                
            } else if(moduleType == ModuleType.KEYBOARD
                    || moduleType == ModuleType.PIT
                    || moduleType == ModuleType.VGA)
            {
                int timingParam = ((Integer)(params[0])).intValue();
                
                success = this.saveTimingParams(document, moduleType, timingParam);
            }
            
            if (!success)
            {
                logger.log(Level.SEVERE, "Error saving CPU data to configuration file.");
                return false; 
            }
            
            
            if (!XmlConnect.writeDocumentToFile(document))
            {    
                return false;
            }
       
        } catch (Exception e)
        {
            logger.log(Level.SEVERE, e.getMessage());
            
            return false;
        }
        
        try 
        {
            xmlConfigInputStream.close();
            
        } catch (IOException e )
        {
            logger.log(Level.SEVERE, "Error closing configuration file during save.");
            return false;
        }
        
        return true;
        
    }
    
    
    private boolean saveBiosParams(Document document, Object[] params)
    {
        
        Node biosNode = DioscuriXmlParams.getModuleNode(document, ModuleType.BIOS);
        
        NodeList eachBiosNode = biosNode.getChildNodes();
                            
        for (int i = 0; i < eachBiosNode.getLength(); i++)
        {
            Node theNode = eachBiosNode.item(i);         
            String name = theNode.getNodeName();
            Node currentNode = theNode.getChildNodes().item(0);
                                 
            if(currentNode != null)
            {
                
                if (name.equals("sysbiosfilepath"))
                {
                   String sysBiosString = (String)params[0];   
                   currentNode.setNodeValue(sysBiosString);
                   
                }else if (name.equals("vgabiosfilepath"))
                {
                    String vgsBiosString = (String)params[1];   
                    currentNode.setNodeValue(vgsBiosString);
                                    
                }else if (name.equals("ramaddresssysbiosstartdec"))
                {
                    String sysRamAddressStart = ((Integer)params[2]).toString();   
                    currentNode.setNodeValue(sysRamAddressStart);                   
                    
                }else if (name.equals("ramaddressvgabiosstartdec"))
                {
                    String vgaRamAddressStart = ((Integer)params[3]).toString();   
                    currentNode.setNodeValue(vgaRamAddressStart);                              
                }
            }
        }
        
        return true;
        
    }
    
    /**
     * Save ATA Params to XML.
     * 
     * @param document
     * @param params
     * @return True if success
     */
    private boolean saveAtaParams(Document document, Object[] params)
    {
           
        Node ataNode = DioscuriXmlParams.getModuleNode(document, ModuleType.ATA);
        Node hardDriveNode = null;
        for (int i = 0; i < ataNode.getChildNodes().getLength(); i++)
        {
            hardDriveNode = ataNode.getChildNodes().item(i);
            
            if(hardDriveNode.getChildNodes() != null && hardDriveNode.getChildNodes().getLength() > 0 )
            {
                break;
            }
        }
               
        NodeList hardDriveParamsNodes = hardDriveNode.getChildNodes();
        
        int updateInt = ((Integer)params[0]);
              
        String isEnabled = ((Boolean)params[1]).toString();
        String channelIndex = ((Integer)params[2]).toString();
        String isMaster = ((Boolean)params[3]).toString();
        String autoDetect = ((Boolean)params[4]).toString();      
        String cylinders = ((Integer)params[5]).toString();
        String heads = ((Integer)params[6]).toString();
        String sectors = ((Integer)params[7]).toString();
        String imageFormatPath = (String)params[8];
        
        this.saveTimingParams(document, ModuleType.ATA, updateInt);
               
        for (int i = 0; i < hardDriveParamsNodes.getLength(); i++)
        {
            
            Node theNode = hardDriveParamsNodes.item(i);         
            String name = theNode.getNodeName();
            Node valueNode = theNode.getChildNodes().item(0);
                               
            if(valueNode != null)
            {
      
                if (name.equals("enabled"))
                {
                    valueNode.setNodeValue(isEnabled);    
                   
                } else if (name.equals("channelindex"))
                {
                    valueNode.setNodeValue(channelIndex);
                    
                } else if (name.equals("master"))
                {
                    valueNode.setNodeValue(isMaster);; 
                    
                } else if (name.equals("autodetectcylinders"))
                {
                    valueNode.setNodeValue(autoDetect);
                   
                } else if (name.equals("cylinders"))
                {
                    valueNode.setNodeValue(cylinders);
                    
                } else if (name.equals("heads"))
                {
                    valueNode.setNodeValue(heads);
                    
                } else if (name.equals("sectorspertrack"))
                {
                    valueNode.setNodeValue(sectors);
          
                } else if (name.equals("imagefilepath"))
                {
                    valueNode.setNodeValue(imageFormatPath);                   
                }                   
            }       
        }

        return true;
    }
    
    
    private boolean saveBootParams(Document document, Object[] params)
    {

        Node bootDrivesNode = XmlConnect.getFirstNode(document,DioscuriXmlParams.BOOT_DRIVES_NODE);
                       
        NodeList eachBootDriveNode = bootDrivesNode.getChildNodes();
        
        int bootCount = 0;
                       
        for (int i = 0; i < eachBootDriveNode.getLength(); i++)
        {
            Node theNode = eachBootDriveNode.item(i);         
            String name = theNode.getNodeName();
            Node currentNode = theNode.getChildNodes().item(0);
                                 
            if(currentNode != null)
            {

                if (name.equals("bootdrive"))
                {
                   
                   int curDriveIndex = ((Integer)params[bootCount]).intValue();
                        
                   String drive = "none";
                   if (curDriveIndex == 0)
                   {
                       drive = "Floppy";
                   
                   } else if (curDriveIndex == 1)
                   {
                      drive =  "harddrive" ;   
                   }
                   
                   currentNode.setNodeValue(drive);
                
                   bootCount++;
                }
            }
        }
        
        Node floppyCheckDisabledNode = XmlConnect.getFirstNode(document,DioscuriXmlParams.FLOPPY_CHECK_DISABLED_NODE);
                
        String name = floppyCheckDisabledNode.getNodeName();
        Node currentNode = floppyCheckDisabledNode.getChildNodes().item(0);
                         
        if(currentNode != null)
        {
         
            if (name.equals("floppycheckdisabled"))
            {
                currentNode.setNodeValue(((Boolean)params[3]).toString());
            }
        }
        
        return true;
            
    }
    
    /**
     * Save FDC Params to XML.
     * 
     * @param document
     * @param params
     * @return True if success
     */
    private boolean saveFdcParams(Document document, Object[] params)
    {
           
        Node floppyDiskDrivesNode = DioscuriXmlParams.getModuleNode(document, ModuleType.FDC);
        Node floppyDriveNode = null;
        for (int i = 0; i < floppyDiskDrivesNode.getChildNodes().getLength(); i++)
        {
            floppyDriveNode = floppyDiskDrivesNode.getChildNodes().item(i);
            
            if(floppyDriveNode.getChildNodes() != null && floppyDriveNode.getChildNodes().getLength() > 0 )
            {
                break;
            }
        }
             
        NodeList floppyParamsNodes = floppyDriveNode.getChildNodes();
             
        int updateInt = ((Integer)params[0]);
        String enabled = ((Boolean)params[1]).toString();
        String inserted = ((Boolean)params[2]).toString();
        String driveLetter = (String)params[3];
        String diskFormat = (String)params[4];
        String writeProtected = ((Boolean)params[5]).toString();
        String selectedfile = (String)params[6];  
        
        this.saveTimingParams(document, ModuleType.FDC, updateInt);
        
        for (int i = 0; i < floppyParamsNodes.getLength(); i++)
        {
            
            Node theNode = floppyParamsNodes.item(i);         
            String name = theNode.getNodeName();
            Node valueNode = theNode.getChildNodes().item(0);
                               
            if(valueNode != null)
            {
                
                if (name.equals("enabled"))
                {
                    valueNode.setNodeValue(enabled);  
                   
                } else if (name.equals("inserted"))
                {
                    valueNode.setNodeValue(inserted); 
                    
                } else if (name.equals("driveletter"))
                {        
                    valueNode.setNodeValue(driveLetter); 
                    
                } else if (name.equals("diskformat"))
                {
                    valueNode.setNodeValue(diskFormat); 
                    
                } else if (name.equals("writeprotected"))
                {
                    valueNode.setNodeValue(writeProtected); 
                    
                } else if (name.equals("imagefilepath"))
                {
                    valueNode.setNodeValue(selectedfile); 
                }
            }       
        }

        return true;
    }
    
    
    private boolean saveTimingParams(Document document, ModuleType moduleType, int timingParam)
    {
            
        Node moduleNode = DioscuriXmlParams.getModuleNode(document, moduleType);
        NamedNodeMap attributes = moduleNode.getAttributes();
        
        String timingNodeText = DioscuriXmlParams.UPDATE_INTERVAL_TEXT;
        
        if (moduleType == ModuleType.PIT)
        {
            timingNodeText = DioscuriXmlParams.PIT_CLOCKRATE_TEXT;
        }
        
        Node timingNode = attributes.getNamedItem(timingNodeText);     
        String timingParamString = (new Integer(timingParam)).toString();
        timingNode.setTextContent(timingParamString);
      
        return true;
    }
        
    /**
     * Save CPU params to XML.
     * 
     * @param document
     * @param cpuSpeedMhz
     * @return true if successful
     */
    private boolean saveCpuParams(Document document, int cpuSpeedMhz)
    {
            
        Node cpuNode = DioscuriXmlParams.getModuleNode(document, ModuleType.CPU);
        NamedNodeMap attributes = cpuNode.getAttributes();
        
        Node cpuSpeedNode = attributes.getNamedItem(DioscuriXmlParams.CPU_SPEED_MHZ_TEXT);
        String cpuSpeedString = (new Integer(cpuSpeedMhz)).toString();
        cpuSpeedNode.setTextContent(cpuSpeedString);
      
        return true;
    }
    
    /**
     * Save RAM params to XML.
     * 
     * @param document
     * @param cpuSpeedMhz
     * @return true if successful
     */
    private boolean saveRamParams(Document document, int ramSpeedMhz)
    {
            
        Node ramNode = DioscuriXmlParams.getModuleNode(document, ModuleType.MEMORY);
        NamedNodeMap attributes = ramNode.getAttributes();
        
        Node ramSizeNode = attributes.getNamedItem(DioscuriXmlParams.RAM_SIZE_TEXT);
        String ramSpeedString = (new Integer(ramSpeedMhz)).toString();
        ramSizeNode.setTextContent(ramSpeedString);
      
        return true;
    }
}
