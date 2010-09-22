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

package dioscuri.module.bios;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import dioscuri.Emulator;
import dioscuri.exception.ModuleException;
import dioscuri.module.ModuleBIOS;

/**
 * An implementation of a hardware BIOS module.
 * 
 * Contains: - 64 KB of ROM - CMOS settings
 * 
 * @see dioscuri.module.AbstractModule
 * 
 *      Metadata module ********************************************
 *      general.type : bios general.name : BIOS ROM general.architecture : Von
 *      Neumann general.description : General implementation of BIOS ROM chip.
 *      general.creator : Tessella Support Services, Koninklijke Bibliotheek,
 *      Nationaal Archief of the Netherlands general.version : 1.0
 *      general.keywords : bios, ROM, 64KB, 32KB, bootstrap, system bios, video
 *      bios, optional rom general.relations : cpu, memory
 *      general.yearOfIntroduction : general.yearOfEnding : general.ancestor :
 *      general.successor : bios.romsize : 64 + 32 KB bios.settings : CMOS
 * 
 *      Notes: none
 * 
 */
public class BIOS extends ModuleBIOS {

    // Logging
    private static final Logger logger = Logger.getLogger(BIOS.class.getName());
    
    // BIOS ROM
    private byte[] systemROM; // Contains the System BIOS, using signed bytes as both signed/unsigned
    private byte[] videoROM; // Contains the Video BIOS

    // Memory size
    private final static int SYSTEMBIOS_ROM_SIZE = 65536; // defined in bytes
                                                          // (64 KB, 2^16)
    private final static int VIDEOBIOS_ROM_SIZE = 32768; // defined in bytes (32
                                                         // KB, 2^15)

    /**
     * Class constructor
     * 
     * @param owner
     */
    public BIOS(Emulator owner) {
        
        // Create new empty bios roms
        systemROM = new byte[SYSTEMBIOS_ROM_SIZE];
        videoROM = new byte[VIDEOBIOS_ROM_SIZE];

        // Set all rom to zero
        Arrays.fill(systemROM, (byte) 0);
        Arrays.fill(videoROM, (byte) 0);

        logger.log(Level.INFO, "[" + super.getType() + "] " + getClass().getName()
                + " -> AbstractModule created successfully.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean reset() {
        // Reset particular CMOS settings?
        logger.log(Level.CONFIG, "[" + super.getType() + "] AbstractModule has been reset.");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDump() {
        String dump = "";
        String ret = "\r\n";
        String tab = "\t";
        String space = " ";

        // Output System BIOS:
        dump = "System BIOS dump of first 800 bytes as stored in ROM:" + ret;

        // Output first 800 ROM bytes to dump
        for (int row = 0; row < (50); row++) {
            dump += row + tab + ": ";
            for (int col = 0; col < 16; col++) {
                dump += Integer.toHexString(
                        0x100 | systemROM[(row * 16) + col] & 0xFF)
                        .substring(1).toUpperCase()
                        + space;
            }
            dump += ret;
        }
        dump += ret;

        // Output Video BIOS:
        dump += "Video BIOS dump of first 800 bytes as stored in ROM:" + ret;

        // Output first 800 ROM bytes to dump
        for (int row = 0; row < (50); row++) {
            dump += row + tab + ": ";
            for (int col = 0; col < 16; col++) {
                dump += Integer.toHexString(
                        0x100 | videoROM[(row * 16) + col] & 0xFF).substring(1)
                        .toUpperCase()
                        + space;
            }
            dump += ret;
        }

        return dump;
    }

    // ******************************************************************************
    // ModuleBIOS Methods

    /**
     * Returns the system BIOS code from ROM with size of SYSTEM_BIOS_ROM_SIZE
     * 
     * @return byte[] biosCode containing the binary code of BIOS
     */
    public byte[] getSystemBIOS() {
        // Make a copy of ROM
        byte[] biosCode = new byte[SYSTEMBIOS_ROM_SIZE];

        // Copy each sequential byte from ROM starting at 0
        // Note: Intel Little-endian is not considered here
        for (int b = 0; b < SYSTEMBIOS_ROM_SIZE; b++) {
            biosCode[b] = systemROM[b];
        }
        return biosCode;
    }

    /**
     * Sets the system BIOS code in ROM Note: System BIOS must be exactly 64 KB
     * 
     * @param biosCode
     * @return true if BIOS code is of specified SYSTEMBIOS_ROM_SIZE and store
     *         is successful, false otherwise
     * @throws ModuleException
     */
    public boolean setSystemBIOS(byte[] biosCode) throws ModuleException {
        // Check if BIOS code complies to 64 KB max
        if (biosCode.length == SYSTEMBIOS_ROM_SIZE) {
            try {
                // Copy each sequential byte into ROM starting at 0
                // Note: Intel Little-endian is not considered here
                for (int b = 0; b < SYSTEMBIOS_ROM_SIZE; b++) {
                    systemROM[b] = biosCode[b];
                }
                return true;
            } catch (ArrayIndexOutOfBoundsException e) {
                logger.log(Level.SEVERE, "[" + super.getType() + "]"
                        + " System BIOS is larger than " + SYSTEMBIOS_ROM_SIZE
                        + " bytes");
                throw new ModuleException("[" + super.getType() + "]"
                        + " System BIOS is larger than " + SYSTEMBIOS_ROM_SIZE
                        + " bytes");
            }
        } else {
            throw new ModuleException("[" + super.getType() + "]"
                    + " System BIOS is not " + SYSTEMBIOS_ROM_SIZE + " bytes");
        }
    }

    /**
     * Returns the Video BIOS code from ROM
     * 
     * @return byte[] biosCode containing the binary code of Video BIOS
     */
    public byte[] getVideoBIOS() {
        // Make a copy of ROM
        byte[] biosCode = new byte[VIDEOBIOS_ROM_SIZE];

        // Copy each sequential byte from ROM starting at 0
        // Note: Intel Little-endian is not considered here
        for (int b = 0; b < VIDEOBIOS_ROM_SIZE; b++) {
            biosCode[b] = videoROM[b];
        }
        return biosCode;
    }

    /**
     * Sets the Video BIOS code in ROM
     * 
     * @param biosCode
     * @return true if BIOS code is of specified VIDEOBIOS_ROM_SIZE and store is
     *         successful, false otherwise
     * @throws ModuleException
     */
    public boolean setVideoBIOS(byte[] biosCode) throws ModuleException {
        // Check if BIOS code complies to 32 KB max
        if (biosCode.length == VIDEOBIOS_ROM_SIZE) {
            try {
                // Copy each sequential byte into VIDEO ROM starting at 0
                // Note: Intel Little-endian is not considered here
                for (int b = 0; b < VIDEOBIOS_ROM_SIZE; b++) {
                    videoROM[b] = biosCode[b];
                }
                return true;
            } catch (ArrayIndexOutOfBoundsException e) {
                logger.log(Level.SEVERE, "[" + super.getType()
                        + " Video BIOS is larger than " + SYSTEMBIOS_ROM_SIZE
                        + " bytes");
                throw new ModuleException("[" + super.getType()
                        + " Video BIOS is larger than " + SYSTEMBIOS_ROM_SIZE
                        + " bytes");
            }
        } else {
            throw new ModuleException("[" + super.getType() + " Video BIOS is not "
                    + SYSTEMBIOS_ROM_SIZE + " bytes");
        }
    }
}
