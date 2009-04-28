/* $Revision: 1.2 $ $Date: 2007-07-31 14:27:05 $ $Author: blohman $
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

import nl.kbna.dioscuri.exception.ModuleException;

/**
 * Intel opcode E6<BR>
 * Output byte in AL to I/O port address indicated by immediate byte.<BR>
 * Flags modified: none
 */
public class Instruction_OUT_IbAL implements Instruction
{

    // Attributes
    private CPU cpu;

    byte data;
    int portAddress;

    // Constructors
    /**
     * Class constructor
     */
    public Instruction_OUT_IbAL()
    {
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_OUT_IbAL(CPU processor)
    {
        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Output byte in AL to I/O port address imm8
     */
    public void execute()
    {
        try
        {
            // Fetch immediate byte from memory and convert to unsigned integer,
            // to prevent lookup table out of bounds
            // and set data to appropriate port
            cpu.setIOPortByte(((int)(cpu.getByteFromCode()) & 0xFF), cpu.ax[CPU.REGISTER_GENERAL_LOW]);
        }
        catch (ModuleException e)
        {
            // TODO: Implement proper catch block for OUT_IbAL instruction
        }
    }
}
