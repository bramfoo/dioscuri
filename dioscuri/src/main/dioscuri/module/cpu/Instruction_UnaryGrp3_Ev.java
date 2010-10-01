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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Intel opcode F7<BR>
 * Unary Group 3 opcode extension: TEST, NOT, NEG, MUL, IMUL, DIV, IDIV.<BR>
 * Performs the selected instruction (indicated by bits 5, 4, 3 of the ModR/M
 * byte).<BR>
 * Flags modified: depending on instruction can be any of: OF, CF, SF, ZF, AF,
 * PF (some of them undefined)
 */
public class Instruction_UnaryGrp3_Ev implements Instruction {

    // Attributes
    private CPU cpu;

    boolean operandWordSize;

    byte addressByte;
    byte[] memoryReferenceLocation;
    byte[] memoryReferenceDisplacement;

    byte[] sourceValue;
    byte[] eSourceValue;
    byte[] sourceValue2;
    byte[] eSourceValue2;
    byte[] destinationRegister;
    byte[] destinationRegister2;
    byte[] eDestinationRegister;
    byte[] eDestinationRegister2;

    long result;
    byte[] tempResult;

    // Logging
    private static final Logger logger = Logger.getLogger(Instruction_UnaryGrp3_Ev.class.getName());

    // Constructors

    /**
     * Class constructor
     */
    public Instruction_UnaryGrp3_Ev()
    {
        operandWordSize = true;

        addressByte = 0;
        memoryReferenceLocation = new byte[2];
        memoryReferenceDisplacement = new byte[2];

        sourceValue = new byte[2];
        eSourceValue = new byte[2];
        sourceValue2 = new byte[2];
        eSourceValue2 = new byte[2];
        destinationRegister = new byte[2];
        destinationRegister2 = new byte[2];
        eDestinationRegister = new byte[2];
        eDestinationRegister2 = new byte[2];

        result = 0;
        tempResult = new byte[2];
    }

