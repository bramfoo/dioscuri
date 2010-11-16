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
 * Intel opcode C0<BR>
 * Immediate Group 2 opcode extension: ROL, ROR, RCL, RCR, SHL/SAL, SHR, SAR.<BR>
 * Performs the selected instruction (indicated by bits 5, 4, 3 of the ModR/M
 * byte) using immediate byte.<BR>
 * Flags modified: depending on instruction can be any of: OF, SF, ZF, AF, PF,
 * CF
 */
public class Instruction_ShiftGRP2_EbIb implements Instruction {

    // Attributes
    private CPU cpu;

    boolean operandWordSize;

    byte addressByte;
    byte[] memoryReferenceLocation;
    byte[] memoryReferenceDisplacement;
    byte registerHighLow;

    byte[] sourceValue;
    int bitShift;
    int carryBit;
    int newCarryBit;
    long shiftResult;

    // Constructors

    /**
     * Class constructor
     */
    public Instruction_ShiftGRP2_EbIb() {
        operandWordSize = false;

        addressByte = 0;
        memoryReferenceLocation = new byte[2];
        memoryReferenceDisplacement = new byte[2];
        registerHighLow = 0;

        sourceValue = new byte[2];
        bitShift = 0;
        carryBit = 0;
        newCarryBit = 0;
        shiftResult = 0;
    }

