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
 * An enumeration representing all the ATA commands.
 */
public enum ATACommand {
    // Define each IDE Command:

    // These commands are implemented in BOCHS
    READ_SECTORS_WITH_RETRY((byte) 0x020, "Read Sectors With Retry"),
    READ_SECTORS_WITHOUT_RETRY((byte) 0x021, "Read Sectors Without Retry"),
    IDENTIFY_DRIVE((byte) 0x0EC, "Identify Drive"),
    WRITE_SECTORS_WITH_RETRY((byte) 0x030, "Write Sectors With Retry"),
    WRITE_MULTIPLE((byte) 0xC5, "WRITE MULTIPLE"),

    // TODO: clarify these commands
    PACKET_A0((byte) 0x0A0, "Packet"),
    PACKET_A1((byte) 0x0A1, "Packet"),

    // These commands are not immplement in BOCHS
    DEVICE_RESET((byte) 0x08, "DEVICE_RESET"),
    RECALIBRATE((byte) 0x010, "Recalibrate"), // Note: the recalibrate command is 1X
    READ_LONG_WITH_RETRY((byte) 0x022, "Read Long With Retry"),
    READ_LONG_WITHOUT_RETRY((byte) 0x023, "Read Long Without Retry"),
    READ_SECTORS_EXT((byte) 0x24, "READ SECTORS EXT"),
    READ_DMA_EXT((byte) 0x25, "READ DMA EXT"),
    READ_DMA_QUEUED_EXT((byte) 0x26, "READ DMA QUEUED EXT"),
    READ_NATIVE_MAX_ADDRESS_EXT((byte) 0x27, "READ NATIVE MAX ADDRESS EXT"),
    READ_MULTIPLE_EXT((byte) 0x29, "READ MULTIPLE EXT"),
    READ_STREAM_DMA((byte) 0x2A, "READ STREAM DMA"),
    READ_STREAM_PIO((byte) 0x2B, "READ STREAM PIO"),
    READ_LOG_EXT((byte) 0x2F, "READ LOG EXT"),
    WRITE_SECTORS_WITHOUT_RETRY((byte) 0x031, "Write Sectors Without Retry"),
    WRITE_LONG_SECTORS_WITH_RETRY((byte) 0x032, "Write Long Sectors With Retry"),
    WRITE_LONG_SECTORS_WITHOUT_RETRY((byte) 0x033, "Write Long Sectors Without Retry"),
    WRITE_SECTORS_EXT((byte) 0x34, "WRITE SECTORS EXT"),
    WRITE_DMA_EXT((byte) 0x35, "WRITE DMA EXT"),
    WRITE_DMA_QUEUED_EXT((byte) 0x36, "WRITE DMA QUEUED EXT"),
    SET_MAX_ADDRESS_EXT((byte) 0x37, "SET MAX ADDRESS EXT"),
    CFA_WRITE_SECTORS_WITHOUT_ERASE((byte) 0x38, "CFA WRITE SECTORS W/OUT ERASE"),
    WRITE_MULTIPLE_EXT((byte) 0x39, "WRITE MULTIPLE EXT"),
    WRITE_STREAM_DMA((byte) 0x3A, "WRITE STREAM DMA"),
    WRITE_STREAM_PIO((byte) 0x3B, "WRITE STREAM PIO"),
    WRITE_LOG_EXT((byte) 0x3F, "WRITE LOG EXT"),
    READ_VERIFY_SECTOR_WITH_RETRY((byte) 0x040, "Read Verify Sector With Retry"),
    READ_VERIFY_SECTOR_WITHOUT_RETRY((byte) 0x041, "Read Verify Sector Without Retry"),
    READ_VERIFY_SECTORS_EXT((byte) 0x42, "READ VERIFY SECTORS EXT"),
    FORMAT_TRACK((byte) 0x050, "Format Track"),
    CONFIGURE_STREAM((byte) 0x51, "CONFIGURE STREAM"),

