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

import java.lang.reflect.*;
import java.util.*;

//import org.jpc.emulator.memory.codeblock.optimised.*;
//import org.jpc.emulator.memory.codeblock.*;

public class MicrocodeNode implements MicrocodeSet {
    private boolean hasImmediate;
    private int microcode, x86Position, immediate, x86Index;

    public MicrocodeNode(int microcode, int x86Position, int x86Index) {
        this.x86Index = x86Index;
        this.x86Position = x86Position;
        this.microcode = microcode;
        hasImmediate = false;
    }

    public MicrocodeNode(int microcode, int x86Position, int x86Index,
            int immediate) {
        this.x86Index = x86Index;
        this.x86Position = x86Position;
        this.microcode = microcode;
        this.immediate = immediate;
        hasImmediate = true;
    }

    public int getMicrocode() {
        return microcode;
    }

    public int getX86Index() {
        return x86Index;
    }

    public int getX86Position() {
        return x86Position;
    }

    public boolean hasImmediate() {
        return hasImmediate;
    }

    public int getImmediate() {
        return immediate;
    }

    public String toString() {
        return getName(microcode);
    }

    public static String getName(int microcode) {
        try {
            return microcodeNames[microcode];
        } catch (Exception e) {
            return "Invalid[" + microcode + "]";
        }
    }

    private static final String[] microcodeNames;
    static {
        Field[] fields = MicrocodeSet.class.getDeclaredFields();
        microcodeNames = new String[MicrocodeSet.MICROCODE_LIMIT];
        for (int i = 0; i < fields.length; i++) {
            if ("MICROCODE_LIMIT".equals(fields[i].getName()))
                continue;

            try {
                microcodeNames[fields[i].getInt(null)] = fields[i].getName();
            } catch (IllegalAccessException e) {
            }
        }
    }

    private static boolean hasImmediate(int microcode) {
        switch (microcode) {
        case LOAD0_IB:
        case LOAD0_IW:
        case LOAD0_ID:

        case LOAD1_IB:
        case LOAD1_IW:
        case LOAD1_ID:

        case LOAD2_IB:

        case ADDR_IB:
        case ADDR_IW:
        case ADDR_ID:
            return true;

        default:
            return false;
        }
    }

    public static MicrocodeNode[] getMicrocodes(InstructionSource source) {
        int x86Length = 0, x86Count = 0;
        Vector<MicrocodeNode> buffer = new Vector<MicrocodeNode>();

        while (source.getNext()) {
            x86Length += source.getX86Length();
            x86Count++;
            int length = source.getLength();
            for (int i = 0; i < length; i++) {
                int microcode = source.getMicrocode();
                MicrocodeNode node = null;

                if (hasImmediate(microcode)) {
                    node = new MicrocodeNode(microcode, x86Length, x86Count,
                            source.getMicrocode());
                    i++;
                } else
                    node = new MicrocodeNode(microcode, x86Length, x86Count);

                buffer.add(node);
            }
        }

        MicrocodeNode[] result = new MicrocodeNode[buffer.size()];
        buffer.toArray(result);
        return result;
    }
}
