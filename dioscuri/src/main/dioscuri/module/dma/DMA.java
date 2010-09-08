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

/*
 * Information used in this module was taken from:
 * - http://bochs.sourceforge.net/techspec/CMOS-reference.txt
 * - 
 */
package dioscuri.module.dma;

/*
 * This class is based on Bochs source code (dma.h, dma.cc}, see bochs.sourceforge.net for details;
 * Conversions from C++ to Java have been made; comments have been added
 * -Bram
 */

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import dioscuri.Emulator;
import dioscuri.exception.ModuleException;
import dioscuri.exception.ModuleUnknownPort;
import dioscuri.exception.ModuleWriteOnlyPortException;
import dioscuri.module.Module;
import dioscuri.module.ModuleCPU;
import dioscuri.module.ModuleDMA;
import dioscuri.module.ModuleMemory;
import dioscuri.module.ModuleMotherboard;

/**
 * An implementation of a DMA controller module.
 * 
 * @see Module
 * 
 *      Metadata module ********************************************
 *      general.type : dma general.name : DMA Controller general.architecture :
 *      Von Neumann general.description : Implements a standard DMA controller
 *      general.creator : Tessella Support Services, Koninklijke Bibliotheek,
 *      Nationaal Archief of the Netherlands general.version : 1.0
 *      general.keywords : DMA, PIO, ATA, IDE, 8237 general.relations :
 *      motherboard, memory general.yearOfIntroduction : general.yearOfEnding :
 *      general.ancestor : general.successor :
 * 
 * 
 */
@SuppressWarnings("unused")
public class DMA extends ModuleDMA {
    boolean busHoldAcknowledged; // Hold Acknowlege; CPU has relinquished
                                 // control of the system busses
    boolean terminalCountReached; // Terminal Count; generated when transfer is
                                  // complete and signals operation complete.
    byte[] ext_page_reg = new byte[16]; // Extra page registers. Can be
                                        // read/written using I/O ports, but is
                                        // otherwise unused.

    // Attributes
    // Create two DMA controllers - these are cascaded via channel 4.
    // Note on cascading: Unlike the IRQ controllers, DMA controllers are
    // counter-intuitively cascaded so that the
    // first ("master", or 0) has its Hold Request (HRQ) and Hold Acknowledge
    // (HLDA) connected to the DMA Request 4 (DREQ4)
    // and DMA Acknowledge 4 (DACK4) of the second ("slave" or 1).
    // This means that the HRQ resulting from DMA requests on channels 0 - 3 is
    // cascaded to DREQ4, which results in an HRQ from
    // the "slave" that is passed to the CPU. So the "slave" controller actually
    // raises the HRQ to the CPU!
    public DMAController[] controller = new DMAController[] {
            new DMAController(), new DMAController() };

    // Create two 4-channel handlers: 8-bit (controller 0) and 16-bit
    // (controller 1)
    public DMA8Handler[] dma8Handler = new DMA8Handler[4];
    public DMA16Handler[] dma16Handler = new DMA16Handler[4];

    // Relations
    private Emulator emu;
    private String[] moduleConnections = new String[] { "motherboard", "cpu",
            "memory" };
    private ModuleMotherboard motherboard;
    private ModuleMemory memory;
    private ModuleCPU cpu;

    // Toggles
    private boolean isObserved;
    private boolean debugMode;

    // Logging
    private static final Logger logger = Logger.getLogger(DMA.class.getName());

    // Constants
    // Module specifics
    public final static int MODULE_ID = 1;
    public final static String MODULE_TYPE = "dma";
    public final static String MODULE_NAME = "8237 DMA Controller";

    // Master/Slave identifiers
    private final static int MASTER_CTRL = 0;
    private final static int SLAVE_CTRL = 1;

    // I/O ports DMA controller 1
    private final static int PORT_DMA1_CH0_ADDRESS = 0x00;
    private final static int PORT_DMA1_CH0_COUNT = 0x01;
    private final static int PORT_DMA1_CH1_ADDRESS = 0x02;
    private final static int PORT_DMA1_CH1_COUNT = 0x03;
    private final static int PORT_DMA1_CH2_ADDRESS = 0x04;
    private final static int PORT_DMA1_CH2_COUNT = 0x05;
    private final static int PORT_DMA1_CH3_ADDRESS = 0x06;
    private final static int PORT_DMA1_CH3_COUNT = 0x07;
    private final static int PORT_DMA1_STATUS_CMD = 0x08;
    private final static int PORT_DMA1_REQUEST = 0x09;
    private final static int PORT_DMA1_MASK = 0x0A;
    private final static int PORT_DMA1_MODE = 0x0B;
    private final static int PORT_DMA1_CLEARBYTE = 0x0C;
    private final static int PORT_DMA1_TEMP_MASTER = 0x0D;
    private final static int PORT_DMA1_CLEARMASK = 0x0E;
    private final static int PORT_DMA1_WRITEMASK = 0x0F;

    // I/O ports DMA page registers
    private final static int PORT_EXTRA_PAGE_0 = 0x80;
    private final static int PORT_CHAN_2_ADDR_BYTE_2 = 0x81;
    private final static int PORT_CHAN_3_ADDR_BYTE_2 = 0x82;
    private final static int PORT_CHAN_1_ADDR_BYTE_2 = 0x83;
    private final static int PORT_EXTRA_PAGE_4 = 0x84;
    private final static int PORT_EXTRA_PAGE_5 = 0x85;
    private final static int PORT_EXTRA_PAGE_6 = 0x86;
    private final static int PORT_CHAN_0_ADDR_BYTE_2 = 0x87;
    private final static int PORT_EXTRA_PAGE_8 = 0x88;
    private final static int PORT_CHAN_6_ADDR_BYTE_2 = 0x89;
    private final static int PORT_CHAN_7_ADDR_BYTE_2 = 0x8A;
    private final static int PORT_CHAN_5_ADDR_BYTE_2 = 0x8B;
    private final static int PORT_EXTRA_PAGE_C = 0x8C;
    private final static int PORT_EXTRA_PAGE_D = 0x8D;
    private final static int PORT_EXTRA_PAGE_E = 0x8E;
    private final static int PORT_REFRESH_PAGE = 0x81;

    // I/O ports DMA controller 2
    private final static int PORT_DMA2_CH4_ADDRESS = 0xC0;
    private final static int PORT_DMA2_CH4_COUNT = 0xC2;
    private final static int PORT_DMA2_CH5_ADDRESS = 0xC4;
    private final static int PORT_DMA2_CH5_COUNT = 0xC6;
    private final static int PORT_DMA2_CH6_ADDRESS = 0xC8;
    private final static int PORT_DMA2_CH6_COUNT = 0xCA;
    private final static int PORT_DMA2_CH7_ADDRESS = 0xCC;
    private final static int PORT_DMA2_CH7_COUNT = 0xCE;
    private final static int PORT_DMA2_STATUS_CMD = 0xD0;
    private final static int PORT_DMA2_REQUEST = 0xD2;
    private final static int PORT_DMA2_MASK = 0xD4;
    private final static int PORT_DMA2_MODE = 0xD6;
    private final static int PORT_DMA2_CLEARBYTE = 0xD8;
    private final static int PORT_DMA2_TEMP_MASTER = 0xDA;
    private final static int PORT_DMA2_CLEARMASK = 0xDC;
    private final static int PORT_DMA2_WRITEMASK = 0xDE;

