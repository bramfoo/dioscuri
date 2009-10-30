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
 * Intel opcode 8A<BR>
 * Byte-sized copy of register (destination) from memory/register (source).<BR>
 * The addressbyte determines the source (sss bits) and destination (rrr bits).<BR>
 * Flags modified: none
 */
public class Instruction_MOV_GbEb implements Instruction {

    // Attributes
    private CPU cpu;

    boolean operandWordSize = false;

    byte addressByte = 0;
    byte[] memoryReferenceLocation = new byte[2];
    byte[] memoryReferenceDisplacement = new byte[2];

    byte sourceValue = 0;
    byte[] destinationRegister = new byte[2];
    byte registerHighLow = 0;

    // Constructors
    /**
     * Class constructor
     */
    public Instruction_MOV_GbEb() {
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor
     *            Reference to CPU class
     */
    public Instruction_MOV_GbEb(CPU processor) {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Byte-sized copy of register (destination) from memory/register (source).<BR>
     * Flags modified: none
     */
    public void execute() {
        // Get addresByte
        addressByte = cpu.getByteFromCode();

        // Determine displacement of memory location (if any)
        memoryReferenceDisplacement = cpu.decodeMM(addressByte);

        // Execute MOV on reg,reg or mem,reg. Determine this from mm bits of
        // addressbyte
        if (((addressByte >> 6) & 0x03) == 3) {
            // MOV reg,reg
            // Determine source value from addressbyte, ANDing it with 0000 0111
            // to get sss bits
            // Determine high/low part of register based on bit 2 (leading sss
            // bit)
            registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW
                    : (byte) CPU.REGISTER_GENERAL_HIGH;
            sourceValue = cpu.decodeRegister(operandWordSize,
                    addressByte & 0x07)[registerHighLow];
        } else {
            // MOV mem,reg
            // Determine memory location
            memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                    memoryReferenceDisplacement);

            // Retrieve source value from memory indicated by reference location
            sourceValue = cpu.getByteFromMemorySegment(addressByte,
                    memoryReferenceLocation);
        }

        // Determine destination register using addressbyte, ANDing it with 0011
        // 1000 and right-shift 3 to get rrr bits
        destinationRegister = (cpu.decodeRegister(operandWordSize,
                (addressByte & 0x38) >> 3));

        // MOV source to destination
        // Re-determine high/low part of register based on bit 5 (leading rrr
        // bit)
        registerHighLow = ((addressByte & 0x20) >> 5) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW
                : (byte) CPU.REGISTER_GENERAL_HIGH;
        destinationRegister[registerHighLow] = sourceValue;
    }
}
