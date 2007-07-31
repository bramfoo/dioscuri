/*
 * $Revision: 1.2 $ $Date: 2007-07-31 14:27:03 $ $Author: blohman $
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
 * Intel opcode 04<BR>
 * Add immediate byte to AL.<BR>
 * Flags modified: OF, SF, ZF, AF, PF, CF
 */
public class Instruction_ADD_ALIb implements Instruction
{

    // Attributes
    private CPU cpu;
    byte immediateByte;
    byte oldDest;

    // Constructors
    /**
     * Class constructor
     */
    public Instruction_ADD_ALIb()
    {
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_ADD_ALIb(CPU processor)
    {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Add immediate byte to AL
     */
    public void execute()
    {
        immediateByte = cpu.getByteFromCode();

        // Copy AL start value for OF test later
        oldDest = cpu.ax[CPU.REGISTER_GENERAL_LOW];

        // Add immediate byte to register AL
        cpu.ax[CPU.REGISTER_GENERAL_LOW] += immediateByte;

        // Test AF
        cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_ADD(oldDest, cpu.ax[CPU.REGISTER_GENERAL_LOW]);
        // Test CF
        cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_ADD(oldDest, immediateByte, 0);
        // Test OF
        cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_ADD(oldDest, immediateByte, cpu.ax[CPU.REGISTER_GENERAL_LOW], 0);
        // Test ZF, only applies to AL
        cpu.flags[CPU.REGISTER_FLAGS_ZF] = cpu.ax[CPU.REGISTER_GENERAL_LOW] == 0 ? true : false;
        // Test SF (set when MSB of AL is 1. In Java can check signed byte)
        cpu.flags[CPU.REGISTER_FLAGS_SF] = cpu.ax[CPU.REGISTER_GENERAL_LOW] < 0 ? true : false;
        // Set PF, only applies to AL
        cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(cpu.ax[CPU.REGISTER_GENERAL_LOW]);

    }
}
