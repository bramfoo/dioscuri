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
	 * Intel opcode 6C<BR>
	 * Copy word from I/O port to ES:DI; update DI register according to DF.<BR>
	 * Flags modified: none
	 */
public class Instruction_INSB_YbDX implements Instruction {

	// Attributes
	private CPU cpu;
    int portAddress;
    byte portByte = 0;
    byte[] transition;
	
    // Logging
    private static Logger logger = Logger.getLogger("dioscuri.module.cpu");


    // Constructors
	/**
	 * Class constructor
	 * 
	 */
	public Instruction_INSB_YbDX()	{}
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_INSB_YbDX(CPU processor)
	{
		this();
		
		// Create reference to cpu class
		cpu = processor;
        
        // Set transition that holds the amount DI should be altered (byte = 1)
        transition = new byte[] { 0x00, 0x01 };
	}

	
	// Methods
	
	/**
	 * Copy byte from I/O port to ES:DI; update DI register according to DF
	 */
	public void execute()
	{
        // Get port address from DX; convert this to unsigned integer to prevent lookup table out of bounds;
        portAddress = (((((int) cpu.dx[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 8) + (((int) cpu.dx[CPU.REGISTER_GENERAL_LOW]) & 0xFF));

        // Read byte from I/O space; write to ES:DI; ES segment override is not allowed
        try
        {
            portByte = cpu.getIOPortByte(portAddress);
            cpu.setByteToExtra(cpu.di, portByte);
        }
        catch (ModuleException e)
        {
            logger.log(Level.WARNING, "[" + cpu.getType() + "] " + e.getMessage());
        }

        // Update DI according to DF flag
        // Check direction of flag: If DF == 0, DI is incremented; if DF == 1, DI is decremented
        if (cpu.flags[CPU.REGISTER_FLAGS_DF] == true)
        {
            // Decrement the DI register by byte size
            cpu.di = Util.subtractWords(cpu.di, transition, 0);
        }
        else
        {
            // Increment the DI register by byte size
            cpu.di = Util.addWords(cpu.di, transition, 0);
        }
    }
}
