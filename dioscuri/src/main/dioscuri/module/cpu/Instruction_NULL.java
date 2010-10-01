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

package dioscuri.module.cpu;

import dioscuri.exception.CPUInstructionException;

import java.util.logging.Logger;

/**
 * @author Bram Lohman
 * @author Bart Kiers
 */
@SuppressWarnings("unused")
public class Instruction_NULL implements Instruction {

    // Attributes
    private CPU cpu;

    // Logging
    private static final Logger logger = Logger.getLogger(Instruction_NULL.class.getName());

    // Constructors

    /**
     * Construct class
     */
    public Instruction_NULL()
    {
    }

    /**
     * Construct class
     *
     * @param processor
     */
    public Instruction_NULL(CPU processor)
    {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Execute instruction
     *
     * @throws CPUInstructionException
     */
    public void execute() throws CPUInstructionException
    {
        // Throw exception for illegal nnn bits
        // byte b1 = (byte) (cpu.getByteFromCode() & 0xFF); // Target
        // instruction
        // System.out.println("Unknown instruction (NULL) encountered at " +
        // cpu.getRegisterHex(0) + ":" + cpu.getRegisterHex(1) +
        // ", next instruction=" + Integer.toHexString(b1));
        throw new CPUInstructionException(
                "Unknown instruction (NULL) encountered at "
                        + cpu.getRegisterHex(0) + ":" + cpu.getRegisterHex(1));
    }
}
