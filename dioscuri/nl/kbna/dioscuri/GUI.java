/*
 * $Revision: 1.16 $ $Date: 2009-04-24 10:57:43 $ $Author: jrvanderhoeven $
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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
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
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.MouseInputListener;

import nl.kbna.dioscuri.config.ConfigController;
import nl.kbna.dioscuri.config.DioscuriXmlReader;
import nl.kbna.dioscuri.config.DioscuriXmlWriter;
import nl.kbna.dioscuri.config.SelectionConfigDialog;
import nl.kbna.dioscuri.datatransfer.TextTransfer;

/**
*
* Graphical User Interface for emulator.
*/
public class GUI extends JFrame implements ActionListener, KeyListener
{
   // Attributes
   private Emulator emu;
   private ConfigController configController;
   private TextTransfer textTransfer;
   
   // Panels
   private JScrollPane screenPane;
   private JPanel statusPanel;
   private JPanel scrolllockPanel;
   private JPanel numlockPanel;
   private JPanel capslockPanel;
   private JPanel floppyAPanel;
   private JPanel hd1Panel;
   
   // Menus
   JMenuBar menuBar;
   JMenu menuEmulator;
   JMenu menuEdit;
   JMenu menuMedia;
   JMenu menuDevices;
   JMenu menuConfig;
   JMenu menuHelp;

   // Menu items
   // Menu emulator
   JMenuItem miEmulatorStart;
   JMenuItem miEmulatorStop;
   JMenuItem miEmulatorReset;
   JMenuItem miEmulatorQuit;
   // Menu edit
   JMenuItem miEditCopyText;
   JMenuItem miEditCopyImage;
   JMenuItem miEditPasteText;
   JMenuItem miEditPasteImage;
   // Menu media
   JMenuItem miMediaEjectA;
   JMenuItem miMediaInsertA;
   // Menu devices
   JMenuItem miDevicesMouseEnabled;
   JMenuItem miDevicesMouseDisabled;
   // Menu configure
   JMenuItem miEditConfig;
   // Menu help
   JMenuItem miHelpAbout;
   
   // Input/output devices
   private JPanel screen;     // Defines the screen of the emulator
   private MouseHandler mouseHandler;
   
   // File selection
   private JFileChooser fcFloppy;
   
   // Logging
   private static Logger logger = Logger.getLogger("nl.kbna.dioscuri.gui");
   
   // Frame and refresh properties
   private int guiWidth;
   private int guiHeight;
   
