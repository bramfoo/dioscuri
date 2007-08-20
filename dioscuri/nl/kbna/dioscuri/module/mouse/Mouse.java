/*
 * $Revision: 1.1 $ $Date: 2007-08-20 15:18:47 $ $Author: jrvanderhoeven $
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

package nl.kbna.dioscuri.module.mouse;

import java.util.logging.Level;
import java.util.logging.Logger;

import nl.kbna.dioscuri.Emulator;
import nl.kbna.dioscuri.exception.ModuleException;
import nl.kbna.dioscuri.exception.ModuleUnknownPort;
import nl.kbna.dioscuri.exception.ModuleWriteOnlyPortException;
import nl.kbna.dioscuri.module.Module;
import nl.kbna.dioscuri.module.ModuleKeyboard;
import nl.kbna.dioscuri.module.ModuleMotherboard;
import nl.kbna.dioscuri.module.ModuleMouse;
import nl.kbna.dioscuri.module.ModulePIC;

/**
 * An implementation of a mouse module.
 *  
 * @see Module
 * 
 * Metadata module
 * ********************************************
 * general.type                : mouse
 * general.name                : PS2 compatible Mouse
 * general.architecture        : Von Neumann
 * general.description         : Models a PS2 compatible mouse 
 * general.creator             : Koninklijke Bibliotheek, Nationaal Archief of the Netherlands
 * general.version             : 1.0
 * general.keywords            : Mouse, Keyboard, PS/2
 * general.relations           : Motherboard, Keyboard
 * general.yearOfIntroduction  : 
 * general.yearOfEnding        : 
 * general.ancestor            : 
 * general.successor           : 
 * 
 * Notes:
 * - mouse uses keyboard controller. Therefore, a keyboard controller is always required when using this mouse.
 * - all controller aspects are implemented in keyboard, so mouse is only pointing device but not controller.
 * 
 */

public class Mouse extends ModuleMouse
{
    // Relations
    private Emulator emu;
    private String[] moduleConnections = new String[] {"motherboard", "keyboard"}; 
    private ModuleMotherboard motherboard;
    private ModuleKeyboard keyboard;
    private MouseBuffer buffer;

    // Toggles
    private boolean isObserved;
    private boolean debugMode;
    
    // Timing
    private int updateInterval;
    
    // Variables
    boolean mouseEnabled;					// Defines if this mouse if enabled
    int mouseType;							// Defines the type of mouse (PS/2, ...)
    int mouseMode;							// Defines the mode of mouse (wrap, stream, remote, ...) (default=stream)
    int mousePreviousMode;					// Remembers the previous mode (only set when going into wrap mode)
    byte lastMouseCommand;					// Remembers the last mouse command
    boolean expectingMouseParameter;		// Denotes if mouse expects another mouse parameter
    int imRequest;							// Wheel mouse mode request
    boolean imMode;							// Wheel mouse mode
    byte sampleRate;						// Defines the sample rate of the mouse (default=100)
    int resolutionCpmm;						// Defines the resolution of the mouse (default=4)
    int scaling;							// Defines the scaling of the mouse (default=1 (1:1))
    int delayed_dx      = 0;
    int delayed_dy      = 0;
    int delayed_dz      = 0;
    byte buttonStatus;
    
    // Logging
    private static Logger logger = Logger.getLogger("nl.kbna.dioscuri.module.mouse");
    
    
    // Constants
    
    // Module specifics
    public final static int MODULE_ID       = 1;
    public final static String MODULE_TYPE  = "mouse";
    public final static String MODULE_NAME  = "PS/2 compatible mouse";
    
    // Mouse type
    private final static int MOUSE_TYPE_PS2 		= 1;			// PS/2 mouse
    private final static int MOUSE_TYPE_IMPS2		= 2;			// PS/2 wheel mouse
    
    // Mouse mode
    private final static int MOUSE_MODE_WRAP 		= 1;
    private final static int MOUSE_MODE_STREAM		= 2;
    private final static int MOUSE_MODE_REMOTE		= 3;
    private final static int MOUSE_MODE_RESET		= 4;
    
