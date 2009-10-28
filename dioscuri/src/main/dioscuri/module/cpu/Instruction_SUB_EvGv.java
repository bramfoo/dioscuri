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
	 * Intel opcode 29<BR>
	 * Subtract word in register (source) from memory/register (destination).<BR>
	 * The addressbyte determines the source (rrr bits) and destination (sss bits).<BR>
	 * Flags modified: OF, SF, ZF, AF, PF, CF
	 */
public class Instruction_SUB_EvGv implements Instruction {

	// Attributes
	private CPU cpu;

    boolean operandWordSize = true;
    
	byte addressByte = 0;
	byte[] memoryReferenceLocation = new byte[2];
	byte[] memoryReferenceDisplacement = new byte[2];

	byte[] sourceValue = new byte[2];
    byte[] sourceValue2 = new byte[2];
    byte[] oldSource = new byte[2];
	byte[] destinationRegister = new byte[2];
    byte[] oldDest = new byte[2];
    int intermediateResult;

	byte[] tempResult = new byte[2];
    byte[] temp = new byte[2];
	
    
	// Constructors
	/**
	 * Class constructor
	 */
	public Instruction_SUB_EvGv()	{}
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_SUB_EvGv(CPU processor)
	{
		this();
		
		// Create reference to cpu class
		cpu = processor;
	}

	// Methods

	/**
	 * Subtract word in register (source) from memory/register (destination).<BR>
	 */
	public void execute()
	{
		// Get addresByte
		addressByte = cpu.getByteFromCode();

		// Determine displacement of memory location (if any) 
		memoryReferenceDisplacement = cpu.decodeMM(addressByte);
		
		// Determine source value using addressbyte. AND it with 0011 1000 and right-shift 3 to get rrr bits
		sourceValue = (cpu.decodeRegister(operandWordSize, (addressByte & 0x38) >> 3));
        System.arraycopy(sourceValue, 0, oldSource, 0, sourceValue.length);
		
		// Execute SUB on reg,reg or mem,reg. Determine this from mm bits of addressbyte
        if (((addressByte >> 6) & 0x03) == 3)
		{
			// SUB reg,reg
			// Determine destination register from addressbyte, ANDing it with 0000 0111
			destinationRegister = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
			
			// Store old value
			System.arraycopy(destinationRegister, 0, oldDest, 0, destinationRegister.length);

			// SUB source and destination, storing result in destination.
			temp = Util.subtractWords(destinationRegister, sourceValue, 0);
            System.arraycopy(temp, 0, destinationRegister, 0, temp.length);
			
            // Test AF
            cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_SUB(oldDest[CPU.REGISTER_GENERAL_LOW], destinationRegister[CPU.REGISTER_GENERAL_LOW]);
			// Test CF
			cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_SUB(oldDest, oldSource, 0);
			// Test OF
			cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB(oldDest, oldSource, destinationRegister, 0);
			// Test ZF on particular byte of destinationRegister
			cpu.flags[CPU.REGISTER_FLAGS_ZF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] == 0 && destinationRegister[CPU.REGISTER_GENERAL_LOW] == 0 ? true : false;
			// Test SF on particular byte of destinationRegister (set when MSB is 1, occurs when destReg >= 0x80)
			cpu.flags[CPU.REGISTER_FLAGS_SF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
			// Set PF on particular byte of destinationRegister
			cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(destinationRegister[CPU.REGISTER_GENERAL_LOW]);
		}
		else
		{
			// SUB mem,reg
			// Determine memory location
			memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);

			// Get byte from memory and SUB source register
			sourceValue2 = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
			
			// Subtract source from destination
			temp = Util.subtractWords(sourceValue2, sourceValue, 0);
            System.arraycopy(temp, 0, tempResult, 0, temp.length);

            // Store result in memory
            cpu.setWordInMemorySegment(addressByte, memoryReferenceLocation, tempResult);

            // Test AF
            cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_SUB(sourceValue2[CPU.REGISTER_GENERAL_LOW], tempResult[CPU.REGISTER_GENERAL_LOW]);
            // Test CF
            cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_SUB(sourceValue2, oldSource, 0);
			// Test OF
			cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB(sourceValue2, oldSource, tempResult, 0);
			// Test ZF on result
			cpu.flags[CPU.REGISTER_FLAGS_ZF] = tempResult[CPU.REGISTER_GENERAL_HIGH] == 0 && tempResult[CPU.REGISTER_GENERAL_LOW] == 0 ? true : false;
			// Test SF on result (set when MSB is 1, occurs when result >= 0x80)
			cpu.flags[CPU.REGISTER_FLAGS_SF] = tempResult[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
			// Set PF on result
			cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(tempResult[CPU.REGISTER_GENERAL_LOW]);
		}
	}
}
