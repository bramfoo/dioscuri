/*
 * $Revision: 1.3 $ $Date: 2007-10-04 14:25:46 $ $Author: jrvanderhoeven $
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

package nl.kbna.dioscuri.module.serialport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.kbna.dioscuri.Emulator;
import nl.kbna.dioscuri.module.Module;
import nl.kbna.dioscuri.module.ModulePIC;
import nl.kbna.dioscuri.module.ModuleSerialPort;
import nl.kbna.dioscuri.module.ModuleMotherboard;
import nl.kbna.dioscuri.exception.ModuleException;
import nl.kbna.dioscuri.exception.ModuleUnknownPort;
import nl.kbna.dioscuri.exception.ModuleWriteOnlyPortException;

/**
 * An implementation of a parallel port module.
 *  
 * @see Module
 * 
 * Metadata module
 * ********************************************
 * general.type                : serialport
 * general.name                : General Serial Port
 * general.architecture        : Von Neumann
 * general.description         : Models a 9-pin serial port 
 * general.creator             : Tessella Support Services, Koninklijke Bibliotheek, Nationaal Archief of the Netherlands
 * general.version             : 1.0
 * general.keywords            : serial port, port, RS232, DE-9, COM, 
 * general.relations           : Motherboard
 * general.yearOfIntroduction  : 
 * general.yearOfEnding        : 
 * general.ancestor            : 
 * general.successor           : 
 * 
 * Notes:
 * "Some of my readers ask me what a 'Serial Port' is. The answer is: 
 * I don't know. Is it some kind of wine you have with breakfast?"
 * - Module serial port supports up to 4 COM ports
 * - Information used in this module was taken from: http://mudlist.eorbit.net/~adam/pickey/ports.html
 * 
 * PORT 03F8-03FF - serial port (8250,8250A,8251,16450,16550,16550A,etc.) COM1
 * Range:	PORT 02E8h-02EFh (COM2), PORT 02F8h-02FFh (typical non-PS/2 COM3), and PORT 03E8h-03EFh (typical non-PS/2 COM4)
 * Note:	chips overview:
 * 	 8250  original PC, specified up to 56Kbd, but mostly runs
 *	       only 9600Bd, no scratchregister, bug: sometimes shots
 *	       ints without reasons
 *	 8250A, 16450, 16C451: ATs, most chips run up to 115KBd,
 *	       no bug: shots no causeless ints
 *	 8250B: PC,XT,AT, pseudo bug: shots one causeless int for
 *		compatibility with 8250, runs up to 56KBd
 *	 16550, 16550N, 16550V: early PS/2, FIFO bugs
 *	 16550A,16550AF,16550AFN,16550C,16C551,16C552: PS/2, FIFO ok
 *	 82510: laptops & industry, multi emulation mode
 *		(default=16450), special-FIFO.
 *	 8251: completely different synchronous SIO chip, not compatible!
 *
 * 03F8   W  serial port, transmitter holding register (THR), which contains the character to be sent. Bit 0 is sent first.
 *		bit 7-0	  data bits when DLAB=0 (Divisor Latch Access Bit)
 * 03F8  R	  receiver buffer register (RBR), which contains the received character. Bit 0 is received first
 *      bit 7-0   data bits when DLAB=0 (Divisor Latch Access Bit)
 * 03F8  RW  divisor latch low byte (DLL) when DLAB=1 (see #P189)
 * 03F9  RW  divisor latch high byte (DLM) when DLAB=1 (see #P189)
 * 03F9  RW  interrupt enable register (IER) when DLAB=0 (see #P190)
 * 03FA  R	  interrupt identification register (see #P191)
 * 	Information about a pending interrupt is stored here. When the ID
 * 	  register is addressed, thehighest priority interrupt is held, and
 * 	  no other interrupts are acknowledged until the CPU services that interrupt.
 * 03FA   W  16650 FIFO Control Register (FCR) (see #P192)
 * 03FB  RW  line control register (LCR) (see #P193)
 * 03FC  RW  modem control register (see #P194)
 * 03FD  R	  line status register (LSR) (see #P195)
 * 03FE  R	  modem status register (MSR) (see #P196)
 * 03FF  RW  scratch register (SCR)
 * 	(not used for serial I/O; available to any application using 16450, 16550) (not present on original 8250)
 * 
 */

public class SerialPort extends ModuleSerialPort
{

    // Attributes

    // Relations
    private Emulator emu;
    private String[] moduleConnections = new String[] {"motherboard", "pic"}; 
    private ModuleMotherboard motherboard;
    private ModulePIC pic;

