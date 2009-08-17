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
package nl.kbna.dioscuri.module.ata;

public class InterruptReason
{
    private int cd = 1;
    private int io = 1;
    private int rel = 1;
    private int tag = 5;
    
    public int getCd()
    {
        return cd;
    }
    
    public void setCd(int cd)
    {
        this.cd = cd;
    }
    
    public int getIo()
    {
        return io;
    }
    
    public void setIo(int io)
    {
        this.io = io;
    }
    
    public int getRel()
    {
        return rel;
    }
    public void setRel(int rel)
    {
        this.rel = rel;
    }
    
    public int getTag()
    {
        return tag;
    }
    
    public void setTag(int tag)
    {
        this.tag = tag;
    }
    
    
    //Original BOCHS code 
    //TODO:
/**    
    #ifdef BX_LITTLE_ENDIAN
    unsigned c_d : 1;
    unsigned i_o : 1;
    unsigned rel : 1;
    unsigned tag : 5;
#else  /* BX_BIG_ENDIAN */
/*  unsigned tag : 5;
    unsigned rel : 1;
    unsigned i_o : 1;
    unsigned c_d : 1;
#endif
*/
    
    
    

}
