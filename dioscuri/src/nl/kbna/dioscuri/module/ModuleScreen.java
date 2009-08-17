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

package nl.kbna.dioscuri.module;

import javax.swing.JPanel;

/**
 * Abstract class representing a generic hardware module.
 * This class defines a template for a screen module.
 *  
 */

public abstract class ModuleScreen extends Module
{
    // Methods
    /**
     * Return a reference to the actual screen
     */
    public abstract JPanel getScreen();
    
    /**
     * Clear screen from any output
     */
    public abstract void clearScreen();

    /**
     * Return the number of rows on screen (text based)
     */
    public abstract int getScreenRows();

    /**
     * Return the number of columns on screen (text based)
     */
    public abstract int getScreenColumns();

    /**
     * Return width of screen in number of pixels
     */
    public abstract int getScreenWidth();

    /**
     * Return height of screen in number of pixels
     */
    public abstract int getScreenHeight();

    /**
     * Set the screen size in number of pixels
     * @param int width New width of the screen in pixels
     * @param int height New height of the screen in pixels
     */
    public abstract void setScreenSize(int width, int height);

    /**
     * Update screen size
     * 
     * @param int screenWidth 
     * @param int screenHeight
     * @param int fontWidth (zero if not relevant)
     * @param int fontHeight (zero if not relevant)
     */
    public abstract void updateScreenSize(int screenWidth, int screenHeight, int fontWidth, int fontHeight);
    
    /**
     * Update the code page
     * The code page is the character encoding table
     * 
     * @param int startAddress
     */
    public abstract void updateCodePage(int startAddress);
    
    /**
     * Set a byte in Code page
     * The code page is the character encoding table
     * 
     * @param int index
     * @param byte data
     */
    public abstract void setByteInCodePage(int index, byte data);

    /**
     * Set a particular colour in palette with RGB-values
     * 
     * @param byte index denoting position of colour in palette 
     * @param int red
     * @param int green
     * @param int blue
     */
    public abstract boolean setPaletteColour(byte index, int red, int green, int blue);
    
    /**
     * Update a tile on screen with given bytes
     * Graphics mode. A tile is a part of the screenbuffer 
     * 
     * @param byte[] tile containing the bytes of the tile to be updated 
     * @param int startPositionX
     * @param int startPositionY
     */
    public abstract void updateGraphicsTile(byte[] tile, int startPositionX, int startPositionY);
    
    /**
     * Update text on screen at given position
     * Text mode. Selected text will replace existing text at given position 
     * 
     * @param int oldText is start position of old text 
     * @param int newText is start position of new text
     * @param long cursorXpos
     * @param long cursorYpos
     * @param short[] textModeAttribs contains (in order):
     *                      fullStartAddress, cursorStartLine, cursorEndLine, 
     *                      lineOffset, lineCompare, horizPanning, vertPanning, 
     *                      lineGraphics, splitHorizPanning
     * @param int numberOfRows denoting the number of text rows to update
     */
    public abstract void updateText(int oldText, int newText, long cursorXPos, long cursorYPos, short[] textModeAttribs, int numberOfRows);
}
