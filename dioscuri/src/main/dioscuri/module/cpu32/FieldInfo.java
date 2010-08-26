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
public class FieldInfo {
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
    public static final int VOLATILE = 0x0040;
    public static final int TRANSIENT = 0x0080;
    public static final int SYNTHETIC = 0x1000;
    public static final int ENUM = 0x4000;

    /**
     *
     * @param in
     * @param pool
     * @throws IOException
     */
    public FieldInfo(DataInputStream in, ConstantPoolInfo[] pool)
            throws IOException {
        accessFlags = in.readUnsignedShort();
        nameIndex = in.readUnsignedShort();
        descriptorIndex = in.readUnsignedShort();

        attributesCount = in.readUnsignedShort();
        attributes = new AttributeInfo[attributesCount];
        for (int i = 0; i < attributesCount; i++)
            attributes[i] = AttributeInfo.construct(in, pool);
    }

    /**
     *
     * @param out
     * @throws IOException
     */
    public void write(DataOutputStream out) throws IOException {
        out.writeShort(accessFlags);
        out.writeShort(nameIndex);
        out.writeShort(descriptorIndex);

        out.writeShort(attributesCount);
        for (int i = 0; i < attributesCount; i++)
            attributes[i].write(out);
    }
}
