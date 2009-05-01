/*
 * $Revision: 1.4 $ $Date: 2009-04-03 11:06:27 $ $Author: jrvanderhoeven $
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

/*
 * Information used in this module was taken from:
 * - http://en.wikipedia.org/wiki/AT_Attachment
 * - http://bochs.sourceforge.net/techspec/IDE-reference.txt
 */
package nl.kbna.dioscuri.config;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import nl.kbna.dioscuri.GUI;

public class AtaConfigDialog extends ConfigurationDialog
{
    
    private JCheckBox enabledCheckBox;
    private JFormattedTextField channelIndexFTextField;  
    private JCheckBox masterCheckBox;
    private JCheckBox autodetectCheckBox;        
    private JFormattedTextField cylindersFTextField;    
    private JFormattedTextField headsFTextField;
    private JFormattedTextField sectorsFTextField;
    
    private JFormattedTextField updateIntField;
    
    private JButton imageBrowseButton;
            
    public AtaConfigDialog(GUI parent)
    {               
        super (parent, "ATA Configuration", false, ModuleType.ATA); 
                    
    }
    
    /**
     * Read in params from XML.
     */
    protected void readInParams()
    {
        
    	DioscuriXmlReader xmlReaderToGui = parent.getXMLReader();
        Object[] params = xmlReaderToGui.getModuleParams(ModuleType.ATA);
        
        Integer updateInt = ((Integer)params[0]);      
        boolean isEnabled = ((Boolean)params[1]).booleanValue();
        int channelIndex = ((Integer)params[2]).intValue();
        boolean isMaster = ((Boolean)params[3]).booleanValue();
        boolean autoDetect = ((Boolean)params[4]).booleanValue();        
        int cylinders = ((Integer)params[5]).intValue();
        int heads = ((Integer)params[6]).intValue();
        int sectors = ((Integer)params[7]).intValue();
        String imageFormatPath = (String)params[8];
        
        this.updateIntField.setValue(updateInt);
        this.enabledCheckBox.setSelected(isEnabled);
        this.channelIndexFTextField.setValue(new Integer(channelIndex));           
        this.masterCheckBox.setSelected(isMaster);
        this.autodetectCheckBox.setSelected(autoDetect);     
        this.cylindersFTextField.setValue(new Integer(cylinders));   
        this.headsFTextField.setValue(new Integer(heads));
        this.sectorsFTextField.setValue(new Integer(sectors));

        // Check if length of filepath is longer than 30 characters
        if (imageFormatPath.length() > 30)
        {
        	// Trail off the beginning of the string
        	this.imageFilePathLabel.setText("..." + imageFormatPath.substring(imageFormatPath.length() - 30));
        }
        else
        {
            this.imageFilePathLabel.setText(imageFormatPath);
        }
        
        this.selectedfile = new File(imageFormatPath);
    }
    
