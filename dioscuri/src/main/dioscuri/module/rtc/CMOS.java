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
package dioscuri.module.rtc;

import java.util.Arrays;
import java.util.Calendar;

/**
 * Implementation of the CMOS memory
 * 
 * Notes: - register information is taken from CMOS Reference by Padgett
 * Peterson, 2001. See: http://moon.inf.uji.es/docs/interr/CMOS/CMOS.HTM
 * 
 */
public class CMOS {
    // CMOS memory
    protected byte[] ram; // CMOS memory module

    // CMOS time
    Calendar calendar;

    // Constants
    // CMOS size and I/O ports
    private final static int CMOS_SIZE = 128; // Upper limit of memory size
                                              // (only bits 0-6 can be used)

    // Variables and hex locations in CMOS memory
    protected final static int RTC_SECONDS = 0x00; // BCD/hex format
    protected final static int RTC_SECOND_ALARM = 0x01; // BCD/hex format
    protected final static int RTC_MINUTES = 0x02; // BCD/hex format
    protected final static int RTC_MINUTE_ALARM = 0x03; // BCD/hex format
    protected final static int RTC_HOURS = 0x04; // BCD/hex format
    protected final static int RTC_HOUR_ALARM = 0x05; // BCD/hex format
    protected final static int RTC_DAYOFWEEK = 0x06; // BCD/hex format
    protected final static int RTC_DATEOFMONTH = 0x07; // BCD/hex format
    protected final static int RTC_MONTH = 0x08; // BCD/hex format
    protected final static int RTC_YEAR = 0x09; // BCD/hex format

    // STATUS REGISTER A (read/write)
    // Bitfields for Real-Time Clock status register A:
    // Bit(s) Description
    // 7 =1 time update cycle in progress, data ouputs undefined
    // (bit 7 is read only)
    // 6-4 22 stage divider
    // 010 = 32768 Hz time base (default)
    // 3-0 rate selection bits for interrupt
    // 0000 none
    // 0011 122 microseconds (minimum)
    // 1111 500 milliseconds
    // 0110 976.562 microseconds (default 1024 Hz)
    protected final static int STATUS_REGISTER_A = 0x0A; // read/write

    // STATUS REGISTER B (read/write)
    // Bitfields for Real-Time Clock status register B:
    // Bit(s) Description
    // 7 enable cycle update
    // 6 enable periodic interrupt
    // 5 enable alarm interrupt
    // 4 enable update-ended interrupt
    // 3 enable square wave output
    // 2 Data Mode - 0: BCD, 1: Binary
    // 1 24/12 hour selection - 1 enables 24 hour mode
    // 0 Daylight Savings Enable - 1 enables
    protected final static int STATUS_REGISTER_B = 0x0B; // read/write

    // STATUS REGISTER C (Read only)
    // Bitfields for Real-Time Clock status register C:
    // Bit(s) Description
    // 7 Interrupt request flag
    // =1 when any or all of bits 6-4 are 1 and appropriate enables
    // (Register B) are set to 1. Generates IRQ 8 when triggered.
    // 6 Periodic Interrupt flag
    // 5 Alarm Interrupt flag
    // 4 Update-Ended Interrupt Flag
    // 3-0 unused???
    protected final static int STATUS_REGISTER_C = 0x0C; // read only

    // STATUS REGISTER D (read only)
    // Bitfields for Real-Time Clock status register D:
    // Bit(s) Description
    // 7 Valid RAM - 1 indicates batery power good, 0 if dead or disconnected.
    // 6-0 unused??? (0)
    protected final static int STATUS_REGISTER_D = 0x0D; // read only

