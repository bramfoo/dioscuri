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
import dioscuri.module.Module;
import dioscuri.module.ModuleDevice;
import dioscuri.module.ModuleMotherboard;

/**
 * This class is a dummy for a peripheral device.
 * 
 * Contains all standard methods for a device.
 * 
 * Note: init this module only after all other devices, because this class
 * claims all available I/O address space that is left
 * 
 * @see Module
 * 
 */
@SuppressWarnings("unused")
public class DeviceDummy extends ModuleDevice {
    // Attributes
    private boolean isObserved;
    private boolean debugMode;

    // Relations
    private Emulator emu;
    private String[] moduleConnections = new String[] { "motherboard" };
    private ModuleMotherboard motherboard;

    // Logging
    private static final Logger logger = Logger.getLogger(DeviceDummy.class.getName());

    // Constants

    // Module specifics
    public final static int MODULE_ID = 5000;
    public final static String MODULE_TYPE = "dummy device";
    public final static String MODULE_NAME = "Dummy Device";

    // Constructor

    /**
     * Class constructor
     * 
     * @param owner
     */
    public DeviceDummy(Emulator owner) {
        super(Type.DUMMY);
        emu = owner;
    }

    // ******************************************************************************
    // Module Methods

    /**
     * Returns the name of the module
     * 
     * @return string containing the name of module
     * @see Module
     */
    public String getName() {
        return MODULE_NAME;
    }

    /**
     * Sets up a connection with another module
     * 
     * @param mod
     *            Module that is to be connected to this class
     * 
     * @return true if connection has been established successfully, false
     *         otherwise
     * 
     * @see Module
     */
    public boolean setConnection(Module mod) {
        // Set connection for motherboard
        if (mod.getType() == Type.MOTHERBOARD) { //.equalsIgnoreCase("motherboard")) {
            this.motherboard = (ModuleMotherboard) mod;
            return true;
        }

        // No connection has been established
        return false;
    }

    /**
     * Reset all parameters of module
     * 
     * @return boolean true if module has been reset successfully, false
     *         otherwise
     */
    public boolean reset() {
        // Register I/O ports in I/O address space

        // Claim all other I/O space
        for (int port = 0x0000; port < motherboard.ioSpaceSize; port++) {
            motherboard.setIOPort(port, this);
        }

        return true;
    }

    /**
     * Starts the module
     * 
     * @see Module
     */
    public void start() {
        // Nothing to start
    }

    /**
     * Stops the module
     * 
     * @see Module
     */
    public void stop() {
        // Nothing to stop
    }

    /**
     * Returns the status of observed toggle
     * 
     * @return state of observed toggle
     * 
     * @see Module
     */
    public boolean isObserved() {
        return isObserved;
    }

    /**
     * Sets the observed toggle
     * 
     * @param status
     * 
     * @see Module
     */
    public void setObserved(boolean status) {
        isObserved = status;
    }

    /**
     * Returns the status of the debug mode toggle
     * 
     * @return state of debug mode toggle
     * 
     * @see Module
     */
    public boolean getDebugMode() {
        return debugMode;
    }

    /**
     * Sets the debug mode toggle
     * 
     * @param status
     * 
     * @see Module
     */
    public void setDebugMode(boolean status) {
        debugMode = status;
    }

    /**
     * Returns data from this module
     * 
     * @param requester
     * @return byte[] with data
     * 
     * @see Module
     */
    public byte[] getData(Module requester) {
        return null;
    }

    /**
     * Set data for this module
     * 
     * @param sender
     * @return boolean true if successful, false otherwise
     * 
     * @see Module
     */
    public boolean setData(byte[] data, Module sender) {
        return false;
    }

    /**
     * Set String[] data for this module
     * 
     * @param sender
     * @return boolean true is successful, false otherwise
     * 
     * @see Module
     */
    public boolean setData(String[] data, Module sender) {
        return false;
    }

    /**
     * Returns a dump of this module
     * 
     * @return string
     * 
     * @see Module
     */
    public String getDump() {
        // TODO Auto-generated method stub
        return null;
    }

    // ******************************************************************************
    // ModuleDevice Methods

    /**
     * Retrieve the interval between subsequent updates
     * 
     * @return int interval in microseconds
     */
    public int getUpdateInterval() {
        return -1;
    }

    /**
     * Defines the interval between subsequent updates
     * 
     */
    public void setUpdateInterval(int interval) {
    }

    /**
     * Update device
     * 
     */
    public void update() {
    }

