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
 * Intel opcode 86<BR>
 * Byte-sized content exchange of memory/register (destination) and register
 * (source).<BR>
 * The addressbyte determines the source (rrr bits) and destination (sss bits).<BR>
 * Flags modified: none
 */
public class Instruction_XCHG_EbGb implements Instruction {

    // Attributes
    private CPU cpu;

    boolean operandWordSize = false;

    byte addressByte = 0;
    byte[] memoryReferenceLocation = new byte[2];
    byte[] memoryReferenceDisplacement = new byte[2];

    byte[] sourceRegister = new byte[2];
    byte[] destinationRegister = new byte[2];
    byte sourceHighLow = 0;
    byte destinationHighLow = 0;

    byte tempValue = 0;

    // Constructors

    /**
     * Class constructor
     */
    public Instruction_XCHG_EbGb() {
    }

    /**
     * Class constructor specifying processor reference
     *
     * @param processor Reference to CPU class
     */
    public Instruction_XCHG_EbGb(CPU processor) {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Byte-sized content exchange of memory/register (destination) and register
     * (source).<BR>
     * Flags modified: none
     */
    public void execute() {

        // Get addresByte
        addressByte = cpu.getByteFromCode();

        // Determine displacement of memory location (if any)
        memoryReferenceDisplacement = cpu.decodeMM(addressByte);

        // Determine source register using addressbyte. AND it with 0011 1000
        // and right-shift 3 to get rrr bits
        // Determine high/low part of register based on bit 5 (leading rrr bit)
        sourceHighLow = ((addressByte & 0x20) >> 5) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW
                : (byte) CPU.REGISTER_GENERAL_HIGH;
        sourceRegister = (cpu.decodeRegister(operandWordSize,
                (addressByte & 0x38) >> 3));

        // Execute XCHG on reg,reg or mem,reg. Determine this from mm bits of
        // addressbyte
        if (((addressByte & 0xC0) >> 6) == 3) {
            // XCHG reg,reg
            // Determine destination register from addressbyte, ANDing it with
            // 0000 0111
            destinationRegister = cpu.decodeRegister(operandWordSize,
                    addressByte & 0x07);
            // Re-determine high/low part of register based on bit 3 (leading
            // sss bit)
            destinationHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW
                    : (byte) CPU.REGISTER_GENERAL_HIGH;

            // XCHG source and destination
            tempValue = destinationRegister[destinationHighLow];
            destinationRegister[destinationHighLow] = sourceRegister[sourceHighLow];
            sourceRegister[sourceHighLow] = tempValue;
        } else {
            // XCHG mem,reg
            // Determine memory location
            memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                    memoryReferenceDisplacement);

            // XCHG source and destination
            tempValue = cpu.getByteFromMemorySegment(addressByte,
                    memoryReferenceLocation);
            cpu.setByteInMemorySegment(addressByte, memoryReferenceLocation,
                    sourceRegister[sourceHighLow]);
            sourceRegister[sourceHighLow] = tempValue;
        }
    }
}
