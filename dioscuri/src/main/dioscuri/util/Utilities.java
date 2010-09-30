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
 * Utility class
 * @author Bart Kiers
 */
public final class Utilities {

    private static final Logger logger = Logger.getLogger(Utilities.class.getName());

    /*
     * No need to instantiate this class
     */
    private Utilities() {}

    /**
     * Resolves a <code>path</code>:  if <code>path</code> exists, it's directly returned.
     * Else, the execution folder of the Dioscuri JAR is prepend to <code>path</code> and
     * then the absolute path of that <code>File</code> is returned
     *
     * @param path      the relative or absolute path of a file
     * @return          the absolute path of a file denoted by the relative-
     *                  or absolute <code>path</code> (the parameter)
     * @see Utilities#resolvePathAsFile(String)
     */
    public static String resolvePathAsString(String path) {
        File file = resolvePathAsFile(path);
        return file.isFile() ? file.getAbsolutePath() : "";
    }

    /**
     * Resolves a <code>path</code>:  if <code>path</code> exists, a <code>File</code>
     * is directly constructed of it. Else, the execution folder of the Dioscuri JAR is
     * prepend to <code>path</code> and then a <code>File</code> is constructed and returned
     *
     * @param path      the relative or absolute path of a file
     * @return          if <code>path</code> exists, a <code>File</code> is directly
     *                  constructed of it. Else, the execution folder of the Dioscuri JAR is
     *                  prepend to <code>path</code> and then a <code>File</code> is directly
     *                  constructed and returned
     * @see Utilities#resolvePathAsString(String)
     */
    public static File resolvePathAsFile(String path) {
        if(path == null) {
            path = "";
        }
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
     * Saves the settings of an <code>dioscuri.config.Emulator</code>
     * object to the file denoted by <code>path</code>.
     *
     * @param emuObject     the settings to be saved
     * @param               path the path of the XML config file
     * @return              <code>true</code> if the settings from
     *                      <code>dioscuri.config.Emulator</code>
     *                      are successfully saved in
     *                      <code>path</code>, else <code>false</code>
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
