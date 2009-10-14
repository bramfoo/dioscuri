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

package dioscuri;

import java.util.ArrayList;

import dioscuri.module.Module;


public class Modules extends ArrayList
{
	// Attributes
	
	// Constructors
	public Modules()
	{
		super();
	}
	
	public Modules(int capacity)
	{
		super(capacity);
	}

	
	// Methods
	public boolean addModule(Module module)
	{
		return super.add(module);
	}


	public Module getModule(String type)
	{
		for (int i = 0; i < super.size(); i++)
		{
			if ((this.getModule(i)).getType().equalsIgnoreCase(type))
			{
				return (Module)super.get(i);
			}
		}
		return null;
	}


	public Module getModule(int index)
	{
		return (Module)super.get(index);
	}
}

