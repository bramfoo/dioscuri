package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class Instruction_DEC_BXTest extends AbstractInstructionTest {

    public Instruction_DEC_BXTest() throws Exception {
        super(80448, "DEC_BX.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_DEC_BX.execute()'
    */
    @Test
    public void testExecute() {
        String BX_ERROR = "BX contains wrong value";
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
        cpu.startDebug(); // DEC bx          ; Decrement BX, test AF
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));

        cpu.startDebug(); // MOV bx, 0x8000  ; Prepare for OF
        assertEquals(BX_ERROR, cpu.getRegisterValue("BX")[0], (byte) 0x80);
        assertEquals(BX_ERROR, cpu.getRegisterValue("BX")[1], (byte) 0x00);
        cpu.startDebug(); // DEC bx          ; Decrement BX, test OF, SF
        assertTrue(OF_ERROR, cpu.getFlagValue('O'));
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));

        cpu.startDebug(); // MOV bx, 0x0001  ; Prepare for ZF
        assertEquals(BX_ERROR, cpu.getRegisterValue("BX")[0], (byte) 0x00);
        assertEquals(BX_ERROR, cpu.getRegisterValue("BX")[1], (byte) 0x01);
        cpu.startDebug(); // DEC bx          ; Decrement BX, test ZF
        assertFalse(OF_ERROR, cpu.getFlagValue('O'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));

        cpu.startDebug(); // HLT
    }

}
