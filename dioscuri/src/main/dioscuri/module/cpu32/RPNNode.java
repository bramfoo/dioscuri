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

//import org.jpc.classfile.*;

/**
 *
 * @author Bram Lohman
 * @author Bart Kiers
 */
public abstract class RPNNode {
    private int id, count, useCount, subtreeIndex;
    private List<RPNNode> argLinks;

    private int writeCount = 0;
    private int writeCountMax = 0;
    private int[] writeCountIndex = new int[100];

    private MicrocodeNode parent;

    private ExceptionHandler exceptionHandler;

    private static int counter = 0;

    /**
     *
     * @param id
     * @param parent
     */
    public RPNNode(int id, MicrocodeNode parent) {
        this.id = id;
        this.parent = parent;

        useCount = 0;
        subtreeIndex = -1;
        argLinks = new Vector<RPNNode>();
        count = counter++;
        writeCount = 0;
        writeCountMax = 0;
    }

    /**
     *
     * @return -
     */
    public abstract boolean hasExternalEffect();

    /**
     *
     * @return -
     */
    public abstract boolean canThrowException();

    /**
     *
     * @return -
     */
    protected abstract Object[] getByteCodes();

    /**
     *
     * @return -
     */
    public int getX86Index() {
        return parent.getX86Index();
    }

    /**
     *
     * @return -
     */
    public int getX86Position() {
        return parent.getX86Position();
    }

    /**
     *
     * @return -
     */
    public int getImmediate() {
        return parent.getImmediate();
    }

    /**
     *
     * @return -
     */
    public boolean hasImmediate() {
        return parent.hasImmediate();
    }

    /**
     *
     * @return -
     */
    public int getID() {
        return id;
    }

    /**
     *
     * @return -
     */
    public int getMicrocode() {
        if (parent == null)
            return -1;

        return parent.getMicrocode();
    }

    /**
     *
     * @return -
     */
    public boolean hasLinks() {
        return argLinks.size() > 0;
    }

    /**
     *
     * @param link
     */
    public void linkTo(RPNNode link) {
        argLinks.add(link);
    }

    /**
     *
     * @param index
     * @return -
     */
    public int markSubtrees(int index) {
        useCount++;
        if ((id != FASTCompiler.PROCESSOR_ELEMENT_MEMORYWRITE)
                && (id != FASTCompiler.PROCESSOR_ELEMENT_IOPORTWRITE)
                && (id != FASTCompiler.PROCESSOR_ELEMENT_EXECUTECOUNT)
                && (subtreeIndex < 0)) {
            if ((useCount > 1) || hasExternalEffect()) {
                if (index == 0x100)
                    throw new IllegalStateException(
                            "Compilation ran out of local variables");
                subtreeIndex = index++;
            }
        }

        if (useCount == 1) {
            for (int i = 0; i < argLinks.size(); i++)
                index = ((RPNNode) argLinks.get(i)).markSubtrees(index);
        }

        return index;
    }

    /**
     *
     * @param handler
     */
    public void attachExceptionHandler(ExceptionHandler handler) {
        exceptionHandler = handler;
    }

    /**
     *
     * @param indent
     */
    public void print(String indent) {
        System.out.println(indent + "[" + id + "] by "
                + MicrocodeNode.getName(getMicrocode()) + "  {" + count
                + " used " + useCount + "}");
        if (argLinks.size() == 0)
            return;
        System.out.println(indent + "{");
        for (int i = 0; i < argLinks.size(); i++)
            ((RPNNode) argLinks.get(i)).print(indent + " ");
        System.out.println(indent + "}");
    }

    /**
     *
     */
    public void print() {
        print("");
    }

    /**
     *
     * @param output
     * @param cf
     * @param bytecodes
     * @throws IOException
     */
    public static void writeBytecodes(CountingOutputStream output,
            ClassFile cf, Object[] bytecodes) throws IOException {
        int lastByte = -1;
        for (int i = 0; i < bytecodes.length; i++) {
            Object o = bytecodes[i];

            if (o instanceof Integer) {
                lastByte = ((Integer) o).intValue();

                if (((i + 1) < bytecodes.length)
                        && ((o = bytecodes[i + 1]) instanceof ConstantPoolSymbol)) {
                    int index = cf.addToConstantPool(((ConstantPoolSymbol) o)
                            .poolEntity());
                    if ((index > 0xff) && (lastByte == JavaOpcode.LDC))
                        lastByte = JavaOpcode.LDC_W;

                    output.write(lastByte);

                    switch (JavaOpcode.getConstantPoolIndexSize(lastByte)) {
                    case 1:
                        output.write(index & 0xff);
                        break;
                    case 2:
                        output.write(index >>> 8);
                        output.write(index & 0xff);
                        break;
                    default:
                        throw new IllegalStateException();
                    }
                    i++;
                } else
                    output.write(lastByte);
            } else
                throw new IllegalStateException(o.toString() + "    "
                        + BytecodeFragments.X86LENGTH + "     "
                        + BytecodeFragments.IMMEDIATE);
        }
    }

