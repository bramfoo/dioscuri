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
 * Class to represent Sense Info.
 *
 */
public class SenseInfo
{
        
    /**
     * Constructor.
     */
    public SenseInfo() {
        //TODO: init values
    }
    
    private SenseType senseKey;
    private AscType ascType;
    
    private byte fruc;
    private byte ascq;
    
    private byte[] information = new byte[4];
    private byte[] specificInf = new byte[4];     
    private byte[] keySpec = new byte[3];   
  
    public byte getFruc()
    {
        return fruc;
    }
    
    public void setFruc(byte fruc)
    {
        this.fruc = fruc;
    }
    
    public AscType getAsc()
    {
        return this.ascType;
    }
    
    public void setAsc(AscType ascType)
    {
        this.ascType = ascType;
    }
    
    public byte getAscq()
    {
        return this.ascq;
    }
    
    public void setAscq(byte ascq)
    {
        this.ascq = ascq;
    }

    public byte[] getInformation()
    {
        return information;
    }

    public void setInformation(int index, byte information)
    {
        this.information[index] = information;
    }

    public byte[] getSpecificInf()
    {
        return specificInf;
    }

    public void setSpecificInf(int index, byte specificInf)
    {
        this.specificInf[index] = specificInf;
    }

    public byte[] getKeySpec()
    {
        return keySpec;
    }

    public void setKeySpec(int index, byte keySpec)
    {
        this.keySpec[index] = keySpec;
    }

    public SenseType getSenseKey()
    {
        return senseKey;
    }

    public void setSenseKey(SenseType senseKey)
    {
        this.senseKey = senseKey;
    }
     
}
