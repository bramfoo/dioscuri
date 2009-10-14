/* $Revision: 159 $ $Date: 2009-08-17 12:52:56 +0000 (ma, 17 aug 2009) $ $Author: blohman $
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

package dioscuri.module.cpu;

import dioscuri.exception.ModuleException;

/**
 * Intel opcode EF<BR>
 * Output word/doubleword in eAX to I/O port address specified by DX.<BR>
 * Flags modified: none
 */
public class Instruction_OUT_DXeAX implements Instruction
{

    // Attributes
    private CPU cpu;

    byte data;
    int portAddress;

    // Constructors
    /**
     * Class constructor
     */
    public Instruction_OUT_DXeAX()
    {
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_OUT_DXeAX(CPU processor)
    {
        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Output word/doubleword in eAX to I/O port address in DX
     */
    public void execute()
    {
        try
        {
            // Convert value in DX to unsigned integer to prevent lookup table out of bounds;
            // set data to appropriate port
            portAddress = (((((int) cpu.dx[CPU.REGISTER_GENERAL_HIGH])& 0xFF)<<8) + (((int) cpu.dx[CPU.REGISTER_GENERAL_LOW]) & 0xFF));
            
            // Check if word or double word should be written
            if (cpu.doubleWord)
            {
                // A double word should be written to I/O space
                // Create double word from eAX and AX (in Big Endian order)
                byte[] doubleWord = new byte[] { cpu.eax[CPU.REGISTER_GENERAL_HIGH], cpu.eax[CPU.REGISTER_GENERAL_LOW], cpu.ax[CPU.REGISTER_GENERAL_HIGH], cpu.ax[CPU.REGISTER_GENERAL_LOW] };
                
                // Write double word to I/O space
                cpu.setIOPortDoubleWord(portAddress, doubleWord);
            }
            else // Word
            {
                // A word should be written to I/O space
                // Create word from AX (in Big Endian order)
                byte[] word = new byte[] { cpu.ax[CPU.REGISTER_GENERAL_HIGH], cpu.ax[CPU.REGISTER_GENERAL_LOW] };
                
                // Write word to I/O space
                cpu.setIOPortWord(portAddress, word);
            }
        }
        catch (ModuleException e)
        {
            // TODO: Implement proper catch block for OUT_DXeAX instruction
        }
    }
}
