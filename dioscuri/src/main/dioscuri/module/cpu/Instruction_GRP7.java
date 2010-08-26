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

import dioscuri.exception.CPUInstructionException;

/**
 * Intel opcode 0F 01<BR>
 * Group 7 opcode extension: SGDT, SIDT, LGDT, LIDT, SMSW, LMSW, INVLPG.<BR>
 * Performs the selected instruction (indicated by bits 5, 4, 3 of the ModR/M
 * byte).<BR>
 * Flags modified: depending on instruction can be any of: OF, SF, ZF, AF, PF,
 * CF
 */
public class Instruction_GRP7 implements Instruction {

    // Attributes
    private CPU cpu;

    boolean operandWordSize;

    byte addressByte;
    byte[] memoryReferenceLocation;
    byte[] memoryReferenceDisplacement;

    byte[] sourceValue1;
    byte[] sourceValue2;
    byte[] oldValue;
    byte[] destinationRegister;
    int intermediateResult;
    byte[] word0x0001;

    int iCarryFlag;
    byte[] tempResult;
    byte[] temp;

    // Logging
    private static final Logger logger = Logger.getLogger(Instruction_GRP7.class.getName());

    // Constructors
    /**
     * Class constructor
     */
    public Instruction_GRP7() {
        operandWordSize = true;

        addressByte = 0;
        memoryReferenceLocation = new byte[2];
        memoryReferenceDisplacement = new byte[2];

        sourceValue1 = new byte[2];
        sourceValue2 = new byte[2];
        oldValue = new byte[2];
        destinationRegister = new byte[2];
        intermediateResult = 0;
        word0x0001 = new byte[] { 0x00, 0x01 };

        iCarryFlag = 0;
        tempResult = new byte[2];
        temp = new byte[2];
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor
     *            Reference to CPU class
     */
    public Instruction_GRP7(CPU processor) {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Execute any of the following Group 7 instructions: SGDT, SIDT, LGDT,
     * LIDT, SMSW, LMSW, INVLPG.<BR>
     * 
     * @throws CPUInstructionException
     */
    public void execute() throws CPUInstructionException {

        logger.log(Level.SEVERE, "Instruction_GRP7.execute()");

        // Get addresByte
        addressByte = cpu.getByteFromCode();

        // Determine displacement of memory location (if any)
        memoryReferenceDisplacement = cpu.decodeMM(addressByte);

        // Execute instruction decoded from nnn (bits 5, 4, 3 in ModR/M byte)
        switch ((addressByte & 0x38) >> 3) {
        case 0: // SGDT
            logger.log(Level.SEVERE, "  case 0"); // TODO BK <<---
            // Stores the limit (16 bits) and base (32/24 bits) values in
            // memory.
            // Limit = size of GDT, base = start of GDT
            // Flags affected: none

            // Determine memory location
            memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                    memoryReferenceDisplacement);

            // Store limit (16 bits) in memory
            cpu.setByteInMemorySegment(addressByte, memoryReferenceLocation,
                    cpu.gdtr[5]);
            Util.addWords(memoryReferenceLocation, word0x0001, 0);
            cpu.setByteInMemorySegment(addressByte, memoryReferenceLocation,
                    cpu.gdtr[4]);

            if (cpu.doubleWord) {
                System.out.println("A");
                // 32 bit: all 4 bytes of base are used
                // Get base (32 bits) from memory
                Util.addWords(memoryReferenceLocation, word0x0001, 0);
                cpu.setByteInMemorySegment(addressByte, memoryReferenceLocation, cpu.gdtr[3]);
                Util.addWords(memoryReferenceLocation, word0x0001, 0);
                cpu.setByteInMemorySegment(addressByte, memoryReferenceLocation, cpu.gdtr[2]);
                Util.addWords(memoryReferenceLocation, word0x0001, 0);
                cpu.setByteInMemorySegment(addressByte, memoryReferenceLocation, cpu.gdtr[1]);
                Util.addWords(memoryReferenceLocation, word0x0001, 0);
                cpu.setByteInMemorySegment(addressByte, memoryReferenceLocation, cpu.gdtr[0]);
            } else {
                System.out.println("B");
                // 16 bit: only 3 bytes of base are used, highest byte is set to
                // zero
                // Get base (32 bits from which high-order byte is not used)
                // from memory
                Util.addWords(memoryReferenceLocation, word0x0001, 0);
                cpu.setByteInMemorySegment(addressByte, memoryReferenceLocation, cpu.gdtr[3]);
                Util.addWords(memoryReferenceLocation, word0x0001, 0);
                cpu.setByteInMemorySegment(addressByte, memoryReferenceLocation, cpu.gdtr[2]);
                Util.addWords(memoryReferenceLocation, word0x0001, 0);
                cpu.setByteInMemorySegment(addressByte, memoryReferenceLocation, cpu.gdtr[1]);
                Util.addWords(memoryReferenceLocation, word0x0001, 0);
                cpu.setByteInMemorySegment(addressByte, memoryReferenceLocation, (byte) 0x00);
            }
            break;

        case 1: // SIDT
            logger.log(Level.SEVERE, "  case 1");
            throw new CPUInstructionException(
                    "Group 7 (0x0F01) instruction SIDT not implemented.");

        case 2: // LGDT - Load Global Descriptor Table Register
            logger.log(Level.SEVERE, "  case 2");
            // Retrieves the limit (16 bits) and base (32/24 bits) values from
            // memory.
            // Limit = size of GDT, base = start of GDT
            // Flags affected: none

            // Determine memory location
            memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                    memoryReferenceDisplacement);

            // Get limit (16 bits) from memory
            cpu.gdtr[5] = cpu.getByteFromMemorySegment(addressByte,
                    memoryReferenceLocation);
            Util.addWords(memoryReferenceLocation, word0x0001, 0);
            cpu.gdtr[4] = cpu.getByteFromMemorySegment(addressByte,
                    memoryReferenceLocation);

            if (cpu.doubleWord) {
                // 32 bit: all 4 bytes of base are used
                // Get base (32 bits) from memory
                Util.addWords(memoryReferenceLocation, word0x0001, 0);
                cpu.gdtr[3] = cpu.getByteFromMemorySegment(addressByte,
                        memoryReferenceLocation);
                Util.addWords(memoryReferenceLocation, word0x0001, 0);
                cpu.gdtr[2] = cpu.getByteFromMemorySegment(addressByte,
                        memoryReferenceLocation);
                Util.addWords(memoryReferenceLocation, word0x0001, 0);
                cpu.gdtr[1] = cpu.getByteFromMemorySegment(addressByte,
                        memoryReferenceLocation);
                Util.addWords(memoryReferenceLocation, word0x0001, 0);
                cpu.gdtr[0] = cpu.getByteFromMemorySegment(addressByte,
                        memoryReferenceLocation);
            } else {
                // 16 bit: only 3 bytes of base are used, highest byte is set to
                // zero
                // Get base (32 bits from which high-order byte is not used)
                // from memory
                Util.addWords(memoryReferenceLocation, word0x0001, 0);
                cpu.gdtr[3] = cpu.getByteFromMemorySegment(addressByte,
                        memoryReferenceLocation);
                Util.addWords(memoryReferenceLocation, word0x0001, 0);
                cpu.gdtr[2] = cpu.getByteFromMemorySegment(addressByte,
                        memoryReferenceLocation);
                Util.addWords(memoryReferenceLocation, word0x0001, 0);
                cpu.gdtr[1] = cpu.getByteFromMemorySegment(addressByte,
                        memoryReferenceLocation);
                Util.addWords(memoryReferenceLocation, word0x0001, 0);
                byte notUsed = cpu.getByteFromMemorySegment(addressByte,
                        memoryReferenceLocation);
                cpu.gdtr[0] = 0x00;
            }
            break;

        case 3: // LIDT
            logger.log(Level.SEVERE, "  case 3");
            throw new CPUInstructionException(
                    "Group 7 (0x0F01) instruction LIDT not implemented.");

        case 4: // SMSW - Store Machine Status Word
            logger.log(Level.SEVERE, "  case 4");
            // Store the lower 16 bits of CR0 into reg or mem
            // Flags affected: none

            // Convert CR0 booleans to bytes (results in a 4 byte array)
            byte[] cr0Bytes = Util.booleansToBytes(cpu.cr0);

            // Store word in reg or mem. Determine this from mm bits of
            // addressbyte
            if (((addressByte >> 6) & 0x03) == 3) {
                // SMSW in reg
                // Determine destination register from addressbyte, ANDing it
                // with 0000 0111
                sourceValue1 = cpu.decodeRegister(operandWordSize,
                        addressByte & 0x07);

                // Store lower 16 bits in reg
                sourceValue1[CPU.REGISTER_GENERAL_HIGH] = cr0Bytes[2];
                sourceValue1[CPU.REGISTER_GENERAL_LOW] = cr0Bytes[3];
            } else {
                // SMSW to mem
                // Determine memory location
                memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                        memoryReferenceDisplacement);

                // Store lower 16 bits in mem
                cpu.setByteInMemorySegment(addressByte,
                        memoryReferenceLocation, cr0Bytes[3]);
                cpu.setByteInMemorySegment(addressByte,
                        memoryReferenceLocation, cr0Bytes[2]);
            }
            break;

        case 5: // Does not exist
            logger.log(Level.SEVERE, "  case 5");
            // Throw exception for illegal nnn bits
            throw new CPUInstructionException(
                    "Group 7 (0x0701/5) illegal reg bits");

        case 6: // LMSW - Load Machine Status Word
            logger.log(Level.SEVERE, "  case 6");
            // Flags affected: none
            // Note: although it seems that a word (16 bits) is loaded,
            // only the four lowest bits should be taken into account (see Intel
            // specs)

            // Load word from reg or mem. Determine this from mm bits of
            // addressbyte
            if (((addressByte >> 6) & 0x03) == 3) {
                // LMSW from reg
                // Determine destination register from addressbyte, ANDing it
                // with 0000 0111
                sourceValue1 = cpu.decodeRegister(operandWordSize,
                        addressByte & 0x07);
            } else {
                // LMSW from mem
                // Determine memory location
                memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                        memoryReferenceDisplacement);

                // Get word from memory
                sourceValue1 = cpu.getWordFromMemorySegment(addressByte,
                        memoryReferenceLocation);
            }

