package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Bram Lohman\n@author Bart Kiers
 */
public class Instruction_CBWTest extends AbstractInstructionTest {

    /**
     *
     * @throws Exception
     */
    public Instruction_CBWTest() throws Exception {
        super(80448, "CBW.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_CBW.execute()'
    */
    /**
     *
     */
    @Test
    public void testExecute() {
        String AX_ERROR = "AX contains wrong value";

        // Test byte to word extension
        cpu.startDebug();    // MOV AL, 0x80
        assertEquals(AX_ERROR, (byte) 0x80, cpu.getRegisterValue("AX")[1]);
        cpu.startDebug();    // CBW
        assertEquals(AX_ERROR, (byte) 0xFF, cpu.getRegisterValue("AX")[0]);

        cpu.startDebug();    // MOV AL, 0x7F
        assertEquals(AX_ERROR, (byte) 0x7F, cpu.getRegisterValue("AX")[1]);
        cpu.startDebug();    // CBW
        assertEquals(AX_ERROR, (byte) 0x00, cpu.getRegisterValue("AX")[0]);

        cpu.startDebug();    // HLT

    }


}
