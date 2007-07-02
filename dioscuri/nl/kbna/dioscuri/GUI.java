/*
 * $Revision: 1.1 $ $Date: 2007-07-02 14:31:24 $ $Author: blohman $
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
package nl.kbna.dioscuri;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuComponent;
import java.awt.MenuItem;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;

import nl.kbna.dioscuri.config.SelectionConfigDialog;

/**
*
* Graphical User Interface for emulator.
*/
public class GUI extends JFrame implements ActionListener, KeyListener
{
   // Attributes
   private Emulator emu;
   
   // Panels
   private ScrollPane screenPanel = null;
   private JPanel statusPanel = null;
   private JPanel scrollPanel = null;
   private JPanel numPanel = null;
   private JPanel capsPanel = null;
   private JPanel floppyAPanel = null;
   private JPanel hd1Panel = null;
   
   // Menus
   MenuBar menuBar;
   Menu menuEmulator;
   Menu menuEdit;
   Menu menuSource;
   Menu menuConfig;
   Menu menuHelp;

   // Menu items
   MenuItem miEmulatorStart;
   MenuItem miEmulatorStop;
   MenuItem miEmulatorReset;
   MenuItem miEmulatorQuit;
   MenuItem miEditCopyText;
   MenuItem miEditCopyImage;
   MenuItem miEditPasteText;
   MenuItem miEditPasteImage;
 
   MenuItem miEditConfig;
   
   MenuItem miSourceEjectA;
   MenuItem miSourceInsertA;
   MenuItem miHelpAbout;
   
   // Screen canvas
   private Canvas screenCanvas;     // Defines the screen of the emulator
   
   // File selection
   private JFileChooser fcFloppy;
   
   // Logging
   private static Logger logger = Logger.getLogger("nl.kbna.dioscuri.gui");
   
   // Frame and refresh properties
   private int guiWidth;
   private int guiHeight;
   
   
   // Constants
   // Emulator characteristics
   protected final static String EMULATOR_NAME = "Dioscuri - modular emulator for digital preservation";
   protected final static String EMULATOR_VERSION = "0.0.9";
   protected final static String EMULATOR_DATE = "July, 2007";
   protected final static String EMULATOR_CREATOR = "National Library of the Netherlands, Nationaal Archief of the Netherlands";
   private final static String EMULATOR_ICON_IMAGE = "config/dioscuri_icon.gif";
   private final static String EMULATOR_SPLASHSCREEN_IMAGE = "config/dioscuri_splashscreen.gif";
   
   // Dimension settings
   private static final int GUI_X_LOCATION = 200;
   private static final int GUI_Y_LOCATION = 200;
   
   // GUI update activities
   public static final int EMU_PROCESS_START            = 0;
   public static final int EMU_PROCESS_STOP             = 1;
   public static final int EMU_PROCESS_RESET            = 2;
   public static final int EMU_FLOPPYA_INSERT           = 3;
   public static final int EMU_FLOPPYA_EJECT            = 4;
   public static final int EMU_HD1_INSERT               = 5;
   public static final int EMU_HD1_EJECT                = 6;
   public static final int EMU_HD1_TRANSFER_START       = 7;
   public static final int EMU_HD1_TRANSFER_STOP        = 8;
   public static final int EMU_KEYBOARD_NUMLOCK_ON      = 9;
   public static final int EMU_KEYBOARD_NUMLOCK_OFF     = 10;
   public static final int EMU_KEYBOARD_CAPSLOCK_ON     = 11;
   public static final int EMU_KEYBOARD_CAPSLOCK_OFF    = 12;
   public static final int EMU_KEYBOARD_SCROLLLOCK_ON   = 13;
   public static final int EMU_KEYBOARD_SCROLLLOCK_OFF  = 14;
   public static final int EMU_FLOPPYA_TRANSFER_START   = 15;
   public static final int EMU_FLOPPYA_TRANSFER_STOP    = 16;
   public static final int GUI_RESET                    = 99;
   
