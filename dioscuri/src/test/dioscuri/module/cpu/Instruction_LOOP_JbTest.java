package dioscuri.module.cpu;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;


import dioscuri.*;
import dioscuri.module.memory.*;

import org.junit.*;

import static org.junit.Assert.*;

public class Instruction_LOOP_JbTest {
    Emulator emu = null;
    CPU cpu = null;
    Memory mem = null;

    int startAddress = 80448;
    String testASMfilename = "test/asm/LOOP_Jb.bin";


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
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_LOOP_Jb.execute()'
    */
    @Test
    public void testExecute() {
        String IP_ERROR = "IP contains wrong value";
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
        assertEquals(IP_ERROR, cpu.getRegisterValue("IP")[0], (byte) 0x01);
        assertEquals(IP_ERROR, cpu.getRegisterValue("IP")[1], (byte) 0x00);

        // Load loop counter CX
        cpu.startDebug(); // MOV CL, 0x03
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[1], (byte) 0x03);

        // Start loop
        cpu.startDebug(); // INC ax
        cpu.startDebug(); // LOOP 
        assertEquals(IP_ERROR, cpu.getRegisterValue("IP")[1], (byte) 0x02);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x01);
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[1], (byte) 0x02);

        cpu.startDebug(); // INC ax
        cpu.startDebug(); // LOOP 
        assertEquals(IP_ERROR, cpu.getRegisterValue("IP")[1], (byte) 0x02);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x02);
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[1], (byte) 0x01);

        cpu.startDebug(); // INC ax
        cpu.startDebug(); // LOOP 
        assertEquals(IP_ERROR, cpu.getRegisterValue("IP")[1], (byte) 0x05);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x03);
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[1], (byte) 0x00);

        cpu.startDebug(); // HLT
    }

}
