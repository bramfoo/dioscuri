/*
 * $Revision: 1.1 $ $Date: 2007-07-02 14:31:38 $ $Author: blohman $
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
	 * Intel opcode 9E<BR>
	 * Load the FLAGS register with values from AH register. <BR>
	 * The FLAGS register is written as SF:ZF:0:AF:0:PF:1:CF. <BR>
	 * Flags modified: SF, ZF, AF, PF, CF.
	 */
public class Instruction_SAHF implements Instruction {

	// Attributes
	private CPU cpu;
	int flagValue;
	int flagsDec;
	
	// Constructors
	/**
	 * Class constructor 
	 * 
	 */
	public Instruction_SAHF()	{}
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_SAHF(CPU processor)
	{
		this();
		
		// Create reference to cpu class
		cpu = processor;
	}

	
	// Methods
	
	/**
	 * Move AH register  into low byte of FLAGS register.
	 */
	public void execute()
	{
		// Convert to decimal
		flagsDec = Integer.parseInt(Integer.toHexString(cpu.ax[CPU.REGISTER_GENERAL_HIGH] & 0xFF), 16);
		
		// Build FLAGS register
		for (int j = 0; j <= 7; j++)
		{
			// Ensure flags decimal value isn't negative
			if (flagsDec > 0 )
			{
				// Determine bit values via division by 2,
				// ignoring reserved bits according to Intel specs
				if (j == 1)
					flagValue = 1;
				else if (j == 3 || j == 5)
					flagValue = 0;
				else
					cpu.flags[j] = flagsDec % 2 == 0 ? false : true;
				// Halve flagsHex decimal value, setting it to next lower even number 
				flagsDec = flagsDec / 2;
			}
			else
				cpu.flags[j] = false;
		}	
	}
}
