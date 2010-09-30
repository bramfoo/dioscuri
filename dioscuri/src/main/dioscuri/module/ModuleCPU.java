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

import dioscuri.interfaces.Addressable;
import dioscuri.interfaces.Module;

/**
 * Interface representing a generic CPU module.
 */
public abstract class ModuleCPU extends AbstractModule implements Addressable {

    /**
     *
     */
    public ModuleCPU() {
        super(Module.Type.CPU,
                Module.Type.MEMORY, Module.Type.MOTHERBOARD, Module.Type.PIC, Module.Type.CLOCK);
    }

    /**
     * Set the Instructions Per Second (ips) for this CPU.
     * 
     * @param ips the Instructions Per Second (ips) for this CPU.
     */
    public abstract void setIPS(int ips);

    /**
     * Get the Instructions Per Second (ips) for this CPU.
     *
     * @return the Instructions Per Second (ips) for this CPU.
     */
    public abstract int getIPS();

    /**
     * Set the Instructions Per Second (ips) for this CPU. Also, define what the
     * smallest period is for sending a clockpulse (in microseconds)
     * 
     * @param ips
     * @param lowestUpdatePeriod the lowest update period in microseconds
     */
    public abstract void setIPS(int ips, int lowestUpdatePeriod);

    /**
     * Retrieve string with information about next instruction to be executed
     * 
     * @return string containing next instruction information
     */
    public abstract String getNextInstructionInfo();

    /**
     * Retrieve current number of instruction (instructions executed so far)
     * 
     * @return long containing number of instructions
     */
    public abstract long getCurrentInstructionNumber();

    /**
     * Increment current number of instruction by one
     */
    protected abstract void incrementInstructionCounter();

    /**
     * Returns a dump of the current registers with their value
     * 
     * @return String containing a register dump
     */
    public abstract String dumpRegisters();

    /**
     * Initialise registers
     * 
     * @return true if initialisation is successful, false otherwise
     */
    protected abstract boolean initRegisters();

    /**
     * Initialise the single and double byte opcode lookup arrays with
     * instructions corresponding to the Intel hexadecimal machinecode values.
     *
     * @return
     */
    protected abstract boolean initInstructionTables();

    /**
     * Set the boolean that starts and stops the CPU loop
     * 
     * @param status sets the isRunning boolean
     */
    protected abstract void setRunning(boolean status);

    /**
     * Returns the value of a named register.
     *
     * @param registerName
     * @return int[] with value of register, null otherwise
     */
    protected abstract byte[] getRegisterValue(String registerName);

    /**
     * Sets the value of a named register to given value.
     *
     * @param registerName
     * @param value containing the value
     * @return true if set was successful, false otherwise
     */
    protected abstract boolean setRegisterValue(String registerName, byte[] value);

    /**
     * Set the interrupt request (IRQ).
     * @param value
     */
    public abstract void interruptRequest(boolean value);

    /**
     * Sets the CPU hold mode by asserting a Hold Request.<BR>
     * This informs the CPU to avoid using the (non-existent) bus as another
     * device (usually via DMA) is using it; it should be scheduled as a
     * asynchronous event in CPU.
     *
     * @param value      state of the Hold Request
     * @param originator -
     */
    public abstract void setHoldRequest(boolean value, Module originator);

    /**
     *
     * @param register
     * @return
     */
    public abstract String getRegisterHex(int register);

    /**
     * Get CPU instruction debug.
     *
     * @return cpuInstructionDebug.
     */
    public abstract boolean getCpuInstructionDebug();

    /**
     * Set the CPU instruction debug.
     *
     * @param isDebugMode status of instructionDebug (on/off)
     */
    public abstract void setCpuInstructionDebug(boolean isDebugMode);

    /**
     * Returns if CPU halted abnormally or not
     * 
     * @return boolean abnormalTermination true if abnormal, false otherwise
     */
    public abstract boolean isAbnormalTermination();

    /**
     * Returns if CPU halted due to full system shutdown or not
     * 
     * @return boolean shutDown true if emulator should shutdown, false
     *         otherwise
     */
    public abstract boolean isShutdown();
}
