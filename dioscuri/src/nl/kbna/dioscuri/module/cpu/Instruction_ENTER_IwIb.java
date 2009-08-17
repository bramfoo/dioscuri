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
    byte nestingLevel;
    byte[] frameTemp;
    byte[] eFrameTemp;
    byte[] stackSizeWord;
    
    byte[] word0x0001;
    byte[] word0x0002;
    byte[] word0x0004;
    
    byte[] memoryReferenceLocation;
    byte[] memoryReferenceDisplacement;

    byte[] sourceValue;
    byte[] oldValue;
    byte[] destinationRegister;
    int internalCarry;

    byte[] temp;
    byte[] eTemp;

    
    // Constructors
    /**
     * Class constructor
     */
    public Instruction_ENTER_IwIb()
    {
        operandWordSize = true;

        addressByte = 0;
        nestingLevel = 0;
        frameTemp = new byte[2];
        eFrameTemp = new byte[2];
        stackSizeWord = new byte[2];
        
        word0x0001 = new byte[] {0x00, 0x01};
        word0x0002 = new byte[] {0x00, 0x02};
        word0x0004 = new byte[] {0x00, 0x04};
        
        memoryReferenceLocation = new byte[2];
        memoryReferenceDisplacement = new byte[2];

        sourceValue = new byte[2];
        oldValue = new byte[2];
        destinationRegister = new byte[2];
        internalCarry = 0;

        temp = new byte[2];
        eTemp = new byte[2];
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
     * Takes care of nesting level (0, 1 or higher)
     */
    public void execute()
    {
        System.out.println("CPU -> instruction ENTER");

        // Get stack-size operand
        cpu.stackSize = cpu.getByteFromCode();

        // Get nesting level operand and limit it to 32
        nestingLevel = (byte) ((cpu.getByteFromCode()) % 32);
        
        // Check if stacksize is 32 or 16 bit
        if (cpu.stackSize == 32)
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
                if (cpu.stackSize == 32)
                {
                    // Stack size is 32
                    // Set amount of subtraction from BP to 4

                    //TODO: implement
                }
                else
                {
                    // Stack size is 16
                    for (int i = 1; i < nestingLevel - 1; i++)
                    {
                        temp = Util.subtractWords(cpu.bp, word0x0002, 0);
                        System.arraycopy(temp, 0, cpu.bp, 0, temp.length);

                        // Stack size 16 bit
                        System.out.println("CPU -> instruction ENTER in 16-bit stacksize in nesting " + i);
                        
                        // Push BP on stack
                        cpu.setWordToStack(cpu.bp);
                    }
                    
                    // Push frameTemp on stack
                    cpu.setWordToStack(frameTemp);
                }
            }
        }
        
        if (cpu.stackSize == 32)
        {
            // Stack size is 32
            // Copy eFrameTemp to eBP
            System.arraycopy(frameTemp, 0, cpu.bp, 0, frameTemp.length);
            System.arraycopy(eFrameTemp, 0, cpu.ebp, 0, eFrameTemp.length);
            
            // Decrement eBP with stacksize
            stackSizeWord = new byte[] { 0x00, (byte)cpu.stackSize };
			temp = Util.subtractWords(cpu.bp, stackSizeWord, 0);
			if (Util.test_CF_SUB(cpu.bp, stackSizeWord, 0) == true)
			{
				eTemp = Util.subtractWords(cpu.ebp, word0x0001, 0);
			}
			else
			{
				eTemp = cpu.ebp;
			}
			
			// Assign eBP to eSP
            System.arraycopy(temp, 0, cpu.sp, 0, temp.length);
            System.arraycopy(eTemp, 0, cpu.esp, 0, eTemp.length);
        }
        else
        {
            // Stack size is 16
            // Copy frameTemp to BP
            System.arraycopy(frameTemp, 0, cpu.bp, 0, frameTemp.length);
            
            // Decrement stack pointer
            temp = Util.subtractWords(cpu.bp, stackSizeWord, 0);
            
            // Assign BP to SP
            System.arraycopy(temp, 0, cpu.sp, 0, temp.length);
        }
    }
}
