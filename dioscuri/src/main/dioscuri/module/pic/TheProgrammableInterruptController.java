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

package dioscuri.module.pic;

/**
 * Programmable Interrupt Controller<BR>
 * Class representing the structure of a PIC<BR>
 * Includes a reset function to reset certain variables to their defaults<BR>
 */
public class TheProgrammableInterruptController {
    // PIC settings
    boolean singleCascadedPIC; // PIC is single or cascaded (true=master only,
    // false=cascaded PIC)
    int interruptOffset; // INT routine <-> IRQ offset (ex. IRQ0 == INT8)
    boolean specialFullyNestedMode; // Specially fully nested mode (true=yes,
    // false=no)
    boolean bufferedMode; // Buffer mode (true=buffered mode, false=no buffered
    // mode)
    boolean isMaster; // master/slave PIC (true=master PIC, false=slave PIC)
    boolean autoEndOfInt; // true=automatic EOI, false=manual EOI

    // Registers
    byte interruptRequestRegister; // interrupt request register (irr)
    byte inServiceRegister; // in service register (isr)
    byte interruptMaskRegister; // interrupt mask register (imr), 1=masked

    // Other variables
    int readRegisterSelect; // 0: IRR, 1: ISR
    byte currentIrqNumber; // current IRQ number
    int lowestPriorityIRQ; // current lowest priority irq
    boolean intRequestPin; // INT request pin of PIC
    int irqPins; // IRQ pins of PIC. Each pin is a bit in irqPins. If pin is
    // high, bit is 1: IRQ 4 high = 0000.0100
    boolean specialMask;
    boolean isPolled; // Set when poll command is issued.
    boolean rotateOnAutoEOI; // Set when should rotate in auto-eoi mode.
    int edgeLevel; // IRQ mode (0=edge, 1=level)

    InitSequence initSequence = new InitSequence();

    /**
     * Resets all common parameters to their default value NOTE: Not all
     * parameters are reset! (interruptOffset, masterSlave, etc.)
     */
    public void reset() {
        singleCascadedPIC = false;
        specialFullyNestedMode = false; // normal nested mode
        bufferedMode = false; // unbuffered mode
        autoEndOfInt = false; // manual EOI from CPU
        interruptMaskRegister = (byte) 0xFF; // all IRQ's initially masked
        inServiceRegister = 0x00; // no IRQ's in service
        interruptRequestRegister = 0x00; // no IRQ's requested
        readRegisterSelect = 0; // IRR
        currentIrqNumber = 0;
        lowestPriorityIRQ = 7;
        intRequestPin = false;
        irqPins = 0;
        specialMask = false;
        isPolled = false;
        rotateOnAutoEOI = false;
        edgeLevel = 0;

        initSequence.reset();
    }
}
