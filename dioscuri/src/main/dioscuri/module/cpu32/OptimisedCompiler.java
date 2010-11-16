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

import dioscuri.module.clock.Clock;

//import org.jpc.emulator.memory.codeblock.*;

/**
 * @author Bram Lohman
 * @author Bart Kiers
 */
public class OptimisedCompiler extends AbstractBasicCompiler {

    private Clock clock;

    /**
     * @param clk
     */
    public OptimisedCompiler(Clock clk) {
        this.clock = clk;
    }

    /**
     * @param source
     * @return -
     */
    public RealModeCodeBlock getRealModeCodeBlock(InstructionSource source) {
        buildCodeBlockBuffers(source);

        int[] newMicrocodes = new int[bufferOffset];
        int[] newPositions = new int[bufferOffset];
        System.arraycopy(bufferMicrocodes, 0, newMicrocodes, 0, bufferOffset);
        System.arraycopy(bufferPositions, 0, newPositions, 0, bufferOffset);

        return new RealModeUBlock(newMicrocodes, newPositions, clock);
    }

    /**
     * @param source
     * @return -
     */
    public ProtectedModeCodeBlock getProtectedModeCodeBlock(
            InstructionSource source) {
        buildCodeBlockBuffers(source);

        int[] newMicrocodes = new int[bufferOffset];
        int[] newPositions = new int[bufferOffset];
        System.arraycopy(bufferMicrocodes, 0, newMicrocodes, 0, bufferOffset);
        System.arraycopy(bufferPositions, 0, newPositions, 0, bufferOffset);

        return new ProtectedModeUBlock(newMicrocodes, newPositions);
    }

    /**
     * @param source
     * @return -
     */
    public Virtual8086ModeCodeBlock getVirtual8086ModeCodeBlock(
            InstructionSource source) {
        buildCodeBlockBuffers(source);

        int[] newMicrocodes = new int[bufferOffset];
        int[] newPositions = new int[bufferOffset];
        System.arraycopy(bufferMicrocodes, 0, newMicrocodes, 0, bufferOffset);
        System.arraycopy(bufferPositions, 0, newPositions, 0, bufferOffset);

        return new Virtual8086ModeUBlock(newMicrocodes, newPositions);
    }
}
