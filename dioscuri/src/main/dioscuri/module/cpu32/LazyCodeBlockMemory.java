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

//import org.jpc.emulator.memory.codeblock.*;
//import org.jpc.emulator.processor.Processor;
//import org.jpc.emulator.processor.ProcessorException;

/**
 *
 * @author Bram Lohman
 * @author Bart Kiers
 */
public class LazyCodeBlockMemory extends LazyMemory {
    private static CodeBlockManager codeBlockManager;

    private static final BlankCodeBlock PLACEHOLDER = new BlankCodeBlock(0, 0);
    protected RealModeCodeBlock[] realCodeBuffer;
    protected ProtectedModeCodeBlock[] protectedCodeBuffer;
    protected Virtual8086ModeCodeBlock[] virtual8086CodeBuffer;


    private Clock clock;

    /**
     *
     * @param src
     * @param clk
     */
    public LazyCodeBlockMemory(Memory src, Clock clk) {
        super((int) src.getSize());
        if (src.getSize() > 1024 * 1024 * 32)
            throw new IllegalStateException("Cannot create code block of size "
                    + src.getSize());
        constructCodeBlocksArray();

        byte[] temp = new byte[(int) src.getSize()];
        src.copyContentsInto(0, temp, 0, temp.length);
        copyContentsFrom(0, temp, 0, temp.length);

        if (codeBlockManager == null)
            codeBlockManager = new CodeBlockManager(clk);

        this.clock = clk;
    }

    /**
     *
     * @param buf
     * @param clk
     */
    public LazyCodeBlockMemory(byte[] buf, Clock clk) {
        super(buf);
        constructCodeBlocksArray();

        if (codeBlockManager == null)
            codeBlockManager = new CodeBlockManager(clk);
    }

    /**
     *
     * @param size
     * @param clk
     */
    public LazyCodeBlockMemory(int size, Clock clk) {
        super(size);
        constructCodeBlocksArray();

        if (codeBlockManager == null)
            codeBlockManager = new CodeBlockManager(clk);
    }
    protected void constructCodeBlocksArray() {
        realCodeBuffer = new RealModeCodeBlock[(int) getSize()];
        protectedCodeBuffer = new ProtectedModeCodeBlock[(int) getSize()];
        virtual8086CodeBuffer = new Virtual8086ModeCodeBlock[(int) getSize()];
    }
    public void relinquishCache() {
    }

    /**
     *
     * @param cpu
     * @param offset
     * @return -
     */
    @Override
    public int execute(Processor cpu, int offset) {
        if (cpu.isProtectedMode())
            if (cpu.isVirtual8086Mode())
                return executeVirtual8086(cpu, offset);
            else
                return executeProtected(cpu, offset);
        else
            return executeReal(cpu, offset);
    }

    /**
     *
     * @param cpu
     * @param offset
     * @return -
     */
    @Override
    public CodeBlock decodeCodeBlockAt(Processor cpu, int offset) {
        if (cpu.isProtectedMode())
            if (cpu.isVirtual8086Mode())
                return decodeVirtual8086(cpu, offset);
            else
                return decodeProtected(cpu, offset);
        else
            return decodeReal(cpu, offset);
    }

    private int executeProtected(Processor cpu, int offset) {
        int x86Count = 0;
        int ip = cpu.getInstructionPointer();
        int startingBlock = ip & AddressSpace.INDEX_MASK;

        do {
            try {
                offset = ip & AddressSpace.BLOCK_MASK;
                ProtectedModeCodeBlock block = getProtectedModeCodeBlockAt(offset);
                try {
                    try {
                        x86Count += block.execute(cpu);
                    } catch (NullPointerException e) {
                        block = codeBlockManager.getProtectedModeCodeBlockAt(
                                this, offset, cpu.cs.getDefaultSizeFlag());
                        x86Count += block.execute(cpu);
                    }
                } catch (CodeBlockReplacementException e) {
                    block = (ProtectedModeCodeBlock) e.getReplacement();
                    setProtectedCodeBlockAt(offset, block);
                    x86Count += block.execute(cpu);
                }
                cpu.processProtectedModeInterrupts();
            } catch (ProcessorException p) {
                cpu.handleProtectedModeException(p.getVector(), p
                        .hasErrorCode(), p.getErrorCode());
            }
        } while (((ip = cpu.getInstructionPointer()) & AddressSpace.INDEX_MASK) == startingBlock);

        return x86Count;
    }