   // Command line parameter settings
   private boolean guiHidden;
   private String configFilePath;
   private boolean autorun;
   private boolean autoshutdown;
   
   
   // Constants
   // Emulator characteristics
   protected final static String EMULATOR_NAME = "Dioscuri - modular emulator for digital preservation";
   protected final static String EMULATOR_VERSION = "0.4.1";
   protected final static String EMULATOR_DATE = "April, 2009";
   protected final static String EMULATOR_CREATOR = "Koninklijke Bibliotheek (KB), Nationaal Archief of the Netherlands, Planets, KEEP";
   private final static String EMULATOR_ICON_IMAGE = "config/dioscuri_icon.gif";
   private final static String EMULATOR_SPLASHSCREEN_IMAGE = "config/dioscuri_splashscreen_2008_v040.gif";
   
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
   public static final int EMU_DEVICES_MOUSE_ENABLED	= 17;
   public static final int EMU_DEVICES_MOUSE_DISABLED	= 18;
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
       new GUI(args);
       
   }

   
   // Constructor
   /**
    * Class constructor
    */
   public GUI()
   {
       // Print startup information of modular emulator
       logger.log(Level.SEVERE, this.toString());
       
       // Create graphical user interface

       // Add handlers and listeners
       // Window closing listener
       this.addWindowListener(new WindowAdapter(){
           public void windowClosing(WindowEvent event)
           {
               exitDioscuri();
           }
       });

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
       screenPane = new JScrollPane();
       screenPane.setBackground(Color.gray);
       this.setScreen(this.getStartupScreen());
       
       // Create panel: statusbar (including panels w/ borders for Num Lock, Caps Lock and Scroll Lock status)
       statusPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
       this.initStatusBar();

       // Add panels to frame (arranged in borderlayout)
       this.getContentPane().add(screenPane, BorderLayout.CENTER);
       this.getContentPane().add(statusPanel, BorderLayout.SOUTH);
       
       // Create file choosers
       fcFloppy = new JFileChooser();
       
       // Set dimensions
       guiWidth = screenPane.getWidth() + 10; // screen width + a random extra value
       guiHeight = screenPane.getHeight() + 2 * 38; // screen height + 2 * menu & statusbar height

       // Actions
       // Key handler to the GUI, disabling focus traversal so Tab events are available
       // KeyEvents will be handled here in screen
       this.addKeyListener(this);
       // Disable moving focus to the next component in the focus cycle root
       this.setFocusTraversalKeysEnabled(false);
       
       // Build frame
       this.setLocation(GUI_X_LOCATION, GUI_Y_LOCATION);
       this.setSize(guiWidth, guiHeight);
       this.setTitle(this.getEmulatorName());
       this.setResizable(false);
       this.updateGUI(GUI_RESET);
       
       // Create configuration controller (uses default config file path)
	   configController = new ConfigController(this);
       
       // Create clipboard functionality
       textTransfer = new TextTransfer(this);
   }

   public GUI(String[] arguments)
   {
	   // Define GUI
	   this();
	   
	   // Interpret arguments (if any)
	   // The following arguments are allowed:
	   // -c "<CONFIGPATH_FILE>"	: uses given config.xml file instead of default. If not available, default will be used
	   // -h						: hide GUI
	   // autorun					: emulator will directly start emulation process
	   // autoshutdown				: emulator will shutdown automatically when emulation process is finished
	   // Example: java -jar Dioscuri.jar -c "c:\emulators\configs\dioscuri_config.xml" autorun
	   
	   // Reset argument parameters
	   configFilePath = "";
	   guiHidden = false;
	   autorun = false;
	   autoshutdown = false;
	   
	   // Check arguments
	   for (int a = 0; a < arguments.length; a++)
	   {
		   // Argument -c
		   if (arguments[a].equalsIgnoreCase("-c"))
		   {
			   // Fetch next argument
			   a++;
			   if(a < arguments.length) 
			   {
			       configFilePath = arguments[a];
			   }
			   else
			   {
			       logger.log(Level.SEVERE, "[gui] No path provided after '-c' option.");
			   }
		   }
		   
		   // Argument -h
		   else if (arguments[a].equalsIgnoreCase("-h"))
		   {
			   guiHidden = true;
		   }

		   // Argument autorun
		   else if (arguments[a].equalsIgnoreCase("autorun"))
		   {
			   autorun = true;
		   }
		   
		   // Argument autoshutdown
		   else if (arguments[a].equalsIgnoreCase("autoshutdown"))
		   {
			   autoshutdown = true;
		   }
		   
		   // Wrong argument
		   else
		   {
	           logger.log(Level.SEVERE, "[gui] Error in command line parameters during invocation of Dioscuri");
		   }
	   }

	   // Perform argument actions
	   // Change configuration file
	   if (!configFilePath.equals("") && configController.setConfigFilePath(configFilePath) == true)
	   {
           logger.log(Level.SEVERE, "[gui] Configuration path changed in " + configFilePath);
	   }
	   
       // Show / hide GUI (based on command line parameter)
	   if (guiHidden == true)
	   {
	       this.setVisible(false);
           logger.log(Level.SEVERE, "[gui] GUI is hidden");
	   }
	   else
	   {
	       this.setVisible(true);
	       this.requestFocus();
           logger.log(Level.SEVERE, "[gui] GUI is visible and has focus");
	   }

	   // Automatically start emulation process
	   if (autorun == true)
	   {
           emu = new Emulator(this, configController);
           new Thread(emu).start();
           this.updateGUI(EMU_PROCESS_START);
	   }
   }
   
   // Methods
   
   /**
    * Initialise menu bar
    */
   private void initMenuBar()
   {
       // Create a menubar
       menuBar = new JMenuBar();
       
       // Create menu: emulator
       menuEmulator = new JMenu("Emulator");
       miEmulatorStart = new JMenuItem("Start process (power on)");
       miEmulatorStop = new JMenuItem("Stop process (shutdown)");
       miEmulatorReset = new JMenuItem("Reset process (warm reset)");
       miEmulatorQuit = new JMenuItem("Quit");
       menuEmulator.add(miEmulatorStart);
       menuEmulator.add(miEmulatorStop);
       menuEmulator.add(miEmulatorReset);
       menuEmulator.add(miEmulatorQuit);

       // Create menu: edit
       menuEdit = new JMenu("Edit");
       miEditCopyText = new JMenuItem("Copy text");
       miEditCopyImage = new JMenuItem("Copy image");
       miEditPasteText = new JMenuItem("Paste text");
       miEditPasteImage = new JMenuItem("Paste image");
       menuEdit.add(miEditCopyText);
       menuEdit.add(miEditCopyImage);
       menuEdit.add(miEditPasteText);
       menuEdit.add(miEditPasteImage);
       
       // Create menu: media
       menuMedia = new JMenu("Media");
       miMediaEjectA = new JMenuItem("Eject floppy A:");
       miMediaInsertA = new JMenuItem("Insert floppy A:");
       menuMedia.add(miMediaEjectA);
       menuMedia.add(miMediaInsertA);
       
       // Create menu: devices
       menuDevices = new JMenu("Devices");
       miDevicesMouseEnabled = new JMenuItem("Mouse: enable");
       miDevicesMouseDisabled = new JMenuItem("Mouse: disable");
       menuDevices.add(miDevicesMouseEnabled);
       menuDevices.add(miDevicesMouseDisabled);

       // Create menu: config        
       menuConfig = new JMenu("Configure");           
       miEditConfig = new JMenuItem("Edit Config");  
       menuConfig.add(miEditConfig);
       
       // Create menu: help
       menuHelp = new JMenu("Help");
       miHelpAbout = new JMenuItem("About..");
       menuHelp.add(miHelpAbout);
       
       // Assign all menus to the menubar
       menuBar.add(menuEmulator);
       menuBar.add(menuEdit);
       menuBar.add(menuMedia);
       menuBar.add(menuDevices);
       menuBar.add(menuConfig);
       menuBar.add(menuHelp);
       
       // Add action listeners for tracing events
       miEmulatorStart.addActionListener(this);
       miEmulatorStop.addActionListener(this);
       miEmulatorReset.addActionListener(this);
       miEmulatorQuit.addActionListener(this);
       miEditCopyText.addActionListener(this);
       miMediaEjectA.addActionListener(this);
       miMediaInsertA.addActionListener(this);    
       miDevicesMouseEnabled.addActionListener(this);
       miDevicesMouseDisabled.addActionListener(this);
       miEditConfig.addActionListener(this);       
       miHelpAbout.addActionListener(this);

       // Assign menubar to frame
       this.setJMenuBar(menuBar);
   }


   /**
    * Initialise status bar
    */
   private void initStatusBar()
   {
       Border blackline;
       blackline = BorderFactory.createLineBorder(Color.black);

       numlockPanel = new JPanel();
//       numlockPanel.setLayout(new BoxLayout(numlockPanel, BoxLayout.X_AXIS));
       JLabel numlockPanelLabel = new JLabel("1");
       numlockPanelLabel.setAlignmentX(JLabel.CENTER);
       numlockPanel.add(numlockPanelLabel);
       numlockPanel.setBorder(blackline);
       numlockPanel.setSize(20, 20);

       capslockPanel = new JPanel();
//       capslockPanel.setLayout(new BoxLayout(capslockPanel, BoxLayout.X_AXIS));
       JLabel capslockPanelLabel = new JLabel("A");
       capslockPanelLabel.setHorizontalAlignment(JLabel.CENTER);
       capslockPanel.add(capslockPanelLabel);
       capslockPanel.setBorder(blackline);

       scrolllockPanel = new JPanel();
//       scrolllockPanel.setLayout(new BoxLayout(scrolllockPanel, BoxLayout.X_AXIS));
       JLabel scrolllockPanelLabel = new JLabel("S");
       scrolllockPanelLabel.setHorizontalAlignment(JLabel.CENTER);
       scrolllockPanel.add(scrolllockPanelLabel);
       scrolllockPanel.setBorder(blackline);
       
       floppyAPanel = new JPanel();
//       floppyAPanel.setLayout(new BoxLayout(floppyAPanel, BoxLayout.X_AXIS));
       JLabel floppyAPanelLabel = new JLabel("A:");
       floppyAPanelLabel.setHorizontalAlignment(JLabel.CENTER);
       floppyAPanel.add(floppyAPanelLabel);
       floppyAPanel.setBorder(blackline);
       
       hd1Panel = new JPanel();
//       hd1Panel.setLayout(new BoxLayout(hd1Panel, BoxLayout.X_AXIS));
       JLabel hd1PanelLabel = new JLabel("HD1");
       hd1PanelLabel.setHorizontalAlignment(JLabel.CENTER);
       hd1Panel.add(hd1PanelLabel);
       hd1Panel.setBorder(blackline);

       // Add panels to statusbar (with spaces inbetween)
       statusPanel.add(Box.createHorizontalGlue());
       statusPanel.add(numlockPanel);
       statusPanel.add(Box.createRigidArea(new Dimension(5,0)));
       statusPanel.add(capslockPanel);
       statusPanel.add(Box.createRigidArea(new Dimension(5,0)));
       statusPanel.add(scrolllockPanel);
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
   private JPanel getStartupScreen()
   {
       // Create startup screen
       StartupPanel startup = new StartupPanel();
       startup.setSize(720, 400);
       startup.setImage(this.getImageFromFile(EMULATOR_SPLASHSCREEN_IMAGE));
       
       return startup;
   }

   
   /**
    * Set given screen to existing screen of GUI
    * 
    * @param Canvas screen containing a reference to canvas of module screen
    * @param boolean mouseEnabled to denote if emulated mouse can be used in screen
    * 
    */
   public void setScreen(JPanel screen)
   {
       // Replace current canvas with new one
       screenPane.removeAll();
       screenPane.add(screen);
       
       // Attach current screen to given screen
       this.screen = screen;
       
       // Update panel
       this.updateScreenPanel();
   }

   /**
    * Enable mouse support in GUI
    *
    * @return true if mouse enabled, false otherwise
    */
   public boolean setMouseEnabled()
   {

	   // Mouse handler to the GUI
	   mouseHandler = new MouseHandler();
       screen.addMouseListener(mouseHandler);
       screen.addMouseMotionListener(mouseHandler);
	   logger.log(Level.SEVERE, "[gui] Mouse in GUI enabled");
       
       return true;
   }
   
   /**
    * Disable mouse support in GUI
    *
    * @return true if mouse enabled, false otherwise
    */
   public boolean setMouseDisabled()
   {
       // Mouse handler to the GUI
	   if (mouseHandler != null)
	   {
	       screen.removeMouseListener(mouseHandler);
	       screen.removeMouseMotionListener(mouseHandler);
	       mouseHandler.setMouseCursorVisibility(true);
	       mouseHandler = null;
		   logger.log(Level.SEVERE, "[gui] Mouse in GUI disabled");
		   
		   return true;
	   }
       
	   logger.log(Level.SEVERE, "[gui] Mouse does not exist or is already disabled");

	   return true;
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
       screenPane.setSize(screen.getWidth(), screen.getHeight());
       // FIXME: notice when canvas of screen has changed (different size) and update screenPanel.setSize(width, height);
//       guiWidth = width + 10; // width + 2 * 5 px sidebars
//       guiHeight = height + 2 * panelHeight; // height + menu & statuspanels
//       this.setSize(guiWidth, guiHeight);
//       this.repaint();
       
       screen.repaint();
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
               miEditCopyText.setEnabled(true);
               break;
               
           case EMU_PROCESS_STOP:
               
               // Redefine menu options
               miEmulatorStart.setEnabled(true);
               miEmulatorStop.setEnabled(false);
               miEmulatorReset.setEnabled(false);
               miMediaEjectA.setEnabled(false);
               miMediaInsertA.setEnabled(false);
               miEditCopyText.setEnabled(false);
               miDevicesMouseEnabled.setEnabled(false);
               miDevicesMouseDisabled.setEnabled(false);
               
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
               miMediaEjectA.setEnabled(true);
               miMediaInsertA.setEnabled(false);
               // Show A: in statusbar
               floppyAPanel.setVisible(true);
               
               break;

           case EMU_FLOPPYA_EJECT:
               // Redefine menu options
               miMediaEjectA.setEnabled(false);
               miMediaInsertA.setEnabled(true);
               // Hide A: in statusbar
               floppyAPanel.setVisible(false);
               break;

           case EMU_FLOPPYA_TRANSFER_START:
               // Highlight A: in statusbar
        	   // NOTE: Used to use floppyAPanel.getComponent(0) to retrieve comp, 
        	   // but as it is not part of layout anymore it is not necesarry
               floppyAPanel.setBackground(Color.GREEN);
               break;
               
           case EMU_FLOPPYA_TRANSFER_STOP:
               // Shadow A: in statusbar
               floppyAPanel.setBackground(Color.LIGHT_GRAY);
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
               hd1Panel.setBackground(Color.GREEN);
               break;
               
           case EMU_HD1_TRANSFER_STOP:
               // Shadow HD1 in statusbar
               hd1Panel.setBackground(Color.LIGHT_GRAY);
               break;
               
           case EMU_KEYBOARD_NUMLOCK_ON:
               numlockPanel.setBackground(Color.YELLOW);
               break;
               
           case EMU_KEYBOARD_NUMLOCK_OFF:
        	   numlockPanel.setBackground(Color.LIGHT_GRAY);
               break;

           case EMU_KEYBOARD_CAPSLOCK_ON:
               capslockPanel.setBackground(Color.YELLOW);
               break;
               
           case EMU_KEYBOARD_CAPSLOCK_OFF:
               capslockPanel.setBackground(Color.LIGHT_GRAY);
               break;

           case EMU_KEYBOARD_SCROLLLOCK_ON:
               scrolllockPanel.setBackground(Color.YELLOW);
               break;
               
           case EMU_KEYBOARD_SCROLLLOCK_OFF:
               scrolllockPanel.setBackground(Color.LIGHT_GRAY);
               break;
               
           case EMU_DEVICES_MOUSE_ENABLED:
               miDevicesMouseEnabled.setEnabled(false);
               miDevicesMouseDisabled.setEnabled(true);
               break;

           case EMU_DEVICES_MOUSE_DISABLED:
               miDevicesMouseEnabled.setEnabled(true);
               miDevicesMouseDisabled.setEnabled(false);
               break;

           case GUI_RESET:
               // Enable/disable menu items
               miEmulatorStop.setEnabled(false);
               miEmulatorReset.setEnabled(false);
               miEditCopyText.setEnabled(false);
               miEditCopyImage.setEnabled(false);
               miEditPasteText.setEnabled(false);
               miEditPasteImage.setEnabled(false);
               miMediaInsertA.setEnabled(false);
               miMediaEjectA.setEnabled(false);
               miDevicesMouseEnabled.setEnabled(false);
               miDevicesMouseDisabled.setEnabled(false);
               
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
       JComponent c = (JComponent) e.getSource();
       if (c == (JComponent) miEmulatorStart)
       {
           // Start emulation process
           emu = new Emulator(this, configController);
           new Thread(emu).start();
           this.updateGUI(EMU_PROCESS_START);
       }
       else if (c == (JComponent) miEmulatorStop)
       {
           // Stop emulation process
           emu.stop();
           this.setMouseDisabled();
           this.updateGUI(EMU_PROCESS_STOP);
       }
       else if (c == (JComponent) miEmulatorReset)
       {
           // Reset emulation process
           emu.reset();
           this.updateGUI(EMU_PROCESS_RESET);
       }
       else if (c == (JComponent) miEmulatorQuit)
       {
           // Quit application
    	   this.exitDioscuri();
       }
       else if (c == (JComponent) miEditCopyText)
       {
    	   // Copy text from screen to clipboard
    	   if (emu != null)
    	   {
    		   String text = emu.getScreenText();
    		   if (text != null)
    		   {
    			   // Text has been extracted from emulation process
    			   // Send text to clipboard
        		   textTransfer.setClipboardContents(text);
        		   
        		   // TODO: update GUI to allow pasting text
    		   }
    	   }
       }
       else if (c == (JComponent) miMediaEjectA)
       {
           // Eject floppy in drive A
           if (emu.ejectFloppy("A") == true)
           {
               this.updateGUI(EMU_FLOPPYA_EJECT);
           }
       }
       else if (c == (JComponent) miMediaInsertA)
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
       else if (c == (JComponent) miDevicesMouseEnabled)
       {
           // Enable mouse
    	   this.setMouseEnabled();
           this.updateGUI(EMU_DEVICES_MOUSE_ENABLED);
       }
       else if (c == (JComponent) miDevicesMouseDisabled)
       {
           // Disable mouse
    	   this.setMouseDisabled();
           this.updateGUI(EMU_DEVICES_MOUSE_DISABLED);
       }
       else if (c == (JComponent) miEditConfig)
       {
                     
          new SelectionConfigDialog(this);
       }     
       else if (c == (JComponent) miHelpAbout)
       {
           // Show About dialog
           JOptionPane.showMessageDialog(this, this.getEmulatorName() + "\n"
                                                   + "Version " + this.getEmulatorVersion() + ", Copyright (C) " + this.getEmulatorDate() + " by " + "\n\n"
                                                   + " Koninklijke Bibliotheek (KB, the national Library of the Netherlands)\n"
                                                   + " The Nationaal Archief of the Netherlands\n"
                                                   + " Planets project\n"
                                                   + " KEEP project\n"
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
                                                   + " Credits: Bram Lohman, Chris Rose, Bart Kiers, Jeffrey van der Hoeven");
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
           emu.notifyKeyboard(keyEvent , KEY_PRESSED);
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
           emu.notifyKeyboard(keyEvent, KEY_RELEASED);
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
   
   public DioscuriXmlReader getXMLReader()
   {
	   return configController.getXMLReader();
   }

   
   public DioscuriXmlWriter getXMLWriter()
   {
	   return configController.getXMLWriter();
   }

   
   /**
    * Notify GUI about status of emulation process and take appropriate GUI action
    * 
    * @param int emulatorStatus indicates the state change of the emulator
    *
    */
   protected void notifyGUI(int emulatorStatus)
   {
		// Check which kind of notification is given
	   if (emulatorStatus == GUI.EMU_PROCESS_STOP)
	   {
		   if (this.autoshutdown == true)
		   {
			   // Exit application
			   this.exitDioscuri();
		   }
		   else
		   {
			   // Update GUI status
			   this.updateGUI(GUI.EMU_PROCESS_STOP);
		   }
	   }

	   // TODO: add all notifications here that are done by emulator class. Currently, emulator class is directly calling gui.update(..)
	   
		
	}
	   
   /**
    * Exit the GUI and stop the application
    *
    */
   private void exitDioscuri()
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
       String info = "---------------------------------------------------------------------------------------" + "\n";
       info += "| " + EMULATOR_NAME + ": version " + EMULATOR_VERSION + "                 |" + "\r\n";
       info += "| " + "Copyright (C) " + EMULATOR_DATE + "                                                        |" + "\r\n";
       info += "| " + EMULATOR_CREATOR + " |" + "\r\n";
       info += "---------------------------------------------------------------------------------------" + "\n";
       return info;
   }


   /**
    * Inner class MouseHandler
    * Takes care of any mouse action (motion, clicking)
    * 
    */
   private class MouseHandler implements MouseInputListener
   {
	   // Attributes
	   Cursor invisibleCursor;
	   
	   public MouseHandler()
	   {
		   // Make cursor invisible
//		   this.setMouseCursorVisible(false);
	   }
	   
	   
		public void mouseClicked(MouseEvent mouseEvent)
		{
			// Probably not needed
		}
		
		
		public void mousePressed(MouseEvent mouseEvent)
		{
			// Probably not needed
		}
		
		
		public void mouseReleased(MouseEvent mouseEvent)
		{
		       if (emu != null)
		       {
		           emu.notifyMouse(mouseEvent);
		       }
		}
		
		
		public void mouseEntered(MouseEvent mouseEvent)
		{
		   this.setMouseCursorVisibility(false);
		}
		
		
		public void mouseExited(MouseEvent mouseEvent)
		{
			// Probably not needed
		}
		
		
		public void mouseDragged(MouseEvent mouseEvent)
		{
			// Probably not needed
		}
		
		
		public void mouseMoved(MouseEvent mouseEvent)
		{
	       if (emu != null)
	       {
	           emu.notifyMouse(mouseEvent);
	       }
		}
		
		public void setMouseCursorVisibility(boolean visible)
		{
			if (visible == false)
			{
				// Hide cursor
				ImageIcon emptyIcon = new ImageIcon(new byte[0]);
				invisibleCursor = getToolkit().createCustomCursor(emptyIcon.getImage(), new Point(0,0), "Invisible");
		        screen.setCursor(invisibleCursor);
			}
			else
			{
				// Show cursor
				screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
			
		}
   }


}

