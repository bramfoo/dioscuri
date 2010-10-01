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
 * Intel opcode 7B<BR>
 * Conditional short jump not parity / parity odd.<BR>
 * Displacement is relative to next instruction.<BR>
 * Flags modified: none
 */
public class Instruction_JNP_JPO implements Instruction {

    // Attributes
    private CPU cpu;
    byte displacement;

    // Constructors

    /**
     * Class constructor
     */
    public Instruction_JNP_JPO()
    {
    }

    /**
     * Class constructor specifying processor reference
     *
     * @param processor Reference to CPU class
     */
    public Instruction_JNP_JPO(CPU processor)
    {
        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Execute conditional short jump not parity / parity odd
     */
    public void execute()
    {
        // Get displacement byte (immediate)
        // Jump is relative to _next_ instruction, but by the time we change
        // the IP, it has already been incremented twice, so no extra arithmetic
        // necessary
        displacement = (byte) cpu.getByteFromCode();

        // Jump if parity flag NOT set, otherwise skip instruction
        // IP has already been properly updated when bytes were retrieved
        if (!cpu.flags[CPU.REGISTER_FLAGS_PF]) {
            // Although not explicitly stated, IA-SDM2 p. 3-332 8-byte
            // displacement is sign-extended and added.
            cpu.ip = Util.addWords(cpu.ip, new byte[]{
                    Util.signExtend(displacement), displacement}, 0);
        }
    }
}
