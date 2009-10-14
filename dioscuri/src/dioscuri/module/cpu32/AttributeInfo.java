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

public abstract class AttributeInfo
{
    public abstract void write(DataOutputStream out) throws IOException;

    public static AttributeInfo construct(DataInputStream in, ConstantPoolInfo[] pool) throws IOException
    {
        int index = in.readUnsignedShort();
        int tag = pool[index].getTag();

        AttributeInfo temp;
        if (tag == ConstantPoolInfo.UTF8)
        {
            String s = ((ConstantPoolInfo.Utf8Info) pool[index]).getBytes();
            if (s.equals("SourceFile"))
                return new SourceFileAttribute(in, index);
            else if (s.equals("ConstantValue"))
                return new ConstantValueAttribute(in, index);
            else if (s.equals("Code"))
                return new CodeAttribute(in, index, pool);
            else if (s.equals("StackMapTable"))
                return new StackMapTableAttribute(in, index);
            else if (s.equals("Exceptions"))
                return new ExceptionsAttribute(in, index);
            else if (s.equals("InnerClasses"))
                return new InnerClassesAttribute(in, index);
            else if (s.equals("EnclosingMethod"))
                return new EnclosingMethodAttribute(in, index);
            else if (s.equals("Synthetic"))
                return new SyntheticAttribute(in, index);
            else if (s.equals("Signature"))
                return new SignatureAttribute(in, index);
            else if (s.equals("LineNumberTable"))
                return new LineNumberTableAttribute(in, index);
            else if (s.equals("LocalVariableTable"))
                return new LocalVariableTableAttribute(in, index);
            else if (s.equals("Deprecated"))
                return new DeprecatedAttribute(in, index);
            else
                return new UnknownAttribute(in, index);
        }
        return null;
    }

    abstract static class Attribute extends AttributeInfo
    {
        protected int attributeNameIndex;
        protected int attributeLength;

       
        Attribute(DataInputStream in, int index) throws IOException
        {
            attributeNameIndex = index;
            attributeLength = in.readInt();
        }

        public void write(DataOutputStream out) throws IOException
        {
            out.writeShort(attributeNameIndex);
            out.writeInt(attributeLength);
        }
    }

    public static class ConstantValueAttribute extends Attribute
    {
        private int constantValueIndex;
        
        ConstantValueAttribute(DataInputStream in, int index) throws IOException
        {
            super(in, index);
            constantValueIndex = in.readUnsignedShort();
        }

        public void write(DataOutputStream out) throws IOException
        {
            super.write(out);
            out.writeShort(constantValueIndex);
        }
    }
    
    public static class CodeAttribute extends Attribute
    {
        private int maxStack;
        private int maxLocals;
        private int codeLength;
        private int[] code;
        private int exceptionTableLength;
        private ExceptionEntry[] exceptionTable;
        private int attributesCount;
        private AttributeInfo[] attributes;

        CodeAttribute(DataInputStream in, int index, ConstantPoolInfo[] pool) throws IOException
        {
            super(in, index);
            maxStack = in.readUnsignedShort();
            maxLocals = in.readUnsignedShort();

            codeLength = in.readInt();
            code = new int[codeLength];
            for(int i = 0; i < codeLength; i++)
                code[i] = in.readUnsignedByte();
            
            exceptionTableLength = in.readUnsignedShort();
            exceptionTable = new ExceptionEntry[exceptionTableLength];
            for(int i = 0; i < exceptionTableLength; i++)
                exceptionTable[i] = new ExceptionEntry(in);

            attributesCount = in.readUnsignedShort();
            attributes = new AttributeInfo[attributesCount];
            for(int i = 0; i < attributesCount; i++)
                attributes[i] = AttributeInfo.construct(in, pool);
        }
        
        public int getMaxStack() { return maxStack; }

        public int getMaxLocals() { return maxLocals; }

        public int[] getCode() 
        { 
            int[] a = new int[code.length];
            System.arraycopy(code, 0, a, 0, a.length);
            return a;
//             return Arrays.copyOf(code, code.length); 
        }

        public void setCode(int[] newCode, ClassFile cf, int argLength) 
        { 
            setCode(newCode, newCode.length, cf, argLength);
        }

