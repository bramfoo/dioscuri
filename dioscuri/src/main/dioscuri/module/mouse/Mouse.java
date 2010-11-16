package dioscuri.module.mouse;

import dioscuri.Emulator;
import dioscuri.interfaces.Module;
import dioscuri.interfaces.UART;
import dioscuri.module.ModuleKeyboard;
import dioscuri.module.ModuleMouse;
import dioscuri.module.ModuleSerialPort;

import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of a mouse module.
 *
 * @see dioscuri.module.AbstractModule
 *      <p/>
 *      Metadata module
 *      ********************************************
 *      general.type                : mouse
 *      general.name                : Serial mouse
 *      general.architecture        : Von Neumann
 *      general.description         : Models a serial mouse
 *      general.creator             : Koninklijke Bibliotheek, Nationaal Archief of the Netherlands
 *      general.version             : 1.0
 *      general.keywords            : Mouse, Keyboard, serial, PS/2
 *      general.relations           : Serialport, Keyboard
 *      general.yearOfIntroduction  : 1987
 *      general.yearOfEnding        :
 *      general.ancestor            : DE-9 RS-232 serial mouse
 *      general.successor           : USB mouse
 *      <p/>
 *      Notes:
 *      - mouse can use one of the following connection types:
 *      + serial port
 *      + PS/2 via keyboard controller
 *      - all controller aspects are implemented in keyboard: (mouse is only pointing device but not controller)
 *      + I/O ports
 *      + IRQ handling
 */

public class Mouse extends ModuleMouse implements UART {

    // Relations
    private Queue<Byte> buffer;

    // Variables
    private boolean mouseEnabled;                     // Defines if this mouse if enabled
    private int mouseType;                            // Defines the type of mouse (PS/2, serial)
    private int mouseMode;                            // Defines the mode of mouse (wrap, stream, remote, ...) (default=stream)
    private int mousePreviousMode;                    // Remembers the previous mode (only set when going into wrap mode)
    private byte lastMouseCommand;                    // Remembers the last mouse command
    private boolean expectingMouseParameter;          // Denotes if mouse expects another mouse parameter
    private int imRequest;                            // Wheel mouse mode request
    private boolean imMode;                           // Wheel mouse mode
    private byte sampleRate = 100;                    // Defines the sample rate of the mouse (default=100)
    private int resolutionCpmm = 4;                   // Defines the resolution of the mouse (default=4)
    private int scaling = 1;                          // Defines the scaling of the mouse (default=1 (1:1))
    //private int previousX = -1;
    //private int previousY = -1;
    private int delayed_dx = 0;
    private int delayed_dy = 0;
    private int delayed_dz = 0;
    private byte buttonStatus;
    private MouseEvent mouseEvent;

    // Logging
    private static Logger logger = Logger.getLogger("dioscuri.module.mouse");

    // Mouse type
    private final static int MOUSE_TYPE_PS2 = 1;            // PS/2 mouse
    private final static int MOUSE_TYPE_IMPS2 = 2;            // PS/2 wheel mouse
    private final static int MOUSE_TYPE_SERIAL = 3;            // Serial mouse
    private final static int MOUSE_TYPE_SERIAL_WHEEL = 4;            // Serial wheel mouse

    // Mouse mode (PS/2)
    private final static int MOUSE_MODE_WRAP = 1;
    private final static int MOUSE_MODE_STREAM = 2;
    private final static int MOUSE_MODE_REMOTE = 3;
    private final static int MOUSE_MODE_RESET = 4;

    // Mouse commands (PS/2)
    private final static byte MOUSE_CMD_ACK = (byte) 0xFA;
    private final static byte MOUSE_CMD_COMPLETION = (byte) 0xAA;    // Completion code
    private final static byte MOUSE_CMD_ID = (byte) 0x00;    // ID code
    private final static byte MOUSE_CMD_RESEND = (byte) 0xFE;    // Also a NACK

    // Mouse buffer capacity
    private final static int MOUSE_BUFFER_SIZE = 16;

