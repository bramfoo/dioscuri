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
 * Intel opcode FF<BR>
 * INC/DEC Group 5 opcode extension: INC, DEC, CALLN, CALLF, JMPN, JMPF, PUSH.<BR>
 * Performs the selected instruction (indicated by bits 5, 4, 3 of the ModR/M
 * byte) using immediate data.<BR>
 * Flags modified: depending on instruction can be any of: OF, SF, ZF, AF, PF,
 * CF
 */
public class Instruction_INCDEC_GRP5 implements Instruction {

    // Attributes
    private CPU cpu;

    boolean operandWordSize;

    byte addressByte;
    byte[] memoryReferenceLocation;
    byte[] memoryReferenceDisplacement;
    byte[] sourceValue;
    byte[] oldSource;
    byte[] incWord;
    byte[] newCS;
    byte[] newIP;

    int intermediateResult;
    byte displacementByte;
    byte[] displacementWord;
    int overUnderFlowCheck;

    byte[] temp;

    // Logging
    private static final Logger logger = Logger.getLogger(Instruction_INCDEC_GRP5.class.getName());

    // Constructors

    /**
     * Class constructor
     */
    public Instruction_INCDEC_GRP5() {
        // Initialise variables
        operandWordSize = true;

        addressByte = 0;
        memoryReferenceLocation = new byte[2];
        memoryReferenceDisplacement = new byte[2];
        sourceValue = new byte[2];
        oldSource = new byte[2];
        newCS = new byte[2];
        newIP = new byte[2];

        // Set word for increment to 1
        incWord = new byte[]{0x00, 0x01};

        intermediateResult = 0;
        displacementByte = 0;
        displacementWord = new byte[2];

        temp = new byte[2];
    }