        public void setCode(int[] newCode, int newCodeLength, ClassFile cf, int argLength) 
        { 
            code = new int[newCodeLength];
            System.arraycopy(newCode, 0, code, 0, code.length);
//             code = Arrays.copyOf(newCode, newCodeLength);
            attributeLength += newCodeLength - codeLength;
            codeLength = newCodeLength;
            maxStack = JavaCodeAnalyser.getMaxStackDepth(code, 0, cf);
            maxLocals = Math.max(JavaCodeAnalyser.getMaxLocalVariables(code), argLength + 1); //+1 accounts for 'this' the hidden argument
        }

    public ExceptionEntry[] getExceptionTable() 
        { 
            ExceptionEntry[] a = new ExceptionEntry[exceptionTable.length];
            System.arraycopy(exceptionTable, 0, a, 0, a.length);
            return a;
//             return (ExceptionEntry[]) Arrays.copyOf(exceptionTable, exceptionTable.length); 
        }

    public void setExceptionTable(ExceptionEntry[] newTable, ClassFile cf)
    {
        setExceptionTable(newTable, newTable.length, cf);
    }

    public void setExceptionTable(ExceptionEntry[] newTable, int newTableLength, ClassFile cf)
    {
        for (int i = 0; i < newTableLength; i++) {
        ExceptionEntry handler = newTable[i];
        if (handler.handlerPC < code.length)
            maxStack = Math.max(maxStack, JavaCodeAnalyser.getMaxStackDepth(code, handler.handlerPC, cf) + 1);
        }

        exceptionTable = new ExceptionEntry[newTableLength];
            System.arraycopy(newTable, 0, exceptionTable, 0, exceptionTable.length);
//      exceptionTable = (ExceptionEntry[]) Arrays.copyOf(newTable, newTableLength);
        attributeLength += 8 * (newTableLength - exceptionTableLength);
        exceptionTableLength = newTableLength;
    }

        public void write(DataOutputStream out) throws IOException
        {
            super.write(out);
            out.writeShort(maxStack);
            out.writeShort(maxLocals);

            out.writeInt(codeLength);
            for(int i = 0; i < codeLength; i++)
                out.writeByte(code[i]);
            
            out.writeShort(exceptionTableLength);
            for(int i = 0; i < exceptionTableLength; i++)
                exceptionTable[i].write(out);

            out.writeShort(attributesCount);
            for(int i = 0; i < attributesCount; i++)
                attributes[i].write(out);
        }


        public static class ExceptionEntry
        {
            private final int startPC;
            private final int endPC;
            private final int handlerPC;
            private final int catchType;
            
        public ExceptionEntry(int start, int end, int handler, int type)
        {
        startPC = start;
        endPC = end;
        handlerPC = handler;
        catchType = type;
        }

            ExceptionEntry(DataInputStream in) throws IOException
            {
                startPC = in.readUnsignedShort();
                endPC = in.readUnsignedShort();
                handlerPC = in.readUnsignedShort();
                catchType = in.readUnsignedShort();
            }

            void write(DataOutputStream out) throws IOException
            {
                out.writeShort(startPC);
                out.writeShort(endPC);
                out.writeShort(handlerPC);
                out.writeShort(catchType);
            }
        }
    }


    public static class StackMapTableAttribute extends Attribute
    {
        private int numberOfEntries;
        private StackMapFrame[] entries;

        StackMapTableAttribute(DataInputStream in, int index) throws IOException
        {
            super(in, index);
            numberOfEntries = in.readUnsignedShort();
            entries = new StackMapFrame[numberOfEntries];
            for(int i = 0; i < numberOfEntries; i++)
                entries[i] = StackMapFrame.construct(in);
        }
        
        public void write(DataOutputStream out) throws IOException
        {
            super.write(out);
            out.writeInt(numberOfEntries);
            for(int i = 0; i < numberOfEntries; i++)
                entries[i].write(out);
        }


        public abstract static class StackMapFrame
        {
            protected int frameType;
            
