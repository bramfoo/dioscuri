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
 * Intel opcode 80<BR>
 * Immediate Group 1 opcode extension: ADD, OR, ADC, SBB, AND, SUB, XOR, CMP.<BR>
 * Performs the selected instruction (indicated by bits 5, 4, 3 of the ModR/M
 * byte) using immediate data.<BR>
 * Flags modified: depending on instruction can be any of: OF, SF, ZF, AF, PF,
 * CF
 */
public class Instruction_ImmGRP1_EbIb implements Instruction {

    // Attributes
    private CPU cpu;

    boolean operandWordSize;

    byte addressByte;
    byte[] memoryReferenceLocation;
    byte[] memoryReferenceDisplacement;

    byte sourceByte;
    byte destByte;
    byte oldDest;
    byte[] destinationRegister;
    byte registerHighLow;

    int iCarryFlag;
    byte tempResult;
    byte tempResultByte;

    // Constructors

    /**
     * Class constructor
     */
    public Instruction_ImmGRP1_EbIb()
    {
        operandWordSize = false;

        addressByte = 0;
        memoryReferenceLocation = new byte[2];
        memoryReferenceDisplacement = new byte[2];

        sourceByte = 0;
        destByte = 0;
        oldDest = 0;
        destinationRegister = new byte[2];

        registerHighLow = 0;

        iCarryFlag = 0;
        tempResult = 0;
        tempResultByte = 0;
    }

