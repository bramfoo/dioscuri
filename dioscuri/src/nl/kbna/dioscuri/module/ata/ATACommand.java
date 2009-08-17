/*
 * $Revision$ $Date$ $Author$
 * 
 * Copyright (C) 2007  National Library of the Netherlands, Nationaal Archief of the Netherlands
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information about this project, visit
 * http://dioscuri.sourceforge.net/
 * or contact us via email:
 * jrvanderhoeven at users.sourceforge.net
 * blohman at users.sourceforge.net
 * 
 * Developed by:
 * Nationaal Archief               <www.nationaalarchief.nl>
 * Koninklijke Bibliotheek         <www.kb.nl>
 * Tessella Support Services plc   <www.tessella.com>
 *
 * Project Title: DIOSCURI
 *
 */

/*
 * Information used in this module was taken from:
 * - http://en.wikipedia.org/wiki/AT_Attachment
 * - http://bochs.sourceforge.net/techspec/IDE-reference.txt
 */

package nl.kbna.dioscuri.module.ata;

/**
 * An enumeration representing all the ATA commands.
 */
public class ATACommand
{

    private byte address;
    private String name;

    /**
     * Class constructor, with the adress and name specified. 
     * 
     * @param theAddress the address associated with the command
     * @param theName the name of the command
     */
    private ATACommand(byte theAddress, String theName)
    {
        this.address = theAddress;
        this.name = theName;
    }

    // Define each IDE Command:

    // These commands are immplemented in BOCHS
    public final static ATACommand READ_SECTORS_WITH_RETRY = new ATACommand((byte)0x020, "Read Sectors With Retry");
    public final static ATACommand READ_SECTORS_WITHOUT_RETRY = new ATACommand((byte)0x021, "Read Sectors Without Retry");
    public final static ATACommand IDENTIFY_DRIVE = new ATACommand((byte)0x0EC, "Identify Drive");
    public final static ATACommand WRITE_SECTORS_WITH_RETRY = new ATACommand((byte)0x030, "Write Sectors With Retry");
    
    public final static ATACommand WRITE_MULTIPLE = new ATACommand((byte)0xC5, "WRITE MULTIPLE");
    
    //TODO: clarify these commands
    public final static ATACommand PACKET_A0 = new ATACommand((byte)0x0A0, "Packet");
    public final static ATACommand PACKET_A1 = new ATACommand((byte)0x0A1, "Packet");
    
