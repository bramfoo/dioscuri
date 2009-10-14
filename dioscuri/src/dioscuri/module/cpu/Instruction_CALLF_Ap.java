/* $Revision: 159 $ $Date: 2009-08-17 12:52:56 +0000 (ma, 17 aug 2009) $ $Author: blohman $
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

package dioscuri.module.cpu;

/**
 * Intel opcode 9A<BR>
 * Call to procedure in another code segment (intersegment call) indicated by immediate signed words.<BR>
 * Displacement is relative to next instruction.<BR>
 * Flags modified: none
 */
public class Instruction_CALLF_Ap implements Instruction
{

    // Attributes
    private CPU cpu;

    byte[] newCS;
    byte[] newIP;

    // Constructors
    /**
     * Class constructor
     */
    public Instruction_CALLF_Ap()
    {
    }

    /**
     * Class constructor specifying processor reference
     * 
     * @param processor Reference to CPU class
     */
    public Instruction_CALLF_Ap(CPU processor)
    {
        // Create reference to cpu class
        cpu = processor;

        newCS = new byte[2];
        newIP = new byte[2];
    }

    // Methods

    /**
     * Execute call to procedure indicated by immediate signed words
     */
    public void execute()
    {
        // Call far absolute address given in operand (IP:CS=r16:16 or r16:32)

        // Get new IP from code
        newIP = cpu.getWordFromCode();

        // Get new CS from code
        newCS = cpu.getWordFromCode();

        // Push current code segment and instruction pointer onto stack
        cpu.setWordToStack(cpu.cs);
        cpu.setWordToStack(cpu.ip);

        // Assign new CS and IP to registers
        cpu.cs[CPU.REGISTER_SEGMENT_LOW] = newCS[CPU.REGISTER_LOW];
        cpu.cs[CPU.REGISTER_SEGMENT_HIGH] = newCS[CPU.REGISTER_HIGH];
        cpu.ip[CPU.REGISTER_LOW] = newIP[CPU.REGISTER_LOW];
        cpu.ip[CPU.REGISTER_HIGH] = newIP[CPU.REGISTER_HIGH];
    }

}