    private final static int FLOPPY_DMA_CHANNEL = 2;
    private final static int CASCADE_DMA_CHANNEL = 4;

    // Constructor

    /**
     * Class constructor
     * 
     * @param owner
     */
    public DMA(Emulator owner) {
        emu = owner;

        int chanNum, ctrlNum, i; // Counters

        // Initialise variables
        isObserved = false;
        debugMode = false;

        // Set DMA REQs and ACKs to 0
        for (ctrlNum = 0; ctrlNum < 2; ctrlNum++) {
            for (i = 0; i < 4; i++) {
                controller[ctrlNum].DRQ[i] = false;
                controller[ctrlNum].DACK[i] = false;
            }
        }

        // Set CPU bus holding, terminal count to false
        busHoldAcknowledged = false;
        terminalCountReached = false;

        // Set default values for DMA controllers
        for (ctrlNum = 0; ctrlNum < 2; ctrlNum++) {
            for (chanNum = 0; chanNum < 4; chanNum++) {
                controller[ctrlNum].channel[chanNum].mode.modeType = 0; // Demand
                                                                        // mode
                controller[ctrlNum].channel[chanNum].mode.addressDecrement = false; // Address
                                                                                    // increment
                controller[ctrlNum].channel[chanNum].mode.autoInitEnable = false; // Autoinit
                                                                                  // disabled
                controller[ctrlNum].channel[chanNum].mode.transferType = 0; // Verify
                controller[ctrlNum].channel[chanNum].baseAddress = 0;
                controller[ctrlNum].channel[chanNum].currentAddress = 0;
                controller[ctrlNum].channel[chanNum].baseCount = 0;
                controller[ctrlNum].channel[chanNum].currentCount = 0;
                controller[ctrlNum].channel[chanNum].pageRegister = 0;
                controller[ctrlNum].channel[chanNum].channelUsed = false; // Channel
                                                                          // not
                                                                          // in
                                                                          // use
            }
        }

        // Fill arrays with values
        Arrays.fill(dma8Handler, null);
        Arrays.fill(dma16Handler, null);
        Arrays.fill(ext_page_reg, (byte) 0);

        // Cascade controllers via channel 4.
        this.setCascadeChannel();

        logger.log(Level.INFO, "[" + MODULE_TYPE + "]"
                + " Module created successfully.");
    }

    // ******************************************************************************
    // Module Methods

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
        // Set connection for motherboard
        if (mod.getType() == Type.MOTHERBOARD) { //.equalsIgnoreCase("motherboard")) {
            this.motherboard = (ModuleMotherboard) mod;
            return true;
        }

        // Set connection for memory
        if (mod.getType() == Type.MEMORY) { //.equalsIgnoreCase("memory")) {
            this.memory = (ModuleMemory) mod;
            return true;
        }

