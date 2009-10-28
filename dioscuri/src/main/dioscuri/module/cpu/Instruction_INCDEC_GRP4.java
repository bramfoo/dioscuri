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


package dioscuri.module.cpu;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Intel opcode FE<BR>
 * INC/DEC Group 4 opcode extension: INC, DEC.<BR>
 * Performs the selected instruction (indicated by bits 5, 4, 3 of the ModR/M byte) using immediate data.<BR>
 * Flags modified: depending on instruction can be any of: OF, SF, ZF, AF, PF, CF
 */
public class Instruction_INCDEC_GRP4 implements Instruction
{

    // Attributes
    private CPU cpu;

    boolean operandWordSize;
    byte[] memoryReferenceLocation;
    byte[] memoryReferenceDisplacement;

    byte addressByte;
    byte[] sourceValue;
    byte[] oldSource;
    byte registerHighLow = 0;

    // Logging
    private static Logger logger = Logger.getLogger("dioscuri.module.cpu");
    
    
    // Constructors
    /**
     * Class constructor
     */
    public Instruction_INCDEC_GRP4()
    {
        // Initialise variables
        operandWordSize = false;
        memoryReferenceLocation = new byte[2];
        memoryReferenceDisplacement = new byte[2];
        
        addressByte = 0;
        sourceValue = new byte[2];
        oldSource = new byte[2];
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_INCDEC_GRP4(CPU processor)
    {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Execute any of the following Immediate Group 4 instructions: INC, DEC.<BR>
     */
    public void execute()
    {
        // Get addresByte
        addressByte = cpu.getByteFromCode();
        
        // Re-initialise source to get rid of pointers
        sourceValue = new byte[2];

        // Execute instruction decoded from nnn (bits 5, 4, 3 in ModR/M byte)
        switch ((addressByte & 0x38) >> 3)
        {
            case 0: // INC Eb
                if (((addressByte >> 6) & 0x03) == 3)
                {
                    // Address given in register (=pointer!)
                    sourceValue = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
                    
                    // Determine high/low part of register based on bit 3 (leading sss bit)
                    registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW : (byte) CPU.REGISTER_GENERAL_HIGH;
                }
                else
                {
                    // Address given in memory (m16:16)
                    // Determine IP displacement of memory location (if any) 
                    memoryReferenceDisplacement = cpu.decodeMM(addressByte);

                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);

                    // Get value from memory
                    registerHighLow = CPU.REGISTER_GENERAL_LOW;
                    sourceValue = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
                }
                
                // Store old value
                System.arraycopy(sourceValue, 0, oldSource, 0, sourceValue.length);
                
                // Increment the source (= destination) register
                sourceValue[registerHighLow]++;
                        
                // No need to check for overflow, as INC is only on source[low]
                
                // Return result to memory if necessary
                // Note: if register, then value automatically updated as array is reference!
                if (((addressByte >> 6) & 0x03) != 3)
                {
                    cpu.setByteInMemorySegment(addressByte, memoryReferenceLocation, sourceValue[registerHighLow]);
                }
                
                // Test AF
                cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_ADD(oldSource[registerHighLow], sourceValue[registerHighLow]);  
                // Test OF
                cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_ADD(oldSource[registerHighLow], (byte) 0x01, sourceValue[registerHighLow], 0);  
                // Test ZF
                cpu.flags[CPU.REGISTER_FLAGS_ZF] = sourceValue[registerHighLow] == 0x00 ? true : false;
                // Test SF (set when MSB of AH is 1. In Java can check signed byte)
                cpu.flags[CPU.REGISTER_FLAGS_SF] = sourceValue[registerHighLow] < 0 ? true : false;
                // Set PF, only applies to LSB
                cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(sourceValue[registerHighLow]);
                break;
                
            case 1: // DEC Eb
                if (((addressByte >> 6) & 0x03) == 3)
                {
                    // Address given in register
                    sourceValue = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
                    
                    // Determine high/low part of register based on bit 3 (leading sss bit)
                    registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW : (byte) CPU.REGISTER_GENERAL_HIGH;
                }
                else
                {
                    // Address given in memory (m16:16)
                    // Determine IP displacement of memory location (if any) 
                    memoryReferenceDisplacement = cpu.decodeMM(addressByte);

                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);

                    // Get value from memory
                    registerHighLow = CPU.REGISTER_GENERAL_LOW;
                    sourceValue = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
                }
                
                // Store old value
                System.arraycopy(sourceValue, 0, oldSource, 0, sourceValue.length);
                
                // Decrement the source (= destination) register
                sourceValue[registerHighLow]--;
                        
                // No need to check for overflow, as DEC is only on source[low]
                
                // Return result to memory if necessary
                // Note: if register, then value automatically updated as array is reference!
                if (((addressByte >> 6) & 0x03) != 3)
                {
                    cpu.setByteInMemorySegment(addressByte, memoryReferenceLocation, sourceValue[registerHighLow]);
                }
                
                // Test AF
                cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_SUB(oldSource[registerHighLow], sourceValue[registerHighLow]);  
                // Test OF
                cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB(oldSource[registerHighLow], (byte) 0x01, sourceValue[registerHighLow], 0);  
                // Test ZF
                cpu.flags[CPU.REGISTER_FLAGS_ZF] = sourceValue[registerHighLow] == 0x00 ? true : false;
                // Test SF (set when MSB of AH is 1. In Java can check signed byte)
                cpu.flags[CPU.REGISTER_FLAGS_SF] = sourceValue[registerHighLow] < 0 ? true : false;
                // Set PF, only applies to LSB
                cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(sourceValue[registerHighLow]);
                break;

            default:
                logger.log(Level.SEVERE, cpu.getType() + " -> Instruction INCDEC_GRP4 (0xFE): no group instruction match.");
                break;
        }
    }
}
