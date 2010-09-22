package dioscuri.interfaces;

import java.util.Map;

public interface Module {

    /**
     * The Type of a AbstractModule.
     */
    enum Type {
        ATA,
        BOOT, // TODO: Really belongs here? Needed in config GUI...
        BIOS,
        CLOCK,
        CPU,
        DEVICE,
        DMA,
        DMACONTROLLER,
        DUMMY,
        FDC,
        KEYBOARD,
        MEMORY,
        MOTHERBOARD,
        MOUSE,
        PARALLELPORT,
        PCI,
        PIC,
        PIT,
        RTC,
        SCREEN,
        SERIALPORT,
        VIDEO
    }

    /**
     *
     * @param type
     * @return
     */
    Module getConnection(Type type);

    /**
     *
     * @return
     */
    Map<Type, Module> getConnections();

    /**
     * Returns the state of debug mode
     *
     * @return true if this module is in debug mode, false otherwise
     */
    boolean getDebugMode();

    /**
     * Return a dump of module status
     *
     * @return string containing a dump of this module
     */
    String getDump();

    /**
     * Get all AbstractModule.Type's this AbstractModule is supposed to be connected to.
     *
     * @return an array of AbstractModule.Type's this AbstractModule is supposed to be connected to.
     */
    String[] getExpectedConnections(); // TODO return type: AbstractModule.Type[]

    /**
     *
     * @return
     */
    Module.Type getType();

    /**
     * Checks if this module is connected to operate normally
     *
     * @return true if this module is connected successfully, false otherwise
     */
    boolean isConnected();

    /**
     * Reset all parameters of module.
     *
     * @return true iff the AbstractModule was reset properly.
     */
    boolean reset();

    /**
     * Connect both Modules 'this' and 'module' to each other.
     *
     * @param module the other AbstractModule.
     * @return       true iff both Modules 'this' and 'module' are
     *               properly connected to each other.
     */
    boolean setConnection(Module module);

    /**
     * Set toggle to define if this module is in debug mode or not
     *
     * @param status the new debug mode for this AbstractModule.
     */
    void setDebugMode(boolean status);

    /**
     * Starts the module to become active.
     */
    void start();

    /**
     * Stops the module from being active.
     */
    void stop();
}
