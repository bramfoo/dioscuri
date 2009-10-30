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

package dioscuri.module.clock;

import dioscuri.module.ModuleDevice;

/**
 * A single counter of the PIT based on the Intel 82C54 chipset.
 * 
 * This counter works following the convention rules of the PIT: 1. For each
 * counter, the control word must be written before the initial count is
 * written. 2. The initial count must follow the count format specified in the
 * Control Word (LSB, MSB, etc.)
 * 
 * 
 */
public class Timer {
    // Attributes
    protected ModuleDevice user;
    protected int intervalLength;
    protected int currentCount;
    protected boolean active; // Timer state: active - timer is running
    // inactive - timer is stopped
    protected boolean typeContinuous; // Timer type : continuous automatically
                                      // resets and runs again

    // one-shot runs once then stops

    // Constructors
    public Timer() {
        this.user = null;
        this.intervalLength = -1;
        this.currentCount = -1;
        this.active = false;
        this.typeContinuous = false;
    }

    public Timer(ModuleDevice user, int intervalLength, boolean type) {
        this.user = user;
        this.intervalLength = intervalLength;
        this.currentCount = intervalLength;
        this.active = false;
        this.typeContinuous = type;
    }

    // Methods

    protected void reset() {
        // Reset countdown to initial update interval
        this.currentCount = intervalLength;

        // Stop the timer from continuing running if it is a one-shot timer
        if (typeContinuous == false) {
            active = false;
        }
    }

    protected void reset(int intervalLength) {
        // Reset update interval
        this.intervalLength = intervalLength;
        this.reset();
    }
}
