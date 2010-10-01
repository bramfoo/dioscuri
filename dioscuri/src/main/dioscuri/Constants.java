/*
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
package dioscuri;

import java.io.File;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class with system wide constants.
 *
 * @author Bram Lohman
 * @author Bart Kiers
 */
public final class Constants {

    private static final Logger logger = Logger.getLogger(Constants.class.getName());

    /**
     * Private c-tor: no need to instantiate this class.
     */
    private Constants()
    {
    }

    /**
     * Returns the directory, or a the location of the JAR file from
     * which Dioscuri is launched, as a java.io.File object.
     *
     * @return the directory, or a the location of the JAR file from
     *         which Dioscuri is launched, as a java.io.File object.
     */
    private static File findRoot()
    {
        try {
            // URLDecoder.decode(...) is used, otherwise special "html" character like spaces are
            // displayed as '%20' 
            return new File(URLDecoder.decode(
                    GUI.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8"));
        } catch (Exception e) {
            logger.log(Level.WARNING, "could not find the exe-path of Dioscuri");
            // could not find the path or file: return the user's 'pwd'
            return new File(".");
        }
    }

    // Constants
    /**
     * represents the parent directory of the executed GUI.class or the File ascociated with Dioscuri-X-X.jar
     */
    public static final File JAR_OR_FOLDER = findRoot();

    /**
     * the root folder of the application (<code>JAR_OR_FOLDER.isFile() ? JAR_OR_FOLDER.getParentFile() : JAR_OR_FOLDER</code>)
     */
    public static final File EXE_FOLDER = JAR_OR_FOLDER.isFile() ? JAR_OR_FOLDER.getParentFile() : JAR_OR_FOLDER;

    /**
     * the name of Dioscuri
     */
    public static final String EMULATOR_NAME = "Dioscuri - modular emulator for digital preservation";

    /**
     * the version of Dioscuri
     */
    public static final String EMULATOR_VERSION = "0.6.0";

    /**
     * the release date (<code>MMMM, YYYY</code>)
     */
    public static final String EMULATOR_DATE = "September, 2010";

    /**
     * institutions participating (or participated) in the development of Dioscuri
     */
    public static final String EMULATOR_CREATOR = "Koninklijke Bibliotheek (KB), Nationaal Archief of the Netherlands, Planets, KEEP";

    /**
     * the absolute path of the config folder
     */
    public static final String CONFIG_DIR = new File(EXE_FOLDER, "config").getAbsolutePath();

    /**
     * the absolute path of Dioscuri's icon
     */
    public static final String EMULATOR_ICON_IMAGE = new File(CONFIG_DIR, "dioscuri_icon.gif").getAbsolutePath();

    /**
     * the absolute path of the splash screen image
     */
    public static final String EMULATOR_SPLASHSCREEN_IMAGE = new File(CONFIG_DIR, "dioscuri_splashscreen_2010_v043.gif").getAbsolutePath();

    /**
     * the absolute path of the logging properties file
     */
    public static final String EMULATOR_LOGGING_PROPERTIES = new File(CONFIG_DIR, "logging.properties").getAbsolutePath();

    /**
     * the absolute path of the default config xml file
     */
    public static final String DEFAULT_CONFIG_XML = new File(CONFIG_DIR, "DioscuriConfig.xml").getAbsolutePath();

    /**
     * the absolute path of the bios image file
     */
    public static final String BOCHS_BIOS = new File(EXE_FOLDER, "images/bios/BIOS-bochs-latest").getAbsolutePath();

    /**
     * the absolute path of the vga bios image file
     */
    public static final String VGA_BIOS = new File(EXE_FOLDER, "images/bios/VGABIOS-lgpl-latest").getAbsolutePath();
}
