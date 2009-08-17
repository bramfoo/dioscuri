/*
 * $Revision$ $Date$ $Author$
 * 
 * Copyright (C) 2007  National Library of the Netherlands, Nationaal Archief of the Netherlands
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information about this project, visit
 * http://dioscuri.sourceforge.net/
 * or contact us via email:
 * jrvanderhoeven at users.sourceforge.net
 * blohman at users.sourceforge.net
 * 
 * Developed by:
 * Nationaal Archief               <www.nationaalarchief.nl>
 * Koninklijke Bibliotheek         <www.kb.nl>
 * Tessella Support Services plc   <www.tessella.com>
 *
 * Project Title: DIOSCURI
 *
 */

package nl.kbna.dioscuri.module.clock;

import java.util.logging.Level;
import java.util.logging.Logger;

import nl.kbna.dioscuri.Emulator;
import nl.kbna.dioscuri.module.Module;
import nl.kbna.dioscuri.module.ModuleCPU;
import nl.kbna.dioscuri.module.ModuleClock;
import nl.kbna.dioscuri.module.ModuleDevice;
import nl.kbna.dioscuri.module.ModuleMotherboard;

/**
 * Module Clock
 * This module implements a pulsing clock mechanism. Based on a user-defined sleepTime the 
 * clock sends a pulse to the PIT-counters after sleeping.
 * 
 * Note:
 * - This clock imitates the crystal clock (crystal timer in hardware)
 * - The (maximum) operating frequency of this clock should be 1193181 Hz (0.00083809581 ms/cycle).
 * - This implementation can distinguish between real-time pulsing by host machine or CPU-pulsed by target machine.
 * - The actual frequency in real-time pulsing is 1 pulse each millisecond
 * - The actual frequency in CPU-triggered pulsing is 1 pulse each microsecond
 * - By default the clock is CPU-triggered
 * - Only 10 clock users can be registered at max
 * 
 */
public class Clock extends ModuleClock
{
    // Attributes

    // Relations
    private Emulator emu;
    private String[] moduleConnections = new String[] {"motherboard", "cpu"}; 
    private ModuleMotherboard motherboard;
    private ModuleCPU cpu;
    private Timer[] timers;
    
    // Toggles
    private boolean isObserved;
    private boolean debugMode;
    private boolean keepRunning;

    // Timing
    private long sleepTime;
    private int arrayIndex;
    
    // Logging
    private static Logger logger = Logger.getLogger("nl.kbna.dioscuri.module.clock");
    
    // Constants
    // Module specifics
    public final static int MODULE_ID       = 1;
    public final static String MODULE_TYPE  = "clock";
    public final static String MODULE_NAME  = "Crystal clock";
    
    public final static int TIMER_ARRAY_SIZE= 10;
    
    
    // Constructor
    /**
     * Constructor Clock
     * 
     */
    public Clock(Emulator owner)
    {
        emu = owner;
        
        // Initialise array for all timers
        timers = new Timer[TIMER_ARRAY_SIZE];
        arrayIndex = 0;
        
        keepRunning = true;
        
        // Set sleepTime on default value
        sleepTime = 1000;

        logger.log(Level.INFO, "[" + MODULE_TYPE + "]" + MODULE_NAME + " -> Module created successfully.");
    }

    
    //******************************************************************************
    // Module Methods
    
    /**
     * Returns the ID of the module
     * 
     * @return string containing the ID of module 
     * @see Module
     */
    public int getID()
    {
        return MODULE_ID;
    }

    
    /**
     * Returns the type of the module
     * 
     * @return string containing the type of module 
     * @see Module
     */
    public String getType()
    {
        return MODULE_TYPE;
    }


    /**
     * Returns the name of the module
     * 
     * @return string containing the name of module 
     * @see Module
     */
    public String getName()
    {
        return MODULE_NAME;
    }

    
    /**
     * Returns a String[] with all names of modules it needs to be connected to
     * 
     * @return String[] containing the names of modules, or null if no connections
     */
    public String[] getConnection()
    {
        // Return all required connections;
        return moduleConnections;
    }

    
    /**
     * Sets up a connection with another module
     * 
     * @param mod   Module that is to be connected to this class
     * 
     * @return true if connection has been established successfully, false otherwise
     * 
     * @see Module
     */
    public boolean setConnection(Module mod)
    {
        // Set connection for motherboard
        if (mod.getType().equalsIgnoreCase("motherboard"))
        {
            this.motherboard = (ModuleMotherboard)mod;
            return true;
        }
        // Set connection for cpu
        else if (mod.getType().equalsIgnoreCase("cpu"))
        {
            this.cpu = (ModuleCPU)mod;
            return true;
        }
        
        // No connection has been established
        return false;
    }