    // Mouse commands
    private final static byte MOUSE_CMD_ACK			= (byte) 0xFA;
    private final static byte MOUSE_CMD_COMPLETION	= (byte) 0xAA;	// Completion code
    private final static byte MOUSE_CMD_ID			= (byte) 0x00;	// ID code
    private final static byte MOUSE_CMD_RESEND		= (byte) 0xFE;	// Also a NACK
    
    // Mouse buffer capacity
    private final static int MOUSE_BUFFER_INITSIZE	= 16;
    
    
    // Constructor

    /**
     * Class constructor
     * 
     */
    public Mouse(Emulator owner)
    {
        emu = owner;
        
        // Create mouse buffer
        buffer = new MouseBuffer(MOUSE_BUFFER_INITSIZE);
        
        // Initialise variables
        isObserved = false;
        debugMode = false;
        
        // Initialise timing
        updateInterval = -1;
        
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
        // Set connection for pic
        else if (mod.getType().equalsIgnoreCase("keyboard"))
        {
            this.keyboard = (ModuleKeyboard)mod;
            this.keyboard.setConnection(this);	// Set connection to keyboard
            return true;
        }
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
        if (motherboard != null&& keyboard != null)
        {
            return true;
        }
        return false;
    }


    /**
     * Default inherited reset. Calls specific reset(int)
     * 
     * @return boolean true if module has been reset successfully, false otherwise
     */
    public boolean reset()
    {
    	// Reset variables
    	// TODO: add all of them
    	mouseType = 0;
    	lastMouseCommand = 0;
    	
        // Request a timer
        if (motherboard.requestTimer(this, updateInterval, true) == false)
        {
            return false;
        }
        motherboard.setTimerActiveState(this, true);
        
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
        String keyboardDump ="Mouse status:\n";
       
        return keyboardDump;
    }


    //******************************************************************************
    // ModuleDevice Methods
    
    /**
     * Retrieve the interval between subsequent updates
     * 
     * @return int interval in microseconds
     */
    public int getUpdateInterval()
    {
        return updateInterval;
    }

    /**
     * Defines the interval between subsequent updates
     * 
     * @param int interval in microseconds
     */
    public void setUpdateInterval(int interval)
    {
        // Check if interval is > 0
        if (interval > 0)
        {
            updateInterval = interval;
        }
        else
        {
            updateInterval = 200; // default is 200 ms
        }
        
        // Notify motherboard that interval has changed
        // (only if motherboard contains a clock, which may not be the case at startup, but may be during execution)
        motherboard.resetTimer(this, updateInterval);
    }


    /**
     * Update device
     */
    public void update()
    {
    }