    // These commands are not immplement in BOCHS
    public final static ATACommand DEVICE_RESET = new ATACommand((byte)0x08, "DEVICE_RESET");
    public final static ATACommand RECALIBRATE = new ATACommand((byte)0x010, "Recalibrate"); // Note: the recalibrate command is 1X
    public final static ATACommand READ_LONG_WITH_RETRY = new ATACommand((byte)0x022, "Read Long With Retry");
    public final static ATACommand READ_LONG_WITHOUT_RETRY = new ATACommand((byte)0x023, "Read Long Without Retry");
    public final static ATACommand READ_SECTORS_EXT = new ATACommand((byte)0x24, "READ SECTORS EXT");
    public final static ATACommand READ_DMA_EXT = new ATACommand((byte)0x25, "READ DMA EXT");
    public final static ATACommand READ_DMA_QUEUED_EXT = new ATACommand((byte)0x26, "READ DMA QUEUED EXT");
    public final static ATACommand READ_NATIVE_MAX_ADDRESS_EXT = new ATACommand((byte)0x27, "READ NATIVE MAX ADDRESS EXT");
    public final static ATACommand READ_MULTIPLE_EXT = new ATACommand((byte)0x29, "READ MULTIPLE EXT");
    public final static ATACommand READ_STREAM_DMA = new ATACommand((byte)0x2A, "READ STREAM DMA");
    public final static ATACommand READ_STREAM_PIO = new ATACommand((byte)0x2B, "READ STREAM PIO");
    public final static ATACommand READ_LOG_EXT = new ATACommand((byte)0x2F, "READ LOG EXT");
    public final static ATACommand WRITE_SECTORS_WITHOUT_RETRY = new ATACommand((byte)0x031, "Write Sectors Without Retry");
    public final static ATACommand WRITE_LONG_SECTORS_WITH_RETRY = new ATACommand((byte)0x032, "Write Long Sectors With Retry");
    public final static ATACommand WRITE_LONG_SECTORS_WITHOUT_RETRY = new ATACommand((byte)0x033, "Write Long Sectors Without Retry");
    public final static ATACommand WRITE_SECTORS_EXT = new ATACommand((byte)0x34, "WRITE SECTORS EXT");
    public final static ATACommand WRITE_DMA_EXT = new ATACommand((byte)0x35, "WRITE DMA EXT");
    public final static ATACommand WRITE_DMA_QUEUED_EXT = new ATACommand((byte)0x36, "WRITE DMA QUEUED EXT");
    public final static ATACommand SET_MAX_ADDRESS_EXT = new ATACommand((byte)0x37, "SET MAX ADDRESS EXT");
    public final static ATACommand CFA_WRITE_SECTORS_WITHOUT_ERASE = new ATACommand((byte)0x38, "CFA WRITE SECTORS W/OUT ERASE");
    public final static ATACommand WRITE_MULTIPLE_EXT = new ATACommand((byte)0x39, "WRITE MULTIPLE EXT");
    public final static ATACommand WRITE_STREAM_DMA = new ATACommand((byte)0x3A, "WRITE STREAM DMA");
    public final static ATACommand WRITE_STREAM_PIO = new ATACommand((byte)0x3B, "WRITE STREAM PIO");
    public final static ATACommand WRITE_LOG_EXT = new ATACommand((byte)0x3F, "WRITE LOG EXT");
    public final static ATACommand READ_VERIFY_SECTOR_WITH_RETRY = new ATACommand((byte)0x040, "Read Verify Sector With Retry");
    public final static ATACommand READ_VERIFY_SECTOR_WITHOUT_RETRY = new ATACommand((byte)0x041, "Read Verify Sector Without Retry");
    public final static ATACommand READ_VERIFY_SECTORS_EXT = new ATACommand((byte)0x42, "READ VERIFY SECTORS EXT");
    public final static ATACommand FORMAT_TRACK = new ATACommand((byte)0x050, "Format Track");
    public final static ATACommand CONFIGURE_STREAM = new ATACommand((byte)0x51, "CONFIGURE STREAM");
    public final static ATACommand SEEK = new ATACommand((byte)0x070, "Seek"); // Note: the seek command is 7X
    public final static ATACommand CFA_TRANSLATE_SECTOR = new ATACommand((byte)0x87, "CFA TRANSLATE SECTOR");
    public final static ATACommand EXECUTE_DEVICE_DIAGNOSTIC = new ATACommand((byte)0x90, "EXECUTE DEVICE DIAGNOSTIC");
    public final static ATACommand INITIALIZE_DEVICE_PARAMETERS = new ATACommand((byte)0x91, "INITIALIZE DEVICE PARAMETERS");
    public final static ATACommand DOWNLOAD_MICROCODE = new ATACommand((byte)0x92, "DOWNLOAD MICROCODE");
    public final static ATACommand STANDBY_IMMEDIATE = new ATACommand((byte)0x94, "STANDBY IMMEDIATE");
    public final static ATACommand IDLE_IMMEDIATE = new ATACommand((byte)0x95, "IDLE IMMEDIATE");
    public final static ATACommand STANDBY = new ATACommand((byte)0x96, "STANDBY");
    public final static ATACommand IDLE = new ATACommand((byte)0x97, "IDLE");
    public final static ATACommand CHECK_POWER_MODE = new ATACommand((byte)0x98, "CHECK POWER MODE");
    public final static ATACommand SLEEP = new ATACommand((byte)0x99, "SLEEP");
    public final static ATACommand SERVICE = new ATACommand((byte)0xA2, "SERVICE");
    public final static ATACommand SMART_DISABLE_OPERATIONS = new ATACommand((byte)0xB0, "SMART DISABLE OPERATIONS");
    public final static ATACommand DEVICE_CONFIGURATION_FREEZE_LOCK = new ATACommand((byte)0xB1, "DEVICE CONFIGURATION FREEZE LOCK");
    public final static ATACommand CFA_ERASE_SECTORS = new ATACommand((byte)0xC0, "CFA ERASE SECTORS");
    public final static ATACommand READ_MULTIPLE = new ATACommand((byte)0xC4, "READ MULTIPLE");
    public final static ATACommand SET_MULTIPLE_MOD = new ATACommand((byte)0xC6, "SET MULTIPLE MOD");
    public final static ATACommand READ_DMA_QUEUED = new ATACommand((byte)0xC7, "READ DMA QUEUED");
    public final static ATACommand READ_DMA = new ATACommand((byte)0xC8, "READ DMA");
    public final static ATACommand READ_DMA_NO_RETRY = new ATACommand((byte)0xC9, "READ DMA NO RETRY");
    public final static ATACommand WRITE_DMA = new ATACommand((byte)0xCA, "WRITE DMA");
    public final static ATACommand WRITE_DMA_QUEUED = new ATACommand((byte)0xCC, "WRITE DMA QUEUED");
    public final static ATACommand CFA_WRITE_MULTIPLE_WITHOUT_ERASED = new ATACommand((byte)0xCD, "CFA WRITE MULTIPLE W/OUT ERASE");
    public final static ATACommand CHECK_MEDIA_CARD_TYPE = new ATACommand((byte)0xD1, "CHECK MEDIA CARD TYPE");
    public final static ATACommand GET_MEDIA_STATUS = new ATACommand((byte)0xDA, "GET MEDIA STATUS");
    public final static ATACommand MEDIA_LOCK = new ATACommand((byte)0xDE, "MEDIA LOCK");
    public final static ATACommand MEDIA_UNLOCK = new ATACommand((byte)0xDF, "MEDIA UNLOCK");
    public final static ATACommand STANDBY_IMMEDIATE_2 = new ATACommand((byte)0xE0, "STANDBY IMMEDIATE");
    public final static ATACommand IDLE_IMMEDIATE_2 = new ATACommand((byte)0xE1, "IDLE IMMEDIATE");
    public final static ATACommand STANDBY_2 = new ATACommand((byte)0xE2, "STANDBY");
    public final static ATACommand IDLE_2 = new ATACommand((byte)0xE3, "IDLE");
    public final static ATACommand READ_BUFFER_2 = new ATACommand((byte)0xE4, "READ BUFFER");
    public final static ATACommand CHECK_POWER_MODE_2 = new ATACommand((byte)0xE5, "CHECK POWER MODE");
    public final static ATACommand SLEEP_2 = new ATACommand((byte)0xE6, "SLEEP");
    public final static ATACommand FLUSH_CACHE = new ATACommand((byte)0xE7, "FLUSH CACHE");
    public final static ATACommand WRITE_BUFFER = new ATACommand((byte)0xE8, "Write Buffer");
    public final static ATACommand FLUSH_CACHE_EXT = new ATACommand((byte)0xEA, "FLUSH CACHE EXT");
    public final static ATACommand MEDIA_EJECT = new ATACommand((byte)0xED, "MEDIA EJECT");
    public final static ATACommand SET_FEATURES = new ATACommand((byte)0x0EF, "Set Features");
    public final static ATACommand SECURITY_SET_PASSWORD = new ATACommand((byte)0xF1, "SECURITY SET PASSWORD");
    public final static ATACommand SECURITY_UNLOCK = new ATACommand((byte)0xF2, "SECURITY UNLOCK");
    public final static ATACommand SECURITY_ERASE_PREPARE = new ATACommand((byte)0xF3, "SECURITY ERASE PREPARE");
    public final static ATACommand SECURITY_ERASE_UNIT = new ATACommand((byte)0xF4, "SECURITY ERASE UNIT");
    public final static ATACommand SECURITY_FREEZE_LOCK = new ATACommand((byte)0xF5, "SECURITY FREEZE LOCK");
    public final static ATACommand SECURITY_DISABLE_PASSWORD = new ATACommand((byte)0xF6, "SECURITY DISABLE PASSWORD");
    public final static ATACommand READ_NATIVE_MAX_ADDRESS = new ATACommand((byte)0xF8, "READ NATIVE MAX ADDRESS");
    public final static ATACommand SET_MAX_ADDRESS = new ATACommand((byte)0xF9, "SET MAX ADDRESS");
    
