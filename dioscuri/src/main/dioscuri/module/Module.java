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
 * Abstract class representing a generic hardware module.
 * 
 */
public abstract class Module {

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
        //VGA,
        VIDEO
    }

    public final Type type;
    private final Map<Type, Module> connections;
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


    public final Module[] getConnections() { // TODO refactor into getConnections()
        return connections.values().toArray(new Module[connections.size()]);  
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
        for(Module connectedTo : this.connections.values()) {
            if(connectedTo == null) {
                return false;
            }
        }
        return true;
    }

    /*
    public boolean setConnection(Module module) {
        return setConnection(module, false);
    }

    public boolean setConnection(Module module, boolean optional) {
        return false;
    }
    */    

    // TODO :: OLD METHODS BELOW :: TODO

    /**
     * Returns the type of module (CPU, Memory, etc.)
     *
     * @return string with the type of this module
     *
     */
    //public abstract String getType();

    /**
     * Returns the name of module
     * 
     * @return string with the name of this module
     * 
     */
    public abstract String getName();

    /**
     * Returns a String[] with all names of modules it needs to be connected to
     * 
     * @return String[] containing the names of modules
     */
    //public abstract String[] getConnections();

    /**
     * Sets up a connection with another module
     * 
     * @param mod
     *            Module that is to be connected
     * 
     * @return true if connection was set successfully, false otherwise
     */
    public abstract boolean setConnection(Module mod);



    /**
     * Reset all parameters of module
     * 
     * @return -
     */
    public abstract boolean reset();

    /**
     * Starts the module to become active
     * 
     */
    public abstract void start();

    /**
     * Stops the module from being active
     * 
     */
    public abstract void stop();

    /**
     * Returns the state of observed
     * 
     * @return true if this module is observed, false otherwise
     */
    public abstract boolean isObserved();

    /**
     * Set toggle to define if this module is observed or not
     * 
     * @param status
     */
    public abstract void setObserved(boolean status);

    /**
     * Returns the state of debug mode
     * 
     * @return true if this module is in debug mode, false otherwise
     */
    public abstract boolean getDebugMode();

    /**
     * Set toggle to define if this module is in debug mode or not
     * 
     * @param status
     */
    public abstract void setDebugMode(boolean status);

    /**
     * Returns data from this module
     * 
     * @param module
     * @return byte[] with data
     */
    public abstract byte[] getData(Module module);

    /**
     * Set data for this module
     * 
     * @param data
     * @param module
     * @return true if data is set successfully, false otherwise
     */
    public abstract boolean setData(byte[] data, Module module);

    /**
     * Set data for this module
     * 
     * @param data
     * @param module
     * @return true if data is set successfully, false otherwise
     */
    public abstract boolean setData(String[] data, Module module);

    /**
     * Return a dump of module status
     * 
     * @return string containing a dump of this module
     */
    public abstract String getDump();

}
