/*
 * $Revision: 1.1 $ $Date: 2007-07-02 14:31:28 $ $Author: blohman $
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

package nl.kbna.dioscuri.module.bios;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.kbna.dioscuri.Emulator;
import nl.kbna.dioscuri.exception.ModuleException;
import nl.kbna.dioscuri.module.Module;
import nl.kbna.dioscuri.module.ModuleBIOS;

/**
 * An implementation of a hardware BIOS module.
 *  
 * Contains:
 *  - 64 KB of ROM
 *  - CMOS settings
 * 
 * @see Module
 * 
 * Metadata module
 * ********************************************
 * general.type                : bios
 * general.name                : BIOS ROM
 * general.architecture        : Von Neumann
 * general.description         : General implementation of BIOS ROM chip.
 * general.creator             : Tessella Support Services, Koninklijke Bibliotheek, Nationaal Archief of the Netherlands
 * general.version             : 1.0
 * general.keywords            : bios, ROM, 64KB, 32KB, bootstrap, system bios, video bios, optional rom
 * general.relations           : cpu, memory
 * general.yearOfIntroduction  : 
 * general.yearOfEnding        : 
 * general.ancestor            : 
 * general.successor           : 
 * bios.romsize                : 64 + 32 KB
 * bios.settings               : CMOS
 * 
 * Notes: none
 * 
 */
public class BIOS extends ModuleBIOS
{

	// Attributes

    // Relations
    private Emulator emu;
    private String[] moduleConnections = new String[] {}; 

    // Toggles
    private boolean isObserved;
	private boolean debugMode;
    
    // BIOS ROM
	private byte[] systemROM;      // Contains the System BIOS, using signed bytes as both signed/unsigned
    private byte[] videoROM;         // Contains the Video BIOS
    private byte[] optionalROM;    // TODO: can contain optional BIOSes
    
	// Logging
	private static Logger logger = Logger.getLogger("nl.kbna.dioscuri.bios");
	
	// Constants

	// Module specifics
	public final static int MODULE_ID		= 1;
	public final static String MODULE_TYPE	= "bios";
	public final static String MODULE_NAME	= "BIOS ROM";

	// Memory size
	private final static int SYSTEMBIOS_ROM_SIZE = 65536;		// defined in bytes (64 KB, 2^16)
    private final static int VIDEOBIOS_ROM_SIZE = 32768;        // defined in bytes (32 KB, 2^15)
	
	
	/**
	 * Class constructor
	 * 
	 */
	public BIOS(Emulator owner)
	{
        emu = owner;
        
        // Initialise variables
        isObserved = false;
        debugMode = false;
		
		// Create new empty bios roms
		systemROM = new byte[SYSTEMBIOS_ROM_SIZE];
        videoROM = new byte[VIDEOBIOS_ROM_SIZE];
        
        // Set all rom to zero
        Arrays.fill(systemROM, (byte) 0);
        Arrays.fill(videoROM, (byte) 0);

        logger.log(Level.INFO, "[" + MODULE_TYPE + "] " + MODULE_NAME + " -> Module created successfully.");
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
        // No connections to return;
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
		// No connections required
		return false;
	}
	

	/**
	 * Checks if this module is connected to operate normally
	 * 
	 * @return true if this module is connected successfully, false otherwise
	 */
	public boolean isConnected()
	{
		// All connections are in place
		return true;
	}

	
    /**
     * Reset all parameters of module.
     * BIOS ROM is not reset. ROM will keep all BIOS ROM data.
     * 
     * @return boolean true if module has been reset successfully, false otherwise
     */
    public boolean reset()
    {
        // TODO: Reset particular CMOS settings

        logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] Module has been reset.");
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
     * @param byte[] containing data
     * @param Module sender, the sender of the data
     * 
     * @return true if data is set successfully, false otherwise
     * 
     * @see Module
     */
	public boolean setData(byte[] data, Module sender)
	{
		return false;
	}


