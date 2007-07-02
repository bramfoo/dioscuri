/*
 * $Revision: 1.1 $ $Date: 2007-07-02 14:31:41 $ $Author: blohman $
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

package nl.kbna.dioscuri.module.fdc;

import java.io.File;
import java.io.IOException;

import nl.kbna.dioscuri.exception.StorageDeviceException;

public class Drive
{
    // Attributes
    
    // Drive parameters
    private int driveType;              // Type of drive this is
    private boolean motorRunning;       // Denotes if motor of drive is on or off
    protected int eot;                  // End of track, contains the number of the final sector of a cylinder
    protected int hds;                  // Current head (Head Select)
    protected int cylinder;             // Current cylinder
    protected int sector;               // Current sector
    
    // Floppy parameters
    private Floppy floppy;              // Floppy in drive (if any, else null)
    protected int floppyType;           // Type of floppy that is in drive
    protected int tracks;               // Total number of tracks on floppy
    protected int heads;                // Total number of heads on floppy
    protected int cylinders;            // Total number of cylinders on floppy (identical to tracks for floppies)
    protected int sectorsPerTrack;      // Total number of sectors per track
    protected int sectors;              // Total number of sectors on a disk
    protected boolean writeProtected;   // Indicates if the floppy is write protected
    protected boolean multiTrack;       // The floppy has two sides, a cylinder under both HD0 and HD1 will be accessed
    
    // DIGITAL INPUT REGISTER (DIR)
    // Note: several schemes exist, but this implementation only considers bit 7 (same for all schemes)
    // PC/AT mode:
    // bit 7 = 1 diskette change/ 0=diskette is present and has not been changed
    // bit 6-0   tri-state on FDC
    // PS/2 mode:
    // bit 7   = 1 diskette change
    // bit 6-3 = 1
    // bit 2       datarate select1
    // bit 1       datarate select0
    // bit 0   = 0 high density select (500Kb/s, 1Mb/s)
    // bit 0     FIXED DISK drive 0 select (conflicting!)
    // PS/2 model 30:
    // bit 7   = 0 diskette change
    // bit 6-4 = 0
    // bit 3       -DMA gate (value from DOR register)
    // bit 2       NOPREC (value from CCR register)
    // bit 1       datarate select1
    // bit 0       datarate select0
    // bit 0     FIXED DISK drive 0 select (conflicting!)
    protected byte dir;

    
    // Constants
    // Supported floppy disk types
    private final static byte FLOPPY_DISKTYPE_NONE  = 0x00;
    private final static byte FLOPPY_DISKTYPE_360K  = 0x01;
    private final static byte FLOPPY_DISKTYPE_1_2   = 0x02;
    private final static byte FLOPPY_DISKTYPE_720K  = 0x03;
    private final static byte FLOPPY_DISKTYPE_1_44  = 0x04;
    private final static byte FLOPPY_DISKTYPE_2_88  = 0x05;
    private final static byte FLOPPY_DISKTYPE_160K  = 0x06;
    private final static byte FLOPPY_DISKTYPE_180K  = 0x07;
    private final static byte FLOPPY_DISKTYPE_320K  = 0x08;

    // Floppy geometry formats
    // Format: floppy type, tracks, heads, sectors per track, sectors, ???
    private final static int[][] FLOPPY_TYPES = new int[][] {
                {FLOPPY_DISKTYPE_NONE, 0, 0, 0, 0, 0x00},
                {FLOPPY_DISKTYPE_360K, 40, 2, 9, 720, 0x05},
                {FLOPPY_DISKTYPE_1_2,  80, 2, 15, 2400, 0x04},
                {FLOPPY_DISKTYPE_720K, 80, 2, 9, 1440, 0x1f},
                {FLOPPY_DISKTYPE_1_44, 80, 2, 18, 2880, 0x18},
                {FLOPPY_DISKTYPE_2_88, 80, 2, 36, 5760, 0x10},
                {FLOPPY_DISKTYPE_160K, 40, 1, 8, 320, 0x05},
                {FLOPPY_DISKTYPE_180K, 40, 1, 9, 360, 0x05},
                {FLOPPY_DISKTYPE_320K, 40, 2, 8, 640, 0x05}
            };
    
    
    // Constructor
    
    /**
     * Drive
     * 
     */
    public Drive()
    {
        // Initialise drive parameters
        driveType = 0;
        eot = 0;
        hds = 0;
        cylinder = 0;
        sector = 0;

        // Floppy parameters
        floppy = null;
        floppyType = 0;
        tracks = 0;
        heads = 0;
        cylinders = 0;
        sectorsPerTrack = 0;
        sectors = 0;
        writeProtected = false;
        multiTrack = false;
        
        // Initialise register
        dir = (byte) 0x80;  // only set bit 7
    }

    
    // Methods
    
    /**
     * Reset drive
     * All geometry parameters for sector selection are reset.
     * 
     */
    protected boolean reset()
    {
        // Reset registers
        dir |= 0x80;
        
        eot = 0;
        hds = 0;
        cylinder = 0;
        sector = 0;
        
        return true;
    }
    

    /**
     * Checks the existence of a floppy disk in drive
     * 
     * @return boolean true if drive contains a floppy, false otherwise
     */
    protected boolean containsFloppy()
    {
        return (floppy != null);
    }
    

    /**
     * Get type of drive
     * 
     * @return int drive type
     */
    protected int getDriveType()
    {
        return driveType;
    }
    

    /**
     * Set type of drive
     * 
     * @param byte drive type
     */
    protected void setDriveType(byte type)
    {
        driveType = type;
    }
    

    /**
     * Set motor status
     * 
     * @param boolean state of drive motor
     */
    protected void setMotor(boolean state)
    {
        motorRunning = state;
    }
    

    /**
     * Returns the state of the drive motor
     * 
     * @return boolean true if motor is running, false otherwise
     */
    protected boolean isMotorRunning()
    {
        return motorRunning;
    }
    

    /**
     * Get type of floppy inserted
     * 
     * @return int type of floppy
     */
    protected int getFloppyType()
    {
        return floppyType;
    }
    

    /**
     * Get size in bytes of floppy
     * Returns -1 if drive is empty.
     * 
     * @return int size of floppy, or -1 if no floppy available
     */
    protected int getFloppySize()
    {
        if (this.containsFloppy() == true)
        {
            return floppy.getSize();
        }
        return -1;
    }
    

    /**
     * Inserts a floppy into the drive
     * 
     * @param byte floppyType that defines the type of the floppy
     * @param File containing the disk image raw bytes of the floppy
     * @param boolean write protected
     */
    protected void insertFloppy(byte floppyType, File imageFile, boolean writeProtected) throws StorageDeviceException
    {
        try
        {
            // Create new virtual floppy
            floppy = new Floppy(floppyType, imageFile);
            
            // Set geometry for drive
            this.floppyType   = floppyType;

            // Support for different floppy sizes
            if (floppy.getSize() <= 1474560)
            {
                tracks            = FLOPPY_TYPES[floppyType][1];
                heads             = FLOPPY_TYPES[floppyType][2];
                sectorsPerTrack   = FLOPPY_TYPES[floppyType][3];
            }
            else if (floppy.getSize() == 1720320)
            {
                tracks            = 80;
                heads             = 2;
                sectorsPerTrack   = 21;
            }
            else if (floppy.getSize() == 1763328)
            {
                tracks            = 82;
                heads             = 2;
                sectorsPerTrack   = 21;
            }
            else if (floppy.getSize() == 1884160)
            {
                tracks            = 80;
                heads             = 2;
                sectorsPerTrack   = 23;
            }
            
            // Define total number of sectors on floppy
            sectors = heads * tracks * sectorsPerTrack;
            
            if (floppy.getSize() > (sectors * 512))
            {
                throw new StorageDeviceException("Error: size of file too large for selected type");
            }
            
            // Check if floppy should be write protected
            this.writeProtected = writeProtected;
        }
        catch (IOException e)
        {
            throw new StorageDeviceException("Floppy could not be inserted.");
        }
    }

    
    /**
     * Ejects a floppy from the drive
     * 
     */
    protected void ejectFloppy() throws StorageDeviceException
    {
        try
        {
            // Store all data from floppy to image
            floppy.storeImageToFile();
        }
        catch (IOException e)
        {
            throw new StorageDeviceException("Floppy data could not be stored to disk image. Data may be lost.");
        }
        finally
        {
            // Eject floppy and reset all drive parameters
            floppy = null;
            driveType = 0;
            floppyType = 0;
            tracks = 0;
            heads = 0;
            sectorsPerTrack = 0;
            sectors = 0;
            writeProtected = false;
            
            this.resetChangeline();
        }
    }
    
    
    /**
     * Read data from floppy into buffer
     * 
     * @param int offset from beginning of floppy
     * @param int totalBytes denoting the number of bytes to read
     * @param byte[] floppyBuffer the buffer to store read data in
     */
    protected void readData(int offset, int totalBytes, byte[] floppyBuffer) throws StorageDeviceException
    {
        // Copy bytes from floppy to buffer
        if (this.containsFloppy())
        {
            System.arraycopy(floppy.bytes, offset, floppyBuffer, 0, totalBytes);
            
            // TODO: Increment sector can also taken care of here instead of in FDC (may be better in OO-terms)
        }
        else
        {
            throw new StorageDeviceException("Error: drive does not contain a floppy");
        }
    }

    
    /**
     * Write data to floppy from buffer
     * 
     * @param int offset from beginning of floppy
     * @param int totalBytes denoting the number of bytes to write
     * @param byte[] floppyBuffer containing the data to write
     */
    protected void writeData(int offset, int totalBytes, byte[] floppyBuffer) throws StorageDeviceException
    {
        // Copy bytes from buffer to floppy
        if (this.containsFloppy() && writeProtected == false)
        {
            System.arraycopy(floppyBuffer, 0, floppy.bytes, offset, totalBytes);
            
            // TODO: Increment sector can also taken care of here instead of in FDC (may be better in OO-terms)
        }
        else
        {
            throw new StorageDeviceException("Error: drive does not contain a floppy or is write protected");
        }
    }
    
    
    /**
     * Increment current sector
     * Note: also takes care of multitrack disks and cylinder position
     * 
     */
    protected void incrementSector()
    {
        // Sector will be updated after data transfer is ready
        sector++;
        if ((sector > eot) || (sector > sectorsPerTrack))
        {
            // Reset sector and go to next cylinder/track
            sector = 1;
            if (multiTrack == true)
            {
                // Double sided
                hds++;
                
                if (hds > 1)
                {
                    // Go back to first side and increment cylinder
                    hds = 0;
                    cylinder++;
                    this.resetChangeline();
                }
            }
            else
            {
                // Single sided, increment cylinder
                cylinder++;
                this.resetChangeline();
            }
            
            // Check if 
            if (cylinder >= tracks)
            {
                // Cylinder may not exceed total number of tracks
                cylinder = tracks;
            }
        }
    }
    
    
    /**
     * Reset change line
     * Updates DIR on bit 7
     * 
     */
    protected void resetChangeline()
    {
        if (this.containsFloppy())
        {
            // Swap b7 of DIR value:
            // b7: 0=diskette is present and has not been changed
            //     1=diskette missing or changed
            dir = (byte)(dir & ~0x80);
        }
    }

    
    /**
     * Get String representation of this class
     * 
     */
    public String toString()
    {
        String driveInfo, floppyInfo, ret, tab;
        
        ret = "\r\n";
        tab = "\t";
        
        // Dump drive info
        driveInfo =  "drivetype=" + driveType +
                     ", motorRunning=" + motorRunning +
                     ", eot=" + eot +
                     ", hds=" + hds +
                     ", cylinder=" + cylinder + 
                     ", sector=" + sector; 
        
        // Dump floppy info
        floppyInfo = "floppy=";
        if (this.containsFloppy() == true)
        {
            floppyInfo += "inserted" + 
                          ", floppytype=" + floppyType + 
                          ", floppysize=" + floppy.getSize() + 
                          ", tracks=" + tracks + 
                          ", heads=" + heads + 
                          ", cylinders=" + cylinders + 
                          ", sectorsPerTrack=" + sectorsPerTrack +
                          ", sectors=" + sectors +
                          ", writeProtected=" + writeProtected + 
                          ", multiTrack=" + multiTrack;
        }
        else
        {
            floppyInfo += "none";
        }
        
        return driveInfo + ret + tab + tab + floppyInfo;
    }
}
