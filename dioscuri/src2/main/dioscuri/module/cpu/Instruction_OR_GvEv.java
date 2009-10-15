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
	 * Intel opcode 0B<BR>
	 * Logical word-sized OR of register (destination) and memory/register (source).<BR>
	 * The addressbyte determines the source (sss bits) and destination (rrr bits).<BR> 
	 * Flags modified: OF, SF, ZF, AF, PF, CF
	 */
public class Instruction_OR_GvEv implements Instruction {

	// Attributes
	private CPU cpu;
    
    boolean operandWordSize = true;

	byte addressByte = 0;
	byte[] memoryReferenceLocation = new byte[2];
	byte[] memoryReferenceDisplacement = new byte[2];

	byte[] sourceValue = new byte[2];
	byte[] destinationRegister = new byte[2];
	
	// Constructors
	/**
	 * Class constructor
	 */
	public Instruction_OR_GvEv()	{}
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_OR_GvEv(CPU processor)
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
		
		// Execute OR on reg,reg or mem,reg. Determine this from mm bits of addressbyte
        if (((addressByte >> 6) & 0x03) == 3)
		{
			// OR reg,reg
			// Determine source value from addressbyte, ANDing it with 0000 0111 to get sss bits
			sourceValue = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
		}
		else
		{
			// OR mem,reg
			// Determine memory location
			memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);

			// Retrieve source value from memory indicated by reference location
			sourceValue = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
		}
		
		// Determine destination register using addressbyte, ANDing it with 0011 1000 and right-shift 3 to get rrr bits
		destinationRegister = (cpu.decodeRegister(operandWordSize, (addressByte & 0x38) >> 3));
		
		// Logical OR of source and destination, store result in destination register
		destinationRegister[CPU.REGISTER_GENERAL_HIGH] |= sourceValue[CPU.REGISTER_GENERAL_HIGH];
		destinationRegister[CPU.REGISTER_GENERAL_LOW] |= sourceValue[CPU.REGISTER_GENERAL_LOW];
		
		// Test ZF on particular byte of destinationRegister
		cpu.flags[CPU.REGISTER_FLAGS_ZF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] == 0 && destinationRegister[CPU.REGISTER_GENERAL_LOW] == 0 ? true : false;
		// Test SF on particular byte of destinationRegister (set when MSB is 1, occurs when destReg >= 0x80)
		cpu.flags[CPU.REGISTER_FLAGS_SF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
		// Set PF on lower byte of destinationRegister
		cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(destinationRegister[CPU.REGISTER_GENERAL_LOW]);

	}
}
