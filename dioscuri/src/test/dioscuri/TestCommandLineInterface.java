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

import dioscuri.CommandLineInterface;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Unit tests for all command line options from CommandLineInterface
 *
 * @author Bart Kiers
 */
public class TestCommandLineInterface {

    private static Logger logger = Logger.getLogger(TestCommandLineInterface.class.getName());

    /*
     * Create and return a temp file in the system's temp-folder. If this is not possible, return null.
     */

    private File createTempFile() {
        File tmp = new File(System.getProperty("java.io.tmpdir"), "tmp-dioscuri" + System.currentTimeMillis());
        try {
            tmp.createNewFile();
            tmp.deleteOnExit();
        } catch (IOException e) {
            tmp = null;
        }
        return tmp;
    }

    /*
     * Create a CommandLineInterface with a custom config file
     */

    private CommandLineInterface parseCommandLineInterface(String... params) throws Exception {
        logger.log(Level.INFO, " [test] trying to parse: " + Arrays.toString(params));
        String[] allParams = new String[params.length + 2];
        allParams[0] = "-c";
        allParams[1] = "C:\\BK\\IntelliJ\\dioscuri_043_paths\\config\\DioscuriConfig.xml";
        System.arraycopy(params, 0, allParams, 2, params.length);
        return new CommandLineInterface(allParams);
    }

    // UnrecognizedOptionException
    /*
     * Test some invalid command line parameters
     */

    private void testInValid(String... params) throws Exception {
        try {
            parseCommandLineInterface(params);
        } catch (Exception e) {
            // exception is expected
            return;
        }
        throw new RuntimeException("exception expected for input: " + Arrays.toString(params));
    }

    /*
     * Test some valid command line parameters
     */

    private void testValid(String... params) throws Exception {
        parseCommandLineInterface(params);
    }

    /**
     * Test all valid parameters:
     * <pre>
     *   -?,--help                         print this message
     *   -a,--architecture <'16'|'32'>     the cpu's architecture
     *   -b,--boot <'floppy'|'harddisk'>   the boot drive
     *   -c,--config <file>                a custom config xml file
     *   -d,--harddisk <file>              a custom hard disk image
     *   -e,--exit                         used for testing purposes, will cause
     *                                     Dioscuri to exit immediately
     *   -f,--floppy <file>                a custom floppy image
     *   -h,--hide                         hide the GUI
     *   -r,--autorun                      emulator will directly start emulatio
     *                                     process
     *   -s,--autoshutdown                 emulator will shutdown automatically
     *                                     when emulation process is finished
     * </pre>
     *
     * @throws Exception -
     */
    @Test
    public void testAllValid() throws Exception {
        Options options = parseCommandLineInterface().commandLineOptions;

        // no parameters is valid, of course
        testValid("");

        // test all single options
        for (Object o : options.getOptions()) {
            Option op = (Option) o;
            if (!op.hasArg()) {
                testValid("-" + op.getOpt());
                testValid("--" + op.getLongOpt());
            }
        }
        // test some multiple params
        testValid("-he");
        testValid("-h", "-e", "-s");

        // test the options that need a valid input as 2nd parameter
        File temp = createTempFile();
        if (temp != null) {
            // couldn't create a temp file, skip : -cfd
            testValid("-c", temp.getAbsolutePath());
            testValid("-f", temp.getAbsolutePath());
            testValid("-d", temp.getAbsolutePath());
        }

        testValid("-b", "floppy");
        testValid("-b", "HARDdisk"); // case insensitive

        testValid("-a", "16");
        testValid("-a", "32");
    }

    /**
     * Test all invalid parameters. Valid ones are:
     * <pre>
     *   -?,--help                         print this message
     *   -a,--architecture <'16'|'32'>     the cpu's architecture
     *   -b,--boot <'floppy'|'harddisk'>   the boot drive
     *   -c,--config <file>                a custom config xml file
     *   -d,--harddisk <file>              a custom hard disk image
     *   -e,--exit                         used for testing purposes, will cause
     *                                     Dioscuri to exit immediately
     *   -f,--floppy <file>                a custom floppy image
     *   -h,--hide                         hide the GUI
     *   -r,--autorun                      emulator will directly start emulatio
     *                                     process
     *   -s,--autoshutdown                 emulator will shutdown automatically
     *                                     when emulation process is finished
     * </pre>
     *
     * @throws Exception -
     */
    @Test
    public void testAllInvalid() throws Exception {
        testInValid("-b", "flopy"); // missing 'p'
        testInValid("-b", "hd");

        testInValid("-a", "-16");
        testInValid("-a", "166");
        testInValid("-a", "31");

        testInValid("-FOO", "--BAR");
        testInValid("-X");
    }
}
