/* $Revision$ $Date$ $Author$ 
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

package dioscuri;

import java.util.ArrayList;

import dioscuri.module.Module;

/**
 *
 * @author Bram Lohman
 * @author Bart Kiers
 */
@SuppressWarnings("serial")
public class Modules extends ArrayList<Module> {
    // Attributes

    // Constructors
    public Modules() {
        super();
    }

    /**
     *
     * @param capacity
     */
    public Modules(int capacity) {
        super(capacity);
    }

    // Methods
    /**
     *
     * @param module
     * @return -
     */
    public boolean addModule(Module module) {
        return super.add(module);
    }

    /**
     *
     * @param type
     * @return -
     */
    public Module getModule(String type) {
        for (int i = 0; i < super.size(); i++) {
            if ((this.getModule(i)).getType().toString().equalsIgnoreCase(type)) {
                return (Module) super.get(i);
            }
        }
        System.out.println(">>> Couldn't find a :: "+type+"\n    in: ");
        for(Module m : this) {
            System.out.println("        "+m.getType());
        }
        return null;
    }

    /**
     *
     * @param index
     * @return -
     */
    public Module getModule(int index) {
        return (Module) super.get(index);
    }
}
