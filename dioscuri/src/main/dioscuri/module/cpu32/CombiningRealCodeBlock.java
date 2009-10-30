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

//import org.jpc.emulator.processor.*;
//import org.jpc.emulator.memory.codeblock.optimised.*;

public class CombiningRealCodeBlock implements RealModeCodeBlock {
    private CodeBlock block0, block1, block2;
    private int x86Length = 0;
    private int block2Start, block1Start, block0Start;
    private boolean selfModified = false;

    public CombiningRealCodeBlock(CodeBlock block0, CodeBlock block1,
            CodeBlock block2, int start, int jumpSize) {
        this.block0 = block0;
        this.block1 = block1;
        this.block2 = block2;
        if (jumpSize < block1.getX86Length())
            x86Length = block0.getX86Length() + block1.getX86Length();
        else
            x86Length = block0.getX86Length() + jumpSize
                    + block2.getX86Length();
        this.block1Start = start + block0.getX86Length();
        this.block0Start = start;
        this.block2Start = block1Start + jumpSize;
    }

    public int getX86Length() {
        return x86Length;
    }

    public int getX86Count() {
        return block0.getX86Count() + block1.getX86Count()
                + block2.getX86Count();
    }

    public void invalidate() {
        this.selfModified = true;
    }

    public int execute(Processor cpu) {
        int count = 0, d = 0;
        int blockEntry = cpu.getInstructionPointer() & ~0xfff;

        // Returns the number of equivalent x86 instructions executed. Negative
        // results indicate an error
        while ((!selfModified)
                && !(cpu.eflagsInterruptEnable && ((cpu.getInterruptFlags() & Processor.IFLAGS_HARDWARE_INTERRUPT) != 0))) {
            d++;
            if (d > 10000)
                return count;
            int pointer = cpu.getInstructionPointer() & 0xfff;
            int blockNow = cpu.getInstructionPointer() & ~0xfff;
            if (blockNow != blockEntry)
                return count;

            if (pointer == block0Start) {
                try {
                    count += block0.execute(cpu);
                } catch (CodeBlockReplacementException e) {
                    block0 = e.getReplacement();
                    count += block0.execute(cpu);
                }
            } else if (pointer == block1Start) {
                try {
                    count += block1.execute(cpu);
                } catch (CodeBlockReplacementException e) {
                    block1 = e.getReplacement();
                    count += block1.execute(cpu);
                }
            } else if (pointer == block2Start) {
                try {
                    count += block2.execute(cpu);
                } catch (CodeBlockReplacementException e) {
                    block2 = e.getReplacement();
                    count += block2.execute(cpu);
                }
            } else {
                break;
            }
        }
        return count;
    }

    public boolean handleMemoryRegionChange(int startAddress, int endAddress) {
        invalidate();
        return false;
    }

    public String getDisplayString() {
        return "\nBlock0:\n" + block0.getDisplayString() + "\nBlock1:\n"
                + block1.getDisplayString() + "\nBlock2:\n"
                + block2.getDisplayString();
    }
}
