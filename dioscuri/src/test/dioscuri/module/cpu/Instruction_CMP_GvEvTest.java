package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class Instruction_CMP_GvEvTest extends AbstractInstructionTest {

    public Instruction_CMP_GvEvTest() throws Exception {
        super(80448, "CMP_GvEv.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_CMP_GvEv.execute()'
    */
    @Test
    public void testExecute() {
        String AX_ERROR = "AX contains wrong value";
        String CX_ERROR = "CX contains wrong value";
        String DX_ERROR = "DX contains wrong value";
        String OF_ERROR = "OF incorrect";
        String SF_ERROR = "SF incorrect";
        String ZF_ERROR = "ZF incorrect";
        String AF_ERROR = "AF incorrect";
        String PF_ERROR = "PF incorrect";
        String CF_ERROR = "CF incorrect";

        // Load memory, registers with pre-arranged values (7 instructions)
        cpu.startDebug(); // MOV AH, 0x80
        cpu.startDebug(); // MOV [0000], AX
        cpu.startDebug(); // MOV AX, 0x0102
        cpu.startDebug(); // MOV [0002], AX
        cpu.startDebug(); // MOV CX, 0x8101
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[0], (byte) 0x81);
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[1], (byte) 0x01);
        cpu.startDebug(); // MOV DX, 0x8102
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[0], (byte) 0x81);
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[1], (byte) 0x02);
        cpu.startDebug(); // MOV AX, 0x0000
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x00);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x00);

        assertFalse(OF_ERROR, cpu.getFlagValue('O'));
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertFalse(AF_ERROR, cpu.getFlagValue('A'));
        assertFalse(PF_ERROR, cpu.getFlagValue('P'));
        assertFalse(CF_ERROR, cpu.getFlagValue('C'));

        // Test CMP
        cpu.startDebug(); // CMP AX, [BX+SI]; 0000 - 8000, test OF, SF, CF
        assertTrue(OF_ERROR, cpu.getFlagValue('O'));
        assertTrue(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(CF_ERROR, cpu.getFlagValue('C'));

        cpu.startDebug(); // CMP CX, [BX+DI+02]      ; 8101 - 0102, test OF, SF, AF, PF flags
        assertTrue(OF_ERROR, cpu.getFlagValue('O'));
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        assertFalse(CF_ERROR, cpu.getFlagValue('C'));

        cpu.startDebug(); // MOV BP, 0x000C          ; Set BP to C
        cpu.startDebug(); // CMP CX, [BP+0x0100]     ; 8101 - 8101, test ZF
        assertFalse(OF_ERROR, cpu.getFlagValue('O'));
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));
        assertFalse(AF_ERROR, cpu.getFlagValue('A'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));

        cpu.startDebug(); // CMP DX, [BP+0x0100]     ; 8102 - 8101, test AF, CF, overflow
        assertFalse(OF_ERROR, cpu.getFlagValue('O'));
        assertFalse(AF_ERROR, cpu.getFlagValue('A'));
        assertFalse(CF_ERROR, cpu.getFlagValue('C'));

        cpu.startDebug(); // MOV CX, 0x7F01
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[0], (byte) 0x7F);
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[1], (byte) 0x01);
        cpu.startDebug(); // MOV DX, 0xFF01
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[0], (byte) 0xFF);
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[1], (byte) 0x01);
        cpu.startDebug(); // CMP CX, DX      ; 7F01 - FF01, test OF, CF
        assertTrue(OF_ERROR, cpu.getFlagValue('O'));
        assertTrue(CF_ERROR, cpu.getFlagValue('C'));

        cpu.startDebug(); // HLT         
    }
}
