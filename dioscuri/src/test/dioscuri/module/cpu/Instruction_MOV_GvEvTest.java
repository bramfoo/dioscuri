package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Bram Lohman\n@author Bart Kiers
 */
public class Instruction_MOV_GvEvTest extends AbstractInstructionTest {

    /**
     * @throws Exception
     */
    public Instruction_MOV_GvEvTest() throws Exception {
        super(80448, "MOV_GvEv.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_MOV_GvEv.execute()'
    */

    /**
     *
     */
    @Test
    public void testExecute() {
        String AX_ERROR = "AX contains wrong value";
        String BX_ERROR = "BX contains wrong value";
        String CX_ERROR = "CX contains wrong value";
        String DX_ERROR = "DX contains wrong value";
        String BP_ERROR = "BP contains wrong value";
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
        cpu.startDebug(); // MOV [0002], AX
        assertEquals(AX_ERROR, (byte) 0xAA, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0xBB, cpu.getRegisterValue("AX")[1]);

        // MOV mem,reg
        cpu.startDebug(); // MOV CX, [BX+SI] ; MOV reg and mem, store in memory
        assertEquals(CX_ERROR, (byte) 0xAA, cpu.getRegisterValue("CX")[0]);
        assertEquals(CX_ERROR, (byte) 0xBB, cpu.getRegisterValue("CX")[1]);

        // MOV mem+8b,reg
        cpu.startDebug(); // MOV DX, [BX+DI+02]      ; MOV reg and mem+8b, store in memory
        assertEquals(DX_ERROR, (byte) 0xAA, cpu.getRegisterValue("DX")[0]);
        assertEquals(DX_ERROR, (byte) 0xBB, cpu.getRegisterValue("DX")[1]);

        // MOV mem+16b,reg
        cpu.startDebug(); // INC BP                  ; Set BP to 1
        cpu.startDebug(); // MOV BX, [BP+0x0100]     ; MOV reg and mem+16b, store in memory
        assertEquals(BX_ERROR, (byte) 0xAA, cpu.getRegisterValue("BX")[0]);
        assertEquals(BX_ERROR, (byte) 0xBB, cpu.getRegisterValue("BX")[1]);

        // MOV reg, reg
        cpu.startDebug(); // MOV BP, BX      ; MOV 2 registers
        assertEquals(BP_ERROR, (byte) 0xAA, cpu.getRegisterValue("BP")[0]);
        assertEquals(BP_ERROR, (byte) 0xBB, cpu.getRegisterValue("BP")[1]);

        assertFalse(OF_ERROR, cpu.getFlagValue('O'));
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertFalse(AF_ERROR, cpu.getFlagValue('A'));
        assertFalse(PF_ERROR, cpu.getFlagValue('P'));
        assertFalse(CF_ERROR, cpu.getFlagValue('C'));

        cpu.startDebug(); // HLT             ; Stop execution        
    }

}
