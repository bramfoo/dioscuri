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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

public class CpuConfigDialog extends ConfigurationDialog
{

    private JFormattedTextField speedField;
        
    public CpuConfigDialog(JFrame parent)
    {               
        super (parent, "CPU Configuration", false, ModuleType.CPU); 
                    
    }
    
    /**
     * Read in params from XML.
     */
    protected void readInParams()
    {
                
        DioscuriXmlReaderToGui xmlReaderToGui = new DioscuriXmlReaderToGui();
        Object[] params = xmlReaderToGui.getModuleParams(ModuleType.CPU);
        Integer cpuSpeed = ((Integer)params[0]);        
        this.speedField.setValue(cpuSpeed);
        
    }

    protected void initDoButton()
    {
        this.doButton = new JButton("Save");
        this.doButton.addActionListener(new ActionListener()
        {         
            public void actionPerformed(ActionEvent e)
            { 
                saveParams();
            }
        });
    }
    
    /**
     * Initialise the panel for data entry.
     */
    protected void initMainEntryPanel()
    {
                   
        JLabel speedLabel = new JLabel("  Speed (MHz)");
        
        //Formats to format and parse numbers
        speedField = new JFormattedTextField();
        speedField.setValue(new Integer(0));
        speedField.setColumns(10);
  
        mainEntryPanel  = new JPanel(new GridLayout(10, 3)); 
        Border blackline = BorderFactory.createLineBorder(Color.black);    
        mainEntryPanel.setBorder(blackline);
        
        //Fill first blanks in grid
        for (int i = 0; i < 3; i++)
        {
            mainEntryPanel.add(new JLabel("")); 
        }
        
        mainEntryPanel.add(speedLabel); 
        mainEntryPanel.add(speedField);
        mainEntryPanel.add(new JLabel("")); 
        
        //Fill end blanks in grid     
        for (int i = 0; i < 24; i++)
        {
            mainEntryPanel.add(new JLabel("")); 
        }
        
    }
       
    /**
     * Get the params from the GUI.
     * 
     * @return object array of params.
     */
    protected Object[] getParamsFromGui()
    {
              
        Object[] params = new Object[1];
        
        int cpuSpeed = ((Number)speedField.getValue()).intValue();      
        params[0] = new Integer(cpuSpeed);
        
        return params;
    }
        
}