    // Toggles
    private boolean isObserved;
    private boolean debugMode;
    
    // Logging
    private static Logger logger = Logger.getLogger("nl.kbna.dioscuri.module.serialport");
    
    // IRQ
    private int irqNumber;                  // Interrupt number assigned by PIC

    // Helper variables
    private String[] cmd;
    private int lastReadPort;
    private int lastReadReturn;
    private int lastWritePort;
    private int lastWriteData;
    
    // COM-ports
    private ComPort[] ports;
    
    // Data queue
    private Stack<Integer> readPortQ = new Stack<Integer>();

    
    // Constants
    // Module specifics
    public final static int MODULE_ID       = 1;
    public final static String MODULE_TYPE  = "serialport";
    public final static String MODULE_NAME  = "RS232 serial port";
    
    public final static int SERIALPORT_TOTALPORTS = 4;	// Defines the total number of COM ports

    // COM1
    private final static int THR = 0x3F8;            // Write-only port
    private final static int RBR = 0x3F8;            // Read-only port
    private final static int DLL = 0x3F8;            // Read/Write port
    private final static int DLM = 0x3F9;            // Read/Write port
    private final static int IER = 0x3F9;            // Read/Write port
    private final static int IIR = 0x3FA;            // Read-only port
    private final static int FCR = 0x3FA;            // Write-only port
    private final static int LCR = 0x3FB;            // Read/Write port
    private final static int MCR = 0x3FC;            // Read/Write port    
    private final static int LSR = 0x3FD;            // Read-only port
    private final static int MSR = 0x3FE;            // Read-only port
    private final static int SCR = 0x3FF;            // Read/Write port
    
    // COM2
    private final static int THR2 = 0x2F8;            // Write-only port
    private final static int RBR2 = 0x2F8;            // Read-only port
    private final static int DLL2 = 0x2F8;            // Read/Write port
    private final static int DLM2 = 0x2F9;            // Read/Write port
    private final static int IER2 = 0x2F9;            // Read/Write port
    private final static int IIR2 = 0x2FA;            // Read-only port
    private final static int FCR2 = 0x2FA;            // Write-only port
    private final static int LCR2 = 0x2FB;            // Read/Write port
    private final static int MCR2 = 0x2FC;            // Read/Write port    
    private final static int LSR2 = 0x2FD;            // Read-only port
    private final static int MSR2 = 0x2FE;            // Read-only port
    private final static int SCR2 = 0x2FF;            // Read/Write port
    
    // COM3
    private final static int THR3 = 0x3E8;            // Write-only port
    private final static int RBR3 = 0x3E8;            // Read-only port
    private final static int DLL3 = 0x3E8;            // Read/Write port
    private final static int DLM3 = 0x3E9;            // Read/Write port
    private final static int IER3 = 0x3E9;            // Read/Write port
    private final static int IIR3 = 0x3EA;            // Read-only port
    private final static int FCR3 = 0x3EA;            // Write-only port
    private final static int LCR3 = 0x3EB;            // Read/Write port
    private final static int MCR3 = 0x3EC;            // Read/Write port    
    private final static int LSR3 = 0x3ED;            // Read-only port
    private final static int MSR3 = 0x3EE;            // Read-only port
    private final static int SCR3 = 0x3EF;            // Read/Write port
    
    // COM4
    private final static int THR4 = 0x2E8;            // Write-only port
    private final static int RBR4 = 0x2E8;            // Read-only port
    private final static int DLL4 = 0x2E8;            // Read/Write port
    private final static int DLM4 = 0x2E9;            // Read/Write port
    private final static int IER4 = 0x2E9;            // Read/Write port
    private final static int IIR4 = 0x2EA;            // Read-only port
    private final static int FCR4 = 0x2EA;            // Write-only port
    private final static int LCR4 = 0x2EB;            // Read/Write port
    private final static int MCR4 = 0x2EC;            // Read/Write port    
    private final static int LSR4 = 0x2ED;            // Read-only port
    private final static int MSR4 = 0x2EE;            // Read-only port
    private final static int SCR4 = 0x2EF;            // Read/Write port
    
    
    // Constructor

