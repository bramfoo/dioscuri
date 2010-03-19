/* $Revision$ $Date$ $Author$ 
 * 
 * Copyright (C) 2007-2009  National Library of the Netherlands, 
 *                          Nationaal Archief of the Netherlands, 
 *                          Planets
 *                          KEEP
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 *
 * For more information about this project, visit
 * http://dioscuri.sourceforge.net/
 * or contact us via email:
 *   jrvanderhoeven at users.sourceforge.net
 *   blohman at users.sourceforge.net
 *   bkiers at users.sourceforge.net
 * 
 * Developed by:
 *   Nationaal Archief               <www.nationaalarchief.nl>
 *   Koninklijke Bibliotheek         <www.kb.nl>
 *   Tessella Support Services plc   <www.tessella.com>
 *   Planets                         <www.planets-project.eu>
 *   KEEP                            <www.keep-project.eu>
 * 
 * Project Title: DIOSCURI
 */

package dioscuri;

import dioscuri.config.ConfigController;
import dioscuri.config.Emulator.Architecture.Modules.Ata.Harddiskdrive;
import dioscuri.config.Emulator.Architecture.Modules.Bios;
import dioscuri.config.Emulator.Architecture.Modules.Bios.Bootdrives;
import dioscuri.config.Emulator.Architecture.Modules.Fdc.Floppy;
import dioscuri.config.ModuleType;
import dioscuri.exception.ModuleException;
import dioscuri.module.*;
import dioscuri.module.ata.ATA;
import dioscuri.module.ata.ATAConstants;
import dioscuri.module.ata.ATATranslationType;
import dioscuri.module.bios.BIOS;
import dioscuri.module.clock.Clock;
import dioscuri.module.cpu.CPU;
import dioscuri.module.cpu32.*;
import dioscuri.module.dma.DMA;
import dioscuri.module.fdc.FDC;
import dioscuri.module.keyboard.Keyboard;
import dioscuri.module.memory.DynamicAllocationMemory;
import dioscuri.module.memory.Memory;
import dioscuri.module.motherboard.DeviceDummy;
import dioscuri.module.motherboard.Motherboard;
import dioscuri.module.mouse.Mouse;
import dioscuri.module.parallelport.ParallelPort;
import dioscuri.module.pic.PIC;
import dioscuri.module.pit.PIT;
import dioscuri.module.rtc.RTC;
import dioscuri.module.screen.Screen;
import dioscuri.module.serialport.SerialPort;
import dioscuri.module.video.Video;
import dioscuri.util.Utilities;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Top class owning all classes of the emulator. Entry point
 * 
 */
public class Emulator implements Runnable {

    // Attributes
    private Modules modules;
    private ArrayList<HardwareComponent> hwComponents;
    private IO io;
    private GUI gui;
    /**
     *
     */
    protected dioscuri.config.Emulator emuConfig;
    /**
     *
     */
    protected dioscuri.config.Emulator.Architecture.Modules moduleConfig;

    // Toggles
    private boolean isAlive;
    private boolean coldStart;
    private boolean resetBusy;
    private boolean cpu32bit;
    private boolean dynamicMem;

    // Logging
    private static Logger logger = Logger.getLogger("dioscuri");

    // Constants
    // General commands
    /**
     *
     */
    protected final static int CMD_START = 0x00; // start the emulator
    /**
     *
     */
    protected final static int CMD_STOP = 0x01; // stop the emulator
    /**
     *
     */
    protected final static int CMD_RESET = 0x02; // stop the emulator
    /**
     *
     */
    protected final static int CMD_DEBUG = 0x04; // turn on debug mode
    /**
     *
     */
    protected final static int CMD_LOGGING = 0x05; // turn on logging
    /**
     *
     */
    protected final static int CMD_OBSERVE = 0x06; // turn on observation
    /**
     *
     */
    protected final static int CMD_LOAD_MODULES = 0x07; // load modules
    /**
     *
     */
    protected final static int CMD_LOAD_DATA = 0x08; // load initial data
                                                     // (program code)
    /**
     *
     */
    protected final static int CMD_LOGTOFILE = 0x09; // write logging
                                                     // information to file

    // Debug commands
    /**
     *
     */
    protected final static int CMD_DEBUG_HELP = 0x03; // show help information
    /**
     *
     */
    protected final static int CMD_DEBUG_STEP = 0x10; // execute one processor
                                                      // cycle
    /**
     *
     */
    protected final static int CMD_DEBUG_DUMP = 0x11; // show dump of observed
                                                      // modules
    /**
     *
     */
    protected final static int CMD_DEBUG_ENTER = 0x12; // enter given values at
                                                       // given memory address
    /**
     *
     */
    protected final static int CMD_DEBUG_STOP = 0x13; // stop the emulator
    /**
     *
     */
    protected final static int CMD_DEBUG_SHOWREG = 0x14; // Simple CPU register
                                                         // view
    /**
     *
     */
    protected final static int CMD_DEBUG_MEM_DUMP = 0x15; // Show contents of
                                                          // memory location(s)

    // Special case commands
    /**
     *
     */
    protected final static int CMD_MISMATCH = 0xFF; // command mismatch

    // Module status
    /**
     *
     */
    public final static int MODULE_FDC_TRANSFER_START = 0;
    /**
     *
     */
    public final static int MODULE_FDC_TRANSFER_STOP = 1;
    /**
     *
     */
    public static final int MODULE_ATA_HD1_TRANSFER_START = 2;
    /**
     *
     */
    public static final int MODULE_ATA_HD1_TRANSFER_STOP = 3;
    /**
     *
     */
    public static final int MODULE_KEYBOARD_NUMLOCK_ON = 4;
    /**
     *
     */
    public static final int MODULE_KEYBOARD_NUMLOCK_OFF = 5;
    /**
     *
     */
    public static final int MODULE_KEYBOARD_CAPSLOCK_ON = 6;
    /**
     *
     */
    public static final int MODULE_KEYBOARD_CAPSLOCK_OFF = 7;
    /**
     *
     */
    public static final int MODULE_KEYBOARD_SCROLLLOCK_ON = 8;
    /**
     *
     */
    public static final int MODULE_KEYBOARD_SCROLLLOCK_OFF = 9;

