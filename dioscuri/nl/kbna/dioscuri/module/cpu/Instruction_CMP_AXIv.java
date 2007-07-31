/*
 * $Revision: 1.2 $ $Date: 2007-07-31 14:27:05 $ $Author: blohman $
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
	 * Intel opcode 3D<BR>
	 * Comparison of immediate word (SUB) with AX.<BR>
	 * Does not update any registers, only sets appropriate flags.<BR>
	 * Flags modified: OF, SF, ZF, AF, PF, CF
	 */
public class Instruction_CMP_AXIv implements Instruction {

	// Attributes
	private CPU cpu;
	byte[] immediateWord = new byte[2];
	byte[] tempResult = new byte[2];
	
	// Constructors
	/**
	 * Class constructor
	 */
	public Instruction_CMP_AXIv()	{}
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_CMP_AXIv(CPU processor)
	{
		this();
		
		// Create reference to cpu class
		cpu = processor;
	}

	
	// Methods
	
	/**
	 * Comparison of immediate word (SUB) with AX.<BR>
	 * Does not update any registers, only sets appropriate flags.
	 */
	public void execute()
	{
		immediateWord = cpu.getWordFromCode();

		// Subtract
		tempResult = Util.subtractWords(cpu.ax, immediateWord, 0);

        // Test AF
        cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_SUB(cpu.ax[CPU.REGISTER_GENERAL_LOW], tempResult[CPU.REGISTER_GENERAL_LOW]);
		// Test CF
		cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_SUB(cpu.ax, immediateWord, 0);
		// Test OF
		cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB(cpu.ax, immediateWord, tempResult, 0);
		// Test ZF
		cpu.flags[CPU.REGISTER_FLAGS_ZF] = tempResult[CPU.REGISTER_GENERAL_HIGH] == 0x00 && tempResult[CPU.REGISTER_GENERAL_LOW] == 0x00 ? true : false;
		// Test SF (set when MSB is 1, occurs when tempResult >= 0x8000)
		cpu.flags[CPU.REGISTER_FLAGS_SF] = tempResult[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
		// Set PF, only applies to tempResult[LOW]
		cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(tempResult[CPU.REGISTER_GENERAL_LOW]);

	}
}
