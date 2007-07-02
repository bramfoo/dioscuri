/*
 * $Revision: 1.1 $ $Date: 2007-07-02 14:31:28 $ $Author: blohman $
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

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import nl.kbna.dioscuri.exception.StorageDeviceException;

/**
 * A class representing a ATA drive and parameters.
 */
public class ATADrive
{

    // Attributes

    // Drive parameters
    protected ATADriveType driveType; // Type of drive
    
    //  512 byte buffer for ID drive command
    // These words are stored in native word endian format, as
    // they are fetched and returned via a return(), so
    // there's no need to keep them in x86 endian format.
//    protected Bit16u id_drive[256]; //TODO
    protected int[] idDrive = new int[256];
    
    protected char[] modelNo = new char[40]; //TODO: originally 41 - but last char not used 

    
    //TODO:in lba mode the values in the below are not local
    protected int currentHead; // Current head, Head Select
    protected int currentCylinder; // Current cylinder
    protected int currentSector; // Current sector
    
    protected int totalNumHeads; // Total number of heads on disk
    protected int totalNumCylinders; // Total number of cylinders on disk (identical to tracks for floppies)
    protected int totalNumSectors; // Total number of sectors on a disk
    
    protected int sectorCount; // TODO: should this be moved?
    
    // TODO: implement this and multi-drives
    protected boolean isMaster; // Is the drive the master?

    // Disk parameters
    private DiskImage disk; // Disk in drive (if any, else null)
    protected boolean containsDisk;

    protected boolean isWriteProtected; // Indicates if the floppy is write protected
    
    protected int ioLightCounter;   
    protected int statusbarId;
    protected int features; 
    
    private ATADriveController control;
    
    private SenseInfo senseInfo;
    
    private CDROM cdRom;
    
    protected Atpi atpi;
    
    protected ATATranslationType translationType;
    
    protected int deviceNum; // for ATAPI identify & inquiry
    
    /**
     * Class Constructor with input parameter IDE drive type.
     * 
     * @param the IDE drive type
     * @param enabled
     */
    public ATADrive(ATADriveType theDriveType, ATA parent, boolean enabled)
    {
        
        this.control = new ATADriveController(parent); 
        this.senseInfo = new SenseInfo();
        this.atpi = new Atpi();
        
        // Initialise drive parameters
        this.driveType = theDriveType;
        
        this.currentHead     = 0;
        this.sectorCount     = 1;
        this.currentSector   = 1;
        this.currentCylinder = 0;       
        this.features        = 0;
        
        // Drive parameters
        this.disk = null;
        this.totalNumHeads = 0;
        this.totalNumCylinders = 0;
        this.totalNumSectors = 0;
        this.isWriteProtected = false;
        
        this.statusbarId = -1;
        this.ioLightCounter = 0;
        
        this.modelNo = ATAConstants.IDE_MODEL.toCharArray();
                                  
    }
    
    
    public ATADrive(ATADriveType theDriveType, ATA parent, boolean enabled, int cdromCount)
    {
        this(theDriveType, parent, enabled);
        
        if(theDriveType == ATADriveType.CDROM)
        {
            
         //   BX_DEBUG(( "CDROM on target %d/%d",channel,device));
            
         //   logger.log(Level.INFO, this.getType() + " -> "CDROM on target, channel .");
            
            this.cdRom = new CDROM();
                    
            this.cdRom.setLocked(false);
            this.senseInfo.setSenseKey(SenseType.NONE);
            this.senseInfo.setAsc(AscType.NOT_SET);
            
            this.senseInfo.setAscq((byte)0);
            
            cdromCount++;
            this.deviceNum = cdromCount + 48;

            // Check bit fields
//TODO: understandand implement
 /*           BX_CONTROLLER(channel,device).sector_count = 0;
            BX_CONTROLLER(channel,device).interrupt_reason.c_d = 1;
            if (BX_CONTROLLER(channel,device).sector_count != 0x01)
            {
                  BX_PANIC(("interrupt reason bit field error"));
            }

            BX_CONTROLLER(channel,device).sector_count = 0;
            BX_CONTROLLER(channel,device).interrupt_reason.i_o = 1;
            if (BX_CONTROLLER(channel,device).sector_count != 0x02)
            {
                  BX_PANIC(("interrupt reason bit field error"));
            }

            BX_CONTROLLER(channel,device).sector_count = 0;
            BX_CONTROLLER(channel,device).interrupt_reason.rel = 1;
            if (BX_CONTROLLER(channel,device).sector_count != 0x04)
            {
                  BX_PANIC(("interrupt reason bit field error"));
            }
    
            BX_CONTROLLER(channel,device).sector_count = 0;
            BX_CONTROLLER(channel,device).interrupt_reason.tag = 3;
            if (BX_CONTROLLER(channel,device).sector_count != 0x18)
            {
                  BX_PANIC(("interrupt reason bit field error"));
            }
*/     
            this.sectorCount = 0;
        
        }   
              
    }
    
