package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Instruction_STCTest extends AbstractInstructionTest {

    public Instruction_STCTest() throws Exception {
        super(80448, "STC.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_STC.execute()'
    */
    @Test
    public void testExecute() {
        String CF_ERROR = "CF incorrect";

        // Test setting of direction flag
        assertFalse(CF_ERROR, cpu.getFlagValue('C'));
        cpu.startDebug();   // STC
        assertTrue(CF_ERROR, cpu.getFlagValue('C'));
        cpu.startDebug();   // STC
        assertTrue(CF_ERROR, cpu.getFlagValue('C'));
        cpu.startDebug();   // HLT

    }


}
