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
	 * Intel opcode 4D<BR>
	 * Decrement general register BP.<BR>
	 * Flags modified: OF, SF, ZF, AF, PF
	 */
public class Instruction_DEC_BP implements Instruction {

	// Attributes
	private CPU cpu;
    private byte[] oldDest;
    private byte[] decWord;
    private byte[] temp;
	
	
	// Constructors
	/**
	 * Class constructor
	 * 
	 */
	public Instruction_DEC_BP()	{}
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_DEC_BP(CPU processor)
	{
		//this();
		
		// Create reference to cpu class
		cpu = processor;
        oldDest = new byte[2];
        temp = new byte[2];

        // Set decrement word to 1
        decWord = new byte[] { 0x00, 0x01 };
	}

	
	// Methods
    
    /**
     * Decrement general register BP
     */
    public void execute()
    {
        // Store old value
        System.arraycopy(cpu.bp, 0, oldDest, 0, cpu.bp.length);
        
        // Decrement the bp register
        temp = Util.subtractWords(cpu.bp, decWord, 0);
        System.arraycopy(temp, 0, cpu.bp, 0, temp.length);
        
        // Test AF
        cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_SUB(oldDest[CPU.REGISTER_GENERAL_LOW], cpu.bp[CPU.REGISTER_GENERAL_LOW]);  
        // Test OF
        cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB(oldDest, decWord, cpu.bp, 0);  
        // Test ZF
        cpu.flags[CPU.REGISTER_FLAGS_ZF] = cpu.bp[CPU.REGISTER_GENERAL_HIGH] == 0x00 && cpu.bp[CPU.REGISTER_GENERAL_LOW] == 0x00 ? true : false;
        // Test SF (set when MSB of AH is 1. In Java can check signed byte)
        cpu.flags[CPU.REGISTER_FLAGS_SF] = cpu.bp[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
        // Set PF, only applies to LSB
        cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(cpu.bp[CPU.REGISTER_GENERAL_LOW]);
    }
}
