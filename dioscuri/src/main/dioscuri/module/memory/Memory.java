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

package dioscuri.module.memory;

import dioscuri.Emulator;
import dioscuri.exception.ModuleException;
import dioscuri.interfaces.Module;
import dioscuri.module.ModuleCPU;
import dioscuri.module.ModuleMemory;
import dioscuri.module.ModuleMotherboard;
import dioscuri.module.ModuleVideo;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of a hardware memory module.
 * <p/>
 * Contains an array of 2^20 integers, offering 1MB of RAM.
 *
 * @see dioscuri.module.AbstractModule
 *      <p/>
 *      Metadata module ********************************************
 *      general.type : memory general.name : 1 Megabyte Random Access Memory
 *      general.architecture : Von Neumann general.description : General
 *      implementation of 1 MB of flat RAM. general.creator : Tessella Support
 *      Services, Koninklijke Bibliotheek, Nationaal Archief of the Netherlands
 *      general.version : 1.0 general.keywords : memory, RAM, 1MB, A20 address
 *      line general.relations : cpu, mainboard general.yearOfIntroduction :
 *      general.yearOfEnding : general.ancestor : general.successor :
 *      memory.size : 1 MB
 *      <p/>
 *      Notes: none
 */
public class Memory extends ModuleMemory {

    // Logging
    private static final Logger logger = Logger.getLogger(Memory.class.getName());

    // Random Access Memory (RAM)
    public byte[] ram; // Using signed bytes as both signed/unsigned
    static protected long A20mask; // Mask used to set/clear 20th bit in memory
    // addresses

    // Constants
    private final static int BYTES_IN_MB = 1048576;
    // Memory size
    private int ramSize = 1 * BYTES_IN_MB; // initial value defined in bytes (1
    // Megabyte, 2^20)

    // debugging functionality
    private boolean watchValue;
    private int watchAddress;

    private ModuleVideo video = null;
    private ModuleCPU cpu = null;
    private ModuleMotherboard motherboard = null;

    // Constructors

