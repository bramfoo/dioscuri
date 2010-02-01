package dioscuri.module.cpu;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import dioscuri.Emulator;
import dioscuri.DummyGUI;

import org.junit.*;

import static org.junit.Assert.*;


import dioscuri.module.memory.*;

public class Instruction_AND_AXIvTest {

    Emulator emu = null;
    CPU cpu = null;
    Memory mem = null;

    int startAddress = 80448;
    String testASMfilename = "test/asm/AND_AXIv.bin";

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
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_AND_AXIv.execute()'
    */
    @Test
    public void testExecute() {
        String AX_ERROR = "AX contains wrong value";
        String SF_ERROR = "SF incorrect";
        String ZF_ERROR = "ZF incorrect";
        String PF_ERROR = "PF incorrect";

        // Execute DEC_AX
        cpu.startDebug();
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0xFF);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0xFF);
        // Execute AND AX, 5555
        cpu.startDebug();
        // TODO: Add Junit-addons for array comparison:
        //ArrayAssert.assertEquals(AX_ERROR, cpu.getRegisterValue("AX"), new byte[]{(byte)0x55, (byte)0x55});
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x55);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x55);
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        // Execute AND AL, AAAA
        cpu.startDebug();
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x00);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x00);
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        // Execute DEC_AX
        cpu.startDebug();
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0xFF);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0xFF);
        // Execute AND AL, 0101
        cpu.startDebug();
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x01);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x01);
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertFalse(PF_ERROR, cpu.getFlagValue('P'));
    }
}
