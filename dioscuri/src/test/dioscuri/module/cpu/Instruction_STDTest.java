package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Bram Lohman\n@author Bart Kiers
 */
public class Instruction_STDTest extends AbstractInstructionTest {

    /**
     * @throws Exception
     */
    public Instruction_STDTest() throws Exception {
        super(80448, "STD.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_STD.execute()'
    */

    /**
     *
     */
    @Test
    public void testExecute() {
        String DF_ERROR = "DF incorrect";

        // Test setting of direction flag
        assertFalse(DF_ERROR, cpu.getFlagValue('D'));
        cpu.startDebug();   // STD
        assertTrue(DF_ERROR, cpu.getFlagValue('D'));
        cpu.startDebug();   // STD
        assertTrue(DF_ERROR, cpu.getFlagValue('D'));
        cpu.startDebug();   // HLT

    }


}
