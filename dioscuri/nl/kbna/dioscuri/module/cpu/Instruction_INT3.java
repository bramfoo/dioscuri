/*
 * $Revision: 1.4 $ $Date: 2007-07-24 15:00:59 $ $Author: jrvanderhoeven $
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

import java.util.Stack;

/**
 * Intel opcode CC<BR>
 * Call to Interrupt 3 - trap to debugger<BR>
 * Flags modified: IF, TF, AC
 */
public class Instruction_INT3 implements Instruction
{

    // Attributes
    private CPU cpu;

    boolean operandWordSize;
    
    byte index;
    int offset;

    byte[] newCS;
    byte[] newIP;

    
    // Constructors
    /**
     * Class constructor
     */
    public Instruction_INT3()
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
    public Instruction_INT3(CPU processor)
    {
        this();
        
        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Call interrupt procedure 3 (trap to debugger) based on the interrupt vector in the IDT.<BR>
     */
    public void execute()
    {
        
        // Set index for IDT to 3
        index = 0x03;
        
        // Turn off all prefixes
    	cpu.resetPrefixes();
        
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
        cpu.cs = new byte[] {0x00, 0x00}; // refer to beginning of code segment
        offset = index * 4; // define offset from code segment (index * 4 bytes)
        cpu.ip = new byte[] {(byte) ((offset >> 8) & 0xFF), (byte) (offset & 0xFF)};

        // Fetch IP value
        newIP = cpu.getWordFromCode();

        // Increment offset by 2 bytes and fetch CS
        offset += 2;
        cpu.ip = new byte[] {(byte) ((offset >> 8) & 0xFF), (byte) (offset & 0xFF)};
        newCS = cpu.getWordFromCode();

        // Assign new CS and IP to registers pointing to interrupt procedure
        cpu.cs[CPU.REGISTER_SEGMENT_LOW] = newCS[CPU.REGISTER_LOW];
        cpu.cs[CPU.REGISTER_SEGMENT_HIGH] = newCS[CPU.REGISTER_HIGH];
        cpu.ip[CPU.REGISTER_LOW] = newIP[CPU.REGISTER_LOW];
        cpu.ip[CPU.REGISTER_HIGH] = newIP[CPU.REGISTER_HIGH];
    }
}
