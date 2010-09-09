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

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import dioscuri.Emulator;
import dioscuri.exception.ModuleException;
import dioscuri.module.Module;
import dioscuri.module.ModuleCPU;
import dioscuri.module.ModuleMemory;
import dioscuri.module.ModuleMotherboard;
import dioscuri.module.ModuleVideo;

/**
 * An implementation of a hardware memory module.
 * 
 * Contains an array of 2^20 integers, offering 1MB of RAM.
 * 
 * @see Module
 * 
 *      Metadata module ********************************************
 *      general.type : memory general.name : 1 Megabyte Random Access Memory
 *      general.architecture : Von Neumann general.description : General
 *      implementation of 1 MB of flat RAM. general.creator : Tessella Support
 *      Services, Koninklijke Bibliotheek, Nationaal Archief of the Netherlands
 *      general.version : 1.0 general.keywords : memory, RAM, 1MB, A20 address
 *      line general.relations : cpu, mainboard general.yearOfIntroduction :
 *      general.yearOfEnding : general.ancestor : general.successor :
 *      memory.size : 1 MB
 * 
 *      Notes: This memory implementation using dynamic allocation was designed
 *      and coded by Tiago Leite, Tiago Taveira and Bruno Martins, students at
 *      the Insituto Superior Tecnico, May 2009.
 * 
 */
@SuppressWarnings("unused")
public class DynamicAllocationMemory extends ModuleMemory {

    // Attributes

    // Relations
    private Emulator emu;
    private String[] moduleConnections = new String[] { "video", "cpu",
            "motherboard" };
    private ModuleVideo video;
    private ModuleCPU cpu;
    private ModuleMotherboard motherboard;

    // Toggles
    private boolean isObserved;
    private boolean debugMode;

    // Two dimensional array of dynamically allocated RAM
    private byte[][] ram;
    private int ramBlock; // Size of first dimension
    private int ramBlockSize; // Size of second dimension
    private int blockIndex; // Index into ram; shifting by this index determines
                            // corresponding ramBlock for memory address
    static protected long A20mask; // Mask used to set/clear 20th bit in memory
                                   // addresses

    // Logging
    private static final Logger logger = Logger.getLogger(DynamicAllocationMemory.class.getName());

    // Constants

    // Module specifics
    public final static int MODULE_ID = 1;
    public final static String MODULE_TYPE = "memory";
    public final static String MODULE_NAME = "RAM";

    private final static int BYTES_IN_MB = 1048576;
    // Memory size
    private int ramSize = 1 * BYTES_IN_MB; // initial value defined in bytes (1
                                           // Megabyte, 2^20)

    // debugging functionality
    private boolean watchValue;
    private int watchAddress;

    // Constructors

