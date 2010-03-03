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

/**
 *
 * @author Bram Lohman
 * @author Bart Kiers
 */
public interface CodeBlockCompiler {
    /**
     *
     * @param source
     * @return -
     */
    public RealModeCodeBlock getRealModeCodeBlock(InstructionSource source);

    /**
     *
     * @param source
     * @return -
     */
    public ProtectedModeCodeBlock getProtectedModeCodeBlock(
            InstructionSource source);

    /**
     *
     * @param source
     * @return -
     */
    public Virtual8086ModeCodeBlock getVirtual8086ModeCodeBlock(
            InstructionSource source);
}
