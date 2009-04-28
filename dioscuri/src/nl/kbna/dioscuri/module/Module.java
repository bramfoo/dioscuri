/*
 * $Revision: 1.1 $ $Date: 2007-07-02 14:31:26 $ $Author: blohman $
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

/**
 * Interface representing a generic hardware module.
 *  
 */

public abstract class Module
{
	// General module variables
	// String moduleDataString;
	// int moduleDataInt;

	// Methods
	
	/**
	 * Returns the ID of module (integer value)
	 * 
	 * @return integer with the ID of this module
	 * 
	 */
	public abstract int getID();
	
	
	/**
	 * Returns the type of module (CPU, Memory, etc.)
	 * 
	 * @return string with the type of this module
	 * 
	 */
	public abstract String getType();
	
	
	/**
	 * Returns the name of module
	 * 
	 * @return string with the name of this module
	 * 
	 */
	public abstract String getName();

	
    /**
     * Returns a String[] with all names of modules it needs to be connected to
     * 
     * @return String[] containing the names of modules
     */
    public abstract String[] getConnection();

    /**
	 * Sets up a connection with another module
	 * 
	 * @param mod	Module that is to be connected
	 * 
	 * @return true if connection was set successfully, false otherwise
	 */
	public abstract boolean setConnection(Module mod);
	
	
	/**
	 * Checks if this module is connected to operate normally
	 * 
	 * @return true if this module is connected successfully, false otherwise
	 */
	public abstract boolean isConnected();

	
    /**
     * Reset all parameters of module
     * 
     */
    public abstract boolean reset();

    /**
	 * Starts the module to become active
	 * 
	 */
	public abstract void start();

	
	/**
	 * Stops the module from being active
	 * 
	 */
	public abstract void stop();

	
	/**
	 * Returns the state of observed
	 * 
	 * @return true if this module is observed, false otherwise
	 */
	public abstract boolean isObserved();

	
	/**
	 * Set toggle to define if this module is observed or not
	 * 
	 * @param boolean to set the observation on true or false
	 */
	public abstract void setObserved(boolean status);

	
	/**
	 * Returns the state of debug mode
	 * 
	 * @return true if this module is in debug mode, false otherwise
	 */
	public abstract boolean getDebugMode();

	
	/**
	 * Set toggle to define if this module is in debug mode or not
	 * 
	 * @param boolean to set the status of debug mode
	 */
	public abstract void setDebugMode(boolean status);

	
	/**
	 * Returns data from this module
     * 
     * @param Module module, the requester of the data
	 *
	 * @return byte[] with data
	 */
	public abstract byte[] getData(Module module);

	
	/**
	 * Set data for this module
	 *
	 * @param byte[] containing data
     * @param Module module, the sender of the data
	 * 
	 * @return true if data is set successfully, false otherwise
	 */
	public abstract boolean setData(byte[] data, Module module);

	
	/**
	 * Set data for this module
	 *
	 * @param String[] containing data
     * @param Module module, the sender of the data
	 * 
	 * @return true if data is set successfully, false otherwise
	 */
	public abstract boolean setData(String[] data, Module module);

	
	/**
	 * Return a dump of module status
	 * 
	 * @return string containing a dump of this module
	 */
	public abstract String getDump();

}
