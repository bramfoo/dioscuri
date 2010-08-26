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
 * Intel opcode C5<BR>
 * Load DS:r16 with far pointer from memory.<BR>
 * Flags modified: none
 */
public class Instruction_LDS_GvMp implements Instruction {

    // Attributes
    private CPU cpu;

    boolean operandWordSize;

    byte addressByte = 0;
    byte[] memoryReferenceLocation = new byte[2];
    byte[] memoryReferenceDisplacement = new byte[2];

    byte[] segmentSelector = new byte[2];
    byte[] segmentOffset = new byte[2];
    byte[] eSegmentOffset = new byte[2];

    byte[] destinationRegister = new byte[2];
    byte[] eDestinationRegister = new byte[2];

    // Constructors
    /**
     * Class constructor
     */
    public Instruction_LDS_GvMp() {
        operandWordSize = true;
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor
     *            Reference to CPU class
     */
    public Instruction_LDS_GvMp(CPU processor) {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Load DS:r16 or DS:r32 with far pointer from memory m16:16 or m16:32.<BR>
     * Flags modified: none
     */
    public void execute() {
        // Get addresByte
        addressByte = cpu.getByteFromCode();

        // Determine displacement of memory location (if any)
        memoryReferenceDisplacement = cpu.decodeMM(addressByte);

        // Determine memory location
        memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                memoryReferenceDisplacement);

        // Retrieve offset, selector from memory (16-bit)
        segmentOffset = cpu.getWordFromMemorySegment(addressByte,
                memoryReferenceLocation);

        // Redetermine memory location (increment displacement with 2)
        memoryReferenceDisplacement = Util.addWords(
                memoryReferenceDisplacement, new byte[] { 0x00, 0x02 }, 0);
        memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                memoryReferenceDisplacement);

        segmentSelector = cpu.getWordFromMemorySegment(addressByte,
                memoryReferenceLocation);

        // Determine destination register using addressbyte. AND it with 0011
        // 1000 and right-shift 3 to get rrr bits
        destinationRegister = (cpu.decodeRegister(operandWordSize,
                (addressByte & 0x38) >> 3));

        // Store segment selector in DS
        cpu.ds[CPU.REGISTER_SEGMENT_LOW] = segmentSelector[CPU.REGISTER_GENERAL_LOW];
        cpu.ds[CPU.REGISTER_SEGMENT_HIGH] = segmentSelector[CPU.REGISTER_GENERAL_HIGH];

        // Store segment offset in destination register
        destinationRegister[CPU.REGISTER_GENERAL_LOW] = segmentOffset[CPU.REGISTER_GENERAL_LOW];
        destinationRegister[CPU.REGISTER_GENERAL_HIGH] = segmentOffset[CPU.REGISTER_GENERAL_HIGH];

        if (cpu.doubleWord) // 32 bit registers
        {
            // Redetermine memory location (increment displacement with 4)
            Util.addWords(memoryReferenceDisplacement,
                    new byte[] { 0x00, 0x04 }, 0);
            memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                    memoryReferenceDisplacement);

            // Retrieve offset (32-bit)
            eSegmentOffset = cpu.getWordFromMemorySegment(addressByte,
                    memoryReferenceLocation);

            // Retrieve destination register (32-bit)
            eDestinationRegister = cpu.decodeExtraRegister(addressByte & 0x07);

            // Store segment offset in destination register (32-bit)
            eDestinationRegister[CPU.REGISTER_GENERAL_LOW] = eSegmentOffset[CPU.REGISTER_GENERAL_LOW];
            eDestinationRegister[CPU.REGISTER_GENERAL_HIGH] = eSegmentOffset[CPU.REGISTER_GENERAL_HIGH];
        }
    }
}