    /**
     * The seek command is 7X.
     */
    SEEK((byte) 0x070, "Seek"), // Note:
    CFA_TRANSLATE_SECTOR((byte) 0x87, "CFA TRANSLATE SECTOR"),
    EXECUTE_DEVICE_DIAGNOSTIC((byte) 0x90, "EXECUTE DEVICE DIAGNOSTIC"),
    INITIALIZE_DEVICE_PARAMETERS((byte) 0x91, "INITIALIZE DEVICE PARAMETERS"),
    DOWNLOAD_MICROCODE((byte) 0x92, "DOWNLOAD MICROCODE"),
    STANDBY_IMMEDIATE((byte) 0x94, "STANDBY IMMEDIATE"),
    IDLE_IMMEDIATE((byte) 0x95, "IDLE IMMEDIATE"),
    STANDBY((byte) 0x96, "STANDBY"),
    IDLE((byte) 0x97, "IDLE"),
    CHECK_POWER_MODE((byte) 0x98, "CHECK POWER MODE"),
    SLEEP((byte) 0x99, "SLEEP"),
    SERVICE((byte) 0xA2, "SERVICE"),
    SMART_DISABLE_OPERATIONS((byte) 0xB0, "SMART DISABLE OPERATIONS"),
    DEVICE_CONFIGURATION_FREEZE_LOCK((byte) 0xB1, "DEVICE CONFIGURATION FREEZE LOCK"),
    CFA_ERASE_SECTORS((byte) 0xC0, "CFA ERASE SECTORS"),
    READ_MULTIPLE((byte) 0xC4, "READ MULTIPLE"),
    SET_MULTIPLE_MOD((byte) 0xC6, "SET MULTIPLE MOD"),
    READ_DMA_QUEUED((byte) 0xC7, "READ DMA QUEUED"),
    READ_DMA((byte) 0xC8, "READ DMA"),
    READ_DMA_NO_RETRY((byte) 0xC9, "READ DMA NO RETRY"),
    WRITE_DMA((byte) 0xCA, "WRITE DMA"),
    WRITE_DMA_QUEUED((byte) 0xCC, "WRITE DMA QUEUED"),
    CFA_WRITE_MULTIPLE_WITHOUT_ERASED((byte) 0xCD, "CFA WRITE MULTIPLE W/OUT ERASE"),
    CHECK_MEDIA_CARD_TYPE((byte) 0xD1, "CHECK MEDIA CARD TYPE"),
    GET_MEDIA_STATUS((byte) 0xDA, "GET MEDIA STATUS"),
    MEDIA_LOCK((byte) 0xDE, "MEDIA LOCK"),
    MEDIA_UNLOCK((byte) 0xDF, "MEDIA UNLOCK"),
    STANDBY_IMMEDIATE_2((byte) 0xE0, "STANDBY IMMEDIATE"),
    IDLE_IMMEDIATE_2((byte) 0xE1, "IDLE IMMEDIATE"),
    STANDBY_2((byte) 0xE2, "STANDBY"),
    IDLE_2((byte) 0xE3, "IDLE"),
    READ_BUFFER_2((byte) 0xE4, "READ BUFFER"),
    CHECK_POWER_MODE_2((byte) 0xE5, "CHECK POWER MODE"),
    SLEEP_2((byte) 0xE6, "SLEEP"),
    FLUSH_CACHE((byte) 0xE7, "FLUSH CACHE"),
    WRITE_BUFFER((byte) 0xE8, "Write Buffer"),
    FLUSH_CACHE_EXT((byte) 0xEA, "FLUSH CACHE EXT"),
    MEDIA_EJECT((byte) 0xED, "MEDIA EJECT"),
    SET_FEATURES((byte) 0x0EF, "Set Features"),
    SECURITY_SET_PASSWORD((byte) 0xF1, "SECURITY SET PASSWORD"),
    SECURITY_UNLOCK((byte) 0xF2, "SECURITY UNLOCK"),
    SECURITY_ERASE_PREPARE((byte) 0xF3, "SECURITY ERASE PREPARE"),
    SECURITY_ERASE_UNIT((byte) 0xF4, "SECURITY ERASE UNIT"),
    SECURITY_FREEZE_LOCK((byte) 0xF5, "SECURITY FREEZE LOCK"),
    SECURITY_DISABLE_PASSWORD((byte) 0xF6, "SECURITY DISABLE PASSWORD"),
    READ_NATIVE_MAX_ADDRESS((byte) 0xF8, "READ NATIVE MAX ADDRESS"),
    SET_MAX_ADDRESS((byte) 0xF9, "SET MAX ADDRESS");

    private final byte address;
    private final String name;

    /**
     * Class constructor, with the address and name specified.
     * 
     * @param theAddress
     *            the address associated with the command
     * @param theName
     *            the name of the command
     */
    private ATACommand(byte theAddress, String theName) {
        this.address = theAddress;
        this.name = theName;
    }

    /**
     * Get the command corresponding to the address
     * 
     * @param portAddress
     *            the port address
     * @return the command corresponding to the address
     */
    public static ATACommand getCommand(int portAddress) {
        ATACommand command = null;

        for (ATACommand cmd : values()) {
            if (cmd.getAddress() == portAddress) {
                command = cmd;
                break;
            }
        }

        return command;
    }

    /**
     * Gets the string name of the command.
     * 
     * @return the string name of the command
     */
    @Override
    public String toString() {

        return this.name;
    }

    /**
     * Gets the name of the command.
     * 
     * @return the name of the command
     */
    public String getName() {

        return this.name;
    }

    /**
     * Get the address of the command.
     * 
     * @return the address
     */
    public int getAddress() {
        return this.address;
    }

}
