package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class Instruction_INC_BXTest extends AbstractInstructionTest {

    public Instruction_INC_BXTest() throws Exception {
        super(80448, "INC_BX.bin");
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

        // Test INC instruction
        cpu.startDebug(); // MOV al, 0x0F    ; Prepare for AF
        assertEquals(BX_ERROR, cpu.getRegisterValue("BX")[1], (byte) 0x0F);
        cpu.startDebug(); // INC bx          ; Increment BX, test AF
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));

        cpu.startDebug(); // MOV bx, 0x7FFF  ; Prepare for OF
        assertEquals(BX_ERROR, cpu.getRegisterValue("BX")[0], (byte) 0x7F);
        assertEquals(BX_ERROR, cpu.getRegisterValue("BX")[1], (byte) 0xFF);
        cpu.startDebug(); // INC bx          ; Increment BX, test OF, SF
        assertTrue(OF_ERROR, cpu.getFlagValue('O'));
        assertTrue(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));

        cpu.startDebug(); // MOV bx, 0xFFFF  ; Prepare for ZF
        assertEquals(BX_ERROR, cpu.getRegisterValue("BX")[0], (byte) 0xFF);
        assertEquals(BX_ERROR, cpu.getRegisterValue("BX")[1], (byte) 0xFF);
        cpu.startDebug(); // INC bx          ; Increment BX, test ZF
        assertFalse(OF_ERROR, cpu.getFlagValue('O'));
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));

        cpu.startDebug(); // INC bx          ; Increment BX, test !AF
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertFalse(AF_ERROR, cpu.getFlagValue('A'));

    }

}
