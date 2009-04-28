/*
    JPC: A x86 PC Hardware Emulator for a pure Java Virtual Machine
    Release Version 2.0

    A project from the Physics Dept, The University of Oxford

    Copyright (C) 2007 Isis Innovation Limited

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 2 as published by
    the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 
    Details (including contact information) can be found at: 

    www.physics.ox.ac.uk/jpc
*/
package nl.kbna.dioscuri.module.cpu32;

//import org.jpc.emulator.memory.codeblock.optimised.*;
//import org.jpc.emulator.memory.*;

public class CodeBlockCombiner
{
    private CodeBlockFactory factory;
    private RealModeUDecoder decoder = new RealModeUDecoder();
    private ByteSourceWrappedMemory source = new ByteSourceWrappedMemory();
    private int depth;

    public CodeBlockCombiner(CodeBlockFactory factory)
    {
        this.factory = factory;
    }

    public RealModeCodeBlock getRealModeCodeBlockAt(Memory memory, int offset)
    {
        source.set(memory, offset & AddressSpace.BLOCK_MASK);
        RealModeCodeBlock block = null;

        //depth check
        depth = 0;
        
        try
        {
            block = combineCodeBlocks(source);
        }
        catch (Exception e)
        {
            return null;
        }
        return block;
    }
    
    private RealModeCodeBlock combineCodeBlocks(ByteSourceWrappedMemory source)
    {
        int start = source.getOffset();

        //decode initial block
        InstructionSource source0 = decoder.decodeReal(source);

        //go through instruction source and get out microcodes
        int[] microcodes = new int[15];
        int i = 0;
        
        try
        {
            while (source0.getNext())
            {
                int operationLength = source0.getLength();
                for (int j = 0; j < operationLength; j++)
                {
                    if (i == microcodes.length)
                    {
                        int[] temp = new int[microcodes.length + 20];
                        System.arraycopy(microcodes,0,temp,0,microcodes.length);
                        microcodes=temp;
                    }
                    microcodes[i] = source0.getMicrocode();
                    i++;
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException f)
        {
            return null;
        }
        catch (Exception e)
        {
            return null;
        }
        
        RealModeCodeBlock block0;
        source.reset();
        int relStart = start - source.getOffset();
        source.skip(relStart);
        try
        {
            block0 = factory.getRealModeCodeBlock(source);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            return null;
        }
        catch (Exception e)
        {
            return null;
        }

        if (block0 instanceof SpanningCodeBlock) //dont' think this is necessary
            return null;
 
        //begin checks ************************************
        if (i < 5)
        {
            //System.out.println("block too small");
            return block0;
        }
        if ((microcodes[i-3] != 8))// && (microcodes[i-3] != 13))
        {
            //System.out.println("not load0_IB/W");
            return block0;
        }
        int jumpType = microcodes[i-1];
        int jumpSize = microcodes[i-2];
        //check we're doing the right kind of jump
        if ((jumpType != MicrocodeSet.JZ_O8) && (jumpType != MicrocodeSet.JNZ_O8) && (jumpType != MicrocodeSet.JUMP_O8) && (jumpType != MicrocodeSet.CALL_O16_A16))
        {
            //System.out.println("wrong jump");
            return block0;
        }     
        //check jump doesn't go before the start of the first block in this tree of combined blocks
        if (jumpSize + source0.getLength() < 0)
        {
            //System.out.println("negative jump");
            return block0;
        }
        //check jump is small enough
        if (jumpSize > 255)  //************check this
        {
            //System.out.println("big jump");
            return block0;
        }
        //end checks ***************************************

        RealModeCodeBlock block1, block2;                    
        //get other blocks and make sure they aren't spanning ones
        try
        {
            depth++;
            if (depth < 6)
            {
                block1 = combineCodeBlocks(source);
                source.reset();
                source.skip(relStart + block0.getX86Length() + jumpSize);
                block2 = combineCodeBlocks(source);
            }
            else
            {
                block1 = factory.getRealModeCodeBlock(source);
                source.reset();
                source.skip(relStart + block0.getX86Length() + jumpSize);
                block2 = factory.getRealModeCodeBlock(source);
            }
        }
        catch (ArrayIndexOutOfBoundsException g)
        {
            return block0;
        }
        catch (Exception e)
        {
            return block0;
        }
        
        //create new code block that wraps 3 code blocks
        if ((block1 == null) || (block2 == null))
            return block0;

        if ((block1 instanceof SpanningCodeBlock) ||(block2 instanceof SpanningCodeBlock))
            return block0;

        //check the three blocks are within the code 4k code segment
        if ((start + block0.getX86Length() + block1.getX86Length() > 4095) || (start + block0.getX86Length() + jumpSize + block2.getX86Length() > 4095))
            return block0;
 
        CombiningRealCodeBlock combinedBlock = new CombiningRealCodeBlock(block0, block1, block2, start, jumpSize);
        //System.out.println("*********************************************************made combined block");
        
        //System.out.println("b0:" + start + "," + block0.getX86Length()+ " b1:" + (start+block0.getX86Length()) + ","+ block1.getX86Length()+" b2:" + (start + block0.getX86Length() + jumpSize) +"," + block2.getX86Length());

        return combinedBlock;
    }

    public ProtectedModeCodeBlock getProtectedModeCodeBlock(ByteSource source, boolean operandSize)
    {
        return factory.getProtectedModeCodeBlock(source, operandSize);
    }
}