   // Key events
   private static final int KEY_PRESSED    = 0;
   private static final int KEY_RELEASED   = 1;
   private static final int KEY_TYPED      = 2;

   
   /**
    * Main method
    * 
    * @param String args containing command line arguments
    */
   public static void main(String[] args)
   {
       
       try
       {
           // Logging
           LogManager.getLogManager().readConfiguration(new BufferedInputStream(new DataInputStream(new FileInputStream(new File("config/logging.properties")))));
           
           logger.setLevel(Level.ALL);
       }
       catch (SecurityException e)
       {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }
       catch (IOException e)
       {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }
         
       // Create GUI
       new GUI();
   }

   
   // Constructor
   /**
    * Class constructor
    */
   public GUI()
   {
       // Print startup information of modular emulator
       logger.log(Level.SEVERE, toString());
       
       // Create graphical user interface

       // Add handlers and listeners
       // Window closing listener
       addWindowListener(new WindowAdapter(){
           public void windowClosing(WindowEvent event)
           {
               exitViewer();
           }
       });

       // Key handler to the GUI, disabling focus traversal so Tab events are available
       // KeyEvents will be handled here in screen
       this.addKeyListener(this);
       this.setFocusTraversalKeysEnabled(false);

       // Set native look and feel
       try
       {
           UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
       }
       catch(Exception e)
       {
           logger.log(Level.WARNING, "GUI error: not able to load native look and feel.");
       }
       
       // Set icon image
       this.setIconImage(this.getImageFromFile(EMULATOR_ICON_IMAGE));
           
       // Create menubar
       this.initMenuBar();

       // Create panel: screen (canvas)
       screenPanel = new ScrollPane();
       screenPanel.setBackground(Color.gray);
       this.setScreen(this.getStartupScreen());
       
       // Create panel: statusbar (including panels w/ borders for Num Lock, Caps Lock and Scroll Lock status)
       statusPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
       this.initStatusBar();

       // Add panels to frame (arranged in borderlayout)
       this.getContentPane().add(screenPanel, BorderLayout.CENTER);
       this.getContentPane().add(statusPanel, BorderLayout.SOUTH);
       
       // Create file choosers
       fcFloppy = new JFileChooser();
       
       // Set dimensions
       guiWidth = screenCanvas.getWidth() + 90; // screen width + a random extra value
       guiHeight = screenCanvas.getHeight() + 2 * 38; // screen height + 2 * menu & statusbar height
     
       // Build frame
       this.setLocation(GUI_X_LOCATION, GUI_Y_LOCATION);
       this.setSize(guiWidth, guiHeight);
       this.setTitle(this.getEmulatorName());
       this.setResizable(false);
       this.updateGUI(GUI_RESET);
       this.setVisible(true);
       this.requestFocus();
   }

   
   // Methods
   
   /**
    * Initialise menu bar
    */
   private void initMenuBar()
   {
       // Create a menubar
       menuBar = new MenuBar();
       
       // Create menu: emulator
       menuEmulator = new Menu("Emulator");
       miEmulatorStart = new MenuItem("Start process (power on)");
       miEmulatorStop = new MenuItem("Stop process (shutdown)");
       miEmulatorReset = new MenuItem("Reset process (warm reset)");
       miEmulatorQuit = new MenuItem("Quit");
       menuEmulator.add(miEmulatorStart);
       menuEmulator.add(miEmulatorStop);
       menuEmulator.add(miEmulatorReset);
       menuEmulator.add(miEmulatorQuit);

       // Create menu: edit
       menuEdit = new Menu("Edit");
       miEditCopyText = new MenuItem("Copy text");
       miEditCopyImage = new MenuItem("Copy image");
       miEditPasteText = new MenuItem("Paste text");
       miEditPasteImage = new MenuItem("Paste image");
       menuEdit.add(miEditCopyText);
       menuEdit.add(miEditCopyImage);
       menuEdit.add(miEditPasteText);
       menuEdit.add(miEditPasteImage);
       
       // Create menu: source
       menuSource = new Menu("Media");
       miSourceEjectA = new MenuItem("Eject floppy A:");
       miSourceInsertA = new MenuItem("Insert floppy A:");
       menuSource.add(miSourceEjectA);
       menuSource.add(miSourceInsertA);
       
       // Create menu: config        
       menuConfig = new Menu("Configure");           
       miEditConfig = new MenuItem("Edit Config");  
       menuConfig.add(miEditConfig);
       
       // Create menu: help
       menuHelp = new Menu("Help");
       miHelpAbout = new MenuItem("About..");
       menuHelp.add(miHelpAbout);
       
       // Assign all menus to the menubar
       menuBar.add(menuEmulator);
       menuBar.add(menuEdit);
       menuBar.add(menuSource);
       menuBar.add(menuConfig);
       menuBar.add(menuHelp);
       
       // Assign menubar to frame
       this.setMenuBar(menuBar);

       // Add action listeners for tracing events
       miEmulatorStart.addActionListener(this);
       miEmulatorStop.addActionListener(this);
       miEmulatorReset.addActionListener(this);
       miEmulatorQuit.addActionListener(this);
       miSourceEjectA.addActionListener(this);
       miSourceInsertA.addActionListener(this);    
       miEditConfig.addActionListener(this);       
       miHelpAbout.addActionListener(this);
   }