    // Methods

    public boolean calculateLogicalAddress(int sector)
    {
        int logicalSector;

        if (this.getControl().getLbaMode() > 0)
        {
            logicalSector = (this.getCurrentHead()) << 24 | (this.getCurrentCylinder()) << 8
                    | this.getCurrentSector();
        }
        else
        {
            logicalSector = (this.getCurrentCylinder() * this.getTotalNumHeads() * this.getTotalNumSectors())
                    + (this.getCurrentHead() * this.getTotalNumSectors())
                    + (this.getCurrentSector() - 1);
        }

        // TODO: use total C H S?
        int sectorCount = this.getTotalNumCylinders() * this.getTotalNumHeads()
                * this.getTotalNumSectors();

        if (logicalSector >= sectorCount)
        {
            // BX_ERROR (("calc_log_addr: out of bounds (%d/%d)", (Bit32u)logical_sector, sector_count));
            return false;
        }

        sector = logicalSector;

        return true;
    }
    
    public int calculateLogicalAddress()
    {
        int logicalSector;

        if (this.getControl().getLbaMode() > 0)
        {
            logicalSector = (this.getCurrentHead()) << 24 | (this.getCurrentCylinder()) << 8
                    | this.getCurrentSector();
        }
        else
        {
            logicalSector = (this.getCurrentCylinder() * this.getTotalNumHeads() * this.getTotalNumSectors())
                    + (this.getCurrentHead() * this.getTotalNumSectors())
                    + (this.getCurrentSector() - 1);
        }

        // TODO: use total C H S?
        int sectorCount = this.getTotalNumCylinders() * this.getTotalNumHeads()
                * this.getTotalNumSectors();

        if (logicalSector >= sectorCount)
        {
            // BX_ERROR (("calc_log_addr: out of bounds (%d/%d)", (Bit32u)logical_sector, sector_count));
            return -1;
        }

        return logicalSector;
    }

    public void incrementAddress()
    {

        this.decrementSectorCount();

        if (this.getControl().getLbaMode() > 0)
        {
            Integer currentAddressObj = new Integer(0);
            calculateLogicalAddress(currentAddressObj);
            
            int currentAddress = currentAddressObj.intValue();
            currentAddress++;

            int newCurrentHead = (currentAddress >> 24) & 0xf;
            this.setCurrentHead(newCurrentHead);
            int newCurrentCylinder = (currentAddress >> 8) & 0xffff;
            this.setCurrentCylinder(newCurrentCylinder);
            int newCurrentSector = (currentAddress) & 0xff;
            this.setCurrentSector(newCurrentSector);

        }
        else
        {

            int newCurrentSector = this.getCurrentSector();
            newCurrentSector++;
            this.setCurrentSector(newCurrentSector);

            if (this.getCurrentSector() > this.getTotalNumSectors())
            {

                this.setCurrentSector(1);

                int newCurrentHead = this.getCurrentHead();
                newCurrentHead++;
                this.setCurrentHead(newCurrentHead);

                if (this.getCurrentHead() >= this.getTotalNumHeads())
                {

                    this.setCurrentHead(0);

                    int newCurrentCylinder = this.getCurrentCylinder();
                    newCurrentCylinder++;
                    this.setCurrentCylinder(newCurrentCylinder);

                    if (this.getCurrentCylinder() >= this.getTotalNumCylinders())
                    {
                        newCurrentCylinder = this.getTotalNumCylinders();
                        newCurrentCylinder--;
                        this.setCurrentCylinder(newCurrentCylinder);

                    }
                }
            }
        }
    }