    /**
     * Class constructor
     *
     * @param owner
     */
    public Memory(Emulator owner) {

        // Create new empty memory
        ram = new byte[this.ramSize];

        // Debugging functionality
        watchValue = false;
        watchAddress = -1;

        logger.log(Level.INFO, "[" + super.getType() + "] " + getClass().getName()
                + " AbstractModule created successfully.");
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.AbstractModule
     */
    @Override
    public boolean reset() {
        // Reset RAM: set all memory to zero
        Arrays.fill(ram, (byte) 0);

        // Fill BIOS RAM area with 0xFF (mimicing Bochs)
        Arrays.fill(ram, 0xC0000, ram.length, (byte) 0xFF);

        // Initialise A20 address bit to non-wrap (0xFFFF FFFF)
        setA20AddressLine(true);

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
        String dump = "";
        String ret = "\r\n";
        String tab = "\t";
        String space = " ";

        dump = "Memory dump of first 3200 bytes as stored in RAM:" + ret;

        // Output first 1600 RAM bytes to dump 65526
        for (int row = 0; row < 200; row++) {
            dump += row + tab + ": ";
            for (int col = 0; col < 16; col++) {
                dump += Integer.toHexString(
                        0x100 | ram[(row * 16) + col] & 0xFF).substring(1)
                        .toUpperCase()
                        + space;
            }
            dump += ret;
        }
        return dump;
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.ModuleMemory
     */
    @Override
    public byte getByte(int address) {
        if(video == null) {
            video = (ModuleVideo) super.getConnection(Module.Type.VIDEO);
            cpu = (ModuleCPU) super.getConnection(Module.Type.CPU);
            motherboard = (ModuleMotherboard) super.getConnection(Module.Type.MOTHERBOARD);
        }
        // Mask 20th address bit
        address &= A20mask;

        // Check if address is in range
        try {
            // Watch certain memory address
            if (watchValue && address == watchAddress) {
                logger
                        .log(Level.CONFIG, "["
                                + super.getType()
                                + "] "
                                + cpu.getRegisterHex(0)
                                + ":"
                                + cpu.getRegisterHex(1)
                                + " Watched BYTE at address "
                                + watchAddress
                                + " is read: ["
                                + Integer.toHexString(ram[address])
                                .toUpperCase() + "]");
            }

            // video card memory range is hardcoded here:
            if (address >= 0xA0000 && address <= 0xBFFFF) {
                return video.readMode(address);
            } else {
                return ram[address];
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.log(Level.SEVERE, "[" + super.getType()
                    + "] Access outside memory bounds; returning 0xFF");
            // Out of range, return default value
            return (byte) 0xFF;
        }

    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.ModuleMemory
     */
    @Override
    public void setByte(int address, byte value) {

        if(video == null) {
            video = (ModuleVideo) super.getConnection(Module.Type.VIDEO);
            cpu = (ModuleCPU) super.getConnection(Module.Type.CPU);
            motherboard = (ModuleMotherboard) super.getConnection(Module.Type.MOTHERBOARD);
        }
        // Mask 20th address bit
        address &= A20mask;

        try {
            // Watch certain memory address
            if (watchValue && address == watchAddress) {
                logger.log(Level.CONFIG, "[" + super.getType() + "] "
                        + cpu.getRegisterHex(0) + ":" + cpu.getRegisterHex(1)
                        + " Watched BYTE at address " + watchAddress
                        + " is written: ["
                        + Integer.toHexString(value).toUpperCase() + "]");
            }

            // video card memory range is hardcoded here:
            if (address >= 0xA0000 && address <= 0xBFFFF) {
                video.writeMode(address, value);
            }
            // Check if address is within allowed writeable boundaries;
            // A0000-BFFFF is video area, which is handled above; C0000-FFFFF is
            // BIOS area, which is out of bounds
            else if (address >= 0xC0000 && address <= 0xFFFFF) {
                return;
            }
            // Write allowed, not handled by any other devices, so process here
            else {
                // Store byte in memory
                ram[address] = value;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.log(Level.SEVERE, "[" + super.getType()
                    + "] Out of memory during write byte");
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.ModuleMemory
     */
    @Override
    public byte[] getWord(int address) {

        if(video == null) {
            video = (ModuleVideo) super.getConnection(Module.Type.VIDEO);
            cpu = (ModuleCPU) super.getConnection(Module.Type.CPU);
            motherboard = (ModuleMotherboard) super.getConnection(Module.Type.MOTHERBOARD);
        }
        // Mask 20th address bit
        address &= A20mask;

        // Check if address is in range
        try {
            // Watch certain memory address
            if (watchValue
                    && (address == watchAddress || address + 1 == watchAddress)) {
                logger
                        .log(Level.CONFIG, "["
                                + super.getType()
                                + "] "
                                + cpu.getRegisterHex(0)
                                + ":"
                                + cpu.getRegisterHex(1)
                                + " Watched WORD at address "
                                + watchAddress
                                + " is read: ["
                                + Integer.toHexString(ram[address + 1])
                                .toUpperCase()
                                + "] ["
                                + Integer.toHexString(ram[address])
                                .toUpperCase() + "]");
            }

            // video card memory range is hardcoded here:
            if (address >= 0xA0000 && address <= 0xBFFFF) {
                // Honour little-endian, put MSB in array[0], LSB in array[1]
                return new byte[]{video.readMode(address + 1),
                        video.readMode(address)};
            } else {
                // Honour little-endian, put MSB in array[0], LSB in array[1]
                return new byte[]{ram[address + 1], ram[address]};
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.log(Level.SEVERE, "[" + super.getType()
                    + "] Access outside memory bounds; returning 0xFFFF");
            // Out of range, return default value
            return new byte[]{(byte) 0xFF, (byte) 0xFF};
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.ModuleMemory
     */
    @Override
    public void setWord(int address, byte[] value) {

        if(video == null) {
            video = (ModuleVideo) super.getConnection(Module.Type.VIDEO);
            cpu = (ModuleCPU) super.getConnection(Module.Type.CPU);
            motherboard = (ModuleMotherboard) super.getConnection(Module.Type.MOTHERBOARD);
        }
        // Mask 20th address bit
        address &= A20mask;

        try {
            // Watch certain memory address
            if (watchValue
                    && (address == watchAddress || address + 1 == watchAddress)) {
                logger.log(Level.CONFIG, "[" + super.getType() + "] "
                        + cpu.getRegisterHex(0) + ":" + cpu.getRegisterHex(1)
                        + " Watched WORD at address " + watchAddress
                        + " is written: ["
                        + Integer.toHexString(value[0]).toUpperCase() + "]["
                        + Integer.toHexString(value[1]).toUpperCase() + "]");
            }

            // video card memory range is hardcoded here:
            if (address >= 0xA0000 && address <= 0xBFFFF) {
                video.writeMode(address, value[1]);
                video.writeMode(address + 1, value[0]);
            }
            // Check if address is within allowed writeable boundaries;
            // A0000-BFFFF is video area, which is handled above; C0000-FFFFF is
            // BIOS area, which is out of bounds
            else if (address >= 0xC0000 && address <= 0xFFFFF) {
                return;
            }
            // Write allowed, not handled by any other devices, so process here
            else {
                // Honour Intel little-endian: store LSB in first position in
                // RAM, MSB in next position
                ram[address] = value[1];
                ram[address + 1] = value[0];
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.log(Level.SEVERE, "[" + super.getType()
                    + "] Out of memory during write word");
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.ModuleMemory
     */
    @Override
    public void setBytes(int address, byte[] binaryStream)
            throws ModuleException {
        // Compute total length of stream
        int streamLength = binaryStream.length;

        // Check if address is in range
        try {
            // Copy each sequential byte into memory starting at location
            // address
            // Note: Intel Little-endian is not considered here
            for (int b = 0; b < streamLength; b++) {
                ram[address + b] = binaryStream[b];
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.log(Level.SEVERE, "[" + super.getType()
                    + "] Out of memory during write stream of bytes");
            throw new ModuleException("[" + super.getType()
                    + "] setBytes: Out of memory at byte " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.ModuleMemory
     */
    @Override
    public void setA20AddressLine(boolean status) {
        // Change the status of A20 address line check
        if (status) {
            // Enable 0x100000 address bit (memory wrapping is turned off)
            A20mask = 0xFFFFFFFF;
        } else {
            // Disable 0x100000 address bit (memory wrapping is turned on)
            A20mask = 0xFFEFFFFF;
        }
        logger.log(Level.CONFIG, "[" + super.getType() + "]"
                + " A20 address line status: " + status + " A20mask: [0x"
                + Long.toHexString(A20mask) + "]");
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.ModuleMemory
     */
    @Override
    public void setWatchValueAndAddress(boolean isWatchOn, int watchAddress) {
        this.watchValue = isWatchOn;
        this.watchAddress = watchAddress;
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.ModuleMemory
     */
    @Override
    public void setRamSizeInMB(int ramSizeMB) {
        this.ramSize = ramSizeMB * BYTES_IN_MB;
        // Create new empty memory
        ram = new byte[this.ramSize];
        A20mask = 0xFFEFFFFF; // Clear 20th address bit (wrap memory address)
    }

    /**
     * Converts a given string into a byte of one integer
     *
     * @param strValue value
     * @return int as byte
     */
    private int convertStringToByte(String strValue) {
        // Parse from string to int (hex)
        try {
            int intRegVal = 0;
            for (int i = strValue.length(); i > 0; i--) {
                intRegVal = intRegVal
                        + ((int) Math.pow(16, strValue.length() - i))
                        * Integer.parseInt(strValue.substring(i - 1, i), 16);
            }

            return intRegVal;
        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "[" + super.getType() + "]"
                    + " Error while parsing input");
            return -1;
        }
    }

}
