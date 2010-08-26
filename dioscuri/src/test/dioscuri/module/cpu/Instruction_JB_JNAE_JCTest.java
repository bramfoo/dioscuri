package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Bram Lohman\n@author Bart Kiers
 */
public class Instruction_JB_JNAE_JCTest extends AbstractInstructionTest {

    /**
     *
     * @throws Exception
     */
    public Instruction_JB_JNAE_JCTest() throws Exception {
        super(80448, "JB_JNAE_JC.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_JB_JNAE_JC.execute()'
    */
    /**
     *
     */
    @Test
    public void testExecute() {
        String IP_ERROR = "IP contains wrong value";
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

        // Test JB/JNAE/JC instruction
        cpu.startDebug(); // JB
        assertEquals(IP_ERROR, (byte) 0x01, cpu.getRegisterValue("IP")[0]);
        assertEquals(IP_ERROR, (byte) 0x02, cpu.getRegisterValue("IP")[1]);
        cpu.startDebug(); // JNAE
        assertEquals(IP_ERROR, (byte) 0x01, cpu.getRegisterValue("IP")[0]);
        assertEquals(IP_ERROR, (byte) 0x04, cpu.getRegisterValue("IP")[1]);
        cpu.startDebug(); // JC
        assertEquals(IP_ERROR, (byte) 0x01, cpu.getRegisterValue("IP")[0]);
        assertEquals(IP_ERROR, (byte) 0x06, cpu.getRegisterValue("IP")[1]);

        cpu.startDebug(); // CMC
        assertTrue(CF_ERROR, cpu.getFlagValue('C'));
        cpu.startDebug(); // JB -2          ; Jump to IP 00FE (start at 100), to test full IP range
        assertEquals(IP_ERROR, (byte) 0x00, cpu.getRegisterValue("IP")[0]);
        assertEquals(IP_ERROR, (byte) 0xFE, cpu.getRegisterValue("IP")[1]);

        // FIXME: Fix ADD instruction
//        cpu.startDebug(); // ADD instruction
//        cpu.startDebug(); // JB
//        assertEquals(IP_ERROR, (byte)0x01, cpu.getRegisterValue("IP")[0]);
//        assertEquals(IP_ERROR, (byte)0x09, cpu.getRegisterValue("IP")[1]);
//        cpu.startDebug(); // CLC
//        assertFalse(CF_ERROR, cpu.getFlagValue('C'));
//        cpu.startDebug(); // JMP
//        assertEquals(IP_ERROR, (byte)0x01, cpu.getRegisterValue("IP")[0]);
//        assertEquals(IP_ERROR, (byte)0x00, cpu.getRegisterValue("IP")[1]);
    }

}
