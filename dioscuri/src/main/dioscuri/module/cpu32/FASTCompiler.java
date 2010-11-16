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

import java.io.*;
import java.util.*;

//import org.jpc.emulator.processor.ProcessorException;
//import org.jpc.emulator.memory.codeblock.*;
//import org.jpc.classfile.*;
//
//import org.jpc.emulator.memory.codeblock.fastcompiler.real.*;
//import org.jpc.emulator.memory.codeblock.fastcompiler.prot.*;

/**
 * @author Bram Lohman
 * @author Bart Kiers
 */
public class FASTCompiler implements CodeBlockCompiler {
    public static final int PROCESSOR_ELEMENT_EAX = 0;
    public static final int PROCESSOR_ELEMENT_ECX = 1;
    public static final int PROCESSOR_ELEMENT_EDX = 2;
    public static final int PROCESSOR_ELEMENT_EBX = 3;
    public static final int PROCESSOR_ELEMENT_ESP = 4;
    public static final int PROCESSOR_ELEMENT_EBP = 5;
    public static final int PROCESSOR_ELEMENT_ESI = 6;
    public static final int PROCESSOR_ELEMENT_EDI = 7;
    public static final int PROCESSOR_ELEMENT_EIP = 8;
    public static final int PROCESSOR_ELEMENT_CFLAG = 9;
    public static final int PROCESSOR_ELEMENT_PFLAG = 10;
    public static final int PROCESSOR_ELEMENT_AFLAG = 11;
    public static final int PROCESSOR_ELEMENT_ZFLAG = 12;
    public static final int PROCESSOR_ELEMENT_SFLAG = 13;
    public static final int PROCESSOR_ELEMENT_TFLAG = 14;
    public static final int PROCESSOR_ELEMENT_IFLAG = 15;
    public static final int PROCESSOR_ELEMENT_DFLAG = 16;
    public static final int PROCESSOR_ELEMENT_OFLAG = 17;
    public static final int PROCESSOR_ELEMENT_IOPL = 18;
    public static final int PROCESSOR_ELEMENT_NTFLAG = 19;
    public static final int PROCESSOR_ELEMENT_RFLAG = 20;
    public static final int PROCESSOR_ELEMENT_VMFLAG = 21;
    public static final int PROCESSOR_ELEMENT_ACFLAG = 22;
    public static final int PROCESSOR_ELEMENT_VIFLAG = 23;
    public static final int PROCESSOR_ELEMENT_VIPFLAG = 24;
    public static final int PROCESSOR_ELEMENT_IDFLAG = 25;
    public static final int PROCESSOR_ELEMENT_ES = 26;
    public static final int PROCESSOR_ELEMENT_CS = 27;
    public static final int PROCESSOR_ELEMENT_SS = 28;
    public static final int PROCESSOR_ELEMENT_DS = 29;
    public static final int PROCESSOR_ELEMENT_FS = 30;
    public static final int PROCESSOR_ELEMENT_GS = 31;
    public static final int PROCESSOR_ELEMENT_IDTR = 32;
    public static final int PROCESSOR_ELEMENT_GDTR = 33;
    public static final int PROCESSOR_ELEMENT_LDTR = 34;
    public static final int PROCESSOR_ELEMENT_TSS = 35;
    public static final int PROCESSOR_ELEMENT_CPL = 36;
    public static final int PROCESSOR_ELEMENT_IOPORTS = 37;
    public static final int PROCESSOR_ELEMENT_ADDR0 = 38;
    public static final int PROCESSOR_ELEMENT_COUNT = 39;
    public static final int PROCESSOR_ELEMENT_REG0 = 39;
    public static final int PROCESSOR_ELEMENT_REG1 = 40;
    public static final int PROCESSOR_ELEMENT_REG2 = 41;
    public static final int PROCESSOR_ELEMENT_SEG0 = 42;
    public static final int POPABLE_ELEMENT_COUNT = 43;
    public static final int PROCESSOR_ELEMENT_MEMORYWRITE = 43;
    public static final int PROCESSOR_ELEMENT_IOPORTWRITE = 44;
    public static final int PROCESSOR_ELEMENT_EXECUTECOUNT = 45;
    public static final int ELEMENT_COUNT = 46;
    public static final int VARIABLE_EXECUTE_COUNT_INDEX = 10;
    public static final int VARIABLE_OFFSET = 11;

    private static int classIndex = 0;