            public static final int SAME_L = 0;
            public static final int SAME_H = 63;
            public static final int SAME_LOCALS_1_STACK_ITEM_L = 64;
            public static final int SAME_LOCALS_1_STACK_ITEM_H = 127;
            public static final int SAME_LOCALS_1_STACK_ITEM_EXTENDED = 247;
            public static final int CHOP_L = 248;
            public static final int CHOP_H = 250;
            public static final int SAME_FRAME_EXTENDED = 251;
            public static final int APPEND_L = 252;
            public static final int APPEND_H = 254;
            public static final int FULL_FRAME = 255;

            abstract void write(DataOutputStream out) throws IOException;
            
            static StackMapFrame construct(DataInputStream in) throws IOException
            {
                int tag = in.readUnsignedByte();
                if ((tag >= SAME_L) && (tag <= SAME_H))
                    return new SameFrame(in, tag);
                else if ((tag >= SAME_LOCALS_1_STACK_ITEM_L) && (tag <= SAME_LOCALS_1_STACK_ITEM_H))
                    return new SameLocals1StackItemFrame(in, tag);
                else if (tag == SAME_LOCALS_1_STACK_ITEM_EXTENDED)
                    return new SameLocals1StackItemFrameExtended(in, tag);
                else if ((tag >= CHOP_L) && (tag <= CHOP_H))
                    return new ChopFrame(in, tag);
                else if (tag == SAME_FRAME_EXTENDED)
                    return new SameFrameExtended(in, tag);
                else if ((tag >= APPEND_L) && (tag <= APPEND_H))
                    return new AppendFrame(in, tag);
                else if (tag == FULL_FRAME)
                    return new FullFrame(in, tag);
                else
                    return null;
            }

            public int getFrameType() { return frameType; }

            public static class SameFrame extends StackMapFrame
            {
                SameFrame(DataInputStream in, int tag) throws IOException 
                { 
                    frameType = tag; 
                }

                void write(DataOutputStream out) throws IOException
                {
                    out.writeByte(frameType);
                }
            }

            public static class SameLocals1StackItemFrame extends StackMapFrame
            {
                private VerificationTypeInfo[] stack;

                SameLocals1StackItemFrame(DataInputStream in, int tag) throws IOException 
                { 
                    frameType = tag;
                    stack = new VerificationTypeInfo[1];
                    stack[0] = VerificationTypeInfo.construct(in);
                }

                void write(DataOutputStream out) throws IOException
                {
                    out.writeByte(frameType);
                    stack[0].write(out);
                }
            }

            public static class SameLocals1StackItemFrameExtended extends StackMapFrame
            {
                private int offsetDelta;
                private VerificationTypeInfo[] stack;

                SameLocals1StackItemFrameExtended(DataInputStream in, int tag) throws IOException 
                { 
                    frameType = tag;
                    offsetDelta = in.readUnsignedShort();
                    stack = new VerificationTypeInfo[1];
                    stack[0] = VerificationTypeInfo.construct(in);
                }

                void write(DataOutputStream out) throws IOException
                {
                    out.writeByte(frameType);
                    out.writeShort(offsetDelta);
                    stack[0].write(out);
                }
            }

            public static class ChopFrame extends StackMapFrame
            {
                private int offsetDelta;

                ChopFrame(DataInputStream in, int tag) throws IOException 
                { 
                    frameType = tag;
                    offsetDelta = in.readUnsignedShort();
                }

                void write(DataOutputStream out) throws IOException
                {
                    out.writeByte(frameType);
                    out.writeShort(offsetDelta);
                }
            }
            
            public static class SameFrameExtended extends ChopFrame
            {
                SameFrameExtended(DataInputStream in, int tag) throws IOException { super(in, tag); }
            }

            public static class AppendFrame extends StackMapFrame
            {
                private int offsetDelta;
                private VerificationTypeInfo[] locals;

                AppendFrame(DataInputStream in, int tag) throws IOException 
                { 
                    frameType = tag;
                    offsetDelta = in.readUnsignedShort();
                    locals = new VerificationTypeInfo[frameType - 251];
                    for(int i = 0; i < locals.length; i++) 
                        locals[i] = VerificationTypeInfo.construct(in);
                }

