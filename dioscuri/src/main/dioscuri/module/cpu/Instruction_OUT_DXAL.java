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

import java.util.logging.Level;
import java.util.logging.Logger;

import dioscuri.exception.ModuleException;


/**
 * Intel opcode EE<BR>
 * Output byte in AL to I/O port address specified by DX.<BR>
 * Flags modified: none
 */
public class Instruction_OUT_DXAL implements Instruction
{

    // Attributes
    private CPU cpu;

    byte data;
    int portAddress;

    // Logging
    private static Logger logger = Logger.getLogger("dioscuri.module.cpu");

    
    // Constructors
    /**
     * Class constructor
     */
    public Instruction_OUT_DXAL()
    {
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_OUT_DXAL(CPU processor)
    {
        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Output byte in AL to I/O port address in DX
     */
    public void execute()
    {
        try
        {
            // Convert value in DX to unsigned integer to prevent lookup table out of bounds;
            // set data to appropriate port
            portAddress = (((((int) cpu.dx[CPU.REGISTER_GENERAL_HIGH])& 0xFF)<<8) + (((int) cpu.dx[CPU.REGISTER_GENERAL_LOW]) & 0xFF));
            
            // FIXME: Temporary shunt for custom Bochs ports defined in the Bochs BIOS; implement properly
            // #define PANIC_PORT  0x400
            // #define PANIC_PORT2 0x401
            // #define INFO_PORT   0x402
            // #define DEBUG_PORT  0x403
            if (portAddress == 1024 || portAddress == 1025 || portAddress == 1026 || portAddress == 1027)
            {
                logger.log(Level.CONFIG, "Instruction_OUT_DXAL targeted custom Bochs port " + portAddress + ". OUT ignored.");
            }
            else
            {
                cpu.setIOPortByte(portAddress, cpu.ax[CPU.REGISTER_GENERAL_LOW]);
            }
        }
        catch (ModuleException e)
        {
            // TODO: Implement proper catch block for OUT_DXAL instruction
        }
    }
}
