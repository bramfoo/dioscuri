package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Bram Lohman\n@author Bart Kiers
 */
public class Instruction_LOOP_JbTest extends AbstractInstructionTest {

    /**
     * @throws Exception
     */
    public Instruction_LOOP_JbTest() throws Exception {
        super(80448, "LOOP_Jb.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_LOOP_Jb.execute()'
    */

    /**
     *
     */
    @Test
    public void testExecute() {
        String IP_ERROR = "IP contains wrong value";
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
        assertEquals(IP_ERROR, (byte) 0x01, cpu.getRegisterValue("IP")[0]);
        assertEquals(IP_ERROR, (byte) 0x00, cpu.getRegisterValue("IP")[1]);

        // Load loop counter CX
        cpu.startDebug(); // MOV CL, 0x03
        assertEquals(CX_ERROR, (byte) 0x03, cpu.getRegisterValue("CX")[1]);

        // Start loop
        cpu.startDebug(); // INC ax
        cpu.startDebug(); // LOOP 
        assertEquals(IP_ERROR, (byte) 0x02, cpu.getRegisterValue("IP")[1]);
        assertEquals(AX_ERROR, (byte) 0x01, cpu.getRegisterValue("AX")[1]);
        assertEquals(CX_ERROR, (byte) 0x02, cpu.getRegisterValue("CX")[1]);

        cpu.startDebug(); // INC ax
        cpu.startDebug(); // LOOP 
        assertEquals(IP_ERROR, (byte) 0x02, cpu.getRegisterValue("IP")[1]);
        assertEquals(AX_ERROR, (byte) 0x02, cpu.getRegisterValue("AX")[1]);
        assertEquals(CX_ERROR, (byte) 0x01, cpu.getRegisterValue("CX")[1]);

        cpu.startDebug(); // INC ax
        cpu.startDebug(); // LOOP 
        assertEquals(IP_ERROR, (byte) 0x05, cpu.getRegisterValue("IP")[1]);
        assertEquals(AX_ERROR, (byte) 0x03, cpu.getRegisterValue("AX")[1]);
        assertEquals(CX_ERROR, (byte) 0x00, cpu.getRegisterValue("CX")[1]);

        cpu.startDebug(); // HLT
    }

}
