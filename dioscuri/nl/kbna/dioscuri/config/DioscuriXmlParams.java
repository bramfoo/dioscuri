/*
 * $Revision: 1.4 $ $Date: 2008-02-12 11:57:30 $ $Author: jrvanderhoeven $
 * 
 * Copyright (C) 2007  National Library of the Netherlands, Nationaal Archief of the Netherlands
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information about this project, visit
 * http://dioscuri.sourceforge.net/
 * or contact us via email:
 * jrvanderhoeven at users.sourceforge.net
 * blohman at users.sourceforge.net
 * 
 * Developed by:
 * Nationaal Archief               <www.nationaalarchief.nl>
 * Koninklijke Bibliotheek         <www.kb.nl>
 * Tessella Support Services plc   <www.tessella.com>
 *
 * Project Title: DIOSCURI
 *
 */

package nl.kbna.dioscuri.config;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class DioscuriXmlParams
{

    public final static String EMULATOR_NODE = "emulator";
    public final static String BOOT_DRIVES_NODE = "bootdrives";
    public final static String FLOPPY_CHECK_DISABLED_NODE = "floppycheckdisabled";

    // Nodes
    private final static String BOOT_NODE = "boot";
    private final static String BIOS_NODE = "bios";
    private final static String CPU_NODE = "cpu";
    private final static String RAM_NODE = "memory";
    private final static String KEYBOARD_NODE = "keyboard";
    private final static String MOUSE_NODE = "mouse";
    private final static String PIT_NODE = "pit";
    private final static String FLOPPY_DISK_DRIVES_NODE = "fdc";
    private final static String ATA_DRIVES_NODE = "ata";
    private final static String VIDEO_ADAPTER_NODE = "video";

    // Node variables
    public final static String UPDATE_INTERVAL_TEXT = "updateintervalmicrosecs";
    public final static String CPU_SPEED_MHZ_TEXT = "speedmhz";
    public final static String CPU_32_BIT_TEXT = "cpu32bit";
    public final static String PIT_CLOCKRATE_TEXT = "clockrate";
    public final static String RAM_SIZE_TEXT = "sizemb";
    public final static String MOUSE_ENABLED_TEXT = "enabled";
    public final static String MOUSE_TYPE_TEXT = "mousetype";
    public final static String DEBUG_TEXT = "debug";
    public final static String FLOPPY = "floppy";
    public final static String HARDDISKDRIVE = "harddiskdrive";

    public final static String RAM_ADDRESS_TEXT = "debugaddressdecimal";

    public static Node getModuleNode(Document document, ModuleType moduleType)
    {

        Node theNode = null;
        
        if (moduleType == ModuleType.BOOT)
        {
            theNode = document.getElementsByTagName(BOOT_NODE).item(0);

        } else if (moduleType == ModuleType.BIOS)
        {
            theNode = document.getElementsByTagName(BIOS_NODE).item(0);

        }
        else if (moduleType == ModuleType.CPU)
        {
            theNode = document.getElementsByTagName(CPU_NODE).item(0);

        }
        else if (moduleType == ModuleType.MEMORY)
        {
            theNode = document.getElementsByTagName(RAM_NODE).item(0);

        }
        else if (moduleType == ModuleType.PIT)
        {
            theNode = document.getElementsByTagName(PIT_NODE).item(0);

        }
        else if (moduleType == ModuleType.KEYBOARD)
        {
            theNode = document.getElementsByTagName(KEYBOARD_NODE).item(0);

        }
        else if (moduleType == ModuleType.MOUSE)
        {
            theNode = document.getElementsByTagName(MOUSE_NODE).item(0);

        }
        else if (moduleType == ModuleType.FDC)
        {
            theNode = document.getElementsByTagName(FLOPPY_DISK_DRIVES_NODE).item(0);

        }
        else if (moduleType == ModuleType.ATA)
        {
            theNode = document.getElementsByTagName(ATA_DRIVES_NODE).item(0);

        }
        else if (moduleType == ModuleType.VGA)
        {
            theNode = document.getElementsByTagName(VIDEO_ADAPTER_NODE).item(0);
        }

        return theNode;

    }
}
