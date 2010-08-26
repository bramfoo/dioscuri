package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Bram Lohman\n@author Bart Kiers
 */
public class Instruction_INC_CXTest extends AbstractInstructionTest {

    /**
     *
     * @throws Exception
     */
    public Instruction_INC_CXTest() throws Exception {
        super(80448, "INC_CX.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_DEC_CX.execute()'
    */
    /**
     *
     */
    @Test
    public void testExecute() {
        String CX_ERROR = "CX contains wrong value";
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
        assertEquals(CX_ERROR, (byte) 0x0F, cpu.getRegisterValue("CX")[1]);
        cpu.startDebug(); // INC cx          ; Increment CX, test AF
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));

        cpu.startDebug(); // MOV cx, 0x7FFF  ; Prepare for OF
        assertEquals(CX_ERROR, (byte) 0x7F, cpu.getRegisterValue("CX")[0]);
        assertEquals(CX_ERROR, (byte) 0xFF, cpu.getRegisterValue("CX")[1]);
        cpu.startDebug(); // INC cx          ; Increment CX, test OF, SF
        assertTrue(OF_ERROR, cpu.getFlagValue('O'));
        assertTrue(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));

        cpu.startDebug(); // MOV cx, 0xFFFF  ; Prepare for ZF
        assertEquals(CX_ERROR, (byte) 0xFF, cpu.getRegisterValue("CX")[0]);
        assertEquals(CX_ERROR, (byte) 0xFF, cpu.getRegisterValue("CX")[1]);
        cpu.startDebug(); // INC cx          ; Increment CX, test ZF
        assertFalse(OF_ERROR, cpu.getFlagValue('O'));
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));

        cpu.startDebug(); // INC cx          ; Increment CX, test !AF
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertFalse(AF_ERROR, cpu.getFlagValue('A'));

    }

}
