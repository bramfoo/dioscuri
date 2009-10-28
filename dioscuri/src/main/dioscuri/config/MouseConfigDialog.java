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

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import dioscuri.GUI;
import dioscuri.config.Emulator;
import dioscuri.config.Emulator.Architecture.Modules.Mouse;

@SuppressWarnings("serial")
public class MouseConfigDialog extends ConfigurationDialog
{
	// Attributes
	private JLabel enabledLabel;
    private JLabel sizeLabel;
    private JCheckBox enabledCheckBox;
    private JComboBox mouseTypeComboxBox;
    
    dioscuri.config.Emulator emuConfig;
    
    // Constructor
    
    public MouseConfigDialog(GUI parent)
    {               
        super (parent, "Mouse Configuration", false, ModuleType.MOUSE);                    
    }
    
    
    // Methods
    /**
     * Read in params from XML.
     */
    protected void readInParams()
    {
    	emuConfig = parent.getEmuConfig();
    	Mouse mouse = emuConfig.getArchitecture().getModules().getMouse();
        
        boolean enabled = mouse.isEnabled();
        String mouseType = mouse.getMousetype();
        
        // Set mouse parameters
        this.enabledCheckBox.setSelected(enabled);
        this.mouseTypeComboxBox.setSelectedItem(mouseType);
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
    	// Create labels
        enabledLabel = new JLabel("Enabled");
        sizeLabel = new JLabel("  Type");
        
        // Create enabled checkbox
        enabledCheckBox = new JCheckBox();
        enabledCheckBox.setSelected(true);
        
        // Create mouse type selection
        DefaultComboBoxModel mouseTypeModel = new DefaultComboBoxModel();
        mouseTypeModel.addElement("ps/2");
        mouseTypeModel.addElement("serial");
        mouseTypeComboxBox = new JComboBox(mouseTypeModel);
        mouseTypeComboxBox.setSelectedIndex(0);

        // Layout components
        mainEntryPanel  = new JPanel(new GridLayout(10, 3)); 
        Border blackline = BorderFactory.createLineBorder(Color.black);    
        mainEntryPanel.setBorder(blackline);
        
        // Add components to panel
        mainEntryPanel.add(enabledLabel);
        mainEntryPanel.add(enabledCheckBox);

        // Fill first blanks in grid
        for (int i = 0; i < 1; i++)
        {
            mainEntryPanel.add(new JLabel("")); 
        }
        
        mainEntryPanel.add(sizeLabel);
        mainEntryPanel.add(mouseTypeComboxBox);
        mainEntryPanel.add(new JLabel("")); 
        
        // Fill end blanks in grid     
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
    	Mouse mouse = emuConfig.getArchitecture().getModules().getMouse();
        
        mouse.setEnabled(enabledCheckBox.isSelected());    
        mouse.setMousetype((String)mouseTypeComboxBox.getSelectedItem());
        
        return emuConfig;
    }
}
