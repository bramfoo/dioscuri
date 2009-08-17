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

import nl.kbna.dioscuri.exception.CPUInstructionException;

/**
 * Intel opcode 83<BR>
 * Immediate Group 1 opcode extension: ADD, OR, ADC, SBB, AND, SUB, XOR, CMP.<BR>
 * Performs the selected instruction (indicated by bits 5, 4, 3 of the ModR/M byte) using immediate data.<BR>
 * Flags modified: depending on instruction can be any of: OF, SF, ZF, AF, PF, CF
 */
public class Instruction_ImmGRP1_EvIb implements Instruction
{

    // Attributes
    private CPU cpu;

    boolean operandWordSize = true;

    byte addressByte;
    byte[] memoryReferenceLocation;
    byte[] memoryReferenceDisplacement;

    byte[] sourceValue;
    byte[] sourceValue2;
    byte[] destinationRegister;
    byte[] oldDest;

    int iCarryFlag;
    byte[] tempResult;
    byte[] temp;

    
    // Constructors
    /**
     * Class constructor
     */
    public Instruction_ImmGRP1_EvIb()
    {
        operandWordSize = true;

        addressByte = 0;
        memoryReferenceLocation = new byte[2];
        memoryReferenceDisplacement = new byte[2];

        sourceValue = new byte[2];
        sourceValue2 = new byte[2];
        destinationRegister = new byte[2];
        oldDest = new byte[2];
        
        iCarryFlag = 0;
        tempResult = new byte[2];
        temp = new byte[2];
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_ImmGRP1_EvIb(CPU processor)
    {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Execute any of the following Immediate Group 1 instructions: ADD, OR, ADC, SBB, AND, SUB, XOR, CMP.<BR>
     * @throws CPUInstructionException 
     */
    public void execute() throws CPUInstructionException
    {
        // Get addresByte
        addressByte = cpu.getByteFromCode();

        // Re-initialise sourceValue words (to lose pointer to earlier words)
        sourceValue = new byte[2];
        sourceValue2 = new byte[2];

        // Determine displacement of memory location (if any)
        memoryReferenceDisplacement = cpu.decodeMM(addressByte);

        // Get immediate byte
        sourceValue[CPU.REGISTER_GENERAL_LOW] = cpu.getByteFromCode();
        // Sign-extend HIGH register with sign from LOW register
        sourceValue[CPU.REGISTER_GENERAL_HIGH] = Util.signExtend(sourceValue[CPU.REGISTER_GENERAL_LOW]);
        
        // Execute instruction decoded from nnn (bits 5, 4, 3 in ModR/M byte)
        switch ((addressByte & 0x38) >> 3)
        {
            case 0: // ADD
                // Execute ADD on reg,reg or mem,reg. Determine this from mm bits of addressbyte
                if (((addressByte >> 6) & 0x03) == 3)
                {
                    // ADD reg,reg
                    // Determine destination register from addressbyte, ANDing it with 0000 0111
                    destinationRegister = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
    
                    // Store initial value for use in OF check
                    System.arraycopy(destinationRegister, 0, oldDest, 0, destinationRegister.length);
    
                    // ADD source and destination, storing result in destination.
                    byte[] temp = Util.addWords(destinationRegister, sourceValue, 0);
                    System.arraycopy(temp, 0, destinationRegister, 0, temp.length);
                    
                    // Test AF
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_ADD(oldDest[CPU.REGISTER_GENERAL_LOW], destinationRegister[CPU.REGISTER_GENERAL_LOW]);  
                    // Test CF
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_ADD(oldDest, sourceValue, 0);
                    // Test OF
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_ADD(oldDest, sourceValue, destinationRegister, 0);
                    // Test ZF on particular byte of destinationRegister
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] == 0 && destinationRegister[CPU.REGISTER_GENERAL_LOW] == 0 ? true : false;
                    // Test SF on particular byte of destinationRegister (set when MSB is 1, occurs when destReg >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
                    // Set PF on particular byte of destinationRegister
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(destinationRegister[CPU.REGISTER_GENERAL_LOW]);
                }
                else
                {
                    // ADD mem,reg
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);
    
                    // Get word from memory and ADD source register
                    sourceValue2 = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
    
                    // Add source to destination
                    tempResult = Util.addWords(sourceValue, sourceValue2, 0);
    
                    // Store result in memory
                    cpu.setWordInMemorySegment(addressByte, memoryReferenceLocation, tempResult);
    
                    // Test AF
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_ADD(sourceValue2[CPU.REGISTER_GENERAL_LOW], tempResult[CPU.REGISTER_GENERAL_LOW]);  
                    // Test CF
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_ADD(sourceValue2, sourceValue, 0);
                    // Test OF
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_ADD(sourceValue2, sourceValue, tempResult, 0);
                    // Test ZF on result
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = tempResult[CPU.REGISTER_GENERAL_HIGH] == 0 && tempResult[CPU.REGISTER_GENERAL_LOW] == 0 ? true : false;
                    // Test SF on result (set when MSB is 1, occurs when result >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = tempResult[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
                    // Set PF on result
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(tempResult[CPU.REGISTER_GENERAL_LOW]);
                }
                break;  // ADD

            case 1: // OR
                // Clear appropriate flags
                cpu.flags[CPU.REGISTER_FLAGS_OF] = false;
                cpu.flags[CPU.REGISTER_FLAGS_CF] = false;
                // Bochs' clears the AF flag as well
                cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
                
                // Execute OR on reg,reg or mem,reg. Determine this from mm bits of addressbyte
                if (((addressByte >> 6) & 0x03) == 3)
                {
                    // OR reg,reg
                    // Determine destination register from addressbyte, ANDing it with 0000 0111
                    destinationRegister = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
                    
                    // OR source and destination, storing result in destination. registerHighLow is re-used here.
                    destinationRegister[CPU.REGISTER_GENERAL_LOW] |= sourceValue[CPU.REGISTER_GENERAL_LOW];
                    destinationRegister[CPU.REGISTER_GENERAL_HIGH] |= sourceValue[CPU.REGISTER_GENERAL_HIGH];
                    
                    // Test ZF on particular byte of destinationRegister
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] == 0 && destinationRegister[CPU.REGISTER_GENERAL_LOW] == 0 ? true : false;
                    // Test SF on particular byte of destinationRegister (set when MSB is 1, occurs when destReg >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
                    // Set PF on lower byte of destinationRegister
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(destinationRegister[CPU.REGISTER_GENERAL_LOW]);
                }
                else
                {
                    // OR mem,reg
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);

                    // Get word from memory and OR with source register
                    sourceValue2 = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
                    
                    // OR source and destination
                    tempResult[CPU.REGISTER_GENERAL_LOW] = (byte) (sourceValue[CPU.REGISTER_GENERAL_LOW] | sourceValue2[CPU.REGISTER_GENERAL_LOW]);
                    tempResult[CPU.REGISTER_GENERAL_HIGH] = (byte) (sourceValue[CPU.REGISTER_GENERAL_HIGH] | sourceValue2[CPU.REGISTER_GENERAL_HIGH]);
                    
                    // Store result in memory
                    cpu.setWordInMemorySegment(addressByte, memoryReferenceLocation, tempResult);

                    // Test ZF on result
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = tempResult[CPU.REGISTER_GENERAL_HIGH] == 0 && tempResult[CPU.REGISTER_GENERAL_LOW] == 0  ? true : false;
                    // Test SF on result (set when MSB is 1, occurs when result >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = tempResult[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
                    // Set PF on lower byte of result
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(tempResult[CPU.REGISTER_GENERAL_LOW]);
                }
                break;  // OR
                
