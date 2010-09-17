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

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import dioscuri.Emulator;
import dioscuri.config.ModuleType;
import dioscuri.exception.ModuleException;
import dioscuri.exception.ModuleUnknownPort;
import dioscuri.exception.ModuleWriteOnlyPortException;
import dioscuri.exception.StorageDeviceException;
import dioscuri.module.Module;
import dioscuri.module.ModuleATA;
import dioscuri.module.ModuleMotherboard;
import dioscuri.module.ModulePIC;
import dioscuri.module.ModuleRTC;

/**
 * An implementation of a ATA controller module.
 *
 * @see Module
 *
 *      Metadata module ********************************************
 *      general.type : ata general.name : ATA Controller / ATA-1 to ATAT-3
 *      general.architecture : Von Neumann general.description : Implements a
 *      standard Integrated Drive Electronic (IDE) controller / parallel ATA
 *      general.creator : Tessella Support Services, Koninklijke Bibliotheek,
 *      Nationaal Archief of the Netherlands general.version : 1.0
 *      general.keywords : IDE, ATA, storage, disk, controller general.relations
 *      : motherboard general.yearOfIntroduction : 1994 general.yearOfEnding :
 *      1999 general.ancestor : - general.successor : ATA-2 (EIDE)
 *
 *      Notes: none
 *
 */
public class ATA extends ModuleATA {

    // Attributes

    // Relations
    private Emulator emu;

    //private ModuleType[] moduleConnections = new ModuleType[] {
    //        ModuleType.MOTHERBOARD, ModuleType.RTC, ModuleType.PIC };// , ModuleType.CPU};

    //private ModuleMotherboard motherboard;
    //private ModuleRTC rtc;
    //private ModulePIC pic;
    // private ModuleCPU cpu;

    // Toggles
    private boolean isObserved;
    private boolean debugMode;

    // Logging
    private static final Logger logger = Logger.getLogger(ATA.class.getName());

    // Timing
    private int updateInterval;

    // Channels
    private ATAChannel[] channels = new ATAChannel[ATAConstants.MAX_NUMBER_IDE_CHANNELS];
    private int curChannelIndex;

    // TODO: confirm use of these fields
    private int bulkIOHostAddr = 0;
    private int bulkIOQuantumsRequested = 0;
    private int bulkIOQuantumsTransferred = 0;

    private int cdromCount;

    // Constructors

    /**
     * Class constructor
     * @param owner
     */
    public ATA(Emulator owner) {
        emu = owner;

        // Initialise module variables
        isObserved = false;
        debugMode = false;

        // Initialise timing
        updateInterval = -1;

        cdromCount = 0;

        for (int i = 0; i < channels.length; i++) {
            channels[i] = new ATAChannel(this,
                    ATAConstants.DEFAULT_IO_ADDR_1[i],
                    ATAConstants.DEFAULT_IO_ADDR_2[i]);
        }

        curChannelIndex = 0;

        logger.log(Level.INFO, "[" + super.getType() + "]"
                + " Module created successfully.");

    }

    // ******************************************************************************
    // Module Methods

    /**
     * Reset all parameters of module
     *
     * @return boolean true if module has been reset successfully, false
     *         otherwise
     */
    public boolean reset() {

        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Type.MOTHERBOARD);
        ModuleRTC rtc = (ModuleRTC)super.getConnection(Type.RTC);
        ModulePIC pic = (ModulePIC)super.getConnection(Type.PIC);

        // Request a timer
        if (motherboard.requestTimer(this, updateInterval, false) == true) {
            logger.log(Level.CONFIG, "[" + super.getType() + "]"
                    + " Timer requested successfully.");
        } else {
            logger.log(Level.WARNING, "[" + super.getType() + "]"
                    + " Failed to request a timer.");
        }

        /*
         * // Register I/O ports 01F0-01F7 - Primary IDE Controller in I/O
         * address space // motherboard.setIOPort(IDEConstants.PORT_IDE_DATA,
         * this); motherboard.setIOPort(IDEConstants.PORT_IDE_ERROR_WPC, this);
         * motherboard.setIOPort(IDEConstants.PORT_IDE_SECTOR_COUNT, this);
         * motherboard.setIOPort(IDEConstants.PORT_IDE_SECTOR_NUMBER, this);
         * motherboard.setIOPort(IDEConstants.PORT_IDE_CYLINDER_LOW, this);
         * motherboard.setIOPort(IDEConstants.PORT_IDE_CYLINDER_HIGH, this);
         * motherboard.setIOPort(IDEConstants.PORT_IDE_DRIVE_HEAD, this);
         * motherboard.setIOPort(IDEConstants.PORT_IDE_STATUS_CMD, this);
         * motherboard.setIOPort(IDEConstants.PORT_IDE_ALT_STATUS_DEVICE, this);
         * motherboard.setIOPort(IDEConstants.PORT_IDE_DRIVE_ADDRESS, this);
         */

        // Reset drives
        for (curChannelIndex = 0; curChannelIndex < channels.length; curChannelIndex++) {
            getSelectedChannel().reset();
        }

        curChannelIndex = 0;

