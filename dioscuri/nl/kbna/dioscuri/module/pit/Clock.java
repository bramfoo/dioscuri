/*
 * $Revision: 1.1 $ $Date: 2007-07-02 14:31:44 $ $Author: blohman $
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

package nl.kbna.dioscuri.module.pit;

/**
 * Class Clock
 * This class implements a pulsing clock mechanism. Based on a user-defined sleepTime the 
 * clock sends a pulse to the PIT-counters after sleeping.
 * 
 * Note:
 * - maybe this clock should be separated from PIT as a real PIT does not contain a clock
 * - This clock imitates the system clock (crystal timer in hardware)
 * - The (maximum) operating frequency of this clock should be 1193181 Hz (0.00083809581 ms/cycle).
 * 
 * 
 */
public class Clock extends Thread
{
    // Attributes
    private PIT pit;
    private Counter[] counters;
    private boolean keepRunning;
    private long sleepTime;
    

    // Constructor
    /**
     * Constructor Clock
     * 
     * @param PIT pit the owner of this clock
     * @param Counter[] counters containing all counters of PIT that should receive a clockpulse
     */
    public Clock(PIT pit, Counter[] counters)
    {
        this.pit = pit;
        this.counters = counters;
        keepRunning = true;
        
        // Set sleepTime on default value
        sleepTime = 1000;
        
        // TODO: Calibrate frequency to host machine
        //this.calibrate();
    }

    
    // Methods
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
            for (int c = 0; c < counters.length; c++)
            {
                counters[c].clockPulse();
            }

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
     * TODO: Implement calibration of clock
     * 
     */
    private void calibrate()
    {
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
}
