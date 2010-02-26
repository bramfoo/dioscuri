/* $Revision: 159 $ $Date: 2009-08-17 12:52:56 +0000 (ma, 17 aug 2009) $ $Author: blohman $ 
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

package dioscuri.module.mouse;

import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import dioscuri.Emulator;
import dioscuri.interfaces.UART;
import dioscuri.module.Module;
import dioscuri.module.ModuleKeyboard;
import dioscuri.module.ModuleMouse;
import dioscuri.module.ModuleSerialPort;

/**
 * An implementation of a mouse module.
 * 
 * @see Module
 * 
 *      Metadata module ********************************************
 *      general.type : mouse general.name : Serial mouse general.architecture :
 *      Von Neumann general.description : Models a serial mouse general.creator
 *      : Koninklijke Bibliotheek, Nationaal Archief of the Netherlands
 *      general.version : 1.0 general.keywords : Mouse, Keyboard, serial, PS/2
 *      general.relations : Serialport, Keyboard general.yearOfIntroduction :
 *      1987 general.yearOfEnding : general.ancestor : DE-9 RS-232 serial mouse
 *      general.successor : USB mouse
 * 
 *      Notes: - mouse can use one of the following connection types: + serial
 *      port + PS/2 via keyboard controller - all controller aspects are
 *      implemented in keyboard: (mouse is only pointing device but not
 *      controller) + I/O ports + IRQ handling
 * 
 *      References: -
 *      http://local.wasp.uwa.edu.au/~pbourke/dataformats/serialmouse/
 * 
 */
@SuppressWarnings("unused")
public class Mouse extends ModuleMouse implements UART {
    // Relations
    private Emulator emu;
    private String[] moduleConnections = new String[] { "keyboard",
            "serialport" };
    private ModuleKeyboard keyboard;
    private ModuleSerialPort serialPort;
    private MouseBuffer buffer;

    // Toggles
    private boolean isObserved;
    private boolean debugMode;

    // Variables
    private boolean mouseEnabled; // Defines if this mouse if enabled
    private int mouseType; // Defines the type of mouse (PS/2, serial)
    private int mouseMode; // Defines the mode of mouse (wrap, stream, remote,
                           // ...) (default=stream)
    private int mousePreviousMode; // Remembers the previous mode (only set when
                                   // going into wrap mode)
    private byte lastMouseCommand; // Remembers the last mouse command
    private boolean expectingMouseParameter; // Denotes if mouse expects another
                                             // mouse parameter
    private int imRequest; // Wheel mouse mode request
    private boolean imMode; // Wheel mouse mode
    private byte sampleRate; // Defines the sample rate of the mouse
                             // (default=100)
    private int resolutionCpmm; // Defines the resolution of the mouse
                                // (default=4)
    private int scaling; // Defines the scaling of the mouse (default=1 (1:1))
    private int previousX;
    private int previousY;
    private int delayed_dx = 0;
    private int delayed_dy = 0;
    private int delayed_dz = 0;
    private byte buttonStatus;

    // Logging
    private static Logger logger = Logger.getLogger("dioscuri.module.mouse");

    // Constants

    // Module specifics
    /**
     *
     */
    public final static int MODULE_ID = 1;
    /**
     *
     */
    public final static String MODULE_TYPE = "mouse";
    /**
     *
     */
    public final static String MODULE_NAME = "Serial mouse";

    // Mouse type
    private final static int MOUSE_TYPE_PS2 = 1; // PS/2 mouse
    private final static int MOUSE_TYPE_IMPS2 = 2; // PS/2 wheel mouse
    private final static int MOUSE_TYPE_SERIAL = 3; // Serial mouse
    private final static int MOUSE_TYPE_SERIAL_WHEEL = 4; // Serial wheel mouse

    // Mouse mode (PS/2)
    private final static int MOUSE_MODE_WRAP = 1;
    private final static int MOUSE_MODE_STREAM = 2;
    private final static int MOUSE_MODE_REMOTE = 3;
    private final static int MOUSE_MODE_RESET = 4;

