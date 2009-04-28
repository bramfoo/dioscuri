/*
 * $Revision: 1.2 $ $Date: 2007-07-31 14:27:03 $ $Author: blohman $
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
 * Intel opcode 01<BR>
 * Add word in register (source) to memory/register (destination).<BR>
 * The addressbyte determines the source (rrr bits) and destination (sss bits).<BR>
 * Flags modified: OF, SF, ZF, AF, PF, CF
 */
public class Instruction_ADD_EvGv implements Instruction
{

    // Attributes
    private CPU cpu;

    boolean operandWordSize = true;
    
    byte addressByte = 0;
    byte[] memoryReferenceLocation = new byte[2];
    byte[] memoryReferenceDisplacement = new byte[2];

    byte[] sourceValue = new byte[2];
    byte[] eSourceValue = new byte[2];
    byte[] oldSource = new byte[2];
    byte[] eOldSource = new byte[2];
    
    byte[] destinationRegister = new byte[2];
    byte[] eDestinationRegister = new byte[2];
    byte[] oldDest = new byte[2];
    byte[] eOldDest = new byte[2];

    int internalCarry = 0;
    byte[] temp = new byte[2];
    
    
    // Constructors
    /**
     * Class constructor
     */
    public Instruction_ADD_EvGv()   {}
    
    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_ADD_EvGv(CPU processor)
    {
        this();
        
        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Add word in register (source) to memory/register (destination).<BR>
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
        
        // Execute ADD on reg,reg or mem,reg. Determine this from mm bits of addressbyte
        if (((addressByte >> 6) & 0x03) == 3)
        {
            // ADD reg,reg
            // Determine destination register from addressbyte, ANDing it with 0000 0111
            destinationRegister = cpu.decodeRegister(operandWordSize, addressByte & 0x07);

    		// Store old values for flag checks
    		System.arraycopy(destinationRegister, 0, oldDest, 0, destinationRegister.length);
            
            if (cpu.doubleWord) // 32 bit registers
            {
                // Repeat actions for extra register
                eSourceValue = (cpu.decodeExtraRegister((addressByte & 0x38) >> 3));
                eDestinationRegister = (cpu.decodeExtraRegister(addressByte & 0x07));
                // Store initial value for use in OF check
                System.arraycopy(eDestinationRegister, 0, eOldDest, 0, eDestinationRegister.length);
        		System.arraycopy(eSourceValue, 0, eOldSource, 0, eSourceValue.length);
            }
        }
        else
        {
            // ADD mem,reg
            // Determine memory location
            memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);

            // Get word from memory
            destinationRegister = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);

            // Store initial value for use in OF check
            System.arraycopy(destinationRegister, 0, oldDest, 0, destinationRegister.length);

            if (cpu.doubleWord) // 32 bit registers
            {
                // Repeat actions for extra register
                eSourceValue = (cpu.decodeExtraRegister((addressByte & 0x38) >> 3));
                
                // Increment memory location
                memoryReferenceLocation = Util.addWords(memoryReferenceLocation, new byte[]{0x00, 0x02}, 0);
                eDestinationRegister = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
                
                // Store initial value for use in OF check
                System.arraycopy(eDestinationRegister, 0, eOldDest, 0, eDestinationRegister.length);
        		System.arraycopy(eSourceValue, 0, eOldSource, 0, eSourceValue.length);
            }
        }
        
        // ADD word
        temp = Util.addWords(destinationRegister, sourceValue, 0);
        System.arraycopy(temp, 0, destinationRegister, 0, temp.length);
        
        if (cpu.doubleWord) // 32 bit registers
        {
            // For CF, check for overflow in high register which may be used when 32-bit regs are used (see later)
            internalCarry = Util.test_CF_ADD(oldDest, oldSource, 0) == true ? 1 : 0;
            
            // ADD double word
            temp = Util.addWords(eDestinationRegister, eSourceValue, internalCarry);
            System.arraycopy(temp, 0, eDestinationRegister, 0, temp.length);

            // Test CF
            cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_ADD(eOldDest, eOldSource, internalCarry);
            // Test OF
            cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_ADD(eOldDest, eOldSource, eDestinationRegister, internalCarry);
            // Test SF on particular byte of destinationRegister (set when MSB is 1, occurs when destReg >= 0x80)
            cpu.flags[CPU.REGISTER_FLAGS_SF] = eDestinationRegister[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
            // Test ZF on particular byte of destinationRegister 
            cpu.flags[CPU.REGISTER_FLAGS_ZF] = eDestinationRegister[CPU.REGISTER_GENERAL_HIGH] == 0 && eDestinationRegister[CPU.REGISTER_GENERAL_LOW] == 0 && destinationRegister[CPU.REGISTER_GENERAL_HIGH] == 0 && destinationRegister[CPU.REGISTER_GENERAL_LOW] == 0 ? true : false;
        }
        else    // 16 bit registers
        {
            // Test CF
            cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_ADD(oldDest, oldSource, 0);
            // Test OF
            cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_ADD(oldDest, oldSource, destinationRegister, 0);
            // Test SF on particular byte of destinationRegister (set when MSB is 1, occurs when destReg >= 0x80)
            cpu.flags[CPU.REGISTER_FLAGS_SF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
            // Test ZF on particular byte of destinationRegister 
            cpu.flags[CPU.REGISTER_FLAGS_ZF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] == 0 && destinationRegister[CPU.REGISTER_GENERAL_LOW] == 0 ? true : false;
        }
        
        // Test AF
        cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_ADD(oldDest[CPU.REGISTER_GENERAL_LOW], destinationRegister[CPU.REGISTER_GENERAL_LOW]);  
        // Test PF on particular byte of destinationRegister
        cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(destinationRegister[CPU.REGISTER_GENERAL_LOW]);
        
        
        // Store result to memory for ADD mem,reg operations
        if (((addressByte >> 6) & 0x03) != 3)
        {
            if (cpu.doubleWord) // 32 bit registers
            {
                // Do this in reverse order because memlocation was incremented
                cpu.setWordInMemorySegment(addressByte, memoryReferenceLocation, eDestinationRegister);
                // Decrement memlocation
                memoryReferenceLocation = Util.subtractWords(memoryReferenceLocation, new byte[]{0x00, 0x02}, 0);
            }
            cpu.setWordInMemorySegment(addressByte, memoryReferenceLocation, destinationRegister);
        }
    }
}
