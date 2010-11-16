package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Bram Lohman\n@author Bart Kiers
 */
public class Instruction_CMP_EvGvTest extends AbstractInstructionTest {

    /**
     * @throws Exception
     */
    public Instruction_CMP_EvGvTest() throws Exception {
        super(80448, "CMP_EvGv.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_CMP_EvGv.execute()'
    */

    /**
     *
     */
    @Test
    public void testExecute() {
        String AX_ERROR = "AX contains wrong value";
        String CX_ERROR = "CX contains wrong value";
        String DX_ERROR = "DX contains wrong value";
        String BP_ERROR = "BP contains wrong value";
        String OF_ERROR = "OF incorrect";
        String SF_ERROR = "SF incorrect";
        String ZF_ERROR = "ZF incorrect";
        String AF_ERROR = "AF incorrect";
        String PF_ERROR = "PF incorrect";
        String CF_ERROR = "CF incorrect";

        // Load memory, registers with pre-arranged values (6 instructions)
        cpu.startDebug(); // MOV [0000], AX
        cpu.startDebug(); // MOV AX, 0x8101
        cpu.startDebug(); // MOV [0002], AX
        cpu.startDebug(); // MOV AX, 0x8000
        assertEquals(AX_ERROR, (byte) 0x80, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0x00, cpu.getRegisterValue("AX")[1]);
        cpu.startDebug(); // MOV CX, 0x0102
        assertEquals(CX_ERROR, (byte) 0x01, cpu.getRegisterValue("CX")[0]);
        assertEquals(CX_ERROR, (byte) 0x02, cpu.getRegisterValue("CX")[1]);
        cpu.startDebug(); // MOV DX, 0x8102        
        assertEquals(DX_ERROR, (byte) 0x81, cpu.getRegisterValue("DX")[0]);
        assertEquals(DX_ERROR, (byte) 0x02, cpu.getRegisterValue("DX")[1]);

        assertFalse(OF_ERROR, cpu.getFlagValue('O'));
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertFalse(AF_ERROR, cpu.getFlagValue('A'));
        assertFalse(PF_ERROR, cpu.getFlagValue('P'));
        assertFalse(CF_ERROR, cpu.getFlagValue('C'));

        // Test CMP
        cpu.startDebug(); // CMP [BX+SI], AX ; 0000 - 8000, test OF, SF, CF
        assertTrue(OF_ERROR, cpu.getFlagValue('O'));
        assertTrue(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(CF_ERROR, cpu.getFlagValue('C'));

        cpu.startDebug(); // CMP [BX+DI+02], CX      ; 8101 - 0102, test OF, SF, AF, PF flags
        assertTrue(OF_ERROR, cpu.getFlagValue('O'));
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        assertFalse(CF_ERROR, cpu.getFlagValue('C'));

        cpu.startDebug(); // MOV AX, 0x8101
        assertEquals(AX_ERROR, (byte) 0x81, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0x01, cpu.getRegisterValue("AX")[1]);
        cpu.startDebug(); // MOV BP, 0x0004          ; Set BP to 4
        assertEquals(BP_ERROR, (byte) 0x04, cpu.getRegisterValue("BP")[1]);
        cpu.startDebug(); // CMP [BP+0x0100], AX     ; 8101 - 8101, test ZF
        assertFalse(OF_ERROR, cpu.getFlagValue('O'));
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));
        assertFalse(AF_ERROR, cpu.getFlagValue('A'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));

        cpu.startDebug(); // CMP [BP+0x0100], DX     ; Test AF, CF, overflow
        assertFalse(OF_ERROR, cpu.getFlagValue('O'));
        assertTrue(AF_ERROR, cpu.getFlagValue('A'));
        assertTrue(CF_ERROR, cpu.getFlagValue('C'));

        cpu.startDebug(); // MOV CX, 0x7F01
        assertEquals(CX_ERROR, (byte) 0x7F, cpu.getRegisterValue("CX")[0]);
        assertEquals(CX_ERROR, (byte) 0x01, cpu.getRegisterValue("CX")[1]);
        cpu.startDebug(); // MOV DX, 0xFF01
        assertEquals(DX_ERROR, (byte) 0xFF, cpu.getRegisterValue("DX")[0]);
        assertEquals(DX_ERROR, (byte) 0x01, cpu.getRegisterValue("DX")[1]);

        cpu.startDebug(); // CMP CX, DX      ; 7F01 - FF01, test OF, CF
        assertTrue(OF_ERROR, cpu.getFlagValue('O'));
        assertTrue(CF_ERROR, cpu.getFlagValue('C'));

        cpu.startDebug(); // HLT         
    }
}
