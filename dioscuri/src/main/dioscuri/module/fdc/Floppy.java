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

package dioscuri.module.fdc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author Bram Lohman
 * @author Bart Kiers
 */
public class Floppy {
    // Attributes
    protected byte type;
    protected byte[] bytes;
    private File imageFile;

    // Constructor

    /**
     * Class Constructor
     * 
     */
    public Floppy() {
        // Initialise variables
        type = 0;
        bytes = null;
    }

    /**
     * Constructor Floppy
     * 
     * @param type
     * @param imageFile
     * @throws IOException
     *             if file cannot be read (or does not exist)
     */
    public Floppy(byte type, File imageFile) throws IOException {
        this();

        // Set type of floppydisk
        this.type = type;

        // Set pointer to image file
        this.imageFile = imageFile;

        // Load image into bytes buffer
        this.loadImageFromFile();
    }

    // Methods

    /**
     * Get the size of floppy in bytes
     * 
     * @return -
     */
    protected int getSize() {
        return bytes.length;
    }

    /**
     * Load image from file
     * 
     * @throws IOException
     *             if file cannot be read (or does not exist)
     */
    private void loadImageFromFile() throws IOException {
        // Fetch bytes from image file
        // open input stream
        BufferedInputStream bdis = new BufferedInputStream(new DataInputStream(
                new FileInputStream(imageFile)));

        // read all bytes (as unsigned) in byte array
        bytes = new byte[bdis.available()];
        bdis.read(bytes, 0, bytes.length);

        // Close input stream
        bdis.close();
    }

    /**
     * Store image to file
     * 
     * @throws IOException
     *             if file cannot be written (or does not exist)
     */
    protected void storeImageToFile() throws IOException {
        // Store bytes to image file
        // Open output stream
        BufferedOutputStream bdos = new BufferedOutputStream(
                new DataOutputStream(new FileOutputStream(imageFile)));

        // Write all bytes (as unsigned) to file
        bdos.write(bytes, 0, bytes.length);

        // Close output stream
        bdos.close();
    }
}
