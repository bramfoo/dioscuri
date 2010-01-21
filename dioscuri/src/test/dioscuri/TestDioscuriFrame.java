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

import org.apache.commons.cli.ParseException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestDioscuriFrame {

    private void testParams(String... params) throws ParseException {
        List<String> paramList = new ArrayList<String>(Arrays.asList(params));
        paramList.add("-e"); // use -e to exit immediately after the command line parameters are tested
        new DioscuriFrame(paramList.toArray(new String[]{}));
    }

    @Test(expected=ParseException.class)  
    public void mainTestInvalidParamA() throws ParseException {
        testParams("-foo");
    }

    @Test(expected=ParseException.class)
    public void mainTestInvalidParamB() throws ParseException {
        testParams("-unknown");
    }

    @Test(expected=ParseException.class)
    public void mainTestInvalidParamC() throws ParseException {
        testParams("-H");
    }

    @Test(expected=ParseException.class)
    public void mainTestInvalidParamD() throws ParseException {
        testParams("-c", "aNoneExistingFile.xml");
    }

    @Test
    public void mainTestValidParamsA() throws ParseException {
        // single parameters
        testParams("-?");
        testParams("-h");
        testParams("-a");
        testParams("-s");

        // multiple single parameters
        testParams("-sha");
        testParams("-s", "-?", "-a", "-h");
        testParams("-?h");

        // long parameters
        testParams("-help");
        testParams("-hide");
        testParams("-autorun");
        testParams("-autoshutdown");

        // multiple long parameters
        testParams("-autorun", "-help");
        testParams("-autoshutdown", "-hide");
    }

    @Test
    public void mainTestValidParamsB() throws ParseException, IOException {
        File temp = new File(System.getProperty("java.io.tmpdir")+"/~TEMP"+System.currentTimeMillis()+".xml");
        temp.createNewFile();
        testParams("-c", temp.getAbsolutePath());
        testParams("-config", temp.getAbsolutePath());
        temp.deleteOnExit();
    }
}
