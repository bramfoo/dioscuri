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

/**
 *
 * @author Bram Lohman
 * @author Bart Kiers
 */
public abstract class ConstantPoolInfo {
    /**
     *
     */
    public static final int CLASS = 7;
    /**
     *
     */
    public static final int FIELDREF = 9;
    /**
     *
     */
    public static final int METHODREF = 10;
    /**
     *
     */
    public static final int INTERFACEMETHODREF = 11;
    /**
     *
     */
    public static final int STRING = 8;
    /**
     *
     */
    public static final int INTEGER = 3;
    /**
     *
     */
    public static final int FLOAT = 4;
    /**
     *
     */
    public static final int LONG = 5;
    /**
     *
     */
    public static final int DOUBLE = 6;
    /**
     *
     */
    public static final int NAMEANDTYPE = 12;
    /**
     *
     */
    public static final int UTF8 = 1;

    /**
     *
     * @return
     */
    public abstract int getTag();

    /**
     *
     * @param out
     * @throws IOException
     */
    public abstract void write(DataOutputStream out) throws IOException;

    @Override
    public abstract boolean equals(Object obj);

    /**
     *
     * @param in
     * @return
     * @throws IOException
     */
    public static ConstantPoolInfo construct(DataInputStream in)
            throws IOException {
        int tag = in.readUnsignedByte();
        switch (tag) {
        case CLASS:
            return new ClassInfo(in);
        case FIELDREF:
            return new FieldRefInfo(in);
        case METHODREF:
            return new MethodRefInfo(in);
        case INTERFACEMETHODREF:
            return new InterfaceMethodRefInfo(in);
        case STRING:
            return new StringInfo(in);
        case INTEGER:
            return new IntegerInfo(in);
        case FLOAT:
            return new FloatInfo(in);
        case LONG:
            return new LongInfo(in);
        case DOUBLE:
            return new DoubleInfo(in);
        case NAMEANDTYPE:
            return new NameAndTypeInfo(in);
        case UTF8:
            return new Utf8Info(in);
        }
        return null;
    }

    /**
     *
     */
    public static class ClassInfo extends ConstantPoolInfo {
        private final int nameIndex;
        private final int hashCode;

        ClassInfo(DataInputStream in) throws IOException {
            nameIndex = in.readUnsignedShort();
            hashCode = (this.getClass().hashCode() * 31) ^ (nameIndex * 37);
        }

        ClassInfo(int val) {
            nameIndex = val;
            hashCode = (this.getClass().hashCode() * 31) ^ (nameIndex * 37);
        }

        /**
         *
         * @return
         */
        public int getTag() {
            return CLASS;
        }

        /**
         *
         * @return
         */
        public int getNameIndex() {
            return nameIndex;
        }

        /**
         *
         * @param out
         * @throws IOException
         */
        public void write(DataOutputStream out) throws IOException {
            out.writeByte(CLASS);
            out.writeShort(nameIndex);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        public boolean equals(Object obj) {
            if (obj == this)
                return true;

            if (!(obj instanceof ClassInfo))
                return false;

            return getNameIndex() == ((ClassInfo) obj).getNameIndex();
        }

        @Override
        public String toString() {
            return "CONSTANT_Class_info : name=" + getNameIndex();
        }
    }

    abstract static class RefInfo extends ConstantPoolInfo {
        private final int classIndex;
        private final int nameAndTypeIndex;
        private final int hashCode;

        RefInfo(DataInputStream in) throws IOException {
            classIndex = in.readUnsignedShort();
            nameAndTypeIndex = in.readUnsignedShort();
            hashCode = (this.getClass().hashCode() * 31) ^ (classIndex * 37)
                    ^ (nameAndTypeIndex * 41);
        }

        RefInfo(int classIndex, int nameAndTypeIndex) {
            this.classIndex = classIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
            hashCode = (this.getClass().hashCode() * 31) ^ (classIndex * 37)
                    ^ (nameAndTypeIndex * 41);
        }

