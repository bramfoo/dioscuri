package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class Instruction_LOOP_JbTest extends AbstractInstructionTest {

    public Instruction_LOOP_JbTest() throws Exception {
        super(80448, "LOOP_Jb.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_LOOP_Jb.execute()'
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
        assertEquals(IP_ERROR, cpu.getRegisterValue("IP")[0], (byte) 0x01);
        assertEquals(IP_ERROR, cpu.getRegisterValue("IP")[1], (byte) 0x00);

        // Load loop counter CX
        cpu.startDebug(); // MOV CL, 0x03
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[1], (byte) 0x03);

        // Start loop
        cpu.startDebug(); // INC ax
        cpu.startDebug(); // LOOP 
        assertEquals(IP_ERROR, cpu.getRegisterValue("IP")[1], (byte) 0x02);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x01);
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[1], (byte) 0x02);

        cpu.startDebug(); // INC ax
        cpu.startDebug(); // LOOP 
        assertEquals(IP_ERROR, cpu.getRegisterValue("IP")[1], (byte) 0x02);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x02);
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[1], (byte) 0x01);

        cpu.startDebug(); // INC ax
        cpu.startDebug(); // LOOP 
        assertEquals(IP_ERROR, cpu.getRegisterValue("IP")[1], (byte) 0x05);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x03);
        assertEquals(CX_ERROR, cpu.getRegisterValue("CX")[1], (byte) 0x00);

        cpu.startDebug(); // HLT
    }

}
