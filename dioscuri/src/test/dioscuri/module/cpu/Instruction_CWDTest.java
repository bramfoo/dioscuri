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
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x80);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x00);
        cpu.startDebug();   // CWD
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[0], (byte) 0xFF);
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[1], (byte) 0xFF);

        cpu.startDebug();   // MOV AH, 0x7F
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[0], (byte) 0x7F);
        assertEquals(AX_ERROR, cpu.getRegisterValue("AX")[1], (byte) 0x00);
        cpu.startDebug();   // CWD
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[0], (byte) 0x00);
        assertEquals(DX_ERROR, cpu.getRegisterValue("DX")[1], (byte) 0x00);

        cpu.startDebug();   // HLT

    }

}
