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

import dioscuri.exception.ModuleException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Intel opcode ED<BR>
 * Put word/doubleword from I/O port address specified by DX into eAX.<BR>
 * Flags modified: none
 */
public class Instruction_IN_eAXDX implements Instruction {

    // Attributes
    private CPU cpu;

    byte data;
    int portAddress;

    // Logging
    private static final Logger logger = Logger.getLogger(Instruction_IN_eAXDX.class.getName());

    // Constructors

    /**
     * Class constructor
     */
    public Instruction_IN_eAXDX()
    {
    }

    /**
     * Class constructor specifying processor reference
     *
     * @param processor Reference to CPU class
     */
    public Instruction_IN_eAXDX(CPU processor)
    {
        // this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Input word/doubleword from I/O port address specified by DX into eAX
     */
    public void execute()
    {

        // Convert value in DX to unsigned integer to prevent lookup table out
        // of bounds;
        // get data to appropriate port
        portAddress = (((((int) cpu.dx[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 8) + (((int) cpu.dx[CPU.REGISTER_GENERAL_LOW]) & 0xFF));

        try {
            // Check if word or double word should be read
            if (cpu.doubleWord) {
                // A double word should be read from I/O space
                byte[] doubleWord = cpu.getIOPortDoubleWord(portAddress);

                // Store value in eAX and AX (in Big Endian order)
                cpu.ax[CPU.REGISTER_GENERAL_LOW] = doubleWord[3];
                cpu.ax[CPU.REGISTER_GENERAL_HIGH] = doubleWord[2];
                cpu.eax[CPU.REGISTER_GENERAL_LOW] = doubleWord[1];
                cpu.eax[CPU.REGISTER_GENERAL_HIGH] = doubleWord[0];
            } else // Word
            {
                // A word should be read from I/O space
                byte[] word = cpu.getIOPortWord(portAddress);

                // Store value in AX (in Big Endian order)
                cpu.ax[CPU.REGISTER_GENERAL_LOW] = word[1];
                cpu.ax[CPU.REGISTER_GENERAL_HIGH] = word[0];
            }
        } catch (ModuleException e) {
            logger.log(Level.WARNING, "[" + cpu.getType() + "] "
                    + e.getMessage());
        }
    }
}