    /**
     * @param source
     * @return -
     */
    public ProtectedModeCodeBlock getProtectedModeCodeBlock(
            InstructionSource source) {
        MicrocodeNode[] microcodes = MicrocodeNode.getMicrocodes(source);
        ClassFile newClass = null;

        try {
            newClass = ClassFileBuilder.createNewProtectedModeSkeletonClass();
            MicrocodeNode last = microcodes[microcodes.length - 1];

            newClass.setClassName("org.jpc.dynamic.FAST_PM_LEN"
                    + last.getX86Index() + "_NUM" + (classIndex++));

            int x86CountIndex = newClass.addToConstantPool(new Integer(last
                    .getX86Index()));
            int x86LengthIndex = newClass.addToConstantPool(new Integer(last
                    .getX86Position()));

            compileX86CountMethod(newClass, x86CountIndex);
            compileX86LengthMethod(newClass, x86LengthIndex);

            compileProtectedModeExecuteMethod(microcodes, newClass,
                    x86CountIndex);

            return (ProtectedModeCodeBlock) ClassFileBuilder
                    .instantiateClass(newClass);
        } catch (Error e) {
            // dumpClass(newClass);
            throw new IllegalStateException(
                    "Failed to compile Protected Mode FAST block : " + e, e);
        } catch (NullPointerException e) {
            throw new IllegalStateException(
                    "Failed to compile Protected Mode FAST block : " + e, e);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to compile Protected Mode FAST block : " + e, e);
        }
    }

    /**
     * @param source
     * @return -
     */
    public Virtual8086ModeCodeBlock getVirtual8086ModeCodeBlock(
            InstructionSource source) {
        throw new IllegalStateException(
                "Cannot compile Virtual8086 Mode FAST blocks");
    }

    /**
     * @param source
     * @return -
     */
    public RealModeCodeBlock getRealModeCodeBlock(InstructionSource source) {
        MicrocodeNode[] microcodes = MicrocodeNode.getMicrocodes(source);
        ClassFile newClass = null;

        try {
            newClass = ClassFileBuilder.createNewRealModeSkeletonClass();
            MicrocodeNode last = microcodes[microcodes.length - 1];

            newClass.setClassName("org.jpc.dynamic.FAST_RM_LEN"
                    + last.getX86Index() + "_NUM" + (classIndex++));

            int x86CountIndex = newClass.addToConstantPool(new Integer(last
                    .getX86Index()));
            int x86LengthIndex = newClass.addToConstantPool(new Integer(last
                    .getX86Position()));

            compileX86CountMethod(newClass, x86CountIndex);
            compileX86LengthMethod(newClass, x86LengthIndex);

            compileRealModeExecuteMethod(microcodes, newClass, x86CountIndex);

            return (RealModeCodeBlock) ClassFileBuilder
                    .instantiateClass(newClass);
        } catch (Error e) {
            // dumpClass(newClass);
            throw new IllegalStateException(
                    "Failed to compile Real Mode FAST block : " + e, e);
        } catch (NullPointerException e) {
            throw new IllegalStateException(
                    "Failed to compile Real Mode FAST block : " + e, e);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to compile Real Mode FAST block : " + e, e);
        }
    }