    /**
     * Checks if this module is connected to operate normally
     * 
     * @return true if this module is connected successfully, false otherwise
     */
    public boolean isConnected()
    {
        // Check if module if connected
        if (this.motherboard != null && this.cpu != null)
        {
            return true;
        }
        
        // One or more connections may be missing
        return false;
    }


    /**
     * Reset all parameters of module
     * 
     * @return boolean true if module has been reset successfully, false otherwise
     */
    public boolean reset()
    {
        // Register clock to motherboard
        if (motherboard.registerClock(this) == false)
        {
            return false;
        }
        
        logger.log(Level.INFO, "[" + MODULE_TYPE + "] Module has been reset.");
        return true;
    }

    
    /**
     * Starts the module
     * @see Module
     */
    public void start()
    {
        // Nothing to start
    }
    

    /**
     * Stops the module
     * @see Module
     */
    public void stop()
    {
        // Stop Clock thread
        this.setKeepRunning(false);
    }
    
    
    /**
     * Returns the status of observed toggle
     * 
     * @return state of observed toggle
     * 
     * @see Module
     */
    public boolean isObserved()
    {
        return isObserved;
    }


    /**
     * Sets the observed toggle
     * 
     * @param status
     * 
     * @see Module
     */
    public void setObserved(boolean status)
    {
        isObserved = status;
    }


    /**
     * Returns the status of the debug mode toggle
     * 
     * @return state of debug mode toggle
     * 
     * @see Module
     */
    public boolean getDebugMode()
    {
        return debugMode;
    }


    /**
     * Sets the debug mode toggle
     * 
     * @param status
     * 
     * @see Module
     */
    public void setDebugMode(boolean status)
    {
        debugMode = status;
    }


    /**
     * Returns data from this module
     *
     * @param Module requester, the requester of the data
     * @return byte[] with data
     * 
     * @see Module
     */
    public byte[] getData(Module requester)
    {
        return null;
    }


    /**
     * Set data for this module
     * 
     * @param byte[] data
     * @param Module sender, the sender of the data
     * 
     * @return boolean true if successful, false otherwise
     * 
     * @see Module
     */
    public boolean setData(byte[] data, Module sender)
    {
        return false;
    }


    /**
     * Set String[] data for this module
     * 
     * @param String[] data
     * @param Module sender, the sender of the data
     * 
     * @return boolean true is successful, false otherwise
     * 
     * @see Module
     */
    public boolean setData(String[] data, Module sender)
    {
        return false;
    }

    
    /**
     * Returns a dump of this module
     * 
     * @return string
     * 
     * @see Module
     */
    public String getDump()
    {
        // Show some status information of this module
        String dump = "";
        String ret = "\r\n";
        String tab = "\t";
        
        dump = "Clock dump:" + ret;
        
        for (int t = 0; t < timers.length; t++)
        {
            if (timers[t] != null)
            {
                dump += "Timer " + t + ": " + timers[t].user.getType() + ", updateInterval=" +  timers[t].intervalLength + " instr., countdown=" + timers[t].currentCount + " instr. " + ret;
            }
        }
        
        return dump;
    }


    //******************************************************************************
    // ModuleClock Methods

