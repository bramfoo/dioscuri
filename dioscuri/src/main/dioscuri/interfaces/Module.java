package dioscuri.interfaces;

import java.util.Map;

public interface Module {

    /**
     * The Type of a Module.
     */
    enum Type {
        ATA,
        BOOT,
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
        VIDEO;

        /**
         * Returns the Type based on a given String.
         *
         * @param strType the String representation of the Type to be fetched.
         * @return        the Type based on a given String, 'strType', or null if
         *                'strType' is not present in the set of enums.
         */
        public static Type resolveType(String strType) {
            try {
                return Type.valueOf(strType.toUpperCase());
            } catch(Exception e) {
                return null;
            }
        }
    }

    /**
     * Returns the Module of a certain Type connected to this Module.
     *
     * @param type the Type of the Module to be fetched.
     * @return     the Module of a certain Type connected to this Module. 
     */
    Module getConnection(Type type);

    /**
     * Returns all connected, or supposedly connected, Modules of this Module.
     *
     * @return all connected, or supposedly connected, Modules of this Module.
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
     * Get all Module.Type's this AbstractModule is supposed to be connected to.
     *
     * @return an array of Module.Type's this AbstractModule is supposed to be connected to.
     */
    Type[] getExpectedConnections();

    /**
     * Returns the Type of this Module.
     *
     * @return the Type of this Module.
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
