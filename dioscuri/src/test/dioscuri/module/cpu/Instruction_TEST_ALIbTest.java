package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class Instruction_TEST_ALIbTest extends AbstractInstructionTest {

    public Instruction_TEST_ALIbTest() throws Exception {
        super(80448, "TEST_ALIb.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_TEST_ALIb.execute()'
    */
    @Test
    public void testExecute() {
        String AX_ERROR = "AX contains wrong value";
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

        // Test TEST instruction
        cpu.startDebug(); // DEC ax          ; Set AL to FF
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0xFF);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0xFF);
        assertTrue(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));

        cpu.startDebug(); // TEST al, 0x54   ; 1111 1111 | 0101 0101 (SF, PF)
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertFalse(AF_ERROR, cpu.getFlagValue('A'));
        assertFalse(PF_ERROR, cpu.getFlagValue('P'));

        cpu.startDebug(); // TEST al, 0xAA   ; 1010 1010 | 1010 1010 (SF, PF)
        assertTrue(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));

        cpu.startDebug(); // TEST al, 0x00   ; 1111 1111 | 0000 0000 (SF, ZF, PF)
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));

        cpu.startDebug(); // HLT             ; Stop execution

    }
}
