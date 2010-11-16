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
 * Intel opcode D5<BR>
 * ASCII adjust AX before division.<BR>
 * Adjust two unpacked BCD digits so a division operation on result yields
 * correct unpacked BCD value<BR>
 * Flags modified: SF, ZF, PF
 */
public class Instruction_AAD_Ib implements Instruction {

    // Attributes
    private CPU cpu;
    byte base;
    int tempResult;

    // Constructors

    /**
     * Class constructor
     */
    public Instruction_AAD_Ib() {
    }

    /**
     * Class constructor specifying processor reference
     *
     * @param processor Reference to CPU class
     */
    public Instruction_AAD_Ib(CPU processor) {
        // Create reference to cpu class
        cpu = processor;

        base = 0;
        tempResult = 0;
    }

    // Methods

    /**
     * Adjust two unpacked BCD digits so a division operation on result yields
     * correct unpacked BCD value.<BR>
     * Set AL register to (AL + (10 * AH)), clear AH register. AX is then equal
     * to binary equivalent of original unpacked two-digit (base 10) number.<BR>
     * The generalized version adjusts two unpacked digits of any number base
     * (defined by imm8); for example, 08H for octal, 0AH for decimal, or 0CH
     * for base 12.
     */
    public void execute() {
        // Get immediate byte for base
        base = cpu.getByteFromCode();

        // AL = (AL + (imm * AH))
        tempResult = ((int) cpu.ax[CPU.REGISTER_GENERAL_LOW] & 0xFF)
                + (((int) cpu.ax[CPU.REGISTER_GENERAL_HIGH] & 0xFF) * base);

        cpu.ax[CPU.REGISTER_GENERAL_LOW] = (byte) (tempResult & 0xFF);
        cpu.ax[CPU.REGISTER_GENERAL_HIGH] = 0x00;

        // Set appropriate flags; follow Bochs' example of undefined flags
        // OF is undefined
        cpu.flags[CPU.REGISTER_FLAGS_OF] = false;
        // AF is undefined
        cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
        // CF is undefined
        cpu.flags[CPU.REGISTER_FLAGS_CF] = false;
        // Set ZF
        cpu.flags[CPU.REGISTER_FLAGS_ZF] = cpu.ax[CPU.REGISTER_GENERAL_LOW] == 0 ? true
                : false;
        // Set SF on particular byte of AX (set when MSB is 1, occurs when
        // destReg >= 0x80)
        cpu.flags[CPU.REGISTER_FLAGS_SF] = cpu.ax[CPU.REGISTER_GENERAL_LOW] < 0 ? true
                : false;
        // Set PF on particular byte of AX
        cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                .checkParityOfByte(cpu.ax[CPU.REGISTER_GENERAL_LOW]);
    }
}
