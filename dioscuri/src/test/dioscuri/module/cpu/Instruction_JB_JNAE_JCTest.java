

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

public class Instruction_JB_JNAE_JCTest {

    Emulator emu = null;
    CPU cpu = null;
    Memory mem = null;

    int startAddress = 80448;
    String testASMfilename = "test/asm/JB_JNAE_JC.bin";


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
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_JB_JNAE_JC.execute()'
    */
    @Test
    public void testExecute() {
        String IP_ERROR = "IP contains wrong value";
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

        // Test JB/JNAE/JC instruction
        cpu.startDebug(); // JB
        assertEquals(IP_ERROR, cpu.getRegisterValue("IP")[0], (byte) 0x01);
        assertEquals(IP_ERROR, cpu.getRegisterValue("IP")[1], (byte) 0x02);
        cpu.startDebug(); // JNAE
        assertEquals(IP_ERROR, cpu.getRegisterValue("IP")[0], (byte) 0x01);
        assertEquals(IP_ERROR, cpu.getRegisterValue("IP")[1], (byte) 0x04);
        cpu.startDebug(); // JC
        assertEquals(IP_ERROR, cpu.getRegisterValue("IP")[0], (byte) 0x01);
        assertEquals(IP_ERROR, cpu.getRegisterValue("IP")[1], (byte) 0x06);

        cpu.startDebug(); // CMC
        assertTrue(CF_ERROR, cpu.getFlagValue('C'));
        cpu.startDebug(); // JB -2          ; Jump to IP 00FE (start at 100), to test full IP range
        assertEquals(IP_ERROR, cpu.getRegisterValue("IP")[0], (byte) 0x00);
        assertEquals(IP_ERROR, cpu.getRegisterValue("IP")[1], (byte) 0xFE);

        // FIXME: Fix ADD instruction
//        cpu.startDebug(); // ADD instruction
//        cpu.startDebug(); // JB
//        assertEquals(IP_ERROR, cpu.getRegisterValue("IP")[0], (byte)0x01);
//        assertEquals(IP_ERROR, cpu.getRegisterValue("IP")[1], (byte)0x09);
//        cpu.startDebug(); // CLC
//        assertFalse(CF_ERROR, cpu.getFlagValue('C'));
//        cpu.startDebug(); // JMP
//        assertEquals(IP_ERROR, cpu.getRegisterValue("IP")[0], (byte)0x01);
//        assertEquals(IP_ERROR, cpu.getRegisterValue("IP")[1], (byte)0x00);
    }

}