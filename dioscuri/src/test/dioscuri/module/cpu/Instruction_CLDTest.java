package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Bram Lohman\n@author Bart Kiers
 */
public class Instruction_CLDTest extends AbstractInstructionTest {

    /**
     *
     * @throws Exception
     */
    public Instruction_CLDTest() throws Exception {
        super(80448, "CLD.bin");
    }

    /**
     *
     */
    @Test
    public void testExecute() {
        String DF_ERROR = "DF incorrect";

        // Test clearing of direction flag
        assertFalse(DF_ERROR, cpu.getFlagValue('D'));
        cpu.startDebug();   // CLD
        assertFalse(DF_ERROR, cpu.getFlagValue('D'));
        cpu.startDebug();   // STD
        assertTrue(DF_ERROR, cpu.getFlagValue('D'));
        cpu.startDebug();   // CLD
        assertFalse(DF_ERROR, cpu.getFlagValue('D'));
        cpu.startDebug();   // HLT

    }


}
