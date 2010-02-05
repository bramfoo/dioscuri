package dioscuri.module.cpu;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Instruction_CBWTest extends AbstractInstructionTest {

    public Instruction_CBWTest() throws Exception {
        super(80448, "CBW.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_CBW.execute()'
    */
    @Test
    public void testExecute() {
        String AX_ERROR = "AX contains wrong value";

        // Test byte to word extension
        cpu.startDebug();    // MOV AL, 0x80
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x80);
        cpu.startDebug();    // CBW
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0xFF);

        cpu.startDebug();    // MOV AL, 0x7F
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x7F);
        cpu.startDebug();    // CBW
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x00);

        cpu.startDebug();    // HLT

    }


}
