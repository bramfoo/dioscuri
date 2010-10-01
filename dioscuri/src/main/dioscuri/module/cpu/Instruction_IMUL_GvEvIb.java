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
 * Intel opcode 6B<BR>
 * Signed multiply.<BR>
 * Multiplication uses three operands: 1=destination, 2=first source, 3=second
 * source<BR>
 * Flags modified: CF, OF. Flags SF, ZF, AF, and PF are undefined
 */
public class Instruction_IMUL_GvEvIb implements Instruction {

    // Attributes
    private CPU cpu;

    boolean operandWordSize;

    byte addressByte;
    byte[] memoryReferenceLocation;
    byte[] memoryReferenceDisplacement;
    byte[] sourceWord1;
    byte[] eSourceWord1;
    byte[] sourceWord2;
    byte[] eSourceWord2;
    byte[] destinationRegister;
    byte[] eDestinationRegister;
    long result;
    byte tempByte;

    // Constructors

    /**
     * Class constructor
     */
    public Instruction_IMUL_GvEvIb()
    {
        operandWordSize = true;

        addressByte = 0;
        memoryReferenceLocation = new byte[2];
        memoryReferenceDisplacement = new byte[2];

        sourceWord1 = new byte[2];
        eSourceWord1 = new byte[2];
        sourceWord2 = new byte[2];
        eSourceWord2 = new byte[2];
        destinationRegister = new byte[2];
        eDestinationRegister = new byte[2];
        result = 0;
        tempByte = 0;
    }

    /**
     * Class constructor specifying processor reference
     *
     * @param processor Reference to CPU class
     */
    public Instruction_IMUL_GvEvIb(CPU processor)
    {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Signed multiply.<BR>
     * Multiplication uses three operands: 1=destination, 2=first source,
     * 3=second source<BR>
     * Flags modified: CF, OF. Flags SF, ZF, AF, and PF are undefined
     */
    public void execute()
    {
        // Get addresByte
        addressByte = cpu.getByteFromCode();

        // Determine displacement of memory location (if any)
        memoryReferenceDisplacement = cpu.decodeMM(addressByte);

        // Option for 32 bits implementation
        if (cpu.doubleWord) {
            // TODO: 32-bit operand size
            System.out
                    .println("[cpu] Instruction 0x6B IMUL 32-bit not implemented");
        } else {
            // 16-bit operand size
            // Determine destination register using addressbyte. AND it with
            // 0011 1000 and right-shift 3 to get rrr bits
            destinationRegister = (cpu.decodeRegister(operandWordSize,
                    (addressByte & 0x38) >> 3));

            // Retrieve source1 reg/mem
            // Execute mul on reg or mem. Determine this from mm bits of
            // addressbyte
            if (((addressByte >> 6) & 0x03) == 3) {
                // IMUL reg, imm
                // Determine source value from addressbyte, ANDing it with 0000
                // 0111
                sourceWord1 = cpu.decodeRegister(operandWordSize,
                        addressByte & 0x07);
            } else {
                // IMUL mem, imm
                // Determine memory location
                memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                        memoryReferenceDisplacement);

                // Get word from memory
                sourceWord1 = cpu.getWordFromMemorySegment(addressByte,
                        memoryReferenceLocation);
            }

            // Retrieve source1 imm
            tempByte = cpu.getByteFromCode();
            sourceWord2 = new byte[]{Util.signExtend(tempByte), tempByte};
        }

        // Signed multiply source1 and source2
        result = ((((long) sourceWord1[CPU.REGISTER_GENERAL_HIGH]) << 8) + (((long) sourceWord1[CPU.REGISTER_GENERAL_LOW]) & 0xFF))
                * ((((long) sourceWord2[CPU.REGISTER_GENERAL_HIGH]) << 8) + (((long) sourceWord2[CPU.REGISTER_GENERAL_LOW]) & 0xFF));

        // Move answer into destination
        destinationRegister[CPU.REGISTER_GENERAL_LOW] = (byte) (result);
        destinationRegister[CPU.REGISTER_GENERAL_HIGH] = (byte) ((result) >> 8);

        // Set appropriate flags
        // Flags: OF, CF set if result has to be truncated to fit in
        // destination. SF, ZF, AF, PF are undefined.
        // Check if result has to be truncated to 16-bit word (65535 dec)
        if (result > 65535) {
            // Some information will be lost due to truncation, set OF and CF
            cpu.flags[CPU.REGISTER_FLAGS_OF] = cpu.flags[CPU.REGISTER_FLAGS_CF] = true;
        } else {
            // No information will be lost, clear OF and CF
            cpu.flags[CPU.REGISTER_FLAGS_OF] = cpu.flags[CPU.REGISTER_FLAGS_CF] = false;
        }
        // Set other flags as Bochs does
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
    }
}
