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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

//import org.jpc.emulator.memory.codeblock.*;
//import org.jpc.emulator.memory.codeblock.optimised.*;
//import org.jpc.classfile.*;
//import org.jpc.emulator.memory.codeblock.fastcompiler.*;

/**
 * @author Bram Lohman
 * @author Bart Kiers
 */
public class UCodeMethodParser implements MicrocodeSet {
    private Object[][][] operations;
    private int[][][] operandArray;
    private boolean[][] externalEffectsArray;
    private boolean[][] explicitThrowArray;

    private static Hashtable<String, Integer> microcodeIndex = new Hashtable<String, Integer>();
    private static Hashtable<String, Integer> elementIndex = new Hashtable<String, Integer>();
    private static Hashtable<String, Integer> opcodeIndex = new Hashtable<String, Integer>();
    private static Hashtable<String, Object> constantPoolIndex = new Hashtable<String, Object>();

    static {
        try {
            Field[] fields = MicrocodeSet.class.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                int mods = fields[i].getModifiers();
                if (!Modifier.isStatic(mods) || !Modifier.isPublic(mods)
                        || !Modifier.isFinal(mods))
                    continue;
                microcodeIndex.put(fields[i].getName(), new Integer(fields[i]
                        .getInt(null)));
            }
        } catch (Throwable t) {
            System.out.println("Warning: microcode lookup table not completed");
            t.printStackTrace();
        }

