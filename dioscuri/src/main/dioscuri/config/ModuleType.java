/* $Revision$ $Date$ $Author$ 
 * 
 * Copyright (C) 2007-2009  National Library of the Netherlands, 
 *                          Nationaal Archief of the Netherlands, 
 *                          Planets
 *                          KEEP
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 *
 * For more information about this project, visit
 * http://dioscuri.sourceforge.net/
 * or contact us via email:
 *   jrvanderhoeven at users.sourceforge.net
 *   blohman at users.sourceforge.net
 *   bkiers at users.sourceforge.net
 * 
 * Developed by:
 *   Nationaal Archief               <www.nationaalarchief.nl>
 *   Koninklijke Bibliotheek         <www.kb.nl>
 *   Tessella Support Services plc   <www.tessella.com>
 *   Planets                         <www.planets-project.eu>
 *   KEEP                            <www.keep-project.eu>
 * 
 * Project Title: DIOSCURI
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
public enum ModuleType {
    // Define each module types:
    ATA("ata", "ATA / IDE Controller"),
    BIOS("bios"),
    BOOT("boot"),
    CPU("cpu"),
    CLOCK("clock"),
    DMA("dma"),
    FDC("fdc"),
    KEYBOARD("keyboard"),
    MOUSE("mouse"),
    MEMORY("memory"),
    MOTHERBOARD("motherboard"),
    PIC("pic"),
    PIT("pit"),
    RTC("rtc"),
    SCREEN("screen"),
    VGA("video");

    // Define all module types as a array
    public final static ModuleType[] moduleTypes = { ATA, BIOS, BOOT, CPU,
            CLOCK, DMA, FDC, KEYBOARD, MOUSE, MEMORY, MOTHERBOARD, PIC, PIT,
            RTC, SCREEN, VGA };

    private final String type;
    private final String name;

    /**
     * Class constructor, with only the string type specified.
     * 
     * @param theType
     *            the string type of the module
     */
    private ModuleType(String theType) {
        this(theType, null);
    }

    /**
     * Class constructor, with the string type and description specified.
     * 
     * @param theType
     *            the string type of the module
     * @param theName
     *            the string description of the module
     */
    private ModuleType(String theType, String theName) {
        this.type = theType;
        this.name = theName;
    }

    /**
     * Gets the string type of the module.
     * 
     * @return the string type of the module
     */
    @Override
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
