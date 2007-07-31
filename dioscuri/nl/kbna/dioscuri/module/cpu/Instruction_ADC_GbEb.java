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
 * Intel opcode 12<BR>
 * Add byte in memory/register (source) + CF to register (destination).<BR>
 * The addressbyte determines the source (rrr bits) and destination (sss bits).<BR>
 * Flags modified: OF, SF, ZF, AF, PF, CF
 */
public class Instruction_ADC_GbEb implements Instruction
{

    // Attributes
    private CPU cpu;

    boolean operandWordSize = false;

    byte addressByte = 0;
    byte[] memoryReferenceLocation = new byte[2];
    byte[] memoryReferenceDisplacement = new byte[2];

    byte sourceValue = 0;
    byte oldDest = 0;
    byte[] destinationRegister = new byte[2];
    byte registerHighLow = 0;

    int iCarryFlag = 0;

    
    // Constructors
    /**
     * Class constructor
     */
    public Instruction_ADC_GbEb()
    {
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_ADC_GbEb(CPU processor)
    {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Add byte in memory/register (source) + CF to register (destination).<BR>
     */
    public void execute()
    {
        // Determine value of carry flag before reset
        iCarryFlag = cpu.flags[CPU.REGISTER_FLAGS_CF] ? 1 : 0;

        // Get addresByte
        addressByte = cpu.getByteFromCode();

        // Determine displacement of memory location (if any)
        memoryReferenceDisplacement = cpu.decodeMM(addressByte);

        // Execute ADC on reg,reg or mem,reg. Determine this from mm bits of addressbyte
        if (((addressByte >> 6) & 0x03) == 3)
        {
            // ADC reg,reg
            // Determine destination register from addressbyte, ANDing it with 0000 0111
            // Determine high/low part of register based on bit 5 (leading rrr bit)
            registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW : (byte) CPU.REGISTER_GENERAL_HIGH;
            sourceValue = cpu.decodeRegister(operandWordSize, addressByte & 0x07)[registerHighLow];
        }
        else
        {
            // ADC mem,reg
            // Determine memory location
            memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);

            // Get byte from memory and ADC source register
            sourceValue = cpu.getByteFromMemorySegment(addressByte, memoryReferenceLocation);
        }

        // Determine destination register using addressbyte. AND it with 0011 1000 and right-shift 3 to get rrr bits
        // Re-determine high/low part of register based on bit 3 (leading sss bit)
        registerHighLow = ((addressByte & 0x20) >> 5) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW : (byte) CPU.REGISTER_GENERAL_HIGH;
        destinationRegister = (cpu.decodeRegister(operandWordSize, (addressByte & 0x38) >> 3));

        // Store initial value for use in OF check
        oldDest = destinationRegister[registerHighLow];

        // ADC (source + CF) and destination, storing result in destination. registerHighLow is re-used here.
        destinationRegister[registerHighLow] += sourceValue + iCarryFlag;

        // Test AF
        cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_ADD(oldDest, destinationRegister[registerHighLow]);
        // Test CF
        cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_ADD(oldDest, sourceValue, iCarryFlag);
        // Test OF
        cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_ADD(oldDest, sourceValue, destinationRegister[registerHighLow], iCarryFlag);
        // Test ZF on particular byte of destinationRegister
        cpu.flags[CPU.REGISTER_FLAGS_ZF] = destinationRegister[registerHighLow] == 0 ? true : false;
        // Test SF on particular byte of destinationRegister (set when MSB is 1, occurs when destReg >= 0x80)
        cpu.flags[CPU.REGISTER_FLAGS_SF] = destinationRegister[registerHighLow] < 0 ? true : false;
        // Set PF on particular byte of destinationRegister
        cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(destinationRegister[registerHighLow]);

    }
}
