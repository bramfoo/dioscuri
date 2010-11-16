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

import dioscuri.exception.CPUInstructionException;

/**
 * Intel opcode D3<BR>
 * Immediate Group 2 opcode extension: ROL, ROR, RCL, RCR, SHL/SAL, SHR, SAR.<BR>
 * Performs the selected instruction (indicated by bits 5, 4, 3 of the ModR/M
 * byte) using CL.<BR>
 * Flags modified: depending on instruction can be any of: OF, SF, ZF, AF, PF,
 * CF
 */
public class Instruction_ShiftGRP2_EvCL implements Instruction {

    // Attributes
    private CPU cpu;

    boolean operandWordSize;

    byte addressByte;
    byte[] memoryReferenceLocation;
    byte[] memoryReferenceDisplacement;

    byte[] sourceValue;
    byte[] eSourceValue;

    int bitShift;
    long shiftResult;
    int carryBit;
    int newCarryBit;

    // Constructors

    /**
     * Class constructor
     */
    public Instruction_ShiftGRP2_EvCL() {
        operandWordSize = true;

        addressByte = 0;
        memoryReferenceLocation = new byte[2];
        memoryReferenceDisplacement = new byte[2];

        sourceValue = new byte[2];
        eSourceValue = new byte[2];

        bitShift = 0;
        shiftResult = 0;
        carryBit = 0;
        newCarryBit = 0;
    }

