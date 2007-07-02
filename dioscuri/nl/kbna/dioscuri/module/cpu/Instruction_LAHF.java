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
 * Intel opcode 9F<BR>
 * Move low byte of the FLAGS register into AH register.<BR>
 * The FLAGS register is read as SF:ZF:0:AF:0:PF:1:CF<BR>
 * Flags modified: none
 */
public class Instruction_LAHF implements Instruction
{

    // Attributes
    private CPU cpu;
    int flagValue;
    int flagHex;

    // Constructors
    /**
     * Class constructor
     */
    public Instruction_LAHF()
    {
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_LAHF(CPU processor)
    {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Move low byte of FLAGS register into AH register.
     */
    public void execute()
    {
        // Reset variables
        flagHex = 0;

        // Build FLAGS register
        for (int j = 0; j <= 7; j++)
        {
            // Convert flag boolean to integer value for low register,
            // taking care of reserved bits according to Intel specs.
            if (j == 1)
                flagValue = 1;
            else if (j == 3 || j == 5)
                flagValue = 0;
            else
                flagValue = cpu.flags[j] ? 1 : 0;
            // Multiply by corresponding power of 2, add to decimal representation
            flagHex += flagValue * (int) Math.pow(2, j);
        }

        cpu.ax[CPU.REGISTER_GENERAL_HIGH] = (byte) flagHex;

    }
}
