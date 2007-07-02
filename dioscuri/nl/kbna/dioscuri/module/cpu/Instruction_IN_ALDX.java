/* $Revision: 1.1 $ $Date: 2007-07-02 14:31:32 $ $Author: blohman $
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

import java.util.logging.Level;
import java.util.logging.Logger;

import nl.kbna.dioscuri.exception.ModuleException;
import nl.kbna.dioscuri.exception.ModuleUnknownPort;
import nl.kbna.dioscuri.exception.ModuleWriteOnlyPortException;

/**
 * Intel opcode EC<BR>
 * Put byte from I/O port address specified by DX into AL.<BR>
 * Flags modified: none
 */
public class Instruction_IN_ALDX implements Instruction
{

    // Attributes
    private CPU cpu;

    byte data;
    int portAddress;

    // Logging
    private static Logger logger = Logger.getLogger("nl.kbna.dioscuri.module.cpu");

    
    // Constructors
    /**
     * Class constructor
     */
    public Instruction_IN_ALDX()
    {
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_IN_ALDX(CPU processor)
    {
        // this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Input byte from I/O port address specified by DX into AL
     * @throws ModuleWriteOnlyPortException 
     * @throws ModuleUnknownPort 
     */
    public void execute()
    {

        // Convert value in DX to unsigned integer to prevent lookup table out of bounds;
        // get data to appropriate port
        portAddress = (((((int) cpu.dx[CPU.REGISTER_GENERAL_HIGH])& 0xFF)<<8) + (((int) cpu.dx[CPU.REGISTER_GENERAL_LOW]) & 0xFF));

        try
        {
            // Write retrieved value into AL
            cpu.ax[CPU.REGISTER_GENERAL_LOW] = cpu.getIOPortByte(portAddress);
        }
        catch (ModuleException e)
        {
            logger.log(Level.WARNING, "[" + cpu.getType() + "] " + e.getMessage());
        }
    }
}
