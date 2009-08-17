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
     * Intel opcode 62<BR>
     * Check array index against bounds.<BR>
     * Flags modified: none
     */
public class Instruction_BOUND_GvMa implements Instruction {

    // Attributes
    private CPU cpu;
    
    boolean operandWordSize = true;
    
    byte addressByte = 0;
    byte[] memoryReferenceLocation = new byte[2];
    byte[] memoryReferenceDisplacement = new byte[2];

    byte[] sourceValue = new byte[2];
    int arrayIndex;
    int lowerBoundary;
    int higherBoundary;

    
    // Constructors
    /**
     * Class constructor
     */
    public Instruction_BOUND_GvMa()   {}
    
    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_BOUND_GvMa(CPU processor)
    {
        this();
        
        // Create reference to cpu class
        cpu = processor;
    }

    
    // Methods

    /**
     * Check array index against bounds.<BR>
     */
    public void execute()
    {
        // Get addresByte
        addressByte = cpu.getByteFromCode();

        // Get array index (register) from addressbyte. AND it with 0011 1000 and right-shift 3 to get rrr bits
        sourceValue = (cpu.decodeRegister(operandWordSize, (addressByte & 0x38) >> 3));
        arrayIndex = (sourceValue[CPU.REGISTER_GENERAL_HIGH] << 8) + sourceValue[CPU.REGISTER_GENERAL_LOW];
        
        // Determine displacement of memory location (if any) 
        memoryReferenceDisplacement = cpu.decodeMM(addressByte);
        
        // Determine memory location
        memoryReferenceLocation = cpu.decodeSSSMemDest(addressByte, memoryReferenceDisplacement);

        // Get lower boundary (word from memory)
        sourceValue = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
        lowerBoundary = (sourceValue[CPU.REGISTER_GENERAL_HIGH] << 8) + sourceValue[CPU.REGISTER_GENERAL_LOW];
        
        // Increment memory location with 2
        memoryReferenceLocation = Util.addWords(memoryReferenceLocation, new byte[] { 0x00, 0x02 }, 0);
        
        // Get higher boundary (word from memory)
        sourceValue = cpu.getWordFromMemorySegment(addressByte, memoryReferenceLocation);
        higherBoundary = (sourceValue[CPU.REGISTER_GENERAL_HIGH] << 8) + sourceValue[CPU.REGISTER_GENERAL_LOW];

        // Check if array index is between (or equals) the lower and higher boundaries
        if (arrayIndex < lowerBoundary || arrayIndex > higherBoundary)
        {
            // FIXME: throw exception #BR
        }
    }
}