   /**
    * Initialise status bar
    */
   private void initStatusBar()
   {
       Border blackline;
       blackline = BorderFactory.createLineBorder(Color.black);

       numPanel = new JPanel();
       numPanel.setLayout(new BoxLayout(numPanel, BoxLayout.X_AXIS));
       Label numPanelLabel = new Label("1");
       numPanelLabel.setAlignment(Label.CENTER);
       numPanel.add(numPanelLabel);
       numPanel.setBorder(blackline);
       numPanel.setSize(20, 20);

       capsPanel = new JPanel();
       capsPanel.setLayout(new BoxLayout(capsPanel, BoxLayout.X_AXIS));
       Label capsPanelLabel = new Label("A");
       capsPanelLabel.setAlignment(Label.CENTER);
       capsPanel.add(capsPanelLabel);
       capsPanel.setBorder(blackline);

       scrollPanel = new JPanel();
       scrollPanel.setLayout(new BoxLayout(scrollPanel, BoxLayout.X_AXIS));
       Label scrollPanelLabel = new Label("S");
       scrollPanelLabel.setAlignment(Label.CENTER);
       scrollPanel.add(scrollPanelLabel);
       scrollPanel.setBorder(blackline);
       
       floppyAPanel = new JPanel();
       floppyAPanel.setLayout(new BoxLayout(floppyAPanel, BoxLayout.X_AXIS));
       Label floppyAPanelLabel = new Label("A:");
       floppyAPanelLabel.setAlignment(Label.CENTER);
       floppyAPanel.add(floppyAPanelLabel);
       floppyAPanel.setBorder(blackline);
       
       hd1Panel = new JPanel();
       hd1Panel.setLayout(new BoxLayout(hd1Panel, BoxLayout.X_AXIS));
       Label hd1PanelLabel = new Label("HD1");
       hd1PanelLabel.setAlignment(Label.CENTER);
       hd1Panel.add(hd1PanelLabel);
       hd1Panel.setBorder(blackline);

       // Add panels to statusbar
       statusPanel.add(Box.createHorizontalGlue());
       statusPanel.add(numPanel);
       statusPanel.add(Box.createRigidArea(new Dimension(5,0)));
       statusPanel.add(capsPanel);
       statusPanel.add(Box.createRigidArea(new Dimension(5,0)));
       statusPanel.add(scrollPanel);
       statusPanel.add(Box.createRigidArea(new Dimension(5,0)));
       statusPanel.add(floppyAPanel);
       statusPanel.add(Box.createRigidArea(new Dimension(5,0)));
       statusPanel.add(hd1Panel);
   }


   /**
    * Returns the name of emulator
    * 
    * @return String containing name
    */
   public String getEmulatorName()
   {
       return EMULATOR_NAME;
   }


   /**
    * Returns the version stamp of emulator
    * 
    * @return String containing version
    */
   public String getEmulatorVersion()
   {
       return EMULATOR_VERSION;
   }

   
   /**
    * Returns the date stamp of emulator
    * 
    * @return String containing date
    */
   public String getEmulatorDate()
   {
       return EMULATOR_DATE;
   }


