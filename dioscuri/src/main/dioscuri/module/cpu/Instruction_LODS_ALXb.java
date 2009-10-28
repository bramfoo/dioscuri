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

	/**
	 * Intel opcode AC<BR>
	 * Load byte from DS:SI into AL; update SI register according to DF.<BR>
	 * Flags modified: none
	 */
public class Instruction_LODS_ALXb implements Instruction {

	// Attributes
	private CPU cpu;
    byte source;
    byte[] incrementSize = new byte[]{0x00, 0x01};  // Byte size increment for SI
	
	// Constructors
	/**
	 * Class constructor
	 * 
	 */
	public Instruction_LODS_ALXb()	{}
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_LODS_ALXb(CPU processor)
	{
		this();
		
		// Create reference to cpu class
		cpu = processor;
	}

	
	// Methods
	
	/**
	 * Load byte from DS:SI into AL; update SI
	 */
	public void execute()
	{
		// Get byte at DS:SI and assign to AL; DS segment override is allowed
        if (cpu.segmentOverride)
        {
            source = cpu.getByteFromMemorySegment((byte) 0, cpu.si);
        }
        else
        {
            source = cpu.getByteFromData(cpu.si);
        }
        
		cpu.ax[CPU.REGISTER_GENERAL_LOW] = source;

        // Update SI according to DF flag
        // Check direction of flag: If DF == 0, SI is incremented; if DF == 1, SI is decremented
        if (cpu.flags[CPU.REGISTER_FLAGS_DF])
        {
            // Decrement the SI register by byte size
            cpu.si = Util.subtractWords(cpu.si, incrementSize, 0);
        }
        else
        {
            // Increment the SI register by byte size
            cpu.si = Util.addWords(cpu.si, incrementSize, 0);
        }
	}
}
