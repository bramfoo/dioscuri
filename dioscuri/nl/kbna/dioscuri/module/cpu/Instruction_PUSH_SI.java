/*
 * $Revision: 1.2 $ $Date: 2007-08-07 15:07:50 $ $Author: jrvanderhoeven $
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
	 * Intel opcode 56<BR>
	 * Push general register SI onto stack SS:SP.<BR>
	 * Flags modified: none
	 */
public class Instruction_PUSH_SI implements Instruction {

	// Attributes
	private CPU cpu;
	
	
	// Constructors
	/**
	 * Class constructor 
	 * 
	 */
	public Instruction_PUSH_SI()	{}
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_PUSH_SI(CPU processor)
	{
		this();
		
		// Create reference to cpu class
		cpu = processor;
	}

	
	// Methods
	
	/**
	 * This pushes the word in SI onto stack top SS:SP
	 */
	public void execute()
	{
        // Push extra register first, if 32 bit instruction
		// Double word will be stored as [si[LSB][MSB] esi[LSB][MSB]] because stack is counting backwards in memory
        if (cpu.doubleWord)
        {
            cpu.setWordToStack(cpu.esi);
        }      

		// Get word at SI and assign to SS:SP 
		cpu.setWordToStack(cpu.si);
	}
}
