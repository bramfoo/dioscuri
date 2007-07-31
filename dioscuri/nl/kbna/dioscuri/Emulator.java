/*
 * $Revision: 1.2 $ $Date: 2007-07-31 15:06:00 $ $Author: jrvanderhoeven $
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

package nl.kbna.dioscuri;

import java.awt.event.KeyEvent;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.kbna.dioscuri.config.ConfigController;
import nl.kbna.dioscuri.module.Module;
import nl.kbna.dioscuri.module.ModuleCPU;
import nl.kbna.dioscuri.module.ModuleFDC;
import nl.kbna.dioscuri.module.ModuleKeyboard;
import nl.kbna.dioscuri.module.cpu.CPU;
import nl.kbna.dioscuri.module.memory.Memory;


/**
 * Top class owning all classes of the emulator. Entry point
 *
 */
public class Emulator implements Runnable
{

	// Attributes
	private Modules modules;
    private IO io;
    private GUI gui;
    private ConfigController configController;
    
    // Toggles
    private boolean isAlive;
    private boolean coldStart;
    private boolean resetBusy;
    
	// Logging
	private static Logger logger = Logger.getLogger("nl.kbna.dioscuri");
    
    
	// Constants
	// General commands
	protected final static int CMD_START				= 0x00;		// start the emulator
	protected final static int CMD_STOP					= 0x01;		// stop the emulator
    protected final static int CMD_RESET                = 0x02;     // stop the emulator
	protected final static int CMD_DEBUG				= 0x04;		// turn on debug mode
	protected final static int CMD_LOGGING				= 0x05;		// turn on logging
	protected final static int CMD_OBSERVE				= 0x06;		// turn on observation
	protected final static int CMD_LOAD_MODULES			= 0x07;		// load modules
	protected final static int CMD_LOAD_DATA			= 0x08;		// load initial data (program code)
	protected final static int CMD_LOGTOFILE			= 0x09;		// write logging information to file
	
	// Debug commands
    protected final static int CMD_DEBUG_HELP           = 0x03;     // show help information
	protected final static int CMD_DEBUG_STEP			= 0x10;		// execute one processor cycle
	protected final static int CMD_DEBUG_DUMP			= 0x11;		// show dump of observed modules
	protected final static int CMD_DEBUG_ENTER			= 0x12;		// enter given values at given memory address
	protected final static int CMD_DEBUG_STOP			= 0x13;		// stop the emulator
    protected final static int CMD_DEBUG_SHOWREG        = 0x14;     // Simple CPU register view
    protected final static int CMD_DEBUG_MEM_DUMP       = 0x15;     // Show contents of memory location(s)
    
	// Special case commands
	protected final static int CMD_MISMATCH				= 0xFF;		// command mismatch
    
    // Module status
    public final static int MODULE_FDC_TRANSFER_START       = 0;
    public final static int MODULE_FDC_TRANSFER_STOP        = 1;
    public static final int MODULE_ATA_HD1_TRANSFER_START   = 2;
    public static final int MODULE_ATA_HD1_TRANSFER_STOP    = 3;
    public static final int MODULE_KEYBOARD_NUMLOCK_ON      = 4;
    public static final int MODULE_KEYBOARD_NUMLOCK_OFF     = 5;
    public static final int MODULE_KEYBOARD_CAPSLOCK_ON     = 6;
    public static final int MODULE_KEYBOARD_CAPSLOCK_OFF    = 7;
    public static final int MODULE_KEYBOARD_SCROLLLOCK_ON   = 8;
    public static final int MODULE_KEYBOARD_SCROLLLOCK_OFF  = 9;
    
    
	// Constructors

