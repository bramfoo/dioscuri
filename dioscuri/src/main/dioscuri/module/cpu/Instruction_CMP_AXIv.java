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
 * Intel opcode 3D<BR>
 * Comparison of immediate word (SUB) with AX.<BR>
 * Does not update any registers, only sets appropriate flags.<BR>
 * Flags modified: OF, SF, ZF, AF, PF, CF
 */
public class Instruction_CMP_AXIv implements Instruction {

    // Attributes
    private CPU cpu;
    byte[] immediateWord = new byte[2];
    byte[] immediateDoubleWord = new byte[2];
    byte[] resultWord = new byte[2];
    byte[] resultDoubleWord = new byte[2];
    int tempCF = 0;
    boolean tempOF = false;

    // Constructors

    /**
     * Class constructor
     */
    public Instruction_CMP_AXIv() {
    }

    /**
     * Class constructor specifying processor reference
     *
     * @param processor Reference to CPU class
     */
    public Instruction_CMP_AXIv(CPU processor) {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Comparison of immediate word (SUB) with AX.<BR>
     * Does not update any registers, only sets appropriate flags.
     */
    public void execute() {
        if (cpu.doubleWord) {
            // 32-bit
            immediateWord = cpu.getWordFromCode();
            immediateDoubleWord = cpu.getWordFromCode();

            // Subtract lower 16 bits
            resultWord = Util.subtractWords(cpu.ax, immediateWord, 0);

            tempCF = Util.test_CF_SUB(cpu.ax, immediateWord, 0) ? 1 : 0;

            // Subtract higher 16 bits
            resultDoubleWord = Util.subtractWords(cpu.eax, immediateDoubleWord,
                    tempCF);

            // Test AF
            cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_SUB(
                    cpu.ax[CPU.REGISTER_GENERAL_LOW],
                    resultWord[CPU.REGISTER_GENERAL_LOW]);
            // Test CF
            cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_SUB(cpu.eax,
                    immediateDoubleWord, tempCF);
            // Test OF
            cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB(cpu.eax,
                    immediateDoubleWord, resultDoubleWord, tempCF);
            // Test ZF
            cpu.flags[CPU.REGISTER_FLAGS_ZF] = resultDoubleWord[CPU.REGISTER_GENERAL_HIGH] == 0x00
                    && resultDoubleWord[CPU.REGISTER_GENERAL_LOW] == 0x00
                    && resultWord[CPU.REGISTER_GENERAL_HIGH] == 0x00
                    && resultWord[CPU.REGISTER_GENERAL_LOW] == 0x00 ? true
                    : false;
            // Test SF (set when MSB is 1, occurs when tempResult >= 0x8000)
            cpu.flags[CPU.REGISTER_FLAGS_SF] = resultDoubleWord[CPU.REGISTER_GENERAL_HIGH] < 0 ? true
                    : false;
            // Set PF, only applies to tempResult[LOW]
            cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                    .checkParityOfByte(resultWord[CPU.REGISTER_GENERAL_LOW]);
        } else {
            // 16-bit
            immediateWord = cpu.getWordFromCode();

            // Subtract
            resultWord = Util.subtractWords(cpu.ax, immediateWord, 0);

            // Test AF
            cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_SUB(
                    cpu.ax[CPU.REGISTER_GENERAL_LOW],
                    resultWord[CPU.REGISTER_GENERAL_LOW]);
            // Test CF
            cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_SUB(cpu.ax,
                    immediateWord, 0);
            // Test OF
            cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB(cpu.ax,
                    immediateWord, resultWord, 0);
            // Test ZF
            cpu.flags[CPU.REGISTER_FLAGS_ZF] = resultWord[CPU.REGISTER_GENERAL_HIGH] == 0x00
                    && resultWord[CPU.REGISTER_GENERAL_LOW] == 0x00 ? true
                    : false;
            // Test SF (set when MSB is 1, occurs when tempResult >= 0x8000)
            cpu.flags[CPU.REGISTER_FLAGS_SF] = resultWord[CPU.REGISTER_GENERAL_HIGH] < 0 ? true
                    : false;
            // Set PF, only applies to tempResult[LOW]
            cpu.flags[CPU.REGISTER_FLAGS_PF] = Util
                    .checkParityOfByte(resultWord[CPU.REGISTER_GENERAL_LOW]);
        }
    }
}
