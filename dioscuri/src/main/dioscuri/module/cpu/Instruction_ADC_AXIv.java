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
     * Intel opcode 15<BR>
     * Add (immediate word + CF) to AX.<BR>
     * Flags modified: OF, SF, ZF, AF, PF, CF
     */
public class Instruction_ADC_AXIv implements Instruction {

    // Attributes
    private CPU cpu;
    byte[] immediateWord;
    byte[] oldDest;
    int iCarryFlag;
    
    byte[] temp;
    
    // Constructors
    /**
     * Class constructor
     */
    public Instruction_ADC_AXIv()
    {
        immediateWord = new byte[2];
        oldDest = new byte[2];
        iCarryFlag = 0;
        
        temp = new byte[2];
    }
    
    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_ADC_AXIv(CPU processor)
    {
        this();
        
        // Create reference to cpu class
        cpu = processor;
    }

    
    // Methods
    
    /**
     * Add (immediate word + CF) to AX
     */
    public void execute()
    {
        // Determine value of carry flag before reset
        iCarryFlag = cpu.flags[CPU.REGISTER_FLAGS_CF] ? 1 : 0; 
        
        // Get word from code
        immediateWord = cpu.getWordFromCode();
        
        // Copy old value of AX
        System.arraycopy(cpu.ax, 0, oldDest, 0, cpu.ax.length);
        
        // Add (immediate word + CF) to register AX
        temp = Util.addWords(cpu.ax, immediateWord, iCarryFlag);
        System.arraycopy(temp, 0, cpu.ax, 0, temp.length);

        // Test AF
        cpu.flags[CPU.REGISTER_FLAGS_AF] = Util.test_AF_ADD(oldDest[CPU.REGISTER_GENERAL_LOW], cpu.ax[CPU.REGISTER_GENERAL_LOW]);  
        // Test CF
        cpu.flags[CPU.REGISTER_FLAGS_CF] = Util.test_CF_ADD(oldDest, immediateWord, iCarryFlag);
        // Test OF
        cpu.flags[CPU.REGISTER_FLAGS_OF] = Util.test_OF_ADD(oldDest, immediateWord, cpu.ax, iCarryFlag);
        // Test ZF
        cpu.flags[CPU.REGISTER_FLAGS_ZF] = cpu.ax[CPU.REGISTER_GENERAL_HIGH] == 0x00 && cpu.ax[CPU.REGISTER_GENERAL_LOW] == 0x00 ? true : false;
        // Test SF (set when MSB of AL is 1. In Java can check signed byte)
        cpu.flags[CPU.REGISTER_FLAGS_SF] = cpu.ax[CPU.REGISTER_GENERAL_HIGH] < 0 ? true : false;
        // Set PF, only applies to AL
        cpu.flags[CPU.REGISTER_FLAGS_PF] = Util.checkParityOfByte(cpu.ax[CPU.REGISTER_GENERAL_LOW]);

    }
}
