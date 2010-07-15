package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Bram Lohman\n@author Bart Kiers
 */
public class Instruction_DEC_SITest extends AbstractInstructionTest {

    /**
     *
     * @throws Exception
     */
    public Instruction_DEC_SITest() throws Exception {
        super(80448, "DEC_SI.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_DEC_SI.execute()'
    */
    /**
     *
     */
    @Test
    public void testExecute() {
        String SI_ERROR = "SI contains wrong value";
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

        // Test DEC instruction
        cpu.startDebug(); // DEC SI          ; Decrement SI, test AF
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));

        cpu.startDebug(); // MOV SI, 0x8000  ; Prepare for OF
        assertEquals(SI_ERROR, (byte) 0x80, cpu.getRegisterValue("SI")[0]);
        assertEquals(SI_ERROR, (byte) 0x00, cpu.getRegisterValue("SI")[1]);
        cpu.startDebug(); // DEC SI          ; Decrement SI, test OF, SF
        assertTrue(OF_ERROR, cpu.getFlagValue('O'));
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));

        cpu.startDebug(); // MOV SI, 0x0001  ; Prepare for ZF
        assertEquals(SI_ERROR, (byte) 0x00, cpu.getRegisterValue("SI")[0]);
        assertEquals(SI_ERROR, (byte) 0x01, cpu.getRegisterValue("SI")[1]);
        cpu.startDebug(); // DEC SI          ; Decrement SI, test ZF
        assertFalse(OF_ERROR, cpu.getFlagValue('O'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));

        cpu.startDebug(); // HLT
    }

}
