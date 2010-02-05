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

public interface GUI {

    // Constants

    // Emulator characteristics.
    public final static String EMULATOR_NAME = "Dioscuri - modular emulator for digital preservation";
    public final static String EMULATOR_VERSION = "0.4.3";
    public final static String EMULATOR_DATE = "January, 2010";
    public final static String EMULATOR_CREATOR = "Koninklijke Bibliotheek (KB), Nationaal Archief of the Netherlands, Planets, KEEP";
    public final static String CONFIG_DIR = "config";
    public final static String EMULATOR_ICON_IMAGE = CONFIG_DIR+"/dioscuri_icon.gif";
    public final static String EMULATOR_SPLASHSCREEN_IMAGE = CONFIG_DIR+"/dioscuri_splashscreen_2010_v043.gif";
    public final static String EMULATOR_LOGGING_PROPERTIES = CONFIG_DIR+"/logging.properties";
    public final static String CONFIG_XML = CONFIG_DIR+"/DioscuriConfig.xml";

    // Dimension settings
    public static final int GUI_X_LOCATION = 200;
    public static final int GUI_Y_LOCATION = 200;

    // GUI update activities
    public static final int EMU_PROCESS_START = 0;
    public static final int EMU_PROCESS_STOP = 1;
    public static final int EMU_PROCESS_RESET = 2;
    public static final int EMU_FLOPPYA_INSERT = 3;
    public static final int EMU_FLOPPYA_EJECT = 4;
    public static final int EMU_HD1_INSERT = 5;
    public static final int EMU_HD1_EJECT = 6;
    public static final int EMU_HD1_TRANSFER_START = 7;
    public static final int EMU_HD1_TRANSFER_STOP = 8;
    public static final int EMU_KEYBOARD_NUMLOCK_ON = 9;
    public static final int EMU_KEYBOARD_NUMLOCK_OFF = 10;
    public static final int EMU_KEYBOARD_CAPSLOCK_ON = 11;
    public static final int EMU_KEYBOARD_CAPSLOCK_OFF = 12;
    public static final int EMU_KEYBOARD_SCROLLLOCK_ON = 13;
    public static final int EMU_KEYBOARD_SCROLLLOCK_OFF = 14;
    public static final int EMU_FLOPPYA_TRANSFER_START = 15;
    public static final int EMU_FLOPPYA_TRANSFER_STOP = 16;
    public static final int EMU_DEVICES_MOUSE_ENABLED = 17;
    public static final int EMU_DEVICES_MOUSE_DISABLED = 18;
    public static final int GUI_RESET = 99;

    // Key events
    public static final int KEY_PRESSED = 0;
    public static final int KEY_RELEASED = 1;
    public static final int KEY_TYPED = 2;

    public JFrame asJFrame();

    public boolean saveXML(Emulator params);

    public Emulator getEmuConfig();

    public String getConfigFilePath();

    public void notifyGUI(int emuProcess);

    public void updateGUI(int activity);

    public void setScreen(JPanel screen);

    public boolean setMouseEnabled();

    public boolean setMouseDisabled();
}