        logger.log(Level.CONFIG, "[" + super.getType() + "]"
                + "  Module has been reset.");
        return true;

    }

    /**
     * Returns a dump of this module.
     *
     * @return string the dumop string
     * @see Module
     */
    public String getDump() {
        // Show some status information of this module
        String dump = "";

        dump = "ATA dump:\n";

        int numDrives = this.getNumDrives();

        dump += "In total " + numDrives + " IDE drive(s) exist:\n";

        for (int i = 0; i < channels.length; i++) {

            ATAChannel currentChannel = channels[i];

            ATADrive[] drives = currentChannel.getDrives();

            for (int j = 0; j < drives.length; j++) {
                if (drives[j] != null
                        && drives[j].driveType != ATADriveType.NONE) {

                    dump += "Drive " + j + ": type '"
                            + drives[j].getDriveType() + "'";

                    if (drives[j].containsDisk()) {
                        dump += "; drive present, type="
                                + drives[j].getDriveType().toString()
                                + ", size=" + drives[j].getDiskImageSize()
                                + " bytes\n";
                    } else {
                        dump += ", empty\n";
                    }
                } else {
                    dump += "Drive " + j + ": not present\n";
                }
            }
        }

        return dump;
    }

    // ******************************************************************************
    // ModuleDevice Methods

    /**
     * Defines the interval between subsequent updates.
     *
     * @param interval
     */
    public void setUpdateInterval(int interval) {
        // Check if interval is > 0
        if (interval > 0) {
            updateInterval = interval;
        } else {
            updateInterval = 100000;
        }
        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Type.MOTHERBOARD);
        motherboard.resetTimer(this, updateInterval);
    }

    /**
     * Update device
     */
    public void update() {

        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Type.MOTHERBOARD);

        for (int channelIndex = 0; channelIndex < ATAConstants.MAX_NUMBER_IDE_CHANNELS; channelIndex++) {

            for (int deviceIndex = 0; deviceIndex < 2; deviceIndex++) {

                if (channels[channelIndex].getDrives()[deviceIndex]
                        .getIoLightCounter() > 0) {

                    int newIoLightCounter = channels[channelIndex].getDrives()[deviceIndex]
                            .getIoLightCounter();
                    newIoLightCounter--;

                    channels[channelIndex].getDrives()[deviceIndex]
                            .setIoLightCounter(newIoLightCounter);

                    if (newIoLightCounter > 0) {
                        // bx_pc_system.activate_timer( BX_HD_THIS
                        // iolight_timer_index, 100000, 0 );
                        motherboard.resetTimer(this, updateInterval);
                        motherboard.setTimerActiveState(this, true);

                    } else {
                        // bx_gui->statusbar_setitem(BX_HD_THIS
                        // channels[channel].drives[device].statusbar_id, 0);
                    }

                }
            }
        }
    }

    /**
     * Return a byte from I/O address space at given port.
     *
     * @param originalPortAddress
     * @return byte containing the data at given I/O address port
     * @throws ModuleException
     *             , ModuleWriteOnlyPortException
     * @throws ModuleUnknownPort
     * @throws ModuleWriteOnlyPortException
     */
    public byte getIOPortByte(int originalPortAddress) throws ModuleException,
            ModuleUnknownPort, ModuleWriteOnlyPortException {
        logger.log(Level.CONFIG, "[" + super.getType() + "]"
                + "  IN command (byte) to port "
                + Integer.toHexString(originalPortAddress).toUpperCase()
                + " received");

        return read(originalPortAddress, 1)[0];
    }

    /**
     * Set a byte in I/O address space at given port TODO: BOCHS code writes to
     * both drives of a channel - this seems wrong but kept in this code to adi
     * debugging and comparison with BOCHS
     *
     * @param originalAddress
     * @param data
     * @throws ModuleException
     *             , ModuleWriteOnlyPortException
     * @throws ModuleUnknownPort
     */
    public void setIOPortByte(int originalAddress, byte data)
            throws ModuleException, ModuleUnknownPort {
        logger.log(Level.CONFIG, "[" + super.getType() + "]"
                + "  OUT command (byte: "
                + Integer.toHexString(data).toUpperCase() + ") to port "
                + Integer.toHexString(originalAddress).toUpperCase()
                + " received.");
        byte[] dataArray = { data };

        write(originalAddress, dataArray, 1);
    }

    /**
     * Return a word from I/O address space at given port
     *
     * @param portAddress
     * @return byte[] containing the word at given I/O address port
     * @throws ModuleException
     *             , ModuleWriteOnlyPortException
     * @throws ModuleUnknownPort
     * @throws ModuleWriteOnlyPortException
     */
    public byte[] getIOPortWord(int portAddress) throws ModuleException,
            ModuleUnknownPort, ModuleWriteOnlyPortException {
        logger.log(Level.CONFIG, "[" + super.getType() + "]"
                + "  IN command (word) to port "
                + Integer.toHexString(portAddress).toUpperCase() + " received");

        return read(portAddress, 2);
    }

    /**
     * Set a word in I/O address space at given port
     *
     * @param portAddress
     * @param dataWord
     * @throws ModuleException
     *             , ModuleWriteOnlyPortException
     * @throws ModuleUnknownPort
     */
    public void setIOPortWord(int portAddress, byte[] dataWord)
            throws ModuleException, ModuleUnknownPort {
        logger
                .log(Level.CONFIG, "[" + super.getType() + "]"
                        + "  OUT command (word) to port "
                        + Integer.toHexString(portAddress).toUpperCase()
                        + " received.");

        write(portAddress, dataWord, 2);
    }

    /**
     * Return a double word from I/O address space at given port
     *
     * @param portAddress
     * @return byte[] containing the double word at given I/O address port
     * @throws ModuleException
     *             , ModuleWriteOnlyPortException
     * @throws ModuleUnknownPort
     * @throws ModuleWriteOnlyPortException
     */
    public byte[] getIOPortDoubleWord(int portAddress) throws ModuleException,
            ModuleUnknownPort, ModuleWriteOnlyPortException {
        logger.log(Level.CONFIG, "[" + super.getType() + "]"
                + "  IN command (double word) to port "
                + Integer.toHexString(portAddress).toUpperCase() + " received");

        return read(portAddress, 4);
    }

    /**
     * Set a double word in I/O address space at given port
     *
     * @param portAddress
     * @param dataDoubleWord
     * @throws ModuleException
     *             , ModuleWriteOnlyPortException
     * @throws ModuleUnknownPort
     */
    public void setIOPortDoubleWord(int portAddress, byte[] dataDoubleWord)
            throws ModuleException, ModuleUnknownPort {
        logger
                .log(Level.CONFIG, "[" + super.getType() + "]"
                        + "  OUT command (double word) to port "
                        + Integer.toHexString(portAddress).toUpperCase()
                        + " received.");

        write(portAddress, dataDoubleWord, 4);
        return;
    }

    // ******************************************************************************
    // ModuleATA Methods

    /**
     * Initiate configuration of the disk drive.
     *
     * @param theIdeChannel
     * @param isMaster
     * @param isHardDisk
     * @param isWriteProtected
     * @param numCylinders
     * @param numHeads
     * @param numSectorsPerTrack
     * @param translationType
     * @param imageFilePath
     */
    public void initConfig(int theIdeChannel, boolean isMaster,
            boolean isHardDisk, boolean isWriteProtected, int numCylinders,
            int numHeads, int numSectorsPerTrack,
            ATATranslationType translationType, String imageFilePath) {

        // Initialise controller variables

        ATADrive drive;

        // Set the drive type
        if (isHardDisk) {
            drive = new ATADrive(ATADriveType.HARD_DISK, this, true);
        } else {
            drive = new ATADrive(ATADriveType.CDROM, this, true, cdromCount);
        }

        drive.setWriteProtected(isWriteProtected);

        // set the drive capacity
        int sectorsPerBlock = ATAConstants.SECTORS_PER_BLOCK;

        drive.setDiskCapacity(numCylinders, numHeads, numSectorsPerTrack,
                sectorsPerBlock);
        drive.setIsMaster(isMaster);
        drive.setTranslationType(translationType);
        this.loadDiskImage(imageFilePath, drive);
        channels[theIdeChannel].setDisk(drive);

        // Do checks

        if ((numHeads == 0) || (numSectorsPerTrack == 0)) {
            logger.log(Level.SEVERE, "[" + super.getType() + "]"
                    + " cannot have zero heads, or sectors/track");

        }

        if (numCylinders > 0) {
            if (drive.getDiskImageSize() != drive.getDiskCapacity()) {

                logger.log(Level.SEVERE, "[" + super.getType() + "]"
                        + " Image size doesn't match specified geometry.");
            }
        }

        // if num cylinders is set to 0 then auto detect is used to calculate
        // the correct number based upon the size of the disk image
        // Auto detect
        else {
            // Autodetect number of cylinders
            numCylinders = (int) (drive.getDiskImageSize() / (numHeads
                    * numSectorsPerTrack * 512));

            drive.setTotalNumCylinders(numCylinders);

            // BX_INFO(("ata%d-%d: autodetect geometry: CHS=%d/%d/%d", channel,
            // device, cyl, heads, spt));
            logger.log(Level.CONFIG, "[" + super.getType() + "]"
                    + " Autodetect geometry: Number Cylinders set to "
                    + numCylinders);
        }
    }

    /**
     * Set CMOS values
     *
     * @param bootDrives
     * @param floppySigCheckDisabled
     */
    public void setCmosSettings(int[] bootDrives, boolean floppySigCheckDisabled) {

        ModuleRTC rtc = (ModuleRTC)super.getConnection(Type.RTC);

        // generate CMOS values for hard drive

        // CMOS 12h - IBM - HARD DISK DATA
        // Notes: A PC with a single type 2 (20 Mb ST-225) hard disk will have
        // 20h in
        // byte 12h
        // some PCs utilizing external disk controller ROMs will use type 0 to
        // disable ROM BIOS (e.g. Zenith 248 with Plus HardCard).
        //
        // Bitfields for IBM hard disk data:
        // Bit(s) Description (Table C014)
        // 7-4 First Hard Disk Drive
        // 00 No drive
        // 01-0Eh Hard drive Type 1-14
        // 0Fh Hard Disk Type 16-255
        // (actual Hard Drive Type is in CMOS RAM 19h)
        // 3-0 Second Hard Disk Drive Type
        // (same as first except extrnded type will be found in 1Ah).
        rtc.setCMOSRegister(0x12, (byte) (0x00)); // start out with: no drive 0,
                                                  // no drive 1

        int theChannel = 0;

        if (channels[theChannel].getDrives()[0].getDriveType() == ATADriveType.HARD_DISK) {

            // Flag drive type as Fh, use extended CMOS location as real type
            rtc.setCMOSRegister(0x12,
                    (byte) ((rtc.getCMOSRegister(0x12) & 0x0f) | 0xf0));

            rtc.setCMOSRegister(0x19, (byte) 47); // user definable type

            // AMI BIOS: 1st hard disk #cyl low byte
            rtc.setCMOSRegister(0x1b, (byte) (channels[theChannel]
                    .getSelectedDrive().getTotalNumCylinders() & 0x00ff));

            // AMI BIOS: 1st hard disk #cyl high byte
            rtc.setCMOSRegister(0x1c, (byte) ((channels[theChannel]
                    .getSelectedDrive().getTotalNumCylinders() & 0xff00) >> 8));

            // AMI BIOS: 1st hard disk #heads
            rtc.setCMOSRegister(0x1d, (byte) (channels[theChannel]
                    .getSelectedDrive().getTotalNumHeads()));

            // AMI BIOS: 1st hard disk write precompensation cylinder, low byte
            rtc.setCMOSRegister(0x1e, (byte) 0xff); // -1

            // AMI BIOS: 1st hard disk write precompensation cylinder, high byte
            rtc.setCMOSRegister(0x1f, (byte) 0xff); // -1

            // AMI BIOS: 1st hard disk control byte
            boolean condition = channels[theChannel].getSelectedDrive()
                    .getTotalNumHeads() > 8;
            int intCondition = 0;

            if (condition == true) {
                intCondition = 1;
            }
            rtc.setCMOSRegister(0x20, (byte) (0xc0 | (intCondition << 3)));

            // AMI BIOS: 1st hard disk landing zone, low byte
            rtc.setCMOSRegister(0x21, (byte) rtc.getCMOSRegister(0x1b));

            // AMI BIOS: 1st hard disk landing zone, high byte
            rtc.setCMOSRegister(0x22, (byte) rtc.getCMOSRegister(0x1c));

            // AMI BIOS: 1st hard disk sectors/track
            rtc.setCMOSRegister(0x23, (byte) channels[theChannel]
                    .getSelectedDrive().getTotalNumSectors());

        }

        // set up cmos for second hard drive
        if (channels[theChannel].getDrives()[1].getDriveType() == ATADriveType.HARD_DISK) {

            // BX_DEBUG(("1: I will put 0xf into the second hard disk field"));

            // fill in lower 4 bits of 0x12 for second HD
            rtc.setCMOSRegister(0x12,
                    (byte) ((rtc.getCMOSRegister(0x12) & 0xf0) | 0x0f));
            rtc.setCMOSRegister(0x1a, (byte) 47); // user definable type

            // AMI BIOS: 2nd hard disk #cyl low byte
            rtc.setCMOSRegister(0x24, (byte) (channels[theChannel]
                    .getSelectedDrive().getTotalNumCylinders() & 0x00ff));

            // AMI BIOS: 2nd hard disk #cyl high byte
            rtc.setCMOSRegister(0x25, (byte) ((channels[theChannel]
                    .getSelectedDrive().getTotalNumCylinders() & 0xff00) >> 8));

            // AMI BIOS: 2nd hard disk #heads
            rtc.setCMOSRegister(0x26, (byte) (channels[theChannel]
                    .getSelectedDrive().getTotalNumHeads()));

            // AMI BIOS: 2nd hard disk write precompensation cylinder, low byte
            rtc.setCMOSRegister(0x27, (byte) 0xff); // -1

            // AMI BIOS: 2nd hard disk write precompensation cylinder, high byte
            rtc.setCMOSRegister(0x28, (byte) 0xff); // -1

            // AMI BIOS: 2nd hard disk, 0x80 if heads>8
            rtc.setCMOSRegister(0x29, (byte) ((channels[theChannel]
                    .getSelectedDrive().getTotalNumHeads() > 8) ? 0x80 : 0x00));

            // AMI BIOS: 2nd hard disk landing zone, low byte
            rtc.setCMOSRegister(0x2a, (byte) rtc.getCMOSRegister(0x24));

            // AMI BIOS: 2nd hard disk landing zone, high byte
            rtc.setCMOSRegister(0x2b, (byte) rtc.getCMOSRegister(0x25));

            // AMI BIOS: 2nd hard disk sectors/track
            rtc.setCMOSRegister(0x2c, (byte) channels[theChannel]
                    .getSelectedDrive().getTotalNumSectors());
        }

        rtc.setCMOSRegister(0x39, (byte) 0);
        rtc.setCMOSRegister(0x3a, (byte) 0);

        for (int channelIndex = 0; channelIndex < channels.length; channelIndex++) {

            ATAChannel channel = channels[channelIndex];

            for (int driveIndex = 0; driveIndex < channel.getDrives().length; driveIndex++) {

                ATADrive curDrive = channel.getDrives()[driveIndex];

                if (curDrive.getDriveType() == ATADriveType.HARD_DISK) {

                    int cylinders = curDrive.getTotalNumCylinders();
                    int heads = curDrive.getTotalNumHeads();
                    int sectors = curDrive.getTotalNumSectors();

                    int reg = 0x39 + channelIndex / 2;
                    int bitshift = 2 * (driveIndex + (2 * (channelIndex % 2)));

                    ATATranslationType translationType = curDrive
                            .getTranslationType();

                    // Find the right translation if autodetect
                    if (translationType == ATATranslationType.AUTO) {

                        if ((cylinders <= 1024) && (heads <= 16)
                                && (sectors <= 63)) {
                            translationType = ATATranslationType.NONE;

                        } else if ((cylinders * heads) <= 131072) {
                            translationType = ATATranslationType.LARGE;

                        } else {
                            translationType = ATATranslationType.LBA;
                        }

                        logger.log(Level.CONFIG, "[" + super.getType() + "]"
                                + " Translation on IDE drive, channel = "
                                + channelIndex + " , drive index " + driveIndex
                                + " , set to " + translationType.toString());

                        // BX_INFO(("translation on ata%d-%d set to '%s'",channel,
                        // device,
                        // translation==BX_ATA_TRANSLATION_NONE?"none":
                        // translation==BX_ATA_TRANSLATION_LARGE?"large":
                        // "lba"));
                    }

                    // BOCHS Comments":
                    // FIXME we should test and warn
                    // - if LBA and spt != 63
                    // - if RECHS and heads != 16
                    // - if NONE and size > 1024*16*SPT blocks
                    // - if LARGE and size > 8192*16*SPT blocks
                    // - if RECHS and size > 1024*240*SPT blocks
                    // - if LBA and size > 1024*255*63, not that we can do much
                    // about it

                    if (translationType == ATATranslationType.NONE) {
                        rtc.setCMOSRegister(reg, (byte) (rtc
                                .getCMOSRegister(reg) | (0 << bitshift)));

                    } else if (translationType == ATATranslationType.LBA) {

                        rtc.setCMOSRegister(reg, (byte) (rtc
                                .getCMOSRegister(reg) | (1 << bitshift)));

                    } else if (translationType == ATATranslationType.LARGE) {

                        rtc.setCMOSRegister(reg, (byte) (rtc
                                .getCMOSRegister(reg) | (2 << bitshift)));

                    } else if (translationType == ATATranslationType.RECHS) {
                        rtc.setCMOSRegister(reg, (byte) (rtc
                                .getCMOSRegister(reg) | (3 << bitshift)));
                    }
                }
            }

        }

        // Set the "non-extended" boot device. This will default to DISKC if
        // cdrom

        if (bootDrives[0] != ATAConstants.BOOT_FLOPPYA) {
            // system boot sequence C:, A:
            int newValue = rtc.getCMOSRegister(0x2d) & 0xdf;
            rtc.setCMOSRegister(0x2d, (byte) newValue);

        } else { // 'a'

            // system boot sequence A:, C:
            int newValue = rtc.getCMOSRegister(0x2d) | 0x20;
            rtc.setCMOSRegister(0x2d, (byte) newValue);
        }

        // Set the "extended" boot sequence, bytes 0x38 and 0x3D (needed for
        // cdrom booting)
        // BX_INFO(("Using boot sequence %s, %s, %s",
        // bx_options.Obootdrive[0]->get_choice(bx_options.Obootdrive[0]->get ()
        // - 1),
        // bx_options.Obootdrive[1]->get_choice(bx_options.Obootdrive[1]->get
        // ()),
        // bx_options.Obootdrive[2]->get_choice(bx_options.Obootdrive[2]->get
        // ())
        // ));

        rtc.setCMOSRegister(0x3d, (byte) (bootDrives[0] | bootDrives[1] << 4));

        // Set the signature check flag in cmos, inverted for compatibility
        int intFloppySigCheckDisabled = 0;
        if (floppySigCheckDisabled == true) {
            intFloppySigCheckDisabled = 1;
        }

        rtc.setCMOSRegister(0x38,
                (byte) (intFloppySigCheckDisabled | (bootDrives[2] << 4)));

        // BX_INFO(("Floppy boot signature check is %sabled",
        // bx_options.OfloppySigCheck->get() ? "dis" : "en"));

        logger.log(Level.INFO, "[" + super.getType() + "]"
                + " Floppy boot signature check is set to "
                + floppySigCheckDisabled);

    }

    /**
     * Gets the current channel index.
     *
     * @return int the current channel index
     */
    public int getCurrentChannelIndex() {
        return this.curChannelIndex;
    }

    // ******************************************************************************
    // Custom Methods

    /**
     * Get the selected channel
     *
     * @return the selected channel
     */
    private ATAChannel getSelectedChannel() {
        return channels[this.curChannelIndex];
    }

    /**
     * Get the selected IDE drive
     *
     * @return the selected IDE drive
     */
    private ATADrive getSelectedDrive() {
        return getSelectedChannel().getSelectedDrive();

    }

    /**
     * Get the selected drive controller
     *
     * @return the selected drive controller
     */
    private ATADriveController getSelectedDriveController() {
        return getSelectedDrive().getControl();
    }

    /**
     * Load disk Image.
     *
     * @param imageFilePath
     *            the file path of the disk image
     * @return true if load successful
     */
    private boolean loadDiskImage(String imageFilePath, ATADrive drive) {
        try {

            File imageFile = new File(imageFilePath);
            drive.loadImage(imageFile);

            logger.log(Level.WARNING, "[" + super.getType() + "]" + "  Disk \""
                    + imageFile.getName() + "\" is successfully loaded.");
            return true;

        } catch (StorageDeviceException sde) {

            logger.log(Level.SEVERE, "[" + super.getType() + "]" + "  Error: "
                    + sde.getMessage());
            return false;
        }
    }

    /**
     * Get number of drives.
     *
     * @return the number of drives
     */
    private int getNumDrives() {

        int numDrives = 0;

        for (int i = 0; i < channels.length; i++) {

            for (int j = 0; j < channels[i].getDrives().length; j++) {
                if (channels[i].getDrives()[j].driveType != ATADriveType.NONE) {
                    numDrives++;
                }
            }
        }

        return numDrives;
    }

    /**
     * Return a byte from I/O address space at given port.
     *
     * @param originalPortAddress containing the address of the I/O port
     * @param ioLength the length of the Io
     * @return byte containing the data at given I/O address port
     *
     * @throws ModuleException
     * @throws ModuleUnknownPort
     * @throws ModuleWriteOnlyPortException
     */
    private byte[] read(int originalPortAddress, int ioLength)
            throws ModuleException, ModuleUnknownPort,
            ModuleWriteOnlyPortException {

        // logger.log(Level.CONFIG, "[" + super.getType() + "]" +
        // "  Read ide at instruction " + cpu.getCurrentInstructionNumber());

        byte[] value = new byte[ioLength];

        for (int i = 0; i < ioLength; i++) {
            value[i] = (byte) 0;
        }

        int channelIndex = ATAConstants.MAX_NUMBER_IDE_CHANNELS;
        int channelPort = 0xff; // undefined

        for (channelIndex = 0; channelIndex < ATAConstants.MAX_NUMBER_IDE_CHANNELS; channelIndex++) {

            if ((originalPortAddress & 0xfff8) == channels[channelIndex]
                    .getIoAddress1()) {
                channelPort = originalPortAddress
                        - channels[channelIndex].getIoAddress1();
                break;

            } else if ((originalPortAddress & 0xfff8) == channels[channelIndex]
                    .getIoAddress2()) {
                channelPort = originalPortAddress
                        - channels[channelIndex].getIoAddress2() + 0x10;
                break;
            }
        }

        if (channelIndex == ATAConstants.MAX_NUMBER_IDE_CHANNELS) {

            if ((originalPortAddress < 0x03f6)
                    || (originalPortAddress > 0x03f7)) {

                logger.log(Level.WARNING, "["
                        + super.getType()
                        + "]"
                        + "  read: unable to find ATA channel, ioport=0x%04x "
                        + Integer.toHexString(originalPortAddress)
                                .toUpperCase());
                return value;
            } else {
                channelIndex = 0;
                channelPort = originalPortAddress - 0x03e0;
            }
        }

        this.curChannelIndex = channelIndex;

        switch (channelPort) {
        case 0x00:
            // PORT_IDE_DATA:
            // hard disk data (16bit) 0x1f0

            value = this.executeGetCommand(originalPortAddress, ioLength);
            break;
        case 0x01:
            // PORT_IDE_ERROR_WPC
            // hard disk error register 0x1f1

            channels[this.curChannelIndex].getSelectedController().getStatus()
                    .setErr(0);
            if (channels[this.curChannelIndex].isAnyDrivePresent()) {
                value[0] = (byte) channels[this.curChannelIndex]
                        .getSelectedController().getErrorRegister();
            } else {
                value[0] = 0;
            }
            break;

        case 0x02:
            // PORT_IDE_SECTOR_COUNT
            // hard disk sector count / interrupt reason 0x1f2

            if (channels[this.curChannelIndex].isAnyDrivePresent()) {
                value[0] = (byte) channels[this.curChannelIndex]
                        .getSelectedDrive().getSectorCount();

                logger.log(Level.CONFIG, "[" + super.getType() + "]"
                        + "  Read sector count returned value " + value[0]);

            } else {
                value[0] = (byte) 0;
            }
            break;
        case 0x03:
            // PORT_IDE_SECTOR_NUMBER
            // sector number 0x1f3

            if (channels[this.curChannelIndex].isAnyDrivePresent()) {
                value[0] = (byte) channels[this.curChannelIndex]
                        .getSelectedDrive().getCurrentSector();
            } else {
                value[0] = 0;
            }
            break;

        case 0x04:
            // PORT_IDE_CYLINDER_LOW
            // cylinder low 0x1f4
            // -- WARNING : On real hardware the controller registers are shared
            // between drives.
            // So we must respond even if the select device is not present. Some
            // OS uses this fact
            // to detect the disks.... minix2 for example
            if (channels[this.curChannelIndex].isAnyDrivePresent()) {
                value[0] = (byte) (channels[this.curChannelIndex]
                        .getSelectedDrive().getCurrentCylinder() & 0x00ff);
            } else {
                value[0] = 0;
            }
            break;

        case 0x05:
            // PORT_IDE_CYLINDER_HIGH
            // cylinder high 0x1f5
            // -- WARNING : On real hardware the controller registers are shared
            // between drives.
            // So we must respond even if the select device is not present. Some
            // OS uses this fact
            // to detect the disks.... minix2 for example
            if (channels[this.curChannelIndex].isAnyDrivePresent()) {
                value[0] = (byte) (channels[this.curChannelIndex]
                        .getSelectedDrive().getCurrentCylinder() >> 8);

            } else {
                value[0] = 0;
            }
            break;

        case 0x06:
            // PORT_IDE_DRIVE_HEAD
            // hard disk drive and head register 0x1f6
            // b7 Extended data field for ECC
            // b6/b5: Used to be sector size. 00=256,01=512,10=1024,11=128
            // Since 512 was always used, bit 6 was taken to mean LBA mode:
            // b6 1=LBA mode, 0=CHS mode
            // b5 1
            // b4: DRV
            // b3..0 HD3..HD0

            int intlbaMode = 0;
            if (channels[this.curChannelIndex].getSelectedController()
                    .getLbaMode() > 0) {
                intlbaMode = 1;
            }

            value[0] = (byte) ((1 << 7) | (intlbaMode << 6)
                    | (1 << 5)
                    | // 01b = 512 sector size
                    (channels[this.curChannelIndex].getSelectedDriveIndex() << 4) | (channels[this.curChannelIndex]
                    .getSelectedDrive().getCurrentHead() << 0));

            break;

        case 0x07:
        case 0x16:
            // PORT_IDE_STATUS_CMD
            // Hard Disk Status 0x1f7
            // PORT_IDE_ALT_STATUS_DEVICE
            // Hard Disk Alternate Status 0x3f6

            if (!channels[this.curChannelIndex].isSelectedDrivePresent()) {
                // (mch) Just return zero for these registers
                value[0] = 0;

            } else {

                value[0] = (byte) ((channels[this.curChannelIndex]
                        .getSelectedController().getStatus().getBusy() << 7)
                        | (channels[this.curChannelIndex]
                                .getSelectedController().getStatus()
                                .getDriveReady() << 6)
                        | (channels[this.curChannelIndex]
                                .getSelectedController().getStatus()
                                .getWriteFault() << 5)
                        | (channels[this.curChannelIndex]
                                .getSelectedController().getStatus()
                                .getSeekComplete() << 4)
                        | (channels[this.curChannelIndex]
                                .getSelectedController().getStatus().getDrq() << 3)
                        | (channels[this.curChannelIndex]
                                .getSelectedController().getStatus()
                                .getCorrectedData() << 2)
                        | (channels[this.curChannelIndex]
                                .getSelectedController().getStatus()
                                .getIndexPulse() << 1) | (channels[this.curChannelIndex]
                        .getSelectedController().getStatus().getErr()));

                channels[this.curChannelIndex].getSelectedController()
                        .getStatus().incrementIndexPulseCount();
                channels[this.curChannelIndex].getSelectedController()
                        .getStatus().setIndexPulse(0);

                if (channels[this.curChannelIndex].getSelectedController()
                        .getStatus().getIndexPulseCount() >= ATAConstants.INDEX_PULSE_CYCLE) {
                    channels[this.curChannelIndex].getSelectedController()
                            .getStatus().setIndexPulse(1);
                    channels[this.curChannelIndex].getSelectedController()
                            .getStatus().setIndexPulseCount(0);
                }

            }

            if (channelPort == 0x07) {
                ModulePIC pic = (ModulePIC)super.getConnection(Type.PIC);
                pic.clearIRQ(getSelectedChannel().getIrqNumber());
            }

            break;

        case 0x17:
            // PORT_IDE_DRIVE_ADDRESS
            // Hard Disk Address Register 0x3f7
            // Obsolete and unsupported register. Not driven by hard
            // disk controller. Report all 1's. If floppy controller
            // is handling this address, it will call this function
            // set/clear D7 (the only bit it handles), then return
            // the combined value
            value[0] = (byte) 0xff;

            break;
        default:
            // Return dummy value 0xFF
            logger.log(Level.WARNING, "[" + super.getType() + "]" + "  Port [0x"
                    + Integer.toHexString(originalPortAddress).toUpperCase()
                    + "] returned default value 0xFF");
            return value;
        }

        logger.log(Level.CONFIG, "[" + super.getType() + "]"
                + "  Read from Port [0x"
                + Integer.toHexString(originalPortAddress).toUpperCase()
                + "] returned value " + value[0]);

        // Return value
        return value;

    }

    /**
     * Read sectors.
     *
     * @param originalAddress
     * @param ioLength
     * @return the data read.
     */
    @SuppressWarnings("empty-statement")
    private byte[] readSectors(int originalAddress, int ioLength) {
        // Clear buffer
        byte[] value = new byte[ioLength];
        for (int i = 0; i < ioLength; i++) {
            value[i] = (byte) 0;
        }

        if (channels[this.curChannelIndex].getSelectedController()
                .getBufferIndex() >= channels[this.curChannelIndex]
                .getSelectedController().getBufferSize()) {
            logger.log(Level.SEVERE, "[" + super.getType() + "]"
                    + "  IO read buffer_index >= 512, for address "
                    + originalAddress + ".");
            return value;
        }

        if (this.bulkIOQuantumsRequested > 0
                && ATAConstants.SUPPORT_REPEAT_SPEEDUPS) {

            int transferLen = 0;

            int quantumsMax = (channels[this.curChannelIndex]
                    .getSelectedController().getBufferSize() - getSelectedDriveController()
                    .getBufferIndex())
                    / ioLength;

            if (quantumsMax == 0) {
                logger.log(Level.SEVERE, "[" + super.getType() + "]"
                        + "  IO read not enough space for read for address "
                        + originalAddress + ".");
                ;

            }

            this.bulkIOQuantumsTransferred = this.bulkIOQuantumsRequested;

            if (quantumsMax < this.bulkIOQuantumsTransferred) {
                this.bulkIOQuantumsTransferred = quantumsMax;
            }

            transferLen = ioLength * this.bulkIOQuantumsTransferred;

            // TODO:
            // memcpy((Bit8u*) DEV_bulk_io_host_addr(),
            // &BX_SELECTED_CONTROLLER(channel).buffer[BX_SELECTED_CONTROLLER(channel).buffer_index],
            // transferLen);

            this.bulkIOHostAddr += transferLen;

            int newBufferIndex = channels[this.curChannelIndex]
                    .getSelectedController().getBufferIndex()
                    + transferLen;
            channels[this.curChannelIndex].getSelectedController()
                    .setBufferIndex(newBufferIndex);

        } else {
            value[0] = (byte) 0;

            if (ioLength >= 2) {
                value[0] = channels[this.curChannelIndex]
                        .getSelectedController().getBuffer()[channels[this.curChannelIndex]
                        .getSelectedController().getBufferIndex()];
                value[1] = channels[this.curChannelIndex]
                        .getSelectedController().getBuffer()[channels[this.curChannelIndex]
                        .getSelectedController().getBufferIndex() + 1];
            }

            if (ioLength == 4) {

                value[2] = channels[this.curChannelIndex]
                        .getSelectedController().getBuffer()[channels[this.curChannelIndex]
                        .getSelectedController().getBufferIndex() + 2];

                value[3] = channels[this.curChannelIndex]
                        .getSelectedController().getBuffer()[channels[this.curChannelIndex]
                        .getSelectedController().getBufferIndex() + 3];

                long full32bitintvalue = (((long) value[0]) & 0xFF)
                        + (((long) (value[1]) & 0xFF) << 8)
                        + (((long) (value[2]) & 0xFF) << 16)
                        + (((long) (value[3]) & 0xFF) << 24);

                logger.log(Level.CONFIG, "["
                        + super.getType()
                        + "]"
                        + "  IO read returns values for index "
                        + channels[this.curChannelIndex]
                                .getSelectedController().getBufferIndex() + " "
                        + value[0] + ", " + value[1] + ", " + value[2]
                        + " and " + value[3] + " = " + full32bitintvalue);

            }

            int newBufferIndex = channels[this.curChannelIndex]
                    .getSelectedController().getBufferIndex()
                    + ioLength;
            channels[this.curChannelIndex].getSelectedController()
                    .setBufferIndex(newBufferIndex);

        }

        // if buffer completely read
        if (channels[this.curChannelIndex].getSelectedController()
                .getBufferIndex() >= channels[this.curChannelIndex]
                .getSelectedController().getBufferSize()) {

            // update sector count, sector number, cylinder,
            // drive, head, status
            // if there are more sectors, read next one in...
            //
            if ((byte) channels[this.curChannelIndex].getSelectedController()
                    .getCurrentCommand() == (byte) 0xC4) {

                if (channels[this.curChannelIndex].getSelectedDrive()
                        .getSectorCount() > channels[this.curChannelIndex]
                        .getSelectedController().getMultipleSectors()) {
                    channels[this.curChannelIndex].getSelectedController()
                            .setBufferSize(
                                    channels[this.curChannelIndex]
                                            .getSelectedController()
                                            .getMultipleSectors() * 512);
                } else {
                    channels[this.curChannelIndex].getSelectedController()
                            .setBufferSize(
                                    channels[this.curChannelIndex]
                                            .getSelectedDrive()
                                            .getSectorCount() * 512);
                }
            }

            getSelectedDriveController().getStatus().setBusy(0);
            getSelectedDriveController().getStatus().setDriveReady(1);
            getSelectedDriveController().getStatus().setWriteFault(0);
            getSelectedDriveController().getStatus().setSeekComplete(1);
            getSelectedDriveController().getStatus().setCorrectedData(0);
            getSelectedDriveController().getStatus().setErr(0);

            if (getSelectedDrive().getSectorCount() == 0) {
                getSelectedDriveController().getStatus().setDrq(0);
            } else { /* read next one into controller buffer */

                getSelectedDriveController().getStatus().setDrq(1);
                getSelectedDriveController().getStatus().setSeekComplete(1);

                if (ideReadData(curChannelIndex, getSelectedDriveController()
                        .getBuffer(), getSelectedDriveController()
                        .getBufferSize())) {
                    getSelectedDriveController().setBufferIndex(0);
                    raiseInterrupt(curChannelIndex);
                }
            }

        }

        return value;
    }

    /**
     * Write data of length ioLength.
     *
     * @param originalAddress
     * @param data
     * @param ioLength
     * @return true if successful / false if write failed
     * @throws ModuleException
     * @throws ModuleUnknownPort
     */
    private boolean write(int originalAddress, byte[] data, int ioLength)
            throws ModuleException, ModuleUnknownPort {

        // logger.log(Level.CONFIG, "[" + super.getType() + "]" +
        // "  Write ide at instruction " + cpu.getCurrentInstructionNumber());

        int channelIndex = ATAConstants.MAX_NUMBER_IDE_CHANNELS;
        int channelPort = 0xff; // undefined

        int[] intData = new int[data.length];

        for (int i = 0; i < data.length; i++) {
            intData[i] = ((int) data[i]) & 0xFF;
        }

        Integer logicalSector = new Integer(0); // Make Integer object so can
                                                // pass by reference
        int ret = 0;
        boolean prevControlReset = false;

        for (channelIndex = 0; channelIndex < ATAConstants.MAX_NUMBER_IDE_CHANNELS; channelIndex++) {

            if ((originalAddress & 0xfff8) == channels[channelIndex]
                    .getIoAddress1()) {
                channelPort = originalAddress
                        - channels[channelIndex].getIoAddress1();
                break;

            } else if ((originalAddress & 0xfff8) == channels[channelIndex]
                    .getIoAddress2()) {
                channelPort = originalAddress
                        - channels[channelIndex].getIoAddress2() + 0x10;
                break;
            }
        }

        if (channelIndex == ATAConstants.MAX_NUMBER_IDE_CHANNELS) {

            if (originalAddress != 0x03f6) {
                logger.log(Level.SEVERE, "[" + super.getType() + "]"
                        + "  write: unable to find ATA channel, ioport "
                        + originalAddress + ".");
                // BX_PANIC(("write: unable to find ATA channel, ioport=0x%04x",
                // address));
                return false;

            } else {
                channelIndex = 0;
                channelPort = originalAddress - 0x03e0;
            }
        }

        this.curChannelIndex = channelIndex;

        switch (channelPort) {
        // PORT_IDE_DATA
        // hard disk data (16bit) 0x1f0 (original byte value 0x00)
        case 0x00: // 0x1f0

            this.executeSetCommand(originalAddress, intData, ioLength);
            break;

        case 0x01: // hard disk write precompensation 0x1f1
            // PORT_IDE_ERROR_WPC
            // hard disk error register 0x1f1 (original byte value 0x01)
            getSelectedChannel().getDrives()[0].setFeatures(intData[0]);
            getSelectedChannel().getDrives()[1].setFeatures(intData[0]);
            break;

        case 0x02: // hard disk sector count 0x1f2
            // PORT_IDE_SECTOR_COUNT
            // hard disk sector count 0x1f2 (original byte value 0x03)
            getSelectedChannel().getDrives()[0].setSectorCount(intData[0]);
            getSelectedChannel().getDrives()[1].setSectorCount(intData[0]);
            break;

        case 0x03: // hard disk sector number 0x1f3
            // PORT_IDE_SECTOR_NUMBER
            // hard disk sector number 0x1f3 (original byte value 0x04)
            getSelectedChannel().getDrives()[0].setCurrentSector(intData[0]);
            getSelectedChannel().getDrives()[1].setCurrentSector(intData[0]);
            break;

        case 0x04: // hard disk cylinder low 0x1f4
            // PORT_IDE_CYLINDER_LOW
            // hard disk cylinder low 0x1f4 (original byte value 0x04)
        {
            int newValue0 = (int) ((getSelectedChannel().getDrives()[0]
                    .getCurrentCylinder() & 0xff00) | intData[0]);
            getSelectedChannel().getDrives()[0].setCurrentCylinder(newValue0);

            int newValue1 = (int) ((getSelectedChannel().getDrives()[1]
                    .getCurrentCylinder() & 0xff00) | intData[0]);
            getSelectedChannel().getDrives()[1].setCurrentCylinder(newValue1);
        }
            break;

        case 0x05: // hard disk cylinder high 0x1f5
            // PORT_IDE_CYLINDER_HIGH
            // hard disk cylinder high 0x1f5 (original byte value 0x05)
        {
            int newValue0 = (int) ((intData[0] << 8) | (getSelectedChannel()
                    .getDrives()[0].getCurrentCylinder() & 0xff));
            getSelectedChannel().getDrives()[0].setCurrentCylinder(newValue0);

            int newValue1 = (int) ((intData[0] << 8) | (getSelectedChannel()
                    .getDrives()[1].getCurrentCylinder() & 0xff));
            getSelectedChannel().getDrives()[1].setCurrentCylinder(newValue1);
        }
            break;

        case 0x06: // hard disk drive and head register 0x1f6
            // PORT_IDE_DRIVE_HEAD
            this.setPortIdeDriveHead(originalAddress, intData);

            break;

        case 0x07: // hard disk command 0x1f7
            // (mch) Writes to the command register with drive_select != 0
            // are ignored if no secondary device is present
            // PORT_IDE_STATUS_CMD
            // hard disk command 0x1f7 (original byte value 0x07)
            this.setHardDiskCommand(originalAddress, intData, logicalSector,
                    ret);
            break;

        case 0x16: // 0x3f6
            // PORT_IDE_ALT_STATUS_DEVICE
            this.setPortIdeAltStatusDevice(originalAddress, prevControlReset,
                    intData);
            break;

        default:
            // Return dummy value 0xFF
            logger.log(Level.WARNING, "[" + super.getType() + "]"
                    + "  Returned default value 0xFF");
            return false;
        }

        return true;
    }

    /**
     * Write sectors of hard drive.
     *
     * @param originalAddress
     * @param data
     * @param channel
     * @param ioLength
     */
    private void writeSectors(int originalAddress, int[] data, int channel,
            int ioLength) {

        if (getSelectedDriveController().getBufferIndex() >= 512) {
            logger.log(Level.SEVERE, "[" + super.getType() + "]"
                    + "  IO write to address " + originalAddress
                    + " buffer index >= 512.");
        }

        if (bulkIOQuantumsRequested > 0 && ATAConstants.SUPPORT_REPEAT_SPEEDUPS) {

            int transferLen = 0;
            int quantumsMax = 0;

            quantumsMax = (512 - getSelectedDriveController().getBufferIndex())
                    / ioLength;

            if (quantumsMax == 0) {
                logger.log(Level.SEVERE, "[" + super.getType() + "]"
                        + "  IO write to address " + originalAddress
                        + " not enough space for write.");
            }

            bulkIOQuantumsTransferred = bulkIOQuantumsRequested;

            if (quantumsMax < bulkIOQuantumsTransferred) {
                bulkIOQuantumsTransferred = quantumsMax;
            }

            transferLen = ioLength * bulkIOQuantumsTransferred;

            // memcpy(&BX_SELECTED_CONTROLLER(channel).buffer[BX_SELECTED_CONTROLLER(channel).buffer_index],
            // (Bit8u*) DEV_bulk_io_host_addr(),
            // transferLen);

            bulkIOHostAddr += transferLen;

            int newBufferIndex = getSelectedDriveController().getBufferIndex();
            newBufferIndex += transferLen;
            getSelectedDriveController().setBufferIndex(newBufferIndex);

        } else {

            if (ioLength >= 2) {
                getSelectedDriveController().setBuffer(
                        getSelectedDriveController().getBufferIndex() + 1,
                        (byte) data[1]);
                getSelectedDriveController().setBuffer(
                        getSelectedDriveController().getBufferIndex(),
                        (byte) data[0]);
            }

            if (ioLength == 4) {
                getSelectedDriveController().setBuffer(
                        getSelectedDriveController().getBufferIndex() + 3,
                        (byte) data[3]);
                getSelectedDriveController().setBuffer(
                        getSelectedDriveController().getBufferIndex() + 2,
                        (byte) data[2]);
            }

            int newBufferIndex = getSelectedDriveController().getBufferIndex();
            newBufferIndex += ioLength;
            getSelectedDriveController().setBufferIndex(newBufferIndex);

        }

        /* if buffer completely written */
        if (getSelectedDriveController().getBufferIndex() >= getSelectedDriveController()
                .getBufferSize()) {

            if (ideWriteData(curChannelIndex, getSelectedDriveController()
                    .getBuffer(), getSelectedDriveController().getBufferSize())) {

                if (getSelectedDriveController().getCurrentCommand() == (byte) 0xC5) {
                    if (getSelectedDrive().getSectorCount() > getSelectedDriveController()
                            .getMultipleSectors()) {
                        getSelectedDriveController().setBufferSize(
                                getSelectedDriveController()
                                        .getMultipleSectors() * 512);

                    } else {
                        getSelectedDriveController().setBufferSize(
                                getSelectedDrive().getSectorCount() * 512);
                    }
                }
                getSelectedDriveController().setBufferIndex(0);

                /*
                 * When the write is complete, controller clears the DRQ bit and
                 * sets the BSY bit. If at least one more sector is to be
                 * written, controller sets DRQ bit, clears BSY bit, and issues
                 * IRQ
                 */

                if (getSelectedDrive().getSectorCount() != 0) {

                    getSelectedDriveController().getStatus().setBusy(0);
                    getSelectedDriveController().getStatus().setDriveReady(1);
                    getSelectedDriveController().getStatus().setDrq(1);
                    getSelectedDriveController().getStatus()
                            .setCorrectedData(0);
                    getSelectedDriveController().getStatus().setErr(0);

                } else {
                    /* no more sectors to write */
                    getSelectedDriveController().getStatus().setBusy(0);
                    getSelectedDriveController().getStatus().setDriveReady(1);
                    getSelectedDriveController().getStatus().setDrq(0);
                    getSelectedDriveController().getStatus().setErr(0);
                    getSelectedDriveController().getStatus()
                            .setCorrectedData(0);
                }
                raiseInterrupt(channel);

            }
        }

        return;

    }

    /**
     * Execute get command.
     *
     * @param originalAddress
     * @param ioLength
     * @return the value got.
     */
    private byte[] executeGetCommand(int originalAddress, int ioLength) {

        byte[] value = new byte[ioLength];
        for (int i = 0; i < ioLength; i++) {
            value[i] = (byte) 0;
        }

        int currentCommand = getSelectedDriveController().getCurrentCommand();

        // Get the command associated with the address
        ATACommand command = ATACommand.getCommand((byte) currentCommand);

        if (getSelectedDriveController().getStatus().getDrq() == 0) {

            logger.log(Level.SEVERE, "[" + super.getType() + "]" + "  IO read("
                    + originalAddress + ") with drq == 0: last command was "
                    + command.toString() + ", address " + command.getAddress()
                    + " IO Length " + ioLength);

            return value;
        }

        if (command != null) {
            logger.log(Level.CONFIG, "[" + super.getType() + "]" + "  IO read("
                    + originalAddress + ") current command is "
                    + command.toString() + ",address " + command.getAddress()
                    + " IO Length " + ioLength);
        }

        // Check if the command is recognised and supported
        if (command == null) {
            logger.log(Level.SEVERE, "[" + super.getType() + "]" + "  read cmd "
                    + currentCommand + " not recognised");

            // value = (byte)0xFF;

            return value;

        } else if (command == ATACommand.READ_SECTORS_WITH_RETRY // case 0x20:
                                                                 // // READ
                                                                 // SECTORS,
                                                                 // with retries
                || command == ATACommand.READ_SECTORS_WITHOUT_RETRY // case
                                                                    // 0x21: //
                                                                    // READ
                                                                    // SECTORS,
                                                                    // without
                                                                    // retries
                || command == ATACommand.READ_MULTIPLE) // case 0xC4: // READ
                                                        // MULTIPLE SECTORS
        {

            value = this.readSectors(originalAddress, ioLength);

        } else if (command == ATACommand.IDENTIFY_DRIVE // TODO: this is the
                                                        // code from bochs to
                                                        // check
                || command == ATACommand.PACKET_A1) {
            // IDENTIFY DEVICE
            // case 0xa1:

            channels[this.curChannelIndex].getSelectedController().getStatus()
                    .setBusy(0);
            channels[this.curChannelIndex].getSelectedController().getStatus()
                    .setDriveReady(1);
            channels[this.curChannelIndex].getSelectedController().getStatus()
                    .setWriteFault(0);
            channels[this.curChannelIndex].getSelectedController().getStatus()
                    .setSeekComplete(1);
            channels[this.curChannelIndex].getSelectedController().getStatus()
                    .setCorrectedData(0);
            channels[this.curChannelIndex].getSelectedController().getStatus()
                    .setErr(0);

            int index = channels[this.curChannelIndex].getSelectedController()
                    .getBufferIndex();

            value[0] = channels[this.curChannelIndex].getSelectedController()
                    .getBuffer()[index];
            index++;

            if (ioLength >= 2) {
                value[1] = channels[this.curChannelIndex]
                        .getSelectedController().getBuffer()[index];

                logger.log(Level.INFO, "[" + super.getType() + "]"
                        + "  IO identify drive for index " + (index - 1)
                        + " returns values " + value[0] + " and " + value[1]);
                index++;
            }
            if (ioLength == 4) {
                value[2] = channels[this.curChannelIndex]
                        .getSelectedController().getBuffer()[index];
                value[3] = channels[this.curChannelIndex]
                        .getSelectedController().getBuffer()[index + 1];
                index += 2;
            }

            channels[this.curChannelIndex].getSelectedController()
                    .setBufferIndex(index);

            if (channels[this.curChannelIndex].getSelectedController()
                    .getBufferIndex() >= 512) {

                channels[this.curChannelIndex].getSelectedController()
                        .getStatus().setDrq(0);
            }
        }
        // 0x0A0
        else if (command == ATACommand.PACKET_A0) {

            int index = channels[this.curChannelIndex].getSelectedController()
                    .getBufferIndex();
            int increment = 0;

            // Load block if necessary
            if (index >= channels[this.curChannelIndex].getSelectedController()
                    .getBufferSize()) {
                if (index > channels[this.curChannelIndex]
                        .getSelectedController().getBufferSize()) {
                    logger
                            .log(
                                    Level.WARNING,
                                    "["
                                            + super.getType()
                                            + "]"
                                            + "  Read IO Packet A0 - index is greater than the bufferr size.");
                }

                switch (getSelectedDrive().getAtpi().getCommand()) {
                case 0x28: // read (10)
                case 0xa8: // read (12)
                case 0xbe: // read cd

                    logger.log(Level.SEVERE, "[" + super.getType() + "]"
                            + "  Read with no LOWLEVEL_CDROM.");
                    break;

                default: // no need to load a new block
                    break;
                }
            }

            value[0] = channels[this.curChannelIndex].getSelectedController()
                    .getBuffer()[index + increment];
            increment++;

            if (ioLength >= 2) {
                value[1] = channels[this.curChannelIndex]
                        .getSelectedController().getBuffer()[index + increment];
                increment++;
            }
            if (ioLength == 4) {
                value[2] = channels[this.curChannelIndex]
                        .getSelectedController().getBuffer()[index + increment];
                value[3] = channels[this.curChannelIndex]
                        .getSelectedController().getBuffer()[index + increment
                        + 1];
                increment += 2;
            }

            channels[this.curChannelIndex].getSelectedController()
                    .setBufferIndex(index + increment);

            int newDrqIndex = channels[this.curChannelIndex]
                    .getSelectedController().getDrqIndex()
                    + increment;
            channels[this.curChannelIndex].getSelectedController().setDrqIndex(
                    newDrqIndex);

            if (channels[this.curChannelIndex].getSelectedController()
                    .getDrqIndex() >= Math.abs(channels[this.curChannelIndex]
                    .getSelectedDrive().getAtpi().getDrqBytes())) {

                channels[this.curChannelIndex].getSelectedController()
                        .getStatus().setDrq(0);
                channels[this.curChannelIndex].getSelectedController()
                        .setDrqIndex(0);

                int newTotalBytesRemaining = channels[this.curChannelIndex]
                        .getSelectedDrive().getAtpi().getTotalBytesRemaining()
                        - channels[this.curChannelIndex].getSelectedDrive()
                                .getAtpi().getDrqBytes();

                channels[this.curChannelIndex].getSelectedDrive().getAtpi()
                        .setTotalBytesRemaining(newTotalBytesRemaining);

                if (channels[this.curChannelIndex].getSelectedDrive().getAtpi()
                        .getTotalBytesRemaining() > 0) {

                    // one or more blocks remaining (works only for single block
                    // commands)
                    channels[this.curChannelIndex].getSelectedController()
                            .getInterruptReason().setIo(1);
                    channels[this.curChannelIndex].getSelectedController()
                            .getStatus().setBusy(0);
                    channels[this.curChannelIndex].getSelectedController()
                            .getStatus().setDrq(1);
                    channels[this.curChannelIndex].getSelectedController()
                            .getInterruptReason().setCd(0);

                    // set new byte count if last block
                    if (channels[this.curChannelIndex].getSelectedDrive()
                            .getAtpi().getTotalBytesRemaining() < channels[this.curChannelIndex]
                            .getSelectedController().getByteCount()) {

                        int totalBytesRemaining = channels[this.curChannelIndex]
                                .getSelectedDrive().getAtpi()
                                .getTotalBytesRemaining();
                        channels[this.curChannelIndex].getSelectedController()
                                .setByteCount(totalBytesRemaining);
                    }

                    int byteCount = channels[this.curChannelIndex]
                            .getSelectedController().getByteCount();
                    channels[this.curChannelIndex].getSelectedDrive().getAtpi()
                            .setDrqBytes(byteCount);

                    raiseInterrupt(curChannelIndex);

                } else {
                    // all bytes read
                    channels[this.curChannelIndex].getSelectedController()
                            .getInterruptReason().setIo(1);
                    channels[this.curChannelIndex].getSelectedController()
                            .getInterruptReason().setCd(1);
                    channels[this.curChannelIndex].getSelectedController()
                            .getStatus().setDriveReady(1);
                    channels[this.curChannelIndex].getSelectedController()
                            .getInterruptReason().setRel(0);
                    channels[this.curChannelIndex].getSelectedController()
                            .getStatus().setBusy(0);
                    channels[this.curChannelIndex].getSelectedController()
                            .getStatus().setDrq(0);
                    channels[this.curChannelIndex].getSelectedController()
                            .getStatus().setErr(0);

                    raiseInterrupt(curChannelIndex);

                }
            }

        } else {
            logger.log(Level.WARNING, "[" + super.getType() + "]"
                    + "  IO read current command " + command.getAddress()
                    + " (" + command.getName() + ") not supported.");
        }

        return value;
    }

    /**
     * Abort the current command.
     *
     * @param channel
     *            the current channel
     * @param value
     *            the current data value
     */
    private void abortCommand(int channel, int value) {
        logger.log(Level.WARNING, "[" + super.getType() + "]"
                + "  aborting: channel " + channel + "data " + value + ".");

        getSelectedDriveController().setCurrentCommand(0);
        getSelectedDriveController().getStatus().setBusy(0);
        getSelectedDriveController().getStatus().setDriveReady(1);
        getSelectedDriveController().getStatus().setErr(1);
        getSelectedDriveController().setErrorRegister(0x04); // command ABORTED
        getSelectedDriveController().getStatus().setDrq(0);
        getSelectedDriveController().getStatus().setSeekComplete(0);
        getSelectedDriveController().getStatus().setCorrectedData(0);
        getSelectedDriveController().setBufferIndex(0);

        raiseInterrupt(channel);
    }

    /**
     * Raise an Interrupt.
     *
     * @param channel
     *            the current channel
     */
    private void raiseInterrupt(int channel) {

        logger.log(Level.CONFIG, "[" + super.getType() + "]"
                + "  raise interrupt called, disable_irq = "
                + getSelectedDriveController().isDisableIrq() + ".");

        if (!getSelectedDriveController().isDisableIrq()) {
            logger.log(Level.INFO, "[" + super.getType() + "]"
                    + "  raising interrupt.");

        } else {
            logger.log(Level.INFO, "[" + super.getType() + "]"
                    + "  not raising interrupt.");
        }

        if (!getSelectedDriveController().isDisableIrq()) {
            int irq = getSelectedChannel().getIrqNumber();

            logger.log(Level.INFO, "[" + super.getType() + "]"
                    + "  Raising interrupt " + irq + " on channel " + channel
                    + ".");

            // PCI support is normally set to false
            if (ATAConstants.SUPPORTS_PCI) {
                // DEV_ide_bmdma_set_irq(channel); // TODO:
            }

            ModulePIC pic = (ModulePIC)super.getConnection(Type.PIC);
            pic.setIRQ(irq);

        } else {
            // TODO:

        }
    }

    /**
     * Set Port Ide Drive Head.
     *
     * @param originalAddress
     * @param data
     */
    private void setPortIdeDriveHead(int originalAddress, int[] data) {
        // hard disk drive and head register 0x1f6 (original byte value 0x06)
        // b7 Extended data field for ECC
        // b6/b5: Used to be sector size. 00=256,01=512,10=1024,11=128
        // Since 512 was always used, bit 6 was taken to mean LBA mode:
        // b6 1=LBA mode, 0=CHS mode
        // b5 1
        // b4: DRV
        // b3..0 HD3..HD0

        if ((data[0] & 0xa0) != 0xa0) // 1x1xxxxx
        {
            logger.log(Level.WARNING, "[" + super.getType() + "]"
                    + "  IO write to address " + originalAddress + ", data "
                    + data + " invalid as not 1x1xxxxxb.");
        }

        getSelectedChannel().setSelectedDriveIndex((data[0] >> 4) & 0x01);

        int driveSelect = getSelectedChannel().getSelectedDriveIndex();

        // write current head number
        int newHeadValue = data[0] & 0xf;
        getSelectedChannel().getDrives()[0].setCurrentHead(newHeadValue);
        getSelectedChannel().getDrives()[1].setCurrentHead(newHeadValue);

        if (getSelectedChannel().getSelectedController().getLbaMode() == 0
                && ((data[0] >> 6) & 1) == 1) {
            logger.log(Level.INFO, "[" + super.getType() + "]"
                    + "  Enabling LBA mode.");
        }

        int newLdaValue = (int) ((data[0] >> 6) & 1);
        getSelectedChannel().getDrives()[0].getControl()
                .setLbaMode(newLdaValue);
        getSelectedChannel().getDrives()[1].getControl()
                .setLbaMode(newLdaValue);

        if (getSelectedDrive().getDriveType() == ATADriveType.NONE) {
            logger.log(Level.WARNING, "[" + super.getType() + "]"
                    + "  ATA device for channel " + curChannelIndex
                    + " set to " + driveSelect + " which does not exist.");
        }

    }

    /**
     * Port IDE alt Status Device (0x3f6)
     *
     * @param originalAddress
     * @param prevControlReset
     * @param data
     */
    private void setPortIdeAltStatusDevice(int originalAddress,
            boolean prevControlReset, int[] data) {
        // hard disk adapter control 0x3f6 (original byte value 0x16)
        // (mch) Even if device 1 was selected, a write to this register
        // goes to device 0 (if device 1 is absent)

        prevControlReset = getSelectedDriveController().isReset();

        boolean resetValue = false;
        if ((int) (data[0] & 0x04) > 0) {
            resetValue = true;
        }
        getSelectedChannel().getDrives()[0].getControl().setReset(resetValue);
        getSelectedChannel().getDrives()[1].getControl().setReset(resetValue);

        boolean disableIrqValue = false;
        if ((int) (data[0] & 0x02) > 0) {
            disableIrqValue = true;
        }
        getSelectedChannel().getDrives()[0].getControl().setDisableIrq(
                disableIrqValue);
        getSelectedChannel().getDrives()[1].getControl().setDisableIrq(
                disableIrqValue);

        logger.log(Level.CONFIG, "[" + super.getType() + "]"
                + "  Adapter control reg: reset controller set to "
                + getSelectedDriveController().isReset() + " for channel "
                + curChannelIndex + ".");

        logger.log(Level.CONFIG, "[" + super.getType() + "]"
                + "  Adapter control reg: disable_irq(X) set to "
                + getSelectedDriveController().isDisableIrq() + " for channel "
                + curChannelIndex + ".");

        if (!prevControlReset && getSelectedDriveController().isReset()) {

            // transition from 0 to 1 causes all drives to reset
            logger.log(Level.CONFIG, "[" + super.getType() + "]"
                    + "  Hard drive: RESET.");

            // (mch) Set BSY, drive not ready
            for (int driveId = 0; driveId < 2; driveId++) {

                getSelectedChannel().getDrives()[driveId].getControl()
                        .getStatus().setBusy(1);
                getSelectedChannel().getDrives()[driveId].getControl()
                        .getStatus().setDriveReady(0);
                getSelectedChannel().getDrives()[driveId].getControl()
                        .setResetInProgress(1);

                getSelectedChannel().getDrives()[driveId].getControl()
                        .getStatus().setWriteFault(0);
                getSelectedChannel().getDrives()[driveId].getControl()
                        .getStatus().setSeekComplete(1);
                getSelectedChannel().getDrives()[driveId].getControl()
                        .getStatus().setDrq(0);
                getSelectedChannel().getDrives()[driveId].getControl()
                        .getStatus().setCorrectedData(0);
                getSelectedChannel().getDrives()[driveId].getControl()
                        .getStatus().setErr(0);

                getSelectedChannel().getDrives()[driveId].getControl()
                        .setErrorRegister(0x01); // diagnostic code: no error

                getSelectedChannel().getDrives()[driveId].getControl()
                        .setCurrentCommand(0x00);
                getSelectedChannel().getDrives()[driveId].getControl()
                        .setBufferIndex(0);

                getSelectedChannel().getDrives()[driveId].getControl()
                        .setMultipleSectors(0);
                getSelectedChannel().getDrives()[driveId].getControl()
                        .setLbaMode(0);

                getSelectedChannel().getDrives()[driveId].getControl()
                        .setDisableIrq(false);

                // TODO as per BOCHS this code is inside a loop, but seems unnecessary
                ModulePIC pic = (ModulePIC)super.getConnection(Type.PIC);
                pic.clearIRQ(getSelectedChannel().getIrqNumber());
            }

        } else if (getSelectedDriveController().getResetInProgress() > 0
                && !getSelectedDriveController().isReset()) {
            // Clear BSY and DRDY

            logger.log(Level.INFO, "[" + super.getType() + "]"
                    + "  Reset complete for channel " + curChannelIndex);

            for (int driveId = 0; driveId < 2; driveId++) {
                getSelectedChannel().getDrives()[driveId].getControl()
                        .getStatus().setBusy(0);
                getSelectedChannel().getDrives()[driveId].getControl()
                        .getStatus().setDriveReady(1);
                getSelectedChannel().getDrives()[driveId].getControl()
                        .setResetInProgress(0);

                // TODO: again inside loop?
                setSignature(curChannelIndex);
            }
        }
        return;
    }

    /**
     * Execute command of IDC Note: assumed is that all bytes of the command are
     * fetched. After execution of the command, the IDC will automatically enter
     * the result or idle phase.
     *
     * @param originalAddress
     * @param data
     * @param ioLength
     */
    private void executeSetCommand(int originalAddress, int[] data, int ioLength) {

        int currentCommand = getSelectedDriveController().getCurrentCommand();

        // Get the command associated with the address
        ATACommand command = ATACommand.getCommand((byte) currentCommand);

        // Check if the command is recognised and supported
        if (command == null) {
            logger.log(Level.SEVERE, "[" + super.getType() + "]" + "  write cmd "
                    + currentCommand + " not recognised");

            return;
        }

        else if (command == ATACommand.WRITE_SECTORS_WITH_RETRY
                || command == ATACommand.WRITE_MULTIPLE) {
            writeSectors(originalAddress, data, curChannelIndex, ioLength);

        } else if (command == ATACommand.PACKET_A0) {
            setPacketA0(originalAddress, curChannelIndex, data, ioLength);
        } else {
            logger.log(Level.SEVERE, "[" + super.getType() + "]" + "  write cmd "
                    + command.getAddress() + " (" + command.getName()
                    + ") not supported");

            return;
        }
    }

    /**
     * Read Data from disk image into buffer.
     *
     * @param channel
     * @param buffer
     * @param bufferSize
     * @return true if read successful / false if failed
     */
    private boolean ideReadData(int channel, byte[] buffer, int bufferSize) {

        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Type.MOTHERBOARD);

        int logicalSector = 0;

        int sectorCount = (bufferSize / 512);

        do {
            if (!getSelectedDrive().calculateLogicalAddress(logicalSector)) {

                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + "  read sector reached invalid sector "
                        + logicalSector + " aborting.");
                abortCommand(channel, channels[this.curChannelIndex]
                        .getSelectedController().getCurrentCommand());
                return false;
            }

            logicalSector = getSelectedDrive().calculateLogicalAddress();

            getSelectedDrive().setIoLightCounter(5);

            motherboard.resetTimer(this, updateInterval);
            motherboard.setTimerActiveState(this, true);

            try {

                // Update status to emulator
                emu.statusChanged(Emulator.MODULE_ATA_HD1_TRANSFER_START);

                byte[] theData = getSelectedDrive().readData(
                        getSelectedDriveController().getBuffer(),
                        logicalSector * 512, 512);

                for (int i = 0; i < theData.length; i++) {
                    getSelectedDriveController().setBuffer(i, theData[i]);
                }

                // Update status to emulator
                emu.statusChanged(Emulator.MODULE_ATA_HD1_TRANSFER_STOP);

            } catch (IOException e) {
                logger.log(Level.SEVERE, "[" + super.getType() + "]"
                        + "  logical sector was " + logicalSector + ".");
                logger.log(Level.SEVERE, "[" + super.getType() + "]"
                        + "  could not read() hard drive image file at byte "
                        + logicalSector * 512 + ".");

                abortCommand(channel, channels[channel].getSelectedController()
                        .getCurrentCommand());
                return false;
            }
            channels[channel].getSelectedDrive().incrementAddress();

        } while (--sectorCount > 0);

        return true;
    }

    /**
     * Write data from buffer to disk image.
     *
     * @param channel
     * @param buffer
     * @param bufferSize
     * @return true if read successful / false if failed
     */
    private boolean ideWriteData(int channel, byte[] buffer, int bufferSize) {

        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Type.MOTHERBOARD);

        int logicalSector = 0;

        int sectorCount = (bufferSize / 512);

        do {

            if (!getSelectedDrive().calculateLogicalAddress(logicalSector)) {

                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + "  IO write reached invalid sector " + logicalSector
                        + ", aborting.");
                abortCommand(channel, getSelectedDriveController()
                        .getCurrentCommand());
                return false;
            }

            logicalSector = getSelectedDrive().calculateLogicalAddress();

            getSelectedDrive().setIoLightCounter(5);
            motherboard.resetTimer(this, updateInterval);
            motherboard.setTimerActiveState(this, true);

            try {
                // Update status to emulator
                emu.statusChanged(Emulator.MODULE_ATA_HD1_TRANSFER_START);

                getSelectedDrive().writeData(
                        getSelectedDriveController().getBuffer(),
                        logicalSector * 512, 512);

                // Update status to emulator
                emu.statusChanged(Emulator.MODULE_ATA_HD1_TRANSFER_STOP);

            } catch (IOException e) {
                logger
                        .log(
                                Level.SEVERE,
                                "["
                                        + super.getType()
                                        + "]"
                                        + "  IO write could not write()hard drive image file at byte "
                                        + logicalSector * 512 + ", aborting.");

                abortCommand(channel, getSelectedDriveController()
                        .getCurrentCommand());
                return false;
            }

            /*
             * update sector count, sector number, cylinder, drive, head, status
             * if there are more sectors, read next one in...
             */
            getSelectedDrive().incrementAddress();

        } while (--sectorCount > 0);

        return true;
    }

    /**
     * Set Packet A0 command.
     *
     * @param originalAddress
     * @param channel
     * @param data
     * @param ioLength
     */
    private void setPacketA0(int originalAddress, int channel, int[] data,
            int ioLength) {

        if (getSelectedDriveController().getBufferIndex() >= ATAConstants.PACKET_SIZE) {
            logger.log(Level.SEVERE, "[" + super.getType() + "]"
                    + "  IO write to adress, " + originalAddress
                    + " buffer_index >= PACKET_SIZE.");
            return;
        }

        if (ioLength >= 2) {
            getSelectedDriveController().setBuffer(
                    getSelectedDriveController().getBufferIndex() + 1,
                    (byte) data[1]);
            getSelectedDriveController().setBuffer(
                    getSelectedDriveController().getBufferIndex(),
                    (byte) data[0]);

        }

        if (ioLength == 4) {
            getSelectedDriveController().setBuffer(
                    getSelectedDriveController().getBufferIndex() + 3,
                    (byte) data[3]);
            getSelectedDriveController().setBuffer(
                    getSelectedDriveController().getBufferIndex() + 2,
                    (byte) data[2]);

        }

        int newBufferIndex = getSelectedDriveController().getBufferIndex();
        newBufferIndex += ioLength;
        getSelectedDriveController().setBufferIndex(newBufferIndex);

        /* if packet completely writtten */
        if (getSelectedDriveController().getBufferIndex() >= ATAConstants.PACKET_SIZE) {
            // complete command received
            byte atapiCommand = getSelectedDriveController().getBuffer()[0];
            getSelectedDriveController().setBufferSize(2048);

            switch (atapiCommand) {
            case (byte) 0x00: // test unit ready

                if (getSelectedDrive().getCdRom().isReady()) {
                    atapiCmdNop(channel);

                } else {
                    atapiCmdError(channel, SenseType.NOT_READY,
                            AscType.MEDIUM_NOT_PRESENT, false);
                }
                raiseInterrupt(channel);

                break;
            case (byte) 0x03: // request sense
            {

                int allocLength = getSelectedDriveController().getBuffer()[4];

                initSendAtapiCommand(channel, atapiCommand, 18, allocLength,
                        false);

                // sense data
                getSelectedDriveController().setBuffer(0,
                        (byte) (0x70 | (1 << 7)));
                getSelectedDriveController().setBuffer(1, (byte) (0));
                getSelectedDriveController().setBuffer(
                        2,
                        (byte) getSelectedDrive().getSenseInfo().getSenseKey()
                                .getValue());
                getSelectedDriveController().setBuffer(
                        3,
                        (byte) getSelectedDrive().getSenseInfo()
                                .getInformation()[0]);
                getSelectedDriveController().setBuffer(
                        4,
                        (byte) getSelectedDrive().getSenseInfo()
                                .getInformation()[1]);
                getSelectedDriveController().setBuffer(
                        5,
                        (byte) getSelectedDrive().getSenseInfo()
                                .getInformation()[2]);
                getSelectedDriveController().setBuffer(
                        6,
                        (byte) getSelectedDrive().getSenseInfo()
                                .getInformation()[3]);
                getSelectedDriveController().setBuffer(7, (byte) (17 - 7)); // TODO:
                                                                            // 17
                                                                            // -7
                                                                            // ???
                getSelectedDriveController().setBuffer(
                        8,
                        (byte) getSelectedDrive().getSenseInfo()
                                .getSpecificInf()[0]);
                getSelectedDriveController().setBuffer(
                        9,
                        (byte) getSelectedDrive().getSenseInfo()
                                .getSpecificInf()[1]);
                getSelectedDriveController().setBuffer(
                        10,
                        (byte) getSelectedDrive().getSenseInfo()
                                .getSpecificInf()[2]);
                getSelectedDriveController().setBuffer(
                        11,
                        (byte) getSelectedDrive().getSenseInfo()
                                .getSpecificInf()[3]);
                getSelectedDriveController().setBuffer(
                        12,
                        (byte) getSelectedDrive().getSenseInfo().getAsc()
                                .getValue());
                getSelectedDriveController().setBuffer(13,
                        (byte) getSelectedDrive().getSenseInfo().getAscq());
                getSelectedDriveController().setBuffer(14,
                        (byte) getSelectedDrive().getSenseInfo().getFruc());
                getSelectedDriveController()
                        .setBuffer(
                                15,
                                (byte) getSelectedDrive().getSenseInfo()
                                        .getKeySpec()[0]);
                getSelectedDriveController()
                        .setBuffer(
                                16,
                                (byte) getSelectedDrive().getSenseInfo()
                                        .getKeySpec()[1]);
                getSelectedDriveController()
                        .setBuffer(
                                17,
                                (byte) getSelectedDrive().getSenseInfo()
                                        .getKeySpec()[2]);

                readyToSendAtapi(channel);
            }
                break;

            case (byte) 0x1b: // start stop unit
            {
                // bx_bool Immed = (BX_SELECTED_CONTROLLER(channel).buffer[1] >>
                // 0) & 1; //BOCHS comment
                int loEj = (getSelectedDriveController().getBuffer()[4] >> 1) & 1;
                int start = (getSelectedDriveController().getBuffer()[4] >> 0) & 1;

                if (!(loEj > 0) && !(start > 0)) { // stop the disc
                    // FIXME: Stop disc not implemented

                    logger.log(Level.SEVERE, "[" + super.getType() + "]"
                            + "  Stop disc not implemented.");

                    atapiCmdNop(channel);
                    raiseInterrupt(channel);

                } else if (!(loEj > 0) && (start > 0)) { // start (spin up) the
                                                         // disc

                    if (ATAConstants.IS_LOW_LEVEL_CDROM) {
                        // TODO:
                        // BX_SELECTED_DRIVE(channel).cdrom.cd->start_cdrom();
                    }

                    // TODO: ATAPI start disc not reading TOC
                    logger.log(Level.SEVERE, "[" + super.getType() + "]"
                            + "  ATAPI start disc not reading TOC.");
                    atapiCmdNop(channel);
                    raiseInterrupt(channel);

                } else if ((loEj > 0) && !(start > 0)) { // Eject the disc

                    atapiCmdNop(channel);

                    if (getSelectedDrive().getCdRom().isReady()) {
                        if (ATAConstants.IS_LOW_LEVEL_CDROM) {
                            // BX_SELECTED_DRIVE(channel).cdrom.cd->eject_cdrom();
                        }

                        getSelectedDrive().getCdRom().setReady(false);
                        // TODO: If you want to update GUI for CD-ROM, then put
                        // it in here!
                    }
                    raiseInterrupt(channel);
                } else { // Load the disc
                    // My guess is that this command only closes the tray,
                    // that's a no-op for us
                    atapiCmdNop(channel);
                    raiseInterrupt(channel);
                }
            }
                break;
            case (byte) 0xbd: // mechanism status
            {

                int allocLength = 0;
                // allocLength =
                // read_16bit(BgetSelectedDriveController().getBuffer() + 8);
                // TODO:check 8 is the offset
                allocLength = read16bit(getSelectedDriveController()
                        .getBuffer(), 8);

                if (allocLength == 0) {
                    logger
                            .log(
                                    Level.SEVERE,
                                    "["
                                            + super.getType()
                                            + "]"
                                            + "  Not implemented: Zero allocation length to MECHANISM STATUS.");
                    return;
                }

                initSendAtapiCommand(channel, atapiCommand, 8, allocLength,
                        false);

                getSelectedDriveController().setBuffer(0, (byte) 0); // reserved
                                                                     // for non
                                                                     // changers
                getSelectedDriveController().setBuffer(1, (byte) 0); // reserved
                                                                     // for non
                                                                     // changers

                // TODO: seems to be set on dummy values
                getSelectedDriveController().setBuffer(2, (byte) 0); // Current
                                                                     // LBA
                getSelectedDriveController().setBuffer(3, (byte) 0); // Current
                                                                     // LBA
                getSelectedDriveController().setBuffer(4, (byte) 0); // Current
                                                                     // LBA

                getSelectedDriveController().setBuffer(5, (byte) 1); // one slot

                getSelectedDriveController().setBuffer(6, (byte) 0); // slot
                                                                     // table
                                                                     // length
                getSelectedDriveController().setBuffer(7, (byte) 0); // slot
                                                                     // table
                                                                     // length

                readyToSendAtapi(channel);
            }
                break;

            case (byte) 0x5a: // mode sense
            {

                int allocLength = 0;

                // int allocLength =
                // read_16bit(getSelectedDriveController().getBuffer() + 7);
                // TODO: check the offset 7
                allocLength = read16bit(getSelectedDriveController()
                        .getBuffer(), 7);

                int pc = getSelectedDriveController().getBuffer()[2] >> 6;
                int pageCode = getSelectedDriveController().getBuffer()[2] & 0x3f;

                switch (pc) {
                case 0x0: // current values
                    switch (pageCode) {
                    case 0x01: // error recovery

                        initSendAtapiCommand(channel, atapiCommand,
                                (getSelectedDrive().getCdRom()
                                        .getErrorRecovery().length + 8),
                                allocLength, false);

                        // TODO: error recove is by ref
                        // initModeSenseSingle(channel,
                        // &BX_SELECTED_DRIVE(channel).cdrom.current.error_recovery,
                        // sizeof(error_recovery_t));

                        // Object[] array =
                        // getSelectedDrive().getCdRom().getErrorRecovery();
                        // TODO:
                        // initModeSenseSingle(channel,
                        // getSelectedDrive().getCdRom().getErrorRecovery(),
                        // getSelectedDrive().getCdRom().getErrorRecovery().length);

                        readyToSendAtapi(channel);
                        break;

                    case 0x2a: // CD-ROM capabilities & mech. status

                        initSendAtapiCommand(channel, atapiCommand, 28,
                                allocLength, false);

                        // TODO: check this
                        Object[] dataAsArray = { getSelectedDriveController()
                                .getBuffer()[8] };

                        // TODO buffer 8 is by ref
                        initModeSenseSingle(channel, dataAsArray, 28);

                        getSelectedDriveController().setBuffer(8, (byte) 0x2a);
                        getSelectedDriveController().setBuffer(9, (byte) 0x12);
                        getSelectedDriveController().setBuffer(10, (byte) 0x00);
                        getSelectedDriveController().setBuffer(11, (byte) 0x00);
                        // Multisession, Mode 2 Form 2, Mode 2 Form 1
                        getSelectedDriveController().setBuffer(12, (byte) 0x70);
                        getSelectedDriveController().setBuffer(13,
                                (byte) (3 << 5));
                        getSelectedDriveController().setBuffer(
                                14,
                                (byte) (1
                                        | // TODO: previously char
                                        (getSelectedDrive().getCdRom()
                                                .isLocked() ? (1 << 1) : 0)
                                        | (1 << 3) | (1 << 5)));
                        getSelectedDriveController().setBuffer(15, (byte) 0x00);
                        getSelectedDriveController().setBuffer(16,
                                (byte) ((706 >> 8) & 0xff));
                        getSelectedDriveController().setBuffer(17,
                                (byte) (706 & 0xff));
                        getSelectedDriveController().setBuffer(18, (byte) 0);
                        getSelectedDriveController().setBuffer(19, (byte) 2);
                        getSelectedDriveController().setBuffer(20,
                                (byte) ((512 >> 8) & 0xff));
                        getSelectedDriveController().setBuffer(21,
                                (byte) (512 & 0xff));
                        getSelectedDriveController().setBuffer(22,
                                (byte) ((706 >> 8) & 0xff));
                        getSelectedDriveController().setBuffer(23,
                                (byte) (706 & 0xff));
                        getSelectedDriveController().setBuffer(24, (byte) 0);
                        getSelectedDriveController().setBuffer(25, (byte) 0);
                        getSelectedDriveController().setBuffer(26, (byte) 0);
                        getSelectedDriveController().setBuffer(27, (byte) 0);
                        readyToSendAtapi(channel);
                        break;

                    case 0x0d: // CD-ROM
                    case 0x0e: // CD-ROM audio control
                    case 0x3f: // all
                        logger.log(Level.SEVERE, "[" + super.getType() + "]"
                                + "  cdrom: MODE SENSE (curr), page code "
                                + pageCode + ", not implemented yet.");
                        atapiCmdError(channel, SenseType.ILLEGAL_REQUEST,
                                AscType.INV_FIELD_IN_CMD_PACKET, true);
                        raiseInterrupt(channel);
                        break;

                    default:
                        // not implemeted by this device
                        logger.log(Level.CONFIG, "[" + super.getType() + "]"
                                + "  cdrom: MODE SENSE pc " + pc
                                + ", page code, " + pageCode
                                + ", not implemented by device.");
                        atapiCmdError(channel, SenseType.ILLEGAL_REQUEST,
                                AscType.INV_FIELD_IN_CMD_PACKET, true);
                        raiseInterrupt(channel);
                        break;
                    }
                    break;

                case 0x1: // changeable values
                    switch (pageCode) {
                    case 0x01: // error recovery
                    case 0x0d: // CD-ROM
                    case 0x0e: // CD-ROM audio control
                    case 0x2a: // CD-ROM capabilities & mech. status
                    case 0x3f: // all

                        logger.log(Level.SEVERE, "[" + super.getType() + "]"
                                + "  cdrom: MODE SENSE (chg), page code "
                                + pageCode + ", not implemented yet.");
                        atapiCmdError(channel, SenseType.ILLEGAL_REQUEST,
                                AscType.INV_FIELD_IN_CMD_PACKET, true);
                        raiseInterrupt(channel);
                        break;

                    default:
                        // not implemeted by this device

                        logger.log(Level.CONFIG, "[" + super.getType() + "]"
                                + "  cdrom: MODE SENSE PC " + pc
                                + ", page code " + pageCode
                                + ", not implemented by device.");
                        atapiCmdError(channel, SenseType.ILLEGAL_REQUEST,
                                AscType.INV_FIELD_IN_CMD_PACKET, true);
                        raiseInterrupt(channel);
                        break;
                    }
                    break;

                case 0x2: // default values
                    switch (pageCode) {
                    case 0x2a: // CD-ROM capabilities & mech. status, copied
                               // from current values
                        initSendAtapiCommand(channel, atapiCommand, 28,
                                allocLength, false);

                        // initModeSenseSingle(channel,
                        // &BX_SELECTED_CONTROLLER(channel).buffer[8], 28);
                        // TODO: by ref input

                        Object[] dummy = { getSelectedDriveController()
                                .getBuffer()[8] };
                        initModeSenseSingle(channel, dummy, 28);

                        getSelectedDriveController()
                                .setBuffer(8, (byte) (0x2a));
                        getSelectedDriveController()
                                .setBuffer(9, (byte) (0x12));
                        getSelectedDriveController().setBuffer(10,
                                (byte) (0x00));
                        getSelectedDriveController().setBuffer(11,
                                (byte) (0x00));
                        // Multisession, Mode 2 Form 2, Mode 2 Form 1
                        getSelectedDriveController().setBuffer(12,
                                (byte) (0x70));
                        getSelectedDriveController().setBuffer(13,
                                (byte) (3 << 5));

                        // getSelectedDriveController().setBuffer(14, (unsigned
                        // char) (1 | //TODO: check use char
                        getSelectedDriveController().setBuffer(
                                14,
                                (byte) (1
                                        | (getSelectedDrive().getCdRom()
                                                .isLocked() ? (1 << 1) : 0)
                                        | (1 << 3) | (1 << 5)));

                        getSelectedDriveController().setBuffer(15,
                                (byte) (0x00));
                        getSelectedDriveController().setBuffer(16,
                                (byte) ((706 >> 8) & 0xff));
                        getSelectedDriveController().setBuffer(17,
                                (byte) (706 & 0xff));
                        getSelectedDriveController().setBuffer(18, (byte) 0);
                        getSelectedDriveController().setBuffer(19, (byte) 2);
                        getSelectedDriveController().setBuffer(20,
                                (byte) ((512 >> 8) & 0xff));
                        getSelectedDriveController().setBuffer(21,
                                (byte) (512 & 0xff));
                        getSelectedDriveController().setBuffer(22,
                                (byte) ((706 >> 8) & 0xff));
                        getSelectedDriveController().setBuffer(23,
                                (byte) (706 & 0xff));
                        getSelectedDriveController().setBuffer(24, (byte) 0);
                        getSelectedDriveController().setBuffer(25, (byte) 0);
                        getSelectedDriveController().setBuffer(26, (byte) 0);
                        getSelectedDriveController().setBuffer(27, (byte) 0);

                        readyToSendAtapi(channel);
                        break;

                    case 0x01: // error recovery
                    case 0x0d: // CD-ROM
                    case 0x0e: // CD-ROM audio control
                    case 0x3f: // all

                        logger.log(Level.SEVERE, "[" + super.getType() + "]"
                                + "  cdrom: MODE SENSE (dflt), page code "
                                + pageCode + ", not implemented.");

                        atapiCmdError(channel, SenseType.ILLEGAL_REQUEST,
                                AscType.INV_FIELD_IN_CMD_PACKET, true);
                        raiseInterrupt(channel);
                        break;

                    default:
                        // not implemeted by this device
                        logger.log(Level.CONFIG, "[" + super.getType() + "]"
                                + "  cdrom: MODE SENSE PC " + pc
                                + ", page code " + pageCode
                                + ", not implemented by device.");

                        atapiCmdError(channel, SenseType.ILLEGAL_REQUEST,
                                AscType.INV_FIELD_IN_CMD_PACKET, true);
                        raiseInterrupt(channel);
                        break;
                    }
                    break;

                case 0x3: // saved values not implemented
                    atapiCmdError(channel, SenseType.ILLEGAL_REQUEST,
                            AscType.SAVING_PARAMETERS_NOT_SUPPORTED, true);
                    raiseInterrupt(channel);
                    break;

                default:
                    logger
                            .log(
                                    Level.SEVERE,
                                    "["
                                            + super.getType()
                                            + "]"
                                            + "  IO Write setPacketA0 pc value not recognised.");

                    break;
                }
            }
                break;

            case (byte) 0x12: // inquiry
            {
                int allocLength = getSelectedDriveController().getBuffer()[4];

                initSendAtapiCommand(channel, atapiCommand, 36, allocLength,
                        false);

                getSelectedDriveController().setBuffer(0, (byte) 0x05); // CD-ROM
                getSelectedDriveController().setBuffer(1, (byte) 0x80); // Removable
                getSelectedDriveController().setBuffer(2, (byte) 0x00); // ISO,
                                                                        // ECMA,
                                                                        // ANSI
                                                                        // version
                getSelectedDriveController().setBuffer(3, (byte) 0x21); // ATAPI-2,
                                                                        // as
                                                                        // specified
                getSelectedDriveController().setBuffer(4, (byte) 31); // additional
                                                                      // length
                                                                      // (total
                                                                      // 36)
                                                                      // TODO:
                                                                      // 0x is
                                                                      // missing
                                                                      // in
                                                                      // BOCHS?
                getSelectedDriveController().setBuffer(5, (byte) 0x00); // reserved
                getSelectedDriveController().setBuffer(6, (byte) 0x00); // reserved
                getSelectedDriveController().setBuffer(7, (byte) 0x00); // reserved

                // Vendor ID
                String tempVendorId = "Undefined   ";
                char[] vendorId = tempVendorId.toCharArray();

                for (int i = 0; i < 8; i++) {
                    getSelectedDriveController().setBuffer(8 + i,
                            (byte) vendorId[i]);
                }

                // Product ID
                String tempProductId = "Compatible CD-ROM    ";
                char[] productId = tempProductId.toCharArray();

                for (int i = 0; i < 16; i++) {
                    getSelectedDriveController().setBuffer(16 + i,
                            (byte) productId[i]);
                }

                // Product Revision level
                String tempRevLevel = "1.0 ";
                char[] revLevel = tempRevLevel.toCharArray();

                for (int i = 0; i < 4; i++) {
                    getSelectedDriveController().setBuffer(32 + i,
                            (byte) revLevel[i]);
                }

                readyToSendAtapi(channel);
            }
                break;
            case (byte) 0x25: // read cd-rom capacity
            {
                // no allocation length???
                initSendAtapiCommand(channel, atapiCommand, 8, 8, false);

                if (getSelectedDrive().getCdRom().isReady()) {
                    int capacity = getSelectedDrive().getCdRom().getCapacity() - 1;
                    getSelectedDriveController().setBuffer(0,
                            (byte) ((capacity >> 24) & 0xff));
                    getSelectedDriveController().setBuffer(1,
                            (byte) ((capacity >> 16) & 0xff));
                    getSelectedDriveController().setBuffer(2,
                            (byte) ((capacity >> 8) & 0xff));
                    getSelectedDriveController().setBuffer(3,
                            (byte) ((capacity >> 0) & 0xff));
                    getSelectedDriveController().setBuffer(4,
                            (byte) ((2048 >> 24) & 0xff));
                    getSelectedDriveController().setBuffer(5,
                            (byte) ((2048 >> 16) & 0xff));
                    getSelectedDriveController().setBuffer(6,
                            (byte) ((2048 >> 8) & 0xff));
                    getSelectedDriveController().setBuffer(7,
                            (byte) ((2048 >> 0) & 0xff));

                    readyToSendAtapi(channel);

                } else {
                    atapiCmdError(channel, SenseType.NOT_READY,
                            AscType.MEDIUM_NOT_PRESENT, true);
                    raiseInterrupt(channel);
                }
            }
                break;
            case (byte) 0xbe: // read cd
            {
                if (getSelectedDrive().getCdRom().isReady()) {
                    int lba = 0;
                    // TODO int lba =
                    // read_32bit(getSelectedDriveController().getBuffer() + 2);
                    lba = read32bit(getSelectedDriveController().getBuffer(), 2);

                    int transferLength = getSelectedDriveController()
                            .getBuffer()[8]
                            | (getSelectedDriveController().getBuffer()[7] << 8)
                            | (getSelectedDriveController().getBuffer()[6] << 16);

                    int transferReq = getSelectedDriveController().getBuffer()[9];
                    if (transferLength == 0) {
                        atapiCmdNop(channel);
                        raiseInterrupt(channel);
                        break;
                    }
                    switch (transferReq & 0xf8) {
                    case 0x00:
                        atapiCmdNop(channel);
                        raiseInterrupt(channel);
                        break;

                    case 0xf8:
                        getSelectedDriveController().setBufferSize(2352);
                        // TODO: No break here in BOCHS

                    case 0x10:

                        initSendAtapiCommand(channel, atapiCommand,
                                transferLength
                                        * getSelectedDriveController()
                                                .getBufferSize(),
                                transferLength
                                        * getSelectedDriveController()
                                                .getBufferSize(), true);

                        getSelectedDrive().getCdRom().setRemainingBlocks(
                                transferLength);
                        getSelectedDrive().getCdRom().setNextLba(lba);

                        if (getSelectedDriveController().getPacketDma() == 0) {
                            readyToSendAtapi(channel);
                        }
                        break;

                    default:
                        logger.log(Level.SEVERE, "[" + super.getType() + "]"
                                + "  Read CD: unknown format.");

                        atapiCmdError(channel, SenseType.ILLEGAL_REQUEST,
                                AscType.INV_FIELD_IN_CMD_PACKET, true);
                        raiseInterrupt(channel);
                    }
                } else {
                    atapiCmdError(channel, SenseType.NOT_READY,
                            AscType.MEDIUM_NOT_PRESENT, true);
                    raiseInterrupt(channel);
                }
            }
                break;

            case (byte) 0x43: // read toc
            {
                int msf = 0;

                if (getSelectedDrive().getCdRom().isReady()) {
                    if (ATAConstants.IS_LOW_LEVEL_CDROM) {
                        msf = (getSelectedDriveController().getBuffer()[1] >> 1) & 1;
                        int startingTrack = getSelectedDriveController()
                                .getBuffer()[6];
                        int toc_length;
                    }

                    @SuppressWarnings("unused")
                    int allocLength = 0;
                    // int allocLength =
                    // read_16bit(getSelectedDriveController().getBuffer() + 7);
                    // TODO: 7 - offset?
                    allocLength = read16bit(getSelectedDriveController()
                            .getBuffer(), 7);

                    int format = (getSelectedDriveController().getBuffer()[9] >> 6);

                    // Win32: I just read the TOC using Win32's IOCTRL functions
                    // (Ben)
                    if (ATAConstants.WITH_WIN32) {

                        if (ATAConstants.IS_LOW_LEVEL_CDROM) {

                            switch (format) {
                            case 2:
                            case 3:
                            case 4:
                                if (msf != 1) {
                                    logger
                                            .log(
                                                    Level.SEVERE,
                                                    "["
                                                            + super.getType()
                                                            + "]"
                                                            + "  READ_TOC_EX: msf not set for format "
                                                            + format + ".");
                                }
                            case 0:
                            case 1:
                            case 5:
                                // TODO:
                                // if
                                // (!(BX_SELECTED_DRIVE(channel).cdrom.cd->read_toc(BX_SELECTED_CONTROLLER(channel).buffer,
                                // &toc_length, msf, starting_track, format)))
                                // {
                                //
                                // atapiCmdError(channel,
                                // SenseType.ILLEGAL_REQUEST,
                                // AscType.INV_FIELD_IN_CMD_PACKET, true);
                                // raiseInterrupt(channel);
                                // } else
                                // {
                                // initSendAtapiCommand(channel, atapi_command,
                                // toc_length, alloc_length);
                                // readyToSendAtapi(channel);
                                // }
                                break;
                            default:
                                logger.log(Level.SEVERE, "[" + super.getType()
                                        + "]" + "  READ TOC format " + format
                                        + " not supported.");
                                atapiCmdError(channel,
                                        SenseType.ILLEGAL_REQUEST,
                                        AscType.INV_FIELD_IN_CMD_PACKET, true);
                                raiseInterrupt(channel);
                            }
                        } else {
                            logger.log(Level.SEVERE, "[" + super.getType() + "]"
                                    + "  LOWLEVEL_CDROM not defined.");
                        }

                    } else { // WIN32

                        switch (format) {
                        case 0:
                        case 1:
                        case 2:
                            if (ATAConstants.IS_LOW_LEVEL_CDROM) {
                                // TODO:
                                /*
                                 * if
                                 * (!(BX_SELECTED_DRIVE(channel).cdrom.cd->read_toc
                                 * (BX_SELECTED_CONTROLLER(channel).buffer,
                                 * &tocLength, msf, startingTrack, format))) {
                                 * atapiCmdError(channel,
                                 * SenseType.ILLEGAL_REQUEST,
                                 * AscType.INV_FIELD_IN_CMD_PACKET, true);
                                 * raiseInterrupt(channel); } else {
                                 * initSendAtapiCommand(channel, atapi_command,
                                 * toc_length, alloc_length);
                                 * readyToSendAtapi(channel); }
                                 */
                            } else {
                                logger
                                        .log(
                                                Level.SEVERE,
                                                "["
                                                        + super.getType()
                                                        + "]"
                                                        + "  LOWLEVEL_CDROM not defined.");
                            }
                            break;

                        default:
                            logger.log(Level.SEVERE, "[" + super.getType() + "]"
                                    + "  READ TOC format " + format
                                    + " not supported.");
                            atapiCmdError(channel, SenseType.ILLEGAL_REQUEST,
                                    AscType.INV_FIELD_IN_CMD_PACKET, true);
                            raiseInterrupt(channel);
                            break;
                        }
                    } // WIN32

                } else {
                    atapiCmdError(channel, SenseType.NOT_READY,
                            AscType.MEDIUM_NOT_PRESENT, true);
                    raiseInterrupt(channel);
                }
            }
                break;

            case (byte) 0x28: // read (10)
            case (byte) 0xa8: // read (12)
            {
                int transferLength = 0;

                if (atapiCommand == 0x28) {
                    // transfer_length =
                    // read_16bit(getSelectedDriveController().getBuffer() + 7);
                    // TODO: 7 - offset?
                    transferLength = read16bit(getSelectedDriveController()
                            .getBuffer(), 7);

                } else {
                    // transfer_length =
                    // read_32bit(getSelectedDriveController().getBuffer() + 6);
                    // TODO: 6 - offset?
                    transferLength = read32bit(getSelectedDriveController()
                            .getBuffer(), 6);
                }

                int lba = 0;

                // int lba = read_32bit(getSelectedDriveController().getBuffer()
                // + 2);
                // TODO: 2 - offset?
                lba = read32bit(getSelectedDriveController().getBuffer(), 2);

                if (!getSelectedDrive().getCdRom().isReady()) {
                    atapiCmdError(channel, SenseType.NOT_READY,
                            AscType.MEDIUM_NOT_PRESENT, true);
                    raiseInterrupt(channel);

                    break;
                }

                // Ben: see comment below
                if (lba + transferLength > getSelectedDrive().getCdRom()
                        .getCapacity()) {
                    transferLength = (getSelectedDrive().getCdRom()
                            .getCapacity() - lba);
                }

                // if (transferLength == 0) { //BOCHS Comment
                if (transferLength <= 0) {
                    atapiCmdNop(channel);
                    raiseInterrupt(channel);
                    logger.log(Level.CONFIG, "[" + super.getType() + "]"
                            + "  Read with transfer length <= 0, ok ("
                            + transferLength + ")");
                    break;
                }

                /*
                 * Ben: I commented this out and added the three lines above. I
                 * am not sure this is the correct thing to do, but it seems to
                 * work. FIXME: I think that if the transfer_length is more than
                 * we can transfer, we should return some sort of
                 * flag/error/bitrep stating so. I haven't read the atapi specs
                 * enough to know what needs to be done though. if (lba +
                 * transfer_length > BX_SELECTED_DRIVE(channel).cdrom.capacity)
                 * { atapi_cmd_error(channel, SENSE_ILLEGAL_REQUEST,
                 * ASC_LOGICAL_BLOCK_OOR, 1); raise_interrupt(channel); break; }
                 */
                logger.log(Level.CONFIG, "[" + super.getType() + "]"
                        + "  cdrom: READ LBA " + lba + ", length "
                        + transferLength + ".");

                // handle command
                initSendAtapiCommand(channel, atapiCommand,
                        transferLength * 2048, transferLength * 2048, true);

                getSelectedDrive().getCdRom()
                        .setRemainingBlocks(transferLength);
                getSelectedDrive().getCdRom().setNextLba(lba);

                // TODO: deal with packet DMA
                if (getSelectedDriveController().getPacketDma() == 0) {
                    readyToSendAtapi(channel);
                }
            }
                break;

            case (byte) 0x2b: // seek
            {

                int lba = 0;
                // lba = read_32bit(BX_SELECTED_CONTROLLER(channel).buffer + 2);
                // TODO: offset - 2?
                lba = read32bit(getSelectedDriveController().getBuffer(), 2);

                if (!getSelectedDrive().getCdRom().isReady()) {
                    atapiCmdError(channel, SenseType.NOT_READY,
                            AscType.MEDIUM_NOT_PRESENT, true);
                    raiseInterrupt(channel);
                    break;
                }

                if (lba > getSelectedDrive().getCdRom().getCapacity()) {
                    atapiCmdError(channel, SenseType.ILLEGAL_REQUEST,
                            AscType.LOGICAL_BLOCK_OOR, true);
                    raiseInterrupt(channel);
                    break;

                }
                if (ATAConstants.IS_LOW_LEVEL_CDROM) {
                    // BX_SELECTED_DRIVE(channel).cdrom.cd->seek(lba); //TODO:

                } else {
                    // BX_PANIC(("Seek with no LOWLEVEL_CDROM"));

                    logger.log(Level.SEVERE, "[" + super.getType() + "]"
                            + "  Seek with no LOWLEVEL_CDROM.");

                }

                atapiCmdNop(channel);
                raiseInterrupt(channel);
            }
                break;
            case (byte) 0x1e: // prevent/allow medium removal
            {

                if (getSelectedDrive().getCdRom().isReady()) {

                    boolean newIsLocked = (getSelectedDriveController()
                            .getBuffer()[4] & 1) > 0;
                    getSelectedDrive().getCdRom().setLocked(newIsLocked);
                    atapiCmdNop(channel);

                } else {
                    atapiCmdError(channel, SenseType.NOT_READY,
                            AscType.MEDIUM_NOT_PRESENT, true);
                }
                raiseInterrupt(channel);
            }
                break;

            case (byte) 0x42: // read sub-channel
            {
                @SuppressWarnings("unused")
                int msf = getPacketField(channel, 1, 1, 1);
                int subQ = getPacketField(channel, 2, 6, 1);

                int dataFormat = getPacketByte(channel, 3);
                @SuppressWarnings("unused")
                int trackNumber = getPacketByte(channel, 6);
                int allocLength = getPacketWord(channel, 7);

                int retLength = 4; // header size

                if (!getSelectedDrive().getCdRom().isReady()) {
                    atapiCmdError(channel, SenseType.NOT_READY,
                            AscType.MEDIUM_NOT_PRESENT, true);
                    raiseInterrupt(channel);

                } else {

                    getSelectedDriveController().setBuffer(0, (byte) 0);
                    getSelectedDriveController().setBuffer(1, (byte) 0); // audio
                                                                         // not
                                                                         // supported
                    getSelectedDriveController().setBuffer(2, (byte) 0);
                    getSelectedDriveController().setBuffer(3, (byte) 0);

                    if (subQ > 0) { // !sub_q == header only
                        if ((dataFormat == 2) || (dataFormat == 3)) { // UPC or
                                                                      // ISRC
                            retLength = 24;
                            getSelectedDriveController().setBuffer(4,
                                    (byte) dataFormat);

                            if (dataFormat == 3) {
                                getSelectedDriveController().setBuffer(5,
                                        (byte) 0x14);
                                getSelectedDriveController().setBuffer(6,
                                        (byte) 1);
                            }

                            getSelectedDriveController().setBuffer(8, (byte) 0); // no
                                                                                 // UPC,
                                                                                 // no
                                                                                 // ISRC

                        } else {
                            atapiCmdError(channel, SenseType.ILLEGAL_REQUEST,
                                    AscType.INV_FIELD_IN_CMD_PACKET, true);
                            raiseInterrupt(channel);
                            break;
                        }
                    }
                    initSendAtapiCommand(channel, atapiCommand, retLength,
                            allocLength, false);
                    readyToSendAtapi(channel);
                }
            }
                break;

            case (byte) 0x51: // read disc info
            {
                // no-op to keep the Linux CD-ROM driver happy
                atapiCmdError(channel, SenseType.ILLEGAL_REQUEST,
                        AscType.INV_FIELD_IN_CMD_PACKET, true);
                raiseInterrupt(channel);
            }
                break;

            case (byte) 0x55: // mode select
            case (byte) 0xa6: // load/unload cd
            case (byte) 0x4b: // pause/resume
            case (byte) 0x45: // play audio
            case (byte) 0x47: // play audio msf
            case (byte) 0xbc: // play cd
            case (byte) 0xb9: // read cd msf
            case (byte) 0x44: // read header
            case (byte) 0xba: // scan
            case (byte) 0xbb: // set cd speed
            case (byte) 0x4e: // stop play/scan
            case (byte) 0x46: // ???
            case (byte) 0x4a: // ???
                logger.log(Level.SEVERE, "[" + super.getType() + "]"
                        + "  ATAPI command " + atapiCommand
                        + " not implemented yet.");

                atapiCmdError(channel, SenseType.ILLEGAL_REQUEST,
                        AscType.ILLEGAL_OPCODE, true);
                raiseInterrupt(channel);
                break;

            default:
                logger.log(Level.SEVERE, "[" + super.getType() + "]"
                        + "  Unknown ATAPI command " + atapiCommand + ".");
                atapiCmdError(channel, SenseType.ILLEGAL_REQUEST,
                        AscType.ILLEGAL_OPCODE, true);

                raiseInterrupt(channel);
                break;
            }
        }

    }

    /**
     * Init Mode Sense Single. TODO: deal with src what is the type of src?
     * private void initModeSenseSingle(int channel, const void* src, int size)
     *
     * @param channel
     * @param src
     * @param size
     */
    private void initModeSenseSingle(int channel, Object[] src, int size) {
        // Header
        getSelectedDriveController().setBuffer(0, (byte) ((size + 6) >> 8));
        getSelectedDriveController().setBuffer(1, (byte) ((size + 6) & 0xff));

        // TODO: if (bx_options.atadevice[channel][BX_HD_THIS
        // channels[channel].drive_select].Ostatus->get () == BX_INSERTED)
        // {
        // getSelectedDriveController().setBuffer(2, (byte)0x12); // media
        // present 120mm CD-ROM (CD-R) data/audio door closed
        //
        // } else
        // {
        getSelectedDriveController().setBuffer(2, (byte) 0x70); // no media
                                                                // present
        // }
        getSelectedDriveController().setBuffer(3, (byte) 0); // reserved
        getSelectedDriveController().setBuffer(4, (byte) 0); // reserved
        getSelectedDriveController().setBuffer(5, (byte) 0); // reserved
        getSelectedDriveController().setBuffer(6, (byte) 0); // reserved
        getSelectedDriveController().setBuffer(7, (byte) 0); // reserved

        // Data
        // TODO:
        // memcpy(BX_SELECTED_CONTROLLER(channel).buffer + 8, src, size);

    }

    /**
     * Execute atapi Cmd Nop.
     *
     * @param channel
     *            the currently selected channel.
     */
    private void atapiCmdNop(int channel) {

        getSelectedDriveController().getInterruptReason().setIo(1);
        getSelectedDriveController().getInterruptReason().setCd(1);
        getSelectedDriveController().getInterruptReason().setRel(0);
        getSelectedDriveController().getStatus().setBusy(0);
        getSelectedDriveController().getStatus().setDriveReady(1);
        getSelectedDriveController().getStatus().setDrq(0);
        getSelectedDriveController().getStatus().setErr(0);

    }

    /**
     * Get packet field.
     *
     * @param channelIndex
     * @param bufferIndex
     * @param start
     * @param numBits
     * @return -
     */
    private int getPacketField(int channelIndex, int bufferIndex, int start,
            int numBits) {

        // #define EXTRACT_FIELD(arr,byte,start,num_bits) (((arr)[(byte)] >>
        // (start)) & ((1 << (num_bits)) - 1))
        // #define get_packet_field(c,b,s,n)
        // (EXTRACT_FIELD((BX_SELECTED_CONTROLLER((c)).buffer),(b),(s),(n)))

        int result = 0;

        byte[] buffer = channels[channelIndex].getSelectedController()
                .getBuffer();

        result = (((buffer)[bufferIndex] >> (start)) & ((1 << (numBits)) - 1)); // TODO:
                                                                                // check
                                                                                // this
                                                                                // operation
                                                                                // works
                                                                                // with
                                                                                // int's

        return result;
    }

    /**
     * Get packet byte.
     *
     * @param channelIndex
     * @param bufferIndex
     * @return the packet byte.
     */
    private int getPacketByte(int channelIndex, int bufferIndex) {
        // #define get_packet_byte(c,b)
        // (BX_SELECTED_CONTROLLER((c)).buffer[(b)])
        int result = channels[channelIndex].getSelectedController().getBuffer()[bufferIndex];

        return result;
    }

    /**
     * Get packet word.
     *
     * @param channelIndex
     * @param bufferIndex
     * @return the packet word.
     */
    private int getPacketWord(int channelIndex, int bufferIndex) {

        // #define get_packet_word(c,b)
        // (((Bit16u)BX_SELECTED_CONTROLLER((c)).buffer[(b)] << 8)
        // | BX_SELECTED_CONTROLLER((c)).buffer[(b)+1])
        int result = ((channels[channelIndex].getSelectedController()
                .getBuffer()[channelIndex] << 8) | channels[channelIndex]
                .getSelectedController().getBuffer()[(channelIndex) + 1]);

        return result;
    }

    /**
     * Get atapi cmd error.
     *
     * @param channel
     * @param senseType
     * @param ascType
     * @param show
     */
    private void atapiCmdError(int channel, SenseType senseType,
            AscType ascType, boolean show) {
        if (show) {
            logger.log(Level.SEVERE, "[" + super.getType() + "]"
                    + "  Atapi_cmd_error, for channel " + channel + ", key "
                    + senseType.toString() + ", asc " + ascType.toString()
                    + ".");
        } else {
            logger.log(Level.CONFIG, "[" + super.getType() + "]"
                    + "  Atapi_cmd_error, for channel " + channel + ", key "
                    + senseType.toString() + ", asc " + ascType.toString()
                    + ".");
        }

        getSelectedDriveController()
                .setErrorRegister(senseType.getValue() << 4);
        getSelectedDriveController().getInterruptReason().setIo(1);
        getSelectedDriveController().getInterruptReason().setCd(1);
        getSelectedDriveController().getInterruptReason().setRel(0);
        getSelectedDriveController().getStatus().setBusy(0);
        getSelectedDriveController().getStatus().setDriveReady(1);
        getSelectedDriveController().getStatus().setWriteFault(0);
        getSelectedDriveController().getStatus().setDrq(0);
        getSelectedDriveController().getStatus().setErr(1);

        getSelectedDrive().getSenseInfo().setSenseKey(senseType);
        getSelectedDrive().getSenseInfo().setAsc(ascType);
        getSelectedDrive().getSenseInfo().setAscq((byte) 0);

    }

    /**
     * Init Send Atapi Command.
     *
     * @param channel
     * @param command
     * @param reqLength
     * @param allocLength
     * @param lazy
     */
    private void initSendAtapiCommand(int channel, int command, int reqLength,
            int allocLength, boolean lazy) {
        // TODO: code below commented out in BOCHS
        // BX_SELECTED_CONTROLLER(channel).byte_count is a union of
        // BX_SELECTED_CONTROLLER(channel).cylinder_no;
        // lazy is used to force a data read in the buffer at the next read.

        if (getSelectedDriveController().getByteCount() == 0xffff) {
            getSelectedDriveController().setByteCount(0xfffe);
        }

        // if ((BX_SELECTED_CONTROLLER(channel).byte_count & 1)
        // && !(alloc_length <= BX_SELECTED_CONTROLLER(channel).byte_count))
        if (((getSelectedDriveController().getByteCount() & 1) > 0)
                && !(allocLength <= getSelectedDriveController().getByteCount())) {
            logger.log(Level.CONFIG, "[" + super.getType() + "]"
                    + "  Odd byte count, for channel " + channel
                    + ", to ATAPI command " + command + ",  using "
                    + getSelectedDriveController().getByteCount() + ".");

            int newByteCount = getSelectedDriveController().getByteCount();
            newByteCount -= 1;
            getSelectedDriveController().setByteCount(newByteCount);
        }

        if (getSelectedDriveController().getByteCount() == 0) {
            logger.log(Level.SEVERE, "[" + super.getType() + "]"
                    + "  ATAPI command with zero byte count.");
        }

        if (allocLength < 0) {
            logger.log(Level.SEVERE, "[" + super.getType() + "]"
                    + "  Allocation length < 0.");
        }
        if (allocLength == 0) {
            allocLength = getSelectedDriveController().getByteCount();
        }

        getSelectedDriveController().getInterruptReason().setIo(1); // TOOD:
                                                                    // check if
                                                                    // order is
                                                                    // important
                                                                    // in IR
        getSelectedDriveController().getInterruptReason().setCd(0);
        getSelectedDriveController().getStatus().setBusy(0);
        getSelectedDriveController().getStatus().setDrq(1);
        getSelectedDriveController().getStatus().setErr(0);

        // no bytes transfered yet
        if (lazy) {

            getSelectedDriveController().setBufferIndex(
                    getSelectedDriveController().getBufferSize());
        } else {
            getSelectedDriveController().setBufferIndex(0);
        }

        getSelectedDriveController().setDrqIndex(0);

        if (getSelectedDriveController().getByteCount() > reqLength) {
            getSelectedDriveController().setByteCount(reqLength);
        }

        if (getSelectedDriveController().getByteCount() > allocLength) {
            getSelectedDriveController().setByteCount(allocLength);
        }

        getSelectedDrive().getAtpi().setCommand(command);
        getSelectedDrive().getAtpi().setDrqBytes(
                getSelectedDriveController().getByteCount());

        int newTotalBytesRemaining = (reqLength < allocLength) ? reqLength
                : allocLength;
        getSelectedDrive().getAtpi().setTotalBytesRemaining(
                newTotalBytesRemaining);

        // Code below commented out in BOCHS:
        // if (lazy) {
        // // bias drq_bytes and total_bytes_remaining
        // BX_SELECTED_DRIVE(channel).atapi.drq_bytes += 2048;
        // BX_SELECTED_DRIVE(channel).atapi.total_bytes_remaining += 2048;
        // }
    }

    /**
     * Ready To Send Atapi.
     *
     * @param channel
     *            the current channel
     */
    private void readyToSendAtapi(int channel) {
        raiseInterrupt(channel);
    }

    /**
     * Set hard disk command 0x1f7 (mch) Writes to the command register with
     * drive_select != 0 are ignored if no secondary device is present
     *
     * @param originalAddress
     * @param data
     * @param logicalSector
     * @param ret
     */
    private void setHardDiskCommand(int originalAddress, int[] data,
            Integer logicalSector, int ret) {

        ModulePIC pic = (ModulePIC)super.getConnection(Type.PIC);

        if (getSelectedChannel().isSlaveSelected()
                && !getSelectedChannel().isSlaveDrivePresent()) {
            return;
        }

        // Writes to the command register clear the IRQ
        pic.clearIRQ(getSelectedChannel().getIrqNumber());

        if (getSelectedDriveController().getStatus().getBusy() > 0) {
            logger.log(Level.WARNING, "[" + super.getType() + "]"
                    + "  hard disk: command sent, controller BUSY.");
        }

        if ((data[0] & 0xf0) == 0x10) {
            data[0] = 0x10;
        }

        switch (data[0]) {

        case 0x10: // CALIBRATE DRIVE

            if (getSelectedDrive().getDriveType() != ATADriveType.HARD_DISK) {
                logger.log(Level.INFO, "[" + super.getType() + "]"
                        + "  Calibrate drive for channel " + curChannelIndex
                        + ", issued to non-disk, with index "
                        + getSelectedChannel().getSelectedDrive() + ".");
                abortCommand(curChannelIndex, data[0]);

                break;
            }

            if (getSelectedDrive().getDriveType() == ATADriveType.NONE) {

                getSelectedDriveController().setErrorRegister(0x02); // Track 0
                                                                     // not
                                                                     // found
                getSelectedDriveController().getStatus().setBusy(0);
                getSelectedDriveController().getStatus().setDriveReady(1);
                getSelectedDriveController().getStatus().setSeekComplete(0);
                getSelectedDriveController().getStatus().setDrq(0);
                getSelectedDriveController().getStatus().setErr(1);

                raiseInterrupt(curChannelIndex);

                logger.log(Level.INFO, "[" + super.getType() + "]"
                        + "  Calibrate drive for channel " + curChannelIndex
                        + ", not present, with index "
                        + getSelectedChannel().getSelectedDrive() + ".");
                break;
            }

            // Move head to cylinder 0, issue IRQ
            getSelectedDriveController().setErrorRegister(0);
            getSelectedDrive().setCurrentCylinder(0);
            getSelectedDriveController().getStatus().setBusy(0);
            getSelectedDriveController().getStatus().setDriveReady(1);
            getSelectedDriveController().getStatus().setSeekComplete(1);
            getSelectedDriveController().getStatus().setDrq(0);
            getSelectedDriveController().getStatus().setErr(0);

            raiseInterrupt(curChannelIndex);

            break;

        case 0x20: // READ SECTORS, with retries
        case 0x21: // READ SECTORS, without retries
        case 0xC4: // READ MULTIPLE SECTORS

            /*
             * update sector_no, always points to current sector after each
             * sector is read to buffer, DRQ bit set and issue IRQ if interrupt
             * handler transfers all data words into main memory, and more
             * sectors to read, then set BSY bit again, clear DRQ and read next
             * sector into buffer sector count of 0 means 256 sectors
             */

            if (getSelectedDrive().getDriveType() != ATADriveType.HARD_DISK) {
                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + "  Read multiple for channel " + curChannelIndex
                        + ", issued to non-disk, with index "
                        + getSelectedChannel().getSelectedDrive() + ".");

                abortCommand(curChannelIndex, data[0]);

                break;
            }

            if (!(getSelectedDriveController().getLbaMode() > 0)
                    && !(getSelectedDrive().getCurrentHead() > 0)
                    && !(getSelectedDrive().getCurrentCylinder() > 0)
                    && !(getSelectedDrive().getCurrentSector() > 0)) {
                logger.log(Level.INFO, "[" + super.getType() + "]"
                        + "  read from 0/0/0, for channel " + curChannelIndex
                        + ", with device index "
                        + getSelectedChannel().getSelectedDrive()
                        + ", aborting command.");

                abortCommand(curChannelIndex, data[0]);

                break;
            }

            // update sector count, sector number, cylinder,
            // drive, head, status
            // if there are more sectors, read next one in...
            if ((byte) channels[this.curChannelIndex].getSelectedController()
                    .getCurrentCommand() == (byte) 0xC4) {
                if (channels[this.curChannelIndex].getSelectedController()
                        .getMultipleSectors() == 0) {
                    abortCommand(curChannelIndex, data[0]);
                    break;
                }

                if (channels[this.curChannelIndex].getSelectedDrive()
                        .getSectorCount() > channels[this.curChannelIndex]
                        .getSelectedController().getMultipleSectors()) {
                    channels[this.curChannelIndex].getSelectedController()
                            .setBufferSize(
                                    channels[this.curChannelIndex]
                                            .getSelectedController()
                                            .getMultipleSectors() * 512);
                } else {
                    channels[this.curChannelIndex].getSelectedController()
                            .setBufferSize(
                                    channels[this.curChannelIndex]
                                            .getSelectedDrive()
                                            .getSectorCount() * 512);
                }
            } else {
                channels[this.curChannelIndex].getSelectedController()
                        .setBufferSize(512);
            }

            getSelectedDriveController().setCurrentCommand(data[0]);

            if (ideReadData(curChannelIndex, getSelectedDriveController()
                    .getBuffer(), getSelectedDriveController().getBufferSize())) {

                getSelectedDriveController().setErrorRegister(0);
                getSelectedDriveController().getStatus().setBusy(0);
                getSelectedDriveController().getStatus().setDriveReady(1);
                getSelectedDriveController().getStatus().setSeekComplete(1);
                getSelectedDriveController().getStatus().setDrq(1);
                getSelectedDriveController().getStatus().setCorrectedData(0);
                getSelectedDriveController().setBufferIndex(0);

                raiseInterrupt(curChannelIndex);

            }

            break;

        case 0x30: // WRITE SECTORS, with retries
        case 0xC5: // WRITE MULTIPLE SECTORS
            /*
             * update sector_no, always points to current sector after each
             * sector is read to buffer, DRQ bit set and issue IRQ if interrupt
             * handler transfers all data words into main memory, and more
             * sectors to read, then set BSY bit again, clear DRQ and read next
             * sector into buffer sector count of 0 means 256 sectors
             */

            if (getSelectedDrive().getDriveType() != ATADriveType.HARD_DISK) {
                logger.log(Level.INFO, "[" + super.getType() + "]"
                        + "  Write sectors, for channel " + curChannelIndex
                        + ", issued to non-disk with device index "
                        + getSelectedChannel().getSelectedDrive() + ".");

                abortCommand(curChannelIndex, data[0]);
                break;
            }

            if (data[0] == (byte) 0xC5) {

                if (getSelectedDriveController().getMultipleSectors() == 0) {
                    abortCommand(curChannelIndex, data[0]);
                    break;
                }

                if (channels[this.curChannelIndex].getSelectedDrive()
                        .getSectorCount() > channels[this.curChannelIndex]
                        .getSelectedController().getMultipleSectors()) {
                    channels[this.curChannelIndex].getSelectedController()
                            .setBufferSize(
                                    channels[this.curChannelIndex]
                                            .getSelectedController()
                                            .getMultipleSectors() * 512);

                } else {
                    channels[this.curChannelIndex].getSelectedController()
                            .setBufferSize(
                                    channels[this.curChannelIndex]
                                            .getSelectedDrive()
                                            .getSectorCount() * 512);
                }

            } else {
                channels[this.curChannelIndex].getSelectedController()
                        .setBufferSize(512);
            }

            getSelectedDriveController().setCurrentCommand(data[0]);

            // implicit seek done :^)
            getSelectedDriveController().setErrorRegister(0);
            getSelectedDriveController().getStatus().setBusy(0);
            // BX_SELECTED_CONTROLLER(channel).status.drive_ready = 1; //note:
            // commented out in BOCHS code
            getSelectedDriveController().getStatus().setSeekComplete(1);
            getSelectedDriveController().getStatus().setDrq(1);
            getSelectedDriveController().getStatus().setErr(0);
            getSelectedDriveController().setBufferIndex(0);

            break;

        // TODO: check this code against BOCHS 2.3
        case 0x90: // EXECUTE DEVICE DIAGNOSTIC

            if (getSelectedDriveController().getStatus().getBusy() > 0) {
                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + "  Diagnostic command, for channel "
                        + curChannelIndex
                        + ", BSY bit set, for disk with device index "
                        + getSelectedChannel().getSelectedDrive() + ".");

                abortCommand(curChannelIndex, data[0]);

                break;
            }
            setSignature(curChannelIndex);

            getSelectedDriveController().setErrorRegister(0x01);
            getSelectedDriveController().getStatus().setDrq(0);
            getSelectedDriveController().getStatus().setErr(0);

            break;

        // TODO: check this code against BOCHS 2.3
        case 0x91: // INITIALIZE DRIVE PARAMETERS

            if (getSelectedDriveController().getStatus().getBusy() > 0) {
                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + "  Init drive parameters command, for channel "
                        + curChannelIndex
                        + ", BSY bit set, for disk with device index "
                        + getSelectedChannel().getSelectedDrive() + ".");

                abortCommand(curChannelIndex, data[0]);

                break;
            }

            if (getSelectedDrive().getDriveType() != ATADriveType.HARD_DISK) {
                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + "  Initialize drive parameters, for channel "
                        + curChannelIndex
                        + ", issued to non-disk, with device index "
                        + getSelectedChannel().getSelectedDrive() + ".");

                abortCommand(curChannelIndex, data[0]);

                break;
            }

            // sets logical geometry of specified drive
            logger.log(Level.INFO, "[" + super.getType() + "]"
                    + "  Initialize drive parameters, for channel "
                    + curChannelIndex + ", issued to disk, with device index "
                    + getSelectedChannel().getSelectedDrive()
                    + ", sector count= " + getSelectedDrive().getSectorCount()
                    + ", current head= " + getSelectedDrive().getCurrentHead()
                    + ".");

            if (getSelectedDrive().getDriveType() == ATADriveType.NONE) {
                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + "  Initialize drive parameters, for channel "
                        + curChannelIndex
                        + ", disk not present, with device index "
                        + getSelectedChannel().getSelectedDrive());

                // BX_SELECTED_CONTROLLER(channel).error_register = 0x12;
                // //commented out in BOCHS

                getSelectedDriveController().getStatus().setBusy(0);
                getSelectedDriveController().getStatus().setDriveReady(1);
                getSelectedDriveController().getStatus().setDrq(0);
                raiseInterrupt(curChannelIndex);

                break;
            }

            if (getSelectedDrive().getSectorCount() != getSelectedDrive()
                    .getTotalNumSectors()) {
                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + "  Initialize drive parameters, for channel "
                        + curChannelIndex + ", device index "
                        + getSelectedChannel().getSelectedDrive()
                        + " sector count doesn't match.");

                getSelectedDrive().setSectorCount(
                        getSelectedDrive().getTotalNumSectors());

                abortCommand(curChannelIndex, data[0]);

                break;
            }
            if (getSelectedDrive().getCurrentHead() != (getSelectedDrive()
                    .getTotalNumHeads() - 1)) {
                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + "  Initialize drive parameters, for channel "
                        + curChannelIndex + ", device index "
                        + getSelectedChannel().getSelectedDrive()
                        + " head number doesn't match.");

                getSelectedDrive().setCurrentHead(
                        (getSelectedDrive().getTotalNumHeads() - 1));

                abortCommand(curChannelIndex, data[0]);

                break;
            }

            getSelectedDriveController().getStatus().setBusy(0);
            getSelectedDriveController().getStatus().setDriveReady(1);
            getSelectedDriveController().getStatus().setDrq(0);
            getSelectedDriveController().getStatus().setErr(0);
            raiseInterrupt(curChannelIndex);

            break;

        case 0xec: // IDENTIFY DEVICE

            if (getSelectedDrive().getDriveType() == ATADriveType.NONE) {
                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + "  Identify device, for channel " + curChannelIndex
                        + ", disk not present, with device index "
                        + getSelectedChannel().getSelectedDrive()
                        + ", aborting.");

                abortCommand(curChannelIndex, data[0]);
                break;
            }

            if (getSelectedDrive().getDriveType() == ATADriveType.CDROM) {
                setSignature(curChannelIndex);
                abortCommand(curChannelIndex, 0xec);

            } else {

                getSelectedDriveController().setCurrentCommand(data[0]);
                getSelectedDriveController().setErrorRegister(0);

                // See ATA/ATAPI-4, 8.12

                getSelectedDriveController().getStatus().setBusy(0);
                getSelectedDriveController().getStatus().setDriveReady(1);
                getSelectedDriveController().getStatus().setWriteFault(0);
                getSelectedDriveController().getStatus().setDrq(1);

                getSelectedDriveController().getStatus().setSeekComplete(1);
                getSelectedDriveController().getStatus().setCorrectedData(0);

                getSelectedDriveController().setBufferIndex(0);

                raiseInterrupt(curChannelIndex);
                identifyDrive(curChannelIndex);

            }

            break;

        case 0xef: // SET FEATURES

            switch (getSelectedDrive().getFeatures()) {

            case 0x03: // Set Transfer Mode

                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + "  Set transfer mode, for channel " + curChannelIndex
                        + ", device index "
                        + getSelectedChannel().getSelectedDrive()
                        + ", not supported for subcommand "
                        + getSelectedDrive().getFeatures()
                        + ", but returning success.");

                getSelectedDriveController().getStatus().setDriveReady(1);
                getSelectedDriveController().getStatus().setSeekComplete(1);

                raiseInterrupt(curChannelIndex);

                break;

            case 0x02: // Enable and
            case 0x82: // Disable write cache.
            case 0xAA: // Enable and
            case 0x55: // Disable look-ahead cache.
            case 0xCC: // Enable and
            case 0x66: // Disable reverting to power-on default

                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + "  SET FEATURES, for channel " + curChannelIndex
                        + ", device index "
                        + getSelectedChannel().getSelectedDrive()
                        + ", not supported for subcommand "
                        + getSelectedDrive().getFeatures()
                        + ", but returning success.");

                getSelectedDriveController().getStatus().setDriveReady(1);
                getSelectedDriveController().getStatus().setSeekComplete(1);
                raiseInterrupt(curChannelIndex);

                break;

            default:

                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + "  SET FEATURES, for channel " + curChannelIndex
                        + ", device index "
                        + getSelectedChannel().getSelectedDrive()
                        + ", with unknown subcommand "
                        + getSelectedDrive().getFeatures() + ".");

                abortCommand(curChannelIndex, data[0]);
            }
            break;

        case 0x40: // READ VERIFY SECTORS

            if (getSelectedDrive().getDriveType() != ATADriveType.HARD_DISK) {
                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + "  read verify, for channel " + curChannelIndex
                        + ", issued to non-disk with device index "
                        + getSelectedChannel().getSelectedDrive() + ".");

                abortCommand(curChannelIndex, data[0]);

                break;
            }

            logger.log(Level.INFO, "[" + super.getType() + "]"
                    + "  verify command : 0x40, for channel " + curChannelIndex
                    + ", issued to device with device index "
                    + getSelectedChannel().getSelectedDrive() + ".");

            getSelectedDriveController().getStatus().setBusy(0);
            getSelectedDriveController().getStatus().setDriveReady(1);
            getSelectedDriveController().getStatus().setDrq(0);
            getSelectedDriveController().getStatus().setErr(0);

            raiseInterrupt(curChannelIndex);

            break;

        case 0xc6: // SET MULTIPLE MODE

            if (getSelectedDrive().getDriveType() != ATADriveType.HARD_DISK) {

                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + "  Set multiple mode, for channel " + curChannelIndex
                        + ", issued to non-hard disk, with device index "
                        + getSelectedChannel().getSelectedDrive() + ".");

                abortCommand(curChannelIndex, data[0]);

            } else if ((getSelectedDrive().getSectorCount() > ATAConstants.MAX_MULTIPLE_SECTORS)
                    || ((getSelectedDrive().getSectorCount() & (getSelectedDrive()
                            .getSectorCount() - 1)) != 0)
                    || (getSelectedDrive().getSectorCount() == 0)) {
                abortCommand(curChannelIndex, data[0]);
            } else {

                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + "  set multiple mode: sectors= "
                        + getSelectedDrive().getSectorCount());

                getSelectedDriveController().setMultipleSectors(
                        getSelectedDrive().getSectorCount());
                getSelectedDriveController().getStatus().setBusy(0);
                getSelectedDriveController().getStatus().setDriveReady(1);
                getSelectedDriveController().getStatus().setWriteFault(0);
                getSelectedDriveController().getStatus().setDrq(0);

                raiseInterrupt(curChannelIndex);

            }

            break;

        // ATAPI commands
        case 0xa1: // IDENTIFY PACKET DEVICE

            if (getSelectedDrive().getDriveType() == ATADriveType.CDROM) {

                getSelectedDriveController().setCurrentCommand(data[0]);
                getSelectedDriveController().setErrorRegister(0);
                getSelectedDriveController().getStatus().setBusy(0);
                getSelectedDriveController().getStatus().setDriveReady(1);
                getSelectedDriveController().getStatus().setWriteFault(0);
                getSelectedDriveController().getStatus().setDrq(1);
                getSelectedDriveController().getStatus().setErr(0);
                getSelectedDriveController().getStatus().setSeekComplete(1);
                getSelectedDriveController().getStatus().setCorrectedData(0);
                getSelectedDriveController().setBufferIndex(0);

                raiseInterrupt(curChannelIndex);
                identifyAtapiDrive(curChannelIndex);

            } else {
                abortCommand(curChannelIndex, 0xa1);
            }
            break;

        case 0x08: // DEVICE RESET (atapi)

            if (getSelectedDrive().getDriveType() == ATADriveType.CDROM) {

                setSignature(curChannelIndex);

                getSelectedDriveController().getStatus().setBusy(1);

                int newErrorReg = getSelectedDriveController()
                        .getErrorRegister();
                newErrorReg &= ~(1 << 7);
                getSelectedDriveController().setErrorRegister(newErrorReg);
                getSelectedDriveController().getStatus().setWriteFault(0);
                getSelectedDriveController().getStatus().setDrq(0);
                getSelectedDriveController().getStatus().setCorrectedData(0);
                getSelectedDriveController().getStatus().setErr(0);
                getSelectedDriveController().getStatus().setBusy(0);

            } else {
                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + "  ATAPI Device Reset, for channel "
                        + curChannelIndex
                        + ", issued to on non-cd device, with device index "
                        + getSelectedChannel().getSelectedDrive() + ".");

                abortCommand(curChannelIndex, 0x08);
            }

            break;

        case 0xa0: // SEND PACKET (atapi)

            if (getSelectedDrive().getDriveType() == ATADriveType.CDROM) {
                // PACKET
                getSelectedDriveController().setPacketDma(
                        getSelectedDrive().getFeatures() & 1);

                if ((getSelectedDrive().getFeatures() & (1 << 1)) > 0) {
                    logger.log(Level.WARNING, "[" + super.getType() + "]"
                            + "  PACKET-overlapped not supported, for channel "
                            + curChannelIndex + ", issued to device index "
                            + getSelectedChannel().getSelectedDrive() + ".");

                    abortCommand(curChannelIndex, 0xa0);

                } else {
                    // We're already ready!
                    getSelectedDrive().setSectorCount(1);
                    getSelectedDriveController().getStatus().setBusy(0);
                    getSelectedDriveController().getStatus().setWriteFault(0);

                    // serv bit??
                    getSelectedDriveController().getStatus().setDrq(1);
                    getSelectedDriveController().getStatus().setErr(0);

                    // NOTE: no interrupt here
                    getSelectedDriveController().setCurrentCommand(data[0]);
                    getSelectedDriveController().setBufferIndex(0);

                }
            } else {
                abortCommand(curChannelIndex, 0xa0);
            }
            break;

        case 0xa2: // SERVICE (atapi), optional

            if (getSelectedDrive().getDriveType() == ATADriveType.CDROM) {

                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + "  ATAPI SERVICE not implemented, for channel "
                        + curChannelIndex + ", issued to device index "
                        + getSelectedChannel().getSelectedDrive() + ".");

                abortCommand(curChannelIndex, 0xa2);

            } else {
                abortCommand(curChannelIndex, 0xa2);
            }
            break;

        // power management
        case 0xe5: // CHECK POWER MODE

            getSelectedDriveController().getStatus().setBusy(0);
            getSelectedDriveController().getStatus().setDriveReady(1);
            getSelectedDriveController().getStatus().setWriteFault(0);
            getSelectedDriveController().getStatus().setDrq(0);
            getSelectedDriveController().getStatus().setErr(0);
            getSelectedDrive().setSectorCount(0xff); // Active or Idle mode

            raiseInterrupt(curChannelIndex);
            break;

        case 0x70: // SEEK (cgs)

            if (getSelectedDrive().getDriveType() == ATADriveType.HARD_DISK) {

                logger.log(Level.INFO, "[" + super.getType() + "]"
                        + "  write cmd 0x70 (SEEK) executing, for channel "
                        + curChannelIndex + ", issued to device index "
                        + getSelectedChannel().getSelectedDrive() + ".");

                if (!getSelectedDrive().calculateLogicalAddress(logicalSector)) {

                    logger.log(Level.WARNING, "[" + super.getType() + "]"
                            + "  seek, for channel " + curChannelIndex
                            + ", issued to device index "
                            + getSelectedChannel().getSelectedDrive()
                            + "initial seek to sector " + logicalSector
                            + "out of bounds, aborting.");

                    abortCommand(curChannelIndex, data[0]);

                    break;
                }

                logicalSector = getSelectedDrive().calculateLogicalAddress();

                getSelectedDriveController().setErrorRegister(0);
                getSelectedDriveController().getStatus().setBusy(0);
                getSelectedDriveController().getStatus().setDriveReady(1);
                getSelectedDriveController().getStatus().setSeekComplete(1);
                getSelectedDriveController().getStatus().setDrq(0);
                getSelectedDriveController().getStatus().setCorrectedData(0);
                getSelectedDriveController().getStatus().setErr(0);
                getSelectedDriveController().setBufferIndex(0);

                logger.log(Level.INFO, "["
                        + super.getType()
                        + "]"
                        + "  SEEK disable ird set to "
                        + getSelectedChannel().getDrives()[0].getControl()
                                .isDisableIrq() + ", for channel "
                        + curChannelIndex + ", with device index " + 0 + ".");

                logger.log(Level.INFO, "["
                        + super.getType()
                        + "]"
                        + "  SEEK disable ird set to "
                        + getSelectedChannel().getDrives()[1].getControl()
                                .isDisableIrq() + ", for channel "
                        + curChannelIndex + ", with device index " + 1 + ".");

                logger.log(Level.INFO, "[" + super.getType() + "]"
                        + "  SEEK completed. error_register "
                        + getSelectedDriveController().getErrorRegister()
                        + ", for channel " + curChannelIndex
                        + ", with device index "
                        + getSelectedChannel().getSelectedDrive() + ".");

                raiseInterrupt(curChannelIndex);

                logger.log(Level.INFO, "[" + super.getType() + "]"
                        + "  SEEK interrupt completed, for channel "
                        + curChannelIndex + ", issued to device index "
                        + getSelectedChannel().getSelectedDrive());
            } else {
                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + "  write cmd 0x70 (SEEK), for channel "
                        + curChannelIndex
                        + ", not supported for non-disk with device index "
                        + getSelectedChannel().getSelectedDrive() + ".");

                abortCommand(curChannelIndex, 0x70);
            }
            break;

        case 0xC8: // READ DMA

            /*
             * if (dma != null) {
             * getSelectedDriveController().getStatus().setDriveReady(1);
             * getSelectedDriveController().getStatus().setSeekComplete(1);
             * getSelectedDriveController().getStatus().setDrq(1);
             * getSelectedDriveController().setCurrentCommand(data[0]);
             *
             * } else {
             */
            logger.log(Level.WARNING, "[" + super.getType() + "]"
                    + "  write cmd 0xC8 (READ DMA) not supported, for channel "
                    + curChannelIndex + ", device index "
                    + getSelectedChannel().getSelectedDrive() + ".");

            abortCommand(curChannelIndex, 0xC8);
            // }
            break;

        case 0xCA: // WRITE DMA

            /*
             * if (dma != null) {
             * getSelectedDriveController().getStatus().setDriveReady(1);
             * getSelectedDriveController().getStatus().setSeekComplete(1);
             * getSelectedDriveController().getStatus().setDrq(1);
             * getSelectedDriveController().setCurrentCommand(data[0]); } else {
             */
            logger
                    .log(
                            Level.WARNING,
                            "["
                                    + super.getType()
                                    + "]"
                                    + "  write cmd 0xCA (WRITE DMA) not supported, for channel "
                                    + curChannelIndex + ", device index "
                                    + getSelectedChannel().getSelectedDrive()
                                    + ".");

            abortCommand(curChannelIndex, 0xCA);
            // }
            break;

        default:
            logger.log(Level.WARNING, "[" + super.getType() + "]"
                    + "  IO write to " + originalAddress + ", unknown command "
                    + data + " for channel " + curChannelIndex
                    + ", device index "
                    + getSelectedChannel().getSelectedDrive() + ".");
            abortCommand(curChannelIndex, data[0]);
        }

    }

    /**
     * Identify the Atapi Drive.
     *
     * @param channel
     *            the channel
     */
    private void identifyAtapiDrive(int channel) {

        int i = 0;
        char[] serialNumber = new char[21];

        getSelectedDrive().setIdDrive(0,
                ((2 << 14) | (5 << 8) | (1 << 7) | (2 << 5) | (0 << 0))); // Removable
                                                                          // CDROM,
                                                                          // 50us
                                                                          // response,
                                                                          // 12
                                                                          // byte
                                                                          // packets

        for (i = 1; i <= 9; i++) {
            getSelectedDrive().setIdDrive(i, 0);
        }

        String tempSerialNumber = "BXCD00000           ";
        serialNumber = tempSerialNumber.toCharArray();

        serialNumber[7] = (char) (channel + 49);
        serialNumber[8] = (char) (getSelectedChannel().getSelectedDriveIndex() + 49);

        for (i = 0; i < 10; i++) {
            char char1 = serialNumber[i * 2];
            char char2 = serialNumber[i * 2 + 1];
            int resultingInt = (int) char1 | ((int) char2 << 8);

            getSelectedDrive().setIdDrive(10 + i, resultingInt);
        }

        for (i = 20; i <= 22; i++) {
            getSelectedDrive().setIdDrive(i, 0);
        }

        String tempFirmWare = "ALPHA1  ";
        char[] firmware = tempFirmWare.toCharArray();

        for (i = 0; i < (firmware.length) / 2; i++) {

            char char1 = firmware[i * 2];
            char char2 = firmware[i * 2 + 1];
            int resultingInt = (int) char1 | ((int) char2 << 8);

            getSelectedDrive().setIdDrive(23 + i, resultingInt);

        }

        for (i = 0; i < (getSelectedDrive().getModelNo().length) / 2; i++) {

            char char1 = getSelectedDrive().getModelNo()[i * 2];
            char char2 = getSelectedDrive().getModelNo()[i * 2 + 1];

            int resultingInt = (int) char1 | ((int) char2 << 8);

            getSelectedDrive().setIdDrive(27 + i, resultingInt);

        }

        getSelectedDrive().setIdDrive(47, 0);
        getSelectedDrive().setIdDrive(47, 1); // 32 bits access

        /*
         * if (dma != null) { getSelectedDrive().setIdDrive(49, ((1 << 9) | (1
         * << 8))); // LBA and DMA } else {
         */
        getSelectedDrive().setIdDrive(49, (1 << 9)); // LBA only supported
        // }

        getSelectedDrive().setIdDrive(50, 0);
        getSelectedDrive().setIdDrive(51, 0);
        getSelectedDrive().setIdDrive(52, 0);

        getSelectedDrive().setIdDrive(53, 3); // words 64-70, 54-58 valid

        for (i = 54; i <= 62; i++) {
            getSelectedDrive().setIdDrive(i, 0);
        }

        // copied from CFA540A
        getSelectedDrive().setIdDrive(63, 0x0103); // variable (DMA stuff)
        getSelectedDrive().setIdDrive(64, 0x0001); // PIO
        getSelectedDrive().setIdDrive(65, 0x00b4);
        getSelectedDrive().setIdDrive(66, 0x00b4);
        getSelectedDrive().setIdDrive(67, 0x012c);
        getSelectedDrive().setIdDrive(68, 0x00b4);

        getSelectedDrive().setIdDrive(69, 0);
        getSelectedDrive().setIdDrive(70, 0);
        getSelectedDrive().setIdDrive(71, 30); // faked
        getSelectedDrive().setIdDrive(72, 30); // faked
        getSelectedDrive().setIdDrive(73, 0);
        getSelectedDrive().setIdDrive(74, 0);

        getSelectedDrive().setIdDrive(75, 0);

        for (i = 76; i <= 79; i++) {
            getSelectedDrive().setIdDrive(i, 0);
        }

        getSelectedDrive().setIdDrive(80, 0x1e); // supports up to ATA/ATAPI-4
        getSelectedDrive().setIdDrive(81, 0);
        getSelectedDrive().setIdDrive(82, 0);
        getSelectedDrive().setIdDrive(83, 0);
        getSelectedDrive().setIdDrive(84, 0);
        getSelectedDrive().setIdDrive(85, 0);
        getSelectedDrive().setIdDrive(86, 0);
        getSelectedDrive().setIdDrive(87, 0);
        getSelectedDrive().setIdDrive(88, 0);

        for (i = 89; i <= 126; i++) {
            getSelectedDrive().setIdDrive(i, 0);
        }

        getSelectedDrive().setIdDrive(127, 0);
        getSelectedDrive().setIdDrive(128, 0);

        for (i = 129; i <= 159; i++) {
            getSelectedDrive().setIdDrive(i, 0);
        }

        for (i = 160; i <= 255; i++) {
            getSelectedDrive().setIdDrive(i, 0);
        }

        // now convert the id_drive array (native 256 word format) to
        // the controller buffer (512 bytes)
        int temp16;
        for (i = 0; i <= 255; i++) {
            temp16 = getSelectedDrive().getIdDrive()[i];
            getSelectedDriveController().setBuffer(i * 2,
                    (byte) (temp16 & 0x00ff));
        }
    }

    /**
     * Identify the drive by setting the ID Drive params and writing the data to
     * the buffer.
     *
     * @param channel
     *            the channel
     */
    private void identifyDrive(int channel) {

        int i = 0;

        char[] serialNumber = new char[21];

        int temp32 = 0;
        int temp16 = 0;

        if (ATAConstants.IS_CONNER_CFA540A_DEFINED) {

            getSelectedDrive().setIdDrive(0, 0x0c5a);
            getSelectedDrive().setIdDrive(1, 0x0418);
            getSelectedDrive().setIdDrive(2, 0);
            getSelectedDrive().setIdDrive(3,
                    getSelectedDrive().getTotalNumHeads());
            getSelectedDrive().setIdDrive(4, 0x9fb7);
            getSelectedDrive().setIdDrive(5, 0x0289);
            getSelectedDrive().setIdDrive(6,
                    getSelectedDrive().getTotalNumSectors());
            getSelectedDrive().setIdDrive(7, 0x0030);
            getSelectedDrive().setIdDrive(8, 0x000a);
            getSelectedDrive().setIdDrive(9, 0x0000);

            String tempSerialNumber = " CA00GSQ\0\0\0\0\0\0\0\0\0\0\0\0";
            serialNumber = tempSerialNumber.toCharArray();

            for (i = 0; i < 10; i++) {

                char char1 = serialNumber[i * 2];
                char char2 = serialNumber[i * 2 + 1];

                int resultingInt = (int) char1 | ((int) char2 << 8);

                getSelectedDrive().setIdDrive(10 + i, resultingInt);
            }

            getSelectedDrive().setIdDrive(20, 3);
            getSelectedDrive().setIdDrive(21, 512); // 512 Sectors = 256kB cache
            getSelectedDrive().setIdDrive(22, 4);

            String tempFirmware = "8FT054  ";
            char[] firmware = tempFirmware.toCharArray();

            for (i = 0; i < (firmware.length) / 2; i++) {
                char char1 = firmware[i * 2];
                char char2 = firmware[i * 2 + 1];

                int resultingInt = (int) char1 | ((int) char2 << 8);

                getSelectedDrive().setIdDrive(23 + i, resultingInt);
            }

            String tempModel = "Conner Peripherals 540MB - CFA540A      ";
            char[] model = tempModel.toCharArray();

            for (i = 0; i < (model.length) / 2; i++) {
                char char1 = model[i * 2];
                char char2 = model[i * 2 + 1];

                int resultingInt = (int) char1 | ((int) char2 << 8);

                getSelectedDrive().setIdDrive(27 + i, resultingInt);
            }

            getSelectedDrive().setIdDrive(47, 0x8080); // multiple mode
                                                       // identification
            getSelectedDrive().setIdDrive(48, 0);
            getSelectedDrive().setIdDrive(49, 0x0f01);

            getSelectedDrive().setIdDrive(50, 0);

            getSelectedDrive().setIdDrive(51, 0);
            getSelectedDrive().setIdDrive(52, 0x0002);
            getSelectedDrive().setIdDrive(53, 0x0003);
            getSelectedDrive().setIdDrive(54, 0x0418);

            getSelectedDrive().setIdDrive(55,
                    getSelectedDrive().getTotalNumHeads());
            getSelectedDrive().setIdDrive(56,
                    getSelectedDrive().getTotalNumSectors());
            getSelectedDrive().setIdDrive(57, 0x1e80);
            getSelectedDrive().setIdDrive(58, 0x0010);
            getSelectedDrive().setIdDrive(
                    59,
                    (0x0100) | getSelectedDriveController()
                            .getMultipleSectors());
            getSelectedDrive().setIdDrive(60, 0x20e0);
            getSelectedDrive().setIdDrive(61, 0x0010);

            getSelectedDrive().setIdDrive(62, 0);

            getSelectedDrive().setIdDrive(63, 0x0103); // variable (DMA stuff)
            getSelectedDrive().setIdDrive(64, 0x0001); // PIO
            getSelectedDrive().setIdDrive(65, 0x00b4);
            getSelectedDrive().setIdDrive(66, 0x00b4);
            getSelectedDrive().setIdDrive(67, 0x012c);
            getSelectedDrive().setIdDrive(68, 0x00b4);

            for (i = 69; i <= 79; i++) {
                getSelectedDrive().setIdDrive(i, 0);
            }

            getSelectedDrive().setIdDrive(80, 0);

            getSelectedDrive().setIdDrive(81, 0);

            getSelectedDrive().setIdDrive(82, 0);
            getSelectedDrive().setIdDrive(83, 0);
            getSelectedDrive().setIdDrive(84, 0);
            getSelectedDrive().setIdDrive(85, 0);
            getSelectedDrive().setIdDrive(86, 0);
            getSelectedDrive().setIdDrive(87, 0);

            for (i = 88; i <= 127; i++) {
                getSelectedDrive().setIdDrive(i, 0);
            }

            getSelectedDrive().setIdDrive(128, 0x0418);
            getSelectedDrive().setIdDrive(129, 0x103f);
            getSelectedDrive().setIdDrive(130, 0x0418);
            getSelectedDrive().setIdDrive(131, 0x103f);
            getSelectedDrive().setIdDrive(132, 0x0004);
            getSelectedDrive().setIdDrive(133, 0xffff);
            getSelectedDrive().setIdDrive(134, 0);
            getSelectedDrive().setIdDrive(135, 0x5050);

            for (i = 136; i <= 144; i++) {
                getSelectedDrive().setIdDrive(i, 0);
            }

            getSelectedDrive().setIdDrive(145, 0x302e);
            getSelectedDrive().setIdDrive(146, 0x3245);
            getSelectedDrive().setIdDrive(147, 0x2020);
            getSelectedDrive().setIdDrive(148, 0x2020);

            for (i = 149; i <= 255; i++) {
                getSelectedDrive().setIdDrive(i, 0);
            }
        } else {

            // Identify Drive command return values definition
            //
            // This code is rehashed from some that was donated.
            // I'm using ANSI X3.221-1994, AT Attachment Interface for Disk
            // Drives
            // and X3T10 2008D Working Draft for ATA-3

            // Word 0: general config bit-significant info
            // Note: bits 1-5 and 8-14 are now "Vendor specific (obsolete)"
            // bit 15: 0=ATA device
            // 1=ATAPI device
            // bit 14: 1=format speed tolerance gap required
            // bit 13: 1=track offset option available
            // bit 12: 1=data strobe offset option available
            // bit 11: 1=rotational speed tolerance is > 0,5% (typo?)
            // bit 10: 1=disk transfer rate > 10Mbs
            // bit 9: 1=disk transfer rate > 5Mbs but <= 10Mbs
            // bit 8: 1=disk transfer rate <= 5Mbs
            // bit 7: 1=removable cartridge drive
            // bit 6: 1=fixed drive
            // bit 5: 1=spindle motor control option implemented
            // bit 4: 1=head switch time > 15 usec
            // bit 3: 1=not MFM encoded
            // bit 2: 1=soft sectored
            // bit 1: 1=hard sectored
            // bit 0: 0=reserved
            getSelectedDrive().setIdDrive(0, 0x0040);

            // Word 1: number of user-addressable cylinders in
            // default translation mode. If the value in words 60-61
            // exceed 16,515,072, this word shall contain 16,383.

            getSelectedDrive().setIdDrive(1,
                    getSelectedDrive().getTotalNumCylinders());

            // Word 2: reserved
            getSelectedDrive().setIdDrive(2, 0);

            // Word 3: number of user-addressable heads in default
            // translation mode
            getSelectedDrive().setIdDrive(3,
                    getSelectedDrive().getTotalNumHeads());

            // Word 4: # unformatted bytes per translated track in default xlate
            // mode
            // Word 5: # unformatted bytes per sector in default xlated mode
            // Word 6: # user-addressable sectors per track in default xlate
            // mode
            // Note: words 4,5 are now "Vendor specific (obsolete)"
            getSelectedDrive().setIdDrive(4,
                    (512 * getSelectedDrive().getTotalNumSectors()));
            getSelectedDrive().setIdDrive(5, 512);
            getSelectedDrive().setIdDrive(6,
                    getSelectedDrive().getTotalNumSectors());

            // Word 7-9: Vendor specific
            for (i = 7; i <= 9; i++) {
                getSelectedDrive().setIdDrive(i, 0);
            }

            // Word 10-19: Serial number (20 ASCII characters, 0000h=not
            // specified)
            // This field is right justified and padded with spaces (20h).
            String tempSerialNumber = "BXHD00000           ";
            serialNumber = tempSerialNumber.toCharArray();

            serialNumber[7] = (char) (channel + 49);
            serialNumber[8] = (char) (getSelectedChannel()
                    .getSelectedDriveIndex() + 49);

            for (i = 0; i < 10; i++) {
                char char1 = serialNumber[i * 2];
                char char2 = serialNumber[i * 2 + 1];

                int resultingInt = ((int) char1) << 8 | ((int) char2);

                getSelectedDrive().setIdDrive(10 + i, resultingInt);
            }

            // Word 20: buffer type
            // 0000h = not specified
            // 0001h = single ported single sector buffer which is
            // not capable of simulataneous data xfers to/from
            // the host and the disk.
            // 0002h = dual ported multi-sector buffer capable of
            // simulatenous data xfers to/from the host and disk.
            // 0003h = dual ported mutli-sector buffer capable of
            // simulatenous data xfers with a read caching
            // capability.
            // 0004h-ffffh = reserved
            getSelectedDrive().setIdDrive(20, 3);

            // Word 21: buffer size in 512 byte increments, 0000h = not
            // specified
            getSelectedDrive().setIdDrive(21, 512); // 512 Sectors = 256kB cache

            // Word 22: # of ECC bytes available on read/write long cmds
            // 0000h = not specified
            getSelectedDrive().setIdDrive(22, 4);

            // Word 23..26: Firmware revision (8 ascii chars, 0000h=not
            // specified)
            // This field is left justified and padded with spaces (20h)
            for (i = 23; i <= 26; i++) {
                getSelectedDrive().setIdDrive(i, 0);
            }

            // Word 27..46: Model number (40 ascii chars, 0000h=not specified)
            // This field is left justified and padded with spaces (20h)
            // for (i=27; i<=46; i++)
            // getSelectedDrive().setIdDrive(i] = 0;

            for (i = 0; i < 20; i++) {
                char char1 = getSelectedDrive().getModelNo()[i * 2];
                char char2 = getSelectedDrive().getModelNo()[i * 2 + 1];

                int resultingInt = (int) char1 << 8 | ((int) char2);

                getSelectedDrive().setIdDrive(27 + i, resultingInt);

            }

            // Word 47: 15-8 Vendor unique
            // 7-0 00h= read/write multiple commands not implemented
            // xxh= maximum # of sectors that can be transferred
            // per interrupt on read and write multiple commands
            getSelectedDrive()
                    .setIdDrive(47, ATAConstants.MAX_MULTIPLE_SECTORS);

            // Word 48: 0000h = cannot perform dword IO
            // 0001h = can perform dword IO
            getSelectedDrive().setIdDrive(48, 1);

            // Word 49: Capabilities
            // 15-10: 0 = reserved
            // 9: 1 = LBA supported
            // 8: 1 = DMA supported
            // 7-0: Vendor unique
            // if (BX_HD_THIS bmdma_present())

            // TODO:
            // if (dma != null)
            // {
            // getSelectedDrive().setIdDrive(49, ((1 << 9) | (1 << 8)));
            // }
            // else
            // {
            getSelectedDrive().setIdDrive(49, (1 << 9));
            // }

            // Word 50: Reserved
            getSelectedDrive().setIdDrive(50, 0);

            // Word 51: 15-8 PIO data transfer cycle timing mode
            // 7-0 Vendor unique
            getSelectedDrive().setIdDrive(51, 0x200);

            // Word 52: 15-8 DMA data transfer cycle timing mode
            // 7-0 Vendor unique
            getSelectedDrive().setIdDrive(52, 0x200);

            // Word 53: 15-1 Reserved
            // 0 1=the fields reported in words 54-58 are valid
            // 0=the fields reported in words 54-58 may be valid
            getSelectedDrive().setIdDrive(53, 0);

            // Word 54: # of user-addressable cylinders in curr xlate mode
            // Word 55: # of user-addressable heads in curr xlate mode
            // Word 56: # of user-addressable sectors/track in curr xlate mode
            getSelectedDrive().setIdDrive(54,
                    getSelectedDrive().getTotalNumCylinders());
            getSelectedDrive().setIdDrive(55,
                    getSelectedDrive().getTotalNumHeads());
            getSelectedDrive().setIdDrive(56,
                    getSelectedDrive().getTotalNumSectors());

            // Word 57-58: Current capacity in sectors
            // Excludes all sectors used for device specific purposes.

            temp32 = getSelectedDrive().getTotalNumCylinders()
                    * getSelectedDrive().getTotalNumHeads()
                    * getSelectedDrive().getTotalNumSectors();

            getSelectedDrive().setIdDrive(57, (temp32 & 0xffff)); // LSW
            getSelectedDrive().setIdDrive(58, (temp32 >> 16)); // MSW

            // Word 59: 15-9 Reserved
            // 8 1=multiple sector setting is valid
            // 7-0 current setting for number of sectors that can be
            // transferred per interrupt on R/W multiple commands
            if (getSelectedDriveController().getMultipleSectors() > 0) {
                getSelectedDrive().setIdDrive(
                        59,
                        0x0100 | getSelectedDriveController()
                                .getMultipleSectors());

            } else {
                getSelectedDrive().setIdDrive(59, 0x0000);
            }

            // Word 60-61:
            // If drive supports LBA Mode, these words reflect total # of user
            // addressable sectors. This value does not depend on the current
            // drive geometry. If the drive does not support LBA mode, these
            // words shall be set to 0.
            int num_sects = getSelectedDrive().getTotalNumCylinders()
                    * getSelectedDrive().getTotalNumHeads()
                    * getSelectedDrive().getTotalNumSectors();
            getSelectedDrive().setIdDrive(60, (num_sects & 0xffff)); // LSW
            getSelectedDrive().setIdDrive(61, (num_sects >> 16)); // MSW

            // Word 62: 15-8 single word DMA transfer mode active
            // 7-0 single word DMA transfer modes supported
            // The low order byte identifies by bit, all the Modes which are
            // supported e.g., if Mode 0 is supported bit 0 is set.
            // The high order byte contains a single bit set to indiciate
            // which mode is active.
            getSelectedDrive().setIdDrive(62, 0x0);

            // Word 63: 15-8 multiword DMA transfer mode active
            // 7-0 multiword DMA transfer modes supported
            // The low order byte identifies by bit, all the Modes which are
            // supported e.g., if Mode 0 is supported bit 0 is set.
            // The high order byte contains a single bit set to indiciate
            // which mode is active.

            // TODO: implement DMA here
            // if (bmdma_present())
            // if (dma != null)
            // {
            // getSelectedDrive().setIdDrive(63, 0x07);
            // }
            // else
            // {
            getSelectedDrive().setIdDrive(63, 0x0);
            // }

            // Word 64-79 Reserved
            for (i = 64; i <= 79; i++) {
                getSelectedDrive().setIdDrive(i, 0);
            }
            // Word 80: 15-5 reserved
            // 4 supports ATA/ATAPI-4
            // 3 supports ATA-3
            // 2 supports ATA-2
            // 1 supports ATA-1
            // 0 reserved
            getSelectedDrive().setIdDrive(80, (1 << 3) | (1 << 2) | (1 << 1));

            // Word 81: Minor version number
            getSelectedDrive().setIdDrive(81, 0);

            // Word 82: 15 obsolete
            // 14 NOP command supported
            // 13 READ BUFFER command supported
            // 12 WRITE BUFFER command supported
            // 11 obsolete
            // 10 Host protected area feature set supported
            // 9 DEVICE RESET command supported
            // 8 SERVICE interrupt supported
            // 7 release interrupt supported
            // 6 look-ahead supported
            // 5 write cache supported
            // 4 supports PACKET command feature set
            // 3 supports power management feature set
            // 2 supports removable media feature set
            // 1 supports securite mode feature set
            // 0 support SMART feature set
            getSelectedDrive().setIdDrive(82, (1 << 14));
            getSelectedDrive().setIdDrive(83, (1 << 14));
            getSelectedDrive().setIdDrive(84, (1 << 14));
            getSelectedDrive().setIdDrive(85, (1 << 14));
            getSelectedDrive().setIdDrive(86, 0);
            getSelectedDrive().setIdDrive(87, (1 << 14));

            for (i = 88; i <= 127; i++) {
                getSelectedDrive().setIdDrive(i, 0);
            }

            // Word 128-159 Vendor unique
            for (i = 128; i <= 159; i++) {
                getSelectedDrive().setIdDrive(i, 0);
            }

            // Word 160-255 Reserved
            for (i = 160; i <= 255; i++) {
                getSelectedDrive().setIdDrive(i, 0);
            }
        }

        logger.log(Level.CONFIG, "ide -> Drive ID Info. initialized.");

        // now convert the id_drive array (native 256 word format) to
        // the controller buffer (512 bytes)
        for (i = 0; i <= 255; i++) {
            temp16 = getSelectedDrive().getIdDrive()[i];
            getSelectedDriveController().setBuffer(i * 2 + 1,
                    (byte) (temp16 & 0x00ff));
            getSelectedDriveController().setBuffer(i * 2, (byte) (temp16 >> 8));
        }
    }

    /**
     * Set the signiture.
     *
     * @param channel
     *            the ide channel
     */
    private void setSignature(int channel) {
        // Device signature
        getSelectedDrive().setCurrentHead(0);
        getSelectedDrive().setSectorCount(1);
        getSelectedDrive().setCurrentSector(1);

        if (getSelectedDrive().getDriveType() == ATADriveType.HARD_DISK) {
            getSelectedDrive().setCurrentCylinder(0);

        } else if (getSelectedDrive().getDriveType() == ATADriveType.CDROM) {
            getSelectedDrive().setCurrentCylinder(0xeb14);

        } else {
            getSelectedDrive().setCurrentCylinder(0xffff);
        }
    }

    /**
     * Read 16 bit from the buffer. TODO: confirm this works as expected
     *
     * @param buf
     * @param offset
     * @return the read value
     */
    private int read16bit(byte[] buf, int offset) {
        int returnValue = (buf[0 + offset] << 8) | buf[1 + offset];

        return returnValue;
    }

    /**
     * read 32 bit from the buffer TODO: confirm this works as expected
     *
     * @param buf
     * @param offset
     * @return the read value
     */
    private int read32bit(byte[] buf, int offset) {

        int returnValue = (buf[0 + offset] << 24) | (buf[1 + offset] << 16)
                | (buf[2 + offset] << 8) | buf[3 + offset];

        return returnValue;
    }

    /**
     * Gte the motherboard.
     *
     * @return the motherboard
     */
    protected ModuleMotherboard getMotherboard() {
        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Type.MOTHERBOARD);
        return motherboard;
    }

    /**
     * Get the RTC module.
     *
     * @return the RTC module
     */
    protected ModuleRTC getRtc() {
        ModuleRTC rtc = (ModuleRTC)super.getConnection(Type.RTC);
        return rtc;
    }

    /**
     * Get the PIC module.
     *
     * @return the PIC module.
     */
    protected ModulePIC getPic() {
        ModulePIC pic = (ModulePIC)super.getConnection(Type.PIC);
        return pic;
    }

    /**
     * Get the update interval.
     *
     * @return the update interval
     */
    public int getUpdateInterval() {
        return updateInterval;
    }

}
