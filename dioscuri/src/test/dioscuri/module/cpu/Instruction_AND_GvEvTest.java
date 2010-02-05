package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class Instruction_AND_GvEvTest extends AbstractInstructionTest {

    public Instruction_AND_GvEvTest() throws Exception {
        super(80448, "AND_GvEv.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_AND_GbEb.execute()'
    */
    @Test
    public void testExecute() {
        String AX_ERROR = "AX contains wrong value";
        String CX_ERROR = "CX contains wrong value";
        String DX_ERROR = "DX contains wrong value";
        String SF_ERROR = "SF incorrect";
        String ZF_ERROR = "ZF incorrect";
        String PF_ERROR = "PF incorrect";

        // Load registers, memory with prearranged values (6 instructions)
        cpu.startDebug();    // MOV AX, 0xAA55
        cpu.startDebug();    // MOV [0000], AX
        cpu.startDebug();    // MOV [0002], AX
        cpu.startDebug();    // MOV AX, 0xFFFF  ; Move FFFF into AX
        cpu.startDebug();    // MOV CX, 0xFFFF  ; Move FFFF into CX
        cpu.startDebug();    // MOV DX, 0xFFFF  ; Move FFFF into DX
        assertEquals(AX_ERROR, (byte) 0xFF, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0xFF, cpu.getRegisterValue("AX")[1]);
        assertEquals(CX_ERROR, (byte) 0xFF, cpu.getRegisterValue("CX")[0]);
        assertEquals(CX_ERROR, (byte) 0xFF, cpu.getRegisterValue("CX")[1]);
        assertEquals(DX_ERROR, (byte) 0xFF, cpu.getRegisterValue("DX")[0]);
        assertEquals(DX_ERROR, (byte) 0xFF, cpu.getRegisterValue("DX")[1]);


        // AND reg,mem
        cpu.startDebug();    // AND AX, [BX+SI]
        assertTrue(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertEquals(AX_ERROR, (byte) 0xAA, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0x55, cpu.getRegisterValue("AX")[1]);

        // AND reg,mem+8b
        cpu.startDebug();    // AND CX, [BX+DI+02]
        assertTrue(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertEquals(CX_ERROR, (byte) 0xAA, cpu.getRegisterValue("CX")[0]);
        assertEquals(CX_ERROR, (byte) 0x55, cpu.getRegisterValue("CX")[1]);

        // AND reg,mem+16b
        cpu.startDebug();    // INC BP
        cpu.startDebug();    // AND DX, [BP+0x0100]
        assertTrue(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertEquals(DX_ERROR, (byte) 0xAA, cpu.getRegisterValue("DX")[0]);
        assertEquals(DX_ERROR, (byte) 0x55, cpu.getRegisterValue("DX")[1]);

        // AND reg, reg
        cpu.startDebug();    // AND AX, BX
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));
        assertEquals(AX_ERROR, (byte) 0x00, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0x00, cpu.getRegisterValue("AX")[1]);
        cpu.startDebug();    // HLT

    }
}
