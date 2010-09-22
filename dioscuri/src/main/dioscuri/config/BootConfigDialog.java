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

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import dioscuri.GUI;
import dioscuri.config.Emulator.Architecture.Modules.Bios.Bootdrives;
import dioscuri.module.Module;

/**
 *
 * @author Bram Lohman
 * @author Bart Kiers
 */
@SuppressWarnings("serial")
public class BootConfigDialog extends ConfigurationDialog {

    private JComboBox bootDrive1ComboBox;
    private JComboBox bootDrive2ComboBox;
    private JComboBox bootDrive3ComboBox;
    private JCheckBox floppyCheckDisabledCheckBox;

    dioscuri.config.Emulator emuConfig;

    /**
     *
     * @param parent -
     */
    public BootConfigDialog(GUI parent) {
        super(parent, "Boot Configuration", false, Module.Type.BOOT);
    }

    /**
     * Read in params from XML.
     */
    @Override
    protected void readInParams() {
        emuConfig = parent.getEmuConfig();
        Bootdrives boot = emuConfig.getArchitecture().getModules().getBios()
                .get(0).getBootdrives();

        String boot1Index = boot.getBootdrive0();
        String boot2Index = boot.getBootdrive1();
        String boot3Index = boot.getBootdrive2();
        boolean floppyCheckDisabled = emuConfig.getArchitecture().getModules()
                .getBios().get(0).isFloppycheckdisabled();

        this.bootDrive1ComboBox.setSelectedItem(boot1Index);
        this.bootDrive2ComboBox.setSelectedItem(boot2Index);
        this.bootDrive3ComboBox.setSelectedItem(boot3Index);
        this.floppyCheckDisabledCheckBox.setSelected(floppyCheckDisabled);

    }

    /**
     * Initialise the panel for data entry.
     */
    @Override
    protected void initMainEntryPanel() {

        JLabel boot1Label = new JLabel("  Select Boot Drive 1");
        JLabel boot2Label = new JLabel("  Select Boot Drive 2");
        JLabel boot3Label = new JLabel("  Select Boot Drive 3");
        JLabel floppyCheckDisabledLabel = new JLabel("  Floppy Check Disabled");

        this.populateControls();

        // Lay out the labels in a panel.

        mainEntryPanel = new JPanel(new GridLayout(10, 3));
        Border blackline = BorderFactory.createLineBorder(Color.black);
        mainEntryPanel.setBorder(blackline);

        // Fill start blanks in grid
        for (int i = 0; i < 3; i++) {
            mainEntryPanel.add(new JLabel(""));
        }

        mainEntryPanel.add(boot1Label);
        mainEntryPanel.add(bootDrive1ComboBox);
        mainEntryPanel.add(new JLabel(""));

        mainEntryPanel.add(boot2Label);
        mainEntryPanel.add(bootDrive2ComboBox);
        mainEntryPanel.add(new JLabel(""));

        mainEntryPanel.add(boot3Label);
        mainEntryPanel.add(bootDrive3ComboBox);
        mainEntryPanel.add(new JLabel(""));

        mainEntryPanel.add(floppyCheckDisabledLabel);
        mainEntryPanel.add(floppyCheckDisabledCheckBox);
        mainEntryPanel.add(new JLabel(""));

        // Fill end blanks in grid
        for (int i = 0; i < 15; i++) {
            mainEntryPanel.add(new JLabel(""));
        }

    }

    /**
     * Initalise the GUI Controls.
     */
    private void populateControls() {

        DefaultComboBoxModel bootModel1 = new DefaultComboBoxModel();
        bootModel1.addElement("Floppy Drive");
        bootModel1.addElement("Hard Drive");
        bootModel1.addElement("None");
        bootDrive1ComboBox = new JComboBox(bootModel1);
        bootDrive1ComboBox.setSelectedIndex(0);

        DefaultComboBoxModel bootModel2 = new DefaultComboBoxModel();
        bootModel2.addElement("Floppy Drive");
        bootModel2.addElement("Hard Drive");
        bootModel2.addElement("None");
        bootDrive2ComboBox = new JComboBox(bootModel2);
        bootDrive2ComboBox.setSelectedIndex(2);

        DefaultComboBoxModel bootModel3 = new DefaultComboBoxModel();
        bootModel3.addElement("Floppy Drive");
        bootModel3.addElement("Hard Drive");
        bootModel3.addElement("None");
        bootDrive3ComboBox = new JComboBox(bootModel3);
        bootDrive3ComboBox.setSelectedIndex(2);

        floppyCheckDisabledCheckBox = new JCheckBox();

    }

    /**
     * Get the params from the GUI.
     * 
     * @return object array of params.
     */
    @Override
    protected Emulator getParamsFromGui() {
        Bootdrives boot = emuConfig.getArchitecture().getModules().getBios()
                .get(0).getBootdrives();

        boot.setBootdrive0((String) bootDrive1ComboBox.getSelectedItem());
        boot.setBootdrive1((String) bootDrive2ComboBox.getSelectedItem());
        boot.setBootdrive2((String) bootDrive3ComboBox.getSelectedItem());
        emuConfig.getArchitecture().getModules().getBios().get(0)
                .setFloppycheckdisabled(
                        floppyCheckDisabledCheckBox.isSelected());

        return emuConfig;
    }

}
