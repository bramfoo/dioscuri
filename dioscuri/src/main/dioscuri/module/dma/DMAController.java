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
 * DMA controller<BR>
 * Class representing the structure of a DMA controller; each controller
 * contains:<BR>
 * - 4 channels, eah having a DMA Request (DRQ) and DMA Acknowledge (DRQ) bit<BR>
 * - mask register (disables incoming DRQs)<BR>
 * - flipflop (additional address bit)<BR>
 * - status register (status of terminal counts, DMA requests)<BR>
 * - command register (operation control)<BR>
 */
public class DMAController {
    // Create channels 0..3 for each controller
    DMAChannel[] channel = new DMAChannel[]{new DMAChannel(),
            new DMAChannel(), new DMAChannel(), new DMAChannel()};

    boolean DRQ[] = new boolean[4]; // DMA Request for channels 0 - 3
    boolean DACK[] = new boolean[4]; // DMA Acknowlege for channels 0 - 3

    byte mask[] = new byte[4]; // Mask for each channel; incoming DMA requests
    // are disabled if set

    boolean flipflop; // Generate additional bit of address, used to determine
    // upper/lower
    // byte of 16-bit address and count registers

    byte statusRegister; // Status of terminal counts and DMA requests
    // Bit 7: Channel 3 request
    // Bit 6: Channel 2 request
    // Bit 5: Channel 1 request
    // Bit 4: Channel 0 request
    // Bit 3: Channel 3 has reached TC
    // Bit 2: Channel 2 has reached TC
    // Bit 1: Channel 1 has reached TC
    // Bit 0: Channel 0 has reached TC

    byte commandRegister; // Controls operation of the 8237A.
    // Bit 7: DACK sense active: low/high
    // Bit 6: DREQ sense active: high/low
    // Bit 5: Write selection : late/extended
    // Bit 4: Priority : fixed/rotating
    // Bit 3: Timing : normal/compressed
    boolean ctrlDisabled; // Bit 2: Controller : enable/disable
    // Bit 1: Chan 0 addr. hold: disable/enable
    // Bit 0: Memory to memory : disable/enable
}
