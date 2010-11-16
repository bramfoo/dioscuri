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
package dioscuri.module.pci;

import dioscuri.Emulator;
import dioscuri.exception.ModuleException;
import dioscuri.exception.WriteOnlyPortException;
import dioscuri.interfaces.Addressable;
import dioscuri.interfaces.Module;
import dioscuri.module.AbstractModule;
import dioscuri.module.ModuleMotherboard;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of a PCI controller module.
 *
 * @see dioscuri.module.AbstractModule
 *      <p/>
 *      Metadata module ********************************************
 *      general.type : pci general.name : Peripheral Component Interconnect
 *      general.architecture : Von Neumann general.description : Implements a
 *      standard PCI controller general.creator : Tessella Support Services,
 *      Koninklijke Bibliotheek, Nationaal Archief of the Netherlands
 *      general.version : 1.0 general.keywords : PCI, controller
 *      general.relations : motherboard general.yearOfIntroduction :
 *      general.yearOfEnding : general.ancestor : general.successor :
 *      <p/>
 *      Notes: PCI can be defined by configuration mechanism 1 or 2. However,
 *      mechanism 2 is deprecated as of PCI version 2.1; only mechanism 1 should
 *      be used for new systems. The Intel "Saturn" and "Neptune" chipsets use
 *      configuration mechanism 2.
 *      <p/>
 *      Bitfields for PCI configuration address port: Bit(s) Description (Table
 *      P207) 1-0 reserved (00) 7-2 configuration register number (see #0597)
 *      10-8 function 15-11 device number 23-16 bus number 30-24 reserved (0) 31
 *      enable configuration space mapping Configuration registers are
 *      considered DWORDs, so the number in bits 7-2 is the configuration space
 *      address shifted right two bits.
 */
public class PCI extends AbstractModule implements Addressable {

    // Logging
    private static final Logger logger = Logger.getLogger(PCI.class.getName());

    // I/O ports 0CF8-0CFF - PCI Configuration Mechanism 1
    private final static int PORT_PCI1_ADDRESS = 0xCF8;
    private final static int PORT_PCI1_DATA = 0xCFC;

    /**
     * Class constructor
     *
     * @param owner
     */
    public PCI(Emulator owner) {
        super(Module.Type.PCI);
        logger.log(Level.INFO, "[" + super.getType() + "]"
                + " AbstractModule created successfully.");
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.module.AbstractModule
     */
    @Override
    public boolean reset() {
        // Register I/O ports PORT 0CF8-0CFF - PCI Configuration Mechanism 1 and
        // 2 in I/O address space
        ModuleMotherboard motherboard = (ModuleMotherboard) super.getConnection(Module.Type.MOTHERBOARD);
        motherboard.setIOPort(PORT_PCI1_ADDRESS, this);
        motherboard.setIOPort(PORT_PCI1_ADDRESS + 1, this);
        motherboard.setIOPort(PORT_PCI1_ADDRESS + 2, this);
        motherboard.setIOPort(PORT_PCI1_ADDRESS + 3, this);
        motherboard.setIOPort(PORT_PCI1_DATA, this);
        motherboard.setIOPort(PORT_PCI1_DATA + 1, this);
        motherboard.setIOPort(PORT_PCI1_DATA + 2, this);
        motherboard.setIOPort(PORT_PCI1_DATA + 3, this);

        logger.log(Level.CONFIG, "[" + super.getType() + "] AbstractModule has been reset.");
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public byte getIOPortByte(int portAddress) throws ModuleException {
        logger.log(Level.WARNING, "[" + super.getType() + "]"
                + " IN command (byte) to port "
                + Integer.toHexString(portAddress).toUpperCase() + " received");
        logger.log(Level.WARNING, "[" + super.getType() + "]"
                + " Returned default value 0xFF");

        // Return dummy value 0xFF
        return (byte) 0xFF;
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public void setIOPortByte(int portAddress, byte data)
            throws ModuleException {
        logger.log(Level.WARNING, "[" + super.getType() + "]"
                + " OUT command (byte) to port "
                + Integer.toHexString(portAddress).toUpperCase()
                + " received. No action taken.");
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public byte[] getIOPortWord(int portAddress) throws ModuleException,
            WriteOnlyPortException {
        logger.log(Level.WARNING, "[" + super.getType() + "]"
                + " IN command (word) to port "
                + Integer.toHexString(portAddress).toUpperCase() + " received");
        logger.log(Level.WARNING, "[" + super.getType() + "]"
                + " Returned default value 0xFFFF");

        // Return dummy value 0xFFFF
        return new byte[]{(byte) 0xFF, (byte) 0xFF};
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public void setIOPortWord(int portAddress, byte[] dataWord)
            throws ModuleException {
        logger.log(Level.WARNING, "[" + super.getType() + "]"
                + " OUT command (word) to port "
                + Integer.toHexString(portAddress).toUpperCase()
                + " received. No action taken.");
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public byte[] getIOPortDoubleWord(int portAddress) throws ModuleException,
            WriteOnlyPortException {
        logger.log(Level.WARNING, "[" + super.getType() + "]"
                + " IN command (double word) to port "
                + Integer.toHexString(portAddress).toUpperCase() + " received");
        logger.log(Level.WARNING, "[" + super.getType() + "]"
                + " Returned default value 0xFFFFFFFF");

        // Return dummy value 0xFFFFFFFF
        return new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    }

    /**
     * {@inheritDoc}
     *
     * @see dioscuri.interfaces.Addressable
     */
    @Override
    public void setIOPortDoubleWord(int portAddress, byte[] dataDoubleWord)
            throws ModuleException {
        logger.log(Level.WARNING, "[" + super.getType() + "]"
                + " OUT command (double word) to port "
                + Integer.toHexString(portAddress).toUpperCase()
                + " received. No action taken.");
    }
}