    /**
     * Sets given String[] data for this module
     * 
     * @param String[] data
     * @param Module sender, the sender of the data
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
        String space = " ";
        
        // Output System BIOS:
        dump = "System BIOS dump of first 800 bytes as stored in ROM:" + ret;
        
        // Output first 800 ROM bytes to dump
        for (int row = 0; row < (50); row++)
        {
            dump += row + tab + ": ";
            for (int col = 0; col < 16; col++)
            {
                dump += Integer.toHexString( 0x100 | systemROM[(row * 16) + col] & 0xFF).substring(1).toUpperCase() + space;
            }
            dump += ret;
        }
        dump += ret;

        // Output Video BIOS:
        dump += "Video BIOS dump of first 800 bytes as stored in ROM:" + ret;
        
        // Output first 800 ROM bytes to dump
        for (int row = 0; row < (50); row++)
        {
            dump += row + tab + ": ";
            for (int col = 0; col < 16; col++)
            {
                dump += Integer.toHexString( 0x100 | videoROM[(row * 16) + col] & 0xFF).substring(1).toUpperCase() + space;
            }
            dump += ret;
        }
        
        return dump;
	}

	
	//******************************************************************************
	// ModuleBIOS Methods
	
    /**
     * Returns the system BIOS code from ROM with size of SYSTEM_BIOS_ROM_SIZE
     * 
     * @return byte[] biosCode containing the binary code of BIOS
     */
    public byte[] getSystemBIOS()
    {
        // Make a copy of ROM
        byte[] biosCode = new byte[SYSTEMBIOS_ROM_SIZE];
        
        // Copy each sequential byte from ROM starting at 0
        // Note: Intel Little-endian is not considered here
        for (int b = 0; b < SYSTEMBIOS_ROM_SIZE; b++)
        {
            biosCode[b] = systemROM[b];
        }
        return biosCode;
    }

    
    /**
	 * Sets the system BIOS code in ROM
     * Note: System BIOS must be exactly 64 KB
	 * 
	 * @param byte[] biosCode containing the binary code of BIOS
	 * 
	 * @return true if BIOS code is of specified SYSTEMBIOS_ROM_SIZE and store is successful, false otherwise
	 * @throws ModuleException
	 */
	public boolean setSystemBIOS(byte[] biosCode) throws ModuleException
	{
		// Check if BIOS code complies to 64 KB max
        if (biosCode.length == SYSTEMBIOS_ROM_SIZE)
        {
    		try
    		{
    			// Copy each sequential byte into ROM starting at 0
    			// Note: Intel Little-endian is not considered here
    			for (int b = 0; b < SYSTEMBIOS_ROM_SIZE; b++)
    			{
                    systemROM[b] = biosCode[b];
    			}
    			return true;
    		}
    		catch (ArrayIndexOutOfBoundsException e)
    		{
    			logger.log(Level.SEVERE, "[" + MODULE_TYPE + "]" + " System BIOS is larger than " + SYSTEMBIOS_ROM_SIZE + " bytes");
    			throw new ModuleException("[" + MODULE_TYPE + "]" + " System BIOS is larger than " + SYSTEMBIOS_ROM_SIZE + " bytes");
    		}
        }
        else
        {
            throw new ModuleException("[" + MODULE_TYPE + "]" + " System BIOS is not " + SYSTEMBIOS_ROM_SIZE + " bytes");
        }
	}

    
    /**
     * Returns the Video BIOS code from ROM
     * 
     * @return byte[] biosCode containing the binary code of Video BIOS
     */
    public byte[] getVideoBIOS()
    {
        // Make a copy of ROM
        byte[] biosCode = new byte[VIDEOBIOS_ROM_SIZE];
        
        // Copy each sequential byte from ROM starting at 0
        // Note: Intel Little-endian is not considered here
        for (int b = 0; b < VIDEOBIOS_ROM_SIZE; b++)
        {
            biosCode[b] = videoROM[b];
        }
        return biosCode;
    }

    
    /**
     * Sets the Video BIOS code in ROM
     * 
     * @param byte[] biosCode containing the binary code of Video BIOS
     * 
     * @return true if BIOS code is of specified VIDEOBIOS_ROM_SIZE and store is successful, false otherwise
     * @throws ModuleException
     */
    public boolean setVideoBIOS(byte[] biosCode) throws ModuleException
    {
        // Check if BIOS code complies to 32 KB max
        if (biosCode.length == VIDEOBIOS_ROM_SIZE)
        {
            try
            {
                // Copy each sequential byte into VIDEO ROM starting at 0
                // Note: Intel Little-endian is not considered here
                for (int b = 0; b < VIDEOBIOS_ROM_SIZE; b++)
                {
                    videoROM[b] = biosCode[b];
                }
                return true;
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                logger.log(Level.SEVERE, "[" + MODULE_TYPE + " Video BIOS is larger than " + SYSTEMBIOS_ROM_SIZE + " bytes");
                throw new ModuleException("[" + MODULE_TYPE + " Video BIOS is larger than " + SYSTEMBIOS_ROM_SIZE + " bytes");
            }
        }
        else
        {
            throw new ModuleException("[" + MODULE_TYPE + " Video BIOS is not " + SYSTEMBIOS_ROM_SIZE + " bytes");
        }
    }
    
	
	//******************************************************************************
	// Additional Methods

}
