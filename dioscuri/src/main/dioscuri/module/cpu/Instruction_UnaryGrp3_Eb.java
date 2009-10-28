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

import dioscuri.exception.CPUInstructionException;

/**
 * Intel opcode F6<BR>
 * Unary Group 3 opcode extension: TEST, NOT, NEG, MUL, IMUL, DIV, IDIV.<BR>
 * Performs the selected instruction (indicated by bits 5, 4, 3 of the ModR/M byte).<BR>
 * Flags modified: depending on instruction can be any of: OF, SF, ZF, AF, PF, CF
 */
public class Instruction_UnaryGrp3_Eb implements Instruction
{

    // Attributes
    private CPU cpu;

    boolean operandWordSize = false;

    byte addressByte = 0;
    byte[] memoryReferenceLocation = new byte[2];
    byte[] memoryReferenceDisplacement = new byte[2];

    byte sourceByte1 = 0;
    byte sourceByte2 = 0;
    byte resultByte  = 0;
    byte[] sourceValue = new byte[2];
    byte[] destinationRegister = new byte[2];
    byte registerHighLow = 0;
    
    int quotient = 0;
    int remainder = 0;
    int overFlowCheck = 0;

    
    // Constructors
    /**
     * Class constructor
     */
    public Instruction_UnaryGrp3_Eb()
    {
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_UnaryGrp3_Eb(CPU processor)
    {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Execute any of the following Unary Group 3 opcode extension: TEST, NOT, NEG, MUL, IMUL, DIV, IDIV.<BR>
     * @throws CPUInstructionException 
     */
    public void execute() throws CPUInstructionException
    {
        // Get addresByte
        addressByte = cpu.getByteFromCode();

        // Determine displacement of memory location (if any)
        memoryReferenceDisplacement = cpu.decodeMM(addressByte);

        // Execute instruction decoded from nnn (bits 5, 4, 3 in ModR/M byte)
        switch ((addressByte & 0x38) >> 3)
        {
            case 0: // TEST
                // Clear appropriate flags
                cpu.flags[CPU.REGISTER_FLAGS_OF] = false;
                cpu.flags[CPU.REGISTER_FLAGS_CF] = false;
                // Clear AF flag as Bochs does
                cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
                
                // Retrieve source byte
                sourceByte1 = cpu.getByteFromCode();
                
                // Execute TEST on imm,reg or imm,mem. Determine this from mm bits of addressbyte
                if (((addressByte >> 6) & 0x03) == 3)
                {
                    // TEST imm,reg
                    // Determine source value from addressbyte, ANDing it with 0000 0111 to get sss bits
                    // Determine high/low part of register based on bit 3 (leading sss bit)
                    registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW : (byte) CPU.REGISTER_GENERAL_HIGH;
                    sourceByte2 = cpu.decodeRegister(operandWordSize, addressByte & 0x07)[registerHighLow];
                }
                else
                {
                    // TEST imm,mem
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);
    
                    // Retrieve source value from memory indicated by reference location
                    sourceByte2 = cpu.getByteFromMemorySegment(addressByte, memoryReferenceLocation);
                }
                
                // Logical TEST (AND) of source1 and source2
                resultByte = (byte) (sourceByte1 & sourceByte2);
                
                // Test ZF on particular byte of tempResult
                cpu.flags[CPU.REGISTER_FLAGS_ZF] = resultByte == 0 ? true : false;
                // Test SF on particular byte of tempResult (set when MSB is 1, occurs when tempResult >= 0x80)
                cpu.flags[CPU.REGISTER_FLAGS_SF] = resultByte  < 0 ? true : false;
                // Set PF on particular byte of tempResult
                cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(resultByte);
                break;
                
            case 1: // does not exist
                // Throw exception for illegal nnn bits
                throw new CPUInstructionException("Unary Group 3 (0xF6) illegal reg bits");
                
            case 2: // NOT: reverse each bit of r/m8
                    // Flags affected: none
                if (((addressByte >> 6) & 0x03) == 3)
                {
                    // NOT reg
                    // Determine source value from addressbyte, ANDing it with 0000 0111
                    sourceValue = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
    
                    // Determine high/low part of register based on bit 2 (leading sss bit)
                    registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW : (byte) CPU.REGISTER_GENERAL_HIGH;

                    // Negate source 8-bit
                    sourceValue[registerHighLow] = (byte) ~sourceValue[registerHighLow];
                }
                else
                {
                    // NOT mem
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);
    
                    // Get byte from memory
                    sourceByte1 = cpu.getByteFromMemorySegment(addressByte, memoryReferenceLocation);
                    
                    // Negate source 8-bit
                    sourceByte1 = (byte) ~sourceByte1;
    
                    // Store result in mem
                    cpu.setByteInMemorySegment(addressByte, memoryReferenceLocation, sourceByte1);
                }
                break;
                
            case 3: // NEG
                // Execute NEG on imm,reg or imm,mem. Determine this from mm bits of addressbyte
                if (((addressByte >> 6) & 0x03) == 3)
                {
                    // NEG imm,reg
                    // Determine source value from addressbyte, ANDing it with 0000 0111 to get sss bits
                    sourceValue = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
                    
                    // Determine high/low part of register based on bit 2 (leading sss bit)
                    registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW : (byte) CPU.REGISTER_GENERAL_HIGH;
                    
                    sourceByte1 = sourceValue[registerHighLow];
                }
                else
                {
                    // NEG mem,reg
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);

                    // Get byte from memory
                    sourceByte1 = cpu.getByteFromMemorySegment(addressByte, memoryReferenceLocation);
                }
                
                // Negate source; equivalent to 0 - source.
                sourceByte2 = (byte) (0 - sourceByte1);
                
                // Store result back in memory if necessary
                if (((addressByte >> 6) & 0x03) != 3)
                {
                    cpu.setByteInMemorySegment(addressByte, memoryReferenceLocation, sourceByte2);
                }
                else
                {
                    sourceValue[registerHighLow] = sourceByte2;
                }

                // Clear CF if source operand is zero, set otherwise
                cpu.flags[CPU.REGISTER_FLAGS_CF] = sourceByte2 == 0 ? false: true;  

                // Set OF flag
                cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB((byte) 0, sourceByte1, sourceByte2, 0); 
                // Set AF flag
                cpu.flags[CPU.REGISTER_FLAGS_AF] = (sourceByte2 & 0x0F) != 0 ? true : false;
                // Test ZF on result
                cpu.flags[CPU.REGISTER_FLAGS_ZF] = sourceByte2 == 0 ? true : false;
                // Test SF on result (set when MSB is 1, occurs when result >= 0x80)
                cpu.flags[CPU.REGISTER_FLAGS_SF] = sourceByte2 < 0 ? true : false;
                // Set PF on lower byte of result
                cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(sourceByte2);
                break;
                
            case 4: // MUL AX. Flags: OF, CF set to 0 if upper half of result is 0; SF, ZF, AF, PF are undefined.
                // Set destination to AX
                destinationRegister = cpu.ax;
                
                // Execute mul on reg or mem. Determine this from mm bits of addressbyte
                if (((addressByte >> 6) & 0x03) == 3)
                {
                    // MUL AX, reg
                    // Determine source value from addressbyte, ANDing it with 0000 0111
                    sourceValue = cpu.decodeRegister(operandWordSize, addressByte & 0x07);
                    
                    //FIXME: is it correct that we don't distinguish in lower or higher part of register as we're working with bytes???
                    
                    // Need to check for possible overflow in destination[low]
                    // This occurs when UNsigned values of source (=destination) > FF
                    // Also determine high/low register for sourceValue here
                    overFlowCheck = ((((int) (destinationRegister[CPU.REGISTER_GENERAL_LOW])) & 0xFF) * (((int) sourceValue[((addressByte & 0x07) > 3 ? CPU.REGISTER_GENERAL_HIGH : CPU.REGISTER_GENERAL_LOW)]) & 0xFF));
                }
                else
                {
                    // MUL AX, mem
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);
    
                    // Get byte from memory
                    sourceByte1 = cpu.getByteFromMemorySegment(addressByte, memoryReferenceLocation);
    
                    // Need to check for possible overflow in destination[low]
                    // This occurs when UNsigned values of source (=destination) > FF
                    overFlowCheck = ((((int) (destinationRegister[CPU.REGISTER_GENERAL_LOW])) & 0xFF) * (((int) sourceByte1) & 0xFF));
                }
    
