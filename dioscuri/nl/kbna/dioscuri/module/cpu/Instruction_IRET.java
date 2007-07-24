/*
 * $Revision: 1.3 $ $Date: 2007-07-24 14:41:36 $ $Author: blohman $
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
 * Intel opcode CF<BR>
 * Interrupt return.<BR>
 * Returns from an interrupt or exception handler and restores IP, CS and flags.<BR>
 * Flags modified: all
 */
public class Instruction_IRET implements Instruction
{

    // Attributes
    private CPU cpu;

    boolean operandWordSize;
    
    byte[] newCS;
    byte[] newIP;
    byte[] newFlags;

    
    // Constructors
    /**
     * Class constructor
     */
    public Instruction_IRET()
    {
        operandWordSize = true;
        
        newCS = new byte[2];
        newIP = new byte[2];
        newFlags = new byte[2];
    }
    
    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_IRET(CPU processor)
    {
        this();
        
        // Create reference to cpu class
        cpu = processor;
    }

    
    // Methods

    /**
     * Returns from an interrupt or exception handler and restores IP, CS and flags.<BR>
     */
    public void execute()
    {
        // Pop IP, CS and flags in reverse order as they were pushed (i.e. by INT instruction)
        
        // Pop IP (16-bit) from stack
        newIP = cpu.getWordFromStack();
        cpu.ip[CPU.REGISTER_LOW] = newIP[CPU.REGISTER_LOW];
        cpu.ip[CPU.REGISTER_HIGH] = newIP[CPU.REGISTER_HIGH];
        
        // Pop CS (16-bit) from stack
        newCS = cpu.getWordFromStack();
        cpu.cs[CPU.REGISTER_SEGMENT_LOW] = newCS[CPU.REGISTER_LOW];
        cpu.cs[CPU.REGISTER_SEGMENT_HIGH] = newCS[CPU.REGISTER_HIGH];
        
        // Pop flags register (16-bit) from stack
        newFlags = cpu.getWordFromStack();
        
        // Convert flagbytes into booleans and store them in flags register
        cpu.flags = Util.bytesToBooleans(newFlags);
        
        // TODO: Set IF flag?
    }
}
