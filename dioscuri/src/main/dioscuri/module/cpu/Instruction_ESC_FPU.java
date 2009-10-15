/*
 * $Revision: 159 $ $Date: 2009-08-17 12:52:56 +0000 (ma, 17 aug 2009) $ $Author: blohman $
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

package dioscuri.module.cpu;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Intel opcodes D8 - DF<BR>
 * Escape to coprocessor instruction set (Floating Point Unit, FPU).<BR>
 * NOTE: This implementation only advances the instruction pointer to the next instruction,<BR>
 * but does not perform the associated FPU instruction!<BR>
 * 
 */
public class Instruction_ESC_FPU implements Instruction
{

    // Attributes
    private CPU cpu;

    int opCode;
    int addressByte;
    byte[] memoryReferenceLocation;
    byte[] memoryReferenceDisplacement;
    byte[] sourceValue;

    // Logging
    private static Logger logger = Logger.getLogger("dioscuri.module.cpu");
    
    
    // Constructors
    /**
     * Class constructor
     */
    public Instruction_ESC_FPU()
    {
        // Initialise variables

        opCode = 0;
        addressByte = 0;
        memoryReferenceLocation = new byte[2];
        memoryReferenceDisplacement = new byte[2];
        sourceValue = new byte[2];
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_ESC_FPU(CPU processor)
    {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Advance IP to next instruction.<BR>
     * NOTE: The actual instruction is not executed!
     */
    public void execute()
    {
        // Get opcode of current instruction
        opCode = cpu.codeByte;

        // Execute instruction decoded from nnn (bits 5, 4, 3 in ModR/M byte)
        switch (opCode)
        {
            case 0xD8:
                logger.log(Level.SEVERE, "[" + cpu.getType() + "]" + " FPU instruction 0x" + Integer.toHexString(opCode) + " not properly implented (" + cpu.getRegisterHex(0) + ":" + cpu.getRegisterHex(1) + ")");
                break;
                
            case 0xD9:
                // Get addresByte; cast to int for switch later
                addressByte = ((int) cpu.getByteFromCode()) & 0xFF ;
                
                if (addressByte < 0xBF)
                {
                    // Switch on 'nnn' (aka 'rrr') bits
                    switch ((addressByte & 0x38) >> 3)
                    {
                        case 7: // FNSTCW
                            // Destination address given in memory (m2byte)
                            memoryReferenceDisplacement = cpu.decodeMM(addressByte);

                            // Determine memory location
                            memoryReferenceLocation = cpu.decodeSSSMemDest((byte) addressByte, memoryReferenceDisplacement);

                            // Get value from memory
                            sourceValue = cpu.getWordFromMemorySegment((byte) addressByte, memoryReferenceLocation);
                            
                            /*
                             * TODO: Insert rest of implementation here...
                             */
                            break;
                            
                        default:
                            logger.log(Level.SEVERE, "[" + cpu.getType() + "]" + " FPU instruction 0x" + Integer.toHexString(opCode) + " not properly implemented (" + cpu.getRegisterHex(0) + ":" + cpu.getRegisterHex(1) + ")");
                            break;  
                    }
                }
                else
                {
                    // Switch on addressByte
                    switch (addressByte)
                    {
                        default:
                            logger.log(Level.SEVERE, "[" + cpu.getType() + "]" + " FPU instruction 0x" + Integer.toHexString(opCode) + " not properly implemented (" + cpu.getRegisterHex(0) + ":" + cpu.getRegisterHex(1) + ")");
                            break;  
                    }
                }
                break;
                
            case 0xDA:
                logger.log(Level.SEVERE, "[" + cpu.getType() + "]" + " FPU instruction 0x" + Integer.toHexString(opCode) + " not properly implented (" + cpu.getRegisterHex(0) + ":" + cpu.getRegisterHex(1) + ")");
                break;
                
            case 0xDB:
                // Get addresByte; cast to int for switch later
                addressByte = ((int) cpu.getByteFromCode()) & 0xFF ;
                
                if (addressByte < 0xBF)
                {
                    // Switch on 'nnn' (aka 'rrr') bits
                    switch ((addressByte & 0x38) >> 3)
                    {
                        default:
                            logger.log(Level.SEVERE, "[" + cpu.getType() + "]" + " FPU instruction 0x" + Integer.toHexString(opCode) + " not properly implemented (" + cpu.getRegisterHex(0) + ":" + cpu.getRegisterHex(1) + ")");
                            break;
                    }
                }
                else
                {
                    // Switch on addressByte
                    switch (addressByte)
                    {
                        case 0xE3:  // FNINIT
                            /*
                             * TODO: Insert implementation here...
                             */                            
                            break;

                        default:
                            logger.log(Level.SEVERE, "[" + cpu.getType() + "]" + " FPU instruction 0x" + Integer.toHexString(opCode) + " not properly implemented (" + cpu.getRegisterHex(0) + ":" + cpu.getRegisterHex(1) + ")");
                            break;  
                    }
                }
                break;
                    
            case 0xDC:
            case 0xDD:
            case 0xDE:
            case 0xDF:
                logger.log(Level.SEVERE, "[" + cpu.getType() + "]" + " FPU instruction 0x" + Integer.toHexString(opCode) + " not properly implented (" + cpu.getRegisterHex(0) + ":" + cpu.getRegisterHex(1) + ")");
                break;
                
            default:
                logger.log(Level.SEVERE, "[" + cpu.getType() + "]" + " FPU instruction 0x" + Integer.toHexString(opCode) + " not recognised (" + cpu.getRegisterHex(0) + ":" + cpu.getRegisterHex(1) + ")");
                break;
        }
    }
}
