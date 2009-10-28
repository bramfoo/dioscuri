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
	 * Intel opcode AE<BR>
	 * Compare AL with byte at ES:(E)DI and set status flags.<BR>
     * ES:(E)DI is incremented/decremented depending on DF flag.<BR>
	 * Flags modified: OF, SF, ZF, AF, PF, and CF.
	 */
public class Instruction_SCAS_ALYb implements Instruction {

	// Attributes
	private CPU cpu;

    boolean operandWordSize;
    
    byte source;
    byte result;
	byte[] transition;
    byte[] temp;

    
	// Constructors
	/**
	 * Class constructor
	 */
	public Instruction_SCAS_ALYb()
    {
        operandWordSize = false;
        
        source = 0;
        result = 0;
        
        // Set transition that holds the amount si and di should be altered (byte = 1)
        transition = new byte[] { 0x00, 0x01 };
        temp = new byte[2];
    }
	
	/**
	 * Class constructor specifying processor reference
	 * 
	 * @param processor	Reference to CPU class
	 */
	public Instruction_SCAS_ALYb(CPU processor)
	{
		this();
		
		// Create reference to cpu class
		cpu = processor;
	}

	// Methods
	
	/**
     * Compare AL with byte at ES:(E)DI and set status flags.<BR>
     * ES:(E)DI is incremented/decremented depending on DF flag.<BR>
	 * Flags modified: OF, SF, ZF, AF, PF, and CF.
	 */
	public void execute()
	{
        // Get byte from ES:DI; no segment override is allowed.
        source = cpu.getByteFromExtra(cpu.di);
        
        // Subtract source byte from register AL
        result = (byte) (cpu.ax[CPU.REGISTER_GENERAL_LOW] - source);
        
        // Set FLAGS
        // Test AF
        cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_SUB(cpu.ax[CPU.REGISTER_GENERAL_LOW], result);
        // Test CF
        cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_SUB(cpu.ax[CPU.REGISTER_GENERAL_LOW], source, 0);
        // Test OF
        cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_SUB(cpu.ax[CPU.REGISTER_GENERAL_LOW], source, result, 0);
        // Test ZF, is tested againt tempResult
        cpu.flags[CPU.REGISTER_FLAGS_ZF] = result == 0 ? true : false;
        // Test SF, only applies to lower byte (set when MSB is 1, occurs when
        // tempResult >= 0x80)
        cpu.flags[CPU.REGISTER_FLAGS_SF] = result < 0 ? true : false;
        // Set PF, only applies to lower byte
        cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(result);

        // Increment or decrement DI depending on DF flag
        if (cpu.flags[CPU.REGISTER_FLAGS_DF] == true)
        {
            // Decrement register
            temp = Util.subtractWords(cpu.di, transition, 0);
            System.arraycopy(temp, 0, cpu.di, 0, temp.length);
        }
        else
        {
            // Increment register
            temp = Util.addWords(cpu.di, transition, 0);
            System.arraycopy(temp, 0, cpu.di, 0, temp.length);
        }
	}
}
