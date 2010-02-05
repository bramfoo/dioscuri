package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class Instruction_AND_AXIvTest extends AbstractInstructionTest {
    
    public Instruction_AND_AXIvTest() throws Exception {
        super(80448, "AND_AXIv.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_AND_AXIv.execute()'
    */
    @Test
    public void testExecute() {
        String AX_ERROR = "AX contains wrong value";
        String SF_ERROR = "SF incorrect";
        String ZF_ERROR = "ZF incorrect";
        String PF_ERROR = "PF incorrect";

        // Execute DEC_AX
        cpu.startDebug();
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0xFF);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0xFF);
        // Execute AND AX, 5555
        cpu.startDebug();
        // TODO: Add Junit-addons for array comparison:
        //ArrayAssert.assertEquals(AX_ERROR, cpu.getRegisterValue("AX"), new byte[]{(byte)0x55, (byte)0x55});
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x55);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x55);
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        // Execute AND AL, AAAA
        cpu.startDebug();
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x00);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x00);
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        // Execute DEC_AX
        cpu.startDebug();
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0xFF);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0xFF);
        // Execute AND AL, 0101
        cpu.startDebug();
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x01);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x01);
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        assertFalse(PF_ERROR, cpu.getFlagValue('P'));
    }
}
