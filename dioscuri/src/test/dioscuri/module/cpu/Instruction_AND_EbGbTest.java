package dioscuri.module.cpu;

import dioscuri.AbstractInstructionTest;
import org.junit.Test;

import java.util.logging.Level;

import static org.junit.Assert.*;

public class Instruction_AND_EbGbTest extends AbstractInstructionTest {

    public Instruction_AND_EbGbTest() throws Exception {
        super(80448, "AND_EbGb.bin");
    }

    /*
     * Test method for 'com.tessella.emulator.module.cpu.Instruction_AND_EbGb.execute()'
     */
    @Test
    public void testExecute() {
        String AX_ERROR = "AX contains wrong value";
        String CX_ERROR = "CX contains wrong value";
        String SF_ERROR = "SF incorrect";
        String ZF_ERROR = "ZF incorrect";
        String PF_ERROR = "PF incorrect";

        // Load registers, memory with prearranged values (4 instructions)
        logger.log(Level.INFO, "\n>>> Before:\n" + cpu.getDump() + "\nregisters:\n" + cpu.registerDump());
        cpu.startDebug();    // MOV AX, 0xFFFF
        cpu.startDebug();    // MOV [0000], AX
        cpu.startDebug();    // MOV AX, 0x0000
        cpu.startDebug();    // MOV CX, 0xAA55
        logger.log(Level.INFO, "\n>>> After:\n" + cpu.getDump() + "\nregisters:\n" + cpu.registerDump());
        assertEquals(CX_ERROR, (byte) 0xAA, cpu.getRegisterValue("CX")[0]);
        assertEquals(CX_ERROR, (byte) 0x55, cpu.getRegisterValue("CX")[1]);

        // AND mem, reg
        cpu.startDebug();    // AND [BX+SI], CL
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        cpu.startDebug();    // MOV AL, [0000]
        assertEquals(AX_ERROR, (byte) 0x55, cpu.getRegisterValue("AX")[1]);

        // AND mem+8b,reg
        cpu.startDebug();    // AND [BX+DI+01], CH
        assertTrue(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        cpu.startDebug();    // MOV AL, [0001]
        assertEquals(AX_ERROR, (byte) 0xAA, cpu.getRegisterValue("AX")[1]);

        // AND mem+16b,reg
        cpu.startDebug();    // INC BP
        cpu.startDebug();    // AND [BP+0x0100], CH
        assertTrue(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        assertFalse(ZF_ERROR, cpu.getFlagValue('Z'));
        cpu.startDebug();    // MOV AL, [0x0101]
        assertEquals(AX_ERROR, (byte) 0xAA, cpu.getRegisterValue("AX")[1]);

        // AND reg, reg
        cpu.startDebug();    // AND AL, CL
        assertFalse(SF_ERROR, cpu.getFlagValue('S'));
        assertTrue(PF_ERROR, cpu.getFlagValue('P'));
        assertTrue(ZF_ERROR, cpu.getFlagValue('Z'));
        assertEquals(AX_ERROR, (byte) 0x00, cpu.getRegisterValue("AX")[1]);
        cpu.startDebug();    // HLT
    }
}