    // Mouse commands (PS/2)
    private final static byte MOUSE_CMD_ACK = (byte) 0xFA;
    private final static byte MOUSE_CMD_COMPLETION = (byte) 0xAA; // Completion
                                                                  // code
    private final static byte MOUSE_CMD_ID = (byte) 0x00; // ID code
    private final static byte MOUSE_CMD_RESEND = (byte) 0xFE; // Also a NACK

    // Mouse buffer capacity
    private final static int MOUSE_BUFFER_SIZE = 16;

    // Constructor

    /**
     * Class constructor
     * 
     * @param owner
     */
    public Mouse(Emulator owner) {
        emu = owner;

        // Create mouse buffer
        buffer = new MouseBuffer(MOUSE_BUFFER_SIZE);

        // Initialise variables
        isObserved = false;
        debugMode = false;

        logger.log(Level.INFO, "[" + MODULE_TYPE + "] " + MODULE_NAME
                + " -> Module created successfully.");
    }

    // ******************************************************************************
    // Module Methods

    /**
     * Returns the ID of the module
     * 
     * @return string containing the ID of module
     * @see Module
     */
    public int getID() {
        return MODULE_ID;
    }

    /**
     * Returns the type of the module
     * 
     * @return string containing the type of module
     * @see Module
     */
    public String getType() {
        return MODULE_TYPE;
    }

    /**
     * Returns the name of the module
     * 
     * @return string containing the name of module
     * @see Module
     */
    public String getName() {
        return MODULE_NAME;
    }

    /**
     * Returns a String[] with all names of modules it needs to be connected to
     * 
     * @return String[] containing the names of modules, or null if no
     *         connections
     */
    public String[] getConnection() {
        // Return all required connections;
        return moduleConnections;
    }

    /**
     * Sets up a connection with another module
     * 
     * @param mod
     *            Module that is to be connected to this class
     * 
     * @return true if connection has been established successfully, false
     *         otherwise
     * 
     * @see Module
     */
    public boolean setConnection(Module mod) {
        // Set connection for keyboard
        if (mod.getType().equalsIgnoreCase("keyboard")) {
            this.keyboard = (ModuleKeyboard) mod;
            this.keyboard.setConnection(this); // Set connection to keyboard
            return true;
        }
        // Set connection for serialport
        else if (mod.getType().equalsIgnoreCase("serialport")) {
            this.serialPort = (ModuleSerialPort) mod;
            this.serialPort.setConnection(this); // Set connection to serialport
            return true;
        }
        return false;
    }

    /**
     * Checks if this module is connected to operate normally
     * 
     * @return true if this module is connected successfully, false otherwise
     */
    public boolean isConnected() {
        // Check if module if connected
        if (keyboard != null && serialPort != null) {
            return true;
        }
        return false;
    }

    /**
     * Default inherited reset. Calls specific reset(int)
     * 
     * @return boolean true if module has been reset successfully, false
     *         otherwise
     */
    public boolean reset() {
        // Reset variables
        // TODO: add all vars
        lastMouseCommand = 0;

        previousX = -1;
        previousY = -1;

        logger.log(Level.INFO, "[" + MODULE_TYPE + "]"
                + " Module has been reset.");

        return true;
    }

    /**
     * Starts the module
     * 
     * @see Module
     */
    public void start() {
        // Nothing to start
    }

    /**
     * Stops the module
     * 
     * @see Module
     */
    public void stop() {
        // Nothing to stop
    }

    /**
     * Returns the status of observed toggle
     * 
     * @return state of observed toggle
     * 
     * @see Module
     */
    public boolean isObserved() {
        return isObserved;
    }

    /**
     * Sets the observed toggle
     * 
     * @param status
     * 
     * @see Module
     */
    public void setObserved(boolean status) {
        isObserved = status;
    }

