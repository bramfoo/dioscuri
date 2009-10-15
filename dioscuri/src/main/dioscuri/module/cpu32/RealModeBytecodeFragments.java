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
package dioscuri.module.cpu32;

//import org.jpc.emulator.memory.codeblock.fastcompiler.BytecodeFragments;
//import org.jpc.emulator.memory.codeblock.fastcompiler.FASTCompiler;

public class RealModeBytecodeFragments extends BytecodeFragments
{
    private static Object[][][] operationArray = new Object[MICROCODE_LIMIT][][];
    private static int[][][] operandArray = new int[MICROCODE_LIMIT][FASTCompiler.ELEMENT_COUNT][];

    private static boolean[][] externalEffectsArray = new boolean[MICROCODE_LIMIT][FASTCompiler.ELEMENT_COUNT];
    private static boolean[][] explicitThrowArray = new boolean[MICROCODE_LIMIT][FASTCompiler.ELEMENT_COUNT];

    private RealModeBytecodeFragments()
    {
    }

    static 
    {
        try 
        {
            UCodeMethodParser p = new UCodeMethodParser(operationArray, operandArray, externalEffectsArray, explicitThrowArray);
            
            System.out.println("Parsed " + p.parse() + " uCodes from file");
        } 
        catch (Exception e) 
        {
            System.err.println("failed loading bytecodes from file:" + e);
            e.printStackTrace();
        }
    }


    public static Object[] getOperation(int element, int microcode, int x86Position)
    {
        Object[] ops = operationArray[microcode][element];
        if (ops == null)
            return null;

        Object[] temp = new Object[ops.length];
        System.arraycopy(ops, 0, temp, 0, temp.length);
        
        for (int i = 0; i < temp.length; i++) 
        {
            if (temp[i] == X86LENGTH) 
                temp[i] = integer(x86Position);
        }
        
        return temp;
    }

    public static Object[] getOperation(int element, int microcode, int x86Position, int immediate)
    {
        Object[] temp = getOperation(element, microcode, x86Position);
        if (temp == null)
            return null;

        for (int i = 0; i < temp.length; i++) 
        {
            if (temp[i] == IMMEDIATE)
                temp[i] = integer(immediate);
        }

        return temp;
    }

    public static Object[] getTargetsOf(int microcode)
    {
        return operationArray[microcode];
    }

    public static int[] getOperands(int element, int microcode)
    {
        return operandArray[microcode][element];
    }

    public static boolean hasExternalEffect(int element, int microcode)
    {
    return externalEffectsArray[microcode][element];
    }

    public static boolean hasExplicitThrow(int element, int microcode)
    {
    return explicitThrowArray[microcode][element];
    }
}
