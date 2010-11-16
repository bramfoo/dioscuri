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
 * Intel opcode D7<BR>
 * Set AL to memory byte DS:[BX + unsigned AL].<BR>
 * Flags modified: none
 */
public class Instruction_XLAT implements Instruction {

    // Attributes
    private CPU cpu;

    byte defaultAddressByteDS;
    byte[] memoryReferenceLocation;

    // Constructors

    /**
     * Class constructor
     */
    public Instruction_XLAT() {
    }

    /**
     * Class constructor specifying processor reference
     *
     * @param processor Reference to CPU class
     */
    public Instruction_XLAT(CPU processor) {
        // Create reference to cpu class
        cpu = processor;

        // The addressbyte set here is hardcoded to reference DS, unless there
        // is a segment override
        defaultAddressByteDS = 0x00;
        memoryReferenceLocation = new byte[2];
    }

    // Methods

    /**
     * Set AL to memory byte DS:[BX + unsigned AL]
     */
    public void execute() {
        // Set memory location as BX + AL
        memoryReferenceLocation = Util.addWords(cpu.bx, new byte[]{0x00,
                cpu.ax[CPU.REGISTER_GENERAL_LOW]}, 0);

        // Get byte from DS:[BX+AL] (segment overrides are possible) and store
        // in AL
        cpu.ax[CPU.REGISTER_GENERAL_LOW] = cpu.getByteFromMemorySegment(
                defaultAddressByteDS, memoryReferenceLocation);
    }
}
