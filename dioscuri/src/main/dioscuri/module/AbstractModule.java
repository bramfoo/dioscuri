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

import dioscuri.interfaces.Module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class representing a generic hardware module.
 */
public abstract class AbstractModule implements Module {

    // a logger object
    private static Logger logger = Logger.getLogger(AbstractModule.class.getName());

    /**
     * the Type of this AbstractModule
     */
    protected final Type type;

    // maps the Type's and AbstractModule's this MOdule is supposed to be connected to
    private final Map<Module.Type, Module> connections;

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
    public AbstractModule(Type type, Type... expectedConnections) {

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
     * {@inheritDoc}
     */
    @Override
    public final Module getConnection(Type type) {
        return connections.get(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Map<Type, Module> getConnections() {
        return connections;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean getDebugMode() {
        return this.debugMode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract String getDump();

    /**
     * {@inheritDoc}
     */
    @Override
    public final String[] getExpectedConnections() { // TODO return type: AbstractModule.Type[]
        List<String> temp = new ArrayList<String>();
        for(Type t : this.connections.keySet()) {
            temp.add(t.toString());
        }
        return temp.toArray(new String[temp.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Type getType() {
        return this.type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isConnected() {
        for(Map.Entry<Type, Module> entry : this.connections.entrySet()) {
            if(entry.getValue() == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract boolean reset();

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean setConnection(Module module) {
        if(module == null) {
            logger.log(Level.SEVERE, "m == null");
            return false;
        }
        if(connections.get(module.getType()) != null) {
            logger.log(Level.INFO, type+" already connected to "+module.getType());
            return false;
        }
        logger.log(Level.INFO, type+" is connected to "+module.getType());
        connections.put(module.getType(), module);
        module.getConnections().put(this.type, this);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setDebugMode(boolean status) {
        this.debugMode = status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(){
        // empty, can be @Override-en in sub-classes
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(){
        // empty, can be @Override-en in sub-classes
    }
}
