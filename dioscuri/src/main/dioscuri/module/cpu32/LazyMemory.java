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

import dioscuri.module.clock.Clock;

//import org.jpc.emulator.processor.Processor;
//import org.jpc.emulator.memory.codeblock.CodeBlock;

/**
 * @author Bram Lohman
 * @author Bart Kiers
 */
public class LazyMemory extends AbstractMemory {
    private int size;
    boolean allocated = false;
    private byte[] buffer;

    private Clock clock;

    // Needed for LazyCBMemory

    /**
     * @param size
     */
    public LazyMemory(int size)
    {
        this.size = size;
        buffer = null;
    }

    /**
     * @param size
     * @param clk
     */
    public LazyMemory(int size, Clock clk)
    {
        this.size = size;
        buffer = null;
        this.clock = clk;
    }

    /**
     * @param data
     */
    public LazyMemory(byte[] data)
    {
        this.size = data.length;
        buffer = data;
    }

    /**
     * @return -
     */
    public boolean isCacheable()
    {
        return true;
    }

    private final void allocateBuffer()
    {
        if (buffer == null) {
            buffer = new byte[size];
            allocated = true;
        }
    }

    /**
     * @param address
     * @param buf
     * @param off
     * @param len
     */
    @Override
    public void copyContentsInto(int address, byte[] buf, int off, int len)
    {
        try {
            System.arraycopy(buffer, address, buf, off, len);
        } catch (NullPointerException e) {
            allocateBuffer();
            System.arraycopy(buffer, address, buf, off, len);
        }
    }

    /**
     * @param address
     * @param buf
     * @param off
     * @param len
     */
    @Override
    public void copyContentsFrom(int address, byte[] buf, int off, int len)
    {
        try {
            System.arraycopy(buf, off, buffer, address, len);
        } catch (NullPointerException e) {
            allocateBuffer();
            System.arraycopy(buf, off, buffer, address, len);
        }
    }

    /**
     * @return -
     */
    public long getSize()
    {
        return size;
    }

    /**
     * @return -
     */
    @Override
    public boolean isAllocated()
    {
        return allocated;
    }

    /**
     * @param offset
     * @return -
     */
    public byte getByte(int offset)
    {
        try {
            return buffer[offset];
        } catch (NullPointerException e) {
            allocateBuffer();
            return buffer[offset];
        }
    }

    /**
     * @param offset
     * @param data
     */
    public void setByte(int offset, byte data)
    {
        try {
            buffer[offset] = data;
        } catch (NullPointerException e) {
            allocateBuffer();
            buffer[offset] = data;
        }
    }

    /**
     * @param offset
     * @return -
     */
    @Override
    public short getWord(int offset)
    {
        try {
            int result = 0xFF & buffer[offset];
            offset++;
            result |= buffer[offset] << 8;
            return (short) result;
        } catch (NullPointerException e) {
            allocateBuffer();
            int result = 0xFF & buffer[offset];
            offset++;
            result |= buffer[offset] << 8;
            return (short) result;
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
            int result = 0xFF & buffer[offset];
            offset++;
            result |= (0xFF & buffer[offset]) << 8;
            offset++;
            result |= (0xFF & buffer[offset]) << 16;
            offset++;
            result |= (buffer[offset]) << 24;
            return result;
        } catch (NullPointerException e) {
            allocateBuffer();
            int result = 0xFF & buffer[offset];
            offset++;
            result |= (0xFF & buffer[offset]) << 8;
            offset++;
            result |= (0xFF & buffer[offset]) << 16;
            offset++;
            result |= (buffer[offset]) << 24;
            return result;
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
            buffer[offset] = (byte) data;
            offset++;
            buffer[offset] = (byte) (data >> 8);
        } catch (NullPointerException e) {
            allocateBuffer();
            buffer[offset] = (byte) data;
            offset++;
            buffer[offset] = (byte) (data >> 8);
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
            buffer[offset] = (byte) data;
            offset++;
            data >>= 8;
            buffer[offset] = (byte) (data);
            offset++;
            data >>= 8;
            buffer[offset] = (byte) (data);
            offset++;
            data >>= 8;
            buffer[offset] = (byte) (data);
        } catch (NullPointerException e) {
            allocateBuffer();
            buffer[offset] = (byte) data;
            offset++;
            data >>= 8;
            buffer[offset] = (byte) (data);
            offset++;
            data >>= 8;
            buffer[offset] = (byte) (data);
            offset++;
            data >>= 8;
            buffer[offset] = (byte) (data);
        }
    }

    @Override
    public void clear()
    {
        buffer = null;
    }

    /**
     * @param cpu
     * @param offset
     * @return -
     */
    public int execute(Processor cpu, int offset)
    {
        return convertMemory(cpu).execute(cpu, offset);
    }

    /**
     * @param cpu
     * @param offset
     * @return -
     */
    public CodeBlock decodeCodeBlockAt(Processor cpu, int offset)
    {
        CodeBlock block = convertMemory(cpu).decodeCodeBlockAt(cpu, offset);
        return block;
    }

    private LazyCodeBlockMemory convertMemory(Processor cpu)
    {
        LazyCodeBlockMemory newMemory = new LazyCodeBlockMemory(this, clock);
        cpu.physicalMemory.replaceBlocks(this, newMemory);
        cpu.linearMemory.replaceBlocks(this, newMemory);
        return newMemory;
    }

    @Override
    public String toString()
    {
        return "LazyMemory[" + getSize() + "] {Allocated=" + (buffer != null)
                + "}";
    }
}