    /**
     * Reset drive All geometry parameters for sector selection are reset.
     */
    protected boolean reset()
    {
        
        this.control.reset();
        
        // Reset params
        currentHead = 0;
        currentCylinder = 0;
        currentSector = 0;
        
        return true;
    }
    
    /**
     * Get the control.
     * 
     * @return the control
     */
    public ATADriveController getControl()
    {

        return this.control;
    }
    
    
    public SenseInfo getSenseInfo()
    {
        return this.senseInfo; 
    }

    /**
     * Checks the existence of a disk in drive
     * 
     * @return boolean true if drive contains a disk, false otherwise
     */
    protected boolean containsDisk()
    {
        return (disk != null);
    }

    /**
     * Get type of drive
     * 
     * @return int drive type
     */
    protected ATADriveType getDriveType()
    {
        return driveType;
    }

    /**
     * Sets type of drive.
     * 
     * @param byte drive type
     */
    protected void setDriveType(ATADriveType type)
    {
        driveType = type;

    }

    /**
     * Get the total capacity of the disk in bytes.
     * 
     * @return the total capacity of the disk in bytes.
     */
    //TODO: confirm use of int - long
    protected long getDiskCapacity()
    {

        // the total HD size is given by
        // Bit64u disk_size = (Bit64u)cyl * heads * spt * 512;

        int totalDiskCapacity = totalNumCylinders * totalNumHeads * totalNumSectors * control.getNumSectorsPerBlock();

        return totalDiskCapacity;

    }

    /**
     * Set the disk capacity, by input of the number of cylinders, 
     * the number of heads, 
     * the number of sectors and the number of sectors per block.
     * 
     * @param theTotalNumCylinders   the total number of cylinders
     * @param theTotalNumHeads       the total number of heads
     * @param theTotalNumSectors     the total number of sectors
     * @param theNumSectorsPerBlock  the number of sectors per block 
     */
    public void setDiskCapacity(int theTotalNumCylinders, int theTotalNumHeads, int theTotalNumSectors, int theNumSectorsPerBlock)
    {
        totalNumCylinders = theTotalNumCylinders;
        totalNumHeads = theTotalNumHeads;
        totalNumSectors = theTotalNumSectors;
        
        control.setNumSectorsPerBlock(theNumSectorsPerBlock);
        
    }

    /**
     * Get size in bytes of the disk image. Returns -1 if drive is empty.
     * 
     * @return int size of disk image, or -1 if no disk image available
     */
    protected long getDiskImageSize()
    {
        if (this.containsDisk() == true)
        {
            return disk.getSize();
        }
        return -1;
    }

    /**
     * Sets if the disk is a master (true) or a slave (false).
     * 
     * @param isMaster  true for master, false for slave
     */
    public void setIsMaster(boolean isMaster)
    {
        this.isMaster = isMaster;
    }

    /**
     * Set if the disk is write protected.
     * 
     * @param isProtected   true for write protected, false for not writed protected
     */
    public void setWriteProtected(boolean isProtected)
    {
        this.isWriteProtected = isProtected;
    }

    /**
     * Reads Data from disk image.
     * 
     * @param offset      the offset
     * @param totalBytes  the total number of bytes
     * @param diskBuffer  the disk buffer
     * @throws StorageDeviceException
     */
    public byte[] readData(byte[] diskBuffer, int offset, int totalBytes) throws IOException
    {
        byte[] dataRead; 
        // Copy bytes from floppy to buffer
        if (this.containsDisk())
        {
            dataRead = disk.readFromImage(diskBuffer, offset, totalBytes);

            //TODO: check - sector count to update?
//            currentSector = currentSector + (totalBytes / 512);
            
        } else
        {
            throw new IOException("Error: drive does not contain a disk");
        }
        
        return dataRead;
    }
    
