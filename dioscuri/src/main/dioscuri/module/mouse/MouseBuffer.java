/* $Revision: 159 $ $Date: 2009-08-17 12:52:56 +0000 (ma, 17 aug 2009) $ $Author: blohman $ 
 * 
 * Copyright (C) 2007-2009  National Library of the Netherlands, 
 *                          Nationaal Archief of the Netherlands, 
 *                          Planets
 *                          KEEP
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 *
 * For more information about this project, visit
 * http://dioscuri.sourceforge.net/
 * or contact us via email:
 *   jrvanderhoeven at users.sourceforge.net
 *   blohman at users.sourceforge.net
 *   bkiers at users.sourceforge.net
 * 
 * Developed by:
 *   Nationaal Archief               <www.nationaalarchief.nl>
 *   Koninklijke Bibliotheek         <www.kb.nl>
 *   Tessella Support Services plc   <www.tessella.com>
 *   Planets                         <www.planets-project.eu>
 *   KEEP                            <www.keep-project.eu>
 * 
 * Project Title: DIOSCURI
 */

package dioscuri.module.mouse;

import java.util.ArrayList;

/**
 * Mouse buffer Virtual buffer - located in the mouse hardware Contains data
 * intended for controller
 * 
 */
@SuppressWarnings("serial")
public class MouseBuffer extends ArrayList<Byte> {
    /**
     *
     */
    protected final static int NUM_ELEMENTS = 16;

    byte keyPressDelay; // Delay between keypresses
    byte keyRepeatRate; // Key repeat rate
    byte scanningEnabled; // Keyboard enabled

    /**
     * 
     * @param capacity
     */
    public MouseBuffer(int capacity) {
        super(capacity);
    }

    // Methods
    /**
     *
     * @param data
     */
    protected void setByte(byte data) {
        super.add(Byte.valueOf(data));
    }

    /**
     *
     * @return -
     */
    protected byte getByte() {
        byte data = ((Byte) super.get(0)).byteValue();
        super.remove(0);
        return data;
    }
}
