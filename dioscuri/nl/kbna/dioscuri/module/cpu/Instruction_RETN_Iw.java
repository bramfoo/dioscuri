/* $Revision: 1.2 $ $Date: 2007-07-31 14:27:03 $ $Author: blohman $
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
	 * Intel opcode C2<BR>
	 * Near (intrasegment) return to calling procedure.<BR>
	 * Transfer control to return address located at top stack and pop immediate from mem.<BR>
	 * Flags modified: none
	 */
public class Instruction_RETN_Iw implements Instruction {

	// Attributes
	private CPU cpu;
	
    byte[] displacement;

    
	// Constructors
	/**
	 * Class constructor
	 * 
	 */
	public Instruction_RETN_Iw()	{}
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_RETN_Iw(CPU processor)
	{
		// Create reference to cpu class
		cpu = processor;
        
        displacement = new byte[2];
	}

	
	// Methods

    /**
     * Transfer control to return address located at top stack
     */
    public void execute()
    {
        // Get immediate word from code segment
        displacement = cpu.getWordFromCode();

        // Pop instruction pointer (offset) from top of stack into IP register
        cpu.ip = cpu.getWordFromStack();
        
        // Add displacement to stack pointer
        cpu.sp = Util.addWords(cpu.sp, displacement, 0);
    }
}
