/* $Revision: 1.2 $ $Date: 2007-07-31 09:20:32 $ $Author: blohman $
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
	 * Intel opcode 7E<BR>
	 * Conditional short jump if zero or sign != overflow.<BR>
	 * Displacement is relative to next instruction.<BR>
	 * Flags modified: none
	 */
public class Instruction_JLE_JNG implements Instruction {

	// Attributes
	private CPU cpu;
	byte displacement;
	
	// Constructors
	/**
	 * Class constructor
	 * 
	 */
	public Instruction_JLE_JNG()	{}
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_JLE_JNG(CPU processor)
	{
		//this();
		
		// Create reference to cpu class
		cpu = processor;
	}

	
	// Methods
	
	/**
	 * Execute conditional short jump if zero or sign != overflow
	 */
	public void execute()
	{
		// Get displacement byte (immediate)
		// This byte is interpreted signed, so cast to Java byte
		// Jump is relative to _next_ instruction, but by the time we change 
		// the IP, it has already been incremented twice, so no extra arithmetic necessary 		
		displacement = (byte) cpu.getByteFromCode();

		// Jump if zero flag or sign not equal to overflow flag, otherwise skip instruction
		// IP has already been properly updated when bytes were retrieved
		if ((cpu.flags[CPU.REGISTER_FLAGS_ZF]) || (cpu.flags[CPU.REGISTER_FLAGS_SF] != cpu.flags[CPU.REGISTER_FLAGS_OF]))
		{
            // Although not explicitly stated, IA-SDM2 p. 3-332 8-byte displacement is sign-extended and added. 
            cpu.ip = Util.addWords(cpu.ip, new byte[]{Util.signExtend(displacement), displacement}, 0);
		}
	}
}