    /**
     * Returns the status of the debug mode toggle
     * 
     * @return state of debug mode toggle
     * 
     * @see Module
     */
    public boolean getDebugMode() {
        return debugMode;
    }

    /**
     * Sets the debug mode toggle
     * 
     * @param status
     * 
     * @see Module
     */
    public void setDebugMode(boolean status) {
        debugMode = status;
    }

    /**
     * Returns data from this module
     * 
     * @param requester
     * @return byte[] with data
     * 
     * @see Module
     */
    public byte[] getData(Module requester) {
        return null;
    }

    /**
     * Set data for this module
     * 
     * @param sender
     * @return true if data is set successfully, false otherwise
     * 
     * @see Module
     */
    public boolean setData(byte[] data, Module sender) {
        return false;
    }

    /**
     * Set String[] data for this module
     * 
     * @param sender
     * @return boolean true is successful, false otherwise
     * 
     * @see Module
     */
    public boolean setData(String[] data, Module sender) {
        return false;
    }

    /**
     * Returns a dump of this module
     * 
     * @return string
     * 
     * @see Module
     */
    public String getDump() {
        String keyboardDump = "Mouse status:\n";

        return keyboardDump;
    }

    // ******************************************************************************
    // ModuleMouse Methods

    public void setMouseEnabled(boolean status) {
        mouseEnabled = status;
    }

    public void setMouseType(String type) {
        // Check the type of mouse by matching string
        if (type.equalsIgnoreCase("serial")) {
            // Serial mouse
            mouseType = MOUSE_TYPE_SERIAL;
            logger.log(Level.INFO, "[" + MODULE_TYPE
                    + "] Mouse type set to serial");
            // Connect mouse to serialport on COM 1 (port 0)
            if (serialPort.setUARTDevice(this, 0) == true) {
                logger.log(Level.CONFIG, "[" + MODULE_TYPE
                        + "] Mouse connected to COM port 1");
            } else {
                logger.log(Level.SEVERE, "[" + MODULE_TYPE
                        + "] Could not connect mouse to COM port 1");
            }
        } else if (type.equalsIgnoreCase("ps/2")) {
            // PS/2 mouse
            mouseType = MOUSE_TYPE_PS2;
            logger.log(Level.INFO, "[" + MODULE_TYPE
                    + "] Mouse type set to PS/2");
        } else {
            // Unknown mouse type
            logger.log(Level.WARNING, "[" + MODULE_TYPE
                    + "] Mouse type not recognised: set to default (serial)");
            mouseType = MOUSE_TYPE_SERIAL;
        }
    }

    public boolean isBufferEmpty() {
        return buffer.isEmpty();
    }

    public synchronized void storeBufferData(boolean forceEnqueue) {
        byte b1, b2, b3, b4;
        int deltaX, deltaY;

        deltaX = delayed_dx;
        deltaY = delayed_dy;

        if (forceEnqueue == false && deltaX == 0 && deltaY == 0) {
            // No mouse movement, so no changes
            return;
        }

        // Limit values
        if (deltaX > 254)
            deltaX = 254;
        if (deltaX < -254)
            deltaX = -254;
        if (deltaY > 254)
            deltaY = 254;
        if (deltaY < -254)
            deltaY = -254;

        // Set bytes
        // Byte b1
        b1 = (byte) ((buttonStatus & 0x0F) | 0x08); // bit3 always set

        // Byte b2
        if ((deltaX >= 0) && (deltaX <= 255)) {
            b2 = (byte) deltaX;
            delayed_dx -= deltaX;
        } else if (deltaX > 255) {
            b2 = (byte) 0xFF;
            delayed_dx -= 255;
        } else if (deltaX >= -256) {
            b2 = (byte) deltaX;
            b1 |= 0x10;
            delayed_dx -= deltaX;
        } else {
            b2 = (byte) 0x00;
            b1 |= 0x10;
            delayed_dx += 256;
        }

        // Byte b3
        if ((deltaY >= 0) && (deltaY <= 255)) {
            b3 = (byte) deltaY;
            delayed_dy -= deltaY;
        } else if (deltaY > 255) {
            b3 = (byte) 0xFF;
            delayed_dy -= 255;
        } else if (deltaY >= -256) {
            b3 = (byte) deltaY;
            b1 |= 0x20;
            delayed_dy -= deltaY;
        } else {
            b3 = (byte) 0x00;
            b1 |= 0x20;
            delayed_dy += 256;
        }

        // Byte b4
        b4 = (byte) -delayed_dz;

        this.enqueueData(b1, b2, b3, b4);
        logger
                .log(
                        Level.CONFIG,
                        "["
                                + MODULE_TYPE
                                + "] Mouse (PS/2) data stored in mouse buffer. Total bytes in buffer: "
                                + buffer.size());
    }