    // Define all IDE comamnds as a array
    public final static ATACommand[] ideCommands = {PACKET_A0, PACKET_A1, IDENTIFY_DRIVE, READ_SECTORS_WITH_RETRY, READ_SECTORS_WITHOUT_RETRY, READ_LONG_WITH_RETRY,
            READ_LONG_WITHOUT_RETRY, READ_VERIFY_SECTOR_WITH_RETRY, READ_VERIFY_SECTOR_WITHOUT_RETRY, WRITE_SECTORS_WITH_RETRY, WRITE_SECTORS_WITHOUT_RETRY,
            WRITE_LONG_SECTORS_WITH_RETRY, WRITE_LONG_SECTORS_WITHOUT_RETRY, DEVICE_RESET, RECALIBRATE, READ_SECTORS_EXT, READ_DMA_EXT, READ_DMA_QUEUED_EXT,
            READ_NATIVE_MAX_ADDRESS_EXT, READ_MULTIPLE_EXT, READ_STREAM_DMA, READ_STREAM_PIO, READ_LOG_EXT, WRITE_SECTORS_EXT, WRITE_DMA_EXT,
            WRITE_DMA_QUEUED_EXT, SET_MAX_ADDRESS_EXT, CFA_WRITE_SECTORS_WITHOUT_ERASE, WRITE_MULTIPLE_EXT, WRITE_STREAM_DMA, WRITE_STREAM_PIO, WRITE_LOG_EXT,
            READ_VERIFY_SECTORS_EXT, FORMAT_TRACK, CONFIGURE_STREAM, SEEK, CFA_TRANSLATE_SECTOR, EXECUTE_DEVICE_DIAGNOSTIC, INITIALIZE_DEVICE_PARAMETERS,
            DOWNLOAD_MICROCODE, STANDBY_IMMEDIATE, IDLE_IMMEDIATE, STANDBY, IDLE, CHECK_POWER_MODE, SLEEP, SERVICE, SMART_DISABLE_OPERATIONS,
            DEVICE_CONFIGURATION_FREEZE_LOCK, CFA_ERASE_SECTORS, READ_MULTIPLE, WRITE_MULTIPLE, SET_MULTIPLE_MOD, READ_DMA_QUEUED, READ_DMA, READ_DMA_NO_RETRY,
            WRITE_DMA, WRITE_DMA_QUEUED, CFA_WRITE_MULTIPLE_WITHOUT_ERASED, CHECK_MEDIA_CARD_TYPE, GET_MEDIA_STATUS, MEDIA_LOCK, MEDIA_UNLOCK,
            STANDBY_IMMEDIATE_2, IDLE_IMMEDIATE_2, STANDBY_2, IDLE_2, READ_BUFFER_2, CHECK_POWER_MODE_2, SLEEP_2, FLUSH_CACHE, WRITE_BUFFER, FLUSH_CACHE_EXT,
            MEDIA_EJECT, SET_FEATURES, SECURITY_SET_PASSWORD, SECURITY_UNLOCK, SECURITY_ERASE_PREPARE, SECURITY_ERASE_UNIT, SECURITY_FREEZE_LOCK,
            SECURITY_DISABLE_PASSWORD, READ_NATIVE_MAX_ADDRESS, SET_MAX_ADDRESS};

    /**
     * Get the command corresponding to the address
     * 
     * @param portAddress the port address
     * @return the command corresponding to the address
     */
    public static ATACommand getCommand(int portAddress)
    {
        ATACommand command = null;

        for (int i = 0; i < ideCommands.length; i++)
        {
            if (ideCommands[i].getAddress() == portAddress)
            {
                command = ideCommands[i];
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
    public String toString()
    {

        return this.name;
    }

    /**
     * Gets the name of the command.
     * 
     * @return the name of the command
     */
    public String getName()
    {

        return this.name;
    }

    /**
     * Get the address of the command.
     * 
     * @return the address
     */
    public int getAddress()
    {
        return this.address;
    }

}