                // Move answer into destination
                destinationRegister[CPU.REGISTER_GENERAL_LOW] = (byte) (overFlowCheck);
                destinationRegister[CPU.REGISTER_GENERAL_HIGH] = (byte) ((overFlowCheck) >> 8);
                
                // Set appropriate flags
                // Clear OF, CF if AX[HIGH] is zero 
                cpu.flags[CPU.REGISTER_FLAGS_OF] = cpu.flags[CPU.REGISTER_FLAGS_CF] = destinationRegister[CPU.REGISTER_GENERAL_HIGH] == 0 ? false : true;
                
                // Mimic Bochs' flag behaviour
                cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(destinationRegister[CPU.REGISTER_GENERAL_LOW]);
                cpu.flags[CPU.REGISTER_FLAGS_AF] = false;
                // Check on AL for the next two
                cpu.flags[CPU.REGISTER_FLAGS_ZF] = destinationRegister[CPU.REGISTER_GENERAL_LOW] == 0x00 ? true : false;
                cpu.flags[CPU.REGISTER_FLAGS_SF] = destinationRegister[CPU.REGISTER_GENERAL_LOW] < 0 ? true : false;
                break;  // MUL AL/eAX
                
            case 5:  // IMUL r/m8; flags affected: CF, OF.
                destinationRegister = cpu.ax;
                
