/*
 * $Revision$ $Date$ $Author$
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
     * Intel opcode 27<BR>
     * DAA - Decimal adjust AL after addition.<BR>
     * This instruction adjusts the sum of two packed BCD values to create a packed BCD result. The AL register is the implied source and destination operand.
     * Flags modified: AF, CF, SF, ZF and PF.
	 */
public class Instruction_DAA implements Instruction {

	// Attributes
	private CPU cpu;
    private byte oldByte;

    
	// Constructors
	/**
	 * Class constructor 
	 * 
	 */
	public Instruction_DAA()	{}
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_DAA(CPU processor)
	{
		// Create reference to cpu class
		cpu = processor;
        
        oldByte = 0;
	}

	
	// Methods
	
	/**
     * This instruction adjusts the sum of two packed BCD values to create a packed BCD result. The AL register is the implied source and destination operand.
	 */
	public void execute()
	{
        // Check if AL > 0x09 or AF = 1, adjust AL and set flags
        if (((cpu.ax[CPU.REGISTER_GENERAL_LOW] & 0x0F) > 9) || cpu.flags[CPU.REGISTER_FLAGS_AF] == true)
        {
            // Adjust AL
            oldByte = cpu.ax[CPU.REGISTER_GENERAL_LOW];
            cpu.ax[CPU.REGISTER_GENERAL_LOW] += 6;
            
            // Set flags AF and CF
            cpu.flags[CPU.REGISTER_FLAGS_CF] = (cpu.flags[CPU.REGISTER_FLAGS_CF] | Util.test_CF_ADD(oldByte, cpu.ax[CPU.REGISTER_GENERAL_LOW], 0));
            cpu.flags[CPU.REGISTER_FLAGS_AF] = true;
        }
        else
        {
            // Clear flag AF
            cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
        }
        
        // Check if AL > 0x90 or CF = 1
        if (((cpu.ax[CPU.REGISTER_GENERAL_LOW] & 0xF0) > 0x90) || cpu.flags[CPU.REGISTER_FLAGS_CF] == true)
        {
            // Adjust AL
            cpu.ax[CPU.REGISTER_GENERAL_LOW] = (byte) (cpu.ax[CPU.REGISTER_GENERAL_LOW] + 0x60);
            
            // Set flag CF
            cpu.flags[CPU.REGISTER_FLAGS_CF] = true;
        }
        else
        {
            // Clear flag CF
            cpu.flags[CPU.REGISTER_FLAGS_CF] = false;
        }
        
        // Test ZF, only applies to AL
        cpu.flags[CPU.REGISTER_FLAGS_ZF] = cpu.ax[CPU.REGISTER_GENERAL_LOW] == 0 ? true : false;
        // Test SF, only applies to AL
        cpu.flags[CPU.REGISTER_FLAGS_SF] = cpu.ax[CPU.REGISTER_GENERAL_LOW] < 0 ? true : false;
        // Set PF, only applies to AL
        cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(cpu.ax[CPU.REGISTER_GENERAL_LOW]);
	}
}
