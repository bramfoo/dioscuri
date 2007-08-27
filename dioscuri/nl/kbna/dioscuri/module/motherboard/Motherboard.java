/*
 * $Revision: 1.4 $ $Date: 2007-08-27 08:26:56 $ $Author: jrvanderhoeven $
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

package nl.kbna.dioscuri.module.motherboard;

import java.util.logging.Level;
import java.util.logging.Logger;

import nl.kbna.dioscuri.Emulator;
import nl.kbna.dioscuri.exception.ModuleException;
import nl.kbna.dioscuri.exception.ModuleUnknownPort;
import nl.kbna.dioscuri.exception.ModuleWriteOnlyPortException;
import nl.kbna.dioscuri.module.Module;
import nl.kbna.dioscuri.module.ModuleCPU;
import nl.kbna.dioscuri.module.ModuleClock;
import nl.kbna.dioscuri.module.ModuleDevice;
import nl.kbna.dioscuri.module.ModuleMemory;
import nl.kbna.dioscuri.module.ModuleMotherboard;

/**
 * An implementation of a motherboard module.
 * This module is responsible for allowing devices to communicate with the CPU and vice versa.
 *  
 * @see Module
 * 
 * Metadata module
 * ********************************************
 * general.type                : motherboard
 * general.name                : General x86 motherboard
 * general.architecture        : Von Neumann
 * general.description         : imitates an x86 motherboard including I/O address space, ...
 * general.creator             : Tessella Support Services, Koninklijke Bibliotheek, Nationaal Archief of the Netherlands
 * general.version             : 1.0
 * general.keywords            : motherboard, mainboard, ioports, ...
 * general.relations           : cpu, devices
 * general.yearOfIntroduction  : 
 * general.yearOfEnding        : 
 * general.ancestor            : 
 * general.successor           : 
 * motherboard.ioAddressSpaceSize : 65536 bytes
 * motherboard.architecture    : ISA, EISA, PCI
 * 
 * Rule:
 * This module should always be initialised before I/O devices.
 * 
 */
public class Motherboard extends ModuleMotherboard
{

	// Attributes
    
    // Relations
    private Emulator emu;
    private String[] moduleConnections = new String[] {"cpu", "memory"}; 
    private ModuleCPU cpu;
    private ModuleMemory memory;
    private Devices devices;       // Array of all peripheral devices
    private ModuleClock clock;      // Relation to internal clock (optional)
	
    // Toggles
    private boolean isObserved;
    private boolean debugMode;
    private boolean A20Enabled;   // A20 address line: True = memory wrapping turned off
    
    // Configuration parameters
    protected int ioSpaceSize;
    
    // I/O address space containing references to devices
    public ModuleDevice[] ioAddressSpace;   // Using signed bytes as both signed/unsigned

    // Logging
	private static Logger logger = Logger.getLogger(Motherboard.class.getPackage().getName());
	
    
	// Constants

	// Module specifics
	public final static int MODULE_ID		= 1;
	public final static String MODULE_TYPE	= "motherboard";
	public final static String MODULE_NAME	= "General x86 motherboard";

	// Memory size
	public final static int IOSPACE_ISA_SIZE    = 1024;		// ISA:  1024 (2^10) spaces, 0x000 - 0x3FF 
    public final static int IOSPACE_EISA_SIZE   = 65536;    // EISA: 65536 (2^16) spaces, 0x0000 - 0xFFFF
    
    public final static int SYSTEM_CONTROL_PORT_A = 0x92;   // A20 port, amongst others 