    public synchronized byte getDataFromBuffer() {
        if (buffer.isEmpty() == false) {
            return buffer.getByte();
        }
        return -1;
    }

    public void controlMouse(byte value) {
        // FIXME: // if we are not using a ps2 mouse, some of the following
        // commands need to return different values
        boolean isMousePS2;
        isMousePS2 = false;
        if ((mouseType == MOUSE_TYPE_PS2) || (mouseType == MOUSE_TYPE_IMPS2)) {
            isMousePS2 = true;
        }

        logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] kbd_ctrl_to_mouse "
                + value);

        // An ACK (0xFA) is always the first response to any valid input
        // received from the system other than Set-Wrap-Mode & Resend-Command

        // Check if this is the second mouse command (expected)
        if (expectingMouseParameter == true) {
            // Reset command parameter
            expectingMouseParameter = false;

            // Execute command
            switch (lastMouseCommand) {
            case (byte) 0xF3: // Set Mouse Sample Rate
                sampleRate = value;
                logger.log(Level.CONFIG, "[" + MODULE_TYPE
                        + "] Sampling rate set to " + value);

                if ((value == 200) && (imRequest == 0)) {
                    imRequest = 1;
                } else if ((value == 100) && (imRequest == 1)) {
                    imRequest = 2;
                } else if ((value == 80) && (imRequest == 2)) {
                    // Check if wheel mouse should be enabled
                    if (mouseType == MOUSE_TYPE_IMPS2) {
                        logger.log(Level.INFO, "[" + MODULE_TYPE
                                + "] Wheel mouse mode enabled");
                        imMode = true;
                    } else {
                        logger.log(Level.INFO, "[" + MODULE_TYPE
                                + "] Wheel mouse mode request rejected");
                    }

                    imRequest = 0;
                } else {
                    imRequest = 0;
                }

                keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ack
                break;

            case (byte) 0xE8: // Set Mouse Resolution
                switch (value) {
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
                    logger.log(Level.WARNING, "[" + MODULE_TYPE
                            + "] Unknown resolution");
                    break;
                }
                logger.log(Level.CONFIG, "[" + MODULE_TYPE
                        + "] Resolution set to " + resolutionCpmm
                        + " counts per mm");

                keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ack
                break;

            default:
                logger.log(Level.WARNING, "[" + MODULE_TYPE
                        + "] unknown last command " + lastMouseCommand);
            }
        } else {
            // This is the first mouse command
            expectingMouseParameter = false;
            lastMouseCommand = value;

            // Check wrap mode
            if (mouseMode == MOUSE_MODE_WRAP) {
                // if not a reset command or reset wrap mode
                // then just echo the byte.
                if ((value != 0xFF) && (value != 0xEC)) {
                    logger.log(Level.CONFIG, "[" + MODULE_TYPE
                            + "] Wrap mode: ignoring command " + value);
                    keyboard.enqueueControllerBuffer(value, 1);
                    return;
                }
            }

            switch (value) {

            case (byte) 0xBB: // OS/2 Warp 3 uses this command
                logger.log(Level.WARNING, "[" + MODULE_TYPE
                        + "] Ignoring command 0xBB");
                break;

            case (byte) 0xE6: // Set Mouse Scaling to 1:1
                keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                scaling = 2;
                logger.log(Level.CONFIG, "[" + MODULE_TYPE
                        + "] Scaling set to 1:1");
                break;

            case (byte) 0xE7: // Set Mouse Scaling to 2:1
                keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                scaling = 2;
                logger.log(Level.CONFIG, "[" + MODULE_TYPE
                        + "] Scaling set to 2:1");
                break;

            case (byte) 0xE8: // Set Mouse Resolution
                keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                expectingMouseParameter = true;
                break;

            case (byte) 0xE9: // Get mouse information
                keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                keyboard.enqueueControllerBuffer(this.getStatusByte(), 1); // status
                keyboard.enqueueControllerBuffer(this.getResolutionByte(), 1); // resolution
                keyboard.enqueueControllerBuffer(sampleRate, 1); // sample rate
                logger.log(Level.CONFIG, "[" + MODULE_TYPE
                        + "] Mouse information returned");
                break;

            case (byte) 0xEA: // Set Stream Mode
                mouseMode = MOUSE_MODE_STREAM;
                keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                logger
                        .log(Level.CONFIG, "[" + MODULE_TYPE
                                + "] Stream mode on");
                break;

            case (byte) 0xEB: // Read Data (send a packet when in Remote Mode)
                keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                // FIXME: unsure if we should send a packet to mouse buffer.
                // Bochs does so:
                this.enqueueData((byte) ((buttonStatus & 0x0F) | 0x08),
                        (byte) 0x00, (byte) 0x00, (byte) 0x00); // bit3 of first
                                                                // byte always
                                                                // set
                // FIXME: assumed we really aren't in polling mode, a rather odd
                // assumption.
                logger.log(Level.WARNING, "[" + MODULE_TYPE
                        + "] Read Data command partially supported");
                break;

            case (byte) 0xEC: // Reset Wrap Mode
                // Check if mouse is in wrap mode, else ignore command
                if (mouseMode == MOUSE_MODE_WRAP) {
                    // Restore previous mode except disable stream mode
                    // reporting.
                    // TODO disabling reporting in stream mode
                    mouseMode = mousePreviousMode;
                    keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                    logger.log(Level.CONFIG, "[" + MODULE_TYPE
                            + "] Wrap mode off. Set to previous mode");
                }
                break;

            case (byte) 0xEE: // Set Wrap Mode
                // TODO flush output queue.
                // TODO disable interrupts if in stream mode.
                mousePreviousMode = mouseMode;
                mouseMode = MOUSE_MODE_WRAP;
                keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] Wrap mode on");
                break;

            case (byte) 0xF0: // Set Remote Mode (polling mode, i.e. not stream
                              // mode.)
                // TODO should we flush/discard/ignore any already queued
                // packets?
                mouseMode = MOUSE_MODE_REMOTE;
                keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                logger
                        .log(Level.CONFIG, "[" + MODULE_TYPE
                                + "] Remote mode on");
                break;

            case (byte) 0xF2: // Read Device Type
                keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                if (imMode == true) {
                    keyboard.enqueueControllerBuffer((byte) 0x03, 1); // Device
                                                                      // ID
                                                                      // (wheel
                                                                      // z-mouse)
                } else {
                    keyboard.enqueueControllerBuffer((byte) 0x00, 1); // Device
                                                                      // ID
                                                                      // (standard)
                }
                logger.log(Level.CONFIG, "[" + MODULE_TYPE + "] Read mouse ID");
                break;

            case (byte) 0xF3: // Enable set Mouse Sample Rate (sample rate
                              // written to port 0x60)
                keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                expectingMouseParameter = true;
                break;

            case (byte) 0xF4: // Enable (in stream mode)
                // is a mouse present?
                if (isMousePS2) {
                    mouseEnabled = true;
                    keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                    logger.log(Level.CONFIG, "[" + MODULE_TYPE
                            + "] Mouse enabled (stream mode)");
                } else {
                    // No mouse present. A 0xFE (resend) need to be returned
                    // instead of a 0xFA (ACK)
                    keyboard.enqueueControllerBuffer(MOUSE_CMD_RESEND, 1); // ACK
                    keyboard.setTimeOut((byte) 1);
                }
                break;

            case (byte) 0xF5: // Disable (in stream mode)
                mouseEnabled = false;
                keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                logger.log(Level.CONFIG, "[" + MODULE_TYPE
                        + "] Mouse disabled (stream mode)");
                break;

            case (byte) 0xF6: // Set mouse to defaults
                sampleRate = 100; // reports per second (default)
                resolutionCpmm = 4; // 4 counts per millimeter (default)
                scaling = 1; // 1:1 (default)
                mouseEnabled = false;
                mouseMode = MOUSE_MODE_STREAM;
                keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                logger.log(Level.CONFIG, "[" + MODULE_TYPE
                        + "] Mouse set to default settings");
                break;

            case (byte) 0xFF: // Reset
                // Check if a mouse is present
                if (isMousePS2) {
                    sampleRate = 100; // reports per second (default)
                    resolutionCpmm = 4; // 4 counts per millimeter (default)
                    scaling = 1; // 1:1 (default)
                    mouseEnabled = false;
                    mouseMode = MOUSE_MODE_RESET;
                    if (imMode == true) {
                        logger.log(Level.CONFIG, "[" + MODULE_TYPE
                                + "] Wheel mouse mode disabled");
                    }
                    imMode = false;

                    // TODO: NT expects an ack here
                    keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                    keyboard.enqueueControllerBuffer(MOUSE_CMD_COMPLETION, 1); // COMPLETION
                    keyboard.enqueueControllerBuffer(MOUSE_CMD_ID, 1); // ID
                    logger.log(Level.CONFIG, "[" + MODULE_TYPE
                            + "] Mouse has been reset");
                } else {
                    // No mouse present. A 0xFE (resend) need to be returned
                    // instead of a 0xFA (ACK)
                    keyboard.enqueueControllerBuffer(MOUSE_CMD_RESEND, 1); // RESEND
                    keyboard.setTimeOut((byte) 1);
                }
                break;

            default:
                // If PS/2 mouse present, send NACK for unknown commands,
                // otherwise ignore
                if (isMousePS2) {
                    logger.log(Level.WARNING, "[" + MODULE_TYPE
                            + "] kbd_ctrl_to_mouse(): no command match");
                    keyboard.enqueueControllerBuffer(MOUSE_CMD_RESEND, 1); // RESEND/NACK
                }
            }
        }
    }

    // FOLLOWING METHOD FROM BOCHS IS ONLY NECESSARY WHEN MOUSE IS SET TO
    // ENABLED/DISABLED
    /*
     * void bx_keyb_c::mouse_enabled_changed(bx_bool enabled) { #if
     * BX_SUPPORT_PCIUSB // if type == usb, connect or disconnect the USB mouse
     * if (BX_KEY_THIS s.mouse.type == BX_MOUSE_TYPE_USB) {
     * DEV_usb_mouse_enable(enabled); return; } #endif
     * 
     * if (BX_KEY_THIS s.mouse.delayed_dx || BX_KEY_THIS s.mouse.delayed_dy ||
     * BX_KEY_THIS s.mouse.delayed_dz) { create_mouse_packet(1); } BX_KEY_THIS
     * s.mouse.delayed_dx=0; BX_KEY_THIS s.mouse.delayed_dy=0; BX_KEY_THIS
     * s.mouse.delayed_dz=0; BX_DEBUG(("PS/2 mouse %s",
     * enabled?"enabled":"disabled")); }
     */

    public void mouseMotion(MouseEvent event) {
        int deltaX, deltaY, deltaZ, buttonLeft, buttonRight;
        byte byte1, byte2, byte3, byte4;

        // Check if this is the first mouse motion
        if (previousX == -1) {
            previousX = event.getX();
            previousY = event.getY();

            return;
        }

        // Measure position change
        deltaX = event.getX() - previousX;
        deltaY = event.getY() - previousY;
        deltaZ = 0;
        buttonRight = event.getButton() == 3 ? 1 : 0;
        buttonLeft = event.getButton() == 1 ? 1 : 0;

        // Preserve current coordinates
        previousX = event.getX();
        previousY = event.getY();

        // Check mouse type
        if (mouseType == MOUSE_TYPE_SERIAL
                || mouseType == MOUSE_TYPE_SERIAL_WHEEL) {
            // Serial mouse: store data in internal mouse buffer
            // scale down the motion
            if ((deltaX < -1) || (deltaX > 1)) {
                deltaX = deltaX / 2;
            }

            if ((deltaY < -1) || (deltaY > 1)) {
                deltaY = deltaY / 2;
            }

            // Limit the boundaries
            if (deltaX > 127)
                deltaX = 127;
            if (deltaY > 127)
                deltaY = 127;
            if (deltaX < -128)
                deltaX = -128;
            if (deltaY < -128)
                deltaY = -128;

            // FIXME:
            /*
             * delayed_dx += deltaX; delayed_dy -= deltaY; delayed_dz = deltaZ;
             * 
             * if (BX_SER_THIS mouse_delayed_dx > 127) { deltaX = 127;
             * BX_SER_THIS mouse_delayed_dx -= 127; } else if (BX_SER_THIS
             * mouse_delayed_dx < -128) { deltaX = -128; BX_SER_THIS
             * mouse_delayed_dx += 128; } else { deltaX = BX_SER_THIS
             * mouse_delayed_dx; BX_SER_THIS mouse_delayed_dx = 0; } if
             * (BX_SER_THIS mouse_delayed_dy > 127) { deltaY = 127; BX_SER_THIS
             * mouse_delayed_dy -= 127; } else if (BX_SER_THIS mouse_delayed_dy
             * < -128) { deltaY = -128; BX_SER_THIS mouse_delayed_dy += 128; }
             * else { deltaY = BX_SER_THIS mouse_delayed_dy; BX_SER_THIS
             * mouse_delayed_dy = 0; }
             */
            // FIXME: delta_z = (byte) -((Bit8s) deltaZ);

            // Serial mouse data format:
            // b7 b6 b5 b4 b3 b2 b1 b0
            // 1st byte 0 1 LB RB Y7 Y6 X7 X6
            // 2nd byte 0 0 X5 X4 X3 X2 X1 X0
            // 3rd byte 0 0 Y5 Y4 Y3 Y2 Y1 Y0
            //
            // LB = state of left button, 1 = pressed, 0 = released
            // RB = state of right button, 1 = pressed, 0 = released
            // X0-7 is movement of mouse in X direction since the last change.
            // Positive movement is toward the right
            // Y0-7 is movement of mouse in Y direction since the last change.
            // Positive movement is back, toward the user

            byte1 = (byte) (0x40
                    | (((((byte) deltaX) & 0xC0) >> 6) | ((((byte) deltaY) & 0xC0) >> 4))
                    | ((buttonLeft & 0x01) << 5) | ((buttonRight & 0x01) << 4));
            byte2 = (byte) (deltaX & 0x3F);
            byte3 = (byte) (deltaY & 0x3F);
            byte4 = 0; // FIXME: (byte) ((deltaZ & 0x0f) | ((buttonState & 0x04)
                       // << 2));

            // Enqueue mouse data in internal mouse buffer
            // this.enqueueData(byte1, byte2, byte3, byte4);
            buffer.setByte(byte1);
            buffer.setByte(byte2);
            buffer.setByte(byte3);

            logger.log(Level.SEVERE, "[" + MODULE_TYPE
                    + "] Mouse movement! dX=" + deltaX + ", dY=" + deltaY
                    + ", dZ=" + deltaZ + ", buttonLeft=" + buttonLeft);
            // logger.log(Level.SEVERE, "[" + MODULE_TYPE +
            // "] Mouse movement vervolg: b1=" + b1 + ", b2=" + b2 + ", b3=" +
            // b3 + ", b4=" + b4);
            logger
                    .log(
                            Level.SEVERE,
                            "["
                                    + MODULE_TYPE
                                    + "] Mouse (serial) data stored in mouse buffer. Total bytes in buffer: "
                                    + buffer.size());
        } else {
            // PS/2 mouse
            // Scale down the motion
            /*
             * if ( (deltaX < -1) || (deltaX > 1) ) deltaX /= 2; if ( (deltaY <
             * -1) || (deltaY > 1) ) deltaY /= 2;
             * 
             * if (imMode == false) { delta_z = 0; }
             * 
             * if (deltaX != 0 || deltaY != 0 || delta_z != 0) {
             * logger.log(Level.CONFIG, "[" + MODULE_TYPE +
             * "] mouse position changed: Dx=" + deltaX + ", Dy=" + deltaY +
             * ", Dz=" + delta_z); }
             * 
             * if ((buttonStatus != (buttonState & 0x7)) || delta_z != 0) {
             * force_enq = true; }
             * 
             * buttonStatus = (byte) (buttonState & 0x07);
             * 
             * if(deltaX > 255) deltaX = 255; if(deltaY > 255) deltaY = 255;
             * if(deltaX < -256) deltaX = -256; if(deltaY < -256) deltaY = -256;
             * 
             * delayed_dx += deltaX; delayed_dy += deltaY; delayed_dz = delta_z;
             * 
             * // TODO: why is this check necessary? if((delayed_dx > 255) ||
             * (delayed_dx<-256) || (delayed_dy > 255) || (delayed_dy < -256)) {
             * force_enq = true; } this.storeBufferData(force_enq);
             */
        }
    }

    // ******************************************************************************
    // Custom Methods

    private byte getStatusByte() {
        // top bit is 0 , bit 6 is 1 if remote mode.
        byte status = (byte) ((mouseMode == MOUSE_MODE_REMOTE) ? 0x40 : 0);
        status |= ((mouseEnabled ? 1 : 0) << 5);
        status |= (scaling == 1) ? 0 : (1 << 4);
        status |= ((buttonStatus & 0x1) << 2);
        status |= ((buttonStatus & 0x2) << 0);

        return status;
    }

    private byte getResolutionByte() {
        byte resolution = 0;

        switch (resolutionCpmm) {
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
            logger.log(Level.WARNING, "[" + MODULE_TYPE
                    + "] Invalid resolution cpmm");
        }

        return resolution;
    }

    private void enqueueData(byte b1, byte b2, byte b3, byte b4) {
        if (imMode == true) {
            // Wheel mouse enabled, store 4 bytes
            buffer.setByte(b1);
            buffer.setByte(b2);
            buffer.setByte(b3);
            buffer.setByte(b4);
        } else {
            // Wheel mouse disabled, store 3 bytes
            buffer.setByte(b1);
            buffer.setByte(b2);
            buffer.setByte(b3);
        }
    }

    // ******************************************************************************
    // UART interface

    public synchronized boolean isDataAvailable() {
        return !(buffer.isEmpty());
    }

    public synchronized byte getSerialData() {
        if (buffer.isEmpty() == false) {
            return buffer.getByte();
        }
        return -1;
    }

    public synchronized void setSerialData(byte data) {
        buffer.setByte(data);
    }

}
