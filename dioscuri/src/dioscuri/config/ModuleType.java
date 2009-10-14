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

/*
 * Information used in this module was taken from:
 * - http://en.wikipedia.org/wiki/AT_Attachment
 * - http://bochs.sourceforge.net/techspec/IDE-reference.txt
 */
package dioscuri.config;

/**
 * An enumeration representing the types of "plug-in" modules available.
 *  
 */
public class ModuleType
{
    
    private String type;
    private String name;
      
    /**
     * Class constructor, with only the string type specified.
     * 
     * @param theType        the string type of the module
     */
    private ModuleType(String theType) 
    {
        this.type = theType;
    }
    
    /**
     * Class constructor, with the string type and description specified.
     * 
     * @param theType        the string type of the module
     * @param theName        the string description of the module
     */
    private ModuleType(String theType, String theName) 
    {
        this.type = theType;
        this.name = theName; 
    }
     
    // Define each module types:
    public final static ModuleType ATA = new ModuleType("ata", "ATA / IDE Controller");
    public final static ModuleType BIOS = new ModuleType("bios"); 
    public final static ModuleType BOOT = new ModuleType("boot");   
    public final static ModuleType CPU = new ModuleType("cpu");
    public final static ModuleType CLOCK = new ModuleType("clock");    
    public final static ModuleType DMA = new ModuleType("dma");    
    public final static ModuleType FDC = new ModuleType("fdc");
    public final static ModuleType KEYBOARD = new ModuleType("keyboard");
    public final static ModuleType MOUSE = new ModuleType("mouse");
    public final static ModuleType MEMORY = new ModuleType("memory"); 
    public final static ModuleType MOTHERBOARD = new ModuleType("motherboard");
    public final static ModuleType PIC = new ModuleType("pic");
    public final static ModuleType PIT = new ModuleType("pit");
    public final static ModuleType RTC = new ModuleType("rtc");

    public final static ModuleType SCREEN = new ModuleType("screen");
    public final static ModuleType VGA = new ModuleType("video");
     
    //Define all module types as a array
    public final static ModuleType[] moduleTypes = {ATA,
                                                    BIOS,
                                                    BOOT,                                                    
                                                    CPU,
                                                    CLOCK,
                                                    DMA,
                                                    FDC,
                                                    KEYBOARD,
                                                    MOUSE,
                                                    MEMORY,
                                                    MOTHERBOARD,
                                                    PIC,
                                                    PIT,
                                                    RTC,
                                                    SCREEN,
                                                    VGA};
    
    /**
     * Gets the string type of the module.
     * 
     * @return the string type of the module
     */
    public String toString() {
        
        return this.type;
    } 
      
    /**
     * Gets the description of the module.
     * 
     * @return the description of the module
     */
    public String getName() {
        
        return this.name;
    } 
    
}
