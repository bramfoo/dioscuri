/* $Revision$ $Date$ $Author$ 
 * 
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import dioscuri.exception.CommandException;

/**
 * Interface for user to interact with emulator
 */
public class IO {
    private String[] cmd;
    private String[] prevCmd;
    private int cmdPointer;
    /**
     *
     */
    public String imageFilename;
    private String[] arguments;

    // Logging
    private static Logger logger = Logger.getLogger("dioscuri.io");

    // Constructors

    /**
     * Class constructor
     */
    public IO() {
        cmd = null;
        prevCmd = null;
        cmdPointer = 0;
        imageFilename = "";
        arguments = null;
    }

    // Methods

    /**
     * Fetches data from input stream and returns it as a byte array.
     * 
     * @param filename
     * @return byte[] byte array containing machinecode
     * @throws IOException
     */
    public byte[] importBinaryStream(String filename) throws IOException {
        // open input stream
        BufferedInputStream bdis = new BufferedInputStream(new DataInputStream(
                new FileInputStream(new File(filename))));

        // read all bytes (as unsigned) in byte array
        byte[] byteArray = new byte[bdis.available()];
        bdis.read(byteArray, 0, byteArray.length);

        // Close input stream
        bdis.close();

        return byteArray;
    }

    /**
     * Exports data from emulator to the file system in given filename.
     * 
     * @param filename
     * @param data
     * @throws IOException
     */
    public void exportBinaryStream(String filename, String data)
            throws IOException {
        // open output stream
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(
                new File(filename)));

        // write all data to output stream
        dos.writeChars(data);

