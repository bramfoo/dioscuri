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

//import org.jpc.emulator.memory.codeblock.fastcompiler.*;

public class RealModeRPNNode extends RPNNode {
    public RealModeRPNNode(int id, MicrocodeNode parent) {
        super(id, parent);
    }

    protected Object[] getByteCodes() {
        if (getMicrocode() == -1)
            return RealModeBytecodeFragments.pushCode(getID());

        if (hasImmediate())
            return RealModeBytecodeFragments.getOperation(getID(),
                    getMicrocode(), getX86Position(), getImmediate());
        else
            return RealModeBytecodeFragments.getOperation(getID(),
                    getMicrocode(), getX86Position());
    }

    public boolean hasExternalEffect() {
        if (getMicrocode() == -1)
            return false;

        return RealModeBytecodeFragments.hasExternalEffect(getID(),
                getMicrocode());
    }

    public boolean canThrowException() {
        if (getMicrocode() == -1)
            return false;

        return RealModeBytecodeFragments.hasExplicitThrow(getID(),
                getMicrocode())
                || hasExternalEffect();
    }
}
