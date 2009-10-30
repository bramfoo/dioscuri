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

public abstract class AbstractMemory extends Memory {
    public abstract long getSize();

    public abstract byte getByte(int offset);

    public abstract void setByte(int offset, byte data);

    public void clear() {
        for (int i = 0; i < getSize(); i++)
            setByte(i, (byte) 0);
    }

    public void clear(int start, int length) {
        int limit = start + length;
        if (limit > getSize())
            throw new ArrayIndexOutOfBoundsException(
                    "Attempt to clear outside of memory bounds");
        for (int i = start; i < limit; i++)
            setByte(i, (byte) 0);
    }

    public void copyContentsInto(int address, byte[] buffer, int off, int len) {
        for (int i = off; i < off + len; i++, address++)
            buffer[i] = getByte(address);
    }

    public void copyContentsFrom(int address, byte[] buffer, int off, int len) {
        for (int i = off; i < off + len; i++, address++)
            setByte(address, buffer[i]);
    }

    protected final short getWordInBytes(int offset) {
        int result = 0xFF & getByte(offset + 1);
        result <<= 8;
        result |= (0xFF & getByte(offset));
        return (short) result;
    }

    protected final int getDoubleWordInBytes(int offset) {
        int result = 0xFFFF & getWordInBytes(offset + 2);
        result <<= 16;
        result |= (0xFFFF & getWordInBytes(offset));
        return result;
    }

    protected final long getQuadWordInBytes(int offset) {
        long result = 0xFFFFFFFFl & getDoubleWordInBytes(offset + 4);
        result <<= 32;
        result |= (0xFFFFFFFFl & getDoubleWordInBytes(offset));
        return result;
    }

    public short getWord(int offset) {
        return getWordInBytes(offset);
    }

    public int getDoubleWord(int offset) {
        return getDoubleWordInBytes(offset);
    }

    public long getQuadWord(int offset) {
        return getQuadWordInBytes(offset);
    }

    public long getLowerDoubleQuadWord(int offset) {
        return getQuadWordInBytes(offset);
    }

    public long getUpperDoubleQuadWord(int offset) {
        return getQuadWordInBytes(offset + 8);
    }

    protected final void setWordInBytes(int offset, short data) {
        setByte(offset, (byte) data);
        offset++;
        setByte(offset, (byte) (data >> 8));
    }

    protected final void setDoubleWordInBytes(int offset, int data) {
        setByte(offset, (byte) data);
        offset++;
        data >>= 8;
        setByte(offset, (byte) data);
        offset++;
        data >>= 8;
        setByte(offset, (byte) data);
        offset++;
        data >>= 8;
        setByte(offset, (byte) data);
    }

    protected final void setQuadWordInBytes(int offset, long data) {
        setDoubleWordInBytes(offset, (int) data);
        setDoubleWordInBytes(offset + 4, (int) (data >> 32));
    }

    public void setWord(int offset, short data) {
        setWordInBytes(offset, data);
    }

    public void setDoubleWord(int offset, int data) {
        setDoubleWordInBytes(offset, data);
    }

    public void setQuadWord(int offset, long data) {
        setQuadWordInBytes(offset, data);
    }

    public void setLowerDoubleQuadWord(int offset, long data) {
        setQuadWordInBytes(offset, data);
    }

    public void setUpperDoubleQuadWord(int offset, long data) {
        setQuadWordInBytes(offset + 8, data);
    }

    public static final short getWord(int offset, byte[] src) {
        return (short) ((0xFF & src[offset]) | (0xFF00 & (src[offset + 1] << 8)));
    }

    public static final int getDoubleWord(int offset, byte[] src) {
        return (0xFFFF & getWord(offset, src))
                | (0xFFFF0000 & (getWord(offset + 2, src) << 16));
    }

    public static final void clearArray(Object[] target, Object value) {
        if (target == null)
            return;

        for (int i = 0; i < target.length; i++)
            target[i] = value;
    }

    public static final void clearArray(byte[] target, byte value) {
        if (target == null)
            return;

        for (int i = 0; i < target.length; i++)
            target[i] = value;
    }
}
