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

package dioscuri.module.serialport;

import java.util.logging.Level;
import java.util.logging.Logger;

import dioscuri.Emulator;
import dioscuri.exception.ModuleException;
import dioscuri.exception.ModuleUnknownPort;
import dioscuri.exception.ModuleWriteOnlyPortException;
import dioscuri.interfaces.Module;
import dioscuri.interfaces.UART;
import dioscuri.module.ModuleMotherboard;
import dioscuri.module.ModulePIC;
import dioscuri.module.ModuleSerialPort;

/**
 * An implementation of a serial port module.
 *
 * @see dioscuri.module.AbstractModule
 *      <p/>
 *      Metadata module ********************************************
 *      general.type : serialport general.name : UART 16550A Serial Port
 *      general.architecture : Von Neumann general.description : Models a 9-pin
 *      serial port general.creator : Tessella Support Services, Koninklijke
 *      Bibliotheek, Nationaal Archief of the Netherlands general.version : 1.0
 *      general.keywords : serial port, port, RS232, DE-9, COM, UART
 *      general.relations : Motherboard, PIC general.yearOfIntroduction :
 *      general.yearOfEnding : general.ancestor : general.successor :
 *      <p/>
 *      Notes: - AbstractModule serial port supports up to 4 COM ports - It is based on
 *      the UART 16550A interface (including FIFO buffer) - Data is transmitted
 *      in little-endian order (least significant bit first)
 *      <p/>
 *      References: - I/O Port information:
 *      http://mudlist.eorbit.net/~adam/pickey/ports.html - UART register
 *      information from PC16550D UART with FIFOs (National Semiconductor):
 *      http://www.national.com/mpf/PC/PC16550D.html - Bochs X86 emulator:
 *      http://bochs.sourceforge.net
 *      <p/>
 *      <p/>
 *      PORT 03F8-03FF - serial port (8250,8250A,8251,16450,16550,16550A,etc.)
 *      COM1 Range: PORT 02E8h-02EFh (COM2), PORT 02F8h-02FFh (typical non-PS/2
 *      COM3), and PORT 03E8h-03EFh (typical non-PS/2 COM4)
 *      <p/>
 *      Chips overview: 8250 original PC, specified up to 56Kbd, but mostly runs
 *      only 9600Bd, no scratchregister, bug: sometimes shots ints without
 *      reasons 8250A, 16450, 16C451: ATs, most chips run up to 115KBd, no bug:
 *      shots no causeless ints 8250B: PC,XT,AT, pseudo bug: shots one causeless
 *      int for compatibility with 8250, runs up to 56KBd 16550, 16550N, 16550V:
 *      early PS/2, FIFO bugs 16550A,16550AF,16550AFN,16550C,16C551,16C552:
 *      PS/2, FIFO ok 82510: laptops & industry, multi emulation mode
 *      (default=16450), special-FIFO. 8251: completely different synchronous
 *      SIO chip, not compatible!
 *      <p/>
 *      Each COM-port uses 8 I/O ports and 12 UART interface registers: 03F8 W
 *      transmitter holding register (THR), which contains the character to be
 *      sent. Bit 0 is sent first. bit 7-0 data bits when DLAB=0 (Divisor Latch
 *      Access Bit) 03F8 R receiver buffer register (RBR), which contains the
 *      received character. Bit 0 is received first bit 7-0 data bits when
 *      DLAB=0 (Divisor Latch Access Bit) 03F8 RW divisor latch low byte (DLL)
 *      when DLAB=1 (see #P189) 03F9 RW divisor latch high byte (DLM) when
 *      DLAB=1 (see #P189) 03F9 RW interrupt enable register (IER) when DLAB=0
 *      (see #P190) 03FA R interrupt identification register (IIR) Information
 *      about a pending interrupt is stored here. When the ID register is
 *      addressed, the highest priority interrupt is held, and no other
 *      interrupts are acknowledged until the CPU services that interrupt. 03FA
 *      W 16650 FIFO Control Register (FCR) (see #P192) 03FB RW line control
 *      register (LCR) (see #P193) 03FC RW modem control register (see #P194)
 *      03FD R line status register (LSR) (see #P195) 03FE R modem status
 *      register (MSR) (see #P196) 03FF RW scratch register (SCR) (not used for
 *      serial I/O; available to any application using 16450, 16550) (not
 *      present on original 8250)
 */
public class SerialPort extends ModuleSerialPort {

    // Logging
    private static final Logger logger = Logger.getLogger(SerialPort.class.getName());

    // Timing
    private int updateInterval;

    // IRQ -> See each COM port

    // COM-ports
    private ComPort[] comPorts;

    // Constants
    public final static int TOTALCOMPORTS = 4; // Defines the total number of
    // COM ports

    // I/O ports COM1 - 4
    private final static int[] IOPORTS = new int[]{0x3F8, 0x2F8, 0x3E8, 0x2E8};

    // Offset UART registers (in conjunction with I/O port addresses)
    private final static int THR = 0; // Write-only port
    private final static int RBR = 0; // Read-only port
    private final static int DLL = 0; // Read/Write port
    private final static int DLM = 1; // Read/Write port
    private final static int IER = 1; // Read/Write port
    private final static int IIR = 2; // Read-only port
    private final static int FCR = 2; // Write-only port
    private final static int LCR = 3; // Read/Write port
    private final static int MCR = 4; // Read/Write port
    private final static int LSR = 5; // Read-only port
    private final static int MSR = 6; // Read-only port
    private final static int SCR = 7; // Read/Write port

