package dioscuri;

import java.awt.Frame;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;

import dioscuri.config.Emulator;

public interface GUI {
	
	// Constants
    // Emulator characteristics
    public final static String EMULATOR_NAME				= "Dioscuri - modular emulator for digital preservation";
    public final static String EMULATOR_VERSION			    = "0.4.2";
    public final static String EMULATOR_DATE				= "April, 2009";
    public final static String EMULATOR_CREATOR			    = "Koninklijke Bibliotheek (KB), Nationaal Archief of the Netherlands, Planets, KEEP";
    public final static String CONFIG_DIR					= "config";
    public final static String EMULATOR_ICON_IMAGE			= "dioscuri_icon.gif";
    public final static String EMULATOR_SPLASHSCREEN_IMAGE	= "dioscuri_splashscreen_2008_v040.gif";
    public final static String EMULATOR_LOGGING_PROPERTIES	= "logging.properties";
    public final static String CONFIG_XML					= "DioscuriConfig.xml";
    public final static String JAR_CONFIG_XML				= File.separator + CONFIG_DIR + File.separator + CONFIG_XML;

    // Dimension settings
    public static final int GUI_X_LOCATION = 200;
    public static final int GUI_Y_LOCATION = 200;
    
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
    public static final int EMU_DEVICES_MOUSE_ENABLED	 = 17;
    public static final int EMU_DEVICES_MOUSE_DISABLED	 = 18;
    public static final int GUI_RESET                    = 99;
    
    // Key events
    public static final int KEY_PRESSED    = 0;
    public static final int KEY_RELEASED   = 1;
    public static final int KEY_TYPED      = 2;
	
    public JFrame asJFrame();

	public boolean saveXML(Emulator params);

	public Emulator getEmuConfig();

    public String getConfigFilePath();

    public void notifyGUI(int emuProcess);

    public void updateGUI(int activity);

    public void setScreen(JPanel screen);

    public boolean setMouseEnabled();

    public boolean setMouseDisabled();
}
