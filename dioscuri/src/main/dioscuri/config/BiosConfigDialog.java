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
import dioscuri.config.Emulator.Architecture.Modules.Bios;
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
public class BiosConfigDialog extends ConfigurationDialog {

    private JFormattedTextField sysBiosStartsFTextField;
    private JFormattedTextField vgaBiosStartFTextField;

    private JButton sysBiosBrowseButton;
    private JButton vgaBiosBrowseButton;

    private File selectedVgaFile = null;
    private JLabel imageFilePathVgaLabel = null;

    dioscuri.config.Emulator emuConfig;

    /**
     *
     * @param parent
     */
    public BiosConfigDialog(GUI parent) {
        super(parent, "BIOS Configuration", false, ModuleType.BIOS);
    }

    /**
     * Read in params from XML.
     */
    @Override
    protected void readInParams() {
        emuConfig = parent.getEmuConfig();
        Bios bios = emuConfig.getArchitecture().getModules().getBios().get(0);

        String sysPath = Utilities.resolvePathAsString(bios.getSysbiosfilepath());
        String vgaPath = Utilities.resolvePathAsString(bios.getVgabiosfilepath());

        Integer ramSysBiosAddressStart = bios.getRamaddresssysbiosstartdec()
                .intValue();
        Integer ramVgaBiosAddressStart = bios.getRamaddressvgabiosstartdec()
                .intValue();

        this.sysBiosStartsFTextField.setValue(ramSysBiosAddressStart);
        this.vgaBiosStartFTextField.setValue(ramVgaBiosAddressStart);

        this.selectedfile = new File(sysPath);
        this.selectedVgaFile = new File(vgaPath);

        // Check if length of System BIOS file is longer than 30 characters
        if (selectedfile.getName().length() > 30) {
            // Trail off the beginning of the string
            this.imageFilePathLabel.setText("..."
                    + selectedfile.getName().substring(
                            selectedfile.getName().length() - 30));
        } else {
            this.imageFilePathLabel.setText(selectedfile.getName());
        }

        // Check if length of Video BIOS file is longer than 30 characters
        if (selectedVgaFile.getName().length() > 30) {
            // Trail off the beginning of the string
            this.imageFilePathVgaLabel.setText("..."
                    + selectedVgaFile.getName().substring(
                            selectedVgaFile.getName().length() - 30));
        } else {
            this.imageFilePathVgaLabel.setText(selectedVgaFile.getName());
        }
    }

    /**
     * Initialise the panel for data entry.
     */
    @Override
    protected void initMainEntryPanel() {

        JLabel sysBiosFileLabel = new JLabel("Sys Bios File");
        JLabel vgaBiosFileLabel = new JLabel("Vga Bios File");
        JLabel sysBiosStartLabel = new JLabel("Sys Bios Start");
        JLabel vgaBiosStartLabel = new JLabel("Vga Bios Start");

        this.populateControls();

        // Lay out the labels in a panel.
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        mainEntryPanel = new JPanel(gridbag);

        // Makeup layout with contraints
        // General layout contraints
        c.fill = GridBagConstraints.HORIZONTAL; // Make the component fill its
                                                // display area entirely
        c.insets = new Insets(0, 10, 0, 10); // Defines the spaces between
                                             // layout and display area

        // Row 1
        c.weightx = 1.0;
        c.gridwidth = 1;
        gridbag.setConstraints(sysBiosFileLabel, c);
        mainEntryPanel.add(sysBiosFileLabel);
        c.gridwidth = GridBagConstraints.RELATIVE; // next-to-last in row
        gridbag.setConstraints(this.imageFilePathLabel, c);
        mainEntryPanel.add(this.imageFilePathLabel);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        gridbag.setConstraints(this.sysBiosBrowseButton, c);
        mainEntryPanel.add(this.sysBiosBrowseButton);

        // Row 2
        c.gridwidth = 1;
        gridbag.setConstraints(vgaBiosFileLabel, c);
        mainEntryPanel.add(vgaBiosFileLabel);
        c.gridwidth = GridBagConstraints.RELATIVE; // next-to-last in row
        gridbag.setConstraints(this.imageFilePathVgaLabel, c);
        mainEntryPanel.add(this.imageFilePathVgaLabel);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        gridbag.setConstraints(this.vgaBiosBrowseButton, c);
        mainEntryPanel.add(this.vgaBiosBrowseButton);

        // Row 3
        c.gridwidth = 1;
        gridbag.setConstraints(sysBiosStartLabel, c);
        mainEntryPanel.add(sysBiosStartLabel);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        gridbag.setConstraints(this.sysBiosStartsFTextField, c);
        mainEntryPanel.add(this.sysBiosStartsFTextField);

        // Row 4
        c.gridwidth = 1;
        gridbag.setConstraints(vgaBiosStartLabel, c);
        mainEntryPanel.add(vgaBiosStartLabel);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        gridbag.setConstraints(this.vgaBiosStartFTextField, c);
        mainEntryPanel.add(this.vgaBiosStartFTextField);
    }

    /**
     * Initalise the GUI Controls.
     */
    private void populateControls() {
        this.imageFilePathLabel = new JLabel("");
        this.imageFilePathVgaLabel = new JLabel("");

        this.sysBiosStartsFTextField = new JFormattedTextField();
        this.sysBiosStartsFTextField.setValue(new Integer(0));
        this.sysBiosStartsFTextField.setColumns(10);

        this.vgaBiosStartFTextField = new JFormattedTextField();
        this.vgaBiosStartFTextField.setValue(new Integer(0));
        this.vgaBiosStartFTextField.setColumns(10);

        this.sysBiosBrowseButton = new JButton("Browse");
        this.sysBiosBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                launchFileChooser();
            }
        });

        this.vgaBiosBrowseButton = new JButton("Browse");
        this.vgaBiosBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                launchFileChooserVga();
            }
        });

    }

    /**
     * Launch a file chooser to select VGA file.
     * 
     */
    private void launchFileChooserVga() {
        JFileChooser fileChooser = new JFileChooser();

        int returnVal = fileChooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            selectedVgaFile = fileChooser.getSelectedFile();

            String filePath = selectedVgaFile.getName();

            imageFilePathVgaLabel.setText("   " + filePath);

        } else {

        }
    }

    /**
     * Get the params from the GUI.
     * 
     * @return object array of params.
     */
    @Override
    protected Emulator getParamsFromGui() {
        Bios bios = emuConfig.getArchitecture().getModules().getBios().get(0);

        bios.setSysbiosfilepath(selectedfile.getAbsoluteFile().toString());
        bios.setVgabiosfilepath(selectedVgaFile.getAbsoluteFile().toString());
        bios.setRamaddresssysbiosstartdec(BigInteger
                .valueOf((Integer) sysBiosStartsFTextField.getValue()));
        bios.setRamaddressvgabiosstartdec(BigInteger
                .valueOf((Integer) vgaBiosStartFTextField.getValue()));

        return emuConfig;
    }

}
