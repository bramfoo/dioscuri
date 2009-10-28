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
	 * Intel opcode A6<BR>
	 * Compare string byte at address DS:(E)SI with address ES:(E)DI.<BR>
	 * After compare, contents of SI and DI are incremented or decremented based on DF flag:<BR>
     * Byte: +/- 1, word: +/- 2, doubleword: +/-4.<BR>
	 * Flags modified: CF, OF, SF, ZF, AF, and PF
	 */
public class Instruction_CMPS_XbYb implements Instruction {

	// Attributes
	private CPU cpu;

    boolean operandWordSize;
    
	byte[] transition;
    byte[] temp;
    byte source1 = 0;
    byte source2 = 0;
    byte result;
    
	// Constructors
	/**
	 * Class constructor
	 */
	public Instruction_CMPS_XbYb()
    {
        operandWordSize = false;
        
        // Set transition that holds the amount si and di should be altered (byte = 1)
        transition = new byte[] { 0x00, 0x01 };
        temp = new byte[2];
        result = 0;
    }
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_CMPS_XbYb(CPU processor)
	{
		this();
		
		// Create reference to cpu class
		cpu = processor;
	}

	// Methods
	
	/**
     * Compare byte at address DS:(E)SI with address ES:(E)DI; set flags accordingly.<BR>
     * Increment/decrement both registers depending on DF flag.<BR>
	 * Flags modified: CF, OF, SF, ZF, AF, and PF
	 */
	public void execute()
	{
        // Get values DS:(E)SI and ES:(E)DI; DS segment override is allowed, ES segment isn't.
        if (cpu.segmentOverride)
        {
            // Note: the addressbyte passed here is a value chosen so if the segmentOverride fails (which it shouldn't!),
            // the DS segment is still chosen. 
            source1 = cpu.getByteFromMemorySegment((byte) 0, cpu.si);
        }
        else
        {
            source1 = cpu.getByteFromData(cpu.si);
        }
        
        // Get source byte 2
        source2 = cpu.getByteFromExtra(cpu.di);
        
        // Compare bytes via substraction (SRC1 - SRC2)
        result = (byte) (source1 - source2);
        
        // Set flags according to compare results
        // Test AF
        cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_SUB(source1, result);
        // Test CF
        cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_SUB(source1, source2, 0);
        // Test OF
        cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB(source1, source2, result, 0);
        // Test ZF on particular byte of destinationRegister
        cpu.flags[CPU.REGISTER_FLAGS_ZF] = result == 0 ? true : false;
        // Test SF on particular byte of destinationRegister (set when MSB is 1, occurs when destReg >= 0x80)
        cpu.flags[CPU.REGISTER_FLAGS_SF] = result < 0 ? true : false;
        // Test PF on particular byte of destinationRegister
        cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(result);
        
        // Increment or decrement SI and DI depending on DF flag
        if (cpu.flags[CPU.REGISTER_FLAGS_DF] == true)
        {
            // Decrement registers
            temp = Util.subtractWords(cpu.si, transition, 0);
            System.arraycopy(temp, 0, cpu.si, 0, temp.length);
            temp = Util.subtractWords(cpu.di, transition, 0);
            System.arraycopy(temp, 0, cpu.di, 0, temp.length);
        }
        else
        {
            // Increment registers
            temp = Util.addWords(cpu.si, transition, 0);
            System.arraycopy(temp, 0, cpu.si, 0, temp.length);
            temp = Util.addWords(cpu.di, transition, 0);
            System.arraycopy(temp, 0, cpu.di, 0, temp.length);
        }
	}
}