                void write(DataOutputStream out) throws IOException
                {
                    out.writeByte(frameType);
                    out.writeShort(offsetDelta);
                    for(int i = 0; i < locals.length; i++) 
                        locals[i].write(out);
                }
            }

            public static class FullFrame extends StackMapFrame
            {
                private int offsetDelta;
                private int numberOfLocals;
                private VerificationTypeInfo[] locals;
                private int numberOfStackItems;
                private VerificationTypeInfo[] stack;

                FullFrame(DataInputStream in, int tag) throws IOException 
                { 
                    frameType = tag;
                    offsetDelta = in.readUnsignedShort();

                    numberOfLocals = in.readUnsignedShort();
                    locals = new VerificationTypeInfo[numberOfLocals];
                    for(int i = 0; i < numberOfLocals; i++) 
                        locals[i] = VerificationTypeInfo.construct(in);

                    numberOfStackItems = in.readUnsignedShort();
                    stack = new VerificationTypeInfo[numberOfStackItems];
                    for(int i = 0; i < numberOfStackItems; i++) 
                        stack[i] = VerificationTypeInfo.construct(in);
                }

                void write(DataOutputStream out) throws IOException
                {
                    out.writeByte(frameType);
                    out.writeShort(offsetDelta);

                    out.writeShort(numberOfLocals);
                    for(int i = 0; i < numberOfLocals; i++) 
                        locals[i].write(out);

                    out.writeShort(numberOfStackItems);
                    for(int i = 0; i < numberOfStackItems; i++) 
                        stack[i].write(out);
                }
            }

            public abstract static class VerificationTypeInfo
            {
                protected int tag;
                
                public static final int TOP = 0;
                public static final int INTEGER = 1;
                public static final int FLOAT = 2;
                public static final int LONG = 4;
                public static final int DOUBLE = 3;
                public static final int NULL = 5;
                public static final int UNINITIALIZEDTHIS = 6;
                public static final int OBJECT = 7;
                public static final int UNINITIALIZED = 8;

            
                static VerificationTypeInfo construct (DataInputStream in) throws IOException
                {
                    int tag = in.readUnsignedByte();
                    switch (tag)
                    {
                    case TOP:
                        return new TopVariableInfo(tag);
                    case INTEGER:
                        return new IntegerVariableInfo(tag);
                    case FLOAT:
                        return new FloatVariableInfo(tag);
                    case LONG:
                        return new LongVariableInfo(tag);
                    case DOUBLE:
                        return new DoubleVariableInfo(tag);
                    case NULL:
                        return new NullVariableInfo(tag);
                    case UNINITIALIZEDTHIS:
                        return new UninitializedThisVariableInfo(tag);
                    case OBJECT:
                        return new ObjectVariableInfo(in, tag);
                    case UNINITIALIZED:
                        return new UninitializedVariableInfo(in, tag);
                    }
                    return null;
                }

                public int getTag() { return tag; }

                public void write(DataOutputStream out) throws IOException  { out.writeByte(tag); }

                
                public static class TopVariableInfo extends VerificationTypeInfo
                {
                    TopVariableInfo(int tag) throws IOException { this.tag = tag; }
                }

                public static class IntegerVariableInfo extends TopVariableInfo
                {
                    IntegerVariableInfo(int tag) throws IOException { super(tag); }
                }

                public static class FloatVariableInfo extends TopVariableInfo
                {
                    FloatVariableInfo(int tag) throws IOException { super(tag); }
                }

                public static class LongVariableInfo extends TopVariableInfo
                {
                    LongVariableInfo(int tag) throws IOException { super(tag); }
                }

                public static class DoubleVariableInfo extends TopVariableInfo
                {
                    DoubleVariableInfo(int tag) throws IOException { super(tag); }
                }

                public static class NullVariableInfo extends TopVariableInfo
                {
                    NullVariableInfo(int tag) throws IOException { super(tag); }
                }

                public static class UninitializedThisVariableInfo extends TopVariableInfo
                {
                    UninitializedThisVariableInfo(int tag) throws IOException { super(tag); }
                }
                
                public static class ObjectVariableInfo extends VerificationTypeInfo
                {
                    private int cpoolIndex;
                    