    /**
     * Class constructor specifying processor reference
     *
     * @param processor Reference to CPU class
     */
    public Instruction_ImmGRP1_EbIb(CPU processor)
    {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Execute any of the following Immediate Group 1 instructions: ADD, OR,
     * ADC, SBB, AND, SUB, XOR, CMP.<BR>
     *
     * @throws CPUInstructionException
     */
    public void execute() throws CPUInstructionException
    {
        // Get addresByte
        addressByte = cpu.getByteFromCode();

        // Determine displacement of memory location (if any)
        memoryReferenceDisplacement = cpu.decodeMM(addressByte);

        // Get immediate byte
        sourceByte = cpu.getByteFromCode();

        // Execute instruction decoded from nnn (bits 5, 4, 3 in ModR/M byte)
        switch ((addressByte & 0x38) >> 3) {
            case 0: // ADD

                // Execute ADD on reg,reg or mem,reg. Determine this from mm bits of
                // addressbyte
                if (((addressByte >> 6) & 0x03) == 3) {
                    // ADD reg,reg

                    // Determine high/low part of register based on bit 3 (leading
                    // sss bit)
                    registerHighLow = ((addressByte & 0x4) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW
                            : (byte) CPU.REGISTER_GENERAL_HIGH;

                    // Determine destination register from addressbyte, ANDing it
                    // with 0000 0111
                    destinationRegister = cpu.decodeRegister(operandWordSize,
                            addressByte & 0x07);

                    // Store initial value for use in OF check
                    oldDest = destinationRegister[registerHighLow];

                    // ADD source and destination, storing result in destination.
                    destinationRegister[registerHighLow] += sourceByte;

                    // Test AF
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_ADD(oldDest,
                            destinationRegister[registerHighLow]);
                    // Test CF
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_ADD(oldDest,
                            sourceByte, 0);
                    // Test OF
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_ADD(oldDest,
                            sourceByte, destinationRegister[registerHighLow], 0);
                    // Test ZF on particular byte of destinationRegister
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = destinationRegister[registerHighLow] == 0 ? true
                            : false;
                    // Test SF on particular byte of destinationRegister (set when
                    // MSB is 1, occurs when destReg >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = destinationRegister[registerHighLow] < 0 ? true
                            : false;
                    // Set PF on particular byte of destinationRegister
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                            .checkParityOfByte(destinationRegister[registerHighLow]);
                } else {
                    // ADD mem,reg
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Get byte from memory and ADD source register
                    oldDest = cpu.getByteFromMemorySegment(addressByte,
                            memoryReferenceLocation);

                    // Add source to destination
                    tempResult = (byte) (sourceByte + oldDest);

                    // Store result in memory
                    cpu.setByteInMemorySegment(addressByte,
                            memoryReferenceLocation, tempResult);

                    // Test AF
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_ADD(oldDest,
                            tempResult);
                    // Test CF
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_ADD(oldDest,
                            sourceByte, 0);
                    // Test OF
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_ADD(oldDest,
                            sourceByte, tempResult, 0);
                    // Test ZF on result
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = tempResult == 0 ? true
                            : false;
                    // Test SF on result (set when MSB is 1, occurs when result >=
                    // 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = tempResult < 0 ? true
                            : false;
                    // Set PF on result
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                            .checkParityOfByte(tempResult);
                }
                break; // ADD

            case 1: // OR
                // Clear appropriate flags
                cpu.flags[CPU.REGISTER_FLAGS_OF] = false;
                cpu.flags[CPU.REGISTER_FLAGS_CF] = false;
                cpu.flags[CPU.REGISTER_FLAGS_AF] = false;

                // Execute OR on reg,reg or mem,reg. Determine this from mm bits of
                // addressbyte
                if (((addressByte >> 6) & 0x03) == 3) {
                    // OR reg,reg

                    // Determine high/low part of register based on bit 3 (leading
                    // sss bit)
                    registerHighLow = ((addressByte & 0x4) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW
                            : (byte) CPU.REGISTER_GENERAL_HIGH;

                    // Determine destination register from addressbyte, ANDing it
                    // with 0000 0111
                    destinationRegister = cpu.decodeRegister(operandWordSize,
                            addressByte & 0x07);

                    // OR source and destination, storing result in destination.
                    // registerHighLow is re-used here.
                    destinationRegister[registerHighLow] |= sourceByte;

                    // Test ZF on particular byte of destinationRegister
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = destinationRegister[registerHighLow] == 0 ? true
                            : false;
                    // Test SF on particular byte of destinationRegister (set when
                    // MSB is 1, occurs when destReg >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = destinationRegister[registerHighLow] < 0 ? true
                            : false;
                    // Set PF on particular byte of destinationRegister
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                            .checkParityOfByte(destinationRegister[registerHighLow]);
                } else {
                    // OR mem,reg
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Get byte from memory and OR with source register
                    destByte = cpu.getByteFromMemorySegment(addressByte,
                            memoryReferenceLocation);
                    destByte |= sourceByte;
                    // Store result in memory
                    cpu.setByteInMemorySegment(addressByte,
                            memoryReferenceLocation, destByte);

                    // Test ZF on result
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = destByte == 0 ? true : false;
                    // Test SF on result (set when MSB is 1, occurs when result >=
                    // 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = destByte < 0 ? true : false;
                    // Set PF on result
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                            .checkParityOfByte(destByte);

                }
                break; // OR

            case 2: // ADC
                // Determine value of carry flag before reset
                iCarryFlag = (byte) (cpu.flags[CPU.REGISTER_FLAGS_CF] ? 1 : 0);

                // Execute ADC on reg,imm or mem,imm. Determine this from mm bits of
                // addressbyte
                if (((addressByte >> 6) & 0x03) == 3) {
                    // ADC reg,imm
                    // Determine destination register from addressbyte, ANDing it
                    // with 0000 0111
                    // Determine high/low part of register based on bit 3 (leading
                    // sss bit)
                    registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW
                            : (byte) CPU.REGISTER_GENERAL_HIGH;
                    destinationRegister = cpu.decodeRegister(operandWordSize,
                            addressByte & 0x07);

                    // Store initial value
                    oldDest = destinationRegister[registerHighLow];

                    // ADC (source + CF) and destination, storing result in
                    // destination.
                    destinationRegister[registerHighLow] += sourceByte + iCarryFlag;

                    // Test AF
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_ADD(oldDest,
                            destinationRegister[registerHighLow]);
                    // Test CF
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_ADD(oldDest,
                            sourceByte, iCarryFlag);
                    // Test OF
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_ADD(oldDest,
                            sourceByte, destinationRegister[registerHighLow],
                            iCarryFlag);
                    // Test ZF on particular byte of destinationRegister
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = destinationRegister[registerHighLow] == 0 ? true
                            : false;
                    // Test SF on particular byte of destinationRegister (set when
                    // MSB is 1, occurs when destReg >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = destinationRegister[registerHighLow] < 0 ? true
                            : false;
                    // Test PF on particular byte of destinationRegister
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                            .checkParityOfByte(destinationRegister[registerHighLow]);
                } else {
                    // ADC mem,imm
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);
                    destByte = cpu.getByteFromMemorySegment(addressByte,
                            memoryReferenceLocation);

                    // Get byte from memory and ADC source + imm + CF
                    tempResultByte = (byte) (destByte + (sourceByte + iCarryFlag));

                    // Store result in memory
                    cpu.setByteInMemorySegment(addressByte,
                            memoryReferenceLocation, tempResultByte);

                    // Test AF
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_ADD(destByte,
                            tempResultByte);
                    // Test CF
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_ADD(destByte,
                            sourceByte, iCarryFlag);
                    // Test OF
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_ADD(destByte,
                            sourceByte, tempResultByte, 0);
                    // Test ZF on result
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = tempResultByte == 0 ? true
                            : false;
                    // Test SF on result (set when MSB is 1, occurs when result >=
                    // 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = tempResultByte < 0 ? true
                            : false;
                    // Set PF on result
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                            .checkParityOfByte(tempResultByte);
                }
                break; // ADC

            case 3: // SBB
                // Determine value of carry flag before reset
                iCarryFlag = cpu.flags[CPU.REGISTER_FLAGS_CF] ? 1 : 0;

                // Execute SBB on reg,imm or mem,imm. Determine this from mm bits of
                // addressbyte
                if (((addressByte >> 6) & 0x03) == 3) {
                    // SBB reg,imm
                    // Determine destination register from addressbyte, ANDing it
                    // with 0000 0111
                    // Determine high/low part of register based on bit 3 (leading
                    // sss bit)
                    registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW
                            : (byte) CPU.REGISTER_GENERAL_HIGH;
                    destinationRegister = cpu.decodeRegister(operandWordSize,
                            addressByte & 0x07);

                    // Store old value
                    oldDest = destinationRegister[registerHighLow];

                    // Subtract (immediate byte + CF) from destination register
                    destinationRegister[registerHighLow] -= (sourceByte + iCarryFlag);

                    // Test AF
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_SUB(oldDest,
                            destinationRegister[registerHighLow]);
                    // Test CF
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_SUB(oldDest,
                            sourceByte, iCarryFlag);
                    // Test OF
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB(oldDest,
                            sourceByte, destinationRegister[registerHighLow],
                            iCarryFlag);
                    // Test ZF
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = destinationRegister[registerHighLow] == 0 ? true
                            : false;
                    // Test SF. In Java can check signed byte)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = destinationRegister[registerHighLow] < 0 ? true
                            : false;
                    // Set PF
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                            .checkParityOfByte(destinationRegister[registerHighLow]);
                } else {
                    // SBB mem,imm
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);
                    oldDest = cpu.getByteFromMemorySegment(addressByte,
                            memoryReferenceLocation);

                    // Subtract (immediate byte + CF) from destination register
                    destByte = (byte) ((oldDest - (sourceByte + iCarryFlag)) & 0xFF);

                    // Store result in memory
                    cpu.setByteInMemorySegment(addressByte,
                            memoryReferenceLocation, destByte);

                    // Test AF
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_SUB(oldDest,
                            destByte);
                    // Test CF
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_SUB(oldDest,
                            sourceByte, iCarryFlag);
                    // Test OF
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB(oldDest,
                            sourceByte, destByte, iCarryFlag);
                    // Test ZF
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = destByte == 0 ? true : false;
                    // Test SF. In Java can check signed byte)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = destByte < 0 ? true : false;
                    // Set PF
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                            .checkParityOfByte(destByte);
                }
                break; // SBB

            case 4: // AND

                // Clear appropriate flags
                cpu.flags[CPU.REGISTER_FLAGS_OF] = false;
                cpu.flags[CPU.REGISTER_FLAGS_CF] = false;
                // Bochs is always right (even if it clashes with the Intel docs)
                // ;-)
                cpu.flags[CPU.REGISTER_FLAGS_AF] = false;

                // Execute AND on reg,reg or mem,reg. Determine this from mm bits of
                // addressbyte
                if (((addressByte >> 6) & 0x03) == 3) {
                    // AND reg,imm

                    // Determine high/low part of register based on bit 3 (leading
                    // sss bit)
                    registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW
                            : (byte) CPU.REGISTER_GENERAL_HIGH;

                    // Determine destination register from addressbyte, ANDing it
                    // with 0000 0111
                    destinationRegister = cpu.decodeRegister(operandWordSize,
                            addressByte & 0x07);

                    destinationRegister[registerHighLow] &= sourceByte;

                    // Set ZF
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = destinationRegister[registerHighLow] == 0 ? true
                            : false;
                    // Test SF, only applies to LOW (set when MSB is 1, occurs when
                    // AL >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = destinationRegister[registerHighLow] < 0 ? true
                            : false;
                    // Set PF, only applies to LOW
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                            .checkParityOfByte(destinationRegister[registerHighLow]);
                } else {
                    // AND mem,imm
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Get byte from memory and AND source register
                    tempResult = cpu.getByteFromMemorySegment(addressByte,
                            memoryReferenceLocation);
                    tempResult &= sourceByte;

                    // Store result back in memory reference location
                    cpu.setByteInMemorySegment(addressByte,
                            memoryReferenceLocation, tempResult);

                    // Set ZF
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = tempResult == 0 ? true
                            : false;
                    // Test SF, only applies to LOW (set when MSB is 1, occurs when
                    // AL >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = tempResult < 0 ? true
                            : false;
                    // Set PF, only applies to LOW
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                            .checkParityOfByte(tempResult);
                }
                break;

            case 5: // SUB
                // Execute SUB on reg,imm or mem,imm. Determine this from mm bits of
                // addressbyte
                if (((addressByte >> 6) & 0x03) == 3) {
                    // SUB reg,imm
                    // Determine destination register from addressbyte, ANDing it
                    // with 0000 0111
                    // Determine high/low part of register based on bit 3 (leading
                    // sss bit)
                    registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW
                            : (byte) CPU.REGISTER_GENERAL_HIGH;
                    destinationRegister = cpu.decodeRegister(operandWordSize,
                            addressByte & 0x07);

                    // Store old value
                    oldDest = destinationRegister[registerHighLow];

                    // Subtract immediate byte from destination register
                    destinationRegister[registerHighLow] -= sourceByte;

                    // Test AF
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_SUB(oldDest,
                            destinationRegister[registerHighLow]);
                    // Test CF
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_SUB(oldDest,
                            sourceByte, 0);
                    // Test OF
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB(oldDest,
                            sourceByte, destinationRegister[registerHighLow], 0);
                    // Test ZF
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = destinationRegister[registerHighLow] == 0 ? true
                            : false;
                    // Test SF. In Java can check signed byte)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = destinationRegister[registerHighLow] < 0 ? true
                            : false;
                    // Set PF
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                            .checkParityOfByte(destinationRegister[registerHighLow]);
                } else {
                    // SUB mem,imm
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);
                    oldDest = cpu.getByteFromMemorySegment(addressByte,
                            memoryReferenceLocation);

                    // Subtract immediate byte from destination register
                    destByte = (byte) (oldDest - sourceByte);

                    // Store result in memory
                    cpu.setByteInMemorySegment(addressByte,
                            memoryReferenceLocation, destByte);

                    // Test AF
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_SUB(oldDest,
                            destByte);
                    // Test CF
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_SUB(oldDest,
                            sourceByte, 0);
                    // Test OF
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB(oldDest,
                            sourceByte, destByte, 0);
                    // Test ZF
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = destByte == 0 ? true : false;
                    // Test SF. In Java can check signed byte)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = destByte < 0 ? true : false;
                    // Set PF
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                            .checkParityOfByte(destByte);
                }
                break; // SUB

            case 6: // XOR

                // Clear appropriate flags
                cpu.flags[CPU.REGISTER_FLAGS_OF] = false;
                cpu.flags[CPU.REGISTER_FLAGS_CF] = false;
                cpu.flags[CPU.REGISTER_FLAGS_AF] = false;

                // Execute XOR on reg,reg or mem,reg. Determine this from mm bits of
                // addressbyte
                if (((addressByte >> 6) & 0x03) == 3) {
                    // XOR reg,reg

                    // Determine high/low part of register based on bit 3 (leading
                    // sss bit)
                    registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW
                            : (byte) CPU.REGISTER_GENERAL_HIGH;

                    // Determine destination register from addressbyte, ANDing it
                    // with 0000 0111
                    destinationRegister = cpu.decodeRegister(operandWordSize,
                            addressByte & 0x07);

                    destinationRegister[registerHighLow] ^= sourceByte;

                    // Test ZF
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = destinationRegister[registerHighLow] == 0 ? true
                            : false;
                    // Test SF, only applies to LOW (set when MSB is 1, occurs when
                    // AL >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = destinationRegister[registerHighLow] < 0 ? true
                            : false;
                    // Set PF, only applies to LOW
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                            .checkParityOfByte(destinationRegister[registerHighLow]);
                } else {
                    // XOR mem,reg
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Get byte from memory and XOR source register
                    tempResult = cpu.getByteFromMemorySegment(addressByte,
                            memoryReferenceLocation);
                    tempResult ^= sourceByte;

                    // Store result back in memory reference location
                    cpu.setByteInMemorySegment(addressByte,
                            memoryReferenceLocation, tempResult);

                    // Test ZF
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = tempResult == 0 ? true
                            : false;
                    // Test SF, only applies to LOW (set when MSB is 1, occurs when
                    // AL >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = tempResult < 0 ? true
                            : false;
                    // Set PF, only applies to LOW
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                            .checkParityOfByte(tempResult);
                }
                break; // XOR

            case 7: // CMP
                // Determine source2 value from mm bits of addressbyte
                if (((addressByte >> 6) & 0x03) == 3) {
                    // Source2 is a register

                    // Determine high/low part of register based on bit 3 (leading
                    // sss bit)
                    registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW
                            : (byte) CPU.REGISTER_GENERAL_HIGH;

                    // Determine "destination" value from addressbyte, ANDing it
                    // with 0000 0111
                    oldDest = cpu.decodeRegister(operandWordSize,
                            addressByte & 0x07)[registerHighLow];
                } else {
                    // Source2 is in memory
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Define registerHighLow standard on LOW
                    registerHighLow = CPU.REGISTER_GENERAL_LOW;

                    // Get byte from memory
                    oldDest = cpu.getByteFromMemorySegment(addressByte,
                            memoryReferenceLocation);
                }

                // Proceed with compare

                // Perform substraction
                // Subtract source byte from tempResult (which contains
                // "destination" byte)
                tempResult = (byte) (oldDest - sourceByte);

                // Test AF
                cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_SUB(oldDest,
                        tempResult);
                // Test CF
                cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_SUB(oldDest,
                        sourceByte, 0);
                // Test OF
                cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB(oldDest,
                        sourceByte, tempResult, 0);
                // Test ZF, is tested against tempResult
                cpu.flags[CPU.REGISTER_FLAGS_ZF] = tempResult == 0x00 ? true
                        : false;
                // Test SF, only applies to highest bit (set when most significant
                // bit is 1, occurs when tempResult >= 0x80)
                cpu.flags[CPU.REGISTER_FLAGS_SF] = tempResult < 0 ? true : false;
                // Set PF, only applies to lower byte
                cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                        .checkParityOfByte(tempResult);

                break; // CMP

            default:
                // TODO Throw exception for illegal nnn bits
                throw new CPUInstructionException(
                        "Immediate Group 1 (0x80) instruction no case match.");
        }
    }
}
