/* $Revision$ $Date$ $Author$
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

package nl.kbna.dioscuri.module.cpu;

	/**
	 * Intel opcode 41<BR>
	 * Increment general register CX.<BR>
	 * Flags modified: OF, SF, ZF, AF, PF
	 */
public class Instruction_INC_CX implements Instruction {

	// Attributes
	private CPU cpu;
    private byte[] temp;
    private byte[] oldDest;
    private byte[] incWord;
	
	
	// Constructors
	/**
	 * Class constructor
	 */
	public Instruction_INC_CX()	{}
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_INC_CX(CPU processor)
	{
		this();
		
		// Create reference to cpu class
		cpu = processor;
        temp = new byte[2];
        oldDest = new byte[2];
        incWord = new byte[] {0x00,0x01};
	}

	
	// Methods
    
    /**
     * Increment general register CX
     */
    public void execute()
    {
        // Make copy of old value
        System.arraycopy(cpu.cx, 0, oldDest, 0, cpu.cx.length);
        
        // Increment the cx register
        temp = Util.addWords(cpu.cx, incWord, 0);
        
        // Assign result to cx
        cpu.cx[CPU.REGISTER_GENERAL_HIGH] = temp[CPU.REGISTER_GENERAL_HIGH];
        cpu.cx[CPU.REGISTER_GENERAL_LOW] = temp[CPU.REGISTER_GENERAL_LOW];
        
        // Test AF
        cpu.flags[CPU.REGISTER_FLAGS_AF] = (oldDest[CPU.REGISTER_GENERAL_LOW] & 0x0F) == 0x0F ? true : false;  
        // Test OF
        cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_ADD(oldDest, incWord, cpu.cx, 0);  
        // Test ZF
        cpu.flags[CPU.REGISTER_FLAGS_ZF] = cpu.cx[CPU.REGISTER_GENERAL_HIGH] == 0x00 && cpu.cx[CPU.REGISTER_GENERAL_LOW] == 0x00 ? true : false;
        // Test SF (set when MSB of BH is 1. In Java can check signed byte)
        cpu.flags[CPU.REGISTER_FLAGS_SF] = cpu.cx[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
        // Set PF, only applies to LSB
        cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(cpu.cx[CPU.REGISTER_GENERAL_LOW]);
    }
}
