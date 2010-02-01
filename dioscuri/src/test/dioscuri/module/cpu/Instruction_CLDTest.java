package dioscuri.module.cpu;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;


import dioscuri.*;
import dioscuri.module.memory.*;

import org.junit.*;

import static org.junit.Assert.*;

public class Instruction_CLDTest {

    Emulator emu = null;
    CPU cpu = null;
    Memory mem = null;

    int startAddress = 80448;
    String testASMfilename = "test/asm/CLD.bin";


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
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_CLD.execute()'
    */
    @Test
    public void testExecute() {
        String DF_ERROR = "DF incorrect";

        // Test clearing of direction flag
        assertFalse(DF_ERROR, cpu.getFlagValue('D'));
        cpu.startDebug();   // CLD
        assertFalse(DF_ERROR, cpu.getFlagValue('D'));
        cpu.startDebug();   // STD
        assertTrue(DF_ERROR, cpu.getFlagValue('D'));
        cpu.startDebug();   // CLD
        assertFalse(DF_ERROR, cpu.getFlagValue('D'));
        cpu.startDebug();   // HLT

    }


}
