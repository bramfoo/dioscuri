/*
 * $Revision: 1.1 $ $Date: 2007-07-02 14:31:37 $ $Author: blohman $
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
	 * Intel opcode 9D<BR>
	 * Pop word from stack into FLAGS register.<BR>
	 * Flags modified: NT, IOPL, OF, DF, IF, TF, SF, ZF, AF, PF, CF
	 */
public class Instruction_POPF implements Instruction {

	// Attributes
	private CPU cpu;
	int flagValue;
	int[] flagsHex = new int[2];
    boolean[] temp = new boolean[16];

	
	// Constructors
	/**
	 * Class constructor 
	 * 
	 */
	public Instruction_POPF()	{}
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_POPF(CPU processor)
	{
		this();
		
		// Create reference to cpu class
		cpu = processor;
	}

	
	// Methods
	
	/**
	 * Pop word from stack into FLAGS register.
	 */
	public void execute()
	{
		// Pop word from stack and convert bytes to booleans
		temp = Util.bytesToBooleans(cpu.getWordFromStack());
        
        // Store booleans in flags register
		System.arraycopy(temp, 0, cpu.flags, 0, temp.length);
        
        // But static bits of the flags register (bit 1, bit 3, bit 5, bit 15) remain constant!
        cpu.flags[1] = true;
        cpu.flags[3] = false;
        cpu.flags[5] = false;
        cpu.flags[15] = false;
	}
}