    /**
     * Class constructor
     * 
     * @param owner
     */
    @SuppressWarnings("empty-statement")
    public DynamicAllocationMemory(Emulator owner) {
        emu = owner;

        // Initialise variables
        isObserved = false;
        debugMode = false;

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
        for (int i = ramBlockSize; i > 1; blockIndex++, i = i >> 1)
            ;

        // Debugging functionality
        watchValue = false;
        watchAddress = -1;

        logger.log(Level.INFO, "[" + MODULE_TYPE + "] " + MODULE_NAME
                + " Module created successfully.");
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
     * @param module
     * @return true if connection has been established successfully, false
     *         otherwise
     * 
     * @see Module
     */
    public boolean setConnection(Module module) {
        // Set connection to video adapter
        if (module.getType() == Type.VIDEO) { //.equalsIgnoreCase("video")) {
            this.video = (ModuleVideo) module;
            return true;
        } else if (module.getType() == Type.CPU) { //.equalsIgnoreCase("cpu")) {
            this.cpu = (ModuleCPU) module;
            return true;
        } else if (module.getType() == Type.MOTHERBOARD) { //.equalsIgnoreCase("motherboard")) {
            this.motherboard = (ModuleMotherboard) module;
            return true;
        }
        return false;
    }

    /**
     * Reset all parameters of module
     * 
     * @return boolean true if module has been reset successfully, false
     *         otherwise
     */
    public boolean reset() {
        Arrays.fill(ram, null);

        // Initialise A20 address bit to non-wrap (0xFFFF FFFF)
        setA20AddressLine(true);

        for (int i = 0xC0000; i <= 0xFFFFF; i++)
            setByte(i, (byte) 0xFF);

        logger
                .log(Level.SEVERE, "[" + MODULE_TYPE
                        + "] Module has been reset.");
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
     * @return true if data is set successfully, false otherwise
     * 
     * @see Module
     */
    public boolean setData(byte[] data, Module sender) {
        return false;
    }

    /**
     * Sets given String[] data for this module
     * 
     * @param sender
     * @see Module
     */
    public boolean setData(String[] data, Module sender) {
        // Check if string[] is not empty
        if (data.length > 1) {
            // Extract address
            int address = Integer.parseInt(data[0]);

            // Extract values
            byte[] value = new byte[data.length - 1];
            for (int v = 0; v < value.length; v++) {
                value[v] = (byte) this.convertStringToByte(data[v + 1]);
            }

            // Set value in memory
            try {
                this.setBytes(address, value);
            } catch (ModuleException e) {
                logger.log(Level.SEVERE, "[" + MODULE_TYPE
                        + "] setData: data not set. " + e.getMessage());
                return false;
            }
        }

        return true;
    }

    /**
     * Returns a dump of this module
     * 
     * @return string
     * 
     * @see Module
     */
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
        logger.log(Level.SEVERE, "[" + MODULE_TYPE + "] GetDump()");
        return dump;
    }

    // ******************************************************************************
    // ModuleMemory Methods

    /**
     * Returns the value of a byte at a specific address
     * 
     * @param address
     *            Flat-address where data can be found
     * 
     * @return byte Byte containing byte data
     */
    public byte getByte(int address) {
        // Mask 20th address bit
        address &= A20mask;

        // logger.log(Level.SEVERE, "[" + MODULE_TYPE + "]" + " getByte(" +
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
            if (watchValue == true && address == watchAddress) {
                // logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] " +
                // cpu.getRegisterHex(0) + ":" + cpu.getRegisterHex(1) +
                // " Watched BYTE at address " + watchAddress + " is read: [" +
                // Integer.toHexString(ram[address]).toUpperCase() + "]");

                logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] "
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
            logger.log(Level.SEVERE, "[" + MODULE_TYPE
                    + "] Access outside memory bounds; returning 0xFF");
            // Out of range, return default value
            return (byte) 0xFF;
        }

    }

    /**
     * Stores the value of a byte at a specific address
     * 
     * @param address
     *            Flat-address where data is stored
     * @param value
     *            Integer containing byte data
     */
    public void setByte(int address, byte value) {
        // logger.log(Level.SEVERE, "[" + MODULE_TYPE + "]" + " setByte(" +
        // address + ", " + value + ")");

        // Mask 20th address bit
        address &= A20mask;

        // Determine which block contains the address
        int addressBlock = address >> blockIndex;

        try {
            // Watch certain memory address
            if (watchValue == true && address == watchAddress) {
                logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] "
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
            logger.log(Level.SEVERE, "[" + MODULE_TYPE
                    + "] setByte: setByte: Out of memory during write byte");
        }
    }