    /**
     * Class constructor
     * 
     */
    public SerialPort(Emulator owner)
    {
        emu = owner;
        
        // Initialise variables
        isObserved = false;
        debugMode = false;
        
        // Initialise IRQ
        irqNumber = -1;
        
        // Init ports
        ports = new ComPort[SERIALPORT_TOTALPORTS];

        // ELKS boot replies:
        readPortQ.push(0x00);
        readPortQ.push(0x00);
        readPortQ.push(0x00);
        readPortQ.push(0x00);
        readPortQ.push(0x00);
        readPortQ.push(0x00);
        readPortQ.push(0x00);
        readPortQ.push(0x00);
        readPortQ.push(0xFF);
        readPortQ.push(0xFF);
        readPortQ.push(0xFF);
        readPortQ.push(0xFF);
        readPortQ.push(0x60);
        readPortQ.push(0x00);
        readPortQ.push(0xC1);
        readPortQ.push(0x00);
        readPortQ.push(0x00);
        readPortQ.push(0x00);
        // Bochs BIOS replies:
        readPortQ.push(0xFF);
        readPortQ.push(0xFF);
        readPortQ.push(0xFF);
        readPortQ.push(0x02);
        readPortQ.push(0x02);

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
        else if (mod.getType().equalsIgnoreCase("pic"))
        {
            this.pic = (ModulePIC)mod;
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
        if (motherboard != null && pic != null)
        {
            return true;
        }
        return false;
    }


    /**
     * Reset all parameters of module
     * 
     * @return boolean true if module has been reset successfully, false otherwise
     */
    public boolean reset()
    {
        // Register I/O ports 0x3F[8-F], 0x2F[8-F], 0x3E[8-F], 0x2E[8-F] in I/O address space
        motherboard.setIOPort(DLL, this);
        motherboard.setIOPort(DLM, this);
        motherboard.setIOPort(IIR, this);
        motherboard.setIOPort(LCR, this);
        motherboard.setIOPort(MCR, this);
        motherboard.setIOPort(LSR, this);
        motherboard.setIOPort(MSR, this);
        motherboard.setIOPort(SCR, this);

        motherboard.setIOPort(DLL2, this);
        motherboard.setIOPort(DLM2, this);
        motherboard.setIOPort(IIR2, this);
        motherboard.setIOPort(LCR2, this);
        motherboard.setIOPort(MCR2, this);
        motherboard.setIOPort(LSR2, this);
        motherboard.setIOPort(MSR2, this);
        motherboard.setIOPort(SCR2, this);
        
        motherboard.setIOPort(DLL3, this);
        motherboard.setIOPort(DLM3, this);
        motherboard.setIOPort(IIR3, this);
        motherboard.setIOPort(LCR3, this);
        motherboard.setIOPort(MCR3, this);
        motherboard.setIOPort(LSR3, this);
        motherboard.setIOPort(MSR3, this);
        motherboard.setIOPort(SCR3, this);
        
        motherboard.setIOPort(DLL4, this);
        motherboard.setIOPort(DLM4, this);
        motherboard.setIOPort(IIR4, this);
        motherboard.setIOPort(LCR4, this);
        motherboard.setIOPort(MCR4, this);
        motherboard.setIOPort(LSR4, this);
        motherboard.setIOPort(MSR4, this);
        motherboard.setIOPort(SCR4, this);
        
        // Request IRQ number
        irqNumber = pic.requestIRQNumber(this);
        if (irqNumber > -1)
        {
            logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]" + " IRQ number set to: " + irqNumber);
        }
        else
        {
            logger.log(Level.WARNING, "[" + MODULE_TYPE + "]" + " Request of IRQ number failed.");
        }

        // TODO: reset COM-ports
        
        logger.log(Level.INFO, "[" + MODULE_TYPE + "] Module has been reset.");

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
    	// Push all bytes in queue
    	if (data != null)
    	{
        	for (int i = 0; i < data.length; i++)
        	{
        		readPortQ.push((int)data[i] & 0xFF);
        	}
            return true;
    	}
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
        String dump ="Serial port status:\n";
        
        dump += "This module is only a stub, no contents available" + "\n";
        
        return dump;
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
        return -1;
    }

    /**
     * Defines the interval between subsequent updates
     * 
     * @param int interval in microseconds
     */
    public void setUpdateInterval(int interval) {}


    /**
     * Update device
     * 
     */
    public void update() {}
    

