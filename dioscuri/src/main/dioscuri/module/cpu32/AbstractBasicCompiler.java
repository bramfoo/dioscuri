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

public abstract class AbstractBasicCompiler implements CodeBlockCompiler {
    protected int bufferOffset;
    protected int[] bufferMicrocodes;
    protected int[] bufferPositions;

    public AbstractBasicCompiler() {
        bufferMicrocodes = new int[100];
        bufferPositions = new int[100];
        bufferOffset = 0;
    }

    protected void buildCodeBlockBuffers(InstructionSource source) {
        bufferOffset = 0;
        int position = 0;

        while (source.getNext()) {
            int uCodeLength = source.getLength();
            int uCodeX86Length = source.getX86Length();
            position += uCodeX86Length;

            for (int i = 0; i < uCodeLength; i++) {
                int data = source.getMicrocode();
                try {
                    bufferMicrocodes[bufferOffset] = data;
                    bufferPositions[bufferOffset] = position;
                } catch (ArrayIndexOutOfBoundsException e) {
                    int[] newMicrocodes = new int[bufferMicrocodes.length * 2];
                    int[] newPositions = new int[bufferMicrocodes.length * 2];
                    System.arraycopy(bufferMicrocodes, 0, newMicrocodes, 0,
                            bufferMicrocodes.length);
                    System.arraycopy(bufferPositions, 0, newPositions, 0,
                            bufferPositions.length);
                    bufferMicrocodes = newMicrocodes;
                    bufferPositions = newPositions;
                    bufferMicrocodes[bufferOffset] = data;
                    bufferPositions[bufferOffset] = position;
                }
                bufferOffset++;
            }
        }
        return;
    }
}