    /**
     * Returns the value of a word at a specific address Note: words in memory
     * are stored in Little Endian order (LSB, MSB), but this method returns a
     * word in Big Endian order (MSB, LSB) because this is the common way words
     * are used by instructions.
     * 
     * @param address
     *            Flat-address where data can be found
     * 
     * @return byte[] array [MSB, LSB] containing data, 0xFFFF if address
     *         outside RAM_SIZE range
     */
    public byte[] getWord(int address) {
        // Mask 20th address bit
        address &= A20mask;

        // logger.log(Level.SEVERE, "[" + MODULE_TYPE + "]" + " getWord(" +
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
            if (watchValue == true
                    && (address == watchAddress || address + 1 == watchAddress)) {
                logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] "
                        + cpu.getRegisterHex(0) + ":" + cpu.getRegisterHex(1)
                        + " Watched WORD at address " + watchAddress
                        + " is read: ["
                        + Integer.toHexString(addrValue2).toUpperCase() + "] ["
                        + Integer.toHexString(addrValue1).toUpperCase() + "]");
            }

            // video card memory range is hardcoded here:
            if (address >= 0xA0000 && address <= 0xBFFFF) {
                // Honour little-endian, put MSB in array[0], LSB in array[1]
                return new byte[] { video.readMode(address + 1),
                        video.readMode(address) };
            } else {
                // Honour little-endian, put MSB in array[0], LSB in array[1]
                return new byte[] { addrValue2, addrValue1 };
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.log(Level.SEVERE, "[" + MODULE_TYPE
                    + "] Access outside memory bounds; returning 0xFFFF");
            // Out of range, return default value
            return new byte[] { (byte) 0xFF, (byte) 0xFF };
        }
    }

    /**
     * Stores the value of a word at a specific address Note: words in memory
     * are stored in Little Endian order (LSB, MSB), but this method assumes
     * that a word is given in Big Endian order (MSB, LSB) because this is the
     * common way words are used by instructions. However, storage takes place
     * in Little Endian order.
     * 
     */
    public void setWord(int address, byte[] value) {
        // Mask 20th address bit
        address &= A20mask;

        // Determine which block contains the address
        int addressBlock1 = address >> blockIndex;
        int addressBlock2 = (address + 1) >> blockIndex;

        // logger.log(Level.SEVERE, "[" + MODULE_TYPE + "]" + " setWord(" +
        // address + ")");

        try {
            // Watch certain memory address
            if (watchValue == true
                    && (address == watchAddress || address + 1 == watchAddress)) {
                logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] "
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
            logger.log(Level.SEVERE, "[" + MODULE_TYPE
                    + "] setWord: Out of memory during write word");
        }
    }

    /**
     * Stores an array of bytes in memory starting at a specific address
     * 
     * @param address
     *            Flat-address where data is stored
     * @throws ModuleException
     */
    public void setBytes(int address, byte[] binaryStream)
            throws ModuleException {
        // logger.log(Level.SEVERE, "[" + MODULE_TYPE + "]" + " setBytes(" +
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
            logger.log(Level.SEVERE, "[" + MODULE_TYPE
                    + "] setBytes: Out of memory during write stream of bytes");
            throw new ModuleException("[" + MODULE_TYPE
                    + "] setBytes: Out of memory at byte " + e.getMessage());
        }
    }

    /**
     * Set the status of A20 address line
     * 
     * 
     */
    public void setA20AddressLine(boolean status) {
        // Change the status of A20 address line check
        if (status == true) {
            // Enable 0x100000 address bit (memory wrapping is turned off)
            A20mask = 0xFFFFFFFF;
        } else {
            // Disable 0x100000 address bit (memory wrapping is turned on)
            A20mask = 0xFFEFFFFF;
        }
        logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]"
                + " A20 address line status: " + status + " A20mask: [0x"
                + Long.toHexString(A20mask) + "]");
    }

    /**
     * Set Debug watch params.
     * 
     * @param isWatchOn
     * @param watchAddress
     */
    public void setWatchValueAndAddress(boolean isWatchOn, int watchAddress) {
        this.watchValue = isWatchOn;
        this.watchAddress = watchAddress;
    }

    /**
     * Set RAM Size in megabytes
     * 
     * @param ramSizeMB
     */
    @SuppressWarnings("empty-statement")
    public void setRamSizeInMB(int ramSizeMB) {
        this.ramSize = ramSizeMB * BYTES_IN_MB;

        // Create new empty memory
        ramBlockSize = this.ramSize / ramBlock;

        blockIndex = 0;
        for (int i = ramBlockSize; i > 1; blockIndex++, i = i >> 1)
            ;

        A20mask = 0xFFEFFFFF; // Clear 20th address bit (wrap memory address)

        logger.log(Level.SEVERE, "[" + MODULE_TYPE + "]"
                + " setting ram size to: " + ramSizeMB + "Mb, ramSize = "
                + ramSize + ", blocks = " + ramBlock + ", block size = "
                + ramBlockSize);
    }

    // ******************************************************************************
    // Custom Methods

    /**
     * Converts a given string into a byte of one integer
     * 
     * @param string
     *            value
     * 
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
            logger.log(Level.SEVERE, "[" + MODULE_TYPE + "]"
                    + " Error while parsing input");
            return -1;
        }
    }

}
