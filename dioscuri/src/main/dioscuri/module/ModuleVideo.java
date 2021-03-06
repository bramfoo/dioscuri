/* $Revision: 160 $ $Date: 2009-08-17 12:56:40 +0000 (ma, 17 aug 2009) $ $Author: blohman $ 
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

package dioscuri.module;

import dioscuri.interfaces.Addressable;
import dioscuri.interfaces.Module;
import dioscuri.interfaces.Updateable;

/**
 * Abstract class representing a generic video module.
 */
public abstract class ModuleVideo extends AbstractModule implements Addressable, Updateable {

    /**
     *
     */
    public ModuleVideo() {
        super(Module.Type.VIDEO,
                Module.Type.MOTHERBOARD, Module.Type.CPU, Module.Type.SCREEN, Module.Type.RTC);
    }

    /**
     * Video read mode implementations
     *
     * @param address
     * @return -
     */
    public abstract byte readMode(int address);

    /**
     * Video write mode implementations
     *
     * @param address
     * @param data
     */
    public abstract void writeMode(int address, byte data);

    /**
     * Determine the screen size in pixels
     *
     * @return integer array containing [height, width] of screen in pixels
     */
    public abstract int[] determineScreenSize();

    /**
     * Returns a pointer to the whole video buffer
     *
     * @return byte[] containing the video buffer
     */
    public abstract byte[] getVideoBuffer();

    /**
     * Returns a byte from video buffer at position index
     *
     * @param index
     * @return byte from video buffer
     */
    public abstract byte getVideoBufferByte(int index);

    /**
     * Stores a byte in video buffer at position index
     *
     * @param index
     * @param data
     */
    public abstract void setVideoBufferByte(int index, byte data);

    /**
     * Returns all characters (as Unicode) that are currently in buffer
     *
     * @return String containing all characters in the buffer or null when no
     *         characters exist
     */
    public abstract String getVideoBufferCharacters();

    /**
     * Returns a byte from text snapshot at position index
     *
     * @param index
     * @return byte from textsnapshot
     */
    public abstract byte getTextSnapshot(int index);

    /**
     * Stores a byte in text snapshot at position index
     *
     * @param index
     * @param data
     */
    public abstract void setTextSnapshot(int index, byte data);

    /**
     * Returns a byte from attribute palette register at position index
     *
     * @param index
     * @return byte from register
     */
    public abstract byte getAttributePaletteRegister(int index);

}
