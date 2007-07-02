/*
 * $Revision: 1.1 $ $Date: 2007-07-02 14:31:27 $ $Author: blohman $
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

package nl.kbna.dioscuri.module;

import nl.kbna.dioscuri.exception.ModuleException;
import nl.kbna.dioscuri.exception.ModuleWriteOnlyPortException;

/**
 * Interface representing a generic hardware module.
 *  
 */

public abstract class ModuleCPU extends Module
{
	// Methods
	
    /**
     * Set the Instructions Per Second (ips) for this CPU.
     * 
     * @param int ips
     */
    public abstract void setIPS(int ips);
    
    /**
     * Get the Instructions Per Second (ips) for this CPU.
     * 
     * @param int ips
     */
    public abstract int getIPS();

    /**
     * Set the Instructions Per Second (ips) for this CPU.
     * Also, define what the smallest period is for sending a clockpulse (in microseconds)
     * 
     * @param int ips
     * @param int lowestUpdatePeriod in microseconds
     */
    public abstract void setIPS(int ips, int lowestUpdatePeriod);
    
    /**
     * Retrieve string with information about next instruction to be executed
     * 
     * @return string containing next instruction information
     * 
     */
    public abstract String getNextInstructionInfo();
    
    /**
     * Retrieve current number of instruction (instructions executed so far)
     * 
     * @return long containing number of instructions
     * 
     */
    public abstract long getCurrentInstructionNumber();
    
    /**
     * Increment current number of instruction by one
     * 
     */
    protected abstract void incrementInstructionCounter();

    /**
	 * Initialise registers
	 * 
	 * @return true if initialisation is successful, false otherwise
	 * 
	 */
	protected abstract boolean initRegisters();

	/**
	 * Initialise the single and double byte opcode lookup arrays with instructions
	 * corresponding to the Intel hexadecimal machinecode values.
	 *
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
	 * 
	 * @return int[] with value of register, null otherwise
	 */
	protected abstract byte[] getRegisterValue(String registerName);

	/**
	 * Sets the value of a named register to given value.
	 * 
	 * @param String registerName
	 * 
	 * @param int[] containing the value
	 * 
	 * @return true if set was successful, false otherwise
	 */
	protected abstract boolean setRegisterValue(String registerName, byte[] value);

    /**
     * Returns the value (byte) in I/O address space at given port address.
     * 
     * @param int portAddress
     * 
     * @return byte value
     * @throws ModuleException 
     */
    protected abstract byte getIOPortByte(int portAddress) throws ModuleException, ModuleWriteOnlyPortException;

    /**
     * Sets the value (byte) in I/O address space at given port address.
     * 
     * @param int portAddress
     * 
     * @param byte value
     * 
     * @throws ModuleException 
     */
    protected abstract void setIOPortByte(int portAddress, byte value) throws ModuleException;

    /**
     * Returns the value (word) in I/O address space at given port address.
     * 
     * @param int portAddress
     * 
     * @return byte[] value (word)
     * @throws ModuleException 
     */
    protected abstract byte[] getIOPortWord(int portAddress) throws ModuleException, ModuleWriteOnlyPortException;

    /**
     * Sets the value (word) in I/O address space at given port address.
     * 
     * @param int portAddress
     * 
     * @param byte[] value (word)
     * 
     * @throws ModuleException 
     */
    protected abstract void setIOPortWord(int portAddress, byte[] value) throws ModuleException;

    /**
     * Returns the value (double word) in I/O address space at given port address.
     * 
     * @param int portAddress
     * 
     * @return byte[] value (double word)
     * @throws ModuleException 
     */
    protected abstract byte[] getIOPortDoubleWord(int portAddress) throws ModuleException, ModuleWriteOnlyPortException;

    /**
     * Sets the value (double word) in I/O address space at given port address.
     * 
     * @param int portAddress
     * 
     * @param byte[] value (double word)
     * 
     * @throws ModuleException 
     */
    protected abstract void setIOPortDoubleWord(int portAddress, byte[] value) throws ModuleException;

    /**
     * Set the interrupt request (IRQ).
     * 
     * @return true if CPU takes care, false otherwise
     */
    public abstract void interruptRequest(boolean value);
    
    public abstract void setHoldRequest(boolean value, ModuleDevice origin);
    
    public abstract String getRegisterHex(int register);
    
    public abstract void setCpuInstructionDebug(boolean isDebugMode);
}
