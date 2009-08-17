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
package nl.kbna.dioscuri.module.ata;

/**
 * Class that stores CDROM attributes.
 *
 */
public class CDROM
{

    boolean isReady;
    boolean isLocked;

    // LowLevelCDROM lowLevelCDROM

    int capacity;
    int nextLba;
    int remainingBlocks;

    int[] errorRecovery = new int[8];

    public CDROM()
    {

        this.errorRecovery[0] = 0x01;
        this.errorRecovery[1] = 0x06;
        this.errorRecovery[2] = 0x00;
        this.errorRecovery[3] = 0x05; // Try to recover 5 times
        this.errorRecovery[4] = 0x00;
        this.errorRecovery[5] = 0x00;
        this.errorRecovery[6] = 0x00;
        this.errorRecovery[7] = 0x00;

        this.isReady = false;

        // TODO: init variables

        if (ATAConstants.IS_LOW_LEVEL_CDROM)
        {
            // lowLevelCDROM = new LowLevelCDROM();
        }
    }

    public boolean isReady()
    {
        return isReady;
    }

    public void setReady(boolean isReady)
    {
        this.isReady = isReady;
    }

    public boolean isLocked()
    {
        return isLocked;
    }

    public void setLocked(boolean isLocked)
    {
        this.isLocked = isLocked;
    }

    public int getCapacity()
    {
        return capacity;
    }

    public void setCapacity(int capacity)
    {
        this.capacity = capacity;
    }

    public int getNextLba()
    {
        return nextLba;
    }

    public void setNextLba(int nextLba)
    {
        this.nextLba = nextLba;
    }

    public int getRemainingBlocks()
    {
        return remainingBlocks;
    }

    public void setRemainingBlocks(int remainingBlocks)
    {
        this.remainingBlocks = remainingBlocks;
    }

    public int[] getErrorRecovery()
    {
        return errorRecovery;
    }

    public void setErrorRecovery(int[] errorRecovery)
    {
        this.errorRecovery = errorRecovery;
    }

}
