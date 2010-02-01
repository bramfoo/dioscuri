package dioscuri.module.cpu;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;


import dioscuri.*;
import dioscuri.module.memory.*;

import org.junit.*;

import static org.junit.Assert.*;

public class Instruction_MOV_GvEvTest {
    Emulator emu = null;
    CPU cpu = null;
    Memory mem = null;

    int startAddress = 80448;
    String testASMfilename = "test/asm/MOV_GvEv.bin";

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
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_MOV_GvEv.execute()'
    */
    @Test
    public void testExecute() {
        String AX_ERROR = "AX contains wrong value";
        String BX_ERROR = "BX contains wrong value";
        String CX_ERROR = "CX contains wrong value";
        String DX_ERROR = "DX contains wrong value";
        String BP_ERROR = "BP contains wrong value";
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
        cpu.startDebug(); // MOV AX, 0xAABB
        cpu.startDebug(); // MOV [0000], AX
        cpu.startDebug(); // MOV [0002], AX
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0xAA);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0xBB);

        // MOV mem,reg
        cpu.startDebug(); // MOV CX, [BX+SI] ; MOV reg and mem, store in memory
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[0], (byte) 0xAA);
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[1], (byte) 0xBB);

        // MOV mem+8b,reg
        cpu.startDebug(); // MOV DX, [BX+DI+02]      ; MOV reg and mem+8b, store in memory
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[0], (byte) 0xAA);
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[1], (byte) 0xBB);

        // MOV mem+16b,reg
        cpu.startDebug(); // INC BP                  ; Set BP to 1
        cpu.startDebug(); // MOV BX, [BP+0x0100]     ; MOV reg and mem+16b, store in memory
        assertEquals(BX_ERROR, cpu.getRegisterValue("BX")[0], (byte) 0xAA);
        assertEquals(BX_ERROR, cpu.getRegisterValue("BX")[1], (byte) 0xBB);

        // MOV reg, reg
        cpu.startDebug(); // MOV BP, BX      ; MOV 2 registers
        assertEquals(BP_ERROR, cpu.getRegisterValue("BP")[0], (byte) 0xAA);
        assertEquals(BP_ERROR, cpu.getRegisterValue("BP")[1], (byte) 0xBB);

        assertFalse(OF_ERROR, cpu.getFlagValue('O'));
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertFalse(AF_ERROR, cpu.getFlagValue('A'));
        assertFalse(PF_ERROR, cpu.getFlagValue('P'));
        assertFalse(CF_ERROR, cpu.getFlagValue('C'));

        cpu.startDebug(); // HLT             ; Stop execution        
    }

}
