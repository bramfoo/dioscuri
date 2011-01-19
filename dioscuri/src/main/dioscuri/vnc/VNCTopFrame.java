/*
 * VNCTopFrame
 */
/* $Revision: 361 $ $Date: 2010-10-01 16:11:11 +0200 (Fr, 01 Okt 2010) $ $Author: bkiers $
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

package dioscuri.vnc;

import gnu.vnc.awt.swing.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.MouseInputListener;

/**
 *
 * @author Gendo
 */
public class VNCTopFrame extends VNCJFrame
{

    /**
     * Constructor
     *
     * @param display
     * @param displayName
     */
    public VNCTopFrame(int display, String displayName) {
        super(displayName, 800, 600);
    }

    /**
     * Move the scrollPane from the GUI to the VNC
     *
     * @param internalFrame
     * @param keyListener
     * @param mouseInputListener
     */
    public void setInternalFrame(JScrollPane internalFrame,
            KeyListener keyListener, MouseInputListener mouseInputListener)
    {
        this.setSize(internalFrame.getWidth(), internalFrame.getHeight());
        JComponent c = new JComponent(){};
        c.setSize(internalFrame.getWidth(), internalFrame.getHeight());
        c.addKeyListener(keyListener);
        if (mouseInputListener != null)
        {
            c.addMouseListener(mouseInputListener);
            c.addMouseMotionListener(mouseInputListener);
        }
        Container contentPane = getContentPane();
        contentPane.add(internalFrame);
        this.setGlassPane(c);
        c.setVisible(true);
    }
}
