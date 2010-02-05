package dioscuri.module.cpu;

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
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0xFF);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0xFF);
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[0], (byte) 0xFF);
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[1], (byte) 0xFF);
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[0], (byte) 0xFF);
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[1], (byte) 0xFF);


        // AND reg,mem
        cpu.startDebug();    // AND AX, [BX+SI]
        assertTrue(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0xAA);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x55);

        // AND reg,mem+8b
        cpu.startDebug();    // AND CX, [BX+DI+02]
        assertTrue(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[0], (byte) 0xAA);
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[1], (byte) 0x55);

        // AND reg,mem+16b
        cpu.startDebug();    // INC BP
        cpu.startDebug();    // AND DX, [BP+0x0100]
        assertTrue(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[0], (byte) 0xAA);
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[1], (byte) 0x55);

        // AND reg, reg
        cpu.startDebug();    // AND AX, BX
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x00);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x00);
        cpu.startDebug();    // HLT

    }
}