        // Close input stream
        dos.close();
    }

    /**
     * Gets input from standard input and returns it as string.
     * 
     * @return string containing input line
     */
    public String getInput() {
        // Read input string, return array of words
        BufferedReader stdin = new BufferedReader(new InputStreamReader(
                System.in));
        try {
            System.out.print(">"); // This avoids a CR at end of line of input
            return stdin.readLine();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to read input.");
        }
        return null;
    }

    /**
     * Sets given string to standard output.
     * 
     * @param string
     */
    public void setOutput(String string) {
        System.out.println(string);
    }

    /**
     * Parses an input string into commands along with their arguments, execute
     * a command if recognised, otherwise ignore.
     * 
     * @return
     */
    public int getCommand() {
        // Declare returnCommand
        int returnCommand = -1;
        arguments = null;

        // If no command in queue, fetch new one from input
        if (cmd == null) {
            // Get new input
            cmd = getInput().split(" ");
            cmdPointer = 0;
        }

        // Parse commands from input
        try {
            // Repeat previous command if 'enter' was pressed
            if (cmd[0].equals("")) {
                // Check if previous command exists.
                if (prevCmd != null) {
                    // Copy previous command into current command array
                    cmd = new String[prevCmd.length];
                    System.arraycopy(prevCmd, 0, cmd, 0, prevCmd.length);
                }
            }
            // Set prevCmd array for copy later
            prevCmd = new String[cmd.length];

            // Debug mode supports the following commands:
            // h = display debug help
            // s [<NUMBER>] = step through the execution process by 1 or number
            // steps
            // r = display contents of registers
            // m <start> [bytes] = display memory contents
            // d <MODULE> = show dump of all modules, or module specified by its
            // type

            // Commands in debug mode
            // Help command
            if (cmd[0].equals("h")) {
                // Help: h
                // Show help information

                // Check for valid arguments
                if (cmd.length > 1) {
                    throw new CommandException(
                            "Debug command h: wrong number of arguments supplied.\n Usage is: h");
                }

                returnCommand = Emulator.CMD_DEBUG_HELP;

                // Copy current command into previous, empty queue
                System.arraycopy(cmd, 0, prevCmd, 0, cmd.length);
                cmd = null;
            }

            else if (cmd[cmdPointer].equals("s")) {
                // Step: s [<NUMBER>]
                // Performs n steps in execution process, with n = 1 or n =
                // <NUMBER>

                // Check for valid arguments
                if (cmd.length > 2) {
                    throw new CommandException(
                            "Debug command s: wrong number of arguments supplied.\n Usage is: s [number].");
                }

                // Parse arguments supplied
                else if (cmd.length > 1) {
                    // Argument is number of instructions to be run
                    try {
                        // Set number in arguments array
                        arguments = new String[] { cmd[1] };

                        // Set emulator command
                        returnCommand = Emulator.CMD_DEBUG_STEP;

                        // Copy current command into previous, empty queue
                        System.arraycopy(cmd, 0, prevCmd, 0, cmd.length);
                        cmd = null;
                    } catch (NumberFormatException e) {
                        logger.log(Level.WARNING, e.getMessage());
                        returnCommand = Emulator.CMD_MISMATCH;

                        // Copy current command into previous, empty queue
                        System.arraycopy(cmd, 0, prevCmd, 0, cmd.length);
                        cmd = null;
                    }
                } else {
                    returnCommand = Emulator.CMD_DEBUG_STEP;

                    // Copy current command into previous, empty queue
                    System.arraycopy(cmd, 0, prevCmd, 0, cmd.length);
                    cmd = null;
                }

            }

            // Debug command show registers
            else if (cmd[0].equals("r")) {
                // ShowRegisters: r <CPU>
                // Simple display of relevant CPU registers

                // Check for valid arguments
                if (cmd.length > 1) {
                    throw new CommandException(
                            "Debug command r: wrong number of arguments supplied.\n Usage is: r");
                }

                returnCommand = Emulator.CMD_DEBUG_SHOWREG;

                // Copy current command into previous, empty queue
                System.arraycopy(cmd, 0, prevCmd, 0, cmd.length);
                cmd = null;
            }

            // Debug command dump
            else if (cmd[0].equals("d")) {
                // Dump: d <MODULE>
                // Displays a dump of all modules, or only from one given
                // moduletype

                // Check for valid arguments
                if (cmd.length != 2) {
                    throw new CommandException(
                            "Debug command d: wrong number of arguments supplied.\n Usage is: d <MODULE>.");
                }

                arguments = new String[] { cmd[1] };
                returnCommand = Emulator.CMD_DEBUG_DUMP;

                // Copy current command into previous, empty queue
                System.arraycopy(cmd, 0, prevCmd, 0, cmd.length);
                cmd = null;
            }

            // Debug command dump
            else if (cmd[0].equals("m")) {
                // m <address> [bytes]
                // Displays the contents of the memory at <address>, [bytes]
                // long

                // Check for valid arguments
                if (cmd.length < 2 || cmd.length > 3) {
                    throw new CommandException(
                            "Debug command m: wrong number of arguments supplied.\n Usage is: m <address> [bytes].");
                }

                // Parse arguments supplied
                if (cmd.length == 3) {
                    arguments = new String[] { cmd[1], cmd[2] };
                } else {
                    arguments = new String[] { cmd[1] };
                }
                returnCommand = Emulator.CMD_DEBUG_MEM_DUMP;

                // Copy current command into previous, empty queue
                System.arraycopy(cmd, 0, prevCmd, 0, cmd.length);
                cmd = null;
            }

            // Check if input resulted in a command match
            if (returnCommand == -1) {
                throw new CommandException("Syntax error.");
            }
        } catch (CommandException e) {
            // Probably wrong syntax, send special command to emulator
            logger.log(Level.WARNING, e.getMessage());
            returnCommand = Emulator.CMD_MISMATCH;

            // Copy current command into previous, empty queue
            System.arraycopy(cmd, 0, prevCmd, 0, cmd.length);
            cmd = null;
        }

        // Increment pointer
        cmdPointer++;

        // Return the command
        return returnCommand;
    }

    /**
     * Returns arguments which may have been set during getCommand
     * 
     * @return string[] with arguments
     */
    public String[] getArguments() {
        return arguments;
    }

    /**
     * Shows help information of emulator to output
     */
    void showHelp() {
        // Show helpfile
        logger.log(Level.SEVERE, this.toString());
    }

    /**
     * Returns a string representation of this class
     * 
     * @return string with help information
     */
    @Override
    public String toString() {
        // String of this class
        String info = "Emulator help" + "\r\n";
        info += "--------------------------------------------------------------------------------------------------"
                + "\r\n";
        info += "In debug mode, the following commands may be used:" + "\r\n";
        info += "h                      = show this help information" + "\r\n";
        info += "r                      = show current contents of all CPU registers"
                + "\r\n";
        info += "s [NUMBER]             = step through the execution process [NUMBER] steps"
                + "\r\n";
        info += "                         (default is 1 step)" + "\r\n";
        info += "m <address> [NUMBER]   = show contents of [NUMBER] bytes at memory location <address>"
                + "\r\n";
        info += "                         (default is 2 bytes)" + "\r\n";
        info += "d <MODULE>             = show current contents of <MODULE>"
                + "\r\n";
        info += "                         valid modules are  ata, bios, clock, cpu, dma, fdc, keyboard,"
                + "\r\n";
        info += "                         memory, motherboard, parallelport, pic, pit, screen, serialport, vga"
                + "\r\n";
        info += "--------------------------------------------------------------------------------------------------"
                + "\r\n";

        return info;
    }

}
