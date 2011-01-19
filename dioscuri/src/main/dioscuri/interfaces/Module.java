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
package dioscuri.interfaces;

import java.util.Map;

/**
 *
 */
public interface Module {

    /**
     * The Type of a Module.
     */
    enum Type {
        ATA,
        BOOT,
        BIOS,
        CLOCK,
        CPU,
        DEVICE,
        DMA,
        DMACONTROLLER,
        DUMMY,
        FDC,
        KEYBOARD,
        MEMORY,
        MOTHERBOARD,
        MOUSE,
        PARALLELPORT,
        PCI,
        PIC,
        PIT,
        RTC,
        SCREEN,
        SERIALPORT,
        VIDEO,
        VNC;

        /**
         * Returns the Type based on a given String.
         *
         * @param strType the String representation of the Type to be fetched.
         * @return the Type based on a given String, 'strType', or null if
         *         'strType' is not present in the set of enums.
         */
        public static Type resolveType(String strType) {
            try {
                return Type.valueOf(strType.toUpperCase());
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * Returns the Module of a certain Type connected to this Module.
     *
     * @param type the Type of the Module to be fetched.
     * @return the Module of a certain Type connected to this Module.
     */
    Module getConnection(Type type);

    /**
     * Returns all connected, or supposedly connected, Modules of this Module.
     *
     * @return all connected, or supposedly connected, Modules of this Module.
     */
    Map<Type, Module> getConnections();

    /**
     * Returns the state of debug mode
     *
     * @return true if this module is in debug mode, false otherwise
     */
    boolean getDebugMode();

    /**
     * Return a dump of module status
     *
     * @return string containing a dump of this module
     */
    String getDump();

    /**
     * Get all Module.Type's this AbstractModule is supposed to be connected to.
     *
     * @return an array of Module.Type's this AbstractModule is supposed to be connected to.
     */
    Type[] getExpectedConnections();

    /**
     * Returns the Type of this Module.
     *
     * @return the Type of this Module.
     */
    Module.Type getType();

    /**
     * Checks if this module is connected to operate normally
     *
     * @return true if this module is connected successfully, false otherwise
     */
    boolean isConnected();

    /**
     * Reset all parameters of module.
     *
     * @return true iff the AbstractModule was reset properly.
     */
    boolean reset();

    /**
     * Connect both Modules 'this' and 'module' to each other.
     *
     * @param module the other AbstractModule.
     * @return true iff both Modules 'this' and 'module' are
     *         properly connected to each other.
     */
    boolean setConnection(Module module);

    /**
     * Set toggle to define if this module is in debug mode or not
     *
     * @param status the new debug mode for this AbstractModule.
     */
    void setDebugMode(boolean status);

    /**
     * Starts the module to become active.
     */
    void start();

    /**
     * Stops the module from being active.
     */
    void stop();
}
