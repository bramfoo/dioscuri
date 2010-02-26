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
 * Class to represent Sense Info.
 * 
 */
public class SenseInfo {

    /**
     * Constructor.
     */
    public SenseInfo() {
        // TODO: init values
    }

    private SenseType senseKey;
    private AscType ascType;

    private byte fruc;
    private byte ascq;

    private byte[] information = new byte[4];
    private byte[] specificInf = new byte[4];
    private byte[] keySpec = new byte[3];

    /**
     *
     * @return
     */
    public byte getFruc() {
        return fruc;
    }

    /**
     *
     * @param fruc
     */
    public void setFruc(byte fruc) {
        this.fruc = fruc;
    }

    /**
     *
     * @return
     */
    public AscType getAsc() {
        return this.ascType;
    }

    /**
     *
     * @param ascType
     */
    public void setAsc(AscType ascType) {
        this.ascType = ascType;
    }

    /**
     *
     * @return
     */
    public byte getAscq() {
        return this.ascq;
    }

    /**
     *
     * @param ascq
     */
    public void setAscq(byte ascq) {
        this.ascq = ascq;
    }

    /**
     *
     * @return
     */
    public byte[] getInformation() {
        return information;
    }

    /**
     *
     * @param index
     * @param information
     */
    public void setInformation(int index, byte information) {
        this.information[index] = information;
    }

    /**
     *
     * @return
     */
    public byte[] getSpecificInf() {
        return specificInf;
    }

    /**
     *
     * @param index
     * @param specificInf
     */
    public void setSpecificInf(int index, byte specificInf) {
        this.specificInf[index] = specificInf;
    }

    /**
     *
     * @return
     */
    public byte[] getKeySpec() {
        return keySpec;
    }

    /**
     *
     * @param index
     * @param keySpec
     */
    public void setKeySpec(int index, byte keySpec) {
        this.keySpec[index] = keySpec;
    }

    /**
     *
     * @return
     */
    public SenseType getSenseKey() {
        return senseKey;
    }

    /**
     *
     * @param senseKey
     */
    public void setSenseKey(SenseType senseKey) {
        this.senseKey = senseKey;
    }

}