	@Override
	public byte getIOPortByte(int portAddress) throws ModuleException, ModuleUnknownPort, ModuleWriteOnlyPortException {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void setIOPortByte(int portAddress, byte data) throws ModuleException, ModuleUnknownPort {
		// TODO Auto-generated method stub
		
	}


	@Override
	public byte[] getIOPortWord(int portAddress) throws ModuleException, ModuleUnknownPort, ModuleWriteOnlyPortException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setIOPortWord(int portAddress, byte[] dataWord) throws ModuleException, ModuleUnknownPort {
		// TODO Auto-generated method stub
		
	}


	@Override
	public byte[] getIOPortDoubleWord(int portAddress) throws ModuleException, ModuleUnknownPort, ModuleWriteOnlyPortException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setIOPortDoubleWord(int portAddress, byte[] dataDoubleWord) throws ModuleException, ModuleUnknownPort {
		// TODO Auto-generated method stub
		
	}

    
	
    //******************************************************************************
    // ModuleMouse Methods
	
	public boolean isBufferEmpty()
	{
		return buffer.isEmpty();
	}
    
    public void prepareBufferData(boolean force_enq)
    {
    	byte b1, b2, b3, b4, buttonState;
    	int delta_x, delta_y;

    	if(buffer.isEmpty() && !force_enq)
    	{
			// Mouse has nothing in buffer or value is 0
			return;
    	}

    	delta_x = delayed_dx;
    	delta_y = delayed_dy;

    	if(!force_enq && delta_x == 0 && delta_y == 0)
    	{
    		// No mouse movement, so no changes
    		return;
    	}

    	// Limit values
    	if(delta_x > 254) delta_x = 254;
    	if(delta_x < -254) delta_x = -254;
    	if(delta_y > 254) delta_y = 254;
    	if(delta_y < -254) delta_y = -254;

    	// Set bytes
    	// Byte b1
    	b1 = (byte) ((buttonStatus & 0x0F) | 0x08); // bit3 always set

    	// Byte b2
    	if ((delta_x >= 0) && (delta_x <= 255))
    	{
    		b2 = (byte) delta_x;
    		delayed_dx -= delta_x;
    	}
    	else if (delta_x > 255)
    	{
    		b2 = (byte) 0xFF;
    		delayed_dx -= 255;
    	}
    	else if (delta_x >= -256)
    	{
	        b2 = (byte) delta_x;
	        b1 |= 0x10;
	        delayed_dx -= delta_x;
    	}
    	else
    	{
    		b2 = (byte) 0x00;
    		b1 |= 0x10;
    		delayed_dx += 256;
    	}

    	// Byte b3
    	if ((delta_y >= 0) && (delta_y <= 255))
    	{
    		b3 = (byte) delta_y;
    		delayed_dy -= delta_y;
    	}
    	else if (delta_y > 255)
    	{
	        b3 = (byte) 0xFF;
	        delayed_dy -= 255;
    	}
    	else if ( delta_y >= -256 )
    	{
    		b3 = (byte) delta_y;
    		b1 |= 0x20;
    		delayed_dy -= delta_y;
    	}
    	else
    	{
    		b3 = (byte) 0x00;
    		b1 |= 0x20;
    		delayed_dy += 256;
    	}

    	// Byte b4
    	b4 = (byte) -delayed_dz;

//    	this.enqueueData(b1, b2, b3, b4);
    }

	public byte getDataFromBuffer()
	{
		return ((Byte) buffer.remove(0)).byteValue();
	}
    
    private boolean mouse_enQ_packet(byte b1, byte b2, byte b3, byte b4)
    {
    	return true;
    }
/*    private boolean mouse_enQ_packet(Bit8u b1, Bit8u b2, Bit8u b3, Bit8u b4)
    {
    	int bytes = 3;
    	if (BX_KEY_THIS s.mouse.im_mode)
    	{
    	  	bytes = 4;
    	}

    	if ((mouse_internal_buffer.num_elements + bytes) >= BX_MOUSE_BUFF_SIZE)
    	{
    		return false; // buffer doesn't have the space
    	}

    	mouse_enQ(b1);
    	mouse_enQ(b2);
    	mouse_enQ(b3);
      
    	if (mouse.im_mode)
    	{
    		mouse_enQ(b4);
    	}

    	return true;
    }

    private void mouse_enQ(Bit8u mouse_data)
    {
      int tail;

      BX_DEBUG(("mouse_enQ(%02x)", (unsigned) mouse_data));

      if (BX_KEY_THIS s.mouse_internal_buffer.num_elements >= BX_MOUSE_BUFF_SIZE) {
        BX_ERROR(("[mouse] internal mouse buffer full, ignoring mouse data.(%02x)",
          (unsigned) mouse_data));
        return;
      }

      // enqueue mouse data in multibyte internal mouse buffer
      tail = (BX_KEY_THIS s.mouse_internal_buffer.head + BX_KEY_THIS s.mouse_internal_buffer.num_elements) %
       BX_MOUSE_BUFF_SIZE;
      BX_KEY_THIS s.mouse_internal_buffer.buffer[tail] = mouse_data;
      BX_KEY_THIS s.mouse_internal_buffer.num_elements++;

      if (!BX_KEY_THIS s.kbd_controller.outb && BX_KEY_THIS s.kbd_controller.aux_clock_enabled) {
        activate_timer();
        return;
      }
    }
*/
    
    public void controlMouse(byte value)
	{
		// if we are not using a ps2 mouse, some of the following commands need to return different values
		boolean isMousePS2;
		isMousePS2 = false;
		if ((mouseType == MOUSE_TYPE_PS2) || (mouseType == MOUSE_TYPE_IMPS2))
		{
			isMousePS2 = true;
		}

		logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] kbd_ctrl_to_mouse " + value);
//	  BX_DEBUG(("  enable = %u", (unsigned) BX_KEY_THIS s.mouse.enable));
//	  BX_DEBUG(("  allow_irq12 = %u", (unsigned) BX_KEY_THIS s.kbd_controller.allow_irq12));
//	  BX_DEBUG(("  aux_clock_enabled = %u", (unsigned) BX_KEY_THIS s.kbd_controller.aux_clock_enabled));

		// An ACK (0xFA) is always the first response to any valid input
		// received from the system other than Set-Wrap-Mode & Resend-Command

		// Check if this is the second mouse command (expected)
		if (expectingMouseParameter == true)
		{
			// Reset command parameter
			expectingMouseParameter = false;
			
			// Execute command
			switch (lastMouseCommand)
			{
				case (byte)0xF3: // Set Mouse Sample Rate
					sampleRate = value;
					logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] Sampling rate set to " + value);
					
					if ((value == 200) && (imRequest == 0))
					{
						imRequest = 1;
					}
					else if ((value == 100) && (imRequest == 1))
					{
						imRequest = 2;
					}
					else if ((value == 80) && (imRequest == 2))
					{
						// Check if wheel mouse should be enabled
						if (mouseType == MOUSE_TYPE_IMPS2)
						{
							logger.log(Level.INFO, "[" + MODULE_TYPE + "] Wheel mouse mode enabled");
							imMode = true;
						}
						else
						{
							logger.log(Level.INFO, "[" + MODULE_TYPE + "] Wheel mouse mode request rejected");
						}
						
						imRequest = 0;
					}
					else
					{
						imRequest = 0;
					}
					
					keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ack
					break;

				case (byte)0xE8: // Set Mouse Resolution
					switch (value)
					{
						case 0:
							resolutionCpmm = 1;
							break;
						case 1:
							resolutionCpmm = 2;
							break;
						case 2:
							resolutionCpmm = 4;
							break;
						case 3:
							resolutionCpmm = 8;
							break;
						default:
							logger.log(Level.WARNING, "[" + MODULE_TYPE + "] Unknown resolution");
						break;
					}
					logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] Resolution set to " + resolutionCpmm + " counts per mm");
	
					keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ack
					break;

				default:
					logger.log(Level.WARNING, "[" + MODULE_TYPE + "] unknown last command " + lastMouseCommand);
			}
		}
		else
		{
			// This is the first mouse command
			expectingMouseParameter = false;
			lastMouseCommand = value;

			// Check wrap mode
			if (mouseMode == MOUSE_MODE_WRAP)
			{
				// if not a reset command or reset wrap mode
				// then just echo the byte.
				if ((value != 0xFF) && (value != 0xEC))
				{
					logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] Wrap mode: ignoring command " + value);
					keyboard.enqueueControllerBuffer(value,1);
	        		return;
				}
			}
			
			switch (value)
			{

				case (byte)0xBB: // OS/2 Warp 3 uses this command
					logger.log(Level.WARNING, "[" + MODULE_TYPE + "] ignoring 0xBB command");
					break;
	
				case (byte)0xE6: // Set Mouse Scaling to 1:1
					keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
					scaling = 2;
					logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] Scaling set to 1:1");
					break;

				case (byte)0xE7: // Set Mouse Scaling to 2:1
					keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
					scaling = 2;
					logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] Scaling set to 2:1");
					break;

				case (byte)0xE8: // Set Mouse Resolution
					keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
					expectingMouseParameter = true;
					break;

				case (byte)0xE9: // Get mouse information
					keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
					keyboard.enqueueControllerBuffer(this.getStatusByte(), 1); // status
					keyboard.enqueueControllerBuffer(this.getResolutionByte(), 1); // resolution
					keyboard.enqueueControllerBuffer(sampleRate, 1); // sample rate
					logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] Mouse information returned");
					break;
	
				case (byte)0xEA: // Set Stream Mode
					mouseMode = MOUSE_MODE_STREAM;
					keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
					logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] Stream mode on");
					break;

				case (byte)0xEB: // Read Data (send a packet when in Remote Mode)
					keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
					// TODO: perhaps we should be adding some movement here.
					mouse_enQ_packet((byte)((buttonStatus & 0x0F) | 0x08), (byte)0x00, (byte)0x00, (byte)0x00); // bit3 of first byte always set
					// FIXME: assumed we really aren't in polling mode, a rather odd assumption.
					logger.log(Level.WARNING, "[" + MODULE_TYPE + "] Read Data command partially supported");
					break;
	
				case (byte)0xEC: // Reset Wrap Mode
					// Check if mouse is in wrap mode, else ignore command
					if (mouseMode == MOUSE_MODE_WRAP)
					{
	            		// Restore previous mode except disable stream mode reporting.
	            		// TODO disabling reporting in stream mode
	            		mouseMode = mousePreviousMode;
						keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
						logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] Wrap mode off. Set to previous mode");
					}
					break;
					
				case (byte)0xEE: // Set Wrap Mode
			        // TODO flush output queue.
			        // TODO disable interrupts if in stream mode.
					mousePreviousMode = mouseMode;
					mouseMode = MOUSE_MODE_WRAP;
					keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
					logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] Wrap mode on");
					break;

				case (byte)0xF0: // Set Remote Mode (polling mode, i.e. not stream mode.)
					// TODO should we flush/discard/ignore any already queued packets?
					mouseMode = MOUSE_MODE_REMOTE;
					keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
					logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] Remote mode on");
	          		break;

				case (byte)0xF2: // Read Device Type
					keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
					if (imMode == true)
					{
						keyboard.enqueueControllerBuffer((byte)0x03, 1); // Device ID (wheel z-mouse)
					}
					else
					{
						keyboard.enqueueControllerBuffer((byte)0x00, 1); // Device ID (standard)
					}
					logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] Read mouse ID");
					break;

				case (byte)0xF3: // Enable set Mouse Sample Rate (sample rate written to port 0x60)
					keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
					expectingMouseParameter = true;
					break;

				case (byte)0xF4: // Enable (in stream mode)
					// is a mouse present?
					if (isMousePS2)
					{
						mouseEnabled = true;
						keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
						logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] Mouse enabled (stream mode)");
					}
					else
					{
						// No mouse present.  A 0xFE (resend) need to be returned instead of a 0xFA (ACK)
						keyboard.enqueueControllerBuffer(MOUSE_CMD_RESEND, 1); // ACK
						keyboard.setTimeOut((byte)1);
					}
					break;

				case (byte)0xF5: // Disable (in stream mode)
					mouseEnabled = false;
					keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
					logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] Mouse disabled (stream mode)");
					break;

				case (byte)0xF6: // Set mouse to defaults
			        sampleRate = 100; // reports per second (default)
			        resolutionCpmm = 4; // 4 counts per millimeter (default)
			        scaling = 1; // 1:1 (default)
			        mouseEnabled = false;
			        mouseMode = MOUSE_MODE_STREAM;
					keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
					logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] Mouse set to default settings");
			        break;

				case (byte)0xFF: // Reset
					// Check if a mouse is present
					if (isMousePS2)
					{
				        sampleRate = 100; // reports per second (default)
				        resolutionCpmm = 4; // 4 counts per millimeter (default)
				        scaling = 1; // 1:1 (default)
				        mouseEnabled = false;
				        mouseMode = MOUSE_MODE_RESET;
						if (imMode == true)
						{
							logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] Wheel mouse mode disabled");
						}
						imMode = false;
						
						// TODO: NT expects an ack here
						keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
						keyboard.enqueueControllerBuffer(MOUSE_CMD_COMPLETION, 1); // COMPLETION
						keyboard.enqueueControllerBuffer(MOUSE_CMD_ID, 1); // ID
						logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] Mouse has been reset");
					}
					else
					{
						// No mouse present.  A 0xFE (resend) need to be returned instead of a 0xFA (ACK)
						keyboard.enqueueControllerBuffer(MOUSE_CMD_RESEND, 1); // RESEND
						keyboard.setTimeOut((byte)1);
					}
					break;

				default:
					// If PS/2 mouse present, send NACK for unknown commands, otherwise ignore
					if (isMousePS2)
					{
						logger.log(Level.WARNING, "[" + MODULE_TYPE + "] kbd_ctrl_to_mouse(): no command match");
						keyboard.enqueueControllerBuffer(MOUSE_CMD_RESEND, 1); // RESEND/NACK
					}
			}
		}
	}


