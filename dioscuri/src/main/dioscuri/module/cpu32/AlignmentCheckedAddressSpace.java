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

//import org.jpc.emulator.memory.codeblock.*;
//import org.jpc.emulator.processor.*;

/**
 *
 * @author Bram Lohman
 * @author Bart Kiers
 */
public class AlignmentCheckedAddressSpace extends AddressSpace {
    private static final ProcessorException ALIGNMENT_CHECK_EXCEPTION = new ProcessorException(
            Processor.PROC_EXCEPTION_AC, 0, true);
    private static final ProcessorException ALIGNMENT_CHECK_EXCEPTION_GP = new ProcessorException(
            Processor.PROC_EXCEPTION_GP, 0, true);

    private AddressSpace addressSpace;

    /**
     *
     * @param target
     */
    public AlignmentCheckedAddressSpace(AddressSpace target) {
        addressSpace = target;
    }

    /**
     *
     * @param offset
     * @return
     */
    public Memory getReadMemoryBlockAt(int offset) {
        return addressSpace.getReadMemoryBlockAt(offset);
    }

    /**
     *
     * @param offset
     * @return
     */
    public Memory getWriteMemoryBlockAt(int offset) {
        return addressSpace.getWriteMemoryBlockAt(offset);
    }

    void replaceBlocks(Memory oldBlock, Memory newBlock) {
        throw new IllegalStateException("Invalid Operation");
    }

    /**
     *
     * @param cpu
     * @param offset
     * @return
     */
    public int execute(Processor cpu, int offset) {
        throw new IllegalStateException("Invalid Operation");
    }

    /**
     *
     * @param cpu
     * @param offset
     * @return
     */
    public CodeBlock decodeCodeBlockAt(Processor cpu, int offset) {
        throw new IllegalStateException("Invalid Operation");
    }

    /**
     *
     * @return
     */
    public boolean updated() {
        return true;
    }

    /**
     *
     */
    public void clear() {
        addressSpace.clear();
    }

    /**
     *
     * @param offset
     * @return
     */
    @Override
    public byte getByte(int offset) {
        return addressSpace.getByte(offset);
    }

    /**
     *
     * @param offset
     * @param data
     */
    @Override
    public void setByte(int offset, byte data) {
        addressSpace.setByte(offset, data);
    }

    /**
     *
     * @param offset
     * @return
     */
    @Override
    public short getWord(int offset) {
        if ((offset & 0x1) != 0)
            throw ALIGNMENT_CHECK_EXCEPTION;

        return addressSpace.getWord(offset);
    }

    /**
     *
     * @param offset
     * @return
     */
    @Override
    public int getDoubleWord(int offset) {
        if ((offset & 0x3) != 0)
            throw ALIGNMENT_CHECK_EXCEPTION;

        return addressSpace.getDoubleWord(offset);
    }

    /**
     *
     * @param offset
     * @return
     */
    @Override
    public long getQuadWord(int offset) {
        if ((offset & 0x7) != 0)
            throw ALIGNMENT_CHECK_EXCEPTION;

        return addressSpace.getQuadWord(offset);
    }

    /**
     *
     * @param offset
     * @return
     */
    @Override
    public long getLowerDoubleQuadWord(int offset) {
        if ((offset & 0xF) != 0)
            throw ALIGNMENT_CHECK_EXCEPTION;

        return addressSpace.getLowerDoubleQuadWord(offset);
    }

    /**
     *
     * @param offset
     * @return
     */
    @Override
    public long getUpperDoubleQuadWord(int offset) {
        return addressSpace.getUpperDoubleQuadWord(offset);
    }

    /**
     *
     * @param offset
     * @param data
     */
    @Override
    public void setWord(int offset, short data) {
        if ((offset & 0x1) != 0)
            throw ALIGNMENT_CHECK_EXCEPTION;

        addressSpace.setWord(offset, data);
    }

    /**
     *
     * @param offset
     * @param data
     */
    @Override
    public void setDoubleWord(int offset, int data) {
        if ((offset & 0x3) != 0)
            throw ALIGNMENT_CHECK_EXCEPTION;

        addressSpace.setDoubleWord(offset, data);
    }

    /**
     *
     * @param offset
     * @param data
     */
    @Override
    public void setQuadWord(int offset, long data) {
        if ((offset & 0x7) != 0)
            throw ALIGNMENT_CHECK_EXCEPTION;

        addressSpace.setQuadWord(offset, data);
    }

    /**
     *
     * @param offset
     * @param data
     */
    @Override
    public void setLowerDoubleQuadWord(int offset, long data) {
        if ((offset & 0xF) != 0)
            throw ALIGNMENT_CHECK_EXCEPTION_GP;

        addressSpace.setLowerDoubleQuadWord(offset, data);
    }

    /**
     *
     * @param offset
     * @param data
     */
    @Override
    public void setUpperDoubleQuadWord(int offset, long data) {
        addressSpace.setUpperDoubleQuadWord(offset, data);
    }

    /**
     *
     * @param address
     * @param buffer
     * @param off
     * @param len
     */
    @Override
    public void copyContentsFrom(int address, byte[] buffer, int off, int len) {
        addressSpace.copyContentsFrom(address, buffer, off, len);
    }

    /**
     *
     * @param address
     * @param buffer
     * @param off
     * @param len
     */
    @Override
    public void copyContentsInto(int address, byte[] buffer, int off, int len) {
        addressSpace.copyContentsInto(address, buffer, off, len);
    }
}
