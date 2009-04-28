/*
 * $Revision: 1.1 $ $Date: 2007-08-20 15:20:21 $ $Author: jrvanderhoeven $
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
 * Intel opcode C9<BR>
 * LEAVE — High Level Procudure Exit.<BR>
 * Flags modified: none
 */
public class Instruction_LEAVE implements Instruction
{

    // Attributes
    private CPU cpu;
    
    byte[] destWord;
    byte[] destDoubleWord;

    
    // Constructors
    /**
     * Class constructor
     */
    public Instruction_LEAVE()
    {
        destWord = new byte[2];
        destDoubleWord = new byte[2];
    }
    
    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_LEAVE(CPU processor)
    {
        this();
        
        // Create reference to cpu class
        cpu = processor;
    }

    
    // Methods

    /**
     * LEAVE — High Level Procudure Exit.<BR>
     */
    public void execute()
    {
        System.out.println("CPU -> instruction LEAVE");

        // Check if stacksize is 32 or 16 bit
        if (cpu.stackSize == 32)
        {
            // Copy eBP to eSP
        	cpu.esp[CPU.REGISTER_GENERAL_HIGH] = cpu.ebp[CPU.REGISTER_GENERAL_HIGH];
        	cpu.esp[CPU.REGISTER_GENERAL_LOW] = cpu.ebp[CPU.REGISTER_GENERAL_LOW];
        	cpu.sp[CPU.REGISTER_GENERAL_HIGH] = cpu.bp[CPU.REGISTER_GENERAL_HIGH];
        	cpu.sp[CPU.REGISTER_GENERAL_LOW] = cpu.bp[CPU.REGISTER_GENERAL_LOW];
        }
        else
        {
            // Stack size is 16
            // Copy BP to SP
        	cpu.sp[CPU.REGISTER_GENERAL_HIGH] = cpu.bp[CPU.REGISTER_GENERAL_HIGH];
        	cpu.sp[CPU.REGISTER_GENERAL_LOW] = cpu.bp[CPU.REGISTER_GENERAL_LOW];
        }
        
        
        // Check operand size 32 or 16 bit
        if (cpu.doubleWord)
        {
            // Operand size is 32
            destWord = cpu.getWordFromStack();
            System.arraycopy(destWord, 0, cpu.bp, 0, destWord.length);
            destDoubleWord = cpu.getWordFromStack();
            System.arraycopy(destDoubleWord, 0, cpu.ebp, 0, destDoubleWord.length);
        }
        else
        {
            // Operand size is 16
            destWord = cpu.getWordFromStack();
            System.arraycopy(destWord, 0, cpu.bp, 0, destWord.length);
        }
    }
}