        try {
            Field[] fields = FASTCompiler.class.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                int mods = fields[i].getModifiers();
                if (!Modifier.isStatic(mods) || !Modifier.isFinal(mods)
                        || Modifier.isPrivate(mods))
                    continue;

                int value = fields[i].getInt(null);
                if ((value >= 0) && (value < FASTCompiler.ELEMENT_COUNT)) {
                    String name = fields[i].getName();
                    if (name.startsWith("PROCESSOR_ELEMENT_")) {
                        name = name.substring("PROCESSOR_ELEMENT_".length());
                        elementIndex.put(name, new Integer(value));
                    }
                }
            }
        } catch (Throwable t) {
            System.out.println("Warning: element lookup table not completed");
            t.printStackTrace();
        }

        try {
            Field[] fields = JavaOpcode.class.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                int mods = fields[i].getModifiers();
                if (!Modifier.isStatic(mods) || !Modifier.isPublic(mods)
                        || !Modifier.isFinal(mods))
                    continue;
                if (fields[i].getType() != Integer.TYPE)
                    continue;
                opcodeIndex.put(fields[i].getName(), new Integer(fields[i]
                        .getInt(null)));
            }
        } catch (Throwable t) {
            System.out.println("Warning: opcode lookup table not completed");
            t.printStackTrace();
        }

        try {
            constantPoolIndex.put("IMMEDIATE",
                    ProtectedModeBytecodeFragments.IMMEDIATE);
            constantPoolIndex.put("X86LENGTH",
                    ProtectedModeBytecodeFragments.X86LENGTH);

            Method[] methods = UCodeStaticMethods.class.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                constantPoolIndex.put(methods[i].getName(),
                        new ConstantPoolSymbol(methods[i]));
            }
        } catch (Throwable t) {
            System.out
                    .println("Warning: constant pool lookup table not completed");
            t.printStackTrace();
        }
    }

    /**
     * @param operations
     * @param operandArray
     * @param externalEffectsArray
     * @param explicitThrowArray
     */
    public UCodeMethodParser(Object[][][] operations, int[][][] operandArray,
                             boolean[][] externalEffectsArray, boolean[][] explicitThrowArray)
    {
        this.operations = operations;
        this.operandArray = operandArray;
        this.externalEffectsArray = externalEffectsArray;
        this.explicitThrowArray = explicitThrowArray;
    }

    private void syntaxError(String message)
    {
        throw new IllegalStateException(message);
    }

    private void printFragmentArrays()
    {
        for (int i = 0; i < MICROCODE_LIMIT; i++)
            for (int j = 0; j < FASTCompiler.ELEMENT_COUNT; j++) {
                if (operations[i][j] != null) {
                    for (int k = 0; k < operations[i][j].length; k++)
                        System.out.println("operations " + i + "," + j + ":"
                                + operations[i][j][k]);
                    System.out.println("exef " + externalEffectsArray[i][j]);
                    System.out.println("excp " + explicitThrowArray[i][j]);
                }
                if (operandArray[i][j] != null)
                    for (int k = 0; k < operandArray[i][j].length; k++)
                        System.out.println("operandArray " + i + "," + j + ":"
                                + operandArray[i][j][k]);
            }
    }

    private void printIndexs()
    {
        System.out.println();
        System.out.println("microcodeIndex");
        Enumeration<String> en = microcodeIndex.keys();
        while (en.hasMoreElements())
            System.out.println(en.nextElement());
        System.out.println();

        System.out.println("elementIndex");
        en = elementIndex.keys();
        while (en.hasMoreElements())
            System.out.println(en.nextElement());
        System.out.println();

        System.out.println("opcodeIndex");
        en = opcodeIndex.keys();
        while (en.hasMoreElements())
            System.out.println(en.nextElement());
        System.out.println();

        System.out.println("constantPoolIndex");
        en = constantPoolIndex.keys();
        while (en.hasMoreElements())
            System.out.println(en.nextElement());
        System.out.println();

    }

    private void insertIntoFragmentArrays(String uCodeName, String resultName,
                                          String[] args, boolean externalEffect, boolean explicitThrow,
                                          Vector<Integer> instructions)
    {
        try {
            Integer codeVal = microcodeIndex.get(uCodeName);
            if (codeVal == null)
                syntaxError("Unknown microcode " + uCodeName);

            int uCode = codeVal.intValue();
            if (operations[uCode] == null)
                operations[uCode] = new Object[FASTCompiler.ELEMENT_COUNT][];
            if (operandArray[uCode] == null)
                operandArray[uCode] = new int[FASTCompiler.ELEMENT_COUNT][];

            Integer elementValue = (Integer) elementIndex.get(resultName);
            if (elementValue == null)
                syntaxError("Unknown PROCESSOR_ELEMENT " + resultName);
            int elementId = elementValue.intValue();

            int[] argIds = new int[args.length];
            for (int i = 0; i < argIds.length; i++)
                argIds[i] = ((Integer) elementIndex.get(args[i])).intValue();

            operandArray[uCode][elementId] = argIds;

            operations[uCode][elementId] = instructions.toArray();

            externalEffectsArray[uCode][elementId] = externalEffect;
            explicitThrowArray[uCode][elementId] = explicitThrow;
        } catch (Exception e) {
            System.out.println("Warning: exception loading uCode fragments");
            System.out.print("Fragment: " + resultName + " " + uCodeName + " ");
            for (int i = 0; i < args.length; i++)
                System.out.print(args[i] + " ");
            System.out.println();
            e.printStackTrace();
        }

    }

    @SuppressWarnings("unchecked")
    private void parseMethod(Method m)
    {
        String name = m.getName();

        int pos = 0;
        String result = null;
        pos = name.indexOf('_');
        result = name.substring(0, pos).toUpperCase();
        pos++;

        int start = pos;
        boolean externalEffect = false;
        pos = name.indexOf('_', start);
        if (name.substring(start, pos).equals("hef"))
            externalEffect = true;
        pos++;
        start = pos;

        boolean explicitThrow = false;
        Class[] clzs = m.getExceptionTypes();
        for (int i = 0; i < clzs.length; i++) {
            if (clzs[i] == ProcessorException.class) {
                explicitThrow = true;
                break;
            }
        }

        int argc = m.getParameterTypes().length;
        String[] args = new String[argc];
        int end = name.length();
        for (int i = argc - 1; i >= 0; i--) {
            pos = name.lastIndexOf('_', end - 1);
            args[i] = name.substring(pos + 1, end).toUpperCase();
            end = pos;
        }

        String uCode = name.substring(start, end);

        Vector instructions = new Vector();
        int newArgc = argc;
        for (int i = 0; i < argc; i++) {
            if (constantPoolIndex.containsKey(args[i])) {
                instructions.add(new Integer(JavaOpcode.LDC));
                instructions.add(constantPoolIndex.get(args[i]));
                args[i] = null;
                newArgc--;
            }
        }

        instructions.add(new Integer(JavaOpcode.INVOKESTATIC));
        instructions.add(constantPoolIndex.get(name));
        if (result.equals("EXECUTECOUNT")) {
            instructions.add(new Integer(JavaOpcode.ILOAD));
            instructions.add(new Integer(
                    FASTCompiler.VARIABLE_EXECUTE_COUNT_INDEX));
            instructions.add(new Integer(JavaOpcode.IADD));
            instructions.add(new Integer(JavaOpcode.ISTORE));
            instructions.add(new Integer(
                    FASTCompiler.VARIABLE_EXECUTE_COUNT_INDEX));
        }

        String[] newArgs;
        if (newArgc < argc) {
            newArgs = new String[newArgc];
            int j = 0;
            for (int i = 0; i < argc; i++) {
                if (args[i] != null)
                    newArgs[j++] = args[i];
            }
        } else {
            newArgs = args;
        }

        insertIntoFragmentArrays(uCode, result, newArgs, externalEffect,
                explicitThrow, instructions);
    }

    /**
     * @return -
     */
    public int parse()
    {
        Method[] methods = UCodeStaticMethods.class.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (Modifier.isPrivate(methods[i].getModifiers()))
                continue;
            parseMethod(methods[i]);
        }

        return methods.length;
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        // If using main(), make sure the static in BytecodeFragments that cause
        // this to parse() is disabled!!

        UCodeMethodParser p = new UCodeMethodParser(
                new Object[MICROCODE_LIMIT][FASTCompiler.ELEMENT_COUNT][],
                new int[MICROCODE_LIMIT][FASTCompiler.ELEMENT_COUNT][],
                new boolean[MICROCODE_LIMIT][FASTCompiler.ELEMENT_COUNT],
                new boolean[MICROCODE_LIMIT][FASTCompiler.ELEMENT_COUNT]);

        p.printIndexs();
        System.out.println(p.parse());
        p.printFragmentArrays();
    }
}

/*
 * Fragment format:
 * 
 * public static int reg1_nef_load1_iw(int immediate) { return immediate &
 * 0xffff; }
 * 
 * operands = immediate resultElement = reg1 uCode = load1_iw externalEffect =
 * false operations = ldc [immediate], invokestatic [reg1_load1_iw]
 * 
 * 
 * 
 * public static int reg0_hef_load0_bp(int ebp) { return ebp & 0xffff; }
 * 
 * operands = ebp resultElement = reg0 uCode = load0_bp externalEffect = true
 * //body doesn't really, but that what hef means operations = invokestatic
 * [reg1_load1_iw]
 * 
 * <[result]>_<exteralEffect>_<uCode>_<[operands1]>_...
 * 
 * last operand is top of the stack result and operands are optional
 * 
 * non-element operands (these mean there needs to be ldc's in the operations):
 * immediate x86count ioports
 */

