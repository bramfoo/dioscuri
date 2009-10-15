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

import dioscuri.exception.CPUInstructionException;

/**
 * Intel opcode D0<BR>
 * Immediate Group 2 opcode extension: ROL, ROR, RCL, RCR, SHL/SAL, SHR, SAR.<BR>
 * Performs the selected instruction (indicated by bits 5, 4, 3 of the ModR/M byte) using constant 1.<BR>
 * Flags modified: depending on instruction can be any of: OF, SF, ZF, AF, PF, CF
 */
public class Instruction_ShiftGRP2_Eb1 implements Instruction
{

    // Attributes
    private CPU cpu;

    boolean operandWordSize;

    byte addressByte;
    byte[] memoryReferenceLocation;
    byte[] memoryReferenceDisplacement;
    byte registerHighLow;

    byte[] sourceValue;
    int tempValue;
    int bitShift;
    int carryBit;
    int newCarryBit;

    byte[] tempResult;


    // Constructors
    /**
     * Class constructor
     */
    public Instruction_ShiftGRP2_Eb1()
    {
        operandWordSize = false;

        addressByte = 0;
        memoryReferenceLocation = new byte[2];
        memoryReferenceDisplacement = new byte[2];

        sourceValue = new byte[2];
        tempValue = 0;

        bitShift = 0;
        carryBit = 0;
        newCarryBit = 0;

        tempResult = new byte[2];
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_ShiftGRP2_Eb1(CPU processor)
    {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Execute any of the following Immediate Group 2 instructions: ROL, ROR, RCL, RCR, SHL/SAL, SHR, SAR.<BR>
     * @throws CPUInstructionException 
     */
    public void execute() throws CPUInstructionException
    {
        // Reset sourceValue (to lose pointer to earlier words)
        sourceValue = new byte[2];

        // Get addresByte
        addressByte = cpu.getByteFromCode();

        // Determine displacement of memory location (if any)
        memoryReferenceDisplacement = cpu.decodeMM(addressByte);

        // Number of positions the bits have to be shifted
        bitShift = 1;

        // Execute instruction decoded from nnn (bits 5, 4, 3 in ModR/M byte)
        switch ((addressByte & 0x38) >> 3)
        {
            case 0: // ROL - Rotate bits 1 position left. Flags affected: CF, OF

                // Execute rotate on reg or mem. Determine this from mm bits of addressbyte
                if (((addressByte >> 6) & 0x03) == 3)
                {
                    // ROL on register
                    // Determine destination from addressbyte (source is the same)
                    sourceValue = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
                    
                    // Determine high/low part of register based on bit 3 (leading sss bit)
                    registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW : (byte) CPU.REGISTER_GENERAL_HIGH;
                }
                else
                {
                    // ROL on memory
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);
                    
                    // Determine high/low part of register
                    registerHighLow = 0;

                    // Get byte from memory
                    sourceValue[registerHighLow] = cpu.getByteFromMemorySegment(addressByte, memoryReferenceLocation);
                }
                
                // Determine result of carry; this is the MSB
                carryBit = (sourceValue[registerHighLow] & 0x80) == 0x80 ? 1 : 0;
                
                // Rotate left by 1. This is equivalent to a SHL of 1 ORed with the previously calculated carryBit
                sourceValue[registerHighLow] = (byte) ((sourceValue[registerHighLow] << bitShift) | carryBit);
                
                // Store result in memory for memory operations
                // Note: if destination register is real register, it already contains result (stored directly)
                if ( ((addressByte >> 6) & 0x03) != 3)
                {
                    // Store result back in memory
                    cpu.setByteInMemorySegment(addressByte, memoryReferenceLocation, sourceValue[registerHighLow]);
                }
                
                // Set appropriate flags
                // Set CF; this has already been calculated
                cpu.flags[CPU.REGISTER_FLAGS_CF] = carryBit == 1 ? true : false;
                
                // Set OF (only defined for 1-bit rotates); XOR of CF (after rotate) and MSB of result.
                cpu.flags[CPU.REGISTER_FLAGS_OF] = (carryBit ^ ((sourceValue[registerHighLow] & 0x80) == 0x80 ? 1 : 0)) == 1 ? true : false;
                break; // ROL
                
            case 1: // ROR - Rotate bits 1 position right; Flags affected: CF, OF
                // Execute rotate on reg or mem. Determine this from mm bits of addressbyte
                if (((addressByte >> 6) & 0x03) == 3)
                {
                    // ROR reg,imm
                    // Determine destination from addressbyte (source is the same)
                    sourceValue = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
                    
                    // Determine high/low part of register based on bit 3 (leading sss bit)
                    registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW : (byte) CPU.REGISTER_GENERAL_HIGH;
                }
                else
                {
                    // ROR mem,imm
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);
                    
                    // Determine high/low part of register
                    registerHighLow = 0;

                    // Get byte from memory
                    sourceValue[registerHighLow] = cpu.getByteFromMemorySegment(addressByte, memoryReferenceLocation);
                }
                
                // Rotate right. This is equivalent to a SHR of 1 ORed with a SHL of 7
                sourceValue[registerHighLow] = (byte) ( (((sourceValue[registerHighLow]) >> bitShift) & 0x7F) | ((sourceValue[registerHighLow]) << (8 - bitShift)) );
                
                // Store result in memory, if necessary
                // Note: if destination register is real register, it already contains result (stored directly)
                if ( ((addressByte >> 6) & 0x03) != 3)
                {
                    // Store result back in memory
                    cpu.setByteInMemorySegment(addressByte, memoryReferenceLocation, sourceValue[registerHighLow]);
                }
                
                // Set appropriate flags
                // Set CF; this is equal to the high bit of the register
                cpu.flags[CPU.REGISTER_FLAGS_CF] = (sourceValue[registerHighLow] & 0x80) == 0x80 ? true : false;
                // OF is the XOR of the two most-significant bits of the result.
                cpu.flags[CPU.REGISTER_FLAGS_OF] = ((cpu.flags[CPU.REGISTER_FLAGS_CF] ^ ((sourceValue[registerHighLow]) & 0x40) == 0x40)) ? true : false;
                break; // ROR

            case 2: // RCL - Rotate bits + CF 1 position left; Flags affected: CF, OF
                // Execute rotate on reg or mem. Determine this from mm bits of addressbyte
                if (((addressByte >> 6) & 0x03) == 3)
                {
                    // RCL on register
                    // Determine destination from addressbyte (source is the same)
                    sourceValue = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
                    
                    // Determine high/low part of register based on bit 3 (leading sss bit)
                    registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW : (byte) CPU.REGISTER_GENERAL_HIGH;
                }
                else
                {
                    // RCL on memory
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);
                    
                    // Determine high/low part of register
                    registerHighLow = 0;

                    // Get byte from memory
                    sourceValue[registerHighLow] = cpu.getByteFromMemorySegment(addressByte, memoryReferenceLocation);
                }
                
                // Determine result of carry; this is the MSB
                carryBit = cpu.flags[CPU.REGISTER_FLAGS_CF] == true ? 1 : 0;
                newCarryBit = (sourceValue[registerHighLow] & 0x80) == 0x80 ? 1 : 0;
                
                // Rotate left by 1. This is equivalent to a SHL of 1 ORed with the current carryBit (CF)
                sourceValue[registerHighLow] = (byte) ((sourceValue[registerHighLow] << bitShift) | carryBit);
                
                // Update carryBit
                carryBit = newCarryBit;
                
                // Store result in memory for memory operations
                // Note: if destination register is real register, it already contains result (stored directly)
                if ( ((addressByte >> 6) & 0x03) != 3)
                {
                    // Store result back in memory
                    cpu.setByteInMemorySegment(addressByte, memoryReferenceLocation, sourceValue[registerHighLow]);
                }
                
                // Set appropriate flags
                // Set CF; this has already been calculated
                cpu.flags[CPU.REGISTER_FLAGS_CF] = carryBit == 1 ? true : false;
                // Set OF (only defined for 1-bit rotates); XOR of CF (after rotate) and MSB of result.
                cpu.flags[CPU.REGISTER_FLAGS_OF] = (carryBit ^ ((sourceValue[registerHighLow] & 0x80) == 0x80 ? 1 : 0)) == 1 ? true : false;
                break;  // RCL
                
            case 3: // RCR - Rotate bits + CF 1 position right; Flags affected: CF, OF
                // Execute rotate on reg or mem. Determine this from mm bits of addressbyte
                if (((addressByte >> 6) & 0x03) == 3)
                {
                    // RCR on register
                    // Determine destination from addressbyte (source is the same)
                    sourceValue = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
                    
                    // Determine high/low part of register based on bit 3 (leading sss bit)
                    registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW : (byte) CPU.REGISTER_GENERAL_HIGH;
                }
                else
                {
                    // RCR on memory
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);
                    
                    // Determine high/low part of register
                    registerHighLow = 0;

                    // Get byte from memory
                    sourceValue[registerHighLow] = cpu.getByteFromMemorySegment(addressByte, memoryReferenceLocation);
                }
                
