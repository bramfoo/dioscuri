/*
 * $Revision: 1.1 $ $Date: 2007-07-02 14:31:26 $ $Author: blohman $
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

package nl.kbna.dioscuri.module;

import nl.kbna.dioscuri.module.ata.ATATranslationType;

/**
 * Interface representing a generic hardware module.
 * TODO: this template of moduleATA could also be made more generic for any fixed storage device
 */

public abstract class ModuleATA extends ModuleDevice
{
    // Methods

    /**
     * Initiate configuration of the disk drive.
     * 
     * @param theIdeChannel
     * @param isMaster
     * @param isHardDisk
     * @param isWriteProtected
     * @param numCylinders
     * @param numHeads
     * @param numSectorsPerTrack
     * @param translationType
     * @param imageFilePath
     */
    public abstract void initConfig(int theIdeChannel, boolean isMaster, boolean isHardDisk, boolean isWriteProtected, int numCylinders, int numHeads,
            int numSectorsPerTrack, ATATranslationType translationType, String imageFilePath);
    
    /**
     * Set CMOS values
     * 
     * @param translationType
     * @param bootDrives
     * @param floppySigCheckDisabled
     */
    public abstract void setCmosSettings(int[] bootDrives, boolean floppySigCheckDisabled);
    
    /**
     * Gets the current channel index.
     * 
     * @return int the current channel index
     */
    public abstract int getCurrentChannelIndex();
    
}