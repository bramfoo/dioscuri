package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Bram Lohman\n@author Bart Kiers
 */
public class Instruction_INC_SITest extends AbstractInstructionTest {

    /**
     * @throws Exception
     */
    public Instruction_INC_SITest() throws Exception {
        super(80448, "INC_SI.bin");
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

        // Test INC instruction
        cpu.startDebug(); // MOV al, 0x0F    ; Prepare for AF
        assertEquals(SI_ERROR, (byte) 0x0F, cpu.getRegisterValue("SI")[1]);
        cpu.startDebug(); // INC si          ; Increment SI, test AF
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));

        cpu.startDebug(); // MOV si, 0x7FFF  ; Prepare for OF
        assertEquals(SI_ERROR, (byte) 0x7F, cpu.getRegisterValue("SI")[0]);
        assertEquals(SI_ERROR, (byte) 0xFF, cpu.getRegisterValue("SI")[1]);
        cpu.startDebug(); // INC si          ; Increment SI, test OF, SF
        assertTrue(OF_ERROR, cpu.getFlagValue('O'));
        assertTrue(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));

        cpu.startDebug(); // MOV si, 0xFFFF  ; Prepare for ZF
        assertEquals(SI_ERROR, (byte) 0xFF, cpu.getRegisterValue("SI")[0]);
        assertEquals(SI_ERROR, (byte) 0xFF, cpu.getRegisterValue("SI")[1]);
        cpu.startDebug(); // INC si          ; Increment SI, test ZF
        assertFalse(OF_ERROR, cpu.getFlagValue('O'));
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));

        cpu.startDebug(); // INC si          ; Increment SI, test !AF
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertFalse(AF_ERROR, cpu.getFlagValue('A'));

    }

}