                // Determine result of carry; this is the MSB
                carryBit = cpu.flags[CPU.REGISTER_FLAGS_CF] == true ? 1 : 0;
                newCarryBit = (sourceValue[registerHighLow] & 0x01) == 0x01 ? 1 : 0;
                
                // Rotate right by 1. This is equivalent to a SHR of 1 ORed with CF
                sourceValue[registerHighLow] = (byte) ((((sourceValue[registerHighLow]) >> bitShift) & 0x7F) | (carryBit << (8 - bitShift)));

                // Update carryBit
                carryBit = newCarryBit;
                
                // Store result in memory for memory operations
                // Note: if destination register is real register, it already contains result (stored directly)
                if ( ((addressByte >> 6) & 0x03) != 3)
                {
                    // Store result back in memory
                    cpu.setByteInMemorySegment(addressByte, memoryReferenceLocation, sourceValue[registerHighLow]);
                }
                
                // Set appropriate flags
                // Set CF; this has already been calculated
                cpu.flags[CPU.REGISTER_FLAGS_CF] = carryBit == 1 ? true : false;
                
                // Set OF (only defined for 1-bit rotates); XOR of CF (after rotate) and MSB of result.
                cpu.flags[CPU.REGISTER_FLAGS_OF] = (((sourceValue[registerHighLow] << 1) ^ sourceValue[registerHighLow]) & 0x80) == 0x80 ? true : false;
                break;  // RCR

