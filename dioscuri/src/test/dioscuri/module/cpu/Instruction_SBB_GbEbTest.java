package dioscuri.module.cpu;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;


import dioscuri.*;
import dioscuri.module.memory.*;

import org.junit.*;

import static org.junit.Assert.*;

public class Instruction_SBB_GbEbTest {
    Emulator emu = null;
    CPU cpu = null;
    Memory mem = null;

    int startAddress = 80448;
    String testASMfilename = "test/asm/SBB_GbEb.bin";


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
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_SBB_GbEb.execute()'
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
        cpu.startDebug(); // MOV AX, 0x7FFF
        cpu.startDebug(); // MOV [0000], AX
        cpu.startDebug(); // MOV AX, 0x017F
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x01);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x7F);

        // SBB mem,reg
        cpu.startDebug(); // CMC             ; Set carry flag
        assertTrue(CF_ERROR, cpu.getFlagValue('C'));
        cpu.startDebug(); // SBB AL, [BX+SI] ; 7F - (FF + CF), test AF flag
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x01);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x7F);
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));
        assertFalse(PF_ERROR, cpu.getFlagValue('P'));
        assertTrue(CF_ERROR, cpu.getFlagValue('C'));

        // SBB mem+8b,reg
        cpu.startDebug(); // INC AX                  ; Set AX
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x01);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x80);
        cpu.startDebug(); // SBB AL, [BX+DI+04]      ; 80 - (00 + CF), test OF, SF, AF, PF flags
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x01);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x7F);
        assertTrue(OF_ERROR, cpu.getFlagValue('O'));
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));
        assertFalse(PF_ERROR, cpu.getFlagValue('P'));
        assertFalse(CF_ERROR, cpu.getFlagValue('C'));

        // SBB mem+16b,reg
        cpu.startDebug(); // INC BP                  ; Set BP to 1
        cpu.startDebug(); // INC BP                  ; Set BP to 2
        cpu.startDebug(); // SBB AL, [BP+0x0100]     ; 7F - 7F, test ZF
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x01);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x00);
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));

        // SBB reg, reg
        cpu.startDebug(); // SBB AL, AH      ; 00 - 01, test CF, SBB, overflow
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x01);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0xFF);
        assertTrue(SF_ERROR, cpu.getFlagValue('S'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        assertTrue(CF_ERROR, cpu.getFlagValue('C'));

        cpu.startDebug(); // SBB AL, BL      ; FF - (00 + CF), test CF, PF
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x01);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0xFE);
        assertFalse(AF_ERROR, cpu.getFlagValue('A'));
        assertFalse(PF_ERROR, cpu.getFlagValue('P'));
        assertFalse(CF_ERROR, cpu.getFlagValue('C'));

        cpu.startDebug(); // HLT             ; Stop execution

    }

}
