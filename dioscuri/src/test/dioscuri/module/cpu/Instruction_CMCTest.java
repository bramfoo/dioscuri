package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Instruction_CMCTest extends AbstractInstructionTest {

    public Instruction_CMCTest() throws Exception {
        super(80448, "CMC.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_CMC.execute()'
    */
    @Test
    public void testExecute() {
        String CF_ERROR = "CF incorrect";

        // Test inverting of carry flag
        assertFalse(CF_ERROR, cpu.getFlagValue('C'));
        cpu.startDebug();   // CMC
        assertTrue(CF_ERROR, cpu.getFlagValue('C'));
        cpu.startDebug();   // CMC
        assertFalse(CF_ERROR, cpu.getFlagValue('C'));
        cpu.startDebug();   // HLT

    }


}
