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

package dioscuri.module.dma;

import dioscuri.module.Module;

/**
 * Handler for the master DMA controller, providing implementations for 8-bit
 * read and write functionality<BR>
 * This handler will be registered with the DMA class to provide device-specific
 * methods for reading (memory -> device) and writing (device -> memory) a byte
 * via DMA.
 * 
 */
public abstract class DMA8Handler {
    // Name of the device that provides the methods
    protected Module.Type owner;

    /**
     * Device-specific implementation of the 8-bit DMA read functionality.<BR>
     * This provides a way for DMA to pass a byte read from memory (by way of
     * DMA request) to the device for further processing.
     * 
     * @param data
     *            Byte from memory that is passed to the device for handling
     */
    public abstract void dma8ReadFromMem(byte data);

    /**
     * Device-specific implementation of the 8-bit DMA write functionality.<BR>
     * This provides a way for DMA to write a byte to memory (by way of DMA
     * request) passed from the device.
     * 
     * @return Byte from device that will be written to memory
     */
    public abstract byte dma8WriteToMem();
}