	/**
	 * Class constructor
	 * 
	 */
	public Motherboard(Emulator owner)
	{
        emu = owner;
        
        // Initialise variables
        isObserved = false;
        debugMode = false;
        
        // Initialise configuration parameters
        // TODO: parameters should be defined based on configuration in the ESD
        ioSpaceSize = IOSPACE_EISA_SIZE;
        
        // Create new empty list of devices
        devices = new Devices(20);
        
        // Initialize clock
        clock = null;
		
		// Create new empty I/O address space
		ioAddressSpace = new ModuleDevice[ioSpaceSize];
		
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
	 * @param mod	Module that is to be connected to this class
	 * 
	 * @return true if connection has been established successfully, false otherwise
	 * 
	 * @see Module
	 */
	public boolean setConnection(Module module)
	{
		// Set connection for CPU
        if (module.getType().equalsIgnoreCase("cpu"))
        {
            this.cpu = (ModuleCPU)module;
            return true;
        }
        // Set connection for memory
        else if (module.getType().equalsIgnoreCase("memory"))
        {
            this.memory = (ModuleMemory)module;
            return true;
        }
        // Set connection for clock
//        else if (module.getType().equalsIgnoreCase("clock"))
//        {
//            this.clock = (ModuleClock)module;
//            return true;
//        }
        
        // Else, module may be a device
        try
        {
            devices.addDevice((ModuleDevice)module);
            return true;
        }
        catch (ClassCastException e)
        {
            logger.log(Level.WARNING, "[" + MODULE_TYPE + "]" + " Failed to establish connection.");
            return false;
        }
	}
	

	/**
	 * Checks if this module is connected to operate normally
     * NOTE: not all devices are required, so they are not checked
	 * 
	 * @return true if this module is connected successfully, false otherwise
	 */
	public boolean isConnected()
	{
		// Check all connections
        if (this.cpu != null && this.memory != null)
        {
            return true;
        }
        
        // Else, one or more connections may be missing
        return false;
	}

	
    /**
     * Reset all parameters of module
     * 
     * @return boolean true if module has been reset successfully, false otherwise
     */
    public boolean reset()
    {
        // Reset I/O address space: set all ports to null
        for (int port = 0; port < ioAddressSpace.length; port++)
        {
            ioAddressSpace[port] = null;
        }

        // Disable memory wrapping for BIOS mem check
        A20Enabled = true;
        
        logger.log(Level.INFO, "[" + MODULE_TYPE + "]" + "  Module has been reset.");
        
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
        // Nothing to stop
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
        String dump = "";
        String ret = "\r\n";
        String tab = "\t";
        
        // Output I/O address space to dump
        dump = "Dump of I/O address space:" + ret;
        
        // To keep a limit on the string size, only display ports 0 - 0x3FF
        for (int port = 0; port < IOSPACE_ISA_SIZE; port++)
        {
            // Only print I/O space info when port is used
            if (ioAddressSpace[port] != null && !(ioAddressSpace[port].getType().equalsIgnoreCase("dummy")))
            {
                dump += "0x" + Integer.toHexString(0x10000 | port & 0x0FFFF).substring(1).toUpperCase() + tab + ": " + ioAddressSpace[port].getName() + " (" + ioAddressSpace[port].getType() + ")" + ret;
            }
        }
        return dump;
	}

	
	//******************************************************************************
	// ModuleMotherboard Methods
	
    /**
     * Registers a clock to motherboard
     * 
     * @return boolean true if registration is successfully, false otherwise
     */
    public boolean registerClock(ModuleClock clock)
    {
        this.clock = clock;
        return true;
    }

    
    /**
     * Requests a timer for given device at clock
     * 
     * @param ModuleDevice device that requests the timer
     * @param int updateInterval defining the frequency that update has to be done in microseconds
     * 
     * @return boolean true if registration is successfully, false otherwise
     */
    public boolean requestTimer(ModuleDevice device, int updateInterval, boolean continuous)
    {
        // Check if clock exists
        if (clock != null)
        {
            // Register device to clock (and assign timer to it)
            return clock.registerDevice(device, updateInterval, continuous);
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
    public boolean setTimerActiveState(ModuleDevice device, boolean activeState)
    {
        // Check if clock exists
        if (clock != null)
        {
            // Set device's timer to requested state
            return clock.setTimerActiveState(device, activeState);
        }
        return false;
    }
    
    /**
     * Resets the timer of device (if any)
     * 
     * @param ModuleDevice device that requests the timer reset
     * @param int updateInterval defining the frequency that update has to be done in microseconds
     * 
     * @return boolean true if reset is successfully, false otherwise
     */
    public boolean resetTimer(ModuleDevice device, int updateInterval)
    {
        // Check if clock exists
        if (clock != null)
        {
            return clock.resetTimer(device, updateInterval);
        }
        return false;
    }


    /**
     * Set I/O address port to given device
     * 
     * @param int portAddress containing the address of the I/O port
     * @param ModuleDevice device
     * 
     * @return boolean true if data is set successfully, false otherwise
     */
    public boolean setIOPort(int portAddress, ModuleDevice device)
    {
        // check if port is already in use
        if (ioAddressSpace[portAddress] == null)
        {
            ioAddressSpace[portAddress] = device;
            return true;
        }
        
        // Print warning
        //logger.log(Level.WARNING"[" + MODULE_TYPE + "]" + "  I/O address space: Registration for " + device.getType() + " failed. I/O port address already registered by " + ioAddressSpace[portAddress].getType());
        return false;
    }

    
    /**
     * Return I/O port data from I/O address space
     * 
     * @param int portAddress containing the address of the I/O port
     * 
     * @return byte containing the data at given I/O address port
     * @throws ModuleException
     */
    public byte getIOPortByte(int portAddress) throws ModuleException
	{
        // check if port is available
        if (ioAddressSpace[portAddress] != null)
        {
            try
            {
                // Return data at appropriate portnumber (could throw an exception in protected mode)
                return ioAddressSpace[portAddress].getIOPortByte(portAddress);
            }
            catch (ModuleUnknownPort e1)
            {
                // Print warning
                logger.log(Level.WARNING, "[" + MODULE_TYPE + "] Unknown I/O port requested (0x" + Integer.toHexString(portAddress).toUpperCase() + ").");
                throw new ModuleException("Unknown I/O port requested (0x" + Integer.toHexString(portAddress).toUpperCase() + ").");
            }
            catch (ModuleWriteOnlyPortException e2)
            {
                logger.log(Level.WARNING, "[" + MODULE_TYPE + "] Writing to I/O port not allowed.");
                throw new ModuleException("I/O port is read-only.");
            }
        } 
        logger.log(Level.WARNING, "[" + MODULE_TYPE + "] Requested I/O port 0x" + Integer.toHexString(portAddress) + " is not in use.");
        throw new ModuleException("Requested I/O port " + Integer.toHexString(portAddress) + " (byte) is not used.");
	}
    
    
    /**
     * Set a byte in I/O address space at given port
     * 
     * @param int portAddress containing the address of the I/O port
     * @param byte data
     * 
     * @throws ModuleException, ModuleWriteOnlyPortException
     */
    public void setIOPortByte(int portAddress, byte dataByte) throws ModuleException
    {
        // check if port is available
        if (ioAddressSpace[portAddress] != null)
        {
            try
            {
                // Set data at appropriate portnumber (could throw an exception in protected mode)
                ioAddressSpace[portAddress].setIOPortByte(portAddress, dataByte);
                
                
            }
            catch (ModuleUnknownPort e)
            {
                // Print warning
                logger.log(Level.WARNING, "[" + MODULE_TYPE + "]" + "  Unknown I/O port requested (0x" + Integer.toHexString(portAddress).toUpperCase() + ").");
                throw new ModuleException("Unknown I/O port requested (0x" + Integer.toHexString(portAddress).toUpperCase() + ").");
            }
        } 
        else 
        {
            throw new ModuleException("Requested I/O port (byte) is not available.");
        }
    }

    
    /**
     * Return a word from I/O address space at given port
     * 
     * @param int portAddress containing the address of the I/O port
     * 
     * @return byte[] containing the word at given I/O address port
     * @throws ModuleException
     */
    public byte[] getIOPortWord(int portAddress) throws ModuleException
    {
        // check if port range is available
        if (ioAddressSpace[portAddress] != null && ioAddressSpace[portAddress + 1] != null)
        {
            try
            {
                // Return data at appropriate portnumber (could throw an exception in protected mode)
                return ioAddressSpace[portAddress].getIOPortWord(portAddress);
            }
            catch (ModuleUnknownPort e1)
            {
                // Print warning
                logger.log(Level.WARNING, "[" + MODULE_TYPE + "] Unknown I/O port requested (0x" + Integer.toHexString(portAddress).toUpperCase() + ").");
                throw new ModuleException("Unknown I/O port requested (0x" + Integer.toHexString(portAddress).toUpperCase() + ").");
            }
            catch (ModuleWriteOnlyPortException e2)
            {
                logger.log(Level.WARNING, "[" + MODULE_TYPE + "] Writing to I/O port not allowed.");
                throw new ModuleException("I/O port is read-only.");
            }
        }
        logger.log(Level.WARNING, "[" + MODULE_TYPE + "] Requested I/O port 0x" + Integer.toHexString(portAddress) + " is not in use.");
        throw new ModuleException("Requested I/O port range (word) is not in use.");
    }

    
    /**
     * Set a word in I/O address space at given port
     * 
     * @param int portAddress containing the address of the I/O port
     * @param byte[] word
     * 
     * @throws ModuleException, ModuleWriteOnlyPortException
     */
    public void setIOPortWord(int portAddress, byte[] dataWord) throws ModuleException
    {
        // check if port range is available
        if (ioAddressSpace[portAddress] != null && ioAddressSpace[portAddress + 1] != null)
        {
            try
            {
                // Set data at appropriate portnumber (could throw an exception in protected mode)
                ioAddressSpace[portAddress].setIOPortWord(portAddress, dataWord);
            }
            catch (ModuleUnknownPort e)
            {
                // Print warning
                logger.log(Level.WARNING, "[" + MODULE_TYPE + "]" + "  Unknown I/O port requested (0x" + Integer.toHexString(portAddress).toUpperCase() + ").");
                throw new ModuleException("Unknown I/O port requested (0x" + Integer.toHexString(portAddress).toUpperCase() + ").");
            }
        } 
        else 
        {
            throw new ModuleException("Requested I/O port range (word) is not available.");
        }
    }

    
    /**
     * Return a double word from I/O address space at given port
     * 
     * @param int portAddress containing the address of the I/O port
     * 
     * @return byte[] containing the double word at given I/O address port
     * @throws ModuleException
     */
    public byte[] getIOPortDoubleWord(int portAddress) throws ModuleException
    {
        // check if port range is available
        if (ioAddressSpace[portAddress] != null && ioAddressSpace[portAddress + 1] != null && ioAddressSpace[portAddress + 2] != null && ioAddressSpace[portAddress + 3] != null)
        {
            try
            {
                // Return data at appropriate portnumber (could throw an exception in protected mode)
                return ioAddressSpace[portAddress].getIOPortDoubleWord(portAddress);
            }
            catch (ModuleUnknownPort e1)
            {
                // Print warning
                logger.log(Level.WARNING, "[" + MODULE_TYPE + "]" + "  Unknown I/O port requested (0x" + Integer.toHexString(portAddress).toUpperCase() + ").");
                throw new ModuleException("Unknown I/O port requested (0x" + Integer.toHexString(portAddress).toUpperCase() + ").");
            }
            catch (ModuleWriteOnlyPortException e2)
            {
                logger.log(Level.WARNING, "[" + MODULE_TYPE + "] Writing to I/O port not allowed.");
                throw new ModuleException("I/O port is read-only.");
            }
        }
        logger.log(Level.WARNING, "[" + MODULE_TYPE + "] Requested I/O port 0x" + Integer.toHexString(portAddress) + " is not in use.");
        throw new ModuleException("Requested I/O port range (double word) is not available.");
    }

    
    /**
     * Set a double word in I/O address space at given port
     * 
     * @param int portAddress containing the address of the I/O port
     * @param byte[] double word
     * 
     * @throws ModuleException, ModuleWriteOnlyPortException
     */
    public void setIOPortDoubleWord(int portAddress, byte[] dataDoubleWord) throws ModuleException
    {
        // check if port range is available
        if (ioAddressSpace[portAddress] != null && ioAddressSpace[portAddress + 1] != null && ioAddressSpace[portAddress + 2] != null && ioAddressSpace[portAddress + 3] != null)
        {
            try
            {
                // Set data at appropriate portnumber (could throw an exception in protected mode)
                ioAddressSpace[portAddress].setIOPortDoubleWord(portAddress, dataDoubleWord);
                
            }
            catch (ModuleUnknownPort e)
            {
                // Print warning
                logger.log(Level.WARNING, "[" + MODULE_TYPE + "]" + "  Unknown I/O port requested (0x" + Integer.toHexString(portAddress).toUpperCase() + ").");
                throw new ModuleException("Unknown I/O port requested (0x" + Integer.toHexString(portAddress).toUpperCase() + ").");
            }
        } 
        else 
        {
            throw new ModuleException("Requested I/O port range (double word) is not available.");
        }
        
    }

    
    /**
     * Return A20 address line status
     * 
     * @return boolean true if A20 is enabled, false otherwise
     */
    public boolean getA20()
    {
        // Return status of A20 address line
        return A20Enabled;
    }

    
    /**
     * Set A20 address line status
     * 
     * @param boolean true to enable A20, false otherwise
     */
    public void setA20(boolean a20)
    {
        // Set status of A20 address line
        // False = memory wrapping turned off
        A20Enabled = a20;
        memory.setA20AddressLine(a20);
    }

    
    /**
     * Retrieve current number of instruction (instructions executed so far)
     * 
     * @return long containing number of instructions
     * 
     */
    public long getCurrentInstructionNumber()
    {
        return cpu.getCurrentInstructionNumber();
    }
    
	//******************************************************************************
	// Custom Methods
    
}
