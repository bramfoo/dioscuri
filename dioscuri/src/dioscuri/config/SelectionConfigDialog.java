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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import dioscuri.GUI;


public class SelectionConfigDialog extends ConfigurationDialog
{
    
    private JList modulesList;
    
    public SelectionConfigDialog(GUI parent)
    {               
        super (parent, "Configuration Selector", true, null);           
    }
    
    /**
     * Initialize the edit button
     */
    protected void initDoButton()
    {
        doButton = new JButton("Edit");
        
        doButton.addActionListener(new ActionListener()
        {         
            public void actionPerformed(ActionEvent e)
            { 
                launchSpecificConfigDialog();
                       
            }
            
        });
    }
    
    protected void initConfirmButton()
    {
        okButton = new JButton("OK");
        
        okButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
               dispose(); 
            }
          });
            
    }

    /**
     * Launch the config dialog for the selected module.
     */
    private void launchSpecificConfigDialog()
    {
    
        ModuleType selectedModule = (ModuleType)modulesList.getSelectedValue();
        
        if (selectedModule.equals(ModuleType.ATA)) 
        {
            AtaConfigDialog ataConfigDialog = new AtaConfigDialog(parent);
            
        } else if (selectedModule.equals(ModuleType.BIOS)) 
        {
           BiosConfigDialog biosConfigDialog = new BiosConfigDialog(parent);
                
        } else if(selectedModule.equals(ModuleType.BOOT))
        {       
            BootConfigDialog bootConfigDialog = new BootConfigDialog(parent);  
            
        } else if(selectedModule.equals(ModuleType.CPU))
        {       
            CpuConfigDialog cpuConfigDialog = new CpuConfigDialog(parent);
        
        } else if (selectedModule.equals(ModuleType.FDC)) 
        {
            FdcConfigDialog fdcConfigDialog = new FdcConfigDialog(parent);
            
        } else if (selectedModule.equals(ModuleType.MEMORY)) 
        {
            RamConfigDialog ramConfigDialog = new RamConfigDialog(parent);
            
        } else if (selectedModule.equals(ModuleType.MOUSE)) 
        {
            MouseConfigDialog mouseConfigDialog = new MouseConfigDialog(parent);
            
        } else if (selectedModule.equals(ModuleType.PIT) 
                || selectedModule.equals(ModuleType.KEYBOARD)
                || selectedModule.equals(ModuleType.VGA)) 
        {
            SimpleConfigDialog simpleConfigDialog = new SimpleConfigDialog(parent, selectedModule);
        }
    }
    
    /**
     * Initialise the panel for data entry.
     */
    protected void initMainEntryPanel()
    {
        DefaultListModel listModel = new DefaultListModel();
        
/*       
//    Add all config modules
      ModuleType[] moduleTypes = ModuleType.moduleTypes;     
        for(int i = 0; i < moduleTypes.length; i++)
        {     
            listModel.addElement(moduleTypes[i]);        
        }
*/
        listModel.addElement(moduleType.ATA);
        listModel.addElement(moduleType.BIOS);
        listModel.addElement(moduleType.BOOT); 
        listModel.addElement(moduleType.CPU);
        listModel.addElement(moduleType.FDC);
        listModel.addElement(moduleType.KEYBOARD);
        listModel.addElement(moduleType.MOUSE);
        listModel.addElement(moduleType.MEMORY); 
        listModel.addElement(moduleType.PIT);
        listModel.addElement(moduleType.VGA);
        
        modulesList = new JList(listModel);
        modulesList.setSelectedIndex(0);
      
     
        JScrollPane selectionScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                          JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        Dimension listSize = new Dimension(dialogWidth - 50, 215); 
        selectionScrollPane.setSize(listSize);
        selectionScrollPane.setPreferredSize(listSize);
        
        
        selectionScrollPane.getViewport().add(modulesList);
        
        mainEntryPanel.add(selectionScrollPane);
        
        Border blackline = BorderFactory.createLineBorder(Color.black);    
        mainEntryPanel.setBorder(blackline);
        
        modulesList.addMouseListener(new MouseAdapter() 
        {

            public void mouseClicked(MouseEvent e) 
            {
                if (e.getClickCount() == 2) 
                {
                    launchSpecificConfigDialog();
                }
            }
        });


    }
    
}
