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

package nl.kbna.dioscuri.interfaces;

/**
 * Interface representing a generic hardware module.
 *  
 */

public interface Module
{
	// Methods
	
	/**
	 * Returns the type of module (CPU, Memory, etc.)
	 * 
	 * @return string with the type of this module
	 * 
	 */
	public String getType();
	
	
	/**
	 * Returns the name of module
	 * 
	 * @return string with the name of this module
	 * 
	 */
	public String getName();

	
    /**
     * Returns a String[] with all names of modules it needs to be connected to
     * 
     * @return String[] containing the names of modules
     */
    public String[] getConnections();

    /**
	 * Sets up a connection with another module
	 * 
	 * @param mod	Module that is to be connected
	 * 
	 * @return true if connection was set successfully, false otherwise
	 */
	public boolean setConnection(Module mod);
	
	
	/**
	 * Checks if this module is connected to operate normally
	 * 
	 * @return true if this module is connected successfully, false otherwise
	 */
	public boolean isConnected();

	
	/**
	 * Returns configuration of this module
     * 
	 * @return byte[] with configuration
	 */
	public byte[] getConfig();

	
	/**
	 * Set configuration for this module
	 *
	 * @param byte[] containing data
	 * 
	 * @return true if configured successfully, false otherwise
	 */
	public boolean setConfig(byte[] data);

	
	/**
	 * Checks if this module is configured to operate normally
	 * 
	 * @return true if this module is configured successfully, false otherwise
	 */
	public boolean isConfigured();

	
    /**
     * Reset all parameters of module
     * 
     */
    public boolean reset();

    /**
	 * Starts the module to become active
	 * 
	 */
	public void start();

	
	/**
	 * Stops the module from being active
	 * 
	 */
	public void stop();

	
	/**
	 * Save the current state of module
	 * 
	 */
	public byte[] saveState();

	
	/**
	 * Load the given state in module
	 * 
	 */
	public boolean loadState(byte[] variables);

	
	/**
	 * Returns the state of debug mode
	 * 
	 * @return true if this module is in debug mode, false otherwise
	 */
	public boolean getDebugMode();

	
	/**
	 * Set toggle to define if this module is in debug mode or not
	 * 
	 * @param boolean to set the status of debug mode
	 */
	public void setDebugMode(boolean status);

	
	/**
	 * Return a dump of module status
	 * 
	 * @return string containing a dump of this module
	 */
	public abstract String getDump();

}