                    ObjectVariableInfo(DataInputStream in, int tag) throws IOException
                    {
                        this.tag = tag;
                        cpoolIndex = in.readUnsignedShort();
                    }

                    public void write(DataOutputStream out) throws IOException  
                    { 
                        out.writeByte(tag); 
                        out.writeShort(cpoolIndex);
                    }
                    
                }

                public static class UninitializedVariableInfo extends VerificationTypeInfo
                {
                    private int offset;
                    
                    UninitializedVariableInfo(DataInputStream in, int tag) throws IOException
                    {
                        this.tag = tag;
                        offset = in.readUnsignedShort();
                    }

                    public void write(DataOutputStream out) throws IOException  
                    { 
                        out.writeByte(tag); 
                        out.writeShort(offset);
                    }
                    
                }
            }
        }
    }


    public static class ExceptionsAttribute extends Attribute
    {
        private int numberOfExceptions;
        private int[] exceptionIndexTable;

        ExceptionsAttribute(DataInputStream in, int index) throws IOException
        {
            super(in, index);
            numberOfExceptions = in.readUnsignedShort();
            exceptionIndexTable = new int[numberOfExceptions];
            for(int i = 0; i < numberOfExceptions; i++)
                exceptionIndexTable[i] = in.readUnsignedShort();
        }
        
        public void write(DataOutputStream out) throws IOException
        {
            super.write(out);
            out.writeShort(numberOfExceptions);
            for(int i = 0; i < numberOfExceptions; i++)
                out.writeShort(exceptionIndexTable[i]);
        }
    }


    public static class InnerClassesAttribute extends Attribute
    {
        private int numberOfClasses;
        private ClassEntry[] classes;

        InnerClassesAttribute(DataInputStream in, int index) throws IOException
        {
            super(in, index);
            numberOfClasses = in.readUnsignedShort();
            classes = new ClassEntry[numberOfClasses];
            for(int i = 0; i < numberOfClasses; i++)
                classes[i] = new ClassEntry(in);
        }

        public void write (DataOutputStream out) throws IOException
        {
            super.write(out);
            out.writeShort(numberOfClasses);
            for(int i = 0; i < numberOfClasses; i++)
                classes[i].write(out);
        }

        public static class ClassEntry
        {
            private int innnerClassInfoIndex;
            private int outerClassInfoIndex;
            private int innnerNameIndex;
            private int innnerClassAccessFlags;

            public static final int PUBLIC = 0x0001;
            public static final int PRIVATE = 0x0002;
            public static final int PROTECTED = 0x0004;
            public static final int STATIC = 0x0008;
            public static final int FINAL = 0x0010;
            public static final int INTERFACE = 0x0200;
            public static final int ABSTRACT = 0x0400;
            public static final int SYNTHETIC = 0x1000;
            public static final int ANNOTATION = 0x2000;
            public static final int ENUM = 0x4000;

            ClassEntry(DataInputStream in) throws IOException
            {
                innnerClassInfoIndex = in.readUnsignedShort();
                outerClassInfoIndex = in.readUnsignedShort();
                innnerNameIndex = in.readUnsignedShort();
                innnerClassAccessFlags = in.readUnsignedShort();
            }

            void write(DataOutputStream out) throws IOException
            {
                out.writeShort(innnerClassInfoIndex);
                out.writeShort(outerClassInfoIndex);
                out.writeShort(innnerNameIndex);
                out.writeShort(innnerClassAccessFlags);
            }
        }

    }


    public static class EnclosingMethodAttribute extends Attribute
    {
        private int classIndex;
        private int methodIndex;

        EnclosingMethodAttribute(DataInputStream in, int index) throws IOException
        {
            super(in, index);
            classIndex = in.readUnsignedShort();
            methodIndex = in.readUnsignedShort();
        }
        
        public void write(DataOutputStream out) throws IOException
        {
            super.write(out);
            out.writeShort(classIndex);
            out.writeShort(methodIndex);
        }
    }


    public static class SyntheticAttribute extends Attribute
    {
        SyntheticAttribute(DataInputStream in, int index) throws IOException
        {
            super(in, index);
        }
    }


