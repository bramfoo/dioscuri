/*
 * $Revision: 1.4 $ $Date: 2008-02-12 11:57:30 $ $Author: jrvanderhoeven $
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

package nl.kbna.dioscuri.module;

import java.awt.event.MouseEvent;

/**
 * Interface representing a generic hardware module.
 *  
 */

public abstract class ModuleMouse extends ModuleDevice
{
    // Methods
	public abstract void setMouseEnabled(boolean status);
	
	public abstract void setMouseType(String type);
	
	public abstract boolean isBufferEmpty();
	
	public abstract byte getDataFromBuffer(); // returns the head of the buffer FIFO, data is automatically removed from buffer
	
    public abstract void storeBufferData(boolean forceEnqueue);

    public abstract void controlMouse(byte value);
    
    public abstract void mouseMotion(MouseEvent mouseEvent);
}
