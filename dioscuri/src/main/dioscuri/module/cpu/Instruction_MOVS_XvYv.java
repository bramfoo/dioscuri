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
 * Intel opcode A5<BR>
 * Move string word at address DS:(E)SI to address ES:(E)DI.<BR>
 * After move, contents of SI and DI are incremented or decremented based on DF
 * flag:<BR>
 * Byte: +/- 1, word: +/- 2, doubleword: +/-4.<BR>
 * Flags modified: none
 */
public class Instruction_MOVS_XvYv implements Instruction {

    // Attributes
    private CPU cpu;

    boolean operandWordSize;

    byte[] source1;
    byte[] word0x0002;
    byte[] word0x0004;
    byte[] temp;

    // Constructors
    /**
     * Class constructor
     */
    public Instruction_MOVS_XvYv() {
        operandWordSize = false;

        source1 = new byte[2];
        // Set transition that holds the amount si and di should be altered
        // (word = 2)
        word0x0002 = new byte[] { 0x00, 0x02 };
        word0x0004 = new byte[] { 0x00, 0x04 };
        temp = new byte[2];
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor
     *            Reference to CPU class
     */
    public Instruction_MOVS_XvYv(CPU processor) {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Move string word at address DS:(E)SI to address ES:(E)DI and
     * increment/decrement both depending on DF flag.<BR>
     * Flags modified: none
     */
    public void execute() {
        if (cpu.doubleWord) {
            // 32-bit
            // Move doubleword at DS:eSI to ES:eDI; DS segment override is
            // allowed, ES segment isn't.
            // Note: the addressbyte (0) passed here is a value chosen so if the
            // segmentOverride fails
            // (which it shouldn't!), the DS segment is still chosen.
            /*
             * source1 = cpu.getWordFromMemorySegment((byte) 0, cpu.si);
             * 
             * cpu.setWordToExtra(cpu.di, source1);
             * 
             * // Increment or decrement SI and DI depending on DF flag if
             * (cpu.flags[CPU.REGISTER_FLAGS_DF] == true) { // Decrement
             * registers temp = Util.subtractWords(cpu.si, word0x0004, 0);
             * System.arraycopy(temp, 0, cpu.si, 0, temp.length); temp =
             * Util.subtractWords(cpu.di, word0x0004, 0); System.arraycopy(temp,
             * 0, cpu.di, 0, temp.length); } else { // Increment registers temp
             * = Util.addWords(cpu.si, word0x0004, 0); System.arraycopy(temp, 0,
             * cpu.si, 0, temp.length); temp = Util.addWords(cpu.di, word0x0004,
             * 0); System.arraycopy(temp, 0, cpu.di, 0, temp.length); }
             */} else {
            // 16-bit
            // Move word at DS:SI to ES:DI; DS segment override is allowed, ES
            // segment isn't.
            // Note: the addressbyte (0) passed here is a value chosen so if the
            // segmentOverride fails
            // (which it shouldn't!), the DS segment is still chosen.
            source1 = cpu.getWordFromMemorySegment((byte) 0, cpu.si);

            cpu.setWordToExtra(cpu.di, source1);

            // Increment or decrement SI and DI depending on DF flag
            if (cpu.flags[CPU.REGISTER_FLAGS_DF] == true) {
                // Decrement registers
                temp = Util.subtractWords(cpu.si, word0x0002, 0);
                System.arraycopy(temp, 0, cpu.si, 0, temp.length);
                temp = Util.subtractWords(cpu.di, word0x0002, 0);
                System.arraycopy(temp, 0, cpu.di, 0, temp.length);
            } else {
                // Increment registers
                temp = Util.addWords(cpu.si, word0x0002, 0);
                System.arraycopy(temp, 0, cpu.si, 0, temp.length);
                temp = Util.addWords(cpu.di, word0x0002, 0);
                System.arraycopy(temp, 0, cpu.di, 0, temp.length);
            }
        }
    }
}
