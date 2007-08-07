/* $Revision: 1.3 $ $Date: 2007-08-07 15:03:55 $ $Author: blohman $
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

package nl.kbna.dioscuri.module.cpu;

	/**
	 * Intel opcode A1<BR>
	 * Copy word from DS:DISPL (DISPL given by word following opcode) to register AX.<BR>
	 * Flags modified: none
	 */
public class Instruction_MOV_AXOv implements Instruction {

	// Attributes
	private CPU cpu;
	private byte[] displ = new byte[2];
	private byte[] tempWord = new byte[2];
    private byte[] word0x02 = new byte[] {0x00, 0x02};
    private byte dataSegmentAddressByte = 0;
		
	
	// Constructors
	/**
	 * Class constructor
	 */
	public Instruction_MOV_AXOv()	{}
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_MOV_AXOv(CPU processor)
	{
		this();
		
		// Create reference to cpu class
		cpu = processor;
	}

	
	// Methods
	
	/**
	 * Copy word from DS:DISPL (DISPL given by word following opcode) to register AX
	 */
	public void execute()
	{
        // Get displacement within segment
		// Honour Intel little-endian: first byte is LSB, followed by MSB. Order array [MSB, LSB]
		displ = cpu.getWordFromCode();
		
        // Get word at DS:DISPL and place in AX
        // This memory segment defaults to DS:DISPL unless there is a segment override
        // Because getWordFromMemorySegment expects an address byte to determine the segment,
        // a default of 0 is used here to end up in the Data Segment (unless of course there is an override,
        // but that is handled in getWord[..])
        cpu.ax = cpu.getWordFromMemorySegment(dataSegmentAddressByte, displ);

        if (cpu.doubleWord)
        {
            // Increment displacement
            tempWord = Util.addWords(displ, word0x02, 0);
            System.arraycopy(tempWord, 0, displ, 0, tempWord.length);
            
            // Store upper 16 bits in eAX
            cpu.eax = cpu.getWordFromMemorySegment(dataSegmentAddressByte, displ);
        }
	}
}