            case 2: // ADC
                // Determine value of carry flag before reset
                iCarryFlag = (byte) (cpu.flags[CPU.REGISTER_FLAGS_CF] ? 1 : 0);

                // Execute ADC on reg,reg or mem,reg. Determine this from mm bits of addressbyte
                if (((addressByte >> 6) & 0x03) == 3)
                {
                    // ADC reg,reg
                    // Determine destination register from addressbyte, ANDing it with 0000 0111
                    destinationRegister = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
    
                    // Store initial value for use in OF check
                    System.arraycopy(destinationRegister, 0, oldDest, 0, destinationRegister.length);
    
                    // ADD source and destination, storing result in destination.
                    byte[] temp = Util.addWords(destinationRegister, sourceValue, iCarryFlag);
                    System.arraycopy(temp, 0, destinationRegister, 0, temp.length);
                    
                    // Test AF
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_ADD(oldDest[CPU.REGISTER_GENERAL_LOW], destinationRegister[CPU.REGISTER_GENERAL_LOW]);  
                    // Test CF
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_ADD(oldDest, sourceValue, iCarryFlag);
                    // Test OF
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_ADD(oldDest, sourceValue, destinationRegister, iCarryFlag);
                    // Test ZF on particular byte of destinationRegister
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] == 0 && destinationRegister[CPU.REGISTER_GENERAL_LOW] == 0 ? true : false;
                    // Test SF on particular byte of destinationRegister (set when MSB is 1, occurs when destReg >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
                    // Set PF on particular byte of destinationRegister
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(destinationRegister[CPU.REGISTER_GENERAL_LOW]);
                }
                else
                {
                    // ADC mem,reg
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);
    
                    // Get word from memory and ADC source register
                    sourceValue2 = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
    
                    // Add source to destination
                    tempResult = Util.addWords(sourceValue, sourceValue2, iCarryFlag);
    
                    // Store result in memory
                    cpu.setWordInMemorySegment(addressByte, memoryReferenceLocation, tempResult);

                    // Test AF
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_ADD(sourceValue2[CPU.REGISTER_GENERAL_LOW], tempResult[CPU.REGISTER_GENERAL_LOW]);  
                    // Test CF
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_ADD(sourceValue2, sourceValue, iCarryFlag);
                    // Test OF
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_ADD(sourceValue2, sourceValue, tempResult, iCarryFlag);
                    // Test ZF on result
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = tempResult[CPU.REGISTER_GENERAL_HIGH] == 0 && tempResult[CPU.REGISTER_GENERAL_LOW] == 0 ? true : false;
                    // Test SF on result (set when MSB is 1, occurs when result >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = tempResult[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
                    // Set PF on result
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(tempResult[CPU.REGISTER_GENERAL_LOW]);
                }
                break; //ADC
                
            case 3: // SBB
                // Flags modified: OF, SF, ZF, AF, PF, CF
                // Determine value of carry flag before reset
                iCarryFlag = cpu.flags[CPU.REGISTER_FLAGS_CF] ? 1 : 0;

                // Execute SBB on reg,imm or mem,imm. Determine this from mm bits of addressbyte
                if (((addressByte >> 6) & 0x03) == 3)
                {
                    // SBB reg,imm
                    // Determine destination register from addressbyte, ANDing it with 0000 0111
                    destinationRegister = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
                    
                    // Store old value
                    System.arraycopy(destinationRegister, 0, oldDest, 0, destinationRegister.length);

                    // SBB source2 - (source1 + carry)
                    temp = Util.subtractWords(destinationRegister, sourceValue, iCarryFlag);
                    System.arraycopy(temp, 0, destinationRegister, 0, temp.length);
                    
                    // Test AF
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_SUB(oldDest[CPU.REGISTER_GENERAL_LOW], destinationRegister[CPU.REGISTER_GENERAL_LOW]);  
                    // Test CF
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_SUB(oldDest, sourceValue, 0);
                    // Test OF
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB(oldDest, sourceValue, destinationRegister, 0);
                    // Test ZF on particular byte of destinationRegister
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] == 0 && destinationRegister[CPU.REGISTER_GENERAL_LOW] == 0 ? true : false;
                    // Test SF on particular byte of destinationRegister (set when MSB is 1, occurs when destReg >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
                    // Set PF on particular byte of destinationRegister
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(destinationRegister[CPU.REGISTER_GENERAL_LOW]);
                }
                else
                {
                    // SBB mem,imm
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);

                    // Get word from memory
                    sourceValue2 = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
                    
                    // SBB source2 - (source1 + carry)
                    temp = Util.subtractWords(sourceValue2, sourceValue, iCarryFlag);
                    System.arraycopy(temp, 0, tempResult, 0, temp.length);

                    // Store result in memory
                    cpu.setWordInMemorySegment(addressByte, memoryReferenceLocation, tempResult);

                    // Test AF
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_SUB(sourceValue2[CPU.REGISTER_GENERAL_LOW], tempResult[CPU.REGISTER_GENERAL_LOW]);  
                    // Test CF
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_SUB(sourceValue2, sourceValue, 0);
                    // Test OF
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB(sourceValue2, sourceValue, tempResult, 0);
                    // Test ZF on result
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = tempResult[CPU.REGISTER_GENERAL_HIGH] == 0 && tempResult[CPU.REGISTER_GENERAL_LOW] == 0 ? true : false;
                    // Test SF on result (set when MSB is 1, occurs when result >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = tempResult[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
                    // Set PF on result
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(tempResult[CPU.REGISTER_GENERAL_LOW]);
                }
                break;  // SBB
                
            case 4: // AND
                // Clear appropriate flags
                cpu.flags[CPU.REGISTER_FLAGS_OF] = false;
                cpu.flags[CPU.REGISTER_FLAGS_CF] = false;
                // Bochs is always right (even if it clashes with the Intel docs) ;-)
                cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
    
                // Execute AND on reg,reg or mem,reg. Determine this from mm bits of addressbyte
                if (((addressByte >> 6) & 0x03) == 3)
                {
                    // AND reg,imm
                    // Determine destination register from addressbyte, ANDing it with 0000 0111
                    destinationRegister = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
                    
                    destinationRegister[CPU.REGISTER_GENERAL_LOW] &= sourceValue[CPU.REGISTER_GENERAL_LOW];
                    destinationRegister[CPU.REGISTER_GENERAL_HIGH] &= sourceValue[CPU.REGISTER_GENERAL_HIGH];
                    
                    // Set ZF
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = destinationRegister[CPU.REGISTER_GENERAL_LOW] == 0 && destinationRegister[CPU.REGISTER_GENERAL_HIGH] == 0? true : false;
                    // Test SF, only applies to LOW (set when MSB is 1, occurs when AL >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
                    // Set PF, only applies to LOW
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(destinationRegister[CPU.REGISTER_GENERAL_LOW]);
                }
                else
                {
                    // AND mem,imm
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);
    
                    // Get byte from memory and AND source register
                    tempResult = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
                    
                    tempResult[CPU.REGISTER_GENERAL_LOW] &= sourceValue[CPU.REGISTER_GENERAL_LOW];
                    tempResult[CPU.REGISTER_GENERAL_HIGH] &= sourceValue[CPU.REGISTER_GENERAL_HIGH];
                    
                    // Store result back in memory reference location 
                    cpu.setWordInMemorySegment(addressByte, memoryReferenceLocation, tempResult);
    
                    // Set ZF
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = tempResult[CPU.REGISTER_GENERAL_LOW] == 0 && tempResult[CPU.REGISTER_GENERAL_HIGH] == 0? true : false;
                    // Test SF, only applies to LOW (set when MSB is 1, occurs when AL >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = tempResult[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
                    // Set PF, only applies to LOW
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(tempResult[CPU.REGISTER_GENERAL_LOW]);
                }
                break;  // AND
            
            case 5: // SUB; flags modified: OF, SF, ZF, AF, PF, CF
                // Execute SUB on reg,imm or mem,imm. Determine this from mm bits of addressbyte
                if (((addressByte >> 6) & 0x03) == 3)
                {
                    // SUB reg,imm
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
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_SUB(oldDest, sourceValue, 0);
                    // Test OF
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB(oldDest, sourceValue, destinationRegister, 0);
                    // Test ZF on particular byte of destinationRegister
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] == 0 && destinationRegister[CPU.REGISTER_GENERAL_LOW] == 0 ? true : false;
                    // Test SF on particular byte of destinationRegister (set when MSB is 1, occurs when destReg >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
                    // Set PF on particular byte of destinationRegister
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(destinationRegister[CPU.REGISTER_GENERAL_LOW]);
                }
                else
                {
                    // SUB mem,imm
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);

                    // Get word from memory
                    sourceValue2 = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
                    
                    // SUB source1 and source2
                    temp = Util.subtractWords(sourceValue2, sourceValue, 0);
                    System.arraycopy(temp, 0, tempResult, 0, temp.length);

                    // Store result in memory
                    cpu.setWordInMemorySegment(addressByte, memoryReferenceLocation, tempResult);

                    // Test AF
                    cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_SUB(sourceValue2[CPU.REGISTER_GENERAL_LOW], tempResult[CPU.REGISTER_GENERAL_LOW]);  
                    // Test CF
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_SUB(sourceValue2, sourceValue, 0);
                    // Test OF
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB(sourceValue2, sourceValue, tempResult, 0);
                    // Test ZF on result
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = tempResult[CPU.REGISTER_GENERAL_HIGH] == 0 && tempResult[CPU.REGISTER_GENERAL_LOW] == 0 ? true : false;
                    // Test SF on result (set when MSB is 1, occurs when result >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = tempResult[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
                    // Set PF on result
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(tempResult[CPU.REGISTER_GENERAL_LOW]);
                }
                break;  //SUB
                
            case 6: // XOR
                // Clear appropriate flags
                cpu.flags[CPU.REGISTER_FLAGS_OF] = false;
                cpu.flags[CPU.REGISTER_FLAGS_CF] = false;
                cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
                
                // Execute XOR on reg,reg or mem,reg. Determine this from mm bits of addressbyte
                if (((addressByte >> 6) & 0x03) == 3)
                {
                    // XOR reg,imm
                    // Determine destination register from addressbyte, ANDing it with 0000 0111
                    destinationRegister = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
                    
                    destinationRegister[CPU.REGISTER_GENERAL_LOW] ^= sourceValue[CPU.REGISTER_GENERAL_LOW];
                    destinationRegister[CPU.REGISTER_GENERAL_HIGH] ^= sourceValue[CPU.REGISTER_GENERAL_HIGH];
                    
                    // Set ZF
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = destinationRegister[CPU.REGISTER_GENERAL_LOW] == 0 && destinationRegister[CPU.REGISTER_GENERAL_HIGH] == 0? true : false;
                    // Test SF, only applies to LOW (set when MSB is 1, occurs when AL >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
                    // Set PF, only applies to LOW
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(destinationRegister[CPU.REGISTER_GENERAL_LOW]);
                }
                else
                {
                    // XOR mem,imm
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);
    
                    // Get byte from memory and XOR source register
                    tempResult = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
                    
                    tempResult[CPU.REGISTER_GENERAL_LOW] ^= sourceValue[CPU.REGISTER_GENERAL_LOW];
                    tempResult[CPU.REGISTER_GENERAL_HIGH] ^= sourceValue[CPU.REGISTER_GENERAL_HIGH];
                    
                    // Store result back in memory reference location 
                    cpu.setWordInMemorySegment(addressByte, memoryReferenceLocation, tempResult);
    
                    // Set ZF
                    cpu.flags[CPU.REGISTER_FLAGS_ZF] = tempResult[CPU.REGISTER_GENERAL_LOW] == 0 && tempResult[CPU.REGISTER_GENERAL_HIGH] == 0? true : false;
                    // Test SF, only applies to LOW (set when MSB is 1, occurs when AL >= 0x80)
                    cpu.flags[CPU.REGISTER_FLAGS_SF] = tempResult[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
                    // Set PF, only applies to LOW
                    cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(tempResult[CPU.REGISTER_GENERAL_LOW]);
                }
                break;  // XOR
                
            case 7: // CMP
                // Determine source2 value from mm bits of addressbyte
                if (((addressByte >> 6) & 0x03) == 3)
                {
                    // Source2 is a register
                    // Determine "destination" value from addressbyte, ANDing it with 0000 0111
                    sourceValue2 = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
                }
                else
                {
                    // Source2 is in memory
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);

                    // Get word from memory
                    sourceValue2 = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
                }
                
                // Proceed with compare
                
                // Perform substraction
                tempResult = Util.subtractWords(sourceValue2, sourceValue, 0);
                
                // Test AF
                cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_SUB(sourceValue2[CPU.REGISTER_GENERAL_LOW], tempResult[CPU.REGISTER_GENERAL_LOW]);  
                // Test CF
                cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_SUB(sourceValue2, sourceValue, 0);
                // Test OF
                cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB(sourceValue2, sourceValue, tempResult, 0);
                // Test ZF, is tested against tempResult
                cpu.flags[CPU.REGISTER_FLAGS_ZF] = tempResult[CPU.REGISTER_GENERAL_HIGH] == 0x00 && tempResult[CPU.REGISTER_GENERAL_LOW] == 0x00? true : false;
                // Test SF, only applies to lower byte (set when MSB is 1, occurs when tempResult >= 0x80)
                cpu.flags[CPU.REGISTER_FLAGS_SF] = tempResult[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
                // Set PF, only applies to lower byte
                cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(tempResult[CPU.REGISTER_GENERAL_LOW]);
                
                break;
                
            default:
                throw new CPUInstructionException("Immediate Group 1 (0x83) instruction no case match.");
        }
    }
}
