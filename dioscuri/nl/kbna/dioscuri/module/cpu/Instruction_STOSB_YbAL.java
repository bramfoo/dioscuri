/*
 * $Revision: 1.1 $ $Date: 2007-07-02 14:31:39 $ $Author: blohman $
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
	 * Intel opcode AA<BR>
	 * Copy byte from register AL to ES:DI; update DI register according to DF.<BR>
	 * Flags modified: none
	 */
public class Instruction_STOSB_YbAL implements Instruction {

	// Attributes
	private CPU cpu;
	
	// Constructors
	/**
	 * Class constructor
	 * 
	 */
	public Instruction_STOSB_YbAL()	{}
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_STOSB_YbAL(CPU processor)
	{
		this();
		
		// Create reference to cpu class
		cpu = processor;
	}

	
	// Methods
	
	/**
	 * Copy word from register AX to ES:DI; update DI register according to flag DF
	 */
	public void execute()
	{
		// Get byte at AL and assign to ES:DI; ES segment override is not allowed
		cpu.setByteToExtra(cpu.di, cpu.ax[CPU.REGISTER_GENERAL_LOW]);

        // Update DI according to DF flag
        // Check direction of flag: If DF == 0, DI is incremented; if DF == 1, DI is decremented
        if (cpu.flags[CPU.REGISTER_FLAGS_DF])
        {
            // Decrement the DI register by byte size
            cpu.di[CPU.REGISTER_GENERAL_LOW] -= 1;

            // Check for underflow in DI[low]
            // This has happened if DI[low] is -1 now
            if (cpu.di[CPU.REGISTER_GENERAL_LOW] == -1)
            {
                // Decrease DI[high]
                cpu.di[CPU.REGISTER_GENERAL_HIGH]--;
            }
        }
        else
        {
            // Increment the DI register by byte size
            cpu.di[CPU.REGISTER_GENERAL_LOW] += 1;

            // Check for overflow in DI[low]
            // This has happened if DI[low] is 0now
            if (cpu.di[CPU.REGISTER_GENERAL_LOW] == 0)
            {
                // Increase DI[high]
                cpu.di[CPU.REGISTER_GENERAL_HIGH]++;
            }
        }
	}
}
