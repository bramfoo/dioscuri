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

public abstract class RealModeTemplateBlock implements RealModeCodeBlock {
    protected static final ProcessorException exceptionDE = new ProcessorException(
            Processor.PROC_EXCEPTION_DE, true);
    protected static final ProcessorException exceptionGP = new ProcessorException(
            Processor.PROC_EXCEPTION_GP, true);
    protected static final ProcessorException exceptionSS = new ProcessorException(
            Processor.PROC_EXCEPTION_SS, true);
    protected static final ProcessorException exceptionUD = new ProcessorException(
            Processor.PROC_EXCEPTION_UD, true);

    protected static final boolean[] parityMap;

    static {
        parityMap = new boolean[256];
        for (int i = 0; i < 256; i++) {
            boolean val = true;
            for (int j = 0; j < 8; j++)
                if ((0x1 & (i >> j)) == 1)
                    val = !val;

            parityMap[i] = val;
        }
    }

    public String getDisplayString() {
        return getClass().getName();
    }

    public boolean handleMemoryRegionChange(int startAddress, int endAddress) {
        return false;
    }

    public String toString() {
        return "ByteCodeCompiled RealModeUBlock";
    }
}