    private final static int INTERRUPT_IER = 0;
    private final static int INTERRUPT_RXDATA = 1;
    private final static int INTERRUPT_TXHOLD = 2;
    private final static int INTERRUPT_RXLSTAT = 3;
    private final static int INTERRUPT_MODSTAT = 4;
    private final static int INTERRUPT_FIFO = 5;

    private final static int BUFFERSIZE = 16; // Maximum allowable FIFO buffer
    // size

    private final static double CLOCKSPEED = 1843200.0;

    // Constructor

    /**
     * Class constructor
     *
     * @param owner
     */
    public SerialPort(Emulator owner) {

        // Create COM-ports
        comPorts = new ComPort[TOTALCOMPORTS];
        for (int c = 0; c < TOTALCOMPORTS; c++) {
            comPorts[c] = new ComPort();
        }
        
        logger.log(Level.INFO, "[" + super.getType() + "] " + getClass().getName()
                + " -> AbstractModule created successfully.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean reset() {

        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Module.Type.MOTHERBOARD);
        ModulePIC pic = (ModulePIC)super.getConnection(Module.Type.PIC);

        // Reset COM-ports
        for (int c = 0; c < comPorts.length; c++) {
            // Register I/O ports
            for (int i = 0; i < 8; i++) {
                // Register range of ports per COM port (0x3F[8-F], 0x2F[8-F],
                // 0x3E[8-F], 0x2E[8-F])
                motherboard.setIOPort(IOPORTS[c] + i, this);
            }

            // Reset UART registers
            comPorts[c].reset();

            // Request IRQ number (only for COM 3 and 4)
            // FIXME: check if more than 1 IRQ number is needed (1 for each
            // port?)
            // comPorts[c].irq = 4 - (c & 0x01);
            if (c < 1) {
                comPorts[c].irq = pic.requestIRQNumber(this);

                if (comPorts[c].irq > -1) {
                    logger.log(Level.CONFIG, "[" + super.getType() + "]"
                            + " IRQ number set to: " + comPorts[c].irq);
                } else {
                    logger.log(Level.WARNING, "[" + super.getType() + "]"
                            + " Request of IRQ number failed.");
                }
            }
        }

        // Request a timer (one shot)
        updateInterval = 0;

        if (motherboard.requestTimer(this, updateInterval, false) == false) {
            return false;
        }

        // Keep timer passive
        motherboard.setTimerActiveState(this, false);

        logger.log(Level.INFO, "[" + super.getType() + "] AbstractModule has been reset.");

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDump() {
        String dump = "Serial port status:\n";

        dump += "This module is only a stub, no contents available" + "\n";

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
        return updateInterval;
    }

    /**
     * Defines the interval between subsequent updates
     */
    public void setUpdateInterval(int interval) {
        // Check if interval is > 0
        if (interval > 0) {
            updateInterval = interval;
        } else {
            updateInterval = 200; // default is 200 ms
        }

        // Notify motherboard that interval has changed
        // (only if motherboard contains a clock, which may not be the case at
        // startup, but may be during execution)
        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Module.Type.MOTHERBOARD);
        motherboard.resetTimer(this, updateInterval);
    }

