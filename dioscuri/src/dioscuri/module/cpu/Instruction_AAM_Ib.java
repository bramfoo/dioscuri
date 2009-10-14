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
	 * Intel opcode D4<BR>
	 * ASCII adjust AX after multiply.<BR>
	 * Adjust multiplication result of two unpacked BCD values to create a pair of unpacked (base 10) BCD values.<BR>
	 * Flags modified: SF, ZF, PF; OF, AF, CF are undefined
	 */
public class Instruction_AAM_Ib implements Instruction {

	// Attributes
	private CPU cpu;
    byte base;
    int tempResult;
	
	// Constructors
	/**
	 * Class constructor 
	 * 
	 */
	public Instruction_AAM_Ib()	{}
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_AAM_Ib(CPU processor)
	{
		// Create reference to cpu class
		cpu = processor;
        
        base = 0;
        tempResult = 0;
	}

	
	// Methods
	
	/**
     * Adjust multiplication result of two unpacked BCD values to create a pair of unpacked (base 10) BCD values.<BR>
	 * Set AX(AH:AL) register to AL/imm8 (AH), and AL%imm8 (AL), respectively.<BR>
     * The base is defined by imm8; for example, 08H for octal, 0AH for decimal, or 0CH for base 12.
	 */
	public void execute()
	{
        // Get immediate byte for base
        base = cpu.getByteFromCode();
        tempResult = cpu.ax[CPU.REGISTER_GENERAL_LOW];
        
        // AH = AL / base
        cpu.ax[CPU.REGISTER_GENERAL_HIGH] = (byte) (tempResult / base);
        // AL = AL % base
        cpu.ax[CPU.REGISTER_GENERAL_LOW] = (byte) (tempResult % base);
        
        
        // Set appropriate flags; follow Bochs' example of undefined flags
        // OF is undefined
        cpu.flags[CPU.REGISTER_FLAGS_OF] = false;
        // AF is undefined
        cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
        // CF is undefined
        cpu.flags[CPU.REGISTER_FLAGS_CF] = false;
        // Set ZF
        cpu.flags[CPU.REGISTER_FLAGS_ZF] = cpu.ax[CPU.REGISTER_GENERAL_LOW] == 0 && cpu.ax[CPU.REGISTER_GENERAL_HIGH] == 0 ? true : false;
        // Set SF on particular byte of AX (set when MSB is 1, occurs when destReg >= 0x80)
        cpu.flags[CPU.REGISTER_FLAGS_SF] = cpu.ax[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
        // Set PF on particular byte of AX
        cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(cpu.ax[CPU.REGISTER_GENERAL_LOW]);
	}
}