        // Set connection for cpu
        if (mod.getType() == Type.CPU) { //.equalsIgnoreCase("cpu")) {
            this.cpu = (ModuleCPU) mod;
            return true;
        }
        // No connection has been established
        return false;
    }

    /**
     * Reset all parameters of module
     * 
     * @return boolean true if module has been reset successfully, false
     *         otherwise
     */
    public boolean reset() {
        // Register I/O ports 0x00 - 0x0F in I/O address space
        motherboard.setIOPort(PORT_DMA1_CH0_ADDRESS, this);
        motherboard.setIOPort(PORT_DMA1_CH0_COUNT, this);
        motherboard.setIOPort(PORT_DMA1_CH1_ADDRESS, this);
        motherboard.setIOPort(PORT_DMA1_CH1_COUNT, this);
        motherboard.setIOPort(PORT_DMA1_CH2_ADDRESS, this);
        motherboard.setIOPort(PORT_DMA1_CH2_COUNT, this);
        motherboard.setIOPort(PORT_DMA1_CH3_ADDRESS, this);
        motherboard.setIOPort(PORT_DMA1_CH3_COUNT, this);
        motherboard.setIOPort(PORT_DMA1_STATUS_CMD, this);
        motherboard.setIOPort(PORT_DMA1_REQUEST, this);
        motherboard.setIOPort(PORT_DMA1_MASK, this);
        motherboard.setIOPort(PORT_DMA1_MODE, this);
        motherboard.setIOPort(PORT_DMA1_CLEARBYTE, this);
        motherboard.setIOPort(PORT_DMA1_TEMP_MASTER, this);
        motherboard.setIOPort(PORT_DMA1_CLEARMASK, this);
        motherboard.setIOPort(PORT_DMA1_WRITEMASK, this);

        // Register I/O ports 0x80 - 0x8F in I/O address space
        motherboard.setIOPort(PORT_EXTRA_PAGE_0, this);
        motherboard.setIOPort(PORT_CHAN_2_ADDR_BYTE_2, this);
        motherboard.setIOPort(PORT_CHAN_3_ADDR_BYTE_2, this);
        motherboard.setIOPort(PORT_CHAN_1_ADDR_BYTE_2, this);
        motherboard.setIOPort(PORT_EXTRA_PAGE_4, this);
        motherboard.setIOPort(PORT_EXTRA_PAGE_5, this);
        motherboard.setIOPort(PORT_EXTRA_PAGE_6, this);
        motherboard.setIOPort(PORT_CHAN_0_ADDR_BYTE_2, this);
        motherboard.setIOPort(PORT_EXTRA_PAGE_8, this);
        motherboard.setIOPort(PORT_CHAN_6_ADDR_BYTE_2, this);
        motherboard.setIOPort(PORT_CHAN_7_ADDR_BYTE_2, this);
        motherboard.setIOPort(PORT_CHAN_5_ADDR_BYTE_2, this);
        motherboard.setIOPort(PORT_EXTRA_PAGE_C, this);
        motherboard.setIOPort(PORT_EXTRA_PAGE_D, this);
        motherboard.setIOPort(PORT_EXTRA_PAGE_E, this);
        motherboard.setIOPort(PORT_REFRESH_PAGE, this);

        // Register I/O ports 0xC0 - 0xDE in I/O address space
        motherboard.setIOPort(PORT_DMA2_CH4_ADDRESS, this);
        motherboard.setIOPort(PORT_DMA2_CH4_COUNT, this);
        motherboard.setIOPort(PORT_DMA2_CH5_ADDRESS, this);
        motherboard.setIOPort(PORT_DMA2_CH5_COUNT, this);
        motherboard.setIOPort(PORT_DMA2_CH6_ADDRESS, this);
        motherboard.setIOPort(PORT_DMA2_CH6_COUNT, this);
        motherboard.setIOPort(PORT_DMA2_CH7_ADDRESS, this);
        motherboard.setIOPort(PORT_DMA2_CH7_COUNT, this);
        motherboard.setIOPort(PORT_DMA2_STATUS_CMD, this);
        motherboard.setIOPort(PORT_DMA2_REQUEST, this);
        motherboard.setIOPort(PORT_DMA2_MASK, this);
        motherboard.setIOPort(PORT_DMA2_MODE, this);
        motherboard.setIOPort(PORT_DMA2_CLEARBYTE, this);
        motherboard.setIOPort(PORT_DMA2_TEMP_MASTER, this);
        motherboard.setIOPort(PORT_DMA2_CLEARMASK, this);
        motherboard.setIOPort(PORT_DMA2_WRITEMASK, this);

        // Reset the controllers individually
        resetController(MASTER_CTRL);
        resetController(SLAVE_CTRL);

        logger.log(Level.INFO, "[" + MODULE_TYPE + "]"
                + " Module has been reset");
        return true;
    }

    /**
     * Reset controller's mask, command register, status registers, and
     * flip-flop
     * 
     * @param ctrlNum
     *            Number of controller to be reset (0 or 1)
     */
    void resetController(int ctrlNum) {
        // Set entire mask register (default on reset)
        for (int i = 0; i < 4; i++) {
            controller[ctrlNum].mask[i] = 1;
        }
        controller[ctrlNum].commandRegister = 0; // Clear command register
        controller[ctrlNum].ctrlDisabled = false; // ... including bit 2
        controller[ctrlNum].statusRegister = 0; // Clear status register
        controller[ctrlNum].flipflop = false; // Reset flip-flop
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
     * Returns information of this module
     * 
     * @return string String of current information about this module
     * 
     * @see Module
     */
    public String getDump() {
        String dump = "";
        String ret = "\r\n";

        dump += "Current registered DMA channels: " + ret;
        for (int ctrlNum = 0; ctrlNum < 2; ctrlNum++) {
            for (int chanNum = 0; chanNum < 4; chanNum++) {
                if (controller[ctrlNum].channel[chanNum].channelUsed) // Channel
                                                                      // used
                {
                    if (ctrlNum == 0) // Check for 8-bit handlers
                    {
                        dump += " Channel " + chanNum + ": "
                                + dma8Handler[chanNum].owner + "\n";
                    } else
                    // Check for 16-bit handlers
                    {
                        dump += " Channel " + (chanNum + 4) + ": "
                                + dma16Handler[chanNum].owner + "\n";
                    }
                }
            }
        }

        return dump;
    }

    // ******************************************************************************
    // ModuleDevice Methods

    /**
     * Retrieve the interval between subsequent updates
     * 
     * @return int interval in microseconds
     */
    public int getUpdateInterval() {
        return -1;
    }

    /**
     * Defines the interval between subsequent updates
     * 
     */
    public void setUpdateInterval(int interval) {
    }

    /**
     * Update device
     * 
     */
    public void update() {
    }

    /**
     * Return a byte from I/O address space at given port
     * 
     * @return byte containing the data at given I/O address port
     * @throws ModuleUnknownPort
     */
    public byte getIOPortByte(int portAddress) throws ModuleUnknownPort {
        byte returnValue;
        int chanNum;

        logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]" + " Read from port 0x"
                + Integer.toHexString(portAddress).toUpperCase());

        // DMA is currently only used for the floppy drive, so if no floppy I/O
        // is used don't bother with DMA; return bogus value
        if (dma8Handler[FLOPPY_DMA_CHANNEL] == null)
            return ((byte) 0xFF);

        // Determine which controller is being read
        int ctrlNum = (portAddress >= 0xC0 ? SLAVE_CTRL : MASTER_CTRL);

        switch (portAddress) {
        // Read current address byte; depending on flipflop state,
        // this is either the high (1) or low (0) byte
        case 0x00: // Controller 0, channel 0
        case 0x02: // Controller 0, channel 1
        case 0x04: // Controller 0, channel 2
        case 0x06: // Controller 0, channel 3
        case 0xC0: // Controller 1, channel 0
        case 0xC4: // Controller 1, channel 1
        case 0xC8: // Controller 1, channel 2
        case 0xCC: // Controller 1, channel 3
            chanNum = (portAddress >> (1 + ctrlNum)) & 0x03; // Determine
                                                             // channel number
                                                             // from above port
                                                             // addresses
            if (controller[ctrlNum].flipflop) {
                // Return high byte, and 'flip' flipflop
                controller[ctrlNum].flipflop = !controller[ctrlNum].flipflop;
                return (byte) (controller[ctrlNum].channel[chanNum].currentAddress >> 8);
            } else {
                // Return low byte, and 'flip' flipflop
                controller[ctrlNum].flipflop = !controller[ctrlNum].flipflop;
                return (byte) (controller[ctrlNum].channel[chanNum].currentAddress & 0xFF);
            }

            // Read current word count byte; depending on flipflop state,
            // this is either the high (1) or low (0) byte
        case 0x01: // Controller 0, channel 0
        case 0x03: // Controller 0, channel 1
        case 0x05: // Controller 0, channel 2
        case 0x07: // Controller 0, channel 3
        case 0xC2: // Controller 1, channel 0
        case 0xC6: // Controller 1, channel 1
        case 0xCA: // Controller 1, channel 2
        case 0xCE: // Controller 1, channel 3
            chanNum = (portAddress >> (1 + ctrlNum)) & 0x03; // Determine
                                                             // channel number
                                                             // from above port
                                                             // addresses
            if (controller[ctrlNum].flipflop) {
                // Return high byte, and 'flip' flipflop
                controller[ctrlNum].flipflop = !controller[ctrlNum].flipflop;
                return (byte) (controller[ctrlNum].channel[chanNum].currentCount >> 8);
            } else {
                // Return low byte, and 'flip' flipflop
                controller[ctrlNum].flipflop = !controller[ctrlNum].flipflop;
                return (byte) (controller[ctrlNum].channel[chanNum].currentCount & 0xFF);
            }

            // Read controller's status register
            // Reading this register clears the TC flags (bits 0-3)
        case 0x08: // Controller 0
        case 0xD0: // Controller 1
            returnValue = controller[ctrlNum].statusRegister;
            controller[ctrlNum].statusRegister &= 0xF0; // Clear TC flags on
                                                        // read
            return returnValue;

            // Read controller's temporary register, used in memory-to-memory
            // transfers
            // Unused register, so return 0 as default value
        case 0x0D: // Controller 0
        case 0xDA: // Controller 1
            logger
                    .log(
                            Level.CONFIG,
                            "["
                                    + MODULE_TYPE
                                    + "]"
                                    + " Controller ["
                                    + ctrlNum
                                    + "] temporary register (unused) read. Returned 0 (default)");
            return 0;

            // Read controller's page register
        case 0x81: // Controller 0, channel 2
            return controller[ctrlNum].channel[2].pageRegister;
        case 0x82: // Controller 0, channel 3
            return controller[ctrlNum].channel[3].pageRegister;
        case 0x83: // Controller 0, channel 1
            return controller[ctrlNum].channel[1].pageRegister;
        case 0x87: // Controller 0, channel 0
            return controller[ctrlNum].channel[0].pageRegister;

        case 0x89: // Controller 1, channel 2
            return controller[ctrlNum].channel[2].pageRegister;
        case 0x8A: // Controller 1, channel 3
            return controller[ctrlNum].channel[3].pageRegister;
        case 0x8B: // Controller 1, channel 1
            return controller[ctrlNum].channel[1].pageRegister;
        case 0x8F: // Controller 1, channel 0
            return controller[ctrlNum].channel[0].pageRegister;

            // Read extra page registers (temporary storage)
            // This are only read and written via I/O but serve no other use
        case 0x0080:
        case 0x0084:
        case 0x0085:
        case 0x0086:
        case 0x0088:
        case 0x008C:
        case 0x008D:
        case 0x008E:
            return ext_page_reg[portAddress & 0x0F];

            // Read all Mask bits (only in Intel 82374)
        case 0x0F: // Controller 0
        case 0xDE: // Controller 1
            returnValue = (byte) (controller[ctrlNum].mask[0]
                    | (controller[ctrlNum].mask[1] << 1)
                    | (controller[ctrlNum].mask[2] << 2) | (controller[ctrlNum].mask[3] << 3));
            return (byte) (0xF0 | returnValue);

        default:
            throw new ModuleUnknownPort("[" + MODULE_TYPE + "]"
                    + " does not recognise port 0x"
                    + Integer.toHexString(portAddress).toUpperCase());
        }
    }

    /**
     * Set a byte in I/O address space at given port
     * 
     * @throws ModuleUnknownPort
     */
    public void setIOPortByte(int portAddress, byte data)
            throws ModuleUnknownPort {
        int chanNum; // Controller channel number

        logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]"
                + " I/O write to port 0x"
                + Integer.toHexString(portAddress).toUpperCase() + ": 0x"
                + Integer.toHexString(data));

        // DMA is currently only used for the floppy drive, so if no floppy I/O
        // is used don't bother with DMA; just return
        if (dma8Handler[FLOPPY_DMA_CHANNEL] == null)
            return;

        // Determine master/slave controller
        int ctrlNum = (byte) (portAddress >= 0xC0 ? SLAVE_CTRL : MASTER_CTRL);

        switch (portAddress) {
        // Write current and base address byte; depending on flipflop state,
        // this is either the high (1) or low (0) byte
        case 0x00: // Controller 0, channel 0
        case 0x02: // Controller 0, channel 1
        case 0x04: // Controller 0, channel 2
        case 0x06: // Controller 0, channel 3
        case 0xC0: // Controller 0, channel 0
        case 0xC4: // Controller 0, channel 1
        case 0xC8: // Controller 0, channel 2
        case 0xCC: // Controller 0, channel 3
            chanNum = (portAddress >> (1 + ctrlNum)) & 0x03; // Determine
                                                             // channel number
                                                             // from above port
                                                             // addresses
            logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]" + "  Controller "
                    + ctrlNum + ",  channel " + chanNum
                    + " base and current address set.");
            if (controller[ctrlNum].flipflop) {
                // High byte; mask sign because a cast from byte occurs
                controller[ctrlNum].channel[chanNum].baseAddress |= ((((int) data) & 0xFF) << 8);
                controller[ctrlNum].channel[chanNum].currentAddress |= ((((int) data) & 0xFF) << 8);
                logger
                        .log(
                                Level.CONFIG,
                                "["
                                        + MODULE_TYPE
                                        + "]"
                                        + "    base = 0x"
                                        + Integer
                                                .toHexString(
                                                        controller[ctrlNum].channel[chanNum].baseAddress)
                                                .toUpperCase());
                logger
                        .log(
                                Level.CONFIG,
                                "["
                                        + MODULE_TYPE
                                        + "]"
                                        + "    curr = 0x"
                                        + Integer
                                                .toHexString(
                                                        controller[ctrlNum].channel[chanNum].currentAddress)
                                                .toUpperCase());
            } else {
                // Lower byte; mask sign because a cast from byte occurs
                controller[ctrlNum].channel[chanNum].baseAddress = ((int) data) & 0xFF;
                controller[ctrlNum].channel[chanNum].currentAddress = ((int) data) & 0xFF;
            }
            // 'Flip' flipflop
            controller[ctrlNum].flipflop = !controller[ctrlNum].flipflop;
            return;

            // Write current word count byte; depending on flipflop state,
            // this is either the high (1) or low (0) byte
        case 0x01:
        case 0x03:
        case 0x05:
        case 0x07:
        case 0xC2:
        case 0xC6:
        case 0xCA:
        case 0xCE:
            chanNum = (portAddress >> (1 + ctrlNum)) & 0x03; // Determine
                                                             // channel number
                                                             // from above port
                                                             // addresses
            logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]" + "  Controller "
                    + ctrlNum + ",  channel " + chanNum
                    + " base and current count set.");
            if (controller[ctrlNum].flipflop) {
                // High byte; mask sign because a cast from byte occurs
                controller[ctrlNum].channel[chanNum].baseCount |= ((((int) data) & 0xFF) << 8);
                controller[ctrlNum].channel[chanNum].currentCount |= ((((int) data) & 0xFF) << 8);
                logger.log(Level.CONFIG, "["
                        + MODULE_TYPE
                        + "]"
                        + "    base = 0x"
                        + Integer.toHexString(
                                controller[ctrlNum].channel[chanNum].baseCount)
                                .toUpperCase());
                logger
                        .log(
                                Level.CONFIG,
                                "["
                                        + MODULE_TYPE
                                        + "]"
                                        + "    curr = 0x"
                                        + Integer
                                                .toHexString(
                                                        controller[ctrlNum].channel[chanNum].currentCount)
                                                .toUpperCase());
            } else {
                // Lower byte; mask sign because a cast from byte occurs
                controller[ctrlNum].channel[chanNum].baseCount = (((int) data) & 0xFF);
                controller[ctrlNum].channel[chanNum].currentCount = (((int) data) & 0xFF);
            }
            // 'Flip' flipflop
            controller[ctrlNum].flipflop = !controller[ctrlNum].flipflop;
            return;

            // Write controller's command register
            // The only supported functionality here is enable/disable
            // controller (bit 2), report all other commands as unsupported
        case 0x08: // Controller 0
        case 0xD0: // Controller 1
            if ((data & 0xFB) != 0x00) // Attempting to enter commands other
                                       // than [en|dis]abling controller
            {
                logger
                        .log(
                                Level.WARNING,
                                "["
                                        + MODULE_TYPE
                                        + "]"
                                        + " command register functionality setting not supported");
            }
            controller[ctrlNum].commandRegister = data;
            controller[ctrlNum].ctrlDisabled = ((data >> 2) & 0x01) == 1 ? true
                    : false;
            controlHoldRequest(ctrlNum); // Set the HoldRequest (HRQ)
                                         // accordingly
            return;

            // Set/Clear DMA request bit in status register
            // Bit 7-3: reserved (0)
            // Bit 2 : 0 - Clear request bit; 1 - Set request bit
            // Bit 1-0: Channel number - 00 channel 0 select
            // 01 channel 1 select
            // 10 channel 2 select
            // 11 channel 3 select
            // Note: a write to 0x0D / 0xDA clears this register
        case 0x09: // Controller 0
        case 0xD2: // Controller 1
            chanNum = data & 0x03; // Determine channel number from bits 0-1
            if ((data & 0x04) != 0) // Check bit 2 for set/clear
            {
                // Set request bit
                controller[ctrlNum].statusRegister |= (1 << (chanNum + 4));
                logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]"
                        + " Controller " + ctrlNum
                        + ": Set DMA request bit for channel " + chanNum);
            } else {
                // Clear request bit
                controller[ctrlNum].statusRegister &= ~(1 << (chanNum + 4));
                logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]"
                        + " Controller " + ctrlNum
                        + ": Clear DMA request bit for channel " + chanNum);
            }
            controlHoldRequest(ctrlNum); // Set the HoldRequest (HRQ)
                                         // accordingly
            return;

            // Set/Clear mask register in status register
            // Bit 7-3: reserved (0)
            // Bit 2 : 0 - Clear mask bit; 1 - Set mask bit
            // Bit 1-0: Channel number - 00 channel 0 select
            // 01 channel 1 select
            // 10 channel 2 select
            // 11 channel 3 select
        case 0x0A:
        case 0xD4:
            chanNum = data & 0x03; // Determine channel number from bits 0-1
            controller[ctrlNum].mask[chanNum] = (byte) ((data & 0x04) > 0 ? 1
                    : 0); // Set/clear appropriate channel's mask
            logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]" + " Controller "
                    + ctrlNum + ", channel " + chanNum + ": set mask as "
                    + ((data & 0x04) > 0 ? 1 : 0) + "; mask now=0x"
                    + controller[ctrlNum].mask[chanNum]);
            controlHoldRequest(ctrlNum); // Set the HoldRequest (HRQ)
                                         // accordingly
            return;

            // Write controller's channel's mode register
        case 0x0B: // Controller 0
        case 0xD6: // Controller 1
            chanNum = data & 0x03; // Determine channel number from bits 0-1
            controller[ctrlNum].channel[chanNum].mode.modeType = (byte) ((data >> 6) & 0x03);
            controller[ctrlNum].channel[chanNum].mode.addressDecrement = ((data >> 5) & 0x01) == 1 ? true
                    : false;
            controller[ctrlNum].channel[chanNum].mode.autoInitEnable = ((data >> 4) & 0x01) == 1 ? true
                    : false;
            controller[ctrlNum].channel[chanNum].mode.transferType = (byte) ((data >> 2) & 0x03);
            logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]" + " Controller "
                    + ctrlNum + ", channel " + chanNum
                    + ": mode register set to 0x"
                    + Integer.toHexString(data).toUpperCase());
            return;

            // Clear flip-flop
        case 0x0C: // Controller 0
        case 0xD8: // Controller 1
            logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]" + " Controller "
                    + ctrlNum + ": flip-flop cleared");
            controller[ctrlNum].flipflop = false;
            return;

            // Master clear
            // Similar to hardware reset: Command, Status, Request, and
            // Temporary registers, and flip-flop are cleared;
            // The Mask register is set (disabling all channels)
        case 0x0D: // Controller 0
        case 0xDA: // Controller 1
            logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]" + " Controller "
                    + ctrlNum + ": master clear (reset)");
            resetController(ctrlNum);
            return;

            // Clear mask register
        case 0x0E: // Controller 0
        case 0xDC: // Controller 1
            logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]" + " Controller "
                    + ctrlNum + ": clear mask register");
            controller[ctrlNum].mask[0] = 0;
            controller[ctrlNum].mask[1] = 0;
            controller[ctrlNum].mask[2] = 0;
            controller[ctrlNum].mask[3] = 0;
            controlHoldRequest(ctrlNum); // Set the HoldRequest (HRQ)
                                         // accordingly
            return;

            // Write mask register
        case 0x0F: // Controller 0
        case 0xDE: // Controller 1
            logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]" + " Controller "
                    + ctrlNum + ": write mask register");
            controller[ctrlNum].mask[0] = (byte) (data & 0x01);
            data >>= 1;
            controller[ctrlNum].mask[1] = (byte) (data & 0x01);
            data >>= 1;
            controller[ctrlNum].mask[2] = (byte) (data & 0x01);
            data >>= 1;
            controller[ctrlNum].mask[3] = (byte) (data & 0x01);
            controlHoldRequest(ctrlNum); // Set the HoldRequest (HRQ)
                                         // accordingly
            return;

            // Write page registers
            // Address bits A16-A23 for DMA channel
        case 0x81: // Controller 0, channel 2
            controller[ctrlNum].channel[2].pageRegister = data;
            logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]" + " Controller "
                    + ctrlNum + ": page register 2 = 0x"
                    + Integer.toHexString(data).toUpperCase());
            return;
        case 0x82: // Controller 0, channel 3
            controller[ctrlNum].channel[3].pageRegister = data;
            logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]" + " Controller "
                    + ctrlNum + ": page register 3 = 0x"
                    + Integer.toHexString(data).toUpperCase());
            return;
        case 0x83: // Controller 0, channel 1
            controller[ctrlNum].channel[1].pageRegister = data;
            logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]" + " Controller "
                    + ctrlNum + ": page register 1 = 0x"
                    + Integer.toHexString(data).toUpperCase());
            return;
        case 0x87: // Controller 0, channel 0
            controller[ctrlNum].channel[0].pageRegister = data;
            logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]" + " Controller "
                    + ctrlNum + ": page register 0 = 0x"
                    + Integer.toHexString(data).toUpperCase());
            return;

        case 0x89: // Controller 1, channel 2
            controller[ctrlNum].channel[2].pageRegister = data;
            logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]" + " Controller "
                    + ctrlNum + ": page register 2 = 0x"
                    + Integer.toHexString(data).toUpperCase());
            return;
        case 0x8A: // Controller 1, channel 3
            controller[ctrlNum].channel[3].pageRegister = data;
            logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]" + " Controller "
                    + ctrlNum + ": page register 3 = 0x"
                    + Integer.toHexString(data).toUpperCase());
            return;
        case 0x8B: // Controller 1, channel 1
            controller[ctrlNum].channel[1].pageRegister = data;
            logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]" + " Controller "
                    + ctrlNum + ": page register 1 = 0x"
                    + Integer.toHexString(data).toUpperCase());
            return;
        case 0x8F: // Controller 1, channel 0
            controller[ctrlNum].channel[0].pageRegister = data;
            logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]" + " Controller "
                    + ctrlNum + ": page register 0 = 0x"
                    + Integer.toHexString(data).toUpperCase());
            return;

            // Write extra page registers (temporary storage)
            // This are only read and written via I/O but serve no other use
        case 0x0080:
        case 0x0084:
        case 0x0085:
        case 0x0086:
        case 0x0088:
        case 0x008C:
        case 0x008D:
        case 0x008E:
            ext_page_reg[portAddress & 0x0F] = data;
            return;

        default:
            throw new ModuleUnknownPort("[" + MODULE_TYPE + "]"
                    + " does not recognise port 0x"
                    + Integer.toHexString(portAddress).toUpperCase());
        }
    }

    /**
     * Return a word from I/O address space at given port
     * 
     * @return byte[] containing the data at given I/O address port
     * @throws ModuleUnknownPort
     */
    public byte[] getIOPortWord(int portAddress) throws ModuleUnknownPort {
        byte[] data = new byte[2];
        data[0] = getIOPortByte(portAddress);
        data[1] = getIOPortByte(portAddress);

        return data;
    }

    /**
     * Set a word in I/O address space at given port
     * 
     * @throws ModuleUnknownPort
     */
    public void setIOPortWord(int portAddress, byte[] dataWord)
            throws ModuleUnknownPort {
        setIOPortByte(portAddress, dataWord[1]);
        setIOPortByte(portAddress, dataWord[0]);
        return;
    }

    /**
     * Return a doubleword from I/O address space at given port
     * 
     * @return byte[] containing the data at given I/O address port
     */
    public byte[] getIOPortDoubleWord(int portAddress)
            throws ModuleWriteOnlyPortException {
        logger.log(Level.WARNING, "[" + MODULE_TYPE + "]"
                + " -> IN command (double word) to port "
                + Integer.toHexString(portAddress).toUpperCase() + " received");
        logger.log(Level.WARNING, "[" + MODULE_TYPE + "]"
                + " -> Returned default value 0xFFFFFFFF to eAX");

        // Return dummy value 0xFFFFFFFF
        return new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
    }

    /**
     * Set a doubleword in I/O address space at given port
     * 
     * @throws ModuleUnknownPort
     */
    public void setIOPortDoubleWord(int portAddress, byte[] dataDoubleWord)
            throws ModuleUnknownPort {
        logger.log(Level.WARNING, "[" + MODULE_TYPE + "]"
                + " OUT command (double word) to port "
                + Integer.toHexString(portAddress).toUpperCase()
                + " received. No action taken.");

        // Do nothing and just return okay
        return;
    }

    // ******************************************************************************
    // ModuleDMA methods

    /**
     * Allows a device to register an 8-bit DMA Handler providing
     * implementations for the ReadFromMem and WriteToMem
     * 
     * @return true if succesfully registered, false otherwise
     */
    public boolean registerDMAChannel(int chanNum, DMA8Handler dma8handler) {
        if (chanNum > 3) {
            logger.log(Level.SEVERE, "[" + MODULE_TYPE + "]"
                    + " registerDMA8Channel: invalid channel number: "
                    + chanNum);
            return false; // Fail
        }
        if (controller[0].channel[chanNum].channelUsed) {
            logger.log(Level.SEVERE, "[" + MODULE_TYPE + "]"
                    + " registerDMA8Channel: channel " + chanNum
                    + " already in use.");
            return false; // Fail
        }
        logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]" + " Channel "
                + chanNum + " (8-bit) used by " + dma8handler.owner);
        dma8Handler[chanNum] = dma8handler;
        controller[0].channel[chanNum].channelUsed = true;
        return true; // OK
    }

    /**
     * Allows a device to register an 16-bit DMA Handler providing
     * implementations for the ReadFromMem and WriteToMem
     * 
     * @return true if succesfully registered, false otherwise
     */
    public boolean registerDMAChannel(int chanNum, DMA16Handler dma16handler) {
        if (chanNum < 4 || chanNum > 7) {
            logger.log(Level.SEVERE, "[" + MODULE_TYPE + "]"
                    + " registerDMA16Channel: invalid channel number: "
                    + chanNum);
            return false; // Fail
        }
        if (controller[1].channel[chanNum - 4].channelUsed) {
            logger.log(Level.SEVERE, "[" + MODULE_TYPE + "]"
                    + " registerDMA16Channel: channel " + chanNum
                    + " already in use.");
            return false; // Fail
        }
        logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]" + " Channel "
                + chanNum + " (16-bit) used by " + dma16handler.owner);
        dma16Handler[chanNum - 4] = dma16handler;
        controller[1].channel[chanNum - 4].channelUsed = true;
        return true; // OK
    }

    /**
     * Allows a device to unregister a previously registered handler
     * 
     * @param chanNum
     * @return true if succesfully unregistered, false otherwise
     * 
     */
    public boolean unregisterDMAChannel(int chanNum) {
        int ctrlNum = (chanNum > 3) ? SLAVE_CTRL : MASTER_CTRL;
        controller[ctrlNum].channel[chanNum & 0x03].channelUsed = false;
        logger.log(Level.INFO, "[" + MODULE_TYPE + "]" + " Channel " + chanNum
                + " no longer used");
        return true;
    }

    /**
     * Getter function for the Terminal Count Reached of the current DMA request
     * 
     * @return Terminal Count Reached of the current DMA request
     */
    public boolean isTerminalCountReached() {
        return terminalCountReached;
    }

    /**
     * Sets the DMA Requests in the corresponding controller's status register,
     * and initiates the handling of Hold Requests
     * 
     * @param chanNum
     *            Channel requesting a DMA transfer
     * @param dmaRequest
     *            Set request (true); clear request (false)
     */
    public void setDMARequest(int chanNum, boolean dmaRequest) {
        int dmaFloor, dmaCeiling; // Memory boundaries of DMA request
        int ctrlNum;

        if (chanNum > 7) {
            logger.log(Level.SEVERE, "[" + MODULE_TYPE + "]"
                    + " setDMARequest(): channel " + chanNum
                    + " not connected to any device");
        }

        ctrlNum = chanNum > 3 ? SLAVE_CTRL : MASTER_CTRL; // Determine
                                                          // controller that
                                                          // should handle DMA
                                                          // request

        controller[ctrlNum].DRQ[chanNum & 0x03] = dmaRequest; // Set DMA Request
                                                              // in the
                                                              // controller

        // Check if the DMA Request came from a registered device
        if (!controller[ctrlNum].channel[chanNum & 0x03].channelUsed) {
            logger.log(Level.SEVERE, "[" + MODULE_TYPE + "]"
                    + " setDMARequest(): channel " + chanNum
                    + " not connected to any device");
        }

        chanNum &= 0x03;

        // Check if a DMA request is to be set/cleared
        if (!dmaRequest) {
            // DMA request is cleared, so exit here
            logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]"
                    + " setDMARequest(): val == 0");
            // Clear statusRegister request bit
            controller[ctrlNum].statusRegister &= ~(1 << (chanNum + 4));

            // Set the Hold Request (HRQ) accordingly
            controlHoldRequest(ctrlNum);
            return;
        }

        logger.log(Level.CONFIG, "["
                + MODULE_TYPE
                + "]"
                + " mask["
                + chanNum
                + "]: 0x"
                + Integer.toHexString(controller[0].mask[chanNum])
                        .toUpperCase());
        logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]" + " flipflop: "
                + ((Boolean) controller[0].flipflop).toString());
        logger.log(Level.CONFIG, "["
                + MODULE_TYPE
                + "]"
                + " statusRegister: 0x"
                + Integer.toHexString(controller[0].statusRegister)
                        .toUpperCase());
        logger.log(Level.CONFIG, "["
                + MODULE_TYPE
                + "]"
                + " modeType: 0x"
                + Integer.toHexString(
                        controller[0].channel[chanNum].mode.modeType)
                        .toUpperCase());
        logger
                .log(
                        Level.CONFIG,
                        "["
                                + MODULE_TYPE
                                + "]"
                                + " addressDecrement: "
                                + ((Boolean) controller[0].channel[chanNum].mode.addressDecrement)
                                        .toString());
        logger
                .log(
                        Level.CONFIG,
                        "["
                                + MODULE_TYPE
                                + "]"
                                + " autoInitEnable: "
                                + ((Boolean) controller[0].channel[chanNum].mode.autoInitEnable)
                                        .toString());
        logger.log(Level.CONFIG, "["
                + MODULE_TYPE
                + "]"
                + " transferType: 0x"
                + Integer.toHexString(
                        controller[0].channel[chanNum].mode.transferType)
                        .toUpperCase());
        logger.log(Level.CONFIG, "["
                + MODULE_TYPE
                + "]"
                + " baseAddress: 0x"
                + Integer.toHexString(
                        controller[0].channel[chanNum].baseAddress)
                        .toUpperCase());
        logger.log(Level.CONFIG, "["
                + MODULE_TYPE
                + "]"
                + " currentAddress: 0x"
                + Integer.toHexString(
                        controller[0].channel[chanNum].currentAddress)
                        .toUpperCase());
        logger.log(Level.CONFIG, "["
                + MODULE_TYPE
                + "]"
                + " baseCount: 0x"
                + Integer.toHexString(controller[0].channel[chanNum].baseCount)
                        .toUpperCase());
        logger.log(Level.CONFIG, "["
                + MODULE_TYPE
                + "]"
                + " currentCount: 0x"
                + Integer.toHexString(
                        controller[0].channel[chanNum].currentCount)
                        .toUpperCase());
        logger.log(Level.CONFIG, "["
                + MODULE_TYPE
                + "]"
                + " pageReg: 0x"
                + Integer.toHexString(
                        controller[0].channel[chanNum].pageRegister)
                        .toUpperCase());

        // Set the request in the status register
        controller[ctrlNum].statusRegister |= (1 << (chanNum + 4));

        // Check if the DMA Mode type is supported
        if ((controller[ctrlNum].channel[chanNum].mode.modeType != DMAModeRegister.DMA_MODE_SINGLE)
                && (controller[ctrlNum].channel[chanNum].mode.modeType != DMAModeRegister.DMA_MODE_DEMAND)
                && (controller[ctrlNum].channel[chanNum].mode.modeType != DMAModeRegister.DMA_MODE_CASCADE)) {
            logger.log(Level.SEVERE, "["
                    + MODULE_TYPE
                    + "]"
                    + " setDMARequest(): mode_type(0x"
                    + Integer.toHexString(
                            controller[ctrlNum].channel[chanNum].mode.modeType)
                            .toUpperCase() + " not supported");
        }

        // Determine the lower boundary of the DMA transfer
        dmaFloor = (controller[ctrlNum].channel[chanNum].pageRegister << 16)
                | (controller[ctrlNum].channel[chanNum].baseAddress << ctrlNum);

        // Depending on the direction of transfer in memory (up/down), determine
        // the upper boundary of the DMA transfer
        if (controller[ctrlNum].channel[chanNum].mode.addressDecrement) {
            dmaCeiling = dmaFloor
                    - (controller[ctrlNum].channel[chanNum].baseCount << ctrlNum);
        } else {
            dmaCeiling = dmaFloor
                    + (controller[ctrlNum].channel[chanNum].baseCount << ctrlNum);
        }

        // Ensure these boundaries are within accepted boundaries (should this
        // be done by memory, and why these values???)
        if ((dmaFloor & (0x7fff0000 << ctrlNum)) != (dmaCeiling & (0x7fff0000 << ctrlNum))) {
            logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]" + " dmaFloor = 0x"
                    + Integer.toHexString(dmaFloor).toUpperCase());
            logger.log(Level.CONFIG, "["
                    + MODULE_TYPE
                    + "]"
                    + " dmaBaseCount = 0x"
                    + Integer.toHexString(
                            controller[ctrlNum].channel[chanNum].baseCount)
                            .toUpperCase());
            logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]"
                    + " dmaCeiling = 0x"
                    + Integer.toHexString(dmaCeiling).toUpperCase());
            logger.log(Level.SEVERE, "[" + MODULE_TYPE + "]"
                    + " request outside " + (64 << ctrlNum) + "k boundary");
        }

        // Set the Hold Request (HRQ) accordingly
        controlHoldRequest(ctrlNum);
    }

    // ******************************************************************************
    // Custom methods

    /**
     * Handles the Hold Request (HRQ)<BR>
     * Based on the controller origin (ctrlNum), the Hold request is passed to
     * the CPU (if the origin is slave), or cascaded via DREQ4 to the slave
     * controller (if the origin is master), to be eventually set/cleared
     * depending on the DMA request status.
     * 
     * @param ctrlNum
     *            Controller (master [0] /slave [1]) from where the Hold Request
     *            originated
     */
    private void controlHoldRequest(int ctrlNum) {
        int chanNum;

        // Do nothing if controller is disabled
        if (controller[ctrlNum].ctrlDisabled)
            return;

        // Clear HoldRequest if no DMA Request is pending
        if ((controller[ctrlNum].statusRegister & 0xF0) == 0) {
            if (ctrlNum != 0) // Any Hold Request from the master are cascaded
                              // via the slave
            {
                cpu.setHoldRequest(false, this); // Request originated from
                                                 // slave, so clear HRQ here
            } else {
                setDMARequest(4, false); // Request originated from master, so
                                         // cascade via slave first
            }
            return;
        }

        // DMA Request is pending, so act on it: select highest priority channel
        for (chanNum = 0; chanNum < 4; chanNum++) {
            // Check statusRegister bits 7-4, and ensure the corresponding mask
            // bit is clear
            if (((controller[ctrlNum].statusRegister & (1 << (chanNum + 4))) != 0)
                    && (controller[ctrlNum].mask[chanNum] == 0)) {
                if (ctrlNum != 0) // Any Hold Request from the master are
                                  // cascaded via the slave
                {
                    cpu.setHoldRequest(true, this); // Request originated from
                                                    // slave, so set HRQ here
                } else {
                    setDMARequest(4, true); // Request originated from master,
                                            // so cascade via slave first
                }
                break;
            }
        }
    }

    /**
     * Control has been relinquished of the system busses<BR>
     * DMA now has control over the system busses, so the highest priority DMA
     * channel that scheduled a request is located and after setting up the
     * necessary parameters (address, count, memory), the DMA transfer is
     * initiated
     * 
     * 
     */
    public void acknowledgeBusHold() {
        int ctrlNum = MASTER_CTRL; // Controller number (master/slave)
        int chanNum; // DMA channel number
        int memoryAddress; // Address in memory data is written to/read from
        boolean countExpired = false; // Extra count variable to check terminal
                                      // count (TC)

        // CPU acknowledged hold, so set HLDA
        busHoldAcknowledged = true;

        // Hold is acknowledged, so act on it: select highest priority channel
        // in slave controller
        for (chanNum = 0; chanNum < 4; chanNum++) {
            // Check statusRegister bits 7-4 (requests), and ensure the
            // corresponding mask bit is clear
            if (((controller[SLAVE_CTRL].statusRegister & (1 << (chanNum + 4))) != 0)
                    && (controller[SLAVE_CTRL].mask[chanNum] == 0)) {
                ctrlNum = SLAVE_CTRL;
                break;
            }
        }

        // Check cascade channel in slave to see if HRQ came from master; if so,
        // select highest priority channel in master controller
        if (chanNum == 0) {
            controller[SLAVE_CTRL].DACK[0] = true; // Acknowledge DMA Request in
                                                   // slave controller
            for (chanNum = 0; chanNum < 4; chanNum++) {
                if (((controller[MASTER_CTRL].statusRegister & (1 << (chanNum + 4))) != 0)
                        && (controller[0].mask[chanNum] == 0)) {
                    ctrlNum = MASTER_CTRL;
                    break;
                }
            }
        }

        logger
                .log(
                        Level.INFO,
                        "["
                                + MODULE_TYPE
                                + "]"
                                + " Hold ACK: OK in response to DRQ("
                                + chanNum
                                + "), address 0x"
                                + Integer
                                        .toHexString(
                                                controller[ctrlNum].channel[chanNum].currentAddress & 0xFF)
                                        .toUpperCase());

        // Determine memory address - this is stored in the DMA channel
        memoryAddress = ((controller[ctrlNum].channel[chanNum].pageRegister << 16) | (controller[ctrlNum].channel[chanNum].currentAddress << ctrlNum));

        logger.log(Level.INFO, "BaseAddress="
                + controller[ctrlNum].channel[chanNum].baseAddress
                + ", currentAddress="
                + controller[ctrlNum].channel[chanNum].currentAddress
                + ", memoryAddress=" + memoryAddress);

        controller[ctrlNum].DACK[chanNum] = true; // Acknowledge DMA Request

        // Decrease count and inc/dec current address depending on mode
        if (controller[ctrlNum].channel[chanNum].mode.addressDecrement) {
            controller[ctrlNum].channel[chanNum].currentAddress--;
        } else {
            controller[ctrlNum].channel[chanNum].currentAddress++;
        }
        controller[ctrlNum].channel[chanNum].currentCount--;

        // Check if word count has 'rolled' over to 0xFFFF (-1 signed); generate
        // TC and check if autoInitialise enabled
        if (controller[ctrlNum].channel[chanNum].currentCount == -1) {
            // Transfer complete
            // Assert TC, deassert HRQ, DACK(n) lines
            controller[ctrlNum].statusRegister |= (1 << chanNum); // Set TC in
                                                                  // status
                                                                  // register
            terminalCountReached = true;
            countExpired = true; // Set extra variable for check later
            if (controller[ctrlNum].channel[chanNum].mode.autoInitEnable) {
                // AutoInit mode restores original values of currentAddress,
                // currentCount from baseAddress, baseCount
                controller[ctrlNum].channel[chanNum].currentAddress = controller[ctrlNum].channel[chanNum].baseAddress;
                controller[ctrlNum].channel[chanNum].currentCount = controller[ctrlNum].channel[chanNum].baseCount;
            } else {
                // End of Process, so set mask bit
                controller[ctrlNum].mask[chanNum] = 1;
            }
        }

        try {
            initiateDMATransfer(ctrlNum, chanNum, memoryAddress);
        } catch (ModuleException e) {
            logger.log(Level.SEVERE, "[DMA] Error in DMA transfer: "
                    + e.getMessage());
        }

        if (countExpired) // DMA process finished, so reset all involved
                          // variables
        {
            terminalCountReached = false; // Device has checked TC during
                                          // handlerWrite()/handlerRead(), so
                                          // can clear TC
            busHoldAcknowledged = false;
            cpu.setHoldRequest(false, this); // clear HRQ to CPU
            if (ctrlNum == 0) // Master controller, so cascade HRQ via DREQ4
            {
                setDMARequest(4, false); // clear DRQ to cascade
                controller[1].DACK[0] = false; // clear DACK to cascade
            }
            controller[ctrlNum].DACK[chanNum] = false; // clear DACK to adapter
                                                       // card
        }
    }

    /**
     * The DMA transfer type (verify/write/read) set in the mode register is
     * initiated by calling the registered 8-bit/16-bit handler
     * 
     * @param ctrlNum
     *            Controller whose channel is requesting a transfer
     * @param chanNum
     *            Channel number of the request
     * @param memoryAddress
     *            Source/destination address in memory of the DMA operation
     * @throws ModuleException
     */
    private void initiateDMATransfer(int ctrlNum, int chanNum, int memoryAddress)
            throws ModuleException {
        byte dataByte; // 8-bit data read/written to/from memory
        byte[] dataWord = new byte[2]; // 16-bit data read/written to/from
                                       // memory

        // Initiate DMA transfer; check type and channel, call registered
        // handler to proceed

        if (controller[ctrlNum].channel[chanNum].mode.transferType == DMAModeRegister.DMA_TRANSFER_WRITE) // device
                                                                                                          // ->
                                                                                                          // memory
        {
            if (ctrlNum == 0) // 8-bit transfer
            {
                if (dma8Handler[chanNum] != null) {
                    dataByte = dma8Handler[chanNum].dma8WriteToMem();
                    memory.setByte(memoryAddress, dataByte);
                } else
                    logger.log(Level.SEVERE, "[" + MODULE_TYPE + "]"
                            + " no dma8 write handler for channel " + chanNum);
            } else // 16-bit transfer
            {
                if (dma16Handler[chanNum] != null) {
                    dataWord = dma16Handler[chanNum].dma16WriteToMem();
                    memory.setWord(memoryAddress, dataWord);
                } else
                    logger.log(Level.SEVERE, "[" + MODULE_TYPE + "]"
                            + " no dma16 write handler for channel " + chanNum);
            }
        } else if (controller[ctrlNum].channel[chanNum].mode.transferType == DMAModeRegister.DMA_TRANSFER_READ) // memory
                                                                                                                // ->
                                                                                                                // device
        {
            if (ctrlNum == 0) // 8-bit transfer
            {
                dataByte = memory.getByte(memoryAddress);
                if (dma8Handler[chanNum] != null)
                    dma8Handler[chanNum].dma8ReadFromMem(dataByte);
            } else // 16-bit transfer
            {
                dataWord = memory.getWord(memoryAddress);
                if (dma16Handler[chanNum] != null)
                    dma16Handler[chanNum].dma16ReadFromMem(dataWord);
            }
        } else if (controller[ctrlNum].channel[chanNum].mode.transferType == DMAModeRegister.DMA_TRANSFER_VERIFY) // Pseudo-transfer
        {
            if (ctrlNum == 0) {
                if (dma8Handler[chanNum] != null)
                    dataByte = dma8Handler[chanNum].dma8WriteToMem();
                else
                    logger.log(Level.SEVERE, "[" + MODULE_TYPE + "]"
                            + " no dma8 write handler for channel " + chanNum);
            } else {
                if (dma16Handler[chanNum] != null)
                    dataWord = dma16Handler[chanNum].dma16WriteToMem();
                else
                    logger.log(Level.SEVERE, "[" + MODULE_TYPE + "]"
                            + " no dma16 write handler for channel " + chanNum);
            }
        } else {
            logger.log(Level.SEVERE, "[" + MODULE_TYPE + "]"
                    + " HLDA: memory->memory transfer (type 3) undefined");
        }
    }

    /**
     * Registers the cascade channel, 4. Does not implement any read or write
     * methods,<BR>
     * as the only use is to prevent other devices from registering on channel 4
     * 
     */
    private void setCascadeChannel() {
        // Extend the DMA16Handler to provide to registerDMAChannel()
        // This class does not implement any of the methods.
        class Cascade16Handler extends DMA16Handler {
            @Override
            // Cascade channel does not support reading
            public void dma16ReadFromMem(byte[] data) {
                // Do nothing
            }

            @Override
            // Casace channel does not support writing
            public byte[] dma16WriteToMem() {
                // Do nothing
                return null;
            }

        }

        // Create instance of cascade and register with DMA
        Cascade16Handler cascade = new Cascade16Handler();
        cascade.owner = "Cascade";
        registerDMAChannel(CASCADE_DMA_CHANNEL, cascade);
    }
}