    /**
     * Initialise the panel for data entry.
     */
    protected void initMainEntryPanel()
    {
    	// Create labels
        JLabel updateIntLabel = new JLabel("Update Interval");
        JLabel updateIntUnitLabel = new JLabel("microseconds");
        JLabel enabledLabel = new JLabel("Enabled");
        JLabel channelIndexLabel = new JLabel("Channel Index");
        JLabel masterLabel = new JLabel("Master");
        JLabel autodetectLabel = new JLabel("Auto Detect");
        JLabel cylindersLabel = new JLabel("Cylinders");
        JLabel headsLabel = new JLabel("Heads");
        JLabel sectorsLabel  = new JLabel("Sectors");
        JLabel imageFileLabel = new JLabel("Image File");
        
        // Create controls
        this.populateControls();
        
        //Lay out the labels in a panel.
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        mainEntryPanel = new JPanel(gridbag);
        
        // Makeup layout with contraints
        // General layout contraints
        c.fill = GridBagConstraints.BOTH; // Make the component fill its display area entirely
        c.insets = new Insets(0, 10, 0, 10);	// Defines the spaces between layout and display area
        
        // Row 1
        c.weightx = 1.0;
        c.gridwidth = 1;
        gridbag.setConstraints(updateIntLabel, c);
        mainEntryPanel.add(updateIntLabel);
        c.gridwidth = GridBagConstraints.RELATIVE; // next-to-last in row
        gridbag.setConstraints(updateIntField, c);
        mainEntryPanel.add(updateIntField);
        c.gridwidth = GridBagConstraints.REMAINDER; //end row
        gridbag.setConstraints(updateIntUnitLabel, c);
        mainEntryPanel.add(updateIntUnitLabel);

        // Row 2
        c.weightx = 0.1;
        c.gridwidth = 1;
        gridbag.setConstraints(enabledLabel, c);
        mainEntryPanel.add(enabledLabel);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        gridbag.setConstraints(enabledCheckBox, c);
        mainEntryPanel.add(enabledCheckBox);

        // Row 3
        c.gridwidth = 1;
        gridbag.setConstraints(channelIndexLabel, c);
        mainEntryPanel.add(channelIndexLabel);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        gridbag.setConstraints(channelIndexFTextField, c);
        mainEntryPanel.add(channelIndexFTextField);
        
        // Row 4
        c.gridwidth = 1;
        gridbag.setConstraints(masterLabel, c);
        mainEntryPanel.add(masterLabel);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        gridbag.setConstraints(masterCheckBox, c);
        mainEntryPanel.add(masterCheckBox);

        // Row 5
        c.gridwidth = 1;
        gridbag.setConstraints(autodetectLabel, c);
        mainEntryPanel.add(autodetectLabel);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        gridbag.setConstraints(autodetectCheckBox, c);
        mainEntryPanel.add(autodetectCheckBox);

        // Row 6
        c.gridwidth = 1;
        gridbag.setConstraints(cylindersLabel, c);
        mainEntryPanel.add(cylindersLabel);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        gridbag.setConstraints(cylindersFTextField, c);
        mainEntryPanel.add(cylindersFTextField);

        // Row 7
        c.gridwidth = 1;
        gridbag.setConstraints(headsLabel, c);
        mainEntryPanel.add(headsLabel);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        gridbag.setConstraints(headsFTextField, c);
        mainEntryPanel.add(headsFTextField);

        // Row 8
        c.gridwidth = 1;
        gridbag.setConstraints(sectorsLabel, c);
        mainEntryPanel.add(sectorsLabel);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        gridbag.setConstraints(sectorsFTextField, c);
        mainEntryPanel.add(sectorsFTextField);

        // Row 9
        c.gridwidth = 1;
        gridbag.setConstraints(imageFileLabel, c);
        mainEntryPanel.add(imageFileLabel);
        c.gridwidth = GridBagConstraints.RELATIVE; // next-to-last in row
        gridbag.setConstraints(imageFilePathLabel, c);
        mainEntryPanel.add(imageFilePathLabel);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        gridbag.setConstraints(imageBrowseButton, c);
        mainEntryPanel.add(imageBrowseButton);

    }
    
    /**
     * Initalise the GUI Controls.
     */
    private void populateControls()
    {
        imageFilePathLabel = new JLabel("");
             
        //Formats to format and parse numbers
        updateIntField = new JFormattedTextField();
        updateIntField.setValue(new Integer(0));
        updateIntField.setColumns(10);
        
        enabledCheckBox = new JCheckBox();
        enabledCheckBox.setSelected(true);   
        
        channelIndexFTextField = new JFormattedTextField(); 
        channelIndexFTextField.setValue(new Integer(0));
        channelIndexFTextField.setColumns(10);
        cylindersFTextField = new JFormattedTextField(); 
        cylindersFTextField.setValue(new Integer(0));
        cylindersFTextField.setColumns(10);
        headsFTextField = new JFormattedTextField();
        headsFTextField.setValue(new Integer(0));
        headsFTextField.setColumns(10);
        sectorsFTextField = new JFormattedTextField();
        sectorsFTextField.setValue(new Integer(0));
        sectorsFTextField.setColumns(10);  
        
        masterCheckBox = new JCheckBox();
        masterCheckBox.setSelected(true);
        
        autodetectCheckBox = new JCheckBox();
          
        imageBrowseButton = new JButton("Browse");
        
        imageBrowseButton.addActionListener(new ActionListener()
        {         
            public void actionPerformed(ActionEvent e)
            { 
                launchFileChooser();
            }
        });
        
    }
      
    /**
     * Get the params from the GUI.
     * 
     * @return object array of params.
     */
    protected Object[] getParamsFromGui()
    {
              
        Object[] params = new Object[9];
        
        int updateInt = ((Number)updateIntField.getValue()).intValue();   
        
        params[0] = new Integer(updateInt);  
        params[1] = Boolean.valueOf(enabledCheckBox.isSelected()); 
        params[2] = channelIndexFTextField.getValue();         
        params[3] = Boolean.valueOf(masterCheckBox.isSelected());
        params[4] = Boolean.valueOf(autodetectCheckBox.isSelected());          
        params[5] = cylindersFTextField.getValue(); 
        params[6] = headsFTextField.getValue(); 
        params[7] = sectorsFTextField.getValue();    
        params[8] = selectedfile.getAbsoluteFile().toString();
        
        return params;
    }
        
}