    // Constructors

    /**
     * Class constructor
     * 
     * @param owner
     *            graphical user interface (owner of emulation process)
     * 
     */
    public Emulator(GUI owner) {
        this.gui = owner;
        modules = null; // Created in createModules()
        hwComponents = new ArrayList<HardwareComponent>();

        // Create IO communication channel
        io = new IO();

        // Set isActive toggle on
        isAlive = true;
        coldStart = true;
        resetBusy = false;
        dynamicMem = false;
    }

    public void run() {
        // TODO: Perform semantic analysis of command (syntaxis is checked by IO
        // class)
        // Especially important when running with multiple threads!!!
        int instr = 0;
        int total = 0;
        int loop = 0;

        while (isAlive) {
            // Start emulator, start all threads

            // Get the module settings from the configuration file
            try {
                File configFile = new File(Utilities.resolvePathAsString(gui.getConfigFilePath()));
                if (!configFile.exists() || !configFile.canRead()) {
                    logger
                            .log(Level.WARNING,
                                    "[emu] No local config file accessible, using read-only jar settings");
                    InputStream fallBack = GUI.class
                            .getResourceAsStream(gui.getConfigFilePath());
                    emuConfig = ConfigController.loadFromXML(fallBack);
                    fallBack.close();
                } else {
                    emuConfig = ConfigController.loadFromXML(configFile);
                }
                moduleConfig = emuConfig.getArchitecture().getModules();
                logger.log(Level.INFO, "[emu] Config contents: "+ moduleConfig.getFdc().getFloppy().get(0).getImagefilepath());
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "[emu] Config file not readable: "
                        + ex.toString());
                return;
            }
            logger.log(Level.INFO, "[emu] Retrieved settings for modules");

            // Module creation
            boolean success = setupEmu();

            // Initialise modules
            // boolean success = this.configController.initModules(this);

            if (!success) {
                logger
                        .log(Level.SEVERE,
                                "[emu] Emulation process halted due to error initalizing the modules.");
                this.stop();
                return;
            }

            if (cpu32bit) {
                // 32-bit CPU processing
                logger.log(Level.INFO,
                        "[emu] Emulation process started (32-bit).");
                try {
                    AddressSpace addressSpace = null;
                    Processor cpu = (Processor) modules
                            .getModule(ModuleType.CPU.toString());

                    if (cpu.isProtectedMode())
                        addressSpace = (AddressSpace) hwComponents.get(0); // linearAddr
                    else
                        addressSpace = (AddressSpace) hwComponents.get(1); // physicalAddr

                    while (total < 15000) {
                        instr = addressSpace.execute(cpu, cpu
                                .getInstructionPointer());
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        total += instr;
                        loop++;
                    }

                    while (isAlive == true) {
                        instr = addressSpace.execute(cpu, cpu
                                .getInstructionPointer());
                        total += instr;
                        loop++;
                    }
                } catch (ModeSwitchException e) {
                    instr = 1;
                }
            } else {
                // 16-bit CPU processing
                logger.log(Level.INFO,
                        "[emu] Emulation process started (16-bit).");

                if (((ModuleCPU) modules.getModule("cpu")).getDebugMode() == false) {
                    // Start CPU process
                    ((ModuleCPU) modules.getModule("cpu")).start();

                    // Check if CPU terminated abnormally -> stop emulation process
                    if (((ModuleCPU) modules.getModule("cpu"))
                            .isAbnormalTermination() == true) {
                        logger
                                .log(Level.SEVERE,
                                        "[emu] Emulation process halted due to error in CPU module.");
                        this.stop();
                        return;
                    }

                    // Check if CPU calls a full shutdown of PC -> stop
                    // emulation process
                    if (((ModuleCPU) modules.getModule("cpu")).isShutdown() == true) {
                        logger
                                .log(Level.SEVERE,
                                        "[emu] Emulation process halted due to request for shutdown by CPU module.");
                        this.stop();
                        return;
                    }

                    // If non of previous conditions caused a full stop of the
                    // emulation process,
                    // Then continue with rebooting the system.
                } else {
                    // Show first upcoming instruction
                    ModuleCPU cpu = (ModuleCPU) modules.getModule("cpu");
                    logger.log(Level.INFO, cpu.getNextInstructionInfo());
                    while (isAlive == true) {
                        this.debug(io.getCommand());
                    }
                }

                // Wait until reset of modules is done (wait about 1 second...)
                // This can occur when another thread causes a reset of this
                // emulation process
                while (resetBusy == true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     *
     */
    protected void stop() {
        // End life of this emulation process
        isAlive = false;

        // Check if emulation process exists
        if (modules != null) {
            // Stop emulation process, stop all threads
            for (int i = 0; i < modules.size(); i++) {
                modules.getModule(i).stop();
            }
            logger.log(Level.INFO, "[emu] Emulation process stopped.");

            // Notify GUI that process has ended
            gui.notifyGUI(GUI.EMU_PROCESS_STOP);
        }
    }

    /**
     *
     */
    protected void reset() {
        // TODO: fix this approach by avoiding deadlock (by using synchronized)
        // Check if emulation process exists
        resetBusy = true;

        if (modules != null) {
            // Reset emulation process
            ((ModuleCPU) modules.getModule("cpu")).stop();

            coldStart = false;
            logger.log(Level.INFO, "[emu] Reset in progress...");
        }
        resetBusy = false;
    }

    /**
     *
     * @param command
     */
    protected void debug(int command) {
        // Debug commands
        switch (command) {
        case CMD_DEBUG_HELP:
            // Debug command -> show help
            io.showHelp();
            break;

        case CMD_DEBUG_STEP:
            // Debug command STEP -> execute 1 or n instructions
            ModuleCPU cpu = (ModuleCPU) modules.getModule("cpu");

            // Execute n number of instructions (or else 1 if no argument
            // supplied)
            String[] sNumber = io.getArguments();
            if (sNumber != null) {
                int totalInstructions = Integer.parseInt(sNumber[0]);
                for (int n = 0; n < totalInstructions; n++) {
                    cpu.start();
                }
            } else {
                // Execute only 1 instruction
                cpu.start();
            }

            // Show dump of next CPU instruction
            logger.log(Level.SEVERE, cpu.getNextInstructionInfo());
            break;

        case CMD_DEBUG_SHOWREG:
            // Debug command SHOWREG -> show CPU registers
            ModuleCPU cpu1 = (ModuleCPU) modules.getModule("cpu");

            // Show simple view of CPU registers and flags
            logger.log(Level.SEVERE, cpu1.dumpRegisters());
            break;

        case CMD_DEBUG_DUMP:
            // Debug command DUMP -> show dump of module on screen

            // Check if moduletype is given
            String[] moduleTypeArray = io.getArguments();
            if (moduleTypeArray != null) {
                String moduleType = moduleTypeArray[0];

                // Show dump of one single module requested
                Module mod = modules.getModule(moduleType);
                if (mod != null) {
                    // Show dump of module
                    logger.log(Level.INFO, mod.getDump());
                } else {
                    logger.log(Level.SEVERE, "[emu] Module not recognised.");
                }
            }
            break;

        case CMD_DEBUG_MEM_DUMP:
            // Debug command MEMORY_DUMP -> show contents of n bytes at memory
            // location y
            ModuleMemory mem = (ModuleMemory) modules.getModule("memory");

            // Show n bytes at memory x (or else 2 if no number of bytes is
            // supplied)
            String[] sArg = io.getArguments();
            int memAddress = Integer.parseInt(sArg[0]);
            int numBytes = 0;
            if (sArg.length == 2) {
                numBytes = Integer.parseInt(sArg[1]);
            } else {
                // Retrieve only 2 byte
                numBytes = 2;
            }

            try {
                for (int n = 0; n < numBytes; n++) {
                    logger.log(Level.SEVERE, "[emu] Value of [0x"
                            + Integer.toHexString(memAddress + n).toUpperCase()
                            + "]: 0x"
                            + Integer.toHexString(
                                    0x100 | mem.getByte(memAddress + n) & 0xFF)
                                    .substring(1).toUpperCase());
                }
            } catch (ModuleException e) {
                e.printStackTrace();
            }
            break;

        default:
            // No command match
            logger
                    .log(Level.SEVERE,
                            "[emu] No command match. Enter a correct emulator command.");
            break;
        }
    }

    /**
     * 
     * @param state
     */
    protected void setActive(boolean state) {
        // Define running state
        isAlive = state;
    }

    /**
     * Get the modules.
     * 
     * @return modules
     */
    public Modules getModules() {
        return this.modules;
    }

    /**
     * Get the hardware components.
     * 
     * @return modules
     */
    public ArrayList<HardwareComponent> getHWcomponents() {
        return this.hwComponents;
    }

    /**
     * Set the modules.
     * 
     * @param modules
     */
    public void setModules(Modules modules) {
        this.modules = modules;
    }

    /**
     * Get the gui.
     * 
     * @return gui
     */
    public GUI getGui() {
        return this.gui;
    }

    /**
     * Get the io.
     * 
     * @return io
     */
    public IO getIo() {
        return this.io;
    }

    /**
     * Get cold start.
     * 
     * @return coldStart
     */
    public boolean getColdStart() {
        return this.coldStart;
    }

    /**
     * Set cold start.
     * 
     * @param coldStart
     */
    public void setColdStart(boolean coldStart) {
        this.coldStart = coldStart;
    }

    /**
     * Return reference to module from given type
     * 
     * @param moduleType stating the type of the requested module
     * 
     * @return Module requested module, or null if module does not exist
     */
    protected Module getModule(String moduleType) {
        return modules.getModule(moduleType);
    }

    /**
     *
     * @param keyEvent
     * @param keyEventType
     */
    protected void notifyKeyboard(KeyEvent keyEvent, int keyEventType) {
        ModuleKeyboard keyboard = (ModuleKeyboard) modules
                .getModule("keyboard");
        if (keyboard != null) {
            keyboard.generateScancode(keyEvent, keyEventType);
        }
    }

    /**
     *
     * @param mouseEvent
     */
    protected void notifyMouse(MouseEvent mouseEvent) {
        ModuleMouse mouse = (ModuleMouse) modules.getModule("mouse");
        if (mouse != null) {
            mouse.mouseMotion(mouseEvent);
        }
    }

    /**
     *
     * @param driveLetter
     * @param carrierType
     * @param imageFile
     * @param writeProtected
     * @return -
     */
    protected boolean insertFloppy(String driveLetter, byte carrierType,
            File imageFile, boolean writeProtected) {
        ModuleFDC fdc = (ModuleFDC) modules.getModule("fdc");
        if (fdc != null) {
            return fdc.insertCarrier(driveLetter, carrierType, imageFile,
                    writeProtected);
        }
        return false;
    }

    /**
     *
     * @param driveLetter
     * @return -
     */
    protected boolean ejectFloppy(String driveLetter) {
        ModuleFDC fdc = (ModuleFDC) modules.getModule("fdc");
        if (fdc != null) {
            return fdc.ejectCarrier(driveLetter);
        }
        return false;
    }

    /**
     *
     * @param status
     */
    public void statusChanged(int status) {
        switch (status) {
        case MODULE_FDC_TRANSFER_START:
            gui.updateGUI(GUI.EMU_FLOPPYA_TRANSFER_START);
            break;

        case MODULE_FDC_TRANSFER_STOP:
            gui.updateGUI(GUI.EMU_FLOPPYA_TRANSFER_STOP);
            break;

        case MODULE_ATA_HD1_TRANSFER_START:
            gui.updateGUI(GUI.EMU_HD1_TRANSFER_START);
            break;

        case MODULE_ATA_HD1_TRANSFER_STOP:
            gui.updateGUI(GUI.EMU_HD1_TRANSFER_STOP);
            break;

        case MODULE_KEYBOARD_NUMLOCK_ON:
            gui.updateGUI(GUI.EMU_KEYBOARD_NUMLOCK_ON);
            break;

        case MODULE_KEYBOARD_NUMLOCK_OFF:
            gui.updateGUI(GUI.EMU_KEYBOARD_NUMLOCK_OFF);
            break;

        case MODULE_KEYBOARD_CAPSLOCK_ON:
            gui.updateGUI(GUI.EMU_KEYBOARD_CAPSLOCK_ON);
            break;

        case MODULE_KEYBOARD_CAPSLOCK_OFF:
            gui.updateGUI(GUI.EMU_KEYBOARD_CAPSLOCK_OFF);
            break;

        case MODULE_KEYBOARD_SCROLLLOCK_ON:
            gui.updateGUI(GUI.EMU_KEYBOARD_SCROLLLOCK_ON);
            break;

        case MODULE_KEYBOARD_SCROLLLOCK_OFF:
            gui.updateGUI(GUI.EMU_KEYBOARD_SCROLLLOCK_OFF);
            break;

        default:
            break;
        }
    }

    /**
     *
     * @return -
     */
    public String getScreenText() {
        // Request characters on screen from video module (if available)
        ModuleVideo video = (ModuleVideo) modules.getModule("video");
        if (video != null) {
            return video.getVideoBufferCharacters();
        }
        return null;
    }

    /**
     *
     * @return -
     */
    public BufferedImage getScreenImage() {
        // TODO: implement
        return null;
    }

    /**
     *
     * @return -
     */
    public boolean isCpu32bit() {
        return cpu32bit;
    }

    /**
     *
     * @return -
     */
    public boolean setupEmu() {
        boolean result = true;

        if (coldStart) {
            // Cold start (hard reset)
            logger.log(Level.INFO,
                    "===================    COLD START   ===================");

            // Create modules
            logger.log(Level.INFO,
                    "=================== CREATE MODULES  ===================");
            result &= createModules();

            // Connect modules together
            logger.log(Level.INFO,
                    "=================== CONNECT MODULES ===================");
            result &= connectModules();

            // Set timers in relevant modules
            logger.log(Level.INFO,
                    "===================   INIT TIMERS   ===================");
            result &= setTimingParams(modules.getModule("cpu"));
            result &= setTimingParams(modules.getModule("video"));
            result &= setTimingParams(modules.getModule("pit"));
            result &= setTimingParams(modules.getModule("keyboard"));
            result &= setTimingParams(modules.getModule("fdc"));
            result &= setTimingParams(modules.getModule("ata"));
        } else {
            // Warm start (soft reset)
            logger.log(Level.INFO,
                    "===================    WARM START   ===================");

            // Make sure next time cold start will happen (FIXME: why?)
            setColdStart(true);
        }

        // Reset all modules
        logger.log(Level.INFO,
                "===================  RESET MODULES  ===================");
        result &= resetModules();
        if (cpu32bit) {
            // FIXME: Need to re-establish I/O port connections with motherboard
            // (?)
        }

        // Initialise screen (if available)
        logger.log(Level.INFO,
                "=================== INIT OUTPUT DEVICES ===================");
        result &= initScreenOutputDevice();

        // Initialise mouse (if available)
        logger.log(Level.INFO,
                "================== INIT INPUT DEVICES =================");
        result &= setMouseParams();
        result &= setMemoryParams();

        // Load system and video BIOS
        logger.log(Level.INFO,
                "===================    LOAD BIOS    ===================");
        result &= loadBIOS();

        // Set storage device settings
        logger.log(Level.INFO,
                "================= LOAD STORAGE MEDIA ==================");
        result &= setFloppyParams(); // At least one floppy is required
        setHardDriveParams(); // Harddisk not required, so doesn't influence
                              // result

        // Set other settings
        logger.log(Level.INFO,
                "===================  MISC SETTINGS  ===================");
        result &= setBootParams();
        if (!cpu32bit) {
            result &= setDebugMode();
        }

        // Print ready status
        logger.log(Level.INFO,
                "================= READY FOR EXECUTION =================");

        return result;

    }

    /**
     *
     * @return -
     */
    public boolean createModules() {
        modules = new Modules(20);

        // Determine CPU type -- assuming a CPU exists in this emulator (pretty
        // pointless without one, really)
        cpu32bit = moduleConfig.getCpu().isCpu32Bit();

        this.getGui().setCpyTypeLabel(cpu32bit ? "32 bit" : "16 bit");

        // Add clock first, as it is needed for 32-bit RAM
        Clock clk = new Clock(this);
        modules.addModule(clk);

        // Create a CPU
        if (cpu32bit) {
            // Add JPC 32-bit processor
            modules.addModule(new Processor());
        } else {
            // Add Dioscuri 16-bit CPU
            modules.addModule(new CPU(this));
        }

        // Create RAM
        if (cpu32bit) {       
            PhysicalAddressSpace physicalAddr = new PhysicalAddressSpace();
            LinearAddressSpace linearAddr = new LinearAddressSpace();
            for (int i = 0; i < PhysicalAddressSpace.SYS_RAM_SIZE; i += AddressSpace.BLOCK_SIZE)
                physicalAddr.allocateMemory(i, new LazyMemory(
                        AddressSpace.BLOCK_SIZE, clk));

            hwComponents.add(linearAddr);
            hwComponents.add(physicalAddr);
        } else {
            // Only add Dioscuri memory if using 16-bit processor

            // Choose between full array or dynamically allocated
            if (dynamicMem)
                modules.addModule(new DynamicAllocationMemory(this));
            else
                modules.addModule(new Memory(this));
        }

        if (moduleConfig.getBios() != null) {
            for (Bios bios : moduleConfig.getBios()) {
                logger.log(Level.CONFIG, "System bios file: "
                        + bios.getSysbiosfilepath()
                        + "; starting at 0x"
                        + Integer.toHexString(bios
                                .getRamaddresssysbiosstartdec().intValue()));
                logger.log(Level.CONFIG, "VGA bios file: "
                        + bios.getVgabiosfilepath()
                        + "; starting at 0x"
                        + Integer.toHexString(bios
                                .getRamaddressvgabiosstartdec().intValue()));
            }
            if (!cpu32bit) {
                modules.addModule(new BIOS(this));
            }
        }

        modules.addModule(new Motherboard(this)); // Motherboard should always
                                                  // be initialised before other
                                                  // I/O devices!!! Else I/O
                                                  // address space will be reset
        modules.addModule(new PIC(this)); // PIC should always be initialised
                                          // directly after motherboard

        if (moduleConfig.getPit() != null) {
            modules.addModule(new PIT(this));
        }

        modules.addModule(new RTC(this)); // RTC should always be initialised
                                          // before other devices

        if (moduleConfig.getAta() != null) {
            modules.addModule(new ATA(this));
        }

        // DMA
        if (cpu32bit) {
            DMAController primaryDMA, secondaryDMA;
            primaryDMA = new DMAController(false, true);
            secondaryDMA = new DMAController(false, false);
            hwComponents.add(primaryDMA);
            hwComponents.add(secondaryDMA);
        } else {
            modules.addModule(new DMA(this));
        }

        if (moduleConfig.getFdc() != null) {
            modules.addModule(new FDC(this));
        }

        if (moduleConfig.getKeyboard() != null) {
            modules.addModule(new Keyboard(this));
        }

        if (moduleConfig.getMouse() != null) {
            modules.addModule(new Mouse(this)); // Mouse always requires a
                                                // keyboard (controller)
        }

        modules.addModule(new ParallelPort(this));
        modules.addModule(new SerialPort(this));

        if (moduleConfig.getVideo() != null) {
            modules.addModule(new Video(this));
        }

        modules.addModule(new DeviceDummy(this));
        modules.addModule(new Screen(this));

        logger.log(Level.INFO, "[emu] All modules are created.");

        return true;
    }

    /**
     * Connect the modules together.
     * @return -
     */
    public boolean connectModules() {

        boolean result = true;

        Module mod1, mod2;
        for (int i = 0; i < modules.size(); i++) {
            mod1 = modules.getModule(i);
            String[] connections = mod1.getConnection();
            for (int c = 0; c < connections.length; c++) {
                mod2 = modules.getModule(connections[c]);
                if (mod2 != null) {
                    if (mod1.setConnection(mod2)) {
                        logger.log(Level.CONFIG,
                                "[emu] Successfully established connection between "
                                        + mod1.getType() + " and "
                                        + mod2.getType());
                    } else {
                        logger.log(Level.SEVERE,
                                "[emu] Failed to establish connection between "
                                        + mod1.getType() + " and "
                                        + mod2.getType());
                    }
                } else {
                    logger.log(Level.SEVERE,
                            "[emu] Failed to establish connection between "
                                    + mod1.getType() + " and unknown module "
                                    + connections[c]);
                }
            }
        }

        // Check if all modules are connected
        boolean isConnected = true;
        for (int i = 0; i < modules.size(); i++) {
            if (!(modules.getModule(i).isConnected())) {
                isConnected = false;
                logger.log(Level.SEVERE, "[emu] Could not connect module: "
                        + modules.getModule(i).getType() + ".");
            }
        }
        if (isConnected == false) {
            logger
                    .log(Level.SEVERE,
                            "[emu] Not all modules are connected. Emulator may be unstable.");
            result &= false;
        } else {
            logger.log(Level.INFO,
                    "[emu] All modules are successfully connected.");
            result &= true;
        }

        // 32-bit setup requires specific connections along with those processed
        // above
        if (cpu32bit) {

            Processor cpu = (Processor) modules.getModule(ModuleType.CPU
                    .toString());
            Video vid = (Video) modules.getModule(ModuleType.VGA.toString());
            Motherboard mb = (Motherboard) modules
                    .getModule(ModuleType.MOTHERBOARD.toString());
            FDC fdc = (FDC) modules.getModule(ModuleType.FDC.toString());
            // TODO: remove hardcoded hwComponents index values
            LinearAddressSpace linearAddr = (LinearAddressSpace) hwComponents
                    .get(0);
            PhysicalAddressSpace physicalAddr = (PhysicalAddressSpace) hwComponents
                    .get(1);
            DMAController primaryDMA = (DMAController) hwComponents.get(2);
            DMAController secondaryDMA = (DMAController) hwComponents.get(3);
            PIC pic = (PIC) modules.getModule(ModuleType.PIC.toString());

            IOPortHandler ioports = new IOPortHandler();
            ioports.setConnection(mb);

            cpu.acceptComponent(linearAddr);
            cpu.acceptComponent(physicalAddr);
            cpu.acceptComponent(ioports);
            cpu.setConnection(pic);

            physicalAddr.acceptComponent(linearAddr);
            linearAddr.acceptComponent(physicalAddr);

            vid.acceptComponent(physicalAddr);

            primaryDMA.acceptComponent(physicalAddr);
            secondaryDMA.acceptComponent(physicalAddr);
            primaryDMA.acceptComponent(ioports);
            secondaryDMA.acceptComponent(ioports);

            fdc.acceptComponent(primaryDMA);
            fdc.acceptComponent(secondaryDMA); // This isn't necessary...

            // Because 32-bit components may not match with 16-bit list,
            // overwrite result to true
            result = true;
        }

        return result;
    }

    /**
     * Set the timing parameters
     * @param module
     * @return -
     */
    public boolean setTimingParams(Module module) {
        boolean result = true;

        // There are 3 types of timing: speed (cpu), clock rate (pit), update
        // intervals (floppy/keyboard/ata/video/)

        if (module instanceof ModuleCPU) {
            int mhz = moduleConfig.getCpu().getSpeedmhz().intValue();
            ((ModuleCPU) module).setIPS(mhz * 1000000);
            result &= true;
        } else if (module instanceof ModuleDevice) {
            // Cast the module to ModuleDevice for the setUpdate
            if (module instanceof ModulePIT)
                ((ModuleDevice) module).setUpdateInterval(moduleConfig.getPit()
                        .getClockrate().intValue());
            else if (module instanceof ModuleVideo)
                ((ModuleDevice) module).setUpdateInterval(moduleConfig
                        .getVideo().getUpdateintervalmicrosecs().intValue());
            else if (module instanceof ModuleKeyboard)
                ((ModuleDevice) module).setUpdateInterval(moduleConfig
                        .getKeyboard().getUpdateintervalmicrosecs().intValue());
            else if (module instanceof ModuleFDC)
                ((ModuleDevice) module).setUpdateInterval(moduleConfig.getFdc()
                        .getUpdateintervalmicrosecs().intValue());
            else if (module instanceof ModuleATA)
                ((ModuleDevice) module).setUpdateInterval(moduleConfig.getAta()
                        .getUpdateintervalmicrosecs().intValue());
            result &= true;
        } else {
            // Unhandled module timing
            result &= false;
        }
        return result;
    }

    /**
     * Reset all modules.
     * @return -
     */
    public boolean resetModules() {
        boolean result = true;
        for (int i = 0; i < modules.size(); i++) {
            if (!(modules.getModule(i).reset())) {
                result = false;
                logger.log(Level.SEVERE, "[emu] Could not reset module: "
                        + modules.getModule(i).getType() + ".");
            }
        }
        if (result == false) {
            logger
                    .log(Level.SEVERE,
                            "[emu] Not all modules are reset. Emulator may be unstable.");
        } else {
            logger.log(Level.INFO, "[emu] All modules are successfully reset.");
        }

        return result;
    }

    /**
     * Init Screen Output Device.
     * @return -
     */
    public boolean initScreenOutputDevice() {

        // Set screen output
        // Connect screen (check if screen is available)
        ModuleScreen screen = (ModuleScreen) modules
                .getModule(ModuleType.SCREEN.toString());
        if (screen != null) {
            getGui().setScreen(screen.getScreen());
            return true;
        } else {
            logger.log(Level.WARNING, "[emu] No screen available.");
            return false;
        }
    }

    /**
     * Read from config and set mouse parameters
     * @return -
     */
    public boolean setMouseParams() {

        Mouse mouse = (Mouse) modules.getModule(ModuleType.MOUSE.toString());
        if (mouse != null) {

            // Init mouse enabled
            boolean enabled = moduleConfig.getMouse().isEnabled();

            mouse.setMouseEnabled(enabled);

            if (enabled == true) {
                gui.setMouseEnabled();
                gui.updateGUI(GUI.EMU_DEVICES_MOUSE_ENABLED);
            } else {
                gui.setMouseDisabled();
                gui.updateGUI(GUI.EMU_DEVICES_MOUSE_DISABLED);
            }

            // Init mouse type
            String type = moduleConfig.getMouse().getMousetype();
            mouse.setMouseType(type);
        }
        return true;
    }

    /**
     * Read from config and set memory parameters
     * @return -
     */
    public boolean setMemoryParams() {
        ModuleMemory mem = (ModuleMemory) modules.getModule(ModuleType.MEMORY
                .toString());

        if (mem != null) {
            int size = moduleConfig.getMemory().getSizemb().intValue();
            mem.setRamSizeInMB(size);
        }
        return true;
    }

    /**
     * Load the BIOS into memory
     * @return -
     */
    public boolean loadBIOS() {
        boolean result = true;

        if (cpu32bit) {
            SystemBIOS sysBIOS;
            VGABIOS vgaBIOS;
            try {
                // open input stream
                BufferedInputStream bdis = new BufferedInputStream(
                        new DataInputStream(new FileInputStream(new File(Constants.BOCHS_BIOS))));

                // read all bytes (as unsigned) in byte array
                byte[] byteArray = new byte[bdis.available()];
                bdis.read(byteArray, 0, byteArray.length);

                // Close input stream
                bdis.close();

                Clock clk = (Clock) modules.getModule(ModuleType.CLOCK
                        .toString());
                sysBIOS = new SystemBIOS(byteArray, clk);

                // open input stream
                BufferedInputStream bdis2 = new BufferedInputStream(
                        new DataInputStream(new FileInputStream(new File(Constants.VGA_BIOS))));

                // read all bytes (as unsigned) in byte array
                byte[] byteArray2 = new byte[bdis2.available()];
                bdis2.read(byteArray2, 0, byteArray2.length);

                // Close input stream
                bdis2.close();

                vgaBIOS = new VGABIOS(byteArray2, clk);

                // FIXME: Move to connect method (but: catch-22, as need to load
                // BIOSes first!)
                PhysicalAddressSpace physicalAddr = (PhysicalAddressSpace) hwComponents
                        .get(1);
                sysBIOS.acceptComponent(physicalAddr);
                vgaBIOS.acceptComponent(physicalAddr);

                // Add to emulator HWcomponents array
                hwComponents.add(sysBIOS);
                hwComponents.add(vgaBIOS);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                result &= false;
            }

            return result;
        } else {
            BIOS bios = (BIOS) modules.getModule(ModuleType.BIOS.toString());

            // FIXME: Do iteration over BIOS list???
            String sysBiosFilePath = Utilities.resolvePathAsString(moduleConfig.getBios().get(0).getSysbiosfilepath());
            String vgaBiosFilePath = Utilities.resolvePathAsString(moduleConfig.getBios().get(0).getVgabiosfilepath());

            int ramAddressSysBiosStart = moduleConfig.getBios().get(0).getRamaddresssysbiosstartdec().intValue();
            int ramAddressVgaBiosStart = moduleConfig.getBios().get(0).getRamaddressvgabiosstartdec().intValue();

            try {
                // Fetch System BIOS binaries from file system and store it in
                // BIOS ROM
                if (bios.setSystemBIOS(getIo().importBinaryStream(
                        sysBiosFilePath))) {
                    logger.log(Level.CONFIG,
                            "[emu] System BIOS successfully stored in ROM.");

                    // Retrieve System BIOS and store it in RAM
                    ModuleMemory mem = (ModuleMemory) modules
                            .getModule(ModuleType.MEMORY.toString());

                    mem.setBytes(ramAddressSysBiosStart, bios.getSystemBIOS());

                    logger.log(Level.CONFIG,
                            "[emu] System BIOS successfully loaded in RAM.");
                } else {
                    logger
                            .log(Level.SEVERE,
                                    "[emu] Not able to retrieve System BIOS binaries from file system.");
                    result &= false;

                }
            } catch (ModuleException emod) {
                logger.log(Level.SEVERE, emod.getMessage());
                result &= false;
            } catch (IOException eio) {
                logger.log(Level.SEVERE, eio.getMessage());
                result &= false;
            }

            // Module BIOS: load Video BIOS in BIOS ROM
            try {
                // Fetch System BIOS binaries from file system and store it in
                // BIOS ROM
                if (bios.setVideoBIOS(getIo().importBinaryStream(
                        vgaBiosFilePath))) {
                    logger.log(Level.CONFIG,
                            "[emu] Video BIOS successfully stored in ROM.");

                    // Retrieve VGA BIOS and store it in RAM at address 0xC0000
                    ModuleMemory mem = (ModuleMemory) modules
                            .getModule(ModuleType.MEMORY.toString());
                    mem.setBytes(ramAddressVgaBiosStart, bios.getVideoBIOS());
                    logger.log(Level.CONFIG,
                            "[emu] Video BIOS successfully loaded in RAM.");
                } else {
                    logger
                            .log(Level.SEVERE,
                                    "[emu] Not able to retrieve Video BIOS binaries from file system.");
                    result &= false;
                }
            } catch (ModuleException emod) {
                logger.log(Level.SEVERE, emod.getMessage());
                result &= false;
            } catch (IOException eio) {
                logger.log(Level.SEVERE, eio.getMessage());
                result &= false;
            }

            return result;

        }
    }

    /**
     * Get and set floppy parameters
     * @return -
     */
    public boolean setFloppyParams() {
        // Module FDC: set number of drives (max 4), insert floppy and set
        // update interval
        ModuleFDC fdc = (ModuleFDC) modules
                .getModule(ModuleType.FDC.toString());

        // FIXME: This needs to be set depending on number of floppies defined
        // in XML/HashMap (also see note on HashMaps)
        fdc.setNumberOfDrives(1);

        // FIXME: loop on number of floppies defined
        for (int i = 0; i < 1; i++) {
            Floppy floppyConfig = moduleConfig.getFdc().getFloppy().get(i);
            // boolean enabled = floppyConfig.isEnabled();
            boolean inserted = floppyConfig.isInserted();
            String driveLetter = floppyConfig.getDriveletter();
            String diskformat = floppyConfig.getDiskformat();
            byte carrierType = 0x0;
            boolean writeProtected = floppyConfig.isWriteprotected();
            String imageFilePath = Utilities.resolvePathAsString(floppyConfig.getImagefilepath());

            if (diskformat.equals("360K"))
                carrierType = 0x01;
            else if (diskformat.equals("1.2M"))
                carrierType = 0x02;
            else if (diskformat.equals("720K"))
                carrierType = 0x03;
            else if (diskformat.equals("1.44M"))
                carrierType = 0x04;
            else if (diskformat.equals("2.88M"))
                carrierType = 0x05;
            else if (diskformat.equals("160K"))
                carrierType = 0x06;
            else if (diskformat.equals("180K"))
                carrierType = 0x07;
            else if (diskformat.equals("320K"))
                carrierType = 0x08;
            else
                logger.log(Level.SEVERE,
                        "[emu] Floppy disk format not recognised.");

            if (inserted) {

                File imageFile = new File(imageFilePath);

                fdc.insertCarrier(driveLetter, carrierType, imageFile,
                        writeProtected);

                // TODO: updates for B ?
                if (driveLetter.equals("A")) {
                    getGui().updateGUI(GUI.EMU_FLOPPYA_INSERT);
                }
            }

        }
        return true;

    }

    /**
     * Read and set the hard drive parameters
     * @return -
     */
    public boolean setHardDriveParams() {
        // TODO: replace ATA reference by ModuleATA

        // FIXME: This needs to be set depending on number of floppies defined
        // in XML/HashMap (also see note on HashMaps)
        ModuleATA ata = (ATA) modules.getModule(ModuleType.ATA.toString());

        if (moduleConfig.getAta().getHarddiskdrive().isEmpty())
            return false;

        // FIXME: loop on number of disks defined
        for (int i = 0; i < 1; i++) {
            Harddiskdrive hddConfig = moduleConfig.getAta().getHarddiskdrive().get(0);
            boolean enabled = hddConfig.isEnabled();
            int ideChannelIndex = hddConfig.getChannelindex().intValue();
            boolean isMaster = hddConfig.isMaster();
            boolean autoDetectCylinders = hddConfig.isAutodetectcylinders();
            int numCylinders = hddConfig.getCylinders().intValue();
            int numHeads = hddConfig.getHeads().intValue();
            int numSectorsPerTrack = hddConfig.getSectorspertrack().intValue();

            //String imageFilePath = hddConfig.getImagefilepath();
            String imageFilePath = Utilities.resolvePathAsString(hddConfig.getImagefilepath());

            if (enabled && ideChannelIndex >= 0 && ideChannelIndex < 4) {
                if (autoDetectCylinders) {
                    numCylinders = 0;
                }

                ata.initConfig(ideChannelIndex, isMaster, true, false,
                        numCylinders, numHeads, numSectorsPerTrack,
                        ATATranslationType.AUTO, imageFilePath);

                // TODO: updates for other hard drives?
                if (ideChannelIndex == 0 && isMaster) {
                    getGui().updateGUI(GUI.EMU_HD1_INSERT);
                }
            }
        }
        return true;
    }

    /**
     * Read from config and set the boot params.
     * @return -
     */
    public boolean setBootParams() {

        // TODO: This is currently done via ATA, perhaps it can be generalised
        // to BIOS/CMOS?
        ATA ata = (ATA) modules.getModule(ModuleType.ATA.toString());

        // These values are currently stored in the BIOS, and bootdrives
        // FIXME: Do iteration of BIOS???
        Bios bios = moduleConfig.getBios().get(0);
        Bootdrives bootdrives = bios.getBootdrives();

        boolean floppyCheckDisabled = bios.isFloppycheckdisabled();
        int[] bootDrives = new int[3];

        // FIXME: Change to list in XML, ataCmos, so can iterate properly over
        // XML and arraylist
        // This currently gets an award for 'ugliest hack'
        for (int i = 0; i < bootDrives.length; i++) {
            String name = "none";
            if (i == 0) {
                name = bootdrives.getBootdrive0();
            }
            if (i == 1) {
                name = bootdrives.getBootdrive1();
            }
            if (i == 2) {
                name = bootdrives.getBootdrive2();
            }

            if (name.equalsIgnoreCase("Hard Drive")) {
                bootDrives[i] = ATAConstants.BOOT_DISKC;

            } else if (name.equalsIgnoreCase("Floppy Drive")) {
                bootDrives[i] = ATAConstants.BOOT_FLOPPYA;
            } else if (name.equalsIgnoreCase("cd")) {
                bootDrives[i] = ATAConstants.BOOT_CDROM;

            } else if (name.equalsIgnoreCase("none")) {
                bootDrives[i] = ATAConstants.BOOT_NONE;
            }
        }

        // Control CMOS settings
        ata.setCmosSettings(bootDrives, floppyCheckDisabled);

        return true;

    }

    /**
     * Set the debug mode.
     * @return -
     */
    public boolean setDebugMode() {
        boolean result = true;

        boolean isDebugMode = emuConfig.isDebug();

        // If debug mode is on set all modules in debug mode
        if (isDebugMode) {
            for (int i = 0; i < modules.size(); i++) {
                modules.getModule(i).setDebugMode(true);
            }
            logger.log(Level.INFO, "[emu] All modules in debug mode.");

            return result;

        }

        // Set debug for modules individually
        modules.getModule(ModuleType.ATA.toString()).setDebugMode(
                moduleConfig.getAta().isDebug());
        modules.getModule(ModuleType.CPU.toString()).setDebugMode(
                moduleConfig.getCpu().isDebug());
        modules.getModule(ModuleType.MEMORY.toString()).setDebugMode(
                moduleConfig.getMemory().isDebug());
        modules.getModule(ModuleType.FDC.toString()).setDebugMode(
                moduleConfig.getFdc().isDebug());
        modules.getModule(ModuleType.PIT.toString()).setDebugMode(
                moduleConfig.getPit().isDebug());
        modules.getModule(ModuleType.KEYBOARD.toString()).setDebugMode(
                moduleConfig.getKeyboard().isDebug());
        modules.getModule(ModuleType.MOUSE.toString()).setDebugMode(
                moduleConfig.getMouse().isDebug());
        modules.getModule(ModuleType.VGA.toString()).setDebugMode(
                moduleConfig.getVideo().isDebug());

        // Set RAM address watch
        boolean memDebug = moduleConfig.getMemory().isDebug();
        int memAddress = moduleConfig.getMemory().getDebugaddressdecimal()
                .intValue();

        if (moduleConfig.getMemory().isDebug()) {
            ((ModuleMemory) modules.getModule(ModuleType.MEMORY.toString()))
                    .setWatchValueAndAddress(memDebug, memAddress);
            logger.log(Level.CONFIG, "[emu] RAM address watch set to "
                    + memDebug + "; address: " + memAddress);
        }

        return true;
    }

}
