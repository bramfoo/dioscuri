/*
 * $Revision: 1.1 $ $Date: 2007-07-02 14:31:25 $ $Author: blohman $
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

public class FdcConfigDialog extends ConfigurationDialog
{
    
    private JCheckBox enabledCheckBox;
    private JCheckBox insertedCheckBox;
    private JComboBox driveLetterComboxBox;
    private JComboBox diskFormatComboBox;
    private JCheckBox writeProtectedCheckBox;   
    private JButton imageBrowseButton;
    
    private JFormattedTextField updateIntField;
    
    public FdcConfigDialog(JFrame parent)
    {               
        super (parent, "FDC Configuration", false, ModuleType.FDC); 
                    
    }
    
    /**
     * Read in params from XML.
     */
    protected void readInParams()
    {
        
        DioscuriXmlReaderToGui xmlReaderToGui = new DioscuriXmlReaderToGui();
        Object[] params = xmlReaderToGui.getModuleParams(ModuleType.FDC);
        
        Integer updateInt = ((Integer)params[0]);        
        boolean enabled = ((Boolean)params[1]).booleanValue();
        boolean inserted = ((Boolean)params[2]).booleanValue();
        int driveLetterIndex = ((Integer)params[3]).intValue();
        int diskFormatIndex = ((Integer)params[4]).intValue();
        boolean writeProtected = ((Boolean)params[5]).booleanValue();
        String imageFormatPath = (String)params[6];
           
        this.updateIntField.setValue(updateInt);
        this.enabledCheckBox.setSelected(enabled);
        this.insertedCheckBox.setSelected(inserted);     
        this.driveLetterComboxBox.setSelectedIndex(driveLetterIndex);
        this.diskFormatComboBox.setSelectedIndex(diskFormatIndex);   
        this.writeProtectedCheckBox.setSelected(writeProtected);
        this.imageFilePathLabel.setText("   " + imageFormatPath);
        this.selectedfile = new File(imageFormatPath);
        
    }
    
    /**
     * Initialise the panel for data entry.
     */
    protected void initMainEntryPanel()
    {
        
        JLabel updateIntLabel = new JLabel("  Update Int (microSecs)");
        JLabel enabledLabel = new JLabel("  Enabled");
        JLabel insertedLabel = new JLabel("  Inserted");
        JLabel driveLetterLabel = new JLabel("  Drive Letter");      
        JLabel diskFormatLabel = new JLabel("  Disk Format");    
        JLabel writeProtectedLabel = new JLabel("  Write Protected");
        JLabel imageFileLabel = new JLabel("  Image File");
        
        this.populateControls();
             
        //Lay out the labels in a panel.
                  
        mainEntryPanel  = new JPanel(new GridLayout(10, 3)); 
        Border blackline = BorderFactory.createLineBorder(Color.black);    
        mainEntryPanel.setBorder(blackline);
        
        //Fill start blanks in grid  
        for (int i = 0; i < 3; i++)
        {
            mainEntryPanel.add(new JLabel("")); 
        }
        
        mainEntryPanel.add(updateIntLabel); 
        mainEntryPanel.add(updateIntField);
        mainEntryPanel.add(new JLabel("")); 
        
        mainEntryPanel.add(enabledLabel); 
        mainEntryPanel.add(enabledCheckBox);
        mainEntryPanel.add(new JLabel("")); 
        
        mainEntryPanel.add(insertedLabel);
        mainEntryPanel.add(insertedCheckBox);  
        mainEntryPanel.add(new JLabel("")); 
        
        mainEntryPanel.add(driveLetterLabel);  
        mainEntryPanel.add(driveLetterComboxBox);  
        mainEntryPanel.add(new JLabel("")); 
            
        mainEntryPanel.add(diskFormatLabel);
        mainEntryPanel.add(diskFormatComboBox);
        mainEntryPanel.add(new JLabel(""));       
        
        mainEntryPanel.add(writeProtectedLabel);
        mainEntryPanel.add(writeProtectedCheckBox);  
        mainEntryPanel.add(new JLabel(""));   
        
        mainEntryPanel.add(imageFileLabel);
        mainEntryPanel.add(imageBrowseButton); 
        mainEntryPanel.add(imageFilePathLabel);
            
        //Fill end blanks in grid    
        for (int i = 0; i < 6; i++)
        {
            mainEntryPanel.add(new JLabel("")); 
        }

        
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
        insertedCheckBox = new JCheckBox();
        insertedCheckBox.setSelected(true);
        
        DefaultComboBoxModel driveLetterModel = new DefaultComboBoxModel();
        driveLetterModel.addElement("A");
        driveLetterModel.addElement("B");   
        driveLetterComboxBox = new JComboBox(driveLetterModel);
        driveLetterComboxBox.setSelectedIndex(0);
             
        DefaultComboBoxModel diskFormatModel = new DefaultComboBoxModel();      
        diskFormatModel.addElement("1.2M");
        diskFormatModel.addElement("1.44M");
        diskFormatModel.addElement("2.88M");
        diskFormatModel.addElement("160K");
        diskFormatModel.addElement("180K");
        diskFormatModel.addElement("320K");
        diskFormatModel.addElement("360K");
        diskFormatModel.addElement("720K");
      
        diskFormatComboBox = new JComboBox(diskFormatModel);
        diskFormatComboBox.setSelectedIndex(1);
        
        writeProtectedCheckBox = new JCheckBox(); 
        
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
              
        Object[] params = new Object[7];
        
        int updateInt = ((Number)updateIntField.getValue()).intValue();      
        params[0] = new Integer(updateInt);
        
        params[1] = enabledCheckBox.isSelected();    
        params[2] = insertedCheckBox.isSelected();
        params[3] =  driveLetterComboxBox.getSelectedItem().toString();
        params[4] =diskFormatComboBox.getSelectedItem().toString();
        params[5]  = writeProtectedCheckBox.isSelected();   
        params[6] = selectedfile.getAbsoluteFile().toString();
        
        return params;
    }
      
}
