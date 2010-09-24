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
import dioscuri.interfaces.Module;
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
    /*
    private JCheckBox enabledCheckBox;
    private JFormattedTextField channelIndexFTextField;
    private JCheckBox masterCheckBox;
    private JCheckBox autoDetectCheckBox;
    private JFormattedTextField cylindersTextField;
    private JFormattedTextField headsTextField;
    private JFormattedTextField sectorsTextField;
    private JButton imageBrowseButton;
    */

    dioscuri.config.Emulator emuConfig;

    private HDPanel hdPanel1;
    private HDPanel hdPanel2;

    private JFormattedTextField updateIntField;

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
        Harddiskdrive hddConfig1 = emuConfig.getArchitecture().getModules().getAta().getHarddiskdrive().get(0);
        Harddiskdrive hddConfig2 = emuConfig.getArchitecture().getModules().getAta().getHarddiskdrive().get(1);

        Integer updateInt = emuConfig.getArchitecture().getModules().getAta().getUpdateintervalmicrosecs().intValue();
        this.updateIntField.setValue(updateInt);

        this.hdPanel1.enabledCheckBox.setSelected(hddConfig1.isEnabled());
        this.hdPanel1.channelIndexFTextField.setValue(hddConfig1.getChannelindex().intValue());
        this.hdPanel1.masterCheckBox.setSelected(hddConfig1.isMaster());
        this.hdPanel1.autoDetectCheckBox.setSelected(hddConfig1.isAutodetectcylinders());
        this.hdPanel1.cylindersTextField.setValue(hddConfig1.getCylinders().intValue());
        this.hdPanel1.headsTextField.setValue(hddConfig1.getHeads().intValue());
        this.hdPanel1.sectorsTextField.setValue(hddConfig1.getSectorspertrack().intValue());
        this.hdPanel1.imageFilePathLabel.setText(Utilities.resolvePathAsString(hddConfig1.getImagefilepath()));

        this.hdPanel2.enabledCheckBox.setSelected(hddConfig2.isEnabled());
        this.hdPanel2.channelIndexFTextField.setValue(hddConfig2.getChannelindex().intValue());
        this.hdPanel2.masterCheckBox.setSelected(hddConfig2.isMaster());
        this.hdPanel2.autoDetectCheckBox.setSelected(hddConfig2.isAutodetectcylinders());
        this.hdPanel2.cylindersTextField.setValue(hddConfig2.getCylinders().intValue());
        this.hdPanel2.headsTextField.setValue(hddConfig2.getHeads().intValue());
        this.hdPanel2.sectorsTextField.setValue(hddConfig2.getSectorspertrack().intValue());
        this.hdPanel2.imageFilePathLabel.setText(Utilities.resolvePathAsString(hddConfig2.getImagefilepath()));
    }

    /**
     * Initialise the panel for data entry.
     */
    @Override
    protected void initMainEntryPanel() {

        // Create controls
        this.populateControls();

        mainEntryPanel.setLayout(new BorderLayout(5, 5));

        JPanel top = new JPanel(new GridLayout(0, 3, 5, 5));
        top.add(new JLabel("Update Interval"));
        top.add(updateIntField);
        top.add(new JLabel("microseconds"));

        hdPanel1 = new HDPanel(this);
        hdPanel2 = new HDPanel(this);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("hard disk 1", null, hdPanel1, null);
        tabbedPane.addTab("hard disk 2", null, hdPanel2, null);

        mainEntryPanel.add(top, BorderLayout.NORTH);
        mainEntryPanel.add(tabbedPane, BorderLayout.CENTER);
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

        /*
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
        */
    }

    /**
     * Get the params from the GUI.
     * 
     * @return object array of params.
     */
    @Override
    protected Emulator getParamsFromGui() {

        Harddiskdrive hddConfig1 = emuConfig.getArchitecture().getModules().getAta().getHarddiskdrive().get(0);
        Harddiskdrive hddConfig2 = emuConfig.getArchitecture().getModules().getAta().getHarddiskdrive().get(1);

        emuConfig.getArchitecture().getModules().getAta().setUpdateintervalmicrosecs(BigInteger.valueOf((Integer)updateIntField.getValue()));

        hddConfig1.setEnabled(this.hdPanel1.enabledCheckBox.isSelected());
        hddConfig1.setChannelindex(BigInteger.valueOf((Integer)this.hdPanel1.channelIndexFTextField.getValue()));
        hddConfig1.setMaster(this.hdPanel1.masterCheckBox.isSelected());
        hddConfig1.setAutodetectcylinders(this.hdPanel1.autoDetectCheckBox.isSelected());
        hddConfig1.setCylinders(BigInteger.valueOf((Integer)this.hdPanel1.cylindersTextField.getValue()));
        hddConfig1.setHeads(BigInteger.valueOf((Integer)this.hdPanel1.headsTextField.getValue()));
        hddConfig1.setSectorspertrack(BigInteger.valueOf((Integer)this.hdPanel1.sectorsTextField.getValue()));
        hddConfig1.setImagefilepath(this.hdPanel1.selectedFile().getAbsoluteFile().toString());

        hddConfig2.setEnabled(this.hdPanel2.enabledCheckBox.isSelected());
        hddConfig2.setChannelindex(BigInteger.valueOf((Integer)this.hdPanel2.channelIndexFTextField.getValue()));
        hddConfig2.setMaster(this.hdPanel2.masterCheckBox.isSelected());
        hddConfig2.setAutodetectcylinders(this.hdPanel2.autoDetectCheckBox.isSelected());
        hddConfig2.setCylinders(BigInteger.valueOf((Integer)this.hdPanel2.cylindersTextField.getValue()));
        hddConfig2.setHeads(BigInteger.valueOf((Integer)this.hdPanel2.headsTextField.getValue()));
        hddConfig2.setSectorspertrack(BigInteger.valueOf((Integer)this.hdPanel2.sectorsTextField.getValue()));
        hddConfig2.setImagefilepath(this.hdPanel2.selectedFile().getAbsoluteFile().toString());

        return emuConfig;
    }

    static class HDPanel extends JPanel {

        final ConfigurationDialog owner;

        JLabel enabledLabel = new JLabel("Enabled");
        JLabel channelIndexLabel = new JLabel("Channel Index");
        JLabel masterLabel = new JLabel("Master");
        JLabel autoDetectLabel = new JLabel("Auto Detect");
        JLabel cylindersLabel = new JLabel("Cylinders");
        JLabel headsLabel = new JLabel("Heads");
        JLabel sectorsLabel = new JLabel("Sectors");
        JLabel imageFileLabel = new JLabel("Image File");

        JCheckBox enabledCheckBox;
        JFormattedTextField channelIndexFTextField;
        JCheckBox masterCheckBox;
        JCheckBox autoDetectCheckBox;
        JFormattedTextField cylindersTextField;
        JFormattedTextField headsTextField;
        JFormattedTextField sectorsTextField;
        JTextField imageFilePathLabel;
        JButton imageBrowseButton;

        HDPanel(ConfigurationDialog owner) {
            this.owner = owner;
            setLayout(new GridLayout(0, 3, 5, 5));
            setupGUI();
        }

        File selectedFile() {
            return new File(imageFilePathLabel.getText());
        }

        private void setupGUI() {

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
            imageFilePathLabel = new JTextField();
            imageBrowseButton = new JButton("Browse");
            imageBrowseButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    HDPanel.this.owner.launchFileChooser();
                }
            });

            // row 1
            add(enabledLabel);
            add(enabledCheckBox);
            add(new JLabel());

            // row 2
            add(channelIndexLabel);
            add(channelIndexFTextField);
            add(new JLabel());

            // row 3
            add(masterLabel);
            add(masterCheckBox);
            add(new JLabel());

            // row 4
            add(autoDetectLabel);
            add(autoDetectCheckBox);
            add(new JLabel());

            // row 5
            add(cylindersLabel);
            add(cylindersTextField);
            add(new JLabel());

            // row 6
            add(headsLabel);
            add(headsTextField);
            add(new JLabel());

            // row 7
            add(sectorsLabel);
            add(sectorsTextField);
            add(new JLabel());

            // row 8
            add(imageFileLabel);
            add(imageFilePathLabel);
            add(imageBrowseButton);
        }
    }
}
