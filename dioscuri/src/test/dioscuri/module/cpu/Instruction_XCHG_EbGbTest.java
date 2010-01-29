/*
 * $Revision: 1.3 $ $Date: 2007-06-29 13:08:04 $ $Author: bram $
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
import dioscuri.Emulator;
import dioscuri.module.memory.*;
import org.junit.*;

import static org.junit.Assert.*;

public class Instruction_XCHG_EbGbTest {
    Emulator emu = null;
    CPU cpu = null;
    Memory mem = null;

    int startAddress = 80448;
    String testASMfilename = "test/asm/XCHG_EbGb.bin";

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
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_XCHG_EbGb.execute()'
    */
    @Test
    public void testExecute() {
        String AX_ERROR = "AX contains wrong value";
        String CX_ERROR = "CX contains wrong value";
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

        // Load memory with pre-arranged values
        cpu.startDebug(); // MOV AX, 0xAABB
        cpu.startDebug(); // MOV [0000], AX
        cpu.startDebug(); // MOV AX, 0xCCDD
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0xCC);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0xDD);

        // XCHG mem,reg
        cpu.startDebug(); // XCHG [BX+SI], AL; XCHG reg and mem, store in memory
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0xCC);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0xBB);
        cpu.startDebug(); // MOV AL, [0000]  ; Retrieve result from memory (result = bb)
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0xCC);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0xDD);

        // XCHG mem+8b,reg
        cpu.startDebug(); // XCHG [BX+DI+01], AH     ; XCHG reg and mem+8b, store in memory
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0xAA);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0xDD);
        cpu.startDebug(); // MOV AL, [0001]          ; Retrieve result from memory (result = aa)
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0xAA);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0xCC);

        // XCHG mem+16b,reg
        cpu.startDebug(); // INC BP                  ; Set BP to 1
        cpu.startDebug(); // XCHG [BP+0x0100], AL    ; XCHG reg and mem+16b, store in memory
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0xAA);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0xBB);
        cpu.startDebug(); // MOV AL, [0x0101]        ; Retrieve result from memory (result = aa) [NOTE: ACCESSING BYTE 2 OF OWN CODE]
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0xAA);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0xCC);

        // XCHG reg, reg
        cpu.startDebug(); // XCHG AL, CH     ; XCHG 2 registers (result = 00)
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0xAA);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x00);
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[0], (byte) 0xCC);
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[1], (byte) 0x00);

        cpu.startDebug(); // HLT             ; Stop execution
    }

}
