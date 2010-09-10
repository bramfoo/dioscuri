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

/*
 * Information used in this module was taken from:
 * - http://en.wikipedia.org/wiki/AT_Attachment
 * - http://bochs.sourceforge.net/techspec/IDE-reference.txt
 */
package dioscuri.module.ata;

/**
 * An class holding the constants associated with the ATA controller.
 * 
 */
public class ATAConstants {

    // Define Ports
    // I/O ports 01F0-01F7 - Primary ATA controller - HDC 1 (1st Fixed Disk
    // Controller) (ISA, EISA)
    public static final int PORT_IDE_DATA = 0x01F0;
    public static final int PORT_IDE_ERROR_WPC = 0x01F1;
    public static final int PORT_IDE_SECTOR_COUNT = 0x01F2;
    public static final int PORT_IDE_SECTOR_NUMBER = 0x01F3;
    public static final int PORT_IDE_CYLINDER_LOW = 0x01F4;
    public static final int PORT_IDE_CYLINDER_HIGH = 0x01F5;
    public static final int PORT_IDE_DRIVE_HEAD = 0x01F6;
    public static final int PORT_IDE_STATUS_CMD = 0x01F7;
    public static final int PORT_IDE_ALT_STATUS_DEVICE = 0x03F6;
    public static final int PORT_IDE_DRIVE_ADDRESS = 0x03F7;
    public static final int DEFAULT_IO_ADDR_1[] = { 0x1f0, 0x170, 0x1e8, 0x168 };
    public static final int DEFAULT_IO_ADDR_2[] = { 0x3f0, 0x370, 0x3e0, 0x360 };

    // Other Constants
    public static final int IDE_DMA_CHANNEL = 3;
    public static final int INDEX_PULSE_CYCLE = 10;
    public static final int PACKET_SIZE = 12;
    public static final int NUM_BYTES_PER_BLOCK = 512;
    public static final int SECTORS_PER_BLOCK = 0x80;
    public static final int MAX_NUMBER_IDE_CHANNELS = 4;
    public static final int MAX_NUMBER_DRIVES_PER_CHANNEL = 2;
    public static final boolean SUPPORT_REPEAT_SPEEDUPS = true; // TODO: what is
                                                                // this?
    public static final boolean IS_LOW_LEVEL_CDROM = false;
    public static final boolean WITH_WIN32 = true;
    public static final String IDE_MODEL = "Generic 1234                            "; // 40
                                                                                       // char's
                                                                                       // long

    // boot devices
    // (using the same values as the rombios)
    public static final int BOOT_NONE = 0;
    public static final int BOOT_FLOPPYA = 1;
    public static final int BOOT_DISKC = 2;
    public static final int BOOT_CDROM = 3;
    public static final boolean IS_CONNER_CFA540A_DEFINED = false; // not
                                                                   // apparently
                                                                   // defined in
                                                                   // BOCHS
    public static final boolean SUPPORTS_PCI = false;
    public static final int MAX_MULTIPLE_SECTORS = 16;
}
