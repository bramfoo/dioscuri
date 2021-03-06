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
 * Class representing the ATA drive controller.
 */
public class ATADriveController {
    private byte[] buffer = new byte[ATAConstants.MAX_MULTIPLE_SECTORS * 512 + 4]; // TODO:
    // Where
    // does
    // 2352
    // come
    // from?

    private int bufferSize;
    private int bufferIndex;
    private int lbaMode;
    private int errorRegister;
    protected int numSectorsPerBlock; // Total number of sectors per block

    private ATAStatus status;

    private int currentCommand;
    private int drqIndex;
    private InterruptReason interruptReason;

    private int byteCount;

    private boolean reset; // 0=normal, 1=reset controller
    private boolean disableIrq;
    private int resetInProgress;

    private int packetDma;
    public ATA parent;
    public int multipleSectors = 0;

    /**
     * Copnstructor.
     *
     * @param theParent
     */
    public ATADriveController(ATA theParent) {

        this.parent = theParent;

        errorRegister = 0x01; // diagnostic code: no error

        currentCommand = 0x00;
        bufferIndex = 0;
        reset = false;
        disableIrq = false;
        resetInProgress = 0;
        lbaMode = 0;

        multipleSectors = 0;

        numSectorsPerBlock = ATAConstants.SECTORS_PER_BLOCK;

        status = new ATAStatus();
        interruptReason = new InterruptReason();

        // TODO
        // Initialise DMA
        // this.packetDma = ????;

    }

    /**
     * reset the control
     */
    public void reset() {
    }

    /**
     * @return -
     */
    public ATAStatus getStatus() {
        return status;
    }

    /**
     * @return -
     */
    public int getLbaMode() {
        return lbaMode;
    }

    /**
     * @param lbaMode
     */
    public void setLbaMode(int lbaMode) {
        this.lbaMode = lbaMode;
    }

    /**
     * @return -
     */
    public int getErrorRegister() {
        return errorRegister;
    }

    /**
     * @param errorRegister
     */
    public void setErrorRegister(int errorRegister) {
        this.errorRegister = errorRegister;
    }

    /**
     * @return -
     */
    public byte[] getBuffer() {
        return buffer;
    }

    /**
     * @param index
     * @param bufferValue
     */
    public void setBuffer(int index, byte bufferValue) {
        this.buffer[index] = bufferValue;
    }

    /**
     * @return -
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * @param bufferSize
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * @return -
     */
    public int getBufferIndex() {
        return bufferIndex;
    }

    /**
     * @param bufferIndex
     */
    public void setBufferIndex(int bufferIndex) {
        this.bufferIndex = bufferIndex;
    }

    /**
     * @return -
     */
    public int getCurrentCommand() {
        return currentCommand;
    }

    /**
     * @param currentCommand
     */
    public void setCurrentCommand(int currentCommand) {
        this.currentCommand = currentCommand;
    }

    /**
     * @return -
     */
    public int getDrqIndex() {
        return drqIndex;
    }

    /**
     * @param drqIndex
     */
    public void setDrqIndex(int drqIndex) {
        this.drqIndex = drqIndex;
    }

    /**
     * @return -
     */
    public InterruptReason getInterruptReason() {
        return interruptReason;
    }

    /**
     * @param interruptReason
     */
    public void setInterruptReason(InterruptReason interruptReason) {
        this.interruptReason = interruptReason;
    }

    /**
     * @return -
     */
    public int getByteCount() {
        return byteCount;
    }

    /**
     * @param byteCount
     */
    public void setByteCount(int byteCount) {
        this.byteCount = byteCount;
    }

    /**
     * @return -
     */
    public boolean isDisableIrq() {
        return disableIrq;
    }

    /**
     * @param disableIrq
     */
    public void setDisableIrq(boolean disableIrq) {
        this.disableIrq = disableIrq;
    }

    /**
     * @return -
     */
    public boolean isReset() {
        return reset;
    }

    /**
     * @param reset
     */
    public void setReset(boolean reset) {
        this.reset = reset;
    }

    /**
     * @return -
     */
    public int getResetInProgress() {
        return resetInProgress;
    }

    /**
     * @param resetInProgress
     */
    public void setResetInProgress(int resetInProgress) {
        this.resetInProgress = resetInProgress;
    }

    /**
     * @return -
     */
    public int getNumSectorsPerBlock() {
        return numSectorsPerBlock;
    }

    /**
     * @param numSectorsPerBlock
     */
    public void setNumSectorsPerBlock(int numSectorsPerBlock) {
        this.numSectorsPerBlock = numSectorsPerBlock;
    }

    /**
     * @return -
     */
    public int getPacketDma() {
        return packetDma;
    }

    /**
     * @param packetDma
     */
    public void setPacketDma(int packetDma) {
        this.packetDma = packetDma;
    }

    /**
     * @return -
     */
    public int getMultipleSectors() {
        return multipleSectors;
    }

    /**
     * @param multipleSectors
     */
    public void setMultipleSectors(int multipleSectors) {
        this.multipleSectors = multipleSectors;
    }

}
