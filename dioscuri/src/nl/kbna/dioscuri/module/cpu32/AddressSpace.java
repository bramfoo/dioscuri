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
package nl.kbna.dioscuri.module.cpu32;

import java.util.*;

//import org.jpc.emulator.*;
//import org.jpc.emulator.memory.codeblock.*;


public abstract class AddressSpace extends AbstractMemory
{ 
public static final int BLOCK_SIZE = 4*1024;
public static final int BLOCK_MASK = BLOCK_SIZE-1;
public static final int INDEX_MASK = ~(BLOCK_MASK);
public static final int INDEX_SHIFT = 12;
public static final int INDEX_SIZE = 1 << (32 - INDEX_SHIFT);

public AddressSpace()
{
}

public final long getSize()
{
    return 0x100000000l;
}

public final int getBlockStart(int address)
{
    return address & INDEX_MASK;
}

public final int getBlockEnd(int address)
{
    return (address & INDEX_MASK) + BLOCK_SIZE;
} 

public abstract Memory getReadMemoryBlockAt(int offset);

public abstract Memory getWriteMemoryBlockAt(int offset);

public abstract void clear();

public abstract boolean updated();

public byte getByte(int offset)
{
    return getReadMemoryBlockAt(offset).getByte(offset & BLOCK_MASK);
}

public void setByte(int offset, byte data)
{
    getWriteMemoryBlockAt(offset).setByte(offset & BLOCK_MASK, data);
}

public short getWord(int offset)
{
    try
    {
        return getReadMemoryBlockAt(offset).getWord(offset & BLOCK_MASK);
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
        return super.getWord(offset);
    }
}

public int getDoubleWord(int offset)
{
    try
    {
        return getReadMemoryBlockAt(offset).getDoubleWord(offset & BLOCK_MASK);
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
        return super.getDoubleWord(offset);
    }
}

public long getQuadWord(int offset)
{
    try
    {
        return getReadMemoryBlockAt(offset).getQuadWord(offset & BLOCK_MASK);
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
        return super.getQuadWord(offset);
    }
}

public long getLowerDoubleQuadWord(int offset)
{
    try
    {
        return getReadMemoryBlockAt(offset).getLowerDoubleQuadWord(offset & BLOCK_MASK);
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
        return super.getLowerDoubleQuadWord(offset);
    }
}

public long getUpperDoubleQuadWord(int offset)
{
    try
    {
        return getReadMemoryBlockAt(offset).getUpperDoubleQuadWord(offset & BLOCK_MASK);
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
        return super.getUpperDoubleQuadWord(offset);
    }
}

public void setWord(int offset, short data)
{
    try
    {
        getWriteMemoryBlockAt(offset).setWord(offset & BLOCK_MASK, data);
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
        super.setWord(offset, data);
    }
}

public void setDoubleWord(int offset, int data)
{
    try
    {
        getWriteMemoryBlockAt(offset).setDoubleWord(offset & BLOCK_MASK, data);
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
        super.setDoubleWord(offset, data);
    }
}

public void setQuadWord(int offset, long data)
{
    try
    {
        getWriteMemoryBlockAt(offset).setQuadWord(offset & BLOCK_MASK, data);
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
        super.setQuadWord(offset, data);
    }
}

public void setLowerDoubleQuadWord(int offset, long data)
{
    try
    {
        getWriteMemoryBlockAt(offset).setLowerDoubleQuadWord(offset & BLOCK_MASK, data);
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
        super.setLowerDoubleQuadWord(offset, data);
    }
}

public void setUpperDoubleQuadWord(int offset, long data)
{
    try
    {
        getWriteMemoryBlockAt(offset).setUpperDoubleQuadWord(offset & BLOCK_MASK, data);
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
        super.setUpperDoubleQuadWord(offset, data);
    }
}

public void copyContentsFrom(int address, byte[] buffer, int off, int len)
{
do {
    int partialLength = Math.min(BLOCK_SIZE - (address & BLOCK_MASK), len);
    getWriteMemoryBlockAt(address).copyContentsFrom(address & BLOCK_MASK, buffer, off, partialLength);
    address += partialLength;
    off += partialLength;       
    len -= partialLength;
} while (len > 0);
}

public void copyContentsInto(int address, byte[] buffer, int off, int len)
{
do {
    int partialLength = Math.min(BLOCK_SIZE - (address & BLOCK_MASK), len);
    getReadMemoryBlockAt(address).copyContentsInto(address & BLOCK_MASK, buffer, off, partialLength);
    address += partialLength;
    off += partialLength;       
    len -= partialLength;
} while (len > 0);
}

abstract void replaceBlocks(Memory oldBlock, Memory newBlock);
}
