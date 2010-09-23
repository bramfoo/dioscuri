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

package dioscuri.module.rtc;

import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import dioscuri.Emulator;
import dioscuri.exception.ModuleException;
import dioscuri.exception.ModuleUnknownPort;
import dioscuri.exception.ModuleWriteOnlyPortException;
import dioscuri.interfaces.Module;
import dioscuri.module.ModuleMotherboard;
import dioscuri.module.ModulePIC;
import dioscuri.module.ModuleRTC;

/**
 * An implementation of a Real Time module. This component takes care of
 * updating the date and time settings of the computer.
 * 
 * @see dioscuri.module.AbstractModule
 * 
 *      Metadata module ********************************************
 *      general.type : rtc general.name : Real Time Clock (RTC)
 *      general.architecture : Von Neumann general.description : Implements an
 *      Intel RTC (part of Intel 82801DB I/O Controller Hub 4, ICH4) and is
 *      compatible with the Motorola MC146818. Includes 256 bytes of CMOS
 *      memory. general.creator : Tessella Support Services, Koninklijke
 *      Bibliotheek, Nationaal Archief of the Netherlands general.version : 1.0
 *      general.keywords : rtc, clock, date, time, cmos general.relations :
 *      motherboard, pit general.yearOfIntroduction : general.yearOfEnding :
 *      general.ancestor : general.successor : rtc.ramsize : 256 bytes
 * 
 */
@SuppressWarnings("unused")
public class RTC extends ModuleRTC {

    // Instance
    CMOS cmos;

    private boolean systemTime; // TRUE = real calender values from host
                                // machine, FALSE = user-defined

    // Logging
    private static final Logger logger = Logger.getLogger(RTC.class.getName());

    // Register settings
    private boolean disableNMI; // Set by bit 7 of port data. Uses inverse
                                // logic: enable (0) / disable (1)

    // IRQ number
    private int irqNumber;
    protected int lookupRegister; // Register set by OUT instruction for
                                  // retrieval of data for next IN

    // Constants
    private final static int OUT_PORT = 0x70; // Write-only port
    private final static int IN_PORT = 0x71; // Read/write port

    // Constructor

    /**
     * Class constructor
     * 
     * @param owner
     */
    public RTC(Emulator owner) {

        cmos = new CMOS();

        // Initialise IRQ number
        irqNumber = -1;

        // Initialise realTime variable
        // TRUE = real calender values from host machine, FALSE = user-defined
        // TODO: this toggle can be used later for user defined time and date
        // via configuration file
        systemTime = false;

        logger.log(Level.INFO, "[" + super.getType() + "] "
                + " AbstractModule created successfully.");
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.AbstractModule
     */
    @Override
    public boolean reset() {

        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Module.Type.MOTHERBOARD);
        ModulePIC pic = (ModulePIC)super.getConnection(Module.Type.PIC);

        // Set current system time in CMOS, stored in BCD format
        // Need to cast calender value to same hex digits (e.g. 48d -> 48h)
        // This is done by taking the calender value as hex and casting it to
        // decimal

        // Reset the CMOS memory array; pass whether to use system time or
        // custom time
        cmos.reset(systemTime);
        if (systemTime) {
            logger.log(Level.INFO, "[" + super.getType()
                    + "] CMOS clock set to host machine's values: "
                    + cmos.getClockValue());
        } else {
            logger.log(Level.INFO, "[" + super.getType()
                    + "] CMOS clock set to user-defined values: "
                    + cmos.getClockValue());
        }

        // Register I/O ports 0x70, 0x71 in I/O address space
        motherboard.setIOPort(OUT_PORT, this);
        motherboard.setIOPort(IN_PORT, this);
        lookupRegister = 0;

        // Register IRQ number
        irqNumber = pic.requestIRQNumber(this);
        if (irqNumber > -1) {
            logger.log(Level.CONFIG, "[" + super.getType()
                    + "] IRQ number set to: " + irqNumber);
        } else {
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] Request of IRQ number failed.");
        }

