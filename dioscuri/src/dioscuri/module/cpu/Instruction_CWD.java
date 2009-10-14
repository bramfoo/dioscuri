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
	 * Intel opcode 99<BR>
	 * Convert Word to DoubleWord.<BR>
	 * Copy the sign (bit 15) in AX into every bit of DX register<BR>
	 * Flags modified: none
	 */
public class Instruction_CWD implements Instruction {

	// Attributes
	private CPU cpu;

	// Constructors
	/**
	 * Class constructor 
	 * 
	 */
	public Instruction_CWD()	{}
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_CWD(CPU processor)
	{
		this();
		
		// Create reference to cpu class
		cpu = processor;
	}

	
	// Methods
	
	/**
	 * Copy the sign (bit 15) in AX into every bit of DX register
	 */
	public void execute()
	{
		// Change DX to 0x0000 if AH < 0x80, or 0xFFFF if AH >= 0x80
		cpu.dx[CPU.REGISTER_GENERAL_HIGH] = cpu.dx[CPU.REGISTER_GENERAL_LOW] = cpu.ax[CPU.REGISTER_GENERAL_HIGH] >= 0 ? (byte) 0x00 : (byte) 0xFF;	
	}
}
