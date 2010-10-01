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

/**
 * Intel opcode 31<BR>
 * Logical word-sized XOR of memory/register (destination) and register
 * (source).<BR>
 * The addressbyte determines the source (rrr bits) and destination (sss bits).<BR>
 * Flags modified: OF, SF, ZF, AF, PF, CF
 */
public class Instruction_XOR_EvGv implements Instruction {

    // Attributes
    private CPU cpu;

    boolean operandWordSize = true;

    byte addressByte = 0;
    byte[] memoryReferenceLocation = new byte[2];
    byte[] memoryReferenceDisplacement = new byte[2];

    byte[] sourceValue = new byte[2];
    byte[] eSourceValue = new byte[2];
    byte[] destinationRegister = new byte[2];
    byte[] eDestinationRegister = new byte[2];

    byte[] logicalXORResult = new byte[2];

    // Constructors

    /**
     * Class constructor
     */
    public Instruction_XOR_EvGv()
    {
    }

    /**
     * Class constructor specifying processor reference
     *
     * @param processor Reference to CPU class
     */
    public Instruction_XOR_EvGv(CPU processor)
    {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Logical XOR of memory/register (destination) and register (source).<BR>
     * OF and CF are cleared. AF is undefined.
     */
    public void execute()
    {
        // Clear appropriate flags
        cpu.flags[CPU.REGISTER_FLAGS_OF] = false;
        cpu.flags[CPU.REGISTER_FLAGS_CF] = false;
        // Intel docs state AF remains undefined, but MS-DOS debug.exe clears AF
        cpu.flags[CPU.REGISTER_FLAGS_AF] = false;

        // Get addresByte
        addressByte = cpu.getByteFromCode();

        // Determine displacement of memory location (if any)
        memoryReferenceDisplacement = cpu.decodeMM(addressByte);

        // Determine source value using addressbyte. AND it with 0011 1000 and
        // right-shift 3 to get rrr bits
        // High / low part of register is also determined here (might be re-used
        // later, so do not depend on it anymore)
        sourceValue = (cpu.decodeRegister(operandWordSize,
                (addressByte & 0x38) >> 3));
        if (cpu.doubleWord) // Dealing with extra registers
        {
            eSourceValue = (cpu.decodeExtraRegister((addressByte & 0x38) >> 3));
        }

        // Execute XOR on reg,reg or mem,reg. Determine this from mm bits of
        // addressbyte
        if (((addressByte >> 6) & 0x03) == 3) {
            // XOR reg,reg
            // Determine destination register from addressbyte, ANDing it with
            // 0000 0111
            destinationRegister = cpu.decodeRegister(operandWordSize,
                    addressByte & 0x07);

            if (cpu.doubleWord) // Dealing with extra registers
            {
                eDestinationRegister = (cpu
                        .decodeExtraRegister(addressByte & 0x07));
            }

            // XOR source and destination, storing result in destination.
            // registerHighLow is re-used here.
            destinationRegister[CPU.REGISTER_GENERAL_HIGH] ^= sourceValue[CPU.REGISTER_GENERAL_HIGH];
            destinationRegister[CPU.REGISTER_GENERAL_LOW] ^= sourceValue[CPU.REGISTER_GENERAL_LOW];

            if (cpu.doubleWord) // Dealing with extra registers
            {
                eDestinationRegister[CPU.REGISTER_GENERAL_HIGH] ^= eSourceValue[CPU.REGISTER_GENERAL_HIGH];
                eDestinationRegister[CPU.REGISTER_GENERAL_LOW] ^= eSourceValue[CPU.REGISTER_GENERAL_LOW];
            }

            if (!cpu.doubleWord) // Not dealing with extra registers
            {
                // 16 bit
                // Test ZF on particular byte of destinationRegister
                cpu.flags[CPU.REGISTER_FLAGS_ZF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] == 0x00
                        && destinationRegister[CPU.REGISTER_GENERAL_LOW] == 0x00 ? true
                        : false;
                // Test SF on particular byte of destinationRegister (set when
                // MSB is 1, occurs when destReg >= 0x80)
                cpu.flags[CPU.REGISTER_FLAGS_SF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] < 0 ? true
                        : false;
            } else {
                // 32 bit
                // Test ZF on particular byte of destinationRegister
                cpu.flags[CPU.REGISTER_FLAGS_ZF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] == 0x00
                        && destinationRegister[CPU.REGISTER_GENERAL_LOW] == 0x00
                        && eDestinationRegister[CPU.REGISTER_GENERAL_HIGH] == 0x00
                        && eDestinationRegister[CPU.REGISTER_GENERAL_LOW] == 0x00 ? true
                        : false;
                // Test SF on particular byte of destinationRegister (set when
                // MSB is 1, occurs when destReg >= 0x80)
                cpu.flags[CPU.REGISTER_FLAGS_SF] = eDestinationRegister[CPU.REGISTER_GENERAL_HIGH] < 0 ? true
                        : false;
            }
            // Set PF on lower byte of destinationRegister
            cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                    .checkParityOfByte(destinationRegister[CPU.REGISTER_GENERAL_LOW]);
        } else {
            // XOR mem,reg
            // Determine memory location
            memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                    memoryReferenceDisplacement);

            // Get byte from memory and XOR with source register
            byte[] memVal = cpu.getWordFromMemorySegment(addressByte,
                    memoryReferenceLocation);

            logicalXORResult[0] = (byte) (memVal[0] ^ sourceValue[0]);
            logicalXORResult[1] = (byte) (memVal[1] ^ sourceValue[1]);
            // Store result in memory
            cpu.setWordInMemorySegment(addressByte, memoryReferenceLocation,
                    logicalXORResult);

            // Test ZF on result
            cpu.flags[CPU.REGISTER_FLAGS_ZF] = logicalXORResult[CPU.REGISTER_GENERAL_HIGH] == 0
                    && logicalXORResult[CPU.REGISTER_GENERAL_LOW] == 0 ? true
                    : false;
            // Test SF on result (set when MSB is 1, occurs when result >= 0x80)
            cpu.flags[CPU.REGISTER_FLAGS_SF] = logicalXORResult[CPU.REGISTER_GENERAL_HIGH] < 0 ? true
                    : false;

            if (cpu.doubleWord) // Dealing with extra registers
            {
                // Increment memory location
                memoryReferenceLocation = Util.addWords(
                        memoryReferenceLocation, new byte[]{0x00, 0x02}, 0);
                memVal = cpu.getWordFromMemorySegment(addressByte,
                        memoryReferenceLocation);

                logicalXORResult[0] = (byte) (memVal[0] ^ eSourceValue[0]);
                logicalXORResult[1] = (byte) (memVal[1] ^ eSourceValue[1]);
                // Store result in memory
                cpu.setWordInMemorySegment(addressByte,
                        memoryReferenceLocation, logicalXORResult);

                // Re-adjust flags
                cpu.flags[CPU.REGISTER_FLAGS_ZF] = cpu.flags[CPU.REGISTER_FLAGS_ZF]
                        && logicalXORResult[CPU.REGISTER_GENERAL_HIGH] == 0
                        && logicalXORResult[CPU.REGISTER_GENERAL_LOW] == 0 ? true
                        : false;
                cpu.flags[CPU.REGISTER_FLAGS_SF] = logicalXORResult[CPU.REGISTER_GENERAL_HIGH] < 0 ? true
                        : false;
            }

            // Set PF on lower byte of result
            cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                    .checkParityOfByte(logicalXORResult[CPU.REGISTER_GENERAL_LOW]);

        }
    }
}
