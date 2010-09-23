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
import dioscuri.exception.ModuleUnknownPort;
import dioscuri.exception.ModuleWriteOnlyPortException;
import dioscuri.interfaces.Addressable;
import dioscuri.interfaces.Module;
import dioscuri.interfaces.Updateable;
import dioscuri.module.ModuleCPU;
import dioscuri.module.ModuleClock;
import dioscuri.module.ModuleMemory;
import dioscuri.module.ModuleMotherboard;

/**
 * An implementation of a motherboard module. This module is responsible for
 * allowing devices to communicate with the CPU and vice versa.
 * 
 * @see dioscuri.module.AbstractModule
 * 
 *      Metadata module ********************************************
 *      general.type : motherboard general.name : General x86 motherboard
 *      general.architecture : Von Neumann general.description : imitates an x86
 *      motherboard including I/O address space, ... general.creator : Tessella
 *      Support Services, Koninklijke Bibliotheek, Nationaal Archief of the
 *      Netherlands general.version : 1.0 general.keywords : motherboard,
 *      mainboard, ioports, ... general.relations : cpu, devices
 *      general.yearOfIntroduction : general.yearOfEnding : general.ancestor :
 *      general.successor : motherboard.ioAddressSpaceSize : 65536 bytes
 *      motherboard.architecture : ISA, EISA, PCI
 * 
 *      Rule: This module should always be initialised before I/O devices.
 * 
 */
public class Motherboard extends ModuleMotherboard {

    // Relations
    private Emulator emu;

    // Toggles
    private boolean A20Enabled; // A20 address line: True = memory wrapping turned off

    // Configuration parameters
    protected int ioSpaceSize;

    // I/O address space containing references to devices
    public Addressable[] ioAddressSpace; // Using signed bytes as both signed/unsigned

    // Logging
    private static final Logger logger = Logger.getLogger(Motherboard.class.getName());

    // Memory size
    public final static int IOSPACE_ISA_SIZE = 1024; // ISA: 1024 (2^10) spaces,
                                                     // 0x000 - 0x3FF
    public final static int IOSPACE_EISA_SIZE = 65536; // EISA: 65536 (2^16)
                                                       // spaces, 0x0000 -
                                                       // 0xFFFF
    public final static int SYSTEM_CONTROL_PORT_A = 0x92; // A20 port, amongst
                                                          // others

