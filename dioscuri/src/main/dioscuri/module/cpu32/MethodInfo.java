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

public class MethodInfo {
    private int accessFlags;
    private int nameIndex;
    private int descriptorIndex;
    private int attributesCount;
    private AttributeInfo[] attributes;

    public static final int PUBLIC = 0x0001;
    public static final int PRIVATE = 0x0002;
    public static final int PROTECTED = 0x0004;
    public static final int STATIC = 0x0008;
    public static final int FINAL = 0x0010;
    public static final int SYNCHRONIZED = 0x0020;
    public static final int BRIDGE = 0x0040;
    public static final int VARARGS = 0x0080;
    public static final int NATIVE = 0x0100;
    public static final int ABSTRACT = 0x0400;
    public static final int STRICT = 0x0800;
    public static final int SYTHETIC = 0x1000;

    public MethodInfo(DataInputStream in, ConstantPoolInfo[] pool)
            throws IOException {
        accessFlags = in.readUnsignedShort();
        nameIndex = in.readUnsignedShort();
        descriptorIndex = in.readUnsignedShort();

        attributesCount = in.readUnsignedShort();
        attributes = new AttributeInfo[attributesCount];
        for (int i = 0; i < attributesCount; i++)
            attributes[i] = AttributeInfo.construct(in, pool);
    }

    public void write(DataOutputStream out) throws IOException {
        out.writeShort(accessFlags);
        out.writeShort(nameIndex);
        out.writeShort(descriptorIndex);

        out.writeShort(attributesCount);
        for (int i = 0; i < attributesCount; i++)
            attributes[i].write(out);
    }

    public int getNameIndex() {
        return nameIndex;
    }

    public int getDescriptorIndex() {
        return descriptorIndex;
    }

    public int getMaxStack() {
        for (int i = 0; (i < attributesCount); i++)
            if (attributes[i] instanceof AttributeInfo.CodeAttribute)
                return ((AttributeInfo.CodeAttribute) attributes[i])
                        .getMaxStack();
        return 0;
    }

    public int getMaxLocals() {
        for (int i = 0; (i < attributesCount); i++)
            if (attributes[i] instanceof AttributeInfo.CodeAttribute)
                return ((AttributeInfo.CodeAttribute) attributes[i])
                        .getMaxLocals();
        return 0;
    }

    public int[] getCode() {
        for (int i = 0; (i < attributesCount); i++)
            if (attributes[i] instanceof AttributeInfo.CodeAttribute)
                return ((AttributeInfo.CodeAttribute) attributes[i]).getCode();
        return null;
    }

    public void setCode(int[] code, ClassFile cf) {
        setCode(code, code.length, cf);
    }

    public void setCode(int[] code, int codeLength, ClassFile cf) {
        String descriptor = cf.getConstantPoolUtf8(this.getDescriptorIndex());
        int argLength = cf.getMethodArgLength(descriptor);
        for (int i = 0; (i < attributesCount); i++) {
            if (attributes[i] instanceof AttributeInfo.CodeAttribute) {
                ((AttributeInfo.CodeAttribute) attributes[i]).setCode(code,
                        codeLength, cf, argLength);
                return;
            }
        }
    }

    public AttributeInfo.CodeAttribute.ExceptionEntry[] getExceptionTable() {
        for (int i = 0; i < attributesCount; i++)
            if (attributes[i] instanceof AttributeInfo.CodeAttribute)
                return ((AttributeInfo.CodeAttribute) attributes[i])
                        .getExceptionTable();
        return null;
    }

    public void setExceptionTable(
            AttributeInfo.CodeAttribute.ExceptionEntry[] exceptionTable,
            ClassFile cf) {
        for (int i = 0; (i < attributesCount); i++) {
            if (attributes[i] instanceof AttributeInfo.CodeAttribute) {
                ((AttributeInfo.CodeAttribute) attributes[i])
                        .setExceptionTable(exceptionTable,
                                exceptionTable.length, cf);
                return;
            }
        }
    }

    public void setExceptionTable(
            AttributeInfo.CodeAttribute.ExceptionEntry[] exceptionTable,
            int exceptionTableLength, ClassFile cf) {
        for (int i = 0; (i < attributesCount); i++) {
            if (attributes[i] instanceof AttributeInfo.CodeAttribute) {
                ((AttributeInfo.CodeAttribute) attributes[i])
                        .setExceptionTable(exceptionTable,
                                exceptionTableLength, cf);
                return;
            }
        }
    }

}
