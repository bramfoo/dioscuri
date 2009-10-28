/* $Revision: 159 $ $Date: 2009-08-17 12:52:56 +0000 (ma, 17 aug 2009) $ $Author: blohman $ 
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
package dioscuri.module.ata;

/**
 * Enumeration class for Asc Type.
 *
 */
public class AscType
{
    
    private int value;
    
    /*
     * Constructor
     */
    private AscType(int theValue) 
    {
        this.value = theValue;
    }
    public final static AscType NOT_SET = new AscType(0x00);
    public final static AscType ILLEGAL_OPCODE = new AscType(0x20);
    public final static AscType LOGICAL_BLOCK_OOR = new AscType(0x21);
    public final static AscType INV_FIELD_IN_CMD_PACKET = new AscType(0x24);
    public final static AscType SAVING_PARAMETERS_NOT_SUPPORTED = new AscType(0x39);
    public final static AscType MEDIUM_NOT_PRESENT = new AscType(0x3a);
      
    public final static AscType[] asc = {NOT_SET,
                                     ILLEGAL_OPCODE,
                                     LOGICAL_BLOCK_OOR,
                                     INV_FIELD_IN_CMD_PACKET,
                                     SAVING_PARAMETERS_NOT_SUPPORTED,
                                     MEDIUM_NOT_PRESENT };
    
    /**
     * get the value associated with the enum entry
     * @return the value
     */
    public int getValue() {
        return this.value;
    } 
    
}
