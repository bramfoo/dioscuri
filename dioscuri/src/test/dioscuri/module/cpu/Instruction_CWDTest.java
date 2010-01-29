

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

public class Instruction_CWDTest {

    Emulator emu = null;
    CPU cpu = null;
    Memory mem = null;

    int startAddress = 80448;
    String testASMfilename = "test/asm/CWD.bin";


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
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_CBW.execute()'
    */
    @Test
    public void testExecute() {
        String AX_ERROR = "AX contains wrong value";
        String DX_ERROR = "DX contains wrong value";

        // Test byte to word extension
        cpu.startDebug();   // MOV AH, 0x80
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x80);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x00);
        cpu.startDebug();   // CWD
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[0], (byte) 0xFF);
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[1], (byte) 0xFF);

        cpu.startDebug();   // MOV AH, 0x7F
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x7F);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x00);
        cpu.startDebug();   // CWD
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[0], (byte) 0x00);
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[1], (byte) 0x00);

        cpu.startDebug();   // HLT

    }

}
