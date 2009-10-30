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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Intel opcode 60<BR>
 * Push all general purpose registers onto stack SS:SP.<BR>
 * The order of the push is AX, CX, DX, BX, SP, BP, SI, DI<BR>
 * Flags modified: none
 */
public class Instruction_PUSHA implements Instruction {

    // Attributes
    private CPU cpu;
    byte[] tempSP;

    // Logging
    private static Logger logger = Logger.getLogger("dioscuri.module.cpu");

    // Constructors
    /**
     * Class constructor
     * 
     */
    public Instruction_PUSHA() {
        tempSP = new byte[2];
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor
     *            Reference to CPU class
     */
    public Instruction_PUSHA(CPU processor) {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Pushes the words in all general purpose registers onto stack top SS:SP
     * The order of the push is AX, CX, DX, BX, SP, BP, SI, DI
     */
    public void execute() {
        // Push extra register first, if 32 bit instruction
        if (cpu.doubleWord) {
            logger.log(Level.WARNING, "[" + cpu.getType()
                    + "] Instruction PUSHA: 32-bits not supported");
        }

        // Get words in register and assign to stack
        System.arraycopy(cpu.sp, 0, tempSP, 0, cpu.sp.length);
        cpu.setWordToStack(cpu.ax);
        cpu.setWordToStack(cpu.cx);
        cpu.setWordToStack(cpu.dx);
        cpu.setWordToStack(cpu.bx);
        cpu.setWordToStack(tempSP);
        cpu.setWordToStack(cpu.bp);
        cpu.setWordToStack(cpu.si);
        cpu.setWordToStack(cpu.di);
    }
}
