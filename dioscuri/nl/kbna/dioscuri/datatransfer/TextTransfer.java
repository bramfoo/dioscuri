package nl.kbna.dioscuri.datatransfer;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.Toolkit;
import java.io.*;

import nl.kbna.dioscuri.GUI;

/**
* This class allows data transfer via the clipboard to and from the emulator
*/
public final class TextTransfer implements ClipboardOwner
{

	// Attributes
	GUI gui;
	
	
	// Constructor
	public TextTransfer(GUI parent)
	{
		gui = parent;
	}
	
	
	// Methods
	/**
	* Empty implementation of the ClipboardOwner interface.
	*/
	public void lostOwnership( Clipboard aClipboard, Transferable aContents)
    {
		//do nothing
    }

	/**
	* Set String on clipboard, and make this class the owner of the Clipboard's contents.
    */
	public void setClipboardContents(String text)
	{
		// Wrap String
		StringSelection stringSelection = new StringSelection(text);
		
		// Request system's clipboard and copy text to it
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, this);
	}

	/**
	* Get String in clipboard.
    *
    * @return any text found on the Clipboard; if none found, return an empty String.
    */
	public String getClipboardContents()
	{
		String result = "";
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		
		// odd: the Object param of getContents is not currently used
		Transferable contents = clipboard.getContents(null);
		boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
		
		if (hasTransferableText)
		{
		      try
		      {
		    	  result = (String)contents.getTransferData(DataFlavor.stringFlavor);
		      }
		      catch (UnsupportedFlavorException ex)
		      {
		    	  //highly unlikely since we are using a standard DataFlavor
		    	  System.out.println(ex);
		    	  ex.printStackTrace();
		      }
		      catch (IOException ex)
		      {
		    	  System.out.println(ex);
		    	  ex.printStackTrace();
		      }
	    }
		return result;
	}
}
