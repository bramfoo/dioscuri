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

//import org.jpc.emulator.memory.*;

/**
 * @author Bram Lohman
 * @author Bart Kiers
 */
public class ByteSourceWrappedMemory implements ByteSource {
    private Memory source;
    private int offset, startingPosition;

    /**
     * @param source
     * @param offset
     */
    public void set(Memory source, int offset) {
        this.source = source;
        this.offset = offset;
        startingPosition = offset;
    }

    /**
     * @return -
     */
    public Memory getMemory() {
        return source;
    }

    /**
     * @return -
     */
    public int getOffset() {
        return offset;
    }

    /**
     * @return -
     */
    public byte getByte() {
        return source.getByte(offset++);
    }

    /**
     * @param count
     * @return -
     */
    public boolean skip(int count) {
        if (offset + count >= source.getSize())
            return false;
        offset += count;
        return true;
    }

    /**
     * @param count
     * @return -
     */
    public boolean rewind(int count) {
        if (offset - count < startingPosition)
            return false;
        offset -= count;
        return true;
    }

    /**
     * @return -
     */
    public boolean reset() {
        offset = startingPosition;
        return true;
    }
}
