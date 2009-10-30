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
import java.lang.reflect.*;
import java.util.*;

public class ClassFile {
    private int magic;
    private int minorVersion;
    private int majorVersion;
    private int constantPoolCount;
    private ConstantPoolInfo[] constantPool;
    private Map<ConstantPoolInfo, Integer> constantPoolMap;

    private int accessFlags;
    private int thisClass;
    private int superClass;
    private int interfacesCount;
    private int[] interfaces;
    private int fieldsCount;
    private FieldInfo[] fields;
    private int methodsCount;
    private MethodInfo[] methods;
    private int attributesCount;
    private AttributeInfo[] attributes;

    public static final short PUBLIC = (short) 0x0001;
    public static final short FINAL = (short) 0x0010;
    public static final short SUPER = (short) 0x0020;
    public static final short INTERFACE = (short) 0x0200;
    public static final short ABSTRACT = (short) 0x0400;

    public static final int MAX_CONSTANT_POOL_SIZE = 64 * 1024;

    public void read(DataInputStream in) throws IOException {
        readMagic(in);
        // System.out.println("magic");
        readVersion(in);
        // System.out.println("version");
        readConstantPool(in);
        // System.out.println("constpool");
        readAccessFlags(in);
        // System.out.println("accflags");
        readThisClass(in);
        // System.out.println("thisclass");
        readSuperClass(in);
        // System.out.println("superclass");
        readInterfaces(in);
        // System.out.println("interfaces");
        readFields(in, constantPool);
        // System.out.println("fields");
        readMethods(in, constantPool);
        // System.out.println("methods");
        readAttributes(in, constantPool);
        // System.out.println("attributes");
        return;
    }

    public void write(DataOutputStream out) throws IOException {
        writeMagic(out);
        // System.out.println("magic");
        writeVersion(out);
        // System.out.println("version");
        writeConstantPool(out);
        // System.out.println("constpool");
        writeAccessFlags(out);
        // System.out.println("accflags");
        writeThisClass(out);
        // System.out.println("thisclass");
        writeSuperClass(out);
        // System.out.println("superclass");
        writeInterfaces(out);
        // System.out.println("interfaces");
        writeFields(out);
        // System.out.println("fields");
        writeMethods(out);
        // System.out.println("methods");
        writeAttributes(out);
        // System.out.println("attributes");
        return;
    }

    public void update() {
        /*
         * this function will supposibly sync all class info. for now I am
         * hoping that by changing to a lower version I can get the class loader
         * to ignore half my problems
         */
        minorVersion = 0;
        majorVersion = 46;
    }

    public String[] getMethodNames() {
        String[] names = new String[methodsCount];
        for (int i = 0; (i < methodsCount); i++) {
            int index = methods[i].getNameIndex();
            names[i] = ((ConstantPoolInfo.Utf8Info) constantPool[index])
                    .getBytes();
        }
        return names;
    }

    public int[] getMethodCode(String methodName) {
        MethodInfo mi = getMethodInfo(methodName);
        return mi.getCode();
    }

    public void setMethodCode(String methodName, int[] codeBytes) {
        setMethodCode(methodName, codeBytes, codeBytes.length);
    }

    public void setMethodCode(String methodName, int[] codeBytes,
            int codeBytesLength) {
        MethodInfo mi = getMethodInfo(methodName);
        mi.setCode(codeBytes, codeBytesLength, this);
    }

    public AttributeInfo.CodeAttribute.ExceptionEntry[] getMethodExceptionTable(
            String methodName) {
        MethodInfo mi = getMethodInfo(methodName);
        return mi.getExceptionTable();
    }

    public void setMethodExceptionTable(String methodName,
            AttributeInfo.CodeAttribute.ExceptionEntry[] exceptionTable) {
        setMethodExceptionTable(methodName, exceptionTable,
                exceptionTable.length);
    }

    public void setMethodExceptionTable(String methodName,
            AttributeInfo.CodeAttribute.ExceptionEntry[] exceptionTable,
            int exceptionTableLength) {
        MethodInfo mi = getMethodInfo(methodName);
        mi.setExceptionTable(exceptionTable, exceptionTableLength, this);
    }

