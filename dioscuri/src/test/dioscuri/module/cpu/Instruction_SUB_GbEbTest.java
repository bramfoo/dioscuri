/*
 * $Revision: 1.3 $ $Date: 2007-06-29 13:08:03 $ $Author: bram $
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
 * [SOURCEFORGE_HOMEPAGE]
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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import dioscuri.DummyEmulator;
import dioscuri.*;
import dioscuri.module.memory.*;

import org.junit.*;

import static org.junit.Assert.*;

public class Instruction_SUB_GbEbTest {
    Emulator emu = null;
    CPU cpu = null;
    Memory mem = null;

    int startAddress = 80448;
    String testASMfilename = "test/asm/SUB_GbEb.bin";


    @Before
    protected void setUp() throws Exception {
        emu = new DummyEmulator();
        cpu = new CPU(emu);
        mem = new DummyMemory();
        cpu.setConnection(mem);
        cpu.setDebugMode(true);

        BufferedInputStream bis = new BufferedInputStream(new DataInputStream(new FileInputStream(new File(testASMfilename))));
        byte[] byteArray = new byte[bis.available()];
        bis.read(byteArray, 0, byteArray.length);
        bis.close();

        mem.setBytes(startAddress, byteArray);
    }


    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_SUB_GbEb.execute()'
    */
    @Test
    public void testExecute() {
        String AX_ERROR = "AX contains wrong value";
        String OF_ERROR = "OF incorrect";
        String SF_ERROR = "SF incorrect";
        String ZF_ERROR = "ZF incorrect";
        String AF_ERROR = "AF incorrect";
        String PF_ERROR = "PF incorrect";
        String CF_ERROR = "CF incorrect";

        assertFalse(OF_ERROR, cpu.getFlagValue('O'));
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertFalse(AF_ERROR, cpu.getFlagValue('A'));
        assertFalse(PF_ERROR, cpu.getFlagValue('P'));
        assertFalse(CF_ERROR, cpu.getFlagValue('C'));

        // Load registers with pre-arranged values
        cpu.startDebug(); // MOV AX, 0x7B05
        cpu.startDebug(); // MOV [0000], AX
        cpu.startDebug(); // MOV AX, 0x0285
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x02);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x85);

        // SUB mem,reg
        cpu.startDebug(); // SUB AL, [BX+SI] ; 85 - 05, test SF, PF flags
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x02);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x80);
        assertTrue(SF_ERROR, cpu.getFlagValue('S'));
        assertFalse(PF_ERROR, cpu.getFlagValue('P'));

        cpu.startDebug(); // SUB AL, [BX+SI] ; 80 - 05, test OF, SF, AF, PF flags
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x02);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x7B);
        assertTrue(OF_ERROR, cpu.getFlagValue('O'));
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));

        // SUB mem+8b,reg
        cpu.startDebug(); // SUB AL, [BX+SI+01]      ; 7B - 7B, test ZF
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x02);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x00);
        assertFalse(OF_ERROR, cpu.getFlagValue('O'));
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));
        assertFalse(AF_ERROR, cpu.getFlagValue('A'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));

        // SUB mem+16b,reg
        cpu.startDebug(); // INC BP                  ; Set BP to 1
        cpu.startDebug(); // SUB AL, [BP+0x0100]     ; 00 - 05, test CF, overflow
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x02);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0xFB);
        assertTrue(SF_ERROR, cpu.getFlagValue('S'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));
        assertFalse(PF_ERROR, cpu.getFlagValue('P'));
        assertTrue(CF_ERROR, cpu.getFlagValue('C'));

        // SUB reg, reg
        cpu.startDebug(); // SUB AL, AH      ; FB - 02, test CF, PF
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x02);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0xF9);
        assertFalse(AF_ERROR, cpu.getFlagValue('A'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        assertFalse(CF_ERROR, cpu.getFlagValue('C'));

        cpu.startDebug(); // HLT             ; Stop execution

    }

}
