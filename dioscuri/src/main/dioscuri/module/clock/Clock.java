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

import java.util.logging.Level;
import java.util.logging.Logger;

import dioscuri.Emulator;
import dioscuri.interfaces.Module;
import dioscuri.interfaces.Updateable;
import dioscuri.module.ModuleCPU;
import dioscuri.module.ModuleClock;
import dioscuri.module.ModuleMotherboard;

/**
 * AbstractModule Clock This module implements a pulsing clock mechanism. Based on a
 * user-defined sleepTime the clock sends a pulse to the PIT-counters after
 * sleeping.
 * 
 * Note: - This clock imitates the crystal clock (crystal timer in hardware) -
 * The (maximum) operating frequency of this clock should be 1193181 Hz
 * (0.00083809581 ms/cycle). - This implementation can distinguish between
 * real-time pulsing by host machine or CPU-pulsed by target machine. - The
 * actual frequency in real-time pulsing is 1 pulse each millisecond - The
 * actual frequency in CPU-triggered pulsing is 1 pulse each microsecond - By
 * default the clock is CPU-triggered - Only 10 clock users can be registered at
 * max
 * 
 */
public class Clock extends ModuleClock implements Runnable {

    // Logging
    private static final Logger logger = Logger.getLogger(Clock.class.getName());

    private Timer[] timers;
    private boolean keepRunning;

    // Timing
    private long sleepTime;
    private int arrayIndex;

    // Constants
    public final static int TIMER_ARRAY_SIZE = 10;

    /**
     * Constructor Clock
     * 
     * @param owner
     */
    public Clock(Emulator owner) {
        // Initialise array for all timers
        timers = new Timer[TIMER_ARRAY_SIZE];
        arrayIndex = 0;

        keepRunning = true;

        // Set sleepTime on default value
        sleepTime = 1000;

        logger.log(Level.INFO, "[" + super.getType() + "]" + getClass().getName()
                + " -> AbstractModule created successfully.");
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.AbstractModule
     */
    @Override
    public boolean reset() {

        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Module.Type.MOTHERBOARD);

        // Register clock to motherboard
        if (!motherboard.registerClock(this)) {
            return false;
        }

        logger.log(Level.INFO, "[" + super.getType() + "] AbstractModule has been reset.");
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Module
     */
    @Override
    public void stop() {
        this.setKeepRunning(false);
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.AbstractModule
     */
    @Override
    public String getDump() {
        // Show some status information of this module
        String dump = "";

        dump = "Clock dump:\n";

        for (int t = 0; t < timers.length; t++) {
            if (timers[t] != null) {
                dump += "Timer " + t + ": " + timers[t].user.getType()
                        + ", updateInterval=" + timers[t].intervalLength
                        + " instr., countdown=" + timers[t].currentCount
                        + " instr.\n";
            }
        }

        return dump;
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.ModuleClock
     */
    @Override
    public boolean registerDevice(Updateable device, int intervalLength, boolean continuous) {

        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Module.Type.MOTHERBOARD);
        ModuleCPU cpu = (ModuleCPU)super.getConnection(Module.Type.CPU);

        // Check if timers are still available
        if (arrayIndex < TIMER_ARRAY_SIZE) {
            // Change the interval length from useconds to instructions
            timers[arrayIndex] = new Timer(device, intervalLength * (cpu.getIPS() / 1000000), continuous);

            logger.log(Level.INFO, "[" + super.getType() + "]" + " Device '"
                    + device.getType() + "' registered a timer with interval "
                    + timers[arrayIndex].intervalLength + " instructions");

            arrayIndex++;
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.ModuleClock
     */
    @Override
    public boolean resetTimer(Updateable device, int updateInterval) {

        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Module.Type.MOTHERBOARD);
        ModuleCPU cpu = (ModuleCPU)super.getConnection(Module.Type.CPU);
        
        // Check if timer exists for given device
        int t = 0;
        while (timers[t] != null) {
            if (timers[t].user.getType() == device.getType()) {
                timers[t].reset(updateInterval * (cpu.getIPS() / 1000000));
                logger.log(Level.INFO, "[" + super.getType() + "]" + " Device '"
                        + device.getType() + "' timer reset to "
                        + timers[t].intervalLength + " instructions");
                return true;
            }
            t++;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.ModuleClock
     */
    @Override
    public boolean setTimerActiveState(Updateable device, boolean runState) {
        // Check if timer exists for given device
        int t = 0;
        while (timers[t] != null) {
            if (timers[t].user.getType() == device.getType()) {
                timers[t].active = runState;
                logger.log(Level.INFO, "[" + super.getType() + "]" + " Device '"
                        + device.getType() + "' timer active state set to "
                        + runState);
                return true;
            }
            t++;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.ModuleClock
     */
    @Override
    public void pulse() {
        // Check all active timers
        int t = 0;
        while (timers[t] != null) {
            if (timers[t].active) {
                timers[t].currentCount--;
                if (timers[t].currentCount == 0) {
                    timers[t].reset();
                    timers[t].user.update();
                }
            }
            t++;
        }
    }

    /**
     * Implements the run method of Runnable
     */
    @Override
    public void run() {
        // Generate a clock pulse each n milliseconds while running
        while (keepRunning) {
            // Try to sleep for a while
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
                keepRunning = false;
            }
        }
    }

    /**
     * Sets the keepRunning toggle keepRunning states if the clock-thread should
     * keep running or not
     * 
     * @param status
     */
    protected void setKeepRunning(boolean status) {
        keepRunning = status;
    }
}