    /**
     * Class constructor specifying processor reference
     *
     * @param processor Reference to CPU class
     */
    public Instruction_UnaryGrp3_Ev(CPU processor)
    {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Execute any of the following Unary Group 3 opcode extension: TEST, NOT,
     * NEG, MUL, IMUL, DIV, IDIV.<BR>
     *
     * @throws CPUInstructionException
     */
    public void execute() throws CPUInstructionException
    {
        // Clear sourceValue for previous pointers
        sourceValue = new byte[2];
        sourceValue2 = new byte[2];

        // Get addresByte
        addressByte = cpu.getByteFromCode();

        // Determine displacement of memory location (if any)
        memoryReferenceDisplacement = cpu.decodeMM(addressByte);

        // Execute instruction decoded from nnn (bits 5, 4, 3 in ModR/M byte)
        switch ((addressByte & 0x38) >> 3) {
            case 0: // TEST
                // Clear appropriate flags
                cpu.flags[CPU.REGISTER_FLAGS_OF] = false;
                cpu.flags[CPU.REGISTER_FLAGS_CF] = false;
                cpu.flags[CPU.REGISTER_FLAGS_AF] = false;

                // Execute TEST on imm,reg or imm,mem. Determine this from mm bits
                // of addressbyte
                if (((addressByte >> 6) & 0x03) == 3) {
                    // TEST imm,reg
                    // Determine source value from addressbyte, ANDing it with 0000
                    // 0111 to get sss bits
                    sourceValue = cpu.decodeRegister(operandWordSize,
                            addressByte & 0x07);
                } else {
                    // TEST mem,reg
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Retrieve source value from memory indicated by reference
                    // location
                    sourceValue = cpu.getWordFromMemorySegment(addressByte,
                            memoryReferenceLocation);
                }

                // Retrieve source word
                sourceValue2 = cpu.getWordFromCode();

                // Logical TEST of source1 and source2
                tempResult[CPU.REGISTER_GENERAL_HIGH] = (byte) (sourceValue2[CPU.REGISTER_GENERAL_HIGH] & sourceValue[CPU.REGISTER_GENERAL_HIGH]);
                tempResult[CPU.REGISTER_GENERAL_LOW] = (byte) (sourceValue2[CPU.REGISTER_GENERAL_LOW] & sourceValue[CPU.REGISTER_GENERAL_LOW]);

                // Test ZF on result
                cpu.flags[CPU.REGISTER_FLAGS_ZF] = tempResult[CPU.REGISTER_GENERAL_HIGH] == 0
                        && tempResult[CPU.REGISTER_GENERAL_LOW] == 0 ? true : false;
                // Test SF on result (set when MSB is 1, occurs when result >= 0x80)
                cpu.flags[CPU.REGISTER_FLAGS_SF] = tempResult[CPU.REGISTER_GENERAL_HIGH] < 0 ? true
                        : false;
                // Set PF on lower byte of result
                cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                        .checkParityOfByte(tempResult[CPU.REGISTER_GENERAL_LOW]);
                break;

            case 1: // does not exist
                // Throw exception for illegal nnn bits
                throw new CPUInstructionException(
                        "Unary Group 3 (0xF6) illegal reg bits");

            case 2: // NOT: reverse each bit of r/m16 or r/m32
                // Flags affected: none

                if (((addressByte >> 6) & 0x03) == 3) {
                    // NOT reg
                    // Determine source value from addressbyte, ANDing it with 0000
                    // 0111
                    sourceValue = cpu.decodeRegister(operandWordSize,
                            addressByte & 0x07);

                    // Negate source 16-bit
                    sourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) ~sourceValue[CPU.REGISTER_GENERAL_LOW];
                    sourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) ~sourceValue[CPU.REGISTER_GENERAL_HIGH];

                    // 32-bits
                    if (cpu.doubleWord) {
                        // Determine extra source value from addressbyte, ANDing it
                        // with 0000 0111
                        eSourceValue = cpu.decodeExtraRegister(addressByte & 0x07);

                        // Negate source 32-bit
                        eSourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) ~eSourceValue[CPU.REGISTER_GENERAL_LOW];
                        eSourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) ~eSourceValue[CPU.REGISTER_GENERAL_HIGH];
                    }
                } else {
                    // NOT mem
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Get word from memory
                    sourceValue = cpu.getWordFromMemorySegment(addressByte,
                            memoryReferenceLocation);

                    // Negate source 16-bit
                    sourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) ~sourceValue[CPU.REGISTER_GENERAL_LOW];
                    sourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) ~sourceValue[CPU.REGISTER_GENERAL_HIGH];

                    // Store result in mem
                    cpu.setWordInMemorySegment(addressByte,
                            memoryReferenceLocation, sourceValue);

                    // 32-bits
                    if (cpu.doubleWord) {
                        // Update memory location by wordsize
                        memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] += 2;
                        if (memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] == 0x00
                                || memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] == 0x01) {
                            // Overflow
                            memoryReferenceLocation[CPU.REGISTER_GENERAL_HIGH]++;
                        }
                        // Retrieve next word from memory
                        eSourceValue = cpu.getWordFromMemorySegment(addressByte,
                                memoryReferenceLocation);

                        // Negate source 32-bit
                        eSourceValue[CPU.REGISTER_GENERAL_LOW] = (byte) ~eSourceValue[CPU.REGISTER_GENERAL_LOW];
                        eSourceValue[CPU.REGISTER_GENERAL_HIGH] = (byte) ~eSourceValue[CPU.REGISTER_GENERAL_HIGH];

                        // Store result in mem
                        cpu.setWordInMemorySegment(addressByte,
                                memoryReferenceLocation, eSourceValue);
                    }
                }
                break;

            case 3: // NEG

                // Execute NEG on imm,reg or imm,mem. Determine this from mm bits of
                // addressbyte
                if (((addressByte >> 6) & 0x03) == 3) {
                    // NEG imm,reg
                    // Determine source value from addressbyte, ANDing it with 0000
                    // 0111 to get sss bits
                    sourceValue = cpu.decodeRegister(operandWordSize,
                            addressByte & 0x07);
                } else {
                    // NEG mem,reg
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Retrieve source value from memory indicated by reference
                    // location
                    sourceValue = cpu.getWordFromMemorySegment(addressByte,
                            memoryReferenceLocation);
                }

                // Copy source value for flag tests later
                System.arraycopy(sourceValue, 0, tempResult, 0, sourceValue.length);

                // Negate source; equivalent to 0 - source.
                destinationRegister = Util.subtractWords(new byte[]{0, 0},
                        sourceValue, 0);

                // Copy result back in source
                System.arraycopy(destinationRegister, 0, sourceValue, 0,
                        destinationRegister.length);

                // Store result back in memory if necessary
                if (((addressByte >> 6) & 0x03) != 3) {
                    cpu.setWordInMemorySegment(addressByte,
                            memoryReferenceLocation, sourceValue);
                }

                // Clear CF if source operand is zero, set otherwise
                cpu.flags[CPU.REGISTER_FLAGS_CF] = sourceValue[CPU.REGISTER_GENERAL_LOW] == 0
                        && sourceValue[CPU.REGISTER_GENERAL_HIGH] == 0 ? false
                        : true;

                // Set OF flag
                cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB(new byte[]{0,
                        0}, tempResult, sourceValue, 0);
                // Set AF flag
                cpu.flags[CPU.REGISTER_FLAGS_AF] = (sourceValue[CPU.REGISTER_GENERAL_LOW] & 0x0F) != 0 ? true
                        : false;
                // Test ZF on result
                cpu.flags[CPU.REGISTER_FLAGS_ZF] = sourceValue[CPU.REGISTER_GENERAL_HIGH] == 0
                        && sourceValue[CPU.REGISTER_GENERAL_LOW] == 0 ? true
                        : false;
                // Test SF on result (set when MSB is 1, occurs when result >= 0x80)
                cpu.flags[CPU.REGISTER_FLAGS_SF] = sourceValue[CPU.REGISTER_GENERAL_HIGH] < 0 ? true
                        : false;
                // Set PF on lower byte of result
                cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                        .checkParityOfByte(sourceValue[CPU.REGISTER_GENERAL_LOW]);
                break;

            case 4: // MUL eAX.
                // Flags: OF, CF set to 0 if upper half of result is 0;
                // Flags undefined (not set): SF, ZF, AF, PF
                // Set destination to DX:AX
                destinationRegister = cpu.ax;
                destinationRegister2 = cpu.dx;

                // Option for 32 bits implementation
                if (cpu.doubleWord) {
                    // Set destination to eDX:eAX
                    eDestinationRegister = cpu.eax;
                    eDestinationRegister2 = cpu.edx;
                }

                // Execute mul on reg or mem. Determine this from mm bits of
                // addressbyte
                if (((addressByte >> 6) & 0x03) == 3) {
                    // MUL eAX, reg
                    // Determine source value from addressbyte, ANDing it with 0000
                    // 0111
                    sourceValue = cpu.decodeRegister(operandWordSize,
                            addressByte & 0x07);

                    // Option for 32 bits implementation
                    if (cpu.doubleWord) {
                        // Determine extra source value from addressbyte, ANDing it
                        // with 0000 0111
                        eSourceValue = cpu.decodeExtraRegister(addressByte & 0x07);
                    }

                } else {
                    // MUL eAX, mem
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Get word from memory
                    sourceValue = cpu.getWordFromMemorySegment(addressByte,
                            memoryReferenceLocation);
                    if (cpu.doubleWord) {
                        // Update memory location by wordsize, retrieve next word
                        memoryReferenceLocation = Util.addWords(
                                memoryReferenceLocation, new byte[]{0x00, 0x02},
                                0);
                        eSourceValue = cpu.getWordFromMemorySegment(addressByte,
                                memoryReferenceLocation);
                    }
                }

                // Unsigned multiply source and destination (as ints)
                result = (((((long) destinationRegister[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 8) + (((long) destinationRegister[CPU.REGISTER_GENERAL_LOW]) & 0xFF))
                        * (((((long) sourceValue[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 8) + (((long) sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF));

                if (cpu.doubleWord) {
                    // Add remaining multiplications to result:
                    // Current result contains AX * REGX
                    // Add (eAX * REGX)<<16
                    result += (((((((int) eDestinationRegister[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 8) + (((int) eDestinationRegister[CPU.REGISTER_GENERAL_LOW]) & 0xFF)) * (((((int) sourceValue[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 8) + (((int) sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF))) << 16);
                    // Add (AX * eREGX)<<16
                    result += (((((((int) destinationRegister[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 8) + (((int) destinationRegister[CPU.REGISTER_GENERAL_LOW]) & 0xFF)) * (((((int) eSourceValue[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 8) + (((int) eSourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF))) << 16);
                    // Add (eAX * eREGX)<<32
                    result += (((((((int) eDestinationRegister[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 8) + (((int) eDestinationRegister[CPU.REGISTER_GENERAL_LOW]) & 0xFF)) * (((((int) eSourceValue[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 8) + (((int) eSourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF))) << 32);
                }

                // Move answer into destination
                destinationRegister[CPU.REGISTER_GENERAL_LOW] = (byte) (result);
                destinationRegister[CPU.REGISTER_GENERAL_HIGH] = (byte) ((result) >> 8);
                destinationRegister2[CPU.REGISTER_GENERAL_LOW] = (byte) ((result) >> 16);
                destinationRegister2[CPU.REGISTER_GENERAL_HIGH] = (byte) ((result) >> 24);

                if (cpu.doubleWord) {
                    // Fill in rest of value, correcting placing for destReg2
                    eDestinationRegister[CPU.REGISTER_GENERAL_LOW] = (byte) ((result) >> 16);
                    eDestinationRegister[CPU.REGISTER_GENERAL_HIGH] = (byte) ((result) >> 24);

                    destinationRegister2[CPU.REGISTER_GENERAL_LOW] = (byte) ((result) >> 32);
                    destinationRegister2[CPU.REGISTER_GENERAL_HIGH] = (byte) ((result) >> 40);
                    eDestinationRegister2[CPU.REGISTER_GENERAL_LOW] = (byte) ((result) >> 48);
                    eDestinationRegister2[CPU.REGISTER_GENERAL_HIGH] = (byte) ((result) >> 56);
                }

                // Set appropriate flags
                // Set OF, CF if significant bits are carried into upper half of
                // result, else clear them.
                if (cpu.doubleWord) {
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = cpu.flags[CPU.REGISTER_FLAGS_CF] = eDestinationRegister2[CPU.REGISTER_GENERAL_HIGH] == 0
                            && eDestinationRegister2[CPU.REGISTER_GENERAL_LOW] == 0 ? false
                            : true;

                    // Mimic Bochs' flag behaviour
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                            .checkParityOfByte(destinationRegister[CPU.REGISTER_GENERAL_LOW]);
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
                    // Check only lower half of result (eAX register)
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = eDestinationRegister[CPU.REGISTER_GENERAL_HIGH] == 0x00
                            && eDestinationRegister[CPU.REGISTER_GENERAL_LOW] == 0x00
                            && destinationRegister[CPU.REGISTER_GENERAL_HIGH] == 0x00
                            && destinationRegister[CPU.REGISTER_GENERAL_LOW] == 0x00 ? true
                            : false;
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = eDestinationRegister2[CPU.REGISTER_GENERAL_HIGH] < 0 ? true
                            : false;
                } else {
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = cpu.flags[CPU.REGISTER_FLAGS_CF] = destinationRegister2[CPU.REGISTER_GENERAL_HIGH] == 0
                            && destinationRegister2[CPU.REGISTER_GENERAL_LOW] == 0 ? false
                            : true;

                    // Mimic Bochs' flag behaviour
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                            .checkParityOfByte(destinationRegister[CPU.REGISTER_GENERAL_LOW]);
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] == 0x00
                            && destinationRegister[CPU.REGISTER_GENERAL_LOW] == 0x00 ? true
                            : false;
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] < 0 ? true
                            : false;
                }

                break; // MUL AL/eAX

            case 5: // IMUL 16-bit: AX*r/mword -> DX:AX or 32-bit: eAX*r/mword ->
                // eDX:eAX.
                // Flags: OF, CF set to 0 if upper half of result is 0; SF, ZF, AF,
                // PF are undefined.

                // Set destination to DX:AX
                destinationRegister = cpu.ax;
                destinationRegister2 = cpu.dx;

                // Option for 32 bits implementation
                if (cpu.doubleWord) {
                    // Set destination to eDX:eAX
                    eDestinationRegister = cpu.eax;
                    eDestinationRegister2 = cpu.edx;
                }

                // Execute mul on reg or mem. Determine this from mm bits of
                // addressbyte
                if (((addressByte >> 6) & 0x03) == 3) {
                    // IMUL eAX, reg
                    // Determine source value from addressbyte, ANDing it with 0000
                    // 0111
                    sourceValue = cpu.decodeRegister(operandWordSize,
                            addressByte & 0x07);

                    // Option for 32 bits implementation
                    if (cpu.doubleWord) {
                        // Determine extra source value from addressbyte, ANDing it
                        // with 0000 0111
                        eSourceValue = cpu.decodeExtraRegister(addressByte & 0x07);
                    }

                } else {
                    // IMUL eAX, mem
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Get word from memory
                    sourceValue = cpu.getWordFromMemorySegment(addressByte,
                            memoryReferenceLocation);
                    if (cpu.doubleWord) {
                        // Update memory location by wordsize
                        memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] += 2;
                        if (memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] == 0x00
                                || memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] == 0x01) {
                            // Overflow
                            memoryReferenceLocation[CPU.REGISTER_GENERAL_HIGH]++;
                        }
                        // Retrieve next byte from memory
                        eSourceValue = cpu.getWordFromMemorySegment(addressByte,
                                memoryReferenceLocation);
                    }
                }

                // Signed multiply source and destination
                result = ((((long) destinationRegister[CPU.REGISTER_GENERAL_HIGH]) << 8) + (((long) destinationRegister[CPU.REGISTER_GENERAL_LOW]) & 0xFF))
                        * ((((long) sourceValue[CPU.REGISTER_GENERAL_HIGH]) << 8) + (((long) sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF));

                if (cpu.doubleWord) {
                    // Add remaining multiplications to result:
                    // Current result contains AX * REGX
                    // Add (eAX * REGX)<<16
                    result += ((((((int) eDestinationRegister[CPU.REGISTER_GENERAL_HIGH]) << 8) + (((int) eDestinationRegister[CPU.REGISTER_GENERAL_LOW]) & 0xFF)) * ((((int) sourceValue[CPU.REGISTER_GENERAL_HIGH]) << 8) + (((int) sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF))) << 16);
                    // Add (AX * eREGX)<<16
                    result += ((((((int) destinationRegister[CPU.REGISTER_GENERAL_HIGH]) << 8) + (((int) destinationRegister[CPU.REGISTER_GENERAL_LOW]) & 0xFF)) * ((((int) eSourceValue[CPU.REGISTER_GENERAL_HIGH]) << 8) + (((int) eSourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF))) << 16);
                    // Add (eAX * eREGX)<<32
                    result += ((((((int) eDestinationRegister[CPU.REGISTER_GENERAL_HIGH]) << 8) + (((int) eDestinationRegister[CPU.REGISTER_GENERAL_LOW]) & 0xFF)) * ((((int) eSourceValue[CPU.REGISTER_GENERAL_HIGH]) << 8) + (((int) eSourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF))) << 32);
                }

                // Move answer into destination
                destinationRegister[CPU.REGISTER_GENERAL_LOW] = (byte) (result);
                destinationRegister[CPU.REGISTER_GENERAL_HIGH] = (byte) ((result) >> 8);
                destinationRegister2[CPU.REGISTER_GENERAL_LOW] = (byte) ((result) >> 16);
                destinationRegister2[CPU.REGISTER_GENERAL_HIGH] = (byte) ((result) >> 24);

                if (cpu.doubleWord) {
                    // Fill in rest of value, correcting placing for destReg2
                    eDestinationRegister[CPU.REGISTER_GENERAL_LOW] = (byte) ((result) >> 16);
                    eDestinationRegister[CPU.REGISTER_GENERAL_HIGH] = (byte) ((result) >> 24);

                    destinationRegister2[CPU.REGISTER_GENERAL_LOW] = (byte) ((result) >> 32);
                    destinationRegister2[CPU.REGISTER_GENERAL_HIGH] = (byte) ((result) >> 40);
                    eDestinationRegister2[CPU.REGISTER_GENERAL_LOW] = (byte) ((result) >> 48);
                    eDestinationRegister2[CPU.REGISTER_GENERAL_HIGH] = (byte) ((result) >> 56);
                }

                // Set appropriate flags
                // Set OF, CF if significant bits are carried into upper half of
                // result, else clear them.
                if (cpu.doubleWord) {
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = cpu.flags[CPU.REGISTER_FLAGS_CF] = eDestinationRegister2[CPU.REGISTER_GENERAL_HIGH] == 0
                            && eDestinationRegister2[CPU.REGISTER_GENERAL_LOW] == 0 ? false
                            : true;
                } else {
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = cpu.flags[CPU.REGISTER_FLAGS_CF] = destinationRegister2[CPU.REGISTER_GENERAL_HIGH] == 0
                            && destinationRegister2[CPU.REGISTER_GENERAL_LOW] == 0 ? false
                            : true;
                }
                // Set other flags as Bochs does - Bochs is always right ;-)
                cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
                // Set PF according to destination
                cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                        .checkParityOfByte(destinationRegister[CPU.REGISTER_GENERAL_LOW]);
                // Set ZF zero if destination is zero
                cpu.flags[CPU.REGISTER_FLAGS_ZF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] == 0
                        && destinationRegister[CPU.REGISTER_GENERAL_LOW] == 0 ? true
                        : false;
                // Set SF according to destination
                cpu.flags[CPU.REGISTER_FLAGS_SF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] < 0 ? true
                        : false;

                break; // IMUL 16-bit: AX*r/mword -> DX:AX or 32-bit: eAX*r/mword ->
            // eDX:eAX.

            case 6: // DIV eAX;
                // Flags: OF, CF, SF, ZF, AF, PF are undefined.
                // FIXME: DIV by 0 not handled (#DE)
                // FIXME: DIV result exceeding register size not handled (#DE)
                // Set destination to DX:AX
                destinationRegister = cpu.ax;
                destinationRegister2 = cpu.dx;

                // Option for 32 bits implementation
                if (cpu.doubleWord) {
                    // Set destination to eDX:eAX
                    eDestinationRegister = cpu.eax;
                    eDestinationRegister2 = cpu.edx;
                }

                // Execute DIV on reg or mem. Determine this from mm bits of
                // addressbyte
                if (((addressByte & 0xC0) >> 6) == 3) {
                    // DIV eAX, reg
                    // Determine source value from addressbyte, ANDing it with 0000
                    // 0111
                    sourceValue = cpu.decodeRegister(operandWordSize,
                            addressByte & 0x07);

                    // Option for 32 bits implementation
                    if (cpu.doubleWord) {
                        // Determine extra source value from addressbyte, ANDing it
                        // with 0000 0111
                        eSourceValue = cpu.decodeExtraRegister(addressByte & 0x07);
                    }

                } else {
                    // DIV eAX, mem
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Get word from memory
                    sourceValue = cpu.getWordFromMemorySegment(addressByte,
                            memoryReferenceLocation);
                    if (cpu.doubleWord) {
                        // Update memory location by wordsize
                        memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] += 2;
                        if (memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] == 0x00
                                || memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] == 0x01) {
                            // Overflow
                            memoryReferenceLocation[CPU.REGISTER_GENERAL_HIGH]++;
                        }
                        // Retrieve next byte from memory
                        eSourceValue = cpu.getWordFromMemorySegment(addressByte,
                                memoryReferenceLocation);
                    }
                }

                if (!cpu.doubleWord) // 16 bit registers
                {

                    long dividend = ((((long) destinationRegister2[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 24)
                            + ((((long) destinationRegister2[CPU.REGISTER_GENERAL_LOW]) & 0xFF) << 16)
                            + ((((long) destinationRegister[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 8)
                            + (((long) destinationRegister[CPU.REGISTER_GENERAL_LOW]) & 0xFF);
                    long divisor = ((((long) sourceValue[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 8)
                            + (((long) sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF);
                    // Calculate quotient, remainder
                    long quotient = dividend / divisor;
                    long remainder = dividend % divisor;

                    // Move quotient into destination
                    destinationRegister[CPU.REGISTER_GENERAL_LOW] = (byte) (quotient);
                    destinationRegister[CPU.REGISTER_GENERAL_HIGH] = (byte) ((quotient) >> 8);

                    // Move remainder into destination2
                    destinationRegister2[CPU.REGISTER_GENERAL_LOW] = (byte) ((remainder));
                    destinationRegister2[CPU.REGISTER_GENERAL_HIGH] = (byte) ((remainder) >> 8);
                } else // 32 bit registers
                {
                    // FIXME: Not all these values will fit into longs:
                    // Long max: +9.223.372.036.854.775.807
                    // 0xFF<<56 = 18.374.686.479.671.623.680

                    logger
                            .log(
                                    Level.WARNING,
                                    "["
                                            + cpu.getType()
                                            + "] Unary Group 3 (0xF7|6) not (properly) implemented");

                    long dividend = ((((long) eDestinationRegister2[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 56)
                            + ((((long) eDestinationRegister2[CPU.REGISTER_GENERAL_LOW]) & 0xFF) << 48)
                            + ((((long) destinationRegister2[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 40)
                            + ((((long) destinationRegister2[CPU.REGISTER_GENERAL_LOW]) & 0xFF) << 32)
                            + ((((int) eDestinationRegister[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 24)
                            + ((((int) eDestinationRegister[CPU.REGISTER_GENERAL_LOW]) & 0xFF) << 16)
                            + ((((int) destinationRegister[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 8)
                            + (((int) destinationRegister[CPU.REGISTER_GENERAL_LOW]) & 0xFF);
                    long divisor = ((((long) eSourceValue[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 24)
                            + ((((long) eSourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF) << 16)
                            + ((((long) sourceValue[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 8)
                            + (((long) sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF);
                    // Calculate quotient
                    long quotient = dividend / divisor;
                    // Calculate remainder
                    long remainder = dividend % divisor;

                    // Move quotient into destination
                    destinationRegister[CPU.REGISTER_GENERAL_LOW] = (byte) (quotient);
                    destinationRegister[CPU.REGISTER_GENERAL_HIGH] = (byte) ((quotient) >> 8);
                    eDestinationRegister[CPU.REGISTER_GENERAL_LOW] = (byte) ((quotient) >> 16);
                    eDestinationRegister[CPU.REGISTER_GENERAL_HIGH] = (byte) ((quotient) >> 24);

                    // Move remainder into destination2
                    destinationRegister2[CPU.REGISTER_GENERAL_LOW] = (byte) ((remainder));
                    destinationRegister2[CPU.REGISTER_GENERAL_HIGH] = (byte) ((remainder) >> 8);
                    eDestinationRegister2[CPU.REGISTER_GENERAL_LOW] = (byte) ((remainder) >> 16);
                    eDestinationRegister2[CPU.REGISTER_GENERAL_HIGH] = (byte) ((remainder) >> 24);
                }
                break; // DIV eAX

            case 7: // IDIV
                // FIXME: Unsure about the value of DX/eDX. Intel specs state that
                // DX/eDX should contain sign extension of AX/eAX.
                // But is that already before execution of IDIV? This implementation
                // takes that as an assumption.
                // So actually no difference between DIV and IDIV...
                // FIXME: IDIV by 0 not handled (#DE)
                // FIXME: IDIV result exceeding register size not handled (#DE)
                // Set destination to DX:AX
                // Flags: OF, CF, SF, ZF, AF, PF are undefined.
                destinationRegister = cpu.ax;
                destinationRegister2 = cpu.dx;

                // Option for 32 bits implementation
                if (cpu.doubleWord) {
                    // Set destination to eDX:eAX
                    eDestinationRegister = cpu.eax;
                    eDestinationRegister2 = cpu.edx;
                }

                // Execute IDIV on reg or mem. Determine this from mm bits of
                // addressbyte
                if (((addressByte & 0xC0) >> 6) == 3) {
                    // IDIV eAX, reg
                    // Determine source value from addressbyte, ANDing it with 0000
                    // 0111
                    sourceValue = cpu.decodeRegister(operandWordSize,
                            addressByte & 0x07);

                    // Option for 32 bits implementation
                    if (cpu.doubleWord) {
                        // Determine extra source value from addressbyte, ANDing it
                        // with 0000 0111
                        eSourceValue = cpu.decodeExtraRegister(addressByte & 0x07);
                    }

                } else {
                    // IDIV eAX, mem
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Get byte from memory
                    sourceValue = cpu.getWordFromMemorySegment(addressByte,
                            memoryReferenceLocation);
                    if (cpu.doubleWord) {
                        // Update memory location by wordsize
                        memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] += 2;
                        if (memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] == 0x00
                                || memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW] == 0x01) {
                            // Overflow
                            memoryReferenceLocation[CPU.REGISTER_GENERAL_HIGH]++;
                        }
                        // Retrieve next byte from memory
                        eSourceValue = cpu.getWordFromMemorySegment(addressByte,
                                memoryReferenceLocation);
                    }
                }

                // Signed division
                if (!cpu.doubleWord) // 16 bit registers (IDIV of word/doubleword)
                {
                    long dividend = ((((int) destinationRegister2[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 24)
                            + ((((int) destinationRegister2[CPU.REGISTER_GENERAL_LOW]) & 0xFF) << 16)
                            + ((((int) destinationRegister[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 8)
                            + (((int) destinationRegister[CPU.REGISTER_GENERAL_LOW]) & 0xFF);
                    long divisor = ((((int) sourceValue[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 8)
                            + (((int) sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF);

                    // Calculate quotient (DX contains sign extension of AX),
                    // remainder
                    long quotient = dividend / divisor;
                    long remainder = dividend % divisor;

                    // Move quotient into destination
                    destinationRegister[CPU.REGISTER_GENERAL_LOW] = (byte) (quotient);
                    destinationRegister[CPU.REGISTER_GENERAL_HIGH] = (byte) ((quotient) >> 8);

                    // Move remainder into destination2
                    destinationRegister2[CPU.REGISTER_GENERAL_LOW] = (byte) ((remainder));
                    destinationRegister2[CPU.REGISTER_GENERAL_HIGH] = (byte) ((remainder) >> 8);
                } else // 32 bit registers (IDIV of doubleword/quadword)
                {
                    // FIXME: Not all these values will fit into longs:
                    // Long max: +9.223.372.036.854.775.807
                    // 0xFF<<56 = 18.374.686.479.671.623.680
                    throw new CPUInstructionException(
                            "Unary Group 3 (0xF6|6) not (properly) implemented");
                    // long dividend =
                    // ((((long)eDestinationRegister2[CPU.REGISTER_GENERAL_HIGH]) &
                    // 0xFF)<<56) + ((((long)
                    // eDestinationRegister2[CPU.REGISTER_GENERAL_LOW]) & 0xFF)<<48)
                    // + ((((long)destinationRegister2[CPU.REGISTER_GENERAL_HIGH]) &
                    // 0xFF)<<40) +
                    // ((((long)destinationRegister2[CPU.REGISTER_GENERAL_LOW]) &
                    // 0xFF)<<32) +
                    // ((((int)eDestinationRegister[CPU.REGISTER_GENERAL_HIGH]) &
                    // 0xFF)<<24) + ((((int)
                    // eDestinationRegister[CPU.REGISTER_GENERAL_LOW]) & 0xFF)<<16)
                    // + ((((int)destinationRegister[CPU.REGISTER_GENERAL_HIGH]) &
                    // 0xFF)<<8) + (((int)
                    // destinationRegister[CPU.REGISTER_GENERAL_LOW]) & 0xFF);
                    // long divisor =
                    // ((((long)eSourceValue[CPU.REGISTER_GENERAL_HIGH]) &
                    // 0xFF)<<24) + ((((long)eSourceValue[CPU.REGISTER_GENERAL_LOW])
                    // & 0xFF)<<16) +
                    // ((((long)sourceValue[CPU.REGISTER_GENERAL_HIGH]) & 0xFF)<<8)
                    // + (((long) sourceValue[CPU.REGISTER_GENERAL_LOW]) & 0xFF);
                    // // Calculate quotient, remainder
                    // long quotient = dividend / divisor;
                    // long remainder = dividend % divisor;
                    //
                    // // Move quotient into destination
                    // destinationRegister[CPU.REGISTER_GENERAL_LOW] = (byte)
                    // (quotient);
                    // destinationRegister[CPU.REGISTER_GENERAL_HIGH] = (byte)
                    // ((quotient)>>8);
                    // eDestinationRegister[CPU.REGISTER_GENERAL_LOW] = (byte)
                    // ((quotient)>>16);
                    // eDestinationRegister[CPU.REGISTER_GENERAL_HIGH] = (byte)
                    // ((quotient)>>24);
                    //
                    // // Move remainder into destination2
                    // destinationRegister2[CPU.REGISTER_GENERAL_LOW] = (byte)
                    // ((remainder));
                    // destinationRegister2[CPU.REGISTER_GENERAL_HIGH] = (byte)
                    // ((remainder)>>8);
                    // eDestinationRegister2[CPU.REGISTER_GENERAL_LOW] = (byte)
                    // ((remainder)>>16);
                    // eDestinationRegister2[CPU.REGISTER_GENERAL_HIGH] = (byte)
                    // ((remainder)>>24);
                }
                break;

            default:
                // Throw exception for illegal nnn bits
                throw new CPUInstructionException(
                        "Unary Group 3 (0xF6) illegal reg bits");
        }
    }
}
