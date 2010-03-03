/* $Revision: $ $Date: $ $Author: $ 
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

import dioscuri.config.Emulator;

import javax.swing.*;

/**
 *
 * @author Bram Lohman
 * @author Bart Kiers
 */
public interface GUI {

    // Dimension settings
    /**
     *
     */
    static final int GUI_X_LOCATION = 200;
    /**
     *
     */
    static final int GUI_Y_LOCATION = 200;

    // GUI update activities
    /**
     *
     */
    static final int EMU_PROCESS_START = 0;
    /**
     *
     */
    static final int EMU_PROCESS_STOP = 1;
    /**
     *
     */
    static final int EMU_PROCESS_RESET = 2;
    /**
     *
     */
    static final int EMU_FLOPPYA_INSERT = 3;
    /**
     *
     */
    static final int EMU_FLOPPYA_EJECT = 4;
    /**
     *
     */
    static final int EMU_HD1_INSERT = 5;
    /**
     *
     */
    static final int EMU_HD1_EJECT = 6;
    /**
     *
     */
    static final int EMU_HD1_TRANSFER_START = 7;
    /**
     *
     */
    static final int EMU_HD1_TRANSFER_STOP = 8;
    /**
     *
     */
    static final int EMU_KEYBOARD_NUMLOCK_ON = 9;
    /**
     *
     */
    static final int EMU_KEYBOARD_NUMLOCK_OFF = 10;
    /**
     *
     */
    static final int EMU_KEYBOARD_CAPSLOCK_ON = 11;
    /**
     *
     */
    static final int EMU_KEYBOARD_CAPSLOCK_OFF = 12;
    /**
     *
     */
    static final int EMU_KEYBOARD_SCROLLLOCK_ON = 13;
    /**
     *
     */
    static final int EMU_KEYBOARD_SCROLLLOCK_OFF = 14;
    /**
     *
     */
    static final int EMU_FLOPPYA_TRANSFER_START = 15;
    /**
     *
     */
    static final int EMU_FLOPPYA_TRANSFER_STOP = 16;
    /**
     *
     */
    static final int EMU_DEVICES_MOUSE_ENABLED = 17;
    /**
     *
     */
    static final int EMU_DEVICES_MOUSE_DISABLED = 18;
    /**
     *
     */
    static final int GUI_RESET = 99;

    // Key events
    /**
     *
     */
    static final int KEY_PRESSED = 0;
    /**
     *
     */
    static final int KEY_RELEASED = 1;
    /**
     *
     */
    static final int KEY_TYPED = 2;

    /**
     *
     * @return -
     */
    JFrame asJFrame();

    /**
     *
     * @return -
     */
    Emulator getEmuConfig();

    /**
     *
     * @return -
     */
    String getConfigFilePath();

    /**
     *
     * @param emuProcess
     */
    void notifyGUI(int emuProcess);

    /**
     *
     * @param activity
     */
    void updateGUI(int activity);

    /**
     *
     * @param screen
     */
    void setScreen(JPanel screen);

    /**
     *
     * @return -
     */
    boolean setMouseEnabled();

    /**
     *
     * @return -
     */
    boolean setMouseDisabled();

    /**
     * Display the CPU type (16 or 32 bit)
     * @param cpuType the CPU type   
     */
    void setCpyTypeLabel(String cpuType);
}
