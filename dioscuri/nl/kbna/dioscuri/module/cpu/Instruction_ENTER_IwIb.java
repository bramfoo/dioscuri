/*
 * $Revision: 1.1 $ $Date: 2007-07-02 14:31:31 $ $Author: blohman $
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
 * Intel opcode C8<BR>
 * ENTER — Make Stack Frame for Procedure Parameters.<BR>
 * Flags modified: none
 */
public class Instruction_ENTER_IwIb implements Instruction
{

    // Attributes
    private CPU cpu;
    
    boolean operandWordSize;

    byte addressByte;
    byte stackSize;
    byte nestingLevel;
    byte[] frameTemp;
    byte[] eFrameTemp;
    byte[] transition;
    
    byte[] memoryReferenceLocation;
    byte[] memoryReferenceDisplacement;

    byte[] sourceValue;
    byte[] oldValue;
    byte[] destinationRegister;
    int internalCarry;

    byte[] temp;

    
    // Constructors
    /**
     * Class constructor
     */
    public Instruction_ENTER_IwIb()
    {
        operandWordSize = true;

        addressByte = 0;
        stackSize = 0;
        nestingLevel = 0;
        frameTemp = new byte[2];
        eFrameTemp = new byte[2];
        transition = new byte[2];
        
        memoryReferenceLocation = new byte[2];
        memoryReferenceDisplacement = new byte[2];

        sourceValue = new byte[2];
        oldValue = new byte[2];
        destinationRegister = new byte[2];
        internalCarry = 0;

        temp = new byte[2];
    }
    
    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_ENTER_IwIb(CPU processor)
    {
        this();
        
        // Create reference to cpu class
        cpu = processor;
    }

    
    // Methods

    /**
     * ENTER — Make Stack Frame for Procedure Parameters.<BR>
     */
    public void execute()
    {
        System.out.println("CPU -> instruction ENTER");

        // Get stack-size operand
        stackSize = cpu.getByteFromCode();

        // Get nesting level operand and limit it to 32
        nestingLevel = (byte) ((cpu.getByteFromCode()) % 32);
        
        // Check if stacksize is 32 or 16 bit
        if (stackSize == 32)
        {
            System.out.println("CPU -> instruction ENTER in 32-bit stacksize not implemented completely");
            // Copy eBP on stack
            cpu.setWordToStack(cpu.ebp);
            cpu.setWordToStack(cpu.bp);
            
            // Copy eSP to temporary frame
            eFrameTemp[CPU.REGISTER_GENERAL_HIGH] = cpu.esp[CPU.REGISTER_GENERAL_HIGH];
            eFrameTemp[CPU.REGISTER_GENERAL_LOW] = cpu.esp[CPU.REGISTER_GENERAL_LOW];
            frameTemp[CPU.REGISTER_GENERAL_HIGH] = cpu.sp[CPU.REGISTER_GENERAL_HIGH];
            frameTemp[CPU.REGISTER_GENERAL_LOW] = cpu.sp[CPU.REGISTER_GENERAL_LOW];
        }
        else
        {
            // Stack size is 16
            // Copy BP on stack
            cpu.setWordToStack(cpu.bp);
            
            // Copy SP to temporary frame
            frameTemp[CPU.REGISTER_GENERAL_HIGH] = cpu.sp[CPU.REGISTER_GENERAL_HIGH];
            frameTemp[CPU.REGISTER_GENERAL_LOW] = cpu.sp[CPU.REGISTER_GENERAL_LOW];
        }
        
        // Distinct between 32 or 16 bit
        if (cpu.doubleWord)
        {
            // Operand size is 32
            System.out.println("CPU -> instruction ENTER in 16-bit stacksize and doubleword (32 bit) not implemented");
        }
        else
        {
            // Operand size is 16
            // Repeat for number of nesting levels
            if (nestingLevel > 0)
            {
                if (stackSize == 32)
                {
                    // Stack size is 32
                    // Set amount of subtraction from BP to 4
                    transition = new byte[] {0x00, 0x04};
                    //TODO: implement
                }
                else
                {
                    // Stack size is 16
                    // Set amount of subtraction from BP to 2
                    transition = new byte[] {0x00, 0x02};
                    
                    for (int i = 1; i < nestingLevel; i++)
                    {
                        temp = Util.subtractWords(cpu.bp, transition, 0);
                        System.arraycopy(temp, 0, cpu.bp, 0, temp.length);

                        // Stack size 16 bit
                        System.out.println("CPU -> instruction ENTER in 16-bit stacksize in nesting " + i);
                        
                        // Copy BP on stack
                        cpu.setWordToStack(cpu.bp);
                    }
                    
                    // Push frameTemp on stack
                    cpu.setWordToStack(frameTemp);
                }
            }
        }
        
        if (stackSize == 32)
        {
            // Stack size is 32
            // Copy frameTemp to eBP
            System.arraycopy(frameTemp, 0, cpu.bp, 0, frameTemp.length);
            System.arraycopy(eFrameTemp, 0, cpu.ebp, 0, eFrameTemp.length);
            // Decrement stack pointer
//FIXME:            temp = Util.subtractDoubleWords(cpu.bp, new byte[] {0x00, (byte)stackSize}, 0);
//            System.arraycopy(temp, 0, cpu.sp, 0, temp.length);
//            System.arraycopy(eTemp, 0, cpu.esp, 0, eTemp.length);
        }
        else
        {
            // Stack size is 16
            // Copy frameTemp to BP
            System.arraycopy(frameTemp, 0, cpu.bp, 0, frameTemp.length);
            // Decrement stack pointer
            temp = Util.subtractWords(cpu.bp, new byte[] {0x00, (byte)stackSize}, 0);
            System.arraycopy(temp, 0, cpu.sp, 0, temp.length);
        }
    }
}
