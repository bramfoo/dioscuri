package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Bram Lohman\n@author Bart Kiers
 */
public class Instruction_PUSHATest extends AbstractInstructionTest {

    /**
     * @throws Exception
     */
    public Instruction_PUSHATest() throws Exception {
        super(80448, "PUSHA.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_PUSHA.execute()'
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
        String SP_ERROR = "SP contains wrong value";
        String BP_ERROR = "BP contains wrong value";
        String SI_ERROR = "SI contains wrong value";
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

        // Load registers with pre-arranged values
        cpu.startDebug(); // MOV ax, 0x1111  ; Move value into ax
        assertEquals(AX_ERROR, (byte) 0x11, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0x11, cpu.getRegisterValue("AX")[1]);
        cpu.startDebug(); // MOV cx, 0xaaaa  ; Move value into cx
        assertEquals(CX_ERROR, (byte) 0xAA, cpu.getRegisterValue("CX")[0]);
        assertEquals(CX_ERROR, (byte) 0xAA, cpu.getRegisterValue("CX")[1]);
        cpu.startDebug(); // MOV dx, 0xbbbb  ; Move value into dx
        assertEquals(DX_ERROR, (byte) 0xBB, cpu.getRegisterValue("DX")[0]);
        assertEquals(DX_ERROR, (byte) 0xBB, cpu.getRegisterValue("DX")[1]);
        cpu.startDebug(); // MOV bx, 0xcccc  ; Move value into bx
        assertEquals(BX_ERROR, (byte) 0xCC, cpu.getRegisterValue("BX")[0]);
        assertEquals(BX_ERROR, (byte) 0xCC, cpu.getRegisterValue("BX")[1]);
        cpu.startDebug(); // MOV bp, 0xdddd  ; Move value into bp
        assertEquals(BP_ERROR, (byte) 0xDD, cpu.getRegisterValue("BP")[0]);
        assertEquals(BP_ERROR, (byte) 0xDD, cpu.getRegisterValue("BP")[1]);
        cpu.startDebug(); // MOV si, 0xeeee  ; Move value into si
        assertEquals(SI_ERROR, (byte) 0xEE, cpu.getRegisterValue("SI")[0]);
        assertEquals(SI_ERROR, (byte) 0xEE, cpu.getRegisterValue("SI")[1]);
        cpu.startDebug(); // MOV di, 0xffff  ; Move value into di
        assertEquals(DI_ERROR, (byte) 0xFF, cpu.getRegisterValue("DI")[0]);
        assertEquals(DI_ERROR, (byte) 0xFF, cpu.getRegisterValue("DI")[1]);

        assertEquals(SP_ERROR, (byte) 0xFF, cpu.getRegisterValue("SP")[0]);
        assertEquals(SP_ERROR, (byte) 0xEE, cpu.getRegisterValue("SP")[1]);

        // Test PUSHA operation
        cpu.startDebug(); // PUSHA           ; Push all registers onto stack

        // Clear all registers
        cpu.startDebug(); // MOV ax, 0x0000  ; Clear register
        assertEquals(AX_ERROR, (byte) 0x00, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0x00, cpu.getRegisterValue("AX")[1]);
        cpu.startDebug(); // MOV cx, 0x0000  ; Clear register
        assertEquals(CX_ERROR, (byte) 0x00, cpu.getRegisterValue("CX")[0]);
        assertEquals(CX_ERROR, (byte) 0x00, cpu.getRegisterValue("CX")[1]);
        cpu.startDebug(); // MOV dx, 0x0000  ; Clear register
        assertEquals(DX_ERROR, (byte) 0x00, cpu.getRegisterValue("DX")[0]);
        assertEquals(DX_ERROR, (byte) 0x00, cpu.getRegisterValue("DX")[1]);
        cpu.startDebug(); // MOV bx, 0x0000  ; Clear register
        assertEquals(BX_ERROR, (byte) 0x00, cpu.getRegisterValue("BX")[0]);
        assertEquals(BX_ERROR, (byte) 0x00, cpu.getRegisterValue("BX")[1]);
        cpu.startDebug(); // MOV bp, 0x0000  ; Clear register
        assertEquals(BP_ERROR, (byte) 0x00, cpu.getRegisterValue("BP")[0]);
        assertEquals(BP_ERROR, (byte) 0x00, cpu.getRegisterValue("BP")[1]);
        cpu.startDebug(); // MOV si, 0x0000  ; Clear register
        assertEquals(SI_ERROR, (byte) 0x00, cpu.getRegisterValue("SI")[0]);
        assertEquals(SI_ERROR, (byte) 0x00, cpu.getRegisterValue("SI")[1]);
        cpu.startDebug(); // MOV di, 0x0000  ; Clear register
        assertEquals(DI_ERROR, (byte) 0x00, cpu.getRegisterValue("DI")[0]);
        assertEquals(DI_ERROR, (byte) 0x00, cpu.getRegisterValue("DI")[1]);

        assertEquals(SP_ERROR, (byte) 0xFF, cpu.getRegisterValue("SP")[0]);
        assertEquals(SP_ERROR, (byte) 0xDE, cpu.getRegisterValue("SP")[1]);

        // Test POPA operation
        cpu.startDebug(); // POPA            ; Pop all registers from stack

        assertEquals(AX_ERROR, (byte) 0x11, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0x11, cpu.getRegisterValue("AX")[1]);
        assertEquals(CX_ERROR, (byte) 0xAA, cpu.getRegisterValue("CX")[0]);
        assertEquals(CX_ERROR, (byte) 0xAA, cpu.getRegisterValue("CX")[1]);
        assertEquals(DX_ERROR, (byte) 0xBB, cpu.getRegisterValue("DX")[0]);
        assertEquals(DX_ERROR, (byte) 0xBB, cpu.getRegisterValue("DX")[1]);
        assertEquals(BX_ERROR, (byte) 0xCC, cpu.getRegisterValue("BX")[0]);
        assertEquals(BX_ERROR, (byte) 0xCC, cpu.getRegisterValue("BX")[1]);
        assertEquals(BP_ERROR, (byte) 0xDD, cpu.getRegisterValue("BP")[0]);
        assertEquals(BP_ERROR, (byte) 0xDD, cpu.getRegisterValue("BP")[1]);
        assertEquals(SI_ERROR, (byte) 0xEE, cpu.getRegisterValue("SI")[0]);
        assertEquals(SI_ERROR, (byte) 0xEE, cpu.getRegisterValue("SI")[1]);
        assertEquals(DI_ERROR, (byte) 0xFF, cpu.getRegisterValue("DI")[0]);
        assertEquals(DI_ERROR, (byte) 0xFF, cpu.getRegisterValue("DI")[1]);

        assertEquals(SP_ERROR, (byte) 0xFF, cpu.getRegisterValue("SP")[0]);
        assertEquals(SP_ERROR, (byte) 0xEE, cpu.getRegisterValue("SP")[1]);

        cpu.startDebug(); // HLT             ; Stop execution

    }
}
