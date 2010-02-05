package dioscuri.module.cpu;

import org.junit.Test;

import static org.junit.Assert.*;

public class Instruction_DEC_DITest extends AbstractInstructionTest {

    public Instruction_DEC_DITest() throws Exception {
        super(80448, "DEC_DI.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_DEC_DI.execute()'
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

        // Test DEC instruction
        cpu.startDebug(); // DEC DI          ; Decrement DI, test AF
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));

        cpu.startDebug(); // MOV DI, 0x8000  ; Prepare for OF
        assertEquals(DI_ERROR, cpu.getRegisterValue("DI")[0], (byte) 0x80);
        assertEquals(DI_ERROR, cpu.getRegisterValue("DI")[1], (byte) 0x00);
        cpu.startDebug(); // DEC DI          ; Decrement DI, test OF, SF
        assertTrue(OF_ERROR, cpu.getFlagValue('O'));
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));

        cpu.startDebug(); // MOV DI, 0x0001  ; Prepare for ZF
        assertEquals(DI_ERROR, cpu.getRegisterValue("DI")[0], (byte) 0x00);
        assertEquals(DI_ERROR, cpu.getRegisterValue("DI")[1], (byte) 0x01);
        cpu.startDebug(); // DEC DI          ; Decrement DI, test ZF
        assertFalse(OF_ERROR, cpu.getFlagValue('O'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));

        cpu.startDebug(); // HLT
    }

}
