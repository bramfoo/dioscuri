/* $Revision: 1.1 $ $Date: 2007-07-02 14:31:34 $ $Author: blohman $
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
 * Intel opcode E1<BR>
 * Loop while CX is not zero and ZF == 1, performing short jump indicated by immediate signed byte.<BR>
 * Displacement is relative to next instruction.<BR>
 * Flags modified: none
 */
public class Instruction_LOOPE_LOOPZ_Jb implements Instruction
{

    // Attributes
    private CPU cpu;
    byte displacement;
    int intermediateResult;

    // Constructors
    /**
     * Class constructor
     */
    public Instruction_LOOPE_LOOPZ_Jb()
    {
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_LOOPE_LOOPZ_Jb(CPU processor)
    {
        // this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Loop while CX is not zero and ZF == 1, performing short jump indicated by immediate signed byte
     */
    public void execute()
    {
        // Get displacement byte (immediate)
        // This byte is interpreted signed, so cast to Java byte
        displacement = (byte) cpu.getByteFromCode();

        // Decrement the CX register
        cpu.cx[CPU.REGISTER_GENERAL_LOW]--;
            
        // Check for underflow in CL
        // This has happened if CL is -1 now        
        if ( cpu.cx[CPU.REGISTER_GENERAL_LOW] == -1 )
        {
            // Decrease CH
            cpu.cx[CPU.REGISTER_GENERAL_HIGH]--;
        }
        

        // Test LOOP condition, jump if CX is not zero and ZF == 1
        if (cpu.flags[CPU.REGISTER_FLAGS_ZF] == true && (cpu.cx[CPU.REGISTER_GENERAL_LOW] != 0x00 || cpu.cx[CPU.REGISTER_GENERAL_HIGH] != 0x00))
        {
            // Jump is relative to _next_ instruction, but by the time we change 
            // the IP, it has already been incremented twice, so no extra arithmetic necessary      

            intermediateResult = (((int) (cpu.ip[CPU.REGISTER_GENERAL_LOW])) & 0xFF) + displacement;
            // Need to check for possible overflow/underflow in IP[low]
            if (intermediateResult < 0)
            {
                // Underflow
                cpu.ip[CPU.REGISTER_GENERAL_HIGH]--;
            }
            else if (intermediateResult > 255)
            {
                // Overflow
                cpu.ip[CPU.REGISTER_GENERAL_HIGH]++;
            }

            // Update IP[low] with displacement
            cpu.ip[CPU.REGISTER_GENERAL_LOW] += displacement;
        }
    }
}
