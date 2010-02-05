package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class Instruction_XCHG_EbGbTest extends AbstractInstructionTest {

    public Instruction_XCHG_EbGbTest() throws Exception {
        super(80448, "XCHG_EbGb.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_XCHG_EbGb.execute()'
    */
    @Test
    public void testExecute() {
        String AX_ERROR = "AX contains wrong value";
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

        // Load memory with pre-arranged values
        cpu.startDebug(); // MOV AX, 0xAABB
        cpu.startDebug(); // MOV [0000], AX
        cpu.startDebug(); // MOV AX, 0xCCDD
        assertEquals(AX_ERROR, (byte) 0xCC, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0xDD, cpu.getRegisterValue("AX")[1]);

        // XCHG mem,reg
        cpu.startDebug(); // XCHG [BX+SI], AL; XCHG reg and mem, store in memory
        assertEquals(AX_ERROR, (byte) 0xCC, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0xBB, cpu.getRegisterValue("AX")[1]);
        cpu.startDebug(); // MOV AL, [0000]  ; Retrieve result from memory (result = bb)
        assertEquals(AX_ERROR, (byte) 0xCC, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0xDD, cpu.getRegisterValue("AX")[1]);

        // XCHG mem+8b,reg
        cpu.startDebug(); // XCHG [BX+DI+01], AH     ; XCHG reg and mem+8b, store in memory
        assertEquals(AX_ERROR, (byte) 0xAA, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0xDD, cpu.getRegisterValue("AX")[1]);
        cpu.startDebug(); // MOV AL, [0001]          ; Retrieve result from memory (result = aa)
        assertEquals(AX_ERROR, (byte) 0xAA, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0xCC, cpu.getRegisterValue("AX")[1]);

        // XCHG mem+16b,reg
        cpu.startDebug(); // INC BP                  ; Set BP to 1
        cpu.startDebug(); // XCHG [BP+0x0100], AL    ; XCHG reg and mem+16b, store in memory
        assertEquals(AX_ERROR, (byte) 0xAA, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0xBB, cpu.getRegisterValue("AX")[1]);
        cpu.startDebug(); // MOV AL, [0x0101]        ; Retrieve result from memory (result = aa) [NOTE: ACCESSING BYTE 2 OF OWN CODE]
        assertEquals(AX_ERROR, (byte) 0xAA, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0xCC, cpu.getRegisterValue("AX")[1]);

        // XCHG reg, reg
        cpu.startDebug(); // XCHG AL, CH     ; XCHG 2 registers (result = 00)
        assertEquals(AX_ERROR, (byte) 0xAA, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0x00, cpu.getRegisterValue("AX")[1]);
        assertEquals(CX_ERROR, (byte) 0xCC, cpu.getRegisterValue("CX")[0]);
        assertEquals(CX_ERROR, (byte) 0x00, cpu.getRegisterValue("CX")[1]);

        cpu.startDebug(); // HLT             ; Stop execution
    }

}