    private static void compileProtectedModeExecuteMethod(
            MicrocodeNode[] microcodes, ClassFile cf, int x86CountIndex)
            throws IOException {
        List<ProtectedModeRPNNode> externalEffects = new ArrayList<ProtectedModeRPNNode>();
        Map<Integer, ProtectedModeRPNNode> currentElements = new HashMap<Integer, ProtectedModeRPNNode>();

        List<ExceptionHandler> exceptionHandlers = new ArrayList<ExceptionHandler>();
        ExceptionHandler currentExceptionHandler = null;

        // set all initial elements to their processor values
        for (int i = 0; i < PROCESSOR_ELEMENT_COUNT; i++)
            currentElements.put(new Integer(i), new ProtectedModeRPNNode(i,
                    null));

        int lastX86Position = 0;

        for (int i = 0; i < microcodes.length; i++) {
            MicrocodeNode node = microcodes[i];
            int uCode = node.getMicrocode();

            Object[] codes = ProtectedModeBytecodeFragments.getTargetsOf(uCode);
            if (codes == null)
                throw new IllegalStateException("Unimplemented Microcode: "
                        + MicrocodeNode.getName(uCode));

            List<ProtectedModeRPNNode> targets = new ArrayList<ProtectedModeRPNNode>();
            for (int j = 0; j < codes.length; j++) {
                if (codes[j] == null)
                    continue;

                ProtectedModeRPNNode rpn = new ProtectedModeRPNNode(j, node);
                if (rpn.hasExternalEffect())
                    externalEffects.add(rpn);

                if (rpn.canThrowException()) {
                    if ((currentExceptionHandler == null)
                            || (currentExceptionHandler.getX86Index() != rpn
                            .getX86Index())) {
                        currentExceptionHandler = new ProtectedModeExceptionHandler(
                                lastX86Position, rpn,
                                new HashMap<Integer, RPNNode>(currentElements));
                        exceptionHandlers.add(currentExceptionHandler);
                    }
                    rpn.attachExceptionHandler(currentExceptionHandler);
                }

                targets.add(rpn);

                int[] argIds = ProtectedModeBytecodeFragments.getOperands(j,
                        uCode);
                if (argIds == null)
                    System.out.println("NULL IDS FOR: " + j + "  " + uCode);

                for (int k = 0; k < argIds.length; k++) {
                    ProtectedModeRPNNode arg = (ProtectedModeRPNNode) currentElements
                            .get(new Integer(argIds[k]));
                    rpn.linkTo(arg);
                }
            }

            for (int j = 0; j < targets.size(); j++) {
                ProtectedModeRPNNode rpn = (ProtectedModeRPNNode) targets
                        .get(j);
                currentElements.put(new Integer(rpn.getID()), rpn);
            }

            if (((i + 1) < microcodes.length)
                    && (node.getX86Position() != microcodes[i + 1]
                    .getX86Position()))
                lastX86Position = node.getX86Position();
        }

        for (int i = PROCESSOR_ELEMENT_COUNT; i < ELEMENT_COUNT; i++)
            currentElements.remove(new Integer(i));

        int localVariableIndex = VARIABLE_OFFSET;
        for (int i = 0; i < externalEffects.size(); i++)
            localVariableIndex = ((ProtectedModeRPNNode) externalEffects.get(i))
                    .markSubtrees(localVariableIndex);

        int affectedCount = 0;
        for (Iterator<ProtectedModeRPNNode> itt = currentElements.values()
                .iterator(); itt.hasNext();) {
            ProtectedModeRPNNode rpn = itt.next();

            if (rpn.getMicrocode() == -1)
                continue;

            affectedCount++;
            localVariableIndex = rpn.markSubtrees(localVariableIndex);
        }

        ByteArrayOutputStream byteCodes = new ByteArrayOutputStream();
        CountingOutputStream countingByteCodes = new CountingOutputStream(
                byteCodes);

        countingByteCodes.write(JavaOpcode.LDC);
        countingByteCodes.write(x86CountIndex);
        countingByteCodes.write(JavaOpcode.ISTORE);
        countingByteCodes.write(VARIABLE_EXECUTE_COUNT_INDEX);

        for (int i = 0; i < externalEffects.size(); i++) {
            ProtectedModeRPNNode rpn = (ProtectedModeRPNNode) externalEffects
                    .get(i);
            rpn.write(countingByteCodes, cf, false);
        }

        int index = 0;
        ProtectedModeRPNNode[] roots = new ProtectedModeRPNNode[affectedCount];
        for (Iterator<ProtectedModeRPNNode> itt = currentElements.values()
                .iterator(); itt.hasNext();) {
            ProtectedModeRPNNode rpn = itt.next();
            if (rpn.getMicrocode() == -1)
                continue;

            rpn.write(countingByteCodes, cf, true);
            roots[index++] = rpn;
        }

        for (int i = index - 1; i >= 0; i--)
            RPNNode.writeBytecodes(countingByteCodes, cf,
                    ProtectedModeBytecodeFragments.popCode(roots[i].getID()));

        countingByteCodes.write(JavaOpcode.ILOAD);
        countingByteCodes.write(VARIABLE_EXECUTE_COUNT_INDEX);
        countingByteCodes.write(JavaOpcode.IRETURN);

        AttributeInfo.CodeAttribute.ExceptionEntry[] exceptionTable = new AttributeInfo.CodeAttribute.ExceptionEntry[exceptionHandlers
                .size()];
        int j = 0;
        for (int i = 0; i < exceptionHandlers.size(); i++) {
            int handlerPC = countingByteCodes.position();
            ExceptionHandler handler = (ExceptionHandler) exceptionHandlers
                    .get(i);
            if (!handler.used())
                continue;
            handler.write(countingByteCodes, cf);
            exceptionTable[j++] = new AttributeInfo.CodeAttribute.ExceptionEntry(
                    handler.start(), handler.end(), handlerPC, cf
                            .addToConstantPool(ProcessorException.class));
        }

        AttributeInfo.CodeAttribute.ExceptionEntry[] et = new AttributeInfo.CodeAttribute.ExceptionEntry[j];
        System.arraycopy(exceptionTable, 0, et, 0, et.length);
        exceptionTable = et;
        // exceptionTable = (AttributeInfo.CodeAttribute.ExceptionEntry[])
        // Arrays.copyOf(exceptionTable, j);

        byte[] bytes = byteCodes.toByteArray();

        int[] ints = new int[bytes.length];
        for (int i = 0; i < ints.length; i++)
            ints[i] = 0xff & bytes[i];

        cf.setMethodCode("execute", ints);

        cf.setMethodExceptionTable("execute", exceptionTable);
    }

