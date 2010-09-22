package dioscuri;

import dioscuri.config.ConfigController;
import dioscuri.module.Module;
import dioscuri.module.cpu.CPU;
import dioscuri.module.memory.Memory;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Logger;

/**
 *
 * @author Bram Lohman\n@author Bart Kiers
 */
public abstract class AbstractInstructionTest {

    /**
     *
     */
    public static Logger logger = Logger.getLogger(AbstractInstructionTest.class.getClass().getName());

    /**
     *
     */
    public final Emulator emu;
    /**
     *
     */
    public final CPU cpu;
    /**
     *
     */
    public final Memory mem;

    private final String rootTestASMfiles = "src/test-asm/";

    /**
     * @param startAddress
     * @param testASMfilename
     * @throws Exception
     */
    public AbstractInstructionTest(final int startAddress, final String testASMfilename) throws Exception {
        // initialize Emulator, Memory and CPU 
        emu = new Emulator(new DummyGUI());
        emu.emuConfig = ConfigController.loadFromXML(new File(Constants.DEFAULT_CONFIG_XML));
        emu.moduleConfig = emu.emuConfig.getArchitecture().getModules();
        emu.setupEmu();
        mem = (Memory)emu.getModule(Module.Type.MEMORY);
        cpu = (CPU)emu.getModule(Module.Type.CPU);
        cpu.setDebugMode(true);

        // load the assembly test in an InputStream
        BufferedInputStream bis = new BufferedInputStream(new DataInputStream(new FileInputStream(new File(rootTestASMfiles + testASMfilename))));
        byte[] byteArray = new byte[bis.available()];
        bis.read(byteArray, 0, byteArray.length);
        bis.close();

        // set the assembly at the specified 'startAddress' 
        mem.setBytes(startAddress, byteArray);
    }

    /*
     * ...
     */

    class DummyGUI implements GUI {

        @Override
        public JFrame asJFrame() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public dioscuri.config.Emulator getEmuConfig() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getConfigFilePath() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void notifyGUI(int emuProcess) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void updateGUI(int activity) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void setScreen(JPanel screen) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean setMouseEnabled() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean setMouseDisabled() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void setCpyTypeLabel(String cpuType) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

}
