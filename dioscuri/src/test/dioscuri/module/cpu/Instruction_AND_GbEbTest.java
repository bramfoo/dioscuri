package dioscuri.module.cpu;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;


import dioscuri.*;
import dioscuri.module.memory.*;

import org.junit.*;

import static org.junit.Assert.*;

public class Instruction_AND_GbEbTest {
    Emulator emu = null;
    CPU cpu = null;
    Memory mem = null;

    int startAddress = 80448;
    String testASMfilename = "test/asm/AND_GbEb.bin";

    @Before
    protected void setUp() throws Exception {
        emu = new Emulator(new DummyGUI());
        cpu = new CPU(emu);
        mem = new Memory(emu);
        cpu.setConnection(mem);
        cpu.setDebugMode(true);

        BufferedInputStream bis = new BufferedInputStream(new DataInputStream(new FileInputStream(new File(testASMfilename))));
        byte[] byteArray = new byte[bis.available()];
        bis.read(byteArray, 0, byteArray.length);
        bis.close();

        mem.setBytes(startAddress, byteArray);
    }


    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_AND_GbEb.execute()'
    */
    @Test
    public void testExecute() {
        String AX_ERROR = "AX contains wrong value";
        String SF_ERROR = "SF incorrect";
        String ZF_ERROR = "ZF incorrect";
        String PF_ERROR = "PF incorrect";

        // Load registers, memory with prearranged values (4 instructions)
        cpu.startDebug();    // MOV AX, 0xA980
        cpu.startDebug();    // MOV [0000], AX
        cpu.startDebug();    // MOV AX, 0x55AA
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x55);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0xAA);

        // AND mem, reg
        cpu.startDebug();    // AND AL, [BX+SI]
        assertTrue(SF_ERROR, cpu.getFlagValue('S'));
        assertFalse(PF_ERROR, cpu.getFlagValue('P'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x80);

        // AND reg,mem+8b
        cpu.startDebug();    // AND AH, [BX+DI+01]
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertFalse(PF_ERROR, cpu.getFlagValue('P'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x01);


        // AND reg,mem+16b
        cpu.startDebug();    // INC BP
        cpu.startDebug();    // AND AH, [BP+0x0100]
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x00);

        // AND reg, reg
        cpu.startDebug();    // AND AL, AH
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x00);
        cpu.startDebug();    // HLT

    }
}
