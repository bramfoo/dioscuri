/*
 * $Revision: 160 $ $Date: 2009-08-17 12:56:40 +0000 (ma, 17 aug 2009) $ $Author: blohman $
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

package dioscuri.module;

import java.io.File;

/**
 * Interface representing a generic hardware module.
 * TODO: this template of moduleFDC could also be made more generic for any removable storage device
 */

public abstract class ModuleFDC extends ModuleDevice
{
    // Methods
    
    /**
     * Defines the total number of available drives
     * Note: total number may not exceed 4
     * 
     * @param int total number of drives
     * @return boolean true if drives set successfully, false otherwise
     */
    public abstract boolean setNumberOfDrives(int totalDrives);
    
    /**
     * Inserts a new carrier into a selected drive
     * 
     * @param String drive to which carrier has to be inserted
     * @param byte carrierType that defines the type of the carrier
     * @param File containing the disk image raw bytes of the carrier
     * @param boolean writeProtected denoting the inserted floppy is write protected or not
     * 
     * @return boolean true if carrier is inserted successfully, false otherwise
     */
    public abstract boolean insertCarrier(String drive, byte carrierType, File imageFile, boolean writeProtected);
    
    /**
     * Ejects a carrier (if any) from a selected drive
     * 
     * @param String drive of which carrier has to be ejected
     * 
     * @return boolean true if carrier is ejected successfully, false otherwise
     */
    public abstract boolean ejectCarrier(String drive);

    /**
     * Inserts a new carrier into a selected drive
     * 
     * @param int driveIndex to which carrier has to be inserted
     * @param byte carrierType that defines the type of the carrier
     * @param File containing the disk image raw bytes of the carrier
     * @param boolean writeProtected denoting the inserted floppy is write protected or not
     * 
     * @return boolean true if carrier is inserted successfully, false otherwise
     */
    public abstract boolean insertCarrier(int driveIndex, byte carrierType, File imageFile, boolean writeProtected);
    
    /**
     * Ejects a carrier (if any) from a selected drive
     * 
     * @param int driveIndex of which carrier has to be ejected
     * 
     * @return boolean true if carrier is ejected successfully, false otherwise
     */
    public abstract boolean ejectCarrier(int driveIndex);
}
