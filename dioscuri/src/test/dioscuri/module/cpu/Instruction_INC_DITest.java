package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Bram Lohman\n@author Bart Kiers
 */
public class Instruction_INC_DITest extends AbstractInstructionTest {

    /**
     * @throws Exception
     */
    public Instruction_INC_DITest() throws Exception {
        super(80448, "INC_DI.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_DEC_DI.execute()'
    */

    /**
     *
     */
    @Test
    public void testExecute() {
        String DI_ERROR = "DI contains wrong value";
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

        // Test INC instruction
        cpu.startDebug(); // MOV al, 0x0F    ; Prepare for AF
        assertEquals(DI_ERROR, (byte) 0x0F, cpu.getRegisterValue("DI")[1]);
        cpu.startDebug(); // INC di          ; Increment DI, test AF
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));

        cpu.startDebug(); // MOV di, 0x7FFF  ; Prepare for OF
        assertEquals(DI_ERROR, (byte) 0x7F, cpu.getRegisterValue("DI")[0]);
        assertEquals(DI_ERROR, (byte) 0xFF, cpu.getRegisterValue("DI")[1]);
        cpu.startDebug(); // INC di          ; Increment DI, test OF, SF
        assertTrue(OF_ERROR, cpu.getFlagValue('O'));
        assertTrue(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));

        cpu.startDebug(); // MOV di, 0xFFFF  ; Prepare for ZF
        assertEquals(DI_ERROR, (byte) 0xFF, cpu.getRegisterValue("DI")[0]);
        assertEquals(DI_ERROR, (byte) 0xFF, cpu.getRegisterValue("DI")[1]);
        cpu.startDebug(); // INC di          ; Increment DI, test ZF
        assertFalse(OF_ERROR, cpu.getFlagValue('O'));
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));

        cpu.startDebug(); // INC di          ; Increment DI, test !AF
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertFalse(AF_ERROR, cpu.getFlagValue('A'));

    }

}
