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
import java.io.RandomAccessFile;

/**
 * A class holding a IDE disk image.
 */
public class DiskImage {
    // Attributes

    /**
     *
     */
    protected File imageFile;

    /**
     * Class constructor without disk image file.
     * 
     */
    public DiskImage() {
    }

    /**
     * Class constructor with disk image file.
     * 
     * @param theImageFile
     * @throws IOException
     */
    public DiskImage(File theImageFile) throws IOException {

        this.imageFile = theImageFile;

    }

    /**
     * Read data from image.
     * 
     * @param theData
     * @param theOffset
     * @param theLength
     * @return the data
     * @throws IOException
     */
    public byte[] readFromImage(byte[] theData, int theOffset, int theLength)
            throws IOException {

        RandomAccessFile randomAccessFile = new RandomAccessFile(imageFile, "r");

        if (theOffset > 0) {
            randomAccessFile.seek(theOffset);
        }

        randomAccessFile.read(theData, 0, theLength);

        randomAccessFile.close();

        return theData;
    }

    /**
     * Write to image.
     * 
     * @param theData
     * @param theOffset
     * @param theLength
     * @throws IOException
     */
    public void writeToImage(byte[] theData, int theOffset, int theLength)
            throws IOException {

        RandomAccessFile randomAccessFile = new RandomAccessFile(imageFile,
                "rw");

        try {
            if (theOffset > 0) {
                randomAccessFile.seek(theOffset);
            }
    
            randomAccessFile.write(theData, 0, theLength);
        } finally {
            randomAccessFile.close();
        }
    }

    /**
     * Gets the size of the disk image in bytes.
     * 
     * @return
     * @returns the size of the disk image in bytes
     */
    protected long getSize() {

        Long imageSize = imageFile.length();
        return imageSize;
    }

}
