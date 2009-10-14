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


//import org.jpc.emulator.processor.Processor;
//import org.jpc.emulator.memory.codeblock.CodeBlock;

public abstract class Memory implements ByteArray
{
public abstract void clear();

public abstract void clear(int start, int length);

public abstract void copyContentsInto(int address, byte[] buffer, int off, int len);

public abstract void copyContentsFrom(int address, byte[] buffer, int off, int len);

public boolean isAllocated()
{
    return true;
}

public abstract long getSize();

public abstract byte getByte(int offset);

public abstract short getWord(int offset);

public abstract int getDoubleWord(int offset);

public abstract long getQuadWord(int offset);

public abstract long getLowerDoubleQuadWord(int offset);

public abstract long getUpperDoubleQuadWord(int offset);

public abstract void setByte(int offset, byte data);

public abstract void setWord(int offset, short data);

public abstract void setDoubleWord(int offset, int data);

public abstract void setQuadWord(int offset, long data);

public abstract void setLowerDoubleQuadWord(int offset, long data);

public abstract void setUpperDoubleQuadWord(int offset, long data);

public abstract int execute(Processor cpu, int address);
public abstract CodeBlock decodeCodeBlockAt(Processor cpu, int address);
}
