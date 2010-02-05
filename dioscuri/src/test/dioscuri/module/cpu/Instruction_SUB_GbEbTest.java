package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class Instruction_SUB_GbEbTest extends AbstractInstructionTest {

    public Instruction_SUB_GbEbTest() throws Exception {
        super(80448, "SUB_GbEb.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_SUB_GbEb.execute()'
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

        // Load registers with pre-arranged values
        cpu.startDebug(); // MOV AX, 0x7B05
        cpu.startDebug(); // MOV [0000], AX
        cpu.startDebug(); // MOV AX, 0x0285
        assertEquals(AX_ERROR, (byte) 0x02, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0x85, cpu.getRegisterValue("AX")[1]);

        // SUB mem,reg
        cpu.startDebug(); // SUB AL, [BX+SI] ; 85 - 05, test SF, PF flags
        assertEquals(AX_ERROR, (byte) 0x02, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0x80, cpu.getRegisterValue("AX")[1]);
        assertTrue(SF_ERROR, cpu.getFlagValue('S'));
        assertFalse(PF_ERROR, cpu.getFlagValue('P'));

        cpu.startDebug(); // SUB AL, [BX+SI] ; 80 - 05, test OF, SF, AF, PF flags
        assertEquals(AX_ERROR, (byte) 0x02, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0x7B, cpu.getRegisterValue("AX")[1]);
        assertTrue(OF_ERROR, cpu.getFlagValue('O'));
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));

        // SUB mem+8b,reg
        cpu.startDebug(); // SUB AL, [BX+SI+01]      ; 7B - 7B, test ZF
        assertEquals(AX_ERROR, (byte) 0x02, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0x00, cpu.getRegisterValue("AX")[1]);
        assertFalse(OF_ERROR, cpu.getFlagValue('O'));
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));
        assertFalse(AF_ERROR, cpu.getFlagValue('A'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));

        // SUB mem+16b,reg
        cpu.startDebug(); // INC BP                  ; Set BP to 1
        cpu.startDebug(); // SUB AL, [BP+0x0100]     ; 00 - 05, test CF, overflow
        assertEquals(AX_ERROR, (byte) 0x02, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0xFB, cpu.getRegisterValue("AX")[1]);
        assertTrue(SF_ERROR, cpu.getFlagValue('S'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));
        assertFalse(PF_ERROR, cpu.getFlagValue('P'));
        assertTrue(CF_ERROR, cpu.getFlagValue('C'));

        // SUB reg, reg
        cpu.startDebug(); // SUB AL, AH      ; FB - 02, test CF, PF
        assertEquals(AX_ERROR, (byte) 0x02, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0xF9, cpu.getRegisterValue("AX")[1]);
        assertFalse(AF_ERROR, cpu.getFlagValue('A'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        assertFalse(CF_ERROR, cpu.getFlagValue('C'));

        cpu.startDebug(); // HLT             ; Stop execution

    }

}
