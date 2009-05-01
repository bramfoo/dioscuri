/*
 * $Revision: 1.6 $ $Date: 2009-04-03 11:06:27 $ $Author: jrvanderhoeven $
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DioscuriXmlReader
{

	// Config variables
	private ConfigController configController;
	private XmlConnect xmlConnect;
	
    // Logging
    private static Logger logger = Logger.getLogger("nl.kbna.dioscuri.config");
    
    
    // Constructor
    public DioscuriXmlReader(ConfigController configController, XmlConnect xmlConnect)
    {
    	this.configController = configController;
    	this.xmlConnect = xmlConnect;
    }
    
    
    // Methods
    
    public Object[] getModuleParams(ModuleType moduleType)
    {
             
        Object[] params = null;
        
        // Retrieve configuration and schema file
        File configFile = new File(configController.getConfigFilePath());
        File schemaFile = new File(configController.getSchemaFilePath());
        
        Document document = null;
        FileInputStream xmlConfigInputStream = null;
        
        try
        {
            xmlConfigInputStream = new FileInputStream(configFile);
            document = xmlConnect.loadXmlDocument(xmlConfigInputStream, schemaFile);
            
            if (moduleType == ModuleType.ATA)
            {
                params = this.getHardDriveParams(document);
                
            } else if (moduleType == ModuleType.BIOS)
            {                                     
                params = this.getBiosParams(document);
            
            }else if (moduleType == ModuleType.BOOT)
            {                                     
                params = this.getBootParams(document);
            
            } else if (moduleType == ModuleType.CPU)
            {
                params = this.getCpuParams(document);
            
            } else if (moduleType == ModuleType.FDC)
            {            
                params = this.getFloppyParams(document);
                                      
            } else if (moduleType == ModuleType.MEMORY)
            { 
                params = this.getRamParams(document);   
                
            } else if (moduleType == ModuleType.MOUSE)
            { 
                params = this.getMouseParams(document);   
                
            } else if (moduleType == ModuleType.KEYBOARD
                    || moduleType == ModuleType.PIT
                    || moduleType == ModuleType.VGA)
            { 
                params = this.getTimingParam(document, moduleType);           
            }
            
        } catch (Exception e)
        {
            logger.log(Level.SEVERE, e.getMessage());
            
            return null;
        }
        
        try 
        {
            xmlConfigInputStream.close();
            
        } catch (IOException e )
        {
            logger.log(Level.SEVERE, "Error closing configuration file during read of CPU params.");
            return null;
        }
        
        return params;
       
    }
    
    private Object[] getBiosParams(Document document)
    {
        Object[] params = new Object[4];

        Node biosNode = DioscuriXmlParams.getModuleNode(document, ModuleType.BIOS);
                       
        NodeList eachBiosNode = biosNode.getChildNodes();
                              
        for (int i = 0; i < eachBiosNode.getLength(); i++)
        {
            Node theNode = eachBiosNode.item(i);         
            String name = theNode.getNodeName();
            Node currentNode = theNode.getChildNodes().item(0);
            
            String theValue;
                     
            if(currentNode != null)
            {
                theValue = currentNode.getNodeValue();
                
               if (name.equalsIgnoreCase("sysbiosfilepath"))
               {
                    params[0] = theValue;
     
               } else if(name.equalsIgnoreCase("vgabiosfilepath"))
               {
                   params[1] = theValue;
                   
               } else if(name.equalsIgnoreCase("ramaddresssysbiosstartdec"))
               {
                   params[2] = Integer.valueOf(theValue);
                   
               } else if(name.equalsIgnoreCase("ramaddressvgabiosstartdec"))
               {
                   params[3] = Integer.valueOf(theValue);
               }
                    
            }
        }
        
        return params;
    }
    
    private Object[] getTimingParam(Document document, ModuleType moduleType)
    {
        
        Object[] params= new Object[1];
        
        Node moduleNode = DioscuriXmlParams.getModuleNode(document, moduleType);

        String timingParamText = DioscuriXmlParams.UPDATE_INTERVAL_TEXT;
        if (moduleType == ModuleType.PIT)
        {
            timingParamText = DioscuriXmlParams.PIT_CLOCKRATE_TEXT;
        }
        
        Node subNode = null;
        for (int i = 0; i < moduleNode.getChildNodes().getLength(); i++)
        {
            subNode = moduleNode.getChildNodes().item(i);
            if (subNode.getNodeName().equals(timingParamText))
                break;
        }
        
        int upateInt = 0;
        if (subNode != null)
        {
            upateInt = Integer.parseInt(subNode.getChildNodes().item(0).getNodeValue());
        }
        
        params[0] = new Integer(upateInt);
           
        return params;
    }
    
    private Object[] getBootParams(Document document)
    {
        
        Object[] params = new Object[4];

        Node bootDrivesNode = xmlConnect.getFirstNode(document,DioscuriXmlParams.BOOT_DRIVES_NODE);
        NodeList biosParamsNodes = bootDrivesNode.getChildNodes();
        
        int bootCount = 0;
        
        for (int i = 0; i < biosParamsNodes.getLength(); i++)
        {
            Node theNode = biosParamsNodes.item(i);         
            Node biosSubSubNode = theNode.getChildNodes().item(0);
            
            String theValue;
                     
            if(biosSubSubNode != null)
            {
                theValue = biosSubSubNode.getNodeValue();
                
                   int bootIndex = 0; 
                   
                   if(theValue.equalsIgnoreCase("Floppy")) 
                   {
                       bootIndex = 0;
                       
                   } else if(theValue.equalsIgnoreCase("HARDDRIVE"))
                        {
                       bootIndex = 1;
                       
                   } else if(theValue.equalsIgnoreCase("none"))
                   {
                       bootIndex = 2;
                   }
                    
                   params[bootCount] = Integer.valueOf(bootIndex); 
                   
                   bootCount++;
            }
        }    
                       
        Node floppyCheckDisabledNode = xmlConnect.getFirstNode(document,DioscuriXmlParams.FLOPPY_CHECK_DISABLED_NODE);
                
        String name = floppyCheckDisabledNode.getNodeName();
        Node currentNode = floppyCheckDisabledNode.getChildNodes().item(0);
        
        String theValue;
                 
        if(currentNode != null)
        {
            theValue = currentNode.getNodeValue();
            
            if (name.equals("floppycheckdisabled"))
            {
               params[3] = Boolean.valueOf(theValue); 
            }
        }
        
        return params;
        
    }
    
    /**
     * Get the CPU params from XML Config.
     * @param document
     * @return
     */
    private Object[] getCpuParams(Document document)
    {
        Object[] params = new Object[2];
               
        Node cpuSpeedNode = xmlConnect.getFirstNode(document,DioscuriXmlParams.CPU_SPEED_MHZ_TEXT);
        Node subNode = cpuSpeedNode.getChildNodes().item(0);
                         
        int cpuSpeed = 0;
        if (subNode != null)
        {
            
            cpuSpeed = Integer.parseInt(subNode.getNodeValue());
        }
        
        params[0] = Integer.valueOf(cpuSpeed);
        
        Node cpu32bitNode = xmlConnect.getFirstNode(document,DioscuriXmlParams.CPU_32_BIT_TEXT);
        subNode = cpu32bitNode.getChildNodes().item(0);
        
        boolean cpu32bit = true;
        if (subNode != null)
        {
            
            cpu32bit = Boolean.parseBoolean(subNode.getNodeValue());
        }
        
        params[1] = Boolean.valueOf(cpu32bit);
        
        return params;
        
        
    }
    
    private Object[] getFloppyParams(Document document)
    {
        
        Object[] params = new Object[7];
        
        params[0] = this.getTimingParam(document, ModuleType.FDC)[0];

        
        Node floppyDriveNode = xmlConnect.getFirstNode(document,DioscuriXmlParams.FLOPPY);
        NodeList floppyParamsNodes = floppyDriveNode.getChildNodes();
        
        for (int i = 0; i < floppyParamsNodes.getLength(); i++)
        {
            Node theNode = floppyParamsNodes.item(i);         
            String name = theNode.getNodeName();
            Node floppyNode = theNode.getChildNodes().item(0);
            
            String theValue;
                     
            if(floppyNode != null)
            {
                theValue = floppyNode.getNodeValue();
                
                if (name.equals("enabled"))
                {
                    params[1] = Boolean.valueOf(theValue);    
                   
                } else if (name.equals("inserted"))
                {
                    params[2] = Boolean.valueOf(theValue);
                    
                } else if (name.equals("driveletter"))
                {
                    if (theValue.toUpperCase().equals("A"))
                    {
                        params[3] = Integer.valueOf(0);
                        
                    } else if (theValue.toUpperCase().equals("B"))
                    {
                        params[3] = Integer.valueOf(1);
                    }
                    
                } else if (name.equals("diskformat"))
                {
                   if(theValue.equals("360K") 
                      || theValue.equals("0.36M")
                      || theValue.equals("360")
                       ||  theValue.equals("0.36")) 
                   {
                       params[4] = Integer.valueOf(6);
                       
                   } else if (theValue.equals("1.2M")
                           || theValue.equals("1.2")
                           || theValue.equals("1_2")
                           ||  theValue.equals("1_2M"))
                   {
                
                       params[4]   = Integer.valueOf(0);
                       
                   } else if (theValue.equals("720K") 
                           || theValue.equals("720")
                           || theValue.equals("0.72")
                           ||  theValue.equals("0.72M"))
                        {
                       
                       params[4] = Integer.valueOf(7);
                                         
                   } else if (theValue.equals("1.44M") 
                       || theValue.equals("1.44")
                       || theValue.equals("1_44")
                       ||  theValue.equals("1_44M"))
                    {
                       params[4] = Integer.valueOf(1);
                        
                    } else if (theValue.equals("2.88M") 
                            || theValue.equals("2.88")
                            || theValue.equals("2_88")
                            ||  theValue.equals("2_88M"))
                    {
                        params[4] = Integer.valueOf(2);
                             
                    } else if (theValue.equals("160K") 
                            || theValue.equals("160")
                            || theValue.equals("0.16")
                            ||  theValue.equals("0.16M"))
                    {
                        params[4] = Integer.valueOf(3);  
                             
                    } else if (theValue.equals("180K") 
                            || theValue.equals("180")
                            || theValue.equals("0.18")
                            ||  theValue.equals("0.18M"))
                    {
                        params[4] = Integer.valueOf(4);   
                             
                    } else if (theValue.equals("320K") 
                            || theValue.equals("320")
                            || theValue.equals("0.32")
                            ||  theValue.equals("0.32M"))
                    {
                        params[4] = Integer.valueOf(5);   
                        
                    } else 
                    {
                        logger.log(Level.SEVERE, "Floppy disk format not recognised.");
                    }

                    
                } else if (name.equals("writeprotected"))
                {
                    params[5] = Boolean.valueOf(theValue);
                    
                } else if (name.equals("imagefilepath"))
                {
                    params[6] = theValue;
                }                       
            }
        } 
        
        return params;
    }
    
    private Object[] getHardDriveParams(Document document)
    {
        
        Object[] params = new Object[9];
        
        params[0] = this.getTimingParam(document, ModuleType.ATA)[0]; 

        Node ataNode = DioscuriXmlParams.getModuleNode(document, ModuleType.ATA);
        Node hardDriveNode = null;
        for (int i = 0; i < ataNode.getChildNodes().getLength(); i++)
        {
            hardDriveNode = ataNode.getChildNodes().item(i);
            
            if (hardDriveNode.getNodeName().equals(DioscuriXmlParams.HARDDISKDRIVE))
            {
                break;
            }
        }
               
        NodeList hardDriveParamsNodes = hardDriveNode.getChildNodes();
        
        for (int i = 0; i < hardDriveParamsNodes.getLength(); i++)
        {
            Node theNode = hardDriveParamsNodes.item(i);
            String name = theNode.getNodeName();
            Node currentNode = theNode.getChildNodes().item(0);
            
            String theValue;
                                 
            if(currentNode != null)
            {
                theValue = currentNode.getNodeValue();
                
                if (name.equals("enabled"))
                {
                   params[1] = Boolean.valueOf(theValue);    
                   
                } else if (name.equals("channelindex"))
                {
                    params[2] = Integer.valueOf(theValue);
                    
                } else if (name.equals("master"))
                {
                    params[3] = Boolean.valueOf(theValue); 
                    
                } else if (name.equals("autodetectcylinders"))
                {
                    params[4] = Boolean.valueOf(theValue); 
                   
                } else if (name.equals("cylinders"))
                {
                    params[5] = Integer.valueOf(theValue);
                    
                } else if (name.equals("heads"))
                {
                    params[6] = Integer.valueOf(theValue);
                    
                } else if (name.equals("sectorspertrack"))
                {
                    params[7] = Integer.valueOf(theValue);
          
                } else if (name.equals("imagefilepath"))
                {
                    params[8] =  theValue;
                }
            }
        }
        
        return params;
    }
    
    /**
     * Get the RAM params from XML Config.
     * @param document
     * @return
     */
    private Object[] getRamParams(Document document)
    {
        Object[] params = new Object[1];
        
        Node ramNode = xmlConnect.getFirstNode(document,DioscuriXmlParams.RAM_SIZE_TEXT);
        Node ramSubNode = ramNode.getChildNodes().item(0);

        int ramSize = 0;
        if (ramSubNode != null)
        {
            
            ramSize  = Integer.parseInt(ramSubNode.getNodeValue());
        }
        
        params[0] = Integer.valueOf(ramSize);
        
        return params;
    }

    /**
     * Get the Mouse params from XML Config.
     * @param document
     * @return
     */
    private Object[] getMouseParams(Document document)
    {
        Object[] params = new Object[2];
        
        Node mouseNode = DioscuriXmlParams.getModuleNode(document, ModuleType.MOUSE);
        NodeList mouseParamsNodes = mouseNode.getChildNodes();
                        
         for (int i = 0; i < mouseParamsNodes.getLength(); i++)
         {
             Node theNode = mouseParamsNodes.item(i);         
             String name = theNode.getNodeName();
             Node mouseSubNode = theNode.getChildNodes().item(0);
             
             String theValue;
             
             if(mouseSubNode != null)
             {
                 theValue = mouseSubNode.getNodeValue();
                 
                 if (name.equals(DioscuriXmlParams.MOUSE_ENABLED_TEXT))
                 {
                     params[0] = Boolean.valueOf(theValue);    
                    
                 } else if (name.equals(DioscuriXmlParams.MOUSE_TYPE_TEXT))
                 {
                     params[1] = (String)theValue;
                 }
             }
         }
         
/*          // Get mouse enabled
        Node mouseNode = XmlConnect.getFirstNode(document,DioscuriXmlParams.MOUSE_ENABLED_TEXT);
        Node mouseSubNode = mouseNode.getChildNodes().item(0);
        
        boolean mouseEnabled = false;
        if (mouseSubNode != null)
        {
            mouseEnabled = Boolean.parseBoolean(mouseSubNode.getNodeValue());
        }
        
        params[0] = mouseEnabled;

        // Get mouse type
        mouseNode = XmlConnect.getFirstNode(document,DioscuriXmlParams.MOUSE_TYPE_TEXT);
        mouseSubNode = mouseNode.getChildNodes().item(0);

        String mouseType = "";
        if (mouseSubNode != null)
        {
            mouseType  = (String) mouseSubNode.getNodeValue();
        }
        
        params[1] = mouseType;
*/        
        return params;
    }
    
}