    public String getClassName() {
        if (constantPool[thisClass].getTag() != ConstantPoolInfo.CLASS)
            throw new ClassFormatError(
                    "thisClass points to non-class constant pool entry");

        int nameIndex = ((ConstantPoolInfo.ClassInfo) constantPool[thisClass])
                .getNameIndex();

        if (constantPool[nameIndex].getTag() != ConstantPoolInfo.UTF8)
            throw new ClassFormatError(
                    "thisClass constant pool entry points to non-utf8 constant pool entry");

        return ((ConstantPoolInfo.Utf8Info) constantPool[nameIndex]).getBytes()
                .replace('/', '.');
    }

    public void setClassName(String name) {
        if (constantPool[thisClass].getTag() != ConstantPoolInfo.CLASS)
            throw new ClassFormatError(
                    "thisClass points to non-class constant pool entry");

        int nameIndex = ((ConstantPoolInfo.ClassInfo) constantPool[thisClass])
                .getNameIndex();

        if (constantPool[nameIndex].getTag() != ConstantPoolInfo.UTF8)
            throw new ClassFormatError(
                    "thisClass constant pool entry points to non-utf8 constant pool entry");

        constantPool[nameIndex] = new ConstantPoolInfo.Utf8Info(name.replace(
                '.', '/'));
    }

    /** @return index into constant pool where value is stored */
    @SuppressWarnings("unchecked")
    public int addToConstantPool(Object o) {
        ConstantPoolInfo cpInfo = null;

        if (o instanceof Field) {
            Field fld = (Field) o;
            String descriptor = getDescriptor(fld.getType());

            ConstantPoolInfo nameInfo = new ConstantPoolInfo.Utf8Info(fld
                    .getName());
            int nameIndex = addToConstantPool(nameInfo);
            ConstantPoolInfo descriptorInfo = new ConstantPoolInfo.Utf8Info(
                    descriptor);
            int descriptorIndex = addToConstantPool(descriptorInfo);
            ConstantPoolInfo nameAndTypeInfo = new ConstantPoolInfo.NameAndTypeInfo(
                    nameIndex, descriptorIndex);
            int nameAndTypeIndex = addToConstantPool(nameAndTypeInfo);

            Class<?> cls = ((Field) o).getDeclaringClass();
            int classIndex = addToConstantPool(cls);

            cpInfo = new ConstantPoolInfo.FieldRefInfo(classIndex,
                    nameAndTypeIndex);
        } else if (o instanceof Method) {
            Method mtd = (Method) o;
            Class[] params = mtd.getParameterTypes();
            StringBuffer buf = new StringBuffer("(");
            for (int i = 0; i < params.length; i++)
                buf.append(getDescriptor(params[i]));
            buf.append(")");
            buf.append(getDescriptor(mtd.getReturnType()));
            String descriptor = buf.toString();

            ConstantPoolInfo nameInfo = new ConstantPoolInfo.Utf8Info(mtd
                    .getName());
            int nameIndex = addToConstantPool(nameInfo);
            ConstantPoolInfo descriptorInfo = new ConstantPoolInfo.Utf8Info(
                    descriptor);
            int descriptorIndex = addToConstantPool(descriptorInfo);
            ConstantPoolInfo nameAndTypeInfo = new ConstantPoolInfo.NameAndTypeInfo(
                    nameIndex, descriptorIndex);
            int nameAndTypeIndex = addToConstantPool(nameAndTypeInfo);

            Class cls = mtd.getDeclaringClass();
            int classIndex = addToConstantPool(cls);

            if (cls.isInterface())
                cpInfo = new ConstantPoolInfo.InterfaceMethodRefInfo(
                        classIndex, nameAndTypeIndex);
            else
                cpInfo = new ConstantPoolInfo.MethodRefInfo(classIndex,
                        nameAndTypeIndex);
        } else if (o instanceof Class) {
            Class cls = (Class) o;
            String className = cls.getName().replace('.', '/');
            cpInfo = new ConstantPoolInfo.Utf8Info(className);
            int utf8Index = addToConstantPool(cpInfo);
            cpInfo = new ConstantPoolInfo.ClassInfo(utf8Index);
        } else if (o instanceof String) {
            cpInfo = new ConstantPoolInfo.Utf8Info((String) o);
            int utf8Index = addToConstantPool(cpInfo);
            cpInfo = new ConstantPoolInfo.StringInfo(utf8Index);
        } else if (o instanceof Integer)
            cpInfo = new ConstantPoolInfo.IntegerInfo(((Integer) o).intValue());
        else if (o instanceof Float)
            cpInfo = new ConstantPoolInfo.FloatInfo(((Float) o).floatValue());
        else if (o instanceof Long)
            cpInfo = new ConstantPoolInfo.LongInfo(((Long) o).longValue());
        else if (o instanceof Double)
            cpInfo = new ConstantPoolInfo.DoubleInfo(((Double) o).doubleValue());
        else if (o instanceof ConstantPoolInfo)
            cpInfo = (ConstantPoolInfo) o;
        else
            throw new IllegalArgumentException(
                    "Invalid Class To Add To Constant Pool");

        int index = searchConstantPool(cpInfo);
        if (index > 0)
            return index;

        if ((cpInfo instanceof ConstantPoolInfo.DoubleInfo)
                || (cpInfo instanceof ConstantPoolInfo.LongInfo)) {
            constantPool[constantPoolCount] = cpInfo;
            constantPoolMap.put(cpInfo, new Integer(constantPoolCount));
            constantPool[constantPoolCount + 1] = cpInfo;
            constantPoolCount += 2;
            return constantPoolCount - 2;
        } else {
            constantPool[constantPoolCount] = cpInfo;
            constantPoolMap.put(cpInfo, new Integer(constantPoolCount));
            constantPoolCount++;
            return constantPoolCount - 1;
        }
    }

