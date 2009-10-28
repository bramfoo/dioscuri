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
 * Intel opcode C6<BR>
 * Group 11 opcode extension: MOV immediate byte (source) into memory/register (destination).<BR>
 * Performs the selected instruction (indicated by bits 5, 4, 3 of the ModR/M byte) using immediate data.<BR>
 * NOTE: Only one instruction in group (MOV EbIb, reg=000).<BR>
 * Flags modified: none
 */
public class Instruction_GRP11_MOV_EbIb implements Instruction
{

    // Attributes
    private CPU cpu;

    boolean operandWordSize;

    byte addressByte;
    byte[] memoryReferenceDisplacement;
    byte[] memoryReferenceLocation;

    byte[] destinationRegister;
    int intermediateResult;
    byte displacement;
    byte registerHighLow;

    
    // Constructors
    /**
     * Class constructor
     */
    public Instruction_GRP11_MOV_EbIb()
    {
        operandWordSize = false;

        addressByte = 0;
        memoryReferenceDisplacement = new byte[2];
        memoryReferenceLocation = new byte[2];

        destinationRegister = new byte[2];
        intermediateResult = 0;
        displacement = 0;
        registerHighLow = 0;
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_GRP11_MOV_EbIb(CPU processor)
    {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * MOV immediate byte into memory/register.<BR>
     * @throws CPUInstructionException 
     */
    public void execute() throws CPUInstructionException
    {
        // Get addresByte
        addressByte = cpu.getByteFromCode();

        // Execute instruction decoded from nnn (bits 5, 4, 3 in ModR/M byte)
        if (((addressByte & 0x38) >> 3) == 0x00)
        {
            // 000
            // Execute MOV on reg,reg or mem,reg. Determine this from mm bits of addressbyte
            if (((addressByte >> 6) & 0x03) == 3)
            {
                // MOV reg,reg
                // Determine destination register from addressbyte, ANDing it with 0000 0111
                destinationRegister = cpu.decodeRegister(operandWordSize, addressByte & 0x07);

                // Determine high/low part of register based on bit 3 (leading sss bit)
                registerHighLow = ((addressByte & 0x04) >> 2) == 0 ? (byte) CPU.REGISTER_GENERAL_LOW : (byte) CPU.REGISTER_GENERAL_HIGH;

                // MOV source to destination
                destinationRegister[registerHighLow] = cpu.getByteFromCode();
            }
            else
            {
                // MOV mem,reg
                // Determine IP displacement of memory location (if any) 
                memoryReferenceDisplacement = cpu.decodeMM(addressByte);

                // Determine memory location
                memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);

                // Store next immediate in memory reference location
                cpu.setByteInMemorySegment(addressByte, memoryReferenceLocation, cpu.getByteFromCode());
            }
        }
        else
        {
            // Throw exception for illegal nnn bits
            throw new CPUInstructionException("Group 11 (0xC6) instruction illegal nnn bits.");
        }
    }
}