    /**
     * Update device
     */
    public void update() {
        int port = 0;
        int timer_id, baudrate;
        boolean isDataReady = false;

        // Check if serial port is being used (UART device)
        if (comPorts[port].uartDevice != null) {
            // FIXME: assuming here that it is always COM 1
            /*
             * timer_id = bx_pc_system.triggeredTimerID();
             * 
             * if (timer_id == comPorts[0].rx_timer_index) { port = 0; } else if
             * (timer_id == BX_SER_THIS s[1].rx_timer_index) { port = 1; } else
             * if (timer_id == BX_SER_THIS s[2].rx_timer_index) { port = 2; }
             * else if (timer_id == BX_SER_THIS s[3].rx_timer_index) { port = 3;
             * }
             */
            baudrate = comPorts[port].baudrate / (comPorts[port].lcr_wordlen_sel + 5);
            byte chbuf = 0;

            // Check if no data is still waiting and if FIFO is enabled
            if ((comPorts[port].lsr_rxdata_ready == 0) || (comPorts[port].fcr_enable == 1)) {

                // Check if data is available to put into FIFO buffer
                if (comPorts[port].uartDevice.isDataAvailable()) {
                    chbuf = comPorts[port].uartDevice.getSerialData();
                    isDataReady = true;
                }

                if (isDataReady) {
                    // Data ready, enqueue data in serial buffer (FIFO) if local
                    // loopback if off
                    if (!(comPorts[port].mcr_local_loopback == 1)) {
                        this.enqueueReceivedData(port, chbuf);
                    }
                } else {
                    // Data is not ready
                    // If FIFO is not active adjust baudrate
                    if (comPorts[port].fcr_enable == 0) {
                        // Set update frequency to 100ms
                        baudrate = (int) (1000000.0 / 100000);
                    }
                }
            } else {
                // Poll at 4x baud rate to see if the next-char can be read
                baudrate *= 4;
            }

            // Activate timer as one shot
            ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Module.Type.MOTHERBOARD);
            motherboard.resetTimer(this, (int) (1000000.0 / comPorts[port].baudrate));
            motherboard.setTimerActiveState(this, true);
        }
    }

    /**
     * IN instruction to serial port<BR>
     *
     * @param portAddress the target port; can be any of 0x03F[8-F], 0x02F[8-F],
     *                    0x03E[8-F], or 2E[8-F]<BR>
     * @return byte of data from COM-port register
     */
    public byte getIOPortByte(int portAddress) throws ModuleUnknownPort,
            ModuleWriteOnlyPortException {
        int offset, port;
        byte value = 0x00;

        // Offset for UART registers
        offset = portAddress & 0x07;

        // Select COM-port
        switch (portAddress & 0x03F8) {
            case 0x03F8:
                port = 0;
                break;

            case 0x02F8:
                port = 1;
                break;

            case 0x03E8:
                port = 2;
                break;

            case 0x02E8:
                port = 3;
                break;

            default:
                port = 0;
                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + " Unknown COM-port. Selected default (COM1)");
        }

        switch (offset) {
            case RBR: // DLAB = 0: Receive buffer, DLAB = 1: divisor latch LSB
                if (comPorts[port].lcr_dlab == 1) {
                    // DLAB = 1: DLL
                    value = (byte) comPorts[port].dll;
                } else {
                    // DLAB = 0: RBR
                    if (comPorts[port].fcr_enable == 1) {
                        // FIFO buffer active
                        value = comPorts[port].rcvrFIFO.poll();

                        if (comPorts[port].rcvrFIFO.isEmpty()) {
                            // FIFO buffer is empty, reset registers
                            comPorts[port].lsr_rxdata_ready = 0;
                            comPorts[port].rx_interrupt = 0;
                            comPorts[port].rx_ipending = 0;
                            comPorts[port].fifo_interrupt = 0;
                            comPorts[port].fifo_ipending = 0;
                            this.clearIRQ(port);
                        }
                    } else {
                        // FIFO buffer not active
                        value = comPorts[port].rbr;
                        comPorts[port].lsr_rxdata_ready = 0;
                        comPorts[port].rx_interrupt = 0;
                        comPorts[port].rx_ipending = 0;
                        this.clearIRQ(port);
                    }
                }
                break;

            case IER: // DLAB = 0: Interrupt Enable register, DLAB = 1: Divisor
                // Latch MSB
                if (comPorts[port].lcr_dlab == 1) {
                    // DLAB = 1: DLM
                    value = (byte) comPorts[port].dlm;
                } else {
                    // DLAB = 0: IER
                    value = (byte) (comPorts[port].ier_rxdata_enable
                            | (comPorts[port].ier_txhold_enable << 1)
                            | (comPorts[port].ier_rxlstat_enable << 2) | (comPorts[port].ier_modstat_enable << 3));
                }
                break;

            case IIR: // Interrupt Identification register
                // Set the interrupt ID based on interrupt source
                if (comPorts[port].ls_interrupt == 1) {
                    comPorts[port].iir_int_ID = 0x3;
                    comPorts[port].iir_ipending = 0;
                } else if (comPorts[port].fifo_interrupt == 1) {
                    comPorts[port].iir_int_ID = 0x6;
                    comPorts[port].iir_ipending = 0;
                } else if (comPorts[port].rx_interrupt == 1) {
                    comPorts[port].iir_int_ID = 0x2;
                    comPorts[port].iir_ipending = 0;
                } else if (comPorts[port].tx_interrupt == 1) {
                    comPorts[port].iir_int_ID = 0x1;
                    comPorts[port].iir_ipending = 0;
                } else if (comPorts[port].ms_interrupt == 1) {
                    comPorts[port].iir_int_ID = 0x0;
                    comPorts[port].iir_ipending = 0;
                } else {
                    comPorts[port].iir_int_ID = 0x0;
                    comPorts[port].iir_ipending = 1;
                }
                comPorts[port].tx_interrupt = 0;
                this.clearIRQ(port);

                value = (byte) (comPorts[port].iir_ipending
                        | (comPorts[port].iir_int_ID << 1) | (comPorts[port].fcr_enable == 1 ? 0xC0
                        : 0x00));
                break;

            case LCR: // Line Control register
                // Return LCR
                value = (byte) (comPorts[port].lcr_wordlen_sel
                        | (comPorts[port].lcr_stopbits << 2)
                        | (comPorts[port].lcr_parity_enable << 3)
                        | (comPorts[port].lcr_evenparity_sel << 4)
                        | (comPorts[port].lcr_stick_parity << 5)
                        | (comPorts[port].lcr_break_cntl << 6) | (comPorts[port].lcr_dlab << 7));
                break;

            case MCR: // MODEM Control register
                // Return LCR
                value = (byte) (comPorts[port].mcr_dtr
                        | (comPorts[port].mcr_rts << 1)
                        | (comPorts[port].mcr_out1 << 2)
                        | (comPorts[port].mcr_out2 << 3) | (comPorts[port].mcr_local_loopback << 4));
                break;

            case LSR: // Line Status register
                // Return LSR
                value = (byte) (comPorts[port].lsr_rxdata_ready
                        | (comPorts[port].lsr_overrun_error << 1)
                        | (comPorts[port].lsr_parity_error << 2)
                        | (comPorts[port].lsr_framing_error << 3)
                        | (comPorts[port].lsr_break_int << 4)
                        | (comPorts[port].lsr_thr_empty << 5)
                        | (comPorts[port].lsr_tsr_empty << 6) | (comPorts[port].lsr_fifo_error << 7));

                // Reset LSR variables and interrupts
                comPorts[port].lsr_overrun_error = 0;
                comPorts[port].lsr_parity_error = 0;
                comPorts[port].lsr_framing_error = 0;
                comPorts[port].lsr_break_int = 0;
                comPorts[port].lsr_fifo_error = 0;
                comPorts[port].ls_interrupt = 0;
                comPorts[port].ls_ipending = 0;
                this.clearIRQ(port);
                break;

            case MSR: // MODEM Status register
                // Return MSR
                value = (byte) (comPorts[port].msr_delta_cts
                        | (comPorts[port].msr_delta_dsr << 1)
                        | (comPorts[port].msr_ri_trailedge << 2)
                        | (comPorts[port].msr_delta_dcd << 3)
                        | (comPorts[port].msr_cts << 4)
                        | (comPorts[port].msr_dsr << 5)
                        | (comPorts[port].msr_ri << 6) | (comPorts[port].msr_dcd << 7));

                // Reset MSR
                comPorts[port].msr_delta_cts = 0;
                comPorts[port].msr_delta_dsr = 0;
                comPorts[port].msr_ri_trailedge = 0;
                comPorts[port].msr_delta_dcd = 0;

                // Clear interrupt
                comPorts[port].ms_interrupt = 0;
                comPorts[port].ms_ipending = 0;
                this.clearIRQ(port);
                break;

            case SCR: // Scratch register
                value = (byte) comPorts[port].scr;
                break;

            default:
                value = -1;
                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + " Error while reading I/O port 0x"
                        + Integer.toHexString(portAddress).toUpperCase()
                        + ": no case match");
                break;
        }

        logger.log(Level.FINE, "[" + super.getType() + "]"
                + " Read (byte) from port 0x"
                + Integer.toHexString(portAddress).toUpperCase() + ": 0x"
                + Integer.toHexString(((int) value) & 0xFF).toUpperCase());

        return value;
    }

    /**
     * OUT instruction to serial port<BR>
     *
     * @param portAddress the target port; can be any of 0x027[8-A], 0x037[8-A], or
     *                    0x03B[C-E]<BR>
     *                    <p/>
     *                    OUT to portAddress 378h does ...<BR>
     *                    OUT to portAddress 379h does ...<BR>
     *                    OUT to portAddress 37Ah does ...<BR>
     */
    public void setIOPortByte(int portAddress, byte data)
            throws ModuleUnknownPort {

        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Module.Type.MOTHERBOARD);
        ModulePIC pic = (ModulePIC)super.getConnection(Module.Type.PIC);

        logger.log(Level.INFO, "[" + super.getType() + "]"
                + " Write (byte) to port "
                + Integer.toHexString(portAddress).toUpperCase() + ": 0x"
                + Integer.toHexString(((int) data) & 0xFF).toUpperCase());

        int new_b0, new_b1, new_b2, new_b3;
        int new_b4, new_b5, new_b6, new_b7;
        boolean raiseInterrupt = false;
        int prev_cts, prev_dsr, prev_ri, prev_dcd;
        int offset, new_wordlen;
        int port = 0;

        // Sort out each bit of new data
        new_b0 = data & 0x01;
        new_b1 = (data & 0x02) >> 1;
        new_b2 = (data & 0x04) >> 2;
        new_b3 = (data & 0x08) >> 3;
        new_b4 = (data & 0x10) >> 4;
        new_b5 = (data & 0x20) >> 5;
        new_b6 = (data & 0x40) >> 6;
        new_b7 = ((data & 0x80) >> 7) & 0x01; // to ensure data is unsigned

        // Offset for UART registers
        offset = portAddress & 0x07;

        // Select COM-port
        switch (portAddress & 0x03F8) {
            case 0x03F8:
                port = 0;
                break;

            case 0x02F8:
                port = 1;
                break;

            case 0x03E8:
                port = 2;
                break;

            case 0x02E8:
                port = 3;
                break;

            default:
                port = 0;
                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + " Unknown COM-port. Selected default (COM1)");
        }

        switch (offset) {
            case THR: // DLAB = 0: Transmit buffer, DLAB = 1: Divisor Latch LSB
                if (comPorts[port].lcr_dlab == 1) {
                    // DLAB = 1: DLL
                    comPorts[port].dll = data;

                    if ((comPorts[port].dll != 0) || (comPorts[port].dlm != 0)) {
                        comPorts[port].baudrate = (int) (CLOCKSPEED / (16 * ((comPorts[port].dlm << 8) | comPorts[port].dll)));
                    }
                } else {
                    // DLAB = 0: THR
                    byte bitmask = (byte) (0xFF >> (3 - comPorts[port].lcr_wordlen_sel));

                    // Check if THR is empty or not
                    if (comPorts[port].lsr_thr_empty == 1) {
                        // Check if FIFO is active
                        if (comPorts[port].fcr_enable == 1) {
                            // FIFO active: set data
                            comPorts[port].xmitFIFO
                                    .offer((byte) (data & bitmask));
                        } else {
                            // FIFO not active: set THR
                            comPorts[port].thr = (byte) (data & bitmask);
                        }

                        // THR no longer empty
                        comPorts[port].lsr_thr_empty = 0;

                        // Check if both THR and TSR were empty
                        if (comPorts[port].lsr_tsr_empty == 1) {
                            if (comPorts[port].fcr_enable == 1) {
                                // FIFO active: set TSR with data
                                comPorts[port].tsr = comPorts[port].xmitFIFO
                                        .poll();
                                comPorts[port].lsr_thr_empty = comPorts[port].xmitFIFO
                                        .isEmpty() ? 1 : 0;
                            } else {
                                // FIFO not active: copy THR to TSR
                                comPorts[port].tsr = comPorts[port].thr;
                                comPorts[port].lsr_thr_empty = 1;
                            }

                            // TSR is not empty anymore
                            comPorts[port].lsr_tsr_empty = 0;
                            this.setIRQ(port, INTERRUPT_TXHOLD);

                            // Start timer again as one shot
                            motherboard
                                    .resetTimer(
                                            this,
                                            (int) (1000000.0 / comPorts[port].baudrate * (comPorts[port].lcr_wordlen_sel + 5)));
                            motherboard.setTimerActiveState(this, true);
                            logger.log(Level.INFO, "[" + super.getType()
                                    + "] Timer activated");
                        } else {
                            // No changes to TSR
                            comPorts[port].tx_interrupt = 0;
                            this.clearIRQ(port);
                        }
                    } else {
                        // THR is not empty, already contains character
                        if (comPorts[port].fcr_enable == 1) {
                            // Check if FIFO does not exceed BUFFERSIZE
                            if (comPorts[port].xmitFIFO.size() < BUFFERSIZE) {
                                comPorts[port].xmitFIFO
                                        .offer((byte) (data & bitmask));
                            } else {
                                // FIFO buffer overflow
                                logger.log(Level.WARNING, "[" + super.getType() + "]"
                                        + " Error: FIFO buffer overflow");
                                comPorts[port].lsr_overrun_error = 1;
                            }
                        } else {
                            logger
                                    .log(
                                            Level.WARNING,
                                            "["
                                                    + super.getType()
                                                    + "]"
                                                    + " Error: THR buffer overflow. Can not write to buffer while not empty");
                            comPorts[port].lsr_overrun_error = 1;
                        }
                    }
                }
                break;

            case IER: // DLAB = 0: Interrupt Enable register, DLAB = 1: Divisor
                // Latch MSB
                if (comPorts[port].lcr_dlab == 1) {
                    // DLAB = 1: set dlm
                    comPorts[port].dlm = data;

                    if ((comPorts[port].dlm != 0) || (comPorts[port].dll != 0)) {
                        comPorts[port].baudrate = (int) (CLOCKSPEED / (16 * ((comPorts[port].dlm << 8) | comPorts[port].dll)));
                    }
                } else {
                    // Check if MODEM Status interrupt should change
                    if (new_b3 != comPorts[port].ier_modstat_enable) {
                        // Update MODEM Status interrupt
                        comPorts[port].ier_modstat_enable = new_b3;

                        if (comPorts[port].ier_modstat_enable == 1) {
                            // MS int enabled
                            if (comPorts[port].ms_ipending == 1) {
                                comPorts[port].ms_interrupt = 1;
                                comPorts[port].ms_ipending = 0;
                                raiseInterrupt = true;
                            }
                        } else {
                            // MS int disabled
                            if (comPorts[port].ms_interrupt == 1) {
                                comPorts[port].ms_interrupt = 0;
                                comPorts[port].ms_ipending = 1;
                                this.clearIRQ(port);
                            }
                        }
                    }

                    // Check if Received Data Available interrupt should change
                    if (new_b0 != comPorts[port].ier_rxdata_enable) {
                        // Update RDA interrupt
                        comPorts[port].ier_rxdata_enable = new_b0;
                        if (comPorts[port].ier_rxdata_enable == 1) {
                            // RDA int enabled
                            if (comPorts[port].fifo_ipending == 1) {
                                comPorts[port].fifo_interrupt = 1;
                                comPorts[port].fifo_ipending = 0;
                                raiseInterrupt = true;
                            }

                            if (comPorts[port].rx_ipending == 1) {
                                comPorts[port].rx_interrupt = 1;
                                comPorts[port].rx_ipending = 0;
                                raiseInterrupt = true;
                            }
                        } else {
                            // RDA int disabled
                            if (comPorts[port].rx_interrupt == 1) {
                                comPorts[port].rx_interrupt = 0;
                                comPorts[port].rx_ipending = 1;
                                this.clearIRQ(port);
                            }

                            if (comPorts[port].fifo_interrupt == 1) {
                                comPorts[port].fifo_interrupt = 0;
                                comPorts[port].fifo_ipending = 1;
                                this.clearIRQ(port);
                            }
                        }
                    }

                    // Check if Transmitter Holding Register Empty interrupt should
                    // change
                    if (new_b1 != comPorts[port].ier_txhold_enable) {
                        // Update THR enable interrupt
                        comPorts[port].ier_txhold_enable = new_b1;

                        if (comPorts[port].ier_txhold_enable == 1) {
                            // Set transfer interrupt if THR/FIFO is empty (can
                            // receive new byte)
                            comPorts[port].tx_interrupt = comPorts[port].lsr_thr_empty;

                            if (comPorts[port].tx_interrupt == 1) {
                                raiseInterrupt = true;
                            }
                        } else {
                            comPorts[port].tx_interrupt = 0;
                            this.clearIRQ(port);
                        }
                    }

                    // Check if Receiver Line Status interrupt should change
                    if (new_b2 != comPorts[port].ier_rxlstat_enable) {
                        // Update RLS interrupt
                        comPorts[port].ier_rxlstat_enable = new_b2;
                        if (comPorts[port].ier_rxlstat_enable == 1) {
                            // RLS int enabled
                            if (comPorts[port].ls_ipending == 1) {
                                comPorts[port].ls_interrupt = 1;
                                comPorts[port].ls_ipending = 0;
                                raiseInterrupt = true;
                            }
                        } else {
                            // RLS int disabled
                            if (comPorts[port].ls_interrupt == 1) {
                                comPorts[port].ls_interrupt = 0;
                                comPorts[port].ls_ipending = 1;
                                this.clearIRQ(port);
                            }
                        }
                    }

                    // Check if interrupt should be raised (do it once)
                    if (raiseInterrupt == true) {
                        this.setIRQ(port, INTERRUPT_IER);
                    }
                }
                break;

            case FCR: // FIFO Control register
                if (new_b0 == 1 && !(comPorts[port].fcr_enable == 1)) {
                    // Activate FIFO buffers
                    comPorts[port].rcvrFIFO.clear();
                    comPorts[port].xmitFIFO.clear();
                    logger.log(Level.INFO, "[" + super.getType() + "]"
                            + " FIFO buffer enabled");
                }

                comPorts[port].fcr_enable = new_b0;

                if (new_b1 == 1) {
                    comPorts[port].rcvrFIFO.clear();
                }

                if (new_b2 == 1) {
                    comPorts[port].xmitFIFO.clear();
                }

                // Set RCVR FIFO trigger level (range is 1,4,8,14 bytes)
                comPorts[port].fcr_rxtrigger = (data & 0xC0) >> 6;
                break;

            case LCR: // Line Control register
                new_wordlen = data & 0x03;
                comPorts[port].lcr_wordlen_sel = new_wordlen;
                comPorts[port].lcr_stopbits = new_b2;
                comPorts[port].lcr_parity_enable = new_b3;
                comPorts[port].lcr_evenparity_sel = new_b4;
                comPorts[port].lcr_stick_parity = new_b5;
                comPorts[port].lcr_break_cntl = new_b6;

                // Check if spacing character should be queued
                if (comPorts[port].mcr_local_loopback == 1
                        && comPorts[port].lcr_break_cntl == 1) {
                    comPorts[port].lsr_break_int = 1;
                    comPorts[port].lsr_framing_error = 1;
                    this.enqueueReceivedData(port, (byte) 0x00);
                }

                // TODO: find out why this is...
                if ((new_b7 == 0) && comPorts[port].lcr_dlab == 1) {
                    // Start the receive polling process if not already started
                    if (comPorts[port].rx_pollstate == ComPort.RX_IDLE
                            && comPorts[port].baudrate != 0) {
                        comPorts[port].rx_pollstate = ComPort.RX_POLL;

                        // Start timer again as one shot
                        motherboard
                                .resetTimer(
                                        this,
                                        (int) (1000000.0 / comPorts[port].baudrate * (comPorts[port].lcr_wordlen_sel + 5)));
                        motherboard.setTimerActiveState(this, true);
                        logger.log(Level.INFO, "[" + super.getType()
                                + "] Timer activated");
                    }

                    logger.log(Level.INFO, "[" + super.getType() + "]"
                            + " baud rate of COM1 set to "
                            + comPorts[port].baudrate);
                }

                // Set DLAB
                comPorts[port].lcr_dlab = new_b7;
                break;

            case MCR: // MODEM control register

                // TODO: Handle modem control mode

                comPorts[port].mcr_dtr = new_b0;
                comPorts[port].mcr_rts = new_b1;
                comPorts[port].mcr_out1 = new_b2;
                comPorts[port].mcr_out2 = new_b3;

                if (new_b4 != comPorts[port].mcr_local_loopback) {
                    comPorts[port].mcr_local_loopback = new_b4;
                    if (comPorts[port].mcr_local_loopback == 1) {
                        // Transition to loopback mode
                        if (comPorts[port].lcr_break_cntl == 1) {
                            comPorts[port].lsr_break_int = 1;
                            comPorts[port].lsr_framing_error = 1;
                            enqueueReceivedData(port, (byte) 0x00);
                        }
                    } else {
                        // TODO: Transition to normal mode
                    }
                }

                // Check if UART should be put into local loopback mode
                if (comPorts[port].mcr_local_loopback == 1) {
                    // Local loopback enabled: shortcut output of TSR to input of
                    // RSR
                    // Disconnect SOUT from SIN

                    // Preserve former values
                    prev_cts = comPorts[port].msr_cts;
                    prev_dsr = comPorts[port].msr_dsr;
                    prev_ri = comPorts[port].msr_ri;
                    prev_dcd = comPorts[port].msr_dcd;

                    comPorts[port].msr_cts = comPorts[port].mcr_rts;
                    comPorts[port].msr_dsr = comPorts[port].mcr_dtr;
                    comPorts[port].msr_ri = comPorts[port].mcr_out1;
                    comPorts[port].msr_dcd = comPorts[port].mcr_out2;

                    if (comPorts[port].msr_cts != prev_cts) {
                        comPorts[port].msr_delta_cts = 1;
                        comPorts[port].ms_ipending = 1;
                    }

                    if (comPorts[port].msr_dsr != prev_dsr) {
                        comPorts[port].msr_delta_dsr = 1;
                        comPorts[port].ms_ipending = 1;
                    }

                    if (comPorts[port].msr_ri != prev_ri) {
                        comPorts[port].ms_ipending = 1;
                    }

                    if ((comPorts[port].msr_ri == 0) && (prev_ri == 1)) {
                        comPorts[port].msr_ri_trailedge = 1;
                    }

                    if (comPorts[port].msr_dcd != prev_dcd) {
                        comPorts[port].msr_delta_dcd = 1;
                        comPorts[port].ms_ipending = 1;
                    }

                    this.setIRQ(port, INTERRUPT_MODSTAT);
                } else {
                    // TODO: perform special mouse mode here...
                }

                // Simulate device connected
                comPorts[port].msr_cts = 1;
                comPorts[port].msr_dsr = 1;
                comPorts[port].msr_ri = 0;
                comPorts[port].msr_dcd = 0;
                break;

            case LSR: // Line Status register
                logger
                        .log(
                                Level.WARNING,
                                "["
                                        + super.getType()
                                        + "]"
                                        + " Not allowed to write to line status register (LSR) on port 0x"
                                        + Integer.toHexString(portAddress)
                                        .toUpperCase());
                break;

            case MSR: // MODEM Status register
                logger
                        .log(
                                Level.WARNING,
                                "["
                                        + super.getType()
                                        + "]"
                                        + " Not allowed to write to modem status register (MSR) on port 0x"
                                        + Integer.toHexString(portAddress)
                                        .toUpperCase());
                break;

            case SCR: // Scratch register
                comPorts[port].scr = data;
                break;

            default:
                logger.log(Level.WARNING, "[" + super.getType() + "]"
                        + " Error while writing I/O port 0x"
                        + Integer.toHexString(portAddress).toUpperCase()
                        + ": no case match");
                break;
        }
    }

    public byte[] getIOPortWord(int portAddress) throws ModuleException,
            ModuleWriteOnlyPortException {
        logger.log(Level.INFO, "[" + super.getType()
                + "] IN command (word) to port "
                + Integer.toHexString(portAddress).toUpperCase() + " received");
        logger.log(Level.INFO, "[" + super.getType()
                + "] Returned default value 0xFFFF to AX");

        // Return dummy value 0xFFFF
        return new byte[]{(byte) 0x0FF, (byte) 0x0FF};
    }

    public void setIOPortWord(int portAddress, byte[] dataWord)
            throws ModuleException {
        logger.log(Level.WARNING, "[" + super.getType()
                + "] OUT command (word) to port "
                + Integer.toHexString(portAddress).toUpperCase()
                + " received. No action taken.");

        // Do nothing and just return okay
        return;
    }

    public byte[] getIOPortDoubleWord(int portAddress) throws ModuleException,
            ModuleWriteOnlyPortException {
        logger.log(Level.INFO, "[" + super.getType()
                + "] IN command (double word) to port "
                + Integer.toHexString(portAddress).toUpperCase() + " received");
        logger.log(Level.INFO, "[" + super.getType()
                + "] Returned default value 0xFFFFFFFF to eAX");

        // Return dummy value 0xFFFFFFFF
        return new byte[]{(byte) 0x0FF, (byte) 0x0FF, (byte) 0x0FF,
                (byte) 0x0FF};
    }

    public void setIOPortDoubleWord(int portAddress, byte[] dataDoubleWord)
            throws ModuleException {
        logger.log(Level.INFO, "[" + super.getType()
                + "] OUT command (double word) to port "
                + Integer.toHexString(portAddress).toUpperCase()
                + " received. No action taken.");

        // Do nothing and just return okay
        return;
    }

    // ******************************************************************************
    // ModuleSerialPort methods

    public boolean setUARTDevice(UART device, int comPort) {
        // Check if valid com port
        if (comPort >= 0 && comPort < comPorts.length) {
            // Check if device is not already connected
            if (comPorts[comPort].uartDevice == null) {
                comPorts[comPort].uartDevice = device;
                return true;
            } else {
                logger.log(Level.WARNING, "[" + super.getType() + "] COM port "
                        + (comPort + 1) + " already occupied.");
            }
        }
        return false;
    }

    // ******************************************************************************
    // Custom methods

    private void setIRQ(int port, int type) {
        // TODO BK always port=0, type=1 
        boolean raiseInterrupt = false;

        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Module.Type.MOTHERBOARD);
        ModulePIC pic = (ModulePIC)super.getConnection(Module.Type.PIC);

        switch (type) {
            case INTERRUPT_IER: // IER has changed
                raiseInterrupt = true;
                break;

            case INTERRUPT_RXDATA: // Received data available interrupt

                if (comPorts[port].ier_rxdata_enable == 1) {                                    // TODO BK 16 && 32 here
                    comPorts[port].rx_interrupt = 1;
                    raiseInterrupt = true;
                    logger.log(Level.INFO, "[" + super.getType() + "] RXDATA interrupt raised and enabled");
                } else {
                    comPorts[port].rx_ipending = 1;
                    logger.log(Level.INFO, "[" + super.getType() + "] RXDATA interrupt pending...");
                }
                break;

            case INTERRUPT_TXHOLD: // Transmitter Holding Register Empty interrupt
                if (comPorts[port].ier_txhold_enable == 1) {
                    comPorts[port].tx_interrupt = 1;
                    raiseInterrupt = true;
                }
                break;

            case INTERRUPT_RXLSTAT: // Receiver Line Status interrupt
                if (comPorts[port].ier_rxlstat_enable == 1) {
                    comPorts[port].ls_interrupt = 1;
                    raiseInterrupt = true;
                } else {
                    comPorts[port].ls_ipending = 1;
                }
                break;

            case INTERRUPT_MODSTAT: // MODEM Status interrupt
                if ((comPorts[port].ier_modstat_enable == 1)
                        && (comPorts[port].ms_ipending == 1)) {
                    comPorts[port].ms_interrupt = 1;
                    comPorts[port].ms_ipending = 0;
                    raiseInterrupt = true;
                }
                break;

            case INTERRUPT_FIFO:
                if (comPorts[port].ier_rxdata_enable == 1) {
                    comPorts[port].fifo_interrupt = 1;
                    raiseInterrupt = true;
                } else {
                    comPorts[port].fifo_ipending = 1;
                }
                break;
        }

        if (raiseInterrupt && (comPorts[port].mcr_out2 == 1)) {                                 // TODO BK 16 && 32 here
            logger.log(Level.CONFIG, "[" + super.getType() + "] Raising IRQ (signalling to PIC)");
            pic.setIRQ(comPorts[port].irq);
        }
    }

    private void clearIRQ(int port) {
        // Check pending interrupts. If none, clear interrupt at PIC
        if ((comPorts[port].rx_interrupt == 0)
                && (comPorts[port].tx_interrupt == 0)
                && (comPorts[port].ls_interrupt == 0)
                && (comPorts[port].ms_interrupt == 0)
                && (comPorts[port].fifo_interrupt == 0)) {
            logger.log(Level.CONFIG, "[" + super.getType()
                    + "] Lowering IRQ (signalling to PIC)");
            ModulePIC pic = (ModulePIC)super.getConnection(Module.Type.PIC);
            pic.clearIRQ(comPorts[port].irq);
        }
    }

    private void enqueueReceivedData(int port, byte data) {
        logger.log(Level.INFO, "[" + super.getType() + "] enqueueReceivedData(...)");

        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Module.Type.MOTHERBOARD);
        ModulePIC pic = (ModulePIC)super.getConnection(Module.Type.PIC);
        
        boolean raiseInterrupt = false;

        // Check if FIFO is active
        if (comPorts[port].fcr_enable == 1) {
            logger.log(Level.INFO, "[" + super.getType() + "] enqueue data in FIFO");
            // Check if FIFO buffer is full
            if (comPorts[port].rcvrFIFO.size() == 16) {
                logger.log(Level.WARNING, "[" + super.getType() + "] FIFO buffer overflow");
                comPorts[port].lsr_overrun_error = 1;
                this.setIRQ(port, INTERRUPT_RXLSTAT);
            } else {
                // Add data byte to FIFO buffer
                comPorts[port].rcvrFIFO.offer(data);

                switch (comPorts[port].fcr_rxtrigger) {
                    case 1:
                        if (comPorts[port].rcvrFIFO.size() == 4)
                            raiseInterrupt = true;
                        break;

                    case 2:
                        if (comPorts[port].rcvrFIFO.size() == 8)
                            raiseInterrupt = true;
                        break;

                    case 3:
                        if (comPorts[port].rcvrFIFO.size() == 14)
                            raiseInterrupt = true;
                        break;

                    default:
                        raiseInterrupt = true;
                }

                if (raiseInterrupt) {
                    // FIXME: use comPorts[port].fifo_timer_index as on/off
                    // toggle
                    // Deactivate timer
                    motherboard.setTimerActiveState(this, false);
                    comPorts[port].lsr_rxdata_ready = 1;
                    logger.log(Level.INFO, "[" + super.getType() + "] Timer deactivated");

                    // Throw IRQ
                    this.setIRQ(port, INTERRUPT_RXDATA);
                } else {
                    // FIXME: use comPorts[port].fifo_timer_index as on/off
                    // toggle
                    // Activate timer as one shot
                    motherboard.resetTimer(this, (int) (1000000.0 / comPorts[port].baudrate * (comPorts[port].lcr_wordlen_sel + 5) * 16));
                    motherboard.setTimerActiveState(this, true);
                    logger.log(Level.INFO, "[" + super.getType() + "] Timer activated");
                }
            }
        } else {
            // FIFO buffer is not active
            logger.log(Level.INFO, "[" + super.getType() + "] enqueue data in RBR");
            if (comPorts[port].lsr_rxdata_ready == 1) {
                // Unread data still exists in receive buffer
                logger.log(Level.SEVERE, "[" + super.getType() + "] Overflow in receive buffer");
                comPorts[port].lsr_overrun_error = 1;
                this.setIRQ(port, INTERRUPT_RXLSTAT);
            }

            comPorts[port].rbr = data;
            comPorts[port].lsr_rxdata_ready = 1;

            // Throw IRQ
            this.setIRQ(port, INTERRUPT_RXDATA);   // TODO BK 16 and 32 bit the same
        }
    }

}
