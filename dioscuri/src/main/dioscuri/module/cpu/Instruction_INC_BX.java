/* $Revision: 159 $ $Date: 2009-08-17 12:52:56 +0000 (ma, 17 aug 2009) $ $Author: blohman $ 
 * 
 * Copyright (C) 2007-2009  National Library of the Netherlands, 
 *                          Nationaal Archief of the Netherlands, 
 *                          Planets
 *                          KEEP
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 *
 * For more information about this project, visit
 * http://dioscuri.sourceforge.net/
 * or contact us via email:
 *   jrvanderhoeven at users.sourceforge.net
 *   blohman at users.sourceforge.net
 *   bkiers at users.sourceforge.net
 * 
 * Developed by:
 *   Nationaal Archief               <www.nationaalarchief.nl>
 *   Koninklijke Bibliotheek         <www.kb.nl>
 *   Tessella Support Services plc   <www.tessella.com>
 *   Planets                         <www.planets-project.eu>
 *   KEEP                            <www.keep-project.eu>
 * 
 * Project Title: DIOSCURI
 */

package dioscuri.module.cpu;

/**
 * Intel opcode 43<BR>
 * Increment general register BX.<BR>
 * Flags modified: OF, SF, ZF, AF, PF
 */
public class Instruction_INC_BX implements Instruction {

    // Attributes
    private CPU cpu;
    private byte[] temp;
    private byte[] oldDest;
    private byte[] incWord;

    // Constructors

    /**
     * Class constructor
     */
    public Instruction_INC_BX() {
    }

    /**
     * Class constructor specifying processor reference
     *
     * @param processor Reference to CPU class
     */
    public Instruction_INC_BX(CPU processor) {
        this();

        // Create reference to cpu class
        cpu = processor;
        temp = new byte[2];
        oldDest = new byte[2];
        incWord = new byte[]{0x00, 0x01};
    }

    // Methods

    /**
     * Increment general register BX
     */
    public void execute() {
        // Make copy of old value
        System.arraycopy(cpu.bx, 0, oldDest, 0, cpu.bx.length);

        // Increment the BX register
        temp = Util.addWords(cpu.bx, incWord, 0);

        // Assign result to BX
        cpu.bx[CPU.REGISTER_GENERAL_HIGH] = temp[CPU.REGISTER_GENERAL_HIGH];
        cpu.bx[CPU.REGISTER_GENERAL_LOW] = temp[CPU.REGISTER_GENERAL_LOW];

        // Test AF
        cpu.flags[CPU.REGISTER_FLAGS_AF] = (oldDest[CPU.REGISTER_GENERAL_LOW] & 0x0F) == 0x0F ? true
                : false;
        // Test OF
        cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_ADD(oldDest, incWord,
                cpu.bx, 0);
        // Test ZF
        cpu.flags[CPU.REGISTER_FLAGS_ZF] = cpu.bx[CPU.REGISTER_GENERAL_HIGH] == 0x00
                && cpu.bx[CPU.REGISTER_GENERAL_LOW] == 0x00 ? true : false;
        // Test SF (set when MSB of BH is 1. In Java can check signed byte)
        cpu.flags[CPU.REGISTER_FLAGS_SF] = cpu.bx[CPU.REGISTER_GENERAL_HIGH] < 0 ? true
                : false;
        // Set PF, only applies to LSB
        cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                .checkParityOfByte(cpu.bx[CPU.REGISTER_GENERAL_LOW]);
    }
}
