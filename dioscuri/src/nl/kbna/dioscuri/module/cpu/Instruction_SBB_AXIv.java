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
	 * Intel opcode 1D<BR>
	 * Subtract (immediate word + CF) from AX.<BR>
	 * Flags modified: OF, SF, ZF, AF, PF, CF
	 */
public class Instruction_SBB_AXIv implements Instruction {

	// Attributes
	private CPU cpu;
	byte[] immediateWord;
    byte[] oldDest;
	int iCarryFlag;

    byte[] temp;

    
	// Constructors
	/**
	 * Class constructor
	 */
	public Instruction_SBB_AXIv()
    {
	    immediateWord = new byte[2];
        oldDest = new byte[2];
        iCarryFlag = 0;
        
        temp = new byte[2];
    }
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_SBB_AXIv(CPU processor)
	{
		this();
		
		// Create reference to cpu class
		cpu = processor;
	}

	
	// Methods
	
	/**
	 * Subtract (immediate word + CF) from AX
	 */
	public void execute()
	{
		// Determine value of carry flag before reset
		iCarryFlag = cpu.flags[CPU.REGISTER_FLAGS_CF] ? 1 : 0; 
		
		// Get word from code
		immediateWord = cpu.getWordFromCode();
		
        // Copy old value of AX
        System.arraycopy(cpu.ax, 0, oldDest, 0, cpu.ax.length);
		
		// Subtract (immediate word + CF) from register AX
		temp = Util.subtractWords(cpu.ax, immediateWord, iCarryFlag);
        System.arraycopy(temp, 0, cpu.ax, 0, temp.length);

        // Test AF
        cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_SUB(oldDest[CPU.REGISTER_GENERAL_LOW], cpu.ax[CPU.REGISTER_GENERAL_LOW]);  
        // Test CF
        cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_SUB(oldDest, immediateWord, iCarryFlag);
        // Test OF
        cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB(oldDest, immediateWord, cpu.ax, iCarryFlag);
        // Test ZF
        cpu.flags[CPU.REGISTER_FLAGS_ZF] = cpu.ax[CPU.REGISTER_GENERAL_HIGH] == 0x00 && cpu.ax[CPU.REGISTER_GENERAL_LOW] == 0x00 ? true : false;
        // Test SF (set when MSB of AL is 1. In Java can check signed byte)
        cpu.flags[CPU.REGISTER_FLAGS_SF] = cpu.ax[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
        // Set PF, only applies to AL
        cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(cpu.ax[CPU.REGISTER_GENERAL_LOW]);

	}
}
