/*
 * $Revision$ $Date$ $Author$
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

	/**
	 * Intel opcode 6E<BR>
	 * Output byte from DS:SI to I/O port (specified in DX); update SI register according to DF.<BR>
	 * Flags modified: none
	 */
public class Instruction_OUTS_DXXb implements Instruction {

	// Attributes
	private CPU cpu;
    int portAddress;
    
    // Note: the addressbyte passed here is a value chosen so if the segmentOverride fails (which it shouldn't!),
    // the DS segment is still chosen. 
    byte defaultAddressByte = 0;
    
    byte memoryValue = 0;
    
    byte[] transition;
	
    // Logging
    private static Logger logger = Logger.getLogger("nl.kbna.dioscuri.module.cpu");


    // Constructors
	/**
	 * Class constructor
	 * 
	 */
	public Instruction_OUTS_DXXb()	{}
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_OUTS_DXXb(CPU processor)
	{
		this();
		
		// Create reference to cpu class
		cpu = processor;
        // Set transition that holds the amount SI should be altered (byte = 1)
        transition = new byte[] { 0x00, 0x01 };
        
	}

	
	// Methods
	
	/**
	 * Output word from DS:SI to I/O port (specified in DX); update SI register according to DF.
	 */
	public void execute()
	{
        
        // Get port address from DX; convert this to unsigned integer to prevent lookup table out of bounds;
        portAddress = (((((int) cpu.dx[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 8) + (((int) cpu.dx[CPU.REGISTER_GENERAL_LOW]) & 0xFF));

        // Read byte from DS:SI; write to portAddress
        // DS segment may be overridden.
        try
        {
            memoryValue = cpu.getByteFromMemorySegment(defaultAddressByte, cpu.si);

            // Write the byte to the I/O port
            cpu.setIOPortByte(portAddress, memoryValue);
        }
        catch (ModuleException e)
        {
            logger.log(Level.WARNING, "[" + cpu.getType() + "] " + e.getMessage());
        }

        // Update SI according to DF flag
        // Check direction of flag: If DF == 0, SI is incremented; if DF == 1, SI is decremented
        if (cpu.flags[CPU.REGISTER_FLAGS_DF])
        {
            // Decrement the SI register by byte size
            cpu.si = Util.subtractWords(cpu.si, transition, 0);
        }
        else
        {
            // Increment the SI register by byte size
            cpu.si = Util.addWords(cpu.si, transition, 0);
        }
	}
}
