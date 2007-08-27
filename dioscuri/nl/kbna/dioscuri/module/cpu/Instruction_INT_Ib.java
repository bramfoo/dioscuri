/*
 * $Revision: 1.6 $ $Date: 2007-08-27 07:43:34 $ $Author: blohman $
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
 * Intel opcode CD<BR>
 * Call to Interrupt Procedure.<BR>
 * The immediate byte specifies the index (0 - 255) within the Interrupt Descriptor Table (IDT).<BR>
 * Flags modified: IF, TF, AC
 */
public class Instruction_INT_Ib implements Instruction
{

    // Attributes
    private CPU cpu;

    boolean operandWordSize;
    
    int index;
    int offset;

    byte[] newCS;
    byte[] newIP;

    
    // Constructors
    /**
     * Class constructor
     */
    public Instruction_INT_Ib()
    {
        operandWordSize = true;
        
        index = 0;
        offset = 0;

        newCS = new byte[2];
        newIP = new byte[2];
    }
    
    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_INT_Ib(CPU processor)
    {
        this();
        
        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Call the interrupt procedure based on the interrupt vector in the IDT.<BR>
     */
    public void execute()
    {
        // Retrieve immediate byte (index for IDT) from memory
        index = (((int) cpu.getByteFromCode()) & 0xFF);
        
        // Check if index is in range of IDT (0 - 255)
        if (index <= 255)
        {
            // Push flags register (16-bit) onto stack
            cpu.setWordToStack(Util.booleansToBytes(cpu.flags));
            
            // Clear flags IF, TF, (and Alignment Check AC, but is not implemented on 16-bit)
            cpu.flags[CPU.REGISTER_FLAGS_IF] = false;
            cpu.flags[CPU.REGISTER_FLAGS_TF] = false;
            
            // Push current code segment and instruction pointer onto stack
            cpu.setWordToStack(cpu.cs);
            cpu.setWordToStack(cpu.ip);
            
            // Retrieve the interrupt vector (IP:CS) from the IDT, based on the index
            // Reset the CS and IP to interrupt vector in IDT
            cpu.cs = new byte[] {0x00, 0x00};   // refer to beginning of code segment
            offset = index * 4;                 // define offset from code segment (index * 4 bytes)
            cpu.ip = new byte[] {(byte)((offset >> 8) & 0xFF), (byte)(offset & 0xFF)};

            // Fetch IP value
            newIP = cpu.getWordFromCode();

            // Increment offset by 2 bytes and fetch CS
            offset += 2;
            cpu.ip = new byte[] {(byte)((offset >> 8) & 0xFF), (byte)(offset & 0xFF)};
            newCS = cpu.getWordFromCode();
            
            // Assign new CS and IP to registers pointing to interrupt procedure
            cpu.cs[CPU.REGISTER_SEGMENT_LOW] = newCS[CPU.REGISTER_LOW];
            cpu.cs[CPU.REGISTER_SEGMENT_HIGH] = newCS[CPU.REGISTER_HIGH];
            cpu.ip[CPU.REGISTER_LOW] = newIP[CPU.REGISTER_LOW];
            cpu.ip[CPU.REGISTER_HIGH] = newIP[CPU.REGISTER_HIGH];
        }
        else
        {
            //TODO: exception because index is out of range of IDT.
        }
        
    }
}
