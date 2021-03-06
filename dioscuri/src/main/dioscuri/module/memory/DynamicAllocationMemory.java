/* $Revision: 158 $ $Date: 2009-08-17 12:05:19 +0000 (ma, 17 aug 2009) $ $Author: blohman $
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
 *      Notes: This memory implementation using dynamic allocation was designed
 *      and coded by Tiago Leite, Tiago Taveira and Bruno Martins, students at
 *      the Insituto Superior Tecnico, May 2009.
 */
public class DynamicAllocationMemory extends ModuleMemory {

    // Logging
    private static final Logger logger = Logger.getLogger(DynamicAllocationMemory.class.getName());

    // Two dimensional array of dynamically allocated RAM
    private byte[][] ram;
    private int ramBlock; // Size of first dimension
    private int ramBlockSize; // Size of second dimension
    private int blockIndex; // Index into ram; shifting by this index determines
    // corresponding ramBlock for memory address
    static protected long A20mask; // Mask used to set/clear 20th bit in memory
    // addresses

    private final static int BYTES_IN_MB = 1048576;
    // Memory size
    private int ramSize = BYTES_IN_MB; // initial value defined in bytes (1 Megabyte, 2^20)

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
    public DynamicAllocationMemory(Emulator owner) {

        // Split ram into sections, more or less arbitrarily chosen size
        // (multiple of two)
        ramBlock = 64;
        ramBlockSize = this.ramSize / ramBlock;
        ram = new byte[ramBlock][ramBlockSize];

        // Initialise first dimension of array
        Arrays.fill(ram, null);
        blockIndex = 0;

        // blockIndex is the logarithm of the ramBlockSize, base 2
        // To determine logarithm, two methods can be used:
        // blockIndex = Math.log(ramBlockSize)/Math.log(2.0);
        // or some bit-voodoo:
        for (int i = ramBlockSize; i > 1; blockIndex++, i = i >> 1) ;

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
        Arrays.fill(ram, null);

        // Initialise A20 address bit to non-wrap (0xFFFF FFFF)
        setA20AddressLine(true);

        for (int i = 0xC0000; i <= 0xFFFFF; i++)
            setByte(i, (byte) 0xFF);

        logger
                .log(Level.SEVERE, "[" + super.getType()
                        + "] AbstractModule has been reset.");
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
        // new
        int address;
        int addressBlock;
        byte value;

        for (int row = 0; row < 200; row++) {
            dump += row + tab + ": ";
            for (int col = 0; col < 16; col++) {
                // dump += Integer.toHexString( 0x100 | ram[(row * 16) + col] &
                // 0xFF).substring(1).toUpperCase() + space;
                address = (row * 16) + col;

                // Determine which block contains the address
                addressBlock = address >> blockIndex;

                if (ram[addressBlock] == null)
                    value = (byte) 0;
                else {
                    value = ram[addressBlock][address & (ramBlockSize - 1)];
                }

                dump += Integer.toHexString(0x100 | value & 0xFF).substring(1)
                        .toUpperCase()
                        + space;
            }
            dump += ret;
        }
        logger.log(Level.SEVERE, "[" + super.getType() + "] GetDump()");
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

        // logger.log(Level.SEVERE, "[" + super.getType() + "]" + " getByte(" +
        // address + ")");

        // Determine which block contains the address
        int addressBlock = address >> blockIndex;

        byte addrValue = (byte) 0;

        if (ram[addressBlock] == null)
            addrValue = (byte) 0;
        else {
            addrValue = ram[addressBlock][address & (ramBlockSize - 1)];
        }

        // Check if address is in range
        try {
            // Watch certain memory address
            if (watchValue && address == watchAddress) {
                // logger.log(Level.CONFIG, "[" + super.getType() + "] " +
                // cpu.getRegisterHex(0) + ":" + cpu.getRegisterHex(1) +
                // " Watched BYTE at address " + watchAddress + " is read: [" +
                // Integer.toHexString(ram[address]).toUpperCase() + "]");

                logger.log(Level.CONFIG, "[" + super.getType() + "] "
                        + cpu.getRegisterHex(0) + ":" + cpu.getRegisterHex(1)
                        + " Watched BYTE at address " + watchAddress
                        + " is read: ["
                        + Integer.toHexString(addrValue).toUpperCase() + "]");
            }

            // video card memory range is hardcoded here:
            if (address >= 0xA0000 && address <= 0xBFFFF) {
                return video.readMode(address);
            } else {
                return addrValue;
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

        // logger.log(Level.SEVERE, "[" + super.getType() + "]" + " setByte(" +
        // address + ", " + value + ")");

        // Mask 20th address bit
        address &= A20mask;

        // Determine which block contains the address
        int addressBlock = address >> blockIndex;

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
                if (ram[addressBlock] == null) {
                    logger
                            .log(Level.SEVERE, "Allocating an extra "
                                    + ramBlockSize + " bytes for block "
                                    + addressBlock);
                    ram[addressBlock] = new byte[ramBlockSize];
                }

                ram[addressBlock][address & (ramBlockSize - 1)] = value;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.log(Level.SEVERE, "[" + super.getType()
                    + "] setByte: setByte: Out of memory during write byte");
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

        // logger.log(Level.SEVERE, "[" + super.getType() + "]" + " getWord(" +
        // address + ")");

        // Determine which block contains the address
        int addressBlock1 = address >> blockIndex;
        int addressBlock2 = (address + 1) >> blockIndex;
        byte addrValue1 = (byte) 0;
        byte addrValue2 = (byte) 0;

        if (ram[addressBlock1] == null)
            addrValue1 = (byte) 0;
        else {
            addrValue1 = ram[addressBlock1][address & (ramBlockSize - 1)];
        }

        if (ram[addressBlock2] == null)
            addrValue2 = (byte) 0;
        else {
            addrValue2 = ram[addressBlock2][(address + 1) & (ramBlockSize - 1)];
        }

        // Check if address is in range
        try {
            // Watch certain memory address
            if (watchValue
                    && (address == watchAddress || address + 1 == watchAddress)) {
                logger.log(Level.CONFIG, "[" + super.getType() + "] "
                        + cpu.getRegisterHex(0) + ":" + cpu.getRegisterHex(1)
                        + " Watched WORD at address " + watchAddress
                        + " is read: ["
                        + Integer.toHexString(addrValue2).toUpperCase() + "] ["
                        + Integer.toHexString(addrValue1).toUpperCase() + "]");
            }

            // video card memory range is hardcoded here:
            if (address >= 0xA0000 && address <= 0xBFFFF) {
                // Honour little-endian, put MSB in array[0], LSB in array[1]
                return new byte[]{video.readMode(address + 1),
                        video.readMode(address)};
            } else {
                // Honour little-endian, put MSB in array[0], LSB in array[1]
                return new byte[]{addrValue2, addrValue1};
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

        // Determine which block contains the address
        int addressBlock1 = address >> blockIndex;
        int addressBlock2 = (address + 1) >> blockIndex;

        // logger.log(Level.SEVERE, "[" + super.getType() + "]" + " setWord(" +
        // address + ")");

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
                if (ram[addressBlock1] == null) {
                    logger.log(Level.SEVERE, "Allocating an extra "
                            + ramBlockSize + " bytes for block "
                            + addressBlock1);
                    ram[addressBlock1] = new byte[ramBlockSize];
                }

                ram[addressBlock1][address & (ramBlockSize - 1)] = value[1];

                if (ram[addressBlock2] == null) {
                    logger.log(Level.SEVERE, "Allocating an extra "
                            + ramBlockSize + " bytes for block "
                            + addressBlock2);
                    ram[addressBlock2] = new byte[ramBlockSize];
                }

                ram[addressBlock2][(address + 1) & (ramBlockSize - 1)] = value[0];
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.log(Level.SEVERE, "[" + super.getType()
                    + "] setWord: Out of memory during write word");
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
        // logger.log(Level.SEVERE, "[" + super.getType() + "]" + " setBytes(" +
        // address + ")");

        // Compute total length of stream
        int streamLength = binaryStream.length;
        int addressBlock = 0;

        // Check if address is in range
        try {
            // Copy each sequential byte into memory starting at location
            // address
            // Note: Intel Little-endian is not considered here
            for (int b = 0; b < streamLength; b++) {
                // Determine which block contains the address
                addressBlock = (address + b) >> blockIndex;

                if (ram[addressBlock] == null) {
                    logger
                            .log(Level.SEVERE, "Allocating an extra "
                                    + ramBlockSize + " bytes for block "
                                    + addressBlock);
                    ram[addressBlock] = new byte[ramBlockSize];
                }
                ram[addressBlock][(address + b) & (ramBlockSize - 1)] = binaryStream[b];
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.log(Level.SEVERE, "[" + super.getType()
                    + "] setBytes: Out of memory during write stream of bytes");
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
        ramBlockSize = this.ramSize / ramBlock;

        blockIndex = 0;
        for (int i = ramBlockSize; i > 1; blockIndex++, i = i >> 1)
            ;

        A20mask = 0xFFEFFFFF; // Clear 20th address bit (wrap memory address)

        logger.log(Level.SEVERE, "[" + super.getType() + "]"
                + " setting ram size to: " + ramSizeMB + "Mb, ramSize = "
                + ramSize + ", blocks = " + ramBlock + ", block size = "
                + ramBlockSize);
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
