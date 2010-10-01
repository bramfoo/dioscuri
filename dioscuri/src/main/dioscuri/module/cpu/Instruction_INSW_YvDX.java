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

import dioscuri.exception.ModuleException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Intel opcode 6D<BR>
 * Copy word from I/O port to ES:DI; update DI register according to DF.<BR>
 * Flags modified: none
 */
public class Instruction_INSW_YvDX implements Instruction {

    // Attributes
    private CPU cpu;
    int portAddress;
    byte[] portValue = new byte[2];
    byte[] ePortValue = new byte[4];
    byte[] transition;

    // Logging
    private static final Logger logger = Logger.getLogger(Instruction_INSW_YvDX.class.getName());

    // Constructors

    /**
     * Class constructor
     */
    public Instruction_INSW_YvDX()
    {
    }

    /**
     * Class constructor specifying processor reference
     *
     * @param processor Reference to CPU class
     */
    public Instruction_INSW_YvDX(CPU processor)
    {
        this();

        // Create reference to cpu class
        cpu = processor;

        // Set transition that holds the amount DI should be altered (word = 2)
        transition = new byte[]{0x00, 0x02};
    }

    // Methods

    /**
     * Copy word from I/O port to ES:DI; update DI register according to DF
     */
    public void execute()
    {

        // Get port address from DX; convert this to unsigned integer to prevent
        // lookup table out of bounds;
        portAddress = (((((int) cpu.dx[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 8) + (((int) cpu.dx[CPU.REGISTER_GENERAL_LOW]) & 0xFF));

        // Read word from I/O space; write to ES:DI; ES segment override is not
        // allowed
        try {
            // Check if the destination is a word or doubleword
            if (cpu.doubleWord) {
                // Get the doubleword
                ePortValue = cpu.getIOPortDoubleWord(portAddress);

                // Write the doubleword as two words into the Extra segment
                // TODO: Should this set to cpu.di or cpu.edi - or both??
                cpu.setWordToExtra(cpu.di, new byte[]{ePortValue[1],
                        ePortValue[0]});

                // Note: DI is updated by word-size here, the other word-size is
                // done below
                // Update DI according to DF flag
                // Check direction of flag: If DF == 0, DI is incremented; if DF
                // == 1, DI is decremented
                if (cpu.flags[CPU.REGISTER_FLAGS_DF]) {
                    // Decrement the DI register by word size
                    cpu.di = Util.subtractWords(cpu.di, transition, 0);
                } else {
                    // Increment the DI register by word size
                    cpu.di = Util.addWords(cpu.di, transition, 0);
                }

                // Set the second part of the doubleword
                cpu.setWordToExtra(cpu.di, new byte[]{ePortValue[3],
                        ePortValue[2]});
                // Second update of DI is done below

            } else // Word-size
            {
                portValue = cpu.getIOPortWord(portAddress);
                cpu.setWordToExtra(cpu.di, portValue);
            }
        } catch (ModuleException e) {
            logger.log(Level.WARNING, "[" + cpu.getType() + "] "
                    + e.getMessage());
        }

        // Update DI according to DF flag
        // Check direction of flag: If DF == 0, DI is incremented; if DF == 1,
        // DI is decremented
        if (cpu.flags[CPU.REGISTER_FLAGS_DF]) {
            // Decrement the DI register by word size
            cpu.di = Util.subtractWords(cpu.di, transition, 0);
        } else {
            // Increment the DI register by word size
            cpu.di = Util.addWords(cpu.di, transition, 0);
        }
    }
}
