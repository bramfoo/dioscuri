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
 * - http://bochs.sourceforge.net/techspec/CMOS-reference.txt
 * - 
 */

package dioscuri.module.fdc;

import dioscuri.Emulator;
import dioscuri.exception.ModuleException;
import dioscuri.exception.StorageDeviceException;
import dioscuri.exception.UnknownPortException;
import dioscuri.exception.WriteOnlyPortException;
import dioscuri.interfaces.Module;
import dioscuri.module.*;
import dioscuri.module.cpu32.DMAController;
import dioscuri.module.cpu32.DMATransferCapable;
import dioscuri.module.cpu32.HardwareComponent;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of a Floppy disk controller module.
 *
 * @see dioscuri.module.AbstractModule
 *      <p/>
 *      Metadata module ********************************************
 *      general.type : fdc general.name : Floppy Disk Controller
 *      general.architecture : Von Neumann general.description : Implements a
 *      standard floppy disk controller for 3.5" floppies general.creator :
 *      Tessella Support Services, Koninklijke Bibliotheek, Nationaal Archief of
 *      the Netherlands general.version : 1.0 general.keywords : floppy, disk,
 *      controller, 1.44, 2.88, 3.5 inch, SD, DD, HD general.relations :
 *      motherboard general.yearOfIntroduction : 1982 general.yearOfEnding :
 *      general.ancestor : 5.25 inch floppy disk general.successor : -
 *      <p/>
 *      Issues: - check all fixme statements - sometimes MSR register is set to
 *      busy, but I am not sure if it is done the right way as it loses all
 *      previous bits: // Data register not ready, drive busy MSR = (byte) (1 <<
 *      drive);
 *      <p/>
 *      Notes: - Floppy disk controller is usually an 8272, 8272A, NEC765 (or
 *      compatible), or an 82072 or 82077AA for perpendicular recording at
 *      2.88M. - The FDC is only capable of reading from and writing to virtual
 *      floppy disks (images), not physical disks. - The FDC does not perform a
 *      CRC (Cyclic Redundancy Check). - FDC commands Scan low, scan low or
 *      equal, scan high or equal are not implemented. - Datarates (speed of
 *      reading/writing) is not taken into account. - Enhanced commands like
 *      lock and unlock are not fully implemented. - Current FDC only works with
 *      DMA data transfer
 *      <p/>
 *      Overview of FDC registers (ref: Port list, made by Ralf Brown:
 *      [http://mudlist.eorbit.net/~adam/pickey/ports.html]) 03F0 R diskette
 *      controller status A (PS/2) (see #P173) 03F0 R diskette controller status
 *      A (PS/2 model 30) (see #P174) 03F0 R diskette EHD controller board
 *      jumper settings (82072AA) (see #P175) 03F1 R diskette controller status
 *      B (PS/2) (see #P176) 03F1 R diskette controller status B (PS/2 model 30)
 *      (see #P177) 03F2 W diskette controller DOR (Digital Output Register)
 *      (see #P178) 03F3 ?W tape drive register (on the 82077AA) bit 7-2
 *      reserved, tri-state bit 1-0 tape select =00 none, drive 0 cannot be a
 *      tape drive. =01 drive1 =10 drive2 =11 drive3 03F4 R diskette controller
 *      main status register (see #P179) Note: in non-DMA mode, all data
 *      transfers occur through PORT 03F5h and the status registers (bit 5 here
 *      indicates data read/write rather than than command/status read/write)
 *      03F4 W diskette controller data rate select register (see #P180) 03F5 R
 *      diskette command/data register 0 (ST0) (see #P181) status register 1
 *      (ST1) (see #P182) status register 2 (ST2) (see #P183) status register 3
 *      (ST3) (see #P184) 03F5 W diskette command register. The commands
 *      summarized here are mostly multibyte commands. This is for brief
 *      recognition only. (see #P187) 03F6 -- reserved on FDC 03F6 rW FIXED disk
 *      controller data register (see #P185) 03F7 RW harddisk controller (see
 *      #P186) 03F7 R diskette controller DIR (Digital Input Register, PC/AT
 *      mode) bit 7 = 1 diskette change bit 6-0 tri-state on FDC 03F7 R diskette
 *      controller DIR (Digital Input Register, PS/2 mode) bit 7 = 1 diskette
 *      change bit 6-3 = 1 bit 2 datarate select1 bit 1 datarate select0 bit 0 =
 *      0 high density select (500Kb/s, 1Mb/s) conflict bit 0 FIXED DISK drive 0
 *      select 03F7 R diskette controller DIR (Digital Input Register, PS/2
 *      model 30) bit 7 = 0 diskette change bit 6-4 = 0 bit 3 -DMA gate (value
 *      from DOR register) bit 2 NOPREC (value from CCR register) bit 1 datarate
 *      select1 bit 0 datarate select0 conflict bit 0 FIXED DISK drive 0 select
 *      03F7 W configuration control register (PC/AT, PS/2) bit 7-2 reserved,
 *      tri-state bit 1-0 = 00 500 Kb/S mode (MFM) = 01 300 Kb/S mode (MFM) = 10
 *      250 Kb/S mode (MFM) = 11 1 Mb/S mode (MFM) (on 82072/82077AA) conflict
 *      bit 0 FIXED DISK drive 0 select 03F7 W configuration control register
 *      (PS/2 model 30) bit 7-3 reserved, tri-state bit 2 NOPREC (has no
 *      function. set to 0 by hardreset) bit 1-0 = 00 500 Kb/S mode (MFM) = 01
 *      300 Kb/S mode (MFM) = 10 250 Kb/S mode (MFM) = 11 1 Mb/S mode (MFM) (on
 *      82072/82077AA) conflict bit 0 FIXED DISK drive 0 select
 */
public class FDC extends ModuleFDC implements DMATransferCapable {

    // Logging
    private static final Logger logger = Logger.getLogger(FDC.class.getName());

    // Relations
    private Emulator emu;
    private DMAController dma32;

    // IRQ and DMA variables
    private int irqNumber; // Interrupt number assigned by PIC
    private boolean pendingIRQ; // IRQ is still in progress
    private int resetSenseInterrupt; // TODO: Maybe this variable has to be extended for multiple drives
    public DMA8Handler dma8Handler; // 8-bit DMA handler for transfer of bytes
    private boolean tc; // TC, Terminal Count status from DMA controller
    private boolean dmaAndInterruptEnabled; // DMA and IRQ are both enabled

    // Timing
    private int updateInterval; // Denotes the update interval for the clock
    // timer

    // Controller variables
    private Drive[] drives; // Array containing 4 drives at maximum
    private int numberOfDrives; // Denotes the total number of available drives
    // of FDC (<= 4)
    private int[] dataRates; // Array containing 4 possible datarates in
    // Kilobytes per second
    private int dataRate; // Index to dataRates-array (0,1,2,3) denoting a
    // specific datarate
    private boolean fdcEnabled; // Denotes if FDC is operating normally
    private boolean fdcEnabledPrevious; // Denotes if previous state of FDC was
    // operating normally (state change
    // means reset has occurred)
    private int drive; // Denotes the currently selected drive

    private int formatCount; // Number of sectors in track
    private byte formatFillbyte; // Filler byte for formatting tracks with this
    // value

    // Buffer for DMA transfer
    protected byte[] floppyBuffer; // Buffer for data transfer between DMA and floppy
    private int floppyBufferIndex; // Index for floppy buffer
    private byte floppyBufferCurrentByte; // Contains the current byte of the floppy buffer

    // Command variables
    private byte[] command; // Array containing bytes that form a command
    private int commandIndex; // Pointer to current byte in command array
    private int commandSize; // Total size of command (in number of bytes)
    private int commandPending; // Denotes if command is pending or not
    private boolean commandComplete; // Denotes if all bytes of command have been received

    // Result variables
    private byte[] result; // Array containing bytes that form result
    private int resultIndex; // Pointer to current byte in result array
    private int resultSize; // Total size of result (in number of bytes)

    // Status and control bits
    byte nonDMA; // non-DMA mode
    byte lock; // FDC lock status
    byte srt; // step rate time, defines the stepping rate in milliseconds (1 - 16) for all drives
    byte hut; // head unload time
    byte hlt; // head load time

    // Enhanced drive command bits
    // TODO: as enhanced commands are not tested, maybe this needs improvement
    byte config; // configure byte
    byte preTrack; // precompensation track
    byte perpMode; // perpendicular mode

    // Registers
    // DIGITAL OUTPUT REGISTER (DOR)
    // Bit(s) Description
    // 7-6 reserved on PS/2
    // 7 drive 3 motor enable
    // 6 drive 2 motor enable
    // 5 drive 1 motor enable
    // 4 drive 0 motor enable
    // 3 diskette DMA enable (reserved PS/2)
    // 2 =1 FDC enable (controller reset)
    // =0 hold FDC at reset
    // 1-0 drive select (0=A 1=B ..)
    private byte dor;

    // TAPE DRIVE REGISTER (TDR)
    // bit 7-2 reserved, tri-state
    // bit 1-0 tape select
    // =00 none, drive 0 cannot be a tape drive.
    // =01 drive1
    // =10 drive2
    // =11 drive3
    private byte tdr;

    // MAIN STATUS REGISTER (MSR)
    // Bit(s) Description
    // 7 =1 RQM data register is ready
    // =0 no access is permitted
    // 6 =1 transfer is from controller to system (ready for data read)
    // =0 transfer is from system to controller (ready for data write)
    // 5 non-DMA mode, 1=controller not in DMA-mode, 0=controller in DMA mode
    // 4 diskette controller is busy, 1=active, 0=not active
    // 3 drive 3 busy (reserved on PS/2)
    // 2 drive 2 busy (reserved on PS/2)
    // 1 drive 1 busy (= drive is in seek mode)
    // 0 drive 0 busy (= drive is in seek mode)
    private byte msr;

    // DISKETTE STATUS REGISTER 0 (COMMAND/DATA)
    // Bitfields for diskette command/data register 0 (ST0):
    // Bit(s) Description
    // 7-6 last command status
    // 00 command terminated successfully
    // 01 command terminated abnormally
    // 10 invalid command
    // 11 terminated abnormally by change in ready signal
    // 5 seek completed
    // 4 equipment check occurred after error
    // 3 not ready
    // 2 head number at interrupt
    // 1-0 unit select (0=A 1=B .. ) (on PS/2: 01=A 10=B
    private int statusRegister0;

    // DISKETTE STATUS REGISTER 1
    // Bitfields for diskette status register 1 (ST1):
    // Bit(s) Description
    // 7 end of cylinder; sector# greater then sectors/track
    // 6 =0
    // 5 CRC error in ID or data field
    // 4 overrun
    // 3 =0
    // 2 sector ID not found
    // 1 write protect detected during write
    // 0 ID address mark not found
    private int statusRegister1;

    // DISKETTE STATUS REGISTER 2
    // Bitfields for diskette status register 2 (ST2):
    // Bit(s) Description
    // 7 =0
    // 6 deleted Data Address Mark detected
    // 5 CRC error in data
    // 4 wrong cylinder detected
    // 3 scan command equal condition satisfied
    // 2 scan command failed, sector not found
    // 1 bad cylinder, ID not found
    // 0 missing Data Address Mark
    private int statusRegister2;

    // DISKETTE STATUS REGISTER 3
    // Bitfields for diskette status register 3 (ST3):
    // Bit(s) Description
    // 7 fault status signal
    // 6 write protect status
    // 5 ready status
    // 4 track zero status
    // 3 two sided status signal
    // 2 side select (head select)
    // 1-0 unit select (0=A 1=B .. )
    private int statusRegister3;

    // Constants

    // I/O ports 03F0-03F7 - Floppy Disk Controller
    private final static int PORT_FLOPPY_STATUS_A = 0x03F0; // R Status register
    // A
    private final static int PORT_FLOPPY_STATUS_B = 0x03F1; // R Status register
    // B
    private final static int PORT_FLOPPY_DOR = 0x03F2; // W Digital Output
    // Register
    private final static int PORT_FLOPPY_TAPEDRIVE = 0x03F3; // ?W Tape drive
    // register
    private final static int PORT_FLOPPY_MAIN_DATARATE = 0x03F4; // RW Main
    // status
    // register /
    // data rate
    // select
    // register:
    // used by
    // DMA-transfer
    private final static int PORT_FLOPPY_CMD_DATA = 0x03F5; // RW Command/data
    // register: used by
    // non-DMA transfer
    private final static int PORT_FLOPPY_RESERVED_FIXED = 0x03F6; // --
    // Reserved.
    // Probably
    // used to
    // check for
    // available
    // Harddisk
    private final static int PORT_FLOPPY_HD_CONTROLLER = 0x03F7; // RW DIR/hard
    // disk
    // controller

    // Floppy drive types
    private final static byte FLOPPY_DRIVETYPE_NONE = 0x00;
    private final static byte FLOPPY_DRIVETYPE_525DD = 0x01;
    private final static byte FLOPPY_DRIVETYPE_525HD = 0x02;
    private final static byte FLOPPY_DRIVETYPE_350DD = 0x03;
    private final static byte FLOPPY_DRIVETYPE_350HD = 0x04;
    private final static byte FLOPPY_DRIVETYPE_350ED = 0x05;

    // Floppy disk types
    private final static byte FLOPPY_DISKTYPE_NONE = 0x00;
    private final static byte FLOPPY_DISKTYPE_360K = 0x01;
    private final static byte FLOPPY_DISKTYPE_1_2 = 0x02;
    private final static byte FLOPPY_DISKTYPE_720K = 0x03;
    private final static byte FLOPPY_DISKTYPE_1_44 = 0x04;
    private final static byte FLOPPY_DISKTYPE_2_88 = 0x05;
    private final static byte FLOPPY_DISKTYPE_160K = 0x06;
    private final static byte FLOPPY_DISKTYPE_180K = 0x07;
    private final static byte FLOPPY_DISKTYPE_320K = 0x08;

    // FDC commands
    private final static int FDC_CMD_MRQ = 0x80;
    private final static int FDC_CMD_DIO = 0x40;
    private final static int FDC_CMD_NDMA = 0x20;
    private final static int FDC_CMD_BUSY = 0x10;
    private final static int FDC_CMD_ACTD = 0x08;
    private final static int FDC_CMD_ACTC = 0x04;
    private final static int FDC_CMD_ACTB = 0x02;
    private final static int FDC_CMD_ACTA = 0x01;

    // DMA channel
    private final static int FDC_DMA_CHANNEL = 2;

    /**
     * Class constructor
     *
     * @param owner
     */
    public FDC(Emulator owner)
    {
        emu = owner;

        // Initialise timing
        updateInterval = -1;

        // Initialise IRQ and DMA
        irqNumber = -1;
        pendingIRQ = false;
        resetSenseInterrupt = 0;
        dma8Handler = null;
        dmaAndInterruptEnabled = false;

        // Initialise controller variables
        drives = new Drive[1]; // set number of drives to default (1), can be
        // overruled by setNumberOfDrives
        drives[0] = new Drive();
        dataRates = new int[]{500, 300, 250, 1000};

        dataRate = 0;
        fdcEnabled = false;
        fdcEnabledPrevious = false;
        drive = 0;

        // Initialise command variables
        command = new byte[10];
        commandIndex = 0;
        commandSize = 0;
        commandPending = 0;
        commandComplete = false;

        formatCount = 0;
        formatFillbyte = 0;
        tc = false;

        // Buffering
        floppyBuffer = new byte[512 + 2]; // 2 extra for good measure
        floppyBufferIndex = 0;

        nonDMA = 0;
        lock = 0;
        srt = 0;
        hut = 0;
        hlt = 0;
        config = 0;
        preTrack = 0;
        perpMode = 0;

        // Initialise result variables
        result = new byte[10];
        resultIndex = 0;
        resultSize = 0;

        // Initialise registers
        dor = 0;
        msr = 0 | FDC_CMD_DIO; // Set to data direction to read - undocumented
        // feature for DMA (bram)
        tdr = 0;
        statusRegister0 = 0;
        statusRegister1 = 0;
        statusRegister2 = 0;
        statusRegister3 = 0;

        logger.log(Level.INFO, "[" + super.getType() + "] " + getClass().getName() + " -> AbstractModule created successfully.");
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.AbstractModule
     */
    @Override
    public boolean reset()
    {

        ModuleMotherboard motherboard = (ModuleMotherboard) super.getConnection(Module.Type.MOTHERBOARD);
        ModuleRTC rtc = (ModuleRTC) super.getConnection(Module.Type.RTC);
        ModulePIC pic = (ModulePIC) super.getConnection(Module.Type.PIC);
        ModuleDMA dma = (ModuleDMA) super.getConnection(Module.Type.DMA);
        ModuleATA ata = (ModuleATA) super.getConnection(Module.Type.ATA);

        // Register I/O ports 03F0-03F7 - Floppy Disk Controller in I/O address
        // space
        motherboard.setIOPort(PORT_FLOPPY_STATUS_A, this);
        motherboard.setIOPort(PORT_FLOPPY_STATUS_B, this);
        motherboard.setIOPort(PORT_FLOPPY_DOR, this);
        motherboard.setIOPort(PORT_FLOPPY_TAPEDRIVE, this);
        motherboard.setIOPort(PORT_FLOPPY_MAIN_DATARATE, this);
        motherboard.setIOPort(PORT_FLOPPY_CMD_DATA, this);
        motherboard.setIOPort(PORT_FLOPPY_RESERVED_FIXED, this);
        motherboard.setIOPort(PORT_FLOPPY_HD_CONTROLLER, this);

        // Request IRQ number
        irqNumber = pic.requestIRQNumber(this);
        if (irqNumber > -1) {
            logger.log(Level.CONFIG, "[" + super.getType() + "]"
                    + " IRQ number set to: " + irqNumber);
        } else {
            logger.log(Level.WARNING, "[" + super.getType() + "]"
                    + " Request of IRQ number failed.");
        }

        // Request a timer
        if (motherboard.requestTimer(this, updateInterval, false)) {
            logger.log(Level.CONFIG, "[" + super.getType() + "]"
                    + " Timer requested successfully.");
        } else {
            logger.log(Level.WARNING, "[" + super.getType() + "]"
                    + " Failed to request a timer.");
        }

        if (!emu.isCpu32bit()) {
            // Request DMA channel
            dma8Handler = new DMA8Handler(this);
            if (dma.registerDMAChannel(FDC_DMA_CHANNEL, dma8Handler)) {
                // Request successful
                logger
                        .log(Level.CONFIG, "[" + super.getType() + "]"
                                + " DMA channel registered to line: "
                                + FDC_DMA_CHANNEL);
            } else {
                // Request failed
                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + " Failed to register DMA channel " + FDC_DMA_CHANNEL);
            }
        }

        // Enable FDC in CMOS
        rtc.setCMOSRegister(0x14, (byte) (rtc.getCMOSRegister(0x14) | 0x01));

        // Initiate cold reset
        return reset(1);
    }

    /**
     * FDC specific reset, with value to indicate reset type
     *
     * @param resetType Type of reset passed to FDC<BR>
     *                  0: Warm reset (SW reset)<BR>
     *                  1: Cold reset (HW reset)
     * @return boolean true if module has been reset successfully, false
     *         otherwise
     */
    private boolean reset(int resetType)
    {

        ModuleMotherboard motherboard = (ModuleMotherboard) super.getConnection(Module.Type.MOTHERBOARD);
        ModuleRTC rtc = (ModuleRTC) super.getConnection(Module.Type.RTC);
        ModulePIC pic = (ModulePIC) super.getConnection(Module.Type.PIC);
        ModuleDMA dma = (ModuleDMA) super.getConnection(Module.Type.DMA);
        ModuleATA ata = (ModuleATA) super.getConnection(Module.Type.ATA);

        pendingIRQ = false;
        resetSenseInterrupt = 0; // No reset result present

        msr = 0 | FDC_CMD_DIO; // Set to data direction to read - undocumented
        // feature for DMA (bram);
        statusRegister0 = 0;
        statusRegister1 = 0;
        statusRegister2 = 0;
        statusRegister3 = 0;

        if (resetType == 1) {
            // Reset all FDC registers and parameters
            dor = 0x0C; // motor off, drive 3..0, DMA/INT enabled, normal
            // operation, drive select 0

            for (int i = 0; i < drives.length; i++) {
                drives[i].dir |= 0x80;
            }
            dataRate = 2; // 250 Kbps
            lock = 0;
        } else {
            logger.log(Level.INFO, "[" + super.getType() + "]"
                    + " FDC controller reset (software)");
        }

        if (lock == 0) {
            config = 0;
            preTrack = 0;
        }

        perpMode = 0;

        // Reset drives
        for (int i = 0; i < drives.length; i++) {
            drives[i].reset();
        }

        // Make sure no interrupt is pending
        pic.clearIRQ(irqNumber);
        if (!emu.isCpu32bit()) {
            dma.setDMARequest(FDC_DMA_CHANNEL, false);
        }

        // Go into idle phase
        this.enterIdlePhase();

        if (resetType == 1) {
            logger.log(Level.INFO, "[" + super.getType() + "]"
                    + " AbstractModule has been reset.");
        }

        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Module
     */
    @Override
    public void stop()
    {
        // Make sure all data on virtual floppies in drives are stored to image
        // files
        for (int i = 0; i < drives.length; i++) {
            if (drives[i] != null && drives[i].containsFloppy()) {
                // Eject floppy from drive and store data in disk image
                if (this.ejectCarrier(i) == false) {
                    logger.log(Level.SEVERE, "[" + super.getType() + "] Drive " + i
                            + ": eject floppy failed.");
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.AbstractModule
     */
    @Override
    public String getDump()
    {
        // Show some status information of this module
        String dump = "";
        String ret = "\r\n";
        String tab = "\t";

        dump = "FDC dump:" + ret;

        dump += "In total " + drives.length + " floppy drives exist:" + ret;
        for (int i = 0; i < drives.length; i++) {
            if (drives[i] != null) {
                dump += "Drive " + i + tab + ":" + tab + drives[i].toString()
                        + ret;

            } else {
                dump += "Drive " + i + tab + ":" + tab + "not enabled" + ret;
            }
        }
        return dump;
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Updateable
     */
    @Override
    public int getUpdateInterval()
    {
        return updateInterval;
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Updateable
     */
    @Override
    public void setUpdateInterval(int interval)
    {
        // Check if interval is > 0
        if (interval > 0) {
            updateInterval = interval;
        } else {
            updateInterval = 1000; // default is 1 ms
        }
        ModuleMotherboard motherboard = (ModuleMotherboard) super.getConnection(Module.Type.MOTHERBOARD);
        motherboard.resetTimer(this, updateInterval);
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Updateable
     */
    @Override
    public void update()
    {

        ModuleMotherboard motherboard = (ModuleMotherboard) super.getConnection(Module.Type.MOTHERBOARD);
        ModuleDMA dma = (ModuleDMA) super.getConnection(Module.Type.DMA);

        // Perform an update on FDC
        // Cannot guarantee the following instruction since 32-bit update:
        // logger.log(Level.INFO, motherboard.getCurrentInstructionNumber() +
        // " " + "[" + super.getType() + "]" + " UPDATE IN PROGRESS");

        // Check if there is a command pending (= zero if nothing to do)
        if (commandPending != 0x00) {
            drive = dor & 0x03;

            switch (commandPending) {
                case 0x07: // recalibrate
                    statusRegister0 = 0x20 | drive;

                    // Check if drive is set and motor running
                    if ((drives[drive].getDriveType() == FLOPPY_DRIVETYPE_NONE)
                            || (drives[drive].isMotorRunning() == false)) {
                        // Command terminated abnormally
                        statusRegister0 |= 0x50;
                    }

                    this.enterIdlePhase();
                    this.setInterrupt();
                    break;

                case 0x0F: // seek
                    statusRegister0 = 0x20 | (drives[drive].hds << 2) | drive;
                    this.enterIdlePhase();
                    this.setInterrupt();
                    break;

                case 0x4A: // read ID
                    this.enterResultPhase();
                    break;

                case 0x45: // write normal data
                case 0xC5:
                    if (tc) {
                        // Terminal Count line, done and reset status registers
                        statusRegister0 = (drives[drive].hds << 2) | drive;
                        statusRegister1 = 0;
                        statusRegister2 = 0;

                        // logger.log(Level.INFO, "[" + super.getType() + "]" +
                        // " WRITE DONE: drive=" + drive + ", hds=" +
                        // drives[drive].hds + ", cylinder=" +
                        // drives[drive].cylinder + ", sector=" +
                        // drives[drive].sector);

                        this.enterResultPhase();
                    } else {
                        // request transfer next sector by DMA
                        if (emu.isCpu32bit()) {
                            dma32.holdDREQ(FDC_DMA_CHANNEL & 3);
                        } else {
                            dma.setDMARequest(FDC_DMA_CHANNEL, true);
                        }
                    }
                    break;

                case 0x46: // read normal data
                case 0x66:
                case 0xC6:
                case 0xE6:
                    // request transfer next sector by DMA
                    if (emu.isCpu32bit()) {
                        dma32.holdDREQ(FDC_DMA_CHANNEL & 3);
                    } else {
                        dma.setDMARequest(FDC_DMA_CHANNEL, true);
                    }
                    break;

                case 0x4D: // format track
                    if ((formatCount == 0) || tc) {
                        formatCount = 0;
                        statusRegister0 = (drives[drive].hds << 2) | drive;
                        this.enterResultPhase();
                    } else {
                        // request transfer next sector by DMA
                        if (emu.isCpu32bit()) {
                            dma32.holdDREQ(FDC_DMA_CHANNEL & 3);
                        } else {
                            dma.setDMARequest(FDC_DMA_CHANNEL, true);
                        }
                    }
                    break;

                case 0xFE: // (contrived) RESET
                    this.reset(0); // Performs a warm reset
                    commandPending = 0;
                    statusRegister0 = 0xC0;
                    this.setInterrupt();
                    resetSenseInterrupt = 4;
                    break;

                default:
                    logger.log(Level.WARNING, "[" + super.getType() + "]"
                            + " CMD: unknown command during update.");
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public byte getIOPortByte(int portAddress) throws ModuleException,
            UnknownPortException, WriteOnlyPortException
    {

        ModuleMotherboard motherboard = (ModuleMotherboard) super.getConnection(Module.Type.MOTHERBOARD);
        ModuleRTC rtc = (ModuleRTC) super.getConnection(Module.Type.RTC);
        ModulePIC pic = (ModulePIC) super.getConnection(Module.Type.PIC);
        ModuleDMA dma = (ModuleDMA) super.getConnection(Module.Type.DMA);
        ModuleATA ata = (ModuleATA) super.getConnection(Module.Type.ATA);

        logger.log(Level.INFO, "[" + super.getType() + "]"
                + " IN command (byte) to port "
                + Integer.toHexString(portAddress).toUpperCase() + " received");

        byte value = 0;

        // Check which port is addressed
        switch (portAddress) {
            case 0x03F0: // Read diskette controller status A
            case 0x03F1: // Read diskette controller status B
                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + " Reading ports 0x3F0 and 0x3F1 not implemented");
                break;

            case 0x03F2: // Digital output register (DMA mode)
                logger.log(Level.CONFIG, "[" + super.getType() + "]"
                        + " Is reading allowed of port 0x3F2? Returned DOR");
                value = dor;
                break;

            case 0x03F3: // Tape Drive Register
                int drv = dor & 0x03;
                if (drives[drv].containsFloppy()) {
                    switch (drives[drv].getFloppyType()) {
                        case FLOPPY_DISKTYPE_160K:
                        case FLOPPY_DISKTYPE_180K:
                        case FLOPPY_DISKTYPE_320K:
                        case FLOPPY_DISKTYPE_360K:
                        case FLOPPY_DISKTYPE_1_2:
                            value = 0x00;
                            break;
                        case FLOPPY_DISKTYPE_720K:
                            value = (byte) 0xc0;
                            break;
                        case FLOPPY_DISKTYPE_1_44:
                            value = (byte) 0x80;
                            break;
                        case FLOPPY_DISKTYPE_2_88:
                            value = 0x40;
                            break;

                        default: // FLOPPY_DISKTYPE_NONE
                            value = 0x20;
                            break;
                    }
                } else {
                    value = 0x20;
                }
                break;

            case 0x03F4: // Main status register (DMA mode)
                value = msr;
                break;

            case 0x03F5: // Diskette controller data
                if (resultSize == 0) {
                    logger.log(Level.INFO, "[" + super.getType() + "]"
                            + " Port 0x3F5: no results to read");
                    // Clear whole MSR to reset its status
                    msr = 0 | FDC_CMD_DIO; // Set to data direction to read -
                    // undocumented feature for DMA (bram);
                    value = result[0];
                } else {
                    value = result[resultIndex];
                    resultIndex++;
                    // Clear MSR bits to indicate that drives are not busy anymore
                    // (LSB)
                    msr &= 0xF0;
                    // Clear interrupt
                    this.clearInterrupt();

                    if (resultIndex >= resultSize) {
                        this.enterIdlePhase();
                    }
                }
                break;

            case 0x03F6: // Reserved for future floppy controllers
                // This address is shared with the hard drive controller

                logger.log(Level.INFO, "[" + super.getType() + "]"
                        + " IN (byte) to port "
                        + Integer.toHexString(portAddress).toUpperCase()
                        + ": reserved port.");

                // this links to ata
                value = ata.getIOPortByte(portAddress);

                break;

            case 0x03F7: // Diskette controller digital input register
                // This address is shared with the hard drive controller:
                // Bit 7 : floppy
                // Bits 6..0: hard drive

                // A link to the ata
                value = ata.getIOPortByte(portAddress);
                value &= 0x7f;

                // Add in diskette change line if motor is on
                drv = dor & 0x03;
                if ((dor & (1 << (drv + 4))) == 1) {
                    if (drives[drv] != null) {
                        value |= (drives[drv].dir & 0x80);
                    } else {
                        logger.log(Level.WARNING, "[" + super.getType() + "]"
                                + " Non-existing drive requested at port "
                                + Integer.toHexString(portAddress).toUpperCase());
                    }
                }
                break;

            default:
                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + " Unknown port address encountered: "
                        + Integer.toHexString(portAddress).toUpperCase());
                break;
        }

        // Return value
        return value;
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public void setIOPortByte(int portAddress, byte value)
            throws ModuleException, UnknownPortException
    {

        ModuleMotherboard motherboard = (ModuleMotherboard) super.getConnection(Module.Type.MOTHERBOARD);
        ModuleRTC rtc = (ModuleRTC) super.getConnection(Module.Type.RTC);
        ModulePIC pic = (ModulePIC) super.getConnection(Module.Type.PIC);
        ModuleDMA dma = (ModuleDMA) super.getConnection(Module.Type.DMA);
        ModuleATA ata = (ModuleATA) super.getConnection(Module.Type.ATA);

        logger.log(Level.INFO, "[" + super.getType() + "]" + " OUT (byte) to port "
                + Integer.toHexString(portAddress).toUpperCase() + ": 0x"
                + Integer.toHexString(((int) value) & 0xFF).toUpperCase());

        // Check which I/O port is addressed
        switch (portAddress) {
            case 0x03F0:
            case 0x03F1:
                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + " Port address is read only and cannot be written to: "
                        + Integer.toHexString(portAddress).toUpperCase());
                break;

            case 0x03F2: // Digital output register (DMA mode)
                // Set new FDC and drive parameters based on new DOR value
                // Motor settings of drives
                for (int i = 0; i < drives.length; i++) {
                    if (drives[i] != null) {
                        switch (i) {
                            case 0:
                                drives[0].setMotor((value & 0x10) > 0 ? true : false);
                                break;
                            case 1:
                                drives[1].setMotor((value & 0x20) > 0 ? true : false);
                                break;
                            case 2:
                                drives[2].setMotor((value & 0x40) > 0 ? true : false);
                                break;
                            case 3:
                                drives[3].setMotor((value & 0x80) > 0 ? true : false);
                                break;
                            default:
                                logger.log(Level.WARNING, "["
                                        + super.getType()
                                        + "]"
                                        + " Unknown drive selected at port "
                                        + Integer.toHexString(portAddress)
                                        .toUpperCase());
                                break;
                        }
                    } else {
                        logger.log(Level.WARNING, "[" + super.getType() + "]"
                                + " Non-existing drive selected at port "
                                + Integer.toHexString(portAddress).toUpperCase());
                    }
                }

                // DMA and interrupt setting
                dmaAndInterruptEnabled = ((value & 0x08) > 0) ? true : false;

                // FDC enabled (normal operation) or reset
                fdcEnabled = ((value & 0x04) > 0) ? true : false;

                // Select drive to operate on
                drive = value & 0x03;

                // Store previous condition and update DOR
                fdcEnabledPrevious = ((dor & 0x04) > 0) ? true : false;
                dor = value;

                // Check state change of FDC
                if (!fdcEnabledPrevious && fdcEnabled) {
                    // Transition from RESET to NORMAL
                    motherboard.resetTimer(this, updateInterval);
                    motherboard.setTimerActiveState(this, true);
                } else if (fdcEnabledPrevious && !fdcEnabled) {
                    // Transition from NORMAL to RESET
                    msr = 0 | FDC_CMD_DIO; // Set to data direction to read -
                    // undocumented feature for DMA (bram);
                    commandPending = 0xFE; // is reset command, see update()
                }

                logger.log(Level.INFO, "[" + super.getType() + "]"
                        + " OUT (byte) to port "
                        + Integer.toHexString(portAddress).toUpperCase()
                        + ", DMA/IRQ=" + dmaAndInterruptEnabled + ", FDC="
                        + fdcEnabled + ", drive=" + drive + ", motorRunning="
                        + drives[drive].isMotorRunning());
                break;

            case 0x03F4: // Main status / data rate select register (DMA mode)
                // MAIN STATUS REGISTER (MSR)
                // Bit(s) Description
                // 7 =1 RQM data register is ready
                // =0 no access is permitted
                // 6 =1 transfer is from controller to system (ready for data read)
                // =0 transfer is from system to controller (ready for data write)
                // 5 non-DMA mode, 1=controller not in DMA-mode, 0=controller in DMA
                // mode
                // 4 diskette controller is busy, 1=active, 0=not active
                // 3 drive 3 busy (reserved on PS/2)
                // 2 drive 2 busy (reserved on PS/2)
                // 1 drive 1 busy (= drive is in seek mode)
                // 0 drive 0 busy (= drive is in seek mode)
                // Set data rate
                dataRate = value & 0x03;

                if ((value & 0x80) > 0) {
                    msr = 0 | FDC_CMD_DIO; // Set to data direction to read -
                    // undocumented feature for DMA (bram);
                    commandPending = 0xFE; // RESET pending
                    motherboard.resetTimer(this, updateInterval);
                    motherboard.setTimerActiveState(this, true);
                }
                if ((value & 0x7C) > 0) {
                    // Drive(s) are busy, but FDC is in non-DMA mode, which is not
                    // supported!
                    logger
                            .log(
                                    Level.WARNING,
                                    "["
                                            + super.getType()
                                            + "]"
                                            + " OUT (byte) to port "
                                            + Integer.toHexString(portAddress)
                                            .toUpperCase()
                                            + ": drive is busy, but in non-DMA mode which is not supported.");
                }
                break;

            case 0x03F5: // Diskette controller data
                // Three phases during each command:
                // 1. command phase: FDC receives all info required to perform
                // operation
                // 2. execution phase: FDC performs the operation
                // 3. result phase: operation is completed and result is made
                // available to the processor

                // Phase 1: command phase
                // Check current state of command
                if (commandComplete) {
                    if (commandPending != 0) {
                        logger
                                .log(
                                        Level.WARNING,
                                        "["
                                                + super.getType()
                                                + "]"
                                                + " new command received while old command is still pending.");
                    }

                    // Create new command
                    command[0] = value;
                    commandComplete = false;
                    commandIndex = 1;

                    // Set registers that read/write command is in progress
                    msr &= 0x0f; // leave drive status untouched, clear bits 7 - 4:
                    // 0000 = no access permitted, transfer system ->
                    // FDC, DMA mode, FDC busy
                    // Set bits MRQ ready (0x80), FDC busy (0x10)
                    msr |= FDC_CMD_MRQ | FDC_CMD_BUSY;

                    // Check the size of command (number of bytes to be expected)
                    int cmd = value & 0xFF;
                    // Also based on values for diskette commands:
                    // MFM = MFM (Modified Frequency Mode) selected, opposite to FM
                    // mode. Assumed to be 1 all times
                    // HDS = head select
                    // DS = drive select
                    // MT = multi track operation
                    // SK = skip deleted data address mark
                    switch (cmd) {
                        case 0x03: // Specify
                            commandSize = 3;
                            break;

                        case 0x04: // Sense drive status
                            commandSize = 2;
                            break;

                        case 0x07: // Recalibrate
                            commandSize = 2;
                            break;

                        case 0x08: // Sense interrupt status
                            commandSize = 1;
                            break;

                        case 0x0F: // Seek
                            commandSize = 3;
                            break;

                        case 0x4A: // MFM selected
                            commandSize = 2;
                            break;

                        case 0x4D: // MFM selected
                            commandSize = 10;
                            break;

                        case 0x45: // MFM selected
                        case 0xC5: // MT + MFM selected
                            commandSize = 9;
                            break;

                        case 0x46: // MFM selected
                        case 0x66: // SK + MFM selected
                        case 0xC6: // MFM + MT selected
                        case 0xE6: // SK + MFM + MT selected
                            commandSize = 9;
                            break;

                        // Enhanced drives (EHD) commands
                        case 0x0E: // Dump registers (Enhanced)
                        case 0x10: // Version (Enhanced)
                        case 0x14: // Unlock command (Enhanced)
                        case 0x94: // Lock command (Enhanced)
                            commandSize = 0;
                            commandPending = cmd;
                            this.enterResultPhase();
                            break;

                        case 0x12: // Perpendicular mode (Enhanced)
                            commandSize = 2;
                            break;

                        case 0x13: // Configure (Enhanced)
                            commandSize = 4;
                            break;

                        case 0x18: // National Semiconductor version command; return 80h
                            commandSize = 0;
                            statusRegister0 = 0x80; // Status: invalid command
                            this.enterResultPhase();
                            break;

                        case 0x8F: // Relative seek (Enhanced)
                        case 0xCF: // DIR selected
                            commandSize = 3;
                            break;

                        default:
                            logger.log(Level.WARNING, "[" + super.getType() + "]"
                                    + " OUT (byte) to port "
                                    + Integer.toHexString(portAddress).toUpperCase()
                                    + ": invalid FDC command.");
                            commandSize = 0;
                            statusRegister0 = 0x80; // Status: invalid command
                            this.enterResultPhase();
                            break;
                    }
                } else {
                    command[commandIndex++] = value;
                }

                // Phase 2: execution phase
                if (commandIndex == commandSize) {
                    // Command phase ready, jump to execution phase
                    this.executeCommand();
                    commandComplete = true;
                }
                break;

            case 0x03F6: // Reserved I/O address

                logger.log(Level.INFO, "[" + super.getType() + "]"
                        + " OUT (byte) to port "
                        + Integer.toHexString(portAddress).toUpperCase()
                        + ": reserved port.");

                // I/O address is shared with the ATA controller
                ata.setIOPortByte(portAddress, value);
                break;

            case 0x03F7: // Diskette controller configuration control register

                dataRate = value & 0x03;
                switch (dataRate) {
                    case 0:
                        logger.log(Level.INFO, "[" + super.getType() + "]"
                                + " Datarate is set to 500 Kbps");
                        break;
                    case 1:
                        logger.log(Level.INFO, "[" + super.getType() + "]"
                                + " Datarate is set to 300 Kbps");
                        break;
                    case 2:
                        logger.log(Level.INFO, "[" + super.getType() + "]"
                                + " Datarate is set to 250 Kbps");
                        break;
                    case 3:
                        logger.log(Level.INFO, "[" + super.getType() + "]"
                                + " Datarate is set to 1 Mbps");
                        break;
                }
                break;

            default:
                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + " Unknown port address encountered: "
                        + Integer.toHexString(portAddress).toUpperCase());
                break;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public byte[] getIOPortWord(int portAddress) throws ModuleException,
            WriteOnlyPortException
    {
        logger.log(Level.WARNING, "[" + super.getType() + "]"
                + " IN command (word) to port "
                + Integer.toHexString(portAddress).toUpperCase() + " received");
        logger.log(Level.WARNING, "[" + super.getType() + "]"
                + " Returned default value 0xFFFF");

        // Return dummy value 0xFFFF
        return new byte[]{(byte) 0x0FF, (byte) 0x0FF};
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public void setIOPortWord(int portAddress, byte[] dataWord)
            throws ModuleException
    {
        logger.log(Level.WARNING, "[" + super.getType() + "]"
                + " OUT command (word) to port "
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
            WriteOnlyPortException
    {
        logger.log(Level.WARNING, "[" + super.getType() + "]"
                + " IN command (double word) to port "
                + Integer.toHexString(portAddress).toUpperCase() + " received");
        logger.log(Level.WARNING, "[" + super.getType() + "]"
                + " Returned default value 0xFFFFFFFF");

        // Return dummy value 0xFFFFFFFF
        return new byte[]{(byte) 0x0FF, (byte) 0x0FF, (byte) 0x0FF,
                (byte) 0x0FF};
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public void setIOPortDoubleWord(int portAddress, byte[] dataDoubleWord)
            throws ModuleException
    {
        logger.log(Level.WARNING, "[" + super.getType() + "]"
                + " OUT command (double word) to port "
                + Integer.toHexString(portAddress).toUpperCase()
                + " received. No action taken.");
    }

    /**
     * Raise interrupt signal
     */
    protected void setInterrupt()
    {
        // Raise an interrupt at IRQ 6 at PIC
        ModulePIC pic = (ModulePIC) super.getConnection(Module.Type.PIC);
        pic.setIRQ(irqNumber);
        pendingIRQ = true;
        resetSenseInterrupt = 0;
    }

    /**
     * Clear interrupt signal
     */
    protected void clearInterrupt()
    {
        // Clear an interrupt at IRQ 6 at PIC
        if (pendingIRQ) {
            // Lower an interrupt at IRQ 6 at PIC
            ModulePIC pic = (ModulePIC) super.getConnection(Module.Type.PIC);
            pic.clearIRQ(irqNumber);
            pendingIRQ = false;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.ModuleFDC
     */
    @Override
    public boolean setNumberOfDrives(int totalDrives)
    {
        // Set number of drives (must be > 0 and <= 4)
        ModuleRTC rtc = (ModuleRTC) super.getConnection(Module.Type.RTC);

        if (totalDrives > 0 && totalDrives <= 4) {
            numberOfDrives = totalDrives;
            drives = new Drive[numberOfDrives];

            // Create new drives
            for (int i = 0; i < drives.length; i++) {
                drives[i] = new Drive();
            }

            // Set CMOS register 0x14 (bit 7,6) to total number of available
            // drives
            if (numberOfDrives == 1) {
                rtc.setCMOSRegister(0x14,
                        (byte) ((rtc.getCMOSRegister(0x14) & 0x3F) | 0x00));
            } else if (numberOfDrives == 2) {
                rtc.setCMOSRegister(0x14,
                        (byte) ((rtc.getCMOSRegister(0x14) & 0x3F) | 0x40));
            } else if (numberOfDrives == 3) {
                rtc.setCMOSRegister(0x14,
                        (byte) ((rtc.getCMOSRegister(0x14) & 0x3F) | 0x80));
            } else if (numberOfDrives == 4) {
                rtc.setCMOSRegister(0x14,
                        (byte) ((rtc.getCMOSRegister(0x14) & 0x3F) | 0xC0));
            }

            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.ModuleFDC
     */
    @Override
    public boolean insertCarrier(String driveLetter, byte carrierType,
                                 File imageFile, boolean writeProtected)
    {
        // Convert driveletter into index
        int driveIndex = -1;
        if (driveLetter.equalsIgnoreCase("A")) {
            driveIndex = 0;
        } else if (driveLetter.equalsIgnoreCase("B")) {
            driveIndex = 1;
        }

        // Perform operation
        return insertCarrier(driveIndex, carrierType, imageFile, writeProtected);
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.ModuleFDC
     */
    @Override
    public boolean ejectCarrier(String driveLetter)
    {
        // Convert driveletter into index
        int driveIndex = -1;
        if (driveLetter.equalsIgnoreCase("A")) {
            driveIndex = 0;
        } else if (driveLetter.equalsIgnoreCase("B")) {
            driveIndex = 1;
        }

        // Perform operation
        return ejectCarrier(driveIndex);
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.ModuleFDC
     */
    @Override
    public boolean insertCarrier(int driveIndex, byte carrierType,
                                 File imageFile, boolean writeProtected)
    {

        ModuleMotherboard motherboard = (ModuleMotherboard) super.getConnection(Module.Type.MOTHERBOARD);
        ModuleRTC rtc = (ModuleRTC) super.getConnection(Module.Type.RTC);
        ModulePIC pic = (ModulePIC) super.getConnection(Module.Type.PIC);
        ModuleDMA dma = (ModuleDMA) super.getConnection(Module.Type.DMA);
        ModuleATA ata = (ModuleATA) super.getConnection(Module.Type.ATA);

        // Select drive
        if (driveIndex == 0) {
            // Drive A
            String driveLetter = "A";

            // Check if drive A exists and if drive is empty
            if (drives.length > 0 && !(drives[driveIndex].containsFloppy())) {
                // Define drive type
                switch (carrierType) {
                    case FLOPPY_DISKTYPE_NONE:
                        rtc.setCMOSRegister(0x10, (byte) ((rtc
                                .getCMOSRegister(0x10) & 0x0f) | 0x00));
                        drives[driveIndex].setDriveType(FLOPPY_DRIVETYPE_NONE);
                        break;

                    case FLOPPY_DISKTYPE_360K:
                        rtc.setCMOSRegister(0x10, (byte) ((rtc
                                .getCMOSRegister(0x10) & 0x0f) | 0x10));
                        drives[driveIndex].setDriveType(FLOPPY_DRIVETYPE_525DD);
                        break;

                    case FLOPPY_DISKTYPE_1_2:
                        rtc.setCMOSRegister(0x10, (byte) ((rtc
                                .getCMOSRegister(0x10) & 0x0f) | 0x20));
                        drives[driveIndex].setDriveType(FLOPPY_DRIVETYPE_525HD);
                        break;

                    case FLOPPY_DISKTYPE_720K:
                        rtc.setCMOSRegister(0x10, (byte) ((rtc
                                .getCMOSRegister(0x10) & 0x0f) | 0x30));
                        drives[driveIndex].setDriveType(FLOPPY_DRIVETYPE_350DD);
                        break;

                    case FLOPPY_DISKTYPE_1_44:
                        rtc.setCMOSRegister(0x10, (byte) ((rtc
                                .getCMOSRegister(0x10) & 0x0f) | 0x40));
                        drives[driveIndex].setDriveType(FLOPPY_DRIVETYPE_350HD);
                        break;

                    case FLOPPY_DISKTYPE_2_88:
                        rtc.setCMOSRegister(0x10, (byte) ((rtc
                                .getCMOSRegister(0x10) & 0x0f) | 0x50));
                        drives[driveIndex].setDriveType(FLOPPY_DRIVETYPE_350ED);
                        break;

                    // CMOS reserved drive types
                    case FLOPPY_DISKTYPE_160K:
                        rtc.setCMOSRegister(0x10, (byte) ((rtc
                                .getCMOSRegister(0x10) & 0x0f) | 0x60));
                        drives[driveIndex].setDriveType(FLOPPY_DRIVETYPE_525DD);
                        logger.log(Level.WARNING, "[" + super.getType() + "]"
                                + " Drive " + driveLetter
                                + " set to reserved CMOS floppy drive type 6");
                        break;

                    case FLOPPY_DISKTYPE_180K:
                        rtc.setCMOSRegister(0x10, (byte) ((rtc
                                .getCMOSRegister(0x10) & 0x0f) | 0x70));
                        drives[driveIndex].setDriveType(FLOPPY_DRIVETYPE_525DD);
                        logger.log(Level.WARNING, "[" + super.getType() + "]"
                                + " Drive " + driveLetter
                                + " set to reserved CMOS floppy drive type 7");
                        break;

                    case FLOPPY_DISKTYPE_320K:
                        rtc.setCMOSRegister(0x10, (byte) ((rtc
                                .getCMOSRegister(0x10) & 0x0f) | 0x80));
                        drives[driveIndex].setDriveType(FLOPPY_DRIVETYPE_525DD);
                        logger.log(Level.WARNING, "[" + super.getType() + "]"
                                + " Drive " + driveLetter
                                + " set to reserved CMOS floppy drive type 8");
                        break;

                    default:
                        // Unknown floppy drive type
                        logger.log(Level.WARNING, "[" + super.getType() + "]"
                                + " Unsupported floppy drive type.");
                        break;
                }

                // Insert new floppy
                try {
                    drives[driveIndex].insertFloppy(carrierType, imageFile,
                            writeProtected);
                    logger.log(Level.WARNING, "[" + super.getType() + "]"
                            + " Floppy \"" + imageFile.getName()
                            + "\" is inserted in drive " + driveIndex);
                    return true;
                } catch (StorageDeviceException e) {
                    logger.log(Level.SEVERE, "[" + super.getType() + "]"
                            + " Error: " + e.getMessage());
                }
            } else {
                // Alert that floppy drive is not empty
                logger
                        .log(
                                Level.SEVERE,
                                "["
                                        + super.getType()
                                        + "]"
                                        + " Drive "
                                        + driveLetter
                                        + " does not exist or already contains a floppy. Eject floppy first!");
            }
        } else if (driveIndex == 1) {
            // Drive B
            String driveLetter = "B";

            // Check if drive B exists and if drive is empty
            if (drives.length > 1 && !(drives[driveIndex].containsFloppy())) {
                // Define drive type
                switch (carrierType) {
                    case FLOPPY_DISKTYPE_NONE:
                        rtc.setCMOSRegister(0x10, (byte) ((rtc
                                .getCMOSRegister(0x10) & 0xf0) | 0x00));
                        drives[driveIndex].setDriveType(FLOPPY_DRIVETYPE_NONE);
                        break;

                    case FLOPPY_DISKTYPE_360K:
                        rtc.setCMOSRegister(0x10, (byte) ((rtc
                                .getCMOSRegister(0x10) & 0xf0) | 0x01));
                        drives[driveIndex].setDriveType(FLOPPY_DRIVETYPE_525DD);
                        break;

                    case FLOPPY_DISKTYPE_1_2:
                        rtc.setCMOSRegister(0x10, (byte) ((rtc
                                .getCMOSRegister(0x10) & 0xf0) | 0x02));
                        drives[driveIndex].setDriveType(FLOPPY_DRIVETYPE_525HD);
                        break;

                    case FLOPPY_DISKTYPE_720K:
                        rtc.setCMOSRegister(0x10, (byte) ((rtc
                                .getCMOSRegister(0x10) & 0xf0) | 0x03));
                        drives[driveIndex].setDriveType(FLOPPY_DRIVETYPE_350DD);
                        break;

                    case FLOPPY_DISKTYPE_1_44:
                        rtc.setCMOSRegister(0x10, (byte) ((rtc
                                .getCMOSRegister(0x10) & 0xf0) | 0x04));
                        drives[driveIndex].setDriveType(FLOPPY_DRIVETYPE_350HD);
                        break;

                    case FLOPPY_DISKTYPE_2_88:
                        rtc.setCMOSRegister(0x10, (byte) ((rtc
                                .getCMOSRegister(0x10) & 0xf0) | 0x05));
                        drives[driveIndex].setDriveType(FLOPPY_DRIVETYPE_350ED);
                        break;

                    // CMOS reserved drive types
                    case FLOPPY_DISKTYPE_160K:
                        rtc.setCMOSRegister(0x10, (byte) ((rtc
                                .getCMOSRegister(0x10) & 0xf0) | 0x06));
                        drives[driveIndex].setDriveType(FLOPPY_DRIVETYPE_525DD);
                        logger.log(Level.WARNING, "[" + super.getType() + "]"
                                + " Drive " + driveLetter
                                + " set to reserved CMOS floppy drive type 6");
                        break;

                    case FLOPPY_DISKTYPE_180K:
                        rtc.setCMOSRegister(0x10, (byte) ((rtc
                                .getCMOSRegister(0x10) & 0xf0) | 0x07));
                        drives[driveIndex].setDriveType(FLOPPY_DRIVETYPE_525DD);
                        logger.log(Level.WARNING, "[" + super.getType() + "]"
                                + " Drive " + driveLetter
                                + " set to reserved CMOS floppy drive type 7");
                        break;

                    case FLOPPY_DISKTYPE_320K:
                        rtc.setCMOSRegister(0x10, (byte) ((rtc
                                .getCMOSRegister(0x10) & 0xf0) | 0x08));
                        drives[driveIndex].setDriveType(FLOPPY_DRIVETYPE_525DD);
                        logger.log(Level.WARNING, "[" + super.getType() + "]"
                                + " Drive " + driveLetter
                                + " set to reserved CMOS floppy drive type 8");
                        break;

                    default:
                        // Unknown floppy drive type
                        logger.log(Level.WARNING, "[" + super.getType() + "]"
                                + " Unsupported floppy drive type.");
                        break;
                }

                // Insert new floppy
                try {
                    drives[driveIndex].insertFloppy(carrierType, imageFile,
                            writeProtected);
                    logger.log(Level.INFO, "[" + super.getType() + "]"
                            + " Floppy \"" + imageFile.getName()
                            + "\" is inserted in drive " + driveIndex);
                    return true;
                } catch (StorageDeviceException e) {
                    logger.log(Level.SEVERE, "[" + super.getType() + "]"
                            + " Error: " + e.getMessage());
                }
            } else {
                // Alert that floppy drive is not empty
                logger
                        .log(
                                Level.SEVERE,
                                "["
                                        + super.getType()
                                        + "]"
                                        + " Drive "
                                        + driveLetter
                                        + " does not exist or already contains a floppy. Eject floppy first!");
            }
        } else {
            logger
                    .log(
                            Level.SEVERE,
                            "["
                                    + super.getType()
                                    + "]"
                                    + " Can not insert floppy because additional drives are not implemented.");
        }

        // Inserting floppy was not successful
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.ModuleFDC
     */
    @Override
    public boolean ejectCarrier(int driveIndex)
    {
        // Try to eject the floppy by removing reference (will be carbage
        // collected)
        try {
            if (driveIndex != -1) {
                boolean writeProtected = drives[driveIndex].writeProtected;
                drives[driveIndex].ejectFloppy();

                logger.log(Level.INFO, "[" + super.getType() + "]"
                        + " Floppy is ejected from drive " + drive + ".");

                if (!writeProtected) {
                    logger.log(Level.INFO, "[" + super.getType() + "]"
                            + " Floppy data is stored to image file.");
                }
                return true;
            } else {
                // Floppy could not be ejected
                logger
                        .log(
                                Level.SEVERE,
                                "["
                                        + super.getType()
                                        + "]"
                                        + " Can not eject floppy because drive is not recognized.");
            }
        } catch (StorageDeviceException e) {
            logger.log(Level.SEVERE, "[" + super.getType() + "]" + e.getMessage());
            return false;
        }

        return false;
    }

    /**
     * Execute command of FDC Note: assumed is that all bytes of the command are
     * fetched. After execution of the command, the FDC will automatically enter
     * the result or idle phase.
     */
    private void executeCommand()
    {

        ModuleMotherboard motherboard = (ModuleMotherboard) super.getConnection(Module.Type.MOTHERBOARD);
        ModuleDMA dma = (ModuleDMA) super.getConnection(Module.Type.DMA);

        // Drive parameters
        int drv, hds, cylinder, sector, eot;
        int sectorSize, sectorTime, logicalSector, dataLength;
        boolean ableToTransfer;

        /*
         * String commandString = ""; for (int i = 0; i < commandSize; i++) {
         * commandString += "[" + Integer.toHexString(0x100 | command[i] &
         * 0xFF).substring(1) + "] "; } logger.log(Level.CONFIG, "[" +
         * super.getType() + "]" + " COMMAND: " + commandString);
         */
        // Get first part of command
        commandPending = command[0] & 0xFF;
        switch (commandPending) {
            case 0x03: // Specify (3 bytes)
                // Execution : specified parameters are loaded
                // Result : none, no interrupt
                logger.log(Level.INFO, "[" + super.getType() + "]" + " CMD: specify");

                srt = (byte) ((command[1] >> 4) & 0x0F);
                hut = (byte) (command[1] & 0x0F);
                hlt = (byte) ((command[2] >> 1) & 0x7F);
                nonDMA = (byte) (command[2] & 0x01);
                // Ready, goto idle phase
                this.enterIdlePhase();
                break;

            case 0x04: // Sense drive status (2 bytes)
                // Execution :
                // Result : status register 3 is set
                logger.log(Level.INFO, "[" + super.getType() + "]"
                        + " CMD: sense drive status");

                drv = (command[1] & 0x03);
                drives[drv].hds = (command[1] >> 2) & 0x01;
                statusRegister3 = (byte) (0x28 | (drives[drv].hds << 2) | drv);
                statusRegister3 |= (drives[drv].writeProtected ? 0x40 : 0x00);
                // check if 'track 0'-bit should be set in status register 3
                if (drives[drv].cylinder == 0) {
                    statusRegister3 |= 0x10;
                }
                // Ready, goto result phase
                this.enterResultPhase();
                break;

            case 0x45: // write normal data, MT=0
            case 0x46: // read normal data, MT=0, SK=0
            case 0x66: // read normal data, MT=0, SK=1
            case 0xC5: // write normal data, MT=1, MFM=1
            case 0xC6: // read normal data, MT=1, SK=0
            case 0xE6: // read normal data, MT=1, SK=1
                // Execution : data transfer between the FDD and CPU/memory
                // Result : status info (st0,st1,st2) + sector ID
                logger.log(Level.INFO, "[" + super.getType() + "]"
                        + " CMD: Read/write data");

                // Update status to emulator
                emu.statusChanged(Emulator.MODULE_FDC_TRANSFER_START);

                // Check if DMA is enabled
                if ((dor & 0x08) == 0) {
                    // TODO: this should also work, but for now DMA is necessary
                    logger.log(Level.WARNING, "[" + super.getType() + "]"
                            + " CMD: read/write normal data -> DMA is disabled");
                }
                drv = command[1] & 0x03;
                dor &= 0xFC;
                dor |= drv;

                // Set drive parameters
                drives[drv].multiTrack = (((command[0] >> 7) & 0x01) == 0x01 ? true
                        : false);
                cylinder = command[2]; // 0..79 depending
                hds = command[3] & 0x01;
                sector = command[4]; // 1..36 depending
                sectorSize = command[5];
                eot = command[6]; // 1..36 depending
                dataLength = command[8];

                // Perform some necessary checks
                ableToTransfer = true;

                // Check if motor is running
                if (!(drives[drv].isMotorRunning())) {
                    logger
                            .log(
                                    Level.WARNING,
                                    "["
                                            + super.getType()
                                            + "]"
                                            + " CMD: read/write normal data -> drive motor of drive "
                                            + drv + " is not running.");
                    msr = FDC_CMD_BUSY;
                    ableToTransfer = false;
                }

                // Check drive type
                if (drives[drv].getDriveType() == FLOPPY_DRIVETYPE_NONE) {
                    logger
                            .log(
                                    Level.WARNING,
                                    "["
                                            + super.getType()
                                            + "]"
                                            + " CMD: read/write normal data -> incorrect drive type if drive "
                                            + drv + ".");
                    msr = FDC_CMD_BUSY;
                    ableToTransfer = false;
                }
                // Check head number match
                if (hds != ((command[1] >> 2) & 0x01)) {
                    logger
                            .log(
                                    Level.WARNING,
                                    "["
                                            + super.getType()
                                            + "] head number in command[1] doesn't match head field");
                    ableToTransfer = false;
                    statusRegister0 = 0x40 | (drives[drv].hds << 2) | drv; // abnormal
                    // termination
                    statusRegister1 = 0x04; // 0000 0100
                    statusRegister2 = 0x00; // 0000 0000
                    enterResultPhase();

                }
                // Check if floppy is present
                if (!drives[drv].containsFloppy()) {
                    logger
                            .log(
                                    Level.WARNING,
                                    "["
                                            + super.getType()
                                            + "]"
                                            + " CMD: read/write normal data -> floppy is not inserted in drive "
                                            + drv + ".");
                    msr = FDC_CMD_BUSY;
                    ableToTransfer = false;
                }
                // Check sector size
                if (sectorSize != 0x02) // 512 bytes
                {
                    logger
                            .log(
                                    Level.WARNING,
                                    "["
                                            + super.getType()
                                            + "]"
                                            + " CMD: read/write normal data -> sector size (bytes per sector) not supported.");
                    ableToTransfer = false;
                }
                // Check if cylinder is not higher than highest available cylinder
                // on disk
                if (cylinder >= drives[drv].tracks) {
                    logger
                            .log(
                                    Level.WARNING,
                                    "["
                                            + super.getType()
                                            + "]"
                                            + " CMD: read/write normal data -> cylinder number exceeds maximum number of tracks.");
                    ableToTransfer = false;
                }
                // Check if sector number is not higher than maximum available
                // sectors per track
                if (sector > drives[drv].sectorsPerTrack) {
                    logger.log(Level.WARNING, "[" + super.getType() + "]"
                            + " CMD: read/write normal data -> sector number ("
                            + sector + ") exceeds sectors per track ("
                            + drives[drv].sectorsPerTrack + ").");
                    drives[drv].cylinder = cylinder;
                    drives[drv].hds = hds;
                    drives[drv].sector = sector;

                    statusRegister0 = 0x40 | (drives[drv].hds << 2) | drv;
                    statusRegister1 = 0x04;
                    statusRegister2 = 0x00;
                    enterResultPhase();
                    return;
                }
                // Check if cylinder does not differ from drive parameter
                if (cylinder != drives[drv].cylinder) {
                    logger
                            .log(
                                    Level.CONFIG,
                                    "["
                                            + super.getType()
                                            + "]"
                                            + " CMD: read/write normal data -> requested cylinder differs from selected cylinder on drive. Will proceed.");
                    drives[drv].resetChangeline();
                }

                // Compute logical sector
                logicalSector = (cylinder * drives[drv].heads * drives[drv].sectorsPerTrack)
                        + (hds * drives[drv].sectorsPerTrack) + (sector - 1);

                logger.log(Level.CONFIG, "[" + super.getType() + "]"
                        + " Logical sectors calculated: " + logicalSector);

                // Check if logical sector does not exceed total number of available
                // sectors on disk
                if (logicalSector >= drives[drv].sectors) {
                    logger
                            .log(
                                    Level.WARNING,
                                    "["
                                            + super.getType()
                                            + "]"
                                            + " CMD: read/write normal data -> logical sectors exceeds total number of sectors on disk.");
                    ableToTransfer = false;
                }

                // Start transfer
                if (ableToTransfer) {
                    // Check if end of track is 0, if so assign total sectors per
                    // track as upper boundary
                    if (eot == 0) {
                        eot = drives[drv].sectorsPerTrack;
                    }

                    // Now that parameters are validated, assign them to drive
                    drives[drv].cylinder = cylinder;
                    drives[drv].hds = hds;
                    drives[drv].sector = sector;
                    drives[drv].eot = eot;

                    if ((command[0] & 0x4F) == 0x46) {
                        // Read data from floppy
                        try {
                            drives[drv].readData(logicalSector * 512, 512,
                                    floppyBuffer);
                        } catch (StorageDeviceException e) {
                            logger.log(Level.WARNING, "[" + super.getType() + "]" + " "
                                    + e.getMessage());
                        }

                        // data register not ready yet, controller set to busy
                        msr = FDC_CMD_BUSY;

                        // NOTE (bram): Added this undocumented msr feature for DMA:
                        // Set data direction to read
                        msr |= FDC_CMD_DIO;

                        // Activate timer
                        sectorTime = 200000 / drives[drv].sectorsPerTrack;
                        motherboard.resetTimer(this, sectorTime);
                        motherboard.setTimerActiveState(this, true);

                    } else if ((command[0] & 0x7F) == 0x45) {
                        // Write data to floppy
                        // data register not ready yet, controller set to busy
                        msr = FDC_CMD_BUSY;

                        // NOTE (bram): Added this undocumented msr feature for DMA:
                        // Set data direction to write
                        msr &= ~FDC_CMD_DIO;

                        // Set a request to perform DMA transfer
                        if (emu.isCpu32bit()) {
                            dma32.holdDREQ(FDC_DMA_CHANNEL & 3);
                        } else {
                            dma.setDMARequest(FDC_DMA_CHANNEL, true);
                        }

                        // FIXME: is the distinction between read/write correct? No
                        // timer is used here, but it seems to work
                    } else {
                        // NOTE (bram): Added this undocumented msr feature for DMA:
                        // Set data direction to read; for safety.
                        msr |= FDC_CMD_DIO;
                        logger.log(Level.WARNING, "[" + super.getType() + "]"
                                + " CMD: unknown read/write command");
                    }
                } else {
                    logger.log(Level.SEVERE, "[" + super.getType() + "]"
                            + " CMD: not able to transfer data");
                }
                break;

            case 0x07: // Recalibrate (2 bytes)
                // Execution : head retracked to track 0
                // Result : none, no interrupt
                logger.log(Level.INFO, "[" + super.getType() + "]"
                        + " CMD: recalibrate drive");

                drv = (command[1] & 0x03);
                // Make sure FDC parameters in DOR are all enabled
                dor &= 0xFC;
                dor |= drv;

                // Delay calibration
                motherboard.resetTimer(this, calculateStepDelay(drv, 0));
                motherboard.setTimerActiveState(this, true);

                // Head retracked to track 0
                drives[drv].cylinder = 0;
                // Controller set to non-busy
                // FDC is busy, 1=active, 0=not active (this is different from what
                // Bochs does!!!)
                msr = (byte) (1 << drv);
                break;

            case 0x08: // Sense interrupt status (1 byte)
                // Execution : get status
                // Result : status information (status reg0 + current cylinder
                // number) at the end of each seek operation about the FDC, no
                // interrupt
                logger.log(Level.INFO, "[" + super.getType() + "]"
                        + " CMD: sense interrupt status");

                // Set status register 0 based on interrupt
                if (resetSenseInterrupt > 0) {
                    drv = 4 - resetSenseInterrupt;
                    statusRegister0 &= 0xF8;
                    statusRegister0 |= (drives[drv].hds << 2) | drv;
                    resetSenseInterrupt--;
                } else if (!pendingIRQ) {
                    statusRegister0 = 0x80;
                }
                this.enterResultPhase();
                break;

            case 0x4A: // Read ID (2 bytes)
                // Execution : the first correct ID information on the cylinder is
                // stored in data register
                // Result : status info + sector ID
                logger.log(Level.INFO, "[" + super.getType() + "]" + " CMD: read ID");

                drv = command[1] & 0x03;
                drives[drv].hds = (command[1] >> 2) & 0x01;
                dor &= 0xFC;
                dor |= drv;

                // Check if motor is running
                if (drives[drv].isMotorRunning()) {
                    logger.log(Level.WARNING, "[" + super.getType() + "]"
                            + " CMD: read ID -> drive motor is not running.");
                    msr = FDC_CMD_BUSY;
                    return; // Hang controller
                }
                // Check drive type
                if (drives[drv].getDriveType() == FLOPPY_DRIVETYPE_NONE) {
                    logger.log(Level.WARNING, "[" + super.getType() + "]"
                            + " CMD: read ID -> incorrect drive type.");
                    msr = FDC_CMD_BUSY;
                    return; // Hang controller
                }
                // Check if floppy is present
                if (!drives[drv].containsFloppy()) {
                    logger.log(Level.WARNING, "[" + super.getType() + "]"
                            + " CMD: read ID -> floppy is not inserted.");
                    msr = FDC_CMD_BUSY;
                    return; // Hang controller
                }

                statusRegister0 = (drives[drv].hds << 2) | drv;
                // time to read one sector at 300 rpm

                // Activate timer
                sectorTime = 200000 / drives[drv].sectorsPerTrack;
                motherboard.resetTimer(this, sectorTime);
                motherboard.setTimerActiveState(this, true);

                // Data register not ready, controller busy
                msr = FDC_CMD_BUSY;
                this.enterResultPhase();
                break;

            case 0x4D: // Format track (10 bytes)
                // Execution : FDC formats an entire cylinder
                // Result : status info + sector ID
                logger.log(Level.INFO, "[" + super.getType() + "]"
                        + " CMD: format track");

                drv = command[1] & 0x03;
                dor &= 0xFC;
                dor |= drv;

                // Check if motor is running
                if (drives[drv].isMotorRunning()) {
                    logger.log(Level.WARNING, "[" + super.getType() + "]"
                            + " CMD: format track -> drive motor is not running.");
                    msr = FDC_CMD_BUSY;
                    return; // Hang controller
                }

                // Determine head
                drives[drv].hds = (command[1] >> 2) & 0x01;

                // Check drive type
                if (drives[drv].getDriveType() == FLOPPY_DRIVETYPE_NONE) {
                    logger.log(Level.WARNING, "[" + super.getType() + "]"
                            + " CMD: format track -> incorrect drive type.");
                    msr = FDC_CMD_BUSY;
                    return; // Hang controller
                }
                // Check if floppy is present
                if (!drives[drv].containsFloppy()) {
                    logger.log(Level.WARNING, "[" + super.getType() + "]"
                            + " CMD: format track -> floppy is not inserted.");
                    msr = FDC_CMD_BUSY;
                    return; // Hang controller
                }

                // Determine number of bytes per sector
                sectorSize = command[2];
                // Determine number of sectors per cylinder
                formatCount = command[3];
                // Determine filler byte
                formatFillbyte = command[5];

                // Check sector size
                if (sectorSize != 0x02) // 512 bytes
                {
                    logger
                            .log(
                                    Level.WARNING,
                                    "["
                                            + super.getType()
                                            + "]"
                                            + " CMD: format track -> sector size (bytes per sector) not supported.");
                }
                // Check expected number of sectors
                if (formatCount != drives[drv].sectorsPerTrack) {
                    logger
                            .log(
                                    Level.WARNING,
                                    "["
                                            + super.getType()
                                            + "]"
                                            + " CMD: format track -> wrong number of sectors per track encountered.");
                }
                // Check if floppy is write protected
                if (drives[drv].writeProtected) {
                    // Floppy is write-protected
                    logger.log(Level.SEVERE, "[" + super.getType() + "]"
                            + " CMD: format track -> floppy is write protected.");
                    statusRegister0 = 0x40 | (drives[drv].hds << 2) | drv; // abnormal
                    // termination
                    statusRegister1 = 0x27; // 0010 0111
                    statusRegister2 = 0x31; // 0011 0001
                    this.enterResultPhase();
                    return;
                }

                // 4 header bytes per sector are required
                formatCount = formatCount * 4;
                // Format a track
                if (emu.isCpu32bit()) {
                    dma32.holdDREQ(FDC_DMA_CHANNEL & 3);
                } else {
                    dma.setDMARequest(FDC_DMA_CHANNEL, true);
                }

                // Data register not ready, controller busy
                msr = FDC_CMD_BUSY;
                break;

            case 0x0F: // Seek (3 bytes)
                // Execution : position head over specified cylinder
                // Result : none, issues an interrupt
                logger.log(Level.INFO, "[" + super.getType() + "]" + " CMD: seek");

                drv = command[1] & 0x03;
                dor &= 0xFC;
                dor |= drv;

                // Set current head number
                drives[drv].hds = (command[1] >> 2) & 0x01;

                // Activate timer
                motherboard.resetTimer(this, calculateStepDelay(drv, command[2]));
                motherboard.setTimerActiveState(this, true);

                // Go to the specified cylinder
                drives[drv].cylinder = command[2];

                // Data register not ready, drive busy
                msr = (byte) (1 << drv);
                break;

            // Enhanced drives (EHD) commands
            case 0x0E: // Dump registers (Enhanced, 1 byte)
                logger.log(Level.INFO, "[" + super.getType() + "]"
                        + " CMD: dump registers (EHD)");
                this.enterResultPhase();
                break;

            case 0x12: // Perpendicular mode (Enhanced, 2 bytes)
                logger.log(Level.INFO, "[" + super.getType() + "]"
                        + " CMD: perpendicular mode (EHD)");
                perpMode = command[1];
                this.enterIdlePhase();
                break;

            case 0x13: // Configure (Enhanced, 4 bytes)
                logger.log(Level.INFO, "[" + super.getType() + "]"
                        + " CMD: configure (EHD)");
                config = command[2];
                preTrack = command[3];
                this.enterIdlePhase();
                break;

            default: // invalid or unsupported command; these are captured in
                // write() above
                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + " Unsupported FDC command 0x" + command[0]);
        }
    }

    /**
     * Store result after execution phase
     */
    private void enterResultPhase()
    {
        // Init variables
        int drv = dor & 0x03;
        resultIndex = 0;
        msr &= 0x0f; // leave drive status untouched
        msr |= FDC_CMD_MRQ | FDC_CMD_DIO | FDC_CMD_BUSY;

        // invalid command
        if ((statusRegister0 & 0xc0) == 0x80) {
            resultSize = 1;
            result[0] = (byte) statusRegister0;
            logger.log(Level.WARNING, "[" + super.getType() + "]"
                    + " result phase: invalid command.");
            return;
        }

        // Set result depending on type of command
        switch (commandPending) {
            case 0x04: // Sense drive status
                resultSize = 1;
                result[0] = (byte) statusRegister3;
                break;

            case 0x08: // Sense interrupt status
                resultSize = 2;
                result[0] = (byte) statusRegister0;
                result[1] = (byte) drives[drv].cylinder;
                break;

            case 0x45:
            case 0x46:
            case 0x4A: // Read ID
            case 0x4D: // Format a track
            case 0x66:
            case 0xC5:
            case 0xC6:
            case 0xE6:
                resultSize = 7;
                result[0] = (byte) statusRegister0;
                result[1] = (byte) statusRegister1;
                result[2] = (byte) statusRegister2;
                result[3] = (byte) drives[drv].cylinder;
                result[4] = (byte) drives[drv].hds;
                result[5] = (byte) drives[drv].sector;
                result[6] = 2; // Sector size code
                // Raise interrupt
                this.setInterrupt();
                break;

            // Enhanced FDC commands
            case 0x0E: // Dump registers
                resultSize = 10;
                // Dump number of cylinders from drives (only 2 drives, fill other 2
                // with 0)
                for (int i = 0; i < 2; i++) {
                    result[i] = (byte) drives[i].cylinders;
                }
                result[2] = 0;
                result[3] = 0;
                // Dump other FDC variables
                result[4] = (byte) (((srt << 4) & 0xF0) | hut);
                result[5] = (byte) (((hlt << 1) & 0xFE) | nonDMA);
                result[6] = (byte) drives[drv].eot;
                result[7] = (byte) ((lock << 7) | (perpMode & 0x7f));
                result[8] = config;
                result[9] = preTrack;
                break;

            case 0x10: // Version
                resultSize = 1;
                result[0] = (byte) 0x90;
                break;

            case 0x14: // Unlock
            case 0x94: // Lock
                lock = (byte) (commandPending >> 7);
                resultSize = 1;
                result[0] = (byte) (lock << 4);
                break;

            default:
                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + " CMD: no command match");
                break;
        }

        // Send ready message to emulator
        emu.statusChanged(Emulator.MODULE_FDC_TRANSFER_STOP);
    }

    /**
     * Reset parameters after result or execution phase
     */
    private void enterIdlePhase()
    {
        // Reset registers
        msr &= 0x0F; // leave drive status untouched
        msr |= FDC_CMD_MRQ; // data register ready

        // Reset command variables
        commandComplete = true;
        commandIndex = 0;
        commandSize = 0;
        commandPending = 0;

        // Reset buffer index
        floppyBufferIndex = 0;

        logger
                .log(Level.INFO, "[" + super.getType() + "]"
                        + " idle phase finished");
    }

    /**
     * Get byte from floppy buffer for DMA transfer This method is used for DMA
     * transfer a byte from FDC to memory
     *
     * @return byte current byte from floppy buffer
     */
    protected byte getDMAByte()
    {

        ModuleMotherboard motherboard = (ModuleMotherboard) super.getConnection(Module.Type.MOTHERBOARD);
        ModuleRTC rtc = (ModuleRTC) super.getConnection(Module.Type.RTC);
        ModulePIC pic = (ModulePIC) super.getConnection(Module.Type.PIC);
        ModuleDMA dma = (ModuleDMA) super.getConnection(Module.Type.DMA);
        ModuleATA ata = (ModuleATA) super.getConnection(Module.Type.ATA);

        int drv, logicalSector, sectorTime;

        // if (floppyBufferIndex > 500)
        // logger.log(Level.INFO, "[" + super.getType() + "]" + " MEM(DMA) byte: " +
        // floppyBufferIndex + "=" +
        // Integer.toHexString(floppyBuffer[floppyBufferIndex] &
        // 0xFF).toUpperCase());

        floppyBufferCurrentByte = floppyBuffer[floppyBufferIndex];

        // Increment buffer index
        floppyBufferIndex++;

        // Get Terminal Count from DMA
        tc = dma.isTerminalCountReached();

        if ((floppyBufferIndex >= 512) || tc) {

            drv = dor & 0x03;

            if (floppyBufferIndex >= 512) {
                // Increment the sector
                drives[drv].incrementSector();

                // Reset the bufferindex to 0;
                floppyBufferIndex = 0;
            }

            // Check if transfer has completed
            if (tc) {
                // End of transfer
                statusRegister0 = ((drives[drv].hds) << 2) | drv;
                statusRegister1 = 0;
                statusRegister2 = 0;

                // Clear DMA request because transfer is finished
                dma.setDMARequest(FDC.FDC_DMA_CHANNEL, false);
                this.enterResultPhase();
            } else {
                // Still data remaining to transfer (while floppyBuffer has to
                // be reloaded)

                // Recompute logical sector
                logicalSector = (drives[drv].cylinder * drives[drv].heads * drives[drv].sectorsPerTrack)
                        + (drives[drv].hds * drives[drv].sectorsPerTrack)
                        + (drives[drv].sector - 1);

                // Read new data into floppy buffer
                try {
                    drives[drv].readData(logicalSector * 512, 512,
                            floppyBuffer);
                } catch (StorageDeviceException e) {
                    logger.log(Level.WARNING, "[" + super.getType() + "]" + " "
                            + e.getMessage());
                }

                // Clear DMA request - it is enabled directly again via timer
                // and update
                dma.setDMARequest(FDC.FDC_DMA_CHANNEL, false);

                // Activate timer to be ready for the next read
                sectorTime = 200000 / drives[drv].sectorsPerTrack;
                logger.log(Level.CONFIG, motherboard
                        .getCurrentInstructionNumber()
                        + " "
                        + "["
                        + super.getType()
                        + "]"
                        + " Activating floppy time to sector time of "
                        + sectorTime + "(" + sectorTime * 5 + ")");
                motherboard.resetTimer(this, sectorTime);
                motherboard.setTimerActiveState(this, true);
            }

        }

        return floppyBufferCurrentByte;
    }

    /**
     * Set byte in floppy buffer for DMA transfer This method is used for DMA
     * transfer a byte from memory to FDC
     *
     * @param data
     */
    protected void setDMAByte(byte data)
    {

        ModuleMotherboard motherboard = (ModuleMotherboard) super.getConnection(Module.Type.MOTHERBOARD);
        ModuleRTC rtc = (ModuleRTC) super.getConnection(Module.Type.RTC);
        ModulePIC pic = (ModulePIC) super.getConnection(Module.Type.PIC);
        ModuleDMA dma = (ModuleDMA) super.getConnection(Module.Type.DMA);
        ModuleATA ata = (ModuleATA) super.getConnection(Module.Type.ATA);

        int drv, logicalSector, sectorTime;

        // Get Terminal Count from DMA
        tc = dma.isTerminalCountReached();

        // Select drive
        drv = dor & 0x03;

        if (commandPending == 0x4D) {
            // Format track in progress
            // Lower number of sectors in track to format
            formatCount--;

            switch (3 - (formatCount & 0x03)) {
                case 0: // Set cylinder
                    drives[drv].cylinder = data;
                    break;

                case 1: // Check head number
                    if (data != drives[drv].hds) {
                        logger
                                .log(
                                        Level.WARNING,
                                        "["
                                                + super.getType()
                                                + "]"
                                                + " DMA transfer formatting track: head number does not match head field.");
                    }
                    break;

                case 2: // Set sector
                    drives[drv].sector = data;
                    break;

                case 3: // Format buffer
                    if (data != 2) {
                        logger
                                .log(
                                        Level.WARNING,
                                        "["
                                                + super.getType()
                                                + "]"
                                                + " DMA transfer formatting track: sector size is not supported.");
                    } else {
                        logger.log(Level.INFO, "[" + super.getType() + "]"
                                + " DMA transfer formatting track: cyl="
                                + drives[drv].cylinder + ", head="
                                + drives[drv].hds + ", sector="
                                + drives[drv].sector);

                        // Format buffer with given fillbyte (set earlier with
                        // command)
                        for (int i = 0; i < 512; i++) {
                            floppyBuffer[i] = formatFillbyte;
                        }

                        // Recompute logical sector
                        logicalSector = (drives[drv].cylinder
                                * drives[drv].heads * drives[drv].sectorsPerTrack)
                                + (drives[drv].hds * drives[drv].sectorsPerTrack)
                                + (drives[drv].sector - 1);

                        // Write new data from buffer to floppy
                        try {
                            drives[drv].writeData(logicalSector * 512, 512,
                                    floppyBuffer);
                        } catch (StorageDeviceException e) {
                            logger.log(Level.WARNING, "[" + super.getType() + "]" + " "
                                    + e.getMessage());
                        }

                        // Clear DMA request - it is enabled directly again via
                        // timer and update
                        dma.setDMARequest(FDC.FDC_DMA_CHANNEL, false);

                        // Activate timer to be ready for the next read
                        sectorTime = 200000 / drives[drv].sectorsPerTrack;
                        motherboard.resetTimer(this, sectorTime);
                        motherboard.setTimerActiveState(this, true);
                    }
                    break;

                default:
                    logger.log(Level.WARNING, "[" + super.getType() + "]"
                            + " DMA transfer formatting track failed.");
                    break;
            }
        } else {
            // Write normal data
            // Store data byte in buffer
            floppyBuffer[floppyBufferIndex++] = data;

            // Check if buffer is full and ready to write to floppy
            if ((floppyBufferIndex >= 512) || (tc)) {
                // Recompute logical sector
                logicalSector = (drives[drv].cylinder * drives[drv].heads * drives[drv].sectorsPerTrack)
                        + (drives[drv].hds * drives[drv].sectorsPerTrack)
                        + (drives[drv].sector - 1);

                if (drives[drv].writeProtected) {
                    // Floppy is write protected
                    logger
                            .log(
                                    Level.WARNING,
                                    "["
                                            + super.getType()
                                            + "]"
                                            + " DMA transfer to floppy failed: floppy is write protected.");
                    // ST0: IC1,0=01 (abnormal termination: started execution
                    // but failed)
                    statusRegister0 = 0x40 | (drives[drv].hds << 2) | drv;
                    // ST1: DataError=1, NDAT=1, NotWritable=1, NID=1
                    statusRegister1 = 0x27; // 0010 0111
                    // ST2: CRCE=1, SERR=1, BCYL=1, NDAM=1.
                    statusRegister2 = 0x31; // 0011 0001
                    this.enterResultPhase();
                    return;
                }

                // Write new data from buffer to floppy
                try {
                    drives[drv].writeData(logicalSector * 512, 512,
                            floppyBuffer);
                    drives[drv].incrementSector(); // increment to next sector
                    // after writing current
                    // one

                    // Reset floppy buffer index
                    floppyBufferIndex = 0;
                } catch (StorageDeviceException e) {
                    logger.log(Level.WARNING, "[" + super.getType() + "]" + " "
                            + e.getMessage());
                }

                // Clear DMA request - it is enabled directly again via timer
                // and update
                dma.setDMARequest(FDC.FDC_DMA_CHANNEL, false);

                // Activate timer to be ready for the next read
                sectorTime = 200000 / drives[drv].sectorsPerTrack;
                motherboard.resetTimer(this, sectorTime);
                motherboard.setTimerActiveState(this, true);
            }
        }
    }

    /**
     * Calculate the delay for timer This method makes an approximation of the
     * delay in the drive It does this based on the gap between current position
     * of head in cylinder and desired cylinder
     *
     * @param drive
     * @param newCylinder
     * @return -
     */
    protected int calculateStepDelay(int drive, int newCylinder)
    {
        int numSteps;
        int oneStepDelayTime;

        // Check if current cylinder is already the same as desired cylinder
        if (newCylinder == drives[drive].cylinder) {
            numSteps = 1;
        } else {
            // Calculate the number of steps to make
            numSteps = Math.abs(newCylinder - drives[drive].cylinder);
            // Notify drive that floppy has changed
            drives[drive].resetChangeline();
        }

        // Calculate the delay for one step (this value is based on Bochs)
        oneStepDelayTime = ((srt ^ 0x0f) + 1) * 500000 / dataRates[dataRate];

        logger.log(Level.INFO, "[" + super.getType() + "]"
                + " Calculated step delay: " + numSteps * oneStepDelayTime);

        return (numSteps * oneStepDelayTime);
    }

    /**
     * Unregisters all registered devices (IRQ, timer, DMA)
     *
     * @return boolean true if succesfully, false otherwise
     */
    private boolean unregisterDevices()
    {

        ModuleMotherboard motherboard = (ModuleMotherboard) super.getConnection(Module.Type.MOTHERBOARD);
        ModuleRTC rtc = (ModuleRTC) super.getConnection(Module.Type.RTC);
        ModulePIC pic = (ModulePIC) super.getConnection(Module.Type.PIC);
        ModuleDMA dma = (ModuleDMA) super.getConnection(Module.Type.DMA);
        ModuleATA ata = (ModuleATA) super.getConnection(Module.Type.ATA);

        boolean reslt = false;

        // Unregister IRQ number
        // Make sure no interrupt is pending
        pic.clearIRQ(irqNumber);
        // result = pic.unregisterIRQNumber(this);
        logger.log(Level.CONFIG, "[" + super.getType() + "]"
                + " IRQ unregister result: " + reslt);

        // Unregister timer
        // result = motherboard.unregisterTimer(this);
        logger.log(Level.CONFIG, "[" + super.getType() + "]"
                + " Timer unregister result: " + reslt);

        // Unregister DMA channel
        // result = dma.unregisterDMAChannel(FDC_DMA_CHANNEL);
        logger.log(Level.CONFIG, "[" + super.getType() + "]"
                + " DMA unregister result: " + reslt);

        return reslt;
    }

    /**
     * @param nchan
     * @param pos
     * @param size
     * @return -
     */
    public int transferHandler(int nchan, int pos, int size)
    {
        final int SECTOR_LENGTH = 512; // Standard length of a sector

        // Determine max size of one DMA transfer
        int dmaSize = Math.min(size, SECTOR_LENGTH);

        if (drives[drive] == null) {
            // Check if this ever occurs:
            logger.log(Level.SEVERE, "[" + super.getType() + "]"
                    + " no floppy in DMA transfer, aborting");
            return 0;
        }

        int startOffset;
        for (startOffset = floppyBufferIndex; startOffset < size; startOffset += SECTOR_LENGTH) {
            int relativeOffset = startOffset % SECTOR_LENGTH;

            // Determine DMA direction, based on bit 6 of msr. NOTE (bram):
            // Added this undocumented msr feature for DMA.
            if ((msr & FDC_CMD_DIO) == FDC_CMD_DIO) {
                // From floppy to memory
                dma32.writeMemory(nchan, floppyBuffer, relativeOffset,
                        startOffset, dmaSize);
            } else {
                // From memory to floppy
                dma32.readMemory(nchan, floppyBuffer, relativeOffset,
                        startOffset, dmaSize);
            }

            // Update the current sector if completely read/written
            if (relativeOffset == 0) {
                // Read new data from/to floppy buffer
                try {
                    if ((msr & FDC_CMD_DIO) == FDC_CMD_DIO) { // Read new data
                        // from disk into
                        // buffer
                        drives[drive].incrementSector();

                        // Reset the bufferindex to 0;
                        floppyBufferIndex = 0;

                        // Recompute logical sector
                        int logicalSector = (drives[drive].cylinder
                                * drives[drive].heads * drives[drive].sectorsPerTrack)
                                + (drives[drive].hds * drives[drive].sectorsPerTrack)
                                + (drives[drive].sector - 1);

                        drives[drive].readData(logicalSector * 512, 512,
                                floppyBuffer);
                    } else { // Write data from buffer to disk
                        // Recompute logical sector
                        int logicalSector = (drives[drive].cylinder
                                * drives[drive].heads * drives[drive].sectorsPerTrack)
                                + (drives[drive].hds * drives[drive].sectorsPerTrack)
                                + (drives[drive].sector - 1);

                        drives[drive].writeData(logicalSector * 512, 512,
                                floppyBuffer);

                        drives[drive].incrementSector();

                        // Reset the bufferindex to 0;
                        floppyBufferIndex = 0;
                    }
                } catch (StorageDeviceException e) {
                    logger.log(Level.WARNING, "[" + super.getType() + "]" + " "
                            + e.getMessage());
                }
            }

        }
        // DMA transfer completed
        statusRegister0 = ((drives[drive].hds) << 2) | drive;
        statusRegister1 = 0;
        statusRegister2 = 0;

        // TODO: Is this the only place for a DREQ release?
        dma32.releaseDREQ(FDC_DMA_CHANNEL & 3);
        this.enterResultPhase();

        return startOffset;
    }

    /**
     * @param component
     */
    public void acceptComponent(HardwareComponent component)
    {
        if ((component instanceof DMAController) && component.initialised()) {
            if (((DMAController) component).isFirst()) {
                if (FDC_DMA_CHANNEL != -1) {
                    dma32 = (DMAController) component;
                    dma32.registerChannel(FDC_DMA_CHANNEL & 3, this);
                }
            }
        }
    }

}
