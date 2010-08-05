/* $Revision: 160 $ $Date: 2009-08-17 12:56:40 +0000 (ma, 17 aug 2009) $ $Author: blohman $ 
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

package dioscuri.module;

import java.util.HashMap;
import java.util.Map;

/**
 * An abstract class representing a generic hardware module.
 *
 * @author Bram Lohman
 * @author Bart Kiers
 */
public abstract class Module {

    /**
     * The type of a Module.
     */
    public static enum Type {
        ATA,
        BIOS,
        CLOCK,
        CPU,
        DMA,
        FDC,
        KEYBOARD,
        MEMORY,
        MOTHERBOARD,
        MOUSE,
        PARALLEL_PORT,
        PCI,
        PIC,
        PIT,
        RTC,
        SCREEN,
        SERIAL_PORT,
        VIDEO
    }

    private final Type type;
    private final Map<Type, Module> connections;
    private boolean debugMode;

    /**
     * Creates a new Module of a specific Type and the expected Types
     * it should be connected to to work properly.
     *
     * @param type                the type of this Module.
     * @param expectedConnections the expected Types it should be
     *                            connected to to work properly.
     */
    public Module(Type type, Type... expectedConnections) {
        this.type = type;
        this.debugMode = false;
        this.connections = new HashMap<Type, Module>();
        for(Type t : expectedConnections) {
            connections.put(t, null);
        }
    }

    /**
     * Returns all modules its connected to.
     *
     * @return all modules its connected to.
     */
    public Module[] getConnections() {
        return this.connections.values().toArray(new Module[this.connections.size()]);
    }

    /**
     * Returns the state of debug mode
     *
     * @return true if this module is in debug mode, false otherwise
     */
    public boolean getDebugMode() {
        return this.debugMode;
    }

    /**
     * Return a dump of module status
     *
     * @return string containing a dump of this module
     */
    public abstract String getDump();

    /**
     * Get a Module given a certain Module.Type.
     *
     * @param type the Type of the Module to be fetched.
     * @return     a Module given a certain Module.Type.
     */
    public Module getModule(Module.Type type) {
        Module module = this.connections.get(type);
        if(module == null) {
            throw new RuntimeException("No such Module: '"+type+"' in '"+this.getClass().getName()+"'.");
        }
        return module;
    }

    /**
     * Returns the type of module (CPU, Memory, etc.)
     *
     * @return string with the type of this module
     *
     */
    public Type getType() {
        return this.type;
    }

    /**
     * Checks if this module is connected to operate normally.
     *
     * @return true if this module is connected successfully, false otherwise
     */
    public boolean isConnected() {
        for(Map.Entry<Type, Module> entry : this.connections.entrySet()) {
            if(entry.getValue() == null) {
                // There is still a module not initialized (the expected Type
                // points to null).
                return false;
            }
        }
        return true;
    }

    /**
     * Set toggle to define if this module is in debug mode or not.
     *
     * @param debugMode a toggle to define if this module is in debug mode or not.
     */
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    /**
     * Sets up a connection with another module.
     *
     * @param module the Module that is to be connected.
     * @return true if connection was set successfully, false otherwise.
     */
    public boolean setConnection(Module module) {
        if(!this.connections.containsKey(module.type)) {
            return false; // or throw exception?
        }
        this.connections.put(module.type, module);
        return true;
    }
}
