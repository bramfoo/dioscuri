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
 * DMA mode register<BR>
 * Class representing the mode register a DMA controller<BR>
 * 
 */
public class DMAModeRegister
{
    
    protected final static int DMA_MODE_DEMAND  = 0;
    protected final static int DMA_MODE_SINGLE  = 1;
    protected final static int DMA_MODE_BLOCK   = 2;
    protected final static int DMA_MODE_CASCADE = 3;
    
    protected final static int DMA_TRANSFER_VERIFY = 0;
    protected final static int DMA_TRANSFER_WRITE  = 1;
    protected final static int DMA_TRANSFER_READ   = 2;
    
    // Variable                 // Mode register bits:
    byte modeType;              // Bits 7-6: Transfer mode; 00b - Demand mode
                                //                          01b - Single mode
                                //                          10b - Block mode
                                //                          11b - Cascade mode
    boolean addressDecrement;   // Bit 5: INC; when set address is decremented, when cleared incremented
    boolean autoInitEnable;     // Bit 4: AI; when set enables auto initialisation
    byte transferType;          // Bits 3-2: Type;          00b - Verify
                                //                          01b - Write to memory
                                //                          10b - Read from memory
                                //                          11b - Illegal
                                //                          XXb - if bits 7-6 are 11b
                                // Bits 1-0: Channel;       00b - Channel 0
                                //                          01b - Channel 1
                                //                          10b - Channel 2
                                //                          11b - Channel 3
}
