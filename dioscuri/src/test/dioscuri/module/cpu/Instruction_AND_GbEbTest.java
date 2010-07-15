package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Bram Lohman\n@author Bart Kiers
 */
public class Instruction_AND_GbEbTest extends AbstractInstructionTest {

    /**
     *
     * @throws Exception
     */
    public Instruction_AND_GbEbTest() throws Exception {
        super(80448, "AND_GbEb.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_AND_GbEb.execute()'
    */
    /**
     *
     */
    @Test
    public void testExecute() {
        String AX_ERROR = "AX contains wrong value";
        String SF_ERROR = "SF incorrect";
        String ZF_ERROR = "ZF incorrect";
        String PF_ERROR = "PF incorrect";

        // Load registers, memory with prearranged values (4 instructions)
        cpu.startDebug();    // MOV AX, 0xA980
        cpu.startDebug();    // MOV [0000], AX
        cpu.startDebug();    // MOV AX, 0x55AA
        assertEquals(AX_ERROR, (byte) 0x55, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0xAA, cpu.getRegisterValue("AX")[1]);

        // AND mem, reg
        cpu.startDebug();    // AND AL, [BX+SI]
        assertTrue(SF_ERROR, cpu.getFlagValue('S'));
        assertFalse(PF_ERROR, cpu.getFlagValue('P'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertEquals(AX_ERROR, (byte) 0x80, cpu.getRegisterValue("AX")[1]);

        // AND reg,mem+8b
        cpu.startDebug();    // AND AH, [BX+DI+01]
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertFalse(PF_ERROR, cpu.getFlagValue('P'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertEquals(AX_ERROR, (byte) 0x01, cpu.getRegisterValue("AX")[0]);


        // AND reg,mem+16b
        cpu.startDebug();    // INC BP
        cpu.startDebug();    // AND AH, [BP+0x0100]
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));
        assertEquals(AX_ERROR, (byte) 0x00, cpu.getRegisterValue("AX")[0]);

        // AND reg, reg
        cpu.startDebug();    // AND AL, AH
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));
        assertEquals(AX_ERROR, (byte) 0x00, cpu.getRegisterValue("AX")[1]);
        cpu.startDebug();    // HLT

    }
}
