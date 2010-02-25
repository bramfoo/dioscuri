package dioscuri;

import java.io.File;

public final class Constants {

    private Constants() {}
    
    // Constants
    public static final File JAR_OR_FOLDER = new File(GUI.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    public static final File EXE_FOLDER = JAR_OR_FOLDER.isFile() ? JAR_OR_FOLDER.getParentFile() : JAR_OR_FOLDER;
    public static final String EMULATOR_NAME = "Dioscuri - modular emulator for digital preservation";
    public static final String EMULATOR_VERSION = "0.4.3";
    public static final String EMULATOR_DATE = "January, 2010";
    public static final String EMULATOR_CREATOR = "Koninklijke Bibliotheek (KB), Nationaal Archief of the Netherlands, Planets, KEEP";
    public static final String CONFIG_DIR = new File(EXE_FOLDER, "config").getAbsolutePath();
    public static final String EMULATOR_ICON_IMAGE = new File(CONFIG_DIR, "dioscuri_icon.gif").getAbsolutePath();
    public static final String EMULATOR_SPLASHSCREEN_IMAGE = new File(CONFIG_DIR, "dioscuri_splashscreen_2010_v043.gif").getAbsolutePath();
    public static final String EMULATOR_LOGGING_PROPERTIES = new File(CONFIG_DIR, "logging.properties").getAbsolutePath();
    public static final String DEFAULT_CONFIG_XML = new File(CONFIG_DIR, "DioscuriConfig.xml").getAbsolutePath();
    public static final String BOCHS_BIOS = new File(EXE_FOLDER, "images/bios/BIOS-bochs-latest").getAbsolutePath();
    public static final String VGA_BIOS = new File(EXE_FOLDER, "images/bios/VGABIOS-lgpl-latest").getAbsolutePath();
}