    /**
     * IN instruction to serial port<BR>
     * @param portAddress   the target port; can be any of 0x03F[8-F], 0x02F[8-F], 0x03E[8-F], or 2E[8-F]<BR>
     * 
     * IN to portAddress 378h does ...<BR>
     * IN to portAddress 379h does ...<BR>
     * IN to portAddress 37Ah does ...<BR>
     * 
     * @return byte of data from ...
     */
    public byte getIOPortByte(int portAddress) throws ModuleUnknownPort, ModuleWriteOnlyPortException
    {
        logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]" + " IO read from " + portAddress);
        int returnValue = 0x00;
        
/* 
 * Interactive mode
 */
//        logger.log(Level.WARNING, motherboard.getCurrentInstructionNumber() + " " + "[" + MODULE_TYPE + "]" + " Last port write: [0x" + Integer.toHexString(lastWritePort).toUpperCase() + "] = 0x" + Integer.toHexString(lastWriteData).toUpperCase());
//        
//        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
//        String input;
//        
//        // Flush streams before printing
//        
//        System.out.print("[" + MODULE_TYPE + "]" + " Return value (byte) for port [0x" + Integer.toHexString(portAddress).toUpperCase() + "] read: 0x");
//        try
//        {
//            input = stdin.readLine();
//            returnValue = Integer.parseInt(input, 16);
//        }
//        catch (IOException e)
//        {
//            logger.log(Level.SEVERE, "Unable to read input; returning default value 0x00");
//            returnValue = 0x00;
//        }
//        catch (NumberFormatException e2)
//        {
//            logger.log(Level.SEVERE, "Unable to parse input to integer value; returning default value 0x00");
//            returnValue = 0x00;
//        }
//        
//        lastReadPort = portAddress;
//        lastReadReturn = returnValue;
//        return (byte) returnValue;
/*
 * Queue mode
 */      
        if (!readPortQ.isEmpty())
        {
            return readPortQ.pop().byteValue();
        }
        else
        {
            logger.log(Level.SEVERE, "[" + MODULE_TYPE + "]" + " Queue empty, returning default value 0x00");
            return (byte) 0x00;
        }
        
    }

    /**
     * OUT instruction to serial port<BR>
     * @param portAddress   the target port; can be any of 0x027[8-A], 0x037[8-A], or 0x03B[C-E]<BR>
     * 
     * OUT to portAddress 378h does ...<BR>
     * OUT to portAddress 379h does ...<BR>
     * OUT to portAddress 37Ah does ...<BR>
     */
    public void setIOPortByte(int portAddress, byte data) throws ModuleUnknownPort
    {
        logger.log(Level.CONFIG, "[" + MODULE_TYPE + "]" + " IO write to " + portAddress + " = " + data);
        
        switch (portAddress)
        {
           case (DLM):
            case (DLM2):
            case (DLM3):
            case (DLM4):                
                logger.log(Level.FINE, "[" + MODULE_TYPE + "] OUT on port " + Integer.toHexString(portAddress).toUpperCase() + " received, not handled");
                break;
            
            default:
                    throw new ModuleUnknownPort("[" + MODULE_TYPE + "] Unknown I/O port requested");
        }
        
        lastWritePort = portAddress;
        lastWriteData = data;
    }

    public byte[] getIOPortWord(int portAddress) throws ModuleException, ModuleWriteOnlyPortException
    {
        logger.log(Level.WARNING, "[" + MODULE_TYPE + "] IN command (word) to port " + Integer.toHexString(portAddress).toUpperCase() + " received");
        logger.log(Level.WARNING, "[" + MODULE_TYPE + "] Returned default value 0xFFFF to AX");
        
        // Return dummy value 0xFFFF
        return new byte[] { (byte) 0x0FF, (byte) 0x0FF };
    }


    public void setIOPortWord(int portAddress, byte[] dataWord) throws ModuleException
    {
        logger.log(Level.WARNING, "[" + MODULE_TYPE + "] OUT command (word) to port " + Integer.toHexString(portAddress).toUpperCase() + " received. No action taken.");
        
        // Do nothing and just return okay
        return;
    }


    public byte[] getIOPortDoubleWord(int portAddress) throws ModuleException, ModuleWriteOnlyPortException
    {
        logger.log(Level.WARNING, "[" + MODULE_TYPE + "] IN command (double word) to port " + Integer.toHexString(portAddress).toUpperCase() + " received");
        logger.log(Level.WARNING, "[" + MODULE_TYPE + "] Returned default value 0xFFFFFFFF to eAX");
        
        // Return dummy value 0xFFFFFFFF
        return new byte[] { (byte) 0x0FF, (byte) 0x0FF, (byte) 0x0FF, (byte) 0x0FF };
    }


    public void setIOPortDoubleWord(int portAddress, byte[] dataDoubleWord) throws ModuleException
    {
        logger.log(Level.WARNING, "[" + MODULE_TYPE + "] OUT command (double word) to port " + Integer.toHexString(portAddress).toUpperCase() + " received. No action taken.");
        
        // Do nothing and just return okay
        return;
    }

    //******************************************************************************
    // Custom methods
}
