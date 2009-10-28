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
 * Intel opcode 8D<BR>
 * Load effective address computed from second operand (source) to register (destination).<BR>
 * The addressbyte determines the source (rrr bits, memory address) and destination (sss bits).<BR>
 * NOTE: The direction (d) bit in the opcode does not seems to be honored here!<BR>
 * Flags modified: none
 */
public class Instruction_LEA_GvM implements Instruction
{

    // Attributes
    private CPU cpu;

    boolean operandWordSize = true;

    byte addressByte = 0;
    
    byte[] memoryReferenceLocation = new byte[2];
    byte[] memoryReferenceDisplacement = new byte[2];
    byte[] destinationRegister = new byte[2];
    
    
    // Constructors
    /**
     * Class constructor
     */
    public Instruction_LEA_GvM()
    {
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_LEA_GvM(CPU processor)
    {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    
    // Methods

    /**
     * Load effective address computed from second operand (source) to register (destination).<BR>
     */
    public void execute()
    {
        // FIXME: take care of address-size and operand-size attributes
        
        // Get addresByte
        addressByte = cpu.getByteFromCode();

        // Determine IP displacement of memory location (if any) 
        memoryReferenceDisplacement = cpu.decodeMM(addressByte);

        // Determine memory location
        memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);

        // Determine destination resgister using addressbyte. AND it with 0011 1000 and right-shift 3 to get rrr bits
        destinationRegister = (cpu.decodeRegister(operandWordSize, (addressByte & 0x38) >> 3));

        destinationRegister[CPU.REGISTER_GENERAL_LOW] = memoryReferenceLocation[CPU.REGISTER_GENERAL_LOW];
        destinationRegister[CPU.REGISTER_GENERAL_HIGH] = memoryReferenceLocation[CPU.REGISTER_GENERAL_HIGH];
    }
}