    public static class SignatureAttribute extends Attribute
    {
        private int classIndex;

        SignatureAttribute(DataInputStream in, int index) throws IOException
        {
            super(in, index);
            classIndex = in.readUnsignedShort();
        }
        
        public void write(DataOutputStream out) throws IOException
        {
            super.write(out);
            out.writeShort(classIndex);
        }
    }


    public static class SourceFileAttribute extends Attribute
    {
        private int sourceFileIndex;

        SourceFileAttribute(DataInputStream in, int index) throws IOException
        {
            super(in, index);
            sourceFileIndex = in.readUnsignedShort();
        }

        public void write(DataOutputStream out) throws IOException
        {
            super.write(out);
            out.writeShort(sourceFileIndex);
        }
    }


    public static class LineNumberTableAttribute extends Attribute
    {
        private int lineNumberTableLength;
        private LineNumberEntry[] lineNumberTable;

        LineNumberTableAttribute(DataInputStream in, int index) throws IOException
        {
            super(in, index);
            lineNumberTableLength = in.readUnsignedShort();
            lineNumberTable = new LineNumberEntry[lineNumberTableLength];
            for(int i = 0; i < lineNumberTableLength; i++)
                lineNumberTable[i] = new LineNumberEntry(in);
        }

        public void write(DataOutputStream out) throws IOException
        {
            super.write(out);
            out.writeShort(lineNumberTableLength);
            for(int i = 0; i < lineNumberTableLength; i++)
                lineNumberTable[i].write(out);
        }

        public static class LineNumberEntry
        {
            private int startPC;
            private int lineNumber;

            LineNumberEntry(DataInputStream in) throws IOException
            {
                startPC = in.readUnsignedShort();
                lineNumber = in.readUnsignedShort();
            }

            public void write(DataOutputStream out) throws IOException
            {
                out.writeShort(startPC);
                out.writeShort(lineNumber);
            }
        }
    }


    public static class LocalVariableTableAttribute extends Attribute
    {
        private int localVariableTableLength;
        private LocalVariableEntry[] localVariableTable;

        LocalVariableTableAttribute(DataInputStream in, int index) throws IOException
        {
            super(in, index);
            localVariableTableLength = in.readUnsignedShort();
            localVariableTable = new LocalVariableEntry[localVariableTableLength];
            for(int i = 0; i < localVariableTableLength; i++)
                localVariableTable[i] = new LocalVariableEntry(in);
        }

        public void write(DataOutputStream out) throws IOException
        {
            super.write(out);
            out.writeShort(localVariableTableLength);
            for(int i = 0; i < localVariableTableLength; i++)
                localVariableTable[i].write(out);
        }

        public static class LocalVariableEntry
        {
            private int startPC;
            private int length;
            private int nameIndex;
            private int descriptorIndex;
            private int index;

            LocalVariableEntry(DataInputStream in) throws IOException
            {
                startPC = in.readUnsignedShort();
                length = in.readUnsignedShort();
                nameIndex = in.readUnsignedShort();
                descriptorIndex = in.readUnsignedShort();
                index = in.readUnsignedShort();
            }

            public void write(DataOutputStream out) throws IOException
            {
                out.writeShort(startPC);
                out.writeShort(length);
                out.writeShort(nameIndex);
                out.writeShort(descriptorIndex);
                out.writeShort(index);
            }
        }
    }


    public static class DeprecatedAttribute extends Attribute
    {
        DeprecatedAttribute(DataInputStream in, int index) throws IOException
        {
            super(in, index);
        }
    }

    public static class UnknownAttribute extends Attribute
    {
        // what to do here??
        // blank the data
        // or do we retain old (possiblely out of date) data??

        int[] bytes;

        UnknownAttribute(DataInputStream in, int index) throws IOException
        {
            super(in, index);
            bytes = new int[attributeLength];
            for(int i = 0; i < attributeLength; i++)
                bytes[i] = in.readUnsignedByte();
            
        }

        public void write(DataOutputStream out) throws IOException
        {
            super.write(out);
            for(int i = 0; i < attributeLength; i++)
                out.writeByte(bytes[i]);
        }
    }
}
