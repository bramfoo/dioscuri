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
	 * Intel opcode 61<BR>
	 * Pop top 8 words off stack into general purpose registers<BR> 
	 * The order is DI, SI, BP, SP, BX, DX, CX, AX<BR>
	 * The SP value popped from the stack is discarded<BR>
	 * Flags modified: none
	 */
public class Instruction_POPA implements Instruction {

	// Attributes
	private CPU cpu;
	
	// Constructors
	/**
	 * Class constructor 
	 * 
	 */
	public Instruction_POPA()	{}
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_POPA(CPU processor)
	{
		this();
		
		// Create reference to cpu class
		cpu = processor;
	}

	
	// Methods
	
	/**
	 * Pops the top 8 words stack top SS:SP into the 8 general purpose registers<BR>
	 * The order is DI, SI, BP, SP, BX, DX, CX, AX<BR>
	 * The SP value popped from the stack is discarded
	 */
	public void execute()
	{
		// Get words from stack and assign to general purpose registers   
		cpu.di = cpu.getWordFromStack();
		cpu.si = cpu.getWordFromStack();
		cpu.bp = cpu.getWordFromStack();
		// Pop, but ignore SP value. Do this by assigning BX twice
		cpu.bx = cpu.getWordFromStack();
		cpu.bx = cpu.getWordFromStack();
		cpu.dx = cpu.getWordFromStack();
		cpu.cx = cpu.getWordFromStack();
		cpu.ax = cpu.getWordFromStack();

        // Pop extra register, if 32 bit instruction
        if (cpu.doubleWord)
        {
            System.out.println("POPA: 32-bits not supported");
        }

    }
}