    // CMOS REGISTER EXTENSIONS
    // IBM - RESET CODE (IBM PS/2 "Shutdown Status Byte")
    // Values for Reset Code / Shutdown Status Byte:
    // 00h-03h perform power-on reset
    // 00h software reset or unexpected reset
    // 01h reset after memory size check in real/virtual mode
    // (or: chip set initialization for real mode reentry)
    // 02h reset after successful memory test in real/virtual mode
    // 03h reset after failed memory test in real/virtual mode
    // 04h INT 19h reboot
    // 05h flush keyboard (issue EOI) and jump via 40h:0067h
    // 06h reset (after successful test in virtual mode)
    // (or: jump via 40h:0067h without EOI)
    // 07h reset (after failed test in virtual mode)
    // 08h used by POST during protected-mode RAM test (return to POST)
    // 09h used for INT 15/87h (block move) support
    // 0Ah resume execution by jump via 40h:0067h
    // 0Bh resume execution via IRET via 40h:0067h
    // 0Ch resume execution via RETF via 40h:0067h
    // 0Dh-FFh perform power-on reset
    protected final static int SHUTDOWN_STATUS = 0x0F; // read/write

    // Bitfields for floppy drives A/B types:
    // Bit(s) Description
    // 7-4 first floppy disk drive type
    // 3-0 second floppy disk drive type
    // Values for floppy drive type:
    // 00h no drive
    // 01h 360 KB 5.25 Drive
    // 02h 1.2 MB 5.25 Drive
    // 03h 720 KB 3.5 Drive
    // 04h 1.44 MB 3.5 Drive
    // 05h 2.88 MB 3.5 drive
    // 06h-0Fh unused
    protected final static int FLOPPYDRIVE_TYPE = 0x10; // read/write : defines
                                                        // the type of floppy
                                                        // drives

    protected final static int IBM_PS2_HD1_DATA = 0x11; // read/write : defines
                                                        // the type of first
                                                        // PS/2 hard disk drive

    // Bitfields for IBM hard disk data:
    // Bit(s) Description
    // 7-4 First Hard Disk Drive
    // 00 No drive
    // 01-0Eh Hard drive Type 1-14
    // 0Fh Hard Disk Type 16-255
    // (actual Hard Drive Type is in CMOS RAM 19h)
    // 3-0 Second Hard Disk Drive Type
    // (same as first except extended type will be found in 1Ah).
    protected final static int IBM_HD_DATA = 0x12; // read/write : defines the
                                                   // type of hard disk drives

    protected final static int IBM_PS2_HD2_DATA = 0x13; // read/write : defines
                                                        // the type of second
                                                        // PS/2 hard disk drive

    // Bitfields for IBM equipment byte:
    // Bit(s) Description
    // 7-6 number of floppy drives (system must have at least one)
    // 00b 1 Drive
    // 01b 2 Drives
    // 10b ??? 3 Drives
    // 11b ??? 4 Drives
    // 5-4 monitor type
    // 00b Not CGA or MDA (observed for EGA & VGA)
    // 01b 40x25 CGA
    // 10b 80x25 CGA
    // 11b MDA (Monochrome)
    // 3 display enabled (turned off to enable boot of rackmount)
    // 2 keyboard enabled (turn off to enable boot of rackmount)
    // 1 math coprocessor installed
    // 0 floppy drive installed (turned off for rackmount boot)
    protected final static int IBM_EQUIPMENT = 0x14; // read/write : unused,
                                                     // defines number of floppy
                                                     // drives, monitor type,
                                                     // etc.

    protected final static int IBM_BASE_MEM_LOW = 0x15; // read/write : unused
    protected final static int IBM_BASE_MEM_HIGH = 0x16; // read/write : unused
    protected final static int IBM_EXTEND_MEM_LOW = 0x17; // read/write : unused
    protected final static int IBM_EXTEND_MEM_HIGH = 0x18; // read/write :
                                                           // unused
    protected final static int IBM_EXTEND_HD1 = 0x19; // read/write : unused
    protected final static int IBM_EXTEND_HD2 = 0x1A; // read/write : unused
    // .. whole block of unused registers (0x1B - 0x2C)
    protected final static int AWARD_HD1_USERDEF = 0x2D; // read/write : defines
                                                         // the boot order
                                                         // (floppy,hd)
    protected final static int CHECKSUM_HIGH_BYTE = 0x2E; // read/write : unused
    protected final static int CHECKSUM_LOW_BYTE = 0x2F; // read/write : unused
    protected final static int IBM_EXTEND_MEM2_LOW = 0x30; // read/write :
    protected final static int IBM_EXTEND_MEM2_HIGH = 0x31; // read/write :
    protected final static int IBM_RTC_CENTURY = 0x32; // read/write : defines
                                                       // the century in BCD
    protected final static int AMI_EXTEND_MEM_LOW = 0x34; // read/write :
    protected final static int AMI_EXTEND_MEM_HIGH = 0x35; // read/write :
    protected final static int IBM_PS2_RTC_CENTURY = 0x37; // read/write : copy
                                                           // of 0x32, used for
                                                           // Windows XP
    protected final static int ELTORITO_BOOT = 0x38; // read/write : eltorito
                                                     // boot sequence + boot
                                                     // signature check
    protected final static int ATA_POLICY_0_1 = 0x39; // read/write : ata
                                                      // translation policy -
                                                      // ata0 + ata1
    protected final static int ATA_POLICY_2_3 = 0x3A; // read/write : ata
                                                      // translation policy -
                                                      // ata2 + ata3
    protected final static int ELTORITO_BOOT2 = 0x3D; // read/write : eltorito
                                                      // boot sequence 2 + boot
                                                      // signature check