        public int getClassIndex() {
            return classIndex;
        }

        public int getNameAndTypeIndex() {
            return nameAndTypeIndex;
        }

        public void write(DataOutputStream out) throws IOException {
            out.writeByte(getTag());
            out.writeShort(classIndex);
            out.writeShort(nameAndTypeIndex);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        public boolean equals(Object obj) {
            if (obj == this)
                return true;

            if (!(obj instanceof RefInfo))
                return false;

            if (getTag() != ((RefInfo) obj).getTag())
                return false;

            return (getClassIndex() == ((RefInfo) obj).getClassIndex())
                    && (getNameAndTypeIndex() == ((RefInfo) obj)
                            .getNameAndTypeIndex());
        }
    }

    /**
     *
     */
    public static class FieldRefInfo extends RefInfo {
        FieldRefInfo(DataInputStream in) throws IOException {
            super(in);
        }

        FieldRefInfo(int classIndex, int nameAndTypeIndex) {
            super(classIndex, nameAndTypeIndex);
        }

        /**
         *
         * @return
         */
        public int getTag() {
            return FIELDREF;
        }

        @Override
        public String toString() {
            return "CONSTANT_FieldRef_info : class=" + getClassIndex()
                    + " : nameandtype=" + getNameAndTypeIndex();
        }
    }

    /**
     *
     */
    public static class MethodRefInfo extends RefInfo {
        MethodRefInfo(DataInputStream in) throws IOException {
            super(in);
        }

        MethodRefInfo(int classIndex, int nameAndTypeIndex) {
            super(classIndex, nameAndTypeIndex);
        }

        /**
         *
         * @return
         */
        public int getTag() {
            return METHODREF;
        }

        @Override
        public String toString() {
            return "CONSTANT_MethodRef_info : class=" + getClassIndex()
                    + " : nameandtype=" + getNameAndTypeIndex();
        }
    }

    /**
     *
     */
    public static class InterfaceMethodRefInfo extends MethodRefInfo {
        InterfaceMethodRefInfo(DataInputStream in) throws IOException {
            super(in);
        }

        InterfaceMethodRefInfo(int classIndex, int nameAndTypeIndex) {
            super(classIndex, nameAndTypeIndex);
        }

        /**
         *
         * @return
         */
        @Override
        public int getTag() {
            return INTERFACEMETHODREF;
        }

        @Override
        public String toString() {
            return "CONSTANT_InterfaceMethodRef_info : class="
                    + getClassIndex() + " : nameandtype="
                    + getNameAndTypeIndex();
        }
    }

    /**
     *
     */
    public static class StringInfo extends ConstantPoolInfo {
        private final int stringIndex;
        private final int hashCode;

        StringInfo(DataInputStream in) throws IOException {
            stringIndex = in.readUnsignedShort();
            hashCode = (this.getClass().hashCode() * 31) ^ (stringIndex * 37);
        }

        StringInfo(int val) {
            stringIndex = val;
            hashCode = (this.getClass().hashCode() * 31) ^ (stringIndex * 37);
        }

        /**
         *
         * @return
         */
        public int getTag() {
            return STRING;
        }

        /**
         *
         * @return
         */
        public int getStringIndex() {
            return stringIndex;
        }

        /**
         *
         * @param out
         * @throws IOException
         */
        public void write(DataOutputStream out) throws IOException {
            out.writeByte(STRING);
            out.writeShort(stringIndex);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        public boolean equals(Object obj) {
            if (obj == this)
                return true;

            if (!(obj instanceof StringInfo))
                return false;

            return getStringIndex() == ((StringInfo) obj).getStringIndex();
        }

        @Override
        public String toString() {
            return "CONSTANT_String_info : string=" + getStringIndex();
        }
    }

    /**
     *
     */
    public static class IntegerInfo extends ConstantPoolInfo {
        private final int bytes;
        private final int hashCode;

        IntegerInfo(DataInputStream in) throws IOException {
            bytes = in.readInt();
            hashCode = (this.getClass().hashCode() * 31) ^ (bytes * 37);
        }