    private static void compileRealModeExecuteMethod(
            MicrocodeNode[] microcodes, ClassFile cf, int x86CountIndex)
            throws IOException {
        List<RealModeRPNNode> externalEffects = new ArrayList<RealModeRPNNode>();
        Map<Integer, RealModeRPNNode> currentElements = new HashMap<Integer, RealModeRPNNode>();

        List<ExceptionHandler> exceptionHandlers = new ArrayList<ExceptionHandler>();
        ExceptionHandler currentExceptionHandler = null;

        // set all initial elements to their processor values
        for (int i = 0; i < PROCESSOR_ELEMENT_COUNT; i++)
            currentElements.put(new Integer(i), new RealModeRPNNode(i, null));

        int lastX86Position = 0;

        for (int i = 0; i < microcodes.length; i++) {
            MicrocodeNode node = microcodes[i];
            int uCode = node.getMicrocode();

            Object[] codes = RealModeBytecodeFragments.getTargetsOf(uCode);
            if (codes == null)
                throw new IllegalStateException("Unimplemented Microcode: "
                        + MicrocodeNode.getName(uCode));

            List<RealModeRPNNode> targets = new ArrayList<RealModeRPNNode>();
            for (int j = 0; j < codes.length; j++) {
                if (codes[j] == null)
                    continue;

                RealModeRPNNode rpn = new RealModeRPNNode(j, node);
                if (rpn.hasExternalEffect())
                    externalEffects.add(rpn);

                if (rpn.canThrowException()) {
                    if ((currentExceptionHandler == null)
                            || (currentExceptionHandler.getX86Index() != rpn
                            .getX86Index())) {
                        currentExceptionHandler = new RealModeExceptionHandler(
                                lastX86Position, rpn,
                                new HashMap<Integer, RPNNode>(currentElements));
                        exceptionHandlers.add(currentExceptionHandler);
                    }
                    rpn.attachExceptionHandler(currentExceptionHandler);
                }

                targets.add(rpn);

                int[] argIds = RealModeBytecodeFragments.getOperands(j, uCode);
                if (argIds == null)
                    System.out.println("NULL IDS FOR: " + j + "  " + uCode);

                for (int k = 0; k < argIds.length; k++) {
                    RealModeRPNNode arg = (RealModeRPNNode) currentElements
                            .get(new Integer(argIds[k]));
                    rpn.linkTo(arg);
                }
            }

            for (int j = 0; j < targets.size(); j++) {
                RealModeRPNNode rpn = (RealModeRPNNode) targets.get(j);
                currentElements.put(new Integer(rpn.getID()), rpn);
            }

            if (((i + 1) < microcodes.length)
                    && (node.getX86Position() != microcodes[i + 1]
                    .getX86Position()))
                lastX86Position = node.getX86Position();
        }

        for (int i = PROCESSOR_ELEMENT_COUNT; i < ELEMENT_COUNT; i++)
            currentElements.remove(new Integer(i));

        int localVariableIndex = VARIABLE_OFFSET;
        for (int i = 0; i < externalEffects.size(); i++)
            localVariableIndex = ((RealModeRPNNode) externalEffects.get(i))
                    .markSubtrees(localVariableIndex);

        int affectedCount = 0;
        for (Iterator<RealModeRPNNode> itt = currentElements.values()
                .iterator(); itt.hasNext();) {
            RealModeRPNNode rpn = itt.next();

            if (rpn.getMicrocode() == -1)
                continue;

            affectedCount++;
            localVariableIndex = rpn.markSubtrees(localVariableIndex);
        }

        ByteArrayOutputStream byteCodes = new ByteArrayOutputStream();
        CountingOutputStream countingByteCodes = new CountingOutputStream(
                byteCodes);

        countingByteCodes.write(JavaOpcode.LDC);
        countingByteCodes.write(x86CountIndex);
        countingByteCodes.write(JavaOpcode.ISTORE);
        countingByteCodes.write(VARIABLE_EXECUTE_COUNT_INDEX);
        for (int i = 0; i < externalEffects.size(); i++) {
            RealModeRPNNode rpn = (RealModeRPNNode) externalEffects.get(i);
            rpn.write(countingByteCodes, cf, false);
        }

        int index = 0;
        RealModeRPNNode[] roots = new RealModeRPNNode[affectedCount];
        for (Iterator<RealModeRPNNode> itt = currentElements.values()
                .iterator(); itt.hasNext();) {
            RealModeRPNNode rpn = (RealModeRPNNode) itt.next();
            if (rpn.getMicrocode() == -1)
                continue;
            rpn.write(countingByteCodes, cf, true);
            roots[index++] = rpn;
        }

        for (int i = index - 1; i >= 0; i--) {
            RPNNode.writeBytecodes(countingByteCodes, cf,
                    RealModeBytecodeFragments.popCode(roots[i].getID()));
        }

        countingByteCodes.write(JavaOpcode.ILOAD);
        countingByteCodes.write(VARIABLE_EXECUTE_COUNT_INDEX);
        countingByteCodes.write(JavaOpcode.IRETURN);

        AttributeInfo.CodeAttribute.ExceptionEntry[] exceptionTable = new AttributeInfo.CodeAttribute.ExceptionEntry[exceptionHandlers
                .size()];
        int j = 0;
        for (int i = 0; i < exceptionHandlers.size(); i++) {
            int handlerPC = countingByteCodes.position();
            ExceptionHandler handler = (ExceptionHandler) exceptionHandlers
                    .get(i);
            if (!handler.used())
                continue;
            handler.write(countingByteCodes, cf);
            exceptionTable[j++] = new AttributeInfo.CodeAttribute.ExceptionEntry(
                    handler.start(), handler.end(), handlerPC, cf
                            .addToConstantPool(ProcessorException.class));
        }

        AttributeInfo.CodeAttribute.ExceptionEntry[] et = new AttributeInfo.CodeAttribute.ExceptionEntry[j];
        System.arraycopy(exceptionTable, 0, et, 0, et.length);
        exceptionTable = et;
        // exceptionTable = (AttributeInfo.CodeAttribute.ExceptionEntry[])
        // Arrays.copyOf(exceptionTable, j);

        byte[] bytes = byteCodes.toByteArray();

        int[] ints = new int[bytes.length];
        for (int i = 0; i < ints.length; i++)
            ints[i] = 0xff & bytes[i];

        cf.setMethodCode("execute", ints);

        cf.setMethodExceptionTable("execute", exceptionTable);
    }