                // Execute IMUL on reg or mem. Determine this from mm bits of addressbyte
                if (((addressByte & 0xC0) >> 6) == 3)
                {
                    // IMUL AX <- AL, reg
                    // Determine high/low part of register based on bit 3 (leading sss bit)
                    registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW : (byte) CPU.REGISTER_GENERAL_HIGH;

                    // Determine source byte from addressbyte, ANDing it with 0000 0111
                    sourceByte1 = cpu.decodeRegister(operandWordSize, addressByte & 0x07)[registerHighLow];
                }
                else
                {
                    // IMUL AX <- AL, mem
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);

                    // Get byte from memory
                    sourceByte1 = cpu.getByteFromMemorySegment(addressByte, memoryReferenceLocation);
                }
                
                // Multiply (signed) source and AL - this works directly because Java bytes are signed
                int signedResult = sourceByte1 * cpu.ax[CPU.REGISTER_GENERAL_LOW];
                
                // Move result into AX
                cpu.ax[CPU.REGISTER_GENERAL_HIGH] = (byte) (signedResult >> 8);
                cpu.ax[CPU.REGISTER_GENERAL_LOW] = (byte) (signedResult & 0xFF);
                
                // Clear CF, OF if result fits exactly AL;
                if (cpu.ax[CPU.REGISTER_GENERAL_HIGH] == 0x00)
                {
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = false;
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = false;
                }
                else    // otherwise set CF, OF.
                {
                    cpu.flags[CPU.REGISTER_FLAGS_CF] = true;
                    cpu.flags[CPU.REGISTER_FLAGS_OF] = true;
                }
                break;  // IMUL
                
            case 6: // DIV
                // Flags: OF, CF, SF, ZF, AF, PF are undefined.
                // FIXME: DIV by 0 not handled (#DE)
                // FIXME: DIV result exceeding register size not handled (#DE)
                // Set destination to AX
                destinationRegister = cpu.ax;
                
                // Execute DIV on reg or mem. Determine this from mm bits of addressbyte
                if (((addressByte & 0xC0) >> 6) == 3)
                {
                    // DIV AX, reg
                    // Determine high/low part of register based on bit 3 (leading sss bit)
                    registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW : (byte) CPU.REGISTER_GENERAL_HIGH;

                    // Determine source byte from addressbyte, ANDing it with 0000 0111
                    sourceByte1 = cpu.decodeRegister(operandWordSize, addressByte & 0x07)[registerHighLow];
                }
                else
                {
                    // DIV AX, mem
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);

                    // Get byte from memory
                    sourceByte1 = cpu.getByteFromMemorySegment(addressByte, memoryReferenceLocation);
                }
                
                // Calculate quotient
                quotient = ( (((((int)destinationRegister[CPU.REGISTER_GENERAL_HIGH]) & 0xFF)<<8) + (((int) destinationRegister[CPU.REGISTER_GENERAL_LOW]) & 0xFF)) 
                                 / (((int)sourceByte1) & 0xFF));

                // Calculate remainder
                remainder = ( (((((int)destinationRegister[CPU.REGISTER_GENERAL_HIGH]) & 0xFF)<<8) + (((int) destinationRegister[CPU.REGISTER_GENERAL_LOW]) & 0xFF)) 
                                % (((int)sourceByte1) & 0xFF));
                
                // Move quotient into AL
                destinationRegister[CPU.REGISTER_GENERAL_LOW] = (byte) (quotient);

                // Move remainder into AH
                destinationRegister[CPU.REGISTER_GENERAL_HIGH] = (byte) ((remainder));
                break;  // DIV
                
            case 7: // IDIV
                // Flags: OF, CF, SF, ZF, AF, PF are undefined.
                // FIXME: IDIV by 0 not handled (#DE)
                // FIXME: IDIV result exceeding register size not handled (#DE)
            	// FIXME: difference DIV and IDIV not clear!
                // Set destination to AX
                destinationRegister = cpu.ax;
                
                // Execute IDIV on reg or mem. Determine this from mm bits of addressbyte
                if (((addressByte & 0xC0) >> 6) == 3)
                {
                    // IDIV AX, reg
                    // Determine high/low part of register based on bit 3 (leading sss bit)
                    registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW : (byte) CPU.REGISTER_GENERAL_HIGH;

                    // Determine source byte from addressbyte, ANDing it with 0000 0111
                    sourceByte1 = cpu.decodeRegister(operandWordSize, addressByte & 0x07)[registerHighLow];
                }
                else
                {
                    // IDIV AX, mem
                    // Determine memory location
                    memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);

                    // Get byte from memory
                    sourceByte1 = cpu.getByteFromMemorySegment(addressByte, memoryReferenceLocation);
                }
                
                // Calculate quotient
                quotient = ( (((((int)destinationRegister[CPU.REGISTER_GENERAL_HIGH]))<<8) + (((int) destinationRegister[CPU.REGISTER_GENERAL_LOW]) & 0xFF)) 
                                 / (((int)sourceByte1) & 0xFF));

                // Calculate remainder
                remainder = ( (((((int)destinationRegister[CPU.REGISTER_GENERAL_HIGH]))<<8) + (((int) destinationRegister[CPU.REGISTER_GENERAL_LOW]) & 0xFF)) 
                                % (((int)sourceByte1) & 0xFF));
                
                // Move quotient into AL
                destinationRegister[CPU.REGISTER_GENERAL_LOW] = (byte) (quotient);

                // Move remainder into AH
                destinationRegister[CPU.REGISTER_GENERAL_HIGH] = (byte) ((remainder));
                break;  // IDIV
                
            default:
                // Throw exception for illegal nnn bits
                throw new CPUInstructionException("Unary Group 3 (0xF6) illegal reg bits");
        }
    }
}
