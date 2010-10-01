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

/**
 * @author Bram Lohman
 * @author Bart Kiers
 */
public class AbstractCodeBlockWrapper implements CodeBlock {
    private static long nextBlockIndex = 0;

    private long blockIndex;
    private CodeBlock actualBlock;

    /**
     * @param target
     */
    public AbstractCodeBlockWrapper(CodeBlock target)
    {
        blockIndex = nextBlockIndex++;
        actualBlock = target;
    }

    /**
     * @return -
     */
    public final int getX86Length()
    {
        return actualBlock.getX86Length();
    }

    /**
     * @return -
     */
    public final int getX86Count()
    {
        return actualBlock.getX86Count();
    }

    // Returns the number of equivalent x86 instructions executed. Negative
    // results indicate an error

    public int execute(Processor cpu)
    {
        return actualBlock.execute(cpu);
    }

    /**
     * @return -
     */
    public String getDisplayString()
    {
        return "WRAP[" + blockIndex + "] " + actualBlock.getDisplayString();
    }

    /**
     * @param block
     */
    public final void setBlock(CodeBlock block)
    {
        this.actualBlock = block;
    }

    /**
     * @return -
     */
    public final CodeBlock getBlock()
    {
        return actualBlock;
    }

    /**
     * @return -
     */
    public final long getBlockIndex()
    {
        return blockIndex;
    }

    /**
     * @param startAddress
     * @param endAddress
     * @return -
     */
    public boolean handleMemoryRegionChange(int startAddress, int endAddress)
    {
        return actualBlock.handleMemoryRegionChange(startAddress, endAddress);
    }

    void replaceInOwner(CodeBlock replacement)
    {
        setBlock(new ReplacementBlockTrigger(replacement));
    }

}
