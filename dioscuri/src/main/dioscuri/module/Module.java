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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class representing a generic hardware module.
 */
public abstract class Module {

    // a logger object
    private static Logger logger = Logger.getLogger(Module.class.getName());

    /**
     * The Type of a Module.
     */
    public static enum Type {
        ATA,
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
        VIDEO
    }

    /**
     * the Type of this Module
     */
    protected final Type type;

    // maps the Type's and Module's this MOdule is supposed to be connected to
    private final Map<Type, Module> connections;

    // flag to keep track of we're in debug mode or not
    private boolean debugMode;

    /**
     * Creates a new instance of a module.
     *
     * @param type                  the type of this module.
     * @param expectedConnections   optional number of types this module
     *                              is supposed to be connected to when
     *                              the emulation process starts.
     */
    public Module(Type type, Type... expectedConnections) {

        if(type == null) {
            throw new NullPointerException("The type of class "+
                    this.getClass().getName()+" cannot be null");
        }

        if(expectedConnections == null) {
            throw new NullPointerException("The expectedConnections of class "+
                    this.getClass().getName()+" cannot be null");
        }

        this.type = type;
        this.connections = new HashMap<Type, Module>();
        this.debugMode = false;

        for(Type t : expectedConnections) {
            this.connections.put(t, null);
        }
    }

    /**
     * Get the Module with a given 'type' this Module is connected to.
     *
     * @param type the Module.Type.
     * @return     the Module with a given 'type' this Module is connected to
     *             or 'null' if no connected has been made (yet).
     */
    public final Module getConnection(Type type) {
        return connections.get(type);
    }

    /**
     * Returns the state of debug mode
     *
     * @return true if this module is in debug mode, false otherwise
     */
    public final boolean getDebugMode() {
        return this.debugMode;
    }

    /**
     * Return a dump of module status
     *
     * @return string containing a dump of this module
     */
    public abstract String getDump();

    /**
     * Get all Module.Type's this Module is supposed to be connected to.
     *
     * @return an array of Module.Type's this Module is supposed to be connected to.
     */
    public final String[] getExpectedConnections() { // TODO return type: Module.Type[]
        List<String> temp = new ArrayList<String>();
        for(Type t : this.connections.keySet()) {
            temp.add(t.toString());
        }
        return temp.toArray(new String[temp.size()]);
    }

    public final Type getType() {
        return this.type;
    }

    /**
     * Checks if this module is connected to operate normally
     *
     * @return true if this module is connected successfully, false otherwise
     */
    public final boolean isConnected() {
        for(Map.Entry<Type, Module> entry : this.connections.entrySet()) {
            if(entry.getValue() == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reset all parameters of module.
     *
     * @return true iff the Module was reset properly.
     */
    public abstract boolean reset();

    /**
     * Connect both Modules 'this' and 'module' to each other.
     *
     * @param module the other Module.
     * @return       true iff both Modules 'this' and 'module' are
     *               properly connected to each other.
     */
    public final boolean setConnection(Module module) {
        if(module == null) {
            logger.log(Level.SEVERE, "m == null");
            return false;
        }
        if(connections.get(module.type) != null) {
            logger.log(Level.INFO, type+" already connected to "+module.type);
            return false;
        }
        logger.log(Level.INFO, type+" is connected to "+module.type);
        connections.put(module.type, module);
        module.connections.put(type, this);
        return true;
    }

    /**
     * Set toggle to define if this module is in debug mode or not
     *
     * @param status the new debug mode for this Module. 
     */
    public final void setDebugMode(boolean status) {
        this.debugMode = status;
    }

    /**
     * Starts the module to become active.
     */
    public void start(){
        // empty, can be @Override-en in sub-classes
    }

    /**
     * Stops the module from being active.
     */
    public void stop(){
        // empty, can be @Override-en in sub-classes
    }
}