   /**
    * Returns the startup screen for GUI
    * 
    * @return Canvas startup screen
    */
   private Canvas getStartupScreen()
   {
       // Create startup screen
       StartupCanvas startup = new StartupCanvas();
       startup.setSize(640, 400);
       startup.setBackground(Color.white);
       startup.setImage(this.getImageFromFile(EMULATOR_SPLASHSCREEN_IMAGE));
       
       return startup;
   }

   
   /**
    * Set given screen to existing screen of GUI
    * 
    * @param Canvas screen containing a reference to canvas of module screen
    * 
    */
   public void setScreen(Canvas screen)
   {
       // Replace current canvas with new one
       screenPanel.removeAll();
       screenPanel.add(screen);
       
       // Attach given canvas to existing canvas
       screenCanvas = screen;
       
       // Update panel
       this.updateScreenPanel();
   }

   
   /**
    * Returns a buffered image loaded from specified location
    * 
    * @param String path location where image resides
    * 
    */
   private BufferedImage getImageFromFile(String path)
   {
       BufferedImage image = null;
       try {
           image = ImageIO.read(new File(path));
       }
       catch(IOException e)
       {
           logger.log(Level.WARNING, "GUI error: problem during loading image.");
       }
       return image;
   }

   
   /**
    * Update the screen panel on screen frame.
    * 
    */
   protected void updateScreenPanel()
   {
       // Repaint canvas
       screenPanel.setSize(screenCanvas.getWidth(), screenCanvas.getHeight());
       // FIXME: notice when canvas of screen has changed (different size) and update screenPanel.setSize(width, height);
//       guiWidth = width + 10; // width + 2 * 5 px sidebars
//       guiHeight = height + 2 * panelHeight; // height + menu & statuspanels
//       this.setSize(guiWidth, guiHeight);
//       this.repaint();
       
       screenCanvas.repaint();
   }
   
  
   /**
    * Updates the status panel on screen frame.
    * 
    */
   protected void updateStatusPanel()
   {
       // TODO: implement
       statusPanel.repaint();
   }
   