    /**
     * Class constructor specifying processor reference
     *
     * @param processor Reference to CPU class
     */
    public Instruction_ShiftGRP2_EbIb(CPU processor) {
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
        // Reset sourceValue (to lose pointer to earlier words)
        sourceValue = new byte[2];

        // Get addresByte
        addressByte = cpu.getByteFromCode();

        // Determine displacement of memory location (if any)
        memoryReferenceDisplacement = cpu.decodeMM(addressByte);

        // Get immediate byte for number of rotates and limit it on 5 bits (only
        // 0 - 31 allowed)
        bitShift = (byte) (cpu.getByteFromCode() & 0x1F);

        // Execute instruction decoded from nnn (bits 5, 4, 3 in ModR/M byte)
        switch ((addressByte & 0x38) >> 3) {
            case 0: // ROL - Rotate bits CL positions left. Flags affected: CF, OF
                // Execute rotate on reg or mem. Determine this from mm bits of
                // addressbyte
                if (((addressByte >> 6) & 0x03) == 3) {
                    // ROL on register
                    // Determine destination from addressbyte (source is the same)
                    sourceValue = cpu.decodeRegister(operandWordSize,
                            addressByte & 0x07);

                    // Determine high/low part of register based on bit 3 (leading
                    // sss bit)
                    registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW
                            : (byte) CPU.REGISTER_GENERAL_HIGH;
                } else {
                    // ROL on memory
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Determine high/low part of register
                    registerHighLow = CPU.REGISTER_GENERAL_LOW;

                    // Get byte from memory
                    sourceValue[registerHighLow] = cpu.getByteFromMemorySegment(
                            addressByte, memoryReferenceLocation);
                }

                // Determine result of carry; this is the MSB
                carryBit = (sourceValue[registerHighLow] & 0x80) == 0x80 ? 1 : 0;

                // Rotate left
                for (int s = 0; s < bitShift; s++) {
                    // Determine result of carry; this is the MSB
                    carryBit = (sourceValue[registerHighLow] & 0x80) == 0x80 ? 1
                            : 0;

                    // Rotate left by 1. This is equivalent to a SHL of 1 ORed with
                    // the previously calculated carryBit
                    sourceValue[registerHighLow] = (byte) ((sourceValue[registerHighLow] << 1) | carryBit);
                }

                // Store result in memory for memory operations
                // Note: if destination register is real register, it already
                // contains result (stored directly)
                if (((addressByte >> 6) & 0x03) != 3) {
                    // Store result back in memory
                    cpu.setByteInMemorySegment(addressByte,
                            memoryReferenceLocation, sourceValue[registerHighLow]);
                }

                // Set appropriate flags
                // Set CF; this has already been calculated
                cpu.flags[CPU.REGISTER_FLAGS_CF] = carryBit == 1 ? true : false;
                // Set OF (only defined for 1-bit rotates); XOR of CF (after rotate)
                // and MSB of result.
                if (bitShift == 1) {
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = (carryBit ^ ((sourceValue[registerHighLow] & 0x80) == 0x80 ? 1
                            : 0)) == 1 ? true : false;
                }
                break; // ROL

            case 1: // ROR - Rotate bits CL positions right; Flags affected: CF, OF
                // Execute rotate on reg or mem. Determine this from mm bits of
                // addressbyte
                if (((addressByte >> 6) & 0x03) == 3) {
                    // ROR reg,imm
                    // Determine destination from addressbyte (source is the same)
                    sourceValue = cpu.decodeRegister(operandWordSize,
                            addressByte & 0x07);

                    // Determine high/low part of register based on bit 3 (leading
                    // sss bit)
                    registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW
                            : (byte) CPU.REGISTER_GENERAL_HIGH;
                } else {
                    // ROR mem,imm
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Determine high/low part of register
                    registerHighLow = 0;

                    // Get byte from memory
                    sourceValue[registerHighLow] = cpu.getByteFromMemorySegment(
                            addressByte, memoryReferenceLocation);
                }

                // Rotate right
                for (int s = 0; s < bitShift; s++) {
                    // Rotate right by 1. This is equivalent to a SHR of 1 ORed with
                    // a SHL of 7
                    sourceValue[registerHighLow] = (byte) ((((sourceValue[registerHighLow]) >> 1) & 0x7F) | ((sourceValue[registerHighLow]) << 7));
                }

                // Store result in memory, if necessary
                // Note: if destination register is real register, it already
                // contains result (stored directly)
                if (((addressByte >> 6) & 0x03) != 3) {
                    // Store result back in memory
                    cpu.setByteInMemorySegment(addressByte,
                            memoryReferenceLocation, sourceValue[registerHighLow]);
                }

                // Set appropriate flags
                // Set CF; this is equal to the high bit of the register
                cpu.flags[CPU.REGISTER_FLAGS_CF] = (sourceValue[registerHighLow] & 0x80) == 0x80 ? true
                        : false;
                // OF (only defined for 1-bit rotates) is the XOR of the two
                // most-significant bits of the result.
                if (bitShift == 1) {
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = ((cpu.flags[CPU.REGISTER_FLAGS_CF] ^ ((sourceValue[registerHighLow]) & 0x40) == 0x40)) ? true
                            : false;
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

                    // Determine high/low part of register based on bit 3 (leading
                    // sss bit)
                    registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW
                            : (byte) CPU.REGISTER_GENERAL_HIGH;
                } else {
                    // RCL on memory
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Determine high/low part of register
                    registerHighLow = 0;

                    // Get byte from memory
                    sourceValue[registerHighLow] = cpu.getByteFromMemorySegment(
                            addressByte, memoryReferenceLocation);
                }

                // Determine result of carry; this is the MSB
                carryBit = cpu.flags[CPU.REGISTER_FLAGS_CF] ? 1 : 0;

                // Rotate left
                for (int s = 0; s < bitShift; s++) {
                    // Determine result of carry; this is the MSB
                    newCarryBit = (sourceValue[registerHighLow] & 0x80) == 0x80 ? 1
                            : 0;

                    // Rotate left by 1. This is equivalent to a SHL of 1 ORed with
                    // the current carryBit (CF)
                    sourceValue[registerHighLow] = (byte) ((sourceValue[registerHighLow] << 1) | carryBit);

                    // Update carryBit
                    carryBit = newCarryBit;
                }

                // Store result in memory for memory operations
                // Note: if destination register is real register, it already
                // contains result (stored directly)
                if (((addressByte >> 6) & 0x03) != 3) {
                    // Store result back in memory
                    cpu.setByteInMemorySegment(addressByte,
                            memoryReferenceLocation, sourceValue[registerHighLow]);
                }

                // Set appropriate flags
                // Set CF; this has already been calculated
                cpu.flags[CPU.REGISTER_FLAGS_CF] = carryBit == 1 ? true : false;
                // Set OF (only defined for 1-bit rotates); XOR of CF (after rotate)
                // and MSB of result.
                if (bitShift == 1) {
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = (carryBit ^ ((sourceValue[registerHighLow] & 0x80) == 0x80 ? 1
                            : 0)) == 1 ? true : false;
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

                    // Determine high/low part of register based on bit 3 (leading
                    // sss bit)
                    registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW
                            : (byte) CPU.REGISTER_GENERAL_HIGH;
                } else {
                    // RCR on memory
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Determine high/low part of register
                    registerHighLow = 0;

                    // Get byte from memory
                    sourceValue[registerHighLow] = cpu.getByteFromMemorySegment(
                            addressByte, memoryReferenceLocation);
                }

                // Determine result of carry; this is the MSB
                carryBit = cpu.flags[CPU.REGISTER_FLAGS_CF] ? 1 : 0;

                // Rotate right
                for (int s = 0; s < bitShift; s++) {
                    // Determine result of carry; this is the MSB
                    newCarryBit = (sourceValue[registerHighLow] & 0x01) == 0x01 ? 1
                            : 0;

                    // Rotate right by 1. This is equivalent to a SHR of 1 ORed with
                    // CF
                    sourceValue[registerHighLow] = (byte) ((((sourceValue[registerHighLow]) >> 1) & 0x7F) | ((carryBit) << 7));

                    // Update carryBit
                    carryBit = newCarryBit;
                }

                // Store result in memory for memory operations
                // Note: if destination register is real register, it already
                // contains result (stored directly)
                if (((addressByte >> 6) & 0x03) != 3) {
                    // Store result back in memory
                    cpu.setByteInMemorySegment(addressByte,
                            memoryReferenceLocation, sourceValue[registerHighLow]);
                }

                // Set appropriate flags
                // Set CF; this has already been calculated
                cpu.flags[CPU.REGISTER_FLAGS_CF] = carryBit == 1 ? true : false;
                // Set OF (only defined for 1-bit rotates); XOR of CF (after rotate)
                // and MSB of result.
                if (bitShift == 1) {
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = (carryBit ^ ((sourceValue[registerHighLow] & 0x80) == 0x80 ? 1
                            : 0)) == 1 ? true : false;
                }
                break; // RCR

            case 4: // SHL/SAL; flags affected: CF, OF, SF, ZF, PF, AF (undefined)
                // If bitShift is 0, do nothing, not even flags.
                if (bitShift > 0) {
                    // Execute shift on reg or mem. Determine this from mm bits of
                    // addressbyte
                    if (((addressByte >> 6) & 0x03) == 3) {
                        // SHL on register
                        // Determine destination from addressbyte (source is the
                        // same)
                        sourceValue = cpu.decodeRegister(operandWordSize,
                                addressByte & 0x07);

                        // Determine high/low part of register based on bit 3
                        // (leading sss bit)
                        registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW
                                : (byte) CPU.REGISTER_GENERAL_HIGH;
                    } else {
                        // SHL on memory
                        // Determine memory location
                        memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                                memoryReferenceDisplacement);

                        // Determine high/low part of register
                        registerHighLow = 0;

                        // Get byte from memory
                        sourceValue[registerHighLow] = cpu
                                .getByteFromMemorySegment(addressByte,
                                        memoryReferenceLocation);
                    }

                    // Shift left
                    sourceValue[registerHighLow] = (byte) ((sourceValue[registerHighLow]) << bitShift);
                    carryBit = (((sourceValue[registerHighLow]) << (bitShift - 1)) & 0x80) == 0x80 ? 1
                            : 0;

                    // Store result in memory for SHL mem operations
                    // Note: if destination register is real register, it already
                    // contains result (stored directly)
                    if (((addressByte >> 6) & 0x03) != 3) {
                        // Store result back in memory
                        cpu.setByteInMemorySegment(addressByte,
                                memoryReferenceLocation,
                                sourceValue[registerHighLow]);
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
                        // Clear OF if the most significant bit of the result is the
                        // same as the CF flag
                        cpu.flags[CPU.REGISTER_FLAGS_OF] = (cpu.flags[CPU.REGISTER_FLAGS_CF] && ((sourceValue[registerHighLow]) >> 7) == 1)
                                || (!(cpu.flags[CPU.REGISTER_FLAGS_CF]) && ((sourceValue[registerHighLow]) >> 7) == 0) ? false
                                : true;
                    } else {
                        // Hardware seems to clear it
                        cpu.flags[CPU.REGISTER_FLAGS_OF] = false;
                    }
                    // Set ZF on particular byte of sourceValue
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = sourceValue[registerHighLow] == 0 ? true
                            : false;
                    // Set SF on particular byte of sourceValue (set when MSB is 1,
                    // occurs when destReg >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = sourceValue[registerHighLow] < 0 ? true
                            : false;
                    // Set PF on particular byte of sourceValue
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                            .checkParityOfByte(sourceValue[registerHighLow]);
                }
                break; // SHL/SAL

            case 5: // SHR; unsigned. flags affected: CF, OF, SF, ZF, PF, AF
                // (undefined)
                // If bitShift is 0, do nothing, not even flags.
                if (bitShift > 0) {
                    // Execute shift on reg or mem. Determine this from mm bits of
                    // addressbyte
                    if (((addressByte >> 6) & 0x03) == 3) {
                        // SHR on register
                        // Determine destination from addressbyte (source is the
                        // same)
                        sourceValue = cpu.decodeRegister(operandWordSize,
                                addressByte & 0x07);

                        // Determine high/low part of register based on bit 3
                        // (leading sss bit)
                        registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW
                                : (byte) CPU.REGISTER_GENERAL_HIGH;
                    } else {
                        // SHR on memory
                        // Determine memory location
                        memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                                memoryReferenceDisplacement);

                        // Determine high/low part of register
                        registerHighLow = 0;

                        // Get byte from memory
                        sourceValue[registerHighLow] = cpu
                                .getByteFromMemorySegment(addressByte,
                                        memoryReferenceLocation);
                    }

                    // Set OF (only if bitShift is 1)
                    if (bitShift == 1) {
                        // Set OF to MSB of original operand
                        cpu.flags[CPU.REGISTER_FLAGS_OF] = (sourceValue[registerHighLow] & 0x80) == 0x80 ? true
                                : false;
                    } else {
                        cpu.flags[CPU.REGISTER_FLAGS_OF] = false;
                    }

                    // Perform SHR of (CL -1) - on unsigned value!
                    shiftResult = ((int) sourceValue[registerHighLow]) & 0xFF;
                    shiftResult >>= (bitShift - 1);

                    // Set CF; this will become what is now the LSB
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = (shiftResult & 0x01) == 1 ? true
                            : false;

                    // Perform final shift
                    shiftResult >>= 1;

                    sourceValue[registerHighLow] = (byte) shiftResult;

                    // Store result in memory for SHR mem operations
                    // Note: if destination register is real register, it already
                    // contains result (stored directly)
                    if (((addressByte >> 6) & 0x03) != 3) {
                        cpu.setByteInMemorySegment(addressByte,
                                memoryReferenceLocation,
                                sourceValue[registerHighLow]);
                    }

                    // Set appropriate flags
                    // Set AF (although is undefined by specs, hardware seems to
                    // clear it)
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
                    // Set ZF
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = sourceValue[registerHighLow] == 0 ? true
                            : false;
                    // Set SF on particular byte of sourceValue (set when MSB is 1,
                    // occurs when destReg >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = sourceValue[registerHighLow] < 0 ? true
                            : false;
                    // Set PF on particular byte of sourceValue
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                            .checkParityOfByte(sourceValue[registerHighLow]);
                }
                break; // SHR