    /**
     * Class constructor
     * 
     */
    public CMOS() {
        // Create RAM array
        ram = new byte[CMOS_SIZE];

        // Initialise CMOS memory with default values
        Arrays.fill(ram, (byte) 0x00);

        // Set Status Register A to defaults:
        // 0 - no time update cycle in progress
        // 010 - 22 stage divider set at 32768 Hz time base (default)
        // 0110 - interrupt rate selection: 976.562 microseconds (default 1024
        // Hz)
        ram[STATUS_REGISTER_A] = (byte) 0x26;

        // Set Status Register B to defaults:
        // 0 - cycle update (disable)
        // 0 - disable periodic interrupt (disable)
        // 0 - disable alarm interrupt (disable)
        // 0 - disable update-ended interrupt (disable)
        // 0 - disable square wave output (disable)
        // 0 - BCD data mode (not binary)
        // 1 - 24 hour mode (not 12 hour)
        // 0 - daylight savings (disable)
        ram[STATUS_REGISTER_B] = (byte) 0x02;

        // Set Status Register C to defaults:
        // 0 - Interrupt request flag
        // 0 - Periodic Interrupt flag
        // 0 - Alarm Interrupt flag
        // 0 - Update-Ended Interrupt Flag
        // 000 - unused???
        ram[STATUS_REGISTER_C] = (byte) 0x00;

        // Set Status Register D to defaults:
        // 1 - Valid RAM (battery power good)
        // 000 0000 - unused???
        ram[STATUS_REGISTER_D] = (byte) 0x80;

        // Clear calendar
        calendar = Calendar.getInstance();
    }

    protected void reset(boolean systemTime) {
        // Check if custom time should be used; system time has already been set
        // as default.
        if (!systemTime) {
            // TODO: Use user-defined time and date (via configuration)
            // Custom date: Fri May 05 1995 00:00:05 (Unix ticks: 799624805)
            // calendar.set(1995,4,5,0,0,5);

            // Custom date: Fri May 05 1995 11:59:59 (Unix ticks: 799667999)
            calendar.set(1995, 4, 5, 11, 59, 59);
        }

        ram[RTC_SECONDS] = decToBcd(calendar.get(Calendar.SECOND));
        ram[RTC_MINUTES] = decToBcd(calendar.get(Calendar.MINUTE));
        ram[RTC_HOURS] = decToBcd(calendar.get(Calendar.HOUR_OF_DAY));
        ram[RTC_DAYOFWEEK] = (byte) calendar.get(Calendar.DAY_OF_WEEK);
        ram[RTC_DATEOFMONTH] = decToBcd(calendar.get(Calendar.DAY_OF_MONTH));
        // Calender.MONTH is zero-based, so add 1 for proper value
        ram[RTC_MONTH] = (byte) (decToBcd(calendar.get(Calendar.MONTH)) + 1);
        // Strip off leading 2 digits of year (introducing Y2K bug)
        ram[RTC_YEAR] = decToBcd(calendar.get(Calendar.YEAR));
        ram[IBM_RTC_CENTURY] = decToBcd((int) (calendar.get(Calendar.YEAR) / 100));
        ram[IBM_PS2_RTC_CENTURY] = ram[IBM_RTC_CENTURY];

        // Return the following values to default:
        // Status register B: set bits 6, 5, 4 to 0
        // Status register C: set bits 7, 6, 5, 4 to 0
        ram[STATUS_REGISTER_B] &= 0x8F;
        ram[STATUS_REGISTER_C] &= 0x0F;

        // Set checksum bits of CMOS
        checksum();
    }