    private static void compileX86CountMethod(ClassFile cf, int x86CountIndex) {
        ByteArrayOutputStream byteCodes = new ByteArrayOutputStream();

        byteCodes.write(JavaOpcode.LDC);
        byteCodes.write(x86CountIndex);
        byteCodes.write(JavaOpcode.IRETURN);

        byte[] bytes = byteCodes.toByteArray();

        int[] ints = new int[bytes.length];
        for (int i = 0; i < ints.length; i++)
            ints[i] = 0xff & bytes[i];

        cf.setMethodCode("getX86Count", ints);
    }

    private static void compileX86LengthMethod(ClassFile cf, int x86LengthIndex) {
        ByteArrayOutputStream byteCodes = new ByteArrayOutputStream();

        byteCodes.write(JavaOpcode.LDC);
        byteCodes.write(x86LengthIndex);
        byteCodes.write(JavaOpcode.IRETURN);

        byte[] bytes = byteCodes.toByteArray();

        int[] ints = new int[bytes.length];
        for (int i = 0; i < ints.length; i++)
            ints[i] = 0xff & bytes[i];

        cf.setMethodCode("getX86Length", ints);
    }

    @SuppressWarnings("unused")
    private static void dumpClass(ClassFile cls) {
        try {
            File dump = new File(cls.getClassName().replace('.', '/')
                    + ".class");
            dump.getParentFile().mkdirs();
            cls.write(new DataOutputStream(new FileOutputStream(dump)));
        } catch (Exception f) {
            System.err.println("Attempt to save class file to disk failed: "
                    + f);
        }
    }
}