    /**
     * Class constructor
     * 
     * @param owner
     */
    public Motherboard(Emulator owner) {
        emu = owner;

        // Initialise configuration parameters
        // TODO: parameters should be defined based on configuration in the ESD
        ioSpaceSize = IOSPACE_EISA_SIZE;

        // Create new empty I/O address space
        ioAddressSpace = new Addressable[ioSpaceSize];

        logger.log(Level.INFO, "[" + super.getType() + "]" + getClass().getName() + " -> AbstractModule created successfully.");
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.AbstractModule
     */
    @Override
    public boolean reset() {
        // FIXME: Reset I/O address space: set all ports to null
        // Doing this in 32-bit mode resets all ports _after_ they have been
        // set...
        // for (int port = 0; port < ioAddressSpace.length; port++)
        // {
        // ioAddressSpace[port] = null;
        // }

        // Disable memory wrapping for BIOS mem check
        A20Enabled = true;

        logger.log(Level.INFO, "[" + super.getType() + "]"
                + "  AbstractModule has been reset.");

        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.AbstractModule
     */
    @Override
    public String getDump() {
        String dump = "";
        String ret = "\r\n";
        String tab = "\t";

        // Output I/O address space to dump
        dump = "Dump of I/O address space:" + ret;

        // To keep a limit on the string size, only display ports 0 - 0x3FF
        for (int port = 0; port < IOSPACE_ISA_SIZE; port++) {
            // Only print I/O space info when port is used
            if (ioAddressSpace[port] != null
                    && ioAddressSpace[port].getType() != Module.Type.DUMMY) { //.equalsIgnoreCase("dummy"))) {
                dump += "0x"
                        + Integer.toHexString(0x10000 | port & 0x0FFFF)
                                .substring(1).toUpperCase() + tab + ": "
                        + ioAddressSpace[port].getType().toString() + " ("
                        + ioAddressSpace[port].getType() + ")" + ret;
            }
        }
        return dump;
    }

    /**
     * Registers a clock to motherboard
     * 
     * @return boolean true if registration is successfully, false otherwise
     */
    public boolean registerClock(ModuleClock clock) {
        super.setConnection(clock);
        return true;
    }

    /**
     * Requests a timer for given device at clock
     * 
     * @param updateInterval
     * @return boolean true if registration is successfully, false otherwise
     */
    public boolean requestTimer(Updateable device, int updateInterval, boolean continuous) {

        ModuleClock clock = (ModuleClock)super.getConnection(Module.Type.CLOCK);

        // Check if clock exists
        if (clock != null) {
            // Register device to clock (and assign timer to it)
            return clock.registerDevice(device, updateInterval, continuous);
        }
        return false;
    }

    /**
     * Set a timer to start/stop running
     * 
     * @param activeState
     * @return boolean true if timer is reset successfully, false otherwise
     */
    public boolean setTimerActiveState(Updateable device, boolean activeState) {
        // Check if clock exists
        ModuleClock clock = (ModuleClock)super.getConnection(Module.Type.CLOCK);

        if (clock != null) {
            // Set device's timer to requested state
            return clock.setTimerActiveState(device, activeState);
        }
        return false;
    }

    /**
     * Resets the timer of device (if any)
     * 
     * @return boolean true if reset is successfully, false otherwise
     */
    public boolean resetTimer(Updateable device, int updateInterval) {
        // Check if clock exists
        ModuleClock clock = (ModuleClock)super.getConnection(Module.Type.CLOCK);

        if (clock != null) {
            return clock.resetTimer(device, updateInterval);
        }
        return false;
    }

    /**
     * Set I/O address port to given device
     * 
     * @return boolean true if data is set successfully, false otherwise
     */
    public boolean setIOPort(int portAddress, Addressable device) {
        // check if port is already in use
        if (ioAddressSpace[portAddress] == null) {
            ioAddressSpace[portAddress] = device;
            return true;
        }

        // Print warning
        logger.log(Level.WARNING, "[" + super.getType() + "]"
                + "  I/O address space: Registration for " + device.getType()
                + " failed. I/O port address already registered by "
                + ioAddressSpace[portAddress].getType());
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public byte getIOPortByte(int portAddress) throws ModuleException {
        // check if port is available
        if (ioAddressSpace[portAddress] != null) {
            try {
                // Return data at appropriate portnumber (could throw an
                // exception in protected mode)
                return ioAddressSpace[portAddress].getIOPortByte(portAddress);
            } catch (ModuleUnknownPort e1) {
                // Print warning
                logger
                        .log(Level.WARNING, "["
                                + super.getType()
                                + "] Unknown I/O port requested (0x"
                                + Integer.toHexString(portAddress)
                                        .toUpperCase() + ").");
                throw new ModuleException("Unknown I/O port requested (0x"
                        + Integer.toHexString(portAddress).toUpperCase() + ").");
            } catch (ModuleWriteOnlyPortException e2) {
                logger.log(Level.WARNING, "[" + super.getType()
                        + "] Writing to I/O port not allowed.");
                throw new ModuleException("I/O port is read-only.");
            }
        }
        logger.log(Level.INFO, "[" + super.getType() + "] Requested I/O port 0x"
                + Integer.toHexString(portAddress)
                + " (getByte) is not in use.");
        // FIXME: Add proper error handling for unknown I/O ports, assuming 0xFF
        // is returned as default
        return (byte) 0xFF;
        // throw new ModuleException("Requested I/O port " +
        // Integer.toHexString(portAddress) + " (getByte) is not used.");
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public void setIOPortByte(int portAddress, byte dataByte)
            throws ModuleException {
        // Check for Bochs BIOS ports first:
        if (portAddress == 0x400 || portAddress == 0x401) {
            logger.log(Level.SEVERE, "[" + super.getType() + "]"
                    + " Problem occurred in ROM BIOS at line: " + dataByte);
            return;
        } else if (portAddress == 0x402 || portAddress == 0x403) {
            try {
                logger.log(Level.WARNING,
                        "["
                                + super.getType()
                                + "]"
                                + " I/O port (0x"
                                + Integer.toHexString(portAddress)
                                        .toUpperCase()
                                + "): "
                                + new String(new byte[] { (byte) dataByte },
                                        "US-ASCII"));
            } catch (Exception e) {
                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + " I/O port (0x"
                        + Integer.toHexString(portAddress).toUpperCase()
                        + "): " + new String(new byte[] { (byte) dataByte }));
            }
            return;
        } else if (portAddress == 0x8900) {
            logger.log(Level.WARNING, "[" + super.getType() + "]" + " I/O port (0x"
                    + Integer.toHexString(portAddress).toUpperCase()
                    + "): attempting to shutdown.");
            return;
        }

        // Check if port is available/registered
        if (ioAddressSpace[portAddress] != null) {
            try {
                // Set data at appropriate portnumber (could throw an exception
                // in protected mode)
                ioAddressSpace[portAddress]
                        .setIOPortByte(portAddress, dataByte);

            } catch (ModuleUnknownPort e) {
                // Print warning
                logger
                        .log(Level.INFO, "["
                                + super.getType()
                                + "]"
                                + "  Unknown I/O port requested (0x"
                                + Integer.toHexString(portAddress)
                                        .toUpperCase() + ").");
                throw new ModuleException("Unknown I/O port requested (0x"
                        + Integer.toHexString(portAddress).toUpperCase() + ").");
            }
        } else {
            logger.log(Level.INFO, "[" + super.getType() + "]"
                    + "  Requested I/O port (0x"
                    + Integer.toHexString(portAddress).toUpperCase()
                    + ", setByte) is not available/registered.");
            // TODO: Add proper error handling for unknown I/O ports, no value
            // TODO: is passed forward
            // TODO: throw new ModuleException("Requested I/O port [" + portAddress +
            // TODO: "] (setByte) is not available.");
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public byte[] getIOPortWord(int portAddress) throws ModuleException {
        // check if port range is available
        if (ioAddressSpace[portAddress] != null
                && ioAddressSpace[portAddress + 1] != null) {
            try {
                // Return data at appropriate portnumber (could throw an
                // exception in protected mode)
                return ioAddressSpace[portAddress].getIOPortWord(portAddress);
            } catch (ModuleUnknownPort e1) {
                // Print warning
                logger
                        .log(Level.WARNING, "["
                                + super.getType()
                                + "] Unknown I/O port requested (0x"
                                + Integer.toHexString(portAddress)
                                        .toUpperCase() + ").");
                throw new ModuleException("Unknown I/O port requested (0x"
                        + Integer.toHexString(portAddress).toUpperCase() + ").");
            } catch (ModuleWriteOnlyPortException e2) {
                logger.log(Level.WARNING, "[" + super.getType()
                        + "] Writing to I/O port not allowed.");
                throw new ModuleException("I/O port is read-only.");
            }
        }
        logger.log(Level.WARNING, "[" + super.getType() + "] Requested I/O port 0x"
                + Integer.toHexString(portAddress)
                + " (getWord) is not in use.");
        // FIXME: Add proper error handling for unknown I/O ports, assuming 0xFF
        // is returned as default
        return new byte[] { (byte) 0xFF, (byte) 0xFF };
        // throw new ModuleException("Requested I/O port range [" + portAddress
        // + "] (getWord) is not in use.");
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public void setIOPortWord(int portAddress, byte[] dataWord)
            throws ModuleException {
        // check if port range is available
        if (ioAddressSpace[portAddress] != null
                && ioAddressSpace[portAddress + 1] != null) {
            try {
                // Set data at appropriate portnumber (could throw an exception
                // in protected mode)
                ioAddressSpace[portAddress]
                        .setIOPortWord(portAddress, dataWord);
            } catch (ModuleUnknownPort e) {
                // Print warning
                logger
                        .log(Level.WARNING, "["
                                + super.getType()
                                + "]"
                                + "  Unknown I/O port requested (0x"
                                + Integer.toHexString(portAddress)
                                        .toUpperCase() + ").");
                throw new ModuleException("Unknown I/O port requested (0x"
                        + Integer.toHexString(portAddress).toUpperCase() + ").");
            }
        } else {
            // FIXME: Add proper error handling for unknown I/O ports, no value
            // is passed forward
            // throw new ModuleException("Requested I/O port range [" +
            // portAddress + "] (setWord) is not available.");
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public byte[] getIOPortDoubleWord(int portAddress) throws ModuleException {
        // check if port range is available
        if (ioAddressSpace[portAddress] != null
                && ioAddressSpace[portAddress + 1] != null
                && ioAddressSpace[portAddress + 2] != null
                && ioAddressSpace[portAddress + 3] != null) {
            try {
                // Return data at appropriate portnumber (could throw an
                // exception in protected mode)
                return ioAddressSpace[portAddress]
                        .getIOPortDoubleWord(portAddress);
            } catch (ModuleUnknownPort e1) {
                // Print warning
                logger
                        .log(Level.WARNING, "["
                                + super.getType()
                                + "]"
                                + "  Unknown I/O port requested (0x"
                                + Integer.toHexString(portAddress)
                                        .toUpperCase() + ").");
                throw new ModuleException("Unknown I/O port requested (0x"
                        + Integer.toHexString(portAddress).toUpperCase() + ").");
            } catch (ModuleWriteOnlyPortException e2) {
                logger.log(Level.WARNING, "[" + super.getType()
                        + "] Writing to I/O port not allowed.");
                throw new ModuleException("I/O port is read-only.");
            }
        }
        logger.log(Level.WARNING, "[" + super.getType() + "] Requested I/O port 0x"
                + Integer.toHexString(portAddress)
                + " (getDoubleWord) is not in use.");
        // FIXME: Add proper error handling for unknown I/O ports, assuming 0xFF
        // is returned as default
        return new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
        // throw new ModuleException("Requested I/O port range [" + portAddress
        // + "] (getDoubleWord) is not available.");
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public void setIOPortDoubleWord(int portAddress, byte[] dataDoubleWord)
            throws ModuleException {
        // check if port range is available
        if (ioAddressSpace[portAddress] != null
                && ioAddressSpace[portAddress + 1] != null
                && ioAddressSpace[portAddress + 2] != null
                && ioAddressSpace[portAddress + 3] != null) {
            try {
                // Set data at appropriate portnumber (could throw an exception
                // in protected mode)
                ioAddressSpace[portAddress].setIOPortDoubleWord(portAddress,
                        dataDoubleWord);

            } catch (ModuleUnknownPort e) {
                // Print warning
                logger
                        .log(Level.WARNING, "["
                                + super.getType()
                                + "]"
                                + "  Unknown I/O port requested (0x"
                                + Integer.toHexString(portAddress)
                                        .toUpperCase() + ").");
                throw new ModuleException("Unknown I/O port requested (0x"
                        + Integer.toHexString(portAddress).toUpperCase() + ").");
            }
        } else {
            // FIXME: Add proper error handling for unknown I/O ports, no value
            // is passed forward
            // throw new ModuleException("Requested I/O port range [" +
            // portAddress + "] (setDoubleWord) is not available.");
        }

    }

    /**
     * Return A20 address line status
     * 
     * @return boolean true if A20 is enabled, false otherwise
     */
    public boolean getA20() {
        // Return status of A20 address line
        return A20Enabled;
    }

    /**
     * Set A20 address line status
     * 
     */
    public void setA20(boolean a20) {
        // Set status of A20 address line
        // False = memory wrapping turned off
        A20Enabled = a20;
        if (emu.isCpu32bit()) {
            logger
                    .log(
                            Level.WARNING,
                            "["
                                    + super.getType()
                                    + "]"
                                    + " Attempting to set memory A20 line in 32-bit mode (unsupported)");
        }
        else {
            ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
            memory.setA20AddressLine(a20);
        }
    }

    /**
     * Retrieve current number of instruction (instructions executed so far)
     * 
     * @return long containing number of instructions
     * 
     */
    public long getCurrentInstructionNumber() {

        if (emu.isCpu32bit()) {
            logger
                    .log(
                            Level.WARNING,
                            "["
                                    + super.getType()
                                    + "]"
                                    + "Attempting to get CPU instruction number in 32-bit mode (unsupported)");
            return 0x1;
        }
        else {
            ModuleCPU cpu = (ModuleCPU)super.getConnection(Module.Type.CPU);
            return cpu.getCurrentInstructionNumber();
        }
    }
}
