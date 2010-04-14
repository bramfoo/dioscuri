/*
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
package dioscuri;

import dioscuri.Constants;
import dioscuri.config.ConfigController;
import dioscuri.util.Utilities;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that handles the parameters provided by the user through
 * the command line. The actual parsing is done by Apache's CLI:
 * <a href="http://commons.apache.org/cli/">http://commons.apache.org/cli/</a>
 *
 * @author Bart Kiers
 */
public class CommandLineInterface {

    private static final Logger logger = Logger.getLogger(CommandLineInterface.class.getName());

    dioscuri.config.Emulator emuConfig;
    Options commandLineOptions;
    CommandLine commandLine;
    
    final boolean help;
    final boolean hide;
    final boolean visible;
    final boolean autorun;
    final boolean exit;
    final boolean autoshutdown;
    String configFilePath;

    /**
     * Creates a CommandLineInterface with the provided parameters
     * @param parameters the parameters to be parsed
     * @throws Exception when there are invalid parameters
     */
    public CommandLineInterface(String... parameters) throws Exception {

        initOptions();
        parse(parameters);

        help = commandLine.hasOption("?");
        exit = commandLine.hasOption("e");
        hide = commandLine.hasOption("h");
        visible = !hide;
        autorun = commandLine.hasOption("r");
        autoshutdown = commandLine.hasOption("s");

        if(commandLine.hasOption("?")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar Dioscuri.jar [OPTIONS]\n", commandLineOptions);
            System.exit(0);
        }

        // a custom config file is used
        if(commandLine.hasOption("c")) {
            File cfg = Utilities.resolvePathAsFile(commandLine.getOptionValue("c"));
            logger.log(Level.INFO, "using custom config file: "+cfg);
            if(cfg == null || !cfg.exists()) {
                throw new IOException(" [cli] config file '"+cfg.getName()+
                        "' does not exist in folder '"+cfg.getParentFile().getAbsolutePath()+"'");
            }
            configFilePath = cfg.getAbsolutePath();
        } else {
            configFilePath = Constants.DEFAULT_CONFIG_XML;
        }
        loadConfigFile();

        boolean changes = false;

        if(commandLine.hasOption("f")) {
            File floppyImg = Utilities.resolvePathAsFile(commandLine.getOptionValue("f"));
            logger.log(Level.INFO, " [cli] using custom floppy image: "+floppyImg);
            if(floppyImg == null || !floppyImg.exists()) {
                throw new IOException(" [cli] floppy image '"+floppyImg.getName()+
                        "' does not exist in folder '"+floppyImg.getParentFile().getAbsolutePath()+"'");
            }
            emuConfig.getArchitecture().getModules().getFdc().getFloppy().get(0).setImagefilepath(floppyImg.getAbsolutePath());
            changes = true;
        }

        if(commandLine.hasOption("d")) {
            File hdImg = Utilities.resolvePathAsFile(commandLine.getOptionValue("d"));
            logger.log(Level.INFO, " [cli] using custom hard disk image: "+hdImg);
            if(hdImg == null || !hdImg.exists()) {
                throw new IOException(" [cli] floppy image '"+hdImg.getName()+
                        "' does not exist in folder '"+hdImg.getParentFile().getAbsolutePath()+"'");
            }
            emuConfig.getArchitecture().getModules().getAta().getHarddiskdrive().get(0).setImagefilepath(hdImg.getAbsolutePath());
            changes = true;
        }

        if(commandLine.hasOption("a")) {
            String val = commandLine.getOptionValue("a");
            int bits;
            if(val.matches("16|32")) {
                bits = Integer.valueOf(val);
            } else {
                throw new UnrecognizedOptionException("illegal architecture value: "+val);
            }
            logger.log(Level.INFO, " [cli] setting cpu architecture to: "+bits+" bits");
            emuConfig.getArchitecture().getModules().getCpu().setCpu32Bit(bits == 32);
            changes = true;
        }

        if(commandLine.hasOption("b")) {
            String val = commandLine.getOptionValue("b").toLowerCase();
            String floppy = "Floppy Drive";
            String hd = "Hard Drive";
            boolean hdEnabled = true;
            String boot = hd;
            if(val.matches("floppy|harddisk")) {
                if(val.equals("floppy")) {
                    hdEnabled = false;
                    boot = floppy;
                }
            }
            else {
                throw new UnrecognizedOptionException("illegal boot value: "+boot);
            }
            logger.log(Level.INFO, " [cli] setting boot drive: "+val);
            emuConfig.getArchitecture().getModules().getBios().get(0).getBootdrives().setBootdrive0(boot);
            if(hdEnabled) {
                emuConfig.getArchitecture().getModules().getAta().getHarddiskdrive().get(0).setEnabled(true);
            }
            else {
                emuConfig.getArchitecture().getModules().getFdc().getFloppy().get(0).setEnabled(true);   
            }
            changes = true;
        }

        if(commandLine.hasOption("m")) {
            String val = commandLine.getOptionValue("m").toLowerCase();
            boolean enabled;
            if(val.matches("enabled|disabled")) {
                enabled = val.equals("enabled");
            } else {
                throw new UnrecognizedOptionException("illegal architecture value: "+val);
            }
            logger.log(Level.INFO, " [cli] mouse enabled? "+enabled);
            emuConfig.getArchitecture().getModules().getMouse().setEnabled(enabled);
            changes = true;
        }

        if(changes) {
            Utilities.saveXML(emuConfig, configFilePath);
        }
    }

    /*
     * load the config file
     */
    private void loadConfigFile() throws Exception {
        File config = Utilities.resolvePathAsFile(configFilePath);
        emuConfig = ConfigController.loadFromXML(config);
    }

    /*
     * initialize the options
     */
    private void initOptions() {
        commandLineOptions = new Options();

/* ? */ commandLineOptions.addOption("?", "help", false, "print this message");
/* h */ commandLineOptions.addOption("h", "hide", false, "hides the GUI");
/* r */ commandLineOptions.addOption("r", "autorun", false, "emulator will directly start emulation process");
/* e */ commandLineOptions.addOption("e", "exit", false, "used for testing purposes, will cause Dioscuri to exit immediately");
/* s */ commandLineOptions.addOption("s", "autoshutdown", false, "emulator will shutdown automatically when emulation process is finished");

/* c */ Option config = new Option("c", "config", true, "loads a custom config xml file");
        config.setArgName("file");
        commandLineOptions.addOption(config);

/* f */ config = new Option("f", "floppy", true, "loads a custom floppy image");
        config.setArgName("file");
        commandLineOptions.addOption(config);

/* d */ config = new Option("d", "harddisk", true, "loads a custom hard disk image");
        config.setArgName("file");
        commandLineOptions.addOption(config);

/* a */ config = new Option("a", "architecture", true, "sets the cpu's architecture");
        config.setArgName("'16'|'32'");
        commandLineOptions.addOption(config);

/* b */ config = new Option("b", "boot", true, "sets the boot drive");
        config.setArgName("'floppy'|'harddisk'");
        commandLineOptions.addOption(config);

/* m */ config = new Option("m", "mouse", true, "enables or disables th mouse");
        config.setArgName("'enabled'|'disabled'");
        commandLineOptions.addOption(config);
    }

    /*
     * parse the parameters
     */
    private void parse(String[] parameters) throws ParseException {
        CommandLineParser parser = new PosixParser();
        commandLine = parser.parse(commandLineOptions, parameters);
    }
}
