package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Instruction_STITest extends AbstractInstructionTest {

    public Instruction_STITest() throws Exception {
        super(80448, "STI.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_STI.execute()'
    */
    @Test
    public void testExecute() {
        String IF_ERROR = "IF incorrect";

        // Test setting of direction flag
        assertTrue(IF_ERROR, cpu.getFlagValue('I'));
        cpu.startDebug();   // STI
        assertTrue(IF_ERROR, cpu.getFlagValue('I'));
        cpu.startDebug();   // CLI
        assertFalse(IF_ERROR, cpu.getFlagValue('I'));
        cpu.startDebug();   // STI
        assertTrue(IF_ERROR, cpu.getFlagValue('I'));
        cpu.startDebug();   // HLT

    }


}
