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

import dioscuri.GUI;
import dioscuri.config.Emulator.Architecture.Modules.Ata.Harddiskdrive;
import dioscuri.module.Module;
import dioscuri.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.math.BigInteger;

/**
 *
 * @author Bram Lohman
 * @author Bart Kiers
 */
@SuppressWarnings("serial")
public class AtaConfigDialog extends ConfigurationDialog {

    private JCheckBox enabledCheckBox;
    private JFormattedTextField channelIndexFTextField;
    private JCheckBox masterCheckBox;
    private JCheckBox autoDetectCheckBox;
    private JFormattedTextField cylindersTextField;
    private JFormattedTextField headsTextField;
    private JFormattedTextField sectorsTextField;

    private JFormattedTextField updateIntField;

    private JButton imageBrowseButton;
    dioscuri.config.Emulator emuConfig;

    /**
     *
     * @param parent the parent GUI
     */
    public AtaConfigDialog(GUI parent) {
        super(parent, "ATA Configuration", false, Module.Type.ATA);

    }

    /**
     * Read in params from XML.
     */
    @Override
    protected void readInParams() {

        emuConfig = parent.getEmuConfig();
        Harddiskdrive hddConfig = emuConfig.getArchitecture().getModules().getAta().getHarddiskdrive().get(0);

        Integer updateInt = emuConfig.getArchitecture().getModules().getAta().getUpdateintervalmicrosecs().intValue();
        boolean isEnabled = hddConfig.isEnabled();
        int channelIndex = hddConfig.getChannelindex().intValue();
        boolean isMaster = hddConfig.isMaster();
        boolean autoDetect = hddConfig.isAutodetectcylinders();
        int cylinders = hddConfig.getCylinders().intValue();
        int heads = hddConfig.getHeads().intValue();
        int sectors = hddConfig.getSectorspertrack().intValue();
        String imageFormatPath = Utilities.resolvePathAsString(hddConfig.getImagefilepath());

        this.updateIntField.setValue(updateInt);
        this.enabledCheckBox.setSelected(isEnabled);
        this.channelIndexFTextField.setValue(channelIndex);
        this.masterCheckBox.setSelected(isMaster);
        this.autoDetectCheckBox.setSelected(autoDetect);
        this.cylindersTextField.setValue(cylinders);
        this.headsTextField.setValue(heads);
        this.sectorsTextField.setValue(sectors);

        /*
        // Check if length of filepath is longer than 30 characters
        if (imageFormatPath.length() > 30) {
            // Trail off the beginning of the string
            this.imageFilePathLabel.setText("..."
                    + imageFormatPath.substring(imageFormatPath.length() - 30));
        } else {
            this.imageFilePathLabel.setText(imageFormatPath);
        }
        */
        
        this.imageFilePathLabel.setText(imageFormatPath);

        this.selectedFile = new File(imageFormatPath);
    }

