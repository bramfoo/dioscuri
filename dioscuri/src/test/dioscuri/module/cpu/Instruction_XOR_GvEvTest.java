

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

public class Instruction_XOR_GvEvTest {
    Emulator emu = null;
    CPU cpu = null;
    Memory mem = null;

    int startAddress = 80448;
    String testASMfilename = "test/asm/XOR_GvEv.bin";


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
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_XOR_GvEv.execute()'
    */
    @Test
    public void testExecute() {
        String AX_ERROR = "AX contains wrong value";
        String DX_ERROR = "DX contains wrong value";
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
        cpu.startDebug(); // MOV AX, 0xFFFF
        cpu.startDebug(); // MOV [0000], AX
        cpu.startDebug(); // MOV [0002], AX
        cpu.startDebug(); // MOV AX, 0xAA55  ; Move AA55 into AX
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0xAA);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x55);
        cpu.startDebug(); // MOV DX, 0xAA55  ; Move AA55 into DX
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[0], (byte) 0xAA);
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[1], (byte) 0x55);

        // XOR reg,mem
        cpu.startDebug(); // XOR AX, [BX+SI] ; XOR reg and mem, store in AX (AA55)
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x55);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0xAA);
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));

        // XOR reg,mem+8b
        cpu.startDebug(); // XOR DX, [BX+DI+02]      ; XOR reg and mem+8b, store in DX (AA55)
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[0], (byte) 0x55);
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[1], (byte) 0xAA);
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));

        // XOR reg,mem+16b
        cpu.startDebug(); // INC BP                  ; Set BP to 1
        cpu.startDebug(); // XOR DX, [BP+0x0100]     ; XOR reg and mem+16b, store in DX (55aa)
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[0], (byte) 0xAA);
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[1], (byte) 0x55);
        assertTrue(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));

        // XOR reg, reg
        cpu.startDebug(); // XOR AX, AX      ; XOR 2 registers (00), check flags                                                 
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x00);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x00);
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));

        cpu.startDebug(); // HLT             ; Stop execution
    }
}
