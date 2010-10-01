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

/**
 * DMA Channel<BR>
 * Class representing the structure of a DMA channel; each channel contains:<BR>
 * - mode register (sets mode and transfer type)<BR>
 * - current and base address (memory location of read/write)<BR>
 * - current and base count (number of transfers)<BR>
 * - used status (channel is in use)
 */
public class DMAChannel {
    // Create mode register for each channel
    DMAModeRegister mode = new DMAModeRegister();

    int currentAddress; // Value of address used during DMA transfers;
    // automatically inc/dec after each transfer
    int currentCount; // Number of transfers to be performed; Actual transfers
    // is one more than currentCount
    // Count is decremented after each transfer. A TC is generated when value
    // rolls 0h to FFFFH.

    int baseAddress; // Original value of currentAddress; used in AutoInitialise
    // to restore current registers
    int baseCount; // Original value of currentCount; used in AutoInitialise to
    // restore current registers

    byte pageRegister; // Specifies base address of page in memory where DMA
    // buffer resides
    boolean channelUsed; // Signals channel is in use by device

}