   /**
    * Update the GUI, including menu and statusbar.
    *  
    * @param   int activity defining the kind of update is required
    */
   public void updateGUI(int activity)
   {
       switch(activity)
       {
           case EMU_PROCESS_START:
               
               // Redefine menu options
               miEmulatorStart.setEnabled(false);
               miEmulatorStop.setEnabled(true);
               miEmulatorReset.setEnabled(true);               
               miEditConfig.setEnabled(false);
               
               break;
               
           case EMU_PROCESS_STOP:
               
               // Redefine menu options
               miEmulatorStart.setEnabled(true);
               miEmulatorStop.setEnabled(false);
               miEmulatorReset.setEnabled(false);
               miSourceEjectA.setEnabled(false);
               miSourceInsertA.setEnabled(false);
               
               // Redefine statusbar
               floppyAPanel.setVisible(false);
               hd1Panel.setVisible(false);                
               miEditConfig.setEnabled(true);
               
               break;

           case EMU_PROCESS_RESET:
               
               // Redefine menu options
               miEmulatorStart.setEnabled(false);
               miEmulatorStop.setEnabled(true);
               miEmulatorReset.setEnabled(true);
               
               miEditConfig.setEnabled(false);
               
               break;

           case EMU_FLOPPYA_INSERT:
               
               // Redefine menu options
               miSourceEjectA.setEnabled(true);
               miSourceInsertA.setEnabled(false);
               // Show A: in statusbar
               floppyAPanel.setVisible(true);
               
               break;

           case EMU_FLOPPYA_EJECT:
               // Redefine menu options
               miSourceEjectA.setEnabled(false);
               miSourceInsertA.setEnabled(true);
               // Hide A: in statusbar
               floppyAPanel.setVisible(false);
               break;

           case EMU_FLOPPYA_TRANSFER_START:
               // Highlight A: in statusbar
               floppyAPanel.getComponent(0).setBackground(Color.GREEN);
               break;
               
           case EMU_FLOPPYA_TRANSFER_STOP:
               // Shadow A: in statusbar
               floppyAPanel.getComponent(0).setBackground(Color.LIGHT_GRAY);
               break;
               
           case EMU_HD1_INSERT:
               // Show HD1 in statusbar
               hd1Panel.setVisible(true);
               break;

           case EMU_HD1_EJECT:
               // Hide HD1 in statusbar
               hd1Panel.setVisible(false);
               break;

           case EMU_HD1_TRANSFER_START:
               // Highlight HD1 in statusbar
               hd1Panel.getComponent(0).setBackground(Color.GREEN);
               break;
               
           case EMU_HD1_TRANSFER_STOP:
               // Shadow HD1 in statusbar
               hd1Panel.getComponent(0).setBackground(Color.LIGHT_GRAY);
               break;
               
           case EMU_KEYBOARD_NUMLOCK_ON:
               scrollPanel.getComponent(0).setBackground(Color.YELLOW);
               break;
               
           case EMU_KEYBOARD_NUMLOCK_OFF:
               scrollPanel.getComponent(0).setBackground(Color.LIGHT_GRAY);
               break;

           case EMU_KEYBOARD_CAPSLOCK_ON:
               capsPanel.getComponent(0).setBackground(Color.YELLOW);
               break;
               
           case EMU_KEYBOARD_CAPSLOCK_OFF:
               capsPanel.getComponent(0).setBackground(Color.LIGHT_GRAY);
               break;

           case EMU_KEYBOARD_SCROLLLOCK_ON:
               scrollPanel.getComponent(0).setBackground(Color.YELLOW);
               break;
               
           case EMU_KEYBOARD_SCROLLLOCK_OFF:
               scrollPanel.getComponent(0).setBackground(Color.LIGHT_GRAY);
               break;
               
           case GUI_RESET:
               // Enable/disable menu items
               miEmulatorStop.setEnabled(false);
               miEmulatorReset.setEnabled(false);
               miEditCopyText.setEnabled(false);
               miEditCopyImage.setEnabled(false);
               miEditPasteText.setEnabled(false);
               miEditPasteImage.setEnabled(false);
               miSourceInsertA.setEnabled(false);
               miSourceEjectA.setEnabled(false);
               
               // Enable/disable status bar items
               floppyAPanel.setVisible(false);
               hd1Panel.setVisible(false);

               break;
               
           default:
               logger.log(Level.WARNING, "No update on GUI could be performed.");
       }
   }

   
   /**
    * Implementation of the interface ActionListener. Takes care of events.
    * 
    * @param ActionEvent e
    */
   public void actionPerformed(ActionEvent e)
   {
       MenuComponent c = (MenuComponent) e.getSource();
       if (c == (MenuComponent) miEmulatorStart)
       {
           // Start emulation process
           emu = new Emulator(this);
           new Thread(emu).start();
           this.updateGUI(EMU_PROCESS_START);
       }
       else if (c == (MenuComponent) miEmulatorStop)
       {
           // Stop emulation process
           emu.stop();
           this.updateGUI(EMU_PROCESS_STOP);
       }
       else if (c == (MenuComponent) miEmulatorReset)
       {
           // Reset emulation process
           emu.reset();
           this.updateGUI(EMU_PROCESS_RESET);
       }
       else if (c == (MenuComponent) miEmulatorQuit)
       {
           // Quit application
           dispose();
           // Check if emulator process is existing, if so shut down
           if (emu != null)
           {
               emu.stop();
           }
           System.exit(0);
       }
       else if (c == (MenuComponent) miSourceEjectA)
       {
           // Eject floppy in drive A
           if (emu.ejectFloppy("A") == true)
           {
               this.updateGUI(EMU_FLOPPYA_EJECT);
           }
       }
       else if (c == (MenuComponent) miSourceInsertA)
       {
           // Insert floppy in drive A
           // Open file select dialog box
           int retval = fcFloppy.showOpenDialog(this);
           if (retval == JFileChooser.APPROVE_OPTION)
           {
               File imageFile = fcFloppy.getSelectedFile();

               // Insert floppy
               boolean writeProtected = false;
               if (emu.insertFloppy("A", (byte) 0x04, imageFile, writeProtected) == true)
               {
                   this.updateGUI(EMU_FLOPPYA_INSERT);
               }
           }
           else if (retval == JFileChooser.ERROR_OPTION)
           {
               JOptionPane.showMessageDialog(this, "Could not select image from file system.");
           }
       }
       else if (c == (MenuComponent) miEditConfig)
       {
                     
          new SelectionConfigDialog(this);
       }     
       else if (c == (MenuComponent) miHelpAbout)
       {
           // Show About dialog
           JOptionPane.showMessageDialog(this, this.getEmulatorName() + "\n"
                                                   + "Version " + this.getEmulatorVersion() + ", Copyright (C) " + this.getEmulatorDate() + " by " + "\n\n"
                                                   + " The National Library of the Netherlands\n"
                                                   + "                               and\n"
                                                   + " The Nationaal Archief of the Netherlands\n"
                                                   + "\n"
                                                   + " This program is free software; you can redistribute it and/or\n"
                                                   + " modify it under the terms of the GNU General Public License\n"
                                                   + " as published by the Free Software Foundation; either version 2\n"
                                                   + " of the License, or (at your option) any later version.\n"
                                                   + "\n"
                                                   + " This program is distributed in the hope that it will be useful,\n"
                                                   + " but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
                                                   + " MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"
                                                   + " GNU General Public License for more details.\n\n\n"
                                                   + " Credits: Bram Lohman, Chris Rose, Jeffrey van der Hoeven");
       }
   }
   
   
   /**
    *  Implement the KeyListener method keyTyped
    *  Empty method, not used 
    */
   public void keyTyped(KeyEvent keyEvent)
   {
       logger.log(Level.FINE, displayInfo(keyEvent, "KEY TYPED: "));
       //FIXME: handle key typed event        emu.generateScancode(keyEvent , KEY_TYPED);
   }
   
   
   /**
    *  Implement the KeyListener method keyPressed
    *  Handles key press events 
    */
   public void keyPressed(KeyEvent keyEvent)
   {
       // Pass keyPress on to keyboard to generate scancode from it
       logger.log(Level.FINE, displayInfo(keyEvent, "KEY PRESSED: "));
       if (emu != null)
       {
           emu.generateScancode(keyEvent , KEY_PRESSED);
       }
   }

   
    /**
     *  Implement the KeyListener method keyReleased
     *  Handles key release events 
     */
   public void keyReleased(KeyEvent keyEvent)
   {
       // Pass keyPress on to keyboard to generate scancode from it
       logger.log(Level.FINE, displayInfo(keyEvent, "KEY RELEASED: "));
       if (emu != null)
       {
           emu.generateScancode(keyEvent, KEY_RELEASED);
       }
   }
   
   
   /**
    * Creates a string containing keypress events
    * 
    * @param keyEvent KeyEvent handled (either Pressed or Released)
    * @param pressReleaseString String passed from the KeyEvent, indicating a press or release
    * 
    * @return  String containing keypress events
    */
   protected String displayInfo(KeyEvent keyEvent, String pressReleaseString)
   {
       String output;
       String keyString, modString, tmpString, actionString, locationString;
       String newline = "\n";

       int keyCode = keyEvent.getKeyCode();
       keyString = "key code = " + keyCode + " (" + KeyEvent.getKeyText(keyCode) + ")";

       int modifiers = keyEvent.getModifiersEx();
       modString = "modifiers = " + modifiers;
       tmpString = KeyEvent.getModifiersExText(modifiers);
       if (tmpString.length() > 0)
       {
           modString += " (" + tmpString + ")";
       } else {
           modString += " (no modifiers)";
       }

       actionString = "action key? ";
       if (keyEvent.isActionKey()) {
           actionString += "YES";
       } else {
           actionString += "NO";
       }

       locationString = "key location: ";
       int location = keyEvent.getKeyLocation();
       if (location == KeyEvent.KEY_LOCATION_STANDARD) {
           locationString += "standard";
       } else if (location == KeyEvent.KEY_LOCATION_LEFT) {
           locationString += "left";
       } else if (location == KeyEvent.KEY_LOCATION_RIGHT) {
           locationString += "right";
       } else if (location == KeyEvent.KEY_LOCATION_NUMPAD) {
           locationString += "numpad";
       } else { // (location == KeyEvent.KEY_LOCATION_UNKNOWN)
           locationString += "unknown";
       }

       output = "[GUI] " + (pressReleaseString + newline
                          + "    " + keyString + newline
                          + "    " + modString + newline
                          + "    " + actionString + newline
                          + "    " + locationString + newline);
       
       return output;
   }

   
   /**
    * Exit the GUI
    *
    */
   protected void exitViewer()
   {
       dispose();
       if (emu != null)
       {
           emu.setActive(false);
       }
       System.exit(0);
   }

   
   /**
    * Versioning information
    * 
    */
   public String toString()
   {
       String info = "\n" + EMULATOR_NAME + ": version " + EMULATOR_VERSION + "\r\n";
       info += "Copyright (C) " + EMULATOR_DATE + " by\r\n";
       info += EMULATOR_CREATOR + "\r\n";
       info += "----------------------------------------------------------------";
       return info;
   }
}

