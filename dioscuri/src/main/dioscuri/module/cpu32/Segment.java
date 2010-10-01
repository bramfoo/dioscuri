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
//import org.jpc.emulator.*;

import java.io.DataOutput;
import java.io.IOException;

/**
 * @author Bram Lohman
 * @author Bart Kiers
 */
public abstract class Segment implements Hibernatable {
    /**
     * @return -
     */
    public abstract boolean isPresent();

    /**
     * @param memory
     */
    public abstract void setAddressSpace(AddressSpace memory);

    /**
     * @return -
     */
    public abstract int getType();

    /**
     * @return -
     */
    public abstract int getSelector();

    /**
     * @return -
     */
    public abstract int getLimit();

    /**
     * @return -
     */
    public abstract int getBase();

    /**
     * @return -
     */
    public abstract boolean getDefaultSizeFlag();

    /**
     * @return -
     */
    public abstract int getRPL();

    /**
     * @param cpl
     */
    public abstract void setRPL(int cpl);

    /**
     * @return -
     */
    public abstract int getDPL();

    /**
     * @param selector
     * @return -
     */
    public abstract boolean setSelector(int selector);

    /**
     * @param offset
     * @throws ProcessorException
     */
    public abstract void checkAddress(int offset) throws ProcessorException;

    /**
     * @param offset
     * @return -
     */
    public abstract int translateAddressRead(int offset);

    /**
     * @param offset
     * @return -
     */
    public abstract int translateAddressWrite(int offset);

    /**
     * @param offset
     * @return -
     */
    public abstract byte getByte(int offset);

    /**
     * @param offset
     * @return -
     */
    public abstract short getWord(int offset);

    /**
     * @param offset
     * @return -
     */
    public abstract int getDoubleWord(int offset);

    /**
     * @param offset
     * @return -
     */
    public abstract long getQuadWord(int offset);

    /**
     * @param offset
     * @param data
     */
    public abstract void setByte(int offset, byte data);

    /**
     * @param offset
     * @param data
     */
    public abstract void setWord(int offset, short data);

    /**
     * @param offset
     * @param data
     */
    public abstract void setDoubleWord(int offset, int data);

    /**
     * @param offset
     * @param data
     */
    public abstract void setQuadWord(int offset, long data);

    /**
     * @param output
     * @return -
     * @throws IOException
     */
    public abstract int dumpState(DataOutput output) throws IOException;
}
