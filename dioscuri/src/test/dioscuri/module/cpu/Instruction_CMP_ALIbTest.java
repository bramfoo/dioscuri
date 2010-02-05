package dioscuri.module.cpu;

import org.junit.Test;

import static org.junit.Assert.*;

public class Instruction_CMP_ALIbTest extends AbstractInstructionTest {

    public Instruction_CMP_ALIbTest() throws Exception {
        super(80448, "CMP_ALIb.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_CMP_ALIb.execute()'
    */
    @Test
    public void testExecute() {
        String AX_ERROR = "AX contains wrong value";
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

        // Test CMP
        cpu.startDebug(); // CMP al, 0x0080
        assertTrue(OF_ERROR, cpu.getFlagValue('O'));
        assertTrue(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(CF_ERROR, cpu.getFlagValue('C'));

        cpu.startDebug(); // MOV ax, 0x0185
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x01);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x85);
        cpu.startDebug(); // CMP al, 0x06
        assertTrue(OF_ERROR, cpu.getFlagValue('O'));
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));
        assertFalse(PF_ERROR, cpu.getFlagValue('P'));

        cpu.startDebug(); // CMP al, 0x85
        assertFalse(OF_ERROR, cpu.getFlagValue('O'));
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertFalse(AF_ERROR, cpu.getFlagValue('A'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));

        cpu.startDebug(); // CMP al, 0x86
        assertFalse(OF_ERROR, cpu.getFlagValue('O'));
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));
        assertTrue(CF_ERROR, cpu.getFlagValue('C'));

        cpu.startDebug(); // MOV ax, 0x017F  ; Move value into al
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x01);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x7F);
        cpu.startDebug(); // CMP al, 0xFF
        assertTrue(OF_ERROR, cpu.getFlagValue('O'));
        assertTrue(CF_ERROR, cpu.getFlagValue('C'));

        cpu.startDebug(); // HLT         
    }
}
