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
 * Intel opcode 87<BR>
 * Word-sized content exchange of memory/register (destination) and register
 * (source).<BR>
 * The addressbyte determines the source (rrr bits) and destination (sss bits).<BR>
 * Flags modified: none
 */
public class Instruction_XCHG_EvGv implements Instruction {

    // Attributes
    private CPU cpu;

    boolean operandWordSize = true;

    byte addressByte = 0;
    byte[] memoryReferenceLocation = new byte[2];
    byte[] memoryReferenceDisplacement = new byte[2];

    byte[] sourceRegister = new byte[2];
    byte[] destinationRegister = new byte[2];

    byte[] tempValue = new byte[2];

    // Constructors

    /**
     * Class constructor
     */
    public Instruction_XCHG_EvGv()
    {
    }

    /**
     * Class constructor specifying processor reference
     *
     * @param processor Reference to CPU class
     */
    public Instruction_XCHG_EvGv(CPU processor)
    {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Word-sized content exchange of memory/register (destination) and register
     * (source).<BR>
     * Flags modified: none
     */
    public void execute()
    {
        // Reset pointers
        sourceRegister = new byte[2];
        destinationRegister = new byte[2];
        tempValue = new byte[2];

        // Get addresByte
        addressByte = cpu.getByteFromCode();

        // Determine displacement of memory location (if any)
        memoryReferenceDisplacement = cpu.decodeMM(addressByte);

        // Determine source register using addressbyte. AND it with 0011 1000
        // and right-shift 3 to get rrr bits
        // High / low part of register is also determined here (might be re-used
        // later, so do not depend on it anymore)
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

            // XCHG source and destination
            System.arraycopy(destinationRegister, 0, tempValue, 0,
                    destinationRegister.length);
            System.arraycopy(sourceRegister, 0, destinationRegister, 0,
                    sourceRegister.length);
            System.arraycopy(tempValue, 0, sourceRegister, 0, tempValue.length);
        } else {
            // XCHG mem,reg
            // Determine memory location
            memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte,
                    memoryReferenceDisplacement);

            // XCHG source and destination
            tempValue = cpu.getWordFromMemorySegment(addressByte,
                    memoryReferenceLocation);
            cpu.setWordInMemorySegment(addressByte, memoryReferenceLocation,
                    sourceRegister);
            System.arraycopy(tempValue, 0, sourceRegister, 0, tempValue.length);
        }
    }
}
