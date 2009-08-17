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

	/**
	 * Intel opcode 3A<BR>
	 * Byte-sized comparison (SUB) of register ("destination") with memory/register (source).<BR>
	 * The addressbyte determines the source (sss bits) and "destination" (rrr bits).<BR>
	 * Flags modified: OF, SF, ZF, AF, PF, CF
	 */
public class Instruction_CMP_GbEb implements Instruction {

	// Attributes
	private CPU cpu;

    boolean operandWordSize = false;
    
	byte addressByte = 0;
	byte[] memoryReferenceLocation = new byte[2];
	byte[] memoryReferenceDisplacement = new byte[2];

	byte sourceValue = 0;
	byte destinationValue = 0;
	byte registerHighLow = 0;

	byte tempResult = 0;
	
	// Constructors
	/**
	 * Class constructor
	 */
	public Instruction_CMP_GbEb()	{}
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_CMP_GbEb(CPU processor)
	{
		this();
		
		// Create reference to cpu class
		cpu = processor;
	}

	// Methods
	/**
	 * Byte-sized comparison (SUB) of memory/register with register.<BR>
	 * Does not update any registers, only sets appropriate flags.
	 */
	public void execute()
	{
		// Get addresByte
		addressByte = cpu.getByteFromCode();

		// Determine displacement of memory location (if any) 
		memoryReferenceDisplacement = cpu.decodeMM(addressByte);
		
		// Determine source value from mm bits of addressbyte
        if (((addressByte >> 6) & 0x03) == 3)
		{
			// Source is a register
			// Determine source value from addressbyte, ANDing it with 0000 0111
            // Determine high/low part of register based on bit 5 (leading rrr bit)
            registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW : (byte) CPU.REGISTER_GENERAL_HIGH;
			sourceValue = cpu.decodeRegister(operandWordSize, addressByte & 0x07)[registerHighLow];
		}
		else
		{
			// Source is in memory
			// Determine memory location
			memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);

			// Get byte from memory
			sourceValue= cpu.getByteFromMemorySegment(addressByte, memoryReferenceLocation);
		}
		
		// Determine "destination" value using addressbyte. AND it with 0011 1000 and right-shift 3 to get rrr bits
        // Re-determine high/low part of register based on bit 3 (leading sss bit)
        registerHighLow = ((addressByte & 0x20) >> 5) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW : (byte) CPU.REGISTER_GENERAL_HIGH;
		destinationValue = (cpu.decodeRegister(operandWordSize, (addressByte & 0x38) >> 3))[registerHighLow];
	
		// Proceed with compare
		
		// Subtract source byte from destination byte 
		tempResult = (byte) (destinationValue - sourceValue);
	
        // Test AF
        cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_SUB(destinationValue, tempResult);  
		// Test CF
		cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_SUB(destinationValue, sourceValue, 0);
		// Test OF
		cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB(destinationValue, sourceValue, tempResult, 0);
		// Test ZF, is tested againt tempResult
		cpu.flags[CPU.REGISTER_FLAGS_ZF] = tempResult == 0x00 ? true : false;
		// Test SF, only applies to lower byte (set when MSB is 1, occurs when tempResult >= 0x80)
		cpu.flags[CPU.REGISTER_FLAGS_SF] = tempResult < 0 ? true : false;
		// Set PF, only applies to lower byte
		cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(tempResult);
	}
}
