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
 * Class to hold status flags of a ATA channel
 * 
 */
public class ATAStatus {

    private int busy;
    private int driveReady;
    private int writeFault;
    private int seekComplete;
    private int drq;
    private int correctedData;
    private int indexPulse;
    private int indexPulseCount;
    private int err;

    /**
     * Constructor.
     */
    public ATAStatus() {
        // Initialize controller state
        busy = 0;
        driveReady = 1;
        writeFault = 0;
        seekComplete = 1;
        drq = 0;
        correctedData = 0;
        indexPulse = 0;
        indexPulseCount = 0;
        err = 0;
    }

    /**
     *
     * @return -
     */
    public int getBusy() {
        return busy;
    }

    /**
     *
     * @param busy
     */
    public void setBusy(int busy) {
        this.busy = busy;
    }

    /**
     *
     * @return -
     */
    public int getDriveReady() {
        return driveReady;
    }

    /**
     *
     * @param driveReady
     */
    public void setDriveReady(int driveReady) {
        this.driveReady = driveReady;
    }

    /**
     *
     * @return -
     */
    public int getWriteFault() {
        return writeFault;
    }

    /**
     *
     * @param writeFault
     */
    public void setWriteFault(int writeFault) {
        this.writeFault = writeFault;
    }

    /**
     *
     * @return -
     */
    public int getSeekComplete() {
        return seekComplete;
    }

    /**
     *
     * @param seekComplete
     */
    public void setSeekComplete(int seekComplete) {
        this.seekComplete = seekComplete;
    }

    /**
     *
     * @return -
     */
    public int getDrq() {
        return drq;
    }

    /**
     *
     * @param drq
     */
    public void setDrq(int drq) {
        this.drq = drq;
    }

    /**
     *
     * @return -
     */
    public int getCorrectedData() {
        return correctedData;
    }

    /**
     *
     * @param correctedData
     */
    public void setCorrectedData(int correctedData) {
        this.correctedData = correctedData;
    }

    /**
     *
     * @return -
     */
    public int getIndexPulse() {
        return indexPulse;
    }

    /**
     *
     * @param indexPulse
     */
    public void setIndexPulse(int indexPulse) {
        this.indexPulse = indexPulse;
    }

    /**
     *
     * @return -
     */
    public int getIndexPulseCount() {
        return indexPulseCount;
    }
    public void decrementIndexPulseCount() {
        indexPulseCount--;
    }
    public void incrementIndexPulseCount() {
        indexPulseCount++;
    }

    /**
     *
     * @param indexPulseCount
     */
    public void setIndexPulseCount(int indexPulseCount) {
        this.indexPulseCount = indexPulseCount;
    }

    /**
     *
     * @return -
     */
    public int getErr() {
        return err;
    }

    /**
     *
     * @param err
     */
    public void setErr(int err) {
        this.err = err;
    }
}
