package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Instruction_CLCTest extends AbstractInstructionTest {

    public Instruction_CLCTest() throws Exception {
        super(80448, "CLC.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_CLC.execute()'
    */
    @Test
    public void testExecute() {
        String CF_ERROR = "CF incorrect";

        // Test clearing of carry flag
        assertFalse(CF_ERROR, cpu.getFlagValue('C'));
        cpu.startDebug();   // CLC
        assertFalse(CF_ERROR, cpu.getFlagValue('C'));
        cpu.startDebug();   // SUB AL, 0x01
        assertTrue(CF_ERROR, cpu.getFlagValue('C'));
        cpu.startDebug();   // CLC
        assertFalse(CF_ERROR, cpu.getFlagValue('C'));
        cpu.startDebug();   // HLT

    }


}
