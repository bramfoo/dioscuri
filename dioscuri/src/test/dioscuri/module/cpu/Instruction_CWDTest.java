package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Instruction_CWDTest extends AbstractInstructionTest {

    public Instruction_CWDTest() throws Exception {
        super(80448, "CWD.bin");
    }

    /*
    * Test method for 'com.tessella.emulator.module.cpu.Instruction_CBW.execute()'
    */
    @Test
    public void testExecute() {
        String AX_ERROR = "AX contains wrong value";
        String DX_ERROR = "DX contains wrong value";

        // Test byte to word extension
        cpu.startDebug();   // MOV AH, 0x80
        assertEquals(AX_ERROR, (byte) 0x80, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0x00, cpu.getRegisterValue("AX")[1]);
        cpu.startDebug();   // CWD
        assertEquals(DX_ERROR, (byte) 0xFF, cpu.getRegisterValue("DX")[0]);
        assertEquals(DX_ERROR, (byte) 0xFF, cpu.getRegisterValue("DX")[1]);

        cpu.startDebug();   // MOV AH, 0x7F
        assertEquals(AX_ERROR, (byte) 0x7F, cpu.getRegisterValue("AX")[0]);
        assertEquals(AX_ERROR, (byte) 0x00, cpu.getRegisterValue("AX")[1]);
        cpu.startDebug();   // CWD
        assertEquals(DX_ERROR, (byte) 0x00, cpu.getRegisterValue("DX")[0]);
        assertEquals(DX_ERROR, (byte) 0x00, cpu.getRegisterValue("DX")[1]);

        cpu.startDebug();   // HLT

    }

}
