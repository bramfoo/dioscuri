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
import dioscuri.exception.ModuleUnknownPort;
import dioscuri.exception.ModuleWriteOnlyPortException;

/**
 * Abstract class representing a generic hardware device module. This class
 * defines all methods that are required for a device module, i.e. a module that
 * is connected to the motherboard and requires I/O address space.
 * 
 */

public abstract class ModuleDevice extends Module {
    // Methods

    /**
     * Retrieve the interval between subsequent updates
     * 
     * @return int interval in microseconds
     */
    public abstract int getUpdateInterval();

    /**
     * Defines the interval between subsequent updates
     * 
     * @param interval
     */
    public abstract void setUpdateInterval(int interval);

    /**
     * Update device
     * 
     */
    public abstract void update();

    /**
     * Return a byte from I/O address space at given I/O port
     * 
     * @param portAddress
     * @return byte containing the data at given I/O address port
     * @throws ModuleException
     *             , ModuleUnknownPort, ModuleWriteOnlyPortException
     * @throws ModuleUnknownPort
     * @throws ModuleWriteOnlyPortException
     */
    public abstract byte getIOPortByte(int portAddress) throws ModuleException,
            ModuleUnknownPort, ModuleWriteOnlyPortException;

    /**
     * Set a byte in I/O address space at given port
     * 
     * @param portAddress 
     * @param data
     * @throws ModuleException
     *             , ModuleUnknownPort, ModuleWriteOnlyPortException
     * @throws ModuleUnknownPort
     */
    public abstract void setIOPortByte(int portAddress, byte data)
            throws ModuleException, ModuleUnknownPort;

    /**
     * Return a word from I/O address space at given port
     * 
     * @param portAddress 
     * @return byte[] containing the word at given I/O address port
     * @throws ModuleException
     *             , ModuleWriteOnlyPortException
     * @throws ModuleUnknownPort
     * @throws ModuleWriteOnlyPortException
     */
    public abstract byte[] getIOPortWord(int portAddress)
            throws ModuleException, ModuleUnknownPort,
            ModuleWriteOnlyPortException;

    /**
     * Set a word in I/O address space at given port
     * 
     * @param portAddress
     * @param dataWord
     * @throws ModuleException
     *             , ModuleWriteOnlyPortException
     * @throws ModuleUnknownPort
     */
    public abstract void setIOPortWord(int portAddress, byte[] dataWord)
            throws ModuleException, ModuleUnknownPort;

    /**
     * Return a double word from I/O address space at given port
     * 
     * @param portAddress
     * @return byte[] containing the double word at given I/O address port
     * @throws ModuleException
     *             , ModuleWriteOnlyPortException
     * @throws ModuleUnknownPort
     * @throws ModuleWriteOnlyPortException
     */
    public abstract byte[] getIOPortDoubleWord(int portAddress)
            throws ModuleException, ModuleUnknownPort,
            ModuleWriteOnlyPortException;

    /**
     * Set a double word in I/O address space at given port
     * 
     * @param portAddress
     * @param dataDoubleWord
     * @throws ModuleException
     *             , ModuleWriteOnlyPortException
     * @throws ModuleUnknownPort
     */
    public abstract void setIOPortDoubleWord(int portAddress,
            byte[] dataDoubleWord) throws ModuleException, ModuleUnknownPort;
}
