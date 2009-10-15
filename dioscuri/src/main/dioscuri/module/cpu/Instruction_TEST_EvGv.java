/*
 * $Revision: 159 $ $Date: 2009-08-17 12:52:56 +0000 (ma, 17 aug 2009) $ $Author: blohman $
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

	/**
	 * Intel opcode 85<BR>
	 * Logical word-sized comparison (AND) of memory/register (destination) and register (source).<BR>
	 * The addressbyte determines the source (rrr bits) and destination (sss bits).<BR>
	 * Does not update any registers, only sets appropriate flags.<BR> 
	 * Flags modified: OF, SF, ZF, AF, PF, CF
	 */
public class Instruction_TEST_EvGv implements Instruction {

	// Attributes
	private CPU cpu;
    
    boolean operandWordSize = true;

	byte addressByte = 0;
	byte[] memoryReferenceLocation = new byte[2];
	byte[] memoryReferenceDisplacement = new byte[2];

	byte[] sourceValue1 = new byte[2];
	byte[] sourceValue2 = new byte[2];

	byte[] tempResult = new byte[2];
	
	// Constructors
	/**
	 * Class constructor
	 */
	public Instruction_TEST_EvGv()	{}
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_TEST_EvGv(CPU processor)
	{
		this();
		
		// Create reference to cpu class
		cpu = processor;
	}

	// Methods

	/**
	 * Logical word-sized comparison (AND) of memory/register (destination) and register (source).<BR>
	 * Does not update any registers, only sets appropriate flags.<BR> 
	 * SF, ZF, and PF are set according to the result;<BR>
	 * OF and CF are cleared. AF is undefined.
	 */
	public void execute()
	{
		// Clear appropriate flags
		cpu.flags[CPU.REGISTER_FLAGS_OF] = false;
		cpu.flags[CPU.REGISTER_FLAGS_CF] = false;
		// Intel docs state AF remains undefined, but MS-DOS debug.exe clears AF
		cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
		
		// Get addresByte
		addressByte = cpu.getByteFromCode();

		// Determine displacement of memory location (if any) 
		memoryReferenceDisplacement = cpu.decodeMM(addressByte);
		
        // Determine source value using addressbyte. AND it with 0011 1000 and right-shift 3 to get rrr bits
        sourceValue1 = cpu.decodeRegister(operandWordSize, (addressByte & 0x38) >> 3);
        
		// Execute TEST on reg,reg or mem,reg. Determine this from mm bits of addressbyte
        if (((addressByte >> 6) & 0x03) == 3)
		{
			// TEST reg,reg
            // Determine "destination" value from addressbyte, ANDing it with 0000 0111
            sourceValue2 = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
		}
		else
		{
			// TEST mem,reg
            // Determine memory location
            memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);

            // Get byte from memory
            sourceValue2 = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
		}
	
		// Logical TEST of source and destination, store result temporarily
		tempResult[CPU.REGISTER_GENERAL_HIGH] = (byte) (sourceValue1[CPU.REGISTER_GENERAL_HIGH] & sourceValue2[CPU.REGISTER_GENERAL_HIGH]);
		tempResult[CPU.REGISTER_GENERAL_LOW] = (byte) (sourceValue1[CPU.REGISTER_GENERAL_LOW] & sourceValue2[CPU.REGISTER_GENERAL_LOW]);

		// Test ZF on result
		cpu.flags[CPU.REGISTER_FLAGS_ZF] = tempResult[CPU.REGISTER_GENERAL_HIGH] == 0 && tempResult[CPU.REGISTER_GENERAL_LOW] == 0  ? true : false;
		// Test SF on result (set when MSB is 1, occurs when result >= 0x80)
		cpu.flags[CPU.REGISTER_FLAGS_SF] = tempResult[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
		// Set PF on lower byte of result
		cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(tempResult[CPU.REGISTER_GENERAL_LOW]);
	}
}
