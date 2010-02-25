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

public class TestCommandLineInterface {
    /*
    private CommandLineInterface getCommandLineInterface() throws Exception {
        boolean testing = true;
        CommandLineInterface cli = new CommandLineInterface(testing,
                "C:/BK/IntelliJ/dioscuri_043_paths/config/DioscuriConfig.xml");
        return cli;
    }

    private void testParams(String... params) throws Exception {
        getCommandLineInterface().parse(params);
    }
    
    @Test(expected=ParseException.class)  
    public void mainTestInvalidParamA() throws Exception {
        // a non-existant parameter
        testParams("-FOO");
    }

    @Test(expected=ParseException.class)
    public void mainTestInvalidParamB() throws Exception {
        // a non-existant parameter
        testParams("-unknown");
    }

    @Test(expected=ParseException.class)
    public void mainTestInvalidParamC() throws Exception {
        // a non-existant parameter
        testParams("-H");
    }

    @Test(expected= IOException.class)
    public void mainTestInvalidParamD() throws Exception {
        // a non-existant file: aNoneExistingFile.xml
        testParams("-c", "aNoneExistingFile.xml");
    }

    @Test
    public void mainTestValidParamsA() throws Exception {
        // test all parameters without a value
        CommandLineInterface cli = getCommandLineInterface();
        for(Object o : cli.commandLineOptions.getOptions()) {
            Option op = (Option)o;
            if(!op.hasArg()) {
                testParams("-"+op.getLongOpt());
                testParams("--"+op.getLongOpt());
                testParams("-"+op.getOpt());
                testParams("--"+op.getOpt());
            }
        }

        // multiple single parameters
        testParams("-sha");
        testParams("-s", "-?", "-a", "-h");
        testParams("-?eh");

        // multiple long parameters
        testParams("-autorun", "-help");
        testParams("-autoshutdown", "-hide");

        // multiple single- and long parameters
        testParams("-autorun", "--h");
        testParams("--autoshutdown", "-?", "-s");
    }

    @Test
    public void mainTestValidParamsB() throws Exception {
        File temp = new File(System.getProperty("java.io.tmpdir")+"/~TEMP"+System.currentTimeMillis()+".xml");
        temp.createNewFile();
        testParams("-c", temp.getAbsolutePath());
        testParams("-config", temp.getAbsolutePath());
        temp.deleteOnExit();
    }
    */

    //TODO rewrite tests
}
