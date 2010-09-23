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

package dioscuri.module.motherboard;

import java.util.logging.Level;
import java.util.logging.Logger;

import dioscuri.Emulator;
import dioscuri.exception.ModuleException;
import dioscuri.exception.ModuleWriteOnlyPortException;
import dioscuri.interfaces.Addressable;
import dioscuri.interfaces.Module;
import dioscuri.module.AbstractModule;
import dioscuri.module.ModuleMotherboard;

/**
 * This class is a dummy for a peripheral device.
 * 
 * Contains all standard methods for a device.
 * 
 * Note: init this module only after all other devices, because this class
 * claims all available I/O address space that is left
 * 
 * @see dioscuri.module.AbstractModule
 * 
 */
public class DeviceDummy extends AbstractModule implements Addressable {

    // Logging
    private static final Logger logger = Logger.getLogger(DeviceDummy.class.getName());

    /**
     * Class constructor
     * 
     * @param owner
     */
    public DeviceDummy(Emulator owner) {
        super(Module.Type.DUMMY,
                Module.Type.MOTHERBOARD);
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.AbstractModule
     */
    @Override
    public boolean reset() {
        // Register I/O ports in I/O address space
        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Module.Type.MOTHERBOARD);
        
        // Claim all other I/O space
        for (int port = 0x0000; port < motherboard.ioSpaceSize; port++) {
            motherboard.setIOPort(port, this);
        }

        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public byte getIOPortByte(int portAddress) throws ModuleException {
        if (portAddress == 0x92) {
            ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Module.Type.MOTHERBOARD);
            logger.log(Level.CONFIG, "[" + super.getType() + "]"
                    + " IN command (byte) to port "
                    + Integer.toHexString(portAddress).toUpperCase()
                    + " received");
            logger.log(Level.INFO, "[" + super.getType() + "]"
                    + " Returned A20 value: "
                    + Boolean.toString(motherboard.getA20()));
            return (byte) (motherboard.getA20() ? (1 << 1) : 0);
        }

        logger.log(Level.CONFIG, "[" + super.getType() + "]"
                + " IN command (byte) to port "
                + Integer.toHexString(portAddress).toUpperCase() + " received");
        logger.log(Level.INFO, "[" + super.getType() + "]"
                + " Returned default value 0xFF to AL");

        // Return dummy value 0xFF
        return (byte) 0x0FF;
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public void setIOPortByte(int portAddress, byte data)
            throws ModuleException {
        if (portAddress == 0x92) {
            ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Module.Type.MOTHERBOARD);
            logger.log(Level.CONFIG, "[" + super.getType() + "]"
                    + " OUT command (byte) to port "
                    + Integer.toHexString(portAddress).toUpperCase()
                    + " received");
            logger.log(Level.INFO, "[" + super.getType() + "]" + " Set A20 value: "
                    + data);
            motherboard.setA20((data & 0x02) == 2 ? true : false);
            return;
        }
        logger.log(Level.WARNING, "[" + super.getType() + "]"
                + " OUT command (byte) to port "
                + Integer.toHexString(portAddress).toUpperCase()
                + " received. No action taken.");
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public byte[] getIOPortWord(int portAddress) throws ModuleException,
            ModuleWriteOnlyPortException {
        if (portAddress == 0x92) {
            ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Module.Type.MOTHERBOARD);
            logger.log(Level.CONFIG, "[" + super.getType() + "]"
                    + " IN command (word) to port "
                    + Integer.toHexString(portAddress).toUpperCase()
                    + " received");
            logger.log(Level.INFO, "[" + super.getType() + "]"
                    + " Returned A20 value");
            return (motherboard.getA20() ? new byte[] { 0x00, (byte) (1 << 1) }
                    : new byte[] { 0x00, 0x00 });
        }
        logger.log(Level.WARNING, "[" + super.getType() + "]"
                + " IN command (word) to port "
                + Integer.toHexString(portAddress).toUpperCase() + " received");
        logger.log(Level.WARNING, "[" + super.getType() + "]"
                + " Returned default value 0xFFFF to AX");

        // Return dummy value 0xFFFF
        return new byte[] { (byte) 0x0FF, (byte) 0x0FF };
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public void setIOPortWord(int portAddress, byte[] dataWord)
            throws ModuleException {
        if (portAddress == 0x92) {
            ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Module.Type.MOTHERBOARD);
            logger.log(Level.CONFIG, "[" + super.getType() + "]"
                    + " OUT command (word) to port "
                    + Integer.toHexString(portAddress).toUpperCase()
                    + " received");
            logger.log(Level.INFO, "[" + super.getType() + "]" + " Set A20 value");
            motherboard.setA20((dataWord[1] & 0x02) == 2 ? true : false);
            return;
        }

        logger.log(Level.WARNING, "[" + super.getType() + "]"
                + " OUT command (word) to port "
                + Integer.toHexString(portAddress).toUpperCase()
                + " received. No action taken.");
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public byte[] getIOPortDoubleWord(int portAddress) throws ModuleException,
            ModuleWriteOnlyPortException {
        logger.log(Level.WARNING, "[" + super.getType() + "]"
                + " IN command (double word) to port "
                + Integer.toHexString(portAddress).toUpperCase() + " received");
        logger.log(Level.WARNING, "[" + super.getType() + "]"
                + " Returned default value 0xFFFFFFFF to eAX");

        // Return dummy value 0xFFFFFFFF
        return new byte[] { (byte) 0x0FF, (byte) 0x0FF, (byte) 0x0FF, (byte) 0x0FF };
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public void setIOPortDoubleWord(int portAddress, byte[] dataDoubleWord)
            throws ModuleException {
        logger.log(Level.WARNING, "[" + super.getType() + "]"
                + " OUT command (double word) to port "
                + Integer.toHexString(portAddress).toUpperCase()
                + " received. No action taken.");
    }
}
