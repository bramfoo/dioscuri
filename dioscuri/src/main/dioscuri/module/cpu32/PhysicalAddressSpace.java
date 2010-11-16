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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

//import org.jpc.emulator.*;
//import org.jpc.emulator.memory.codeblock.*;
//import org.jpc.emulator.processor.Processor;

/**
 * @author Bram Lohman
 * @author Bart Kiers
 */
public final class PhysicalAddressSpace extends AddressSpace implements
        HardwareComponent {
    private static final int GATEA20_MASK = 0xffefffff;

    // Edit Bram: Added constant here and changed reference to local
    // private static final int QUICK_INDEX_SIZE = PC.SYS_RAM_SIZE >>>
    // INDEX_SHIFT;
    public static final int SYS_RAM_SIZE = 256 * 1024 * 1024;
    private static final int QUICK_INDEX_SIZE = SYS_RAM_SIZE >>> INDEX_SHIFT;

    private static final int TOP_INDEX_BITS = (32 - INDEX_SHIFT) / 2;
    private static final int BOTTOM_INDEX_BITS = 32 - INDEX_SHIFT
            - TOP_INDEX_BITS;

    private static final int TOP_INDEX_SHIFT = 32 - TOP_INDEX_BITS;
    private static final int TOP_INDEX_SIZE = 1 << TOP_INDEX_BITS;
    // private static final int TOP_INDEX_MASK = TOP_INDEX_SIZE - 1;

    private static final int BOTTOM_INDEX_SHIFT = 32 - TOP_INDEX_BITS
            - BOTTOM_INDEX_BITS;
    private static final int BOTTOM_INDEX_SIZE = 1 << BOTTOM_INDEX_BITS;
    private static final int BOTTOM_INDEX_MASK = BOTTOM_INDEX_SIZE - 1;

    private boolean gateA20MaskState;
    private int mappedRegionCount;

    private Memory[] quickNonA20MaskedIndex, quickA20MaskedIndex, quickIndex;
    private Memory[][] nonA20MaskedIndex, a20MaskedIndex, index;
    public static final Memory UNCONNECTED = new UnconnectedMemoryBlock();

    private LinearAddressSpace linearAddr;

    public PhysicalAddressSpace() {
        mappedRegionCount = 0;

        quickNonA20MaskedIndex = new Memory[QUICK_INDEX_SIZE];
        clearArray(quickNonA20MaskedIndex, UNCONNECTED);
        quickA20MaskedIndex = new Memory[QUICK_INDEX_SIZE];
        clearArray(quickA20MaskedIndex, UNCONNECTED);

        nonA20MaskedIndex = new Memory[TOP_INDEX_SIZE][];
        a20MaskedIndex = new Memory[TOP_INDEX_SIZE][];

        setGateA20State(false);
    }

    private void dumpMemory(DataOutput output, Memory[] mem) throws IOException {
        long len;
        byte[] temp = new byte[0];
        for (int i = 0; i < mem.length; i++) {
            len = mem[i].getSize();
            if (temp.length < (int) len)
                temp = new byte[(int) len];
            if (mem[i].isAllocated()) {
                try {
                    if (mem[i] instanceof MapWrapper) {
                        len = 0;
                    } else
                        mem[i].copyContentsInto(0, temp, 0, (int) len);
                } catch (IllegalStateException e) {
                    len = 0;
                }
                output.writeLong(len);
                if (len > 0)
                    output.write(temp);
            } else {
                output.writeLong(0);
            }
        }
    }

    private void dumpLotsOfMemory(DataOutput output, Memory[][] mem)
            throws IOException {
        output.writeInt(mem.length);
        for (int i = 0; i < mem.length; i++) {
            if (mem[i] == null)
                output.writeInt(0);
            else {
                dumpMemory(output, mem[i]);
            }
        }
    }

    /**
     * @param output
     * @throws IOException
     */
    public void dumpState(DataOutput output) throws IOException {
        output.writeBoolean(gateA20MaskState);
        output.writeInt(mappedRegionCount);

        output.writeInt(quickA20MaskedIndex.length);
        dumpMemory(output, quickA20MaskedIndex);
        output.writeInt(quickNonA20MaskedIndex.length);
        dumpMemory(output, quickNonA20MaskedIndex);
        if (quickIndex == quickNonA20MaskedIndex)
            output.writeInt(1);
        else
            output.writeInt(2);

        dumpLotsOfMemory(output, nonA20MaskedIndex);
        dumpLotsOfMemory(output, a20MaskedIndex);
        if (index == nonA20MaskedIndex)
            output.writeInt(1);
        else
            output.writeInt(2);
    }

    private void loadMemory(DataInput input, Memory[] mem, int size)
            throws IOException {
        long len;
        byte[] temp;
        for (int i = 0; i < size; i++) {
            len = input.readLong();
            temp = new byte[(int) len];
            if (len > 0) {
                input.readFully(temp, 0, (int) len);
                mem[i].copyContentsFrom(0, temp, 0, (int) len);
            }
        }
    }

    private void loadLotsOfMemory(DataInput input, Memory[][] mem)
            throws IOException {
        int width = input.readInt();
        int len = 0;
        // mem = new Memory[width][];
        for (int i = 0; i < width; i++) {
            loadMemory(input, mem[i], len);
        }
    }

    /**
     * @param input
     * @throws IOException
     */
    public void loadState(DataInput input) throws IOException {
        clearArray(quickA20MaskedIndex, UNCONNECTED);
        clearArray(quickNonA20MaskedIndex, UNCONNECTED);
        clearArray(quickIndex, UNCONNECTED);
        reset();
        for (int i = 0; i < SYS_RAM_SIZE; i += AddressSpace.BLOCK_SIZE)
            allocateMemory(i, new LazyMemory(AddressSpace.BLOCK_SIZE));
        gateA20MaskState = input.readBoolean();
        mappedRegionCount = input.readInt();

        int size = input.readInt();
        loadMemory(input, quickA20MaskedIndex, size);
        size = input.readInt();
        loadMemory(input, quickNonA20MaskedIndex, size);
        int which = input.readInt();
        if (which == 1)
            quickIndex = quickNonA20MaskedIndex;
        else
            quickIndex = quickA20MaskedIndex;

        loadLotsOfMemory(input, nonA20MaskedIndex);
        loadLotsOfMemory(input, a20MaskedIndex);
        which = input.readInt();
        if (which == 1)
            index = nonA20MaskedIndex;
        else
            index = a20MaskedIndex;
    }

    /**
     * @param value
     */
    public void setGateA20State(boolean value) {
        gateA20MaskState = value;
        if (value) {
            quickIndex = quickNonA20MaskedIndex;
            index = nonA20MaskedIndex;
        } else {
            quickIndex = quickA20MaskedIndex;
            index = a20MaskedIndex;
        }

        if ((linearAddr != null) && !linearAddr.pagingDisabled())
            linearAddr.flush();
    }

    /**
     * @return -
     */
    public boolean getGateA20State() {
        return gateA20MaskState;
    }

    /**
     * @return -
     */
    public int getAllocatedBufferSize() {
        return mappedRegionCount * BLOCK_SIZE;
    }

    /**
     * @param offset
     * @return -
     */
    public Memory getReadMemoryBlockAt(int offset) {
        return getMemoryBlockAt(offset);
    }

    /**
     * @param offset
     * @return -
     */
    public Memory getWriteMemoryBlockAt(int offset) {
        return getMemoryBlockAt(offset);
    }

    /**
     * @param cpu
     * @param offset
     * @return -
     */
    public int execute(Processor cpu, int offset) {
        return getReadMemoryBlockAt(offset).execute(cpu,
                offset & AddressSpace.BLOCK_MASK);
    }

    /**
     * @param cpu
     * @param offset
     * @return -
     */
    public CodeBlock decodeCodeBlockAt(Processor cpu, int offset) {
        CodeBlock block = getReadMemoryBlockAt(offset).decodeCodeBlockAt(cpu,
                offset & AddressSpace.BLOCK_MASK);
        return block;

    }

    void replaceBlocks(Memory oldBlock, Memory newBlock) {
        for (int i = 0; i < quickA20MaskedIndex.length; i++)
            if (quickA20MaskedIndex[i] == oldBlock)
                quickA20MaskedIndex[i] = newBlock;

        for (int i = 0; i < quickNonA20MaskedIndex.length; i++)
            if (quickNonA20MaskedIndex[i] == oldBlock)
                quickNonA20MaskedIndex[i] = newBlock;

        for (int i = 0; i < a20MaskedIndex.length; i++) {
            Memory[] subArray = a20MaskedIndex[i];
            try {
                for (int j = 0; j < subArray.length; j++)
                    if (subArray[j] == oldBlock)
                        subArray[j] = newBlock;
            } catch (NullPointerException e) {
            }
        }

        for (int i = 0; i < nonA20MaskedIndex.length; i++) {
            Memory[] subArray = nonA20MaskedIndex[i];
            try {
                for (int j = 0; j < subArray.length; j++)
                    if (subArray[j] == oldBlock)
                        subArray[j] = newBlock;
            } catch (NullPointerException e) {
            }
        }
    }

    public static class MapWrapper extends Memory {
        private Memory memory;
        private int baseAddress;

        MapWrapper(Memory mem, int base) {
            baseAddress = base;
            memory = mem;
        }

        /**
         * @return -
         */
        public long getSize() {
            return BLOCK_SIZE;
        }

        public void clear() {
            memory.clear(baseAddress, (int) getSize());
        }

        /**
         * @param start
         * @param length
         */
        public void clear(int start, int length) {
            if (start + length > getSize())
                throw new ArrayIndexOutOfBoundsException(
                        "Attempt to clear outside of memory bounds");
            start = baseAddress | start;
            memory.clear(start, length);
        }

        /**
         * @param offset
         * @param buffer
         * @param off
         * @param len
         */
        public void copyContentsInto(int offset, byte[] buffer, int off, int len) {
            offset = baseAddress | offset;
            memory.copyContentsInto(offset, buffer, off, len);
        }

        /**
         * @param offset
         * @param buffer
         * @param off
         * @param len
         */
        public void copyContentsFrom(int offset, byte[] buffer, int off, int len) {
            offset = baseAddress | offset;
            memory.copyContentsFrom(offset, buffer, off, len);
        }

        /**
         * @param offset
         * @return -
         */
        public byte getByte(int offset) {
            offset = baseAddress | offset;
            return memory.getByte(offset);
        }

        /**
         * @param offset
         * @return -
         */
        public short getWord(int offset) {
            offset = baseAddress | offset;
            return memory.getWord(offset);
        }

        /**
         * @param offset
         * @return -
         */
        public int getDoubleWord(int offset) {
            offset = baseAddress | offset;
            return memory.getDoubleWord(offset);
        }

        /**
         * @param offset
         * @return -
         */
        public long getQuadWord(int offset) {
            offset = baseAddress | offset;
            return memory.getQuadWord(offset);
        }

        /**
         * @param offset
         * @return -
         */
        public long getLowerDoubleQuadWord(int offset) {
            offset = baseAddress | offset;
            return memory.getQuadWord(offset);
        }

        /**
         * @param offset
         * @return -
         */
        public long getUpperDoubleQuadWord(int offset) {
            offset += 8;
            offset = baseAddress | offset;
            return memory.getQuadWord(offset);
        }

        /**
         * @param offset
         * @param data
         */
        public void setByte(int offset, byte data) {
            offset = baseAddress | offset;
            memory.setByte(offset, data);
        }

        /**
         * @param offset
         * @param data
         */
        public void setWord(int offset, short data) {
            offset = baseAddress | offset;
            memory.setWord(offset, data);
        }

        /**
         * @param offset
         * @param data
         */
        public void setDoubleWord(int offset, int data) {
            offset = baseAddress | offset;
            memory.setDoubleWord(offset, data);
        }

        /**
         * @param offset
         * @param data
         */
        public void setQuadWord(int offset, long data) {
            offset = baseAddress | offset;
            memory.setQuadWord(offset, data);
        }

        /**
         * @param offset
         * @param data
         */
        public void setLowerDoubleQuadWord(int offset, long data) {
            offset = baseAddress | offset;
            memory.setQuadWord(offset, data);
        }

        /**
         * @param offset
         * @param data
         */
        public void setUpperDoubleQuadWord(int offset, long data) {
            offset += 8;
            offset = baseAddress | offset;
            memory.setQuadWord(offset, data);
        }

        /**
         * @param cpu
         * @param offset
         * @return -
         */
        public int execute(Processor cpu, int offset) {
            offset = baseAddress | offset;
            return memory.execute(cpu, offset);
        }

        /**
         * @param cpu
         * @param offset
         * @return -
         */
        public CodeBlock decodeCodeBlockAt(Processor cpu, int offset) {
            offset = baseAddress | offset;
            CodeBlock block = memory.decodeCodeBlockAt(cpu, offset);
            if (block != null)
                System.out.println(getClass().getName() + ":1");
            else
                System.out.println(getClass().getName() + ":0");
            return block;
        }
    }

    public void clear() {
        for (int i = 0; i < quickNonA20MaskedIndex.length; i++)
            quickNonA20MaskedIndex[i].clear();

        for (int i = 0; i < nonA20MaskedIndex.length; i++) {
            Memory[] subArray = nonA20MaskedIndex[i];
            try {
                for (int j = 0; j < subArray.length; j++) {
                    try {
                        subArray[j].clear();
                    } catch (NullPointerException e) {
                    }
                }
            } catch (NullPointerException e) {
            }
        }
    }

    /**
     * @param start
     * @param length
     */
    public void unmap(int start, int length) {
        if ((start % BLOCK_SIZE) != 0)
            throw new IllegalStateException(
                    "Cannot deallocate memory starting at "
                            + Integer.toHexString(start)
                            + "; this is not block aligned at " + BLOCK_SIZE
                            + " boundaries");
        if ((length % BLOCK_SIZE) != 0)
            throw new IllegalStateException(
                    "Cannot deallocate memory in partial blocks. " + length
                            + " is not a multiple of " + BLOCK_SIZE);

        for (int i = start; i < start + length; i += BLOCK_SIZE) {
            if (getMemoryBlockAt(i) != UNCONNECTED)
                mappedRegionCount--;
            setMemoryBlockAt(i, UNCONNECTED);
        }
    }

    /**
     * @param underlying
     * @param start
     * @param length
     */
    public void mapMemoryRegion(Memory underlying, int start, int length) {
        if (underlying.getSize() < length)
            throw new IllegalStateException("Underlying memory (length="
                    + underlying.getSize()
                    + ") is too short for mapping into region " + length
                    + " bytes long");
        if ((start % BLOCK_SIZE) != 0)
            throw new IllegalStateException("Cannot map memory starting at "
                    + Integer.toHexString(start) + "; this is not aligned to "
                    + BLOCK_SIZE + " blocks");
        if ((length % BLOCK_SIZE) != 0)
            throw new IllegalStateException(
                    "Cannot map memory in partial blocks: " + length
                            + " is not a multiple of " + BLOCK_SIZE);

        unmap(start, length);

        long s = 0xFFFFFFFFl & start;
        for (long i = s; i < s + length; i += BLOCK_SIZE) {
            Memory w = new MapWrapper(underlying, (int) (i - s));
            setMemoryBlockAt((int) i, w);
            mappedRegionCount++;
        }
    }

    /**
     * @param start
     * @param block
     */
    public void allocateMemory(int start, Memory block) {
        if ((start % BLOCK_SIZE) != 0)
            throw new IllegalStateException(
                    "Cannot allocate memory starting at "
                            + Integer.toHexString(start)
                            + "; this is not aligned to " + BLOCK_SIZE
                            + " blocks");
        if (block.getSize() != BLOCK_SIZE)
            throw new IllegalStateException(
                    "Can only allocate memory in blocks of " + BLOCK_SIZE);

        unmap(start, BLOCK_SIZE);

        long s = 0xFFFFFFFFl & start;
        setMemoryBlockAt((int) s, block);
        mappedRegionCount++;
    }

    public static final class UnconnectedMemoryBlock extends Memory {
        public void clear() {
        }

        /**
         * @param start
         * @param length
         */
        public void clear(int start, int length) {
        }

        /**
         * @param address
         * @param buffer
         * @param off
         * @param len
         */
        public void copyContentsInto(int address, byte[] buffer, int off,
                                     int len) {
        }

        /**
         * @param address
         * @param buffer
         * @param off
         * @param len
         */
        public void copyContentsFrom(int address, byte[] buffer, int off,
                                     int len) {
            len = Math.min(BLOCK_SIZE - address, Math.min(buffer.length - off,
                    len));
            for (int i = off; i < len; i++)
                buffer[i] = getByte(0);
        }

        /**
         * @return -
         */
        public long getSize() {
            return BLOCK_SIZE;
        }

        /**
         * @param offset
         * @return -
         */
        public byte getByte(int offset) {
            return (byte) 0xFF;
        }

        /**
         * @param offset
         * @return -
         */
        public short getWord(int offset) {
            return (short) 0xFFFF;
        }

        /**
         * @param offset
         * @return -
         */
        public int getDoubleWord(int offset) {
            return 0xFFFFFFFF;
        }

        /**
         * @param offset
         * @return -
         */
        public long getQuadWord(int offset) {
            return -1l;
        }

        /**
         * @param offset
         * @return -
         */
        public long getLowerDoubleQuadWord(int offset) {
            return -1l;
        }

        /**
         * @param offset
         * @return -
         */
        public long getUpperDoubleQuadWord(int offset) {
            return -1l;
        }

        /**
         * @param offset
         * @param data
         */
        public void setByte(int offset, byte data) {
        }

        /**
         * @param offset
         * @param data
         */
        public void setWord(int offset, short data) {
        }

        /**
         * @param offset
         * @param data
         */
        public void setDoubleWord(int offset, int data) {
        }

        /**
         * @param offset
         * @param data
         */
        public void setQuadWord(int offset, long data) {
        }

        /**
         * @param offset
         * @param data
         */
        public void setLowerDoubleQuadWord(int offset, long data) {
        }

        /**
         * @param offset
         * @param data
         */
        public void setUpperDoubleQuadWord(int offset, long data) {
        }

        /**
         * @param cpu
         * @param offset
         * @return -
         */
        public int execute(Processor cpu, int offset) {
            throw new IllegalStateException(
                    "Trying to execute in Unconnected Block @ 0x"
                            + Integer.toHexString(offset));
        }

        /**
         * @param cpu
         * @param offset
         * @return -
         */
        public CodeBlock decodeCodeBlockAt(Processor cpu, int offset) {
            throw new IllegalStateException(
                    "Trying to execute in Unconnected Block @ 0x"
                            + Integer.toHexString(offset));
        }
    }

    /**
     * @return -
     */
    public boolean reset() {
        clear();
        setGateA20State(false);
        linearAddr = null;

        return true;
    }

    /**
     * @return -
     */
    public boolean updated() {
        return true;
    }

    /**
     * @param component
     */
    public void updateComponent(HardwareComponent component) {
    }

    /**
     * @return -
     */
    public boolean initialised() {
        return (linearAddr != null);
    }

    /**
     * @param component
     */
    public void acceptComponent(HardwareComponent component) {
        if (component instanceof LinearAddressSpace)
            linearAddr = (LinearAddressSpace) component;
    }

    @Override
    public String toString() {
        return "Physical Address Bus";
    }

    private Memory getMemoryBlockAt(int i) {
        try {
            return quickIndex[i >>> INDEX_SHIFT];
        } catch (ArrayIndexOutOfBoundsException e) {
            try {
                return index[i >>> TOP_INDEX_SHIFT][(i >>> BOTTOM_INDEX_SHIFT)
                        & BOTTOM_INDEX_MASK];
            } catch (NullPointerException n) {
                return UNCONNECTED;
            }
        }
    }

    private void setMemoryBlockAt(int i, Memory b) {
        try {
            int idx = i >>> INDEX_SHIFT;
            quickNonA20MaskedIndex[idx] = b;
            if ((idx & (GATEA20_MASK >>> INDEX_SHIFT)) == idx) {
                quickA20MaskedIndex[idx] = b;
                quickA20MaskedIndex[idx | ((~GATEA20_MASK) >>> INDEX_SHIFT)] = b;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            try {
                nonA20MaskedIndex[i >>> TOP_INDEX_SHIFT][(i >>> BOTTOM_INDEX_SHIFT)
                        & BOTTOM_INDEX_MASK] = b;
            } catch (NullPointerException n) {
                nonA20MaskedIndex[i >>> TOP_INDEX_SHIFT] = new Memory[BOTTOM_INDEX_SIZE];
                nonA20MaskedIndex[i >>> TOP_INDEX_SHIFT][(i >>> BOTTOM_INDEX_SHIFT)
                        & BOTTOM_INDEX_MASK] = b;
            }

            if ((i & GATEA20_MASK) == i) {
                try {
                    a20MaskedIndex[i >>> TOP_INDEX_SHIFT][(i >>> BOTTOM_INDEX_SHIFT)
                            & BOTTOM_INDEX_MASK] = b;
                } catch (NullPointerException n) {
                    a20MaskedIndex[i >>> TOP_INDEX_SHIFT] = new Memory[BOTTOM_INDEX_SIZE];
                    a20MaskedIndex[i >>> TOP_INDEX_SHIFT][(i >>> BOTTOM_INDEX_SHIFT)
                            & BOTTOM_INDEX_MASK] = b;
                }

                int modi = i | ~GATEA20_MASK;
                try {
                    a20MaskedIndex[modi >>> TOP_INDEX_SHIFT][(modi >>> BOTTOM_INDEX_SHIFT)
                            & BOTTOM_INDEX_MASK] = b;
                } catch (NullPointerException n) {
                    a20MaskedIndex[modi >>> TOP_INDEX_SHIFT] = new Memory[BOTTOM_INDEX_SIZE];
                    a20MaskedIndex[modi >>> TOP_INDEX_SHIFT][(modi >>> BOTTOM_INDEX_SHIFT)
                            & BOTTOM_INDEX_MASK] = b;
                }
            }
        }
    }

    public void timerCallback() {
    }
}