            case 4: // SHL/SAL; flags affected: CF, OF, SF, ZF, PF, AF (undefined)
                // Execute shift on reg or mem. Determine this from mm bits of addressbyte
                if (((addressByte >> 6) & 0x03) == 3)
                {
                    // SHL on register
                    // Determine destination from addressbyte (source is the same)
                    sourceValue = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
                    
                    // Determine high/low part of register based on bit 3 (leading sss bit)
                    registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW : (byte) CPU.REGISTER_GENERAL_HIGH;
                }
                else
                {
                    // SHL mem,imm
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);
                    
                    // Determine high/low part of register
                    registerHighLow = 0;

                    // Get byte from memory
                    sourceValue[registerHighLow] = cpu.getByteFromMemorySegment(addressByte, memoryReferenceLocation);
                }

                // Set CF; this is equal to the MSB
                cpu.flags[CPU.REGISTER_FLAGS_CF] =  (sourceValue[registerHighLow] & 0x80) == 0x80 ? true : false;
                
                // Shift left by 1
                sourceValue[registerHighLow] = (byte) (sourceValue[registerHighLow] << bitShift); 
                
                // Store result in memory, if necessary
                // Note: if destination register is real register, it already contains result (stored directly)
                if ( ((addressByte >> 6) & 0x03) != 3)
                {
                    // Store result back in memory
                    cpu.setByteInMemorySegment(addressByte, memoryReferenceLocation, sourceValue[registerHighLow]);
                }

                // Set appropriate flags
                // Set AF (although 'undefined' by Intel specs, hardware seems to clear it)
                cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
                // Set OF; clear if the most significant bit of the result is the same as the CF flag
                cpu.flags[CPU.REGISTER_FLAGS_OF] = (cpu.flags[CPU.REGISTER_FLAGS_CF] && ((sourceValue[registerHighLow]) >> 7) == 1)
                        || (!(cpu.flags[CPU.REGISTER_FLAGS_CF]) && ((sourceValue[registerHighLow]) >> 7) == 0) ? false : true;
                // Set ZF
                cpu.flags[CPU.REGISTER_FLAGS_ZF] = sourceValue[registerHighLow] == 0 ? true : false;
                // Set SF on particular byte of destinationRegister (set when MSB is 1, occurs when destReg >= 0x80)
                cpu.flags[CPU.REGISTER_FLAGS_SF] = sourceValue[registerHighLow] < 0 ? true : false;
                // Set PF on particular byte of destinationRegister
                cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(sourceValue[registerHighLow]);
                break; // SHL/SAL

            case 5: // SHR - Shift right unsigned. Flags affected: CF, OF, SF, ZF, PF, AF (undefined)
                // Execute shift on reg or mem. Determine this from mm bits of addressbyte
                if (((addressByte >> 6) & 0x03) == 3)
                {
                    // SHR on register
                    // Determine destination from addressbyte (source is the same)
                    sourceValue = cpu.decodeRegister(operandWordSize, addressByte & 0x07);

                    // Determine high/low part of register based on bit 3 (leading sss bit)
                    registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW : (byte) CPU.REGISTER_GENERAL_HIGH;
                }
                else
                {
                    // SHR on memory
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);

                    // Determine high/low part of register
                    registerHighLow = 0;

                    // Get byte from memory
                    sourceValue[registerHighLow] = cpu.getByteFromMemorySegment(addressByte, memoryReferenceLocation);
                }

                // Set CF; this will become what is now the LSB
                cpu.flags[CPU.REGISTER_FLAGS_CF] = (sourceValue[registerHighLow] & 0x01) == 1 ? true : false;
                // Set OF to MSB of original operand
                cpu.flags[CPU.REGISTER_FLAGS_OF] = (sourceValue[registerHighLow] & 0x80) == 0x80 ? true : false;

                // SHR by 1 - on unsigned value! (Else, ones are shifted into value if 0x80 or higher due to Java nature)
                tempValue = ((int) sourceValue[registerHighLow]) & 0xFF;
                tempValue = (tempValue >> bitShift) & 0x7F;
                sourceValue[registerHighLow] = (byte) tempValue;

                // Store result in memory for SHR mem operations
                // Note: if destination register is real register, it already contains result (stored directly)
                if (((addressByte >> 6) & 0x03) != 3)
                {
                    cpu.setByteInMemorySegment(addressByte, memoryReferenceLocation, sourceValue[registerHighLow]);
                }

                // Set appropriate flags
                // Set AF (although 'undefined' by Intel specs, hardware seems to clear it)
                cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
                // Set ZF
                cpu.flags[CPU.REGISTER_FLAGS_ZF] = sourceValue[registerHighLow] == 0 ? true : false;
                // Set SF on particular byte of destinationRegister (set when MSB is 1, occurs when destReg >= 0x80)
                cpu.flags[CPU.REGISTER_FLAGS_SF] = sourceValue[registerHighLow] < 0 ? true : false;
                // Set PF on particular byte of destinationRegister
                cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(sourceValue[registerHighLow]);
                break; // SHR

            case 6: // Does not exist
                // Throw exception for illegal nnn bits
                throw new CPUInstructionException("Shift Group 2 (0xD0/6) illegal reg bits");
                
            case 7: // SAR - Shift right signed. Flags affected: CF, OF, SF, ZF, PF, AF (undefined)
                // Execute shift on reg or mem. Determine this from mm bits of addressbyte
                if (((addressByte >> 6) & 0x03) == 3)
                {
                    // SAR on register
                    // Determine destination from addressbyte (source is the same)
                    sourceValue = cpu.decodeRegister(operandWordSize, addressByte & 0x07);

                    // Determine high/low part of register based on bit 3 (leading sss bit)
                    registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW : (byte) CPU.REGISTER_GENERAL_HIGH;
                }
                else
                {
                    // SAR on memory
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);

                    // Determine high/low part of register
                    registerHighLow = 0;

                    // Get byte from memory
                    sourceValue[registerHighLow] = cpu.getByteFromMemorySegment(addressByte, memoryReferenceLocation);
                }

                // Set CF; this will become what is now the LSB
                cpu.flags[CPU.REGISTER_FLAGS_CF] = (sourceValue[registerHighLow] & 0x01) == 1 ? true : false;
                // Set OF to MSB of original operand
                cpu.flags[CPU.REGISTER_FLAGS_OF] = (sourceValue[registerHighLow] & 0x80) == 0x80 ? true : false;

                // SAR by 1 - on signed value! (This is natural to the Java behaviour)
                sourceValue[registerHighLow] = (byte) ((sourceValue[registerHighLow] >> bitShift) & 0x7F);

                // Store result in memory for SHR mem operations
                // Note: if destination register is real register, it already contains result (stored directly)
                if (((addressByte >> 6) & 0x03) != 3)
                {
                    cpu.setByteInMemorySegment(addressByte, memoryReferenceLocation, sourceValue[registerHighLow]);
                }

                // Set appropriate flags
                // Set AF (although 'undefined' by Intel specs, hardware seems to clear it)
                cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
                // Set ZF
                cpu.flags[CPU.REGISTER_FLAGS_ZF] = sourceValue[registerHighLow] == 0 ? true : false;
                // Set SF on particular byte of destinationRegister (set when MSB is 1, occurs when destReg >= 0x80)
                cpu.flags[CPU.REGISTER_FLAGS_SF] = sourceValue[registerHighLow] < 0 ? true : false;
                // Set PF on particular byte of destinationRegister
                cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(sourceValue[registerHighLow]);
                break; // SAR

            default:
                // Throw exception for illegal nnn bits
                throw new CPUInstructionException("Shift Group 2 (0xD0/6) no case match");
        }
    }
}