    /**
     * Return a byte from I/O address space at given port
     * 
     * @return byte containing the data at given I/O address port
     * @throws ModuleException
     *             , ModuleWriteOnlyPortException
     */
    public byte getIOPortByte(int portAddress) throws ModuleException {
        if (portAddress == 0x92) {
            logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]"
                    + " IN command (byte) to port "
                    + Integer.toHexString(portAddress).toUpperCase()
                    + " received");
            logger.log(Level.INFO, "[" + MODULE_TYPE + "]"
                    + " Returned A20 value: "
                    + Boolean.toString(motherboard.getA20()));
            return (byte) (motherboard.getA20() ? (1 << 1) : 0);
        }

        logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]"
                + " IN command (byte) to port "
                + Integer.toHexString(portAddress).toUpperCase() + " received");
        logger.log(Level.INFO, "[" + MODULE_TYPE + "]"
                + " Returned default value 0xFF to AL");

        // Return dummy value 0xFF
        return (byte) 0x0FF;
    }

    /**
     * Set a byte in I/O address space at given port
     * 
     * @throws ModuleException
     *             , ModuleWriteOnlyPortException
     */
    public void setIOPortByte(int portAddress, byte data)
            throws ModuleException {
        if (portAddress == 0x92) {
            logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]"
                    + " OUT command (byte) to port "
                    + Integer.toHexString(portAddress).toUpperCase()
                    + " received");
            logger.log(Level.INFO, "[" + MODULE_TYPE + "]" + " Set A20 value: "
                    + data);
            motherboard.setA20((data & 0x02) == 2 ? true : false);
            return;
        }
        logger.log(Level.WARNING, "[" + MODULE_TYPE + "]"
                + " OUT command (byte) to port "
                + Integer.toHexString(portAddress).toUpperCase()
                + " received. No action taken.");

        // Do nothing and just return okay
        return;
    }

    public byte[] getIOPortWord(int portAddress) throws ModuleException,
            ModuleWriteOnlyPortException {
        if (portAddress == 0x92) {
            logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]"
                    + " IN command (word) to port "
                    + Integer.toHexString(portAddress).toUpperCase()
                    + " received");
            logger.log(Level.INFO, "[" + MODULE_TYPE + "]"
                    + " Returned A20 value");
            return (motherboard.getA20() ? new byte[] { 0x00, (byte) (1 << 1) }
                    : new byte[] { 0x00, 0x00 });
        }
        logger.log(Level.WARNING, "[" + MODULE_TYPE + "]"
                + " IN command (word) to port "
                + Integer.toHexString(portAddress).toUpperCase() + " received");
        logger.log(Level.WARNING, "[" + MODULE_TYPE + "]"
                + " Returned default value 0xFFFF to AX");

        // Return dummy value 0xFFFF
        return new byte[] { (byte) 0x0FF, (byte) 0x0FF };
    }

    public void setIOPortWord(int portAddress, byte[] dataWord)
            throws ModuleException {
        if (portAddress == 0x92) {
            logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]"
                    + " OUT command (word) to port "
                    + Integer.toHexString(portAddress).toUpperCase()
                    + " received");
            logger.log(Level.INFO, "[" + MODULE_TYPE + "]" + " Set A20 value");
            motherboard.setA20((dataWord[1] & 0x02) == 2 ? true : false);
            return;
        }

        logger.log(Level.WARNING, "[" + MODULE_TYPE + "]"
                + " OUT command (word) to port "
                + Integer.toHexString(portAddress).toUpperCase()
                + " received. No action taken.");

        // Do nothing and just return okay
        return;
    }

    public byte[] getIOPortDoubleWord(int portAddress) throws ModuleException,
            ModuleWriteOnlyPortException {
        logger.log(Level.WARNING, "[" + MODULE_TYPE + "]"
                + " IN command (double word) to port "
                + Integer.toHexString(portAddress).toUpperCase() + " received");
        logger.log(Level.WARNING, "[" + MODULE_TYPE + "]"
                + " Returned default value 0xFFFFFFFF to eAX");

        // Return dummy value 0xFFFFFFFF
        return new byte[] { (byte) 0x0FF, (byte) 0x0FF, (byte) 0x0FF,
                (byte) 0x0FF };
    }

    public void setIOPortDoubleWord(int portAddress, byte[] dataDoubleWord)
            throws ModuleException {
        logger.log(Level.WARNING, "[" + MODULE_TYPE + "]"
                + " OUT command (double word) to port "
                + Integer.toHexString(portAddress).toUpperCase()
                + " received. No action taken.");

        // Do nothing and just return okay
        return;
    }

}
