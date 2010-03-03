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

import java.io.*;

/**
 *
 * @author Bram Lohman
 * @author Bart Kiers
 */
public class CountingOutputStream extends OutputStream {
    private final OutputStream backing;
    private int count;

    /**
     *
     * @param wraps
     */
    public CountingOutputStream(OutputStream wraps) {
        backing = wraps;
        count = 0;
    }

    /**
     *
     * @return -
     */
    public int position() {
        return count;
    }

    @Override
    public void close() throws IOException {
        backing.close();
    }

    @Override
    public void flush() throws IOException {
        backing.flush();
    }

    @Override
    public void write(byte[] b) throws IOException {
        if ((count + b.length) > 0xffff)
            throw new IllegalStateException("Oversize Method");

        backing.write(b);
        count += b.length;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if ((count + len) > 0xffff)
            throw new IllegalStateException("Oversize Method");

        backing.write(b, off, len);
        count += len;
    }

    public void write(int b) throws IOException {
        if ((count + 1) > 0xffff)
            throw new IllegalStateException("Oversize Method");

        backing.write(b);
        count++;
    }
}
