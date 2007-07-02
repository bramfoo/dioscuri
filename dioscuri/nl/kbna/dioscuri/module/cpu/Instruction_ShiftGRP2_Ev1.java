/*
 * $Revision: 1.1 $ $Date: 2007-07-02 14:31:39 $ $Author: blohman $
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

package nl.kbna.dioscuri.module.cpu;

import nl.kbna.dioscuri.exception.CPUInstructionException;

/**
 * Intel opcode D1<BR>
 * Immediate Group 2 opcode extension: ROL, ROR, RCL, RCR, SHL/SAL, SHR, SAR.<BR>
 * Performs the selected instruction (indicated by bits 5, 4, 3 of the ModR/M byte) using constant 1.<BR>
 * Flags modified: depending on instruction can be any of: OF, SF, ZF, AF, PF, CF
 */
public class Instruction_ShiftGRP2_Ev1 implements Instruction
{

    // Attributes
    private CPU cpu;

    boolean operandWordSize;

    byte addressByte;
    byte[] memoryReferenceLocation;
    byte[] memoryReferenceDisplacement;

    byte[] sourceValue;
    byte[] eSourceValue;
    int overFlowCheck;

    int bitShift;
    int byteShift;
    long shiftResult;
    int carryBit;
    int newCarryBit;

    byte[] tempResult;

    
    // Constructors
    /**
     * Class constructor
     */
    public Instruction_ShiftGRP2_Ev1()
    {
        operandWordSize = true;

        addressByte = 0;
        memoryReferenceLocation = new byte[2];
        memoryReferenceDisplacement = new byte[2];

        sourceValue = new byte[2];
        eSourceValue = new byte[2];
        overFlowCheck = 0;

        bitShift = 0;
        byteShift = 0;
        shiftResult = 0;
        carryBit = 0;
        newCarryBit = 0;

        tempResult = new byte[2];
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_ShiftGRP2_Ev1(CPU processor)
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
        // Reset sourceValue and eSourceValue (to lose pointer to earlier words)
        sourceValue = new byte[2];
        eSourceValue = new byte[2];

        // Get addresByte
        addressByte = cpu.getByteFromCode();

        // Determine displacement of memory location (if any)
        memoryReferenceDisplacement = cpu.decodeMM(addressByte);

        // Set number of shifts to 1
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
                }
                else
                {
                    // ROL on memory
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);
                    