        IntegerInfo(int val) {
            bytes = val;
            hashCode = (this.getClass().hashCode() * 31) ^ (bytes * 37);
        }

        /**
         *
         * @return
         */
        public int getTag() {
            return INTEGER;
        }

        /**
         *
         * @return
         */
        public int getBytes() {
            return bytes;
        }

        /**
         *
         * @param out
         * @throws IOException
         */
        public void write(DataOutputStream out) throws IOException {
            out.writeByte(INTEGER);
            out.writeInt(bytes);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        public boolean equals(Object obj) {
            if (obj == this)
                return true;

            if (!(obj instanceof IntegerInfo))
                return false;

            return getBytes() == ((IntegerInfo) obj).getBytes();
        }

        @Override
        public String toString() {
            return "CONSTANT_Integer_info : value=" + getBytes();
        }
    }

    /**
     *
     */
    public static class FloatInfo extends ConstantPoolInfo {
        private final float bytes;
        private final int hashCode;

        FloatInfo(DataInputStream in) throws IOException {
            bytes = in.readFloat();
            hashCode = (this.getClass().hashCode() * 31)
                    ^ (Float.floatToRawIntBits(bytes) * 37);
        }

        FloatInfo(float val) {
            bytes = val;
            hashCode = (this.getClass().hashCode() * 31)
                    ^ (Float.floatToRawIntBits(bytes) * 37);
        }

        /**
         *
         * @return
         */
        public int getTag() {
            return FLOAT;
        }

        /**
         *
         * @return
         */
        public float getBytes() {
            return bytes;
        }

        /**
         *
         * @param out
         * @throws IOException
         */
        public void write(DataOutputStream out) throws IOException {
            out.writeByte(FLOAT);
            out.writeFloat(bytes);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        public boolean equals(Object obj) {
            if (obj == this)
                return true;

            if (!(obj instanceof FloatInfo))
                return false;

            return getBytes() == ((FloatInfo) obj).getBytes();
        }

        @Override
        public String toString() {
            return "CONSTANT_Float_info : value=" + getBytes();
        }
    }

    /**
     *
     */
    public static class LongInfo extends ConstantPoolInfo {
        private final long bytes;
        private final int hashCode;

        LongInfo(DataInputStream in) throws IOException {
            bytes = in.readLong();
            hashCode = (this.getClass().hashCode() * 31) ^ (((int) bytes) * 37)
                    ^ (((int) (bytes >>> 32)) * 41);
        }

        LongInfo(long val) {
            bytes = val;
            hashCode = (this.getClass().hashCode() * 31) ^ (((int) bytes) * 37)
                    ^ (((int) (bytes >>> 32)) * 41);
        }

        /**
         *
         * @return
         */
        public int getTag() {
            return LONG;
        }

        /**
         *
         * @return
         */
        public long getBytes() {
            return bytes;
        }

        /**
         *
         * @param out
         * @throws IOException
         */
        public void write(DataOutputStream out) throws IOException {
            out.writeByte(LONG);
            out.writeLong(bytes);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        public boolean equals(Object obj) {
            if (obj == this)
                return true;

            if (!(obj instanceof LongInfo))
                return false;

            return getBytes() == ((LongInfo) obj).getBytes();
        }

        @Override
        public String toString() {
            return "CONSTANT_Long_info : value=" + getBytes();
        }
    }

    /**
     *
     */
    public static class DoubleInfo extends ConstantPoolInfo {
        private final double bytes;
        private final int hashCode;

        DoubleInfo(DataInputStream in) throws IOException {
            bytes = in.readDouble();
            long longBytes = Double.doubleToRawLongBits(bytes);
            hashCode = (this.getClass().hashCode() * 31)
                    ^ (((int) longBytes) * 37)
                    ^ (((int) (longBytes >>> 32)) * 41);
        }

