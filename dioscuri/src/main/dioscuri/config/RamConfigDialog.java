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

/*
 * Information used in this module was taken from:
 * - http://en.wikipedia.org/wiki/AT_Attachment
 * - http://bochs.sourceforge.net/techspec/IDE-reference.txt
 */
package dioscuri.config;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import dioscuri.GUI;
import dioscuri.config.Emulator.Architecture.Modules.Memory;

/**
 *
 * @author Bram Lohman
 * @author Bart Kiers
 */
@SuppressWarnings("serial")
public class RamConfigDialog extends ConfigurationDialog {

    private JFormattedTextField sizeField;

    dioscuri.config.Emulator emuConfig;

    /**
     *
     * @param parent
     */
    public RamConfigDialog(GUI parent) {
        super(parent, "RAM Configuration", false, ModuleType.MEMORY);
    }

    /**
     * Read in params from XML.
     */
    @Override
    protected void readInParams() {
        emuConfig = parent.getEmuConfig();
        Memory mem = emuConfig.getArchitecture().getModules().getMemory();

        Integer sizeMb = mem.getSizemb().intValue();
        this.sizeField.setValue(sizeMb);

    }
    @Override
    protected void initSaveButton() {
        this.saveButton = new JButton("Save");
        this.saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveParams();
            }
        });
    }

    /**
     * Initialise the panel for data entry.
     */
    @Override
    protected void initMainEntryPanel() {

        JLabel sizeLabel = new JLabel("  Size (MB)");

        // Formats to format and parse numbers
        sizeField = new JFormattedTextField();
        sizeField.setValue(new Integer(0));
        sizeField.setColumns(10);

        mainEntryPanel = new JPanel(new GridLayout(10, 3));
        Border blackline = BorderFactory.createLineBorder(Color.black);
        mainEntryPanel.setBorder(blackline);

        // Fill first blanks in grid
        for (int i = 0; i < 3; i++) {
            mainEntryPanel.add(new JLabel(""));
        }

        mainEntryPanel.add(sizeLabel);
        mainEntryPanel.add(sizeField);
        mainEntryPanel.add(new JLabel(""));

        // Fill end blanks in grid
        for (int i = 0; i < 24; i++) {
            mainEntryPanel.add(new JLabel(""));
        }
    }

    /**
     * Get the params from the GUI.
     * 
     * @return object array of params.
     */
    @Override
    protected Emulator getParamsFromGui() {
        Memory mem = emuConfig.getArchitecture().getModules().getMemory();
        mem.setSizemb(BigDecimal.valueOf(((Number) sizeField.getValue())
                .intValue()));

        return emuConfig;
    }

}
