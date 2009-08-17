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
	 * Intel opcode 3B<BR>
	 * Word-sized comparison (SUB) of register ("destination") with memory/register (source).<BR>
	 * The addressbyte determines the source (rrr bits) and "destination" (sss bits).<BR>
	 * Flags modified: OF, SF, ZF, AF, PF, CF
	 */
public class Instruction_CMP_GvEv implements Instruction {

	// Attributes
	private CPU cpu;

    boolean operandWordSize;
    
	byte addressByte;
	byte[] memoryReferenceLocation;
	byte[] memoryReferenceDisplacement;

	byte[] sourceValue;
	byte[] destinationValue;

	byte[] tempResult;
    byte[] temp;
    int intermediateResult;
	
    
	// Constructors
	/**
	 * Class constructor
	 */
	public Instruction_CMP_GvEv()
    {
        operandWordSize = true;
        
        addressByte = 0;
        memoryReferenceLocation = new byte[2];
        memoryReferenceDisplacement = new byte[2];

        sourceValue = new byte[2];
        destinationValue = new byte[2];

        tempResult = new byte[2];
        intermediateResult = 0;
    }
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_CMP_GvEv(CPU processor)
	{
		this();
		
		// Create reference to cpu class
		cpu = processor;
	}

	// Methods
	/**
     * Word-sized comparison (SUB) of register ("destination") with memory/register (source).<BR>
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
			// Determine "destination" value from addressbyte, ANDing it with 0000 0111
			sourceValue = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
		}
		else
		{
			// Source is in memory
			// Determine memory location
			memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);

			// Get byte from memory
			sourceValue = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
		}
		
        // Determine "destination" value using addressbyte. AND it with 0011 1000 and right-shift 3 to get rrr bits
		destinationValue = cpu.decodeRegister(operandWordSize, (addressByte & 0x38) >> 3);
		
        // Proceed with compare
		
		// Perform substraction
        tempResult = Util.subtractWords(destinationValue, sourceValue, 0);
        
        // Test AF
        cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_SUB(destinationValue[CPU.REGISTER_GENERAL_LOW], tempResult[CPU.REGISTER_GENERAL_LOW]);
		// Test CF
		cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_SUB(destinationValue, sourceValue, 0);
		// Test OF
		cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB(destinationValue, sourceValue, tempResult, 0);
		// Test ZF, is tested againt tempResult
		cpu.flags[CPU.REGISTER_FLAGS_ZF] = tempResult[CPU.REGISTER_GENERAL_HIGH] == 0x00 && tempResult[CPU.REGISTER_GENERAL_LOW] == 0x00? true : false;
		// Test SF, only applies to lower byte (set when MSB is 1, occurs when tempResult >= 0x80)
		cpu.flags[CPU.REGISTER_FLAGS_SF] = tempResult[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
		// Set PF, only applies to lower byte
		cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(tempResult[CPU.REGISTER_GENERAL_LOW]);
	}
}