        DoubleInfo(double val) {
            bytes = val;
            long longBytes = Double.doubleToRawLongBits(bytes);
            hashCode = (this.getClass().hashCode() * 31)
                    ^ (((int) longBytes) * 37)
                    ^ (((int) (longBytes >>> 32)) * 41);
        }

        /**
         *
         * @return
         */
        public int getTag() {
            return DOUBLE;
        }

        /**
         *
         * @return
         */
        public double getBytes() {
            return bytes;
        }

        /**
         *
         * @param out
         * @throws IOException
         */
        public void write(DataOutputStream out) throws IOException {
            out.writeByte(DOUBLE);
            out.writeDouble(bytes);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        public boolean equals(Object obj) {
            if (obj == this)
                return true;

            if (!(obj instanceof DoubleInfo))
                return false;

            return getBytes() == ((DoubleInfo) obj).getBytes();
        }

        @Override
        public String toString() {
            return "CONSTANT_Double_info : value=" + getBytes();
        }
    }

    /**
     *
     */
    public static class NameAndTypeInfo extends ConstantPoolInfo {
        private final int nameIndex;
        private final int descriptorIndex;
        private final int hashCode;

        NameAndTypeInfo(DataInputStream in) throws IOException {
            nameIndex = in.readUnsignedShort();
            descriptorIndex = in.readUnsignedShort();
            hashCode = (this.getClass().hashCode() * 31) ^ (nameIndex * 37)
                    ^ (descriptorIndex * 41);
        }

        NameAndTypeInfo(int nameIndex, int descriptorIndex) {
            this.nameIndex = nameIndex;
            this.descriptorIndex = descriptorIndex;
            hashCode = (this.getClass().hashCode() * 31) ^ (nameIndex * 37)
                    ^ (descriptorIndex * 41);
        }

        /**
         *
         * @return
         */
        public int getTag() {
            return NAMEANDTYPE;
        }

        /**
         *
         * @return
         */
        public int getNameIndex() {
            return nameIndex;
        }

        /**
         *
         * @return
         */
        public int getDescriptorIndex() {
            return descriptorIndex;
        }

        /**
         *
         * @param out
         * @throws IOException
         */
        public void write(DataOutputStream out) throws IOException {
            out.writeByte(NAMEANDTYPE);
            out.writeShort(nameIndex);
            out.writeShort(descriptorIndex);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        public boolean equals(Object obj) {
            if (obj == this)
                return true;

            if (!(obj instanceof NameAndTypeInfo))
                return false;

            return (getNameIndex() == ((NameAndTypeInfo) obj).getNameIndex())
                    && (getDescriptorIndex() == ((NameAndTypeInfo) obj)
                            .getDescriptorIndex());
        }

        @Override
        public String toString() {
            return "CONSTANT_NameAndType_info : descriptor="
                    + getDescriptorIndex() + " : name=" + getNameIndex();
        }
    }

    /**
     *
     */
    public static class Utf8Info extends ConstantPoolInfo {
        private final String bytes;
        private final int hashCode;

        Utf8Info(DataInputStream in) throws IOException {
            bytes = in.readUTF();
            hashCode = (this.getClass().hashCode() * 31)
                    ^ (bytes.hashCode() * 37);
        }

        Utf8Info(String val) {
            bytes = val;
            hashCode = (this.getClass().hashCode() * 31)
                    ^ (bytes.hashCode() * 37);
        }

        /**
         *
         * @return
         */
        public int getTag() {
            return UTF8;
        }

        /**
         *
         * @return
         */
        public String getBytes() {
            return bytes;
        }

        /**
         *
         * @param out
         * @throws IOException
         */
        public void write(DataOutputStream out) throws IOException {
            out.writeByte(UTF8);
            out.writeUTF(bytes);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        public boolean equals(Object obj) {
            if (obj == this)
                return true;

            if (!(obj instanceof Utf8Info))
                return false;

            return getBytes().equals(((Utf8Info) obj).getBytes());
        }

        @Override
        public String toString() {
            return "CONSTANT_Utf8_info : value=" + getBytes();
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }
}
