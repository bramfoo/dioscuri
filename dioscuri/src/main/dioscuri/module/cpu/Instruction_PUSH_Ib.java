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
 * Intel opcode 6A<BR>
 * Push immediate byte onto stack SS:SP.<BR>
 * NOTE: Since only words can be popped from the stack, the word {0x00, Ib} is
 * pushed on the stack<BR>
 * This may be a wrong asumption as nowhere is documented what MSB of word has
 * to be!! Flags modified: none
 */
public class Instruction_PUSH_Ib implements Instruction {

    // Attributes
    private CPU cpu;

    // Constructors

    /**
     * Class constructor
     */
    public Instruction_PUSH_Ib() {
    }

    /**
     * Class constructor specifying processor reference
     *
     * @param processor Reference to CPU class
     */
    public Instruction_PUSH_Ib(CPU processor) {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Pushes the immediate word onto stack top SS:SP
     */
    public void execute() {
        // Get immediate byte, complement to word with '0x00' and assign word to
        // SS:SP
        // Not sure if byte should be extended with 0x00 (is undocumented)!!
        cpu.setWordToStack(new byte[]{0x00, cpu.getByteFromCode()});
    }
}