	/**
	 * Class constructor
     * 
     * @param GUI graphical user interface (owner of emulation process)
	 *
	 */
	public Emulator(GUI owner)
	{
        this.gui = owner;
        modules = null;
        
		// Create IO communication channel
		io = new IO();
		
		// Set isActive toggle on
		isAlive = true;
        coldStart = true;
        resetBusy = false;
        
        configController =  new ConfigController();
    }

	
	public void run()
	{
		// TODO: Perform semantic analysis of command (syntaxis is checked by IO class)
		// Especially important when running with multiple threads!!!
		
		while (isAlive == true)
		{
            // Start emulator, start all threads

            // Initialise modules
            boolean success = this.configController.initModules(this);
            
            if (!success)
            {
                logger.log(Level.SEVERE, "Emulation process halted due to error initalizing the modules.");
                this.stop();
                return;
            }

            logger.log(Level.INFO, "Emulation process started.");
            
            if (((ModuleCPU)modules.getModule("cpu")).getDebugMode() == false)
            {
                ((ModuleCPU)modules.getModule("cpu")).start();
                if (((ModuleCPU)modules.getModule("cpu")).isAbnormalTermination() == true)
                {
                    logger.log(Level.SEVERE, "Emulation process halted due to error in CPU module.");
                    this.stop();
                    return;
                }
            }
            else
            {
                // Show first upcoming instruction
                CPU cpu = (CPU)modules.getModule("cpu");
                logger.log(Level.INFO, cpu.getNextInstructionInfo());
                while (isAlive == true)
                {
                    this.debug(io.getCommand());
                }
            }

            // Wait until reset of modules is done (wait about 1 second...)
            // This can occur when another thread causes a reset of this emulation process
            while (resetBusy == true)
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
	}

    
    protected void stop()
    {
        // End life of this emulation process
        isAlive = false;

        // Check if emulation process exists
        if (modules != null)
        {
            // Stop emulation process, stop all threads
            for (int i = 0; i < modules.size(); i++)
            {
                modules.getModule(i).stop();
            }
            logger.log(Level.INFO, "Emulation process stopped.");
        }
    }

    
    protected void reset()
    {
        // TODO: fix this approach by avoiding deadlock (by using synchronized)
        // Check if emulation process exists
        resetBusy = true;
        
        if (modules != null)
        {
            // Reset emulation process
            ((ModuleCPU)modules.getModule("cpu")).stop();
            
            coldStart = false;
            logger.log(Level.INFO, "Reset in progress...");
        }
        resetBusy = false;
    }

    
    protected void debug(int command)
    {
        // Debug commands
        switch (command)
        {
            case CMD_DEBUG_HELP:
                // Debug command -> show help
                io.showHelp();
                break;
                
            case CMD_DEBUG_STEP:
                // Debug command STEP -> execute 1 or n instructions
                CPU cpu = (CPU)modules.getModule("cpu");
                
                // Execute n number of instructions (or else 1 if no argument supplied)
                String[] sNumber = io.getArguments();
                if (sNumber != null)
                {
                    int totalInstructions = Integer.parseInt(sNumber[0]);
                    for (int n = 0; n < totalInstructions; n++)
                    {
                        cpu.start();
                    }
                }
                else
                {
                    // Execute only 1 instruction
                    cpu.start();
                }
                
                // Show dump of next CPU instruction
                logger.log(Level.INFO, cpu.getNextInstructionInfo());
                break;
    
            case CMD_DEBUG_SHOWREG:
                // Debug command SHOWREG -> show CPU registers 
                CPU cpu1 = (CPU)modules.getModule("cpu");
    
                // Show simple view of CPU registers and flags
                logger.log(Level.INFO, cpu1.dumpRegisters());
                break;
                
            case CMD_DEBUG_DUMP:
                // Debug command DUMP -> show dump of module on screen
                
                // Check if moduletype is given
                String[] moduleTypeArray = io.getArguments();
                if (moduleTypeArray != null)
                {
                    String moduleType = moduleTypeArray[0];
                    
                    // Show dump of one single module requested
                    Module mod = modules.getModule(moduleType);
                    if (mod != null)
                    {
                        // Show dump of module
                        logger.log(Level.INFO, mod.getDump());
                    }
                    else
                    {
                        logger.log(Level.WARNING, "Module not recognised.");
                    }
                }
                break;
    
            case CMD_DEBUG_MEM_DUMP:
                // Debug command MEMORY_DUMP -> show contents of n bytes at memory location y
                Memory mem = (Memory)modules.getModule("memory");
                
                // Show n bytes at memory x (or else 2 if no number of bytes is supplied)
                String[] sArg = io.getArguments();
                int memAddress = Integer.parseInt(sArg[0]);
                int numBytes = 0;
                if (sArg.length == 2)
                {
                    numBytes = Integer.parseInt(sArg[1]);
                }
                else
                {
                    // Retrieve only 2 byte
                    numBytes = 2;
                    }
                
                for (int n = 0; n < numBytes; n++)
                {
                        logger.log(Level.INFO, "Value of [0x" + Integer.toHexString(memAddress + n).toUpperCase() + "]: 0x" + Integer.toHexString( 0x100 | mem.getByte(memAddress + n) & 0xFF).substring(1).toUpperCase());
                }
                break;
    
            default:
                // No command match
                logger.log(Level.WARNING, "No command match. Enter a correct emulator command.");
                break;
        }
    }

    
    /**
     * Method setRunning.
     * 
     * @param boolean setRunning
     */
    protected void setActive(boolean state)
    {
        // Define running state
        isAlive = state;
    }
    
    /**
     * Get the modules.
     * 
     * @return modules
     */
    public Modules getModules()
    {
        return this.modules;
    }
  
    /**
     * Set the modules.
     * @param modules
     */
    public void setModules(Modules modules)
    {
        this.modules = modules;
    }
    
    /**
     * Get the gui.
     * 
     * @return gui
     */
    public GUI getGui()
    {
        return this.gui;
    }
  
    /**
     * Get the io.
     * 
     * @return io
     */
    public IO getIo()
    {
        return this.io;
    }
    
    /**
     * Get cold start.
     * 
     * @return coldStart
     */
    public boolean getColdStart()
    {
        return this.coldStart;
    }
    
    /**
     * Set cold start.
     * 
     * @return coldStart
     */
    public void setColdStart(boolean coldStart)
    {
        this.coldStart = coldStart;
    }
    
    
    /**
     * Return reference to module from given type
     * 
     * @param String moduleType stating the type of the requested module
     * 
     * @return Module requested module, or null if module does not exist
     */
    protected Module getModule(String moduleType)
    {
        return modules.getModule(moduleType);
    }

    
    protected void generateScancode(KeyEvent keyEvent, int keyEventType)
    {
        ModuleKeyboard keyboard = (ModuleKeyboard) modules.getModule("keyboard");
        if (keyboard != null)
        {
            keyboard.generateScancode(keyEvent , keyEventType);
        }
    }

    
    protected boolean insertFloppy(String driveLetter, byte carrierType, File imageFile, boolean writeProtected)
    {
        ModuleFDC fdc = (ModuleFDC) modules.getModule("fdc");
        if (fdc != null)
        {
            return fdc.insertCarrier(driveLetter, carrierType, imageFile, writeProtected);
        }
        return false;
    }
    
    
    protected boolean ejectFloppy(String driveLetter)
    {
        ModuleFDC fdc = (ModuleFDC) modules.getModule("fdc");
        if (fdc != null)
        {
            return fdc.ejectCarrier(driveLetter);
        }
        return false;
    }

    
    public void statusChanged(int status)
    {
        switch(status)
        {
            case MODULE_FDC_TRANSFER_START:
                gui.updateGUI(GUI.EMU_FLOPPYA_TRANSFER_START);
                break;
            
            case MODULE_FDC_TRANSFER_STOP:
                gui.updateGUI(GUI.EMU_FLOPPYA_TRANSFER_STOP);
                break;
            
            case MODULE_ATA_HD1_TRANSFER_START:
                gui.updateGUI(GUI.EMU_HD1_TRANSFER_START);
                break;
            
            case MODULE_ATA_HD1_TRANSFER_STOP:
                gui.updateGUI(GUI.EMU_HD1_TRANSFER_STOP);
                break;

            case MODULE_KEYBOARD_NUMLOCK_ON:
                gui.updateGUI(GUI.EMU_KEYBOARD_NUMLOCK_ON);
                break;
            
            case MODULE_KEYBOARD_NUMLOCK_OFF:
                gui.updateGUI(GUI.EMU_KEYBOARD_NUMLOCK_OFF);
                break;

            default:
                    break;
        }
    }
}