    public int getMethodMaxStack(String methodName) {
        MethodInfo mi = getMethodInfo(methodName);
        return mi.getMaxStack();
    }

    public int getMethodMaxLocals(String methodName) {
        MethodInfo mi = getMethodInfo(methodName);
        return mi.getMaxLocals();
    }

    protected String getConstantPoolFieldDescriptor(int index) {
        ConstantPoolInfo cpi = constantPool[index];
        // get name and type index from method ref
        index = ((ConstantPoolInfo.FieldRefInfo) cpi).getNameAndTypeIndex();
        cpi = constantPool[index];
        // get descriptor index from name and type
        index = ((ConstantPoolInfo.NameAndTypeInfo) cpi).getDescriptorIndex();
        cpi = constantPool[index];

        return ((ConstantPoolInfo.Utf8Info) cpi).getBytes();
    }

    protected int getFieldLength(String fieldDescriptor) {
        return getFieldLength(fieldDescriptor.charAt(0));
    }

    private int getFieldLength(char ch) {
        switch (ch) {
        case 'V':
            return 0;
        case 'B':
        case 'C':
        case 'F':
        case 'I':
        case 'L':
        case 'S':
        case 'Z':
        case '[':
            return 1;
        case 'D':
        case 'J':
            return 2;
        }
        throw new IllegalStateException();
    }

    protected String getConstantPoolUtf8(int index) {
        return ((ConstantPoolInfo.Utf8Info) constantPool[index]).getBytes();
    }

    protected String getConstantPoolMethodDescriptor(int index) {
        ConstantPoolInfo cpi = constantPool[index];
        // get name and type index from method ref
        index = ((ConstantPoolInfo.MethodRefInfo) cpi).getNameAndTypeIndex();
        cpi = constantPool[index];
        // get descriptor index from name and type
        index = ((ConstantPoolInfo.NameAndTypeInfo) cpi).getDescriptorIndex();
        cpi = constantPool[index];

        return ((ConstantPoolInfo.Utf8Info) cpi).getBytes();
    }