            // Copy lower 4 bits into CR0
            cpu.cr0[CPU.REGISTER_CR0_PE] = ((sourceValue1[CPU.REGISTER_GENERAL_LOW] & 0x01) == 0x01) ? true
                    : false;
            cpu.cr0[CPU.REGISTER_CR0_MP] = ((sourceValue1[CPU.REGISTER_GENERAL_LOW] & 0x02) == 0x02) ? true
                    : false;
            cpu.cr0[CPU.REGISTER_CR0_EM] = ((sourceValue1[CPU.REGISTER_GENERAL_LOW] & 0x04) == 0x04) ? true
                    : false;
            cpu.cr0[CPU.REGISTER_CR0_TS] = ((sourceValue1[CPU.REGISTER_GENERAL_LOW] & 0x06) == 0x06) ? true
                    : false;

            // TODO: this check only exists to notify if FPU emulation is turned
            // off
            if (cpu.cr0[CPU.REGISTER_CR0_EM] == false) {
                logger.log(Level.WARNING, "[" + cpu.getType()
                        + "] FPU emulation is turned off.");
            }
            break;

        case 7: // INVLPG
            logger.log(Level.SEVERE, "  case 7");
            throw new CPUInstructionException(
                    "Group 7 (0x0F01) instruction INVLPG not implemented.");

        default:
            logger.log(Level.SEVERE, "  default");
            // TODO Throw exception for illegal nnn bits
            throw new CPUInstructionException(
                    "Group 7 (0x0F01) instruction no case match.");
        }
    }
}