    /**
     * Writes data to the disk image.
     * 
     * @param offset        the offset
     * @param totalBytes    the total number of bytes
     * @param diskBuffer    the disk buffer
     * @throws StorageDeviceException
     */
    public void writeData(byte[] diskBuffer, int offset, int totalBytes) throws IOException
    {
        // Copy bytes from buffer to floppy
        if (this.containsDisk()&& isWriteProtected == false)
        {
            disk.writeToImage(diskBuffer, offset, totalBytes);

//          TODO: check - sector count to update?
//            currentSector = currentSector + (totalBytes / 512);
            
        } else
        {
            throw new IOException("Error: drive does not contain a disk or is write protected");
        }
    }
    
    /**
     * Load disk image.
     * 
     * @param imageFile  The disk image file
     * @throws StorageDeviceException
     */
    public void loadImage(File imageFile) throws StorageDeviceException
    {
        try
        {
            // Create new virtual floppy
            disk = new DiskImage(imageFile);

        }
        catch (IOException e)
        {
            throw new StorageDeviceException("Disk could not be loaded.");
        }
    }

    /**
     * Provides the string representation of the class.
     * 
     * @return  the string representation of the class
     */
    public String toString()
    {
        return "Drive status info:" + "  drivetype=" + driveType.toString() + ", diskPresent=" + this.containsDisk() + ", writeProtected="
                + this.isWriteProtected + ", currentHead=" + currentHead + ", currentCylinder=" + currentCylinder + ", currentSector=" + currentSector
                + ", numberHeads=" + totalNumHeads + ", numberCylinders=" + totalNumCylinders + ", numberSectors="
                + totalNumSectors;
    }

    public boolean isMaster()
    {
        return isMaster;
    }

    public void setMaster(boolean isMaster)
    {
        this.isMaster = isMaster;
    }

    public int getCurrentHead()
    {
        return currentHead;
    }

    public void setCurrentHead(int currentHead)
    {
        this.currentHead = currentHead;
    }

    public int getCurrentCylinder()
    {
        return currentCylinder;
    }

    public void setCurrentCylinder(int currentCylinder)
    {
        this.currentCylinder = currentCylinder;
    }

    public int getCurrentSector()
    {
        return currentSector;
    }

    public void setCurrentSector(int currentSector)
    {
        this.currentSector = currentSector;
    }

    public int getTotalNumSectors()
    {
        return totalNumSectors;
    }

    public Atpi getAtpi()
    {
        return atpi;
    }

    public void setAtpi(Atpi atpi)
    {
        this.atpi = atpi;
    }

    public int getIoLightCounter()
    {
        return ioLightCounter;
    }

    public void setIoLightCounter(int ioLightCounter)
    {
        this.ioLightCounter = ioLightCounter;
    }

    public int getTotalNumHeads()
    {
        return totalNumHeads;
    }

    public void setTotalNumHeads(int totalNumHeads)
    {
        this.totalNumHeads = totalNumHeads;
    }

    public int getTotalNumCylinders()
    {
        return totalNumCylinders;
    }

    public void setTotalNumCylinders(int totalNumCylinders)
    {
        this.totalNumCylinders = totalNumCylinders;
    }
     
    public void incrementSectorCount()
    {
        this.sectorCount++;
    }
    
    public void decrementSectorCount()
    {
        this.sectorCount--;
    }

    public int getSectorCount()
    {
        return sectorCount;
    }

    public void setSectorCount(int sectorCount)
    {
        this.sectorCount = sectorCount;
    }
    public int getFeatures()
    {
        return features;
    }

    public void setFeatures(int features)
    {
        this.features = features;
    }

    public int[] getIdDrive()
    {
        return idDrive;
    }

    
    public void setIdDrive(int index, int idDrive)
    {
        this.idDrive[index] = idDrive;
    }

    public char[] getModelNo()
    {
        return modelNo;
    }

    public CDROM getCdRom()
    {
        return cdRom;
    }

    public void setCdRom(CDROM cdRom)
    {
        this.cdRom = cdRom;
    }

    public ATATranslationType getTranslationType()
    {
        return translationType;
    }

    public void setTranslationType(ATATranslationType translationType)
    {
        this.translationType = translationType;
    }
}
