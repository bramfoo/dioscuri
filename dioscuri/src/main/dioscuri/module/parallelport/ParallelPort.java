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

package dioscuri.module.parallelport;

/*
 * Information used in this module was taken from:
 * - http://mudlist.eorbit.net/~adam/pickey/ports.html
 */

import dioscuri.Emulator;
import dioscuri.exception.ModuleException;
import dioscuri.exception.UnknownPortException;
import dioscuri.exception.WriteOnlyPortException;
import dioscuri.interfaces.Module;
import dioscuri.module.ModuleMotherboard;
import dioscuri.module.ModuleParallelPort;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of a parallel port module.
 *
 * @see dioscuri.module.AbstractModule
 *      <p/>
 *      Metadata module ********************************************
 *      general.type : parallelport general.name : General Parallel Port
 *      general.architecture : Von Neumann general.description : Models a 25-pin
 *      parallel port general.creator : Tessella Support Services, Koninklijke
 *      Bibliotheek, Nationaal Archief of the Netherlands general.version : 1.0
 *      general.keywords : Parallel port, port, IEEE 1284, Centronics, LPT,
 *      printer general.relations : Motherboard general.yearOfIntroduction :
 *      general.yearOfEnding : general.ancestor : general.successor :
 */
public class ParallelPort extends ModuleParallelPort {

    // Logging
    private static final Logger logger = Logger.getLogger(ParallelPort.class.getName());

    // Constants
    // FIXME: Separate ports into different parallel devices
    private final static int DATA_PORT = 0x378; // Read/Write port (?)
    private final static int STATUS_PORT = 0x379; // Read/Write port
    private final static int CONTROL_PORT = 0x37A; // Read/Write port

    private final static int DATA_PORT2 = 0x278; // Read/Write port (?)
    private final static int STATUS_PORT2 = 0x279; // Read/Write port
    private final static int CONTROL_PORT2 = 0x27A; // Read/Write port

    // Constructor

    /**
     * Class constructor
     *
     * @param owner
     */
    public ParallelPort(Emulator owner)
    {
        logger.log(Level.INFO, "[" + super.getType() + "] " + getClass().getName()
                + " -> AbstractModule created successfully.");
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.AbstractModule
     */
    @Override
    public boolean reset()
    {

        ModuleMotherboard motherboard = (ModuleMotherboard) super.getConnection(Module.Type.MOTHERBOARD);

        // Register I/O ports 0x37[8-A], 0x27[8-A] in I/O address space
        motherboard.setIOPort(DATA_PORT, this);
        motherboard.setIOPort(STATUS_PORT, this);
        motherboard.setIOPort(CONTROL_PORT, this);

        motherboard.setIOPort(DATA_PORT2, this);
        motherboard.setIOPort(STATUS_PORT2, this);
        motherboard.setIOPort(CONTROL_PORT2, this);

        logger.log(Level.INFO, "[" + super.getType() + "] AbstractModule has been reset.");

        return true;

    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public byte getIOPortByte(int portAddress) throws UnknownPortException,
            WriteOnlyPortException
    {
        logger.log(Level.CONFIG, "[" + super.getType() + "]" + " IO read from "
                + portAddress);

        switch (portAddress) {
            // Return identical values to Bochs during BIOS boot:
            case (DATA_PORT):
                logger.log(Level.INFO, "[" + super.getType()
                        + "] returning default value 'available'");
                return (byte) 0xAA;

            case (DATA_PORT2):
                logger.log(Level.INFO, "[" + super.getType()
                        + "] returning default value 'not available'");
                return (byte) 0xFF;

            case (STATUS_PORT):
            case (STATUS_PORT2):
                logger.log(Level.INFO, "[" + super.getType()
                        + "] returning default value 0x58");
                return 0x58;

            // Return identical values to Bochs during BIOS boot:
            case (CONTROL_PORT):
                logger.log(Level.INFO, "[" + super.getType()
                        + "] returning default value 'available'");
                return (byte) 0x0C;

            case (CONTROL_PORT2):
                logger.log(Level.INFO, "[" + super.getType()
                        + "] returning default value 'not available'");
                return (byte) 0xFF;

            default:
                throw new UnknownPortException("[" + super.getType()
                        + "] Unknown I/O port requested");
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public void setIOPortByte(int portAddress, byte data)
            throws UnknownPortException
    {
        logger.log(Level.CONFIG, "[" + super.getType() + "]" + " IO write to "
                + portAddress + " = " + data);

        switch (portAddress) {
            case (DATA_PORT):
                logger.log(Level.INFO, "[" + super.getType() + "] OUT on port "
                        + Integer.toHexString(DATA_PORT).toUpperCase()
                        + " received, not handled");
                return;

            case (DATA_PORT2):
                logger.log(Level.INFO, "[" + super.getType() + "] OUT on port "
                        + Integer.toHexString(portAddress).toUpperCase()
                        + " received, not handled");
                return;

            case (STATUS_PORT):
            case (STATUS_PORT2):
                // Do nothing
                logger.log(Level.INFO, "[" + super.getType() + "] OUT on port "
                        + Integer.toHexString(portAddress).toUpperCase()
                        + " received, not handled");
                return;

            case (CONTROL_PORT):
                logger.log(Level.INFO, "[" + super.getType() + "] OUT on port "
                        + Integer.toHexString(CONTROL_PORT).toUpperCase()
                        + " received, not handled");
                return;

            case (CONTROL_PORT2):
                logger.log(Level.INFO, "[" + super.getType() + "] OUT on port "
                        + Integer.toHexString(CONTROL_PORT2).toUpperCase()
                        + " received, not handled");
                return;

            default:
                throw new UnknownPortException("[" + super.getType()
                        + "] Unknown I/O port requested");
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public byte[] getIOPortWord(int portAddress) throws ModuleException,
            UnknownPortException, WriteOnlyPortException
    {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public void setIOPortWord(int portAddress, byte[] dataWord)
            throws ModuleException, UnknownPortException
    {
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public byte[] getIOPortDoubleWord(int portAddress) throws ModuleException,
            UnknownPortException, WriteOnlyPortException
    {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public void setIOPortDoubleWord(int portAddress, byte[] dataDoubleWord)
            throws ModuleException, UnknownPortException
    {
    }
}
