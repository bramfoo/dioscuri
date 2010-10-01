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

import java.io.IOException;
import java.util.Map;

//import org.jpc.classfile.*;
//import org.jpc.emulator.processor.*;
//
//import org.jpc.emulator.memory.codeblock.fastcompiler.ExceptionHandler;
//import org.jpc.emulator.memory.codeblock.fastcompiler.CountingOutputStream;

/**
 * @author Bram Lohman
 * @author Bart Kiers
 */
public class ProtectedModeExceptionHandler extends ExceptionHandler {
    /**
     * @param lastX86Position
     * @param initialNode
     * @param stateMap
     */
    public ProtectedModeExceptionHandler(int lastX86Position,
                                         ProtectedModeRPNNode initialNode, Map<Integer, RPNNode> stateMap)
    {
        super(lastX86Position, initialNode, stateMap);
    }

    /**
     * @param byteCodes
     * @param cf
     * @throws IOException
     */
    protected void writeHandlerRoutine(CountingOutputStream byteCodes,
                                       ClassFile cf) throws IOException
    {
        // update eip?

        byteCodes.write(JavaOpcode.ALOAD_1); // cpu, e
        byteCodes.write(JavaOpcode.SWAP); // e, cpu
        byteCodes.write(JavaOpcode.DUP); // e, e, cpu
        byteCodes.write(JavaOpcode.INVOKEVIRTUAL);
        try {
            int cpIndex = cf.addToConstantPool(ProcessorException.class
                    .getDeclaredMethod("getVector", (Class[]) null));
            if (cpIndex > 0xffff)
                throw new IllegalStateException(
                        "Compilation ran out of constant pool slots");
            byteCodes.write(cpIndex >>> 8);
            byteCodes.write(cpIndex & 0xff);
        } catch (NoSuchMethodException e) {
            System.err.println(e);
        } // vector, e, cpu
        byteCodes.write(JavaOpcode.SWAP); // e, vector, cpu
        byteCodes.write(JavaOpcode.DUP); // e, e, vector, cpu
        byteCodes.write(JavaOpcode.INVOKEVIRTUAL);
        try {
            int cpIndex = cf.addToConstantPool(ProcessorException.class
                    .getDeclaredMethod("hasErrorCode", (Class[]) null));
            if (cpIndex > 0xffff)
                throw new IllegalStateException(
                        "Compilation ran out of constant pool slots");
            byteCodes.write(cpIndex >>> 8);
            byteCodes.write(cpIndex & 0xff);
        } catch (NoSuchMethodException e) {
            System.err.println(e);
        } // hasec, e, vector, cpu
        byteCodes.write(JavaOpcode.SWAP); // e, hasec, vector, cpu
        byteCodes.write(JavaOpcode.INVOKEVIRTUAL);
        try {
            int cpIndex = cf.addToConstantPool(ProcessorException.class
                    .getDeclaredMethod("getErrorCode", (Class[]) null));
            if (cpIndex > 0xffff)
                throw new IllegalStateException(
                        "Compilation ran out of constant pool slots");
            byteCodes.write(cpIndex >>> 8);
            byteCodes.write(cpIndex & 0xff);
        } catch (NoSuchMethodException e) {
            System.err.println(e);
        } // ec, hasec, vector, cpu

        byteCodes.write(JavaOpcode.INVOKEVIRTUAL);
        try {
            int cpIndex = cf.addToConstantPool(Processor.class
                    .getDeclaredMethod("handleProtectedModeException",
                    new Class[]{Integer.TYPE, Boolean.TYPE,
                            Integer.TYPE}));
            if (cpIndex > 0xffff)
                throw new IllegalStateException(
                        "Compilation ran out of constant pool slots");
            byteCodes.write(cpIndex >>> 8);
            byteCodes.write(cpIndex & 0xff);
        } catch (NoSuchMethodException e) {
            System.err.println(e);
        }
    }
}
