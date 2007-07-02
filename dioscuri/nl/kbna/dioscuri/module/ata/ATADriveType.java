/*
 * $Revision: 1.1 $ $Date: 2007-07-02 14:31:28 $ $Author: blohman $
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
package nl.kbna.dioscuri.module.ata;


/**
 * An enumeration representing the types of drives available for the ATA Module.
 *  
 */
public class ATADriveType
{
    
    private String name;
    
    public final static ATADriveType NONE = new ATADriveType("None");
    public final static ATADriveType HARD_DISK = new ATADriveType("Hard Disk");
    public final static ATADriveType CDROM = new ATADriveType("CD ROM");
      
    public final static ATADriveType[] driveType = {NONE, HARD_DISK, CDROM};
    
    /**
     * Class constructor.
     * 
     * @param theName   the name of the drive type 
     */
    private ATADriveType(String theName) 
    {
        this.name = theName;
    }
    
    /**
     * get the name of the drive type.
     * 
     * @return the name of the drive type
     */
    public String toString() {
        return this.name;
    }
    
}