    /**
     * Register a device to clock and assign a timer to it
     * 
     * @param ModuleDevice device that requires a timer
     * @param int intervalLength in microseconds
     * @param boolean continuous type of timer requested, true = continuous, false = one-shot 
     * 
     * @return boolean true if timer assigned successfully, false otherwise
     */
    public boolean registerDevice(ModuleDevice device, int intervalLength, boolean continuous)
    {
        // Check if timers are still available
        if (arrayIndex < TIMER_ARRAY_SIZE)
        {
            // Change the interval length from useconds to instructions
            timers[arrayIndex] = new Timer(device, intervalLength * (cpu.getIPS() / 1000000), continuous);
            
            logger.log(Level.INFO, "[" + MODULE_TYPE + "]" + " Device '" + device.getType() + "' registered a timer with interval " + timers[arrayIndex].intervalLength + " instructions");
            
            arrayIndex++;
            return true;
        }
        return false;
    }

    
    /**
     * Reset the countdown of a timer to the interval length
     * 
     * @param ModuleDevice device that request a timer reset
     * @param int updateInterval in microseconds
     * 
     * @return boolean true if timer is reset successfully, false otherwise
     */
    public boolean resetTimer(ModuleDevice device, int updateInterval)
    {
        // Check if timer exists for given device
        int t = 0;
        while (timers[t] != null)
        {
            if (timers[t].user.getType().equalsIgnoreCase(device.getType()))
            {
                timers[t].reset(updateInterval * (cpu.getIPS() / 1000000));
                logger.log(Level.FINE, "[" + MODULE_TYPE + "]" + " Device '" + device.getType() + "' timer reset to " + timers[t].intervalLength + " instructions");
                return true;
            }
            t++;
        }
        return false;
    }
    
    /**
     * Set a timer to start/stop running
     * 
     * @param ModuleDevice device that request a timer to be set
     * @param boolean runState the state to set the timer to (start/stop)
     * 
     * @return boolean true if timer is reset successfully, false otherwise
     */
    public boolean setTimerActiveState(ModuleDevice device, boolean runState)
    {
        // Check if timer exists for given device
        int t = 0;
        while (timers[t] != null)
        {
            if (timers[t].user.getType().equalsIgnoreCase(device.getType()))
            {
                timers[t].active = runState;
                logger.log(Level.FINE, "[" + MODULE_TYPE + "]" + " Device '" + device.getType() + "' timer active state set to " + runState);
                return true;
            }
            t++;
        }
        return false;
    }
    

    /**
     * Triggers device's update if timer goes off
     * 
     */
    public void pulse()
    {
        // Check all active timers
        int t = 0;
        while (timers[t] != null)
        {
            if (timers[t].active)
            {
                timers[t].currentCount--;
                if (timers[t].currentCount == 0)
                {
                    timers[t].reset();
                    timers[t].user.update();
                }
            }
            t++;
        }
    }
    
    
    //******************************************************************************
    // Additional Methods
    
    /**
     * Implements the run method of Thread
     * 
     */
    public void run()
    {
        // Generate a clock pulse each n millisecons while running
        while (keepRunning)
        {
            // Send pulse to all counters
/*            for (int c = 0; c < counters.length; c++)
            {
                counters[c].clockPulse();
            }
*/            
            // Try to sleep for a while
            try
            {
                Thread.sleep(sleepTime);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                keepRunning = false;
            }
        }
    }
    
    
    /**
     * Calibrates the system clock in comparison with host machine
     * It sets a penalty time to which the clock should pause until the next pulse may be send.
     * FIXME: This method is not completely implemented yet!!!
     * 
     */
    private void calibrate()
    {
        // Get system time
        boolean isCalibrated;
        
        isCalibrated = false;
        
        while (isCalibrated == false)
        {
            
        }
        
    }


    /**
     * Sets the keepRunning toggle
     * keepRunning states if the clockthread should keep running or not
     * 
     * @param boolean status
     */
    protected void setKeepRunning(boolean status)
    {
        keepRunning = status;
    }
    
    
    /**
     * Retrieves the current clockrate of this clock in milliseconds
     * 
     * @return long milliseconds defining how long the clock sleeps before sending a pulse
     */
    public long getClockRate()
    {
        // Return the current number of milliseconds the clock is sleeping
        return this.sleepTime;
    }
    
    
    /**
     * Sets the rate for this clock
     * 
     * @param long milliseconds defines how long the clock should wait periodically before sending a pulse
     */
    public void setClockRate(long milliseconds)
    {
        // Set the number of milliseconds before a pulse is generated
        this.sleepTime = milliseconds;
    }
    
    /**
     * Sets the active state for this clock
     * 
     * @param boolean state the type of active state the timer is set to
     */
    public void setActiveState(ModuleDevice device, boolean state)
    {
        // Check if timer exists for given device
        int t = 0;
        while (timers[t] != null)
        {
            if (timers[t].user.getType().equalsIgnoreCase(device.getType()))
            {
                timers[t].active = state;
            }
            t++;
        }
    }
}
