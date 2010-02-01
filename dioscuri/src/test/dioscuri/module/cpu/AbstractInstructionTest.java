package dioscuri.module.cpu;

import dioscuri.DummyGUI;
import dioscuri.Emulator;
import dioscuri.module.memory.Memory;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

public abstract class AbstractInstructionTest {

    final Emulator emu;
    final CPU cpu;
    final Memory mem;

    final String rootTestASMfiles = "src/test-asm/";

    /**
     * 
     * @param startAddress
     * @param testASMfilename
     * @throws Exception
     */
    public AbstractInstructionTest(final int startAddress, final String testASMfilename) throws Exception {
        // initialize Emulator, Memory and CPU 
        emu = new Emulator(new DummyGUI());
        emu.setupEmu();
        mem = (Memory)emu.getModules().getModule("memory");
        cpu = (CPU)emu.getModules().getModule("cpu");
        cpu.setDebugMode(true);

        // load the assembly test in an InputStream
        BufferedInputStream bis = new BufferedInputStream(new DataInputStream(new FileInputStream(new File(rootTestASMfiles + testASMfilename))));
        byte[] byteArray = new byte[bis.available()];
        bis.read(byteArray, 0, byteArray.length);
        bis.close();

        // set the assembly at the specified 'startAddress' 
        mem.setBytes(startAddress, byteArray);
    }
}
