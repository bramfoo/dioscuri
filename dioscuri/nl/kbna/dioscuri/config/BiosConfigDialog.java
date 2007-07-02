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
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

public class BiosConfigDialog extends ConfigurationDialog
{
    
    private JFormattedTextField sysBiosStartsFTextField;  
    private JFormattedTextField vgaBiosStartFTextField; 
    
    private JButton sysBiosBrowseButton;
    private JButton vgaBiosBrowseButton;  
        
    private File selectedVgaFile = null;
    private JLabel imageFilePathVgaLabel = null;
            
    public BiosConfigDialog(JFrame parent)
    {               
        super (parent, "BIOS Configuration", false, ModuleType.BIOS);                  
    }
    
    /**
     * Read in params from XML.
     */
    protected void readInParams()
    {
        
        DioscuriXmlReaderToGui xmlReaderToGui = new DioscuriXmlReaderToGui();
        Object[] params = xmlReaderToGui.getModuleParams(ModuleType.BIOS);
                        
        String sysPath = (String)params[0];
        String vgaPath = (String)params[1];
        
        Integer ramSysBiosAddressStart = ((Integer)params[2]).intValue();
        Integer ramVgaBiosAddressStart = ((Integer)params[3]).intValue();
                
        this.sysBiosStartsFTextField.setValue(ramSysBiosAddressStart);           
        this.vgaBiosStartFTextField.setValue(ramVgaBiosAddressStart);  
        
        this.selectedfile = new File(sysPath);
        this.selectedVgaFile = new File(vgaPath);
        
        this.imageFilePathLabel.setText("   " + selectedfile.getName());
        this.imageFilePathVgaLabel.setText("   " + selectedVgaFile.getName());      
        

        
    }
    
    /**
     * Initialise the panel for data entry.
     */
    protected void initMainEntryPanel()
    {
                 
        JLabel sysBiosFileLabel = new JLabel("  Sys Bios File");  
        JLabel vgaBiosFileLabel = new JLabel("  Vga Bios File"); 
        JLabel sysBiosStartLabel = new JLabel("  Sys Bios Start");
        JLabel vgaBiosStartLabel = new JLabel("  Vga Bios Start");   
        
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
        
        mainEntryPanel.add(sysBiosFileLabel);
        mainEntryPanel.add(this.sysBiosBrowseButton);  
        mainEntryPanel.add(this.imageFilePathLabel); 
        
        mainEntryPanel.add(vgaBiosFileLabel);
        mainEntryPanel.add(this.vgaBiosBrowseButton);  
        mainEntryPanel.add(this.imageFilePathVgaLabel); 
        
        mainEntryPanel.add(sysBiosStartLabel);
        mainEntryPanel.add(this.sysBiosStartsFTextField); 
        mainEntryPanel.add(new JLabel(""));
        
        mainEntryPanel.add(vgaBiosStartLabel);
        mainEntryPanel.add(this.vgaBiosStartFTextField); 
        mainEntryPanel.add(new JLabel(""));
                    
        //Fill end blanks in grid    
        for (int i = 0; i < 15; i++)
        {
            mainEntryPanel.add(new JLabel("")); 
        }
  
    }
    
    /**
     * Initalise the GUI Controls.
     */
    private void populateControls()
    {
        this.imageFilePathLabel = new JLabel("");
        this.imageFilePathVgaLabel  = new JLabel("");
                   
        this.sysBiosStartsFTextField = new JFormattedTextField();
        this.sysBiosStartsFTextField.setValue(new Integer(0));
        this.sysBiosStartsFTextField.setColumns(10);
        
        this.vgaBiosStartFTextField = new JFormattedTextField();
        this.vgaBiosStartFTextField.setValue(new Integer(0));
        this.vgaBiosStartFTextField.setColumns(10);  
         
        this.sysBiosBrowseButton = new JButton("Browse");       
        this.sysBiosBrowseButton.addActionListener(new ActionListener()
        {         
            public void actionPerformed(ActionEvent e)
            { 
                launchFileChooser();
            }
        });
               
        this.vgaBiosBrowseButton = new JButton("Browse");
        this.vgaBiosBrowseButton.addActionListener(new ActionListener()
                {         
                    public void actionPerformed(ActionEvent e)
                    { 
                        launchFileChooserVga();
                    }
                });
        
        
    }
    
    /**
     * Launch a file chooser to select VGA file.
     *
     */
    private void launchFileChooserVga()
    {
        JFileChooser fileChooser = new JFileChooser();
        
        int returnVal = fileChooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) 
        {
            
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
    protected Object[] getParamsFromGui()
    {
              
        Object[] params = new Object[4];
        
        params[0] = selectedfile.getAbsoluteFile().toString();    
        params[1] = selectedVgaFile.getAbsoluteFile().toString();
        params[2] = sysBiosStartsFTextField.getValue();
        params[3] = vgaBiosStartFTextField.getValue();  
  
        return params;
    }
        
}
