package dioscuri.module.cpu;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import dioscuri.DummyEmulator;
import dioscuri.Emulator;
import dioscuri.module.memory.DummyMemory;
import dioscuri.module.memory.*;

import org.junit.*;

import static org.junit.Assert.*;

public class Instruction_AND_ALIbTest {

    Emulator emu = null;
    CPU cpu = null;
    Memory mem = null;

    int startAddress = 80448;
    String testASMfilename = "test/asm/AND_ALIb.bin";

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
     * Test method for 'com.tessella.emulator.module.cpu.Instruction_AND_ALIb.execute()'
     */
    @Test
    public void testExecute() {
        String AX_ERROR = "AX contains wrong value";
        String SF_ERROR = "SF incorrect";
        String ZF_ERROR = "ZF incorrect";
        String PF_ERROR = "PF incorrect";

        // Execute DEC_AX
        cpu.startDebug();
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0xFF);
        // Execute AND AL, 55
        cpu.startDebug();
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x55);
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        // Execute AND AL, AA
        cpu.startDebug();
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x00);
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        // Execute DEC_AX
        cpu.startDebug();
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0xFF);
        // Execute AND AL, 00
        cpu.startDebug();
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x00);
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
    }
}
