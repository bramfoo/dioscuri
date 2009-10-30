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

import java.util.logging.Level;
import java.util.logging.Logger;

import dioscuri.exception.ModuleException;

/**
 * Intel opcode 6F<BR>
 * Output word from DS:(E)SI to I/O port (specified in DX); update SI register
 * according to DF.<BR>
 * Flags modified: none
 */
public class Instruction_OUTSW_DXXv implements Instruction {

    // Attributes
    private CPU cpu;
    int portAddress;

    // Note: the addressbyte passed here is a value chosen so if the
    // segmentOverride fails (which it shouldn't!),
    // the DS segment is still chosen.
    byte defaultAddressByte = 0;

    byte[] memoryValue = new byte[2];
    byte[] eMemoryValue = new byte[2];

    byte[] transition;

    // Logging
    private static Logger logger = Logger.getLogger("dioscuri.module.cpu");

    // Constructors
    /**
     * Class constructor
     * 
     */
    public Instruction_OUTSW_DXXv() {
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor
     *            Reference to CPU class
     */
    public Instruction_OUTSW_DXXv(CPU processor) {
        this();

        // Create reference to cpu class
        cpu = processor;
        // Set transition that holds the amount SI should be altered (word = 2)
        transition = new byte[] { 0x00, 0x02 };

    }

    // Methods

    /**
     * Output word from DS:(E)SI to I/O port (specified in DX); update SI
     * register according to DF.
     */
    public void execute() {

        // Get port address from DX; convert this to unsigned integer to prevent
        // lookup table out of bounds;
        portAddress = (((((int) cpu.dx[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 8) + (((int) cpu.dx[CPU.REGISTER_GENERAL_LOW]) & 0xFF));

        // Read word from DS:SI; write to portAddress
        // DS segment may be overridden.
        try {
            // Check if the destination is a word or doubleword
            if (cpu.doubleWord) {
                // Get the doubleword
                eMemoryValue = cpu.getWordFromMemorySegment(defaultAddressByte,
                        cpu.si);
                // Increment offset when getting the next word
                memoryValue = cpu.getWordFromMemorySegment(defaultAddressByte,
                        Util.addWords(cpu.si, new byte[] { 0x00, 0x02 }, 0));

                // Write the doubleword to the I/O port
                cpu.setIOPortDoubleWord(portAddress, new byte[] {
                        eMemoryValue[CPU.REGISTER_GENERAL_LOW],
                        eMemoryValue[CPU.REGISTER_GENERAL_HIGH],
                        memoryValue[CPU.REGISTER_GENERAL_LOW],
                        memoryValue[CPU.REGISTER_GENERAL_HIGH] });

                // Note: SI is updated by word-size here, the other word-size is
                // done below
                // Update SI according to DF flag
                // Check direction of flag: If DF == 0, SI is incremented; if DF
                // == 1, SI is decremented
                if (cpu.flags[CPU.REGISTER_FLAGS_DF]) {
                    // Decrement the SI register by word size
                    cpu.si = Util.subtractWords(cpu.si, transition, 0);
                } else {
                    // Increment the SI register by word size
                    cpu.si = Util.addWords(cpu.si, transition, 0);
                }

            } else // Word-size
            {
                memoryValue = cpu.getWordFromMemorySegment(defaultAddressByte,
                        cpu.si);

                // Write the word to the I/O port
                cpu.setIOPortWord(portAddress, memoryValue);
            }
        } catch (ModuleException e) {
            logger.log(Level.WARNING, "[" + cpu.getType() + "] "
                    + e.getMessage());
        }

        // Update SI according to DF flag
        // Check direction of flag: If DF == 0, SI is incremented; if DF == 1,
        // SI is decremented
        if (cpu.flags[CPU.REGISTER_FLAGS_DF]) {
            // Decrement the SI register by word size
            cpu.si = Util.subtractWords(cpu.si, transition, 0);
        } else {
            // Increment the SI register by word size
            cpu.si = Util.addWords(cpu.si, transition, 0);
        }
    }
}
