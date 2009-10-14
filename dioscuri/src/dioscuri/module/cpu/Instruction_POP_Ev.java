/*
 * $Revision: 159 $ $Date: 2009-08-17 12:52:56 +0000 (ma, 17 aug 2009) $ $Author: blohman $
 * 
 * Copyright (C) 2007  National Library of the Netherlands, Nationaal Archief of the Netherlands
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information about this project, visit
 * http://dioscuri.sourceforge.net/
 * or contact us via email:
 * jrvanderhoeven at users.sourceforge.net
 * blohman at users.sourceforge.net
 * 
 * Developed by:
 * Nationaal Archief               <www.nationaalarchief.nl>
 * Koninklijke Bibliotheek         <www.kb.nl>
 * Tessella Support Services plc   <www.tessella.com>
 *
 * Project Title: DIOSCURI
 *
 */

package dioscuri.module.cpu;

	/**
	 * Intel opcode 8F<BR>
	 * Pop word or double word from stack SP:SS into current segment at given offset (mem/reg).<BR>
     * NOTE: Stack is incr. automatically
     * NOTE: The POP instruction cannot pop a value into the CS register. To load the CS register from the stack, use the RET instruction.
     * Flags modified: none
	 */
public class Instruction_POP_Ev implements Instruction {

	// Attributes
	private CPU cpu;
    private byte addressByte;
    private boolean operandWordSize;
    private byte[] memoryReferenceDisplacement;
    private byte[] offset;
	
	
	// Constructors
	/**
	 * Class constructor 
	 * 
	 */
	public Instruction_POP_Ev()	{}
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_POP_Ev(CPU processor)
	{
		this();
        
		// Create reference to cpu class
		cpu = processor;
        
        // Initialise all variables
        addressByte = 0x00;
        operandWordSize = true;
        memoryReferenceDisplacement = new byte[2];
        offset = new byte[2];
	}

	
	// Methods
	
	/**
	 * This pops the word or doubleword at stack top SS:SP into current segment at given offset (reg/mem)
     * NOTE: Stack is incr. automatically
     * NOTE: The POP instruction cannot pop a value into the CS register. To load the CS register from the stack, use the RET instruction.
	 */
	public void execute()
	{
		// Get word SS:SP and assign to DI
        if (cpu.doubleWord)
        {
            // 32-bit
            addressByte = cpu.getByteFromCode();
            System.out.println("POP_Ev: 32-bits not supported");
        }
        else
        {
            // 16-bit
            // Get addresByte
            addressByte = cpu.getByteFromCode();

            // Determine displacement of memory location (if any) 
            memoryReferenceDisplacement = cpu.decodeMM(addressByte);
            
            // Determine if offset is given in register or memory (by mm bits of addressbyte)
            if (((addressByte >> 6) & 0x03) == 3)
            {
                // reg offset
                // Determine register from addressbyte, ANDing it with 0000 0111
                offset = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
            }
            else
            {
                // mem offset
                // Determine memory location
                offset = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);
            }

            // Pop word from stack and store it in segment + offset
            cpu.setWordInMemorySegment(addressByte, offset, cpu.getWordFromStack());
        }
	}
}
