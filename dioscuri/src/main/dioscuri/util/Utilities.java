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
package dioscuri.util;

import dioscuri.Constants;
import dioscuri.config.ConfigController;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Bram Lohman
 * @author Bart Kiers
 */
public final class Utilities {

    private static final Logger logger = Logger.getLogger(Utilities.class.getName());

    /*
     * No need to instantiate this class
     */
    private Utilities() {}

    /**
     *
     * @param path
     * @return -
     */
    public static String resolvePathAsString(String path) {
        File file = resolvePathAsFile(path);
        return file.getAbsolutePath();
    }

    /**
     *
     * @param path
     * @return -
     */
    public static File resolvePathAsFile(String path) {
        File validPath = new File(path);
        if(!validPath.exists()) {
            // assume `path` is relative and Dioscuri is not executed from it's "root" folder
            validPath = new File(Constants.EXE_FOLDER, path);
            if(!validPath.exists()) {
                // both the relative- and absolute paths do not exist
                logger.log(Level.SEVERE, "neither '"+path+"' nor '"+validPath.getAbsolutePath()+"' exists");
            }
        }
        return validPath;
    }

    /**
     *
     * @param emuObject
     * @param path
     * @return -
     */
    public static boolean saveXML(dioscuri.config.Emulator emuObject, String path) {
        try {
            ConfigController.saveToXML(emuObject, Utilities.resolvePathAsFile(path));
        } catch (Exception e) {
            logger.log(Level.SEVERE, " [util] Failed to save config file");
            return false;
        }
        return true;
    }
}
