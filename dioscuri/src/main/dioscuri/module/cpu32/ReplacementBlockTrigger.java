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
 *
 * @author Bram Lohman
 * @author Bart Kiers
 */
public class ReplacementBlockTrigger implements CodeBlock {
    private CodeBlock replacement;
    private CodeBlockReplacementException exception;

    /**
     *
     * @param block
     */
    public ReplacementBlockTrigger(CodeBlock block) {
        replacement = block;
        exception = new CodeBlockReplacementException(block);
    }

    /**
     *
     * @return
     */
    public int getX86Length() {
        return replacement.getX86Length();
    }

    /**
     *
     * @return
     */
    public int getX86Count() {
        return replacement.getX86Count();
    }

    // Returns the number of equivalent x86 instructions executed. Negative
    // results indicate an error
    public int execute(Processor cpu) {
        throw exception;
    }

    /**
     *
     * @return
     */
    public String getDisplayString() {
        return replacement.getDisplayString();
    }

    /**
     *
     * @param startAddress
     * @param endAddress
     * @return
     */
    public boolean handleMemoryRegionChange(int startAddress, int endAddress) {
        return false;
    }
}
