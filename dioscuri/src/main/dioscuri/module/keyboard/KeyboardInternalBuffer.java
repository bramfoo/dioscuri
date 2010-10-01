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

package dioscuri.module.keyboard;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal keyboard buffer Virtual buffer - located in the keyboard hardware
 * Contains data intended for controller (ACKS/NACKS, scancodes, etc.)
 */
public class KeyboardInternalBuffer {
    protected final static int NUM_ELEMENTS = 16;

    private List<Byte> buffer = new ArrayList<Byte>(NUM_ELEMENTS);// List of data
    // elements
    byte expectingTypematic; // Keyboard repeat rate command issued
    byte expectingLEDWrite; // Keyboard LED change command issued
    byte expectingScancodeSet; // Alternate scancode set command issued
    byte keyPressDelay; // Delay between keypresses
    byte keyRepeatRate; // Key repeat rate
    byte ledStatus; // Current Num-Lock, Caps-Lock, Scroll-Lock status
    byte scanningEnabled; // Keyboard enabled

    /**
     * Returns a List of data elements (as bytes) of this internal buffer.
     *
     * @return a List of data elements (as bytes) of this internal buffer.
     */
    public synchronized List<Byte> getBuffer()
    {
        return buffer;
    }
}