                    // Get word from memory
                    sourceValue = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
                }
                
                // Convert bytes to long
                shiftResult = (((((long)sourceValue[CPU.REGISTER_GENERAL_HIGH]) << 8) & 0xFF00) + 
                        ((((long)sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF)));
                
                // Determine result of carry; this is the MSB
                carryBit = (shiftResult & 0x80) == 0x80 ? 1 : 0;
                
                // Rotate left by 1. This is equivalent to a SHL of 1 ORed with the previously calculated carryBit
                shiftResult = (((shiftResult << bitShift) & 0xFFFF) | carryBit);
                
                // Return result to register
                sourceValue[CPU.REGISTER_GENERAL_HIGH] = ((byte) (shiftResult >> 8));
                sourceValue[CPU.REGISTER_GENERAL_LOW] = ((byte) (shiftResult & 0xFF));
                
                // Store result in memory for memory operations
                // Note: if destination register is real register, it already contains result (stored directly)
                if ( ((addressByte >> 6) & 0x03) != 3)
                {
                    // Store result back in memory
                    cpu.setWordInMemorySegment(addressByte, memoryReferenceLocation, sourceValue);
                }
                
                // Set appropriate flags
                // Set CF; this has already been calculated
                cpu.flags[CPU.REGISTER_FLAGS_CF] = carryBit == 1 ? true : false;
                // Set OF (only defined for 1-bit rotates); XOR of CF (after rotate) and MSB of result.
                cpu.flags[CPU.REGISTER_FLAGS_OF] = (carryBit ^ ((sourceValue[CPU.REGISTER_GENERAL_HIGH] & 0x80) == 0x80 ? 1 : 0)) == 1 ? true : false;
                break; // ROL
                
            case 1: // ROR - Rotate bits 1 position right. Flags affected: CF, OF
                // Execute rotate on reg or mem. Determine this from mm bits of addressbyte
                if (((addressByte >> 6) & 0x03) == 3)
                {
                    // ROR on register
                    // Determine destination from addressbyte (source is the same)
                    sourceValue = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
                }
                else
                {
                    // ROR on memory
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);
                    
                    // Get word from memory
                    sourceValue = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
                }
                
                // Convert bytes to long
                shiftResult = (((((long)sourceValue[CPU.REGISTER_GENERAL_HIGH]) << 8) & 0xFF00) + 
                        ((((long)sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF)));
                
                // Determine result of carry; this is the LSB
                carryBit = (shiftResult & 0x01) == 0x01 ? 1 : 0;
                
                // Rotate right by 1. This is equivalent to a SHR of 1 ORed with the previously calculated carryBit
                shiftResult = ((shiftResult >> bitShift) | (carryBit << (16 - bitShift)));
                
                // Return result to register
                sourceValue[CPU.REGISTER_GENERAL_HIGH] = ((byte) (shiftResult >> 8));
                sourceValue[CPU.REGISTER_GENERAL_LOW] = ((byte) (shiftResult & 0xFF));
                
                // Store result in memory for memory operations
                // Note: if destination register is real register, it already contains result (stored directly)
                if ( ((addressByte >> 6) & 0x03) != 3)
                {
                    // Store result back in memory
                    cpu.setWordInMemorySegment(addressByte, memoryReferenceLocation, sourceValue);
                }
                
                // Set appropriate flags
                // Set CF; this has already been calculated
                cpu.flags[CPU.REGISTER_FLAGS_CF] = carryBit == 1 ? true : false;
                // OF (only defined for 1-bit rotates) is XOR of the two most-significant bits
                cpu.flags[CPU.REGISTER_FLAGS_OF] = (((sourceValue[CPU.REGISTER_GENERAL_HIGH]>>7) & 0x01) ^ ((sourceValue[CPU.REGISTER_GENERAL_HIGH]>>6) & 0x01)) == 0x01 ? true : false;
                break; // ROR
                
            case 2: // RCL - Rotate bits + CF 1 position left; Flags affected: CF, OF
                // Execute rotate on reg or mem. Determine this from mm bits of addressbyte
                if (((addressByte >> 6) & 0x03) == 3)
                {
                    // RCL on register
                    // Determine destination from addressbyte (source is the same)
                    sourceValue = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
                }
                else
                {
                    // RCL on memory
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);
                    sourceValue = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
                }
                
                // Determine carry value
                carryBit = cpu.flags[CPU.REGISTER_FLAGS_CF] ? 1 : 0;
                newCarryBit = (sourceValue[CPU.REGISTER_GENERAL_HIGH] & 0x80) == 0x80 ? 1 : 0;
                
                // Convert bytes to long
                shiftResult = (((((long)sourceValue[CPU.REGISTER_GENERAL_HIGH]) << 8) & 0xFF00) + 
                                   ((((long)sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF)));

                // Rotate left by 1
                shiftResult = ((shiftResult << bitShift) & 0xFFFF) | carryBit;
                
                // Update carryBit
                carryBit = newCarryBit;

                // Write value back to origin
                sourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) ((shiftResult >> 8) & 0xFF);
                sourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) (shiftResult & 0xFF);
    
                // Store result in memory for RCL mem operations
                // Note: if destination register is real register, it already contains result (stored directly)
                if ( ((addressByte >> 6) & 0x03) != 3)
                {
                    cpu.setWordInMemorySegment(addressByte, memoryReferenceLocation, sourceValue);
                }
                
                // Set appropriate flags
                // Set CF; this has already been calculated
                cpu.flags[CPU.REGISTER_FLAGS_CF] = carryBit == 1 ? true : false;
                // Set OF (only defined for 1-bit rotates); XOR of CF (after rotate) and MSB of result.
                cpu.flags[CPU.REGISTER_FLAGS_OF] = (carryBit ^ ((sourceValue[CPU.REGISTER_GENERAL_HIGH] & 0x80) == 0x80 ? 1 : 0)) == 1 ? true : false;
                break; // RCL
            
            case 3: // RCR - Rotate bits + CF 1 position right; Flags affected: CF, OF
                // Execute rotate on reg or mem. Determine this from mm bits of addressbyte
                if (((addressByte >> 6) & 0x03) == 3)
                {
                    // RCR on register
                    // Determine destination from addressbyte (source is the same)
                    sourceValue = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
                }
                else
                {
                    // RCR on memory
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);
                    sourceValue = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
                }
                
                // Determine carry value
                carryBit = cpu.flags[CPU.REGISTER_FLAGS_CF] ? 1 : 0;
                newCarryBit = (sourceValue[CPU.REGISTER_GENERAL_LOW] & 0x01) == 0x01 ? 1 : 0;
                
                // Convert bytes to long
                shiftResult = (((((long)sourceValue[CPU.REGISTER_GENERAL_HIGH]) << 8) & 0xFF00) + 
                                   ((((long)sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF)));
                
                // Rotate right by 1
                shiftResult = ((shiftResult >> bitShift) | (carryBit << (16 - bitShift)));
                
                // Update carryBit
                carryBit = newCarryBit;
                
                // Write value back to origin
                sourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) ((shiftResult >> 8) & 0xFF);
                sourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) (shiftResult & 0xFF);
    
                // Store result in memory for RCR mem operations
                // Note: if destination register is real register, it already contains result (stored directly)
                if ( ((addressByte >> 6) & 0x03) != 3)
                {
                    cpu.setWordInMemorySegment(addressByte, memoryReferenceLocation, sourceValue);
                }
                
                // Set appropriate flags
                // Set CF; this has already been calculated
                cpu.flags[CPU.REGISTER_FLAGS_CF] = carryBit == 1 ? true : false;
                // OF (only defined for 1-bit rotates) is XOR of the two most-significant bits
                cpu.flags[CPU.REGISTER_FLAGS_OF] = (((sourceValue[CPU.REGISTER_GENERAL_HIGH]>>7) & 0x01) ^ ((sourceValue[CPU.REGISTER_GENERAL_HIGH]>>6) & 0x01)) == 0x01 ? true : false;
                break;  // RCR
                
            case 4: // SHL/SAL;
                // Flags affected: OF, while CF, SF, ZF, PF, AF are undefined
                // To shift, mainly 2 approaches can be taken:
                // A. Shifting using a long
                // B. Shifting by shifting each byte individually (first on byte level, than on bit level)
                
                // Below, approach A is chosen. Part of approach B is also implemented and included as comment.
                // Approach A:
                
                // Check if 16 or 32-bit
                if (cpu.doubleWord)
                {
                    // 32-bit registers
                    // Execute shift on reg or mem. Determine this from mm bits of addressbyte
                    if (((addressByte >> 6) & 0x03) == 3)
                    {
                        // SHL on register
                        // Determine destination from addressbyte (source is the same)
                        sourceValue = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
                        eSourceValue = cpu.decodeExtraRegister(addressByte & 0x07);
                    }
                    else
                    {
                        // SHL on memory
                        // Determine memory location
                        memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);
    
                        sourceValue = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
                        
                        // Increase memory location by 2
                        int overFlowCheck = (((int) memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW]) & 0xFF) + 2;
                        if (overFlowCheck > 0xFF)
                        {
                            memoryReferenceLocation[CPU.REGISTER_GENERAL_HIGH]++;
                        }
                        memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] += 2;
    
                        eSourceValue = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
                    }
    
                    // Convert bytes to long and shift left
                    shiftResult = (((((long)eSourceValue[CPU.REGISTER_GENERAL_HIGH]) << 24) & 0xFF000000) + 
                                       ((((long)eSourceValue[CPU.REGISTER_GENERAL_LOW]) << 16) & 0xFF0000) + 
                                       ((((long)sourceValue[CPU.REGISTER_GENERAL_HIGH]) << 8) & 0xFF00) + 
                                       ((((long)sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF))) << (bitShift - 1);
                    carryBit = (int) ((shiftResult & 0x80000000) >> 31) & 0x01;
                    shiftResult = shiftResult << 1;
    
                    // Set result in double word
                    eSourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) ((shiftResult >> 24) & 0xFF);
                    eSourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) ((shiftResult >> 16) & 0xFF);
                    sourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) ((shiftResult >> 8) & 0xFF);
                    sourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) (shiftResult & 0xFF);
    
                    // Store result in memory for SHL mem operations
                    // Note: if destination register is real register, it already contains result (stored directly)
                    if ( ((addressByte >> 6) & 0x03) != 3)
                    {
                        // Do this in reverse order because memlocation was incremented
                        cpu.setWordInMemorySegment(addressByte, memoryReferenceLocation, eSourceValue);
                        
                        // Decrement memlocation
                        memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] -= 2;
                        if (memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] == -1 || memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] == -2)
                        {
                            // Underflow
                            memoryReferenceLocation[CPU.REGISTER_GENERAL_HIGH]--;
                        }
                        cpu.setWordInMemorySegment(addressByte, memoryReferenceLocation, sourceValue);
                    }
    
                    // Set appropriate flags
                    // Set AF (although is undefined by specs, hardware seems to clear it)
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
                    // Set CF; this is equal to the last bit shifted out of the high register
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = carryBit == 1? true : false;
                    // Clear OF if the most significant bit of the result is the same as the CF flag (only for 1-bitshifts)
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = (cpu.flags[CPU.REGISTER_FLAGS_CF] && ((eSourceValue[CPU.REGISTER_GENERAL_HIGH])>>7) == 1) || (!(cpu.flags[CPU.REGISTER_FLAGS_CF]) && ((eSourceValue[CPU.REGISTER_GENERAL_HIGH])>>7) == 0) ? false : true;
                    // Set ZF
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = sourceValue[CPU.REGISTER_GENERAL_HIGH] == 0 && sourceValue[CPU.REGISTER_GENERAL_LOW] == 0 && eSourceValue[CPU.REGISTER_GENERAL_HIGH] == 0 && eSourceValue[CPU.REGISTER_GENERAL_LOW] == 0 ? true : false;
                    // Set SF on particular byte of sourceValue (set when MSB is 1, occurs when destReg >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = eSourceValue[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
                    // Set PF on particular byte of sourceValue
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(sourceValue[CPU.REGISTER_GENERAL_LOW]);
                }
                else
                {
                    // 16-bit registers
                    // Execute shift on reg or mem. Determine this from mm bits of addressbyte
                    if (((addressByte >> 6) & 0x03) == 3)
                    {
                        // SHL on register
                        // Determine destination from addressbyte (source is the same)
                        sourceValue = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
                    }
                    else
                    {
                        // SHL on memory
                        // Determine memory location
                        memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);
                        sourceValue = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
                    }
    
                    // Convert bytes to long and shift
                    shiftResult = (((((long)sourceValue[CPU.REGISTER_GENERAL_HIGH]) << 8) & 0xFF00) + 
                                       ((((long)sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF))) << (bitShift - 1);
                    carryBit = (int) (shiftResult >> 15) & 0x01;
                    shiftResult = shiftResult << 1;
    
                    // Set result in word
                    sourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) ((shiftResult >> 8) & 0xFF);
                    sourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) (shiftResult & 0xFF);
    
                    // Store result in memory for SHL mem operations
                    // Note: if destination register is real register, it already contains result (stored directly)
                    if ( ((addressByte >> 6) & 0x03) != 3)
                    {
                        // Do this in reverse order because memlocation was incremented
                        cpu.setWordInMemorySegment(addressByte, memoryReferenceLocation, sourceValue);
                    }
    
                    // Set appropriate flags
                    // Set CF; this is equal to the last bit shifted out of the high register
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = carryBit == 1? true : false;
                    
                    // Set OF only if 1-bit shift occurred
                    if (bitShift == 1)
                    {
                        // Clear OF if the most significant bit of the result is the same as the CF flag
                        cpu.flags[CPU.REGISTER_FLAGS_OF] = (cpu.flags[CPU.REGISTER_FLAGS_CF] && ((sourceValue[CPU.REGISTER_GENERAL_HIGH])>>7) == 1) || (!(cpu.flags[CPU.REGISTER_FLAGS_CF]) && ((sourceValue[CPU.REGISTER_GENERAL_HIGH])>>7) == 0) ? false : true;
                    }
                    
                    // Set AF (although is undefined by specs, hardware seems to clear it)
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
                    // Set ZF
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = sourceValue[CPU.REGISTER_GENERAL_HIGH] == 0 && sourceValue[CPU.REGISTER_GENERAL_LOW] == 0 ? true : false;
                    // Set SF on particular byte of sourceValue (set when MSB is 1, occurs when destReg >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = sourceValue[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
                    // Set PF on particular byte of sourceValue
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(sourceValue[CPU.REGISTER_GENERAL_LOW]);
                }
                break;  // SHL/SAL
                
            case 5:  // SHR; unsigned. flags affected: CF, OF, SF, ZF, PF, AF (undefined)
                // Check if 16 or 32-bit
                if (cpu.doubleWord)
                {
                    // 32-bit registers
                    // Execute shift on reg or mem. Determine this from mm bits of addressbyte
                    if (((addressByte >> 6) & 0x03) == 3)
                    {
                        // SHR on register
                        // Determine destination from addressbyte (source is the same)
                        sourceValue = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
                        eSourceValue = cpu.decodeExtraRegister(addressByte & 0x07);
                    }
                    else
                    {
                        // SHR on memory
                        // Determine memory location
                        memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);
    
                        sourceValue = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
                        
                        // Increase memory location by 2
                        int overFlowCheck = (((int) memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW]) & 0xFF) + 2;
                        if (overFlowCheck > 0xFF)
                        {
                            memoryReferenceLocation[CPU.REGISTER_GENERAL_HIGH]++;
                        }
                        memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] += 2;
    
                        eSourceValue = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
                    }
    
                    // Set OF to MSB of original operand (for 1-bitshifts only)
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = (eSourceValue[CPU.REGISTER_GENERAL_HIGH] & 0x80) == 0x80 ? true : false;
                    
                    // Convert bytes to long and shift right
                    shiftResult = (((((long)eSourceValue[CPU.REGISTER_GENERAL_HIGH]) << 24) & 0xFF000000) + 
                                       ((((long)eSourceValue[CPU.REGISTER_GENERAL_LOW]) << 16) & 0xFF0000) + 
                                       ((((long)sourceValue[CPU.REGISTER_GENERAL_HIGH]) << 8) & 0xFF00) + 
                                       ((((long)sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF))) >> (bitShift - 1);
                    carryBit = (int) (shiftResult & 0x01);
                    shiftResult = shiftResult >> 1;
    
                    // Set result in double word
                    eSourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) ((shiftResult >> 24) & 0xFF);
                    eSourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) ((shiftResult >> 16) & 0xFF);
                    sourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) ((shiftResult >> 8) & 0xFF);
                    sourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) (shiftResult & 0xFF);
    
                    // Store result in memory for SHR mem operations
                    // Note: if destination register is real register, it already contains result (stored directly)
                    if ( ((addressByte >> 6) & 0x03) != 3)
                    {
                        // Do this in reverse order because memlocation was incremented
                        cpu.setWordInMemorySegment(addressByte, memoryReferenceLocation, eSourceValue);
                        
                        // Decrement memlocation
                        memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] -= 2;
                        if (memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] == -1 || memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] == -2)
                        {
                            // Underflow
                            memoryReferenceLocation[CPU.REGISTER_GENERAL_HIGH]--;
                        }
                        cpu.setWordInMemorySegment(addressByte, memoryReferenceLocation, sourceValue);
                    }
    
                    // Set appropriate flags
                    // Set CF; this is equal to the last bit shifted out of the high register
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = carryBit == 1? true : false;
                    // Set AF (although is undefined by specs, hardware seems to clear it)
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
                    // Set ZF
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = sourceValue[CPU.REGISTER_GENERAL_HIGH] == 0 && sourceValue[CPU.REGISTER_GENERAL_LOW] == 0 && eSourceValue[CPU.REGISTER_GENERAL_HIGH] == 0 && eSourceValue[CPU.REGISTER_GENERAL_LOW] == 0 ? true : false;
                    // Set SF on particular byte of sourceValue (set when MSB is 1, occurs when destReg >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = eSourceValue[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
                    // Set PF on particular byte of sourceValue
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(sourceValue[CPU.REGISTER_GENERAL_LOW]);
                }
                else
                {
                    // 16-bit registers
                    // Execute shift on reg or mem. Determine this from mm bits of addressbyte
                    if (((addressByte >> 6) & 0x03) == 3)
                    {
                        // SHR on register
                        // Determine destination from addressbyte (source is the same)
                        sourceValue = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
                    }
                    else
                    {
                        // SHR on memory
                        // Determine memory location
                        memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);
                        sourceValue = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
                    }
    
                    // Set OF to MSB of original operand (for 1-bitshifts only)
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = (sourceValue[CPU.REGISTER_GENERAL_HIGH] & 0x80) == 0x80 ? true : false;
                    
                    // Convert bytes to long and shift
                    shiftResult = (((((long)sourceValue[CPU.REGISTER_GENERAL_HIGH]) << 8) & 0xFF00) + 
                                       ((((long)sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF))) >> (bitShift - 1);
                    carryBit = (int) (shiftResult & 0x01);
                    shiftResult = shiftResult >> 1;
    
                    // Set result in word
                    sourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) ((shiftResult >> 8) & 0xFF);
                    sourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) (shiftResult & 0xFF);
    
                    // Store result in memory for SHR mem operations
                    // Note: if destination register is real register, it already contains result (stored directly)
                    if ( ((addressByte >> 6) & 0x03) != 3)
                    {
                        // Do this in reverse order because memlocation was incremented
                        cpu.setWordInMemorySegment(addressByte, memoryReferenceLocation, sourceValue);
                    }
    
                    // Set appropriate flags
                    // Set CF; this is equal to the last bit shifted out of the high register
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = carryBit == 1? true : false;
                    // Set AF (although is undefined by specs, hardware seems to clear it)
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
                    // Set ZF
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = sourceValue[CPU.REGISTER_GENERAL_HIGH] == 0 && sourceValue[CPU.REGISTER_GENERAL_LOW] == 0 ? true : false;
                    // Set SF on particular byte of sourceValue (set when MSB is 1, occurs when destReg >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = sourceValue[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
                    // Set PF on particular byte of sourceValue
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(sourceValue[CPU.REGISTER_GENERAL_LOW]);
                }
                break; //SHR
            
            case 6: // does not exist 
                // Throw exception for illegal nnn bits
                throw new CPUInstructionException("Shift Group 2 (0xD2/6) illegal reg bits");
                
            case 7: // SAR - Shift right signed. Flags affected: CF, OF, SF, ZF, PF, AF (undefined)
                // Check if 16 or 32-bit
                if (cpu.doubleWord)
                {
                    // 32-bit registers
                    // Execute shift on reg or mem. Determine this from mm bits of addressbyte
                    if (((addressByte >> 6) & 0x03) == 3)
                    {
                        // SAR on register
                        // Determine destination from addressbyte (source is the same)
                        sourceValue = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
                        eSourceValue = cpu.decodeExtraRegister(addressByte & 0x07);
                    }
                    else
                    {
                        // SAR on memory
                        // Determine memory location
                        memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);
    
                        sourceValue = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
                        
                        // Increase memory location by 2
                        int overFlowCheck = (((int) memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW]) & 0xFF) + 2;
                        if (overFlowCheck > 0xFF)
                        {
                            memoryReferenceLocation[CPU.REGISTER_GENERAL_HIGH]++;
                        }
                        memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] += 2;
    
                        eSourceValue = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
                    }
    
                    // Set OF to MSB of original operand (for 1-bitshifts only)
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = (eSourceValue[CPU.REGISTER_GENERAL_HIGH] & 0x80) == 0x80 ? true : false;
                    
                    // Convert bytes to long and shift right (with preserving sign of MSB)
                    shiftResult = ((((long)eSourceValue[CPU.REGISTER_GENERAL_HIGH]) << 24) + 
                                       ((((long)eSourceValue[CPU.REGISTER_GENERAL_LOW]) << 16) & 0xFF0000) + 
                                       ((((long)sourceValue[CPU.REGISTER_GENERAL_HIGH]) << 8) & 0xFF00) + 
                                       ((((long)sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF))) >> (bitShift - 1);
                    carryBit = (int) (shiftResult & 0x01);
                    shiftResult = shiftResult >> 1;
    
                    // Set result in double word
                    eSourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) ((shiftResult >> 24) & 0xFF);
                    eSourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) ((shiftResult >> 16) & 0xFF);
                    sourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) ((shiftResult >> 8) & 0xFF);
                    sourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) (shiftResult & 0xFF);
    
                    // Store result in memory for SAR mem operations
                    // Note: if destination register is real register, it already contains result (stored directly)
                    if ( ((addressByte >> 6) & 0x03) != 3)
                    {
                        // Do this in reverse order because memlocation was incremented
                        cpu.setWordInMemorySegment(addressByte, memoryReferenceLocation, eSourceValue);
                        
                        // Decrement memlocation
                        memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] -= 2;
                        if (memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] == -1 || memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] == -2)
                        {
                            // Underflow
                            memoryReferenceLocation[CPU.REGISTER_GENERAL_HIGH]--;
                        }
                        cpu.setWordInMemorySegment(addressByte, memoryReferenceLocation, sourceValue);
                    }
    
                    // Set appropriate flags
                    // Set CF; this is equal to the last bit shifted out of the high register
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = carryBit == 1? true : false;
                    // Set AF (although is undefined by specs, hardware seems to clear it)
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
                    // Set ZF
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = sourceValue[CPU.REGISTER_GENERAL_HIGH] == 0 && sourceValue[CPU.REGISTER_GENERAL_LOW] == 0 && eSourceValue[CPU.REGISTER_GENERAL_HIGH] == 0 && eSourceValue[CPU.REGISTER_GENERAL_LOW] == 0 ? true : false;
                    // Set SF on particular byte of sourceValue (set when MSB is 1, occurs when destReg >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = eSourceValue[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
                    // Set PF on particular byte of sourceValue
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(sourceValue[CPU.REGISTER_GENERAL_LOW]);
                }
                else
                {
                    // 16-bit registers
                    // Execute shift on reg or mem. Determine this from mm bits of addressbyte
                    if (((addressByte >> 6) & 0x03) == 3)
                    {
                        // SAR on register
                        // Determine destination from addressbyte (source is the same)
                        sourceValue = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
                    }
                    else
                    {
                        // SAR on memory
                        // Determine memory location
                        memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);
                        sourceValue = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
                    }
    
                    // Set OF to MSB of original operand (for 1-bitshifts only)
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = (sourceValue[CPU.REGISTER_GENERAL_HIGH] & 0x80) == 0x80 ? true : false;
                    
                    // Convert bytes to long and shift right (with preserving sign of MSB)
                    shiftResult = ((((long)sourceValue[CPU.REGISTER_GENERAL_HIGH]) << 8) + 
                                       ((((long)sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF))) >> (bitShift - 1);
                    carryBit = (int) (shiftResult & 0x01);
                    shiftResult = shiftResult >> 1;
    
                    // Set result in word
                    sourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) ((shiftResult >> 8) & 0xFF);
                    sourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) (shiftResult & 0xFF);
    
                    // Store result in memory for SAR mem operations
                    // Note: if destination register is real register, it already contains result (stored directly)
                    if ( ((addressByte >> 6) & 0x03) != 3)
                    {
                        // Do this in reverse order because memlocation was incremented
                        cpu.setWordInMemorySegment(addressByte, memoryReferenceLocation, sourceValue);
                    }
    
                    // Set appropriate flags
                    // Set CF; this is equal to the last bit shifted out of the high register
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = carryBit == 1? true : false;
                    // Set AF (although is undefined by specs, hardware seems to clear it)
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
                    // Set ZF
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = sourceValue[CPU.REGISTER_GENERAL_HIGH] == 0 && sourceValue[CPU.REGISTER_GENERAL_LOW] == 0 ? true : false;
                    // Set SF on particular byte of sourceValue (set when MSB is 1, occurs when destReg >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = sourceValue[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
                    // Set PF on particular byte of sourceValue
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(sourceValue[CPU.REGISTER_GENERAL_LOW]);
                }
                break; //SAR
                
            default:
                // Throw exception for illegal nnn bits
                throw new CPUInstructionException("Shift Group 2 (0xD2/6) no case match");
        }
    }
}
