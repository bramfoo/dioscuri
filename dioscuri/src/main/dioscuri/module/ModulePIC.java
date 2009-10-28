/* $Revision: 160 $ $Date: 2009-08-17 12:56:40 +0000 (ma, 17 aug 2009) $ $Author: blohman $ 
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


package dioscuri.module;

/**
 * Interface representing a generic hardware module.
 *  
 */

public abstract class ModulePIC extends ModuleDevice
{
    // Methods
    
    /**
     * Returns an IRQ number.
     * 
     * @param module that would like to have an IRQ number
     * @return int IRQ number between 1 to 16, or -1 if not allowed/possible
     */
    public abstract int requestIRQNumber(Module module);
    
    /**
     * Lowers an interrupt request (IRQ) of given IRQ number
     * 
     * @param int irqNumber the number of IRQ to be cleared
     */
    public abstract void clearIRQ(int irqNumber);
    
    /**
     * Raises an interrupt request (IRQ) of given IRQ number
     * 
     * @param int irqNumber the number of IRQ to be raised
     */
    public abstract void setIRQ(int irqNumber);
    
    /**
     * Acknowledges an interrupt request from PIC by CPU
     * Note: only the CPU can acknowledge an interrupt
     * 
     * @return int address defining the jump address for handling the IRQ by the CPU
     */
    public abstract int interruptAcknowledge();
}