    private CodeBlock decodeProtected(Processor cpu, int offset) {
        ProtectedModeCodeBlock block = getProtectedModeCodeBlockAt(offset);
        try {
            try {
                block.getX86Length();
            } catch (NullPointerException e) {
                block = codeBlockManager.getProtectedModeCodeBlockAt(this,
                        offset, cpu.cs.getDefaultSizeFlag());
                block.getX86Length();
            }
        } catch (CodeBlockReplacementException e) {
            block = (ProtectedModeCodeBlock) e.getReplacement();
            setProtectedCodeBlockAt(offset, block);
        }
        return block;
    }

    private int executeReal(Processor cpu, int offset) {
        int x86Count = 0;
        int ip = cpu.getInstructionPointer();
        int startingBlock = ip & AddressSpace.INDEX_MASK;

        do {
            try {
                offset = ip & AddressSpace.BLOCK_MASK;
                RealModeCodeBlock block = getRealModeCodeBlockAt(offset);
                try {
                    try {
                        x86Count += block.execute(cpu);
                    } catch (NullPointerException e) {
                        block = codeBlockManager.getRealModeCodeBlockAt(this,
                                offset);
                        x86Count += block.execute(cpu);
                    }
                } catch (CodeBlockReplacementException e) {
                    block = (RealModeCodeBlock) e.getReplacement();
                    setRealCodeBlockAt(offset, block);
                    x86Count += block.execute(cpu);
                }
                cpu.processRealModeInterrupts();
            } catch (ProcessorException p) {
                cpu.handleRealModeException(p.getVector());
            }
        } while (((ip = cpu.getInstructionPointer()) & AddressSpace.INDEX_MASK) == startingBlock);

        return x86Count;
    }

    private CodeBlock decodeReal(Processor cpu, int offset) {
        RealModeCodeBlock block = getRealModeCodeBlockAt(offset);
        try {
            try {
                block.getX86Length();
            } catch (NullPointerException e) {
                block = codeBlockManager.getRealModeCodeBlockAt(this, offset);
            }
        } catch (CodeBlockReplacementException e) {
            block = (RealModeCodeBlock) e.getReplacement();
            setRealCodeBlockAt(offset, block);
        }

        return block;
    }

    private int executeVirtual8086(Processor cpu, int offset) {
        int x86Count = 0;
        int ip = cpu.getInstructionPointer();
        int startingBlock = ip & AddressSpace.INDEX_MASK;

        do {
            try {
                offset = ip & AddressSpace.BLOCK_MASK;
                Virtual8086ModeCodeBlock block = getVirtual8086ModeCodeBlockAt(offset);
                try {
                    try {
                        x86Count += block.execute(cpu);
                    } catch (NullPointerException e) {
                        block = codeBlockManager.getVirtual8086ModeCodeBlockAt(
                                this, offset);
                        x86Count += block.execute(cpu);
                    }
                } catch (CodeBlockReplacementException e) {
                    block = (Virtual8086ModeCodeBlock) e.getReplacement();
                    setVirtual8086CodeBlockAt(offset, block);
                    x86Count += block.execute(cpu);
                }
                cpu.processVirtual8086ModeInterrupts();
            } catch (ProcessorException p) {
                cpu.handleVirtual8086ModeException(p.getVector(), p
                        .hasErrorCode(), p.getErrorCode());
            }
        } while (((ip = cpu.getInstructionPointer()) & AddressSpace.INDEX_MASK) == startingBlock);

        return x86Count;
    }

    private CodeBlock decodeVirtual8086(Processor cpu, int offset) {
        Virtual8086ModeCodeBlock block = getVirtual8086ModeCodeBlockAt(offset);
        try {
            try {
                block.getX86Length();
            } catch (NullPointerException e) {
                block = codeBlockManager.getVirtual8086ModeCodeBlockAt(this,
                        offset);
                block.getX86Length();
            }
        } catch (CodeBlockReplacementException e) {
            block = (Virtual8086ModeCodeBlock) e.getReplacement();
            setVirtual8086CodeBlockAt(offset, block);
        }
        return block;
    }

