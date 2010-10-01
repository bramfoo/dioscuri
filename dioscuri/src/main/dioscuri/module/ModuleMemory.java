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

import dioscuri.exception.ModuleException;
import dioscuri.interfaces.Module;

/**
 * Abstract class representing a memory hardware module.
 */
public abstract class ModuleMemory extends AbstractModule {

    /**
     *
     */
    public ModuleMemory()
    {
        super(Module.Type.MEMORY,
                Module.Type.VIDEO, Module.Type.CPU, Module.Type.MOTHERBOARD);
    }

    /**
     * Return a byte from memory at a specific address
     *
     * @param address Flat-address where data can be found
     * @return the byte at the given address
     * @throws ModuleException
     */
    public abstract byte getByte(int address) throws ModuleException;

    /**
     * Set a byte in memory at given address
     *
     * @param address
     * @param value
     * @throws ModuleException
     */
    public abstract void setByte(int address, byte value) throws ModuleException;

    /**
     * Returns the value of a word at a specific address Note: words in memory
     * are stored in Little Endian order (LSB, MSB), but this method returns a
     * word in Big Endian order (MSB, LSB) because this is the common way words
     * are used by instructions.
     *
     * @param address Flat-address where data can be found
     * @return byte[] array [MSB, LSB] containing data, 0xFFFF if address
     *         outside RAM_SIZE range
     * @throws ModuleException
     */
    public abstract byte[] getWord(int address) throws ModuleException;

    /**
     * Stores the value of a word at a specific address Note: words in memory
     * are stored in Little Endian order (LSB, MSB), but this method assumes
     * that a word is given in Big Endian order (MSB, LSB) because this is the
     * common way words are used by instructions. However, storage takes place
     * in Little Endian order.
     *
     * @param address
     * @param value
     * @throws ModuleException
     */
    public abstract void setWord(int address, byte[] value) throws ModuleException;

    /**
     * Stores an array of bytes in memory starting at a specific address
     *
     * @param address      Flat-address where data is stored
     * @param binaryStream
     * @throws ModuleException
     */
    public abstract void setBytes(int address, byte[] binaryStream) throws ModuleException;

    /**
     * Set A20 address line toggle
     *
     * @param status
     */
    public abstract void setA20AddressLine(boolean status);

    /**
     * Set watch toggle and address to trace in memory
     *
     * @param isWatchOn
     * @param watchAddress
     */
    public abstract void setWatchValueAndAddress(boolean isWatchOn, int watchAddress);

    /**
     * Set RAM Size in megabytes
     *
     * @param ramSizeMB
     */
    public abstract void setRamSizeInMB(int ramSizeMB);
}
