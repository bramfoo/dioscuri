/* $Revision: 163 $ $Date: 2009-08-17 15:12:57 +0000 (ma, 17 aug 2009) $ $Author: blohman $ 
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

import dioscuri.interfaces.UART;

public class ComPort
{

	// Attributes
	// UART port user
	protected UART uartDevice;
	
    // Interrupt variables
    protected int ls_ipending = 0;
    protected int ms_ipending = 0;				// MODEM status interrupt pending: 1 if interrupt pending
    protected int rx_ipending = 0;				// Receive buffer transfer interrupt pending: 1 if interrupt pending
    protected int fifo_ipending = 0;			// FIFO interrupt pending: 1 if interrupt pending
    protected int ls_interrupt = 0;
    protected int ms_interrupt = 0;				// MODEM status interrupt
    protected int rx_interrupt = 0;				// Receive buffer transfer interrupt
    protected int fifo_interrupt = 0;			// FIFO interrupt
    protected int tx_interrupt = 0;				// Transfer interrupt
    
    protected int rx_pollstate = 0;

    // COM-port registers
    
    // RBR, THR, TSR
    protected byte rbr;							// RBR, receiver buffer register 
    protected byte thr;							// THR, transmitter holding register
    protected byte tsr;							// TSR, transmitter shift register (internal register)

	// FIFO buffers
    FIFObuffer rcvrFIFO = new FIFObuffer(16);
    FIFObuffer xmitFIFO = new FIFObuffer(16);

    // IER - interrupt enable register: b0000 0000
    protected int ier_rxdata_enable = 0;		// Received Data Available interrupt: 1 if interrupt is enabled
    protected int ier_txhold_enable = 0;		// Transmitter Holding Register Empty interrupt: 1 if interrupt is enabled
    protected int ier_rxlstat_enable = 0;		// Receiver Line Status interrupt: 1 if interrupt is enabled
    protected int ier_modstat_enable = 0;		// MODEM Status interrupt enable: 1 if interrupt is enabled

    // IIR - Interrupt Identification register: b0000 0001
    protected int iir_ipending = 1;
    protected int iir_int_ID = 0;

    // FCR - FIFO control register: b0000 0000
    protected int fcr_enable = 0;				// FIFO enable indicator: 1 if FIFO buffers XMIT and RCVR are enabled
    protected int fcr_rxtrigger = 0;			// RCVR FIFO trigger level: 0 = 1 byte, 1 = 4 bytes, 2 = 8 bytes, 3 = 14 bytes

    // LCR - Line Control register: b0000 0000
    protected int lcr_wordlen_sel = 0;			// Character length: 0 = 5 bits, 1 = 6 bits, 2 = 7 bits, 3 = 8 bits
    protected int lcr_stopbits = 0;				// Stop bits: number of stop bits in each serial character
    protected int lcr_parity_enable = 0;		// Parity bit enable: 1 if the parity of transmitted or received word should be checked
    protected int lcr_evenparity_sel = 0;		// Even parity selected: 1 if the parity of transmitted or received word should be even
    protected int lcr_stick_parity = 0;			// Stick parity enable: 1 if the stick to parity of bits 3, 5 are 1 while 4 is 0
    protected int lcr_break_cntl = 0;			// Break control bit: 1 if a break condition should be sent to receiving UART
    protected int lcr_dlab = 0;					// Divisor Latch Access Bit (DLAB)

    // MCR - Modem Control register: b0000 0000
    protected int mcr_dtr = 0;
    protected int mcr_rts = 0;
    protected int mcr_out1 = 0;
    protected int mcr_out2 = 0;
    protected int mcr_local_loopback = 0;		// Local loopback mode: 1 if UART should be diagnostic tested

    // LSR - Line Status register: b0110 0000
    protected int lsr_rxdata_ready = 0;			// Receiver Data Ready (DR) indicator: 1 if complete character is stored in FIFO or RBR
    protected int lsr_overrun_error = 0;		// Overrun Error (OE) indicator: 1 if overrun condition is met (full FIFO or unread RBR data)
    protected int lsr_parity_error = 0;			// Parity Error (PE) indicator: 1 if incorrect even or odd parity
    protected int lsr_framing_error = 0;		// Framing Error (FE) indicator: 1 if received character did not have a valid stop bit
    protected int lsr_break_int = 0;			// Break Interrupt (BI) indicator: 1 if received data is in spacing state for longer than a full word transmission time
    protected int lsr_thr_empty = 1;			// Transmitter Holding Register Empty (THRE) indicator: 1 if THR / FIFO is empty
    protected int lsr_tsr_empty = 1;			// Transmitter Empty (TEMT) indicator: 1 if both THR and TSR are empty
    protected int lsr_fifo_error = 0;			// FIFO error indicator: 1 if at least one parity, framing or break error is encountered (FIFO mode only)

    // MSR - Modem Status register: bXXXX 0000
    protected int msr_delta_cts = 0;
    protected int msr_delta_dsr = 0;
    protected int msr_ri_trailedge = 0;
    protected int msr_delta_dcd = 0;
    protected int msr_cts = 0;
    protected int msr_dsr = 0;
    protected int msr_ri = 0;
    protected int msr_dcd = 0;

    // SCR - Scratch register
    protected byte scr = 0;

    // DLL - Divisor Latch LSB
    protected byte dll = 1;

    // DLM - Divisor Latch MSB
    protected byte dlm = 0; 

    
    // Other variables
    // IRQ
    protected int irq;			// IRQ number of this COM-port
    
    // Data transfer speed
    protected int baudrate;		// Baudrate is amount of symbols per second
	
    
    // Constants
    protected final static int RX_IDLE = 0;
    protected final static int RX_POLL = 1;
    protected final static int RX_WAIT = 2;
    
    
    // constructor
    public ComPort()
    {
    	uartDevice = null;
    	
    	rbr = 0;
    	thr = 0;
    	scr = 0;
    	dll = 0;
    	dlm = 0;
    	
    	irq = 0;
    	baudrate = 0;
    }
    
    
	// Methods
	
	protected void reset()
	{
		// Reset all internal COM parameters
		// Reset registers
		
		// TODO: aanvullen met regs
		
		// Scratch reg
		scr = 0;
		
		// Divisor Latch LSB
		dll = 1;
		
		// Divisor Latch MSB
		dlm = 0;
		
		// Baudrate
		baudrate = 115200;

		rx_pollstate = RX_IDLE;

	}
	
}
