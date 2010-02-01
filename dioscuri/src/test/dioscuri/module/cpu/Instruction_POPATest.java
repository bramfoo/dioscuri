package dioscuri.module.cpu;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;


import dioscuri.*;
import dioscuri.module.memory.*;

import org.junit.*;

import static org.junit.Assert.*;

public class Instruction_POPATest {
    Emulator emu = null;
    CPU cpu = null;
    Memory mem = null;

    int startAddress = 80448;
    String testASMfilename = "test/asm/POPA.bin";


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
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_POPA.execute()'
    */
    @Test
    public void testExecute() {
        String AX_ERROR = "AX contains wrong value";
        String BX_ERROR = "BX contains wrong value";
        String CX_ERROR = "CX contains wrong value";
        String DX_ERROR = "DX contains wrong value";
        String SP_ERROR = "SP contains wrong value";
        String BP_ERROR = "BP contains wrong value";
        String SI_ERROR = "SI contains wrong value";
        String DI_ERROR = "DI contains wrong value";
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
        cpu.startDebug(); // MOV ax, 0x1111  ; Move value into ax
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x11);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x11);
        cpu.startDebug(); // MOV cx, 0xaaaa  ; Move value into cx
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[0], (byte) 0xAA);
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[1], (byte) 0xAA);
        cpu.startDebug(); // MOV dx, 0xbbbb  ; Move value into dx
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[0], (byte) 0xBB);
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[1], (byte) 0xBB);
        cpu.startDebug(); // MOV bx, 0xcccc  ; Move value into bx
        assertEquals(BX_ERROR, cpu.getRegisterValue("BX")[0], (byte) 0xCC);
        assertEquals(BX_ERROR, cpu.getRegisterValue("BX")[1], (byte) 0xCC);
        cpu.startDebug(); // MOV bp, 0xdddd  ; Move value into bp
        assertEquals(BP_ERROR, cpu.getRegisterValue("BP")[0], (byte) 0xDD);
        assertEquals(BP_ERROR, cpu.getRegisterValue("BP")[1], (byte) 0xDD);
        cpu.startDebug(); // MOV si, 0xeeee  ; Move value into si
        assertEquals(SI_ERROR, cpu.getRegisterValue("SI")[0], (byte) 0xEE);
        assertEquals(SI_ERROR, cpu.getRegisterValue("SI")[1], (byte) 0xEE);
        cpu.startDebug(); // MOV di, 0xffff  ; Move value into di
        assertEquals(DI_ERROR, cpu.getRegisterValue("DI")[0], (byte) 0xFF);
        assertEquals(DI_ERROR, cpu.getRegisterValue("DI")[1], (byte) 0xFF);

        assertEquals(SP_ERROR, cpu.getRegisterValue("SP")[0], (byte) 0xFF);
        assertEquals(SP_ERROR, cpu.getRegisterValue("SP")[1], (byte) 0xEE);

        // Test PUSHA operation
        cpu.startDebug(); // PUSHA           ; Push all registers onto stack

        // Clear all registers
        cpu.startDebug(); // MOV ax, 0x0000  ; Clear register
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x00);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x00);
        cpu.startDebug(); // MOV cx, 0x0000  ; Clear register
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[0], (byte) 0x00);
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[1], (byte) 0x00);
        cpu.startDebug(); // MOV dx, 0x0000  ; Clear register
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[0], (byte) 0x00);
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[1], (byte) 0x00);
        cpu.startDebug(); // MOV bx, 0x0000  ; Clear register
        assertEquals(BX_ERROR, cpu.getRegisterValue("BX")[0], (byte) 0x00);
        assertEquals(BX_ERROR, cpu.getRegisterValue("BX")[1], (byte) 0x00);
        cpu.startDebug(); // MOV bp, 0x0000  ; Clear register
        assertEquals(BP_ERROR, cpu.getRegisterValue("BP")[0], (byte) 0x00);
        assertEquals(BP_ERROR, cpu.getRegisterValue("BP")[1], (byte) 0x00);
        cpu.startDebug(); // MOV si, 0x0000  ; Clear register
        assertEquals(SI_ERROR, cpu.getRegisterValue("SI")[0], (byte) 0x00);
        assertEquals(SI_ERROR, cpu.getRegisterValue("SI")[1], (byte) 0x00);
        cpu.startDebug(); // MOV di, 0x0000  ; Clear register
        assertEquals(DI_ERROR, cpu.getRegisterValue("DI")[0], (byte) 0x00);
        assertEquals(DI_ERROR, cpu.getRegisterValue("DI")[1], (byte) 0x00);

        assertEquals(SP_ERROR, cpu.getRegisterValue("SP")[0], (byte) 0xFF);
        assertEquals(SP_ERROR, cpu.getRegisterValue("SP")[1], (byte) 0xDE);

        // Test POPA operation
        cpu.startDebug(); // POPA            ; Pop all registers from stack

        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x11);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x11);
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[0], (byte) 0xAA);
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[1], (byte) 0xAA);
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[0], (byte) 0xBB);
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[1], (byte) 0xBB);
        assertEquals(BX_ERROR, cpu.getRegisterValue("BX")[0], (byte) 0xCC);
        assertEquals(BX_ERROR, cpu.getRegisterValue("BX")[1], (byte) 0xCC);
        assertEquals(BP_ERROR, cpu.getRegisterValue("BP")[0], (byte) 0xDD);
        assertEquals(BP_ERROR, cpu.getRegisterValue("BP")[1], (byte) 0xDD);
        assertEquals(SI_ERROR, cpu.getRegisterValue("SI")[0], (byte) 0xEE);
        assertEquals(SI_ERROR, cpu.getRegisterValue("SI")[1], (byte) 0xEE);
        assertEquals(DI_ERROR, cpu.getRegisterValue("DI")[0], (byte) 0xFF);
        assertEquals(DI_ERROR, cpu.getRegisterValue("DI")[1], (byte) 0xFF);

        assertEquals(SP_ERROR, cpu.getRegisterValue("SP")[0], (byte) 0xFF);
        assertEquals(SP_ERROR, cpu.getRegisterValue("SP")[1], (byte) 0xEE);

        cpu.startDebug(); // HLT             ; Stop execution

    }
}
