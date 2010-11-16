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
 * Intel opcode A3<BR>
 * Copy word from register AX to DS:DISPL (DISPL given by word following
 * opcode).<BR>
 * Flags modified: none
 */
public class Instruction_MOV_OvAX implements Instruction {

    // Attributes
    private CPU cpu;
    private byte[] displ = new byte[2];
    private byte[] tempWord = new byte[2];
    private byte[] word0x02 = new byte[]{0x00, 0x02};
    private byte dataSegmentAddressByte = 0;

    // Constructors

    /**
     * Class constructor
     */
    public Instruction_MOV_OvAX() {
    }

    /**
     * Class constructor specifying processor reference
     *
     * @param processor Reference to CPU class
     */
    public Instruction_MOV_OvAX(CPU processor) {
        this();

        // Create reference to cpu class
        cpu = processor;
    }

    // Methods

    /**
     * Copy word from register AX to DS:DISPL (DISPL given by word following
     * opcode)
     */
    public void execute() {
        // Get displacement within segment
        displ = cpu.getWordFromCode();

        // Get word at AX and assign to memory segment
        // This memory segment defaults to DS:DISPL unless there is a segment
        // override
        // Because setWordInMemorySegment expects an address byte to determine
        // the segment,
        // a default of 0 is used here to end up in the Data Segment (unless of
        // course there is an override,
        // but that is handled in setWord[..])
        cpu.setWordInMemorySegment(dataSegmentAddressByte, displ, cpu.ax);

        if (cpu.doubleWord) {
            // Increment displacement
            tempWord = Util.addWords(displ, word0x02, 0);
            System.arraycopy(tempWord, 0, displ, 0, tempWord.length);

            // Store upper 16 bits of eAX in memory
            cpu.setWordInMemorySegment(dataSegmentAddressByte, displ, cpu.eax);
        }

    }
}
