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
	 * Intel opcode 63<BR>
	 * Adjust RPL Field of Segment Selector.<BR>
	 * This instruction can be used by operating systems to check the privilege level of an application in protected mode.<BR>
	 * Flags modified: ZF
	 */
public class Instruction_ARPL_EwGw implements Instruction {

	// Attributes
	private CPU cpu;

	byte addressByte;
	
	// Constructors
	/**
	 * Class constructor 
	 * 
	 */
	public Instruction_ARPL_EwGw()	{}
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_ARPL_EwGw(CPU processor)
	{
		//this();
		
		// Create reference to cpu class
		cpu = processor;

        addressByte = 0;
	}

	
	// Methods
	
	/**
	 * Adjust RPL Field of Segment Selector.
	 */
	public void execute()
	{
		// FIXME: this instruction is not implemented. Just a dummy!
		System.out.println("[" + cpu.getType() + "] Instruction ARPL_EwGw not implemented! Should only be used in protected mode!");

        // Get addresByte (eat it to keep cpu in line if instructions)
        addressByte = cpu.getByteFromCode();
	}
}
