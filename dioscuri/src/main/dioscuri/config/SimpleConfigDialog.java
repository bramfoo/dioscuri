/*
 * $Revision$ $Date$ $Author$
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
package dioscuri.config;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import dioscuri.GUI;
import dioscuri.config.Emulator;



@SuppressWarnings("serial")
public class SimpleConfigDialog extends ConfigurationDialog
{

    private JFormattedTextField updateIntField;
        
    dioscuri.config.Emulator emuConfig;
    
    public SimpleConfigDialog(GUI parent, ModuleType moduleType)
    {               
        super (parent, moduleType.toString().toUpperCase() + " Configuration", false, moduleType); 
                    
    }
    
    /**
     * Read in params from XML.
     */
    protected void readInParams()
    {
    	emuConfig = parent.getEmuConfig();
    	Integer updateInt = 0;
    	
    	if (moduleType.equals(ModuleType.PIT))
    	{
    		updateInt = emuConfig.getArchitecture().getModules().getPit().getClockrate().intValue();
    	}
    	else if (moduleType.equals(ModuleType.KEYBOARD))
    	{
    		updateInt = emuConfig.getArchitecture().getModules().getKeyboard().getUpdateintervalmicrosecs().intValue();
    	}
    	else if (moduleType.equals(ModuleType.VGA))
    	{
    		updateInt = emuConfig.getArchitecture().getModules().getVideo().getUpdateintervalmicrosecs().intValue();
    	}
    	
        this.updateIntField.setValue(updateInt);
        
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
        String labelText =  "  Update Int (microSecs)";
        if(this.moduleType == ModuleType.PIT)
        {
            labelText = "  Clock Rate";
        }
        JLabel updateIntLabel = new JLabel(labelText);
        
        //Formats to format and parse numbers
        updateIntField = new JFormattedTextField();
        updateIntField.setValue(new Integer(0));
        updateIntField.setColumns(10);
  
        mainEntryPanel  = new JPanel(new GridLayout(10, 3)); 
        Border blackline = BorderFactory.createLineBorder(Color.black);    
        mainEntryPanel.setBorder(blackline);
        
        //Fill first blanks in grid
        for (int i = 0; i < 3; i++)
        {
            mainEntryPanel.add(new JLabel("")); 
        }
        
        mainEntryPanel.add(updateIntLabel); 
        mainEntryPanel.add(updateIntField);
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
    protected Emulator getParamsFromGui()
    {
              
    	if (moduleType.equals(ModuleType.PIT))
    	{
    		emuConfig.getArchitecture().getModules().getPit().setClockrate(BigInteger.valueOf(((Number)updateIntField.getValue()).intValue()));
    	}
    	else if (moduleType.equals(ModuleType.KEYBOARD))
    	{
    		emuConfig.getArchitecture().getModules().getKeyboard().setUpdateintervalmicrosecs(BigInteger.valueOf(((Number)updateIntField.getValue()).intValue()));
    	}
    	else if (moduleType.equals(ModuleType.VGA))
    	{
    		emuConfig.getArchitecture().getModules().getVideo().setUpdateintervalmicrosecs(BigInteger.valueOf(((Number)updateIntField.getValue()).intValue()));
    	}
        
        return emuConfig;
    }
        
}
