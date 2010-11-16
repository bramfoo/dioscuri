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
 * Intel opcode 3C<BR>
 * Comparison of immediate byte (SUB) with AL.<BR>
 * Does not update any registers, only sets appropriate flags.<BR>
 * Flags modified: OF, SF, ZF, AF, PF, CF
 */
public class Instruction_CMP_ALIb implements Instruction {

    // Attributes
    private CPU cpu;

    byte immediateByte;

    byte tempResult;

    // Constructors

    /**
     * Class constructor
     */
    public Instruction_CMP_ALIb() {
    }

    /**
     * Class constructor specifying processor reference
     *
     * @param processor Reference to CPU class
     */
    public Instruction_CMP_ALIb(CPU processor) {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Comparison of immediate byte (SUB) with AL.<BR>
     * Does not update any registers, only sets appropriate flags.
     */
    public void execute() {
        immediateByte = cpu.getByteFromCode();

        // Subtract immediate byte from register AL
        tempResult = (byte) (cpu.ax[CPU.REGISTER_GENERAL_LOW] - immediateByte);

        // Test AF
        cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_SUB(
                cpu.ax[CPU.REGISTER_GENERAL_LOW], tempResult);
        // Test CF
        cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_SUB(
                cpu.ax[CPU.REGISTER_GENERAL_LOW], immediateByte, 0);
        // Test OF
        cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB(
                cpu.ax[CPU.REGISTER_GENERAL_LOW], immediateByte, tempResult, 0);
        // Test ZF, is tested againt tempResult
        cpu.flags[CPU.REGISTER_FLAGS_ZF] = tempResult == 0 ? true : false;
        // Test SF, only applies to lower byte (set when MSB is 1, occurs when
        // tempResult >= 0x80)
        cpu.flags[CPU.REGISTER_FLAGS_SF] = tempResult < 0 ? true : false;
        // Set PF, only applies to lower byte
        cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(tempResult);
    }
}
