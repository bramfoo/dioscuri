/*
 * $Revision: 1.1 $ $Date: 2007-07-02 14:31:43 $ $Author: blohman $
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
package nl.kbna.dioscuri.module.pic;

/**
 * Programmable Interrupt Controller<BR>
 * Class representing the structure of a PIC<BR>
 * Includes a reset function to reset certain variables to their defaults<BR>
 * 
 */
public class TheProgrammableInterruptController
{
        boolean singleCascadedPIC;      // 0: cascaded PIC, 1: master only
        int interruptOffset;            // INT <-> IRQ offset (ex. IRQ0 == INT8)
        boolean specialFullyNestedMode; // specially fully nested mode: 0=no, 1=yes
        boolean bufferedMode;           // 0: no buffered mode, 1: buffered mode
        boolean master;                 // master/slave: 0: slave PIC, 1: master PIC
        boolean autoEndOfInt;           // 0: manual EOI, 1: automatic EOI
        byte interruptMaskRegister;     // interrupt mask register, 1: masked
        byte inServiceRegister;         // in service register
        byte interruptRequestRegister;  // interrupt request register
        int readRegisterSelect;         // 0: IRR, 1: ISR
        byte irqNumber;                 // current IRQ number
        int lowestPriorityIRQ;          // current lowest priority irq
        boolean intRequestPin;          // INT request pin of PIC
        int irqPins;                    // IRQ pins of PIC
        boolean specialMask;
        boolean isPolled;               // Set when poll command is issued.
        boolean rotateOnAutoEOI;        // Set when should rotate in auto-eoi mode.
        int edgeLevel;                  // IRQ mode (0: edge, 1: level)
        
        InitSequence initSequence = new InitSequence();
        
        /**
         * Resets all common parameters to their default value
         * NOTE: Not all parameters are reset! (interruptOffset, masterSlave, etc.) 
         *
         */
        protected void reset()
        {
            singleCascadedPIC = false;
            specialFullyNestedMode = false;         // normal nested mode
            bufferedMode = false;                   // unbuffered mode
            autoEndOfInt = false;                   // manual EOI from CPU
            interruptMaskRegister = (byte) 0xFF;    // all IRQ's initially masked
            inServiceRegister = 0x00;               // no IRQ's in service
            interruptRequestRegister = 0x00;        // no IRQ's requested
            readRegisterSelect = 0;                 // IRR
            irqNumber = 0;
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