    /**
     * Class constructor specifying processor reference
     *
     * @param processor Reference to CPU class
     */
    public Instruction_INCDEC_GRP5(CPU processor) {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Execute any of the following Immediate Group 5 instructions: INC, DEC,
     * CALLN, CALLF, JMPN, JMPF, PUSH.<BR>
     */
    public void execute() {
        // Get addresByte
        addressByte = cpu.getByteFromCode();

        // Re-initialise source to get rid of pointers
        sourceValue = new byte[2];

        // Execute instruction decoded from nnn (bits 5, 4, 3 in ModR/M byte)
        switch ((addressByte & 0x38) >> 3) {
            case 0: // INC Ev
                if (((addressByte >> 6) & 0x03) == 3) {
                    // Address given in register
                    sourceValue = cpu.decodeRegister(operandWordSize,
                            addressByte & 0x07);
                } else {
                    // Address given in memory (m16:16)
                    // Determine IP displacement of memory location (if any)
                    memoryReferenceDisplacement = cpu.decodeMM(addressByte);

                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Get value from memory
                    sourceValue = cpu.getWordFromMemorySegment(addressByte,
                            memoryReferenceLocation);
                }

                // Store initial value
                System.arraycopy(sourceValue, 0, oldSource, 0, sourceValue.length);

                // Increment the source (= destination) register
                temp = Util.addWords(sourceValue, incWord, 0);
                System.arraycopy(temp, 0, sourceValue, 0, temp.length);

                // Return result to memory if necessary
                if (((addressByte >> 6) & 0x03) != 3) {
                    cpu.setWordInMemorySegment(addressByte,
                            memoryReferenceLocation, sourceValue);
                }

                // Set appropriate flags
                // Test AF
                cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_ADD(
                        oldSource[CPU.REGISTER_GENERAL_LOW],
                        sourceValue[CPU.REGISTER_GENERAL_LOW]);
                // Test OF
                cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_ADD(oldSource,
                        incWord, sourceValue, 0);
                // Test ZF
                cpu.flags[CPU.REGISTER_FLAGS_ZF] = (sourceValue[CPU.REGISTER_GENERAL_HIGH] == 0x00 && sourceValue[CPU.REGISTER_GENERAL_LOW] == 0x00) ? true
                        : false;
                // Test SF (set when MSB of BH is 1. In Java can check signed byte)
                cpu.flags[CPU.REGISTER_FLAGS_SF] = sourceValue[CPU.REGISTER_GENERAL_HIGH] < 0 ? true
                        : false;
                // Set PF, only applies to LSB
                cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                        .checkParityOfByte(sourceValue[CPU.REGISTER_GENERAL_LOW]);
                break; // INC Ev

            case 1: // DEC Ev
                if (((addressByte >> 6) & 0x03) == 3) {
                    // Address given in register
                    sourceValue = cpu.decodeRegister(operandWordSize,
                            addressByte & 0x07);
                } else {
                    // Address given in memory (m16:16)
                    // Determine IP displacement of memory location (if any)
                    memoryReferenceDisplacement = cpu.decodeMM(addressByte);

                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Get value from memory
                    sourceValue = cpu.getWordFromMemorySegment(addressByte,
                            memoryReferenceLocation);
                }

                // Store initial value
                System.arraycopy(sourceValue, 0, oldSource, 0, sourceValue.length);

                // Increment the source (= destination) register
                temp = Util.subtractWords(sourceValue, incWord, 0);
                System.arraycopy(temp, 0, sourceValue, 0, temp.length);

                // Return result to memory if necessary
                if (((addressByte >> 6) & 0x03) != 3) {
                    cpu.setWordInMemorySegment(addressByte,
                            memoryReferenceLocation, sourceValue);
                }

                // Set appropriate flags
                // Test AF
                cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_SUB(
                        oldSource[CPU.REGISTER_GENERAL_LOW],
                        sourceValue[CPU.REGISTER_GENERAL_LOW]);
                // Test OF
                cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB(oldSource,
                        incWord, sourceValue, 0);
                // Test ZF
                cpu.flags[CPU.REGISTER_FLAGS_ZF] = (sourceValue[CPU.REGISTER_GENERAL_HIGH] == 0x00 && sourceValue[CPU.REGISTER_GENERAL_LOW] == 0x00) ? true
                        : false;
                // Test SF (set when MSB of BH is 1. In Java can check signed byte)
                cpu.flags[CPU.REGISTER_FLAGS_SF] = sourceValue[CPU.REGISTER_GENERAL_HIGH] < 0 ? true
                        : false;
                // Set PF, only applies to LSB
                cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                        .checkParityOfByte(sourceValue[CPU.REGISTER_GENERAL_LOW]);
                break; // DEC Ev

            case 2: // CALLN Ev
                // Call near is absolute indirect, address given in register/memory
                // (r/m16, r/m32)
                // Note: 32-bit addresses are not considered here

                if (((addressByte >> 6) & 0x03) == 3) {
                    // Address given in register
                    newIP = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
                } else {
                    // Address given in memory (m16 or m32)

                    // Determine IP displacement of memory location (if any)
                    memoryReferenceDisplacement = cpu.decodeMM(addressByte);

                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Get new IP from memory
                    newIP = cpu.getWordFromMemorySegment(addressByte,
                            memoryReferenceLocation);
                }

                // Push current instruction pointer onto stack
                cpu.setWordToStack(cpu.ip);

                // Assign new IP to registers
                cpu.ip[CPU.REGISTER_LOW] = newIP[CPU.REGISTER_LOW];
                cpu.ip[CPU.REGISTER_HIGH] = newIP[CPU.REGISTER_HIGH];
                break; // CALLN Ev

            case 3: // CALLF Ep
                // Call far is absolute indirect, address given in memory
                // (IP:CS=m16:16 or m16:32)

                if (((addressByte >> 6) & 0x03) == 3) {
                    // Address given in register
                    // TODO: is this case possible?
                    logger
                            .log(
                                    Level.WARNING,
                                    "["
                                            + cpu.getType()
                                            + "]"
                                            + " Instruction INCDEC_GRP5 (0xFF): CALLF in unsupported case.");
                } else {
                    // Address given in memory (m16:16 or m16:32)
                    // First retrieve IP, then CS
                    // Note: 32-bit addresses are not considered here

                    // Determine IP displacement of memory location (if any)
                    memoryReferenceDisplacement = cpu.decodeMM(addressByte);

                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Get new IP from memory
                    newIP = cpu.getWordFromMemorySegment(addressByte,
                            memoryReferenceLocation);

                    // Determine CS displacement of memory location (increment disp
                    // with 2, because of 16-bit IP)
                    temp = Util.addWords(new byte[]{0x00, 0x02},
                            memoryReferenceDisplacement, 0);
                    System.arraycopy(temp, 0, memoryReferenceDisplacement, 0,
                            temp.length);

                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Get new CS from memory
                    newCS = cpu.getWordFromMemorySegment(addressByte,
                            memoryReferenceLocation);

                    // Push current code segment and instruction pointer onto stack
                    cpu.setWordToStack(cpu.cs);
                    cpu.setWordToStack(cpu.ip);

                    // Assign new CS and IP to registers
                    cpu.cs[CPU.REGISTER_SEGMENT_LOW] = newCS[CPU.REGISTER_LOW];
                    cpu.cs[CPU.REGISTER_SEGMENT_HIGH] = newCS[CPU.REGISTER_HIGH];
                    cpu.ip[CPU.REGISTER_LOW] = newIP[CPU.REGISTER_LOW];
                    cpu.ip[CPU.REGISTER_HIGH] = newIP[CPU.REGISTER_HIGH];
                }
                break;

            case 4: // JMPN Ev
                // Jump near, absolute indirect, address given in r/m16
                if (((addressByte >> 6) & 0x03) == 3) {
                    // Address given in register (r16)
                    // Determine register from addressbyte, ANDing it with 0000 0111
                    newIP = cpu.decodeRegister(operandWordSize, addressByte & 0x07);

                    // Assign new IP to registers
                    cpu.ip[CPU.REGISTER_LOW] = newIP[CPU.REGISTER_LOW];
                    cpu.ip[CPU.REGISTER_HIGH] = newIP[CPU.REGISTER_HIGH];
                } else {
                    // Address given in memory (m16)
                    // Determine displacement of memory location (if any)
                    memoryReferenceDisplacement = cpu.decodeMM(addressByte);

                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Get new IP from memory
                    newIP = cpu.getWordFromMemorySegment(addressByte,
                            memoryReferenceLocation);

                    // Assign new IP to registers
                    cpu.ip[CPU.REGISTER_LOW] = newIP[CPU.REGISTER_LOW];
                    cpu.ip[CPU.REGISTER_HIGH] = newIP[CPU.REGISTER_HIGH];
                }
                break; // JMPN Ev

            case 5: // JMPF Ep
                // Jump far, absolute indirect, address given in m16:16
                if (((addressByte >> 6) & 0x03) == 3) {
                    // Address given in register
                    // TODO: is this case possible?
                    logger
                            .log(
                                    Level.WARNING,
                                    "["
                                            + cpu.getType()
                                            + "]"
                                            + " Instruction INCDEC_GRP5 (0xFF): JMPF in unsupported case.");
                } else {
                    // Address given in memory (m16:16 or m16:32)
                    // First retrieve IP, then CS
                    // Note: 32-bit addresses are not considered here

                    // Determine IP displacement of memory location (if any)
                    memoryReferenceDisplacement = cpu.decodeMM(addressByte);

                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Get new IP from memory
                    newIP = cpu.getWordFromMemorySegment(addressByte,
                            memoryReferenceLocation);

                    // Determine CS displacement of memory location (increment disp
                    // with 2, because of 16-bit IP)
                    temp = Util.addWords(new byte[]{0x00, 0x02},
                            memoryReferenceDisplacement, 0);
                    System.arraycopy(temp, 0, memoryReferenceDisplacement, 0,
                            temp.length);

                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                            memoryReferenceDisplacement);

                    // Get new CS from memory
                    newCS = cpu.getWordFromMemorySegment(addressByte,
                            memoryReferenceLocation);

                    // Assign new CS and IP to registers
                    cpu.cs[CPU.REGISTER_SEGMENT_LOW] = newCS[CPU.REGISTER_LOW];
                    cpu.cs[CPU.REGISTER_SEGMENT_HIGH] = newCS[CPU.REGISTER_HIGH];
                    cpu.ip[CPU.REGISTER_LOW] = newIP[CPU.REGISTER_LOW];
                    cpu.ip[CPU.REGISTER_HIGH] = newIP[CPU.REGISTER_HIGH];
                }
                break; // JMPF Ep

            case 6: // PUSH Ev: push word or doubleword onto the stack
                // Flags: no flags affected.
                // FIXME: take notice of the stack address size (16 / 32) and
                // doubleWord

                // Determine IP displacement of memory location (if any)
                memoryReferenceDisplacement = cpu.decodeMM(addressByte);

                // Determine memory location
                memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                        memoryReferenceDisplacement);

                // Retrieve source value from memory and push it onto stack (also
                // takes care of decrementing SP)
                cpu.setWordToStack(cpu.getWordFromMemorySegment(addressByte,
                        memoryReferenceLocation));
                break; // PUSH Ev

            default:
                logger
                        .log(
                                Level.SEVERE,
                                "["
                                        + cpu.getType()
                                        + "]"
                                        + " Instruction INCDEC_GRP5 (0xFF): no group instruction match.");
                break;
        }
    }
}