    /**
     * @return stack delta caused by an invoke on this method descriptor -- delta
     *         = within () - outside ()
     */
    @SuppressWarnings("unused")
    protected int getMethodStackDelta(String methodDescriptor) {
        // System.out.println("methodDescriptor = " + methodDescriptor);
        // int end = methodDescriptor.lastIndexOf(")");
        // int begin = methodDescriptor.lastIndexOf("(", end);
        // String s = methodDescriptor.substring(begin + 1, end);
        // System.out.println("methodDescriptor = " + methodDescriptor);

        int argLength = getMethodArgLength(methodDescriptor);

        int count = 0;
        int delta = 0;
        boolean inReference = false;
        boolean inParameterDescriptor = false;
        char ch;
        for (int i = 0; i < methodDescriptor.length(); i++) {
            ch = methodDescriptor.charAt(i);
            if (ch == ')')
                return argLength
                        - getFieldLength(methodDescriptor.charAt(i + 1));
        }
        throw new IllegalStateException("Invalid method descriptor");
    }

    /** @return count of arguments -- within () */
    @SuppressWarnings("unused")
    int getMethodArgLength(String methodDescriptor) {
        int count = 0;
        boolean inReference = false;

        for (int i = 0; i < methodDescriptor.length(); i++) {
            char ch = methodDescriptor.charAt(i);
            switch (ch) {
            case '[':
                while ((ch = methodDescriptor.charAt(++i)) == '[')
                    ;
                if (ch != 'L') {
                    count += 1;
                    break;
                }
            case 'L':
                while (methodDescriptor.charAt(++i) != ';')
                    ;
                count += 1;
                break;
            case 'B':
            case 'C':
            case 'F':
            case 'I':
            case 'S':
            case 'Z':
                count += 1;
                break;
            case 'D':
            case 'J':
                count += 2;
                break;
            case ')':
                return count;
            default:
                break;
            }
        }
        throw new IllegalStateException("Invalid method descriptor");
    }

    private static String getDescriptor(Class<?> cls) {
        if (cls.isArray())
            return cls.getName().replace('.', '/');
        else {
            if (cls.isPrimitive()) {
                if (cls.equals(Byte.TYPE))
                    return "B";
                else if (cls.equals(Character.TYPE))
                    return "C";
                else if (cls.equals(Double.TYPE))
                    return "D";
                else if (cls.equals(Float.TYPE))
                    return "F";
                else if (cls.equals(Integer.TYPE))
                    return "I";
                else if (cls.equals(Long.TYPE))
                    return "J";
                else if (cls.equals(Short.TYPE))
                    return "S";
                else if (cls.equals(Boolean.TYPE))
                    return "Z";
                else if (cls.equals(Void.TYPE))
                    return "V";
            } else {
                return 'L' + cls.getName().replace('.', '/') + ';';
            }
        }
        throw new IllegalStateException(
                "They added a primitive!!! Is it unsigned!!! " + cls.getName());
    }

    private int searchConstantPool(ConstantPoolInfo query) {
        Integer value = (Integer) (constantPoolMap.get(query));
        if (value != null)
            return value.intValue();
        else
            return -1;

        // for (int i = 1; i < constantPoolCount; i++) {
        // if (constantPool[i].equals(query))
        // return i;
        // }
        // return -1;
    }

    private MethodInfo getMethodInfo(String methodName) {
        for (int i = 0; (i < methodsCount); i++) {
            int index = methods[i].getNameIndex();
            if (constantPool[index].getTag() == ConstantPoolInfo.UTF8) {
                if (((ConstantPoolInfo.Utf8Info) constantPool[index])
                        .getBytes().equals(methodName))
                    return methods[i];
            }
        }
        return null;
    }

    private void readMagic(DataInputStream in) throws IOException {
        magic = in.readInt();
    }

    private void writeMagic(DataOutputStream out) throws IOException {
        out.writeInt(magic);
    }

    private void readVersion(DataInputStream in) throws IOException {
        minorVersion = in.readUnsignedShort();
        majorVersion = in.readUnsignedShort();
    }

    private void writeVersion(DataOutputStream out) throws IOException {
        out.writeShort(minorVersion);
        out.writeShort(majorVersion);
    }