    /**
     * Class constructor specifying processor reference
     *
     * @param processor Reference to CPU class
     */
    public Instruction_ShiftGRP2_EvCL(CPU processor) {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Execute any of the following Immediate Group 2 instructions: ROL, ROR,
     * RCL, RCR, SHL/SAL, SHR, SAR.<BR>
     *
     * @throws CPUInstructionException
     */
    public void execute() throws CPUInstructionException {
        // Reset sourceValue and eSourceValue (to lose pointer to earlier words)
        sourceValue = new byte[2];
        eSourceValue = new byte[2];

        // Get addresByte
        addressByte = cpu.getByteFromCode();

        // Determine displacement of memory location (if any)
        memoryReferenceDisplacement = cpu.decodeMM(addressByte);

        // Get CL byte for number of shifts and limit it to 5 bits (only 0 - 31
        // shifts allowed)
        bitShift = (byte) (cpu.cx[CPU.REGISTER_GENERAL_LOW] & 0x1F);

        // Execute instruction decoded from nnn (bits 5, 4, 3 in ModR/M byte)
        switch ((addressByte & 0x38) >> 3) {
            case 0: // ROL - Rotate bits 1 position left. Flags affected: CF, OF
                // Execute rotate on reg or mem. Determine this from mm bits of
                // addressbyte
                if (((addressByte >> 6) & 0x03) == 3) {
                    // ROL on register
                    // Determine destination from addressbyte (source is the same)
                    sourceValue = cpu.decodeRegister(operandWordSize,
                            addressByte & 0x07);
                } else {
                    // ROL on memory
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Get word from memory
                    sourceValue = cpu.getWordFromMemorySegment(addressByte,
                            memoryReferenceLocation);
                }

                // Convert bytes to long
                shiftResult = (((((long) sourceValue[CPU.REGISTER_GENERAL_HIGH]) << 8) & 0xFF00) + ((((long) sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF)));

                // FIXME: For a ROL of 0 w/ MSB == 1, does the carry flag come out
                // correct?
                // Determine result of carry; this is the MSB
                carryBit = (shiftResult & 0x8000) == 0x8000 ? 1 : 0;

                // Rotate left
                for (int s = 0; s < bitShift; s++) {
                    // Determine result of carry; this is the MSB
                    carryBit = (shiftResult & 0x8000) == 0x8000 ? 1 : 0;

                    // Rotate left by 1. This is equivalent to a SHL of 1 ORed with
                    // the previously calculated carryBit
                    shiftResult = (((shiftResult << 1) & 0xFFFF) | carryBit);
                }

                // Return result to register
                sourceValue[CPU.REGISTER_GENERAL_HIGH] = ((byte) (shiftResult >> 8));
                sourceValue[CPU.REGISTER_GENERAL_LOW] = ((byte) (shiftResult & 0xFF));

                // Store result in memory for memory operations
                // Note: if destination register is real register, it already
                // contains result (stored directly)
                if (((addressByte >> 6) & 0x03) != 3) {
                    // Store result back in memory
                    cpu.setWordInMemorySegment(addressByte,
                            memoryReferenceLocation, sourceValue);
                }

                // Set appropriate flags
                // Set CF; this has already been calculated
                cpu.flags[CPU.REGISTER_FLAGS_CF] = carryBit == 1 ? true : false;
                // Set OF only if 1-bit shift occurred
                if (bitShift == 1) {
                    // Set OF (only defined for 1-bit rotates); XOR of CF (after
                    // rotate) and MSB of result.
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = (carryBit ^ ((sourceValue[CPU.REGISTER_GENERAL_HIGH] & 0x80) == 0x80 ? 1
                            : 0)) == 1 ? true : false;
                } else {
                    // Hardware seems to clear it
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = false;
                }
                break; // ROL

            case 1: // ROR
                // Execute rotate on reg or mem. Determine this from mm bits of
                // addressbyte
                if (((addressByte >> 6) & 0x03) == 3) {
                    // ROR on register
                    // Determine destination from addressbyte (source is the same)
                    sourceValue = cpu.decodeRegister(operandWordSize,
                            addressByte & 0x07);
                } else {
                    // ROR on memory
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Get word from memory
                    sourceValue = cpu.getWordFromMemorySegment(addressByte,
                            memoryReferenceLocation);
                }

                // Convert bytes to long
                shiftResult = (((((long) sourceValue[CPU.REGISTER_GENERAL_HIGH]) << 8) & 0xFF00) + ((((long) sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF)));

                // Set the carryBit to 0 (for RORs of length 0)
                carryBit = 0;

                // Rotate right
                for (int s = 0; s < bitShift; s++) {
                    // Determine result of carry; this is the LSB
                    carryBit = (shiftResult & 0x01) == 0x01 ? 1 : 0;

                    // Rotate right by 1. This is equivalent to a SHR of 1 ORed with
                    // the previously calculated carryBit
                    shiftResult = ((shiftResult >> 1) | (carryBit << (15)));
                }

                // Return result to register
                sourceValue[CPU.REGISTER_GENERAL_HIGH] = ((byte) (shiftResult >> 8));
                sourceValue[CPU.REGISTER_GENERAL_LOW] = ((byte) (shiftResult & 0xFF));

                // Store result in memory for memory operations
                // Note: if destination register is real register, it already
                // contains result (stored directly)
                if (((addressByte >> 6) & 0x03) != 3) {
                    // Store result back in memory
                    cpu.setWordInMemorySegment(addressByte,
                            memoryReferenceLocation, sourceValue);
                }

                // Set appropriate flags
                // Set CF; this has already been calculated
                cpu.flags[CPU.REGISTER_FLAGS_CF] = carryBit == 1 ? true : false;
                // Set OF only if 1-bit shift occurred
                if (bitShift == 1) {
                    // OF (only defined for 1-bit rotates) is XOR of the two
                    // most-significant bits
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = (((sourceValue[CPU.REGISTER_GENERAL_HIGH] >> 7) & 0x01) ^ ((sourceValue[CPU.REGISTER_GENERAL_HIGH] >> 6) & 0x01)) == 0x01 ? true
                            : false;
                } else {
                    // Hardware seems to clear it
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = false;
                }
                break; // ROR

            case 2: // RCL - Rotate bits + CF CL positions left; Flags affected: CF,
                // OF
                // Execute rotate on reg or mem. Determine this from mm bits of
                // addressbyte
                if (((addressByte >> 6) & 0x03) == 3) {
                    // RCL on register
                    // Determine destination from addressbyte (source is the same)
                    sourceValue = cpu.decodeRegister(operandWordSize,
                            addressByte & 0x07);
                } else {
                    // RCL on memory
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);
                    sourceValue = cpu.getWordFromMemorySegment(addressByte,
                            memoryReferenceLocation);
                }

                // Convert bytes to long
                shiftResult = (((((long) sourceValue[CPU.REGISTER_GENERAL_HIGH]) << 8) & 0xFF00) + ((((long) sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF)));

                // Determine result of carry;
                carryBit = cpu.flags[CPU.REGISTER_FLAGS_CF] ? 1 : 0;

                // Rotate left
                for (int s = 0; s < bitShift; s++) {
                    // Determine result of carry; this is the MSB
                    newCarryBit = (shiftResult & 0x8000) == 0x8000 ? 1 : 0;

                    // Rotate left by 1
                    shiftResult = ((shiftResult << 1) & 0xFFFF) | carryBit;

                    // Update carryBit
                    carryBit = newCarryBit;
                }

                // Write value back to origin
                sourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) ((shiftResult >> 8) & 0xFF);
                sourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) (shiftResult & 0xFF);

                // Store result in memory for RCL mem operations
                // Note: if destination register is real register, it already
                // contains result (stored directly)
                if (((addressByte >> 6) & 0x03) != 3) {
                    cpu.setWordInMemorySegment(addressByte,
                            memoryReferenceLocation, sourceValue);
                }

                // Set appropriate flags
                // Set CF; this has already been calculated
                cpu.flags[CPU.REGISTER_FLAGS_CF] = carryBit == 1 ? true : false;
                // Set OF only if 1-bit shift occurred
                if (bitShift == 1) {
                    // Set OF (only defined for 1-bit rotates); XOR of CF (after
                    // rotate) and MSB of result.
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = (carryBit ^ ((sourceValue[CPU.REGISTER_GENERAL_HIGH] & 0x80) == 0x80 ? 1
                            : 0)) == 1 ? true : false;
                } else {
                    // Hardware seems to clear it
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = false;
                }
                break; // RCL

            case 3: // RCR - Rotate bits + CF CL positions right; Flags affected:
                // CF, OF
                // Execute rotate on reg or mem. Determine this from mm bits of
                // addressbyte
                if (((addressByte >> 6) & 0x03) == 3) {
                    // RCR on register
                    // Determine destination from addressbyte (source is the same)
                    sourceValue = cpu.decodeRegister(operandWordSize,
                            addressByte & 0x07);
                } else {
                    // RCR on memory
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);
                    sourceValue = cpu.getWordFromMemorySegment(addressByte,
                            memoryReferenceLocation);
                }

                // Convert bytes to long
                shiftResult = (((((long) sourceValue[CPU.REGISTER_GENERAL_HIGH]) << 8) & 0xFF00) + ((((long) sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF)));

                // Determine result of carry;
                carryBit = cpu.flags[CPU.REGISTER_FLAGS_CF] ? 1 : 0;

                // Rotate right
                for (int s = 0; s < bitShift; s++) {
                    // Determine result of carry; this is the MSB
                    newCarryBit = (shiftResult & 0x0001) == 0x0001 ? 1 : 0;

                    // Rotate right by 1
                    shiftResult = ((shiftResult >> 1) & 0x7FFF)
                            | ((carryBit) << 15);

                    // Update carryBit
                    carryBit = newCarryBit;
                }

                // Write value back to origin
                sourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) ((shiftResult >> 8) & 0xFF);
                sourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) (shiftResult & 0xFF);

                // Store result in memory for RCR mem operations
                // Note: if destination register is real register, it already
                // contains result (stored directly)
                if (((addressByte >> 6) & 0x03) != 3) {
                    cpu.setWordInMemorySegment(addressByte,
                            memoryReferenceLocation, sourceValue);
                }

                // Set appropriate flags
                // Set CF; this has already been calculated
                cpu.flags[CPU.REGISTER_FLAGS_CF] = carryBit == 1 ? true : false;
                // Set OF only if 1-bit shift occurred
                if (bitShift == 1) {
                    // OF (only defined for 1-bit rotates) is XOR of the two
                    // most-significant bits
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = (((sourceValue[CPU.REGISTER_GENERAL_HIGH] >> 7) & 0x01) ^ ((sourceValue[CPU.REGISTER_GENERAL_HIGH] >> 6) & 0x01)) == 0x01 ? true
                            : false;
                } else {
                    // Hardware seems to clear it
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = false;
                }
                break; // RCR

            case 4: // SHL/SAL; flags affected: CF, OF, SF, ZF, PF, AF (undefined)
                // If bitShift is 0, do nothing, not even flags
                if (bitShift > 0) {
                    // Check if 16 or 32-bit
                    if (cpu.doubleWord) {
                        // 32-bit registers
                        // Execute shift on reg or mem. Determine this from mm bits
                        // of addressbyte
                        if (((addressByte >> 6) & 0x03) == 3) {
                            // SHL on register
                            // Determine destination from addressbyte (source is the
                            // same)
                            sourceValue = cpu.decodeRegister(operandWordSize,
                                    addressByte & 0x07);
                            eSourceValue = cpu
                                    .decodeExtraRegister(addressByte & 0x07);
                        } else {
                            // SHL on memory
                            // Determine memory location
                            memoryReferenceLocation = cpu.decodeSSSMemDest(
                                    addressByte, memoryReferenceDisplacement);

                            sourceValue = cpu.getWordFromMemorySegment(addressByte,
                                    memoryReferenceLocation);

                            // Increase memory location by 2
                            int overFlowCheck = (((int) memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW]) & 0xFF) + 2;
                            if (overFlowCheck > 0xFF) {
                                memoryReferenceLocation[CPU.REGISTER_GENERAL_HIGH]++;
                            }
                            memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] += 2;

                            eSourceValue = cpu.getWordFromMemorySegment(
                                    addressByte, memoryReferenceLocation);
                        }

                        // Convert bytes to long and shift left
                        shiftResult = (((((long) eSourceValue[CPU.REGISTER_GENERAL_HIGH]) << 24) & 0xFF000000)
                                + ((((long) eSourceValue[CPU.REGISTER_GENERAL_LOW]) << 16) & 0xFF0000)
                                + ((((long) sourceValue[CPU.REGISTER_GENERAL_HIGH]) << 8) & 0xFF00) + ((((long) sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF))) << (bitShift - 1);
                        carryBit = (int) ((shiftResult & 0x80000000) >> 31) & 0x01;
                        shiftResult = shiftResult << 1;

                        // Set result in double word
                        eSourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) ((shiftResult >> 24) & 0xFF);
                        eSourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) ((shiftResult >> 16) & 0xFF);
                        sourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) ((shiftResult >> 8) & 0xFF);
                        sourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) (shiftResult & 0xFF);

                        // Store result in memory for SHR mem operations
                        // Note: if destination register is real register, it
                        // already contains result (stored directly)
                        if (((addressByte >> 6) & 0x03) != 3) {
                            // Do this in reverse order because memlocation was
                            // incremented
                            cpu.setWordInMemorySegment(addressByte,
                                    memoryReferenceLocation, eSourceValue);

                            // Decrement memlocation
                            memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] -= 2;
                            if (memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] == -1
                                    || memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] == -2) {
                                // Underflow
                                memoryReferenceLocation[CPU.REGISTER_GENERAL_HIGH]--;
                            }
                            cpu.setWordInMemorySegment(addressByte,
                                    memoryReferenceLocation, sourceValue);
                        }
                    } else {
                        // 16-bit registers
                        // Execute shift on reg or mem. Determine this from mm bits
                        // of addressbyte
                        if (((addressByte >> 6) & 0x03) == 3) {
                            // SHL on register
                            // Determine destination from addressbyte (source is the
                            // same)
                            sourceValue = cpu.decodeRegister(operandWordSize,
                                    addressByte & 0x07);
                        } else {
                            // SHL on memory
                            // Determine memory location
                            memoryReferenceLocation = cpu.decodeSSSMemDest(
                                    addressByte, memoryReferenceDisplacement);
                            sourceValue = cpu.getWordFromMemorySegment(addressByte,
                                    memoryReferenceLocation);
                        }

                        // Convert bytes to long and shift
                        shiftResult = (((((long) sourceValue[CPU.REGISTER_GENERAL_HIGH]) << 8) & 0xFF00) + ((((long) sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF))) << (bitShift - 1);
                        carryBit = (int) ((shiftResult & 0x8000) >> 15) & 0x01;
                        shiftResult = shiftResult << 1;

                        // Set result in word
                        sourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) ((shiftResult >> 8) & 0xFF);
                        sourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) (shiftResult & 0xFF);

                        // Store result in memory for SHR mem operations
                        // Note: if destination register is real register, it
                        // already contains result (stored directly)
                        if (((addressByte >> 6) & 0x03) != 3) {
                            // Do this in reverse order because memlocation was
                            // incremented
                            cpu.setWordInMemorySegment(addressByte,
                                    memoryReferenceLocation, sourceValue);
                        }
                    }

                    // Set appropriate flags
                    // Set AF (although is undefined by specs, hardware seems to
                    // clear it)
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
                    // Set CF; this is equal to the last bit shifted out of the high
                    // register
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = carryBit == 1 ? true : false;

                    if (!cpu.doubleWord) // 16 bit registers
                    {
                        // "Officialy" (Intel docs) OF is only set if 1-bit shift
                        // occurred; in hardware, it always sets the flag
                        // if (bitShift == 1)
                        // {
                        // Clear OF if the most significant bit of the result is the
                        // same as the CF flag
                        cpu.flags[CPU.REGISTER_FLAGS_OF] = (cpu.flags[CPU.REGISTER_FLAGS_CF] && ((sourceValue[CPU.REGISTER_GENERAL_HIGH]) >> 7) == 1)
                                || (!(cpu.flags[CPU.REGISTER_FLAGS_CF]) && ((sourceValue[CPU.REGISTER_GENERAL_HIGH]) >> 7) == 0) ? false
                                : true;
                        // }
                        // Set ZF
                        cpu.flags[CPU.REGISTER_FLAGS_ZF] = sourceValue[CPU.REGISTER_GENERAL_HIGH] == 0
                                && sourceValue[CPU.REGISTER_GENERAL_LOW] == 0 ? true
                                : false;
                        // Set SF on particular byte of sourceValue (set when MSB is
                        // 1, occurs when destReg >= 0x80)
                        cpu.flags[CPU.REGISTER_FLAGS_SF] = sourceValue[CPU.REGISTER_GENERAL_HIGH] < 0 ? true
                                : false;
                    } else // 32 bit registers
                    {
                        // Set OF only if 1-bit shift occurred
                        if (bitShift == 1) {
                            // Clear OF if the most significant bit of the result is
                            // the same as the CF flag
                            cpu.flags[CPU.REGISTER_FLAGS_OF] = (cpu.flags[CPU.REGISTER_FLAGS_CF] && ((eSourceValue[CPU.REGISTER_GENERAL_HIGH]) >> 7) == 1)
                                    || (!(cpu.flags[CPU.REGISTER_FLAGS_CF]) && ((eSourceValue[CPU.REGISTER_GENERAL_HIGH]) >> 7) == 0) ? false
                                    : true;
                        }
                        // Set ZF
                        cpu.flags[CPU.REGISTER_FLAGS_ZF] = sourceValue[CPU.REGISTER_GENERAL_HIGH] == 0
                                && sourceValue[CPU.REGISTER_GENERAL_LOW] == 0
                                && eSourceValue[CPU.REGISTER_GENERAL_HIGH] == 0
                                && eSourceValue[CPU.REGISTER_GENERAL_LOW] == 0 ? true
                                : false;
                        // Set SF on particular byte of sourceValue (set when MSB is
                        // 1, occurs when destReg >= 0x80)
                        cpu.flags[CPU.REGISTER_FLAGS_SF] = eSourceValue[CPU.REGISTER_GENERAL_HIGH] < 0 ? true
                                : false;
                    }
                    // Set PF on particular byte of sourceValue
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                            .checkParityOfByte(sourceValue[CPU.REGISTER_GENERAL_LOW]);
                }
                break;

            case 5: // SHR; unsigned. flags affected: CF, OF, SF, ZF, PF, AF
                // (undefined)
                // If bitShift is 0, do nothing, not even flags
                if (bitShift > 0) {
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = false; // According to Bochs,
                    // is always reset
                    // except when shift =
                    // 1 (handled below)

                    // Check if 16 or 32-bit
                    if (cpu.doubleWord) {
                        // 32-bit registers
                        // Execute shift on reg or mem. Determine this from mm bits
                        // of addressbyte
                        if (((addressByte >> 6) & 0x03) == 3) {
                            // SHR on register
                            // Determine destination from addressbyte (source is the
                            // same)
                            sourceValue = cpu.decodeRegister(operandWordSize,
                                    addressByte & 0x07);
                            eSourceValue = cpu
                                    .decodeExtraRegister(addressByte & 0x07);
                        } else {
                            // SHR on memory
                            // Determine memory location
                            memoryReferenceLocation = cpu.decodeSSSMemDest(
                                    addressByte, memoryReferenceDisplacement);

                            sourceValue = cpu.getWordFromMemorySegment(addressByte,
                                    memoryReferenceLocation);

                            // Increase memory location by 2
                            int overFlowCheck = (((int) memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW]) & 0xFF) + 2;
                            if (overFlowCheck > 0xFF) {
                                memoryReferenceLocation[CPU.REGISTER_GENERAL_HIGH]++;
                            }
                            memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] += 2;

                            eSourceValue = cpu.getWordFromMemorySegment(
                                    addressByte, memoryReferenceLocation);
                        }

                        // Set OF only if 1-bit shift occurred
                        if (bitShift == 1) {
                            // Set OF to MSB of original operand
                            cpu.flags[CPU.REGISTER_FLAGS_OF] = (eSourceValue[CPU.REGISTER_GENERAL_HIGH] & 0x80) == 0x80 ? true
                                    : false;
                        }

                        // Convert bytes to long and shift right
                        shiftResult = (((((long) eSourceValue[CPU.REGISTER_GENERAL_HIGH]) << 24) & 0xFF000000)
                                + ((((long) eSourceValue[CPU.REGISTER_GENERAL_LOW]) << 16) & 0xFF0000)
                                + ((((long) sourceValue[CPU.REGISTER_GENERAL_HIGH]) << 8) & 0xFF00) + ((((long) sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF))) >> (bitShift - 1);
                        carryBit = (int) (shiftResult & 0x01);
                        shiftResult = shiftResult >> 1;

                        // Set result in double word
                        eSourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) ((shiftResult >> 24) & 0xFF);
                        eSourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) ((shiftResult >> 16) & 0xFF);
                        sourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) ((shiftResult >> 8) & 0xFF);
                        sourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) (shiftResult & 0xFF);

                        // Store result in memory for SHR mem operations
                        // Note: if destination register is real register, it
                        // already contains result (stored directly)
                        if (((addressByte >> 6) & 0x03) != 3) {
                            // Do this in reverse order because memlocation was
                            // incremented
                            cpu.setWordInMemorySegment(addressByte,
                                    memoryReferenceLocation, eSourceValue);

                            // Decrement memlocation
                            memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] -= 2;
                            if (memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] == -1
                                    || memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] == -2) {
                                // Underflow
                                memoryReferenceLocation[CPU.REGISTER_GENERAL_HIGH]--;
                            }
                            cpu.setWordInMemorySegment(addressByte,
                                    memoryReferenceLocation, sourceValue);
                        }
                    } else {
                        // 16-bit registers
                        // Execute shift on reg or mem. Determine this from mm bits
                        // of addressbyte
                        if (((addressByte >> 6) & 0x03) == 3) {
                            // SHR on register
                            // Determine destination from addressbyte (source is the
                            // same)
                            sourceValue = cpu.decodeRegister(operandWordSize,
                                    addressByte & 0x07);
                        } else {
                            // SHR on memory
                            // Determine memory location
                            memoryReferenceLocation = cpu.decodeSSSMemDest(
                                    addressByte, memoryReferenceDisplacement);
                            sourceValue = cpu.getWordFromMemorySegment(addressByte,
                                    memoryReferenceLocation);
                        }

                        // Set OF only if 1-bit shift occurred
                        if (bitShift == 1) {
                            // Set OF to MSB of original operand
                            cpu.flags[CPU.REGISTER_FLAGS_OF] = (sourceValue[CPU.REGISTER_GENERAL_HIGH] & 0x80) == 0x80 ? true
                                    : false;
                        }

                        // Convert bytes to long and shift
                        shiftResult = (((((long) sourceValue[CPU.REGISTER_GENERAL_HIGH]) << 8) & 0xFF00) + ((((long) sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF))) >> (bitShift - 1);
                        carryBit = (int) (shiftResult & 0x01);
                        shiftResult = shiftResult >> 1;

                        // Set result in word
                        sourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) ((shiftResult >> 8) & 0xFF);
                        sourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) (shiftResult & 0xFF);

                        // Store result in memory for SHR mem operations
                        // Note: if destination register is real register, it
                        // already contains result (stored directly)
                        if (((addressByte >> 6) & 0x03) != 3) {
                            // Do this in reverse order because memlocation was
                            // incremented
                            cpu.setWordInMemorySegment(addressByte,
                                    memoryReferenceLocation, sourceValue);
                        }
                    }

                    // Set appropriate flags
                    // Set AF (although is undefined by specs, hardware seems to
                    // clear it)
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
                    // Set CF; this is equal to the last bit shifted out of the high
                    // register
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = carryBit == 1 ? true : false;

                    if (!cpu.doubleWord) // 16 bit registers
                    {
                        // Set ZF
                        cpu.flags[CPU.REGISTER_FLAGS_ZF] = sourceValue[CPU.REGISTER_GENERAL_HIGH] == 0
                                && sourceValue[CPU.REGISTER_GENERAL_LOW] == 0 ? true
                                : false;
                        // Set SF on particular byte of sourceValue (set when MSB is
                        // 1, occurs when destReg >= 0x80)
                        cpu.flags[CPU.REGISTER_FLAGS_SF] = sourceValue[CPU.REGISTER_GENERAL_HIGH] < 0 ? true
                                : false;
                    } else // 32 bit registers
                    {
                        // Set ZF
                        cpu.flags[CPU.REGISTER_FLAGS_ZF] = sourceValue[CPU.REGISTER_GENERAL_HIGH] == 0
                                && sourceValue[CPU.REGISTER_GENERAL_LOW] == 0
                                && eSourceValue[CPU.REGISTER_GENERAL_HIGH] == 0
                                && eSourceValue[CPU.REGISTER_GENERAL_LOW] == 0 ? true
                                : false;
                        // Set SF on particular byte of sourceValue (set when MSB is
                        // 1, occurs when destReg >= 0x80)
                        cpu.flags[CPU.REGISTER_FLAGS_SF] = eSourceValue[CPU.REGISTER_GENERAL_HIGH] < 0 ? true
                                : false;
                    }
                    // Set PF on particular byte of sourceValue
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                            .checkParityOfByte(sourceValue[CPU.REGISTER_GENERAL_LOW]);
                }
                break;

            case 6: // does not exist
                // Throw exception for illegal nnn bits
                throw new CPUInstructionException(
                        "Shift Group 2 (0xD2/6) illegal reg bits");

            case 7: // SAR; flags affected: CF, OF, SF, ZF, PF, AF (undefined)
                // If bitShift is 0, do nothing, not even flags
                if (bitShift > 0) {
                    // Execute shift on reg or mem. Determine this from mm bits of
                    // addressbyte
                    if (((addressByte >> 6) & 0x03) == 3) {
                        // SAR on register
                        // Determine destination from addressbyte (source is the
                        // same)
                        sourceValue = cpu.decodeRegister(operandWordSize,
                                addressByte & 0x07);
                    } else {
                        // SAR on memory
                        // Determine memory location
                        memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                                memoryReferenceDisplacement);
                        sourceValue = cpu.getWordFromMemorySegment(addressByte,
                                memoryReferenceLocation);
                    }

                    // If bitShift is >= 16, the result will be the MSB in all bit
                    // positions
                    if (bitShift >= 16) {
                        if ((sourceValue[CPU.REGISTER_GENERAL_HIGH] & 0x80) == 0x80) {
                            // All 1s
                            sourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) 0xff;
                            sourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) 0xff;
                        } else {
                            // All 0s
                            sourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) 0x00;
                            sourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) 0x00;
                        }
                    }
                    // bitShift < 16, so perform SHR and depending on MSB OR with
                    // (0xFFFF << 16 - bitShift)
                    else {
                        // Convert bytes to long and shift
                        shiftResult = (((((long) sourceValue[CPU.REGISTER_GENERAL_HIGH]) << 8) & 0xFF00) + ((((long) sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF))) >> (bitShift - 1);
                        carryBit = (int) (shiftResult & 0x01);
                        shiftResult = shiftResult >> 1;

                        if ((sourceValue[CPU.REGISTER_GENERAL_HIGH] & 0x80) == 0x80) {
                            // Set all shifted MSB to 1
                            shiftResult |= (0xFFFF << (16 - bitShift));
                        }
                    }

                    // Set result in word
                    sourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) ((shiftResult >> 8) & 0xFF);
                    sourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) (shiftResult & 0xFF);

                    // Store result in memory for SAR mem operations
                    // Note: if destination register is real register, it already
                    // contains result (stored directly)
                    if (((addressByte >> 6) & 0x03) != 3) {
                        cpu.setWordInMemorySegment(addressByte,
                                memoryReferenceLocation, sourceValue);
                    }

                    // Set appropriate flags
                    // Set AF (although is undefined by specs, hardware seems to
                    // clear it)
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
                    // Set CF; this is equal to the last bit shifted out of the high
                    // register
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = carryBit == 1 ? true : false;

                    // Set OF only if 1-bit shift occurred
                    if (bitShift == 1) {
                        // Clear OF
                        cpu.flags[CPU.REGISTER_FLAGS_OF] = false;
                    }
                    // Set ZF
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = sourceValue[CPU.REGISTER_GENERAL_HIGH] == 0
                            && sourceValue[CPU.REGISTER_GENERAL_LOW] == 0 ? true
                            : false;
                    // Set SF on particular byte of sourceValue (set when MSB is 1,
                    // occurs when destReg >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = sourceValue[CPU.REGISTER_GENERAL_HIGH] < 0 ? true
                            : false;
                    // Set PF on particular byte of sourceValue
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                            .checkParityOfByte(sourceValue[CPU.REGISTER_GENERAL_LOW]);
                }
                break; // SAR

            default:
                // Throw exception for illegal nnn bits
                throw new CPUInstructionException(
                        "Shift Group 2 (0xD2/6) no case match");
        }
    }
}
