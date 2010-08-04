/* $Revision: 160 $ $Date: 2009-08-17 12:56:40 +0000 (ma, 17 aug 2009) $ $Author: blohman $ 
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

package dioscuri.module;

import java.io.File;

/**
 * Abstract class representing a generic hardware module.
 * TODO: this template of moduleFDC could also be made more generic for any removable storage device
 */

public abstract class ModuleFDC extends Module {

    public ModuleFDC() {
        super(Type.FDC,
                Type.MOTHERBOARD, Type.RTC, Type.PIC, Type.DMA, Type.ATA);
    }

    /**
     * Defines the total number of available drives Note: total number may not
     * exceed 4
     * 
     * @param totalDrives
     * @return boolean true if drives set successfully, false otherwise
     */
    public abstract boolean setNumberOfDrives(int totalDrives);

    /**
     * Inserts a new carrier into a selected drive
     * 
     * @param drive
     * @param carrierType
     * @param imageFile
     * @param writeProtected
     * @return boolean true if carrier is inserted successfully, false otherwise
     */
    public abstract boolean insertCarrier(String drive, byte carrierType,
            File imageFile, boolean writeProtected);

    /**
     * Ejects a carrier (if any) from a selected drive
     * 
     * @param drive
     * @return boolean true if carrier is ejected successfully, false otherwise
     */
    public abstract boolean ejectCarrier(String drive);

    /**
     * Inserts a new carrier into a selected drive
     * 
     * @param driveIndex 
     * @param carrierType
     * @param imageFile
     * @param writeProtected
     * @return boolean true if carrier is inserted successfully, false otherwise
     */
    public abstract boolean insertCarrier(int driveIndex, byte carrierType,
            File imageFile, boolean writeProtected);

    /**
     * Ejects a carrier (if any) from a selected drive
     * 
     * @param driveIndex
     * @return boolean true if carrier is ejected successfully, false otherwise
     */
    public abstract boolean ejectCarrier(int driveIndex);
}