    private RealModeCodeBlock getRealModeCodeBlockAt(int offset) {
        return realCodeBuffer[offset];
    }

    private ProtectedModeCodeBlock getProtectedModeCodeBlockAt(int offset) {
        return protectedCodeBuffer[offset];
    }

    private Virtual8086ModeCodeBlock getVirtual8086ModeCodeBlockAt(int offset) {
        return virtual8086CodeBuffer[offset];
    }

    private void removeVirtual8086CodeBlockAt(int offset) {
        Virtual8086ModeCodeBlock b = virtual8086CodeBuffer[offset];
        if ((b == null) || (b == PLACEHOLDER))
            return;

        virtual8086CodeBuffer[offset] = null;
        int len = b.getX86Length();
        for (int i = offset + 1; (i < offset + len)
                && (i < virtual8086CodeBuffer.length); i++) {
            if (virtual8086CodeBuffer[i] == PLACEHOLDER)
                virtual8086CodeBuffer[i] = null;
        }

        for (int i = Math.min(offset + len, virtual8086CodeBuffer.length) - 1; i >= 0; i--) {
            if (virtual8086CodeBuffer[i] == null) {
                if (i < offset)
                    break;
                else
                    continue;
            }
            if (virtual8086CodeBuffer[i] == PLACEHOLDER)
                continue;

            Virtual8086ModeCodeBlock bb = virtual8086CodeBuffer[i];
            len = bb.getX86Length();

            for (int j = i + 1; (j < i + len)
                    && (j < virtual8086CodeBuffer.length); j++) {
                if (virtual8086CodeBuffer[j] == null)
                    virtual8086CodeBuffer[j] = PLACEHOLDER;
            }
        }
    }

    private void removeProtectedCodeBlockAt(int offset) {
        ProtectedModeCodeBlock b = protectedCodeBuffer[offset];
        if ((b == null) || (b == PLACEHOLDER))
            return;

        protectedCodeBuffer[offset] = null;
        int len = b.getX86Length();
        for (int i = offset + 1; (i < offset + len)
                && (i < protectedCodeBuffer.length); i++) {
            if (protectedCodeBuffer[i] == PLACEHOLDER)
                protectedCodeBuffer[i] = null;
        }

        for (int i = Math.min(offset + len, protectedCodeBuffer.length) - 1; i >= 0; i--) {
            if (protectedCodeBuffer[i] == null) {
                if (i < offset)
                    break;
                else
                    continue;
            }
            if (protectedCodeBuffer[i] == PLACEHOLDER)
                continue;

            ProtectedModeCodeBlock bb = protectedCodeBuffer[i];
            len = bb.getX86Length();

            for (int j = i + 1; (j < i + len)
                    && (j < protectedCodeBuffer.length); j++) {
                if (protectedCodeBuffer[j] == null)
                    protectedCodeBuffer[j] = PLACEHOLDER;
            }
        }
    }

    private void removeRealCodeBlockAt(int offset) {
        RealModeCodeBlock b = realCodeBuffer[offset];
        if ((b == null) || (b == PLACEHOLDER))
            return;

        realCodeBuffer[offset] = null;
        int len = b.getX86Length();
        for (int i = offset + 1; (i < offset + len)
                && (i < realCodeBuffer.length); i++) {
            if (realCodeBuffer[i] == PLACEHOLDER)
                realCodeBuffer[i] = null;
        }

        for (int i = Math.min(offset + len, realCodeBuffer.length) - 1; i >= 0; i--) {
            if (realCodeBuffer[i] == null) {
                if (i < offset)
                    break;
                else
                    continue;
            }
            if (realCodeBuffer[i] == PLACEHOLDER)
                continue;

            RealModeCodeBlock bb = realCodeBuffer[i];
            len = bb.getX86Length();

            for (int j = i + 1; (j < i + len) && (j < realCodeBuffer.length); j++) {
                if (realCodeBuffer[j] == null)
                    realCodeBuffer[j] = PLACEHOLDER;
            }
        }
    }

