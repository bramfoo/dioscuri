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
import dioscuri.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 *
 * @author Bram Lohman
 * @author Bart Kiers
 */
@SuppressWarnings("serial")
public abstract class ConfigurationDialog extends JDialog {
    protected JPanel mainEntryPanel;
    protected JPanel statusPanel;
    protected JButton doButton;
    protected JButton okButton;
    protected JButton cancelButton;
    protected JLabel imageFilePathLabel;
    protected File selectedfile;
    protected int dialogWidth;
    protected int dialogHeight;
    protected int dialogXPosition;
    protected int dialogYPosition;
    protected GUI parent;
    protected boolean isMainConfigScreen;
    protected ModuleType moduleType;
    public ConfigurationDialog() {
    }

    /**
     *
     * @param parent
     * @param title
     * @param isMainConfigScreen
     * @param moduleType
     */
    public ConfigurationDialog(GUI parent, String title,
            boolean isMainConfigScreen, ModuleType moduleType) {
        super(parent.asJFrame(), title, true);
        this.moduleType = moduleType;

        this.initComponents(parent, isMainConfigScreen);

    }

    private void initComponents(GUI parent, boolean isMainConfigScreen) {

        this.parent = parent;
        this.isMainConfigScreen = isMainConfigScreen;

        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        this.setLayout(new BorderLayout());

        // Set dimensions
        dialogWidth = 600;
        if (!isMainConfigScreen) {
            dialogWidth -= 200;
        }

        dialogHeight = 315;

        dialogXPosition = 250;
        dialogYPosition = 250;

        if (!isMainConfigScreen) {
            dialogXPosition += 100;
            dialogYPosition += 100;
        }

        // Build frame
        this.setLocation(0, 0);

        Dimension dialogDim = new Dimension(dialogWidth, dialogHeight);
        this.setSize(dialogDim);
        this.setPreferredSize(dialogDim);
        this.setResizable(false);

        this.setLocation(dialogXPosition, dialogYPosition);

        mainEntryPanel = new JPanel();
        statusPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 0, 0));

        Dimension statusDim = new Dimension(dialogWidth, 50);
        statusPanel.setSize(statusDim);
        statusPanel.setPreferredSize(statusDim);

        // Border blackline = BorderFactory.createLineBorder(Color.black);
        // statusPanel.setBorder(blackline);

        this.initBottomButtonPanel();
        this.initMainEntryPanel();

        if (!isMainConfigScreen) {
            this.readInParams();
        }

        // Add panels to dialog (arranged in borderlayout)
        this.getContentPane().add(mainEntryPanel, BorderLayout.CENTER);
        this.getContentPane().add(statusPanel, BorderLayout.SOUTH);

        this.setVisible(true);
        this.requestFocus();
    }
    protected void initMainEntryPanel() {
    }
    protected void initDoButton() {
        this.doButton = new JButton("Save");
        this.doButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveParams();
            }
        });
    }

    
    private void initCancelButton() {
        Dimension buttonSize = new Dimension(70, 25);

        cancelButton.setSize(buttonSize);
        cancelButton.setPreferredSize(buttonSize);
        cancelButton.setSize(buttonSize);
        cancelButton.setPreferredSize(buttonSize);

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    protected void saveParams() {

        dioscuri.config.Emulator params = null;

        String moduleText = "";
        if (moduleType != null) {
            moduleText = moduleType.toString();
        }

        if (moduleType != null
                && (moduleType == ModuleType.ATA || moduleType == ModuleType.FDC)) {
            if (selectedfile == null) {
                JOptionPane.showMessageDialog(this,
                        "Error saving data - please browse for an image file.",
                        "DIOSCURI", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        try {

            params = getParamsFromGui();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving data - please correct the " + moduleText
                            + " parameters entered.", "DIOSCURI",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!Utilities.saveXML(params, parent.getConfigFilePath())) {
            JOptionPane.showMessageDialog(this, "Error saving " + moduleText
                    + " parameter to configuration file.", "DIOSCURI",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        dispose();

    }

    /**
     * Get the params from the GUI - overriden in sub classes.
     * 
     * @return object array of params.
     */
    protected Emulator getParamsFromGui() {
        dioscuri.config.Emulator params = null;

        return params;
    }

    /**
     * Read in params from XML - overriden in sub classes.
     * 
     */
    protected void readInParams() {
    }

    /**
     * Initialise the Bottom Button Panel.
     * 
     */
    protected void initBottomButtonPanel() {
        initDoButton();

        initConfirmButton();

        Dimension buttonSize = new Dimension(70, 25);
        okButton.setSize(buttonSize);
        okButton.setPreferredSize(buttonSize);
        doButton.setSize(buttonSize);
        doButton.setPreferredSize(buttonSize);

        statusPanel = new JPanel(new GridLayout(2, 1));

        this.getContentPane().add(statusPanel, BorderLayout.CENTER);

        Dimension statusDim = new Dimension(dialogWidth, 60);
        statusPanel.setSize(statusDim);
        statusPanel.setPreferredSize(statusDim);

        // Border blackline;
        // blackline = BorderFactory.createLineBorder(Color.black);
        // statusPanel.setBorder(blackline);

        JPanel innerStatusPanel = new JPanel(new FlowLayout(
                FlowLayout.TRAILING, 0, 0));

        innerStatusPanel.add(Box.createRigidArea(new Dimension(5, 5)));
        innerStatusPanel.add(Box.createHorizontalGlue());
        innerStatusPanel.add(doButton);
        innerStatusPanel.add(Box.createRigidArea(new Dimension(5, 5)));
        innerStatusPanel.add(okButton);
        innerStatusPanel.add(Box.createRigidArea(new Dimension(5, 5)));

        statusPanel.add(new JPanel());
        statusPanel.add(innerStatusPanel);

    }
    protected void initConfirmButton() {
        okButton = new JButton("Cancel");

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

    }

    /**
     * Launch a file chooser to select a file.
     * 
     */
    protected void launchFileChooser() {
        JFileChooser fileChooser = new JFileChooser(selectedfile);

        int returnVal = fileChooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            selectedfile = fileChooser.getSelectedFile();

            String filePath = selectedfile.getName();

            // Check if length of filepath is longer than 30 characters
            if (filePath.length() > 30) {
                // Trail off the beginning of the string
                filePath = filePath.substring(filePath.length() - 30);
                imageFilePathLabel.setText("..." + filePath);
            } else {
                imageFilePathLabel.setText(filePath);
            }
        } else {

        }
    }

}