    /**
     * Initialise the panel for data entry.
     */
    @Override
    protected void initMainEntryPanel() {
        // Create labels
        JLabel updateIntLabel = new JLabel("Update Interval");
        JLabel updateIntUnitLabel = new JLabel("microseconds");
        JLabel enabledLabel = new JLabel("Enabled");
        JLabel channelIndexLabel = new JLabel("Channel Index");
        JLabel masterLabel = new JLabel("Master");
        JLabel autoDetectLabel = new JLabel("Auto Detect");
        JLabel cylindersLabel = new JLabel("Cylinders");
        JLabel headsLabel = new JLabel("Heads");
        JLabel sectorsLabel = new JLabel("Sectors");
        JLabel imageFileLabel = new JLabel("Image File");

        // Create controls
        this.populateControls();

        mainEntryPanel.setLayout(new GridLayout(0, 3, 5, 5));

        // row 1
        mainEntryPanel.add(updateIntLabel);
        mainEntryPanel.add(updateIntField);
        mainEntryPanel.add(updateIntUnitLabel);

        // row 2
        mainEntryPanel.add(enabledLabel);
        mainEntryPanel.add(enabledCheckBox);
        mainEntryPanel.add(new JLabel());

        // row 3
        mainEntryPanel.add(channelIndexLabel);
        mainEntryPanel.add(channelIndexFTextField);
        mainEntryPanel.add(new JLabel());

        // row 4
        mainEntryPanel.add(masterLabel);
        mainEntryPanel.add(masterCheckBox);
        mainEntryPanel.add(new JLabel());

        // row 5
        mainEntryPanel.add(autoDetectLabel);
        mainEntryPanel.add(autoDetectCheckBox);
        mainEntryPanel.add(new JLabel());

        // row 6
        mainEntryPanel.add(cylindersLabel);
        mainEntryPanel.add(cylindersTextField);
        mainEntryPanel.add(new JLabel());

        // row 7
        mainEntryPanel.add(headsLabel);
        mainEntryPanel.add(headsTextField);
        mainEntryPanel.add(new JLabel());

        // row 8
        mainEntryPanel.add(sectorsLabel);
        mainEntryPanel.add(sectorsTextField);
        mainEntryPanel.add(new JLabel());

        // row 9
        mainEntryPanel.add(imageFileLabel);
        mainEntryPanel.add(imageFilePathLabel);
        mainEntryPanel.add(imageBrowseButton);
    }

    /**
     * Initalise the GUI Controls.
     */
    private void populateControls() {
        imageFilePathLabel = new JTextField("");
        imageFilePathLabel.setEditable(false);
        
        // Formats to format and parse numbers
        updateIntField = new JFormattedTextField();
        updateIntField.setValue(0);
        updateIntField.setColumns(10);

        enabledCheckBox = new JCheckBox();
        enabledCheckBox.setSelected(true);

        channelIndexFTextField = new JFormattedTextField();
        channelIndexFTextField.setValue(0);
        channelIndexFTextField.setColumns(10);
        cylindersTextField = new JFormattedTextField();
        cylindersTextField.setValue(0);
        cylindersTextField.setColumns(10);
        headsTextField = new JFormattedTextField();
        headsTextField.setValue(0);
        headsTextField.setColumns(10);
        sectorsTextField = new JFormattedTextField();
        sectorsTextField.setValue(0);
        sectorsTextField.setColumns(10);

        masterCheckBox = new JCheckBox();
        masterCheckBox.setSelected(true);

        autoDetectCheckBox = new JCheckBox();

        imageBrowseButton = new JButton("Browse");

        imageBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                launchFileChooser();
            }
        });

    }

    /**
     * Get the params from the GUI.
     * 
     * @return object array of params.
     */
    @Override
    protected Emulator getParamsFromGui() {

        Harddiskdrive hddConfig = emuConfig.getArchitecture().getModules()
                .getAta().getHarddiskdrive().get(0);

        emuConfig
                .getArchitecture()
                .getModules()
                .getAta()
                .setUpdateintervalmicrosecs(
                        BigInteger.valueOf((Integer) updateIntField.getValue()));
        hddConfig.setEnabled(enabledCheckBox.isSelected());
        hddConfig.setChannelindex(BigInteger
                .valueOf((Integer) channelIndexFTextField.getValue()));
        hddConfig.setMaster(masterCheckBox.isSelected());
        hddConfig.setAutodetectcylinders(autoDetectCheckBox.isSelected());
        hddConfig.setCylinders(BigInteger.valueOf((Integer) cylindersTextField
                .getValue()));
        hddConfig.setHeads(BigInteger.valueOf((Integer) headsTextField
                .getValue()));
        hddConfig.setSectorspertrack(BigInteger
                .valueOf((Integer) sectorsTextField.getValue()));
        hddConfig.setImagefilepath(selectedFile.getAbsoluteFile().toString());

        return emuConfig;
    }

}
