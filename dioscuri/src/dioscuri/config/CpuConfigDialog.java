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
import java.math.BigDecimal;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

import dioscuri.GUI;
import dioscuri.config.Emulator;
import dioscuri.config.Emulator.Architecture.Modules.Cpu;



public class CpuConfigDialog extends ConfigurationDialog
{
    private JFormattedTextField speedField;
    private JRadioButton cpu32Button;
    private JRadioButton cpu16Button;
        
    dioscuri.config.Emulator emuConfig;
    
    public CpuConfigDialog(GUI parent)
    {               
        super (parent, "CPU Configuration", false, ModuleType.CPU); 
                    
    }
    
    /**
     * Read in params from XML.
     */
    protected void readInParams()
    {
    	emuConfig = parent.getEmuConfig();
    	Cpu cpu = emuConfig.getArchitecture().getModules().getCpu();
    	
        Integer cpuSpeed = cpu.getSpeedmhz().intValue();        
        this.speedField.setValue(cpuSpeed);
        Boolean cpu32Bit = cpu.isCpu32Bit();
        if (cpu32Bit)
            this.cpu32Button.setSelected(true);
        else
            this.cpu16Button.setSelected(true);
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
        JLabel bitLabel = new JLabel("  CPU bits:");
        JLabel speedLabel = new JLabel("  Speed (MHz)");

        String cpu32String = "32-bit";
        String cpu16String = "16-bit";

        // Formats to format and parse numbers
        
        cpu32Button = new JRadioButton(cpu32String);
        cpu16Button = new JRadioButton(cpu16String);
        cpu32Button.setSelected(true);
        
        ButtonGroup cpuBit = new ButtonGroup();
        cpuBit.add(cpu32Button);
        cpuBit.add(cpu16Button);
        
        speedField = new JFormattedTextField();
        speedField.setValue(new Integer(0));
        speedField.setColumns(10);
  
        mainEntryPanel  = new JPanel(new GridLayout(10, 3)); 
        Border blackline = BorderFactory.createLineBorder(Color.black);    
        mainEntryPanel.setBorder(blackline);
        
        mainEntryPanel.add(bitLabel);
        mainEntryPanel.add(cpu32Button);
        mainEntryPanel.add(cpu16Button);

        //Fill first blanks in grid
        for (int i = 0; i < 3; i++)
        {
            mainEntryPanel.add(new JLabel("")); 
        }
        
        mainEntryPanel.add(speedLabel); 
        mainEntryPanel.add(speedField);
        mainEntryPanel.add(new JLabel("")); 
        
        //Fill end blanks in grid     
        for (int i = 0; i < 21; i++)
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
    	Cpu cpu = emuConfig.getArchitecture().getModules().getCpu();

    	cpu.setSpeedmhz(BigDecimal.valueOf(((Number)speedField.getValue()).intValue()));
    	cpu.setCpu32Bit(cpu32Button.isSelected());
        return emuConfig;
    }
        
}
