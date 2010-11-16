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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Bram Lohman
 * @author Bart Kiers
 */
public class ConstantPoolSymbol {
    private Object poolEntity;

    /**
     * @param o
     */
    public ConstantPoolSymbol(Object o) {
        boolean ok = (o instanceof Class<?>) || (o instanceof Method)
                || (o instanceof Field) || (o instanceof String)
                || (o instanceof Integer) || (o instanceof Float)
                || (o instanceof Long) || (o instanceof Double);

        if (!ok)
            throw new IllegalArgumentException();

        poolEntity = o;
    }

    /**
     * @return -
     */
    public Object poolEntity() {
        return poolEntity;
    }

    @Override
    public String toString() {
        return "ConstantPoolSymbol[" + poolEntity + "]";
    }
}