        logger.log(Level.INFO, "[" + super.getType() + "] AbstractModule has been reset.");
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.AbstractModule
     */
    @Override
    public String getDump() {
        String cmos_Dump = "Dump of RTC/CMOS registers 0x00 - 0x2F and checksum regs:\n";
        cmos_Dump += "Reg\tVal(hex)\tVal(BCD)\n";

        // Display CMOS registers 0x00 - 0x2F in HEX and BCD
        // Display output with leading zeros
        DecimalFormat twoDigits = new DecimalFormat("00");

        for (int reg = 0; reg <= 0x2F; reg++) {
            cmos_Dump += "0x"
                    + Integer.toHexString(0x100 | reg & 0xFF).substring(1)
                            .toUpperCase();
            cmos_Dump += "\t";
            cmos_Dump += "["
                    + Integer.toHexString(0x100 | cmos.ram[reg] & 0xFF)
                            .substring(1).toUpperCase() + "]";
            cmos_Dump += "\t";
            cmos_Dump += "[" + twoDigits.format(cmos.ram[reg]) + "]";

            cmos_Dump += "\n";
        }

        return cmos_Dump;
    }

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
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public byte getIOPortByte(int portAddress) throws ModuleUnknownPort,
            ModuleWriteOnlyPortException {
        logger.log(Level.CONFIG, "[" + super.getType() + "]" + " IO read from "
                + portAddress);

        switch (portAddress) {
        case (OUT_PORT):
            // Undefined
            logger.log(Level.INFO, "[" + super.getType() + "]"
                    + " IN command (byte) from port 0x"
                    + Integer.toHexString(portAddress).toUpperCase()
                    + "; Returned 0xFF");
            // throw new ModuleWriteOnlyPortException(super.getType() + " -> port "
            // + Integer.toHexString(portAddress).toUpperCase() +
            // " is write-only and cannot be read");
            return (byte) 0xFF;

        case (IN_PORT):
            // Return data from previously indicated CMOS register
            logger.log(Level.INFO, "["
                    + super.getType()
                    + "]"
                    + " IN command (byte) from register 0x"
                    + Integer.toHexString(lookupRegister).toUpperCase()
                    + ": 0x"
                    + Integer.toHexString(cmos.ram[lookupRegister])
                            .toUpperCase());

            if (lookupRegister == CMOS.STATUS_REGISTER_C) {
                // Clear register C if read occurs
                cmos.ram[CMOS.STATUS_REGISTER_C] = 0x00;
                ModulePIC pic = (ModulePIC)super.getConnection(Module.Type.PIC);
                pic.clearIRQ(irqNumber);
            }
            return cmos.ram[lookupRegister];

        default:
            throw new ModuleUnknownPort("[" + super.getType()
                    + "] Unknown I/O port requested");
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public void setIOPortByte(int portAddress, byte data)
            throws ModuleUnknownPort {
        logger.log(Level.CONFIG, "[" + super.getType() + "]" + " IO write to "
                + portAddress + " = " + data);

        // Check bit 7 of data for enabling/disabling of NMI:
        disableNMI = (data & 0x80) == 1 ? true : false;

        switch (portAddress) {
        case (OUT_PORT):
            // 'data' sets the register for the next port instruction;
            // Limited range to 127 to fit within CMOS array size
            lookupRegister = data & 0x7F;
            logger.log(Level.INFO, "[" + super.getType() + "]"
                    + " OUT command (byte) to port 0x"
                    + Integer.toHexString(portAddress).toUpperCase()
                    + ": lookup byte set to 0x"
                    + Integer.toHexString(lookupRegister).toUpperCase());
            return;

        case (IN_PORT):
            // 'data' is to be written to a previously set register in CMOS
            if (lookupRegister == 0x0C || lookupRegister == 0x0D) {
                // These registers are read-only, so do not write data to them
            } else {
                cmos.ram[lookupRegister] = data;
            }
            logger.log(Level.INFO, "[" + super.getType() + "]"
                    + " OUT command (byte) to port 0x"
                    + Integer.toHexString(portAddress).toUpperCase() + " [0x"
                    + Integer.toHexString(lookupRegister).toUpperCase()
                    + "] set to: 0x" + Integer.toHexString(data).toUpperCase());
            return;

        default:
            throw new ModuleUnknownPort("[" + super.getType()
                    + "] Unknown I/O port requested");
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public byte[] getIOPortWord(int portAddress) throws ModuleException,
            ModuleWriteOnlyPortException {
        logger.log(Level.WARNING, "[" + super.getType()
                + "] IN command (word) from port "
                + Integer.toHexString(portAddress).toUpperCase() + " received");
        logger.log(Level.WARNING, "[" + super.getType()
                + "] Returned default value 0xFFFF to AX");

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
        logger.log(Level.WARNING, "[" + super.getType()
                + "] OUT command (word) to port "
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
        logger.log(Level.WARNING, "[" + super.getType()
                + "] IN command (double word) from port "
                + Integer.toHexString(portAddress).toUpperCase() + " received");
        logger.log(Level.WARNING, "[" + super.getType()
                + "] Returned default value 0xFFFFFFFF to eAX");

        // Return dummy value 0xFFFFFFFF
        return new byte[] { (byte) 0x0FF, (byte) 0x0FF, (byte) 0x0FF,
                (byte) 0x0FF };
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public void setIOPortDoubleWord(int portAddress, byte[] dataDoubleWord)
            throws ModuleException {
        logger.log(Level.WARNING, "[" + super.getType()
                + "] OUT command (double word) to port "
                + Integer.toHexString(portAddress).toUpperCase()
                + " received. No action taken.");
    }

    /**
     * Return requested CMOS register
     * 
     * @return byte containing value of register
     */
    public byte getCMOSRegister(int register) {
        logger.log(Level.CONFIG, "[" + super.getType()
                + "] Returned CMOS register " + register + ": 0x"
                + Integer.toHexString(cmos.ram[register]).toUpperCase());

        // Return the value of given register
        return cmos.ram[register];
    }

    /**
     * Set given CMOS register with value
     * 
     */
    public void setCMOSRegister(int register, byte value) {
        logger.log(Level.CONFIG, "[" + super.getType() + "] Set CMOS register "
                + register + ": 0x" + Integer.toHexString(value).toUpperCase());

        // Store the value in given register
        cmos.ram[register] = value;
    }

    /**
     * Update clock Increment the clock value
     */
    public void updateClock() {
        // Update with one second
        cmos.setClockValue(1);
        logger.log(Level.INFO, "[" + super.getType() + "] CMOS clock updated: "
                + cmos.getClockValue());
    }
}