    /**
     * Performs a byte-wise additive checksum of bytes of the values in
     * locations 0x10-0x2D.<BR>
     * Bytes 0x00-0x0F and 0x30-0x33 are omitted.<BR>
     * Stores the result in CHECKSUM_HIGH_BYTE and CHECKSUM_LOW_BYTE
     */
    private void checksum() {
        int checksum = 0;
        for (int reg = 0x10; reg <= 0x2D; reg++) {
            checksum += ram[reg];
        }

        ram[CHECKSUM_LOW_BYTE] = (byte) (checksum & 0xFF);
        ram[CHECKSUM_HIGH_BYTE] = (byte) ((checksum >> 8) & 0xFF);
    }

    /**
     * Performs a conversion of decimal value into Binary Code Decimal (BCD).
     * Note: results only in a one-byte-value. Large unsigned integers (> 255)
     * will be cut off. Example of conversion: int 22 will result in 0x22 (or
     * 0010.0010).
     * 
     * @param decimalValue
     * @return byte containing the BCD value of decimal
     */
    protected byte decToBcd(int decimalValue) {
        return (byte) ((16 * ((decimalValue / 10) % 10)) + (decimalValue % 10));
    }

    /**
     * Performs a conversion of bcd value into decimal. Example of conversion:
     * 0x22 (or 0010.0010) will result in int 22.
     * 
     * @param bcdValue
     * @return int containing the decimal value of bcd
     */
    protected int bcdToDec(byte bcdValue) {
        // Convert BCD-byte into decimal value
        return (((bcdValue >> 4) & 0x0F) * 10) + (bcdValue & 0x0F);
    }

    /**
     * Returns string of decimal representation of time and date
     * 
     * @return String
     */
    protected String getClockValue() {
        return "" + bcdToDec(ram[CMOS.RTC_HOURS]) + ":"
                + bcdToDec(ram[CMOS.RTC_MINUTES]) + ":"
                + bcdToDec(ram[CMOS.RTC_SECONDS]) + " "
                + bcdToDec(ram[CMOS.RTC_DATEOFMONTH]) + "-"
                + bcdToDec(ram[CMOS.RTC_MONTH]) + "-"
                + bcdToDec(ram[CMOS.RTC_YEAR]);
    }

    /**
     * Updates the clock values
     * 
     * @param seconds
     *            Number of seconds to update the clock
     */
    protected void setClockValue(int seconds) {
        calendar.add(Calendar.SECOND, seconds);

        // Write values to RAM array
        ram[RTC_SECONDS] = decToBcd(calendar.get(Calendar.SECOND));
        ram[RTC_MINUTES] = decToBcd(calendar.get(Calendar.MINUTE));
        ram[RTC_HOURS] = decToBcd(calendar.get(Calendar.HOUR_OF_DAY));
        ram[RTC_DAYOFWEEK] = (byte) calendar.get(Calendar.DAY_OF_WEEK);
        ram[RTC_DATEOFMONTH] = decToBcd(calendar.get(Calendar.DAY_OF_MONTH));
        // Calender.MONTH is zero-based, so add 1 for proper value
        ram[RTC_MONTH] = (byte) (decToBcd(calendar.get(Calendar.MONTH)) + 1);
        // Strip off leading 2 digits of year (introducing Y2K bug)
        ram[RTC_YEAR] = decToBcd(calendar.get(Calendar.YEAR));
        ram[IBM_RTC_CENTURY] = decToBcd((int) (calendar.get(Calendar.YEAR) / 100));
        ram[IBM_PS2_RTC_CENTURY] = ram[IBM_RTC_CENTURY];
    }
}