            case 6: // does not exist
                // Throw exception for illegal nnn bits
                throw new CPUInstructionException(
                        "Shift Group 2 (0xD2/6) illegal reg bits");

            case 7: // SAR - Shift CL positions right signed. Flags affected: CF,
                // OF, SF, ZF, PF
                // If bitShift is 0, do nothing, not even flags.
                if (bitShift > 0) {
                    // Execute shift on reg or mem. Determine this from mm bits of
                    // addressbyte
                    if (((addressByte >> 6) & 0x03) == 3) {
                        // SHR on register
                        // Determine destination from addressbyte (source is the
                        // same)
                        sourceValue = cpu.decodeRegister(operandWordSize,
                                addressByte & 0x07);

                        // Determine high/low part of register based on bit 3
                        // (leading sss bit)
                        registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW
                                : (byte) CPU.REGISTER_GENERAL_HIGH;
                    } else {
                        // SHR on memory
                        // Determine memory location
                        memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                                memoryReferenceDisplacement);

                        // Determine high/low part of register
                        registerHighLow = 0;

                        // Get byte from memory
                        sourceValue[registerHighLow] = cpu
                                .getByteFromMemorySegment(addressByte,
                                        memoryReferenceLocation);
                    }

                    // Set CF; this will become what is now the LSB
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = (sourceValue[registerHighLow] & 0x01) == 1 ? true
                            : false;
                    // Set OF (only if bitShift is 1)
                    if (bitShift == 1) {
                        // Set OF to MSB of original operand
                        cpu.flags[CPU.REGISTER_FLAGS_OF] = (sourceValue[registerHighLow] & 0x80) == 0x80 ? true
                                : false;
                    } else {
                        cpu.flags[CPU.REGISTER_FLAGS_OF] = false;
                    }

                    // SHR by 1 - on signed value! (This is natural to the Java
                    // behaviour)
                    sourceValue[registerHighLow] = (byte) (sourceValue[registerHighLow] >> bitShift);

                    // Store result in memory for SHR mem operations
                    // Note: if destination register is real register, it already
                    // contains result (stored directly)
                    if (((addressByte >> 6) & 0x03) != 3) {
                        cpu.setByteInMemorySegment(addressByte,
                                memoryReferenceLocation,
                                sourceValue[registerHighLow]);
                    }

                    // Set appropriate flags
                    // Set AF (although is undefined by specs, hardware seems to
                    // clear it)
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
                    // Set ZF
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = sourceValue[registerHighLow] == 0 ? true
                            : false;
                    // Set SF on particular byte of destinationRegister (set when
                    // MSB is 1, occurs when destReg >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = sourceValue[registerHighLow] < 0 ? true
                            : false;
                    // Set PF on particular byte of destinationRegister
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                            .checkParityOfByte(sourceValue[registerHighLow]);
                }
                break; // SAR

            default:
                // Throw exception for illegal nnn bits
                throw new CPUInstructionException(
                        "Shift Group 2 (0xD2/6) no case match");
        }
    }
}