    /**
     *
     * @param output
     * @param cf
     * @param leaveResultOnStack
     * @throws IOException
     */
    public void write(CountingOutputStream output, ClassFile cf,
            boolean leaveResultOnStack) throws IOException {
        reset = false;
        writeCount++;
        try {
            writeCountIndex[writeCount] = output.position();
        } catch (ArrayIndexOutOfBoundsException e) {
            int[] temp = new int[writeCountIndex.length * 2];
            System.arraycopy(writeCountIndex, 0, temp, 0,
                    writeCountIndex.length);
            temp[writeCount] = output.position();
            writeCountIndex = temp;
        }
        writeCountMax = Math.max(writeCountMax, writeCount);

        if ((writeCount == 1)
                || !((subtreeIndex >= 0) && (subtreeIndex <= 0xFF))) {
            if ((writeCount > 1)
                    && ((id == FASTCompiler.PROCESSOR_ELEMENT_MEMORYWRITE)
                            || (id == FASTCompiler.PROCESSOR_ELEMENT_IOPORTWRITE) || (id == FASTCompiler.PROCESSOR_ELEMENT_EXECUTECOUNT))) {
                return;
            }

            for (int i = 0; i < argLinks.size(); i++)
                ((RPNNode) argLinks.get(i)).write(output, cf, true);

            int min = output.position();
            writeBytecodes(output, cf, getByteCodes());
            int max = output.position();

            if (exceptionHandler != null)
                exceptionHandler.assignRange(min, max);

            if ((subtreeIndex >= 0) && (subtreeIndex <= 0xFF)) {
                switch (id) {
                case FASTCompiler.PROCESSOR_ELEMENT_ES:
                case FASTCompiler.PROCESSOR_ELEMENT_CS:
                case FASTCompiler.PROCESSOR_ELEMENT_SS:
                case FASTCompiler.PROCESSOR_ELEMENT_DS:
                case FASTCompiler.PROCESSOR_ELEMENT_FS:
                case FASTCompiler.PROCESSOR_ELEMENT_GS:
                case FASTCompiler.PROCESSOR_ELEMENT_IDTR:
                case FASTCompiler.PROCESSOR_ELEMENT_GDTR:
                case FASTCompiler.PROCESSOR_ELEMENT_LDTR:
                case FASTCompiler.PROCESSOR_ELEMENT_TSS:
                case FASTCompiler.PROCESSOR_ELEMENT_IOPORTS:
                case FASTCompiler.PROCESSOR_ELEMENT_SEG0:
                    if (leaveResultOnStack) {
                        output.write(JavaOpcode.DUP);
                        output.write(JavaOpcode.ASTORE);
                        output.write(subtreeIndex);
                    } else {
                        output.write(JavaOpcode.ASTORE);
                        output.write(subtreeIndex);
                    }
                    break;
                default:
                    if (leaveResultOnStack) {
                        output.write(JavaOpcode.DUP);
                        output.write(JavaOpcode.ISTORE);
                        output.write(subtreeIndex);
                    } else {
                        output.write(JavaOpcode.ISTORE);
                        output.write(subtreeIndex);
                    }
                    break;
                case FASTCompiler.PROCESSOR_ELEMENT_MEMORYWRITE:
                case FASTCompiler.PROCESSOR_ELEMENT_IOPORTWRITE:
                case FASTCompiler.PROCESSOR_ELEMENT_EXECUTECOUNT:
                    break;
                }
            }
        } else {
            if (!leaveResultOnStack)
                return;

            switch (id) {
            case FASTCompiler.PROCESSOR_ELEMENT_ES:
            case FASTCompiler.PROCESSOR_ELEMENT_CS:
            case FASTCompiler.PROCESSOR_ELEMENT_SS:
            case FASTCompiler.PROCESSOR_ELEMENT_DS:
            case FASTCompiler.PROCESSOR_ELEMENT_FS:
            case FASTCompiler.PROCESSOR_ELEMENT_GS:
            case FASTCompiler.PROCESSOR_ELEMENT_IDTR:
            case FASTCompiler.PROCESSOR_ELEMENT_GDTR:
            case FASTCompiler.PROCESSOR_ELEMENT_LDTR:
            case FASTCompiler.PROCESSOR_ELEMENT_TSS:
            case FASTCompiler.PROCESSOR_ELEMENT_IOPORTS:
            case FASTCompiler.PROCESSOR_ELEMENT_SEG0:
                output.write(JavaOpcode.ALOAD);
                output.write(subtreeIndex);
                break;
            default:
                output.write(JavaOpcode.ILOAD);
                output.write(subtreeIndex);
                break;
            case FASTCompiler.PROCESSOR_ELEMENT_MEMORYWRITE:
            case FASTCompiler.PROCESSOR_ELEMENT_IOPORTWRITE:
            case FASTCompiler.PROCESSOR_ELEMENT_EXECUTECOUNT:
                break;
            }
        }
    }

