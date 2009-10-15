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
import java.io.*;

public abstract class Segment implements Hibernatable
{
public abstract boolean isPresent();

public abstract void setAddressSpace(AddressSpace memory);

public abstract int getType();

public abstract int getSelector();

public abstract int getLimit();

public abstract int getBase();

public abstract boolean getDefaultSizeFlag();

public abstract int getRPL();

public abstract void setRPL(int cpl);

public abstract int getDPL();

public abstract boolean setSelector(int selector);

public abstract void checkAddress(int offset) throws ProcessorException;

public abstract int translateAddressRead(int offset);

public abstract int translateAddressWrite(int offset);

public abstract byte getByte(int offset);

public abstract short getWord(int offset);

public abstract int getDoubleWord(int offset);

public abstract long getQuadWord(int offset);

public abstract void setByte(int offset, byte data);

public abstract void setWord(int offset, short data);

public abstract void setDoubleWord(int offset, int data);

public abstract void setQuadWord(int offset, long data);

public abstract int dumpState(DataOutput output) throws IOException;
}