    private void readConstantPool(DataInputStream in) throws IOException {
        constantPoolCount = in.readUnsignedShort();
        // be aware that constant pool indices start at 1!! (not 0)
        constantPool = new ConstantPoolInfo[MAX_CONSTANT_POOL_SIZE];
        constantPoolMap = new HashMap<ConstantPoolInfo, Integer>();
        // constantPool = new ConstantPoolInfo[constantPoolCount];
        for (int i = 1; i < constantPoolCount; i++) {
            ConstantPoolInfo cpInfo = ConstantPoolInfo.construct(in);
            constantPool[i] = cpInfo;
            constantPoolMap.put(cpInfo, new Integer(i));
            if ((constantPool[i] instanceof ConstantPoolInfo.DoubleInfo)
                    || (constantPool[i] instanceof ConstantPoolInfo.LongInfo)) {
                i++;
                constantPool[i] = constantPool[i - 1];
            }
        }
    }

    private void writeConstantPool(DataOutputStream out) throws IOException {
        out.writeShort(constantPoolCount);
        // be aware that constant pool indices start at 1!! (not 0)
        for (int i = 1; i < constantPoolCount; i++) {
            constantPool[i].write(out);
            if ((constantPool[i] instanceof ConstantPoolInfo.DoubleInfo)
                    || (constantPool[i] instanceof ConstantPoolInfo.LongInfo))
                i++;
        }
    }

    private void readAccessFlags(DataInputStream in) throws IOException {
        accessFlags = in.readUnsignedShort();
    }

    private void writeAccessFlags(DataOutputStream out) throws IOException {
        out.writeShort(accessFlags);
    }

    private void readThisClass(DataInputStream in) throws IOException {
        thisClass = in.readUnsignedShort();
    }

    private void writeThisClass(DataOutputStream out) throws IOException {
        out.writeShort(thisClass);
    }

    private void readSuperClass(DataInputStream in) throws IOException {
        superClass = in.readUnsignedShort();
    }

    private void writeSuperClass(DataOutputStream out) throws IOException {
        out.writeShort(superClass);
    }

    private void readInterfaces(DataInputStream in) throws IOException {
        interfacesCount = in.readUnsignedShort();
        interfaces = new int[interfacesCount];
        for (int i = 0; i < interfacesCount; i++) {
            interfaces[i] = in.readUnsignedShort();
        }
    }

    private void writeInterfaces(DataOutputStream out) throws IOException {
        out.writeShort(interfacesCount);
        for (int i = 0; i < interfacesCount; i++)
            out.writeShort(interfaces[i]);
    }

    private void readFields(DataInputStream in, ConstantPoolInfo[] pool)
            throws IOException {
        fieldsCount = in.readUnsignedShort();
        fields = new FieldInfo[fieldsCount];
        for (int i = 0; (i < fieldsCount); i++) {
            fields[i] = new FieldInfo(in, pool);
        }
    }

    private void writeFields(DataOutputStream out) throws IOException {
        out.writeShort(fieldsCount);
        for (int i = 0; (i < fieldsCount); i++)
            fields[i].write(out);
    }

    private void readMethods(DataInputStream in, ConstantPoolInfo[] pool)
            throws IOException {
        methodsCount = in.readUnsignedShort();
        methods = new MethodInfo[methodsCount];
        for (int i = 0; (i < methodsCount); i++)
            methods[i] = new MethodInfo(in, pool);
    }

    private void writeMethods(DataOutputStream out) throws IOException {
        out.writeShort(methodsCount);
        for (int i = 0; (i < methodsCount); i++)
            methods[i].write(out);
    }

    private void readAttributes(DataInputStream in, ConstantPoolInfo[] pool)
            throws IOException {
        attributesCount = in.readUnsignedShort();
        attributes = new AttributeInfo[attributesCount];
        for (int i = 0; (i < attributesCount); i++)
            attributes[i] = AttributeInfo.construct(in, pool);
    }

    private void writeAttributes(DataOutputStream out) throws IOException {
        out.writeShort(attributesCount);
        for (int i = 0; (i < attributesCount); i++)
            attributes[i].write(out);
    }
}
