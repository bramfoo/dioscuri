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

/**
 *
 * @author Bram Lohman
 * @author Bart Kiers
 */
public abstract class AbstractMemory extends Memory {
    /**
     *
     * @return
     */
    public abstract long getSize();

    /**
     *
     * @param offset
     * @return
     */
    public abstract byte getByte(int offset);

    /**
     *
     * @param offset
     * @param data
     */
    public abstract void setByte(int offset, byte data);

    /**
     *
     */
    public void clear() {
        for (int i = 0; i < getSize(); i++)
            setByte(i, (byte) 0);
    }

    /**
     *
     * @param start
     * @param length
     */
    public void clear(int start, int length) {
        int limit = start + length;
        if (limit > getSize())
            throw new ArrayIndexOutOfBoundsException(
                    "Attempt to clear outside of memory bounds");
        for (int i = start; i < limit; i++)
            setByte(i, (byte) 0);
    }

    /**
     *
     * @param address
     * @param buffer
     * @param off
     * @param len
     */
    public void copyContentsInto(int address, byte[] buffer, int off, int len) {
        for (int i = off; i < off + len; i++, address++)
            buffer[i] = getByte(address);
    }

    /**
     *
     * @param address
     * @param buffer
     * @param off
     * @param len
     */
    public void copyContentsFrom(int address, byte[] buffer, int off, int len) {
        for (int i = off; i < off + len; i++, address++)
            setByte(address, buffer[i]);
    }

    /**
     *
     * @param offset
     * @return
     */
    protected final short getWordInBytes(int offset) {
        int result = 0xFF & getByte(offset + 1);
        result <<= 8;
        result |= (0xFF & getByte(offset));
        return (short) result;
    }

    /**
     *
     * @param offset
     * @return
     */
    protected final int getDoubleWordInBytes(int offset) {
        int result = 0xFFFF & getWordInBytes(offset + 2);
        result <<= 16;
        result |= (0xFFFF & getWordInBytes(offset));
        return result;
    }

    /**
     *
     * @param offset
     * @return
     */
    protected final long getQuadWordInBytes(int offset) {
        long result = 0xFFFFFFFFl & getDoubleWordInBytes(offset + 4);
        result <<= 32;
        result |= (0xFFFFFFFFl & getDoubleWordInBytes(offset));
        return result;
    }

    /**
     *
     * @param offset
     * @return
     */
    public short getWord(int offset) {
        return getWordInBytes(offset);
    }

    /**
     *
     * @param offset
     * @return
     */
    public int getDoubleWord(int offset) {
        return getDoubleWordInBytes(offset);
    }

    /**
     *
     * @param offset
     * @return
     */
    public long getQuadWord(int offset) {
        return getQuadWordInBytes(offset);
    }

    /**
     *
     * @param offset
     * @return
     */
    public long getLowerDoubleQuadWord(int offset) {
        return getQuadWordInBytes(offset);
    }

    /**
     *
     * @param offset
     * @return
     */
    public long getUpperDoubleQuadWord(int offset) {
        return getQuadWordInBytes(offset + 8);
    }

    /**
     *
     * @param offset
     * @param data
     */
    protected final void setWordInBytes(int offset, short data) {
        setByte(offset, (byte) data);
        offset++;
        setByte(offset, (byte) (data >> 8));
    }

    /**
     *
     * @param offset
     * @param data
     */
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

    /**
     *
     * @param offset
     * @param data
     */
    protected final void setQuadWordInBytes(int offset, long data) {
        setDoubleWordInBytes(offset, (int) data);
        setDoubleWordInBytes(offset + 4, (int) (data >> 32));
    }

    /**
     * 
     * @param offset
     * @param data
     */
    public void setWord(int offset, short data) {
        setWordInBytes(offset, data);
    }

    /**
     *
     * @param offset
     * @param data
     */
    public void setDoubleWord(int offset, int data) {
        setDoubleWordInBytes(offset, data);
    }

    /**
     *
     * @param offset
     * @param data
     */
    public void setQuadWord(int offset, long data) {
        setQuadWordInBytes(offset, data);
    }

    /**
     *
     * @param offset
     * @param data
     */
    public void setLowerDoubleQuadWord(int offset, long data) {
        setQuadWordInBytes(offset, data);
    }

    /**
     *
     * @param offset
     * @param data
     */
    public void setUpperDoubleQuadWord(int offset, long data) {
        setQuadWordInBytes(offset + 8, data);
    }

    /**
     *
     * @param offset
     * @param src
     * @return
     */
    public static final short getWord(int offset, byte[] src) {
        return (short) ((0xFF & src[offset]) | (0xFF00 & (src[offset + 1] << 8)));
    }

    /**
     *
     * @param offset
     * @param src
     * @return
     */
    public static final int getDoubleWord(int offset, byte[] src) {
        return (0xFFFF & getWord(offset, src))
                | (0xFFFF0000 & (getWord(offset + 2, src) << 16));
    }

    /**
     *
     * @param target
     * @param value
     */
    public static final void clearArray(Object[] target, Object value) {
        if (target == null)
            return;

        for (int i = 0; i < target.length; i++)
            target[i] = value;
    }

    /**
     *
     * @param target
     * @param value
     */
    public static final void clearArray(byte[] target, byte value) {
        if (target == null)
            return;

        for (int i = 0; i < target.length; i++)
            target[i] = value;
    }
}
