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

/**
 * Intel opcode E8<BR>
 * Call to procedure within the current code segment (intrasegment call)
 * indicated by immediate signed word.<BR>
 * Displacement is relative to next instruction.<BR>
 * Flags modified: none
 */
public class Instruction_CALL_Jv implements Instruction {

    // Attributes
    private CPU cpu;

    byte[] displacement = new byte[2];
    int overUnderFlowCheck;

    // Constructors
    /**
     * Class constructor
     * 
     */
    public Instruction_CALL_Jv() {
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor
     *            Reference to CPU class
     */
    public Instruction_CALL_Jv(CPU processor) {
        // this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Execute call to procedure indicated by immediate signed word
     */
    public void execute() {
        // Call is relative to _next_ instruction, but by the time we change
        // the IP, it has already been incremented thrice, so no extra
        // arithmetic necessary
        displacement = cpu.getWordFromCode();

        // Push current instruction pointer onto stack
        cpu.setWordToStack(cpu.ip);

        // Add to current IP (interpreted unsigned) the displacement
        // (interpreted signed)
        // However, lower byte is always interpreted unsigned (as high byte
        // carries sign)
        // So cast low bytes to int for unsigned, then add to check for
        // over/underflow in IP
        overUnderFlowCheck = (((int) (cpu.ip[CPU.REGISTER_GENERAL_LOW])) & 0xFF)
                + (((int) displacement[CPU.REGISTER_GENERAL_LOW]) & 0xFF);
        cpu.ip[CPU.REGISTER_GENERAL_LOW] = (byte) (overUnderFlowCheck);

        // Need to check for possible overflow/underflow in IP[low]
        if (overUnderFlowCheck > 255) {
            // Overflow
            cpu.ip[CPU.REGISTER_GENERAL_HIGH]++;
        }

        // Update IP[high] with displacement; this can be done unsigned
        cpu.ip[CPU.REGISTER_GENERAL_HIGH] = (byte) (cpu.ip[CPU.REGISTER_GENERAL_HIGH] + displacement[CPU.REGISTER_GENERAL_HIGH]);
    }
}