    /**
     *
     * @param output
     * @param cf
     * @param leaveResultOnStack
     * @throws IOException
     */
    public void writeExceptionCleanup(CountingOutputStream output,
            ClassFile cf, boolean leaveResultOnStack) throws IOException {
        reset = false;
        writeCount++;

        if ((writeCount > 1) && (subtreeIndex >= 0) && (subtreeIndex <= 0xFF)) {
            if (!leaveResultOnStack)
                return;

            switch (id) {
            case FASTCompiler.PROCESSOR_ELEMENT_ES:
            case FASTCompiler.PROCESSOR_ELEMENT_CS:
            case FASTCompiler.PROCESSOR_ELEMENT_SS:
            case FASTCompiler.PROCESSOR_ELEMENT_DS:
            case FASTCompiler.PROCESSOR_ELEMENT_FS:
            case FASTCompiler.PROCESSOR_ELEMENT_GS:
            case FASTCompiler.PROCESSOR_ELEMENT_IDTR:
            case FASTCompiler.PROCESSOR_ELEMENT_GDTR:
            case FASTCompiler.PROCESSOR_ELEMENT_LDTR:
            case FASTCompiler.PROCESSOR_ELEMENT_TSS:
            case FASTCompiler.PROCESSOR_ELEMENT_IOPORTS:
            case FASTCompiler.PROCESSOR_ELEMENT_SEG0:
                output.write(JavaOpcode.ALOAD);
                output.write(subtreeIndex);
                break;
            default:
                output.write(JavaOpcode.ILOAD);
                output.write(subtreeIndex);
                break;
            case FASTCompiler.PROCESSOR_ELEMENT_MEMORYWRITE:
            case FASTCompiler.PROCESSOR_ELEMENT_IOPORTWRITE:
            case FASTCompiler.PROCESSOR_ELEMENT_EXECUTECOUNT:
                break;
            }
        } else {
            if ((writeCount > 1)
                    && ((id == FASTCompiler.PROCESSOR_ELEMENT_MEMORYWRITE)
                            || (id == FASTCompiler.PROCESSOR_ELEMENT_IOPORTWRITE) || (id == FASTCompiler.PROCESSOR_ELEMENT_EXECUTECOUNT)))
                return;

            for (int i = 0; i < argLinks.size(); i++)
                ((RPNNode) argLinks.get(i)).writeExceptionCleanup(output, cf,
                        true);

            writeBytecodes(output, cf, getByteCodes());

            if ((subtreeIndex >= 0) && (subtreeIndex <= 0xFF)) {
                switch (id) {
                case FASTCompiler.PROCESSOR_ELEMENT_ES:
                case FASTCompiler.PROCESSOR_ELEMENT_CS:
                case FASTCompiler.PROCESSOR_ELEMENT_SS:
                case FASTCompiler.PROCESSOR_ELEMENT_DS:
                case FASTCompiler.PROCESSOR_ELEMENT_FS:
                case FASTCompiler.PROCESSOR_ELEMENT_GS:
                case FASTCompiler.PROCESSOR_ELEMENT_IDTR:
                case FASTCompiler.PROCESSOR_ELEMENT_GDTR:
                case FASTCompiler.PROCESSOR_ELEMENT_LDTR:
                case FASTCompiler.PROCESSOR_ELEMENT_TSS:

                case FASTCompiler.PROCESSOR_ELEMENT_IOPORTS:
                case FASTCompiler.PROCESSOR_ELEMENT_SEG0:
                    if (leaveResultOnStack) {
                        output.write(JavaOpcode.DUP);
                        output.write(JavaOpcode.ASTORE);
                        output.write(subtreeIndex);
                    } else {
                        output.write(JavaOpcode.ASTORE);
                        output.write(subtreeIndex);
                    }
                    break;
                default:
                    if (leaveResultOnStack) {
                        output.write(JavaOpcode.DUP);
                        output.write(JavaOpcode.ISTORE);
                        output.write(subtreeIndex);
                    } else {
                        output.write(JavaOpcode.ISTORE);
                        output.write(subtreeIndex);
                    }
                    break;
                case FASTCompiler.PROCESSOR_ELEMENT_MEMORYWRITE:
                case FASTCompiler.PROCESSOR_ELEMENT_IOPORTWRITE:
                case FASTCompiler.PROCESSOR_ELEMENT_EXECUTECOUNT:
                    break;
                }
            }
        }
    }

    private boolean reset;

    /**
     *
     * @param location
     */
    public void reset(int location) {
        if (reset)
            return;
        reset = true;

        // writeCount = Arrays.binarySearch(writeCountIndex, 0, writeCountMax +
        // 1, location);
        writeCount = Arrays.binarySearch(writeCountIndex, location);

        if (writeCount < 0)
            writeCount = ~writeCount;
        writeCount = Math.max(writeCount - 1, 0);

        for (int i = 0; i < argLinks.size(); i++)
            ((RPNNode) argLinks.get(i)).reset(location);
    }
}
