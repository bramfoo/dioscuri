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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

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
            
    public AtaConfigDialog(JFrame parent)
    {               
        super (parent, "ATA Configuration", false, ModuleType.ATA); 
                    
    }
    
    /**
     * Read in params from XML.
     */
    protected void readInParams()
    {
        
        DioscuriXmlReaderToGui xmlReaderToGui = new DioscuriXmlReaderToGui();
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
        JLabel channelIndexLabel = new JLabel("  Channel Index");  
        JLabel masterLabel = new JLabel("  Master");
        JLabel autodetectLabel = new JLabel("  Auto Detect");        
        JLabel cylindersLabel = new JLabel("  Cylinders");    
        JLabel headsLabel = new JLabel("  Heads");
        JLabel sectorsLabel  = new JLabel("  Sectors");
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
        
        mainEntryPanel.add(channelIndexLabel);
        mainEntryPanel.add(channelIndexFTextField);  
        mainEntryPanel.add(new JLabel("")); 
        
        mainEntryPanel.add(masterLabel);  
        mainEntryPanel.add(masterCheckBox);  
        mainEntryPanel.add(new JLabel("")); 
            
        mainEntryPanel.add(autodetectLabel);
        mainEntryPanel.add(autodetectCheckBox);
        mainEntryPanel.add(new JLabel(""));       
        
        mainEntryPanel.add(cylindersLabel);
        mainEntryPanel.add(cylindersFTextField);  
        mainEntryPanel.add(new JLabel("")); 
        
        mainEntryPanel.add(headsLabel);
        mainEntryPanel.add(headsFTextField);  
        mainEntryPanel.add(new JLabel(""));    
        
        mainEntryPanel.add(sectorsLabel);
        mainEntryPanel.add(sectorsFTextField);  
        mainEntryPanel.add(new JLabel(""));
        
        mainEntryPanel.add(imageFileLabel);
        mainEntryPanel.add(imageBrowseButton); 
        mainEntryPanel.add(imageFilePathLabel);
            
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
        params[1] = enabledCheckBox.isSelected(); 
        params[2] = channelIndexFTextField.getValue();         
        params[3] = masterCheckBox.isSelected();
        params[4] = autodetectCheckBox.isSelected();          
        params[5] = cylindersFTextField.getValue(); 
        params[6] = headsFTextField.getValue(); 
        params[7] = sectorsFTextField.getValue();    
        params[8] = selectedfile.getAbsoluteFile().toString();
        
        return params;
    }
        
}
