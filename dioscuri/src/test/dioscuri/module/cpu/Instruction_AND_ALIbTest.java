package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class Instruction_AND_ALIbTest extends AbstractInstructionTest {

    public Instruction_AND_ALIbTest() throws Exception {
        super(80448, "AND_ALIb.bin");
    }

    /*
     * Test method for 'com.tessella.emulator.module.cpu.Instruction_AND_ALIb.execute()'
     */
    @Test
    public void testExecute() {
        String AX_ERROR = "AX contains wrong value";
        String SF_ERROR = "SF incorrect";
        String ZF_ERROR = "ZF incorrect";
        String PF_ERROR = "PF incorrect";

        // Execute DEC_AX
        cpu.startDebug();
        assertEquals(AX_ERROR, (byte) 0xFF, cpu.getRegisterValue("AX")[1]);
        // Execute AND AL, 55
        cpu.startDebug();
        assertEquals(AX_ERROR, (byte) 0x55, cpu.getRegisterValue("AX")[1]);
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        // Execute AND AL, AA
        cpu.startDebug();
        assertEquals(AX_ERROR, (byte) 0x00, cpu.getRegisterValue("AX")[1]);
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        // Execute DEC_AX
        cpu.startDebug();
        assertEquals(AX_ERROR, (byte) 0xFF, cpu.getRegisterValue("AX")[1]);
        // Execute AND AL, 00
        cpu.startDebug();
        assertEquals(AX_ERROR, (byte) 0x00, cpu.getRegisterValue("AX")[1]);
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
    }
}