/*	void bx_keyb_c::create_mouse_packet(bool force_enq)
	{
	  Bit8u b1, b2, b3, b4;

	  if(BX_KEY_THIS s.mouse_internal_buffer.num_elements && !force_enq)
	    return;

	  Bit16s delta_x = BX_KEY_THIS s.mouse.delayed_dx;
	  Bit16s delta_y = BX_KEY_THIS s.mouse.delayed_dy;
	  Bit8u button_state=BX_KEY_THIS s.mouse.button_status | 0x08;

	  if(!force_enq && !delta_x && !delta_y) {
	    return;
	  }

	  if(delta_x>254) delta_x=254;
	  if(delta_x<-254) delta_x=-254;
	  if(delta_y>254) delta_y=254;
	  if(delta_y<-254) delta_y=-254;

	  b1 = (button_state & 0x0f) | 0x08; // bit3 always set

	  if ( (delta_x>=0) && (delta_x<=255) ) {
	    b2 = (Bit8u) delta_x;
	    BX_KEY_THIS s.mouse.delayed_dx-=delta_x;
	  }
	  else if ( delta_x > 255 ) {
	    b2 = (Bit8u) 0xff;
	    BX_KEY_THIS s.mouse.delayed_dx-=255;
	  }
	  else if ( delta_x >= -256 ) {
	    b2 = (Bit8u) delta_x;
	    b1 |= 0x10;
	    BX_KEY_THIS s.mouse.delayed_dx-=delta_x;
	  }
	  else {
	    b2 = (Bit8u) 0x00;
	    b1 |= 0x10;
	    BX_KEY_THIS s.mouse.delayed_dx+=256;
	  }

	  if ( (delta_y>=0) && (delta_y<=255) ) {
	    b3 = (Bit8u) delta_y;
	    BX_KEY_THIS s.mouse.delayed_dy-=delta_y;
	  }
	  else if ( delta_y > 255 ) {
	    b3 = (Bit8u) 0xff;
	    BX_KEY_THIS s.mouse.delayed_dy-=255;
	  }
	  else if ( delta_y >= -256 ) {
	    b3 = (Bit8u) delta_y;
	    b1 |= 0x20;
	    BX_KEY_THIS s.mouse.delayed_dy-=delta_y;
	  }
	  else {
	    b3 = (Bit8u) 0x00;
	    b1 |= 0x20;
	    BX_KEY_THIS s.mouse.delayed_dy+=256;
	  }

	  b4 = (Bit8u) -BX_KEY_THIS s.mouse.delayed_dz;

	  mouse_enQ_packet(b1, b2, b3, b4);
	}


	void bx_keyb_c::mouse_enabled_changed(bx_bool enabled)
	{
	#if BX_SUPPORT_PCIUSB
	  // if type == usb, connect or disconnect the USB mouse
	  if (BX_KEY_THIS s.mouse.type == BX_MOUSE_TYPE_USB) {
	    DEV_usb_mouse_enable(enabled);
	    return;
	  }
	#endif

	  if (BX_KEY_THIS s.mouse.delayed_dx || BX_KEY_THIS s.mouse.delayed_dy ||
	      BX_KEY_THIS s.mouse.delayed_dz) {
	    create_mouse_packet(1);
	  }
	  BX_KEY_THIS s.mouse.delayed_dx=0;
	  BX_KEY_THIS s.mouse.delayed_dy=0;
	  BX_KEY_THIS s.mouse.delayed_dz=0;
	  BX_DEBUG(("PS/2 mouse %s", enabled?"enabled":"disabled"));
	}

	void bx_keyb_c::mouse_motion(int delta_x, int delta_y, int delta_z, unsigned button_state)
	{
	  bool force_enq=0;

	  // If mouse events are disabled on the GUI headerbar, don't
	  // generate any mouse data
	  if (SIM->get_param_bool(BXPN_MOUSE_ENABLED)->get() == 0)
	    return;

	  // if type == serial, redirect mouse data to the serial device
	  if ((BX_KEY_THIS s.mouse.type == BX_MOUSE_TYPE_SERIAL) ||
	      (BX_KEY_THIS s.mouse.type == BX_MOUSE_TYPE_SERIAL_WHEEL)) {
	    DEV_serial_mouse_enq(delta_x, delta_y, delta_z, button_state);
	    return;
	  }

	#if BX_SUPPORT_BUSMOUSE
	  // if type == bus, redirect mouse data to the bus device
	  if (BX_KEY_THIS s.mouse.type == BX_MOUSE_TYPE_BUS) {
	    DEV_bus_mouse_enq(delta_x, delta_y, 0, button_state);
	    return;
	  }
	#endif

	#if BX_SUPPORT_PCIUSB
	  // if an usb mouse is connected redirect mouse data to the usb device
	  if (DEV_usb_mouse_connected()) {
	    DEV_usb_mouse_enq(delta_x, delta_y, delta_z, button_state);
	    return;
	  }
	#endif

	  // don't generate interrupts if we are in remote mode.
	  if ( BX_KEY_THIS s.mouse.mode == MOUSE_MODE_REMOTE)
	    // is there any point in doing any work if we don't act on the result
	    // so go home.
	    return;

	  // Note: enable only applies in STREAM MODE.
	  if ( BX_KEY_THIS s.mouse.enable==0 )
	    return;

	  // scale down the motion
	  if ( (delta_x < -1) || (delta_x > 1) )
	    delta_x /= 2;
	  if ( (delta_y < -1) || (delta_y > 1) )
	    delta_y /= 2;

	  if (!BX_KEY_THIS s.mouse.im_mode)
	    delta_z = 0;

	#ifdef VERBOSE_KBD_DEBUG
	  if (delta_x != 0 || delta_y != 0 || delta_z != 0)
	    BX_DEBUG(("[mouse] Dx=%d Dy=%d Dz=%d", delta_x, delta_y, delta_z));
	#endif  // ifdef VERBOSE_KBD_DEBUG

	  if( (delta_x==0) && (delta_y==0) && (delta_z==0) && (BX_KEY_THIS s.mouse.button_status == (button_state & 0x7) ) ) {
	    BX_DEBUG(("Ignoring useless mouse_motion call:"));
	    BX_DEBUG(("This should be fixed in the gui code."));
	    return;
	  }

	  if ((BX_KEY_THIS s.mouse.button_status != (button_state & 0x7)) || delta_z) {
	    force_enq=1;
	  }

	  BX_KEY_THIS s.mouse.button_status = button_state & 0x7;

	  if(delta_x>255) delta_x=255;
	  if(delta_y>255) delta_y=255;
	  if(delta_x<-256) delta_x=-256;
	  if(delta_y<-256) delta_y=-256;

	  BX_KEY_THIS s.mouse.delayed_dx+=delta_x;
	  BX_KEY_THIS s.mouse.delayed_dy+=delta_y;
	  BX_KEY_THIS s.mouse.delayed_dz = delta_z;

	  if((BX_KEY_THIS s.mouse.delayed_dx>255)||
	     (BX_KEY_THIS s.mouse.delayed_dx<-256)||
	     (BX_KEY_THIS s.mouse.delayed_dy>255)||
	     (BX_KEY_THIS s.mouse.delayed_dy<-256)) {
	    force_enq=1;
	  }

	  create_mouse_packet(force_enq);
	}

*/	
    //******************************************************************************
    // Custom Methods

    private byte getStatusByte()
	{
	  // top bit is 0 , bit 6 is 1 if remote mode.
	  byte status = (byte) ((mouseMode == MOUSE_MODE_REMOTE) ? 0x40 : 0);
	  status |= ((mouseEnabled? 1: 0) << 5);
	  status |= (scaling == 1) ? 0 : (1 << 4);
	  status |= ((buttonStatus & 0x1) << 2);
	  status |= ((buttonStatus & 0x2) << 0);
	  
	  return status;
	}

    
    private byte getResolutionByte()
	{
	  byte resolution = 0;

	  switch (resolutionCpmm)
	  {
	  	case 1:
	  		resolution = 0;
	  		break;

	  	case 2:
	  		resolution = 1;
	  		break;

	  	case 4:
	  		resolution = 2;
	  		break;

	  	case 8:
	  		resolution = 3;
	  		break;

	  	default:
	  		logger.log(Level.WARNING, "[" + MODULE_TYPE + "] Invalid resolution cpmm");
	  }
	  
	  return resolution;
	}
}