    /**
     *
     * @param offset
     * @param block
     */
    public void setVirtual8086CodeBlockAt(int offset,
            Virtual8086ModeCodeBlock block) {
        removeVirtual8086CodeBlockAt(offset);
        if (block == null)
            return;

        virtual8086CodeBuffer[offset] = block;
        int len = block.getX86Length();
        for (int i = offset + 1; (i < offset + len)
                && (i < virtual8086CodeBuffer.length); i++) {
            if (virtual8086CodeBuffer[i] == null)
                virtual8086CodeBuffer[i] = PLACEHOLDER;
        }
    }

    /**
     *
     * @param offset
     * @param block
     */
    public void setProtectedCodeBlockAt(int offset, ProtectedModeCodeBlock block) {
        removeProtectedCodeBlockAt(offset);
        if (block == null)
            return;

        protectedCodeBuffer[offset] = block;
        int len = block.getX86Length();
        for (int i = offset + 1; (i < offset + len)
                && (i < protectedCodeBuffer.length); i++) {
            if (protectedCodeBuffer[i] == null)
                protectedCodeBuffer[i] = PLACEHOLDER;
        }
    }

    /**
     *
     * @param offset
     * @param block
     */
    public void setRealCodeBlockAt(int offset, RealModeCodeBlock block) {
        removeRealCodeBlockAt(offset);
        if (block == null)
            return;

        realCodeBuffer[offset] = block;
        int len = block.getX86Length();
        for (int i = offset + 1; (i < offset + len)
                && (i < realCodeBuffer.length); i++) {
            if (realCodeBuffer[i] == null)
                realCodeBuffer[i] = PLACEHOLDER;
        }
    }

    /**
     *
     * @param start
     * @param end
     */
    protected void regionAltered(int start, int end) {
        for (int i = end; i >= 0; i--) {
            RealModeCodeBlock b = realCodeBuffer[i];
            if (b == null) {
                if (i < start)
                    break;
                else
                    continue;
            }

            if (b == PLACEHOLDER)
                continue;

            if (!b.handleMemoryRegionChange(start, end))
                removeRealCodeBlockAt(i);
        }

        for (int i = end; i >= 0; i--) {
            ProtectedModeCodeBlock b = protectedCodeBuffer[i];
            if (b == null) {
                if (i < start)
                    break;
                else
                    continue;
            }

            if (b == PLACEHOLDER)
                continue;

            if (!b.handleMemoryRegionChange(start, end))
                removeProtectedCodeBlockAt(i);
        }

        for (int i = end; i >= 0; i--) {
            Virtual8086ModeCodeBlock b = virtual8086CodeBuffer[i];
            if (b == null) {
                if (i < start)
                    break;
                else
                    continue;
            }

            if (b == PLACEHOLDER)
                continue;

            if (!b.handleMemoryRegionChange(start, end))
                removeVirtual8086CodeBlockAt(i);
        }
    }

    /**
     *
     * @param address
     * @param buf
     * @param off
     * @param len
     */
    @Override
    public void copyContentsFrom(int address, byte[] buf, int off, int len) {
        super.copyContentsFrom(address, buf, off, len);
        regionAltered(address, address + len - 1);
    }

    /**
     *
     * @param offset
     * @param data
     */
    @Override
    public void setByte(int offset, byte data) {
        if (super.getByte(offset) == data)
            return;
        super.setByte(offset, data);
        regionAltered(offset, offset);
    }

    /**
     *
     * @param offset
     * @param data
     */
    @Override
    public void setWord(int offset, short data) {
        if (super.getWord(offset) == data)
            return;
        super.setWord(offset, data);
        regionAltered(offset, offset + 1);
    }

    /**
     *
     * @param offset
     * @param data
     */
    @Override
    public void setDoubleWord(int offset, int data) {
        if (super.getDoubleWord(offset) == data)
            return;
        super.setDoubleWord(offset, data);
        regionAltered(offset, offset + 3);
    }
    @Override
    public void clear() {
        constructCodeBlocksArray();
        super.clear();
    }

    @Override
    public String toString() {
        return "LazyCodeBlockMemory[" + getSize() + "]";
    }
    public static void dispose() {
        if (codeBlockManager != null)
            codeBlockManager.dispose();
        codeBlockManager = null;
    }
}
