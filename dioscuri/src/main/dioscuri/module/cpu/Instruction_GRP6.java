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
 * Intel opcode 0F 00<BR>
 * Group 6 opcode extension: SLDT, STR, LLDT, LTR, VERR, VERW.<BR>
 * Performs the selected instruction (indicated by bits 5, 4, 3 of the ModR/M byte).<BR>
 * Flags modified: depending on instruction can be any of: OF, SF, ZF, AF, PF, CF
 */
public class Instruction_GRP6 implements Instruction
{

    // Attributes
    private CPU cpu;

    boolean operandWordSize;

    byte addressByte;
    byte[] memoryReferenceLocation;
    byte[] memoryReferenceDisplacement;

    byte[] sourceValue;
    byte[] sourceValue2;
    byte[] oldDest;
    byte[] destinationRegister;
    int intermediateResult;

    int iCarryFlag;
    byte[] tempResult;
    byte[] temp;

    
    // Constructors
    /**
     * Class constructor
     */
    public Instruction_GRP6()
    {
        operandWordSize = true;

        addressByte = 0;
        memoryReferenceLocation = new byte[2];
        memoryReferenceDisplacement = new byte[2];

        sourceValue = new byte[2];
        sourceValue2 = new byte[2];
        oldDest = new byte[2];
        destinationRegister = new byte[2];
        intermediateResult = 0;

        iCarryFlag = 0;
        tempResult = new byte[2];
        temp = new byte[2];
    }

    
    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_GRP6(CPU processor)
    {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    
    // Methods

    /**
     * Execute any of the following Group 6 instructions: SLDT, STR, LLDT, LTR, VERR, VERW.<BR>
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
            case 0: // SLDT
                throw new CPUInstructionException("Group 6 (0x0F00) instruction SLDT not implemented.");
                
            case 1: // STR
                throw new CPUInstructionException("Group 6 (0x0F00) instruction STR not implemented.");

            case 2: // LLDT
                throw new CPUInstructionException("Group 6 (0x0F00) instruction LLDT not implemented.");

            case 3: // LTR
                throw new CPUInstructionException("Group 6 (0x0F00) instruction LTR not implemented.");

            case 4: // VERR
                throw new CPUInstructionException("Group 6 (0x0F00) instruction VERR not implemented.");

            case 5: // VERW
                throw new CPUInstructionException("Group 6 (0x0F00) instruction VERW not implemented.");

            default:
                // TODO Throw exception for illegal nnn bits
                throw new CPUInstructionException("Group 6 (0x0F00) instruction no case match.");
        }
    }
}