    /**
     * Class constructor
     */
    public Mouse(Emulator owner) {
        // Create mouse buffer
        buffer = new LinkedList<Byte>();
        logger.log(Level.INFO, "[" + super.getType() + "] " + getClass().getName() + " -> AbstractModule created successfully.");
    }


    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.AbstractModule
     */
    @Override
    public boolean reset() {
        // Reset variables
        // TODO: add all vars
        lastMouseCommand = 0;

        //previousX = -1;
        //previousY = -1;

        logger.log(Level.INFO, "[" + super.getType() + "]" + " AbstractModule has been reset.");

        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.AbstractModule
     */
    @Override
    public String getDump() {
        String keyboardDump = "Mouse status:\n";

        return keyboardDump;
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.ModuleMouse
     */
    @Override
    public void setMouseEnabled(boolean status) {
        mouseEnabled = status;
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.ModuleMouse
     */
    @Override
    public void setMouseType(String type) {

        ModuleSerialPort serialPort = (ModuleSerialPort) super.getConnection(Module.Type.SERIALPORT);

        // Check the type of mouse by matching string
        if (type.equalsIgnoreCase("serial")) {
            // Serial mouse
            mouseType = MOUSE_TYPE_SERIAL;
            logger.log(Level.INFO, "[" + super.getType() + "] Mouse type set to serial");
            // Connect mouse to serialport on COM 1 (port 0)
            if (serialPort.setUARTDevice(this, 0)) {
                logger.log(Level.CONFIG, "[" + super.getType() + "] Mouse connected to COM port 1");
            } else {
                logger.log(Level.SEVERE, "[" + super.getType() + "] Could not connect mouse to COM port 1");
            }
        } else if (type.equalsIgnoreCase("ps/2")) {
            // PS/2 mouse
            mouseType = MOUSE_TYPE_PS2;
            logger.log(Level.INFO, "[" + super.getType() + "] Mouse type set to PS/2");
        } else {
            // Unknown mouse type
            logger.log(Level.WARNING, "[" + super.getType() + "] Mouse type not recognised: set to default (serial)");
            mouseType = MOUSE_TYPE_SERIAL;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.ModuleMouse
     */
    @Override
    public boolean isBufferEmpty() {
        return buffer.isEmpty();
    }

    final int pointerWidth = 90;
    final int pointerHeight = 25;
    final double scaleX = 710.0 / pointerWidth;
    final double scaleY = 400.0 / pointerHeight;

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.ModuleMouse
     */
    @Override
    public void storeBufferData(boolean forceEnqueue) {

        byte b1 = (byte) 0x87; // init b1 to: 1000_0111
        byte b2 = (byte) ((this.mouseEvent.getX() / scaleX) - (pointerWidth / 2.0));
        byte b3 = (byte) (-((this.mouseEvent.getY() / scaleY) - (pointerHeight / 2.0)));

        //if(b2 < -127) b2 = -127;
        //if(b2 > 127) b2 = 127;

        //if(b3 < -pointerHeight) b3 = -((byte)pointerHeight);
        //if(b3 > 0) b3 = 0;

        logger.log(Level.INFO, "[" + super.getType() + "] moved to (" + b2 + "," + b3 + ")");

        // Only act on mouse-presses: ignore mouse releases
        if (this.mouseEvent != null && this.mouseEvent.getID() == MouseEvent.MOUSE_PRESSED) {
            if (this.mouseEvent.getButton() == MouseEvent.BUTTON1) b1 &= 0xFB; // AND b1 with 1111_1011
            if (this.mouseEvent.getButton() == MouseEvent.BUTTON2) b1 &= 0xFD; // AND b1 with 1111_1101
            if (this.mouseEvent.getButton() == MouseEvent.BUTTON3) b1 &= 0xFE; // AND b1 with 1111_1110
        }

        if (this.enqueueData(b1, b2, b3, (byte) 0, (byte) 0)) {
            logger.log(Level.INFO, "[" + super.getType() + "] Mouse data stored in mouse buffer. Total bytes in buffer: " + buffer.size());
        } else {
            logger.log(Level.WARNING, "[" + super.getType() + "] Mouse data could not be stored in mouse buffer");
        }
    }

    private boolean enqueueData(byte b1, byte b2, byte b3, byte b4, byte b5) {
        return buffer.offer(b1) && buffer.offer(b2) && buffer.offer(b3) && buffer.offer(b4) && buffer.offer(b5);
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.ModuleMouse
     */
    @Override
    public byte getDataFromBuffer() {
        return buffer.isEmpty() ? -1 : buffer.poll();
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.ModuleMouse
     */
    @Override
    public void controlMouse(byte value) {

        ModuleKeyboard keyboard = (ModuleKeyboard) super.getConnection(Module.Type.KEYBOARD);

        // FIXME: if we are not using a ps2 mouse, some of the following commands need to return different values
        boolean isMousePS2 = false;
        if ((mouseType == MOUSE_TYPE_PS2) || (mouseType == MOUSE_TYPE_IMPS2)) {
            isMousePS2 = true;
        }

        logger.log(Level.CONFIG, "[" + super.getType() + "] kbd_ctrl_to_mouse " + value);

        // An ACK (0xFA) is always the first response to any valid input
        // received from the system other than Set-Wrap-Mode & Resend-Command

        // Check if this is the second mouse command (expected)
        if (expectingMouseParameter) {
            // Reset command parameter
            expectingMouseParameter = false;

            // Execute command
            switch (lastMouseCommand) {
                case (byte) 0xF3: // Set Mouse Sample Rate
                    sampleRate = value;
                    logger.log(Level.INFO, "[" + super.getType() + "] Sampling rate set to " + value);

                    if ((value == 200) && (imRequest == 0)) {
                        imRequest = 1;
                    } else if ((value == 100) && (imRequest == 1)) {
                        imRequest = 2;
                    } else if ((value == 80) && (imRequest == 2)) {
                        // Check if wheel mouse should be enabled
                        if (mouseType == MOUSE_TYPE_IMPS2) {
                            logger.log(Level.INFO, "[" + super.getType() + "] Wheel mouse mode enabled");
                            imMode = true;
                        } else {
                            logger.log(Level.INFO, "[" + super.getType() + "] Wheel mouse mode request rejected");
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
                            logger.log(Level.WARNING, "[" + super.getType() + "] Unknown resolution");
                            break;
                    }
                    logger.log(Level.INFO, "[" + super.getType() + "] Resolution set to " + resolutionCpmm + " counts per mm");

                    keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ack
                    break;

                default:
                    logger.log(Level.WARNING, "[" + super.getType() + "] unknown last command " + lastMouseCommand);
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
                    logger.log(Level.INFO, "[" + super.getType() + "] Wrap mode: ignoring command " + value);
                    keyboard.enqueueControllerBuffer(value, 1);
                    return;
                }
            }

            switch (value) {

                case (byte) 0xBB: // OS/2 Warp 3 uses this command
                    logger.log(Level.WARNING, "[" + super.getType() + "] Ignoring command 0xBB");
                    break;

                case (byte) 0xE6: // Set Mouse Scaling to 1:1
                    keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                    scaling = 2;
                    logger.log(Level.INFO, "[" + super.getType() + "] Scaling set to 1:1");
                    break;

                case (byte) 0xE7: // Set Mouse Scaling to 2:1
                    keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                    scaling = 2;
                    logger.log(Level.INFO, "[" + super.getType() + "] Scaling set to 2:1");
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
                    logger.log(Level.INFO, "[" + super.getType() + "] Mouse information returned");
                    break;

                case (byte) 0xEA: // Set Stream Mode
                    mouseMode = MOUSE_MODE_STREAM;
                    keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                    logger.log(Level.INFO, "[" + super.getType() + "] Stream mode on");
                    break;

                case (byte) 0xEB: // Read Data (send a packet when in Remote Mode)
                    keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                    // FIXME: unsure if we should send a packet to mouse buffer. Bochs does so:
                    this.enqueueData((byte) ((buttonStatus & 0x0F) | 0x08), (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00); // bit3 of first byte always set // TODO BK commented mouse test
                    // FIXME: assumed we really aren't in polling mode, a rather odd assumption.
                    logger.log(Level.WARNING, "[" + super.getType() + "] Read Data command partially supported");
                    break;

                case (byte) 0xEC: // Reset Wrap Mode
                    // Check if mouse is in wrap mode, else ignore command
                    if (mouseMode == MOUSE_MODE_WRAP) {
                        // Restore previous mode except disable stream mode reporting.
                        // TODO disabling reporting in stream mode
                        mouseMode = mousePreviousMode;
                        keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                        logger.log(Level.INFO, "[" + super.getType() + "] Wrap mode off. Set to previous mode");
                    }
                    break;

                case (byte) 0xEE: // Set Wrap Mode
                    // TODO flush output queue.
                    // TODO disable interrupts if in stream mode.
                    mousePreviousMode = mouseMode;
                    mouseMode = MOUSE_MODE_WRAP;
                    keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                    logger.log(Level.INFO, "[" + super.getType() + "] Wrap mode on");
                    break;

                case (byte) 0xF0: // Set Remote Mode (polling mode, i.e. not stream mode.)
                    // TODO should we flush/discard/ignore any already queued packets?
                    mouseMode = MOUSE_MODE_REMOTE;
                    keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                    logger.log(Level.CONFIG, "[" + super.getType() + "] Remote mode on");
                    break;

                case (byte) 0xF2: // Read Device Type
                    keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                    if (imMode) {
                        keyboard.enqueueControllerBuffer((byte) 0x03, 1); // Device ID (wheel z-mouse)
                    } else {
                        keyboard.enqueueControllerBuffer((byte) 0x00, 1); // Device ID (standard)
                    }
                    logger.log(Level.CONFIG, "[" + super.getType() + "] Read mouse ID");
                    break;

                case (byte) 0xF3: // Enable set Mouse Sample Rate (sample rate written to port 0x60)
                    keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                    expectingMouseParameter = true;
                    break;

                case (byte) 0xF4: // Enable (in stream mode)
                    // is a mouse present?
                    if (isMousePS2) {
                        mouseEnabled = true;
                        keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                        logger.log(Level.CONFIG, "[" + super.getType() + "] Mouse enabled (stream mode)");
                    } else {
                        // No mouse present.  A 0xFE (resend) need to be returned instead of a 0xFA (ACK)
                        keyboard.enqueueControllerBuffer(MOUSE_CMD_RESEND, 1); // ACK
                        keyboard.setTimeOut((byte) 1);
                    }
                    break;

                case (byte) 0xF5: // Disable (in stream mode)
                    mouseEnabled = false;
                    keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                    logger.log(Level.CONFIG, "[" + super.getType() + "] Mouse disabled (stream mode)");
                    break;

                case (byte) 0xF6: // Set mouse to defaults
                    sampleRate = 100; // reports per second (default)
                    resolutionCpmm = 4; // 4 counts per millimeter (default)
                    scaling = 1; // 1:1 (default)
                    mouseEnabled = false;
                    mouseMode = MOUSE_MODE_STREAM;
                    keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                    logger.log(Level.CONFIG, "[" + super.getType() + "] Mouse set to default settings");
                    break;

                case (byte) 0xFF: // Reset
                    // Check if a mouse is present
                    if (isMousePS2) {
                        sampleRate = 100; // reports per second (default)
                        resolutionCpmm = 4; // 4 counts per millimeter (default)
                        scaling = 1; // 1:1 (default)
                        mouseEnabled = false;
                        mouseMode = MOUSE_MODE_RESET;
                        if (imMode) {
                            logger.log(Level.CONFIG, "[" + super.getType() + "] Wheel mouse mode disabled");
                        }
                        imMode = false;

                        // TODO: NT expects an ack here
                        keyboard.enqueueControllerBuffer(MOUSE_CMD_ACK, 1); // ACK
                        keyboard.enqueueControllerBuffer(MOUSE_CMD_COMPLETION, 1); // COMPLETION
                        keyboard.enqueueControllerBuffer(MOUSE_CMD_ID, 1); // ID
                        logger.log(Level.CONFIG, "[" + super.getType() + "] Mouse has been reset");
                    } else {
                        // No mouse present.  A 0xFE (resend) need to be returned instead of a 0xFA (ACK)
                        keyboard.enqueueControllerBuffer(MOUSE_CMD_RESEND, 1); // RESEND
                        keyboard.setTimeOut((byte) 1);
                    }
                    break;

                default:
                    // If PS/2 mouse present, send NACK for unknown commands, otherwise ignore
                    if (isMousePS2) {
                        logger.log(Level.WARNING, "[" + super.getType() + "] kbd_ctrl_to_mouse(): no command match");
                        keyboard.enqueueControllerBuffer(MOUSE_CMD_RESEND, 1); // RESEND/NACK
                    }
            }
        }
    }

    // FOLLOWING METHOD FROM BOCHS IS ONLY NECESSARY WHEN MOUSE IS SET TO ENABLED/DISABLED
/*	void bx_keyb_c::mouse_enabled_changed(bx_bool enabled)
	{
	#if BX_SUPPORT_PCIUSB
	  // if type == usb, connect or disconnect the USB mouse
	  if (BX_KEY_THIS s.mouse.type == BX_MOUSE_TYPE_USB)
	  {
	    DEV_usb_mouse_enable(enabled);
	    return;
	  }
	#endif

	  if (BX_KEY_THIS s.mouse.delayed_dx || BX_KEY_THIS s.mouse.delayed_dy ||
	      BX_KEY_THIS s.mouse.delayed_dz)
	      {
	      	create_mouse_packet(1);
	  }
	  BX_KEY_THIS s.mouse.delayed_dx=0;
	  BX_KEY_THIS s.mouse.delayed_dy=0;
	  BX_KEY_THIS s.mouse.delayed_dz=0;
	  BX_DEBUG(("PS/2 mouse %s", enabled?"enabled":"disabled"));
	}
*/

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.ModuleMouse
     */
    @Override
    public void mouseMotion(MouseEvent event) {

        this.mouseEvent = event;
        // Check if this is the first mouse motion
        /*
        if (previousX == -1) {
            previousX = event.getX();
            previousY = event.getY();
            return;
        }
        */

        // Measure position change
        delayed_dx = event.getX();// - previousX;
        delayed_dy = event.getY();// - previousY;
        delayed_dz = event.getButton();

        //logger.log(Level.INFO, "[" + super.getType() + "] previous :: ("+previousX+","+previousY+"), delayed :: ("+delayed_dx+","+delayed_dy+")");
        logger.log(Level.INFO, "[" + super.getType() + "] delayed :: (" + delayed_dx + "," + delayed_dy + ")");

        //previousX += delayed_dx; // TODO BK try out
        //previousY += delayed_dy; // TODO BK try out   

        boolean force_enq = true;

        // Check mouse type
        if (mouseType == MOUSE_TYPE_SERIAL || mouseType == MOUSE_TYPE_SERIAL_WHEEL) {
            // Serial mouse: store data in internal mouse buffer
            this.storeBufferData(force_enq);
        }
        /* else {

            // PS/2 mouse
            // Scale down the motion
            if ((delta_x < -1) || (delta_x > 1))
                delta_x /= 2;
            if ((delta_y < -1) || (delta_y > 1))
                delta_y /= 2;

            if (!imMode) {
                delta_z = 0;
            }

            if (delta_x != 0 || delta_y != 0 || delta_z != 0) {
                logger.log(Level.CONFIG, "[" + super.getType() + "] mouse position changed: Dx=" + delta_x + ", Dy=" + delta_y + ", Dz=" + delta_z);
            }

            if ((buttonStatus != (buttonState & 0x7)) || delta_z != 0) {
                force_enq = true;
            }

            buttonStatus = (byte) (buttonState & 0x07);

            if (delta_x > 255) delta_x = 255;
            if (delta_y > 255) delta_y = 255;
            if (delta_x < -256) delta_x = -256;
            if (delta_y < -256) delta_y = -256;

            delayed_dx += delta_x;
            delayed_dy += delta_y;
            delayed_dz = delta_z;

            // TODO: why is this check necessary?
            if ((delayed_dx > 255) || (delayed_dx < -256) || (delayed_dy > 255) || (delayed_dy < -256)) {
                force_enq = true;
            }
            this.storeBufferData(force_enq);
        }
        */
    }

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
                logger.log(Level.WARNING, "[" + super.getType() + "] Invalid resolution cpmm");
        }

        return resolution;
    }

    @Override
    public synchronized boolean isDataAvailable() {
        return !(buffer.isEmpty());
    }

    @Override
    public synchronized byte getSerialData() {
        return buffer.isEmpty() ? -1 : buffer.poll();
    }

    @Override
    public synchronized void setSerialData(byte data) {
        buffer.offer(data);
    }
}
