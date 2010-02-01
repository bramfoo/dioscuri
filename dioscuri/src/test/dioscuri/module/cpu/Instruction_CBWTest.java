package dioscuri.module.cpu;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;


import dioscuri.*;
import dioscuri.module.memory.*;

import org.junit.*;

import static org.junit.Assert.*;

public class Instruction_CBWTest {

    Emulator emu = null;
    CPU cpu = null;
    Memory mem = null;

    int startAddress = 80448;
    String testASMfilename = "test/asm/CBW.bin";


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
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_CBW.execute()'
    */
    @Test
    public void testExecute() {
        String AX_ERROR = "AX contains wrong value";

        // Test byte to word extension
        cpu.startDebug();    // MOV AL, 0x80
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x80);
        cpu.startDebug();    // CBW
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0xFF);

        cpu.startDebug();    // MOV AL, 0x7F
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x7F);
        cpu.startDebug();    // CBW
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x00);

        cpu.startDebug();    // HLT

    }


}
