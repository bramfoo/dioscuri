/*
 * $Revision: 159 $ $Date: 2009-08-17 12:52:56 +0000 (ma, 17 aug 2009) $ $Author: blohman $
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
package dioscuri.module.ata;

/**
 * Enumeration class for sense type.
 *
 */
public final class SenseType
{

    private int value;
    
    /*
     * Constructor
     */
    private SenseType(int theValue) 
    {
        this.value = theValue;
    }
    
    public final static SenseType NONE = new SenseType(0);
    public final static SenseType NOT_READY = new SenseType(2);
    public final static SenseType ILLEGAL_REQUEST = new SenseType(5);
    public final static SenseType UNIT_ATTENTION = new SenseType(6);
      
    public final static SenseType[] sense = {NONE,NOT_READY,
        ILLEGAL_REQUEST, UNIT_ATTENTION};
    
    /**
     * get the value associated with the enum entry.
     * 
     * @return the value
     */
    public int getValue() {
        return this.value;
    }

}
