package dioscuri.module.cpu;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class Instruction_XCHG_BXAXTest extends AbstractInstructionTest {

    public Instruction_XCHG_BXAXTest() throws Exception {
        super(80448, "XCHG_BXAX.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_XCHG_BXAX.execute()'
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
    }

}
