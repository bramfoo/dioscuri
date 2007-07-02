/* $Revision: 1.1 $ $Date: 2007-07-02 14:31:33 $ $Author: blohman $
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
 * Intel opcode 75<BR>
 * Conditional short jump not zero.<BR>
 * Displacement is relative to next instruction.<BR>
 * Flags modified: none
 */
public class Instruction_JNZ_JNE implements Instruction
{

    // Attributes
    private CPU cpu;
    byte displacement;
    int overFlowCheck;

    // Constructors
    /**
     * Class constructor
     */
    public Instruction_JNZ_JNE()
    {
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_JNZ_JNE(CPU processor)
    {
        // this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Execute conditional short jump not zero
     */
    public void execute()
    {
        // Get displacement byte (immediate)
        // Jump is relative to _next_ instruction, but by the time we change
        // the IP, it has already been incremented twice, so no extra arithmetic necessary
        displacement = cpu.getByteFromCode();

        // Jump if zero flag not set, otherwise skip instruction
        // IP has already been properly updated when bytes were retrieved
        if (!cpu.flags[CPU.REGISTER_FLAGS_ZF])
        {
            // Need to check for possible overflow/underflow in IP[low]
            overFlowCheck = (((int) (cpu.ip[CPU.REGISTER_GENERAL_LOW])) & 0xFF) + (((int)displacement)&0xFF);
            if (overFlowCheck > 0xFF)
            {
                cpu.ip[CPU.REGISTER_GENERAL_HIGH]++;
            }
            
            // Add displacement to bytes           
            cpu.ip[CPU.REGISTER_GENERAL_LOW] += displacement;
            // Although not explicitly stated, IA-SDM2 p. 3-332 8-byte displacement is sign-extended and added. 
            cpu.ip[CPU.REGISTER_GENERAL_HIGH] += Util.signExtend(displacement);
        }
    }
}
