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

//import org.jpc.emulator.*;
//import org.jpc.emulator.memory.codeblock.*;

/**
 * @author Bram Lohman
 * @author Bart Kiers
 */
public abstract class AddressSpace extends AbstractMemory {
    public static final int BLOCK_SIZE = 4 * 1024;
    public static final int BLOCK_MASK = BLOCK_SIZE - 1;
    public static final int INDEX_MASK = ~(BLOCK_MASK);
    public static final int INDEX_SHIFT = 12;
    public static final int INDEX_SIZE = 1 << (32 - INDEX_SHIFT);

    public AddressSpace()
    {
    }

    /**
     * @return -
     */
    public final long getSize()
    {
        return 0x100000000l;
    }

    /**
     * @param address
     * @return -
     */
    public final int getBlockStart(int address)
    {
        return address & INDEX_MASK;
    }

    /**
     * @param address
     * @return -
     */
    public final int getBlockEnd(int address)
    {
        return (address & INDEX_MASK) + BLOCK_SIZE;
    }

    /**
     * @param offset
     * @return -
     */
    public abstract Memory getReadMemoryBlockAt(int offset);

    /**
     * @param offset
     * @return -
     */
    public abstract Memory getWriteMemoryBlockAt(int offset);

    @Override
    public abstract void clear();

    /**
     * @return -
     */
    public abstract boolean updated();

    /**
     * @param offset
     * @return -
     */
    public byte getByte(int offset)
    {
        return getReadMemoryBlockAt(offset).getByte(offset & BLOCK_MASK);
    }

    /**
     * @param offset
     * @param data
     */
    public void setByte(int offset, byte data)
    {
        getWriteMemoryBlockAt(offset).setByte(offset & BLOCK_MASK, data);
    }

    /**
     * @param offset
     * @return -
     */
    @Override
    public short getWord(int offset)
    {
        try {
            return getReadMemoryBlockAt(offset).getWord(offset & BLOCK_MASK);
        } catch (ArrayIndexOutOfBoundsException e) {
            return super.getWord(offset);
        }
    }

    /**
     * @param offset
     * @return -
     */
    @Override
    public int getDoubleWord(int offset)
    {
        try {
            return getReadMemoryBlockAt(offset).getDoubleWord(
                    offset & BLOCK_MASK);
        } catch (ArrayIndexOutOfBoundsException e) {
            return super.getDoubleWord(offset);
        }
    }

    /**
     * @param offset
     * @return -
     */
    @Override
    public long getQuadWord(int offset)
    {
        try {
            return getReadMemoryBlockAt(offset)
                    .getQuadWord(offset & BLOCK_MASK);
        } catch (ArrayIndexOutOfBoundsException e) {
            return super.getQuadWord(offset);
        }
    }

    /**
     * @param offset
     * @return -
     */
    @Override
    public long getLowerDoubleQuadWord(int offset)
    {
        try {
            return getReadMemoryBlockAt(offset).getLowerDoubleQuadWord(
                    offset & BLOCK_MASK);
        } catch (ArrayIndexOutOfBoundsException e) {
            return super.getLowerDoubleQuadWord(offset);
        }
    }

    /**
     * @param offset
     * @return -
     */
    @Override
    public long getUpperDoubleQuadWord(int offset)
    {
        try {
            return getReadMemoryBlockAt(offset).getUpperDoubleQuadWord(
                    offset & BLOCK_MASK);
        } catch (ArrayIndexOutOfBoundsException e) {
            return super.getUpperDoubleQuadWord(offset);
        }
    }

    /**
     * @param offset
     * @param data
     */
    @Override
    public void setWord(int offset, short data)
    {
        try {
            getWriteMemoryBlockAt(offset).setWord(offset & BLOCK_MASK, data);
        } catch (ArrayIndexOutOfBoundsException e) {
            super.setWord(offset, data);
        }
    }

    /**
     * @param offset
     * @param data
     */
    @Override
    public void setDoubleWord(int offset, int data)
    {
        try {
            getWriteMemoryBlockAt(offset).setDoubleWord(offset & BLOCK_MASK,
                    data);
        } catch (ArrayIndexOutOfBoundsException e) {
            super.setDoubleWord(offset, data);
        }
    }

    /**
     * @param offset
     * @param data
     */
    @Override
    public void setQuadWord(int offset, long data)
    {
        try {
            getWriteMemoryBlockAt(offset)
                    .setQuadWord(offset & BLOCK_MASK, data);
        } catch (ArrayIndexOutOfBoundsException e) {
            super.setQuadWord(offset, data);
        }
    }

    /**
     * @param offset
     * @param data
     */
    @Override
    public void setLowerDoubleQuadWord(int offset, long data)
    {
        try {
            getWriteMemoryBlockAt(offset).setLowerDoubleQuadWord(
                    offset & BLOCK_MASK, data);
        } catch (ArrayIndexOutOfBoundsException e) {
            super.setLowerDoubleQuadWord(offset, data);
        }
    }

    /**
     * @param offset
     * @param data
     */
    @Override
    public void setUpperDoubleQuadWord(int offset, long data)
    {
        try {
            getWriteMemoryBlockAt(offset).setUpperDoubleQuadWord(
                    offset & BLOCK_MASK, data);
        } catch (ArrayIndexOutOfBoundsException e) {
            super.setUpperDoubleQuadWord(offset, data);
        }
    }

    /**
     * @param address
     * @param buffer
     * @param off
     * @param len
     */
    @Override
    public void copyContentsFrom(int address, byte[] buffer, int off, int len)
    {
        do {
            int partialLength = Math.min(BLOCK_SIZE - (address & BLOCK_MASK),
                    len);
            getWriteMemoryBlockAt(address).copyContentsFrom(
                    address & BLOCK_MASK, buffer, off, partialLength);
            address += partialLength;
            off += partialLength;
            len -= partialLength;
        } while (len > 0);
    }

    /**
     * @param address
     * @param buffer
     * @param off
     * @param len
     */
    @Override
    public void copyContentsInto(int address, byte[] buffer, int off, int len)
    {
        do {
            int partialLength = Math.min(BLOCK_SIZE - (address & BLOCK_MASK),
                    len);
            getReadMemoryBlockAt(address).copyContentsInto(
                    address & BLOCK_MASK, buffer, off, partialLength);
            address += partialLength;
            off += partialLength;
            len -= partialLength;
        } while (len > 0);
    }

    abstract void replaceBlocks(Memory oldBlock, Memory newBlock);
}
