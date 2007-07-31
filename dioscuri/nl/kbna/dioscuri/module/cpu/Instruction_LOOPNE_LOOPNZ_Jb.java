/* $Revision: 1.2 $ $Date: 2007-07-31 09:39:31 $ $Author: blohman $
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
 * Intel opcode E0<BR>
 * Loop while CX is not zero and ZF == 0, performing short jump indicated by immediate signed byte.<BR>
 * Displacement is relative to next instruction.<BR>
 * Flags modified: none
 */
public class Instruction_LOOPNE_LOOPNZ_Jb implements Instruction
{

    // Attributes
    private CPU cpu;
    byte displacement;
    byte[] decrement = new byte[]{0x00, 0x01};  // Decrement by one

    // Constructors
    /**
     * Class constructor
     */
    public Instruction_LOOPNE_LOOPNZ_Jb()
    {
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_LOOPNE_LOOPNZ_Jb(CPU processor)
    {
        // this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Loop while CX is not zero and ZF == 0, performing short jump indicated by immediate signed byte
     */
    public void execute()
    {
        // Get displacement byte (immediate)
        // This byte is interpreted signed, so cast to Java byte
        displacement = cpu.getByteFromCode();

        // Decrement the CX register
        cpu.cx = Util.subtractWords(cpu.cx, decrement, 0);

        // Test LOOP condition, jump if CX is not zero and ZF == 0
        if (cpu.flags[CPU.REGISTER_FLAGS_ZF] == false && (cpu.cx[CPU.REGISTER_GENERAL_LOW] != 0x00 || cpu.cx[CPU.REGISTER_GENERAL_HIGH] != 0x00))
        {
            // Jump is relative to _next_ instruction, but by the time we change
            // the IP, it has already been incremented twice, so no extra arithmetic necessary
            cpu.ip = Util.addWords(cpu.ip, new byte[]{Util.signExtend(displacement), displacement}, 0);
        }
    }
}
