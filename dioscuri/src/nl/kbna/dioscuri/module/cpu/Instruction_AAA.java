/*
 * $Revision: 1.1 $ $Date: 2007-07-02 14:31:29 $ $Author: blohman $
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
     * Intel opcode 37<BR>
     * AAA- ASCII adjust after addition.<BR>
     * Adjust two unpacked BCD digits so a addition operation on result yields correct unpacked BCD value<BR>
     * Flags modified: AF, CF (OF, SF, ZF and PF are undefined.
	 */
public class Instruction_AAA implements Instruction {

	// Attributes
	private CPU cpu;

    
	// Constructors
	/**
	 * Class constructor 
	 * 
	 */
	public Instruction_AAA()	{}
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_AAA(CPU processor)
	{
		// Create reference to cpu class
		cpu = processor;
	}

	
	// Methods
	
	/**
     * Adjust two unpacked BCD digits so a addition operation on result yields correct unpacked BCD value.<BR>
	 */
	public void execute()
	{
        // Check if AL > 9 or AF = 1, adjust AX and set flags
        if (((cpu.ax[CPU.REGISTER_GENERAL_LOW] & 0x0F) > 9) || cpu.flags[CPU.REGISTER_FLAGS_AF] == true)
        {
            // Adjust AL and AH
            cpu.ax[CPU.REGISTER_GENERAL_LOW] += 6;
            cpu.ax[CPU.REGISTER_GENERAL_HIGH] ++;
            
            // Set flags AF and CF
            cpu.flags[CPU.REGISTER_FLAGS_AF] = true;
            cpu.flags[CPU.REGISTER_FLAGS_CF] = true;
        }
        else
        {
            // Clear flags AF and CF
            cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
            cpu.flags[CPU.REGISTER_FLAGS_CF] = false;
        }
        
        // Mask AL
        cpu.ax[CPU.REGISTER_GENERAL_LOW] = (byte) (cpu.ax[CPU.REGISTER_GENERAL_LOW] & 0x0F);
	}
}
