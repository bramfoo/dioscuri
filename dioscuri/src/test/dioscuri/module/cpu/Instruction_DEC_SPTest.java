package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class Instruction_DEC_SPTest extends AbstractInstructionTest {

    public Instruction_DEC_SPTest() throws Exception {
        super(80448, "DEC_SP.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_DEC_SP.execute()'
    */
    @Test
    public void testExecute() {
        String SP_ERROR = "SP contains wrong value";
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
        cpu.startDebug(); // Initialise SP to 0
        cpu.startDebug(); // DEC SP          ; Decrement SP, test AF
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));

        cpu.startDebug(); // MOV SP, 0x8000  ; Prepare for OF
        assertEquals(SP_ERROR, (byte) 0x80, cpu.getRegisterValue("SP")[0]);
        assertEquals(SP_ERROR, (byte) 0x00, cpu.getRegisterValue("SP")[1]);
        cpu.startDebug(); // DEC SP          ; Decrement SP, test OF, SF
        assertTrue(OF_ERROR, cpu.getFlagValue('O'));
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));

        cpu.startDebug(); // MOV SP, 0x0001  ; Prepare for ZF
        assertEquals(SP_ERROR, (byte) 0x00, cpu.getRegisterValue("SP")[0]);
        assertEquals(SP_ERROR, (byte) 0x01, cpu.getRegisterValue("SP")[1]);
        cpu.startDebug(); // DEC SP          ; Decrement SP, test ZF
        assertFalse(OF_ERROR, cpu.getFlagValue('O'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));

        cpu.startDebug(); // HLT
    }

}
