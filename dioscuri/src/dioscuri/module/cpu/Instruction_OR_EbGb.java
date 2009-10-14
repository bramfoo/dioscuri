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
	 * Intel opcode 08<BR>
	 * Logical byte-sized OR of memory/register (destination) and register (source).<BR>
	 * The addressbyte determines the source (rrr bits) and destination (sss bits).<BR>
	 * Flags modified: OF, SF, ZF, AF, PF, CF
	 */
public class Instruction_OR_EbGb implements Instruction {

	// Attributes
	private CPU cpu;
    
    boolean operandWordSize = false;

	byte addressByte = 0;
	byte[] memoryReferenceLocation = new byte[2];
	byte[] memoryReferenceDisplacement = new byte[2];

	byte sourceValue = 0;
	byte[] destinationRegister = new byte[2];
	byte registerHighLow = 0;

	byte logicalORResult = 0;
	
	// Constructors
	/**
	 * Class constructor
	 */
	public Instruction_OR_EbGb()	{}
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_OR_EbGb(CPU processor)
	{
		this();
		
		// Create reference to cpu class
		cpu = processor;
	}

	// Methods

	/**
	 * Logical OR of memory/register (destination) and register (source).<BR>
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
        // Determine high/low part of register based on bit 5 (leading rrr bit)
        registerHighLow = ((addressByte & 0x20) >> 5) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW : (byte) CPU.REGISTER_GENERAL_HIGH;
		sourceValue = (cpu.decodeRegister(operandWordSize, (addressByte & 0x38) >> 3))[registerHighLow];
		
		// Execute OR on reg,reg or mem,reg. Determine this from mm bits of addressbyte
        if (((addressByte >> 6) & 0x03) == 3)
		{
			// OR reg,reg
			// Determine destination register from addressbyte, ANDing it with 0000 0111
			destinationRegister = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
			
			// OR source and destination, storing result in destination. registerHighLow is re-used here.
            // Re-determine high/low part of register based on bit 3 (leading sss bit)
            registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW : (byte) CPU.REGISTER_GENERAL_HIGH;
			destinationRegister[registerHighLow] |= sourceValue;
			
			// Test ZF on particular byte of destinationRegister
			cpu.flags[CPU.REGISTER_FLAGS_ZF] = destinationRegister[registerHighLow] == 0 ? true : false;
			// Test SF on particular byte of destinationRegister (set when MSB is 1, occurs when destReg >= 0x80)
			cpu.flags[CPU.REGISTER_FLAGS_SF] = destinationRegister[registerHighLow] < 0 ? true : false;
			// Set PF on particular byte of destinationRegister
			cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(destinationRegister[registerHighLow]);
		}
		else
		{
			// OR mem,reg
			// Determine memory location
			memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);

			// Get byte from memory and OR with source register
			logicalORResult = (byte) (cpu.getByteFromMemorySegment(addressByte, memoryReferenceLocation) | sourceValue);
			// Store result in memory
			cpu.setByteInMemorySegment(addressByte, memoryReferenceLocation, logicalORResult);

			// Test ZF on result
			cpu.flags[CPU.REGISTER_FLAGS_ZF] = logicalORResult == 0 ? true : false;
			// Test SF on result (set when MSB is 1, occurs when result >= 0x80)
			cpu.flags[CPU.REGISTER_FLAGS_SF] = logicalORResult < 0 ? true : false;
			// Set PF on result
			cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(logicalORResult);
			
		}
	}
}
