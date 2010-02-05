package dioscuri.module.cpu;

import org.junit.Test;

import static org.junit.Assert.*;

public class Instruction_DEC_DXTest extends AbstractInstructionTest {

    public Instruction_DEC_DXTest() throws Exception {
        super(80448, "DEC_DX.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_DEC_DX.execute()'
    */
    @Test
    public void testExecute() {
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

        // Test DEC instruction
        cpu.startDebug(); // DEC DX          ; Decrement DX, test AF
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));

        cpu.startDebug(); // MOV DX, 0x8000  ; Prepare for OF
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[0], (byte) 0x80);
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[1], (byte) 0x00);
        cpu.startDebug(); // DEC DX          ; Decrement DX, test OF, SF
        assertTrue(OF_ERROR, cpu.getFlagValue('O'));
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));

        cpu.startDebug(); // MOV DX, 0x0001  ; Prepare for ZF
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[0], (byte) 0x00);
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[1], (byte) 0x01);
        cpu.startDebug(); // DEC DX          ; Decrement DX, test ZF
        assertFalse(OF_ERROR, cpu.getFlagValue('O'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));

        cpu.startDebug(); // HLT
    }

}
